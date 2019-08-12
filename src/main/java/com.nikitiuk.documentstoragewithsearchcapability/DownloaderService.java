package com.nikitiuk.documentstoragewithsearchcapability;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.solr.client.solrj.SolrServerException;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.thymeleaf.context.Context;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/doc")
public class DownloaderService {

    private static final String PATH = "/home/npalexey/workenv/DOWNLOADED/";
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static ThymeleafService thymeleafService = new ThymeleafService();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showFilesInDoc() {
        List<String> docList = new ArrayList<>();
        final Context ctx = new Context();
        docList.add("Files in storage:");
        try (Stream<java.nio.file.Path> walk = Files.walk(Paths.get(PATH))) {
            Set<String> allowedFormats = Stream.of("doc", "docx", "pdf", "txt", "html", "xml")
                    .collect(Collectors.toCollection(HashSet::new));
            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> allowedFormats.contains(FilenameUtils.getExtension(f)))
                    .collect(Collectors.toList());
            if (result.isEmpty()) {
                docList.add("No files in storage.");
            } else {
                docList.addAll(result);
            }
        } catch (IOException e) {
            ctx.setVariable("code", "NOT FOUND");
            ctx.setVariable("message", "Error while producing list of content.");
            return Response.status(404).entity(thymeleafService.getEngine().process("info", ctx)).build();
        }
        ctx.setVariable("inStorage", docList);

        return Response.ok(thymeleafService.getEngine().process("storagehome", ctx)).build();
    }

    @GET
    @Path("/{filename}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response showContentOfFile(@PathParam("filename") String filename) {
        List<String> docContent = new ArrayList<>();
        final Context ctx = new Context();
        if (filename.endsWith(".pdf")) {
            try {
                PDDocument document = PDDocument.load(new File(PATH + filename));
                if (!document.isEncrypted()) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    String text = stripper.getText(document);
                    docContent.add(text
                            .replace(" ", "&nbsp;")
                            .replace("\n", "<br />"));
                }
                document.close();
            } catch (IOException e) {
                ctx.setVariable("code", "NOT FOUND");
                ctx.setVariable("message", "Error while getting content of " + filename + ". Please, try again");
                return Response.status(404).entity(thymeleafService.getEngine().process("info", ctx)).build();
            }
        } else {
            try (Stream<String> stream = Files.lines(Paths.get(PATH + filename), StandardCharsets.UTF_8)) {
                stream.forEach(s -> {
                    docContent.add(s.replace(" ", "&nbsp;") + "<br />");
                });
            } catch (IOException e) {
                ctx.setVariable("code", "NOT FOUND");
                ctx.setVariable("message", "Error while getting content of " + filename + ". Please, try again");
                return Response.status(404).entity(thymeleafService.getEngine().process("info", ctx)).build();
            }
        }
        ctx.setVariable("docContent", docContent);
        ctx.setVariable("fileName", filename);
        return Response.ok(thymeleafService.getEngine().process("content", ctx)).build();
    }

    @GET
    @Path("/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("filename") String filename) throws Exception {
        StreamingOutput fileStream = output -> {
            try {
                java.nio.file.Path path = Paths.get(PATH + filename);
                byte[] data = Files.readAllBytes(path);
                output.write(data);
                output.flush();
            } catch (Exception e) {
                throw new WebApplicationException("File Not Found");
            }
        };
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = " + filename)
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
            throw new WebApplicationException("Error while uploading file. Please, try again");
        }
        Runnable addTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(parentid, URLConnection.guessContentTypeFromName(new File(PATH + parentid).getName()));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(addTask);
        return Response.ok("Data uploaded successfully").build();
    }

    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public Response searchInEveryFileWithStringQuery(@DefaultValue("") @QueryParam("query") String query) {
        StringBuilder contentBuilder = new StringBuilder("Nothing was found");
        final Context ctx = new Context();
        try {
            contentBuilder.append(SolrService.searchAndReturnDocsAndHighlightedText(query)).delete(0, 18);
        } catch (IOException | SolrServerException e) {
            ctx.setVariable("code", "NOT FOUND");
            ctx.setVariable("message", "Error while searching for: " + query + ". Please, try again");
            return Response.status(404).entity(thymeleafService.getEngine().process("info", ctx)).build();
        }
        ctx.setVariable("searchResult", contentBuilder.toString().replace("\n", "<br/>"));
        return Response.ok(thymeleafService.getEngine().process("search", ctx)).build();
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
                OutputStream out = new FileOutputStream(new File(PATH + docID));
                while ((read = fileInputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new WebApplicationException("Error while updating file. Please, try again");
            }
        } else {
            return Response.noContent().build();
        }
        Runnable putTask = () -> {
            try {
                SolrService.indexDocumentWithSolr(docID, URLConnection.guessContentTypeFromName(tempFile.getName()));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(putTask);
        return Response.ok("File updated successfully").build();
    }

    @DELETE
    @Path("/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocument(@PathParam("documentid") String docID) throws Exception {
        FileUtils.touch(new File(PATH + docID));
        File fileToDelete = FileUtils.getFile(PATH + docID);
        boolean success = FileUtils.deleteQuietly(fileToDelete);
        final Context ctx = new Context();
        Runnable deleteTask = () -> {
            try {
                SolrService.deleteDocumentFromSolrIndex(docID);
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(deleteTask);
        if (success) {
            ctx.setVariable("code", "OK");
            ctx.setVariable("message", "File deleted successfully");
            return Response.ok(thymeleafService.getEngine().process("info", ctx)).build();
        }
        ctx.setVariable("code", "NOT FOUND");
        ctx.setVariable("message", "Error occurred while deleting the file " + docID);
        return Response.status(404).entity(thymeleafService.getEngine().process("info", ctx)).build();
    }
}