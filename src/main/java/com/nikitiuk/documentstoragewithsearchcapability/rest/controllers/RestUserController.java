package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestUserService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ThymeleafResponseService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.enums.Actions;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.enums.EntityTypes;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Tag(name = "User Controller")
@PermitAll
@Path("/users")
public class RestUserController {

    private static final Logger logger = LoggerFactory.getLogger(RestUserController.class);
    private RestUserService userService = new RestUserService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getUsers() {
        try {
            List<UserBean> userBeanList = userService.getUsers();
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.USER, userBeanList);
        } catch (Exception e) {
            logger.error("Error at RestUserController getUsers.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while producing list of users. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{userid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getSingleUser(@PathParam("userid") long userId) {
        try {
            UserBean singleUser = userService.getSingleUser(userId);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.USER, singleUser, Actions.FOUND);
        } catch (Exception e) {
            logger.error("Error at RestUserController getSingleUser.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while getting user by id: %d. %s", userId, e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response createUser(UserBean userBean) {
        try {
            UserBean createdUser = userService.createUser(userBean);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.USER, createdUser, Actions.CREATED);
        } catch (Exception e) {
            logger.error("Error at RestUserController createUser.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, String.format("Error while creating user. %s", e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response updateUser(UserBean userBean) {
        try {
            UserBean updatedUser = userService.updateUser(userBean);
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.USER, updatedUser, Actions.UPDATED);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, String.format("Error while updating user. %s", e.getMessage()));
        }
    }

    @RolesAllowed({ "ADMINS" })
    @DELETE
    @Path("/{userid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteUserById(@PathParam("userid") long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseService.okResponseSimple(String.format("User with id: %d deleted successfully.", userId));
        } catch (Exception e) {
            logger.error("Error at RestUserController deleteUser.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, String.format("Error while deleting user by id: %d. %s", userId, e.getMessage()));
        }
    }
}
