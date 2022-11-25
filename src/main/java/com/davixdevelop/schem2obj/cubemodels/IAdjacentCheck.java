package com.davixdevelop.schem2obj.cubemodels;

import com.davixdevelop.schem2obj.namespace.Namespace;

/**
 * A interface for CubeModels which generation is dependent on adjacent blocks
 *
 * @author DavixDevelop
 */
public interface IAdjacentCheck {
    boolean checkCollision(Namespace adjacent, int y_index, String orientation);
}
