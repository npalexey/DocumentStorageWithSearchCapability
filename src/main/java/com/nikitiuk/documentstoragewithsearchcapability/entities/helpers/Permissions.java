package com.nikitiuk.documentstoragewithsearchcapability.entities.helpers;

public enum Permissions {
    WRITE, READ;

    public static boolean contains(String string) {
        for(Permissions permissions : Permissions.values()){
            if (string.equals(permissions.name())){
                return true;
            }
        }
        return false;
    }
}
