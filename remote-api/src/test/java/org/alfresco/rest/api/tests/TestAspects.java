/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Aspect;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


public class TestAspects extends BaseModelApiTest
{
    @Test
    public void testAllAspects() throws PublicApiException
    {
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
    public void filterAspectsByNamespace() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(namespaceUri matches('http://www.mycompany.com/model.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(4));
        assertFalse(aspects.getPaging().getHasMoreItems());

        otherParams.put("where", "(not namespaceUri matches('http://www.mycompany.com/model.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertTrue(aspects.getPaging().getTotalItems() > 130);
        assertTrue(aspects.getPaging().getHasMoreItems());
    }

    @Test
    public void filterAspectsByParentId() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(parentId in ('smf:smartFolder','mycompany:testAspect'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        aspects.getList().get(0).expected(childAspect);
        aspects.getList().get(1).expected(testAspect);
        aspects.getList().get(3).expected(smartFilterAspect);
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(4));
        assertFalse(aspects.getPaging().getHasMoreItems());

        otherParams.put("where", "(parentId in ('smf:smartFolder','mycompany:testAspect') AND namespaceUri matches('http://www.test.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        aspects.getList().get(0).expected(smartFilterAspect);
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(1));

        otherParams.put("where", "(parentId in ('smf:smartFolder', 'mycompany:testAspect') AND not namespaceUri matches('http://www.test.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        aspects.getList().get(0).expected(childAspect);
        aspects.getList().get(1).expected(testAspect);
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(3));

        // match everything
        otherParams.put("where", "(parentId in ('smf:smartFolder','mycompany:testAspect') AND namespaceUri matches('.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(4));

        // match nothing
        otherParams.put("where", "(parentId in ('smf:smartFolder', 'mycompany:testAspect') AND not namespaceUri matches('.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(0));
    }

    @Test
    public void filterAspectsByModelId() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(6));
        assertFalse(aspects.getPaging().getHasMoreItems());

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND namespaceUri matches('http://www.test.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        aspects.getList().get(0).expected(rescanAspect);
        aspects.getList().get(1).expected(smartFilterAspect);
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(2));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND not namespaceUri matches('http://www.test.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(4));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND namespaceUri matches('.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(6));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND not namespaceUri matches('.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(0));
    }

    @Test
    public void testIncludeProperty() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND namespaceUri matches('http://www.test.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        aspects.getList().get(0).expected(rescanAspect);
        assertNull(aspects.getList().get(0).getProperties());
        aspects.getList().get(1).expected(smartFilterAspect);
        assertNull(aspects.getList().get(1).getProperties());

        otherParams.put("where", "(modelId in ('mycompany:model','test:scan') AND namespaceUri matches('http://www.test.*'))");
        otherParams.put("include", "properties");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        aspects.getList().get(0).expected(rescanAspect);
        assertNotNull(aspects.getList().get(0).getProperties());
        aspects.getList().get(1).expected(smartFilterAspect);
        assertNotNull(aspects.getList().get(0).getProperties());
    }

    @Test
    public void testIncludeAssociation() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('api:apiModel'))");
        otherParams.put("include", "associations");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(6));

        for (Aspect aspect : aspects.getList())
        {
            assertNotNull(aspect.getAssociations());
            assertNull(aspect.getProperties());
            assertNull(aspect.getMandatoryAspects());
        }
        assertTrue(aspects.getList().get(0).getAssociations().isEmpty());
        assertTrue(aspects.getList().get(1).getAssociations().isEmpty());
        assertTrue(aspects.getList().get(2).getAssociations().isEmpty());
        assertTrue(aspects.getList().get(3).getAssociations().isEmpty());
        assertEquals(aspects.getList().get(4).getAssociations(), testAllAspect.getAssociations());
        assertTrue(aspects.getList().get(5).getAssociations().isEmpty());
    }

    @Test
    public void testIncludeMandatoryAspect() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('api:apiModel'))");
        otherParams.put("include", "mandatoryAspects");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(6));

        for (Aspect aspect : aspects.getList())
        {
            assertNotNull(aspect.getMandatoryAspects());
            assertNull(aspect.getProperties());
            assertNull(aspect.getAssociations());
        }
        assertTrue(aspects.getList().get(0).getMandatoryAspects().isEmpty());
        assertTrue(aspects.getList().get(1).getMandatoryAspects().isEmpty());
        assertTrue(aspects.getList().get(2).getMandatoryAspects().isEmpty());
        assertTrue(aspects.getList().get(3).getMandatoryAspects().isEmpty());
        assertEquals(aspects.getList().get(4).getMandatoryAspects(), testAllAspect.getMandatoryAspects());
        assertTrue(aspects.getList().get(5).getMandatoryAspects().isEmpty());
    }

    @Test
    public void testIncludes() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('api:apiModel'))");
        otherParams.put("include", "associations,mandatoryAspects");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(6));

        for (Aspect aspect : aspects.getList())
        {
            assertNotNull(aspect.getAssociations());
            assertNotNull(aspect.getMandatoryAspects());
            assertNull(aspect.getProperties());
        }
        assertTrue(aspects.getList().get(0).getAssociations().isEmpty());
        assertTrue(aspects.getList().get(1).getAssociations().isEmpty());
        assertTrue(aspects.getList().get(2).getAssociations().isEmpty());
        assertTrue(aspects.getList().get(3).getAssociations().isEmpty());
        assertEquals(aspects.getList().get(4).getAssociations(), testAllAspect.getAssociations());
        assertEquals(aspects.getList().get(4).getMandatoryAspects(), testAllAspect.getMandatoryAspects());
        assertTrue(aspects.getList().get(5).getAssociations().isEmpty());
    }

    @Test
    public void testSubAspects() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        otherParams.put("where", "(modelId in ('mycompany:model'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(4));

        otherParams.put("where", "(modelId in ('mycompany:model INCLUDESUBASPECTS'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(5));

        otherParams.put("where", "(modelId in ('mycompany:model INCLUDESUBASPECTS') AND namespaceUri matches('http://www.test.*'))");
        aspects = publicApiClient.aspects().getAspects(createParams(paging, otherParams));
        aspects.getList().get(0).expected(smartFilterAspect);
        assertEquals(aspects.getPaging().getTotalItems(), Integer.valueOf(1));
    }

    @Test
    public void testAspectsById() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        aspect = publicApiClient.aspects().getAspect("mycompany:childAspect");
        aspect.expected(childAspect);

        aspect = publicApiClient.aspects().getAspect("test2:aspect-all");
        assertEquals("mandatoryAspects not matched", aspect.getMandatoryAspects(), testAllAspect.getMandatoryAspects());
        assertEquals("association not matched", aspect.getAssociations(), testAllAspect.getAssociations());
        aspect.expected(testAllAspect);
    }

    @Test
    public void testListAspectByInvalidValue() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        testListAspectException("(modelId in ('mycompany:model','unknown:model','known:model'))");
        testListAspectException("(modelId in ('unknown:model','mycompany:model'))");
        testListAspectException("(modelId in (' ',' ',' ')");
        testListAspectException("(parentId in ('smf:smartFolder','unknown:aspect'))");
        testListAspectException("(parentId in ('unknown:aspect','smf:smartFolder'))");
        testListAspectException("(parentId in (' ',' ',' ')");
        testListAspectException("(namespaceUri matches('*'))"); // wrong pattern
    }

    @Test
    public void testGetAspectByInvalidValue() throws PublicApiException
    {
        AuthenticationUtil.setRunAsUser(user1);
        publicApiClient.setRequestContext(new RequestContext(networkOne.getId(), user1));

        testGetAspectExceptions("unknown:childAspect");
        testGetAspectExceptions("aspect:");
        testGetAspectExceptions("aspect");
    }


    private void testGetAspectExceptions(String aspectId)
    {
        try
        {
            publicApiClient.aspects().getAspect(aspectId);
            fail("Aspect not found expected");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }
    }

    private void testListAspectException(String query)
    {
        try
        {
            otherParams.put("where", query);
            publicApiClient.aspects().getAspects(createParams(paging, otherParams));
            fail("Bad request expected");
        }
        catch (PublicApiException e)
        {
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
        }
    }
}
