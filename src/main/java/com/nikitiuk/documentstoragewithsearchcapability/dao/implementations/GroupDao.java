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

public class GroupDao extends GenericHibernateDao<GroupBean> {

    private static final Logger logger =  LoggerFactory.getLogger(GroupDao.class);
    public static List<GroupBean> groupList = new ArrayList<>(Arrays.asList(new GroupBean("ADMINS", "rwd"),
            new GroupBean("USERS", "rw"), new GroupBean("GUESTS", "r")));

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
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
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

    public static void updateGroup(GroupBean group) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<GroupBean> beanList = session.createQuery("FROM GroupBean", GroupBean.class).list();
            if(!beanList.isEmpty()){
                for (GroupBean groupBean : beanList) {
                    if(groupBean.equals(group)){
                        // start a transaction
                        transaction = session.beginTransaction();
                        // save the group object
                        session.saveOrUpdate(group);
                        //session.persist(group);
                        // commit transaction
                        transaction.commit();
                        return;
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
        Transaction transaction = null;
        List<GroupBean> groupBeanList = new ArrayList<>();
        List<GroupBean> mergedList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            groupBeanList = session.createQuery("FROM GroupBean", GroupBean.class).list();
            for (GroupBean groupBean : groupBeanList) {
                Hibernate.initialize(groupBean.getUsers());
                for (UserBean user : groupBean.getUsers()) {
                    Hibernate.initialize(user.getGroups());
                }
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

    public static void deleteGroup(GroupBean groupBean){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM GroupBean WHERE name = '"
                    + groupBean.getName() + "' AND permissions = '" + groupBean.getPermissions() + "' ");
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at GroupDao delete: ", e);
        }
    }
}