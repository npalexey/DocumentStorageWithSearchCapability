package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocGroupPermissionsDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;

public class RestDocGroupPermissionsService {

    private DocGroupPermissionsDao docGroupPermissionsDao = new DocGroupPermissionsDao();
    private GroupDao groupDao = new GroupDao();

    public Response showGroupPermissionsForDocuments(long groupId) {
        List<DocGroupPermissions> docGroupPermissionsList;
        GroupBean checkedGroupBean = groupDao.getOneById(groupId);
        try {
            docGroupPermissionsList = docGroupPermissionsDao.getGroupPermissionsForDocuments(checkedGroupBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while producing group permissions for documents.");
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
        ctx.setVariable("entityName", "DocGroupPermissions");
        ctx.setVariable("inStorage", docGroupPermissionsList);
        return ResponseService.okResponseForText("storagehome", ctx);
    }
}
