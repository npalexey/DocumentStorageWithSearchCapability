package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import javassist.NotFoundException;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DocGroupPermissionsDao extends GenericHibernateDao<DocGroupPermissions> {

    private static final Logger logger = LoggerFactory.getLogger(DocGroupPermissionsDao.class);

    public DocGroupPermissionsDao() {
        super(DocGroupPermissions.class);
    }

    public List<DocGroupPermissions> getAllDocGroupPermissions() {
        Transaction transaction = null;
        List<DocGroupPermissions> docGroupPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docGroupPermissionsList = session.createQuery("FROM DocGroupPermissions", DocGroupPermissions.class).list();
            if(CollectionUtils.isNotEmpty(docGroupPermissionsList)){
                initializeConnectionsForList(docGroupPermissionsList);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao getAllDocGroupPermissions: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docGroupPermissionsList;
    }

    public List<DocGroupPermissions> getPermissionsForDocumentsForGroup(Long groupId) {
        Transaction transaction = null;
        List<DocGroupPermissions> docGroupPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docGroupPermissionsList = session.createQuery("FROM DocGroupPermissions WHERE group = " + groupId, DocGroupPermissions.class).list();
            if (CollectionUtils.isNotEmpty(docGroupPermissionsList)) {
                initializeConnectionsForList(docGroupPermissionsList);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao getPermissionsForDocumentsForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docGroupPermissionsList;
    }

    public List<DocGroupPermissions> getPermissionsForDocument(Long docId) {
        Transaction transaction = null;
        List<DocGroupPermissions> docPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docPermissionsList = session.createQuery("FROM DocGroupPermissions WHERE document = " + docId, DocGroupPermissions.class).list();
            if (CollectionUtils.isNotEmpty(docPermissionsList)) {
                initializeConnectionsForList(docPermissionsList);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao getPermissionsForDocument: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docPermissionsList;
    }

    public Integer deleteAllPermissionsForGroup(Long groupId) throws Exception {
        Transaction transaction = null;
        Integer quantityOfDeletedPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            quantityOfDeletedPermissions = session.createQuery("DELETE FROM DocGroupPermissions WHERE group = "
                    + groupId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deleteAllPermissionsForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Integer deletePermissionsForDocumentForGroup(Long docId, Long groupId) throws Exception {
        Transaction transaction = null;
        Integer quantityOfDeletedPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            quantityOfDeletedPermissions = session.createQuery("DELETE FROM DocGroupPermissions WHERE group = "
                    + groupId + " AND document = " + docId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deletePermissionsForDocumentForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Integer deleteAllPermissionsForDocumentExceptAdmin(Long docId) throws Exception {
        Transaction transaction = null;
        Integer quantityOfDeletedPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            quantityOfDeletedPermissions = session.createQuery("DELETE FROM DocGroupPermissions WHERE group != 1 AND document = "
                    + docId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deleteAllPermissionsForDocumentExceptAdmin: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Integer deleteAllPermissionsForDocument(Long docId) throws Exception {
        Transaction transaction = null;
        Integer quantityOfDeletedPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            quantityOfDeletedPermissions = session.createQuery("DELETE FROM DocGroupPermissions WHERE document = "
                    + docId).executeUpdate();
            transaction.commit();
            return quantityOfDeletedPermissions;
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deleteAllPermissionsForDocument: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public DocGroupPermissions setWriteForDocumentForGroup(DocBean docBean, GroupBean groupBean) throws Exception {
        return setPermission(docBean.getId(), groupBean.getId(), Permissions.WRITE);
    }

    public DocGroupPermissions setWriteForDocumentForGroup(long docId, long groupId) throws Exception {
        return setPermission(docId, groupId, Permissions.WRITE);
    }

    public DocGroupPermissions setReadForDocumentForGroup(DocBean docBean, GroupBean groupBean) throws Exception {
        return setPermission(docBean.getId(), groupBean.getId(), Permissions.READ);
    }

    public DocGroupPermissions setReadForDocumentForGroup(long docId, long groupId) throws Exception {
        return setPermission(docId, groupId, Permissions.READ);
    }

    private DocGroupPermissions setPermission(Long docId, Long groupId, Permissions permission) throws Exception {
        Transaction transaction = null;
        DocGroupPermissions setDocGroupPermissions;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocGroupPermissions docGroupPermissions = session.createQuery("FROM DocGroupPermissions WHERE document = "
                    + docId + " AND group = " + groupId, DocGroupPermissions.class).uniqueResult();
            if (docGroupPermissions != null) {
                docGroupPermissions.setPermissions(permission);
                session.merge(docGroupPermissions);
                setDocGroupPermissions = docGroupPermissions;
            } else {
                DocBean docBean = session.get(DocBean.class, docId);
                InspectorService.checkIfDocumentIsNull(docBean);
                GroupBean groupBean = session.get(GroupBean.class, groupId);
                InspectorService.checkIfGroupIsNull(groupBean);
                setDocGroupPermissions = createNewPermissions(docBean, groupBean, permission);
                session.saveOrUpdate(setDocGroupPermissions);
                session.merge(docBean);
            }
            transaction.commit();
            return setDocGroupPermissions;
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao set " + permission + " ForDocumentForGroup, where " +
                    "DocId = " + docId + " and GroupId = " + groupId + ": ", e);
            if (transaction != null && (transaction.getStatus() == TransactionStatus.FAILED_COMMIT || transaction.getStatus() == TransactionStatus.COMMITTING)) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private DocGroupPermissions createNewPermissions(DocBean docBean, GroupBean groupBean, Permissions permission) {
        DocGroupPermissions newDocGroupPermissions = new DocGroupPermissions(groupBean, docBean);
        newDocGroupPermissions.setPermissions(permission);
        docBean.addGroup(groupBean, permission);
        return newDocGroupPermissions;
    }

    private void initializeConnectionsForList(List<DocGroupPermissions> docGroupPermissionsList) {
        for (DocGroupPermissions docGroupPermissions : docGroupPermissionsList) {
            initializeConnections(docGroupPermissions);
        }
    }

    private void initializeConnections(DocGroupPermissions docGroupPermissions) {
        Hibernate.initialize(docGroupPermissions.getDocument());
        Hibernate.initialize(docGroupPermissions.getGroup());
    }
}