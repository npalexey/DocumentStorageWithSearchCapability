package com.nikitiuk.documentstoragewithsearchcapability.rest.controllers;

import com.nikitiuk.documentstoragewithsearchcapability.rest.services.RestDocService;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
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
    public Response showFilesInDoc() {
        return docService.showFilesInDoc();
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
    public Response searchInEveryFileWithStringQuery(@DefaultValue("") @QueryParam("query") String query) {
        return docService.searchInEveryFileWithStringQuery(query);
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
