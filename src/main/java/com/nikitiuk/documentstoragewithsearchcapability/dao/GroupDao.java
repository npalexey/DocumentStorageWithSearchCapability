package com.nikitiuk.documentstoragewithsearchcapability.dao;

import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GroupDao {

    private static final Logger logger =  LoggerFactory.getLogger(GroupDao.class);
    public static List<GroupBean> groupList = new ArrayList<>();
    static {
        groupList.add(new GroupBean("ADMIN", "rwd"));
        groupList.add(new GroupBean("USERS", "rw"));
        groupList.add(new GroupBean("GUESTS", "r"));
        Set<UserBean> userBeans0 = new HashSet<>(Collections.singletonList(UserDao.userList.get(0)));
        groupList.get(0).setUsers(userBeans0);
        Set<UserBean> userBeans1 = new HashSet<>(Arrays.asList(UserDao.userList.get(0), UserDao.userList.get(1)));
        groupList.get(1).setUsers(userBeans1);
        Set<UserBean> userBeans2 = new HashSet<>(Arrays.asList(UserDao.userList.get(0), UserDao.userList.get(1),
                UserDao.userList.get(2)));
        groupList.get(2).setUsers(userBeans2);
    }

    public static void populateTableWithGroups() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            for (GroupBean groupBean : groupList) {
                Transaction transaction = null;
                try {
                    // start a transaction
                    transaction = session.beginTransaction();
                    // save the group object
                    session.saveOrUpdate(groupBean);
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

    public static void saveGroup(GroupBean group) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<GroupBean> beanList = session.createQuery("FROM GroupBean", GroupBean.class).list();
            if(!beanList.isEmpty()){
                for (GroupBean groupBean : beanList) {
                    if(!groupBean.equals(group)){
                        // start a transaction
                        transaction = session.beginTransaction();
                        // save the group object
                        session.save(group);
                        //session.persist(group);
                        // commit transaction
                        transaction.commit();
                    }
                }
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at GroupDao save: ", e);
        }
    }

    public static List<GroupBean> getGroups() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM GroupBean", GroupBean.class).list();
        }
    }

    public static void deleteGroup(GroupBean groupBean){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.createQuery("DELETE FROM GroupBean WHERE name = '"
                    + groupBean.getName() + "' AND permissions = '" + groupBean.getPermissions() + "' ");
        }
    }
}