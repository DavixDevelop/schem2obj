package com.davixdevelop.schem2obj.blockstates;

import com.davixdevelop.schem2obj.blockstates.json.BlockStateTemplate;
import com.davixdevelop.schem2obj.namespace.Namespace;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class BlockState {

    private final Random RANDOM = new Random();

    //key: null (the default variant) |
    // Map<property:String, state:String> => This also includes the variants from the "variants" part |
    // List<Map<property:String, state:String>> (OR list -> when the block states match any of the list item states in entry, "apply" the variant (models))
    private HashMap<Object, Integer[]> variantMultiKeys;

    private Boolean multiPart;

    //List of variants
    private List<Variant> variants;

    public BlockState(List<Variant> variants, HashMap<Object, Integer[]> variantMultiKeys, Boolean multiPart){
        this.variants = variants;
        this.variantMultiKeys = variantMultiKeys;
        this.multiPart = multiPart;
    }

    public static BlockState readFromJson(InputStream jsonInputStream){
        Gson gson = new Gson();

        //Get reader from stream
        Reader reader = new InputStreamReader(jsonInputStream);
        //Deserialize the json from the reader
        BlockStateTemplate rawModel = gson.fromJson(reader, BlockStateTemplate.class);

        List<Variant> variants = new ArrayList<>();
        HashMap<Object, Integer[]> variantMultiKeys = new HashMap<>();

        //Check if BlockState contains variants
        if(rawModel.variants != null){
            Map<String, Object> rawVariants = rawModel.variants;
            //Loop through the variants
            for(String rawProps : rawVariants.keySet()){
                //ArrayList<BlockStateTemplate.Apply> blockVariants = new ArrayList<>();

                //Variable to store indexes to added variants
                Integer[] variantIndexes = null;

                Object rawPropValue = rawVariants.get(rawProps);
                //Check if variant contains multiple models (applies)
                if(rawPropValue instanceof List){
                    //Convert rawPropValue (List) back to JSON
                    JsonElement jsonVariants = gson.toJsonTree(rawPropValue);
                    //Deserialize jsonVariants back to ArrayList<BlockStateTemplate.Apply>
                    List<BlockStateTemplate.Apply> stateModels = gson.fromJson(jsonVariants, new TypeToken<ArrayList<BlockStateTemplate.Apply>>(){}.getType());

                    //Crete new array of size of stateModels
                    variantIndexes = new Integer[stateModels.size()];
                    for(int c = 0; c < variantIndexes.length; c++){
                        //Add the stateModel to variants
                        variants.add(ApplyToVariant(stateModels.get(c)));
                        //Track the added variant index
                        variantIndexes[c] = variants.size() - 1;
                    }
                }else{
                    //Convert rawPropValue (Map) back to JSON
                    JsonElement jsonVariant = gson.toJsonTree(rawPropValue);
                    //Deserialize jsonVariant back to BlockStateTemplate.Apply
                    variants.add(ApplyToVariant(gson.fromJson(jsonVariant, BlockStateTemplate.Apply.class)));
                    variantIndexes = new Integer[]{variants.size() - 1};
                }

                //Check if rawProps is a single string (doesn't contain any key=values)
                if(!rawProps.contains("=")){
                    //Add single variant string key (ex. normal or all)
                    variantMultiKeys.put(rawProps, variantIndexes);
                }else {
                    HashMap<String, String> when = new HashMap<>();

                    //Split the key: rawProps to get the properties
                    String[] props = rawProps.split(",");
                    for (String prop : props) {
                        //Split prop to get the prop name and value
                        String[] propValues = prop.split("=");

                        //Add the property to the when map
                        when.put(propValues[0], propValues[1]);
                    }

                    //Add single variant props key (key has one or more properties)
                    variantMultiKeys.put(when, variantIndexes);
                }
            }
        }else{
            //BlockState is multipart
            List<BlockStateTemplate.Part> multiParts = rawModel.multipart;
            for(BlockStateTemplate.Part multiPart : multiParts){
                List<BlockStateTemplate.Apply> applies = new ArrayList<>();

                if(multiPart.apply instanceof  List){
                    //Convert list back to json
                    JsonElement jsonElement = gson.toJsonTree(multiPart.apply);
                    applies = gson.fromJson(jsonElement, new TypeToken<List<BlockStateTemplate.Apply>>(){}.getType());
                }else
                {
                    //Convert java object back to json element
                    JsonElement jsonElement = gson.toJsonTree(multiPart.apply);
                    applies.add(gson.fromJson(jsonElement, BlockStateTemplate.Apply.class));
                }

                //Create array of size of applies, to store the indexes
                Integer[] variantIndexes = new Integer[applies.size()];
                for(int c = 0; c < applies.size(); c++){
                    BlockStateTemplate.Apply rawApply = applies.get(c);

                    //Create variant from raw apply
                    Variant variant = ApplyToVariant(rawApply);
                    variants.add(variant);

                    //Populate the variant indexes
                    variantIndexes[c] = variants.size() - 1;
                }

                //Check if part is not default (doesn't contain when condition)
                if(multiPart.when != null){
                    //Get raw when
                    Map<String, Object> rawWhen = multiPart.when;

                    //Check if when contains OR conditions
                    if(rawWhen.containsKey("OR")){
                        //Convert OR list back to JSON
                        JsonElement jsonElement = gson.toJsonTree(rawWhen.get("OR"));
                        //Get a list of whens from "OR"
                        List<Map<String, String>> orWhens = gson.fromJson(jsonElement, new TypeToken<List<Map<String,String>>>(){}.getType());

                        List<HashMap<String, String>> listOfWhens = new ArrayList<>();

                        for(Map<String, String> orWhen : orWhens){
                            HashMap<String, String> whens = new HashMap<>();
                            //Copy orWhen to whens
                            for(String key : orWhen.keySet())
                                whens.put(key, whens.get(key));

                            //Add whens to list key
                            listOfWhens.add(whens);
                        }

                        //Add multiple variants list key
                        variantMultiKeys.put(listOfWhens, variantIndexes);
                    }else{
                        //Convert raw when back to json
                        JsonElement jsonElement = gson.toJsonTree(rawWhen);
                        //Get map of strings from converted rawWhen
                        Map<String, String> when = gson.fromJson(jsonElement, new TypeToken<Map<String, String>>(){}.getType());

                        //Add multiple variants key
                        variantMultiKeys.put(when, variantIndexes);
                    }
                }else {
                    //Add default variant/variants
                    variantMultiKeys.put(null, variantIndexes);
                }
            }
        }


        return new BlockState(variants, variantMultiKeys, rawModel.multipart != null);
    }

    public static Variant ApplyToVariant(BlockStateTemplate.Apply apply){
        return new Variant(apply.model, (apply.x != null) ? apply.x.doubleValue() : null, (apply.y != null) ? apply.y.doubleValue() : null, apply.uvlock, (apply.weight != null) ? apply.weight.intValue() : 1);
    }

    public ArrayList<Variant> getVariants(Namespace blockNamespace){
        ArrayList<Variant> blockVariants = new ArrayList<>();
        Set<Integer> variantIndexes = new HashSet<>();

        //Get the actual properties of the block
        HashMap<String, String> blockProps = blockNamespace.getData();

        //Check if block uses variants
        //Else get the models the block uses from variantMultiKeys
        if(!multiPart){
            //Check if block contains data key "seamless"
            //If it does, change it it to type=normal for seamless=false and type=all for seamless=true

            String isSeamless = null;

            if(blockNamespace.getData().containsKey("seamless")){
                HashMap<String, String> data = blockNamespace.getData();

                isSeamless = data.get("seamless");

                if(data.get("seamless").equals("false"))
                    data.put("type", "normal");
                else
                    data.put("type","all");

                data.remove("seamless");

                blockNamespace.setData(data);

            }

            //Check if block data has any data
            if(!blockNamespace.getData().isEmpty()) {
                for (Object when : variantMultiKeys.keySet()) {
                    if (when instanceof HashMap) { //Check if when is regular HashMap
                        //Get the when properties
                        HashMap<String, String> whenProps = (HashMap<String, String>) when;
                        variantIndexes = getVariantIndexesFromBlockProps(when, whenProps, blockProps, variantIndexes);
                    }
                }
            }

            //If variant indexes is still empty, get the normal or all variant
            if(variantIndexes.isEmpty()){
                if(isSeamless != null){
                    if(isSeamless.equals("false") && variantMultiKeys.containsKey("normal"))
                        variantIndexes = getMultiPartVariantIndexes("normal", variantIndexes);
                    else if (variantMultiKeys.containsKey("all"))
                        variantIndexes = getMultiPartVariantIndexes("all", variantIndexes);
                    else
                        variantIndexes = getMultiPartVariantIndexes("normal", variantIndexes);
                }else {

                    if (variantMultiKeys.containsKey("normal"))
                        variantIndexes = getMultiPartVariantIndexes("normal", variantIndexes);
                    else if (variantMultiKeys.containsKey("all"))
                        variantIndexes = getMultiPartVariantIndexes("all", variantIndexes);
                }
            }

            //If there are more than one variants(models), choose only one depending on the weight of the variant
            //If the weight of of the variant isn't set, It's by default 1
            //To chose which variant to choose, we need to first sum all the variant weight
            //The percentage is then calculated by variant weight / sum of weights, ex 1/4 -> 0.25 or 25%
            //Then if a variant is chosen is based on if a random float value (from 0.0 to 1.0) is less then the
            //calculated percentage
            //This is repeated until the above condition is true (until variantIndexes size is exactly one)
            if(variantIndexes.size() > 1){
                Integer combinedWeight = 0;
                for(int index: variantIndexes) {
                    combinedWeight += variants.get(index).getWeight();
                }

                while (variantIndexes.size() > 1) {
                    for (int index : variantIndexes) {
                        Integer weight = variants.get(index).getWeight();

                        if (RANDOM.nextFloat() < weight.floatValue() / combinedWeight.floatValue()) {
                            variantIndexes.clear();
                            variantIndexes.add(index);
                            break;
                        }
                    }
                }
            }

        }else {

            for(Object when : variantMultiKeys.keySet()){

                //Check if when is an OR list
                if(when instanceof List){
                    List<Map<String, String>> orWhens = (List<Map<String, String>>) when;

                    for(Map<String, String> whenProps : orWhens){
                        variantIndexes = getVariantIndexesFromBlockProps(when, whenProps, blockProps, variantIndexes);
                    }

                }else if(when instanceof Map){ //Check if when is regular HashMap
                    //Get the when properties
                    Map<String, String> whenProps = (Map<String, String>) when;
                    variantIndexes = getVariantIndexesFromBlockProps(when, whenProps, blockProps, variantIndexes);


                }else {
                    //The default variant/variants
                    variantIndexes = getMultiPartVariantIndexes(null, variantIndexes);
                }
            }


        }

        for(int index : variantIndexes)
            blockVariants.add(variants.get(index));

        return blockVariants;
    }

    /**
     * Get the variants indexes an block uses, depending on the when
     * See BlockState.getVariants for more info
     * @param when The key to the indexes to the variants
     * @param currentVariantIndexes The list of current variants (to prevent duplicates)
     * @return An list of multipart variant indexes the block uses
     */
    private Set<Integer> getMultiPartVariantIndexes(Object when, Set<Integer> currentVariantIndexes){
        Integer[] variantIndexes = variantMultiKeys.get(when);
        for(Integer index : variantIndexes){
            //Only add index if It's not in current list yet
            if(!currentVariantIndexes.contains(index))
                currentVariantIndexes.add(index);
        }

        return currentVariantIndexes;
    }

    /**
     * Get the variant indexes from the variantMultiKeys key if the block props matches the when props
     * @param multiPartKey The original key to the variant indexes
     * @param whenProps The properties of the when condition
     * @param blockProps The properties of the actual block
     * @param currentVariantIndexes The list of current variants (to prevent duplicates)
     * @return An list of multipart variant indexes from the variantMultiKeys key
     */
    private Set<Integer> getVariantIndexesFromBlockProps(Object multiPartKey, Map<String, String>  whenProps, HashMap<String, String> blockProps, Set<Integer> currentVariantIndexes){
        //Loop through the actual properties of the block
        for(String property : blockProps.keySet()){
            //Check if block property exists in when
            if(whenProps.containsKey(property)){

                //Get the when property value
                String whenPropValue = whenProps.get(property);

                //Check if value has OR separator
                //Else check if value of when property matches the property value of the actual block
                if(whenPropValue.contains("|")){
                    //Split by | to get the OR value's
                    String[] orValues = whenPropValue.split("\\|");
                    boolean containsVal = false;

                    for(String orValue : orValues){
                        if(blockProps.get(property).equals(orValue)){
                            containsVal = true;
                            break;
                        }
                    }

                    //Add variant indexes if block property value matches at least one orValue
                    if(containsVal)
                        currentVariantIndexes = getMultiPartVariantIndexes(multiPartKey, currentVariantIndexes);

                }else if(blockProps.get(property).equals(whenPropValue)){
                    currentVariantIndexes = getMultiPartVariantIndexes(multiPartKey, currentVariantIndexes);
                }else
                    break;


            }
        }

        return currentVariantIndexes;
    }



    public static class Variant{
        private String model;
        private Double x;
        private Double y;
        private Boolean uvlock;
        private Integer weight;

        public Variant(String model, Double x, Double y, Boolean uvlock, Integer weight){
            this.model = model;
            this.x = x;
            this.y = y;
            this.uvlock = uvlock;
            this.weight = weight;
        }

        public String getModel() {
            return model;
        }

        public Double getX() {
            return x;
        }

        public Double getY() {
            return y;
        }

        public Integer getWeight() {
            return weight;
        }
    }

}
