package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;

public class RestUserService {

    private UserDao userDao = new UserDao();

    public Response showUsers() {
        List<UserBean> userBeanList;
        try {
            userBeanList = userDao.getUsers();
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while producing list of users.");
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "User");
        ctx.setVariable("inStorage", userBeanList);
        return ResponseService.okResponseForText("storagehome", ctx);
    }

    public Response createUser(UserBean userBean) {
        try {
            userDao.saveUser(userBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while creating user.");
        }
        return ResponseService.okResponseSimple("User created successfully");
    }

    public Response updateUser(UserBean userBean) {
        try {
            userDao.updateUser(userBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while updating user.");
        }
        return ResponseService.okResponseSimple("User updated successfully");
    }

    public Response deleteUserByName(String name) {
        try {
            userDao.deleteUserByName(name);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while deleting user.");
        }
        return ResponseService.okResponseSimple("User deleted successfully");
    }
}
