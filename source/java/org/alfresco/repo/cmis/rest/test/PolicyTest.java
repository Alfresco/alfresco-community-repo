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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.chemistry.abdera.ext.CMISConstants;
import org.apache.chemistry.abdera.ext.CMISTypeDefinition;
import org.junit.Assert;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;

/**
 * Tests Alfresco CMIS Policy implementation.
 * 
 * @author dward
 */
public class PolicyTest extends BaseCMISTest
{
    public void testPolicies() throws Exception
    {
        // Try creating an object with the cmis:policy base type (expect a constraint exception)
        Link children = cmisClient.getChildrenLink(testCaseFolder);
        createObject(children.getHref(), getName(), "cmis:policy", 409);
        
        // Try creating an object of any of the cmis:policy subtypes
        IRI typesHREF = cmisClient.getTypesChildrenCollection(cmisClient.getWorkspace(cmisService));
        Map<String, String> args = new HashMap<String, String>();
        args.put("typeId", "cmis:policy");
        args.put("includePropertyDefinitions", "true");
        Feed types = fetch(typesHREF, args);
        List<Entry> entries = types.getEntries();
        assertNotSame(0, entries.size());
        for (Entry type : entries) {
            CMISTypeDefinition entryType = type.getExtension(CMISConstants.TYPE_DEFINITION);
            Assert.assertNotNull(entryType);
            createObject(children.getHref(), getName(), entryType.getId(), 409);            
        }
        
        // Create a document to attempt to apply policies to
        Entry document = createObject(children.getHref(), getName(), "cmis:document");
        
        // retrieve policies feed on document (this should be empty)
        Link polsLink = document.getLink(CMISConstants.REL_POLICIES);
        assertNotNull(polsLink);
        Feed polsBefore = fetch(polsLink.getHref(), null);
        assertNotNull(polsBefore);
        assertEquals(0, polsBefore.getEntries().size());

        // Try applying a policy (expect a constraint exception)
        String policyFile = localTemplates.load("PolicyTest.applyPolicy.atomentry.xml");
        policyFile = policyFile.replace("${OBJECTID}", "doesnotexist");
        Request req = new PostRequest(polsLink.getHref().toString(), policyFile, CMISConstants.MIMETYPE_ENTRY);
        sendRequest(req, 409);
    }
}
