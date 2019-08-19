package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestGroupService;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@PermitAll
@Path("/group")
public class RestGroupController {

    private RestGroupService groupService = new RestGroupService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showGroups() {
        return groupService.showUsers();
    }
}
