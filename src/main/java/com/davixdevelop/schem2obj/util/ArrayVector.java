package com.davixdevelop.schem2obj.util;

public class ArrayVector {
    public static Double[] subtract(Double[] minuend, Double[] subtrahend){
        return new Double[] {minuend[0] - subtrahend[0], minuend[1] - subtrahend[1], minuend[2] - subtrahend[2]};
    }

    public static Double[] add(Double[] augend, Double[] addend){
        return new Double[] {augend[0] + addend[0], augend[1] + addend[1], augend[2] + addend[2]};
    }

    public static Double[] multiply(Double[] multiplier, Object multiplicand){
        if(multiplicand instanceof Double[]) {
            Double[] m = (Double[])multiplicand;
            return new Double[]{multiplier[0] * m[0], multiplier[1] * m[1], multiplier[2] * m[2]};
        }else {
            Double m = (Double) multiplicand;
            return new Double[]{multiplier[0] * m, multiplier[1] * m, multiplier[2] * m};
        }
    }

    public static Double[] normalize(Double[] input){
        Double m = Math.abs(Math.sqrt(Math.pow(input[0], 2) + Math.pow(input[1], 2) + Math.pow(input[2], 2)));
        return new Double[] {input[0] / m, input[1] / m, input[2] / m};
    }

    public static class MatrixRotation {
        private Double[][] matrix;
        Double rot;

        /**
         *
         * @param rot Rotation in degrees
         * @param axis Axis of rotation
         */
        public MatrixRotation(Double rot, String axis){
            rot = Math.toRadians(-rot);
            this.rot = rot;
            switch (axis){
                case "Z":
                    matrix = new Double[][]{
                            new Double[] { Math.cos(rot),Math.sin(rot),0.0,0.0},
                            new Double[] { -Math.sin(rot),Math.cos(rot),0.0,0.0},
                            new Double[] {0.0,0.0,1.0,0.0},
                            new Double[] {0.0,0.0,0.0,1.0}
                    };
                    break;
                case "X":
                    matrix = new Double[][] {
                            new Double[] {1.0,0.0,0.0,0.0},
                            new Double[] {0.0,Math.cos(rot),Math.sin(rot),0.0},
                            new Double[] {0.0,-Math.sin(rot),Math.cos(rot),0.0},
                            new Double[] {0.0,0.0,0.0,1.0}
                    };
                    break;
                case "Y":
                    matrix = new Double[][]{
                            new Double[] {Math.cos(rot),0.0,-Math.sin(rot),0.0},
                            new Double[] {0.0,1.0,0.0,0.0},
                            new Double[] {Math.sin(rot),0.0,Math.cos(rot),0.0},
                            new Double[] {0.0,0.0,0.0,1.0}
                    };
                    break;
            }
        }

        public Double[] rotate(Double[] vector, Double w){
            if(w == null)
                w = 1.0; //0 is vector, 1 is point
            double x = (vector[0] * matrix[0][0]) + (vector[1] * matrix[1][0]) + (vector[2] * matrix[2][0]) + (w * matrix[3][0]);
            double y = (vector[0] * matrix[0][1]) + (vector[1] * matrix[1][1]) + (vector[2] * matrix[2][1]) + (w * matrix[3][1]);
            double z = (vector[0] * matrix[0][2]) + (vector[1] * matrix[1][2]) + (vector[2] * matrix[2][2]) + (w * matrix[3][2]);

            return new Double[] {x, y, z};
        }

        /**
         * Return the rotation angle in degrees
         * @return rotation angle of the matrix in degrees
         */
        public Double getRot() {
            return Math.toDegrees(rot);
        }
    }
}
