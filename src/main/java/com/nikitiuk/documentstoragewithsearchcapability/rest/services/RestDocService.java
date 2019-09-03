package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.FolderDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithsearchcapability.services.LocalStorageService;
import com.nikitiuk.documentstoragewithsearchcapability.services.SearchResultsModifier;
import com.nikitiuk.documentstoragewithsearchcapability.services.SolrService;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestDocService {

    private static final String PATH = "/home/npalexey/workenv/DOWNLOADED/";
    private static final Logger logger = LoggerFactory.getLogger(RestDocService.class);
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DocDao docDao = new DocDao();
    private FolderDao folderDao = new FolderDao();
    private LocalStorageService localStorageService = new LocalStorageService();

    public Response getDocuments(SecurityContextImplementation securityContext) {
        List<DocBean> docBeanList;
        try {
            docBeanList = docDao.getDocumentsForUser(securityContext.getUser());
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing list of documents. " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("entityName", "Document");
        ctx.setVariable("inStorage", docBeanList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response getContentOfDocumentById(Long documentId) {
        DocBean documentToGetContentOf = docDao.getById(documentId);
        if (documentToGetContentOf == null) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while getting content of the document by id: " + documentId
                    + ". No document with such id.");
        }
        return getContentOfDocument(documentToGetContentOf.getPath());
    }

    public Response getContentOfDocument(String documentPath) {
        List<String> docContent;
        try {
            InspectorService.checkIfStringDataIsBlank(documentPath);
            docContent = new ArrayList<>(localStorageService.documentContentGetter(documentPath));
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while getting content of: " + documentPath
                    + ". " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("docContent", docContent);
        ctx.setVariable("filePath", documentPath);
        return ResponseService.okResponseWithContext("content", ctx);
    }

    public Response downloadDocumentById(Long documentId) {
        DocBean documentToDownload = docDao.getById(documentId);
        if (documentToDownload == null) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while downloading the document by id: " + documentId
                    + ". No document with such id.");
        }
        return downloadDocumentByPath(documentToDownload.getPath());
    }

    public Response downloadDocumentByPath(String documentPath) {
        StreamingOutput fileStream;
        try {
            InspectorService.checkIfStringDataIsBlank(documentPath);
            fileStream = localStorageService.fileDownloader(documentPath);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while downloading document: " + documentPath
                    + ". " + e.getMessage());
        }
        return ResponseService.okResponseForFile(fileStream, documentPath);
    }

    public Response uploadDocument(InputStream fileInputStream,
                                   //FormDataContentDisposition fileMetaData,
                                   String designatedName, Long parentFolderId) {
        DocBean createdDoc;
        String folderPath;
        try {
            InspectorService.checkIfStringDataIsBlank(designatedName);
            if (parentFolderId == null || parentFolderId == 0) {
                folderPath = PATH;
            } else {
                folderPath = folderDao.getById(parentFolderId).getPath();
            }
            localStorageService.fileUploader(fileInputStream, folderPath + designatedName);
            createdDoc = docDao.saveDocument(new DocBean(designatedName, folderPath + designatedName));
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while uploading document: " + designatedName
                    + ". " + e.getMessage());
        }
        Runnable addTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(folderPath + designatedName,
                        URLConnection.guessContentTypeFromName(new File(folderPath + designatedName).getName()));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing document. Please, try again.");
            }
        };
        executorService.execute(addTask);
        final Context ctx = new Context();
        ctx.setVariable("entityType", "Document");
        ctx.setVariable("entity", createdDoc);
        ctx.setVariable("action", "uploaded");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response searchInEveryDocumentWithStringQuery(String query, SecurityContextImplementation securityContext) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            if (StringUtils.isBlank(query)) {
                throw new NoValidDataFromSourceException("Query is blank.");
            }
            List<DocBean> permittedDocs = docDao.getDocumentsForUser(securityContext.getUser());
            QueryResponse response = SolrService.searchInDocumentsByQuery(query);
            contentBuilder.append(SearchResultsModifier.getSearchResultForPermittedDocs(response, query, permittedDocs));
        } catch (IOException | SolrServerException | NoValidDataFromSourceException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while searching for: " + query
                    + ". " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("searchResult", contentBuilder.toString().replace("\n", "<br/>"));
        return ResponseService.okResponseWithContext("search", ctx);
    }

    public Response updateDocumentById(Long documentId, InputStream fileInputStream) {
        DocBean documentToUpdate = docDao.getById(documentId);
        if (documentToUpdate == null) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while updating the document by id: " + documentId
                    + ". No document with such id.");
        }
        return updateDocumentByPath(documentToUpdate.getPath(), fileInputStream);
    }

    public Response updateDocumentByPath(String documentPath, InputStream fileInputStream) {
        String docNameForContentTypeCheck;
        DocBean updatedDocument;
        try {
            InspectorService.checkIfStringDataIsBlank(documentPath);
            docNameForContentTypeCheck = localStorageService.fileUpdater(fileInputStream, documentPath);
            updatedDocument = docDao.getDocByPath(documentPath);
        } catch (IOException | NoValidDataFromSourceException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while updating document: " + documentPath
                    + ". " + e.getMessage());
        }
        Runnable putTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(documentPath, URLConnection.guessContentTypeFromName(docNameForContentTypeCheck));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing document. Please, try again.");
            }
        };
        executorService.execute(putTask);
        final Context ctx = new Context();
        ctx.setVariable("entityType", "Document");
        ctx.setVariable("entity", updatedDocument);
        ctx.setVariable("action", "updated");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public Response deleteDocumentById(Long documentId) {
        DocBean documentToDelete = docDao.getById(documentId);
        if (documentToDelete == null) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while deleting the document by id: " + documentId
                    + ". No document with such id.");
        }
        return deleteDocument(documentToDelete.getPath());
    }

    public Response deleteDocument(String documentPath) {
        try {
            InspectorService.checkIfStringDataIsBlank(documentPath);
            localStorageService.fileDeleter(documentPath);
            docDao.deleteDocument(new DocBean(documentPath, documentPath));
        } catch (IOException | NoValidDataFromSourceException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while deleting the document: " + documentPath
                    + ". " + e.getMessage());
        }
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentFromSolrIndex(documentPath);
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while deleting document from index. Please, try again.");
            }
        };
        executorService.execute(deleteTask);
        return ResponseService.okResponseSimple("Document: '" + documentPath + "' deleted successfully.");
    }
}