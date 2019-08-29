package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DocGroupPermissionsDao extends GenericHibernateDao<DocGroupPermissions> {

    private static final Logger logger = LoggerFactory.getLogger(DocGroupPermissionsDao.class);
    private static List<DocGroupPermissions> docGroupPermissionsList = new ArrayList<>();

    public DocGroupPermissionsDao() {
        super(DocGroupPermissions.class);
    }

    private static void getDocGroupPermissionsListForPopulate() {
        DocDao docDao = new DocDao();
        List<DocBean> docBeanList = docDao.getAllDocumentsForPopulate();
        if (!docBeanList.isEmpty()) {
            GroupDao groupDao = new GroupDao();
            List<GroupBean> groupBeanList = groupDao.getGroups();
            for (DocBean docBean : docBeanList) {
                DocGroupPermissions docGroupPermissionsAdmin = new DocGroupPermissions(groupBeanList.get(0), docBean);
                DocGroupPermissions docGroupPermissionsUser = new DocGroupPermissions(groupBeanList.get(1), docBean);
                DocGroupPermissions docGroupPermissionsGuest = new DocGroupPermissions(groupBeanList.get(2), docBean);
                docGroupPermissionsAdmin.setPermissions(Permissions.WRITE);
                docGroupPermissionsUser.setPermissions(Permissions.READ);
                docGroupPermissionsGuest.setPermissions(Permissions.READ);
                docGroupPermissionsList.add(docGroupPermissionsAdmin);
                docGroupPermissionsList.add(docGroupPermissionsUser);
                docGroupPermissionsList.add(docGroupPermissionsGuest);
            }
        }
    }

    public static void populateTableWithDocGroupPermissions() {
        getDocGroupPermissionsListForPopulate();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (DocGroupPermissions docGroupPermissions : docGroupPermissionsList) {
                Transaction transaction = null;
                try {
                    // start a transaction
                    transaction = session.beginTransaction();
                    // save the group object
                    session.saveOrUpdate(docGroupPermissions);
                    //Hibernate.initialize(groupBean.getUsers());
                    // commit transaction
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("Error at DocGroupPermissionsDao populate: ", e);
                    if (transaction != null) {
                        transaction.rollback();
                    }
                }
            }
        }
    }

    public List<DocGroupPermissions> getGroupPermissionsForDocuments(GroupBean groupBean) {
        Transaction transaction = null;
        List<DocGroupPermissions> docGroupPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docGroupPermissionsList = session.createQuery("FROM DocGroupPermissions WHERE group = " + groupBean.getId(), DocGroupPermissions.class).list();
            initializeList(docGroupPermissionsList);
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao getGroupPermissionsForDocuments: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docGroupPermissionsList;
    }

    public List<DocGroupPermissions> getAllDocGroupPermissions() {
        Transaction transaction = null;
        List<DocGroupPermissions> docGroupPermissionsList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            docGroupPermissionsList = session.createQuery("FROM DocGroupPermissions", DocGroupPermissions.class).list();
            initializeList(docGroupPermissionsList);
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao getAllDocGroupPermissions: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return docGroupPermissionsList;
    }

    public void deleteAllPermissionsForGroup(GroupBean groupBean) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM DocGroupPermissions WHERE group = "
                    + groupBean.getId()).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deleteAllPermissionsForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public void deletePermissionsForDocumentForGroup(DocBean docBean, GroupBean groupBean) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM DocGroupPermissions WHERE group = "
                    + groupBean.getId() + " AND document = " + docBean.getId()).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deleteAllPermissionsForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public void deleteAllPermissionsForDocumentExceptAdmin(DocBean docBean) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM DocGroupPermissions WHERE group != 1 AND document = "
                    + docBean.getId()).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deleteAllPermissionsForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public void deleteAllPermissionsForDocument(DocBean docBean) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createQuery("DELETE FROM DocGroupPermissions WHERE document = "
                    + docBean.getId()).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao deleteAllPermissionsForGroup: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public void setWriteForDocumentForGroup(DocBean docBean, GroupBean groupBean) {
        setPermission(docBean.getId(), groupBean.getId(), Permissions.WRITE);
    }

    public void setWriteForDocumentForGroup(long docId, long groupId) {
        setPermission(docId, groupId, Permissions.WRITE);
    }

    public void setReadForDocumentForGroup(DocBean docBean, GroupBean groupBean) {
        setPermission(docBean.getId(), groupBean.getId(), Permissions.READ);
    }

    public void setReadForDocumentForGroup(long docId, long groupId) {
        setPermission(docId, groupId, Permissions.READ);
    }

    private void setPermission(Long docId, Long groupId, Permissions permission) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            DocGroupPermissions docGroupPermissions = session.createQuery("FROM DocGroupPermissions WHERE document = "
                    + docId + " AND group = " + groupId, DocGroupPermissions.class).uniqueResult();
            if (docGroupPermissions != null) {
                docGroupPermissions.setPermissions(permission);
                session.merge(docGroupPermissions);
            } else {
                session.saveOrUpdate(createNewPermissions(docId, groupId, permission));
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at DocGroupPermissionsDao set " + permission + " ForDocumentForGroup, where " +
                    "DocId = " + docId + " and GroupId = " + groupId, e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    private DocGroupPermissions createNewPermissions(Long docId, Long groupId, Permissions permission) {
        DocDao docDao = new DocDao();
        GroupDao groupDao = new GroupDao();
        DocBean docBean = docDao.getById(docId);
        GroupBean groupBean = groupDao.getById(groupId);
        DocGroupPermissions newDocGroupPermissions = new DocGroupPermissions(groupBean, docBean);
        newDocGroupPermissions.setPermissions(permission);
        groupBean.addDocument(docBean, permission);
        return newDocGroupPermissions;
    }

    private void initializeList(List<DocGroupPermissions> docGroupPermissionsList) {
        for (DocGroupPermissions docGroupPermissions : docGroupPermissionsList) {
            initializeConnections(docGroupPermissions);
        }
    }

    private void initializeConnections(DocGroupPermissions docGroupPermissions) {
        Hibernate.initialize(docGroupPermissions.getDocument());
        Hibernate.initialize(docGroupPermissions.getGroup());
    }
}