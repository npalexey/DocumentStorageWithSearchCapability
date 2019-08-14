package com.nikitiuk.documentstoragewithsearchcapability.services;

import com.nikitiuk.documentstoragewithsearchcapability.utils.ThymeleafUtil;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

public class ResponseService {

    public static Response errorResponse(int code, String message) {
        final Context ctx = new Context();
        ctx.setVariable("code", code);
        ctx.setVariable("message", message);
        return Response.status(code).entity(new ThymeleafUtil().getEngine().process("info", ctx)).build();
    }

    public static Response okResponseForText(String template, Context ctx) {
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
