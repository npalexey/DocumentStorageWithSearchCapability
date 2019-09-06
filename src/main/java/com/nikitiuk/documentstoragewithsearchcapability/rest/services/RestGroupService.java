package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class RestGroupService {

    private GroupDao groupDao = new GroupDao();

    public List<GroupBean> getGroups() throws Exception {
        return groupDao.getGroups();
    }

    public GroupBean getGroupById(Long id) throws Exception {
        InspectorService.checkIfIdIsNull(id);
        return groupDao.getById(id);
    }

    public GroupBean createGroup(GroupBean groupBean) throws Exception {
        checkOnCreateOrUpdateForNulls(groupBean);
        return groupDao.saveGroup(groupBean);
    }

    public GroupBean updateGroup(GroupBean groupBean) throws Exception {
        checkOnCreateOrUpdateForNulls(groupBean);
        return groupDao.updateGroup(groupBean);
    }

    public void deleteGroupById(Long groupId) throws Exception {
        InspectorService.checkIfIdIsNull(groupId);
        groupDao.deleteById(groupId);
    }

    private void checkOnCreateOrUpdateForNulls(GroupBean groupBean) throws NoValidDataFromSourceException {
        if (groupBean == null || StringUtils.isBlank(groupBean.getName())) {
            throw new NoValidDataFromSourceException("No valid data was passed.");
        }
    }
}