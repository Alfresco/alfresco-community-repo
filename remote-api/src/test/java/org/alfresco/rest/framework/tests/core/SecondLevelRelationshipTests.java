/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

package org.alfresco.rest.framework.tests.core;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceDictionary;
import org.alfresco.rest.framework.core.ResourceDictionaryBuilder;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceLookupDictionary;
import org.alfresco.rest.framework.core.ResourceMetadata;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedResourceOperationException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.rest.framework.tests.api.mocks.GoatEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.GrassEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepBaaaahResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepBlackSheepResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource;
import org.alfresco.rest.framework.tests.api.mocks3.FlockEntityResource;
import org.alfresco.rest.framework.tests.api.mocks3.FlocketEntityResource;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author janv
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-rest-context.xml" })
public class SecondLevelRelationshipTests
{
    @Autowired
    ResourceLookupDictionary locator;

    @Autowired
    private ApplicationContext applicationContext;

    private static Api api = Api.valueOf("alfrescomockabc", "private", "1");

    @Before
    public void setUp() throws Exception
    {
        Map<String, Object> entityResourceBeans = applicationContext.getBeansWithAnnotation(EntityResource.class);
        Map<String, Object> relationResourceBeans = applicationContext.getBeansWithAnnotation(RelationshipResource.class);
        locator.setDictionary(ResourceDictionaryBuilder.build(entityResourceBeans.values(), relationResourceBeans.values()));
    }

    @Test
    public void testLocateRelationResource()
    {
        Map<String, String> templateVars = new HashMap<String, String>();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "type-a1");
        ResourceWithMetadata collResource = locator.locateResource(api, templateVars, HttpMethod.GET);
        assertNotNull(collResource);
        assertNotNull(collResource.getMetaData().getOperation(HttpMethod.GET));

        templateVars = new HashMap<String, String>();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "type-b1");
        collResource = locator.locateResource(api, templateVars, HttpMethod.GET);
        assertNotNull(collResource);
        assertNotNull(collResource.getMetaData().getOperation(HttpMethod.GET));

        templateVars = new HashMap<String, String>();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "type-b2");
        collResource = locator.locateResource(api, templateVars, HttpMethod.GET);
        assertNotNull(collResource);
        assertNotNull(collResource.getMetaData().getOperation(HttpMethod.GET));

        templateVars = new HashMap<String, String>();
        templateVars.put(ResourceLocator.COLLECTION_RESOURCE, "type-c1");
        collResource = locator.locateResource(api, templateVars, HttpMethod.GET);
        assertNotNull(collResource);
        assertNotNull(collResource.getMetaData().getOperation(HttpMethod.GET));

        Collection<String> relKeys = Arrays.asList("relation-a1-b1", "relation-a1-b2");
        Map<String,ResourceWithMetadata> embeds = locator.locateRelationResource(api,"type-a1", relKeys, HttpMethod.GET);
        assertNotNull(embeds);
    }

    @Test
    public void testLocateRelationResource2()
    {
        // /relation-b1-c1/{entityId}/relation-b1-c1
        String relKey = ResourceDictionary.resourceKey("relation-a1-b1", "relation-b1-c1");
        Collection<String> relKeys = Arrays.asList(relKey);

        // /type-a1/{entityId}/relation-b1-c1/{entityId}/relation-b1-c1
        Map<String,ResourceWithMetadata> embeds = locator.locateRelationResource(api,"type-a1", relKeys, HttpMethod.GET);
        assertNotNull(embeds);
    }
}
