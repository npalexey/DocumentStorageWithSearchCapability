package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.AlreadyExistsException;
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
                    transaction = session.beginTransaction();
                    session.saveOrUpdate(docBean);
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

    public DocBean saveDocument(DocBean document) throws Exception {
        try {
            if (exists(document)) {
                throw new AlreadyExistsException("Such Document Already Exists");
            }
            return save(document);
        } catch (Exception e) {
            logger.error("Error at DocDao saveDocument: ", e);
            throw e;
        }
    }

    protected List<DocBean> getAllDocuments() {
        Transaction transaction = null;
        List<DocBean> docBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("FROM DocBean", DocBean.class).list();
            transaction.commit();
            return docBeanList;
        } catch (Exception e) {
            logger.error("Error at DocDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docBeanList;
    }

    public DocBean getDocByPath(String path) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocBean docBean = session.createQuery("FROM DocBean WHERE path = '"
                    + path + "'", DocBean.class).uniqueResult();
            transaction.commit();
            session.close();
            return docBean;
        } catch (Exception e) {
            logger.error("Error at DocDao getDocByPath: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<DocBean> getDocumentsForUser(UserBean userBean) {
        List<DocBean> docBeanList = new ArrayList<>();
        Hibernate.initialize(userBean.getGroups());
        if (userBean.getGroups() == null || userBean.getGroups().isEmpty()) {
            return docBeanList;
        }
        List<Long> groupIds = new ArrayList<>();
        for (GroupBean groupBean : userBean.getGroups()) {
            groupIds.add(groupBean.getId());
        }
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("SELECT DISTINCT doc FROM DocBean doc INNER JOIN DocGroupPermissions permissions ON doc.id = permissions.document.id " +
                    "WHERE permissions.group.id IN (:ids)", DocBean.class).setParameterList("ids", groupIds).list();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocDao getDocumentsForUser: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docBeanList;
    }

    public void deleteDocument(DocBean docBean) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createSQLQuery("DELETE FROM Document_group_permissions WHERE document_id IN (SELECT * FROM (SELECT id FROM Documents WHERE document_path = '"
                    + docBean.getPath() + "') AS X)").executeUpdate();
            session.createQuery("DELETE FROM DocBean WHERE path = '" + docBean.getPath() + "'").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocDao delete: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public boolean exists(DocBean document) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        return session.createQuery(
                "SELECT 1 FROM DocBean WHERE EXISTS (SELECT 1 FROM DocBean WHERE path = '" + document.getPath() + "')")
                .uniqueResult() != null;
    }

    private DocBean save(DocBean document) throws Exception {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(document);
            transaction.commit();
            return document;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}