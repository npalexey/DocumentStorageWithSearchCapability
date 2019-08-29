package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocGroupPermissionsDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.requests.DocGroupPermissionsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;

public class RestDocGroupPermissionsService {

    private static final Logger logger = LoggerFactory.getLogger(RestDocGroupPermissionsService.class);
    private DocGroupPermissionsDao docGroupPermissionsDao = new DocGroupPermissionsDao();

    public Response showPermissionsForDocumentsByGroupId(long groupId) {
        List<DocGroupPermissions> docGroupPermissionsList;
        try {
            GroupDao groupDao = new GroupDao();
            GroupBean checkedGroupBean = groupDao.getById(groupId);
            docGroupPermissionsList = docGroupPermissionsDao.getGroupPermissionsForDocuments(checkedGroupBean);
        } catch (Exception e) {
            logger.error("Error at showGroupPermissionsForDocuments: ", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing permissions for documents for group.");
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "DocGroupPermissions");
        ctx.setVariable("inStorage", docGroupPermissionsList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response showAllGroupPermissionsForDocuments() {
        List<DocGroupPermissions> docGroupPermissionsList;
        try {
            docGroupPermissionsList = docGroupPermissionsDao.getAllDocGroupPermissions();
        } catch (Exception e) {
            logger.error("Error at showAllGroupPermissionsForDocuments: ", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing all permissions for documents for every group.");
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "DocGroupPermissions");
        ctx.setVariable("inStorage", docGroupPermissionsList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response setWriteForDocumentForGroup(DocGroupPermissionsRequest requestObj) {
        if (checkIfRequestJsonHasNulls(requestObj)) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "No ids were passed.");
        }
        try {
            docGroupPermissionsDao.setWriteForDocumentForGroup(requestObj.getDocumentId(), requestObj.getGroupId());
        } catch (Exception e) {
            logger.error("Error at setWritePermissionsForDocumentForGroup: ", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while setting write permissions for document for group.");
        }
        return ResponseService.okResponseSimple("Permissions set successfully");
    }

    public Response setReadForDocumentForGroup(DocGroupPermissionsRequest requestObj) {
        if (checkIfRequestJsonHasNulls(requestObj)) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "No ids were passed.");
        }
        try {
            docGroupPermissionsDao.setReadForDocumentForGroup(requestObj.getDocumentId(), requestObj.getGroupId());
        } catch (Exception e) {
            logger.error("Error at setWritePermissionsForDocumentForGroup: ", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while setting read permissions for document for group.");
        }
        return ResponseService.okResponseSimple("Permissions set successfully");
    }

    public Response deleteAllPermissionsForGroup(long groupId) {
        try {
            GroupDao groupDao = new GroupDao();
            GroupBean checkedGroupBean = groupDao.getById(groupId);
            docGroupPermissionsDao.deleteAllPermissionsForGroup(checkedGroupBean);
        } catch (Exception e) {
            logger.error("Error at deleteAllPermissionsForGroup: ", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting all permissions for documents for group.");
        }
        return ResponseService.okResponseSimple("All permissions for documents for group deleted successfully");
    }

    public Response deleteAllPermissionsForDocumentExceptAdmin(long documentId) {
        try {
            DocDao docDao = new DocDao();
            DocBean checkedDocBean = docDao.getById(documentId);
            docGroupPermissionsDao.deleteAllPermissionsForDocumentExceptAdmin(checkedDocBean);
        } catch (Exception e) {
            logger.error("Error at deleteAllPermissionsForDocumentExceptAdmin: ", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting all permissions for document.");
        }
        return ResponseService.okResponseSimple("All permissions for document deleted successfully");
    }

    public Response deletePermissionsForDocumentForGroup(DocGroupPermissionsRequest requestObj) {
        if (checkIfRequestJsonHasNulls(requestObj)) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "No ids were passed.");
        }
        try {
            DocDao docDao = new DocDao();
            GroupDao groupDao = new GroupDao();
            DocBean checkedDocBean = docDao.getById(requestObj.getDocumentId());
            GroupBean checkedGroupBean = groupDao.getById(requestObj.getGroupId());
            docGroupPermissionsDao.deletePermissionsForDocumentForGroup(checkedDocBean, checkedGroupBean);
        } catch (Exception e) {
            logger.error("Error at deletePermissionsForDocumentForGroup: ", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting permissions for document for group.");
        }
        return ResponseService.okResponseSimple("Permissions for document for group deleted successfully");
    }

    private boolean checkIfRequestJsonHasNulls(DocGroupPermissionsRequest requestObj) {
        return requestObj.getDocumentId() == null || requestObj.getGroupId() == null;
    }
}
