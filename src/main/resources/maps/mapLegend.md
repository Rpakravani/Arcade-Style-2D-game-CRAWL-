# ASCII Map Legend (Team 7)

This project uses a simple ASCII text file to define a dungeon map

## Coordinate system
- The map is a rectangle of characters
- Each character = 1 tile (1 cell unit)
- `x` increases to the right (columns)
- `y` increases downward (rows)

## Symbols
- `#` = Wall (not walkable)
- `.` = Floor (walkable)
- `B` = Barrier (not walkable)
- `S` = Start (walkable floor; also sets Board startPosition)
- `E` = Exit (walkable floor; also sets Board endPosition)

## Future symbols (reserved for later phases / other members)
These may appear later once gameplay logic is integrated:
- `K` = Key reward (walkable; treated as floor + reward by game logic)
- `T` = Trap punishment (walkable; treated as floor + punishment by game logic)
- `G` = Goblin spawn (walkable floor; enemy created by game engine)
- `O` = Ogre spawn (walkable floor; enemy created by game engine)

## Rules
- Map must be rectangular (all lines same length)
- Map must contain exactly one `S` and one `E`
- Unknown characters should cause a loader error