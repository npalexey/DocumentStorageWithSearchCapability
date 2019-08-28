package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestGroupService {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    private GroupDao groupDao = new GroupDao();

    public Response showGroups() {
        List<GroupBean> groupBeanList;
        try {
            groupBeanList = groupDao.getGroups();
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while producing list of groups.");
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
        ctx.setVariable("entityName", "Group");
        ctx.setVariable("inStorage", groupBeanList);
        return ResponseService.okResponseForText("storagehome", ctx);
    }

    public Response createGroup(GroupBean groupBean) {
        try {
            groupDao.saveGroup(groupBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while creating group.");
        }
        return ResponseService.okResponseSimple("Group created successfully");
    }

    public Response updateGroup(GroupBean groupBean) {
        try {
            groupDao.updateGroup(groupBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while updating group.");
        }
        return ResponseService.okResponseSimple("Group updated successfully");
    }

    public Response deleteGroupByName(String name) {
        try {
            groupDao.deleteGroupByName(name);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while deleting group.");
        }
        return ResponseService.okResponseSimple("Group deleted successfully");
    }

    public Response getGroupById(long id) {
        List<GroupBean> groupBeanList = new ArrayList<>();
        try {
            groupBeanList.add(groupDao.getOneById(id));
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while getting group by id.");
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "Group");
        ctx.setVariable("inStorage", groupBeanList);
        return ResponseService.okResponseForText("storagehome", ctx);
    }

    public Response deleteGroupById(long id) {
        try {
            groupDao.deleteById(id);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while deleting group by id.");
        }
        return ResponseService.okResponseSimple("Group deleted successfully");
    }

    public Response deleteGroup(Object groupNameOrId) {
        if (groupNameOrId instanceof Long) {
            return deleteGroupById((Long) groupNameOrId);
        } else if (groupNameOrId instanceof String) {
            return deleteGroupByName((String) groupNameOrId);
        }
        return ResponseService.errorResponse(404, "Error in syntax of group name or id.");
    }
}
