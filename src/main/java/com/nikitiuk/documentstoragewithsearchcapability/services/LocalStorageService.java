package com.nikitiuk.documentstoragewithsearchcapability.services;

import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStorageService {

    private static final String PATH = "/home/npalexey/workenv/DOWNLOADED/";

    public List<DocBean> listDocumentsInPath() throws IOException {
        List<DocBean> docBeanList = new ArrayList<>();
        List<String> collectedDocuments = collectDocumentsInPath();
        if (!collectedDocuments.isEmpty()) {
            for (String docPath : collectedDocuments) {
                docBeanList.add(new DocBean(docPath.substring(docPath.lastIndexOf("/") + 1), docPath));
            }
        }
        return docBeanList;
    }

    private List<String> collectDocumentsInPath() throws IOException {
        Stream<Path> walk = Files.walk(Paths.get(PATH));
        Set<String> allowedFormats = Stream.of("doc", "docx", "pdf", "txt", "html", "xml")
                .collect(Collectors.toCollection(HashSet::new));
        return walk.map(x -> x.toString())
                .filter(f -> allowedFormats.contains(FilenameUtils.getExtension(f)))
                .collect(Collectors.toList());
    }

    public List<String> documentContentGetter(String filename) throws IOException {
        if (filename.endsWith(".pdf")) {
            return pdfContentGetter(filename);
        } else {
            return otherTypesContentGetter(filename);
        }
    }

    private List<String> pdfContentGetter(String filename) throws IOException {
        List<String> docContent = new ArrayList<>();
        PDDocument document = PDDocument.load(new File(PATH + filename));
        if (!document.isEncrypted()) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            docContent.add(text
                    .replace(" ", "&nbsp;")
                    .replace("\n", "<br />"));
        }
        document.close();
        return docContent;
    }

    private List<String> otherTypesContentGetter(String filename) throws IOException {
        List<String> docContent = new ArrayList<>();
        Stream<String> stream = Files.lines(Paths.get(PATH + filename), StandardCharsets.UTF_8);
        stream.forEach(s -> docContent.add(s.replace(" ", "&nbsp;") + "<br />"));
        return docContent;
    }

    public StreamingOutput fileDownloader(String filename) throws Exception {
        return output -> {
            Path path = Paths.get(PATH + filename);
            byte[] data = Files.readAllBytes(path);
            output.write(data);
            output.flush();
        };
    }

    public void fileUploader(InputStream fileInputStream, String parentID) throws IOException {
        int read;
        byte[] bytes = new byte[1024];
        OutputStream out = new FileOutputStream(new File(PATH + parentID));//fileMetaData.getFileName()
        while ((read = fileInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }

    public String fileUpdater(InputStream fileInputStream, String docID) throws IOException {
        File tempFile = new File(PATH + docID);
        if (tempFile.exists()) {
            fileUploader(fileInputStream, docID);
        }
        return tempFile.getName();
    }

    public void fileDeleter(String docID) throws IOException {
        FileUtils.touch(new File(PATH + docID));
        File fileToDelete = FileUtils.getFile(PATH + docID);
        FileUtils.deleteQuietly(fileToDelete);
    }
}
