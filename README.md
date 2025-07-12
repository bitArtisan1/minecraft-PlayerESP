# Minecraft PlayerESP (PESP) - Fabric

A client-side Minecraft mod that provides ESP (Extra Sensory Perception) functionality for players.

<div align="center">
  <img src="https://github.com/user-attachments/assets/e1273ab9-5515-4401-b3e7-20b85bec2e57" alt="Description" width="400">
  <br>
  <i>Your PlayerESP Mod For Minecraft- PESP</i>
</div>

---

## Features

### Core Features
- **Player Name ESP** - Shows player names through walls with distance-based scaling
- **Player Hitbox ESP** - Renders colored hitbox outlines around players
- **Whitelist System** - Filter ESP to show only specific players
- **Configuration GUI** - In-game whitelist management interface
- **Command System** - Full command-line control with tab completion

### Additional Features
- Toggle features independently with keybinds
- Color-coded hitboxes based on player name formatting
- Search and filter players in whitelist GUI
- Mod Menu integration for easy access
- Custom color override system

---

## Controls

| Key | Function |
|-----|----------|
| `H` | Toggle Hitbox ESP |
| `N` | Toggle Name ESP |
| `J` | Open Whitelist Management |

---

## Commands

All commands use the `/pesp` prefix:

| Command | Description |
|---------|-------------|
| `/pesp on` | Enable both hitbox and name ESP |
| `/pesp off` | Disable both ESP features |
| `/pesp color <r> <g> <b> [a]` | Set custom ESP color (0-1 floats) |
| `/pesp whitelist <players...>` | Add players to whitelist |
| `/pesp whitelist-remove <players...>` | Remove players from whitelist |
| `/pesp whitelist-clear` | Clear entire whitelist |
| `/pesp status` | Show current ESP configuration |
| `/pesp help` | Display command help |

### Command Examples
```
/pesp whitelist Steve Alex Notch
/pesp color 1.0 0.5 0.0       # Orange color
/pesp color 0.0 1.0 0.0 0.5   # Semi-transparent green
/pesp whitelist-remove Steve
```

**Note:** Commands support tab completion for player names and will suggest online players for whitelist commands.

---

## Installation

### Requirements
| Component | Version |
|-----------|---------|
| Minecraft | 1.21.4 |
| Fabric Loader | 0.15.0+ |
| Fabric API | 0.112.0+1.21.4 |
| Java | 21+ |

### Optional Dependencies
- **Mod Menu** (13.0.3+) - For configuration menu integration

### Installation Steps
1. Install Fabric Loader for Minecraft 1.21.4
2. Download and install Fabric API
3. Place the mod jar in your `.minecraft/mods/` folder
4. Launch Minecraft with Fabric profile

---

## Building & Development

```bash
./gradlew build
```

Built jars will be in `build/libs/`

Run test:
```bash
./gradlew runClient
```
---

## Development Note

This mod was built for personal use. I may maintain it for future Minecraft versions, but no guarantees. Feel free to fork and modify for your own builds if you need support for other mod loaders or Minecraft versions.

---

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

**Author:**
