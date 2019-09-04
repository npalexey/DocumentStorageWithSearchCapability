package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.*;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoRightsForActionException;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocService;
import javassist.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class InspectorService {
    private static final Logger logger = LoggerFactory.getLogger(RestDocService.class);

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

    public static void checkIfUserHasRightsForDocument(UserBean user, DocBean docBean, Permissions permissions) throws NoRightsForActionException {

        if (user.getGroups().isEmpty()) {
            throw new NoRightsForActionException("User " + user.getName() + " is not in any group.");
        }
        Set<GroupBean> groupSet = user.getGroups();
        for (GroupBean group : groupSet) {
            for (DocGroupPermissions docGroupPermissions : group.getDocumentsPermissions()) {
                if (permissions == Permissions.READ && docGroupPermissions.getDocument().equals(docBean)) {
                    return;
                }
                if (permissions == Permissions.WRITE && docGroupPermissions.getDocument().equals(docBean)
                        && docGroupPermissions.getPermissions() == permissions ) {
                    return;
                }
            }
        }
        throw new NoRightsForActionException("User " + user.getName() + " doesn't have rights to " + permissions.toString() + " this document.");
    }

    public static void checkIfUserHasRightsForFolder(UserBean user, FolderBean folder, Permissions permissions) throws NoRightsForActionException {
        if(user.getGroups().isEmpty()){
            throw new NoRightsForActionException("User " + user.getName() + " is not in any group.");
        }
        Set<GroupBean> groupSet = user.getGroups();
        for(GroupBean group : groupSet) {
            for(FolderGroupPermissions folderGroupPermissions : group.getFoldersPermissions()){
                if(permissions == Permissions.READ && folderGroupPermissions.getFolder().equals(folder)){
                    return;
                }
                if(permissions == Permissions.WRITE && folderGroupPermissions.getFolder().equals(folder)
                        && folderGroupPermissions.getPermissions() == permissions){
                    return;
                }
            }
        }
        throw new NoRightsForActionException("User " + user.getName() + " doesn't have rights to " + permissions.toString() + " to this folder.");
    }
}
