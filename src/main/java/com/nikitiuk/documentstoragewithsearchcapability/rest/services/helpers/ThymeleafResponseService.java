package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.entities.DocBean;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;

public class ThymeleafResponseService {

    public static Response visualiseDocumentsInStorage(List<DocBean> docBeanList) {
        final Context ctx = new Context();
        ctx.setVariable("entityName", "Document");
        ctx.setVariable("inStorage", docBeanList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public static Response visualiseDocumentContent(List<String> documentContent) {
        final Context ctx = new Context();
        ctx.setVariable("docContent", documentContent);
        ctx.setVariable("filePath", documentContent.get(0));
        return ResponseService.okResponseWithContext("content", ctx);
    }

    public static Response visualiseUploadedDocument(DocBean uploadedDocument) {
        final Context ctx = new Context();
        ctx.setVariable("entityType", "Document");
        ctx.setVariable("entity", uploadedDocument);
        ctx.setVariable("action", "uploaded");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public static Response visualiseSearchResult(String searchResult) {
        final Context ctx = new Context();
        ctx.setVariable("searchResult", searchResult);
        return ResponseService.okResponseWithContext("search", ctx);
    }

    public static Response visualiseUpdatedDocument(DocBean updatedDocument) {
        final Context ctx = new Context();
        ctx.setVariable("entityType", "Document");
        ctx.setVariable("entity", updatedDocument);
        ctx.setVariable("action", "updated");
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }
}