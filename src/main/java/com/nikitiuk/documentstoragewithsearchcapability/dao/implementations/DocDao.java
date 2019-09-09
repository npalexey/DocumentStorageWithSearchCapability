package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.*;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.AlreadyExistsException;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import javassist.NotFoundException;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.acl.Group;
import java.util.*;

public class DocDao extends GenericHibernateDao<DocBean> {

    private static final Logger logger = LoggerFactory.getLogger(DocDao.class);

    public DocDao() {
        super(DocBean.class);
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

    public DocBean updateDocument(DocBean document) throws Exception {
        try {
            DocBean updatedDocument = getById(document.getId());
            if (updatedDocument == null) {
                throw new NotFoundException("Document not found.");
            }
            /*updatedDocument.setDocumentsPermissions(document.getDocumentsPermissions());*/
            initializeConnections(updatedDocument);
            return save(updatedDocument);
        } catch (Exception e) {
            logger.error("Error at DocDao updateDocument: ", e);
            throw e;
        }
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

    public List<DocBean> getAllDocuments() {
        Transaction transaction = null;
        List<DocBean> docBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("FROM DocBean", DocBean.class).list();
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
        }
        return docBeanList;
    }

    public DocBean getDocByPath(String path) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocBean docBean = session.createQuery("FROM DocBean WHERE path = '"
                    + path + "'", DocBean.class).uniqueResult();
            if(docBean != null) {
                initializeConnections(docBean);
            }
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
        if (CollectionUtils.isEmpty(userBean.getGroups())) {
            return docBeanList;
        }
        List<Long> groupIds = userBean.getGroupsIds();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("SELECT DISTINCT doc FROM DocBean doc INNER JOIN DocGroupPermissions permissions ON doc.id = permissions.document.id " +
                    "WHERE permissions.group.id IN (:ids)", DocBean.class).setParameterList("ids", groupIds).list();
            if(CollectionUtils.isNotEmpty(docBeanList)){
                initializeConnectionsForList(docBeanList);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocDao getDocumentsForUser: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docBeanList;
    }

    public List<DocBean> getDocumentsForUserInFolder(UserBean userBean, FolderBean folderBean) {
        List<DocBean> docBeanList = new ArrayList<>();
        Hibernate.initialize(userBean.getGroups());
        if(folderBean == null || folderBean.getPath() == null
                || CollectionUtils.isEmpty(userBean.getGroups())) {
            return docBeanList;
        }
        List<Long> groupIds = userBean.getGroupsIds();
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docBeanList = session.createQuery("SELECT DISTINCT doc FROM DocBean doc INNER JOIN DocGroupPermissions permissions ON doc.id = permissions.document.id " +
                    "WHERE permissions.group.id IN (:ids) AND doc.path LIKE '" + folderBean.getPath() + "%'", DocBean.class).setParameterList("ids", groupIds).list();
            if(CollectionUtils.isNotEmpty(docBeanList)){
                initializeConnectionsForList(docBeanList);
            }
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            /*document.setDocumentsPermissions(getPermissionsOnlyForExistingGroups(document));*/
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

    /*private Set<DocGroupPermissions> getPermissionsOnlyForExistingGroups(DocBean document) throws Exception {
        if (document == null) {
            throw new Exception("No DocBean was passed to check.");
        }
        Set<DocGroupPermissions> docGroupPermissionsToSet = new HashSet<>();
        Set<DocGroupPermissions> docGroupPermissionsToCheck = document.getDocumentsPermissions();
        if (CollectionUtils.isEmpty(docGroupPermissionsToCheck)) {
            return docGroupPermissionsToSet;
        }
        Map<String, Permissions> groupNamesAndPermissions = new HashMap<>();
        Map<Long, Permissions> groupIdsAndPermissions = new HashMap<>();
        for (DocGroupPermissions docGroupPermissions : docGroupPermissionsToCheck) {
            if (docGroupPermissions.getPermissions() != null) {
                if (docGroupPermissions.getGroup().getId() != null) {
                    groupIdsAndPermissions.put(docGroupPermissions.getGroup().getId(), docGroupPermissions.getPermissions());
                } else if (docGroupPermissions.getGroup().getName() != null) {
                    groupNamesAndPermissions.put(docGroupPermissions.getGroup().getName(), docGroupPermissions.getPermissions());
                }
            }
        }
        if (groupNamesAndPermissions.isEmpty() && groupIdsAndPermissions.isEmpty()) {
            return docGroupPermissionsToSet;
        }
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            if (!groupNamesAndPermissions.isEmpty()) {
                Set<GroupBean> checkedGroupsWithNames = new HashSet<>(session.createQuery("FROM GroupBean WHERE name IN (:groupNames)", GroupBean.class)
                        .setParameterList("groupNames", groupNamesAndPermissions.keySet()).list());
                for (GroupBean groupBean : checkedGroupsWithNames) {
                    docGroupPermissionsToSet.add(new DocGroupPermissions(groupBean, document, groupNamesAndPermissions.get(groupBean.getName())));
                }
            }
            if (!groupIdsAndPermissions.isEmpty()) {
                Set<GroupBean> checkedGroupsWithIds = new HashSet<>(session.createQuery("FROM GroupBean WHERE id IN (:ids)", GroupBean.class)
                        .setParameterList("ids", groupIdsAndPermissions.keySet()).list());
                for (GroupBean groupBean : checkedGroupsWithIds) {
                    docGroupPermissionsToSet.add(new DocGroupPermissions(groupBean, document, groupIdsAndPermissions.get(groupBean.getId())));
                }
            }
            transaction.commit();
            return docGroupPermissionsToSet;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }*/

    /*private List<Long> getGroupsIdsOfUser(UserBean userBean) {
        List<Long> groupIds = new ArrayList<>();
        for (GroupBean groupBean : userBean.getGroups()) {
            groupIds.add(groupBean.getId());
        }
        return groupIds;
    }*/

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