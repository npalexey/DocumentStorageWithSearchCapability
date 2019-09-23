package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocGroupPermissionsDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocGroupPermissionsRequest;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;

import java.util.List;

public class RestDocGroupPermissionsService {

    private DocGroupPermissionsDao docGroupPermissionsDao = new DocGroupPermissionsDao();

    public List<DocGroupPermissions> getAllGroupPermissionsForDocuments() throws Exception {
        return docGroupPermissionsDao.getAllDocGroupPermissions();
    }

    public List<DocGroupPermissions> getPermissionsForDocumentsByGroupId(Long groupId) throws Exception {
        InspectorService.checkIfIdIsNull(groupId);
        List<DocGroupPermissions> docGroupPermissionsList =
                docGroupPermissionsDao.getPermissionsForDocumentsForGroup(groupId);
        InspectorService.checkIfDesiredCollectionIsEmpty(docGroupPermissionsList);
        return docGroupPermissionsList;
    }

    public List<DocGroupPermissions> getPermissionsForDocumentByDocId(Long docId) throws Exception {
        InspectorService.checkIfIdIsNull(docId);
        List<DocGroupPermissions> docGroupPermissionsList = docGroupPermissionsDao.getPermissionsForDocument(docId);
        InspectorService.checkIfDesiredCollectionIsEmpty(docGroupPermissionsList);
        return docGroupPermissionsList;
    }

    public DocGroupPermissions setWriteForDocumentForGroup(DocGroupPermissionsRequest requestObj) throws Exception {
        checkIfRequestJsonHasNulls(requestObj);
        return docGroupPermissionsDao.setWriteForDocumentForGroup(requestObj.getDocumentId(), requestObj.getGroupId());
    }

    public DocGroupPermissions setReadForDocumentForGroup(DocGroupPermissionsRequest requestObj) throws Exception {
        checkIfRequestJsonHasNulls(requestObj);
        return docGroupPermissionsDao.setReadForDocumentForGroup(requestObj.getDocumentId(), requestObj.getGroupId());
    }

    public Integer deleteAllPermissionsForGroup(Long groupId) throws Exception {
        InspectorService.checkIfIdIsNull(groupId);
        Integer quantityOfDeletedPermission = docGroupPermissionsDao.deleteAllPermissionsForGroup(groupId);
        InspectorService.checkIfAnyWasDeleted(quantityOfDeletedPermission);
        return quantityOfDeletedPermission;
    }

    public Integer deleteAllPermissionsForDocumentExceptAdmin(Long docId) throws Exception {
        InspectorService.checkIfIdIsNull(docId);
        Integer quantityOfDeletedPermission = docGroupPermissionsDao.deleteAllPermissionsForDocumentExceptAdmin(docId);
        InspectorService.checkIfAnyWasDeleted(quantityOfDeletedPermission);
        return quantityOfDeletedPermission;
    }

    public Integer deletePermissionsForDocumentForGroup(DocGroupPermissionsRequest requestObj) throws Exception {
        checkIfRequestJsonHasNulls(requestObj);
        Integer quantityOfDeletedPermission = docGroupPermissionsDao
                .deletePermissionsForDocumentForGroup(requestObj.getDocumentId(), requestObj.getGroupId());
        InspectorService.checkIfAnyWasDeleted(quantityOfDeletedPermission);
        return quantityOfDeletedPermission;
    }

    private void checkIfRequestJsonHasNulls(DocGroupPermissionsRequest requestObj)
            throws NoValidDataFromSourceException {
        if (requestObj.getDocumentId() == null || requestObj.getGroupId() == null) {
            throw new NoValidDataFromSourceException("No ids were passed.");
        }
    }
}