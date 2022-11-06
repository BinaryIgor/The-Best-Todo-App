package com.igor101.thebesttodoapp.application;

import com.igor101.thebesttodoapp.core.TheBestTodoAppException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpFunctions {

    private static final Logger LOG = LoggerFactory.getLogger(HttpFunctions.class);

    public static void writeJsonResponse(Context context, Object response, int responseCode) {
        var jsonResponse = JsonMapper.toJson(response);

        context.status(responseCode)
                .result(jsonResponse)
                .header("content-type", "application/json");
    }

    public static <T> T jsonFromBody(Context context, Class<T> type) {
        try {
            return JsonMapper.toObject(context.body(), type);
        } catch (Exception e) {
            LOG.warn("Problem while parsing json body...", e);
            throw new TheBestTodoAppException(ApiErrors.INVALID_BODY);
        }
    }

    public static <T> T pathParam(Context context, String param, Class<T> type) {
        try {
            return context.pathParamAsClass(param, type).get();
        } catch (Exception e) {
            throw new TheBestTodoAppException(ApiErrors.INVALID_PATH_PARAM);
        }
    }

    public static <T> T queryParam(Context context, String param, Class<T> type, T defaultValue) {
        try {
            return context.queryParamAsClass(param, type).getOrDefault(defaultValue);
        } catch (Exception e) {
            throw new TheBestTodoAppException(ApiErrors.INVALID_QUERY_PARAM);
        }
    }
}
