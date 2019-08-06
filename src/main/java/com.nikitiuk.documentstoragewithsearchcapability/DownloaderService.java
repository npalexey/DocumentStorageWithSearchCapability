package com.nikitiuk.documentstoragewithsearchcapability;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/doc")
public class DownloaderService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response showFilesInDoc() {
        String UPLOAD_PATH = "/home/npalexey/workenv/DOWNLOADED/";
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<java.nio.file.Path> walk = Files.walk(Paths.get(UPLOAD_PATH))) {

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
        String UPLOAD_PATH = "/home/npalexey/workenv/DOWNLOADED/";
        StringBuilder contentBuilder = new StringBuilder();
        if (name.endsWith(".pdf")) {
            try {
                PDDocument document = PDDocument.load(new File(UPLOAD_PATH + name));
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
            try (Stream<String> stream = Files.lines(Paths.get(UPLOAD_PATH + name), StandardCharsets.UTF_8)) {
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
        String UPLOAD_PATH = "/home/npalexey/workenv/DOWNLOADED/";
        StreamingOutput fileStream = output -> {
            try {
                java.nio.file.Path path = Paths.get(UPLOAD_PATH + name);
                byte[] data = Files.readAllBytes(path);
                output.write(data);
                output.flush();
            } catch (Exception e) {
                throw new WebApplicationException("File Not Found !!");
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
    public Response uploadPdfFile(@FormDataParam("file") InputStream fileInputStream,
                                  //@FormDataParam("file") FormDataContentDisposition fileMetaData,
                                  @PathParam("parentid") String parentid) throws Exception {
        String UPLOAD_PATH = "/home/npalexey/workenv/DOWNLOADED/";
        try {
            int read = 0;
            byte[] bytes = new byte[1024];

            OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + parentid));//fileMetaData.getFileName()
            while ((read = fileInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new WebApplicationException("Error while uploading file. Please try again");
        }
        return Response.ok("Data uploaded successfully").build();
    }

    @PUT
    @Path("/{documentid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateDocument(@PathParam("documentid") String docID,
                                   @FormDataParam("file") InputStream fileInputStream) throws Exception {
        String UPLOAD_PATH = "/home/npalexey/workenv/DOWNLOADED/";
        File tempFile = new File(UPLOAD_PATH + docID);
        if (tempFile.exists()) {
            try {
                int read = 0;
                byte[] bytes = new byte[1024];

                OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + docID));//fileMetaData.getFileName()
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

        return Response.ok("File updated successfully").build();
    }
}

