package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.*;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoRightsForActionException;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocService;
import javassist.NotFoundException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class InspectorService {

    private static final Logger logger = LoggerFactory.getLogger(InspectorService.class);

    public static void checkIfIdIsNull(Long id) throws NoValidDataFromSourceException {
        if (id == null) {
            throw new NoValidDataFromSourceException("No id was passed.");
        }
    }

    public static void checkIfStringDataIsBlank(String name) throws NoValidDataFromSourceException {
        if (StringUtils.isBlank(name)) {
            throw new NoValidDataFromSourceException("No valid name was passed.");
        }
    }

    public static void checkIfAnyWasDeleted(Integer quantity) throws NotFoundException {
        if (quantity == 0) {
            throw new NotFoundException("No entities were deleted.");
        }
    }

    public static void checkIfDocumentIsNull(DocBean document) throws NotFoundException {
        if(document == null || document.getPath() == null) {
            throw new NotFoundException("No document with such properties exists.");
        }
    }

    public static void checkIfFolderIsNull(FolderBean folder) throws NotFoundException {
        if(folder == null || folder.getPath() == null) {
            throw new NotFoundException("No folder with such properties exists.");
        }
    }

    public static void checkIfGroupIsNull(GroupBean group) throws NotFoundException {
        if(group == null || group.getName() == null) {
            throw new NotFoundException("No group with such properties exists.");
        }
    }

    public static void checkIfUserIsNull(UserBean user) throws NotFoundException {
        if(user == null || user.getName() == null) {
            throw new NotFoundException("No user with such properties exists.");
        }
    }

    public static Set<GroupBean> checkIfUserHasRightsForDocument(UserBean user, DocBean document, Permissions permissions) throws NoRightsForActionException {
        if (CollectionUtils.isEmpty(user.getGroups())) {
            throw new NoRightsForActionException("User " + user.getName() + " is not in any group.");
        }
        Set<GroupBean> userGroups = user.getGroups();
        Set<GroupBean> groupSet = new HashSet<>();
        /*for (GroupBean group : userGroups) {
            for (DocGroupPermissions docGroupPermissions : group.getDocumentsPermissions()) {
                if (permissions == Permissions.READ && docGroupPermissions.getDocument().equals(document)) {
                    groupSet.add(group);
                }
                if (permissions == Permissions.WRITE && docGroupPermissions.getDocument().equals(document)
                        && docGroupPermissions.getPermissions() == permissions) {
                    groupSet.add(group);
                }
            }
        }*/
        for(GroupBean group : userGroups) {
            if(permissions == Permissions.READ && document.checkIfDocumentHasGroup(group)) {
                groupSet.add(group);
            }
            if(permissions == Permissions.WRITE) {
                for(DocGroupPermissions docGroupPermissions : document.getDocumentsPermissions()){
                    if(docGroupPermissions.getGroup().equals(group) && docGroupPermissions.getPermissions() == permissions) {
                        groupSet.add(group);
                    }
                }
            }
        }
        if(groupSet.isEmpty()){
            throw new NoRightsForActionException("User " + user.getName() + " doesn't have rights to " + permissions.toString() + " this document.");
        }
        return groupSet;
    }

    public static Set<GroupBean> checkIfUserHasRightsForFolder(UserBean user, FolderBean folder, Permissions permissions) throws NoRightsForActionException {
        if (user.getGroups().isEmpty()) {
            throw new NoRightsForActionException("User " + user.getName() + " is not in any group.");
        }
        Set<GroupBean> userGroups = user.getGroups();
        Set<GroupBean> groupSet = new HashSet<>();
        /*for (GroupBean group : userGroups) {
            for (FolderGroupPermissions folderGroupPermissions : group.getFoldersPermissions()) {
                if (permissions == Permissions.READ && folderGroupPermissions.getFolder().equals(folder)) {
                    groupSet.add(group);
                }
                if (permissions == Permissions.WRITE && folderGroupPermissions.getFolder().equals(folder)
                        && folderGroupPermissions.getPermissions() == permissions) {
                    groupSet.add(group);
                }
            }
        }*/
        for(GroupBean group : userGroups) {
            if(permissions == Permissions.READ && folder.checkIfFolderHasGroup(group)) {
                groupSet.add(group);
            }
            if(permissions == Permissions.WRITE) {
                for(FolderGroupPermissions folderGroupPermissions : folder.getFoldersPermissions()){
                    if(folderGroupPermissions.getGroup().equals(group) && folderGroupPermissions.getPermissions() == permissions) {
                        groupSet.add(group);
                    }
                }
            }
        }
        if(groupSet.isEmpty()){
            throw new NoRightsForActionException("User " + user.getName() + " doesn't have rights to " + permissions.toString() + " to/from this folder.");
        }
        return groupSet;
    }
}