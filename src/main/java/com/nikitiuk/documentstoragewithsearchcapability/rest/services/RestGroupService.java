package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;

public class RestGroupService {

    private GroupDao groupDao = new GroupDao();

    public Response getGroups() {
        List<GroupBean> groupBeanList;
        try {
            groupBeanList = groupDao.getGroups();
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing list of groups. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "Group");
        ctx.setVariable("inStorage", groupBeanList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response getGroupById(Long id) {
        GroupBean groupById;
        try {
            InspectorService.checkIfIdIsNull(id);
            groupById = groupDao.getById(id);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while getting group by id. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "Group");
        ctx.setVariable("entity", groupById);
        ctx.setVariable("action", "found");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response getGroupByName(String groupName) {
        GroupBean groupByName;
        try {
            InspectorService.checkIfStringDataIsBlank(groupName);
            groupByName = groupDao.getGroupByName(groupName);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while getting group by name. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "Group");
        ctx.setVariable("entity", groupByName);
        ctx.setVariable("action", "found");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response createGroup(GroupBean groupBean) {
        GroupBean createdGroup;
        try {
            checkOnCreateOrUpdateForNulls(groupBean);
            createdGroup = groupDao.saveGroup(groupBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while creating group. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "Group");
        ctx.setVariable("entity", createdGroup);
        ctx.setVariable("action", "created");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response updateGroup(GroupBean groupBean) {
        GroupBean updatedGroup;
        try {
            checkOnCreateOrUpdateForNulls(groupBean);
            updatedGroup = groupDao.updateGroup(groupBean);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while updating group. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityType", "Group");
        ctx.setVariable("entity", updatedGroup);
        ctx.setVariable("action", "updated");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response deleteGroupById(Long groupId) {
        try {
            InspectorService.checkIfIdIsNull(groupId);
            groupDao.deleteById(groupId);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting group. " + e.getMessage());
        }
        return ResponseService.okResponseSimple("Group deleted successfully");
    }

    public Response deleteGroupByName(String groupName) {
        try {
            InspectorService.checkIfStringDataIsBlank(groupName);
            groupDao.deleteGroupByName(groupName);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while deleting group. " + e.getMessage());
        }
        return ResponseService.okResponseSimple("Group deleted successfully");
    }

    private void checkOnCreateOrUpdateForNulls(GroupBean groupBean) throws NoValidDataFromSourceException {
        if (groupBean == null || StringUtils.isBlank(groupBean.getName())) {
            throw new NoValidDataFromSourceException("No valid data was passed.");
        }
    }
}
