package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestUserService;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@PermitAll
@Path("/user")
public class RestUserController {

    private RestUserService userService = new RestUserService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showUsers() {
        return userService.showUsers();
    }

    @PermitAll
    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response createUser(UserBean userBean) {
        return userService.createUser(userBean);
    }

    @PermitAll
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response updateUser(UserBean userBean) {
        return userService.updateUser(userBean);
    }

    @PermitAll
    @DELETE
    @Path("/{username}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteUserByName(@PathParam("username") String username) {
        return userService.deleteUserByName(username);
    }
}
