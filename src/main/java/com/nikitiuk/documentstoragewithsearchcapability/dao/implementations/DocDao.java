package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import java.util.List;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocDao extends GenericHibernateDao<DocBean> {

    private static final Logger logger =  LoggerFactory.getLogger(DocDao.class);

    public DocDao() {
        super(DocBean.class);
    }

    public static void populateTableWithDocs(List<DocBean> docBeanList) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<DocBean> beanList = session.createQuery("FROM DocBean", DocBean.class).list();
            for (DocBean docBean : docBeanList) {
                Transaction transaction = null;
                try {
                    // start a transaction
                    transaction = session.beginTransaction();
                    // save the document object
                    session.saveOrUpdate(docBean);
                    // commit transaction
                    transaction.commit();
                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    logger.error("Error at DocDao populate: ", e);
                }
            }
        }
    }

    public static void saveDocument(DocBean document) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<DocBean> beanList = session.createQuery("FROM DocBean", DocBean.class).list();
            if(!beanList.isEmpty()){
                for (DocBean docBean : beanList) {
                    if(!docBean.equals(document)){
                        // start a transaction
                        transaction = session.beginTransaction();
                        // save the document object
                        session.save(document);
                        // commit transaction
                        transaction.commit();
                    }
                }
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at DocDao save: ", e);
        }
    }

    public static List<DocBean> getDocuments() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM DocBean", DocBean.class).list();
        }
    }

    public static void deleteDocument(DocBean docBean){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM DocBean WHERE name = '"
                    + docBean.getName() + "' AND path = '" + docBean.getPath() + "' ").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at DocDao delete: ", e);
        }
    }
}