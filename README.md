# Sonus

A universal cross-server voicechat service for [SimpleVoiceChat](https://github.com/henkelmax/simple-voice-chat),
[PlasmoVoice](https://github.com/plasmoapp/plasmo-voice) and the web.

Discord for support: https://discord.gg/zC8xjtSPKC

## Setup

There are no releases yet (though there will be soon), you will need to download a build
from [GitHub Actions](https://github.com/MinceraftMC/Sonus/actions) or compile it yourself using `./gradlew build`.

### Service

The Sonus Service will need to be installed on your network proxy (currently only Velocity supported)
and will connect with the players (provided they are using either SimpleVoiceChat or PlasmoVoice).
There is currently no standalone version of this service.

You will need to have a public ip address and port. Both SimpleVoiceChat and PlasmoVoice use UDP
for communication.

The `bind` in the configuration file specifies on which address/port the voicechat will be bound on.
Use `0.0.0.0` to listen on every available network address.

The `host` in the configuration file will be sent to clients and tells them where they can reach your Sonus service.
The ip address/hostname specified here should be reachable from all your players.

### Agent

The "Sonus Agent" will run on the backend servers of your network (currently only Paper + most Forks supported) and
will send metadata about the environment to the service. This also offers a simple API
which can be used by other plugins on the backend servers to e.g. play back music.

### Web/WebRTC

To establish connections with web browsers, Sonus uses [pion](https://pion.ly/) to implement a WebRTC media server.

Support for browsers can be activated in the Sonus Service configuration file,
where you can configure which address and port will be used for the websocket using `address`.
It is recommended to use a Domain (with TLS/SSL) for the websocket by reverse-proxying the port
Sonus binds on using a full webserver (e.g. [Caddy](https://caddyserver.com/)).

The `link-pattern` will need to be set to a public instance
of [github.com/MinceraftMC/SonusWeb](https://github.com/MinceraftMC/SonusWeb), please see the respective repository for
more info about setting it up.

If you want to prevent exposing your backend server you will need to set up an
external TURN-server and configure it in the configuration file of the Sonus Service.
Be sure to read about how WebRTC and ICE works before trying to set this up.
Don't forget to set the `ice-transport-policy` to `ONLY_RELAY`.

After setting up web, players can use `/voicechat web` ingame to get a link for their voicechat session.

## Agent API

The API is available at `dev.minceraft.sonus:sonus-agent-paper:2.0.0-SNAPSHOT` using
https://repo.minceraft.dev/releases/ as a maven artifact.

There is currently no documentation available for this API,
but you may be able to figure out stuff yourself
using [SonusAgentApi.java](agent-paper/src/main/java/dev/minceraft/sonus/agent/paper/api/SonusAgentApi.java) (registered
in Bukkit's ServicesManager) as a starting point.
