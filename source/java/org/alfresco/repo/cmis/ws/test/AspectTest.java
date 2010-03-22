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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.Holder;

import org.alfresco.repo.cmis.ws.Aspects;
import org.alfresco.repo.cmis.ws.CmisExtensionType;
import org.alfresco.repo.cmis.ws.CmisPropertiesType;
import org.alfresco.repo.cmis.ws.CmisProperty;
import org.alfresco.repo.cmis.ws.CmisPropertyString;
import org.alfresco.repo.cmis.ws.SetAspects;
import org.junit.Assert;

/**
 * Tests Alfresco CMIS WebService API extensions for Aspects.
 * 
 * @author dward
 */
public class AspectTest extends BaseCMISTest
{
    public void testAspectSet() throws Exception
    {
        // create document for checkout
        Holder<String> objectId = new Holder<String>();
        objectServicePort.createDocument(repositoryId, createObjectProperties(getName(), "cmis:document"),
                testFolderId, null, null, null, null, null, new Holder<CmisExtensionType>(), objectId);
        Assert.assertNotNull(objectId.value);

        // checkout
        versioningServicePort.checkOut(repositoryId, objectId, new Holder<CmisExtensionType>(), new Holder<Boolean>());
        Assert.assertNotNull(objectId.value);

        // Apply some aspects to the working copy
        {
            CmisPropertiesType properties = new CmisPropertiesType();
            SetAspects extension = new SetAspects();
            properties.getAny().add(extension);
            extension.getAspectsToAdd().addAll(Arrays.asList(new String[]
            {
                "P:cm:syndication", "P:cm:summarizable"
            }));
            CmisPropertiesType extensionProperties = new CmisPropertiesType();
            extension.setProperties(extensionProperties);
            setStringProperty(extensionProperties, "cm:summary", "Aspect Test (summary)");
            // Add a property without explicitly adding its aspect. Should be automatically added.
            setStringProperty(extensionProperties, "cm:author", "David Ward");
            objectServicePort.updateProperties(repositoryId, objectId, null, properties, null);
            CmisPropertiesType updated = objectServicePort.getProperties(repositoryId, objectId.value, null, null);
            Set<String> appliedAspects = new HashSet<String>(5);
            Map<String, String> aspectProperties = new HashMap<String, String>(11);
            extractAspectsAndProperties(updated, appliedAspects, aspectProperties);
            assertContains(appliedAspects, "P:cm:syndication", "P:cm:summarizable", "P:cm:author");
            assertEquals("Aspect Test (summary)", aspectProperties.get("cm:summary"));
            assertEquals("David Ward", aspectProperties.get("cm:author"));
        }

        // check in with updated aspects
        {
            CmisPropertiesType properties = new CmisPropertiesType();
            SetAspects extension = new SetAspects();
            properties.getAny().add(extension);
            extension.getAspectsToAdd().add("P:cm:countable");
            extension.getAspectsToRemove().add("P:cm:author");
            CmisPropertiesType extensionProperties = new CmisPropertiesType();
            extension.setProperties(extensionProperties);
            setStringProperty(extensionProperties, "cm:summary", "Aspect Test (new summary)");
            versioningServicePort.checkIn(repositoryId, objectId, null, properties, null, null, null, null, null,
                    new Holder<CmisExtensionType>());
            CmisPropertiesType checkedIn = objectServicePort.getProperties(repositoryId, objectId.value, null, null);
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
     * @param properties
     * @param appliedAspects
     * @param aspectProperties
     */
    private void extractAspectsAndProperties(CmisPropertiesType properties, Set<String> appliedAspects,
            Map<String, String> aspectProperties)
    {
        Aspects extension = null;
        for (Object object : properties.getAny())
        {
            if (object instanceof Aspects)
            {
                extension = (Aspects) object;
                break;
            }
        }
        if (extension == null)
        {
            fail("alf:aspects element not included");
        }
        appliedAspects.addAll(extension.getAppliedAspects());
        CmisPropertiesType extensionProperties = extension.getProperties();
        if (extensionProperties == null)
        {
            return;
        }
        for (CmisProperty property : extensionProperties.getProperty())
        {
            if (property instanceof CmisPropertyString)
            {
                aspectProperties.put(property.getPropertyDefinitionId(), ((CmisPropertyString) property).getValue()
                        .get(0));
            }
        }
    }
}
