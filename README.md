# Discord Bridge

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen)
![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)
![Java](https://img.shields.io/badge/Java-21%2B-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

> A lightweight fabric server side mod for linking your Minecraft server's chat to Discord and vice-versa.

>This mod is still in development and is not yet feature complete.

---

## 📖 Brief Description

Discord bridge integrates your Minecraft Fabric server with your discord server, allowing admins to take action remotely and community members to interact with one another.

Features Include:
- MC Chat -> Discord (and vice-versa).
- MC Server console logging.
- Ability to send commands to the server via discord.
- Fully customisable formatting.
- Ability to use webhooks for player messages.
- Integration with **Nexia Utils**.

---

## ⌨️ Commands

COMMANDS SECTION (TO DO)

### Player Commands

| Command | Arguments | Description |
|--------|-----------|-------------|
| `/example` | — | Brief description of what this command does |
| `/example <value>` | `value` | Description of the argument |
| `/example toggle` | — | Toggles a feature |

### Admin / Operator Commands

| Command | Permission Level | Description |
|--------|------------------|-------------|
| `/example reload` | OP | Reloads the configuration |
| `/example debug` | OP | Enables debug output |

---

## ⚙️ Example Configuration

Click below to expand an example configuration file.

<details>
<summary><strong>Click to reveal example config</strong></summary>

```yml
# =====================================================
# Discord Bridge Configuration
# =====================================================
# This file controls how your Minecraft server connects
# to Discord and how messages are formatted.
#
# Any changes require a SERVER RESTART unless you
# implement a reload command.
# =====================================================


# -----------------------------------------------------
# Discord Bot Settings
# -----------------------------------------------------

# Your Discord bot token.
botToken: 'PUT_YOUR_TOKEN_HERE'

# Bot activity type.
# Allowed values:
#  - watching
#  - competing
#  - listening
#  - playing
#  - streaming
botActivity: watching

# Streaming URL.
# REQUIRED if botActivity is set to "streaming".
botStreamingUrl: ''

# Text displayed in the bot activity.
# Example: "Watching Your Minecraft Server"
botDoing: Your fantastic Minecraft Server


# -----------------------------------------------------
# Discord Channel IDs
# -----------------------------------------------------
# These must be valid Discord channel IDs.
# Do NOT wrap them in quotes.

# Channel that relays chat between Discord and Minecraft.
discordChannelToMinecraft:

# Channel that displays Minecraft server console output.
discordChannelDisplayConsole:

# Channel where Discord users can send commands to Minecraft.
discordChannelCommandToMinecraft:


# -----------------------------------------------------
# Minecraft → Discord Message Formatting
# -----------------------------------------------------

minecraftMessageFormatting:

  # Discord roles allowed to send commands to the server.
  rolesThatSendCommands:
    - Owner
    - Manager

  # Message format when the user has a recognised role.
  # Available placeholders:
  # %name%, %username%, %message%, %toprole%, %toprolecolour%
  messageFormatUserWithRole: '&f[&bDiscord&f | %toprolecolour%%toprole%&f] %name% » %message%'

  # Message format when the user has no recognised role.
  messageFormatUserWithNoRole: '&f[&bDiscord&f] %name% » %message%'

  # Discord role → Minecraft colour code mappings.
  # Role names are CASE-SENSITIVE.
  roles:
    Owner: '&4'
    Manager: '&c'
    Staff: '&6'

  # Roles that should NOT be shown in Minecraft chat.
  excludedRoles:
    - Vanilla
    - North America
    - Europe

  # Messages from users (or bots) with these roles
  # will NOT be sent to Minecraft.
  # IMPORTANT: Your bot should usually have one of these roles
  # to avoid message echo loops.
  blockedRoles:
    - Bot

  # Colour used for roles not defined above.
  otherRoleColourCode: '&d'


# -----------------------------------------------------
# Discord → Minecraft Message Formatting
# -----------------------------------------------------

discordMessageFormatting:

  # Message format for messages sent from Minecraft to Discord.
  # Placeholders:
  # %name%, %uuid%, %message%
  # messageFormat is only used if webhooks are disabled.
  messageFormat: '%name% » %message%'
  webhooks: true
  # If you want to use webhooks, you must create the webhook in the required channel, copy the link and paste it here.
  discord_webhook_url: "https://discord.com/api/webhooks/...."



# -----------------------------------------------------
# Join / Leave Messages
# -----------------------------------------------------

# Enable join and leave announcements in Discord.
joinLeaveMessageEnabled: true


joinMessageFormatting:
  messageTitle: Player Joined
  messageDescription: '%name% has joined the server!'
  embedColour: '#66ff00'

  image:
    enabled: false
    usePlayerSkinAsImage: true
    useOtherImage: false
    imageSettings:
      url: https://playnexia.net/assets/logo-border.png

  thumbnail:
    enabled: true
    usePlayerSkinAsThumbnail: true
    useOtherThumbnail: false
    thumbnailSettings:
      url: https://playnexia.net/assets/logo-border.png


leaveMessageFormatting:
  messageTitle: Player Left
  messageDescription: '%name% has left the server!'
  embedColour: '#FF0000'

  image:
    enabled: false
    usePlayerSkinAsImage: true
    useOtherImage: false
    imageSettings:
      url: https://playnexia.net/assets/logo-border.png

  thumbnail:
    enabled: true
    usePlayerSkinAsThumbnail: true
    useOtherThumbnail: false
    thumbnailSettings:
      url: https://playnexia.net/assets/logo-border.png


# Permissions that allow players to join/leave silently.
joinSilentlyPermission: nexia.discordbridge.join.silent
leaveSilentlyPermission: nexia.discordbridge.leave.silent



# -----------------------------------------------------
# Advancements
# -----------------------------------------------------

announceAdvancementsEnabled: true

advancementFormatting:
  messageTitle: Advancement Unlocked
  messageDescription: '%name% has made the advancement %advancement%'
  embedColour: '#CC8899'

  image:
    enabled: false
    usePlayerSkinAsImage: true
    useOtherImage: false
    imageSettings:
      url: https://playnexia.net/assets/logo-border.png

  thumbnail:
    enabled: true
    usePlayerSkinAsThumbnail: true
    useOtherThumbnail: false
    thumbnailSettings:
      url: https://playnexia.net/assets/logo-border.png



# -----------------------------------------------------
# Death Messages
# -----------------------------------------------------

announceDeathsEnabled: true

deathFormatting:
  messageTitle: Player Died
  messageDescription: '%deathMessage%'
  embedColour: '#CC8899'

  image:
    enabled: false
    usePlayerSkinAsImage: true
    useOtherImage: false
    imageSettings:
      url: https://playnexia.net/assets/logo-border.png

  thumbnail:
    enabled: true
    usePlayerSkinAsThumbnail: true
    useOtherThumbnail: false
    thumbnailSettings:
      url: https://playnexia.net/assets/logo-border.png



# -----------------------------------------------------
# AFK Messages
# -----------------------------------------------------

announceAfkEnabled: true

afkFormatting:
  afkMessageTitle: Player AFK
  returnMessageTitle: Player is back
  afkMessageDescription: '%name% is AFK'
  returnMessageDescription: '%name% is back'
  afkEmbedColour: '#FF0000'
  returnEmbedColour: '#66ff00'

  image:
    enabled: false
    usePlayerSkinAsImage: true
    useOtherImage: false
    imageSettings:
      url: https://playnexia.net/assets/logo-border.png

  thumbnail:
    enabled: true
    usePlayerSkinAsThumbnail: true
    useOtherThumbnail: false
    thumbnailSettings:
      url: https://playnexia.net/assets/logo-border.png



# -----------------------------------------------------
# Server Lifecycle Announcements
# -----------------------------------------------------

announceServerStartup: true
serverStartupMessageFormat: ':white_check_mark: **Server has started**'

announceServerDisable: true
serverDisableMessageFormat: ':octagonal_sign: **Server has stopped**'



# -----------------------------------------------------
# Miscellaneous
# -----------------------------------------------------

# If true, Discord emojis will be converted to text
# before being sent to Minecraft chat.
translateDiscordEmojisToText: true




```
</details>

## Building the Mod
This project uses **Gradle with Fabric Loom**

### Requirements
 - Java 21+
 - Git

### Build Steps
```bash
git clone https://github.com/PhileasFogg3/Fabric-Discord-Bridge
cd discord-bridge
./gradlew build

```

## Known Issues
- The reload command does not work if you want to hot change between webhook messages and bot messages

## License
This project is licensed under the MIT License

You are free to:
- Use
- Modify
- Distribute
- Include in private or commercial projects

See the [License](LICENSE.txt) file for more info.

## Credits and Acknowledgements
- Created with the Fabric Loader and Fabric API using the Discord API / JDA
- Created for the [Nexia Network](playnexia.net). Thank you to staff and community members for bug testing and feature requests.
