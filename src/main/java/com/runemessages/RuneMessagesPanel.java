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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class RuneMessagesPanel extends PluginPanel
{
	private static final int MAX_MESSAGE_LENGTH = 100;
	private static final Map<String, String[]> WORD_CATEGORIES = new LinkedHashMap<>();

	private JLabel apiKeyValueLabel;
	private JLabel usernameValueLabel;

	static
	{
		WORD_CATEGORIES.put("Actions", new String[] {
			"Try", "Beware of", "Watch out for", "Be wary of", "Seek", "Find", "Avoid",
			"Attack", "Don't attack", "Jump", "Roll", "Run", "Walk", "Sneak", "Hide",
			"Look", "Listen", "Wait", "Stop", "Go", "Turn", "Climb", "Fall", "Die",
			"Kill", "Slay", "Defeat", "Flee", "Escape", "Enter", "Exit", "Open", "Close",
			"Pull", "Push", "Grab", "Drop", "Use", "Equip", "Unequip", "Eat", "Drink",
			"Pray", "Teleport", "Bank", "Trade", "Buy", "Sell", "Steal", "Pickpocket",
			"Mine", "Chop", "Fish", "Cook", "Craft", "Smith", "Fletch", "Alch", "Splash",
			"Hop", "Log out", "Touch grass", "Git gud", "Panic buy", "Panic sell",
			"Trim armour", "Double money", "Trust trade", "Skull trick", "Lure"
		});
		WORD_CATEGORIES.put("Directions", new String[] {
			"ahead", "behind", "left", "right", "up", "down", "above", "below",
			"north", "south", "east", "west", "nearby", "far away", "around the corner",
			"through the door", "past the gate", "over there", "here", "somewhere",
			"in the corner", "at the edge", "in the middle", "on the path", "off the path",
			"underground", "upstairs", "downstairs", "inside", "outside", "beyond",
			"at Lumbridge", "at Varrock", "at Falador", "at Camelot", "at Ardougne",
			"in the Wilderness", "at the G.E.", "at the Grand Exchange", "Grand Exchange",
			"at the bank", "in the dungeon", "at the altar", "by the fountain",
			"near the tree", "by the rocks"
		});
		WORD_CATEGORIES.put("Things", new String[] {
			"enemy", "enemies", "boss", "monster", "creature", "NPC", "player",
			"treasure", "loot", "gold", "coins", "GP", "item", "drop", "rare drop",
			"weapon", "armour", "shield", "helmet", "sword", "bow", "staff", "wand",
			"rune", "runes", "bones", "ashes", "food", "potion", "prayer pot",
			"teleport", "portal", "door", "gate", "lever", "ladder", "stairs",
			"chest", "crate", "barrel", "rock", "tree", "bush", "flower",
			"trap", "pit", "spikes", "poison", "fire", "water", "lava",
			"dragon", "demon", "goblin", "guard", "wizard", "knight", "king", "queen",
			"cow", "chicken", "rat", "spider", "snake", "wolf", "bear", "unicorn",
			"slayer task", "clue scroll", "pet", "skilling pet", "boss pet",
			"ironman", "HCIM", "UIM", "GIM", "pure", "main", "alt", "bot",
			"noob", "pro", "chad", "legend", "king", "absolute unit"
		});
		WORD_CATEGORIES.put("OSRS", new String[] {
			"RNG", "RNGesus", "XP", "XP waste", "efficiency", "tick perfect",
			"one tick", "two tick", "three tick", "prayer flick", "lazy flick",
			"spec", "DPS", "max hit", "accuracy", "defence", "prayer",
			"Bandos", "Armadyl", "Zamorak", "Saradomin", "Guthix", "Zaros",
			"AGS", "BGS", "SGS", "ZGS", "Godsword", "Scythe", "Tbow", "Bowfa",
			"Blowpipe", "Whip", "Tentacle", "Rapier", "Mace", "Fang",
			"Torva", "Ancestral", "Masori", "Virtus", "Justiciar",
			"Fire cape", "Infernal cape", "Assembler", "Imbued heart",
			"Zenyte", "Onyx", "Dragonstone", "Diamond", "Ruby", "Sapphire",
			"Zulrah", "Vorkath", "Hydra", "Cerberus", "Sire", "Kraken",
			"TOB", "COX", "TOA", "Inferno", "Fight caves", "Gauntlet",
			"Slayer", "Konar", "Duradel", "Nieve", "Turael", "Krystilia",
			"Runecrafting", "Agility", "Mining", "Farming", "Hunter",
			"Bird house", "Herb run", "Tree run", "Birdhouse run",
			"Bond", "Membership", "F2P", "P2P", "Ironman btw", "BTW"
		});
		WORD_CATEGORIES.put("Feelings", new String[] {
			"danger", "safety", "victory", "defeat", "death", "life",
			"happiness", "sadness", "fear", "courage", "despair", "hope",
			"anger", "peace", "chaos", "order", "pain", "pleasure",
			"boredom", "excitement", "surprise", "shock", "awe", "disgust",
			"love", "hate", "friendship", "betrayal", "trust", "suspicion",
			"greed", "generosity", "envy", "pride", "shame", "glory",
			"madness", "sanity", "confusion", "clarity", "exhaustion", "energy",
			"tilt", "rage", "zen", "vibes", "mood", "feels"
		});
		WORD_CATEGORIES.put("Descriptors", new String[] {
			"amazing", "terrible", "good", "bad", "great", "awful", "best", "worst",
			"hidden", "secret", "obvious", "mysterious", "strange", "normal",
			"hard", "easy", "impossible", "simple", "complex", "tricky",
			"dangerous", "safe", "deadly", "harmless", "toxic", "blessed",
			"strong", "weak", "powerful", "pathetic", "mighty", "feeble",
			"fast", "slow", "quick", "sluggish", "instant", "eternal",
			"big", "small", "huge", "tiny", "massive", "minuscule",
			"hot", "cold", "warm", "freezing", "burning", "frozen",
			"dark", "light", "bright", "dim", "shadowy", "radiant",
			"old", "new", "ancient", "modern", "legendary", "common",
			"rare", "epic", "godly", "blessed", "cursed", "haunted",
			"sweaty", "casual", "tryhard", "chill", "intense", "relaxed",
			"based", "cringe", "poggers", "sadge", "pepega", "5head",
			"absolute", "utter", "pure", "complete", "total", "partial"
		});
		WORD_CATEGORIES.put("British", new String[] {
			"mate", "lad", "lass", "bloke", "chap", "geezer", "bruv", "fam",
			"bollocks", "bloody", "bugger", "blimey", "crikey", "cor",
			"brilliant", "rubbish", "naff", "dodgy", "cheeky", "proper",
			"knackered", "gobsmacked", "gutted", "chuffed", "miffed", "narked",
			"mental", "daft", "barmy", "bonkers", "crackers", "mental",
			"quid", "tenner", "fiver", "bob", "nicker", "brass",
			"cuppa", "brekkie", "sarnie", "butty", "chippy", "pub",
			"loo", "bog", "khazi", "toilet", "lavatory", "WC",
			"bum", "arse", "backside", "bottom", "rear", "behind",
			"innit", "init", "right", "yeah", "nah", "dunno",
			"wanker", "tosser", "muppet", "numpty", "plonker", "pillock",
			"sod off", "bugger off", "jog on", "do one", "get stuffed",
			"having a laugh", "taking the piss", "absolute legend", "top bloke",
			"cheers", "ta", "thanks", "lovely", "smashing", "cracking"
		});
		WORD_CATEGORIES.put("Memes", new String[] {
			"finger", "but hole", "dog", "try tongue", "offer seed",
			"visions of", "no", "yes", "why", "how", "when", "where",
			"praise the sun", "don't give up skeleton", "time for crab",
			"horse", "but hole", "try thrust", "offer pickle",
			"liar ahead", "try jumping", "hidden path", "illusory wall",
			"seek head", "behold", "O you don't have the right",
			"could this be a", "if only I had a", "didn't expect",
			"is this", "I can't take this", "let me solo her",
			"we go jim", "gains", "no XP waste", "efficiency scape",
			"buying GF", "trimming armour free", "doubling money",
			"sit", "get rekt", "EZ", "GG", "L", "W", "F", "Pog",
			"Kappa", "Keepo", "MonkaS", "PepeHands", "Copium", "Hopium",
			"sus", "amogus", "imposter", "sussy", "baka",
			"bruh", "oof", "yeet", "vibe check", "no cap", "bussin",
			"sheesh", "respectfully", "down bad", "touch grass",
			"least insane", "most sane", "average", "enjoyer", "fan",
			"73", "69", "420", "nice", "gamer moment", "skill issue"
		});
		WORD_CATEGORIES.put("Connectors", new String[] {
			"and", "or", "but", "so", "then", "therefore", "however",
			"the", "a", "an", "this", "that", "these", "those",
			"is", "are", "was", "were", "be", "been", "being",
			"I", "you", "he", "she", "it", "we", "they", "one",
			"my", "your", "his", "her", "its", "our", "their",
			"in", "on", "at", "to", "from", "with", "without",
			"for", "of", "by", "about", "between", "through",
			"before", "after", "during", "while", "until", "since",
			"if", "unless", "although", "because", "when", "where",
			"not", "no", "never", "always", "sometimes", "maybe",
			"very", "really", "quite", "rather", "somewhat", "extremely",
			"!", "?", "...", ",", ":", ";"
		});
		WORD_CATEGORIES.put("Messages", new String[] {
			"Good luck", "You can do it", "Don't give up", "Keep going",
			"Almost there", "So close", "One more try", "Believe",
			"You died", "Game over", "Try again", "Respawn",
			"Welcome", "Hello", "Goodbye", "See you", "Nice to meet you",
			"Thank you", "Sorry", "Excuse me", "Please", "Help",
			"Well done", "Congratulations", "GG", "Nice one", "Well played",
			"Be careful", "Watch your step", "Stay alert", "Stay safe",
			"Go back", "Wrong way", "Dead end", "No entry",
			"Follow me", "Wait here", "Come with me", "Stay close",
			"I was here", "Remember this", "Never forget", "Witness me",
			"First", "Last", "Only one", "The chosen one",
			"Praise be", "Blessed be", "May RNG be with you", "GL HF",
			"No pain no gain", "Risk it for the biscuit", "Send it",
			"LEROY JENKINS", "At least I have chicken"
		});
		WORD_CATEGORIES.put("Famous People", new String[] {
			// Jagex Staff
			"Mod Ash", "God Ash", "The Ash", "Mod Archie", "Mod Arcane", "Mod Ayiza",
			"Mod Boko", "Mod Bruno", "Mod Curse", "Mod Ed", "Mod Elena", "Mod Goblin",
			"Mod Husky", "Mod Kieren", "Mod Light", "Mod Lottie", "Mod Mack",
			"Mod Oasis", "Mod Sarnie", "Mod Squid", "Mod Sween", "Mod Tide", "Mod West",
			"Mod Roq", "Mod Flippy", "Mod Abyss", "Mod Crystal", "Mod Daizong",
			// Legendary Players
			"Zezima", "Woox", "Lynx Titan", "Hey Jase", "Suomi",
			// Content Creators
			"B0aty", "Settled", "Swampletics", "C Engineer", "Mudkip", "Torvesta",
			"Framed", "Skill Specs", "A Friend", "Dovydas", "J1mmy", "9Rain",
			"SoloMission", "EVscape", "Tanzoo", "Virtoso", "Faux", "Sick Nerd",
			"Rendi", "Dino", "FlippingOldschool", "Alfie", "Odablock", "Purespam",
			"Mmorpg", "Theoatrix", "Slayermusiq1", "Soup", "Spookdog", "Jakeyosaurus",
			"KempQ", "Ricecup", "Vannaka", "Westham", "Gunschilli", "25 Buttholes",
			"Lower the Better", "Verf", "UIM Lenny", "xRakine", "WildMudkip",
			// Meme References
			"Durial321", "the Falador Massacre", "a Jmod", "a famous player",
			"a streamer", "a YouTuber", "a content creator", "a legend"
		});
	}

	private final RuneMessagesPlugin plugin;
	private final JTextArea messageInput;
	private final JLabel charCountLabel;
	private final JPanel wordButtonsPanel;
	private final JTextField searchField;
	private final JComboBox<String> categoryComboBox;
	private final JComboBox<MarkerType> markerTypeComboBox;
	private final JPanel myMessagesPanel;
	private final JLabel myMessagesStatusLabel;

	public RuneMessagesPanel(RuneMessagesPlugin plugin)
	{
		super(false);
		this.plugin = plugin;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel titleLabel = new JLabel("RuneMessages");
		titleLabel.setForeground(new Color(255, 215, 0));
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

		messageInput = new JTextArea(3, 20);
		messageInput.setLineWrap(true);
		messageInput.setWrapStyleWord(true);
		messageInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		messageInput.setForeground(new Color(255, 215, 0));
		messageInput.setEditable(false);
		messageInput.setFocusable(false);
		messageInput.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(255, 215, 0, 100)),
			BorderFactory.createEmptyBorder(5, 5, 5, 5)
		));

		JScrollPane messageScrollPane = new JScrollPane(messageInput);
		messageScrollPane.setBorder(null);
		messageScrollPane.setPreferredSize(new Dimension(0, 60));

		charCountLabel = new JLabel("0 / " + MAX_MESSAGE_LENGTH);
		charCountLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		JButton deleteLastButton = new JButton("Undo");
		deleteLastButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		deleteLastButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		deleteLastButton.setFocusPainted(false);
		deleteLastButton.addActionListener(e -> deleteLastWord());

		JButton clearButton = new JButton("Clear");
		clearButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		clearButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		clearButton.setFocusPainted(false);
		clearButton.addActionListener(e -> { messageInput.setText(""); updateCharCount(); });

		JPanel buttonRow = new JPanel(new GridLayout(1, 2, 3, 0));
		buttonRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttonRow.add(deleteLastButton);
		buttonRow.add(clearButton);

		JPanel charCountPanel = new JPanel(new BorderLayout());
		charCountPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		charCountPanel.add(buttonRow, BorderLayout.WEST);
		charCountPanel.add(charCountLabel, BorderLayout.EAST);

		JPanel inputPanel = new JPanel(new BorderLayout(0, 5));
		inputPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		inputPanel.add(messageScrollPane, BorderLayout.CENTER);
		inputPanel.add(charCountPanel, BorderLayout.SOUTH);

		JPanel filterPanel = new JPanel(new BorderLayout(5, 0));
		filterPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		filterPanel.setBorder(new EmptyBorder(10, 0, 5, 0));

		searchField = new JTextField();
		searchField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchField.setForeground(Color.WHITE);
		searchField.setCaretColor(Color.WHITE);
		searchField.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			BorderFactory.createEmptyBorder(3, 5, 3, 5)
		));
		searchField.setText("Search...");
		searchField.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		searchField.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(java.awt.event.FocusEvent e) {
				if (searchField.getText().equals("Search...")) { searchField.setText(""); searchField.setForeground(Color.WHITE); }
			}
			public void focusLost(java.awt.event.FocusEvent e) {
				if (searchField.getText().isEmpty()) { searchField.setText("Search..."); searchField.setForeground(ColorScheme.LIGHT_GRAY_COLOR); }
			}
		});
		searchField.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { filterWords(); } });

		String[] categories = new String[WORD_CATEGORIES.size() + 1];
		categories[0] = "All Categories";
		int i = 1;
		for (String cat : WORD_CATEGORIES.keySet()) { categories[i++] = cat; }

		categoryComboBox = new JComboBox<>(categories);
		categoryComboBox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		categoryComboBox.setForeground(Color.WHITE);
		categoryComboBox.setPreferredSize(new Dimension(120, 25));
		categoryComboBox.addActionListener(e -> filterWords());

		filterPanel.add(searchField, BorderLayout.CENTER);
		filterPanel.add(categoryComboBox, BorderLayout.EAST);

		wordButtonsPanel = new JPanel();
		wordButtonsPanel.setLayout(new BoxLayout(wordButtonsPanel, BoxLayout.Y_AXIS));
		wordButtonsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JScrollPane wordsScrollPane = new JScrollPane(wordButtonsPanel);
		wordsScrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR));
		wordsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		wordsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		filterWords();

		JPanel wordsSection = new JPanel(new BorderLayout());
		wordsSection.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wordsSection.add(filterPanel, BorderLayout.NORTH);
		wordsSection.add(wordsScrollPane, BorderLayout.CENTER);

		JPanel markerPanel = new JPanel(new BorderLayout(5, 0));
		markerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		markerPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
		JLabel markerLabel = new JLabel("Marker:");
		markerLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		markerTypeComboBox = new JComboBox<>(MarkerType.values());
		markerTypeComboBox.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		markerTypeComboBox.setForeground(Color.WHITE);
		markerTypeComboBox.setSelectedItem(MarkerType.NOTE);
		markerPanel.add(markerLabel, BorderLayout.WEST);
		markerPanel.add(markerTypeComboBox, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
		JButton submitButton = new JButton("Write");
		submitButton.setBackground(new Color(60, 90, 60));
		submitButton.setForeground(Color.WHITE);
		submitButton.setFocusPainted(false);
		submitButton.addActionListener(e -> submitMessage());
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		cancelButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		cancelButton.setFocusPainted(false);
		cancelButton.addActionListener(e -> cancel());
		buttonPanel.add(cancelButton);
		buttonPanel.add(submitButton);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		bottomPanel.add(markerPanel, BorderLayout.NORTH);
		bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

		JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
		contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		contentPanel.add(inputPanel, BorderLayout.NORTH);
		contentPanel.add(wordsSection, BorderLayout.CENTER);
		contentPanel.add(bottomPanel, BorderLayout.SOUTH);

		JPanel myMessagesSection = new JPanel(new BorderLayout(0, 5));
		myMessagesSection.setBackground(ColorScheme.DARK_GRAY_COLOR);
		myMessagesSection.setBorder(new EmptyBorder(10, 0, 0, 0));
		JSeparator separator = new JSeparator();
		separator.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		JPanel myMessagesHeader = new JPanel(new BorderLayout());
		myMessagesHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		myMessagesHeader.setBorder(new EmptyBorder(5, 0, 5, 0));
		JLabel myMessagesTitleLabel = new JLabel("My Messages");
		myMessagesTitleLabel.setForeground(new Color(255, 215, 0));
		JButton refreshButton = new JButton("Refresh");
		refreshButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		refreshButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		refreshButton.setFocusPainted(false);
		refreshButton.setPreferredSize(new Dimension(70, 22));
		refreshButton.addActionListener(e -> refreshMyMessages());
		myMessagesHeader.add(myMessagesTitleLabel, BorderLayout.WEST);
		myMessagesHeader.add(refreshButton, BorderLayout.EAST);

		myMessagesPanel = new JPanel();
		myMessagesPanel.setLayout(new BoxLayout(myMessagesPanel, BoxLayout.Y_AXIS));
		myMessagesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		myMessagesStatusLabel = new JLabel("Click Refresh to load your messages");
		myMessagesStatusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		myMessagesStatusLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		myMessagesPanel.add(myMessagesStatusLabel);

		JScrollPane myMessagesScrollPane = new JScrollPane(myMessagesPanel);
		myMessagesScrollPane.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR));
		myMessagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		myMessagesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		myMessagesScrollPane.setPreferredSize(new Dimension(0, 120));
		myMessagesSection.add(separator, BorderLayout.NORTH);
		myMessagesSection.add(myMessagesHeader, BorderLayout.CENTER);
		myMessagesSection.add(myMessagesScrollPane, BorderLayout.SOUTH);

		// Account Info Section
		JPanel accountSection = new JPanel(new BorderLayout(0, 5));
		accountSection.setBackground(ColorScheme.DARK_GRAY_COLOR);
		accountSection.setBorder(new EmptyBorder(10, 0, 0, 0));

		JSeparator accountSeparator = new JSeparator();
		accountSeparator.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);

		JLabel accountTitleLabel = new JLabel("Account Info");
		accountTitleLabel.setForeground(new Color(255, 215, 0));
		accountTitleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

		JPanel accountInfoPanel = new JPanel();
		accountInfoPanel.setLayout(new BoxLayout(accountInfoPanel, BoxLayout.Y_AXIS));
		accountInfoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		accountInfoPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR),
			BorderFactory.createEmptyBorder(8, 8, 8, 8)
		));

		JPanel usernameRow = new JPanel(new BorderLayout(5, 0));
		usernameRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JLabel usernameLabel = new JLabel("Username:");
		usernameLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		usernameValueLabel = new JLabel("Not registered");
		usernameValueLabel.setForeground(Color.WHITE);
		usernameRow.add(usernameLabel, BorderLayout.WEST);
		usernameRow.add(usernameValueLabel, BorderLayout.CENTER);

		JPanel apiKeyRow = new JPanel(new BorderLayout(5, 0));
		apiKeyRow.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		apiKeyRow.setBorder(new EmptyBorder(5, 0, 0, 0));
		JLabel apiKeyLabel = new JLabel("API Key:");
		apiKeyLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		apiKeyValueLabel = new JLabel("Not registered");
		apiKeyValueLabel.setForeground(Color.WHITE);
		apiKeyValueLabel.setToolTipText("Save this key to recover your account after reinstall");
		apiKeyRow.add(apiKeyLabel, BorderLayout.WEST);
		apiKeyRow.add(apiKeyValueLabel, BorderLayout.CENTER);

		JLabel warningLabel = new JLabel("<html><i>Save your API key to recover access after reinstall!</i></html>");
		warningLabel.setForeground(new Color(255, 200, 100));
		warningLabel.setFont(warningLabel.getFont().deriveFont(10f));
		warningLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

		accountInfoPanel.add(usernameRow);
		accountInfoPanel.add(apiKeyRow);
		accountInfoPanel.add(warningLabel);

		accountSection.add(accountSeparator, BorderLayout.NORTH);
		accountSection.add(accountTitleLabel, BorderLayout.CENTER);
		accountSection.add(accountInfoPanel, BorderLayout.SOUTH);

		// Bottom wrapper for My Messages and Account Info
		JPanel bottomWrapper = new JPanel();
		bottomWrapper.setLayout(new BoxLayout(bottomWrapper, BoxLayout.Y_AXIS));
		bottomWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		bottomWrapper.add(myMessagesSection);
		bottomWrapper.add(accountSection);

		JPanel mainWrapper = new JPanel(new BorderLayout());
		mainWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainWrapper.add(contentPanel, BorderLayout.CENTER);
		mainWrapper.add(bottomWrapper, BorderLayout.SOUTH);

		add(titleLabel, BorderLayout.NORTH);
		add(mainWrapper, BorderLayout.CENTER);

		// Initial update of account info
		updateAccountInfo();
	}

	public void updateAccountInfo() {
		String apiKey = plugin.getMessageService().getApiKey();
		String username = plugin.getCurrentPlayerName();

		if (apiKey != null && !apiKey.isEmpty()) {
			// Show first 8 and last 4 characters for security
			if (apiKey.length() > 12) {
				apiKeyValueLabel.setText(apiKey.substring(0, 8) + "..." + apiKey.substring(apiKey.length() - 4));
			} else {
				apiKeyValueLabel.setText(apiKey);
			}
			apiKeyValueLabel.setToolTipText(apiKey + " (click to copy - save this key!)");
		} else {
			apiKeyValueLabel.setText("Not registered");
			apiKeyValueLabel.setToolTipText("Log in to register automatically");
		}

		if (username != null && !username.isEmpty()) {
			usernameValueLabel.setText(username);
		} else {
			usernameValueLabel.setText("Not logged in");
		}
	}

	private void filterWords() {
		wordButtonsPanel.removeAll();
		String searchText = searchField.getText().toLowerCase();
		if (searchText.equals("search...")) searchText = "";
		String selectedCategory = (String) categoryComboBox.getSelectedItem();
		boolean allCategories = "All Categories".equals(selectedCategory);
		List<String> wordsToShow = new ArrayList<>();
		for (Map.Entry<String, String[]> entry : WORD_CATEGORIES.entrySet()) {
			if (!allCategories && !entry.getKey().equals(selectedCategory)) continue;
			for (String word : entry.getValue()) {
				if (searchText.isEmpty() || word.toLowerCase().contains(searchText)) {
					if (!wordsToShow.contains(word)) wordsToShow.add(word);
				}
			}
		}
		for (String word : wordsToShow) wordButtonsPanel.add(createWordButton(word));
		wordButtonsPanel.revalidate();
		wordButtonsPanel.repaint();
	}

	private JButton createWordButton(String word) {
		JButton btn = new JButton(word);
		btn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		btn.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR), BorderFactory.createEmptyBorder(4, 10, 4, 10)));
		btn.setAlignmentX(JButton.LEFT_ALIGNMENT);
		btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		btn.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(80, 80, 80)); btn.setForeground(new Color(255, 215, 0)); }
			public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(ColorScheme.DARKER_GRAY_COLOR); btn.setForeground(ColorScheme.LIGHT_GRAY_COLOR); }
		});
		btn.addActionListener(e -> appendWord(word));
		return btn;
	}

	private void appendWord(String word) {
		String currentText = messageInput.getText();
		String newText;
		if (currentText.isEmpty()) newText = word;
		else if (currentText.endsWith(" ") || word.equals(",") || word.equals(".") || word.equals("!") || word.equals("?") || word.equals(":") || word.equals(";") || word.equals("...")) newText = currentText + word;
		else newText = currentText + " " + word;
		if (newText.length() <= MAX_MESSAGE_LENGTH) { messageInput.setText(newText); updateCharCount(); }
	}

	private void deleteLastWord() {
		String text = messageInput.getText().trim();
		if (text.isEmpty()) return;
		int lastSpace = text.lastIndexOf(' ');
		messageInput.setText(lastSpace >= 0 ? text.substring(0, lastSpace) : "");
		updateCharCount();
	}

	private void updateCharCount() {
		int length = messageInput.getText().length();
		charCountLabel.setText(length + " / " + MAX_MESSAGE_LENGTH);
		charCountLabel.setForeground(length > MAX_MESSAGE_LENGTH * 0.8 ? new Color(255, 150, 150) : ColorScheme.LIGHT_GRAY_COLOR);
	}

	private void submitMessage() {
		String message = messageInput.getText().trim();
		if (!message.isEmpty()) {
			plugin.submitMessage(message, (MarkerType) markerTypeComboBox.getSelectedItem());
			messageInput.setText("");
			updateCharCount();
		}
	}

	public MarkerType getSelectedMarkerType() { return (MarkerType) markerTypeComboBox.getSelectedItem(); }
	private void cancel() { messageInput.setText(""); updateCharCount(); plugin.closeMessagePanel(); }

	private void refreshMyMessages() {
		String playerName = plugin.getCurrentPlayerName();
		if (playerName == null || playerName.isEmpty()) {
			myMessagesPanel.removeAll(); myMessagesStatusLabel.setText("Not logged in"); myMessagesPanel.add(myMessagesStatusLabel); myMessagesPanel.revalidate(); myMessagesPanel.repaint(); return;
		}
		myMessagesPanel.removeAll(); myMessagesStatusLabel.setText("Loading..."); myMessagesPanel.add(myMessagesStatusLabel); myMessagesPanel.revalidate(); myMessagesPanel.repaint();
		plugin.getMessageService().getAllAuthorMessages()
			.thenAccept(messages -> SwingUtilities.invokeLater(() -> {
				myMessagesPanel.removeAll();
				if (messages.isEmpty()) { myMessagesStatusLabel.setText("No messages found"); myMessagesPanel.add(myMessagesStatusLabel); }
				else { for (MessageData msg : messages) myMessagesPanel.add(createMessageEntry(msg)); }
				myMessagesPanel.revalidate(); myMessagesPanel.repaint();
			}))
			.exceptionally(ex -> { SwingUtilities.invokeLater(() -> { myMessagesPanel.removeAll(); myMessagesStatusLabel.setText("Failed to load messages"); myMessagesPanel.add(myMessagesStatusLabel); myMessagesPanel.revalidate(); myMessagesPanel.repaint(); }); return null; });
	}

	private JPanel createMessageEntry(MessageData message) {
		JPanel entry = new JPanel(new BorderLayout(5, 2));
		entry.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		entry.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
		entry.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
		String msgText = message.getMessage();
		if (msgText.length() > 35) msgText = msgText.substring(0, 32) + "...";
		JLabel messageLabel = new JLabel("\"" + msgText + "\"");
		messageLabel.setForeground(new Color(255, 215, 0));
		JLabel locationLabel = new JLabel("World: " + message.getWorldId() + " | Region: " + message.getRegionId());
		locationLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		locationLabel.setFont(locationLabel.getFont().deriveFont(10f));
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JPanel votesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		votesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JLabel thumbsUpLabel = new JLabel("\u25B2 " + message.getThumbsUp());
		thumbsUpLabel.setForeground(new Color(50, 205, 50));
		JLabel thumbsDownLabel = new JLabel("\u25BC " + message.getThumbsDown());
		thumbsDownLabel.setForeground(new Color(220, 20, 60));
		votesPanel.add(thumbsUpLabel); votesPanel.add(thumbsDownLabel);
		JButton deleteButton = new JButton("Delete");
		deleteButton.setBackground(new Color(120, 40, 40));
		deleteButton.setForeground(Color.WHITE);
		deleteButton.setFocusPainted(false);
		deleteButton.setFont(deleteButton.getFont().deriveFont(10f));
		deleteButton.setPreferredSize(new Dimension(60, 20));
		deleteButton.addActionListener(e -> deleteMessage(message));
		JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
		deletePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		deletePanel.add(deleteButton);
		rightPanel.add(votesPanel); rightPanel.add(deletePanel);
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		leftPanel.add(messageLabel); leftPanel.add(locationLabel);
		entry.add(leftPanel, BorderLayout.CENTER);
		entry.add(rightPanel, BorderLayout.EAST);
		return entry;
	}

	private void deleteMessage(MessageData message) {
		plugin.getMessageService().deleteMessage(message.getWorldId(), message.getRegionId(), message.getId())
			.thenAccept(v -> SwingUtilities.invokeLater(() -> { plugin.removeMessage(message.getId()); refreshMyMessages(); }))
			.exceptionally(ex -> { SwingUtilities.invokeLater(() -> { myMessagesPanel.removeAll(); myMessagesStatusLabel.setText("Failed to delete message"); myMessagesPanel.add(myMessagesStatusLabel); myMessagesPanel.revalidate(); myMessagesPanel.repaint(); }); return null; });
	}
}
