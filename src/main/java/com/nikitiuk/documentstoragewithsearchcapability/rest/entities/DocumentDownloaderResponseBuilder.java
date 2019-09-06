package com.nikitiuk.documentstoragewithsearchcapability.rest.entities;

import javax.ws.rs.core.StreamingOutput;

public class DocumentDownloaderResponseBuilder {

    private StreamingOutput fileStream;
    private String documentName;

    public DocumentDownloaderResponseBuilder() {

    }

    public DocumentDownloaderResponseBuilder(StreamingOutput fileStream, String documentName) {
        this.fileStream = fileStream;
        this.documentName = documentName;
    }

    public StreamingOutput getFileStream() {
        return fileStream;
    }

    public void setFileStream(StreamingOutput fileStream) {
        this.fileStream = fileStream;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
}
