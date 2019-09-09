package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.FolderBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.FolderGroupPermissions;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.UserBean;
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

import java.util.*;

public class FolderDao extends GenericHibernateDao<FolderBean> {

    private static final Logger logger = LoggerFactory.getLogger(FolderDao.class);

    public FolderDao() {
        super(FolderBean.class);
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

    public FolderBean updateFolder(FolderBean folder) throws Exception {
        try {
            FolderBean updatedFolder = getById(folder.getId());
            if (updatedFolder == null) {
                throw new NotFoundException("Folder not found.");
            }
            /*updatedFolder.setDocumentsPermissions(folder.getDocumentsPermissions());*/
            initializeConnections(updatedFolder);
            return save(updatedFolder);
        } catch (Exception e) {
            logger.error("Error at FolderDao updateFolder: ", e);
            throw e;
        }
    }

    @Override
    public FolderBean getById(Long folderId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            FolderBean folderBean = session.get(FolderBean.class, folderId);
            if (folderBean != null) {
                initializeConnections(folderBean);
            }
            transaction.commit();
            session.close();
            return folderBean;
        } catch (Exception e) {
            logger.error("Error at FolderDao getFolderById: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<FolderBean> getAllFolders() {
        Transaction transaction = null;
        List<FolderBean> folderBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderBeanList = session.createQuery("FROM FolderBean", FolderBean.class).list();
            if (CollectionUtils.isNotEmpty(folderBeanList)) {
                initializeConnectionsForList(folderBeanList);
            }
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
            if (folderBean != null) {
                initializeConnections(folderBean);
            }
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
        List<Long> groupIds = userBean.getGroupsIds();/*new ArrayList<>();
        for (GroupBean groupBean : userBean.getGroups()) {
            groupIds.add(groupBean.getId());
        }*/
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            folderBeanList = session.createQuery("SELECT DISTINCT folder FROM FolderBean folder INNER JOIN FolderGroupPermissions permissions ON folder.id = permissions.folder.id " +
                    "WHERE permissions.group.id IN (:ids)", FolderBean.class).setParameterList("ids", groupIds).list();
            if (CollectionUtils.isNotEmpty(folderBeanList)) {
                initializeConnectionsForList(folderBeanList);
            }
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
        try (Session session = HibernateUtil.getSessionFactory().openSession();) {
            /*folder.setFoldersPermissions(getPermissionsOnlyForExistingGroups(folder));*/
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

    /*private Set<FolderGroupPermissions> getPermissionsOnlyForExistingGroups(FolderBean folder) throws Exception {
        if (folder == null) {
            throw new Exception("No FolderBean was passed to check.");
        }
        Set<FolderGroupPermissions> folderGroupPermissionsToSet = new HashSet<>();
        Set<FolderGroupPermissions> folderGroupPermissionsToCheck = folder.getFoldersPermissions();
        if (CollectionUtils.isEmpty(folderGroupPermissionsToCheck)) {
            return folderGroupPermissionsToSet;
        }
        Map<String, Permissions> groupNamesAndPermissions = new HashMap<>();
        Map<Long, Permissions> groupIdsAndPermissions = new HashMap<>();
        for (FolderGroupPermissions folderGroupPermissions : folderGroupPermissionsToCheck) {
            if (folderGroupPermissions.getPermissions() != null) {
                if (folderGroupPermissions.getGroup().getId() != null) {
                    groupIdsAndPermissions.put(folderGroupPermissions.getGroup().getId(), folderGroupPermissions.getPermissions());
                } else if (folderGroupPermissions.getGroup().getName() != null) {
                    groupNamesAndPermissions.put(folderGroupPermissions.getGroup().getName(), folderGroupPermissions.getPermissions());
                }
            }
        }
        if (groupNamesAndPermissions.isEmpty() && groupIdsAndPermissions.isEmpty()) {
            return folderGroupPermissionsToSet;
        }
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            if (!groupNamesAndPermissions.isEmpty()) {
                Set<GroupBean> checkedGroupsWithNames = new HashSet<>(session.createQuery("FROM GroupBean WHERE name IN (:groupNames)", GroupBean.class)
                        .setParameterList("groupNames", groupNamesAndPermissions.keySet()).list());
                for (GroupBean groupBean : checkedGroupsWithNames) {
                    folderGroupPermissionsToSet.add(new FolderGroupPermissions(groupBean, folder, groupNamesAndPermissions.get(groupBean.getName())));
                }
            }
            if (!groupIdsAndPermissions.isEmpty()) {
                Set<GroupBean> checkedGroupsWithIds = new HashSet<>(session.createQuery("FROM GroupBean WHERE id IN (:ids)", GroupBean.class)
                        .setParameterList("ids", groupIdsAndPermissions.keySet()).list());
                for (GroupBean groupBean : checkedGroupsWithIds) {
                    folderGroupPermissionsToSet.add(new FolderGroupPermissions(groupBean, folder, groupIdsAndPermissions.get(groupBean.getId())));
                }
            }
            transaction.commit();
            return folderGroupPermissionsToSet;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }*/

    private void initializeConnectionsForList(List<FolderBean> folderBeanList) {
        for (FolderBean folderBean : folderBeanList) {
            initializeConnections(folderBean);
        }
    }

    private void initializeConnections(FolderBean folder) {
        for (FolderGroupPermissions folderGroupPermissions : folder.getFoldersPermissions()) {
            Hibernate.initialize(folderGroupPermissions);
        }
    }
}