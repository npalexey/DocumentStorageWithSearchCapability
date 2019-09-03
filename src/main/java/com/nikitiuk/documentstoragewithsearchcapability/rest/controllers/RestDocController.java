package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocService;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@PermitAll
@Path("/doc")
public class RestDocController {

    private RestDocService docService = new RestDocService();

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDocuments(@Context ContainerRequestContext context) {
        SecurityContextImplementation securityContextImplementation = (SecurityContextImplementation) context.getSecurityContext();
        return docService.getDocuments(securityContextImplementation);
    }

    @PermitAll
    @GET
    @Path("{docid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentById(@PathParam("docid") long docId) {
        return docService.downloadDocumentById(docId);
    }

    @PermitAll
    @GET
    @Path("/by-path")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentByPath(@FormDataParam("documentPath") String documentPath) {
        return docService.downloadDocumentByPath(documentPath);
    }

    @PermitAll
    @GET
    @Path("/{docid}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response getContentOfDocumentById(@PathParam("docid") long docId) {
        return docService.getContentOfDocumentById(docId);
    }

    @PermitAll
    @GET
    @Path("/by-path/content")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response getContentOfDocumentByPath(@FormDataParam("documentPath") String documentPath) {
        return docService.getContentOfDocument(documentPath);
    }

    @PermitAll
    @POST
    @Path("/{parentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDocument(@FormDataParam("file") InputStream fileInputStream,
                               @FormDataParam("designatedName") String designatedName,
                               //@FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @PathParam("parentid") long parentFolderId) {
        return docService.uploadDocument(fileInputStream, designatedName, parentFolderId);
    }

    @PermitAll
    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public Response searchInEveryDocumentWithStringQuery(@DefaultValue("") @QueryParam("query") String query, @Context ContainerRequestContext context) {
        SecurityContextImplementation securityContextImplementation = (SecurityContextImplementation) context.getSecurityContext();
        return docService.searchInEveryDocumentWithStringQuery(query, securityContextImplementation);
    }

    @PermitAll
    @PUT
    @Path("/{docid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocumentById(@PathParam("docid") long docId,
                                   @FormDataParam("file") InputStream fileInputStream) {
        return docService.updateDocumentById(docId, fileInputStream);
    }

    @PermitAll
    @PUT
    @Path("/by-path/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocumentByPath(@FormDataParam("documentPath") String documentPath,
                                         @FormDataParam("file") InputStream fileInputStream) {
        return docService.updateDocumentByPath(documentPath, fileInputStream);
    }

    @PermitAll
    @DELETE
    @Path("/{docid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocumentById(@PathParam("docid") long docId) {
        return docService.deleteDocumentById(docId);
    }

    @PermitAll
    @DELETE
    @Path("/by-path/")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocumentByName(@FormDataParam("documentPath") String documentPath) {
        return docService.deleteDocument(documentPath);
    }
}