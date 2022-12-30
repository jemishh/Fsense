package com.example.fsense.utils;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {
    /**
     * Search a directory and return a list of all **directories** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPath(String directory){
        ArrayList<String> pathArray=new ArrayList<>();
        File file=new File(directory);
        Log.d(TAG, "getDirectoryPath: "+file);
        File[] listOfFiles=file.listFiles();

        for(int i = 0; i < listOfFiles.length; i++){
            if(listOfFiles[i].isDirectory() && listOfFiles[i].list().length!=0){
                pathArray.add(listOfFiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }
    /**
     * Search a directory and return a list of all **files** contained inside
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory){
        ArrayList<String> pathArray=new ArrayList<>();
        File file=new File(directory);
        File[] listOfFiles=file.listFiles();
        Log.d(TAG, "getFilePaths: "+listOfFiles.length);
        for(int i = 0; i< listOfFiles.length; i++){
            if(listOfFiles[i].isFile()) {
                    pathArray.add(listOfFiles[i].getAbsolutePath());
            }

        }
        return pathArray;
    }
}
