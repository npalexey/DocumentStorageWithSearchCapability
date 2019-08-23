package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserDao extends GenericHibernateDao<UserBean> {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private static List<UserBean> userList = new ArrayList<>();

    private static void getUserListForPopulate() {
        userList.add(new UserBean("Admin", "adminpswrd"));
        userList.add(new UserBean("Employee", "employeepswrd"));
        userList.add(new UserBean("Guest", "guestpswrd"));
        Set<GroupBean> groupBeans0 = new HashSet<>(Collections.singletonList(GroupDao.groupList.get(2)));
        userList.get(2).setGroups(groupBeans0);
        Set<GroupBean> groupBeans1 = new HashSet<>(Arrays.asList(GroupDao.groupList.get(2), GroupDao.groupList.get(1)));
        userList.get(1).setGroups(groupBeans1);
        Set<GroupBean> groupBeans2 = new HashSet<>(Arrays.asList(GroupDao.groupList.get(2), GroupDao.groupList.get(1),
                GroupDao.groupList.get(0)));
        userList.get(0).setGroups(groupBeans2);
    }

    public UserDao() {
        super(UserBean.class);
    }

    public static void populateTableWithUsers() {
        getUserListForPopulate();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (UserBean userBean : userList) {
                Transaction transaction = null;
                try {
                    // start a transaction
                    transaction = session.beginTransaction();
                    // save the user object
                    session.saveOrUpdate(userBean);
                    Hibernate.initialize(userBean.getGroups());
                    // commit transaction
                    transaction.commit();
                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    logger.error("Error at UserDao populate: ", e);
                }
            }
        }
    }

    public List<UserBean> getUsers() {
        Transaction transaction = null;
        List<UserBean> userBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            userBeanList = session.createQuery("FROM UserBean", UserBean.class).list();
            for (UserBean userBean : userBeanList) {
                Hibernate.initialize(userBean.getGroups());
            }
            transaction.commit();
            return userBeanList;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at UserDao getAll: ", e);
            return userBeanList;
        }
    }

    public UserBean getUserByName(String userName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            UserBean userBean = session.createQuery("FROM UserBean WHERE name = '"
                    + userName + "'", UserBean.class).uniqueResult();
            Hibernate.initialize(userBean.getGroups());
            transaction.commit();
            session.close();
            return userBean;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void saveUser(UserBean userBean) {
        try {
            if (exists(userBean)) {
                throw new Exception("Such User Already Exists");
            }
            save(userBean);
        } catch (Exception e) {
            logger.error("Error at UserDao saveUser: ", e);
        }
    }

    public void updateUser(UserBean userBean) {
        try {
            if (exists(userBean)) {
                UserBean updatedUser = getUserByName(userBean.getName());
                updatedUser.setGroups(userBean.getGroups());
                updatedUser.setPassword(userBean.getPassword());
                Hibernate.initialize(updatedUser.getGroups());
                save(updatedUser);
            }
        } catch (Exception e) {
            logger.error("Error at UserDao updateAndSaveUser: ", e);
        }
    }

    public void deleteUserByName(String userName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM UserBean WHERE name = '"
                    + userName + "'").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at UserDao deleteUserByName: ", e);
        }
    }

    public boolean exists(UserBean user) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<UserBean> beanList = session.createQuery("FROM UserBean", UserBean.class).list();
        if (!beanList.isEmpty()) {
            for (UserBean userBean : beanList) {
                if (userBean.equals(user)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void save(UserBean user) throws Exception {
        Transaction transaction = null;
        try {
            user.setGroups(checkGroupsAndReturnMatched(user));
            Session session = HibernateUtil.getSessionFactory().openSession();
            // start a transaction
            transaction = session.beginTransaction();
            // save the user object
            session.saveOrUpdate(user);
            Hibernate.initialize(user.getGroups());
            // commit transaction
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private Set<GroupBean> checkGroupsAndReturnMatched(UserBean user) throws Exception {
        Set<GroupBean> checkedGroups = new HashSet<>();
        GroupDao groupDao = new GroupDao();
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
