/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

public class CMISAtomTemplatesTest extends BaseCMISTest
{
    static String docName;

    static String xmlResponse;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // create document
        docName = "Test 1                                        _" + System.currentTimeMillis();
        Link children = cmisClient.getChildrenLink(testCaseFolder);
        Entry document = createObject(children.getHref(), docName, "cmis:document");
        Request documentReq = new GetRequest(document.getSelfLink().getHref().toString());
        Response documentRes = sendRequest(documentReq, 200);
        Assert.assertNotNull(documentRes);
        xmlResponse = documentRes.getContentAsString();
        Assert.assertNotNull(xmlResponse);
    }

    @Test
    public void testChildrenGetAtomFeed() throws Exception
    {
        // retrieve the list of nodes
        String id = testCaseFolder.getId().toString().replace("urn:uuid:", "workspace:SpacesStore/i/");
        Request get = new GetRequest("/cmis/s/" + id + "/children");
        Response res = sendRequest(get, 200);
        String xml = res.getContentAsString();

        // check document name with repeatable spaces in xml response.
        assertEquals("Probably, children.get.atomfeed.ftl template has compress dirrective", true, xml.contains(docName));
    }

    @Test
    public void testItemGetAtomentryTemplate() throws Exception
    {
        // check document name with repeatable spaces in xml response.
        assertEquals("Probably, item.get.atomentry.ftl template has compress dirrective", true, xmlResponse.contains(docName));
    }
}