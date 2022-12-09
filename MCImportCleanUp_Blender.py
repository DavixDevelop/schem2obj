import bpy
import bmesh

import re

from bpy import context

def createImprovedWaterMaterial():
    #Create new material
    mat = bpy.data.materials.new('improved_water')
    mat.use_nodes = True

    if mat.node_tree:
        mat.node_tree.links.clear()
        mat.node_tree.nodes.clear()

    nodes = mat.node_tree.nodes
    links = mat.node_tree.links

    #Create output node
    output_node = nodes.new('ShaderNodeOutputMaterial')
    #Create mix shader node
    mix_shader = nodes.new('ShaderNodeMixShader')
    mix_shader.inputs['Fac'].default_value = 0.840532
    #Create transparent node
    transparent_node = nodes.new('ShaderNodeBsdfTransparent')
    #Connect transparent node with mix shader
    links.new(transparent_node.outputs['BSDF'], mix_shader.inputs[2])
    #Create second mix shader node
    mix_shader2 = nodes.new('ShaderNodeMixShader')
    #Connect second mix shader with first mix shader
    links.new(mix_shader2.outputs['Shader'], mix_shader.inputs[1])
    #Create refraction shader
    refraction_node = nodes.new('ShaderNodeBsdfRefraction')
    refraction_node.inputs['Color'].default_value = (205 / 255, 237 / 255, 1, 1)
    #Create fresnel node
    fresnel_node = nodes.new('ShaderNodeFresnel')
    fresnel_node.inputs['IOR'].default_value = 0.25
    #Connect frensel node to refraction node
    links.new(fresnel_node.outputs['Fac'], refraction_node.inputs['IOR'])
    #Connect fresnel node to second mix shader Fac
    links.new(fresnel_node.outputs['Fac'], mix_shader2.inputs['Fac'])
    #Connect refraction node to second mix shader
    links.new(refraction_node.outputs['BSDF'], mix_shader2.inputs[1]);
    #Create glossy node
    glossy_node = nodes.new('ShaderNodeBsdfGlossy')
    glossy_node.inputs['Color'].default_value = (205 / 255, 237 / 255, 1, 1)
    glossy_node.inputs['Roughness'].default_value = 0.2
    #Connect glossy node to second mix shader
    links.new(glossy_node.outputs['BSDF'], mix_shader2.inputs[2])
    #Connect mix shader to output node
    links.new(mix_shader.outputs['Shader'], output_node.inputs['Surface'])

    #Create multiply node
    multiply_node = nodes.new('ShaderNodeMath')
    multiply_node.operation = 'MULTIPLY'
    multiply_node.inputs[1].default_value = -0.2;
    #Create add node
    add_node = nodes.new('ShaderNodeMath');
    #Connect add and multiply node
    links.new(add_node.outputs[0], multiply_node.inputs[0])
    #Create second multiply node
    multiply_node2 = nodes.new('ShaderNodeMath')
    multiply_node2.operation = 'MULTIPLY'
    #Connect second multiply node with rhe add node
    links.new(multiply_node2.outputs[0], add_node.inputs[1])
    #Create noise texture node
    noise_texture_node = nodes.new('ShaderNodeTexNoise')
    noise_texture_node.inputs['Scale'].default_value = 2.0
    noise_texture_node.inputs['Detail'].default_value = 8.0
    noise_texture_node.inputs['Distortion'].default_value = 1.0
    #Connect noise texture to second multiply node
    links.new(noise_texture_node.outputs['Fac'], multiply_node2.inputs[1])
    #Create 2nd noise texture node
    noise_texture_node2 = nodes.new('ShaderNodeTexNoise')
    noise_texture_node2.inputs['Scale'].default_value = 5.0
    noise_texture_node2.inputs['Detail'].default_value = 8.0
    noise_texture_node2.inputs['Distortion'].default_value = 2.0
    #Connect 2nd noise texture node to add node
    links.new(noise_texture_node2.outputs['Fac'], add_node.inputs[0])
    #Connect multiply node to output node
    links.new(multiply_node.outputs[0], output_node.inputs['Displacement'])

    mat.blend_method = 'HASHED'

#increase transparent_max_bounces if there are still black spot's visible on transparent parts
for scene in bpy.data.scenes:
    scene.cycles.transparent_max_bounces = 8

POWER_VALUE = re.compile(r"^.+power_([0-9]+)")

#Set's the max emission value for the light value (0-15)
MAX_EMISSION_VALUE = 10.0

#Add's a bump map to each material that uses a normal texture
ADD_BUMP_MAP = True

#Set to true to use a more realistic material for water
IMPROVED_WATER_MATERIAL = False

if IMPROVED_WATER_MATERIAL:
    if 'improved_water' not in bpy.data.materials:
        createImprovedWaterMaterial()


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
                            bump_node.inputs['Strength'].default_value = 10.0

                            #Conntect the Alpha output of the texture image node to the Height input of the Bump node
                            links.new(normal_texture_node.outputs['Alpha'], bump_node.inputs['Height'])

                            #Remove the link between the Bsdf Principled and Normal Map
                            links.remove(normal_link)

                            #Connect the Normal Map output normal to the Bump Node "Normal" input socket
                            links.new(normal_map_node.outputs['Normal'], bump_node.inputs['Normal'])

                            #Connect the Bump node to the Bsdf Principled Normal Socket
                            links.new(bump_node.outputs['Normal'], node.inputs['Normal'])

            if "glass" in mat.name:
                #Check if node Bsdf Principled
                if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                    bsdf_rougness_node_link = node.inputs['Roughness']
                    if len(bsdf_rougness_node_link.links) == 0:
                        node.inputs['Roughness'].default_value = 0.0

                    bsdf_transmission_node_link = node.inputs['Transmission']
                    if len(bsdf_transmission_node_link.links) == 0:
                        node.inputs['Transmission'].default_value = 1.0

                    bsdf_specular_node_link = node.inputs['Specular']
                    if len(bsdf_specular_node_link.links) == 0:
                        node.inputs['Specular'].default_value = 0.5

                    #Get the NodeLink for the "BSDF" output socket
                    bsdf_output_node_link = node.outputs['BSDF']
                    #Get the bsdf link
                    bsdf_link = bsdf_output_node_link.links[0]
                    #Get the output node
                    material_node = bsdf_link.to_node

                    #Check if output node is of tpye materila output node
                    if type(material_node) is bpy.types.ShaderNodeOutputMaterial:
                        #Create mix shader
                        mix_shader = nodes.new('ShaderNodeMixShader')
                        #Create light path node
                        light_node = nodes.new('ShaderNodeLightPath')
                        #Connect light node `Is Shadow Ray` output with mix shader Fac
                        links.new(light_node.outputs['Is Shadow Ray'], mix_shader.inputs['Fac'])
                        #Create transparent node
                        transparent_node = nodes.new('ShaderNodeBsdfTransparent')
                        #Set the color of the transparent node to white
                        transparent_node.inputs['Color'].default_value = (1, 1, 1, 1)
                        #Connect transparent node to mix shader
                        links.new(transparent_node.outputs['BSDF'], mix_shader.inputs[2])
                        #Remove link betweeen principal and material output node
                        links.remove(bsdf_link)
                        #Connect princial node to mix shader
                        links.new(node.outputs['BSDF'], mix_shader.inputs[1])
                        #Connect mix shader to material output node
                        links.new(mix_shader.outputs['Shader'], material_node.inputs['Surface'])



            if "water_" in mat.name:
                #Check if node Bsdf Principled
                if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                    #Get the NodeLink for the "BSDF" output socket
                    bsdf_output_node_link = node.outputs['BSDF']
                    #Get the bsdf link
                    bsdf_link = bsdf_output_node_link.links[0]
                    #Get the material output node
                    material_node = bsdf_link.to_node

                    bsdf_rougness_node_link = node.inputs['Roughness']
                    if len(bsdf_rougness_node_link.links) == 0:
                        node.inputs['Roughness'].default_value = 0.0

                    bsdf_IOR_node_link = node.inputs['IOR']
                    if len(bsdf_IOR_node_link.links) == 0:
                        node.inputs['IOR'].default_value = 1.33

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

                        links.new(diffuse_texture_node.outputs['Alpha'], node.inputs['Transmission'])



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

if IMPROVED_WATER_MATERIAL:
    if 'Water' in bpy.data.objects:
        water_obj = bpy.data.objects['Water']
        if water_obj is not None:
            improved_water_material = bpy.data.materials['improved_water']
            for m in water_obj.material_slots:
                m.material = improved_water_material
    else:
        selection = bpy.context.selected_objects
        improved_water_material = bpy.data.materials['improved_water']
        for o in selection:
            for m in o.material_slots:
                if "water_flow" in m.material.name or "water_still" in m.material.name:
                    m.material = improved_water_material


#Reset the normals in every selected object
#selection = bpy.context.selected_objects

#for o in selection:
#    bpy.context.view_layer.objects.active = o
#    try:
#        bpy.ops.mesh.customdata_custom_splitnormals_clear()
#    except Exception as ex:
#        print(ex)