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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class RuneMessagesOverlay extends Overlay
{
	private static final Color MESSAGE_COLOR = new Color(255, 215, 0);
	private static final Color AUTHOR_COLOR = new Color(180, 180, 180);
	private static final Color SHADOW_COLOR = new Color(0, 0, 0, 180);
	private static final Color THUMBS_UP_COLOR = new Color(50, 205, 50);
	private static final Color THUMBS_DOWN_COLOR = new Color(220, 20, 60);

	private final Client client;
	private final RuneMessagesPlugin plugin;
	private final RuneMessagesConfig config;

	@Inject
	public RuneMessagesOverlay(Client client, RuneMessagesPlugin plugin, RuneMessagesConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return null;
		}

		WorldPoint playerLocation = localPlayer.getWorldLocation();
		int displayRadius = config.displayRadius();

		for (Map.Entry<String, RuneLiteObject> entry : plugin.getSpawnedGraves().entrySet())
		{
			RuneLiteObject grave = entry.getValue();
			if (grave == null || !grave.isActive())
			{
				continue;
			}

			MessageData message = plugin.getMessageDataMap().get(entry.getKey());
			if (message == null)
			{
				continue;
			}

			WorldPoint messageWorldPoint = new WorldPoint(message.getX(), message.getY(), message.getPlane());

			LocalPoint expectedLocal = LocalPoint.fromWorld(client, messageWorldPoint);
			if (expectedLocal == null)
			{
				continue;
			}

			int distance = playerLocation.distanceTo(messageWorldPoint);
			if (distance > displayRadius)
			{
				continue;
			}

			renderMessage(graphics, expectedLocal, message, message.getPlane());
		}

		return null;
	}

	private void renderMessage(Graphics2D graphics, LocalPoint location, MessageData message, int plane)
	{
		Point point = Perspective.localToCanvas(client, location, plane, 150);
		if (point == null)
		{
			return;
		}

		String messageText = "\"" + message.getMessage() + "\"";
		String authorText = "- " + message.getAuthor();

		FontMetrics metrics = graphics.getFontMetrics();
		int messageWidth = metrics.stringWidth(messageText);
		int authorWidth = metrics.stringWidth(authorText);
		int maxWidth = Math.max(messageWidth, authorWidth);

		int x = point.getX() - maxWidth / 2;
		int y = point.getY();

		graphics.setColor(SHADOW_COLOR);
		graphics.drawString(messageText, x + 1, y + 1);
		graphics.drawString(messageText, x - 1, y - 1);
		graphics.drawString(messageText, x + 1, y - 1);
		graphics.drawString(messageText, x - 1, y + 1);

		graphics.setColor(MESSAGE_COLOR);
		graphics.drawString(messageText, x, y);

		int authorX = point.getX() - authorWidth / 2;
		int authorY = y + metrics.getHeight() + 2;

		graphics.setColor(SHADOW_COLOR);
		graphics.drawString(authorText, authorX + 1, authorY + 1);

		graphics.setColor(AUTHOR_COLOR);
		graphics.drawString(authorText, authorX, authorY);

		int ratingsY = authorY + metrics.getHeight() + 2;
		String thumbsUpText = "\u25B2 " + message.getThumbsUp();
		String thumbsDownText = "\u25BC " + message.getThumbsDown();

		int thumbsUpWidth = metrics.stringWidth(thumbsUpText);
		int thumbsDownWidth = metrics.stringWidth(thumbsDownText);
		int totalRatingsWidth = thumbsUpWidth + 15 + thumbsDownWidth;
		int ratingsX = point.getX() - totalRatingsWidth / 2;

		graphics.setColor(SHADOW_COLOR);
		graphics.drawString(thumbsUpText, ratingsX + 1, ratingsY + 1);
		graphics.setColor(THUMBS_UP_COLOR);
		graphics.drawString(thumbsUpText, ratingsX, ratingsY);

		int thumbsDownX = ratingsX + thumbsUpWidth + 15;
		graphics.setColor(SHADOW_COLOR);
		graphics.drawString(thumbsDownText, thumbsDownX + 1, ratingsY + 1);
		graphics.setColor(THUMBS_DOWN_COLOR);
		graphics.drawString(thumbsDownText, thumbsDownX, ratingsY);
	}
}
