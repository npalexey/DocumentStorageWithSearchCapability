package com.nikitiuk.documentstoragewithsearchcapability.services;

import com.nikitiuk.documentstoragewithsearchcapability.dao.DocDao;
import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.utils.ThymeleafUtil;
import org.apache.commons.io.FileUtils;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@Path("/doc")
public class RestService {

    private static final String PATH = "/home/npalexey/workenv/DOWNLOADED/";
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showFilesInDoc() {
        List<DocBean> docBeanList;
        try {
            docBeanList = new ArrayList<>(LocalStorageService.getDocsInPath());
        } catch (IOException e) {
            return ResponseService.errorResponse(404, "Error while producing list of content.");
        }
        final Context ctx = new Context();
        ctx.setVariable("inStorage", docBeanList);
        return ResponseService.okResponseForText("storagehome", ctx);
    }

    @GET
    @Path("/{filename}/content")
    @Produces(MediaType.TEXT_HTML)
    public Response showContentOfFile(@PathParam("filename") String filename) {
        List<String> docContent = new ArrayList<>();
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
                return ResponseService.errorResponse(404, "Error while getting content of " + filename + ". Please, try again");
            }
        } else {
            try (Stream<String> stream = Files.lines(Paths.get(PATH + filename), StandardCharsets.UTF_8)) {
                stream.forEach(s -> {
                    docContent.add(s.replace(" ", "&nbsp;") + "<br />");
                });
            } catch (IOException e) {
                return ResponseService.errorResponse(404, "Error while getting content of " + filename + ". Please, try again");
            }
        }
        final Context ctx = new Context();
        ctx.setVariable("docContent", docContent);
        ctx.setVariable("fileName", filename);
        return ResponseService.okResponseForText("content", ctx);
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
        return ResponseService.okResponseForFile(fileStream , filename);
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
                DocDao.saveDocument(new DocBean(parentid, PATH + parentid));
            } catch (IOException | SolrServerException e) {
                throw new WebApplicationException("Error while indexing file. Please, try again");
            }
        };
        executorService.execute(addTask);
        return ResponseService.okResponseSimple("Data uploaded successfully");
    }

    @POST
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public Response searchInEveryFileWithStringQuery(@DefaultValue("") @QueryParam("query") String query) {
        StringBuilder contentBuilder = new StringBuilder("Nothing was found");
        try {
            contentBuilder.append(SolrService.searchAndReturnDocsAndHighlightedText(query)).delete(0, 18);
        } catch (IOException | SolrServerException e) {
            return ResponseService.errorResponse(404, "Error while searching for: " + query + ". Please, try again");
        }
        final Context ctx = new Context();
        ctx.setVariable("searchResult", contentBuilder.toString().replace("\n", "<br/>"));
        return ResponseService.okResponseForText("search", ctx);
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
        return ResponseService.okResponseSimple("File updated successfully");
    }

    @DELETE
    @Path("/{documentid}")
    @Produces(MediaType.TEXT_HTML)
    public Response deleteDocument(@PathParam("documentid") String docID) throws Exception {
        FileUtils.touch(new File(PATH + docID));
        File fileToDelete = FileUtils.getFile(PATH + docID);
        boolean success = FileUtils.deleteQuietly(fileToDelete);
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
        if (success) {
            ctx.setVariable("code", "OK");
            ctx.setVariable("message", "File deleted successfully");
            return ResponseService.okResponseForText("info", ctx);
        }
        return ResponseService.errorResponse(404, "Error occurred while deleting the file " + docID);
    }
}