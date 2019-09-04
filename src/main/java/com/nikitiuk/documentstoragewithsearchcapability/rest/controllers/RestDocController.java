package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

@Tag(name = "Document Controller")
@PermitAll
@Path("/doc")
public class RestDocController {

    private static final Logger logger = LoggerFactory.getLogger(RestDocController.class);
    private RestDocService docService = new RestDocService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDocuments(@Context ContainerRequestContext context) {
        List<DocBean> docBeanList;
        try {
            docBeanList = docService.getDocuments((SecurityContextImplementation) context.getSecurityContext());
        } catch (Exception e) {
            logger.error("Error at RestDocController getDocuments", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing list of documents. " + e.getMessage());
        }
        final org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("entityName", "Document");
        ctx.setVariable("inStorage", docBeanList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    @PermitAll
    @GET
    @Path("/in-folder/{folderid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getDocumentInFolder(@Context ContainerRequestContext context,
                                        @PathParam("folderid") long folderId) {
        List<DocBean> docBeanList;
        try {
            docBeanList = docService.getDocumentsInFolder((SecurityContextImplementation) context.getSecurityContext(), folderId);
        } catch (Exception e) {
            logger.error("Error at RestDocController getDocumentsInFolder", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while producing list of documents in folder. " + e.getMessage());
        }
        final org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("entityName", "Document");
        ctx.setVariable("inStorage", docBeanList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    @PermitAll
    @GET
    @Path("{documentid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentById(@Context ContainerRequestContext context,
                                         @PathParam("documentid") long documentId) {
        DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder;
        try {
            documentDownloaderResponseBuilder = docService.downloadDocumentById((SecurityContextImplementation) context.getSecurityContext(), documentId);
        } catch (Exception e) {
            logger.error("Error at RestDocController downloadDocumentById", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while downloading the document by id: " + documentId
                    + ". " + e.getMessage());
        }
        return ResponseService.okResponseForFile(documentDownloaderResponseBuilder);
    }

    @PermitAll
    @GET
    @Path("/by-path")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentByPath(@Context ContainerRequestContext context,
                                           @FormDataParam("documentPath") String documentPath) {
        DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder;
        try {
            documentDownloaderResponseBuilder = docService.downloadDocumentByPath((SecurityContextImplementation) context.getSecurityContext(), documentPath);
        } catch (Exception e) {
            logger.error("Error at RestDocController downloadDocumentByPath", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while downloading document: " + documentPath
                    + ". " + e.getMessage());
        }
        return ResponseService.okResponseForFile(documentDownloaderResponseBuilder);
    }

    @PermitAll
    @GET
    @Path("/{documentid}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response getContentOfDocumentById(@PathParam("documentid") long docId,
                                             @Context ContainerRequestContext context) {
        List<String> documentContent;
        try {
            documentContent = docService.getContentOfDocumentById((SecurityContextImplementation) context.getSecurityContext(), docId);
        } catch (Exception e) {
            logger.error("Error at RestDocController getContentOfDocumentById", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while getting content of the document by id: "
                    + docId + ". " + e.getMessage());
        }
        final org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("docContent", documentContent);
        ctx.setVariable("filePath", documentContent.get(0));
        return ResponseService.okResponseWithContext("content", ctx);
    }

    @PermitAll
    @GET
    @Path("/by-path/content")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response getContentOfDocumentByPath(@FormDataParam("documentPath") String documentPath,
                                               @Context ContainerRequestContext context) {
        List<String> documentContent;
        try {
            documentContent = docService.getContentOfDocument((SecurityContextImplementation) context.getSecurityContext(), documentPath);
        } catch (Exception e) {
            logger.error("Error at RestDocController getContentOfDocumentByPath", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while getting content of: " + documentPath
                    + ". " + e.getMessage());
        }
        final org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("docContent", documentContent);
        ctx.setVariable("filePath", documentContent.get(0));
        return ResponseService.okResponseWithContext("content", ctx);
    }

    @PermitAll
    @POST
    @Path("/{parentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDocument(@FormDataParam("file") InputStream fileInputStream,
                                   @Context ContainerRequestContext context,
                                   @FormDataParam("designatedName") String designatedName,
                                   //@FormDataParam("file") FormDataContentDisposition fileMetaData,
                                   @PathParam("parentid") long parentFolderId) {
        DocBean uploadedDocument;
        try {
            uploadedDocument = docService.uploadDocument(fileInputStream, (SecurityContextImplementation) context.getSecurityContext(), designatedName, parentFolderId);
        } catch (Exception e) {
            logger.error("Error at RestDocController uploadDocument", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while uploading document: " + designatedName
                    + ". " + e.getMessage());
        }
        final org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("entityType", "Document");
        ctx.setVariable("entity", uploadedDocument);
        ctx.setVariable("action", "uploaded");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    @PermitAll
    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public Response searchInEveryDocumentWithStringQuery(@DefaultValue("") @QueryParam("query") String query,
                                                         @Context ContainerRequestContext context) {
        String searchResult;
        try {
            searchResult = docService.searchInEveryDocumentWithStringQuery(query, (SecurityContextImplementation) context.getSecurityContext());
        } catch (Exception e) {
            logger.error("Error at RestDocController searchInEveryDocumentWithStringQuery", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while searching for: " + query
                    + ". " + e.getMessage());
        }
        final org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("searchResult", searchResult);
        return ResponseService.okResponseWithContext("search", ctx);
    }

    @PermitAll
    @PUT
    @Path("/{documentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocumentById(@Context ContainerRequestContext context,
                                       @PathParam("documentid") long documentId,
                                       @FormDataParam("file") InputStream fileInputStream) {
        DocBean updatedDocument;
        try {
            updatedDocument = docService.updateDocumentById((SecurityContextImplementation) context.getSecurityContext(), documentId, fileInputStream);
        } catch (Exception e) {
            logger.error("Error at RestDocController updateDocumentById", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while updating the document by id: " + documentId
                    + ". " + e.getMessage());
        }
        final org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("entityType", "Document");
        ctx.setVariable("entity", updatedDocument);
        ctx.setVariable("action", "updated");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    @PermitAll
    @PUT
    @Path("/by-path/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocumentByPath(@Context ContainerRequestContext context,
                                         @FormDataParam("documentPath") String documentPath,
                                         @FormDataParam("file") InputStream fileInputStream) {
        DocBean updatedDocument;
        try {
            updatedDocument = docService.updateDocumentByPath((SecurityContextImplementation) context.getSecurityContext(), documentPath, fileInputStream);
        } catch (Exception e) {
            logger.error("Error at RestDocController updateDocumentByPath", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error while updating document: " + documentPath
                    + ". " + e.getMessage());
        }
        final org.thymeleaf.context.Context ctx = new org.thymeleaf.context.Context();
        ctx.setVariable("entityType", "Document");
        ctx.setVariable("entity", updatedDocument);
        ctx.setVariable("action", "updated");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    @PermitAll
    @DELETE
    @Path("/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocumentById(@Context ContainerRequestContext context,
                                       @PathParam("documentid") long documentId) {
        String deletedDocumentPath;
        try {
            deletedDocumentPath = docService.deleteDocumentById((SecurityContextImplementation) context.getSecurityContext(), documentId);
        } catch (Exception e) {
            logger.error("Error at RestDocController deleteDocumentById", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while deleting the document by id: " + documentId
                    + ". " + e.getMessage());
        }
        return ResponseService.okResponseSimple("Document: '" + deletedDocumentPath + "' deleted successfully.");
    }

    @PermitAll
    @DELETE
    @Path("/by-path/")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocumentByName(@Context ContainerRequestContext context,
                                         @FormDataParam("documentPath") String documentPath) {
        String deletedDocumentPath;
        try {
            deletedDocumentPath = docService.deleteDocument((SecurityContextImplementation) context.getSecurityContext(), documentPath);
        } catch (Exception e) {
            logger.error("Error at RestDocController deleteDocumentByName", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND, "Error occurred while deleting the document: " + documentPath
                    + ". " + e.getMessage());
        }
        return ResponseService.okResponseSimple("Document: '" + deletedDocumentPath + "' deleted successfully.");
    }
}