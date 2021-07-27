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

package org.nuxeo.labs;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
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
import static org.nuxeo.ecm.restapi.server.jaxrs.ContentAPI.CUSTOM_RESPONSE_HEADER;

@RunWith(FeaturesRunner.class)
@Features({RestServerFeature.class, TransactionalFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "org.nuxeo.labs.nuxeo-resource-rest-api-core"
})
public class TestContentAPI extends BaseTest {

    @Inject
    public CoreSession session;

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Test
    public void testNotFound() {
        ClientResponse response = getResponse(RequestType.GET, "/content/File/123?schemas=dublincore");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testFound() throws IOException {
        //create document
        DocumentModel file = session.createDocumentModel(session.getRootDocument().getPathAsString(),"File","File");
        file.setPropertyValue("dc:title","123");
        session.createDocument(file);
        transactionalFeature.nextTransaction();

        ClientResponse response = getResponse(RequestType.GET, "/content/File/123");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        //check custom header
        Assert.assertTrue(response.getHeaders().containsKey(CUSTOM_RESPONSE_HEADER));
        Assert.assertEquals("123",response.getHeaders().getFirst(CUSTOM_RESPONSE_HEADER));

        JsonNode node = mapper.readTree(response.getEntityInputStream());
        JsonNode properties = node.get("properties");
        Assert.assertTrue(properties.isObject());
    }

    @Test
    public void testFoundAndGetProperties() throws IOException {
        //create document
        DocumentModel file = session.createDocumentModel(session.getRootDocument().getPathAsString(),"File","File");
        file.setPropertyValue("dc:title","123");
        session.createDocument(file);
        transactionalFeature.nextTransaction();

        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("schemas","file");
        ClientResponse response = getResponse(RequestType.GET, "/content/File/123",queryParams);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        JsonNode node = mapper.readTree(response.getEntityInputStream());
        JsonNode properties = node.get("properties");
        Assert.assertTrue(properties.isObject());
        JsonNode content = properties.get("file:content");
        Assert.assertNotNull(content);
        Assert.assertTrue(content.isNull());
    }


}
