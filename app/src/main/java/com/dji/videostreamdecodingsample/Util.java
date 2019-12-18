package com.dji.videostreamdecodingsample;



public class Util {

    //----------------------------------------------------------------------------------
    //展示路径
    static StringBuilder stringBuilder;

    private static String displayPath(String path) {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }

        path = path + "\n";
        stringBuilder.append(path);
        //savePath.setText();
        return stringBuilder.toString();
    }

}
