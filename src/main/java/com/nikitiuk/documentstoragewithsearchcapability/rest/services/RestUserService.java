package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestUserService {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

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

    public Response createUser(UserBean userBean) {
        try {
            UserDao.saveUser(userBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while creating user.");
        }
        return ResponseService.okResponseSimple("User created successfully");
    }

    public Response updateUser(UserBean userBean) {
        try {
            UserDao.updateUser(userBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while updating user.");
        }
        return ResponseService.okResponseSimple("User updated successfully");
    }

    public Response deleteUserByName(String name) {
        try {
            UserDao.deleteUserByName(name);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while deleting user.");
        }
        return ResponseService.okResponseSimple("User deleted successfully");
    }
}
