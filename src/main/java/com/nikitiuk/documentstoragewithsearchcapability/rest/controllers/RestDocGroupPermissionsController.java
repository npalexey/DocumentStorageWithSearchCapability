package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocGroupPermissionsRequest;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocGroupPermissionsService;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Tag(name = "Document Permissions Controller")
@RolesAllowed({ "ADMINS" })
@Path("/permissions")
public class RestDocGroupPermissionsController {

    private RestDocGroupPermissionsService restDocGroupPermissionsService = new RestDocGroupPermissionsService();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getAllGroupPermissionsForDocuments() {
        return restDocGroupPermissionsService.getAllGroupPermissionsForDocuments();
    }

    @GET
    @Path("/for-group/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getPermissionsForDocumentsByGroupId(@PathParam("groupid") long groupId) {
        return restDocGroupPermissionsService.getPermissionsForDocumentsByGroupId(groupId);
    }

    @GET
    @Path("/for-document/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getPermissionsForDocumentByDocId(@PathParam("documentid") long docId) {
        return restDocGroupPermissionsService.getPermissionsForDocumentByDocId(docId);
    }

    @PUT
    @Path("/set-write")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response setWritePermissionsForGroupForDocument(DocGroupPermissionsRequest requestObj) {
        return restDocGroupPermissionsService.setWriteForDocumentForGroup(requestObj);
    }

    @PUT
    @Path("/set-read")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response setReadPermissionsForGroupForDocument(DocGroupPermissionsRequest requestObj) {
        return restDocGroupPermissionsService.setReadForDocumentForGroup(requestObj);
    }

    @DELETE
    @Path("/for-group/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteAllPermissionsForGroup(@PathParam("groupid") long groupid) {
        return restDocGroupPermissionsService.deleteAllPermissionsForGroup(groupid);
    }

    @DELETE
    @Path("/for-document/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteAllPermissionsForDocument(@PathParam("documentid") long documentid) {
        return restDocGroupPermissionsService.deleteAllPermissionsForDocumentExceptAdmin(documentid);
    }

    @DELETE
    @Path("/for-document-for-group/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response deletePermissionsForDocumentForGroup(DocGroupPermissionsRequest requestObj) {
        return restDocGroupPermissionsService.deletePermissionsForDocumentForGroup(requestObj);
    }
}