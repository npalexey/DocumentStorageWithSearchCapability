package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.filters.SecurityContextImplementation;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocService;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;

@PermitAll
@Path("/doc")
public class RestDocController {

    private RestDocService docService = new RestDocService();
    private static final Logger logger = LoggerFactory.getLogger(RestDocController.class);

    @PermitAll
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showFilesInDoc(@Context ContainerRequestContext context) {
        SecurityContextImplementation securityContextImplementation = (SecurityContextImplementation) context.getSecurityContext();
        return docService.showFilesInDoc(securityContextImplementation);
    }

    @PermitAll
    @GET
    @Path("/{filename}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response showContentOfFile(@PathParam("filename") String filename) {
        return docService.showContentOfFile(filename);
    }

    @PermitAll
    @GET
    @Path("/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("filename") String filename) {
        return docService.downloadFile(filename);
    }

    @PermitAll
    @POST
    @Path("/{parentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormDataParam("file") InputStream fileInputStream,
                               //@FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @PathParam("parentid") String parentID) {
        return docService.uploadFile(fileInputStream, parentID);
    }

    @PermitAll
    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public Response searchInEveryFileWithStringQuery(@DefaultValue("") @QueryParam("query") String query, @Context ContainerRequestContext context) {
        SecurityContextImplementation securityContextImplementation = (SecurityContextImplementation) context.getSecurityContext();
        return docService.searchInEveryFileWithStringQuery(query, securityContextImplementation);
    }

    @PermitAll
    @PUT
    @Path("/{documentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocument(@PathParam("documentid") String docID,
                                   @FormDataParam("file") InputStream fileInputStream) {
        return docService.updateDocument(docID, fileInputStream);
    }

    @PermitAll
    @DELETE
    @Path("/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocument(@PathParam("documentid") String docID) {
        return docService.deleteDocument(docID);
    }
}
