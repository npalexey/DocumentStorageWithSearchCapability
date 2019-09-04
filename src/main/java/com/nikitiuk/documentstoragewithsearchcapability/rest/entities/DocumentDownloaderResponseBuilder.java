package com.nikitiuk.documentstoragewithsearchcapability.rest.entities;

import javax.ws.rs.core.StreamingOutput;

public class DocumentDownloaderResponseBuilder {

    private StreamingOutput fileStream;
    private String documentPath;

    public DocumentDownloaderResponseBuilder() {

    }

    public DocumentDownloaderResponseBuilder(StreamingOutput fileStream, String documentPath) {
        this.fileStream = fileStream;
        this.documentPath = documentPath;
    }

    public StreamingOutput getFileStream() {
        return fileStream;
    }

    public void setFileStream(StreamingOutput fileStream) {
        this.fileStream = fileStream;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }
}
