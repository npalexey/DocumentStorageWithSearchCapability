package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.rest.entities.DocumentDownloaderResponseBuilder;
import com.nikitiuk.documentstoragewithsearchcapability.utils.ThymeleafUtil;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ResponseService {

    public static Response errorResponse(Status status, String message) {
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

    public static Response noContentResponseSimple() {
        return Response.noContent().build();
    }

    public static Response createdResponse(String template, Context ctx) {
        return Response.status(Status.CREATED).entity(new ThymeleafUtil().getEngine().process(template, ctx)).build();
    }

    public static Response okResponseForFile(DocumentDownloaderResponseBuilder documentDownloaderResponseBuilder) throws Exception {
        String fileName = documentDownloaderResponseBuilder.getDocumentName();
        String mimeType = new Tika().detect(fileName);
        return Response
                .ok(documentDownloaderResponseBuilder.getFileStream(), mimeType/*MediaType.APPLICATION_OCTET_STREAM*/)
                .header("content-disposition", String.format("attachment; filename = %s", fileName))
                .build();
    }
}