# Conch Minecraft Admin Plugin

A bottom-bar admin console for Paper Minecraft servers managed by Cube Coders AMP, running inside the Conch Workbench IDE.

<!-- screenshot goes here -->

## What it is

Conch Minecraft Admin adds a persistent tool window to the Conch Workbench that lets you monitor and control one or more Paper Minecraft servers without leaving your IDE. It pulls live status from the AMP panel REST API (server state, CPU, RAM, uptime, player count, TPS) and fires RCON commands for player lists, broadcasts, and arbitrary console input. Crash events generate balloon notifications. Multiple named server profiles are supported, each with vault-backed credentials so passwords are never stored in plain text.

The plugin is a satellite of the Conch Workbench platform. It loads inside Conch (or any IntelliJ-based IDE that has Conch's core and vault plugins installed) and depends on Conch's credential vault for all password resolution at runtime.

## Prerequisites

- **Conch Workbench** — required. The plugin declares `<depends>com.conch.core</depends>` and `<depends>com.conch.vault</depends>` in its `plugin.xml`, so it will not load in a stock IntelliJ IDEA without those plugins present.
- **Cube Coders AMP** — a running AMP panel managing at least one Paper Minecraft instance.
- **Paper Minecraft server** with RCON enabled (`enable-rcon=true`, `rcon.port`, `rcon.password` in `server.properties`). RCON is used for the player name list, TPS readings, broadcast, and arbitrary command input.

## Features

- Live server status (Running / Stopped / Starting / Stopping / Crashed) from AMP
- CPU usage, RAM usage, and uptime metrics from AMP
- Player count and TPS from AMP's instance metrics
- Online player names list fetched via RCON (`list` command)
- Lifecycle controls: Start, Stop, Restart, Backup
- Live console tail from AMP's instance console endpoint
- Broadcast and arbitrary RCON command input
- Multiple named server profiles with vault-backed credentials
- Crash detection and balloon notifications in the IDE
- Responsive tool window that works docked at the bottom stripe or as a floating/undocked window (use the tool window gear menu)

## Installation

1. Download the latest `termlabs-amp-minecraft-admin-plugin-<version>.zip` from the [Releases](../../releases) page.
2. In Conch: **Settings → Plugins → gear icon → Install Plugin from Disk**, then select the zip.
3. Restart Conch when prompted.

## Configuration

1. Open the **Minecraft Admin** tool window from the bottom stripe (look for the server icon).
2. Click **+** to add a server profile.
3. Fill in:
   - **AMP URL** — the base URL of your AMP panel (e.g., `http://amp.example.com:8080`)
   - **AMP Username** — your AMP login name
   - **AMP Credential** — pick an entry from the Conch vault that holds your AMP password
   - **RCON Host** — hostname or IP of the Minecraft server
   - **RCON Port** — default is `25575`
   - **RCON Credential** — pick an entry from the Conch vault that holds your RCON password
4. Click **Save**. The status strip should populate within a few seconds.

To switch between profiles, use the dropdown in the tool window header. To edit or delete a profile, use the gear icon.

## Building from source

```bash
./gradlew buildPlugin
# Output: build/distributions/termlabs-amp-minecraft-admin-plugin-0.1.0.zip
```

The build targets IntelliJ Community 2024.3 using a JDK 21 toolchain. Gradle 9.4.1 is pinned via the wrapper and is self-bootstrapping — no local Gradle installation needed. (Note: Java 25 is supported as the host JVM; the plugin sources target JDK 21 via Gradle's toolchain.)

## Running tests

```bash
./gradlew test
```

The test suite is 83 tests covering the AMP client, RCON client and packet codec, crash detector, server poller, state merger, persistence layer, and Swing panel unit tests.

## Diagnostic scripts

Two Python 3 scripts help verify that the remote endpoints are reachable and the credentials are correct before configuring the plugin:

### `scripts/amp-probe.py`

Exercises the AMP REST endpoints that the plugin uses against a live AMP panel.

```
Env vars:
  AMP_URL       AMP panel URL        (default: http://localhost:8080)
  AMP_USERNAME  AMP panel username   (required)
  AMP_PASSWORD  AMP panel password   (required)
  AMP_INSTANCE  Instance name        (optional; GetInstances still runs)
```

Example:

```bash
AMP_USERNAME=admin AMP_PASSWORD=secret AMP_INSTANCE=survival python3 scripts/amp-probe.py
```

### `scripts/rcon-probe.py`

Verifies RCON authentication and command dispatch against a live server.

```
Env vars:
  RCON_HOST      RCON host     (default: localhost)
  RCON_PORT      RCON port     (default: 25575)
  RCON_PASSWORD  RCON password (required)
  RCON_COMMAND   Command       (default: list)
```

Example:

```bash
RCON_PASSWORD=secret python3 scripts/rcon-probe.py
RCON_HOST=mc.example.com RCON_PORT=25575 RCON_PASSWORD=secret RCON_COMMAND='tps' python3 scripts/rcon-probe.py
```

## Relationship to Conch

This plugin is a satellite of the [Conch Workbench](https://github.com/an0nn30/conch_workbench) IDE platform.

At runtime, Conch's vault plugin (`com.conch.vault`) provides a `CredentialProvider` implementation through IntelliJ's extension point mechanism. The plugin looks up that implementation to resolve AMP and RCON passwords from the vault — passwords are never stored in the plugin's own persistence layer.

The `com.conch.sdk.CredentialProvider` interface is mirrored as a compile-time stub in `src/main/java/com/conch/sdk/`. It is excluded from the packaged plugin jar (see `tasks.jar { exclude("com/conch/sdk/**") }` in `build.gradle.kts`) so that only one copy of the class exists at runtime — the real one provided by Conch. If the upstream Conch SDK ever changes that interface, re-copy the file from `conch_workbench/sdk/src/com/conch/sdk/CredentialProvider.java`.

## Design notes

See [`docs/design.md`](docs/design.md) for the full design specification, including AMP API integration notes, RCON packet codec details, credential resolution flow, and the tool window layout decisions.

## License

Apache 2.0 — see [LICENSE](LICENSE).
