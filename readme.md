### List of languages
- [English (en_us)](#en_us)
- [Polish (pl_pl)](#pl_pl)
# szczur4 Minesweeper
#### En_us
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
- Stats
- Chunk clearing
- Lost chunks unlocking
___
# Changelog
## Version 2.1 (latest)
### Changes
- You can now toggle automatic tile uncovering and texture changes when placing a flag
- Added stats
  - `Flags` - tells you how many flags do you have
  - `Cleared` - counts how many chunks you have cleared
  - `Lost` - counts how many times you have lost a chunk
- Added chunk clearing
  - A chunk is considered as "cleared" when all flags are placed correctly
  - After clearing, the amount of flags from that chunk is added to the `Flags` stat
- Cleared and lost chunks are now loaded correctly
- You can now unlock lost chunks for `Flags`
- Added Polish translation for the changelog
___
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
- World is now saved on shutdown
- Removed `Flag` and `Check` buttons
  - Flags are now placed by right-clicking
  - Checking is now automatic
- Added `Zoom` feature
  - Zoom by scrolling
- Movement
  - Removed keyboard movement
  - Now you move by dragging your mouse
- Reworked textures
  - Textures are now 16 × 16 instead of 10 × 10
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
  - You can move with `W`, `A`, `S` and `D`
  - Pressing `Key + Ctrl` multiplies movement by 50 times
  - Pressing `Key + Shift` puts you at the edge of the map
___
# szczur4 Saper
#### Pl_pl
#### Moja próba zrobienia gry sapera
___
### Sterowanie
- Przeciągnij myszką, aby się przemieścić
- Pokręć kółkiem, aby przybliżyć/oddalić
- Kliknij prawym, aby postawić flagę
- Kliknij lewym, aby odkryć płytkę
___
### Lista zawartości
- Nieskończony obszar gry[^1]
- Automatyczne sprawdzanie flag i odkrywanie płytek
- Zapisywanie świata
- Animacja odkrywania płytek
- Wyświetlanie koordynatów
- Przybliżanie
- Statystyki
- Czyszczenie chunków
- Odblokowywanie straconych chunków
___
# Lista zmian
## Wersja 2.1 (najnowsza)
### Zmiany
- Można przełączyć automatyczne odkrywanie płytek oraz zmiany tekstur przy staiwaniu flagi
- Dodano statystyki
  - `Flagi` - mówi Ci ile masz flag
  - `Wyczyszczone` - liczy iile chunków zostało wyczyszczonych
  - `Stracone` - liczy ile razy chunki zostały stracone
- Dodano czyszczenie chunków
  - Chunk jest uważany za "wyczyszczony" gdy wszystkie flagi są postawione prawidłowo
  - Po czyszczeniu ilość flag z tego chunka jest dodawana to statystyki `Flagi`
- Wyczyszczone i stracone chunki się ładują poprawnie
- Można odblokowywać chunki za `Flagi`
- Dodano polskie tłumaczenie dla listy zmian
___
## Wersja 2.0
### Zmiany
- Przepisano silnik gry
  - Zmieniono strukturę świata (regiony > chunki > płytki)
  - Obszar gry jest teraz efektywnie nieskończony[^1]
    - Usunięto obszary wejściowe `Szerokość`, `Wysokość` i `Miny`, ponieważ są już niepotrzebne
- Przepisano renderer
  - Dodano selekcję obszaru widoku
  - Dodano optymalizację "LOD"
  - UI jest teraz wyświetlane jako HUD
- Dodano animację odkrywania płytek
- Świat jest teraz zapisywany podczas wyłączania
- Usunięto przyciski `Flaga` i `Sprawdź`
  - Flagi są teraz stawiane prawym kliknięciem
  - Sprawdzanie jest teraz automatyczne
- Dodano `Przybliżanie`
  - Przybliżaj poruszając kółkiem myszy
- Poruszanie
  - Usunięto poruszanie za pomocą klawiatury
  - Teraz poruszasz się przeciągając mysz
- Przerobiono tekstury
  - Tekstury są teraz 16 × 16 zamiast 10 × 10
  - Dodano teksturę dla brzegów odkrytych obszarów
- Zmieniono kompilator Intellij na Maven
- Dodano to, co teraz czytasz
___
## Wersja 1.0
### Zawartość
- Dowolny rozmiar obszaru do gry
- Dowolna ilość min
- Stawianie flag
  - Może zostać przełączone klikając przycisk `Flaga` lub klawisz `F`
- Sprawdzanie płytek
  - Sprawdza dla każdej płytki czy liczba flag naokoło jest taka sama jak ilość min
    - Jeżeli tak, zmienia teksturę tej płytki i odkrywa płytki naokoło
    - Jeżeli nie, nic nie robi dla tej płytki
  - Może zostać wywołane poprzez kliknięcie przycisku `Sprawdź` lub klawisza `C`
- Poruszanie
  - Można się poruszać za pomocą `W`, `A`, `S` i `D`
  - Przytrzymanie `Klawisz + Ctrl` mnoży przemieszczenie 50-krotnie
  - Przytrzymanie `Klawisz + Shift` przesuwa Cię na koniec mapy