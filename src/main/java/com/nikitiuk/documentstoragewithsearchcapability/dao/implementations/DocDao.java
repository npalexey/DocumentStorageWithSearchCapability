package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.*;
import com.nikitiuk.documentstoragewithsearchcapability.security.UserPrincipal;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.AlreadyExistsException;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DocDao extends GenericHibernateDao<DocBean> {

    private static final Logger logger = LoggerFactory.getLogger(DocDao.class);

    public DocDao() {
        super(DocBean.class);
    }

    public DocBean saveDocument(DocBean document) throws Exception {
        if (exists(document)) {
            throw new AlreadyExistsException("Such Document Already Exists");
        }
        return save(document);
    }

    public DocBean updateDocument(DocBean document) throws Exception {
        InspectorService.checkIfDocumentIsNull(document);
        return save(document);
    }

    @Override
    public DocBean getById(Long documentId){
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocBean docBean = session.get(DocBean.class, documentId);
            if(docBean != null) {
                initializeConnections(docBean);
            }
            transaction.commit();
            session.close();
            return docBean;
        } catch (Exception e) {
            logger.error("Error at DocDao getDocById: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<DocBean> getAllDocuments() throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            List<DocBean> docBeanList = session.createQuery("FROM DocBean", DocBean.class).list();
            if(CollectionUtils.isNotEmpty(docBeanList)) {
                initializeConnectionsForList(docBeanList);
            }
            transaction.commit();
            return docBeanList;
        } catch (Exception e) {
            logger.error("Error at DocDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<DocBean> getDocumentsForUser(UserPrincipal userPrincipal) throws Exception {
        List<DocBean> docBeanList = new ArrayList<>();
        if (CollectionUtils.isEmpty(userPrincipal.getGroups())) {
            return docBeanList;
        }
        List<Long> groupIds = userPrincipal.getGroupsIds();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("SELECT DISTINCT doc FROM DocBean doc INNER JOIN DocGroupPermissions permissions ON doc.id = permissions.document.id " +
                    "WHERE permissions.group.id IN (:ids)", DocBean.class).setParameterList("ids", groupIds).list();
            if(CollectionUtils.isNotEmpty(docBeanList)){
                initializeConnectionsForList(docBeanList);
            }
            transaction.commit();
            return docBeanList;
        } catch (Exception e) {
            logger.error("Error at DocDao getDocumentsForUser: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<DocBean> getDocumentsForUserInFolder(UserPrincipal userPrincipal, FolderBean folderBean) throws Exception {
        List<DocBean> docBeanList = new ArrayList<>();
        if(folderBean == null || folderBean.getPath() == null
                || CollectionUtils.isEmpty(userPrincipal.getGroups())) {
            return docBeanList;
        }
        List<Long> groupIds = userPrincipal.getGroupsIds();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("SELECT DISTINCT doc FROM DocBean doc INNER JOIN DocGroupPermissions permissions ON doc.id = permissions.document.id " +
                    "WHERE permissions.group.id IN (:ids) AND doc.path LIKE '" + folderBean.getPath() + "%'", DocBean.class).setParameterList("ids", groupIds).list();
            if(CollectionUtils.isNotEmpty(docBeanList)){
                initializeConnectionsForList(docBeanList);
            }
            transaction.commit();
            return docBeanList;
        } catch (Exception e) {
            logger.error("Error at DocDao getDocumentsForUser: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }

    }

    public void deleteDocument(Long documentId) throws Exception {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createSQLQuery("DELETE FROM Document_group_permissions WHERE document_id = (:id)")
                    .setParameter("id", documentId).executeUpdate();
            session.createQuery("DELETE FROM DocBean WHERE id = (:id)")
                    .setParameter("id", documentId).executeUpdate();
            /*session.createSQLQuery("DELETE FROM Document_group_permissions WHERE document_id IN (SELECT * FROM (SELECT id FROM Documents WHERE document_path = '"
                    + docBean.getPath() + "') AS X)").executeUpdate();
            session.createQuery("DELETE FROM DocBean WHERE path = '" + docBean.getPath() + "'").executeUpdate();*/
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocDao delete: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.saveOrUpdate(document);
            transaction.commit();
            return document;
        } catch (Exception e) {
            logger.error("Error at DocDao save.", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private void initializeConnectionsForList(List<DocBean> docBeanList) {
        for(DocBean docBean : docBeanList) {
            initializeConnections(docBean);
        }
    }

    private void initializeConnections(DocBean document) {
        for (DocGroupPermissions docGroupPermissions : document.getDocumentsPermissions()) {
            Hibernate.initialize(docGroupPermissions);
        }
    }
}