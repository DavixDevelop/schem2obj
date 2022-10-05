A java program for converting Minecraft 1.12 schematics to OBJ (Wavefront)
with support for multiple resource packs (only in SEUS Format for now). The MTL file uses
the PBR .mtl extension, so for now importing of MTL files with all PBR texturs (when resource pack is used) is only supported
on Blender 3.4 Alpha.

Usage:`schem2obj -i <input schematic> (-t <resource pack path>) -o <output obj> (-allBlocks)`
<br>The paths can either be absolute or relative (must start with `.`).
Optional parameters:<br>
- `-t` The absolute or relative path to the resource pack (folder with `pack.mcdata`)
- `-allBlocks` If set, all blocks (including hidden) are exported

All basic blocks (that are only basic cubes) are supported. The ones that do use only a basic cube and are not working properly or rely on parent blocks to work properly (ex. glass panes) are currently being implemented. 
<br>List of custom block implemented:
- Grass Block (non-snowy and snowy)

Currently in development.