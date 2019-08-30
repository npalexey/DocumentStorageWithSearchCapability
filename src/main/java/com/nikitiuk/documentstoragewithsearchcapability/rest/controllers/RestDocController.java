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
    @Path("/get-by-name/{docname}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocumentByName(@PathParam("docname") String docName) {
        return docService.downloadDocument(docName);
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
    @Path("/get-by-name/{docname}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response getContentOfDocumentByName(@PathParam("docname") String docName) {
        return docService.getContentOfDocument(docName);
    }

    @PermitAll
    @POST
    @Path("/{designatedname}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadDocument(@FormDataParam("file") InputStream fileInputStream,
                               //@FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @PathParam("designatedname") String designatedName) {
        return docService.uploadDocument(fileInputStream, designatedName);
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
    @Path("/update-by-name/{docname}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocumentByName(@PathParam("docname") String docName,
                                   @FormDataParam("file") InputStream fileInputStream) {
        return docService.updateDocument(docName, fileInputStream);
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
    @Path("/delete-by-name/{docname}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocumentByName(@PathParam("docname") String docName) {
        return docService.deleteDocument(docName);
    }
}