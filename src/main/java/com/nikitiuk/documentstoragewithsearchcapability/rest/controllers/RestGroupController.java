package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestGroupService;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@PermitAll
@Path("/group")
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

    @PermitAll
    @GET
    @Path("/{groupname}")
    @Produces(MediaType.TEXT_HTML)
    public Response getGroupByName(@PathParam("groupname") String groupName) {
        return groupService.getGroupByName(groupName);
    }

    @PermitAll
    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response createGroup(GroupBean groupBean) {
        return groupService.createGroup(groupBean);
    }

    @PermitAll
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response updateGroup(GroupBean groupBean) {
        return groupService.updateGroup(groupBean);
    }

    @PermitAll
    @DELETE
    @Path("/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteGroupById(@PathParam("groupid") long groupId) {
        return groupService.deleteGroupById(groupId);
    }

    @PermitAll
    @DELETE
    @Path("/delete-by-name/{groupname}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteGroupByName(@PathParam("groupname") String groupname) {
        return groupService.deleteGroupByName(groupname);
    }
}
