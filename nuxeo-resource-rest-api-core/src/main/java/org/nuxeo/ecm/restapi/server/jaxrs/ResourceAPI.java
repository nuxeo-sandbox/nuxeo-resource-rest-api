package org.nuxeo.ecm.restapi.server.jaxrs;

import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentNotFoundException;
import org.nuxeo.ecm.core.io.registry.context.RenderingContext;
import org.nuxeo.ecm.webengine.jaxrs.coreiodelegate.RenderingContextWebUtils;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static org.nuxeo.ecm.core.io.registry.MarshallingConstants.EMBED_PROPERTIES;

@WebObject(type = "resource")
public class ResourceAPI extends DefaultObject {

    @GET
    @Path("/{resourceName}/{resourceID}")
    public Object doGetResource(@PathParam("resourceName") String resourceName,
								@PathParam("resourceID") String resourceId,
								@QueryParam("schemas") String schemas) throws DocumentNotFoundException {
        // set schema list in rendering context
        // The rendering context is the class that holds all the parameters for the response: schemas, enrichers ...
        RenderingContext renderingContext = RenderingContextWebUtils.getContext(getContext().getRequest());
        renderingContext.setParameterValues(EMBED_PROPERTIES, schemas);

        DocumentModelList list = getContext().getCoreSession().query(
        		String.format("Select * From Document Where ecm:primaryType = '%s' AND dc:title='%s'", resourceName, resourceId));

        if (list.size() == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return list.get(0);
        }
    }
}
