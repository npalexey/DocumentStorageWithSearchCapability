package com.nikitiuk.documentstoragewithsearchcapability;

import java.io.File;

public class FolderHierarchyService {

    private static final String ROOT = "/home/npalexey/workenv/DOWNLOADED/";

    public static void checkIfPathExists(String pathToCheck){
        if(new File(pathToCheck).exists()){

        }
    }
}
