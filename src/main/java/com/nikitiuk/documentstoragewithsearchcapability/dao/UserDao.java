package com.nikitiuk.documentstoragewithsearchcapability.dao;

import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final Logger logger =  LoggerFactory.getLogger(UserDao.class);
    public static List<UserBean> userList = new ArrayList<>();
    static {
        userList.add(new UserBean("Admin", "ADMIN"));
        userList.add(new UserBean("Employee", "USERS"));
        userList.add(new UserBean("Guest", "GUESTS"));
    }

    public static void populateTableWithUsers(Session session) {
        for (UserBean userBean : userList) {
            Transaction transaction = null;
            try {
                // start a transaction
                transaction = session.beginTransaction();
                // save the user object
                session.save(userBean);
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

    public static void saveUser(UserBean user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<UserBean> beanList = session.createQuery("FROM UserBean", UserBean.class).list();
            if(!beanList.isEmpty()){
                for (UserBean userBean : beanList) {
                    if(!userBean.equals(user)){
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

    public static void deleteUser(UserBean userBean){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.createQuery("DELETE FROM UserBean WHERE name = '"
                    + userBean.getName() + "' AND group = '" + userBean.getGroup() + "' ");
        }
    }
}
