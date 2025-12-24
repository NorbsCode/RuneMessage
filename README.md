# RuneMessages

A RuneLite plugin that allows players to leave messages for other players around the world.

## Features

- Write messages at your current location using the side panel
- Choose from different marker types (Note, Wooden Signpost, Stone Signpost)
- Use Dark Souls-style word builder with templates and conjunctions
- View messages from other players in the world
- Vote up/down on messages to boost visibility
- Report inappropriate messages
- Delete your own messages
- Messages sync across all players via server

## Word Categories

The word builder includes many categories:
- **Actions** - Try, Beware of, Attack, Jump, etc.
- **Directions** - ahead, behind, north, at Lumbridge, Grand Exchange, etc.
- **Things** - enemy, treasure, dragon, slayer task, etc.
- **OSRS** - RNG, Tbow, TOB, Ironman btw, etc.
- **Feelings** - danger, victory, tilt, vibes, etc.
- **Descriptors** - amazing, hidden, legendary, based, etc.
- **British** - mate, bollocks, cheeky, absolute legend, etc.
- **Memes** - finger but hole, praise the sun, 73, etc.
- **Connectors** - and, or, but, the, etc.
- **Messages** - Good luck, GG, LEROY JENKINS, etc.
- **Famous People** - Mod Ash, Zezima, B0aty, Settled, etc.

## Configuration

| Setting | Description | Default |
|---------|-------------|---------|
| Show Own Messages | Display your own messages as markers | Enabled |
| Show Other Players' Messages | Display messages left by other players | Enabled |
| Message Display Radius | How close you need to be to see message text (1-20 tiles) | 5 |
| Sync Messages | Share your messages with other players online | Enabled |

## Account Recovery

Your API key is generated automatically on first login. **Save your API key somewhere safe!** If you reinstall RuneLite, you will need it to recover access to your messages.

The API key can be found in the plugin settings under the "Account" section.

## How It Works

1. Open the RuneMessages panel from the sidebar
2. Use the word builder to construct your message (or type directly)
3. Select a marker type
4. Click "Write" to place your message at your current location
5. Other players will see your message when they enter the area

https://github.com/user-attachments/assets/08dec20e-98b0-4d3e-b086-9f694d926162



## Rate Limits

To keep things fair and prevent spam:

| Limit | Amount |
|-------|--------|
| Messages per region (chunk) | 1 |
| Messages per world | 5 |
| Max characters per message | 100 |

## Message Display Algorithm

When you enter a region, messages are loaded using this algorithm:

1. **Your own messages** - Always displayed first (no limit)
2. **Top 10 voted messages** - The 10 highest-voted messages (thumbsUp - thumbsDown) always appear
3. **Random selection** - Additional messages are randomly selected from the remaining pool (up to 50 total per region)

This ensures:
- Popular, helpful messages stay visible
- New messages still have a chance to be seen
- Your own messages are always visible to you

## Voting & Reporting

- **Upvote/Downvote** - Right-click a message marker to vote. Higher-voted messages appear more often
- **Report** - Report inappropriate messages. After 5 reports, a message is automatically hidden
- **One vote per message** - You can only vote once per message

## Support

For issues or support, join our Discord: https://discord.gg/aHndtqWEzF

## License

BSD 2-Clause License - See [LICENSE](LICENSE) for details.
