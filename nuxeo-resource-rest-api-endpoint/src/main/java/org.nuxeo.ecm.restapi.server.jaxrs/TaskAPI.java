/*
 * (C) Copyright 2021 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 */

package org.nuxeo.ecm.restapi.server.jaxrs;

import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.routing.api.exception.DocumentRouteException;
import org.nuxeo.ecm.platform.routing.core.io.TaskCompletionRequest;
import org.nuxeo.ecm.restapi.server.jaxrs.routing.TaskObject;
import org.nuxeo.ecm.webengine.model.WebObject;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

@WebObject(type = "task")
@Produces(MediaType.APPLICATION_JSON)
public class TaskAPI extends TaskObject {

    @Override
    @PUT
    @Path("{taskId}/{taskAction}")
    public Response completeTask(@PathParam("taskId") String taskId, @PathParam("taskAction") String action,
                                 TaskCompletionRequest taskCompletionRequest) {
        try {
            return super.completeTask(taskId,action,taskCompletionRequest);
        } catch (DocumentRouteException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OperationException) {
                Throwable automationCause = cause.getCause();
                if (automationCause instanceof UndeclaredThrowableException) {
                    Throwable t = ((UndeclaredThrowableException)automationCause).getUndeclaredThrowable();
                    if (t instanceof InvocationTargetException) {
                        Throwable target = ((InvocationTargetException)t).getTargetException();
                        String message = target.getMessage();
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).type("application/json").build();
                    }
                }
            }
            throw new NuxeoException(e);
        }
    }

}
