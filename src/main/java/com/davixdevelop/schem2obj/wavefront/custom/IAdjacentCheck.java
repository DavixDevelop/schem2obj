package com.davixdevelop.schem2obj.wavefront.custom;

import com.davixdevelop.schem2obj.namespace.Namespace;

public interface IAdjacentCheck {
    boolean checkCollision(Namespace adjacent, int y_index, String orientation);
}
