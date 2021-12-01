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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentNotFoundException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(FeaturesRunner.class)
@Features({RestServerFeature.class, TransactionalFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "nuxeo-resource-rest-api-endpoint"
})
public class TestActionAPI extends BaseTest {

    @Inject
    public CoreSession session;

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Test
    public void testCreateVersion() throws IOException {
        //create document
        DocumentModel file = session.createDocumentModel(session.getRootDocument().getPathAsString(),"File","File");
        file.setPropertyValue("dc:title","123");
        file = session.createDocument(file);
        Assert.assertTrue(file.isCheckedOut());

        // commit the transaction and record the change in the DB
        transactionalFeature.nextTransaction();

        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("documentId",file.getId());
        queryParams.add("increment","Major");
        ClientResponse response = getResponse(RequestType.GET, "/action/Document.CreateVersion",queryParams);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        JsonNode node = mapper.readTree(response.getEntityInputStream());
        JsonNode entityType = node.get("entity-type");
        Assert.assertEquals("document",entityType.asText());

        //Get Version
        DocumentModelList versions = session.query(
                String.format(
                        "Select * From Document Where ecm:versionVersionableId = '%s' AND ecm:isVersion = 1", file.getId()));
        Assert.assertEquals(1,versions.size());
    }

    @Test(expected = DocumentNotFoundException.class)
    @Deploy("nuxeo-resource-rest-api-endpoint:test-exception-automation-script.xml")
    public void testCatchExceptionMessage() throws IOException {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("documentId",session.getRootDocument().getId());
        ClientResponse response = getResponse(RequestType.GET, "/action/javascript.test_exception",queryParams);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        JsonNode node = mapper.readTree(response.getEntityInputStream());
        JsonNode entityType = node.get("messages");
        Assert.assertTrue(entityType.isArray());
        Assert.assertEquals("There was an error",entityType.get(0).asText());
        session.getDocument(new PathRef(session.getRootDocument().getPathAsString()+"TheDOC"));
    }

}
