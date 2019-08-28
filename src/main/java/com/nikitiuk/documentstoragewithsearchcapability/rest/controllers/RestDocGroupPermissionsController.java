package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.requests.DocGroupPermissionsRequest;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocGroupPermissionsService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RolesAllowed({ "ADMINS" })
@Path("/permissions")
public class RestDocGroupPermissionsController {

    private RestDocGroupPermissionsService restDocGroupPermissionsService = new RestDocGroupPermissionsService();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showAllGroupPermissionsForDocuments() {
        return restDocGroupPermissionsService.showAllGroupPermissionsForDocuments();
    }

    @GET
    @Path("/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response showPermissionsForDocumentsByGroupId(@PathParam("groupid") long groupid) {
        return restDocGroupPermissionsService.showPermissionsForDocumentsByGroupId(groupid);
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
    @Path("/delete-all-for-group/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteAllPermissionsForGroup(@PathParam("groupid") long groupid) {
        return restDocGroupPermissionsService.deleteAllPermissionsForGroup(groupid);
    }

    @DELETE
    @Path("/delete-all-for-document/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteAllPermissionsForDocument(@PathParam("documentid") long documentid) {
        return restDocGroupPermissionsService.deleteAllPermissionsForDocumentExceptAdmin(documentid);
    }

    @DELETE
    @Path("/delete-for-document-for-group/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response deletePermissionsForDocumentForGroup(DocGroupPermissionsRequest requestObj) {
        return restDocGroupPermissionsService.deletePermissionsForDocumentForGroup(requestObj);
    }
}