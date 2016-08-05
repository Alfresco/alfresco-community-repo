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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.People;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Preference;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;

public class TestUserPreferences extends EnterpriseTestApi
{
	@Test
	public void testUserPreferences() throws Exception
	{
		Iterator<TestNetwork> networksIt = getTestFixture().getNetworksIt();
		assertTrue(networksIt.hasNext());
		final TestNetwork network1 = networksIt.next();
		assertTrue(networksIt.hasNext());
		final TestNetwork network2 = networksIt.next();

		final List<TestPerson> people = new ArrayList<TestPerson>(3);

		// create users and some preferences
		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network1.createUser();
				people.add(person);
				person = network1.createUser();
				people.add(person);
				return null;
			}
		}, network1.getId());
		
		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network2.createUser();
				people.add(person);
				return null;
			}
		}, network2.getId());
		
		final TestPerson person1 = people.get(0);
		final TestPerson person2 = people.get(1);
		final TestPerson person3 = people.get(2);

        final List<Preference> expectedPreferences = new ArrayList<Preference>();
        expectedPreferences.add(new Preference("org.alfresco.share.documentList.testPreference2", String.valueOf(true)));
        expectedPreferences.add(new Preference("org.alfresco.share.documentList.testPreference1", String.valueOf(true)));
        expectedPreferences.add(new Preference("org.alfresco.share.documentList.sortAscending", String.valueOf(true)));
        expectedPreferences.add(new Preference("org.alfresco.share.documentList.testPreference3", String.valueOf(true)));
		// new preference name for issue REPO-855
		expectedPreferences.add(new Preference("org.alfresco.ext.folders.favourites.workspace://SpacesStore/4e3d0779-388a-4b94-91e1-eab588a7da3d.createdAt", String.valueOf(true)));

		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				for(Preference pref : expectedPreferences)
				{
					// TODO add preferences thru api
					repoService.addPreference(person1.getId(), pref.getId(), pref.getValue());
				}

	    		return null;
			}
		}, person1.getId(), network1.getId());

        Collections.sort(expectedPreferences);

        People peopleProxy = publicApiClient.people();
        
        // GET preferences
        // Test case: cloud-1492

        // unknown user
        try
        {
	        int skipCount = 0;
	        int maxItems = 2;
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
	        Paging paging = getPaging(skipCount, maxItems, expectedPreferences.size(), expectedPreferences.size());
	        peopleProxy.getPreferences(GUID.generate(), createParams(paging, null));

        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }

        // test paging
        {
	        int skipCount = 0;
	        int maxItems = 2;
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
	        Paging paging = getPaging(skipCount, maxItems, expectedPreferences.size(), expectedPreferences.size());
	        ListResponse<Preference> resp = peopleProxy.getPreferences(person1.getId(), createParams(paging, null));
	        checkList(expectedPreferences.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
        }

        {
	        int skipCount = 2;
	        int maxItems = 10;
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
	        Paging paging = getPaging(skipCount, maxItems, expectedPreferences.size(), expectedPreferences.size());
	        ListResponse<Preference> resp = peopleProxy.getPreferences(person1.getId(), createParams(paging, null));
	        checkList(expectedPreferences.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
        }
        
        // "-me-" user
        {
	        int skipCount = 0;
	        int maxItems = 2;
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
	        Paging paging = getPaging(skipCount, maxItems, expectedPreferences.size(), expectedPreferences.size());
	        ListResponse<Preference> resp = peopleProxy.getPreferences(org.alfresco.rest.api.People.DEFAULT_USER, createParams(paging, null));
	        checkList(expectedPreferences.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
        }

		// invalid user - 404
        try
        {
	        int skipCount = 2;
	        int maxItems = 10;
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
	        Paging paging = getPaging(skipCount, maxItems, expectedPreferences.size(), expectedPreferences.size());
	        peopleProxy.getPreferences("invalid.user", createParams(paging, null));
			fail("");
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }

		// user from another account - 401
        try
        {
	        int skipCount = 0;
	        int maxItems = 2;
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person3.getId()));
	        Paging paging = getPaging(skipCount, maxItems, expectedPreferences.size(), expectedPreferences.size());
//	        ListResponse<Preference> resp = peopleProxy.getPreferences(person1.getId(), createParams(paging, null));
//	        checkList(expectedPreferences.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
	        peopleProxy.getPreferences(person1.getId(), createParams(paging, null));
	        fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(e.getHttpResponse().getStatusCode(), HttpStatus.SC_UNAUTHORIZED);
        }

		// another user from the same account - 403
        try
        {
	        int skipCount = 0;
	        int maxItems = 2;
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
	        Paging paging = getPaging(skipCount, maxItems, expectedPreferences.size(), expectedPreferences.size());
	        peopleProxy.getPreferences(person1.getId(), createParams(paging, null));
	        fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());        	
        }

        // get a single preference
        // Test Case: cloud-1493
        
        {
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
	        Preference pref = expectedPreferences.get(0);
        	Preference ret = peopleProxy.getPreference(person1.getId(), pref.getId());
        	pref.expected(ret);
        }
        
        // unknown person id
        try
        {
	        Preference pref = expectedPreferences.get(0);
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
        	peopleProxy.getPreference(GUID.generate(), pref.getId());
        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }
        
        // unknown preference id
        try
        {
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
        	peopleProxy.getPreference(person1.getId(), GUID.generate());
        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
        }
        
        // Invalid methods
        // Test case: cloud-1968
        try
        {
	        Preference pref = expectedPreferences.get(0);

	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
        	peopleProxy.create("people", person1.getId(), "preferences", pref.getId(), pref.toJSON().toString(), "Unable to POST to a preference");
        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }
        
        try
        {
	        Preference pref = expectedPreferences.get(0);

	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
        	peopleProxy.update("people", person1.getId(), "preferences", pref.getId(), pref.toJSON().toString(), "Unable to PUT a preference");
        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }
        
        try
        {
	        Preference pref = expectedPreferences.get(0);

	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
        	peopleProxy.remove("people", person1.getId(), "preferences", pref.getId(), "Unable to DELETE a preference");
        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }
        
        try
        {
	        Preference pref = expectedPreferences.get(0);

	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
        	peopleProxy.create("people", person1.getId(), "preferences", null, pref.toJSON().toString(), "Unable to POST to preferences");
        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }

        try
        {
	        Preference pref = expectedPreferences.get(0);

	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
        	peopleProxy.update("people", person1.getId(), "preferences", null, pref.toJSON().toString(), "Unable to PUT preferences");
        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }
        
        try
        {
	        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
        	peopleProxy.remove("people", person1.getId(), "preferences", null, "Unable to DELETE preferences");
        	fail();
        }
        catch(PublicApiException e)
        {
        	assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
        }
	}
}
