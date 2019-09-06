package com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers;

import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.enums.Actions;
import com.nikitiuk.documentstoragewithsearchcapability.rest.services.helpers.enums.EntityTypes;
import org.thymeleaf.context.Context;

import javax.ws.rs.core.Response;
import java.util.List;

public class ThymeleafResponseService {

    public static Response visualiseEntitiesInStorage(EntityTypes entityType, List<?> entityList) {
        final Context ctx = new Context();
        ctx.setVariable("entityType", entityType);
        ctx.setVariable("inStorage", entityList);
        return ResponseService.okResponseWithContext("storagehome", ctx);
    }

    public static Response visualiseSingleEntity(EntityTypes entityType, Object entity, Actions action) {
        final Context ctx = new Context();
        ctx.setVariable("entityType", entityType);
        ctx.setVariable("entity", entity);
        ctx.setVariable("action", action);
        if(action == Actions.CREATED || action == Actions.UPLOADED) {
            return ResponseService.createdResponse("singleentity", ctx);
        }
        return ResponseService.okResponseWithContext("singleentity", ctx);
    }

    public static Response visualiseDocumentContent(List<String> documentContent) {
        final Context ctx = new Context();
        ctx.setVariable("docContent", documentContent);
        ctx.setVariable("filePath", documentContent.get(0));
        return ResponseService.okResponseWithContext("content", ctx);
    }

    public static Response visualiseSearchResult(String searchResult) {
        final Context ctx = new Context();
        ctx.setVariable("searchResult", searchResult);
        return ResponseService.okResponseWithContext("search", ctx);
    }
}