package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.services.LocalStorageService;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class DocGroupPermissionsDao {

    private static final Logger logger = LoggerFactory.getLogger(DocGroupPermissionsDao.class);
    private static List<DocGroupPermissions> docGroupPermissionsList = new ArrayList<>()/*(Arrays.asList(new UserBean("Admin"),
            new UserBean("Employee"), new UserBean("Guest")))*/;

    static {
        DocDao docDao = new DocDao();
        List<DocBean> docBeanList = docDao.getDocuments();
        if(!docBeanList.isEmpty()) {
            GroupDao groupDao = new GroupDao();
            List<GroupBean> groupBeanList = groupDao.getGroups();
            for(DocBean docBean : docBeanList){
                DocGroupPermissions docGroupPermissionsAdmin = new DocGroupPermissions(groupBeanList.get(0), docBean);
                DocGroupPermissions docGroupPermissionsUser = new DocGroupPermissions(groupBeanList.get(1), docBean);
                DocGroupPermissions docGroupPermissionsGuest = new DocGroupPermissions(groupBeanList.get(2), docBean);
                docGroupPermissionsAdmin.setPermissions(Permissions.WRITE);
                docGroupPermissionsUser.setPermissions(Permissions.READ);
                docGroupPermissionsGuest.setPermissions(Permissions.READ);
                docGroupPermissionsList.add(docGroupPermissionsAdmin);
                docGroupPermissionsList.add(docGroupPermissionsUser);
                docGroupPermissionsList.add(docGroupPermissionsGuest);
            }
            //docGroupPermissionsList.add(new DocGroupPermissions(groupBeanList.get(0), docBeanList.get(0)));
            //docGroupPermissionsList.get(0).setPermissions(Permissions.READ);
        }
        /*userList.add(new UserBean("Admin", "adminpswrd"));
        userList.add(new UserBean("Employee", "employeepswrd"));
        userList.add(new UserBean("Guest", "guestpswrd"));
        Set<GroupBean> groupBeans0 = new HashSet<>(Collections.singletonList(GroupDao.groupList.get(2)));
        userList.get(2).setGroups(groupBeans0);
        Set<GroupBean> groupBeans1 = new HashSet<>(Arrays.asList(GroupDao.groupList.get(2), GroupDao.groupList.get(1)));
        userList.get(1).setGroups(groupBeans1);
        Set<GroupBean> groupBeans2 = new HashSet<>(Arrays.asList(GroupDao.groupList.get(2), GroupDao.groupList.get(1),
                GroupDao.groupList.get(0)));
        userList.get(0).setGroups(groupBeans2);*/
    }

    public static void populateTableWithDocGroupPermissions() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (DocGroupPermissions docGroupPermissions : docGroupPermissionsList) {
                Transaction transaction = null;
                try {
                    // start a transaction
                    transaction = session.beginTransaction();
                    // save the group object
                    session.saveOrUpdate(docGroupPermissions);
                    //Hibernate.initialize(groupBean.getUsers());
                    // commit transaction
                    transaction.commit();
                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    logger.error("Error at DocGroupPermissionsDao populate: ", e);
                }
            }
        }
    }

    public List<DocGroupPermissions> getGroupPermissionsForDocuments(GroupBean groupBean) {
        Transaction transaction = null;
        List<DocGroupPermissions> docGroupPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docGroupPermissionsList = session.createQuery("FROM DocumentGroupPermissions WHERE group = " + groupBean.getId(), DocGroupPermissions.class).list();
            transaction.commit();
            return docGroupPermissionsList;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at DocumentGroupPermissionsDao getGroupPermissionsForDocuments: ", e);
            return docGroupPermissionsList;
        }
    }
}
