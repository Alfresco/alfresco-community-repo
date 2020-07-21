/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.rest.framework.tests.metadata;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceDictionary;
import org.alfresco.rest.framework.core.ResourceDictionaryBuilder;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.metadata.ResourceMetaDataWriter;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test rest writer classes
 * 
 * @author Gethin James
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-rest-context.xml" })
public class WriterTests
{
    @Autowired
    ResourceLookupDictionary locator;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultMetaWriter")
    private ResourceMetaDataWriter defaultMetaWriter;

    private static Api api = Api.valueOf("alfrescomock", "private", "1");
    
    @Before
    public void setUp() throws Exception
    {
        Map<String, Object> entityResourceBeans = applicationContext.getBeansWithAnnotation(EntityResource.class);
        Map<String, Object> relationResourceBeans = applicationContext.getBeansWithAnnotation(RelationshipResource.class);
        locator.setDictionary(ResourceDictionaryBuilder.build(entityResourceBeans.values(), relationResourceBeans.values()));
    }
    
    @Test
    public void testWriters() throws IOException
    {
        ResourceDictionary resourceDic = locator.getDictionary();
        Map<String, ResourceWithMetadata> apiResources = resourceDic.getAllResources().get(api);
        String writtenOut = testWriter(defaultMetaWriter, apiResources.get("/sheep"), apiResources);
        assertTrue(writtenOut.startsWith("{\"list\":{\"pagination\":{\"count\":5"));
        
//        ResourceMetaDataWriter wadlWriter = new WebScriptOptionsMetaData();
//        writtenOut = testWriter(wadlWriter, apiResources.get("/sheep"), apiResources);
//        assertTrue(writtenOut.startsWith("{\"list\":{\"pagination\":{\"count\":4"));
    }

    private String testWriter(ResourceMetaDataWriter defaultWriter,
                ResourceWithMetadata resourceWithMetadata,
                Map<String, ResourceWithMetadata> apiResources) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        defaultWriter.writeMetaData(out, apiResources.get("/sheep"), apiResources);
        String outResult = out.toString();
        System.out.println(outResult);
        return outResult;
    }
    

}
