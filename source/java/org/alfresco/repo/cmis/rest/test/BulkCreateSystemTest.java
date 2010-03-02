/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.rest.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.Response;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.chemistry.abdera.ext.CMISConstants;
import org.apache.chemistry.abdera.ext.CMISExtensionFactory;
import org.apache.chemistry.abdera.ext.CMISObject;
import org.apache.chemistry.abdera.ext.CMISProperties;
import org.apache.chemistry.abdera.ext.CMISProperty;
import org.apache.commons.httpclient.UsernamePasswordCredentials;


/**
 * Test ALFCOM-2947  CMIS: createFolder & immediately add document can fail
 */
public class BulkCreateSystemTest
{
    private static Abdera abdera;

    public static void main(String[] args) throws Exception
    {
        abdera = new Abdera();
        abdera.getFactory().registerExtension(new CMISExtensionFactory());

        AbderaClient client = new AbderaClient(abdera);
        client.usePreemptiveAuthentication(true);
        client.addCredentials("http://localhost:8080", null, "basic", new UsernamePasswordCredentials("admin", "admin"));

        String root = createFolder(client,
                "http://localhost:8080/alfresco/service/api/path/workspace/SpacesStore/Company%20Home/children",
                "testfolder14");

        for (int i = 0; i < 100; i++)
        {
            String folder = createFolder(client, root, "folder_" + i);
            createDocument(client, folder, "doc");
        }
    }

    private static String createFolder(AbderaClient client, String parent, String name) throws Exception
    {
        Entry entry = abdera.newEntry();
        entry.setTitle(name);
        entry.setSummary("Summarize summarize...");

        CMISObject cmisObject = entry.addExtension(CMISConstants.OBJECT);
        CMISProperties properties = cmisObject.addExtension(CMISConstants.PROPERTIES);
        CMISProperty property = properties.addExtension(CMISConstants.ID_PROPERTY);
        property.setAttributeValue(CMISConstants.PROPERTY_ID, CMISConstants.PROP_OBJECT_TYPE_ID);
        Element value = property.addExtension(CMISConstants.PROPERTY_VALUE);
        value.setText("cmis:Folder");

        ClientResponse resp = client.post(parent, entry);

        try
        {
            if (resp.getType() != Response.ResponseType.SUCCESS)
            {
                printError(resp);

                throw new RuntimeException(resp.getStatusText());
            } else
            {
                System.out.println("Created folder " + name);

                Document<Entry> entryDoc = resp.getDocument();
                Entry root = entryDoc.getRoot();

                return root.getLink("children").getHref().toString();
            }
        } finally
        {
            resp.release();
        }
    }

    private static void createDocument(AbderaClient client, String parent, String name)
    {
        Entry entry = abdera.newEntry();
        entry.setTitle(name);
        entry.setSummary("Summarize summarize...");

        CMISObject cmisObject = entry.addExtension(CMISConstants.OBJECT);
        CMISProperties properties = cmisObject.addExtension(CMISConstants.PROPERTIES);
        CMISProperty property = properties.addExtension(CMISConstants.ID_PROPERTY);
        property.setAttributeValue(CMISConstants.PROPERTY_ID, CMISConstants.PROP_OBJECT_TYPE_ID);
        Element value = property.addExtension(CMISConstants.PROPERTY_VALUE);
        value.setText("document");

        ClientResponse resp = client.post(parent, entry);

        try
        {
            if (resp.getType() != Response.ResponseType.SUCCESS)
            {
                printError(resp);

                throw new RuntimeException(resp.getStatusText());
            }
        } finally
        {
            resp.release();
        }
    }

    private static void printError(ClientResponse resp)
    {
        try
        {
            InputStream inputStream = resp.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line != null)
            {
                System.out.println(line);
                line = reader.readLine();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}