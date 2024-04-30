package com.davixdevelop.schem2obj.cubemodels.item.armor;

import com.davixdevelop.schem2obj.namespace.Namespace;

public class ArmorBootsCubeModel extends ArmorCubeModel{
    @Override
    public boolean fromNamespace(Namespace namespace) {
        toCubeModel(namespace, "boots", "layer_1");
        return true;
    }
}
