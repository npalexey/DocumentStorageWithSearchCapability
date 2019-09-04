package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestUserService;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Tag(name = "User Controller")
@PermitAll
@Path("/user")
public class RestUserController {

    private RestUserService userService = new RestUserService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getUsers() {
        return userService.getUsers();
    }

    @PermitAll
    @GET
    @Path("/{userid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getUserById(@PathParam("userid") long userId) {
        return userService.getUserById(userId);
    }

    @PermitAll
    @GET
    @Path("/by-name/{username}")
    @Produces(MediaType.TEXT_HTML)
    public Response getUserByName(@PathParam("username") String username) {
        return userService.getUserByName(username);
    }

    @RolesAllowed({ "ADMINS" })
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response createUser(UserBean userBean) {
        return userService.createUser(userBean);
    }

    @RolesAllowed({ "ADMINS" })
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response updateUser(UserBean userBean) {
        return userService.updateUser(userBean);
    }

    @RolesAllowed({ "ADMINS" })
    @DELETE
    @Path("/{userid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteUserById(@PathParam("userid") long userId){
        return userService.deleteUserById(userId);
    }

    @RolesAllowed({ "ADMINS" })
    @DELETE
    @Path("/by-name/{username}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteUserByName(@PathParam("username") String username) {
        return userService.deleteUserByName(username);
    }
}
