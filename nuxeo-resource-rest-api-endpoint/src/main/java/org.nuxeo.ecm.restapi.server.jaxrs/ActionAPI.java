package org.nuxeo.ecm.restapi.server.jaxrs;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext;
import org.nuxeo.ecm.webengine.jaxrs.coreiodelegate.RenderingContextWebUtils;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import static org.nuxeo.ecm.core.io.registry.MarshallingConstants.EMBED_PROPERTIES;

@WebObject(type = "action")
public class ActionAPI extends DefaultObject {

    @GET
    @Path("/{operationName}")
    public Object doGetResource(@PathParam("operationName") String operationName,
								@QueryParam("documentId") String documentId,
                                @QueryParam("schemas") String schemas) {

        // set schema list in rendering context
        // The rendering context is the class that holds all the parameters for the response: schemas, enrichers ...
        RenderingContext renderingContext = RenderingContextWebUtils.getContext(getContext().getRequest());
        renderingContext.setParameterValues(EMBED_PROPERTIES, schemas);

        if(StringUtils.isEmpty(documentId) || StringUtils.isEmpty(operationName)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        CoreSession session = getContext().getCoreSession();
        DocumentModel document = session.getDocument(new IdRef(documentId));

        AutomationService automationService = Framework.getService(AutomationService.class);

        OperationContext automationCtx = new OperationContext(session);
        Map<String, Object> automationParams = new HashMap<>();
        automationCtx.setInput(document);

        //get all query params and inject in the automation context
        Map<String, String[]> allQueryParams = getContext().getRequest().getParameterMap();
        for (Map.Entry<String, String[]> entry: allQueryParams.entrySet()) {
            String[] values = entry.getValue();
            if (values.length > 0 ) {
                automationParams.put(entry.getKey(),values[0]);
            }
        }

        try {
            Object result = automationService.run(automationCtx, operationName, automationParams);
            session.save();
            return result;
        } catch (OperationException e) {
            Throwable cause = e.getCause();
            // in this case, the cause is a nashorn exception
            if (cause instanceof UndeclaredThrowableException) {
                Throwable t = ((UndeclaredThrowableException)cause).getUndeclaredThrowable();
                if (t instanceof InvocationTargetException) {
                    Throwable target = ((InvocationTargetException)t).getTargetException();
                    String message = target.getMessage();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).type("application/json").build();
                }
            }
            return e;
        }
    }
}
