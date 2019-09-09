package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocGroupPermissionsDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.FolderDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.FolderBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.GroupBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.helpers.enums.Permissions;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.services.LocalStorageService;
import com.nikitiuk.documentstoragewithsearchcapability.services.SearchResultsModifier;
import com.nikitiuk.documentstoragewithsearchcapability.services.SolrService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestDocService {

    private static final Logger logger = LoggerFactory.getLogger(RestDocService.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DocDao docDao = new DocDao();
    private FolderDao folderDao = new FolderDao();
    private DocGroupPermissionsDao docGroupPermissionsDao = new DocGroupPermissionsDao();
    private LocalStorageService localStorageService = new LocalStorageService();

    public List<DocBean> getDocuments(SecurityContextImplementation securityContext) throws Exception {
        return docDao.getDocumentsForUser(securityContext.getUser());
    }

    public List<DocBean> getDocumentsInFolder(SecurityContextImplementation securityContext, Long folderId) throws Exception {
        InspectorService.checkIfIdIsNull(folderId);
        FolderBean folder = folderDao.getById(folderId);
        return docDao.getDocumentsForUserInFolder(securityContext.getUser(), folder);
    }

    public List<String> getContentOfDocumentById(SecurityContextImplementation securityContext, Long documentId) throws Exception {
        InspectorService.checkIfIdIsNull(documentId);
        DocBean documentToGetContentOf = docDao.getById(documentId);
        if (documentToGetContentOf == null) {
            throw new NoValidDataFromSourceException("No document with such id.");
        }
        return getContentOfDocument(securityContext, documentToGetContentOf.getPath());
    }

    public List<String> getContentOfDocument(SecurityContextImplementation securityContext, String documentPath) throws Exception {
        InspectorService.checkIfStringDataIsBlank(documentPath);
        InspectorService.checkIfUserHasRightsForDocument(securityContext.getUser(), docDao.getDocByPath(documentPath), Permissions.READ);
        List<String> docContent = new ArrayList<>();
        docContent.add(documentPath);
        docContent.addAll(localStorageService.documentContentGetter(documentPath));
        return docContent;
    }

    public DocumentDownloaderResponseBuilder downloadDocumentById(SecurityContextImplementation securityContext, Long documentId) throws Exception {
        InspectorService.checkIfIdIsNull(documentId);
        DocBean documentToDownload = docDao.getById(documentId);
        if (documentToDownload == null) {
            throw new NoValidDataFromSourceException("No document with such id.");
        }
        InspectorService.checkIfUserHasRightsForDocument(securityContext.getUser(), documentToDownload, Permissions.READ);
        return new DocumentDownloaderResponseBuilder(localStorageService.fileDownloader(documentToDownload.getPath()), documentToDownload.getName());
    }

    public DocBean uploadDocument(InputStream fileInputStream,
                                  SecurityContextImplementation securityContext,
                                  String designatedName, Long parentFolderId) throws Exception {

        String folderPath;
        InspectorService.checkIfStringDataIsBlank(designatedName);
        final String trimmedDesignatedName = designatedName.trim();
        Set<GroupBean> allowedGroups;
        if (parentFolderId == null || parentFolderId == 0) {
            FolderBean folderBean = folderDao.getById(1L);
            folderPath = folderBean.getPath();
            allowedGroups = InspectorService.checkIfUserHasRightsForFolder(securityContext.getUser(), folderBean, Permissions.WRITE);
        } else {
            FolderBean folderBean = folderDao.getById(parentFolderId);
            folderPath = folderBean.getPath();
            allowedGroups = InspectorService.checkIfUserHasRightsForFolder(securityContext.getUser(), folderBean, Permissions.WRITE);
        }
        localStorageService.fileUploader(fileInputStream, folderPath + trimmedDesignatedName);
        DocBean createdDoc = docDao.saveDocument(new DocBean(trimmedDesignatedName, folderPath + trimmedDesignatedName));
        if (CollectionUtils.isNotEmpty(allowedGroups)) {
            for (GroupBean groupBean : allowedGroups) {
                docGroupPermissionsDao.setWriteForDocumentForGroup(createdDoc, groupBean);
            }
        }
        Runnable addTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(folderPath + trimmedDesignatedName,
                        new Tika().detect(trimmedDesignatedName)/*URLConnection.guessContentTypeFromName(new File(folderPath + trimmedDesignatedName).getName())*/);
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing document. Please, try again.");
            }
        };
        executorService.execute(addTask);
        return docDao.getById(createdDoc.getId());
    }

    public String searchInEveryDocumentWithStringQuery(String query, SecurityContextImplementation securityContext) throws Exception {
        StringBuilder contentBuilder = new StringBuilder();
        if (StringUtils.isBlank(query)) {
            throw new NoValidDataFromSourceException("Query is blank.");
        }
        List<DocBean> permittedDocs = docDao.getDocumentsForUser(securityContext.getUser());
        QueryResponse response = SolrService.searchInDocumentsByQuery(query);
        contentBuilder.append(SearchResultsModifier.getSearchResultForPermittedDocs(response, query, permittedDocs));
        return contentBuilder.toString().replace("\n", "<br/>");
    }

    public DocBean updateDocumentById(SecurityContextImplementation securityContext, Long documentId, InputStream fileInputStream) throws Exception {
        InspectorService.checkIfIdIsNull(documentId);
        DocBean documentToUpdate = docDao.getById(documentId);
        if (documentToUpdate == null) {
            throw new NoValidDataFromSourceException("No document with such id");
        }
        return updateDocumentByPath(securityContext, documentToUpdate.getPath(), fileInputStream);
    }

    public DocBean updateDocumentByPath(SecurityContextImplementation securityContext, String documentPath, InputStream fileInputStream) throws Exception {
        InspectorService.checkIfStringDataIsBlank(documentPath);
        DocBean updatedDocument = docDao.getDocByPath(documentPath);
        InspectorService.checkIfUserHasRightsForDocument(securityContext.getUser(), updatedDocument, Permissions.WRITE);
        String docNameForContentTypeCheck = localStorageService.fileUpdater(fileInputStream, documentPath);
        Runnable putTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(documentPath, new Tika().detect(docNameForContentTypeCheck));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing document. Please, try again.");
            }
        };
        executorService.execute(putTask);
        return updatedDocument;
    }

    public String deleteDocumentById(SecurityContextImplementation securityContext, Long documentId) throws Exception {
        InspectorService.checkIfIdIsNull(documentId);
        DocBean documentToDelete = docDao.getById(documentId);
        if (documentToDelete == null) {
            throw new NoValidDataFromSourceException("No document with such id.");
        }
        return deleteDocument(securityContext, documentToDelete.getPath());
    }

    public String deleteDocument(SecurityContextImplementation securityContext, String documentPath) throws Exception {
        InspectorService.checkIfStringDataIsBlank(documentPath);
        DocBean docBeanToDelete = docDao.getDocByPath(documentPath);
        InspectorService.checkIfUserHasRightsForDocument(securityContext.getUser(), docBeanToDelete, Permissions.WRITE);
        localStorageService.fileDeleter(documentPath);
        docDao.deleteDocument(docBeanToDelete);
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentFromSolrIndex(documentPath);
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while deleting document from index. Please, try again.");
            }
        };
        executorService.execute(deleteTask);
        return documentPath;
    }
}