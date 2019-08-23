package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GroupDao extends GenericHibernateDao<GroupBean> {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);
    private DocGroupPermissionsDao docGroupPermissionsDao = new DocGroupPermissionsDao();
    private DocDao docDao = new DocDao();
    static List<GroupBean> groupList = new ArrayList<>(Arrays.asList(new GroupBean("ADMINS"),
            new GroupBean("USERS"), new GroupBean("GUESTS")));

    public GroupDao() {
        super(GroupBean.class);
    }

    public static void populateTableWithGroups() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (GroupBean groupBean : groupList) {
                Transaction transaction = null;
                try {
                    // start a transaction
                    transaction = session.beginTransaction();
                    // save the group object
                    session.saveOrUpdate(groupBean);
                    //Hibernate.initialize(groupBean.getUsers());
                    // commit transaction
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("Error at GroupDao populate: ", e);
                    if (transaction != null) {
                        transaction.rollback();
                    }
                }
            }
        }
    }

    public List<GroupBean> getGroups() {
        Transaction transaction = null;
        List<GroupBean> groupBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            groupBeanList = session.createQuery("FROM GroupBean", GroupBean.class).list();
            for (GroupBean groupBean : groupBeanList) {
                initializeConnections(groupBean);
                /*Hibernate.initialize(groupBean.getUsers());
                Hibernate.initialize(groupBean.getDocumentsPermissions());*/
                /*for (UserBean user : groupBean.getUsers()) {
                    Hibernate.initialize(user.getGroups());
                }*/
            }
            transaction.commit();
            return groupBeanList;
        } catch (Exception e) {
            logger.error("Error at GroupDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            return groupBeanList;
        }
    }

    public GroupBean getGroupByName(String groupName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            GroupBean groupBean = session.createQuery("FROM GroupBean WHERE name = '"
                    + groupName + "'", GroupBean.class).uniqueResult();
            initializeConnections(groupBean);
            /*Hibernate.initialize(groupBean.getUsers());
            Hibernate.initialize(groupBean.getDocumentsPermissions());*/
            transaction.commit();
            session.close();
            return groupBean;
        } catch (Exception e) {
            logger.error("Error at GroupDao getGroupByName: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public GroupBean getOneById(long id){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            GroupBean groupBean = session.get(GroupBean.class, id);
            initializeConnections(groupBean);
            /*Hibernate.initialize(groupBean.getUsers());
            Hibernate.initialize(groupBean.getDocumentsPermissions());*/
            transaction.commit();
            session.close();
            return groupBean;
        } catch (Exception e) {
            logger.error("Error at GroupDao getOneById: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void saveGroup(GroupBean groupBean) {
        try {
            if (exists(groupBean)) {
                throw new Exception("Such Group Already Exists");
            }
            save(groupBean);
        } catch (Exception e) {
            logger.error("Error at GroupDao saveGroup: ", e);
        }
    }

    public void updateGroup(GroupBean groupBean) {
        try {
            if (exists(groupBean)) {
                GroupBean updatedGroup = getGroupByName(groupBean.getName());
                updatedGroup.setUsers(groupBean.getUsers());
                updatedGroup.setDocumentsPermissions(groupBean.getDocumentsPermissions());
                //updatedGroup.setPermissions(groupBean.getPermissions());
                initializeConnections(updatedGroup);
                /*Hibernate.initialize(updatedGroup.getUsers());
                Hibernate.initialize(updatedGroup.getDocumentsPermissions());*/
                save(updatedGroup);
            }
        } catch (Exception e) {
            logger.error("Error at GroupDao updateAndSaveGroup: ", e);
        }
    }

    public void deleteGroupByName(String groupName) {
        Transaction transaction = null;
        GroupBean checkedGroup = getGroupByName(groupName);
        if(checkedGroup != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                transaction = session.beginTransaction();
                /*session.createQuery("DELETE FROM GroupBean WHERE name = '" +
                        groupName + "'").executeUpdate();*/
                session.delete(checkedGroup);
                transaction.commit();
            } catch (Exception e) {
                logger.error("Error at GroupDao deleteGroupByName: ", e);
                if (transaction != null) {
                    transaction.rollback();
                }
            }
        }
    }

    public boolean exists(GroupBean group) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<GroupBean> beanList = session.createQuery("FROM GroupBean", GroupBean.class).list();
        if (!beanList.isEmpty()) {
            for (GroupBean groupBean : beanList) {
                if (groupBean.equals(group)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void save(GroupBean group) throws Exception {
        Transaction transaction = null;
        try {
            group.setUsers(checkUsersAndReturnMatched(group));
            //group.setDocumentsPermissions(docGroupPermissionsDao.);
            Session session = HibernateUtil.getSessionFactory().openSession();
            // start a transaction
            transaction = session.beginTransaction();
            // save the group object
            session.saveOrUpdate(group);
            session.merge(group);
            // commit transaction
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private static Set<UserBean> checkUsersAndReturnMatched(GroupBean group) throws Exception {
        Set<UserBean> checkedUsers = new HashSet<>();
        UserDao userDao = new UserDao();
        if (group == null) {
            throw new Exception("No GroupBean was passed to check");
        }
        for (UserBean userBean : userDao.getUsers()) {
            if (group.getUsers().contains(userBean)) {          //checks equality with hashCode()
                checkedUsers.add(userBean);
            }
        }
        return checkedUsers;
    }
    //TODO
    /*private void setPermissions(GroupBean group) throws Exception {
        Set<DocGroupPermissions> docGroupPermissions = new HashSet<>();
        for (DocGroupPermissions permissions : group.getDocumentsPermissions()){
            DocGroupPermissions newPermissions = new DocGroupPermissions();
            DocBean docBean = docDao.getDocuments().
            newPermissions.setDocument();
            newPermissions.setGroup(group);
        }
    }*/

    private void initializeConnections(GroupBean group) {
        Hibernate.initialize(group.getUsers());
        for (DocGroupPermissions docGroupPermissions : group.getDocumentsPermissions()) {
            Hibernate.initialize(docGroupPermissions);
        }
    }
}