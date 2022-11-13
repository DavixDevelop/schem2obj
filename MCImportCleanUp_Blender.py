import bpy
import bmesh

import re

from bpy import context


#increase transparent_max_bounces if there are still black spot's visible on transparent parts
for scene in bpy.data.scenes:
    scene.cycles.transparent_max_bounces = 32

POWER_VALUE = re.compile(r"^.+power_([0-9]+)")

MAX_EMISSION_VALUE = 2.0

ADD_BUMP_MAP = True

# Run through all materials of the current blend file
for mat in bpy.data.materials:
    # If the material has a node tree
    if mat.node_tree:
        
        # Check if material name has power in it
        isPowered = "power" in mat.name 
        emission = 0.0
        
        if isPowered:
            m = POWER_VALUE.match(mat.name)
            if m:
                val = int(m.group(1))
                if val > 0:
                    emission = (val / 16) * MAX_EMISSION_VALUE

        if "sea_lantern" in mat.name or "glowstone" in mat.name or "sea_lantern" in mat.name or "pumpkin_face_on" in mat.name or "lamp_on" in mat.name or "fire" in mat.name or "beacon" in mat.name:
            emission = (15 / 16) * MAX_EMISSION_VALUE

        if "lava_" in mat.name:
            emission = (64 / 16) * MAX_EMISSION_VALUE

        if "end_rod" in mat.name or "torch_on" in mat.name:
            emission = (14 / 16) * MAX_EMISSION_VALUE

        if "furnace_front_on" in mat.name:
            emission = (13 / 16) * MAX_EMISSION_VALUE

        if "lit_redstone_ore" in mat.name or "comparator_on" in mat.name or "repeater_on" in mat.name:
            emission = (9 / 16) * MAX_EMISSION_VALUE

        if "torch_on" in mat.name:
            emission = (8 / 16) * MAX_EMISSION_VALUE

        if "magma" in mat.name:
            emission = (4 / 16) * MAX_EMISSION_VALUE

        if "mushroom_block_inside" in mat.name:
            emission = (1 / 16) * MAX_EMISSION_VALUE
        
        isLit = "lit_" in mat.name
        
        # Run through all nodes
        nodes = mat.node_tree.nodes
        links = mat.node_tree.links
        for node in nodes:
            if ADD_BUMP_MAP:
                #Check if node Bsdf Principled
                if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                    #Get the NodeLink for the "Normal" socket
                    normal_map_input = node.inputs["Normal"]
                    #Check if the socket is connected
                    if len(normal_map_input.links):
                        #Get the first link for the socket
                        normal_link = normal_map_input.links[0]
                        #Check if the link is connected to a Normal Map
                        if type(normal_link.from_node) is bpy.types.ShaderNodeNormalMap:
                            #Get the Normal Map node
                            normal_map_node = normal_link.from_node
                            #Get the link for the Color socket
                            normal_map_color_link = normal_map_node.inputs["Color"].links[0]

                            #Get the texture image node for the normal
                            normal_texture_node = normal_map_color_link.from_node

                            #Create a new Bump node
                            bump_node = nodes.new('ShaderNodeBump')

                            #Conntect the Alpha output of the texture image node to the Height input of the Bump node
                            links.new(normal_texture_node.outputs['Alpha'], bump_node.inputs['Height'])

                            #Remove the link between the Bsdf Principled and Normal Map
                            links.remove(normal_link)

                            #Connect the Normal Map output normal to the Bump Node "Normal" input socket
                            links.new(normal_map_node.outputs['Normal'], bump_node.inputs['Normal'])

                            #Connect the Bump node to the Bsdf Principled Normal Socket
                            links.new(bump_node.outputs['Normal'], node.inputs['Normal'])

            if "water_" in mat.name:
                #Check if node Bsdf Principled
                if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                    #Get the NodeLink for the "BSDF" output socket
                    bsdf_output_node_link = node.outputs['BSDF']
                    #Get the bsdf link
                    bsdf_link = bsdf_output_node_link.links[0]
                    #Get the material output node
                    material_node = bsdf_link.to_node

                    #Get the NodeLink for the "Volume" input socket
                    volume_input_node_link = material_node.inputs['Volume']
                    #Check if the socket is not connected
                    if len(volume_input_node_link.links) == 0:
                        #Create principal volume node
                        principal_volume_node = nodes.new('ShaderNodeVolumePrincipled')
                        #Set It's color to black
                        principal_volume_node.inputs['Color'].default_value = (0, 0, 0, 1)
                        #Set the Density
                        principal_volume_node.inputs['Density'].default_value = 0.11;
                        
    	                #Get the NodeLink for the "Base Color" input socket of the ShaderNodeBsdfPrincipled
                        base_color_node_link = node.inputs['Base Color']
                        #Get the base color link
                        base_color_link = base_color_node_link.links[0]
                        diffuse_texture_node = base_color_link.from_node

                        #Set the Absorption Color to the Image the diffuse texture
                        links.new(diffuse_texture_node.outputs['Color'], principal_volume_node.inputs['Absorption Color'])

                        #Connect the material node volume socket to the principal volume node
                        links.new(principal_volume_node.outputs['Volume'], material_node.inputs['Volume'])



            if emission > 0.0:
                if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                    node.inputs["Emission Strength"].default_value = emission    
            elif isLit:
                if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                    current = node.inputs["Emission Strength"].default_value
                    if current > 0.0:
                        node.inputs["Emission Strength"].default_value = (15/ 16) * MAX_EMISSION_VALUE
            
            # If the node type is texture 
            if node.type == 'TEX_IMAGE':
                # Set the interpolation -> Linear, Closest, Cubic, Smart
                node.interpolation = 'Closest' 

        #Now run through all node link
        for link in links:
            #Check if link is connected to a normal map node
            if type(link.to_node) is bpy.types.ShaderNodeNormalMap: 
                if type(link.from_node) is bpy.types.ShaderNodeTexImage:
                    imgNode = link.from_node
                    imgNode.image.colorspace_settings.name = 'Non-Color'




#Reset the normals in every selected object
selection = bpy.context.selected_objects

#for o in selection:
#    bpy.context.view_layer.objects.active = o
#    try:
#        bpy.ops.mesh.customdata_custom_splitnormals_clear()
#    except Exception as ex:
#        print(ex)
