package com.davixdevelop.schem2obj.models;

import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockstates.BlockState;

import java.util.ArrayList;

public class VariantModels {
    private BlockState.Variant variant;
    private ArrayList<BlockModel> models;

    public VariantModels(BlockState.Variant variant, ArrayList<BlockModel> models){
        this.variant = variant;
        this.models = models;
    }

    public BlockState.Variant getVariant() {
        return variant;
    }

    public ArrayList<BlockModel> getModels() {
        return models;
    }
}
