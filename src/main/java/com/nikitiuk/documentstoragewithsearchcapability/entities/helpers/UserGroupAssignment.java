package com.nikitiuk.documentstoragewithsearchcapability.entities.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.GroupDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.UserDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;

import java.util.HashSet;
import java.util.Set;

public class UserGroupAssignment {

    private GroupDao groupDao = new GroupDao();
    private UserDao userDao = new UserDao();

    public Set<GroupBean> checkGroupsAndReturnMatched(UserBean user) throws Exception {
        Set<GroupBean> checkedGroups = new HashSet<>();
        if (user == null) {
            throw new Exception("No UserBean was passed to check");
        }
        for (GroupBean groupBean : groupDao.getGroups()) {
            if (user.getGroups().contains(groupBean)) {          //checks equality with hashCode()
                checkedGroups.add(groupBean);
            }
        }
        return checkedGroups;
    }
}
