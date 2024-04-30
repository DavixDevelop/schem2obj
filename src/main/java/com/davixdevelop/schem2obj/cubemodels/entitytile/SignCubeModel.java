package com.davixdevelop.schem2obj.cubemodels.entitytile;

import com.davixdevelop.schem2obj.Constants;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;
import com.davixdevelop.schem2obj.materials.IMaterial;
import com.davixdevelop.schem2obj.models.IntegerString;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.davixdevelop.schem2obj.resourceloader.ResourceLoader;
import com.davixdevelop.schem2obj.schematic.EntityValues;
import com.davixdevelop.schem2obj.util.ImageUtility;
import com.davixdevelop.schem2obj.util.LogUtility;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * The CubeModel for the Sign entity
 * It's only responsible to create the material for the sing (text).
 * For the implementation of the Wall and Standing Sign,
 * see WallSignCubeModel and StandingSignCubeModel
 *
 * @author DavixDevelop
 */
public class SignCubeModel extends TileEntityCubeModel {

    //Key: Color Index:Char, Value: Colored Image of Glyph
    public static Map<String, BufferedImage> COLORED_GLYPHS = new HashMap<>();
    //Key: MD5 encoded value (Color:ROW1_Color:ROW2...)
    public static Set<String> COLORED_TEXT_ROWS = new HashSet<>();

    public static Map<String, BufferedImage> FONT_BITMAPS = new HashMap<>();

    private static byte[] UNICODE_GLYPH_SIZES = null;

    String signText;

    Integer bitmapYRes;

    @Override
    public boolean fromNamespace(Namespace namespace) {
        EntityValues entityValues = namespace.getCustomData();

        EntityValues Text1 = entityValues.getEntityValues("Text1");
        EntityValues Text2 = entityValues.getEntityValues("Text2");
        EntityValues Text3 = entityValues.getEntityValues("Text3");
        EntityValues Text4 = entityValues.getEntityValues("Text4");


        List<String[]> coloredTextRows = new ArrayList<>();
        //Color the glyphs of each row
        colorGlyphsRow(Text1, coloredTextRows);
        colorGlyphsRow(Text2, coloredTextRows);
        colorGlyphsRow(Text3, coloredTextRows);
        colorGlyphsRow(Text4, coloredTextRows);

        StringBuilder keyBuilder = new StringBuilder();
        for(int c = 0; c < coloredTextRows.size(); c++){
            String[] coloredTextRow = coloredTextRows.get(c);
            keyBuilder.append(String.format((c != 3) ? "%s:%s_" : "%s:%s", coloredTextRow[0], coloredTextRow[1]));
        }

        String key = keyBuilder.toString();

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(StandardCharsets.UTF_8.encode(key));
            key = String.format("%032x", new BigInteger(1, md5.digest()));
        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
        }



        if(!COLORED_TEXT_ROWS.contains(key)){
            //Calculate the x and y resolution so that the glyph with the highest resolution gets displayed at It's full resolution, without downscaling
            int rowSize = (bitmapYRes != null) ? bitmapYRes / 16 : 8;

            int yPixelSize = rowSize / 8;
            //row height + bottom/top border + row spacing
            //int yResolution = (4 * (8 * yPixelSize)) + (2 * (4 * yPixelSize)) + (3 * (2 * yPixelSize));
            int yResolution = 48 * yPixelSize;
            int xResolution = 2 * yResolution;
            //Create empty diffuse image
            BufferedImage diffuseImage = new BufferedImage(xResolution, yResolution, BufferedImage.TYPE_INT_ARGB_PRE);

            for(int c = 0; c < coloredTextRows.size(); c++){
                 typeGlyphRow(diffuseImage, coloredTextRows.get(c), c + 1, rowSize);
            }

            //Create new material for text
            String signMaterialPath = "entity/sign";
            CubeModelUtility.generateOrGetMaterial(signMaterialPath, namespace);
            IMaterial sign_text_material = Constants.BLOCK_MATERIALS.getMaterial(signMaterialPath).duplicate();

            sign_text_material.setName(String.format("sign-%s", key));
            sign_text_material.setDiffuseImage(diffuseImage);
            Constants.BLOCK_MATERIALS.setMaterial(String.format("entity/sign-%s", key), sign_text_material);

            COLORED_TEXT_ROWS.add(key);
        }

        signText = key;


        return false;

    }

    public String getSignText() {
        return signText;
    }

    public void colorGlyphsRow(EntityValues row, List<String[]> coloredTextRows){
        if(UNICODE_GLYPH_SIZES == null){
            setUnicodeGlyphSizes();
        }

        //Get color of row
        IntegerString color;

        //Create outRow for all valid chars
        StringBuilder outRow = new StringBuilder();

        if(row.containsKey("color")){
            String colorName = row.getString("color");
            color = Constants.TEXT_COLORS.get(colorName);
        }else
            color = Constants.TEXT_COLORS.get("black");

        String text = row.getString("text");

        if(row.containsKey("extra")){
            List<?> extra = row.getList("extra");
            if(extra.size() > 0){
                if(extra.get(0) instanceof Map){
                    Map<?, ?> map = (Map<?, ?>) extra.get(0);
                    if(map.containsKey("text"))
                        text = (String) map.get("text");

                    if(row.containsKey("color")) {
                        String colorName = row.getString("color");
                        color = Constants.TEXT_COLORS.get(colorName);
                    }
                }
            }
        }

        for(int d = 0; d < text.length(); d++){

            String coloredGlyphKey = String.format("%s:%s", color.getStringValue(), text.charAt(d));
            if(!COLORED_GLYPHS.containsKey(coloredGlyphKey)) {


                char c = text.charAt(d);
                //Get numeric value of char

                BufferedImage coloredGlyph;

                //If numeric value is less than 256, It's a ASCII char
                if ((int) c < 256) {
                    //Get textures/font/ascii.png from resources
                    BufferedImage asciiBitmap = getFontBitmap("ascii");
                    //If resource doesn't exist, skip char
                    if (asciiBitmap == null)
                        continue;

                    //Calculate position of glyph on fontBitmap
                    int x = (int) c % 16;
                    int y = (int) c / 16;

                    coloredGlyph = getGlyph(asciiBitmap, x, y, 0, 15);

                    //Clip the glyph if needed
                    coloredGlyph = clipGlyph(coloredGlyph);

                    if(coloredGlyph == null && Character.isWhitespace(c)){
                        coloredGlyph = new BufferedImage(asciiBitmap.getWidth() / 16, asciiBitmap.getHeight() / 16, asciiBitmap.getType());
                    }

                }else {
                    //Else It's a unicode char

                    //Check if glyph is not in glyph sizes
                    if(UNICODE_GLYPH_SIZES.length <= (int) c)
                        continue;

                    //Get the page number the char lies in
                    int page = (int) c / 256;
                    //Get the name of the unicode page
                    String unicodePageName = String.format("unicode_page_%02X", page);
                    BufferedImage unicodePage = getFontBitmap(unicodePageName);

                    if(unicodePage == null)
                        continue;

                    //Get the local index of the char on the page
                    int local_ci = (int) c - (256 * page);
                    //Calculate position of glyph on page
                    int x = local_ci % 16;
                    int y = local_ci / 16;

                    byte glyphSize = UNICODE_GLYPH_SIZES[c];
                    //Get the start and end column of the glyph from the upper and lower 4 bits of the byte
                    int start_collum = glyphSize >> 4;
                    int end_column = glyphSize & 0x0F;

                    coloredGlyph = getGlyph(unicodePage, x, y, start_collum, end_column);

                    if(coloredGlyph == null)
                        continue;

                    boolean isGlyphTransparent = ImageUtility.isTransparent(coloredGlyph);

                    if(isGlyphTransparent && !Character.isWhitespace(c))
                        continue;
                }

                if(coloredGlyph == null)
                    continue;

                //Color the glyph
                coloredGlyph = ImageUtility.colorImage(coloredGlyph, color.getIntegerValue(), true);
                COLORED_GLYPHS.put(coloredGlyphKey, coloredGlyph);
                outRow.append(c);

            }else {
                outRow.append(text.charAt(d));
            }
        }

        coloredTextRows.add(new String[]{color.getStringValue(), outRow.toString()});
    }

    /**
     * Get glyph from provide font page
     * @param fontBitmap The image of the font page
     * @param x_pos The x position of the glyph on the page (0-15)
     * @param y_pos The y position of the glyph on the page (0-15)
     * @param start_column The column where the glyph starts
     * @param end_column The column where the glyph ends
     * @return The image of the glyph
     */
    public BufferedImage getGlyph(BufferedImage fontBitmap, int x_pos, int y_pos, int start_column, int end_column){
        try {
            int fontMapWidth = fontBitmap.getWidth();
            int fontMapHeight = fontBitmap.getHeight();

            int fontWidth = fontMapWidth / 16;
            int fontHeight = fontMapHeight / 16;

            if(bitmapYRes == null || fontMapHeight > bitmapYRes)
                bitmapYRes = fontMapHeight;

            double pixelWidth = fontWidth / 16.0;
            double pixelHeight = fontHeight / 16.0;

            int startPos = (int) ((x_pos * fontWidth) + Math.floor(start_column * pixelWidth));
            int endPos = (int) ((x_pos * fontWidth) + Math.floor(end_column * pixelWidth));

            int endYPos = (int) ((y_pos * fontHeight) + (15 * pixelHeight));

            BufferedImage glyph = new BufferedImage((int) (((end_column - start_column) + 1) * pixelWidth), (int) (16 * pixelHeight), BufferedImage.TYPE_INT_ARGB_PRE);

            int local_x = 0;
            for(int x = startPos; x <= endPos; x++ ){
                int local_y = 0;
                for(int y = y_pos * fontHeight; y <= endYPos; y++){
                    int pixelColor = fontBitmap.getRGB(x, y);
                    glyph.setRGB(local_x, local_y, pixelColor);

                    local_y += 1;
                }

                local_x += 1;
            }

            return glyph;

        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
        }

        return null;
    }

    /**
     * Horizontally clip the glyph. If the glyph is transparent, return null
     * @param glyph The image of the glyph to clip
     * @return The clipped glyph
     */
    public BufferedImage clipGlyph(BufferedImage glyph){
        Integer min_x = null;
        Integer max_x = null;

        for(int x = 0; x < glyph.getWidth(); x++){
            for(int y = 0; y < glyph.getHeight(); y++){
                int color = glyph.getRGB(x, y);

                int alpha = (color >> 24 & 255);

                if(alpha != 0){
                    if(min_x == null){
                        min_x = x;
                        max_x = x;
                    }else
                    {
                        if(x < min_x)
                            min_x = x;

                        if(x > max_x)
                            max_x = x;
                    }
                }
            }
        }

        if(min_x == null)
            return null;

        BufferedImage clippedGlyph = new BufferedImage((max_x + 1) - min_x, glyph.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

        int local_x = 0;
        for(int x = min_x; x <= max_x; x++){
            int local_y = 0;
            for(int y = 0; y < clippedGlyph.getHeight(); y++){
                int color = glyph.getRGB(x, y);

                clippedGlyph.setRGB(local_x, local_y, color);
                local_y += 1;
            }
            local_x += 1;
        }

        return clippedGlyph;
    }

    /**
     * Get font page from It's name from Resources. If it doesn't exist, return null
     * @param name The name of the font page, ex. ascii
     * @return The image of the font page
     */
    public BufferedImage getFontBitmap(String name){
        if(!FONT_BITMAPS.containsKey(name)){
            try{
                String fontPath = ResourceLoader.getResourcePath("textures", String.format("font/%s", name), "png");
                if(ResourceLoader.resourceExists(fontPath)){
                    InputStream bitmapStream = ResourceLoader.getResource(fontPath);
                    BufferedImage bitmapFont = ImageUtility.toBuffedImage(bitmapStream);

                    FONT_BITMAPS.put(name, bitmapFont);

                    return bitmapFont;
                }
            }catch (Exception ex){
                LogUtility.Log(ex.getMessage());
            }

            return null;
        }else
            return FONT_BITMAPS.get(name);
    }

    /**
     * Type a row of glyphs on a image. Each glyph gets upscaled to the rowHeight
     * @param image A image to type the glyphs on
     * @param colorAndTextRow A 2 length array [color index, string]
     * @param row The index of the row (1-4)
     * @param rowHeight The pixel height of each row
     */
    public void typeGlyphRow(BufferedImage image, String[] colorAndTextRow, Integer row, Integer rowHeight){
        if(colorAndTextRow[1].length() > 0) {
            String text = colorAndTextRow[1];
            BufferedImage[] glyphRow = new BufferedImage[text.length()];
            String color = colorAndTextRow[0];

            int rowWidth = 0;
            double columnSpacing = rowHeight * (1 / 8.0);

            int yPixelSize = rowHeight / 8;
            int rowSpacing = (2 * yPixelSize);
            int yOffset = (row * rowSpacing) + ((row - 1) * rowHeight) + rowSpacing;

            for(int c = 0; c < text.length(); c++){
                char t = text.charAt(c);
                BufferedImage glyph = COLORED_GLYPHS.get(String.format("%s:%s", color, t));
                //Upscale glyph is Its height doesn't match the rowHeight
                if(glyph.getHeight() != rowHeight){
                    double yRatio = rowHeight.doubleValue() / glyph.getHeight();
                    int xRes = (int) (glyph.getWidth() * yRatio);
                    if(xRes == 0)
                        xRes = 1;
                    glyph = ImageUtility.upscaleImage(glyph, xRes, rowHeight);
                }

                if (c != text.length() - 1) {
                    if(Character.isWhitespace(t))
                        rowWidth += glyph.getWidth() / 1.77;
                    else
                        rowWidth += glyph.getWidth() + (int) columnSpacing;
                }else {
                    if(Character.isWhitespace(t))
                        rowWidth += glyph.getWidth() / 1.77;
                    else
                        rowWidth += glyph.getWidth();
                }

                glyphRow[c] = glyph;
            }

            int xOffset = (image.getWidth() - rowWidth) / 2;

            for(int c = 0; c < text.length(); c++){
                char t = text.charAt(c);
                BufferedImage glyph = glyphRow[c];

                try {
                    ImageUtility.insertIntoImage(image, glyph, xOffset, yOffset);
                }catch (Exception ignored){

                }

                if (c != text.length() - 1) {
                    if (Character.isWhitespace(t))
                        xOffset += (glyph.getWidth() / 1.77);
                    else
                        xOffset += glyph.getWidth() + (int) columnSpacing;
                }

            }
        }
    }

    /**
     * Read glyph_sizes.bin from resource into a byte array
     */
    public void setUnicodeGlyphSizes(){
        try {
            String glyphSizesPath = ResourceLoader.getResourcePath("font", "glyph_sizes","bin");
            InputStream glyphSizesRaw = ResourceLoader.getResource(glyphSizesPath);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int readBytes = glyphSizesRaw.read(buffer);

            while (readBytes != -1){
                outputStream.write(buffer, 0, readBytes);
                readBytes = glyphSizesRaw.read(buffer);
            }

            UNICODE_GLYPH_SIZES = outputStream.toByteArray();

        }catch (Exception ex){
            LogUtility.Log(ex.getMessage());
        }
    }
}
