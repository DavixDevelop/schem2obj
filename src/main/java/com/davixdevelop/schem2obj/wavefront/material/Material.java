package com.davixdevelop.schem2obj.wavefront.material;

import com.davixdevelop.schem2obj.utilities.ImageUtility;
import com.davixdevelop.schem2obj.wavefront.material.IMaterial;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**Default Material that only uses the defuse texture
 * @author DavixDevelop
 */
public class Material implements IMaterial {
    private String texture;
    private String texturePath;
    private double lightValue;
    private String name;

    private InputStream customDiffuse;

    public Material() {
        this.texture = null;
        this.texturePath = null;
        this.lightValue = 0.0;
    }

    @Override
    public String getTextureName() {
        return texture;
    }

    @Override
    public void setTextureName(String textureName) {
        this.texture = textureName;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getTexture() {
        return texturePath;
    }

    @Override
    public void setTexture(String texture) {
        this.texturePath = texture;
    }

    @Override
    public InputStream getDiffuseImage() {
        if(customDiffuse != null)
            return customDiffuse;

        String assetMaterial = String.format("minecraft/textures/%s.png", getTexture());

        InputStream assetStream = this.getClass().getClassLoader().getResourceAsStream("assets/" + assetMaterial);

        return assetStream;
    }

    @Override
    public void setDiffuseImage(InputStream diffuseImage) {
        customDiffuse = diffuseImage;
    }

    @Override
    public double getLightValue() {
        return lightValue;
    }

    @Override
    public void setLightValue(double lightValue) {
        this.lightValue = lightValue;
    }

    @Override
    public ArrayList<String> toMat(String textureFolder) {

        Path diffuseTextureOut = Paths.get(textureFolder, getTextureName() + ".png");

        InputStream assetStream = getDiffuseImage();

        ImageUtility.copyImageToFile(assetStream, diffuseTextureOut.toFile().toString());

        //To store each line that defines a new material
        ArrayList<String> matLines = new ArrayList<>();

        matLines.add(String.format("newmtl %s", getName()));

        if(getLightValue() > 0.0){
            matLines.add(String.format("Ke %f %f %f",getLightValue(), getLightValue(), getLightValue()));
        }
        matLines.add(String.format("map_Kd -boost %f /%s/%s.png", 1.0, diffuseTextureOut.getParent().toFile().getName(), getTextureName()));
        if(getLightValue() > 0.0){
            matLines.add(String.format("map_Ke /%s/%s.png", diffuseTextureOut.getParent().toFile().getName(), getTextureName()));
        }


        return matLines;
    }
}
