import bpy
import re

#increase transparent_max_bounces if there are still black spot's visible on transparent parts
for scene in bpy.data.scenes:
    scene.cycles.transparent_max_bounces = 8

POWER_VALUE = re.compile(r"^.+power_([0-9]+)")

#Set's the max emission value for the light value (0-15)
MAX_EMISSION_VALUE = 10.0

#Add's a bump map to each material that uses a normal texture
ADD_BUMP_MAP = True

#Set to True to use a more realistic material for water
IMPROVED_WATER_MATERIAL = True

#Set to True to use a more realistic material for glass blocks and panes
IMPROVED_GLASS_MATERIAL = True

#Set the interpolation of the images, default Closest (possible values: Closest, Linear, Cubic, Smart)
TEXTURE_INTERPOLATION = 'Closest'

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

def create_minecraft_glass_shader(name, is_clear):
    #create group
    glass_shader_group = bpy.data.node_groups.new(name, 'ShaderNodeTree')

    #add group inputs
    glass_shader_group.interface.new_socket(name='Base Color', in_out='INPUT', socket_type='NodeSocketColor')
    glass_shader_group.interface.new_socket(name='Alpha', in_out='INPUT', socket_type='NodeSocketFloat')
    glass_color_bright_socket = glass_shader_group.interface.new_socket(name='Glass Color Bright', in_out='INPUT', socket_type='NodeSocketFloat')
    
    shadow_bright_socket = None
    shadow_contrast_socket = None

    if not is_clear:
        shadow_bright_socket = glass_shader_group.interface.new_socket(name='Shadow Bright', in_out='INPUT', socket_type='NodeSocketFloat')
        shadow_contrast_socket = glass_shader_group.interface.new_socket(name='Shadow Contrast', in_out='INPUT', socket_type='NodeSocketFloat')
    
    glass_roughness_socket = glass_shader_group.interface.new_socket(name='Glass Rougness', in_out='INPUT', socket_type='NodeSocketFloat')

    group_input = glass_shader_group.nodes.new('NodeGroupInput')
    group_input.location = (-706, 0)

    glass_color_bright_socket.default_value = 1.000
    glass_color_bright_socket.min_value = 0.000
    glass_color_bright_socket.max_value = 100.000

    if not is_clear:
        shadow_bright_socket.default_value = 0.510
        shadow_bright_socket.min_value = 0.000
        shadow_bright_socket.max_value = 100.000

        shadow_contrast_socket.default_value = 0.770
        shadow_contrast_socket.min_value = 0.000
        shadow_contrast_socket.max_value = 100.000
    
    glass_roughness_socket.default_value = 0.100
    glass_roughness_socket.min_value = 0.000
    glass_roughness_socket.max_value = 1.000
    glass_roughness_socket.subtype = 'FACTOR'

    #add group outputs
    
    glass_shader_group.interface.new_socket(name='Shader', in_out='OUTPUT', socket_type='NodeSocketShader')
    group_output = glass_shader_group.nodes.new('NodeGroupOutput')
    group_output.location = (744, 0)

    #add mix shader node
    mix_shader = glass_shader_group.nodes.new('ShaderNodeMixShader')
    mix_shader.location = (500, 0)

    #add glass bsdf shader node
    glass_shader = glass_shader_group.nodes.new('ShaderNodeBsdfGlass')
    glass_shader.location = (174, 0)
    glass_shader.inputs['Roughness'].default_value = 0.03
    glass_shader.inputs['IOR'].default_value = 0.89

    #add transparent bsfd shader node
    transparent_shader = glass_shader_group.nodes.new('ShaderNodeBsdfTransparent')
    transparent_shader.location = (174, -200)

    #add color ramp node
    color_ramp_node = glass_shader_group.nodes.new('ShaderNodeValToRGB')
    color_ramp_node.location = (174, 250)
    color_ramp_node.color_ramp.elements[0].position = 0.400
    color_ramp_node.color_ramp.elements[0].color = (0.053633, 0.053633, 0.053633, 1.0)
    if is_clear:
        color_ramp_node.color_ramp.elements[0].color = (0.0, 0.0, 0.0, 1.0)
    color_ramp_node.color_ramp.elements[1].color = (1.0, 1.0, 1.0, 1.0)

    #add mix color node
    mix_color_node = glass_shader_group.nodes.new('ShaderNodeMix')
    mix_color_node.location = (-36, 250)
    mix_color_node.data_type = 'RGBA'
    mix_color_node.clamp_factor = True
    mix_color_node.inputs['B'].default_value = (1.0, 1.0, 1.0, 1.0)

    #add brightness/contrast node
    brightness_contrast_node = glass_shader_group.nodes.new('ShaderNodeBrightContrast')
    brightness_contrast_node.location = (-36, 0)
    brightness_contrast_node.inputs['Bright'].default_value = 1.000

    #add brightness/contrast node 2
    brightness_contrast_node2 = None
    if not is_clear:
        brightness_contrast_node2 = glass_shader_group.nodes.new('ShaderNodeBrightContrast')
        brightness_contrast_node2.location = (-36, -200)
        brightness_contrast_node2.inputs['Bright'].default_value = 0.125

    #add vector maximum node
    vector_node = glass_shader_group.nodes.new('ShaderNodeVectorMath')
    vector_node.location = (-246, 394)
    vector_node.operation = 'MAXIMUM'

    #add light path node
    light_path_node = glass_shader_group.nodes.new('ShaderNodeLightPath')
    light_path_node.location = (-466, 394)

    #Add aditional nodes for clear glass shader
    if is_clear:
        color_ramp_node2 = glass_shader_group.nodes.new('ShaderNodeValToRGB')
        color_ramp_node2.location = (500, 380)
        color_ramp_node2.color_ramp.elements[0].position = 0.400
        color_ramp_node2.color_ramp.elements[0].color = (0.0, 0.0, 0.0, 1.0)
        color_ramp_node2.color_ramp.elements[1].color = (1.0, 1.0, 1.0, 1.0)

        invert_node = glass_shader_group.nodes.new('ShaderNodeInvert')
        invert_node.location = (500, 140)

        invert_node2 = glass_shader_group.nodes.new('ShaderNodeInvert')
        invert_node2.location = (744, 140)

        #add mix shader node
        mix_shader1 = glass_shader_group.nodes.new('ShaderNodeMixShader')
        mix_shader1.inputs['Fac'].default_value = 0.200
        mix_shader1.location = (500, -180)

        mix_shader2 = glass_shader_group.nodes.new('ShaderNodeMixShader')
        mix_shader2.location = (744, -180)

        glossy_shader_node = glass_shader_group.nodes.new('ShaderNodeBsdfAnisotropic')
        glossy_shader_node.location = (174, -310)

        transparent_shader.inputs['Color'].default_value = (0.799237, 0.799237, 0.799237, 1.0)

        glass_shader_group.links.new(color_ramp_node.outputs['Color'], invert_node.inputs['Color'])
        glass_shader_group.links.new(invert_node.outputs['Color'], mix_shader.inputs['Fac'])

        glass_shader_group.links.new(group_input.outputs['Alpha'], color_ramp_node2.inputs['Fac'])
        glass_shader_group.links.new(color_ramp_node2.outputs['Color'], invert_node2.inputs['Color'])
        glass_shader_group.links.new(invert_node2.outputs['Color'], mix_shader2.inputs['Fac'])

        glass_shader_group.links.new(mix_shader.outputs['Shader'], mix_shader2.inputs['Shader'])
        glass_shader_group.links.new(transparent_shader.outputs['BSDF'], mix_shader2.inputs['Shader_001'])
        glass_shader_group.links.new(transparent_shader.outputs['BSDF'], mix_shader1.inputs['Shader'])
        glass_shader_group.links.new(glossy_shader_node.outputs['BSDF'], mix_shader1.inputs['Shader_001'])
        glass_shader_group.links.new(mix_shader1.outputs['Shader'], mix_shader.inputs['Shader_001'])

        glass_shader_group.links.new(group_input.outputs['Glass Rougness'], glossy_shader_node.inputs['Roughness'])

        glass_shader_group.links.new(mix_shader2.outputs['Shader'], group_output.inputs['Shader'])





    #link nodes
    glass_shader_group.links.new(glass_shader.outputs['BSDF'], mix_shader.inputs['Shader'])

    glass_shader_group.links.new(mix_color_node.outputs['Result'], color_ramp_node.inputs['Fac'])
    glass_shader_group.links.new(brightness_contrast_node.outputs['Color'], glass_shader.inputs['Color'])

    glass_shader_group.links.new(vector_node.outputs['Vector'], mix_color_node.inputs['A'])
    glass_shader_group.links.new(group_input.outputs['Alpha'], mix_color_node.inputs['Factor'])
    glass_shader_group.links.new(group_input.outputs['Base Color'], brightness_contrast_node.inputs['Color'])
    glass_shader_group.links.new(group_input.outputs['Glass Color Bright'], brightness_contrast_node.inputs['Bright'])

    glass_shader_group.links.new(light_path_node.outputs['Is Shadow Ray'], vector_node.inputs['Vector'])
    glass_shader_group.links.new(light_path_node.outputs['Is Reflection Ray'], vector_node.inputs['Vector_001'])

    
    glass_shader_group.links.new(group_input.outputs['Glass Rougness'], glass_shader.inputs['Roughness'])

    if not is_clear:
        glass_shader_group.links.new(mix_shader.outputs['Shader'], group_output.inputs['Shader'])

        glass_shader_group.links.new(color_ramp_node.outputs['Color'], mix_shader.inputs['Fac'])
        
        glass_shader_group.links.new(transparent_shader.outputs['BSDF'], mix_shader.inputs['Shader_001'])
        glass_shader_group.links.new(brightness_contrast_node2.outputs['Color'], transparent_shader.inputs['Color'])
        glass_shader_group.links.new(group_input.outputs['Base Color'], brightness_contrast_node2.inputs['Color'])
        glass_shader_group.links.new(group_input.outputs['Shadow Bright'], brightness_contrast_node2.inputs['Bright'])
        glass_shader_group.links.new(group_input.outputs['Shadow Contrast'], brightness_contrast_node2.inputs['Contrast'])

def has_displacement_map(image): 
    alpha_value = None

    for index in range(0, len(image.pixels), 4):
        if alpha_value is None:
            alpha_value = image.pixels[index + 4]
        else:
            if alpha_value != image.pixels[index + 4]:
                return True
    
    return False

if IMPROVED_WATER_MATERIAL:
    if 'improved_water' not in bpy.data.materials:
        createImprovedWaterMaterial()

colored_glass_shader_name = "ColoredGlassShader_Minecraft"
clear_glass_shader_name = "ClearGlassShader_Minecraft"
if IMPROVED_GLASS_MATERIAL:
    #Create colored glass shader
    if bpy.data.node_groups.keys().__contains__(colored_glass_shader_name):
        bpy.data.node_groups.remove(bpy.data.node_groups[colored_glass_shader_name])
    create_minecraft_glass_shader(colored_glass_shader_name, False)

    #Create clear glass shader
    if bpy.data.node_groups.keys().__contains__(clear_glass_shader_name):
        bpy.data.node_groups.remove(bpy.data.node_groups[clear_glass_shader_name])
    create_minecraft_glass_shader(clear_glass_shader_name, True)

processed_materials = []

# Run through all materials of the selected objects
for obj in bpy.context.selected_objects:
    for mat in obj.data.materials:
        if mat.name not in processed_materials:

            # If the material has a node tree
            if mat.node_tree:
                nodes = mat.node_tree.nodes
                links = mat.node_tree.links

                if IMPROVED_GLASS_MATERIAL and "glass" in mat.name:
                    diffuse_image_texture_node = None
                    shader_output_node = None

                    glass_shader_name = colored_glass_shader_name
                    is_clear_glass = False

                    if mat.name == "glass_transparent" or mat.name == "glass":
                        glass_shader_name = clear_glass_shader_name
                        is_clear_glass = True

                    for node in nodes:
                        #Find shader output and diffues image texture node
                        if shader_output_node is None and type(node) is bpy.types.ShaderNodeOutputMaterial:
                            shader_output_node = node
                        elif diffuse_image_texture_node is None and type(node) is bpy.types.ShaderNodeTexImage:
                            #Get the NodeLink for the "Color" output socket
                            color_node_link = node.outputs['Color']
                            #Get the color link
                            if len(color_node_link.links) > 0:
                                color_link = color_node_link.links[0]
                                #Check that the color link is connected to the 'Base Color' socket (of the BsdfPrincipled node or the shader group node)
                                if color_link.to_socket.name == 'Base Color':
                                    diffuse_image_texture_node = node
                        elif type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                            #if shader output node was not yet found, get it from the BSDF Principled node
                            if shader_output_node is None:
                                #Get the NodeLink for the "BSDF" output socket
                                bsdf_output_node_link = node.outputs['BSDF']
                                #Get the bsdf link
                                bsdf_link = bsdf_output_node_link.links[0]
                                #Get the output node
                                shader_output_node = bsdf_link.to_node
                            
                            #if diffuse texture node was not yet found, get it from the BSDF Principled node
                            if diffuse_image_texture_node is None:
                                #Get the NodeLink for the "Base Color" input socket of the ShaderNodeBsdfPrincipled
                                base_color_node_link = node.inputs['Base Color']
                                #Get the base color link
                                base_color_link = base_color_node_link.links[0]
                                diffuse_image_texture_node = base_color_link.from_node

                        elif type(node) is bpy.types.ShaderNodeGroup:
                            if node.label == glass_shader_name:
                                #if shader output node was not yet found, get it from the shader node group
                                if shader_output_node is None:
                                    #Get the NodeLink for the "Shader" output socket
                                    shader_output_node_link = node.outputs['Shader']
                                    #Get the shader link
                                    shader_link = shader_output_node_link.links[0]
                                    #Get the output node
                                    shader_output_node = shader_link.to_node
                                
                                #if diffuse texture node was not yet found, get it from the shader node group
                                if diffuse_image_texture_node is None:
                                    #Get the NodeLink for the "Base Color" input socket of the shader node group
                                    base_color_node_link = node.inputs['Base Color']
                                    #Get the base color link
                                    base_color_link = base_color_node_link.links[0]
                                    diffuse_image_texture_node = base_color_link.from_node
                        
                        if diffuse_image_texture_node and shader_output_node:
                            break

                    if diffuse_image_texture_node and shader_output_node:

                        #Remove all other nodes then the material output node and the image texture node
                        for node in nodes:
                            if node.name != diffuse_image_texture_node.name and node.name != shader_output_node.name:
                                nodes.remove(node)

                        # Set the interpolation -> Linear, Closest, Cubic, Smart
                        diffuse_image_texture_node.interpolation = TEXTURE_INTERPOLATION

                        #Add shader group node and use the improved minecraft glass shader
                        glass_shader_group = nodes.new('ShaderNodeGroup')
                        #Get the glass shader node tree
                        minecraft_glass_shader_node_tree = bpy.data.node_groups[glass_shader_name]
                        glass_shader_group.node_tree = minecraft_glass_shader_node_tree

                        if "white" in mat.name:
                            glass_shader_group.inputs['Shadow Bright'].default_value = 0.05
                        
                        if "pink" in mat.name:
                            glass_shader_group.inputs['Shadow Bright'].default_value = 0.07

                        if is_clear_glass:
                            glass_shader_group.inputs['Glass Rougness'].default_value = 0.03

                        #Link the shader group to the diffuse texture
                        links.new(diffuse_image_texture_node.outputs['Color'], glass_shader_group.inputs['Base Color'])
                        links.new(diffuse_image_texture_node.outputs['Alpha'], glass_shader_group.inputs['Alpha'])

                        #Link the shader group to the shader output node
                        links.new(glass_shader_group.outputs['Shader'], shader_output_node.inputs['Surface'])  
                else:
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

                    
                    height_map_texture_node = None

                    #Run through all nodes
                    for node in nodes:
                        # If the node type is texture
                        if node.type == 'TEX_IMAGE':
                            # Set the interpolation -> Linear, Closest, Cubic, Smart
                            node.interpolation = TEXTURE_INTERPOLATION
                        
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

                                        if has_displacement_map(normal_texture_node.image):
                                            #Create a new Bump node
                                            bump_node = nodes.new('ShaderNodeBump')
                                            bump_node.inputs['Distance'].default_value = 0.300

                                            height_map_texture_node = nodes.new('ShaderNodeTexImage')
                                            normal_texture_node_location = normal_texture_node.location
                                            height_map_texture_node.location = (normal_texture_node_location[0], normal_texture_node_location[1] - normal_texture_node.height - 20)
                                            height_map_texture_node.image = normal_texture_node.image

                                            #Conntect the Alpha output of the texture image node to the Height input of the Bump node
                                            links.new(height_map_texture_node.outputs['Alpha'], bump_node.inputs['Height'])

                                            #Remove the link between the Bsdf Principled and Normal Map
                                            links.remove(normal_link)

                                            #Connect the Normal Map output normal to the Bump Node "Normal" input socket
                                            links.new(normal_map_node.outputs['Normal'], bump_node.inputs['Normal'])

                                            #Connect the Bump node to the Bsdf Principled Normal Socket
                                            links.new(bump_node.outputs['Normal'], node.inputs['Normal'])

                        if emission > 0.0:
                            if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                                node.inputs["Emission Strength"].default_value = emission
                        elif isLit:
                            if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                                current = node.inputs["Emission Strength"].default_value
                                if current > 0.0:
                                    node.inputs["Emission Strength"].default_value = (15/ 16) * MAX_EMISSION_VALUE
                        
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

                                    links.new(diffuse_texture_node.outputs['Alpha'], node.inputs['Transmission Weight'])
                        
                        if "glass" in mat.name:
                            #Check if node Bsdf Principled
                            if type(node) is bpy.types.ShaderNodeBsdfPrincipled:
                                bsdf_rougness_node_link = node.inputs['Roughness']
                                if len(bsdf_rougness_node_link.links) == 0:
                                    node.inputs['Roughness'].default_value = 0.0

                                bsdf_transmission_node_link = node.inputs['Transmission Weight']
                                if len(bsdf_transmission_node_link.links) == 0:
                                    node.inputs['Transmission Weight'].default_value = 1.0

                                bsdf_specular_node_link = node.inputs['Specular IOR Level']
                                if len(bsdf_specular_node_link.links) == 0:
                                    node.inputs['Specular IOR Level'].default_value = 0.5

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

                    if ADD_BUMP_MAP and height_map_texture_node:
                        height_map_texture_node.interpolation = 'Linear'

                    #Now run through all node link
                    for link in links:
                        #Check if link is connected to a normal map node
                        if type(link.to_node) is bpy.types.ShaderNodeNormalMap:
                            link.to_node.inputs['Strength'].default_value = 2.000
                            link.to_node.uv_map = 'UVMap'
                            if type(link.from_node) is bpy.types.ShaderNodeTexImage:
                                imgNode = link.from_node
                                imgNode.image.colorspace_settings.name = 'Non-Color'
                    
                    #Add texture mapping setup
                    texture_coordinate_node = None
                    mapping_node = None
                    for node in nodes:
                        if type(node) is bpy.types.ShaderNodeMapping or type(node) is bpy.types.ShaderNodeTexCoord:
                            nodes.remove(node)

                    for node in nodes:
                        if type(node) is bpy.types.ShaderNodeTexImage:
                            if texture_coordinate_node is None:
                                mapping_node = nodes.new('ShaderNodeMapping')
                                texture_coordinate_node = nodes.new('ShaderNodeTexCoord')
                                links.new(texture_coordinate_node.outputs['UV'], mapping_node.inputs['Vector'])
                            links.new(mapping_node.outputs['Vector'], node.inputs['Vector'])

            processed_materials.append(mat.name)
            
    
    if IMPROVED_WATER_MATERIAL:
        improved_water_material = bpy.data.materials['improved_water']
        if obj.name == 'Water':
            for m in obj.material_slots:
                m.material = improved_water_material
        else:
            for m in obj.material_slots:
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