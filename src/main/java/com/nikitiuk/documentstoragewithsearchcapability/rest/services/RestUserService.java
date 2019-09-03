package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;

public class RestUserService {

    private UserDao userDao = new UserDao();

    public Response getUsers() {
        List<UserBean> userBeanList;
        try {
            userBeanList = userDao.getUsers();
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing list of users. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "User");
        ctx.setVariable("inStorage", userBeanList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response getUserById(Long userId) {
        UserBean userById;
        try {
            InspectorService.checkIfIdIsNull(userId);
            userById = userDao.getById(userId);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while getting user by id. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "User");
        ctx.setVariable("entity", userById);
        ctx.setVariable("action", "found");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response getUserByName(String username){
        UserBean userByName;
        try{
            InspectorService.checkIfStringDataIsBlank(username);
            userByName = userDao.getUserByName(username);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while getting user by name. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "User");
        ctx.setVariable("entity", userByName);
        ctx.setVariable("action", "found");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response createUser(UserBean userBean) {
        UserBean createdUser;
        try {
            checkOnCreateOrUpdateForNulls(userBean);
            createdUser = userDao.saveUser(userBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while creating user. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "User");
        ctx.setVariable("entity", createdUser);
        ctx.setVariable("action", "created");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response updateUser(UserBean userBean) {
        UserBean updatedUser;
        try {
            checkOnCreateOrUpdateForNulls(userBean);
            updatedUser = userDao.updateUser(userBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while updating user. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "User");
        ctx.setVariable("entity", updatedUser);
        ctx.setVariable("action", "updated");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response deleteUserById(Long userId) {
        try {
            InspectorService.checkIfIdIsNull(userId);
            userDao.deleteById(userId);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting user. " + e.getMessage());
        }
        return ResponseService.okResponseSimple("User deleted successfully");
    }

    public Response deleteUserByName(String username) {
        try {
            InspectorService.checkIfStringDataIsBlank(username);
            userDao.deleteUserByName(username);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting user. " + e.getMessage());
        }
        return ResponseService.okResponseSimple("User deleted successfully");
    }

    private void checkOnCreateOrUpdateForNulls(UserBean userBean) throws NoValidDataFromSourceException {
        if(userBean == null || StringUtils.isBlank(userBean.getName())){
            throw new NoValidDataFromSourceException("No valid data was passed.");
        }
    }
}
