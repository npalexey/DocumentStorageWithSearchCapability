package com.nikitiuk.documentstoragewithsearchcapability.dao.implementations;

import com.nikitiuk.documentstoragewithsearchcapability.dao.GenericHibernateDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.*;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.AlreadyExistsException;
import com.nikitiuk.documentstoragewithsearchcapability.utils.HibernateUtil;
import javassist.NotFoundException;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GroupDao extends GenericHibernateDao<GroupBean> {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);
    static List<GroupBean> groupList = new ArrayList<>(Arrays.asList(new GroupBean("ADMINS"),
            new GroupBean("USERS"), new GroupBean("GUESTS")));

    public GroupDao() {
        super(GroupBean.class);
    }

    public static void populateTableWithGroups() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (GroupBean groupBean : groupList) {
                Transaction transaction = null;
                try {
                    transaction = session.beginTransaction();
                    session.saveOrUpdate(groupBean);
                    transaction.commit();
                } catch (Exception e) {
                    logger.error("Error at GroupDao populate: ", e);
                    if (transaction != null) {
                        transaction.rollback();
                    }
                }
            }
        }
    }

    public List<GroupBean> getGroups() {
        Transaction transaction = null;
        List<GroupBean> groupBeanList = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            groupBeanList = session.createQuery("FROM GroupBean", GroupBean.class).list();
            for (GroupBean groupBean : groupBeanList) {
                initializeConnections(groupBean);
            }
            transaction.commit();
            return groupBeanList;
        } catch (Exception e) {
            logger.error("Error at GroupDao getAll: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            return groupBeanList;
        }
    }

    public GroupBean getGroupByName(String groupName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            GroupBean groupBean = session.createQuery("FROM GroupBean WHERE name = '"
                    + groupName + "'", GroupBean.class).uniqueResult();
            if (groupBean != null) {
                initializeConnections(groupBean);
            }
            transaction.commit();
            session.close();
            return groupBean;
        } catch (Exception e) {
            logger.error("Error at GroupDao getGroupByName: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public GroupBean getById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            GroupBean groupBean = session.get(GroupBean.class, id);
            initializeConnections(groupBean);
            transaction.commit();
            session.close();
            return groupBean;
        } catch (Exception e) {
            logger.error("Error at GroupDao getById: ", e);
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public GroupBean saveGroup(GroupBean groupBean) throws Exception {
        try {
            if (exists(groupBean)) {
                throw new AlreadyExistsException("Such Group Already Exists.");
            }
            boolean requiresMerge = true;
            groupBean.setId(null);
            return save(groupBean, requiresMerge);
        } catch (Exception e) {
            logger.error("Error at GroupDao saveGroup: ", e);
            throw e;
        }
    }

    public GroupBean updateGroup(GroupBean groupBean) throws Exception {
        try {
            boolean requiresMerge = true;
            GroupBean updatedGroup = getGroupByName(groupBean.getName());
            if (updatedGroup == null) {
                throw new NotFoundException("Group not found.");
            }
            if (updatedGroup.getUsers().containsAll(groupBean.getUsers())) {
                requiresMerge = false;
            }
            updatedGroup.setUsers(groupBean.getUsers());
            updatedGroup.setDocumentsPermissions(groupBean.getDocumentsPermissions());
            updatedGroup.setFoldersPermissions(groupBean.getFoldersPermissions());
            initializeConnections(updatedGroup);
            return save(updatedGroup, requiresMerge);
        } catch (Exception e) {
            logger.error("Error at GroupDao updateAndSaveGroup: ", e);
            throw e;
        }
    }

    public void deleteGroupByName(String groupName) {
        Transaction transaction = null;
        GroupBean checkedGroup = getGroupByName(groupName);
        if (checkedGroup != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                transaction = session.beginTransaction();
                /*session.createQuery("DELETE FROM GroupBean WHERE name = '" +
                        groupName + "'").executeUpdate();*/
                session.delete(checkedGroup);
                transaction.commit();
            } catch (Exception e) {
                logger.error("Error at GroupDao deleteGroupByName: ", e);
                if (transaction != null) {
                    transaction.rollback();
                }
            }
        }
    }

    public boolean exists(GroupBean group) throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        return session.createQuery(
                "SELECT 1 FROM GroupBean WHERE EXISTS (SELECT 1 FROM GroupBean WHERE name = '" + group.getName() + "')")
                .uniqueResult() != null;
    }

    private GroupBean save(GroupBean group, boolean requiresMerge) throws Exception {
        Transaction transaction = null;
        try {
            group.setUsers(getExistingUsers(group));
            group.setDocumentsPermissions(getPermissionsForExistingDocs(group));
            group.setFoldersPermissions(getPermissionsForExistingFolders(group));
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(group);
            if (requiresMerge) {
                session.merge(group);
            }
            transaction.commit();
            return group;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private Set<UserBean> getExistingUsers(GroupBean group) throws Exception {
        if (group == null) {
            throw new Exception("No GroupBean was passed to check.");
        }
        Set<UserBean> checkedUsers = new HashSet<>();
        if (group.getUsers() == null || group.getUsers().isEmpty()) {
            return checkedUsers;
        }
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Set<String> userNames = new HashSet<>();
            for (UserBean userBean : group.getUsers()) {
                userNames.add(userBean.getName());
            }
            transaction = session.beginTransaction();
            checkedUsers.addAll(session.createQuery("FROM UserBean WHERE name IN (:userNames)", UserBean.class).setParameterList("userNames", userNames).list());
            transaction.commit();
            return checkedUsers;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private Set<DocGroupPermissions> getPermissionsForExistingDocs(GroupBean group) throws Exception {
        if (group == null) {
            throw new Exception("No GroupBean was passed to check.");
        }
        Set<DocGroupPermissions> docGroupPermissionsToSet = new HashSet<>();
        Set<DocGroupPermissions> docGroupPermissionsToCheck = group.getDocumentsPermissions();
        if (docGroupPermissionsToCheck == null || docGroupPermissionsToCheck.isEmpty()) {
            return docGroupPermissionsToSet;
        }
        Map<String, Permissions> docPathsAndPermissions = new HashMap<>();
        Map<Long, Permissions> docIdsAndPermissions = new HashMap<>();
        for (DocGroupPermissions docGroupPermissions : docGroupPermissionsToCheck) {
            if (docGroupPermissions.getPermissions() != null) {
                if (docGroupPermissions.getDocument().getId() != null) {
                    docIdsAndPermissions.put(docGroupPermissions.getDocument().getId(), docGroupPermissions.getPermissions());
                } else if (docGroupPermissions.getDocument().getPath() != null) {
                    docPathsAndPermissions.put(docGroupPermissions.getDocument().getPath(), docGroupPermissions.getPermissions());
                }
            }
        }
        if (docPathsAndPermissions.isEmpty() && docIdsAndPermissions.isEmpty()) {
            return docGroupPermissionsToSet;
        }
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            if (!docPathsAndPermissions.isEmpty()) {
                Set<DocBean> checkedDocsWithPath = new HashSet<>(session.createQuery("FROM DocBean WHERE path IN (:docPaths)", DocBean.class)
                        .setParameterList("docPaths", docPathsAndPermissions.keySet()).list());
                for (DocBean docBean : checkedDocsWithPath) {
                    docGroupPermissionsToSet.add(new DocGroupPermissions(group, docBean, docPathsAndPermissions.get(docBean.getPath())));
                }
            }
            if (!docIdsAndPermissions.isEmpty()) {
                Set<DocBean> checkedDocsWithIds = new HashSet<>(session.createQuery("FROM DocBean WHERE id IN (:ids)", DocBean.class)
                        .setParameterList("ids", docIdsAndPermissions.keySet()).list());
                for (DocBean docBean : checkedDocsWithIds) {
                    docGroupPermissionsToSet.add(new DocGroupPermissions(group, docBean, docIdsAndPermissions.get(docBean.getId())));
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
    }

    private Set<FolderGroupPermissions> getPermissionsForExistingFolders(GroupBean group) throws Exception {
        if (group == null) {
            throw new Exception("No GroupBean was passed to check.");
        }
        Set<FolderGroupPermissions> folderGroupPermissionsToSet = new HashSet<>();
        Set<FolderGroupPermissions> folderGroupPermissionsToCheck = group.getFoldersPermissions();
        if (folderGroupPermissionsToCheck == null || folderGroupPermissionsToCheck.isEmpty()) {
            return folderGroupPermissionsToSet;
        }
        Map<String, Permissions> folderPathsAndPermissions = new HashMap<>();
        Map<Long, Permissions> folderIdsAndPermissions = new HashMap<>();
        for (FolderGroupPermissions folderGroupPermissions : folderGroupPermissionsToCheck) {
            if (folderGroupPermissions.getPermissions() != null) {
                if (folderGroupPermissions.getFolder().getId() != null) {
                    folderIdsAndPermissions.put(folderGroupPermissions.getFolder().getId(), folderGroupPermissions.getPermissions());
                } else if (folderGroupPermissions.getFolder().getPath() != null) {
                    folderPathsAndPermissions.put(folderGroupPermissions.getFolder().getPath(), folderGroupPermissions.getPermissions());
                }
            }
        }
        if (folderPathsAndPermissions.isEmpty() && folderIdsAndPermissions.isEmpty()) {
            return folderGroupPermissionsToSet;
        }
        Transaction transaction = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            if (!folderPathsAndPermissions.isEmpty()) {
                Set<FolderBean> checkedFoldersWithPath = new HashSet<>(session.createQuery("FROM FolderBean WHERE path IN (:folderPaths)", FolderBean.class)
                        .setParameterList("folderPaths", folderPathsAndPermissions.keySet()).list());
                for (FolderBean folderBean : checkedFoldersWithPath) {
                    folderGroupPermissionsToSet.add(new FolderGroupPermissions(group, folderBean, folderPathsAndPermissions.get(folderBean.getPath())));
                }
            }
            if (!folderIdsAndPermissions.isEmpty()) {
                Set<FolderBean> checkedFoldersWithIds = new HashSet<>(session.createQuery("FROM FolderBean WHERE id IN (:ids)", FolderBean.class)
                        .setParameterList("ids", folderIdsAndPermissions.keySet()).list());
                for (FolderBean folderBean : checkedFoldersWithIds) {
                    folderGroupPermissionsToSet.add(new FolderGroupPermissions(group, folderBean, folderIdsAndPermissions.get(folderBean.getId())));
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
    }

    private void initializeConnections(GroupBean group) {
        Hibernate.initialize(group.getUsers());
        for (DocGroupPermissions docGroupPermissions : group.getDocumentsPermissions()) {
            Hibernate.initialize(docGroupPermissions);
        }
        for (FolderGroupPermissions folderGroupPermissions : group.getFoldersPermissions()) {
            Hibernate.initialize(folderGroupPermissions);
        }
    }
}