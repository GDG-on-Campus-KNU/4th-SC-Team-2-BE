package com.example.soop.global.util;

public class VectorUtils {

    public static String toPgVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}
