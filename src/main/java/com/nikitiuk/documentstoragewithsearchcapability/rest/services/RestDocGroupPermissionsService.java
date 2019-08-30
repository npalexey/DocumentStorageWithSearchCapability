package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocGroupPermissionsDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocGroupPermissionsRequest;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import javassist.NotFoundException;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;

public class RestDocGroupPermissionsService {

    private DocGroupPermissionsDao docGroupPermissionsDao = new DocGroupPermissionsDao();

    public Response getAllGroupPermissionsForDocuments() {
        List<DocGroupPermissions> docGroupPermissionsList;
        try {
            docGroupPermissionsList = docGroupPermissionsDao.getAllDocGroupPermissions();
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing all permissions for documents for every group. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "DocGroupPermissions");
        ctx.setVariable("inStorage", docGroupPermissionsList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response getPermissionsForDocumentsByGroupId(Long groupId) {
        List<DocGroupPermissions> docGroupPermissionsList;
        try {
            InspectorService.checkIfIdIsNull(groupId);
            docGroupPermissionsList = docGroupPermissionsDao.getPermissionsForDocumentsForGroup(groupId);
            if (docGroupPermissionsList == null) {
                throw new NotFoundException("No permissions for such group.");
            }
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing permissions for documents for group. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "DocGroupPermissions");
        ctx.setVariable("inStorage", docGroupPermissionsList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response getPermissionsForDocumentByDocId(Long docId) {
        List<DocGroupPermissions> docGroupPermissionsList;
        try {
            InspectorService.checkIfIdIsNull(docId);
            docGroupPermissionsList = docGroupPermissionsDao.getPermissionsForDocument(docId);
            if (docGroupPermissionsList == null) {
                throw new NotFoundException("No permissions for such document.");
            }
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing permissions for document. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "DocGroupPermissions");
        ctx.setVariable("inStorage", docGroupPermissionsList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response setWriteForDocumentForGroup(DocGroupPermissionsRequest requestObj) {
        DocGroupPermissions setDocGroupPermissions;
        try {
            checkIfRequestJsonHasNulls(requestObj);
            setDocGroupPermissions = docGroupPermissionsDao.setWriteForDocumentForGroup(requestObj.getDocumentId(), requestObj.getGroupId());
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while setting write permissions for document for group. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "DocGroupPermissions");
        ctx.setVariable("entity", setDocGroupPermissions);
        ctx.setVariable("action", "set");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response setReadForDocumentForGroup(DocGroupPermissionsRequest requestObj) {
        DocGroupPermissions setDocGroupPermissions;
        try {
            checkIfRequestJsonHasNulls(requestObj);
            setDocGroupPermissions = docGroupPermissionsDao.setReadForDocumentForGroup(requestObj.getDocumentId(), requestObj.getGroupId());
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while setting read permissions for document for group. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "DocGroupPermissions");
        ctx.setVariable("entity", setDocGroupPermissions);
        ctx.setVariable("action", "set");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response deleteAllPermissionsForGroup(Long groupId) {
        Integer quantityOfDeletedPermission;
        try {
            InspectorService.checkIfIdIsNull(groupId);
            quantityOfDeletedPermission = docGroupPermissionsDao.deleteAllPermissionsForGroup(groupId);
            InspectorService.checkIfAnyWasDeleted(quantityOfDeletedPermission);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting all permissions for documents for group. " + e.getMessage());
        }
        return ResponseService.okResponseSimple("All " + quantityOfDeletedPermission + " permissions for documents for group(id:" + groupId + ") deleted successfully");
    }

    public Response deleteAllPermissionsForDocumentExceptAdmin(Long docId) {
        Integer quantityOfDeletedPermission;
        try {
            InspectorService.checkIfIdIsNull(docId);
            quantityOfDeletedPermission = docGroupPermissionsDao.deleteAllPermissionsForDocumentExceptAdmin(docId);
            InspectorService.checkIfAnyWasDeleted(quantityOfDeletedPermission);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting all permissions for document. " + e.getMessage());
        }
        return ResponseService.okResponseSimple("All " + quantityOfDeletedPermission + " permissions for document(id:" + docId + ") deleted successfully");
    }

    public Response deletePermissionsForDocumentForGroup(DocGroupPermissionsRequest requestObj) {
        Integer quantityOfDeletedPermission;
        try {
            checkIfRequestJsonHasNulls(requestObj);
            quantityOfDeletedPermission = docGroupPermissionsDao.deletePermissionsForDocumentForGroup(requestObj.getDocumentId(), requestObj.getGroupId());
            InspectorService.checkIfAnyWasDeleted(quantityOfDeletedPermission);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting permissions for document for group. " + e.getMessage());
        }
        return ResponseService.okResponseSimple(quantityOfDeletedPermission + " permission for document(id:" + requestObj.getDocumentId()
                + ") for group(id:" + requestObj.getGroupId() + ") deleted successfully");
    }

    private void checkIfRequestJsonHasNulls(DocGroupPermissionsRequest requestObj) throws NoValidDataFromSourceException {
        if (requestObj.getDocumentId() == null || requestObj.getGroupId() == null) {
            throw new NoValidDataFromSourceException("No ids were passed.");
        }
    }
}
