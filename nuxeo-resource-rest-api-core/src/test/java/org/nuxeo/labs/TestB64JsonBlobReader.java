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

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(FeaturesRunner.class)
@Features({RestServerFeature.class, TransactionalFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "org.nuxeo.labs.nuxeo-resource-rest-api-core"
})
public class TestB64JsonBlobReader extends BaseTest {

    @Inject
    public CoreSession session;

    @Test
    public void testCreateDocumentFound() throws IOException {
        String data = new String(
                getClass().getResourceAsStream("/files/document_with_b64_blob.json").readAllBytes(),
                StandardCharsets.UTF_8);
        ClientResponse response = getResponse(
                RequestType.POST,
                "/path"+session.getRootDocument().getPathAsString(),
                data);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        DocumentModel doc = session.getDocument(new PathRef(session.getRootDocument().getPathAsString()+"newDoc"));
        assertNotNull(doc);
        Blob blob = (Blob) doc.getPropertyValue("file:content");
        assertEquals("text/plain",blob.getMimeType());
        assertEquals("sample.txt",blob.getFilename());
        assertEquals("This is a test",blob.getString());
    }

}
