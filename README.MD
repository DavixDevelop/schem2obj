A java program for converting Minecraft 1.12 schematics to OBJ (Wavefront)
with support for multiple resource packs (only in SEUS Format for now). The MTL file uses
the PBR .mtl extension, so for now importing of MTL files with all PBR texturs (when resource pack is used) is only supported
on Blender 3.4 Alpha.

Usage:`schem2obj -i <input schematic> (-t <resource pack path>) -o <output obj> (-allBlocks)`
<br>The paths can either be absolute or relative (must start with `.`).
Optional parameters:<br>
- `-t` The absolute or relative path to the resource pack (folder with `pack.mcdata`)
- `-allBlocks` If set, all blocks (including hidden) are exported
- `-snowy` If set, only the snowy grass is generated for now

To import the generated OBJ into Blender 3.4 for example, create a new collection and import the obj into the newly created collection.
Once It's imported, to properly set up the import, use the companion `MCImportCleanUp_Blender.py` script

All basic blocks (that are only basic cubes), and some built blocks (ex, water and lava) are supported. Other built in blocks (ex, bed, player head...) or blocks that don't work are currently being implemented. 
<br>List of custom blocks implemented:
- Grass Block (non-snowy and snowy)
- Glass Pane & Block (regular and stained)
- Stairs
- Doors
- Fences
- Redstone Dust
- Slabs
- Iron Bars
- Water
- Lava
- Fire
- Leaves
- Cauldron
- Command Block
- Lit Furnace
- Lit Pumpkin
- Rough Prismarine Block
- Sea Lantern
- Magma Block (fixed compressed texture)

Currently in development.