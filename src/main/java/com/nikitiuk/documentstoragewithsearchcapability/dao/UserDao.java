package com.nikitiuk.documentstoragewithsearchcapability.dao;

import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    public static List<UserBean> userList = new ArrayList<>(Arrays.asList(new UserBean("Admin"),
            new UserBean("Employee"), new UserBean("Guest")));

    /*static {
        userList.add(new UserBean("Admin"));
        userList.add(new UserBean("Employee"));
        userList.add(new UserBean("Guest"));
        Set<GroupBean> groupBeans = new HashSet<>(Arrays.asList(GroupDao.getGroups().get(1),
                GroupDao.getGroups().get(2), GroupDao.getGroups().get(3)));
        userList.get(1).setGroups(groupBeans);
        groupBeans.clear();
        groupBeans.add(GroupDao.getGroups().get(2));
        groupBeans.add(GroupDao.getGroups().get(3));
        userList.get(2).setGroups(groupBeans);
        groupBeans.clear();
        groupBeans.add(GroupDao.getGroups().get(3));
        userList.get(3).setGroups(groupBeans);
    }*/

    public static void populateTableWithUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            for (UserBean userBean : userList) {
                Transaction transaction = null;
                try {
                    // start a transaction
                    transaction = session.beginTransaction();
                    // save the user object
                    session.saveOrUpdate(userBean);
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

    public static void saveUser(UserBean user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<UserBean> beanList = session.createQuery("FROM UserBean", UserBean.class).list();
            if (!beanList.isEmpty()) {
                for (UserBean userBean : beanList) {
                    if (!userBean.equals(user)) {
                        // start a transaction
                        transaction = session.beginTransaction();
                        // save the user object
                        session.save(user);
                        // commit transaction
                        transaction.commit();
                    }
                }
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at UserDao save: ", e);
        }
    }

    public static List<UserBean> getUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM UserBean", UserBean.class).list();
        }
    }

    public static void deleteUser(UserBean userBean) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.createQuery("DELETE FROM UserBean WHERE name = '"
                    + userBean.getName());
        }
    }
}
