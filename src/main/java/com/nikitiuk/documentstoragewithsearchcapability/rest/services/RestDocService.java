package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.services.LocalStorageService;
import com.nikitiuk.documentstoragewithsearchcapability.services.SolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.thymeleaf.context.Context;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Path;
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

@PermitAll
@Path("/doc")
public class RestDocService {

    private static final String PATH = "/home/npalexey/workenv/DOWNLOADED/";
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    private DocDao docDao = new DocDao();

    public Response showFilesInDoc(SecurityContextImplementation securityContext) {
        List<DocBean> docBeanList;
        try {
            docBeanList = docDao.getDocuments(securityContext.getUser());
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing list of content.");
        }
        /*Runnable getTask = () -> {
            try {
                HibernateUtil.getSessionFactory().openSession();
            } catch (Exception e) {
                throw new WebApplicationException("Error while indexing files with DB. Please, try again");
            }
        };
        executorService.execute(getTask);*/
        //Executor.updateDbInfo();
        final Context ctx = new Context();
        ctx.setVariable("entityName", "Document");
        ctx.setVariable("inStorage", docBeanList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public Response showContentOfFile(String filename) {
        List<String> docContent;
        try {
            docContent = new ArrayList<>(LocalStorageService.documentContentGetter(filename));
        } catch (IOException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while getting content of " + filename);
        }
        final Context ctx = new Context();
        ctx.setVariable("docContent", docContent);
        ctx.setVariable("fileName", filename);
        return ResponseService.okResponseWithContext("content", ctx);
    }

    public Response downloadFile(String filename) {
        StreamingOutput fileStream;
        try {
            fileStream = LocalStorageService.fileDownloader(filename);
        } catch (Exception e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while downloading " + filename);
        }
        return ResponseService.okResponseForFile(fileStream, filename);
    }

    public Response uploadFile(InputStream fileInputStream,
                               //FormDataContentDisposition fileMetaData,
                               String parentID) {
        try {
            LocalStorageService.fileUploader(fileInputStream, parentID);
        } catch (IOException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while uploading file " + parentID);
        }
        Runnable addTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(parentID,
                        URLConnection.guessContentTypeFromName(new File(PATH + parentID).getName()));
                docDao.saveDocument(new DocBean(parentID, PATH + parentID));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(addTask);
        return ResponseService.okResponseSimple("Data uploaded successfully");
    }

    public Response searchInEveryFileWithStringQuery(String query, SecurityContextImplementation securityContext) {
        StringBuilder contentBuilder = new StringBuilder("Nothing was found");
        List<DocBean> docBeanList;
        try {
            docBeanList = docDao.getDocuments(securityContext.getUser());
            contentBuilder.append(SolrService.searchAndReturnDocsAndHighlightedText(query, docBeanList)).delete(0, 18);
        } catch (IOException | SolrServerException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while searching for: " + query
                    + ". Please, try again");
        }
        final Context ctx = new Context();
        ctx.setVariable("searchResult", contentBuilder.toString().replace("\n", "<br/>"));
        return ResponseService.okResponseWithContext("search", ctx);
    }

    public Response updateDocument(String docID, InputStream fileInputStream) {
        String filename;
        try {
            filename = LocalStorageService.fileUpdater(fileInputStream, docID);
        } catch (IOException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while updating file: " + docID);
        }
        Runnable putTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(docID, URLConnection.guessContentTypeFromName(filename));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(putTask);
        return ResponseService.okResponseSimple("File updated successfully");
    }

    public Response deleteDocument(String docID) {
        try {
            LocalStorageService.fileDeleter(docID);
        } catch (IOException e) {
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while deleting the file " + docID);
        }
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentFromSolrIndex(docID);
                docDao.deleteDocument(new DocBean(docID, PATH + docID));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(deleteTask);
        final Context ctx = new Context();
        ctx.setVariable("code", "OK");
        ctx.setVariable("message", "File deleted successfully");
        return ResponseService.okResponseWithContext("info", ctx);
    }
}