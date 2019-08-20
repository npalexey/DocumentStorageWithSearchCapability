package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.filters.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupDao extends GenericHibernateDao<GroupBean> {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);
    static List<GroupBean> groupList = new ArrayList<>(Arrays.asList(new GroupBean("ADMINS", Permissions.WRITE),
            new GroupBean("USERS", Permissions.WRITE), new GroupBean("GUESTS", Permissions.READ)));

    /*static {
        groupList.add(new GroupBean("ADMINS", "rwd"));
        groupList.add(new GroupBean("USERS", "rw"));
        groupList.add(new GroupBean("GUESTS", "r"));
        Set<UserBean> userBeans0 = new HashSet<>(Collections.singletonList(UserDao.userList.get(0)));
        groupList.get(0).setUsers(userBeans0);
        Set<UserBean> userBeans1 = new HashSet<>(Arrays.asList(UserDao.userList.get(0), UserDao.userList.get(1)));
        groupList.get(1).setUsers(userBeans1);
        Set<UserBean> userBeans2 = new HashSet<>(Arrays.asList(UserDao.userList.get(0), UserDao.userList.get(1),
                UserDao.userList.get(2)));
        groupList.get(2).setUsers(userBeans2);
    }*/

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
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    logger.error("Error at GroupDao populate: ", e);
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
                Hibernate.initialize(groupBean.getUsers());
                /*for (UserBean user : groupBean.getUsers()) {
                    Hibernate.initialize(user.getGroups());
                }*/
            }
            transaction.commit();
            return groupBeanList;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at GroupDao getAll: ", e);
            return groupBeanList;
        }
    }

    public GroupBean getGroupByName(String groupName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            GroupBean groupBean = session.createQuery("FROM GroupBean WHERE name = '"
                    + groupName + "'", GroupBean.class).uniqueResult();
            Hibernate.initialize(groupBean.getUsers());
            transaction.commit();
            session.close();
            return groupBean;
        } catch (Exception e) {
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
            Hibernate.initialize(groupBean.getUsers());
            transaction.commit();
            session.close();
            return groupBean;
        } catch (Exception e) {
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
                updatedGroup.setPermissions(groupBean.getPermissions());
                Hibernate.initialize(updatedGroup.getUsers());
                //Hibernate.initialize(updatedGroup.getPermissions());
                save(updatedGroup);
            }
        } catch (Exception e) {
            logger.error("Error at GroupDao updateAndSaveGroup: ", e);
        }
    }

    public void deleteGroupByName(String groupName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM GroupBean WHERE name = '"
                    + groupName + "'").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at GroupDao deleteGroupByName: ", e);
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

    public void save(GroupBean group) throws Exception {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            // start a transaction
            transaction = session.beginTransaction();
            // save the user object
            session.saveOrUpdate(group);
            // commit transaction
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}