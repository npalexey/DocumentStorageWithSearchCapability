package com.nikitiuk.documentstoragewithsearchcapability.serverpart;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.solr.client.solrj.SolrServerException;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/doc")
public class DownloaderService {

    private static final String PATH = "/home/npalexey/workenv/DOWNLOADED/";

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response showFilesInDoc() {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<java.nio.file.Path> walk = Files.walk(Paths.get(PATH))) {

            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".doc") || f.endsWith(".pdf") || f.endsWith(".txt")).collect(Collectors.toList());

            //result.forEach(System.out::println);
            for (String document : result) {
                contentBuilder.append(document).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(contentBuilder.toString(), MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/{filename}/content")
    @Produces(MediaType.TEXT_PLAIN)
    public Response showContentOfFile(@PathParam("filename") String name) {
        StringBuilder contentBuilder = new StringBuilder();
        if (name.endsWith(".pdf")) {
            try {
                PDDocument document = PDDocument.load(new File(PATH + name));
                if (!document.isEncrypted()) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    contentBuilder.append(text);
                }
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (Stream<String> stream = Files.lines(Paths.get(PATH + name), StandardCharsets.UTF_8)) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return Response.ok(contentBuilder.toString(), MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("filename") String name) throws Exception {
        StreamingOutput fileStream = output -> {
            try {
                java.nio.file.Path path = Paths.get(PATH + name);
                byte[] data = Files.readAllBytes(path);
                output.write(data);
                output.flush();
            } catch (Exception e) {
                throw new WebApplicationException("File Not Found");
            }
        };
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = " + name)
                .build();
    }

    @POST
    @Path("/{parentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormDataParam("file") InputStream fileInputStream,
                               //@FormDataParam("file") FormDataContentDisposition fileMetaData,
                               @PathParam("parentid") String parentid) throws Exception {
        try {
            int read = 0;
            byte[] bytes = new byte[1024];

            OutputStream out = new FileOutputStream(new File(PATH + parentid));//fileMetaData.getFileName()
            while ((read = fileInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new WebApplicationException("Error while uploading file. Please try again");
        }

        SolrService.indexDocumentWithSolr(parentid, URLConnection.guessContentTypeFromName(new File(PATH + parentid).getName()));
        return Response.ok("Data uploaded successfully").build();
    }

    /*@POST
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response searchInEveryFile(String query) {
        StringBuilder contentBuilder;// = new StringBuilder("");
        try{
            contentBuilder = (SolrService.searchAndReturnDocsAndHighlightedText(query));
        } catch (IOException|SolrServerException e){
            throw new WebApplicationException("Error while searching. Please try again");
        }

        return Response.ok(contentBuilder.toString(), MediaType.TEXT_PLAIN).build();
    }*/

    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_PLAIN)
    public Response searchInEveryFileWithStringQuery(@DefaultValue("") @QueryParam("query") String query) {
        StringBuilder contentBuilder = new StringBuilder("");
        try{
            contentBuilder.append(SolrService.searchAndReturnDocsAndHighlightedText(query));
        } catch (IOException|SolrServerException e){
            throw new WebApplicationException("Error while searching. Please try again");
        }

        return Response.ok(contentBuilder.toString(), MediaType.TEXT_PLAIN).build();
    }

    @PUT
    @Path("/{documentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocument(@PathParam("documentid") String docID,
                                   @FormDataParam("file") InputStream fileInputStream) throws Exception {
        File tempFile = new File(PATH + docID);
        if (tempFile.exists()) {
            try {
                int read = 0;
                byte[] bytes = new byte[1024];

                OutputStream out = new FileOutputStream(new File(PATH + docID));//fileMetaData.getFileName()
                while ((read = fileInputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.flush();
                out.close();

            } catch (IOException e) {
                throw new WebApplicationException("Error while updating file. Please try again");
            }
        } else {
            return Response.noContent().build();
        }

        SolrService.indexDocumentWithSolr(docID, URLConnection.guessContentTypeFromName(tempFile.getName()));
        return Response.ok("File updated successfully").build();
    }

    @DELETE
    @Path("/{documentid}")
    public Response deleteDocument(@PathParam("documentid") String docID) throws Exception {
        FileUtils.touch(new File(PATH + docID));
        File fileToDelete = FileUtils.getFile(PATH + docID);
        boolean success = FileUtils.deleteQuietly(fileToDelete);

        SolrService.deletDocumentFromSolrIndex(docID);
        return Response.ok("File deleted successfully? " + success).build();
    }
}

