package com.davixdevelop.schem2obj.cubemodels.item.armor;

import com.davixdevelop.schem2obj.namespace.Namespace;

public class ArmorLeggingsCubeModel extends ArmorCubeModel{
    @Override
    public boolean fromNamespace(Namespace namespace) {
        toCubeModel(namespace, "leggings", "layer_2");
        return true;
    }
}
