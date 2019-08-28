package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
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
        try {
            if (exists(document)) {
                throw new Exception("Such Document Already Exists");
            }
            save(document);
        } catch (Exception e) {
            logger.error("Error at DocDao saveDocument: ", e);
        }
    }

    protected List<DocBean> getAllDocumentsForPopulate() {
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
            /*Hibernate.initialize(groupBean.getUsers());
            Hibernate.initialize(groupBean.getDocumentsPermissions());*/
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

    public List<DocBean> getDocuments(UserBean userBean) {
        Transaction transaction = null;
        List<DocBean> docBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Set<Long> ids = getIdsOfUserPermittedDocs(userBean);
            if(!ids.isEmpty()){
                docBeanList = session.createQuery("FROM DocBean WHERE id IN :ids", DocBean.class).setParameter("ids", ids).list();
            }
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

    public Set<Long> getIdsOfUserPermittedDocs(UserBean userBean) {
        Set<Long> permittedDocsIds = new HashSet<>();
        if(userBean != null){
            Hibernate.initialize(userBean.getGroups());
            for(GroupBean groupBean : userBean.getGroups()){
                for(DocGroupPermissions docGroupPermissions : docGroupPermissionsDao.getGroupPermissionsForDocuments(groupBean)) {
                    if(docGroupPermissions.getPermissions() != null) {
                        permittedDocsIds.add(docGroupPermissions.getDocument().getId());
                    }
                }
            }

        }
        return permittedDocsIds;
    }

    public void deleteDocument(DocBean docBean) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocBean checkedDoc = session.createQuery("FROM DocBean WHERE name = '"
                    + docBean.getName() + "' AND path = '" + docBean.getPath() + "' ", DocBean.class).uniqueResult();
            if(checkedDoc != null){
                docGroupPermissionsDao.deleteAllPermissionsForDocument(checkedDoc);
                session.delete(checkedDoc);
            }
            /*session.createQuery("DELETE FROM DocBean WHERE name = '"
                    + docBean.getName() + "' AND path = '" + docBean.getPath() + "' ").executeUpdate();*/
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
        /*List<DocBean> beanList = session.createQuery("FROM DocBean", DocBean.class).list();
        if (!beanList.isEmpty()) {
            for (DocBean docBean : beanList) {
                if (docBean.equals(document)) {
                    return true;
                }
            }
        }
        return false;*/
    }

    private void save(DocBean document) throws Exception {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            // start a transaction
            transaction = session.beginTransaction();
            // save the user object
            session.saveOrUpdate(document);
            // commit transaction
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}