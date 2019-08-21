package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.rest.services.ResponseService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocGroupPermissionsService;
import org.thymeleaf.context.Context;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@PermitAll
@Path("/permissions")
public class RestDocGroupPermissionsController {

    private RestDocGroupPermissionsService restDocGroupPermissionsService= new RestDocGroupPermissionsService();

    @PermitAll
    @GET
    @Path("/{groupid}")
    @Produces(MediaType.TEXT_HTML)
    public Response showGroupPermissionsForDocuments(@PathParam("groupid") long groupid) {
        return restDocGroupPermissionsService.showGroupPermissionsForDocuments(groupid);
    }
}
