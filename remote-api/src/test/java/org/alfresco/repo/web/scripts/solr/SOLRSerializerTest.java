/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail. Otherwise, the software is
 * provided under the following open source license terms:
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.web.scripts.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.solr.SOLRSerializer.SOLRTypeConverter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

public class SOLRSerializerTest
{
    @Test
    public void testDateSerializer()
    {
        SOLRTypeConverter typeConverter = new SOLRTypeConverter(null);

        trip(typeConverter, "1912-01-01T00:40:00-06:00", "1912-01-01T06:40:00.000Z");
        trip(typeConverter, "1812-01-01T00:40:00-06:00", "1812-01-01T06:40:00.000Z");
        trip(typeConverter, "1845-01-01T00:40:00-06:00", "1845-01-01T06:40:00.000Z");
        trip(typeConverter, "1846-01-01T00:40:00-06:00", "1846-01-01T06:40:00.000Z");
        trip(typeConverter, "1847-01-01T00:40:00-06:00", "1847-01-01T06:40:00.000Z");
        trip(typeConverter, "1848-01-01T00:40:00-06:00", "1848-01-01T06:40:00.000Z");

    }

    private void trip(SOLRTypeConverter typeConverter, String iso, String zulu)
    {
        Date testDate = ISO8601DateFormat.parse(iso);
        String strDate = typeConverter.INSTANCE.convert(String.class, testDate);
        assertEquals(zulu, strDate);
    }

    /**
     * Test SOLR Serialization including values with special characters for ChildAssociationRef
     */
    @Test
    public void testChildAssociationRefToJSONString()
    {
        SOLRSerializer solrSerializer = new SOLRSerializer();
        solrSerializer.setDictionaryService(Mockito.mock(DictionaryService.class));
        solrSerializer.setNamespaceService(Mockito.mock(NamespaceService.class));
        solrSerializer.init();
        
        // Create a Child QName including special character \
        QName childQName = QName.createQName("hello", "wo\rld");
        
        ChildAssociationRef childAssociationRef = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                new NodeRef("workspace://SpacesStore/parent"), childQName,
                new NodeRef("workspace://SpacesStore/child"));
        String validJsonString = solrSerializer.serializeToJSONString(childAssociationRef);
        String jsonObjectString = String.format("{ \"key\": \"%s\" }", validJsonString);
        
        try
        {
            new JSONObject(jsonObjectString);
        }
        catch (JSONException e)
        {
            assertTrue("JSON String " + jsonObjectString + " is not a valid JSON", false);
        }
    }

    /**
     * Test SOLR Serialization including values with special characters for AssociationRef
     */
    @Test
    public void testAssociationRefToJSONString()
    {
        SOLRSerializer solrSerializer = new SOLRSerializer();
        solrSerializer.setDictionaryService(Mockito.mock(DictionaryService.class));
        solrSerializer.setNamespaceService(Mockito.mock(NamespaceService.class));
        solrSerializer.init();
        AssociationRef associationRef = new AssociationRef(
                new NodeRef("workspace://SpacesStore/wo\rld"),
                ContentModel.ASSOC_ATTACHMENTS,
                new NodeRef("workspace://SpacesStore/hello"));
        String validJsonString = solrSerializer.serializeToJSONString(associationRef);
        String jsonObjectString = String.format("{ \"key\": \"%s\" }", validJsonString);
        
        try
        {
            new JSONObject(jsonObjectString);
        }
        catch (JSONException e)
        {
            assertTrue("JSON String " + jsonObjectString + " is not a valid JSON", false);
        }
    }

}
