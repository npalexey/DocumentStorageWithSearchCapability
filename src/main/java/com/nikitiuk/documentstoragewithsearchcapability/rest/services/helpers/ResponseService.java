package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithsearchcapability.utils.ThymeleafUtil;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ResponseService {

    public static Response errorResponse(Response.Status status, String message) {
        final Context ctx = new Context();
        ctx.setVariable("status", status.getReasonPhrase());
        ctx.setVariable("message", message);
        return Response.status(status).entity(new ThymeleafUtil().getEngine().process("info", ctx)).build();
    }

    public static Response okResponseWithContext(String template, Context ctx) {
        return Response.ok(new ThymeleafUtil().getEngine().process(template, ctx)).build();
    }

    public static Response okResponseSimple(String info) {
        return Response.ok(info).build();
    }

    public static Response okResponseForFile(DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder) {
        return Response
                .ok(documentDownloaderResponseBuilder.getFileStream(), MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = " + documentDownloaderResponseBuilder.getDocumentPath()
                        .substring(documentDownloaderResponseBuilder.getDocumentPath().lastIndexOf("/") + 1))
                .build();
    }
}
