package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class RestUserService {

    private UserDao userDao = new UserDao();

    public List<UserBean> getUsers() throws Exception {
        return userDao.getUsers();
    }

    public UserBean getSingleUser(Long userId) throws Exception {
        InspectorService.checkIfIdIsNull(userId);
        return userDao.getById(userId);
    }

    public UserBean createUser(UserBean userBean) throws Exception {
        checkOnCreateOrUpdateForNulls(userBean);
        return userDao.saveUser(userBean);
    }

    public UserBean updateUser(UserBean userBean) throws Exception {
        checkOnCreateOrUpdateForNulls(userBean);
        return userDao.updateUser(userBean);
    }

    public void deleteUser(Long userId) throws Exception {
        InspectorService.checkIfIdIsNull(userId);
        userDao.deleteById(userId);
    }

    private void checkOnCreateOrUpdateForNulls(UserBean userBean) throws NoValidDataFromSourceException {
        if (userBean == null || StringUtils.isBlank(userBean.getName())) {
            throw new NoValidDataFromSourceException("No valid data was passed.");
        }
    }
}
