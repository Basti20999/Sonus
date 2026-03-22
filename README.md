# Sonus

<img src="https://imgur.com/x1LTPri.png" alt="logo" width="200">

[![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/MinceraftMC/Sonus?style=flat-square)](https://github.com/MinceraftMC/Sonus)
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-yellow.svg?style=flat-square)](https://opensource.org/license/gpl-3.0/)
![Status Alpha](https://img.shields.io/badge/Status-Alpha-red?style=flat-square)
[![Discord](https://img.shields.io/discord/1094193723191070793?style=flat-square&label=Discord&link=https%3A%2F%2Fdiscord.gg%2FzC8xjtSPKC)](https://discord.gg/zC8xjtSPKC)

## Description

Sonus is plugin for Minecraft proxies and Paper servers. It supports multiple protocols and makes them compatible among
each other. Therefore, your players aren't forced to use a specific mod or even mod the game.

## Features

- Plug and Play installation
- Single UDP port for all voice mod protocols
- Cross-protocol compatibility
- Cross-server group voice chats
- WebRTC-based web browser support with isolated media server implementation
- [SonusWeb](https://github.com/MinceraftMC/SonusWeb) frontend
- [SimpleVoiceChat](https://github.com/henkelmax/simple-voice-chat) support
- [PlasmoVoice](https://github.com/plasmoapp/plasmo-voice) support

## Usage

To use Sonus, you will need to install the [Sonus Service](#service) and the [Sonus Agent](#agent).
Note that Sonus will completely replace your existing voice chat solution, so make sure to uninstall any existing voice
chat
plugins before installing Sonus on your servers.

### Service

The Sonus Service will need to be installed on your network proxy (currently only Velocity supported)
and will connect with the players (provided they are using either SimpleVoiceChat or PlasmoVoice).
There is currently no standalone version of this service.

You will need to have a public ip address and port. Both SimpleVoiceChat and PlasmoVoice use UDP
for communication. They will be reachable on the same port.

#### Configuration

- `bind`: The address and port Sonus should use for udp voice communication
- `host`: Sonus will tell the clients to connect to this address for voice communication. If you are using a reverse
  proxy or are behind NAT, make sure to set this to the public address.
- `opus-codec`: Encoding quality. `VOIP`, `AUDIO`, `RESTRICTED_LOWDELAY` are supported.
  See [opus-docs](https://www.opus-codec.org/docs/opus_api-1.3.1/group__opus__encoder.html#gaa89264fd93c9da70362a0c9b96b9ca88)
  for more information. Default: `VOIP`
- `mtu-size`: Maximum transmission unit size for UDP packets. If you experience issues with voice communication,
  try lowering this value. Default: `1412`
- `voice-chat-range`: Distance for spatial voice chat in blocks. Default: `32`
- `allow-recordings`: If enabled, clients are allowed to record voice chats. Default: `false`
- `keep-alive-ms`: Interval in milliseconds for keep-alive packets. Default: `1000`
- `keep-alive-timeout-ms`: Timeout in milliseconds until a client is considered disconnected. Default: `30000`
- `auto-gain-control`: Sonus will automatically adjust the microphone volume of clients. This prevents very loud clients
  and inaudible clients. Default: `true`
- `adapter-configs`: Configuration for each protocol adapter.
    - `plasmo`: Configuration for [PlasmoVoice](https://github.com/plasmoapp/plasmo-voice)
        - `enabled`: Whether the adapter should be enabled. Default: `true`
        - `server-id`: The server id will be used to identify the server by the clients to store some server specific
          settings.
    - `svc`: Configuration for [SimpleVoiceChat](https://github.com/henkelmax/simple-voice-chat)
        - `enabled`: Whether the adapter should be enabled. Default `true`
    - `web`: Configuration for [SonusWeb](https://github.com/MinceraftMC/SonusWeb)
        - `enabled`: Whether the adapter should be enabled. Default `false`
        - `use-root-command`: Whether the web adapter should be accessible via the root command (`/sonus`) if the client
          has
          no other adapter enabled. Default: `true`
        - `address`: The bind address for the web adapter signaling websocket. Default: `127.0.0.1:8032`
        - `link-pattern`: The pattern for the public frontend link.
          See [SonusWeb](https://github.com/MinceraftMC/SonusWeb) for more information.
        - `ice-servers`: List of STUN/TURN servers for WebRTC Connections
            - `url`: The URL of the STUN/TURN server. Default: `stun:stun.l.google.com:5349`
            - `username`: The username for the TURN server (optional).
            - `credential`: The credential for the TURN server (optional).
        - `ice-transport-policy`: The WebRTC ICE transport policy. `ALL`, `ONLY_RELAY`, `NO_HOSTS` are supported.
          Default:
          `ALL`
        - `bundle-policy`: The WebRTC bundle policy. `BALANCED`, `MAX_COMPAT`, `MAX_BUNDLE` are supported. Default:
          `MAX_BUNDLE`

#### Permissions

| Permission                     | Description                                                                                                                                    |
|--------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `sonus.voice.speak`            | Players with this permission will be able to speak. Can be toggled via the moderation commands.                                                |
| `sonus.voice.listen`           | Players with this permission will be able to listen to the voice chat.                                                                         |
| `sonus.connect`                | Only players with this permission will be able to connect to the Sonus Service. Can be toggled via the moderation commands. Defaults to `true` |
| `sonus.connect.plasmo `        | Only players with this permission will be able to connect with the PlasmoVoice adapter. Defaults to `true`                                     |
| `sonus.connect.svc`            | Only players with this permission will be able to connect with the SimpleVoiceChat adapter. Defaults to `true`                                 |
| `sonus.connect.web`            | Only players with this permission will be able to connect with the Web adapter. Defaults to `true`                                             |
| `sonus.groups.bypass.password` | Players with this permision can bypass the password of voice chat groups. This is useful for moderators.                                       |
| `sonus.groups.use`             | Players with this permission can use voice chat groups. Defaults to `true`                                                                     |

#### Commands

| Command                                        | Description                                                                                                                                                                                                                                                                                                      | Permission                             |
|------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------|
| `/sonus`                                       | Main command for Sonus. Shows the status of your voice chat connection and can be used to get a link for the web client if the web adapter is enabled and the config option `use-root-command` is set to `true`.                                                                                                 | `sonus.command`                        |
| `/sonus web`                                   | Shows a link for the web client. Requires the web adapter to be enabled.                                                                                                                                                                                                                                         | `sonus.command.web`                    |
| `/sonus group list`                            | Lists all voice chat groups. To join them you can click on the group name in the chat.                                                                                                                                                                                                                           | `sonus.command.groups.list`            |
| `/sonus group create <name> <type> [password]` | Creates a voice chat group with the given name and type. Type can be `OPEN`, `NORMAL` or `ISOLATED`. See [here](#room-types).                                                                                                                                                                                    | `sonus.command.groups.create`          |
| `/sonus group join <name> [password]`          | Joins a voice chat group with the given name. If the group is password protected, you will need to provide the password. If multiple groups with the same name exists and a password matches, you will join the respective group. Otherwise, you have to choose from a list of groups which share the same name. | `sonus.command.groups.join`            |
| `/sonus group invite <player>`                 | Invites a player to your current voice chat group. The player will receive a clickable message to join the group.                                                                                                                                                                                                | `sonus.command.groups.invite`          |
| `/sonus group leave`                           | Leaves your current voice chat group.                                                                                                                                                                                                                                                                            | `sonus.command.groups.leave`           |
| `/sonus moderate mute <player>`                | Mutes a player by setting their speak permission to false. The player will still be able to listen to the voice chat, but won't be able to speak.                                                                                                                                                                | `sonus.command.moderate.mute`          |
| `/sonus moderate unmute <player>`              | Unmutes a player be resetting their speak permission to unset.                                                                                                                                                                                                                                                   | `sonus.command.moderate.unmute`        |
| `/sonus moderate ban <player>`                 | Bans a player by setting their connect permission to false. The player won't be able to connect to the Sonus Service at all.                                                                                                                                                                                     | `sonus.command.moderate.ban`           |
| `/sonus moderate unban <player>`               | Unbans a player by resetting their connect permission to unset.                                                                                                                                                                                                                                                  | `sonus.command.moderate.unban`         |
| `/sonus moderate groups remove <group>`        | Removes a voice chat group. If multiple groups with the same name exist, you can choose which one to remove from a list.                                                                                                                                                                                         | `sonus.command.moderate.groups.remove` |
| `/sonus moderate groups kick <player>`         | Kicks a player from their current voice chat group.                                                                                                                                                                                                                                                              | `sonus.command.moderate.groups.kick`   |
| `/sonus moderate groups ban <player>`          | Bans a player from the voice chat group system by setting their using group permission to false. The player won't be able to join any voice chat group.                                                                                                                                                          | `sonus.command.moderate.groups.ban`    |
| `/sonus moderate groups unban <player>`        | Unbans a player from the voice chat group system by resetting their using group permission to unset.                                                                                                                                                                                                             | `sonus.command.moderate.groups.unban`  |

Note: `[...]` is an optional argument, `<...>` is a required argument

#### Room types

| Type       | Description                                                                       |
|------------|-----------------------------------------------------------------------------------|
| `OPEN`     | Players who are not in your group can hear you, and you can hear them too.        | 
| `NORMAL`   | Players who are not in your group can't hear you, but you can hear them.          |
| `ISOLATED` | Players who are not in your group can't hear you, and you can't hear them either. |

### Agent

The "Sonus Agent" will run on the backend servers of your network (currently only Paper + most Forks supported) and
will send metadata about the environment to the service. This also offers a simple API
which can be used by other plugins on the backend servers to e.g. play back music.

#### Agent API

The API is available at `dev.minceraft.sonus:sonus-agent-paper:2.0.0-SNAPSHOT` using
https://repo.minceraft.dev/releases/ as a maven artifact.

There is currently no documentation available for this API,
but you may be able to figure out stuff yourself
using [SonusAgentApi.java](agent-paper/src/main/java/dev/minceraft/sonus/agent/paper/api/SonusAgentApi.java) (registered
in Bukkit's ServicesManager) as a starting point.

### Web/WebRTC

Support for browsers can be activated in the Sonus Service configuration file,
where you can configure which address and port will be used for the websocket using `address`.
It is recommended to use a Domain (with TLS/SSL) for the websocket by reverse-proxying the port
Sonus binds on using a proper webserver (e.g. [Caddy](https://caddyserver.com/)).

The `link-pattern` will need to be set to a public instance
of [github.com/MinceraftMC/SonusWeb](https://github.com/MinceraftMC/SonusWeb), please see the respective repository for
more info about setting it up.

If you want to prevent exposing your backend server you will need to set up an
external TURN-server and configure it in the configuration file of the Sonus Service.
Be sure to read about how WebRTC and ICE works before trying to set this up.
Don't forget to set the `ice-transport-policy` to `ONLY_RELAY`.

After setting up web, players can use `/voicechat web` ingame to get a link for their voicechat session.

## Building

Before building, ensure [java](https://adoptium.net/) and [golang](https://go.dev/) are installed on your system.

1. Clone the project (`git clone https://github.com/MinceraftMC/Sonus.git`)
2. Go to the cloned directory (`cd Sonus`)
3. Build the jar (`./gradlew build` on Linux/MacOS, `gradlew build` on Windows)

The Sonus jar can be found in the `build` → `libs` directory.

## Support

If you need help with Sonus, feel free to ask in our [Discord](https://discord.gg/zC8xjtSPKC). We also welcome any
bug reports or feedback to help us improve Sonus.

## Limitations

Currently, no SimpleVoiceChat/PlasmoVoice extension plugins/mods are supported.
Extension plugins will need to be adapted to support Sonus.

### Contributing

If you want to contribute to Sonus, feel free to fork the repository and create a pull request.
Please make sure to follow the code style and conventions used in the project. If you have any questions or need help,
feel free to ask in our [Discord](https://discord.gg/zC8xjtSPKC).

You can test your changes by running `./gradlew :service-velocity:runVelocity` and `./gradlew :agent-paper:runPaper`. These
will start a local development servers with the compiled plugin automatically installed. This can be combined with
the debugger of your IDE. <br/>
