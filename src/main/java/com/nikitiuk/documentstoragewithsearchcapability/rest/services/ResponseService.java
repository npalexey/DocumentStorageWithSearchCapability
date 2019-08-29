package com.nikitiuk.documentstoragewithsearchcapability.rest.services;

import com.nikitiuk.documentstoragewithsearchcapability.utils.ThymeleafUtil;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

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

    public static Response okResponseForFile(StreamingOutput fileStream, String filename) {
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = " + filename)
                .build();
    }
}
