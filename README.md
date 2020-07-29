# RenPy Cheat Generator

> I created this to make it easier to make custom cheat menus for RenPy games, its basically a text editor where you can
> make a menu and open it ingame.

The only requirement is that you know the name of the variables.

## Features:
- Enables console.
- Get list of current used variables + values to file "Game Variables.txt" in game root dir 
(Default to 'M' if enabled in settings)
- Adds editable shortcut to console (Default to 'Shift+O') and cheat menu (Default to 'Shift+C').
- Add variables and submenus with simple button click. (Submenus cant be placed in submenus)
- Drag & Drop to modify order of items.
- Text editor, for the more advanced users, its basically a bare bone script system.
- Generate the button with generate a menu which will be copied to the clipboard.
- Install will create a file named 'CustomCheatMenu.rpy' at selected location.

### Variable types:
- **variable_name(str)** (Will make a menuitem with the selected variable and ask for input ingame)
- **variable_name(str);custom_text** (Same as above, however will have custom text)
- **variable_name(int)=500** (Fixed variable, will set value to specify)
- **variable_name(int)=500;custom_text** (Same as before, will show custom text)
- **\<submenu_title** (Creates a submenu, everything between start and end will be added to submenu)
- **\>** (Ends the submenu)

### Warning:
Use the cheats generated with care, as you might break something in your game if you modify the wrong variables.

Using the function to write game variables to a file is experimental, please be cautious while using it.

Feedback is much appreciated.

(Preview game is 'Corruption')

## Developer notes:
Although application mimic the original, it has some differences:
- Variables now have types defined (string, integer and float)
- String variables can have quotes (single quotes or double quotes)
- Builder is a tree style and so no need for special "End of submenu" marker
- Column built-in sorting and resizing
- When installing, select game directory, and cheat will be placed at correct location
- I would recommend grouping variables by submenus, as excess of variables will be offscreen

based on [this f95 thread](https://f95zone.to/threads/renpy-cheat-generator.14104/) 

(thanks to [@Aziien](https://f95zone.to/members/aziien.13215/))

## TODO
- Improve a visual hint where dropped item will be placed, as it's difficult to aim
  - (should think of the way how to do it)
- ~~Visually separate submenus~~

## The latest changes
- 05.08.2020 18:00 - First final usable version.
- 10.08.2020 13:00 - Packaging into exe.
- 10.08.2020 20:00 - Save&Load feature. Rearranged settings.
- 10.08.2020 23:00 - Color if size is too big.
- 12.08.2020 15:00 - Visually separate submenus.
- 12.08.2020 22:00 - Changed colors (and moved them to stylesheets).
- 29.08.2020 22:00 - Added extended console dump.
- 19.09.2020 17:00 - Added history rollback option (use of mouse wheel).
- 21.09.2020 23:00 - Directory chooser switched to tree dialog (custom).
- 02.10.2020 18:00 - Directory chooser expansion bug.
- 05.10.2020 23:00 - Directory chooser directory changes watching.

### List of tested games
- Corruption

