package com.davixdevelop.schem2obj.util;

import com.davixdevelop.schem2obj.blockmodels.CubeElement;
import com.davixdevelop.schem2obj.cubemodels.CubeModelUtility;

import java.util.ArrayList;
import java.util.List;

public class BlockModelUtility {
    /**
     * Shift each element to reflect changes when parent element is rotated
     * @param parentRotation To rotation of the parent element (X, Z, Y)
     * @param parentOrigin The origin of the parent element (X, Y, Z)
     * @param cubeElements A list of cube elements
     * @param elementIndex Index/indexes of cube elements to modify
     */
    public static void shiftElementFromParentElement(Double[] parentRotation, Double[] parentOrigin, List<CubeElement> cubeElements, Integer ...elementIndex){
        for(Integer index : elementIndex){
            CubeElement element = cubeElements.get(index);

            CubeElement.CubeRotation cubeRotation = element.getRotation();

            //Get element rotation origin
            Double[] rotationOrigin = cubeRotation.getOrigin();
            //Get element from and to
            Double[] from = element.getFrom();
            Double[] to = element.getTo();

            for(int axis = 0; axis < 3; axis++){
                if(!parentRotation[axis].equals(0.0)){
                    //Construct rotation matrix for axis
                    ArrayVector.MatrixRotation rotation = new ArrayVector.MatrixRotation(parentRotation[axis], axis == 0 ? "X" : axis == 1 ? "Z" : "Y");

                    //Rotate the element rotation origin, "from" and "to" around the parent rotation origin
                    rotationOrigin = CubeModelUtility.rotatePoint(rotationOrigin, rotation, parentOrigin);
                    from = CubeModelUtility.rotatePoint(from, rotation, parentOrigin);
                    to = CubeModelUtility.rotatePoint(to, rotation, parentOrigin);

                }
            }

            element.setFrom(from);
            element.setTo(to);

            cubeRotation.setOrigin(rotationOrigin);
            element.setRotation(cubeRotation);

            cubeElements.set(index, element);
        }
    }

    /**
     * Set the cube element/elements rotation to the desired rotation
     * @param rotation A 3 size float rotation array (X, Z, Y)
     * @param elements A list of cube elements
     * @param elementIndex Index/indexes of cube elements to modify
     */
    public static void setElementRotation(Double[] rotation, List<CubeElement> elements, Integer ...elementIndex){
        for(Integer index : elementIndex){
            CubeElement element = elements.get(index);


            List<String> rotationAxis = new ArrayList<>();
            List<Double> rotationAngle = new ArrayList<>();

            for(int axis = 0; axis < 3; axis++){
                if(!rotation[axis].equals(0.0)){
                    rotationAxis.add(axis == 0 ? "X" : axis == 1 ? "Z" : "Y");
                    rotationAngle.add(rotation[axis]);
                }
            }

            CubeElement.CubeRotation cubeRotation = element.getRotation();
            cubeRotation.setAxis(rotationAxis.toArray(new String[0]));
            cubeRotation.setAngle(rotationAngle.toArray(new Double[0]));
            element.setRotation(cubeRotation);

            elements.set(index, element);
        }
    }

    /**
     * Get the rotation of a cube element
     * @param element A cube element
     * @return A double array of rotation (X, Z, Y)
     */
    public static Double[] getElementRotation(CubeElement element){
        Double[] rotation = new Double[] {0.0, 0.0, 0.0};

        CubeElement.CubeRotation cubeRotation = element.getRotation();
        if(cubeRotation != null){
            String[] axis = cubeRotation.getAxis();
            Double[] angle = cubeRotation.getAngle();
            if(axis != null){
                for(int c = 0; c < axis.length; c++) {
                    String a = axis[c];

                    if(a.equals("X"))
                        rotation[0] = angle[c];
                    else if(a.equals("Y"))
                        rotation[2] = angle[c];
                    else
                        rotation[1] = angle[c];
                }
            }
        }

        return rotation;
    }

    /**
     * Get the rotation origin of the element
     * @param element A cube element
     * @return A double array of the origin (X, Y, Z)
     */
    public static Double[] getElementRotationOrigin(CubeElement element){
        CubeElement.CubeRotation cubeRotation = element.getRotation();
        if(cubeRotation != null){
            if(cubeRotation.getOrigin() != null)
                return cubeRotation.getOrigin();
        }

        return new Double[] {0.0, 0.0, 0.0};
    }

    /**
     * Move the element to the desired new origin
     * @param element A element to move
     * @param origin The origin of the element (X, Y, Z)
     * @param newOrigin The new origin of the element (X, Y, Z)
     */
    public static CubeElement moveElement(CubeElement element, Double[] origin, Double[] newOrigin){
        Double[] originDif = ArrayVector.subtract(newOrigin, origin);

        element.setTo(ArrayVector.add(element.getTo(), originDif));
        element.setFrom(ArrayVector.add(element.getFrom(), originDif));

        if(element.getRotation() != null && element.getRotation().getOrigin() != null){
            CubeElement.CubeRotation cubeRotation = element.getRotation();
            cubeRotation.setOrigin(ArrayVector.add(cubeRotation.getOrigin(), originDif));
            element.setRotation(cubeRotation);
        }

        return element;
    }
}
