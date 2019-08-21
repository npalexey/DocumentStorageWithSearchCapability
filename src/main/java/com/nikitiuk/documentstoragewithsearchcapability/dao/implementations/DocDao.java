package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DocDao extends GenericHibernateDao<DocBean> {

    private static final Logger logger = LoggerFactory.getLogger(DocDao.class);

    public DocDao() {
        super(DocBean.class);
    }

    public static void populateTableWithDocs(List<DocBean> docBeanList) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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

    public void saveDocument(DocBean document) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<DocBean> beanList = session.createQuery("FROM DocBean", DocBean.class).list();
            if (!beanList.isEmpty()) {
                for (DocBean docBean : beanList) {
                    if (!docBean.equals(document)) {
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

    public List<DocBean> getDocuments() {
        Transaction transaction = null;
        List<DocBean> docBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("FROM DocBean", DocBean.class).list();
            transaction.commit();
            return docBeanList;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error at DocDao getAll: ", e);
            return docBeanList;
        }

    }

    public void deleteDocument(DocBean docBean) {
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
