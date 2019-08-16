package com.nikitiuk.documentstoragewithsearchcapability.services.rest;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.Hibernate;
import org.thymeleaf.context.Context;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@PermitAll
@Path("/user")
public class RestUserService {

    private static final String PATH = "/home/npalexey/workenv/DOWNLOADED/";
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showUsers() {
        List<UserBean> userBeanList;
        try {
            userBeanList = UserDao.getUsers();
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while producing list of users.");
        }
        /*Runnable getTask = () -> {
            try {
                HibernateUtil.getSessionFactory().openSession();
            } catch (Exception e) {
                throw new WebApplicationException("Error while indexing files with DB. Please, try again");
            }
        };
        executorService.execute(getTask);*/
        //Executor.updateDbInfo();
        final Context ctx = new Context();
        ctx.setVariable("entityName", "User");
        ctx.setVariable("inStorage", userBeanList);
        return ResponseService.okResponseForText("storagehome", ctx);
    }

    @PermitAll
    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    public Response createUser(UserBean userBean){
        try {
            UserDao.saveUser(userBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while creating user.");
        }
        return ResponseService.okResponseSimple("User created successfully");
    }
}
