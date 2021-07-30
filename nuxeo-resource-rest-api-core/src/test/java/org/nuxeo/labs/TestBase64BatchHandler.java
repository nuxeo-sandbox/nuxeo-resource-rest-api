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
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.server.jaxrs.batch.BatchManager;
import org.nuxeo.ecm.core.api.Blob;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(FeaturesRunner.class)
@Features({RestServerFeature.class, TransactionalFeature.class})
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({
        "org.nuxeo.labs.nuxeo-resource-rest-api-core"
})
public class TestBase64BatchHandler extends BaseTest {

    @Inject
    protected BatchManager batchManager;

    @Test
    public void testUploadBase64() throws IOException {
        ClientResponse batchResponse = getResponse(RequestType.POST, "/upload/new/base64");
        assertEquals(Response.Status.OK.getStatusCode(), batchResponse.getStatus());
        JsonNode node = mapper.readTree(batchResponse.getEntityInputStream());
        String batchId = node.get("batchId").asText();
        Assert.assertNotNull(batchId);

        String content = "Content to upload";
        Base64 base64 = new Base64();
        String encodedString = new String(base64.encode(content.getBytes()));

        Map<String, String> headers  =new HashMap<>();
        headers.put("X-File-Name","test.text");
        headers.put("X-File-Type","text/plain");
        headers.put("Content-Type","application/octet-stream");

        String fileIndex = "1";
        ClientResponse uploadResponse = getResponse(RequestType.POST, String.format("/upload/%s/%s",batchId,fileIndex),encodedString);
        assertEquals(Response.Status.CREATED.getStatusCode(),uploadResponse.getStatus());

        Assert.assertEquals(encodedString,batchManager.getBatch(batchId).getBlob(fileIndex).getString());

        ClientResponse completeResponse = getResponse(RequestType.POST, String.format("/upload/%s/%s/complete",batchId,fileIndex));
        assertEquals(Response.Status.OK.getStatusCode(),completeResponse.getStatus());

        Blob blob = batchManager.getBatch(batchId).getBlob(fileIndex);
        Assert.assertEquals(content,blob.getString());
    }
}