package com.davixdevelop.schem2obj.blockstates;

import com.davixdevelop.schem2obj.util.LogUtility;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjacentBlockState {

    private static Gson GSON = new Gson();

    //key: orientation of the adjacent block to the original block
    //value: list of adjacent variants
    private final HashMap<String, List<AdjacentVariant>> adjacentOrientations = new HashMap<>();
    private List<String> checkOrder = new ArrayList<>();

    public AdjacentBlockState(String assetPath){
        readJSON(assetPath);
    }

    private void readJSON(String assetPath){
        try{

            //Get input stream for adjacent block state json file from assets
            InputStream assetStream = AdjacentBlockState.class.getClassLoader().getResourceAsStream(assetPath);

            //Get reader for asset stream
            Reader assetReader = new InputStreamReader(assetStream);

            //Read the json to a map
            Map<String, Object> adjacent = GSON.fromJson(assetReader, new TypeToken<Map<String, Object>>(){}.getType());

            for(String orientation : adjacent.keySet()){
                List<AdjacentVariant> orientationVariants = new ArrayList<>();

                Object rawVariants = adjacent.get(orientation);
                //Check if rawVariants is list, else threat it as a object of a AdjacentVariant
                if(rawVariants instanceof List){
                    JsonElement variantsJson = GSON.toJsonTree(rawVariants);
                    List<AdjacentVariant> adjacentVariants = GSON.fromJson(variantsJson, new TypeToken<List<AdjacentVariant>>(){}.getType());
                    for(AdjacentVariant rawVariant : adjacentVariants){
                        //Convert the apply to either states to apply or to a list of adjacent variants
                        convertApply(rawVariant);
                        orientationVariants.add(rawVariant);
                    }
                }else{
                    JsonElement variantJson = GSON.toJsonTree(rawVariants);
                    AdjacentVariant rawVariant = GSON.fromJson(variantJson, AdjacentVariant.class);
                    //Convert the apply to either states to apply or to a list of adjacent variants
                    convertApply(rawVariant);
                    orientationVariants.add(rawVariant);
                }

                adjacentOrientations.put(orientation, orientationVariants);
                checkOrder.add(orientation);
            }
        }catch (Exception ex){
            LogUtility.Log("Error while reading adjacent block state for " + assetPath);
            LogUtility.Log(ex.getMessage());
        }
    }

    /**
     * Convert the adjacent variants apply to either states or to a list of Adjacent Variants
     * @param adjacentVariant The raw adjacent variant to convert
     */
    private void convertApply(AdjacentVariant adjacentVariant){
        //Check if "apply" is a list, else threat it as a map (raw states to apply)
        if(adjacentVariant.apply instanceof List){
            List<AdjacentVariant> variants = new ArrayList<>();

            JsonElement jsonApply = GSON.toJsonTree(adjacentVariant.apply);
            List<AdjacentVariant> rawVariants = GSON.fromJson(jsonApply, new TypeToken<List<AdjacentVariant>>(){}.getType());
            for(AdjacentVariant rawVariant : rawVariants){
                convertApply(rawVariant);
                variants.add(rawVariant);
            }

            adjacentVariant.apply = variants;
        }else{
            JsonElement jsonApply = GSON.toJsonTree(adjacentVariant.apply);
            Map<String, String> states = GSON.fromJson(jsonApply, new TypeToken<Map<String, String>>(){}.getType());

            adjacentVariant.apply = states;
        }
    }

    /**
     * Get the states to apply depending on the state/states of the original block,
     * the state/state of the adjacent block and the orientation of the adjacent block
     * @param adjacentOrientation The orientation of the adjacent block, compared to the original block (ex, east...)
     * @param originalSates A map of states of the original block
     * @return A map of state to apply depending on the adjacent block
     */
    public Map<String, String> getStates(String adjacentOrientation, Map<String, String> originalSates, Map<String, String> adjacentStates){
        //Get a list of adjacent variants for the adjacent orientation
        List<AdjacentVariant> orientationVariants = adjacentOrientations.get(adjacentOrientation);

        //Loop through them
        for(AdjacentVariant adjacentVariant : orientationVariants){
            boolean matches = false;

            if(adjacentVariant.when != null)
                matches = matchWhen(adjacentVariant.when, originalSates);
            else
                matches = true;

            if(matches){
                //Check if apply is a list of adjacent variants, else threat is a map of state to apply and return it
                if(adjacentVariant.apply instanceof List){
                    List<AdjacentVariant> adjacentVariants = (List<AdjacentVariant>) adjacentVariant.apply;

                    for(AdjacentVariant variant : adjacentVariants){


                        if(variant.when != null) {
                            //Check if variant when matches the adjacent states
                            matches = matchWhen(variant.when, adjacentStates);
                        }else
                            matches = true;

                        //If it does return the apply (apply must be a map of states to apply)
                        if(matches)
                            return (Map<String, String>) variant.apply;
                    }

                }else{
                    return (Map<String, String>) adjacentVariant.apply;
                }
            }
        }

        return null;
    }

    public List<String> getCheckOrder() {
        return checkOrder;
    }

    private boolean matchWhen(Map<String, String> when, Map<String, String> inputStates){
        boolean matches = false;

        //Loop through the sates in when
        for(String property : when.keySet()){
            //Check if the state property is in the input states
            if(inputStates.containsKey(property)){
                String state = when.get(property);
                String originalState = inputStates.get(property);

                //Check if the state contains | (state consists of multiple states)
                if(state.contains("|")){
                    //Split the state into multiple states, and check if the original state is in the states
                    String[] states = state.split("\\|");
                    for(String s : states) {
                        matches = s.equals(originalState);
                        if(matches)
                            break;
                    }

                    if(!matches)
                        break;

                }else {
                    matches = state.equals(originalState);
                    if(!matches)
                        break;
                }
            }
        }

        return matches;
    }

    /**
     * Represents what states to apply (apply is a map) to the original block, depending on the state of adjacent block (when),
     * or to store adjacent variants (apply is a list), depending on the state of original block (when)
     */
    public static class AdjacentVariant {
        public Map<String, String> when;
        public Object apply;

    }
}
