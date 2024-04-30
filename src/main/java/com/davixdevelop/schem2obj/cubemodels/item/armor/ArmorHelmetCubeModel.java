package com.davixdevelop.schem2obj.cubemodels.item.armor;

import com.davixdevelop.schem2obj.namespace.Namespace;

public class ArmorHelmetCubeModel extends ArmorCubeModel {
    @Override
    public boolean fromNamespace(Namespace namespace) {
        toCubeModel(namespace, "helmet","layer_1");
        return true;
    }
}
