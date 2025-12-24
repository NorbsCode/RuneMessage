/*
 * Copyright (c) 2025, LordStrange
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.runemessages;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Model;
import net.runelite.api.Player;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.ChatMessageType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "RuneMessages",
	description = "Leave messages for other players around the world",
	tags = {"rune", "message", "note", "sign", "communication"}
)
public class RuneMessagesPlugin extends Plugin
{
	private static final String VOTE_UP_OPTION = "Vote Up";
	private static final String VOTE_DOWN_OPTION = "Vote Down";
	private static final String EXAMINE_OPTION = "Examine";
	private static final String REPORT_OPTION = "Report";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private RuneMessagesConfig config;

	@Inject
	private RuneMessagesService messageService;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RuneMessagesOverlay overlay;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ConfigManager configManager;

	private static final String CONFIG_GROUP = "runemessages";
	private static final int MAX_MESSAGES_PER_REGION = 30;
	private static final int TOP_VOTED_COUNT = 10;
	private static final int MAX_AUTHOR_MESSAGES_PER_REGION = 1;
	private static final int MAX_AUTHOR_MESSAGES_PER_WORLD = 5;

	@Getter
	private final Map<String, RuneLiteObject> spawnedGraves = new ConcurrentHashMap<>();

	@Getter
	private final Map<String, MessageData> messageDataMap = new ConcurrentHashMap<>();

	private final Set<Integer> loadedRegions = new HashSet<>();
	private final Set<String> votedMessages = new HashSet<>();
	private final Set<String> reportedMessages = new HashSet<>();

	// Cache of all messages per world+region (persists until logout)
	// Key format: "worldId:regionId"
	private final Map<String, List<MessageData>> regionMessageCache = new ConcurrentHashMap<>();

	// Track occupied tile locations to prevent duplicate messages at same spot
	private final Set<String> occupiedLocations = new HashSet<>();

	private RuneMessagesPanel messagePanel;
	private NavigationButton navButton;
	private boolean panelVisible = false;
	private boolean registrationAttempted = false;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);

		// Load API key from config
		String savedApiKey = config.apiKey();
		if (savedApiKey != null && !savedApiKey.isEmpty())
		{
			messageService.setApiKey(savedApiKey);
			log.debug("Loaded API key from config");
		}

		messagePanel = new RuneMessagesPanel(this);

		// Use a simple placeholder icon (16x16 gray square with darker border)
		BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = icon.createGraphics();
		g.setColor(new java.awt.Color(80, 80, 80));
		g.fillRect(0, 0, 16, 16);
		g.setColor(new java.awt.Color(60, 60, 60));
		g.drawRect(0, 0, 15, 15);
		g.setColor(new java.awt.Color(200, 180, 100)); // Gold-ish cross
		g.drawLine(8, 3, 8, 12);
		g.drawLine(5, 6, 11, 6);
		g.dispose();

		navButton = NavigationButton.builder()
			.tooltip("RuneMessages")
			.icon(icon)
			.priority(10)
			.panel(messagePanel)
			.build();

		// Always show the navigation button
		clientToolbar.addNavigation(navButton);

		log.info("RuneMessages plugin started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);

		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
		}

		clearAllGraves();
		loadedRegions.clear();
		messageDataMap.clear();
		votedMessages.clear();
		reportedMessages.clear();
		regionMessageCache.clear();
		occupiedLocations.clear();

		log.info("RuneMessages plugin stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
			event.getGameState() == GameState.HOPPING)
		{
			clearAllGraves();
			loadedRegions.clear();
			messageDataMap.clear();
			regionMessageCache.clear();
			occupiedLocations.clear();
			registrationAttempted = false; // Allow re-registration on next login
			// Keep votedMessages and reportedMessages across hops to prevent abuse
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// Try to register if we haven't already
		if (!registrationAttempted)
		{
			// Check if we need to register: no API key OR username mismatch
			boolean needsRegistration = messageService.getApiKey() == null || messageService.getApiKey().isEmpty();

			if (!needsRegistration)
			{
				// Have API key, but check if username matches current player
				Player localPlayer = client.getLocalPlayer();
				if (localPlayer != null && localPlayer.getName() != null)
				{
					String savedUsername = config.registeredUsername();
					if (!savedUsername.isEmpty() && !savedUsername.equalsIgnoreCase(localPlayer.getName()))
					{
						// Different player logged in - need to re-register
						needsRegistration = true;
						log.info("Different player detected (saved: {}, current: {}), re-registering",
							savedUsername, localPlayer.getName());
					}
				}
			}

			if (needsRegistration)
			{
				registrationAttempted = true;
				tryRegister();
			}
			else
			{
				// API key valid and username matches - no need to register
				registrationAttempted = true;
			}
		}

		// Check for new regions and load messages
		int[] regions = client.getMapRegions();
		if (regions == null)
		{
			return;
		}

		int worldId = client.getWorld();

		// Convert to set for easy lookup
		Set<Integer> currentRegions = new HashSet<>();
		for (int r : regions)
		{
			currentRegions.add(r);
		}

		// Clear regions that are no longer visible - this allows fresh loading when returning
		Set<Integer> regionsToRemove = new HashSet<>();
		for (Integer loadedRegion : loadedRegions)
		{
			if (!currentRegions.contains(loadedRegion))
			{
				regionsToRemove.add(loadedRegion);
			}
		}
		for (Integer regionToRemove : regionsToRemove)
		{
			loadedRegions.remove(regionToRemove);
			// Also clear the cache for this region
			String cacheKey = worldId + ":" + regionToRemove;
			regionMessageCache.remove(cacheKey);
		}

		// Clean up messages from regions no longer in view
		cleanupDistantMessages(currentRegions);

		// Load new regions
		for (int region : regions)
		{
			if (!loadedRegions.contains(region))
			{
				loadedRegions.add(region);
				loadMessagesForRegion(worldId, region);
			}
		}

		// Re-spawn graves that went out of view and came back
		refreshGraves();
	}

	/**
	 * Remove messages from regions that are no longer in view
	 */
	private void cleanupDistantMessages(Set<Integer> currentRegions)
	{
		List<String> messagesToRemove = new ArrayList<>();

		for (Map.Entry<String, MessageData> entry : messageDataMap.entrySet())
		{
			MessageData message = entry.getValue();
			if (!currentRegions.contains(message.getRegionId()))
			{
				messagesToRemove.add(entry.getKey());
			}
		}

		for (String messageId : messagesToRemove)
		{
			// Deactivate and remove the grave
			RuneLiteObject grave = spawnedGraves.remove(messageId);
			if (grave != null)
			{
				grave.setActive(false);
			}

			// Remove from message data and occupied locations
			MessageData data = messageDataMap.remove(messageId);
			if (data != null)
			{
				String locationKey = data.getX() + "," + data.getY() + "," + data.getPlane();
				occupiedLocations.remove(locationKey);
			}
		}

		if (!messagesToRemove.isEmpty())
		{
			log.debug("Cleaned up {} messages from distant regions", messagesToRemove.size());
		}
	}

	private void refreshGraves()
	{
		for (Map.Entry<String, MessageData> entry : messageDataMap.entrySet())
		{
			String messageId = entry.getKey();
			MessageData message = entry.getValue();

			RuneLiteObject existingGrave = spawnedGraves.get(messageId);

			// Check if grave needs to be re-spawned
			WorldPoint worldPoint = new WorldPoint(message.getX(), message.getY(), message.getPlane());
			LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

			if (localPoint == null)
			{
				// Location not in view - deactivate if active
				if (existingGrave != null && existingGrave.isActive())
				{
					existingGrave.setActive(false);
				}
			}
			else
			{
				// Location is in view
				if (existingGrave == null || !existingGrave.isActive())
				{
					// Need to re-spawn
					if (existingGrave != null)
					{
						existingGrave.setActive(false);
						spawnedGraves.remove(messageId);
					}
					spawnGrave(message);
				}
				else
				{
					// Grave exists and is active - update location in case it shifted
					existingGrave.setLocation(localPoint, message.getPlane());
				}
			}
		}
	}

	private void tryRegister()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null)
		{
			registrationAttempted = false; // Retry later
			return;
		}

		String username = localPlayer.getName();
		String savedUsername = config.registeredUsername();

		// Check if already registered with a different username
		if (!savedUsername.isEmpty() && !savedUsername.equalsIgnoreCase(username))
		{
			// Different user - need to re-register
			log.info("Different user detected, re-registering for RuneMessages");
		}

		log.info("Registering for RuneMessages as: {}", username);

		messageService.register(username)
			.thenAccept(apiKey -> {
				// Save to config
				configManager.setConfiguration(CONFIG_GROUP, "apiKey", apiKey);
				configManager.setConfiguration(CONFIG_GROUP, "registeredUsername", username);
				log.info("Successfully registered for RuneMessages");

				clientThread.invokeLater(() -> {
					sendChatMessage("RuneMessages: Registered successfully!");
					if (messagePanel != null)
					{
						messagePanel.updateAccountInfo();
					}
				});
			})
			.exceptionally(ex -> {
				log.warn("Failed to register for RuneMessages: {}", ex.getMessage());
				clientThread.invokeLater(() -> {
					sendChatMessage("RuneMessages: Registration failed. Messages will be local only.");
				});
				return null;
			});
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// Add menu options for spawned messages (right-click on ground near message)
		addMessageMenuOptions(event);
	}

	private void addMessageMenuOptions(MenuEntryAdded event)
	{
		// Check for WALK menu action (like ground markers plugin does)
		MenuAction menuAction = event.getMenuEntry().getType();
		if (menuAction != MenuAction.WALK)
		{
			return;
		}

		// Get the world view
		int worldViewId = event.getMenuEntry().getWorldViewId();
		WorldView wv = client.getWorldView(worldViewId);
		if (wv == null)
		{
			return;
		}

		// Get the selected/hovered tile
		Tile selectedTile = wv.getSelectedSceneTile();
		if (selectedTile == null)
		{
			return;
		}

		// Get the world point of the selected tile
		WorldPoint selectedWorldPoint = selectedTile.getWorldLocation();
		if (selectedWorldPoint == null)
		{
			return;
		}

		// Check if we already added message options
		MenuEntry[] entries = client.getMenuEntries();
		for (MenuEntry entry : entries)
		{
			if (EXAMINE_OPTION.equals(entry.getOption()) && entry.getTarget().contains("'s Message"))
			{
				return; // Already added
			}
		}

		// Find message at this tile using world coordinates
		MessageData nearbyMessage = findMessageAtWorldPoint(selectedWorldPoint);
		if (nearbyMessage == null)
		{
			return;
		}

		String messageTarget = "<col=ffff00>" + nearbyMessage.getAuthor() + "'s Message</col>";

		// Add Examine option
		client.createMenuEntry(-1)
			.setOption(EXAMINE_OPTION)
			.setTarget(messageTarget)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> examineMessage(nearbyMessage));

		// Add Vote Up option
		client.createMenuEntry(-1)
			.setOption(VOTE_UP_OPTION)
			.setTarget(messageTarget)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> voteMessage(nearbyMessage, true));

		// Add Vote Down option
		client.createMenuEntry(-1)
			.setOption(VOTE_DOWN_OPTION)
			.setTarget(messageTarget)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> voteMessage(nearbyMessage, false));

		// Add Report option
		client.createMenuEntry(-1)
			.setOption(REPORT_OPTION)
			.setTarget(messageTarget)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> reportMessage(nearbyMessage));
	}

	private MessageData findMessageAtWorldPoint(WorldPoint worldPoint)
	{
		// Check each message to see if it's at this world point
		for (Map.Entry<String, MessageData> entry : messageDataMap.entrySet())
		{
			MessageData message = entry.getValue();
			if (message == null)
			{
				continue;
			}

			// Compare world coordinates directly
			if (message.getX() == worldPoint.getX() &&
				message.getY() == worldPoint.getY() &&
				message.getPlane() == worldPoint.getPlane())
			{
				return message;
			}
		}

		return null;
	}

	private void examineMessage(MessageData message)
	{
		if (message == null)
		{
			return;
		}

		java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MMM/yyyy");
		String dateStr = dateFormat.format(new java.util.Date(message.getTimestamp()));

		String chatMessage = new ChatMessageBuilder()
			.append("Message from ")
			.append(ColorScheme.PROGRESS_COMPLETE_COLOR, message.getAuthor())
			.append(" on " + dateStr)
			.append(": \"")
			.append(ColorScheme.GRAND_EXCHANGE_ALCH, message.getMessage())
			.append("\"")
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}

	private void voteMessage(MessageData message, boolean thumbsUp)
	{
		if (message == null)
		{
			return;
		}

		// Check if already voted on this message
		if (votedMessages.contains(message.getId()))
		{
			String chatMsg = new ChatMessageBuilder()
				.append(ColorScheme.PROGRESS_ERROR_COLOR, "You have already voted on this message.")
				.build();

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMsg)
				.build());
			return;
		}

		messageService.rateMessage(message, thumbsUp)
			.thenRun(() ->
			{
				votedMessages.add(message.getId());
				String voteType = thumbsUp ? "up" : "down";
				log.info("Voted {} on message: {}", voteType, message.getId());

				String chatMsg = new ChatMessageBuilder()
					.append(ColorScheme.PROGRESS_COMPLETE_COLOR, "Vote recorded!")
					.build();

				chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.CONSOLE)
					.runeLiteFormattedMessage(chatMsg)
					.build());
			})
			.exceptionally(ex ->
			{
				log.warn("Failed to vote on message: {}", ex.getMessage());
				return null;
			});
	}

	private void reportMessage(MessageData message)
	{
		if (message == null)
		{
			return;
		}

		// Check if already reported this message
		if (reportedMessages.contains(message.getId()))
		{
			String chatMsg = new ChatMessageBuilder()
				.append(ColorScheme.PROGRESS_ERROR_COLOR, "You have already reported this message.")
				.build();

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(chatMsg)
				.build());
			return;
		}

		// Get reporter's name
		Player localPlayer = client.getLocalPlayer();
		String reporterName = localPlayer != null ? localPlayer.getName() : "Unknown";

		messageService.reportMessage(message, reporterName)
			.thenRun(() ->
			{
				reportedMessages.add(message.getId());

				String chatMsg = new ChatMessageBuilder()
					.append(ColorScheme.PROGRESS_ERROR_COLOR, "Message has been reported.")
					.build();

				chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.CONSOLE)
					.runeLiteFormattedMessage(chatMsg)
					.build());
			})
			.exceptionally(ex ->
			{
				log.warn("Failed to report message: {}", ex.getMessage());
				return null;
			});
	}

	/**
	 * Called from the message panel when user submits a message
	 */
	public void submitMessage(String message, MarkerType markerType)
	{
		if (message == null || message.trim().isEmpty())
		{
			return;
		}

		final String trimmedMessage = message.trim();
		final MarkerType selectedMarker = markerType != null ? markerType : MarkerType.NOTE;

		// Must run on client thread to access game state
		clientThread.invokeLater(() ->
		{
			Player localPlayer = client.getLocalPlayer();
			if (localPlayer == null)
			{
				sendChatMessage("You must be logged in to write a message!");
				return;
			}

			WorldPoint location = localPlayer.getWorldLocation();
			String author = localPlayer.getName();
			int worldId = client.getWorld();

			saveAndSpawnMessage(trimmedMessage, location, author, worldId, selectedMarker);
		});
	}

	/**
	 * Get current world ID (for panel to fetch messages)
	 */
	public int getCurrentWorldId()
	{
		return client.getWorld();
	}

	/**
	 * Get current player name
	 */
	public String getCurrentPlayerName()
	{
		Player localPlayer = client.getLocalPlayer();
		return localPlayer != null ? localPlayer.getName() : null;
	}

	/**
	 * Get the message service for fetching player's messages
	 */
	public RuneMessagesService getMessageService()
	{
		return messageService;
	}

	/**
	 * Close/hide the message panel (called from cancel button)
	 */
	public void closeMessagePanel()
	{
		// Panel stays in navigation, just clears the input via cancel()
		// No additional action needed as the navButton keeps panel accessible
	}

	/**
	 * Remove a message from the world (called after successful API delete)
	 */
	public void removeMessage(String messageId)
	{
		clientThread.invokeLater(() -> {
			// Remove the grave object
			RuneLiteObject grave = spawnedGraves.remove(messageId);
			if (grave != null)
			{
				grave.setActive(false);
			}

			// Remove from message data
			MessageData data = messageDataMap.remove(messageId);

			// Remove from occupied locations
			if (data != null)
			{
				String locationKey = data.getX() + "," + data.getY() + "," + data.getPlane();
				occupiedLocations.remove(locationKey);
			}

			// Remove from region cache
			for (List<MessageData> messages : regionMessageCache.values())
			{
				messages.removeIf(m -> m.getId().equals(messageId));
			}

			sendChatMessage("Message deleted successfully!");
		});
	}

	private void saveAndSpawnMessage(String message, WorldPoint location, String author, int worldId, MarkerType markerType)
	{
		int modelId = markerType.getModelId();
		String locationKey = location.getX() + "," + location.getY() + "," + location.getPlane();

		// Check if there's already a message at this location locally
		if (occupiedLocations.contains(locationKey))
		{
			sendChatMessage("You cannot place a message here - this spot is already taken!");
			return;
		}

		if (config.syncMessages())
		{
			// Check if we have an API key
			if (messageService.getApiKey() == null || messageService.getApiKey().isEmpty())
			{
				sendChatMessage("Not registered yet. Please wait for registration to complete.");
				return;
			}

			// Save via API - it handles rate limiting and returns errors
			messageService.saveMessage(location, message.trim(), author, worldId, modelId)
				.thenAccept(data ->
				{
					clientThread.invokeLater(() ->
					{
						if (spawnGrave(data))
						{
							messageDataMap.put(data.getId(), data);
							occupiedLocations.add(locationKey);
							sendChatMessage("Message placed successfully!");
						}
					});
				})
				.exceptionally(ex ->
				{
					String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
					log.warn("Failed to save message: {}", errorMsg);
					clientThread.invokeLater(() ->
						sendChatMessage(errorMsg)
					);
					return null;
				});
		}
		else
		{
			// Local only - no limits
			MessageData localData = MessageData.builder()
				.id("local-" + System.currentTimeMillis())
				.author(author)
				.message(message.trim())
				.x(location.getX())
				.y(location.getY())
				.plane(location.getPlane())
				.worldId(worldId)
				.regionId(location.getRegionID())
				.timestamp(System.currentTimeMillis())
				.modelId(modelId)
				.build();
			if (spawnGrave(localData))
			{
				messageDataMap.put(localData.getId(), localData);
				occupiedLocations.add(locationKey);
				sendChatMessage("Message placed locally (sync disabled).");
			}
		}
	}

	private void sendChatMessage(String message)
	{
		String chatMessage = new ChatMessageBuilder()
			.append(ColorScheme.PROGRESS_COMPLETE_COLOR, "[RuneMessages] ")
			.append(message)
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}

	private void loadMessagesForRegion(int worldId, int regionId)
	{
		if (!config.showOtherMessages())
		{
			return;
		}

		// Always fetch fresh from API - this ensures random selection each visit
		messageService.getMessagesForRegion(worldId, regionId)
			.thenAccept(messages ->
			{
				// Store temporarily for spawning
				String cacheKey = worldId + ":" + regionId;
				regionMessageCache.put(cacheKey, new ArrayList<>(messages));

				clientThread.invokeLater(() -> spawnMessagesFromCache(worldId, regionId));
			})
			.exceptionally(ex ->
			{
				log.warn("Failed to load messages for region {}: {}", regionId, ex.getMessage());
				return null;
			});
	}

	private void spawnMessagesFromCache(int worldId, int regionId)
	{
		String cacheKey = worldId + ":" + regionId;
		List<MessageData> allMessages = regionMessageCache.get(cacheKey);
		if (allMessages == null || allMessages.isEmpty())
		{
			return;
		}

		Player localPlayer = client.getLocalPlayer();
		String localPlayerName = localPlayer != null ? localPlayer.getName() : "";

		// Separate own messages from others - own messages ALWAYS load first
		List<MessageData> ownMessages = new ArrayList<>();
		List<MessageData> otherMessages = new ArrayList<>();

		for (MessageData msg : allMessages)
		{
			// CRITICAL: Verify message belongs to this world
			if (msg.getWorldId() != worldId)
			{
				log.warn("Message {} has worldId {} but expected {}",
					msg.getId(), msg.getWorldId(), worldId);
				continue;
			}
			// CRITICAL: Verify message belongs to this region
			if (msg.getRegionId() != regionId)
			{
				log.warn("Message {} has regionId {} but expected {}",
					msg.getId(), msg.getRegionId(), regionId);
				continue;
			}
			// Skip already spawned
			if (spawnedGraves.containsKey(msg.getId()))
			{
				continue;
			}

			// Separate own vs other messages
			if (msg.getAuthor().equalsIgnoreCase(localPlayerName))
			{
				if (config.showOwnMessages())
				{
					ownMessages.add(msg);
				}
			}
			else
			{
				otherMessages.add(msg);
			}
		}

		// ALWAYS spawn player's own messages first (no limit)
		int ownSpawned = 0;
		for (MessageData msg : ownMessages)
		{
			String locationKey = msg.getX() + "," + msg.getY() + "," + msg.getPlane();
			if (occupiedLocations.contains(locationKey))
			{
				continue;
			}
			if (spawnGrave(msg))
			{
				messageDataMap.put(msg.getId(), msg);
				occupiedLocations.add(locationKey);
				ownSpawned++;
			}
		}

		// Now handle other players' messages with the voting algorithm
		if (otherMessages.isEmpty() || !config.showOtherMessages())
		{
			if (ownSpawned > 0)
			{
				log.debug("Loaded {} own messages for region {}", ownSpawned, regionId);
			}
			return;
		}

		// Sort by vote score (thumbsUp - thumbsDown) descending
		otherMessages.sort(Comparator.comparingInt(
			(MessageData m) -> m.getThumbsUp() - m.getThumbsDown()
		).reversed());

		// Take top 10 voted
		List<MessageData> topVoted = otherMessages.stream()
			.limit(TOP_VOTED_COUNT)
			.collect(Collectors.toList());

		// Get remaining messages (after top 10)
		List<MessageData> remaining = otherMessages.stream()
			.skip(TOP_VOTED_COUNT)
			.collect(Collectors.toList());

		// Shuffle the remaining and take up to (MAX - top10) random ones
		Collections.shuffle(remaining);
		int randomCount = Math.min(remaining.size(), MAX_MESSAGES_PER_REGION - topVoted.size());
		List<MessageData> randomPicks = remaining.stream()
			.limit(randomCount)
			.collect(Collectors.toList());

		// Combine: top voted + random picks
		List<MessageData> messagesToSpawn = new ArrayList<>();
		messagesToSpawn.addAll(topVoted);
		messagesToSpawn.addAll(randomPicks);

		// Spawn messages, avoiding duplicate locations
		int othersSpawned = 0;
		for (MessageData msg : messagesToSpawn)
		{
			String locationKey = msg.getX() + "," + msg.getY() + "," + msg.getPlane();

			// Skip if location already has a message
			if (occupiedLocations.contains(locationKey))
			{
				continue;
			}

			// Spawn the grave
			if (spawnGrave(msg))
			{
				messageDataMap.put(msg.getId(), msg);
				occupiedLocations.add(locationKey);
				othersSpawned++;
			}
		}

		if (ownSpawned > 0 || othersSpawned > 0)
		{
			log.debug("Loaded {} own + {} others for region {} (top {} voted + {} random)",
				ownSpawned, othersSpawned, regionId,
				Math.min(topVoted.size(), othersSpawned),
				Math.max(0, othersSpawned - topVoted.size()));
		}
	}

	private boolean spawnGrave(MessageData message)
	{
		WorldPoint worldPoint = new WorldPoint(message.getX(), message.getY(), message.getPlane());

		// Validate that the world point's region matches the stored region
		int computedRegion = worldPoint.getRegionID();
		if (computedRegion != message.getRegionId())
		{
			log.warn("Message {} coordinates ({},{}) compute to region {} but stored regionId is {}",
				message.getId(), message.getX(), message.getY(), computedRegion, message.getRegionId());
			return false;
		}

		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

		if (localPoint == null)
		{
			log.debug("Cannot spawn grave - location not in view: {}", worldPoint);
			return false;
		}

		RuneLiteObject grave = client.createRuneLiteObject();
		Model model = client.loadModel(message.getModelId());

		if (model == null)
		{
			log.warn("Failed to load grave model {}", message.getModelId());
			return false;
		}

		grave.setModel(model);
		grave.setLocation(localPoint, message.getPlane());
		grave.setActive(true);

		spawnedGraves.put(message.getId(), grave);
		log.debug("Spawned grave for message {} at {}", message.getId(), worldPoint);
		return true;
	}

	private void despawnGrave(String messageId)
	{
		RuneLiteObject grave = spawnedGraves.remove(messageId);
		if (grave != null)
		{
			grave.setActive(false);
		}
		messageDataMap.remove(messageId);
	}

	private void clearAllGraves()
	{
		for (RuneLiteObject grave : spawnedGraves.values())
		{
			grave.setActive(false);
		}
		spawnedGraves.clear();
	}

	@Provides
	RuneMessagesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneMessagesConfig.class);
	}
}
