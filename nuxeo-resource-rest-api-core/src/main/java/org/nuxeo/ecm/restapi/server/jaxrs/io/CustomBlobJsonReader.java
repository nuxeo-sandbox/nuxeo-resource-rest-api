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

package org.nuxeo.ecm.restapi.server.jaxrs.io;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Base64;
import org.nuxeo.ecm.automation.core.io.BlobJsonReader;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.ByteArrayBlob;
import org.nuxeo.ecm.core.io.registry.reflect.Setup;

import java.io.IOException;

import static org.nuxeo.ecm.core.io.registry.reflect.Instantiations.SINGLETON;
import static org.nuxeo.ecm.core.io.registry.reflect.Priorities.REFERENCE;

@Setup(mode = SINGLETON, priority = REFERENCE)
public class CustomBlobJsonReader extends BlobJsonReader {

    @Override
    public Blob read(JsonNode jn) throws IOException {
        if (jn.isObject() && jn.has("base64")) {
            String base64str = jn.get("base64").textValue();
            String filename = jn.get("filename").textValue();
            String mimeType = jn.get("mimetype").textValue();
            byte[] decodedBinary = Base64.decodeBase64(base64str);
            Blob blob = new ByteArrayBlob(decodedBinary,mimeType);
            blob.setFilename(filename);
            return blob;
        } else {
            return super.read(jn);
        }
    }

}
