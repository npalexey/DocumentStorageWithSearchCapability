package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestGroupService {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Response showUsers() {
        List<GroupBean> groupBeanList;
        try {
            groupBeanList = GroupDao.getGroups();
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

}
