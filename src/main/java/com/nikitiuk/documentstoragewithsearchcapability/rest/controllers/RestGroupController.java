package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestGroupService;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Tag(name = "Group Controller")
@PermitAll
@Path("/groups")
public class RestGroupController {

    private RestGroupService groupService = new RestGroupService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getGroups() {
        return groupService.getGroups();
    }

    @PermitAll
    @GET
    @Path("/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getGroupById(@PathParam("groupid") long groupId) {
        return groupService.getGroupById(groupId);
    }

    @RolesAllowed({ "ADMINS" })
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response createGroup(GroupBean groupBean) {
        return groupService.createGroup(groupBean);
    }

    @RolesAllowed({ "ADMINS" })
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response updateGroup(GroupBean groupBean) {
        return groupService.updateGroup(groupBean);
    }

    @RolesAllowed({ "ADMINS" })
    @DELETE
    @Path("/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteGroupById(@PathParam("groupid") long groupId) {
        return groupService.deleteGroupById(groupId);
    }
}
