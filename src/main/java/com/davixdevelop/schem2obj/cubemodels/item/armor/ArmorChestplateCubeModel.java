package com.davixdevelop.schem2obj.cubemodels.item.armor;

import com.davixdevelop.schem2obj.namespace.Namespace;

public class ArmorChestplateCubeModel extends ArmorCubeModel{
    @Override
    public boolean fromNamespace(Namespace namespace) {
        toCubeModel(namespace, "chestplate", "layer_1");
        return true;
    }
}
