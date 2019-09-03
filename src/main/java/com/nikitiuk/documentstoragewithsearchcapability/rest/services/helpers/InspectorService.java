package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import javassist.NotFoundException;
import org.apache.commons.lang3.StringUtils;

public class InspectorService {

    public static void checkIfIdIsNull(Long id) throws NoValidDataFromSourceException {
        if(id == null) {
            throw new NoValidDataFromSourceException("No id was passed.");
        }
    }

    public static void checkIfStringDataIsBlank(String name) throws NoValidDataFromSourceException {
        if(StringUtils.isBlank(name)){
            throw new NoValidDataFromSourceException("No valid name was passed.");
        }
    }

    public static void checkIfAnyWasDeleted(Integer quantity) throws NotFoundException {
        if(quantity == 0){
            throw new NotFoundException("No entities were deleted.");
        }
    }
}
