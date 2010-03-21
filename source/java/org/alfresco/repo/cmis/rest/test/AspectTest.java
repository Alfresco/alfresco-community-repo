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

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.chemistry.abdera.ext.CMISConstants;
import org.apache.chemistry.abdera.ext.CMISObject;
import org.apache.chemistry.abdera.ext.CMISProperty;
import org.junit.Assert;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Tests Alfresco CMIS REST API extensions for Aspects.
 * 
 * @author dward
 */
public class AspectTest extends BaseCMISTest
{
    public void testAspectSet() throws Exception
    {
        // create document for checkout
        Link children = cmisClient.getChildrenLink(testCaseFolder);
        Entry document = createObject(children.getHref(), getName(), "cmis:document");
        Request documentReq = new GetRequest(document.getSelfLink().getHref().toString());
        Response documentRes = sendRequest(documentReq, 200);
        Assert.assertNotNull(documentRes);
        String xml = documentRes.getContentAsString();
        Assert.assertNotNull(xml);

        // checkout
        IRI checkedoutHREF = cmisClient.getCheckedOutCollection(cmisClient.getWorkspace(cmisService));
        Request checkoutReq = new PostRequest(checkedoutHREF.toString(), xml, CMISConstants.MIMETYPE_ENTRY);
        Response pwcRes = sendRequest(checkoutReq, 201);
        Assert.assertNotNull(pwcRes);
        Entry pwc = parse(new StringReader(pwcRes.getContentAsString()));
        Assert.assertNotNull(pwc);

        // Apply some aspects to the working copy
        String updateFile = localTemplates.load("AspectTest.addAspects.cmisatomentry.xml");

        Request updateReq = new PutRequest(pwc.getEditLink().getHref().toString(), updateFile,
                CMISConstants.MIMETYPE_ENTRY);
        Response pwcUpdatedres = sendRequest(updateReq, 200);
        Assert.assertNotNull(pwcUpdatedres);
        Entry updated = parse(new StringReader(pwcUpdatedres.getContentAsString()));

        {
            Set<String> appliedAspects = new HashSet<String>(5);
            Map<String, String> aspectProperties = new HashMap<String, String>(11);
            extractAspectsAndProperties(updated, appliedAspects, aspectProperties);
            assertContains(appliedAspects, "P:cm:syndication", "P:cm:summarizable", "P:cm:author");
            assertEquals("Aspect Test (summary)", aspectProperties.get("cm:summary"));
            assertEquals("David Ward", aspectProperties.get("cm:author"));
        }

        // check in with updated aspects
        String checkinFile = localTemplates.load("AspectTest.removeAndAddAspects.cmisatomentry.xml");
        String checkinUrl = pwc.getSelfLink().getHref().toString();
        Request checkinReq = new PutRequest(checkinUrl, checkinFile, CMISConstants.MIMETYPE_ENTRY).setArgs(Collections
                .singletonMap("checkin", "true"));
        Response checkinRes = sendRequest(checkinReq, 200);
        Assert.assertNotNull(checkinRes);
        Entry checkedIn = parse(new StringReader(checkinRes.getContentAsString()));
        {
            Set<String> appliedAspects = new HashSet<String>(5);
            Map<String, String> aspectProperties = new HashMap<String, String>(11);
            extractAspectsAndProperties(checkedIn, appliedAspects, aspectProperties);
            assertContains(appliedAspects, "P:cm:syndication", "P:cm:summarizable", "P:cm:countable");
            assertDoesNotContain(appliedAspects, "P:cm:author");
            assertEquals("Aspect Test (new summary)", aspectProperties.get("cm:summary"));
            assertNull(aspectProperties.get("cm:author"));
        }
    }

    /**
     * @param document
     * @param appliedAspects
     * @param aspectProperties
     */
    private void extractAspectsAndProperties(Entry document, Set<String> appliedAspects,
            Map<String, String> aspectProperties)
    {
        CMISObject documentObject = document.getExtension(CMISConstants.OBJECT);
        Assert.assertNotNull(documentObject);
        Element aspectEl = documentObject.getProperties().getExtension(new QName("http://www.alfresco.org", "aspects"));
        Assert.assertNotNull(aspectEl);
        for (Element child : aspectEl)
        {
            if (child.getQName().equals(ELEMENT_APPLIED_ASPECTS))
            {
                appliedAspects.add(child.getText());
            }
            else if (child.getQName().equals(ELEMENT_PROPERTIES))
            {
                for (Element propertyEl : child)
                {
                    if (propertyEl instanceof CMISProperty)
                    {
                        CMISProperty prop = (CMISProperty) propertyEl;
                        aspectProperties.put(prop.getId(), prop.getStringValue());
                    }
                }
            }
            else
            {
                fail("Unexpected element: " + child.getQName());
            }
        }
    }
}
