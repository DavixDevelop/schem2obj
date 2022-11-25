package com.davixdevelop.schem2obj.models;

import com.davixdevelop.schem2obj.blockmodels.BlockModel;
import com.davixdevelop.schem2obj.blockstates.BlockState;

import java.util.ArrayList;

/**
 * Stores all BlockModels a variant uses
 *
 * @author DavixDevelop
 */
public class VariantModels {
    BlockState.Variant variant;
    ArrayList<BlockModel> models;

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
