# Lavalogging
Bring balance to the fluids!
This mod extends Minecraft’s familiar waterlogging mechanic to lava, letting certain blocks safely coexist with molten rock instead of removing it.
## Features
- Adds lavalogging support to:
  -   \- Stairs
  -   \- Slabs
  -   \- Fences
  -   \- Walls
  -   \- Copper grates
  -   \- Anvils
  -   \- Iron bars
- By default, only non‑flammable variants (stone, metal, etc.) can be lavalogged.
- Behavior is fully configurable via a simple JSON file, so you can whitelist blocks to your taste.
## Why use it?
- Expands building possibilities in Nether bases and lava‑themed builds.
- Keeps parity with waterlogging.
- Lightweight, vanilla‑aligned, and designed for compatibility.
## FAQs
Q: Will this be ported to any other modloaders?\
  A: I do not _currently_ have plans to port this mod.
  
Q: How do I change which blocks are lavaloggable?\
  A: Inside the .jar file, there are 2 lavalog_eligible.json files (one for server side and one for client). You can add or remove blocks using their namespaced ids (minecraft:oak_stairs or yourModID:yourModBlock).
  
Q: If my mod's eligible blocks list is different from my server's, what will happen?\
  A: The mod will default to the server's eliglble list when in a multiplayer world.
