package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.enums;

public enum Actions {

    CREATED, UPDATED, FOUND, UPLOADED;

    public static Boolean contains(String string) {
        for(Actions actions : Actions.values()){
            if (string.equals(actions.name())){
                return true;
            }
        }
        return false;
    }
}
