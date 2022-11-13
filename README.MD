## Schem2Obj

 A java program for converting Minecraft 1.12 schematics to OBJ (Wavefront)
with support for multiple resource packs (only in SEUS Format for now). The MTL file uses
the PBR .mtl extension, so for now importing of MTL files with all PBR textures (when resource pack is used) is only supported
on Blender 3.4 Alpha.

### Supported blocks
All basic blocks (that are only basic cubes), and some builtin blocks (ex, water and lava) are supported. Other built in blocks (ex, bed, player head...) or blocks that don't work are currently being implemented.
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

### Usage
In terminal run: `java -jar (<path-to-jar>/)schem2obj-<release version>.jar -i <input schematic> (-t <resource pack path>) -o <output obj> (Optional parameters: -allBlock -snowy)`
<br>The paths can either be absolute or relative (must start with `.`).
Optional parameters:<br>
- `-t` The absolute or relative path to the resource pack (folder with `pack.mcdata`)
- `-allBlocks` If set, all blocks (including hidden) are exported
- `-snowy` If set, only the snowy grass is generated for now

> Note: Marked with (some parameter) are optional parts, and the actual inputs with <some value>. Remove the <> and () before running the command in a terminal. 
> <br>Ex with both jar, schematic and output OBJ file in same folder, and the unpacked resource pack (with the resource pack only in the SEUS format for now) in the .minecraft folder:<br>
> `java -jar schem2obj-0.1.9.jar -i .lava_test.schematic -t SEUS:C:\Users\user\AppData\Roaming\.minecraft\resourcepacks\PBRResourcePack -o .lava_test.obj`


### Blender 3.4 Import Procedure

To import the generated OBJ into Blender 3.4 for example, create a new collection and import the obj into the newly created collection (select the collection, `File->Import->Wavefront (.obj)`).

Once It's imported, to properly set up the import, use the companion `MCImportCleanUp_Blender.py` script (Go to scripting tab, open the script and run it with `Alt+P`).

> Currently in development.