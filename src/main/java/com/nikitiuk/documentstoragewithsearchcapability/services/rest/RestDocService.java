package com.nikitiuk.documentstoragewithsearchcapability.services.rest;

import com.nikitiuk.documentstoragewithsearchcapability.dao.implementations.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.services.LocalStorageService;
import com.nikitiuk.documentstoragewithsearchcapability.services.SolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.thymeleaf.context.Context;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showFilesInDoc() {
        List<DocBean> docBeanList;
        try {
            docBeanList = new ArrayList<>(LocalStorageService.listDocumentsInPath());
        } catch (IOException e) {
            return ResponseService.errorResponse(404, "Error while producing list of content.");
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
        return ResponseService.okResponseForText("storagehome", ctx);
    }

    @PermitAll
    @GET
    @Path("/{filename}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response showContentOfFile(@PathParam("filename") String filename) {
        List<String> docContent;
        try {
            docContent = new ArrayList<>(LocalStorageService.documentContentGetter(filename));
        } catch (IOException e) {
            return ResponseService.errorResponse(404, "Error while getting content of " + filename);
        }
        final Context ctx = new Context();
        ctx.setVariable("docContent", docContent);
        ctx.setVariable("fileName", filename);
        return ResponseService.okResponseForText("content", ctx);
    }

    @PermitAll
    @GET
    @Path("/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("filename") String filename) {
        StreamingOutput fileStream;
        try {
            fileStream = LocalStorageService.fileDownloader(filename);
        } catch (Exception e) {
            return ResponseService.errorResponse(404, "Error while downloading " + filename);
        }
        return ResponseService.okResponseForFile(fileStream, filename);
    }

    @PermitAll
    @POST
    @Path("/{parentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormDataParam("file") InputStream fileInputStream,
                               //@FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @PathParam("parentid") String parentID) throws Exception {
        try {
            LocalStorageService.fileUploader(fileInputStream, parentID);
        } catch (IOException e) {
            return ResponseService.errorResponse(404, "Error while uploading file " + parentID);
        }
        Runnable addTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(parentID,
                        URLConnection.guessContentTypeFromName(new File(PATH + parentID).getName()));
                DocDao.saveDocument(new DocBean(parentID, PATH + parentID));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(addTask);
        return ResponseService.okResponseSimple("Data uploaded successfully");
    }

    @PermitAll
    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public Response searchInEveryFileWithStringQuery(@DefaultValue("") @QueryParam("query") String query) {
        StringBuilder contentBuilder = new StringBuilder("Nothing was found");
        try {
            contentBuilder.append(SolrService.searchAndReturnDocsAndHighlightedText(query)).delete(0, 18);
        } catch (IOException | SolrServerException e) {
            return ResponseService.errorResponse(404,"Error while searching for: " + query
                    + ". Please, try again");
        }
        final Context ctx = new Context();
        ctx.setVariable("searchResult", contentBuilder.toString().replace("\n", "<br/>"));
        return ResponseService.okResponseForText("search", ctx);
    }

    @PermitAll
    @PUT
    @Path("/{documentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocument(@PathParam("documentid") String docID,
                                   @FormDataParam("file") InputStream fileInputStream) {
        String filename;
        try {
            filename = LocalStorageService.fileUpdater(fileInputStream, docID);
        } catch (IOException e) {
            return ResponseService.errorResponse(404, "Error while updating file: " + docID);
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

    @PermitAll
    @DELETE
    @Path("/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocument(@PathParam("documentid") String docID) {
        try {
            LocalStorageService.fileDeleter(docID);
        } catch (IOException e) {
            return ResponseService.errorResponse(404, "Error occurred while deleting the file " + docID);
        }
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentFromSolrIndex(docID);
                DocDao.deleteDocument(new DocBean(docID, PATH + docID));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(deleteTask);
        final Context ctx = new Context();
        ctx.setVariable("code", "OK");
        ctx.setVariable("message", "File deleted successfully");
        return ResponseService.okResponseForText("info", ctx);
    }
}