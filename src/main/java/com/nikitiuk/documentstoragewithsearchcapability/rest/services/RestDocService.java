package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.exceptions.NoValidDataFromSourceException;
import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.InspectorService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithsearchcapability.services.LocalStorageService;
import com.nikitiuk.documentstoragewithsearchcapability.services.SolrService;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
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
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    private DocDao docDao = new DocDao();
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
        if(documentToGetContentOf == null) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while getting content of the document by id: " + documentId
                    + ". No document with such id.");
        }
        return getContentOfDocument(documentToGetContentOf.getName());
    }

    public Response getContentOfDocument(String docName) {
        List<String> docContent;
        try {
            InspectorService.checkIfNameIsBlank(docName);
            docContent = new ArrayList<>(localStorageService.documentContentGetter(docName));
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while getting content of: " + docName
                    + ". " + e.getMessage());
        }
        final Context ctx = new Context();
        ctx.setVariable("docContent", docContent);
        ctx.setVariable("fileName", docName);
        return ResponseService.okResponseWithContext("content", ctx);
    }

    public Response downloadDocumentById(Long documentId) {
        DocBean documentToDownload = docDao.getById(documentId);
        if(documentToDownload == null) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while downloading the document by id: " + documentId
                    + ". No document with such id.");
        }
        return downloadDocument(documentToDownload.getName());
    }

    public Response downloadDocument(String docName) {
        StreamingOutput fileStream;
        try {
            InspectorService.checkIfNameIsBlank(docName);
            fileStream = localStorageService.fileDownloader(docName);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while downloading document: " + docName
                    + ". " + e.getMessage());
        }
        return ResponseService.okResponseForFile(fileStream, docName);
    }

    public Response uploadDocument(InputStream fileInputStream,
                                   //FormDataContentDisposition fileMetaData,
                                   String designatedName) {
        DocBean createdDoc;
        try {
            InspectorService.checkIfNameIsBlank(designatedName);
            localStorageService.fileUploader(fileInputStream, designatedName);
            createdDoc = docDao.saveDocument(new DocBean(designatedName, PATH + designatedName));
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while uploading document: " + designatedName
                    + ". " + e.getMessage());
        }
        Runnable addTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(designatedName,
                        URLConnection.guessContentTypeFromName(new File(PATH + designatedName).getName()));
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
        StringBuilder contentBuilder = new StringBuilder(/*"Nothing was found"*/);
        try {
            if (StringUtils.isBlank(query)) {
                throw new NoValidDataFromSourceException("Query is blank.");
            }
            List<DocBean> docBeanList = docDao.getDocumentsForUser(securityContext.getUser());
            contentBuilder.append(SolrService.searchAndReturnDocsAndHighlightedText(query, docBeanList))/*.delete(0, 17)*/;
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
        if(documentToUpdate == null) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while updating the document by id: " + documentId
                    + ". No document with such id.");
        }
        return updateDocument(documentToUpdate.getName(), fileInputStream);
    }

    public Response updateDocument(String docName, InputStream fileInputStream) {
        String docNameForContentTypeCheck;
        DocBean updatedDocument;
        try {
            InspectorService.checkIfNameIsBlank(docName);
            docNameForContentTypeCheck = localStorageService.fileUpdater(fileInputStream, docName);
            updatedDocument = docDao.getDocByName(docName);
        } catch (IOException | NoValidDataFromSourceException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while updating document: " + docName
                    + ". " + e.getMessage());
        }
        Runnable putTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(docName, URLConnection.guessContentTypeFromName(docNameForContentTypeCheck));
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
        if(documentToDelete == null) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while deleting the document by id: " + documentId
                    + ". No document with such id.");
        }
        return deleteDocument(documentToDelete.getName());
    }

    public Response deleteDocument(String docName) {
        try {
            InspectorService.checkIfNameIsBlank(docName);
            localStorageService.fileDeleter(docName);
            docDao.deleteDocument(new DocBean(docName, PATH + docName));
        } catch (IOException | NoValidDataFromSourceException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while deleting the document: " + docName
                    + ". " + e.getMessage());
        }
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentFromSolrIndex(docName);
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while deleting document from index. Please, try again.");
            }
        };
        executorService.execute(deleteTask);
        return ResponseService.okResponseSimple("Document deleted successfully.");
    }
}