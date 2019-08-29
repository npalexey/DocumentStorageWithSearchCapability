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
    @Path("/get-by-name/{username}")
    @Produces(MediaType.TEXT_HTML)
    public Response getUserByName(@PathParam("username") String username) {
        return userService.getUserByName(username);
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
    @Path("/{userid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteUserById(@PathParam("userid") long userId){
        return userService.deleteUserById(userId);
    }

    @PermitAll
    @DELETE
    @Path("/delete-by-name/{username}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteUserByName(@PathParam("username") String username) {
        return userService.deleteUserByName(username);
    }
}
