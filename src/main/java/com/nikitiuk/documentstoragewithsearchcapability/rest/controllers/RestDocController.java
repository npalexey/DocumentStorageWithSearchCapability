package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ResponseService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.ThymeleafResponseService;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.enums.Actions;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.enums.EntityTypes;
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
@Path("/docs")
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
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.DOCUMENT, docBeanList);
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
            return ThymeleafResponseService.visualiseEntitiesInStorage(EntityTypes.DOCUMENT, docBeanList);
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
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.DOCUMENT, uploadedDocument, Actions.UPLOADED);
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
            return ThymeleafResponseService.visualiseSingleEntity(EntityTypes.DOCUMENT, updatedDocument, Actions.UPDATED);
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
}