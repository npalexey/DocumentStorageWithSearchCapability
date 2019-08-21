package com.nikitiuk.documentstoragewithsearchcapability.entities.TestEntities.TestTwo;

import java.util.HashMap;
import java.util.Map;

public enum PermissionType {
    READ(1),
    WRITE(2);

    private int value;
    private static Map map = new HashMap<>();

    private PermissionType(int value) {
        this.value = value;
    }

    static {
        for (PermissionType pageType : PermissionType.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static PermissionType valueOf(int pageType) {
        return (PermissionType) map.get(pageType);
    }

    public int getValue() {
        return value;
    }
}