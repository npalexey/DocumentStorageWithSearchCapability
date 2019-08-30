package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocDao extends GenericHibernateDao<DocBean> {

    private static final Logger logger = LoggerFactory.getLogger(DocDao.class);
    private DocGroupPermissionsDao docGroupPermissionsDao = new DocGroupPermissionsDao();

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

    public DocBean saveDocument(DocBean document) throws Exception{
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
            //docBeanList = session.createQuery("FROM DocBean INNER JOIN DocGroupPermissions ON DocBean.id = DocGroupPermissions.document", DocBean.class).list();
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


    public DocBean getDocByName(String name) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocBean docBean = session.createQuery("FROM DocBean WHERE name = '"
                    + name + "'", DocBean.class).uniqueResult();
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
        Transaction transaction = null;
        List<DocBean> docBeanList = new ArrayList<>();
        Set<Long> ids = getIdsOfUserPermittedDocs(userBean);
        if(ids.isEmpty()){
            return docBeanList;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("FROM DocBean WHERE id IN :ids", DocBean.class).setParameter("ids", ids).list();
            //docBeanList = session.createQuery("FROM DocBean INNER JOIN DocGroupPermissions ON DocBean.id = DocGroupPermissions.document", DocBean.class).list();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docBeanList;
    }

    public Set<Long> getIdsOfUserPermittedDocs(UserBean userBean) {
        Set<Long> permittedDocsIds = new HashSet<>();
        Hibernate.initialize(userBean.getGroups());
        if(userBean.getGroups().isEmpty()) {
            return permittedDocsIds;
        }
        for(GroupBean groupBean : userBean.getGroups()){
            for(DocGroupPermissions docGroupPermissions : docGroupPermissionsDao.getPermissionsForDocumentsForGroup(groupBean.getId())) {
                if(docGroupPermissions != null && docGroupPermissions.getPermissions() != null) {
                    permittedDocsIds.add(docGroupPermissions.getDocument().getId());
                }
            }
        }
        return permittedDocsIds;
    }

    public void deleteDocument(DocBean docBean) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            /*session.createQuery("DELETE FROM DocGroupPermissions WHERE document IN (SELECT * FROM (SELECT id FROM DocBean WHERE " +
                    "name = '" + docBean.getName() + "' AND path = '" + docBean.getPath() + "') AS X").executeUpdate();
            session.createQuery("DELETE FROM DocBean WHERE name = '" + docBean.getName() + "' AND path = '" + docBean.getPath() + "' ").executeUpdate();*/
            DocBean checkedDoc = session.createQuery("FROM DocBean WHERE name = '"
                    + docBean.getName() + "' AND path = '" + docBean.getPath() + "' ", DocBean.class).uniqueResult();
            if(checkedDoc != null){
                docGroupPermissionsDao.deleteAllPermissionsForDocument(checkedDoc.getId());
                session.delete(checkedDoc);
            }
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
                "SELECT 1 FROM DocBean WHERE EXISTS (SELECT 1 FROM DocBean WHERE path = '"+ document.getPath() +"')")
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