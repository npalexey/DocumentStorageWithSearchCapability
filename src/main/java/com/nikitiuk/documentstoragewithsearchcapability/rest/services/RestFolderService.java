package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.FolderDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.FolderBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.DtoDaoTransformer;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.security.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.services.LocalStorageService;
import com.nikitiuk.documentstoragewithsearchcapability.services.SolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestFolderService {

    private static final Logger logger = LoggerFactory.getLogger(RestFolderService.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FolderDao folderDao = new FolderDao();
    private DocDao docDao = new DocDao();
    private DtoDaoTransformer dtoDaoTransformer = new DtoDaoTransformer();
    private LocalStorageService localStorageService = new LocalStorageService();

    public List<FolderBean> getFolders(SecurityContextImplementation securityContext) throws Exception {
        return folderDao.getFoldersForUser(dtoDaoTransformer.userPrincipalToUserBean(securityContext.getUserPrincipal()));
    }

    public String deleteFolderById(SecurityContextImplementation securityContext, Long folderId) throws Exception {
        InspectorService.checkIfIdIsNull(folderId);
        FolderBean folderToDelete = folderDao.getById(folderId);
        InspectorService.checkIfFolderIsNull(folderToDelete);
        InspectorService.checkUserRightsForFolderAndGetAllowedGroups(securityContext.getUserPrincipal(), folderToDelete, Permissions.WRITE);
        localStorageService.fileOrRecursiveFolderDeleter(folderToDelete.getPath());
        /*for(DocBean docBean : docDao.getDocumentsForUserInFolder(
                dtoDaoTransformer.userPrincipalToUserBean(securityContext.getUserPrincipal()), folderToDelete)) {
            docDao.deleteDocument(docBean.getId());
        }*/
        folderDao.deleteFolder(folderToDelete.getId());
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentOrRecursiveFolderFromSolrIndex(folderToDelete.getPath());
            } catch (IOException | SolrServerException e) {
                logger.error("Error wile deleting from Solr.", e);
                throw new WebApplicationException("Error while deleting document from index. Please, try again.");
            }
        };
        executorService.execute(deleteTask);
        return folderToDelete.getPath();
    }
}
