# szczur4 Minesweeper

#### This is my take at making a minesweeper game

___

### Controls

- Drag your mouse to move
- Scroll to zoom
- Right-click to place a flag
- Left-click to uncover a tile

___

### List of features

- Infinite playing field[^1]
- Automatic flag checking and tile uncovering
- World saving
- Tile uncovering animation
- Coordinate display
- Zoom

___

# Changelog

## Version 2.0

### Changes

- Rewrote the game engine
  - Changed the world structure (regions > chunks > tiles)
  - Made the playing field effectively infinite[^1] 
    - Removed `Width`, `Height` and `Mines` input fields because they are now unnecessary
- Rewrote the renderer
  - Added viewport culling
  - Added LOD optimization
  - The UI is now rendered as a HUD
- Added tile uncovering animation
- World is now automatically saved
- Removed `Flag` and `Check` buttons
  - Flags are now placed by right-clicking
  - Checking is now automatic
- Added `Zoom` feature
  - Zoom by scrolling
- Movement
  - Removed keyboard movement
  - Now you move by dragging your mouse
- Reworked textures
  - Textures are now 16 x 16
  - Added a texture for edges of uncovered zones
- Changed from Intellij compiler to Maven
- Added the thing that you are reading now

[^1]:### **2<sup>73</sup> x 2<sup>73</sup> tiles**

___

## Version 1.0

### Features

- Custom playing field size
- Custom number of mines
- Flag placing
  - Can be toggled by pressing the `Flag` button or `F` key
- Tile checking
  - Checks for every tile if the amount of flags surrounding the tile matches amount of surrounding mines
    - If yes, changes the texture of the tile and uncovers covered tiles around it
    - If not, it does nothing to that tile
  - Can be triggered by pressing the `Check` button or `C` key
- Movement
  - You can move with `W`, `A`, `S`, `D`
  - Pressing `Key + Ctrl` multiplies movement by x50
  - Pressing `Key + Shift` puts you at the edge of the map