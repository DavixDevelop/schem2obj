## Schem2Obj

A java program for converting Minecraft 1.12 schematics to OBJ (Wavefront)
with support for multiple resource packs (only in SEUS and Vanilla Format for now).  The MTL file uses
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

### Supported entities
- Beds
- Banners (Colored and patterns)
- Signs (ASCII and Unicode glyphs)
- Minecarts
- Skull's & Player Heads
- Chests (Including Christmas chests)

### Supported resource packs
For now, only resource packs that use the SEUS or Vanilla format are supported. Beside using the textures the resource pack includes,
it also reads the models & block states the resource pack defines. It is recommended to unzip the resource pack beforehand, but zipped resource packs are supported as well.
<br>To add a resource pack or multiple ones, first add the optional parameter `-t`, specify the format, ex. `SEUS:` and afterwards specify the path to the resource pack.
<br> Then to add any additional resource pack separate them with a space. Ex:

`-t SEUS:C:\Users\user\AppData\Roaming\.minecraft\resourcepacks\PBRResourcePack SEUS:C:\Users\user\AppData\Roaming\.minecraft\resourcepacks\PBRResourcePack2.zip`

> Note: If the path contains a space, wrap each parameter value in quotation marks. Ex:<br>
> `-t "SEUS:C:\Users\user name\AppData\Roaming\.minecraft\resourcepacks\PBRResourcePack" "SEUS:C:\Users\user name\AppData\Roaming\.minecraft\resourcepacks\PBRResourcePack2.zip"`

### Usage
In terminal run: `java -jar (<path-to-jar>/)schem2obj-<release version>.jar -minecraftFolder <minecraft path> -i <input schematic> -o <output obj> (Optional parameters: -t <resource pack path> -allBlock -snowy)`
<br>The paths can either be absolute or relative (must start with `.`).

Required parameters:
- `-minecraftFolder` The absolute path to the minecraft installation game directory.<br> 
> **Make sure the installation has the 1.12.2 version installed. You can check this if the file `versions/1.12.2/1.12.2.jar` exist inside the game directory.**<br>The reason why the program needs access to the `1.12.2.jar` file is because It's against the Minecraft EULA to provide the default textures and models in the source code itself.
- `-i` The absolute or relative path to the Schematic
- `-o` The absolute or relative path to the OBJ file<br>

Optional parameters:
- `-t` The absolute or relative path to the resource pack (folder with `pack.mcdata` or zipped resource pack)
- `-allBlocks` If set, all blocks (including hidden) are exported
- `-snowy` If set, only the snowy grass is generated for now
- `-christmasChests` If set, all chests (except ender chests) use the Christmas texture

> Note: Marked with (some parameter) are optional parts, and the actual inputs with <some value>. Remove the <> and () before running the command in a terminal.
> <br>Ex with both jar, schematic and output OBJ file in same folder, and the unpacked resource pack (with the resource pack only in the SEUS or Vanilla format for now) in the .minecraft folder:<br>
> `java -jar schem2obj-0.1.9.jar -minecraftFolder C:\Users\user\AppData\Roaming\.minecraft -i .lava_test.schematic -o .lava_test.obj -t SEUS:C:\Users\user\AppData\Roaming\.minecraft\resourcepacks\PBRResourcePack`


### Blender 3.4 Import Procedure

To import the generated OBJ into Blender 3.4 for example, create a new collection and import the obj into the newly created collection (select the collection, `File->Import->Wavefront (.obj)`).

Once It's imported, to properly set up the import, use the companion `MCImportCleanUp_Blender.py` script (Go to scripting tab, open the script and run it with `Alt+P`).

> Currently in development.