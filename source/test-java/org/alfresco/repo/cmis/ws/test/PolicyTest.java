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
package org.alfresco.repo.cmis.ws.test;

import java.util.List;

import javax.xml.ws.Holder;

import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.CmisExtensionType;
import org.alfresco.repo.cmis.ws.CmisObjectType;
import org.alfresco.repo.cmis.ws.CmisTypeDefinitionListType;
import org.alfresco.repo.cmis.ws.CmisTypeDefinitionType;
import org.alfresco.repo.cmis.ws.EnumServiceException;
import org.junit.Assert;

/**
 * Tests Alfresco CMIS Policy Web Service implementation.
 * 
 * @author dward
 */
public class PolicyTest extends BaseCMISTest
{
    public void testPolicies() throws Exception
    {
        // Try creating an object with the cmis:policy base type (expect a constraint exception)
        Holder<String> objectId = new Holder<String>();
        try
        {
            objectServicePort.createPolicy(repositoryId, createObjectProperties(getName(), "cmis:policy"),
                    testFolderId, null, null, null, new Holder<CmisExtensionType>(), objectId);
            fail("Expected CmisException");
        }
        catch (CmisException e)
        {
            Assert.assertEquals(EnumServiceException.CONSTRAINT, e.getFaultInfo().getType());
        }

        // Try creating an object of any of the cmis:policy subtypes
        CmisTypeDefinitionListType typeDefs = repositoryServicePort.getTypeChildren(repositoryId, "cmis:policy", true,
                null, null, null);
        List<CmisTypeDefinitionType> entries = typeDefs.getTypes();
        assertNotSame(0, entries.size());
        for (CmisTypeDefinitionType type : entries)
        {
            try
            {
                objectServicePort.createPolicy(repositoryId, createObjectProperties(getName(), type.getId()),
                        testFolderId, null, null, null, new Holder<CmisExtensionType>(), objectId);
                fail("Expected CmisException");
            }
            catch (CmisException e)
            {
                Assert.assertEquals(EnumServiceException.CONSTRAINT, e.getFaultInfo().getType());
            }
        }

        // Create a document to attempt to apply policies to
        objectServicePort.createDocument(repositoryId, createObjectProperties(getName(), "cmis:document"),
                testFolderId, null, null, null, null, null, new Holder<CmisExtensionType>(), objectId);
        Assert.assertNotNull(objectId.value);

        // retrieve list of policies applied to document (this should be empty)
        List<CmisObjectType> policies = policyServicePort.getAppliedPolicies(repositoryId, objectId.value, null, null);
        assertNotNull(policies);
        assertEquals(0, policies.size());

        // Try applying a policy (expect a constraint exception)
        try
        {
            policyServicePort
                    .applyPolicy(repositoryId, "doesnotexist", objectId.value, new Holder<CmisExtensionType>());
            fail("Expected CmisException");
        }
        catch (CmisException e)
        {
            Assert.assertEquals(EnumServiceException.CONSTRAINT, e.getFaultInfo().getType());
        }

    }
}
