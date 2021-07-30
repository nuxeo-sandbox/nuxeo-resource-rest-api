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

package org.nuxeo.ecm.restapi.server.jaxrs.batch;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.server.jaxrs.batch.Batch;
import org.nuxeo.ecm.automation.server.jaxrs.batch.BatchFileEntry;
import org.nuxeo.ecm.automation.server.jaxrs.batch.handler.BatchFileInfo;
import org.nuxeo.ecm.automation.server.jaxrs.batch.handler.impl.DefaultBatchHandler;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;

import java.io.IOException;

public class Base64BatchHandler extends DefaultBatchHandler {

    private static final Log log = LogFactory.getLog(Base64BatchHandler.class);

    @Override
    public boolean completeUpload(String batchId, String fileIndex, BatchFileInfo fileInfo) {
        Batch batch = getBatch(batchId);
        BatchFileEntry fileEntry = batch.getFileEntry(fileIndex, true);
        try (Base64InputStream b64Stream = new Base64InputStream(fileEntry.getBlob().getStream())) {
            Blob decodedBlob = Blobs.createBlob(b64Stream, fileInfo.getMimeType());
            decodedBlob.setMimeType(fileInfo.getMimeType());
            decodedBlob.setFilename(fileInfo.getFilename());
            batch.addFile(fileIndex, decodedBlob, fileInfo.getFilename(), fileInfo.getMimeType());
            return true;
        } catch (IOException e) {
            log.error(e);
            return false;
        }
    }
}
