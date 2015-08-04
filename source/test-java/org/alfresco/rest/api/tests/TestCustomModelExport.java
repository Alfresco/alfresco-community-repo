package org.alfresco.rest.api.tests;
/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.alfresco.repo.dictionary.CMMDownloadTestUtil;
import org.alfresco.rest.api.model.CustomModelDownload;
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.api.model.CustomModel.ModelStatus;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.junit.Test;

/**
 * Tests REST API download of the {@link CustomModelService}.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class TestCustomModelExport extends BaseCustomModelApiTest
{

    private static final long PAUSE_TIME = 1000;

    private CMMDownloadTestUtil downloadTestUtil;

    @Override
    public void setup() throws Exception
    {
        super.setup();
        this.downloadTestUtil = new CMMDownloadTestUtil(applicationContext);
    }

    @Override
    public void tearDown() throws Exception
    {
        this.downloadTestUtil.cleanup();
        super.tearDown();
    }

    @Test
    public void testCreateDownload() throws Exception
    {
        final String modelName = "testModel" + System.currentTimeMillis();
        final String modelExportFileName = modelName + ".xml";
        final String shareExtExportFileName = "CMM_" + modelName + "_module.xml";

        Pair<String, String> namespacePair = getTestNamespaceUriPrefixPair();
        // Create the model as a Model Administrator
        createCustomModel(modelName, namespacePair, ModelStatus.DRAFT, null, "Mark Moe");

        // Add type
        String typeBaseName = "testTypeBase" + System.currentTimeMillis();
        createTypeAspect(CustomType.class, modelName, typeBaseName, "test typeBase title", "test typeBase Desc", "cm:content");

        // Create Share extension module
        downloadTestUtil.createShareExtModule(modelName);

        // Try to create download the model as a non Admin user
        post("cmm/" + modelName + "/download", nonAdminUserName, RestApiUtil.toJsonAsString(new CustomModelDownload()), getExtModuleQS(false), 403);

        // Create download for custom model only
        HttpResponse response = post("cmm/" + modelName + "/download", customModelAdmin, RestApiUtil.toJsonAsString(new CustomModelDownload()), getExtModuleQS(false), 201);
        CustomModelDownload returnedDownload = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelDownload.class);
        assertNotNull(returnedDownload);
        assertNotNull(returnedDownload.getNodeRef());

        NodeRef downloadNode = new NodeRef(returnedDownload.getNodeRef()); 

        DownloadStatus status = downloadTestUtil.getDownloadStatus(downloadNode);
        while (status.getStatus() == DownloadStatus.Status.PENDING)
        {
            Thread.sleep(PAUSE_TIME);
            status = downloadTestUtil.getDownloadStatus(downloadNode);
        }

        Set<String> entries = downloadTestUtil.getDownloadEntries(downloadNode);
        assertEquals(1, entries.size());
        String modelEntry = downloadTestUtil.getDownloadEntry(entries, modelExportFileName);
        assertNotNull(modelEntry);
        assertEquals(modelEntry, modelExportFileName);

        // Create download for custom model and its share extension module
        response = post("cmm/" + modelName + "/download", customModelAdmin, RestApiUtil.toJsonAsString(new CustomModelDownload()), getExtModuleQS(true), 201);
        returnedDownload = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), CustomModelDownload.class);
        assertNotNull(returnedDownload);
        assertNotNull(returnedDownload.getNodeRef());

        downloadNode = new NodeRef(returnedDownload.getNodeRef()); 

        status = downloadTestUtil.getDownloadStatus(downloadNode);
        while (status.getStatus() == DownloadStatus.Status.PENDING)
        {
            Thread.sleep(PAUSE_TIME);
            status = downloadTestUtil.getDownloadStatus(downloadNode);
        }

        entries = downloadTestUtil.getDownloadEntries(downloadNode);
        assertEquals(2, entries.size());

        modelEntry = downloadTestUtil.getDownloadEntry(entries, modelExportFileName);
        assertNotNull(modelEntry);
        assertEquals(modelEntry, modelExportFileName);
        
        String shareExtEntry = downloadTestUtil.getDownloadEntry(entries, shareExtExportFileName);
        assertNotNull(shareExtEntry);
        assertEquals(shareExtEntry, shareExtExportFileName);
    }

    private String getExtModuleQS(boolean withShareExtModule)
    {
        return "?extModule=" + withShareExtModule;
    }
}
