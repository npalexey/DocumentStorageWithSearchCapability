package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.FolderBean;
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

public class FolderDao extends GenericHibernateDao<FolderBean> {

    private static final Logger logger = LoggerFactory.getLogger(FolderDao.class);

    public FolderDao() {
        super(FolderBean.class);
    }

    public static void populateTableWithFolders(List<FolderBean> folderBeanList) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (FolderBean folderBean : folderBeanList) {
                Transaction transaction = null;
                try {
                    transaction = session.beginTransaction();
                    session.saveOrUpdate(folderBean);
                    transaction.commit();
                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    logger.error("Error at FolderDao populate: ", e);
                }
            }
        }
    }

    public FolderBean saveFolder(FolderBean folder) throws Exception {
        try {
            if (exists(folder)) {
                throw new AlreadyExistsException("Such Folder Already Exists");
            }
            return save(folder);
        } catch (Exception e) {
            logger.error("Error at FolderDao saveFolder: ", e);
            throw e;
        }
    }

    protected List<FolderBean> getAllFolders() {
        Transaction transaction = null;
        List<FolderBean> folderBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderBeanList = session.createQuery("FROM FolderBean", FolderBean.class).list();
            transaction.commit();
            return folderBeanList;
        } catch (Exception e) {
            logger.error("Error at FolderDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return folderBeanList;
    }

    public FolderBean getFolderByPath(String path) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            FolderBean folderBean = session.createQuery("FROM FolderBean WHERE path = '"
                    + path + "'", FolderBean.class).uniqueResult();
            transaction.commit();
            session.close();
            return folderBean;
        } catch (Exception e) {
            logger.error("Error at FolderDao getFolderByPath: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<FolderBean> getFoldersForUser(UserBean userBean) {
        List<FolderBean> folderBeanList = new ArrayList<>();
        Hibernate.initialize(userBean.getGroups());
        if (userBean.getGroups() == null || userBean.getGroups().isEmpty()) {
            return folderBeanList;
        }
        List<Long> groupIds = new ArrayList<>();
        for (GroupBean groupBean : userBean.getGroups()) {
            groupIds.add(groupBean.getId());
        }
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderBeanList = session.createQuery("SELECT DISTINCT folder FROM FolderBean folder INNER JOIN FolderGroupPermissions permissions ON folder.id = permissions.folder.id " +
                    "WHERE permissions.group.id IN (:ids)", FolderBean.class).setParameterList("ids", groupIds).list();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at FolderDao getFoldersForUser: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return folderBeanList;
    }

    public void deleteFolder(FolderBean folderBean) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createSQLQuery("DELETE FROM Folder_group_permissions WHERE folder_id IN (SELECT * FROM (SELECT id FROM Folders WHERE folder_path = '"
                    + folderBean.getPath() + "') AS X)").executeUpdate();
            session.createQuery("DELETE FROM FolderBean WHERE path = '" + folderBean.getPath() + "'").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Error at FolderDao delete: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    public boolean exists(FolderBean folder) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        return session.createQuery(
                "SELECT 1 FROM FolderBean WHERE EXISTS (SELECT 1 FROM FolderBean WHERE path = '" + folder.getPath() + "')")
                .uniqueResult() != null;
    }

    private FolderBean save(FolderBean folder) throws Exception {
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(folder);
            transaction.commit();
            return folder;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}