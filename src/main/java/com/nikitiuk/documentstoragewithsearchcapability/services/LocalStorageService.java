package com.nikitiuk.documentstoragewithsearchcapability.services;

import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import com.nikitiuk.documentstoragewithsearchcapability.entities.FolderBean;
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
import java.util.*;
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

    public List<FolderBean> listFoldersInPath() throws IOException {
        List<FolderBean> folderBeanList = new ArrayList<>();
        List<String> collectedFolders = collectFoldersInPath();
        if (!collectedFolders.isEmpty()) {
            for (String folderPath : collectedFolders) {
                folderBeanList.add(new FolderBean(folderPath + "/"));
            }
        }
        return folderBeanList;
    }

    private List<String> collectFoldersInPath() throws IOException {
        Stream<Path> walk = Files.walk(Paths.get(PATH));
        return walk.filter(Files::isDirectory)
                .map(x -> x.toString())
                .collect(Collectors.toList());
    }

    private List<String> collectDocumentsInPath() throws IOException {
        Stream<Path> walk = Files.walk(Paths.get(PATH));
        Set<String> allowedFormats = Stream.of("doc", "docx", "pdf", "txt", "html", "xml")
                .collect(Collectors.toCollection(HashSet::new));
        return walk.filter(Files::isRegularFile)
                .map(x -> x.toString())
                .filter(f -> allowedFormats.contains(FilenameUtils.getExtension(f)))
                .collect(Collectors.toList());
    }

    public List<String> documentContentGetter(String filePath) throws IOException {
        if (filePath.endsWith(".pdf")) {
            return pdfContentGetter(filePath);
        } else {
            return otherTypesContentGetter(filePath);
        }
    }

    private List<String> pdfContentGetter(String filePath) throws IOException {
        List<String> docContent = new ArrayList<>();
        PDDocument document = PDDocument.load(new File(filePath));
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

    private List<String> otherTypesContentGetter(String filePath) throws IOException {
        List<String> docContent = new ArrayList<>();
        Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8);
        stream.forEach(s -> docContent.add(s.replace(" ", "&nbsp;") + "<br />"));
        return docContent;
    }

    public StreamingOutput fileDownloader(String filePath) throws Exception {
        return output -> {
            Path path = Paths.get(filePath);
            byte[] data = Files.readAllBytes(path);
            output.write(data);
            output.flush();
        };
    }

    public void fileUploader(InputStream fileInputStream, String documentPath) throws IOException {
        int read;
        byte[] bytes = new byte[1024];
        OutputStream out = new FileOutputStream(new File(documentPath));
        while ((read = fileInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }

    public String fileUpdater(InputStream fileInputStream, String documentPath) throws IOException {
        File tempFile = new File(documentPath);
        if (tempFile.exists()) {
            fileUploader(fileInputStream, documentPath);
        }
        return tempFile.getName();
    }

    public void fileDeleter(String documentPath) throws IOException {
        FileUtils.touch(new File(documentPath));
        File fileToDelete = FileUtils.getFile(documentPath);
        FileUtils.deleteQuietly(fileToDelete);
    }
}
