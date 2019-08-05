package com.nikitiuk.documentstoragewithsearchcapability;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
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
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<java.nio.file.Path> walk = Files.walk(Paths.get("/home/npalexey/workenv/DOWNLOADED"))) {

            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".doc") || f.endsWith(".pdf") || f.endsWith(".txt")).collect(Collectors.toList());

            //result.forEach(System.out::println);
            for (String document : result) {
                contentBuilder.append(document).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response
                .ok(contentBuilder.toString(), MediaType.TEXT_PLAIN)
                .build();
    }

    @GET
    @Path("/{filename}/content")
    @Produces(MediaType.TEXT_PLAIN)
    public Response showContentOfFile(@PathParam("filename") String name) {
        StringBuilder contentBuilder = new StringBuilder();
        if (name.endsWith(".pdf")) {
            try {
                PDDocument document = PDDocument.load(new File("/home/npalexey/Downloads/" + name));
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
            try (Stream<String> stream = Files.lines(Paths.get("/home/npalexey/Downloads/" + name), StandardCharsets.UTF_8)) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*StreamingOutput fileStream = output -> {
            try
            {
                java.nio.file.Path path = Paths.get("/home/npalexey/Downloads/" + name);
                BufferedReader reader =
                        Files.newBufferedReader(Paths.get("/home/npalexey/Downloads/" + name));
                byte[] data = Files.readAllBytes(reader);
                output.write(data);
                output.flush();
            }
            catch (Exception e)
            {
                throw new WebApplicationException("File Not Found !!");
            }
        };*/
        return Response
                .ok(contentBuilder.toString(), MediaType.TEXT_PLAIN)
                .build();
    }

    @GET
    @Path("/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("filename") String name) {
        StreamingOutput fileStream = output -> {
            try {
                java.nio.file.Path path = Paths.get("/home/npalexey/Downloads/" + name);
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
}

