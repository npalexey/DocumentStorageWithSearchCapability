package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ThymeleafResponseService;
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
        try {
            List<DocBean> docBeanList = docService.getDocuments(
                    (SecurityContextImplementation) context.getSecurityContext());
            return ThymeleafResponseService.visualiseDocumentsInStorage(docBeanList);
        } catch (Exception e) {
            logger.error("Error at RestDocController getDocuments.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while producing list of documents. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/in-folder/{folderid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getDocumentInFolder(@Context ContainerRequestContext context,
                                        @PathParam("folderid") long folderId) {
        try {
            List<DocBean> docBeanList = docService.getDocumentsInFolder(
                    (SecurityContextImplementation) context.getSecurityContext(), folderId);
            return ThymeleafResponseService.visualiseDocumentsInStorage(docBeanList);
        } catch (Exception e) {
            logger.error("Error at RestDocController getDocumentsInFolder.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while producing list of documents in folder. %s", e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("{documentid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentById(@Context ContainerRequestContext context,
                                         @PathParam("documentid") long documentId) {
        try {
            DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder = docService.downloadDocumentById(
                    (SecurityContextImplementation) context.getSecurityContext(), documentId);
            return ResponseService.okResponseForFile(documentDownloaderResponseBuilder);
        } catch (Exception e) {
            logger.error("Error at RestDocController downloadDocumentById.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while downloading the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/{documentid}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response getContentOfDocumentById(@PathParam("documentid") long documentId,
                                             @Context ContainerRequestContext context) {
        try {
            List<String> documentContent = docService.getContentOfDocumentById(
                    (SecurityContextImplementation) context.getSecurityContext(), documentId);
            return ThymeleafResponseService.visualiseDocumentContent(documentContent);
        } catch (Exception e) {
            logger.error("Error at RestDocController getContentOfDocumentById.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while getting content of the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
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
        try {
            DocBean uploadedDocument = docService.uploadDocument(fileInputStream,
                    (SecurityContextImplementation) context.getSecurityContext(), designatedName, parentFolderId);
            return ThymeleafResponseService.visualiseUploadedDocument(uploadedDocument);
        } catch (Exception e) {
            logger.error("Error at RestDocController uploadDocument.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while uploading document: %s. %s", designatedName, e.getMessage()));
        }
    }

    @PermitAll
    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public Response searchInEveryDocumentWithStringQuery(@DefaultValue("") @QueryParam("query") String query,
                                                         @Context ContainerRequestContext context) {
        try {
            String searchResult = docService.searchInEveryDocumentWithStringQuery(query,
                    (SecurityContextImplementation) context.getSecurityContext());
            return ThymeleafResponseService.visualiseSearchResult(searchResult);
        } catch (Exception e) {
            logger.error("Error at RestDocController searchInEveryDocumentWithStringQuery.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while searching for: %s. %s", query, e.getMessage()));
        }
    }

    @PermitAll
    @PUT
    @Path("/{documentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocumentById(@Context ContainerRequestContext context,
                                       @PathParam("documentid") long documentId,
                                       @FormDataParam("file") InputStream fileInputStream) {
        try {
            DocBean updatedDocument = docService.updateDocumentById(
                    (SecurityContextImplementation) context.getSecurityContext(), documentId, fileInputStream);
            return ThymeleafResponseService.visualiseUpdatedDocument(updatedDocument);
        } catch (Exception e) {
            logger.error("Error at RestDocController updateDocumentById.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while updating the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @DELETE
    @Path("/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocumentById(@Context ContainerRequestContext context,
                                       @PathParam("documentid") long documentId) {
        try {
            String deletedDocumentPath = docService.deleteDocumentById(
                    (SecurityContextImplementation) context.getSecurityContext(), documentId);
            return ResponseService.okResponseSimple(String.format("Document: '%s' deleted successfully.", deletedDocumentPath));
        } catch (Exception e) {
            logger.error("Error at RestDocController deleteDocumentById.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while deleting the document by id: %d. %s",
                            documentId, e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/by-path")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentByPath(@Context ContainerRequestContext context,
                                           @FormDataParam("documentPath") String documentPath) {
        try {
            DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder = docService.downloadDocumentByPath(
                    (SecurityContextImplementation) context.getSecurityContext(), documentPath);
            return ResponseService.okResponseForFile(documentDownloaderResponseBuilder);
        } catch (Exception e) {
            logger.error("Error at RestDocController downloadDocumentByPath.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while downloading document: %s. %s", documentPath, e.getMessage()));
        }
    }

    @PermitAll
    @GET
    @Path("/by-path/content")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response getContentOfDocumentByPath(@FormDataParam("documentPath") String documentPath,
                                               @Context ContainerRequestContext context) {
        try {
            List<String> documentContent = docService.getContentOfDocument(
                    (SecurityContextImplementation) context.getSecurityContext(), documentPath);
            return ThymeleafResponseService.visualiseDocumentContent(documentContent);
        } catch (Exception e) {
            logger.error("Error at RestDocController getContentOfDocumentByPath.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while getting content of: %s. %s", documentPath, e.getMessage()));
        }
    }

    @PermitAll
    @PUT
    @Path("/by-path/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocumentByPath(@Context ContainerRequestContext context,
                                         @FormDataParam("documentPath") String documentPath,
                                         @FormDataParam("file") InputStream fileInputStream) {
        try {
            DocBean updatedDocument = docService.updateDocumentByPath(
                    (SecurityContextImplementation) context.getSecurityContext(), documentPath, fileInputStream);
            return ThymeleafResponseService.visualiseUpdatedDocument(updatedDocument);
        } catch (Exception e) {
            logger.error("Error at RestDocController updateDocumentByPath.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error while updating document: %s. %s", documentPath, e.getMessage()));
        }
    }
    @PermitAll
    @DELETE
    @Path("/by-path/")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocumentByName(@Context ContainerRequestContext context,
                                         @FormDataParam("documentPath") String documentPath) {
        try {
            String deletedDocumentPath = docService.deleteDocument(
                    (SecurityContextImplementation) context.getSecurityContext(), documentPath);
            return ResponseService.okResponseSimple(String.format("Document: '%s' deleted successfully.", deletedDocumentPath));
        } catch (Exception e) {
            logger.error("Error at RestDocController deleteDocumentByName.", e);
            return ResponseService.errorResponse(Response.Status.NOT_FOUND,
                    String.format("Error occurred while deleting the document: %s. %s", documentPath, e.getMessage()));
        }
    }
}