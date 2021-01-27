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

package org.alfresco.rest.api.tests;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class TestAspect extends AbstractBaseApiTest {

    private PublicApiClient.Paging paging = getPaging(0, 10);
    PublicApiClient.ListResponse<org.alfresco.rest.api.tests.client.data.Aspect> aspects = null;
    org.alfresco.rest.api.tests.client.data.Aspect aspect, expectedModel = null;
    Map<String, String> otherParams = new HashMap<>();

    @Before
    public void setup() throws Exception {
        super.setup();
        expectedModel = new org.alfresco.rest.api.tests.client.data.Aspect();
        expectedModel.setId("mycompany:childAspect");
        expectedModel.setTitle("Child Aspect");
        expectedModel.setDescription("Child Aspect Description");
        expectedModel.setParentId("smf:smartFolder");
    }

    @Test
    public void testAllAspects() throws PublicApiException {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertTrue(aspects.getPaging().getTotalItems() > 135);
        assertTrue(aspects.getPaging().getHasMoreItems());

        paging.setSkipCount(130);
        paging.setMaxItems(50);
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertFalse(aspects.getPaging().getHasMoreItems());
    }

    @Test
    public void filterAspectsByNamespace() throws PublicApiException {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(uriPrefix matches('http://www.mycompany.com/model.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(2));
        assertFalse(aspects.getPaging().getHasMoreItems());

        otherParams.put("where", "(not uriPrefix matches('http://www.mycompany.com/model.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertTrue(aspects.getPaging().getTotalItems() > 130);
        assertTrue(aspects.getPaging().getHasMoreItems());
    }

    @Test
    public void filterAspectsByParentId() throws PublicApiException {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(parentIds='smf:smartFolder')");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        aspects.getList().get(0).expected(expectedModel);
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(2));
        assertFalse(aspects.getPaging().getHasMoreItems());
    }

    @Test
    public void filterAspectsByModelId() throws PublicApiException {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelIds='mycompany:exampleModel')"); // wrong model id
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(2));
        assertFalse(aspects.getPaging().getHasMoreItems());
    }

    @Test
    public void testAspectsById() throws PublicApiException {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        aspect = publicApiClient.aspects().getAspect("mycompany:childAspect");
        aspect.expected(expectedModel);
    }

    @Test
    public void testListAspectByInvalidValue() throws PublicApiException {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        testListAspectException("(modelIds='unknown:model,known:model')");
        testListAspectException("(modelIds=' , , ')");
        testListAspectException("(parentIds='unknown:aspect,known:aspect')");
        testListAspectException("(parentIds=' , , ')");
        testListAspectException("");
        testListAspectException("(uriPrefix matches(' '))");
        testListAspectException("(uriPrefix matches(' , , '))");
    }

    @Test
    public void testGetAspectByInvalidValue() throws PublicApiException {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        testGetAspectExceptions("uknown:childAspect");
        testGetAspectExceptions(" ");
        testGetAspectExceptions(null);
    }


    private void testGetAspectExceptions(String aspectId) {
        try
        {
            publicApiClient.aspects().getAspect(aspectId);
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
        }
    }

    private void testListAspectException(String query) {
        try
        {
            otherParams.put("where", query); // wrong model id
            publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
        }
    }


    @Override
    public String getScope() {
        return "public";
    }
}
