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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("runemessages")
public interface RuneMessagesConfig extends Config
{
	@ConfigSection(
		name = "Display Settings",
		description = "Configure how messages are displayed",
		position = 0
	)
	String displaySection = "displaySection";

	@ConfigItem(
		keyName = "showOwnMessages",
		name = "Show Own Messages",
		description = "Display your own messages as markers",
		position = 1,
		section = displaySection
	)
	default boolean showOwnMessages()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showOtherMessages",
		name = "Show Other Players' Messages",
		description = "Display messages left by other players",
		position = 2,
		section = displaySection
	)
	default boolean showOtherMessages()
	{
		return true;
	}

	@ConfigItem(
		keyName = "displayRadius",
		name = "Message Display Radius",
		description = "How close you need to be to see the message text (in tiles)",
		position = 3,
		section = displaySection
	)
	@Range(min = 1, max = 20)
	default int displayRadius()
	{
		return 5;
	}

	@ConfigSection(
		name = "Sync Settings",
		description = "Configure message synchronization",
		position = 10
	)
	String syncSection = "syncSection";

	@ConfigItem(
		keyName = "syncMessages",
		name = "Sync Messages",
		description = "Share your messages with other players online",
		position = 11,
		section = syncSection
	)
	default boolean syncMessages()
	{
		return true;
	}

	@ConfigSection(
		name = "Account",
		description = "Your RuneMessages account information. WARNING: Save your API key somewhere safe! If you reinstall RuneLite, you will need it to recover access to your messages.",
		position = 20
	)
	String accountSection = "accountSection";

	@ConfigItem(
		keyName = "apiKey",
		name = "API Key (DO NOT EDIT)",
		description = "Your unique API key. SAVE THIS KEY! You need it to recover your account after a fresh reinstall. Do not share this key with anyone.",
		position = 21,
		section = accountSection,
		warning = "Do not edit this field. Save this key somewhere safe to recover your account."
	)
	default String apiKey()
	{
		return "";
	}

	@ConfigItem(
		keyName = "registeredUsername",
		name = "Registered Username (DO NOT EDIT)",
		description = "The username this API key is registered to. This is set automatically.",
		position = 22,
		section = accountSection,
		warning = "Do not edit this field. It is set automatically when you log in."
	)
	default String registeredUsername()
	{
		return "";
	}
}
