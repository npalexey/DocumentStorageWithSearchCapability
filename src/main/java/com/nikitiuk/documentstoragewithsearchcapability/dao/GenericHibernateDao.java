package com.nikitiuk.documentstoragewithsearchcapability.dao;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

public class GenericHibernateDao<T> {

    private static final Logger logger =  LoggerFactory.getLogger(GenericHibernateDao.class);

    private Class<T> clazz;
    private SessionFactory sessionFactory;

    public GenericHibernateDao(Class<T> clazz) {
        this.sessionFactory = HibernateUtil.getSessionFactory();
        this.clazz = clazz;
    }

    /*public void setClazz(Class<T> clazzToSet) {
        this.clazz = clazzToSet;
    }*/

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }


    private  <E> E executeFunction(Function<Session, E> function) {
        Transaction transaction = null;
        try (Session session = getCurrentSession()) {
            transaction = session.beginTransaction();
            return function.apply(session);
        } catch (Exception e) {
            logger.error("Error while executing function. ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            return null;
        }
    }

    public T getOneById(long id) {
        return executeFunction((session) -> session.get(clazz, id));
        //return (T) getCurrentSession().get(clazz, id);
    }

    public List getAll() {
        return executeFunction((session) -> session.createQuery("From " + clazz.getSimpleName(), clazz).list());
        //return getCurrentSession().createQuery("FROM " + clazz.getName()).list();
    }

    public void create(T entity) {
        executeFunction((session) -> {
            List<T> beanList = session.createQuery("FROM " + clazz.getSimpleName(), clazz).list();
            if (!beanList.isEmpty()) {
                for (T bean : beanList) {
                    if (!bean.equals(entity)) {
                        session.saveOrUpdate(entity);
                        session.getTransaction().commit();
                    }
                }
            }
            return entity;
        });
        /*getCurrentSession().saveOrUpdate(entity);
        return entity;*/
    }

    /*public T update(T entity) {
        return (T) getCurrentSession().merge(entity);
    }*/

    /*public void populateTableWithEntities(List<T> entityList){
        executeFunction((session) -> {
            for (T bean : entityList) {
                Transaction transaction = null;
                try {
                    // start a transaction
                    transaction = session.beginTransaction();
                    // save the group object
                    session.saveOrUpdate(bean);
                    // commit transaction
                    transaction.commit();
                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    logger.error("Error at " + clazz + " populate: ", e);
                }
            }
            return entityList;
        });
    }*/

    public void deleteById(long entityId) {
        executeFunction((session) -> {
            T entity = session.load(clazz, entityId);
            session.delete(entity);
            session.getTransaction().commit();
            return entityId;
        });
        /*T entity = findOne(entityId);
        delete(entity);*/
    }

    /*public void delete(T entity) {
        getCurrentSession().delete(entity);
    }*/
}
