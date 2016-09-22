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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Sites;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.rest.api.tests.client.data.Site;
import org.alfresco.rest.api.tests.client.data.SiteImpl;
import org.alfresco.rest.api.tests.client.data.SiteMember;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.Test;

public class TestSiteMembers extends EnterpriseTestApi
{
	// TODO set create member for a user who is a member of the site (not the creator)
	// TODO split into more manageable test methods
	@Test
	public void testSiteMembers() throws Exception
	{
		Iterator<TestNetwork> networksIt = getTestFixture().getNetworksIt();
		final TestNetwork testNetwork = networksIt.next();
		final List<String> networkPeople = testNetwork.getPersonIds();
		String personId = networkPeople.get(0);

    	Sites sitesProxy = publicApiClient.sites();

    	{
    		final List<SiteMember> expectedSiteMembers = new ArrayList<SiteMember>();

    		// Create a private site and invite some users
    		// TODO create site members using public api rather than directly using the services
    		TestSite testSite = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
    		{
    			@Override
    			public TestSite doWork() throws Exception
    			{
    				TestSite testSite = testNetwork.createSite(SiteVisibility.PRIVATE);
    				for(int i = 1; i <= 5; i++)
    				{
    					String inviteeId = networkPeople.get(i);
    					testSite.inviteToSite(inviteeId, SiteRole.SiteConsumer);
    					SiteMember sm = new SiteMember(inviteeId, repoService.getPerson(inviteeId), testSite.getSiteId(), SiteRole.SiteConsumer.toString());
    					expectedSiteMembers.add(sm);
    				}

    				return testSite;
    			}
    		}, personId, testNetwork.getId());
    		
    		{
    			SiteMember sm = new SiteMember(personId, repoService.getPerson(personId), testSite.getSiteId(), SiteRole.SiteManager.toString());
    			expectedSiteMembers.add(sm);
    			Collections.sort(expectedSiteMembers);
    		}
    		
	    	// Test Case cloud-1482
	    	{
		    	int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedSiteMembers.size(), null);
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
				ListResponse<SiteMember> siteMembers = sitesProxy.getSiteMembers(testSite.getSiteId(), createParams(paging, null));
				checkList(expectedSiteMembers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), siteMembers);
	    	}
	
	    	{
				int skipCount = 2;
				int maxItems = 10;
				Paging paging = getPaging(skipCount, maxItems, expectedSiteMembers.size(), null);
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
				ListResponse<SiteMember> siteMembers = sitesProxy.getSiteMembers(testSite.getSiteId(), createParams(paging, null));
				checkList(expectedSiteMembers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), siteMembers);

				HttpResponse response = sitesProxy.getAll("sites", testSite.getSiteId(), "members", null, createParams(paging,Collections.singletonMap("includeSource", "true")), "Failed to get all site members");
				checkList(expectedSiteMembers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), SiteMember.parseSiteMembers(testSite.getSiteId(), response.getJsonResponse()));
				JSONObject source = sitesProxy.parseListSource(response.getJsonResponse());
				Site sourceSite = SiteImpl.parseSite(source);
				assertNotNull(sourceSite);
				testSite.expected(sourceSite);
	    	}
	    	
	    	// invalid site id
	    	try
	    	{
				int skipCount = 2;
				int maxItems = 10;
				Paging paging = getPaging(skipCount, maxItems, expectedSiteMembers.size(), null);
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
				sitesProxy.getSiteMembers(GUID.generate(), createParams(paging, null));
				fail();
	    	}
	    	catch(PublicApiException e)
	    	{
	    		assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
	    	}
	
	    	// invalid methods
			try
			{
	    		SiteMember siteMember = expectedSiteMembers.get(0);
	
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
				sitesProxy.update("sites", testSite.getSiteId(), "members", null, siteMember.toJSON().toString(), "Unable to PUT site members");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
	    	
	    	// Test Case cloud-1965
	    	try
	    	{
	    		SiteMember siteMember1 = expectedSiteMembers.get(0);
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
	    		sitesProxy.create("sites", testSite.getSiteId(), "members", siteMember1.getMemberId(), siteMember1.toJSON().toString(), "Unable to POST to a site member");
				fail();
	    	}
	    	catch(PublicApiException e)
	    	{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
	    	}
	
	    	try
	    	{
	    		SiteMember siteMember1 = expectedSiteMembers.get(0);
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
	    		sitesProxy.update("sites", testSite.getSiteId(), "members", null, siteMember1.toJSON().toString(), "Unable to PUT site members");
				fail();
	    	}
	    	catch(PublicApiException e)
	    	{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
	    	}
	
	    	try
	    	{
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
	    		sitesProxy.remove("sites", testSite.getSiteId(), "members", null, "Unable to DELETE site members");
				fail();
	    	}
	    	catch(PublicApiException e)
	    	{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
	    	}
	    	
	    	// update site member
	    	{
	    		SiteMember siteMember1 = expectedSiteMembers.get(0);
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
	    		SiteMember ret = sitesProxy.updateSiteMember(testSite.getSiteId(), siteMember1);
	    		assertEquals(siteMember1.getRole(), ret.getRole());
	    		Person expectedSiteMember = repoService.getPerson(siteMember1.getMemberId());
	    		expectedSiteMember.expected(ret.getMember());
	    	}
	
	    	// GET single site member
			{
	    		SiteMember siteMember1 = expectedSiteMembers.get(0);
				publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), personId));
				SiteMember ret = sitesProxy.getSingleSiteMember(testSite.getSiteId(), siteMember1.getMemberId());
				siteMember1.expected(ret);
			}
		}

		// test: user is member of different tenant, but has site membership(s) in common with the http request user
		{
			Iterator<TestNetwork> accountsIt = getTestFixture().getNetworksIt();
	
			assertTrue(accountsIt.hasNext());
			final TestNetwork network1 = accountsIt.next();
			
			assertTrue(accountsIt.hasNext());
			final TestNetwork network2 = accountsIt.next();
	
			final List<TestPerson> people = new ArrayList<TestPerson>();
	
			// Create users
			TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					TestPerson person = network1.createUser();
					people.add(person);
					person = network1.createUser();
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
			final TestPerson person4 = people.get(3);
			
			// Create site
			final TestSite site = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
			{
				@Override
				public TestSite doWork() throws Exception
				{
					TestSite site = network1.createSite(SiteVisibility.PUBLIC);
					return site;
				}
			}, person2.getId(), network1.getId());
	
			// invalid role - 400
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person1.getId(), "dodgyRole"));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
			}
	
			// user in network but not site member, try to create site member
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person3.getId(), SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
			}
	
			// unknown invitee - 404
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember("dodgyUser", SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			// unknown site - 404
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				sitesProxy.createSiteMember("dodgySite", new SiteMember(person1.getId(), SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			// inviter is not a member of the site
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person1.getId(), SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(e.getMessage(), HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
			}
	
			// inviter is not a member of the site nor a member of the tenant
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person4.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person1.getId(), SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode()); // TODO check that 404 is correct here - external user of network can't see public site??
			}
	
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				SiteMember sm = new SiteMember(person1.getId(), SiteRole.SiteConsumer.toString());
				SiteMember siteMember = sitesProxy.createSiteMember(site.getSiteId(), sm);
				assertEquals(person1.getId(), siteMember.getMemberId());
				assertEquals(SiteRole.SiteConsumer.toString(), siteMember.getRole());
			}
			
			// already invited
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person1.getId(), SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_CONFLICT, e.getHttpResponse().getStatusCode());
			}
	
			// inviter is consumer member of the site, should not be able to add site member
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person4.getId(), SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(e.getMessage(), HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			// invitee from another network
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person4.getId(), SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(e.getMessage(), HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
            
			// missing person id
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(null, SiteRole.SiteContributor.toString()));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
			}

			// missing role
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person1.getId(), null));
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
			}
	
			// check site membership in GET
			List<SiteMember> expectedSiteMembers = site.getMembers();
	
			{
		    	int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedSiteMembers.size(), null);
				ListResponse<SiteMember> siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
				checkList(expectedSiteMembers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), siteMembers);
			}
		}

		// test: create site membership, remove it, get list of site memberships
		{
			Iterator<TestNetwork> accountsIt = getTestFixture().getNetworksIt();
	
			assertTrue(accountsIt.hasNext());
			final TestNetwork network1 = accountsIt.next();
			
			assertTrue(accountsIt.hasNext());
	
			final List<TestPerson> people = new ArrayList<TestPerson>();
	
			// Create user
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
			
			TestPerson person1 = people.get(0);
			TestPerson person2 = people.get(1);
	
			// Create site
			TestSite site = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
			{
				@Override
				public TestSite doWork() throws Exception
				{
					TestSite site = network1.createSite(SiteVisibility.PRIVATE);
					return site;
				}
			}, person2.getId(), network1.getId());
	
			// remove site membership
	
			// for -me- user (PUBLICAPI-90)
			{
				// create a site member
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				SiteMember siteMember = sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person1.getId(), SiteRole.SiteContributor.toString()));
				assertEquals(person1.getId(), siteMember.getMemberId());
				assertEquals(SiteRole.SiteContributor.toString(), siteMember.getRole());
	
				SiteMember toRemove = new SiteMember("-me-");
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				sitesProxy.removeSiteMember(site.getSiteId(), toRemove);
			}
			
			{
				// create a site member
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
				SiteMember siteMember = sitesProxy.createSiteMember(site.getSiteId(), new SiteMember(person1.getId(), SiteRole.SiteContributor.toString()));
				assertEquals(person1.getId(), siteMember.getMemberId());
				assertEquals(SiteRole.SiteContributor.toString(), siteMember.getRole());
	
				// unknown site
				try
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					sitesProxy.removeSiteMember(GUID.generate(), siteMember);
					fail();
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
				}
		
				// unknown user
				try
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					sitesProxy.removeSiteMember(site.getSiteId(), new SiteMember(GUID.generate()));
					fail();
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
				}
		
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					sitesProxy.removeSiteMember(site.getSiteId(), siteMember);
				}
		
				// check site membership in GET
				List<SiteMember> expectedSiteMembers = site.getMembers();
				assertFalse(expectedSiteMembers.contains(siteMember));
		
				{
			    	int skipCount = 0;
					int maxItems = Integer.MAX_VALUE;
					Paging paging = getPaging(skipCount, maxItems, expectedSiteMembers.size(), null);
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					ListResponse<SiteMember> siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
					checkList(expectedSiteMembers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), siteMembers);
				}
				
				// update site membership
		
				// unknown site
				try
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					sitesProxy.updateSiteMember(GUID.generate(), siteMember);
					fail();
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
				}
		
				// unknown user
				try
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					sitesProxy.updateSiteMember(site.getSiteId(), new SiteMember(GUID.generate()));
					fail();
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
				}
				
				// invalid role
				try
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					sitesProxy.updateSiteMember(site.getSiteId(), new SiteMember(person1.getId(), "invalidRole"));
					fail();
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
				}
				
				// user is not a member of the site - 400
				try
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					sitesProxy.updateSiteMember(site.getSiteId(), new SiteMember(person1.getId(), SiteRole.SiteContributor.toString()));
					fail();
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
				}

				// cannot update last member of site to be a non-manager
				try
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					sitesProxy.updateSiteMember(site.getSiteId(), new SiteMember(person2.getId(), SiteRole.SiteContributor.toString()));
					fail();
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_UNPROCESSABLE_ENTITY, e.getHttpResponse().getStatusCode());
				}
		
				// successful update
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
		
					SiteMember sm = new SiteMember(person1.getId(), SiteRole.SiteContributor.toString());
					SiteMember ret = sitesProxy.createSiteMember(site.getSiteId(), sm);
					assertEquals(SiteRole.SiteContributor.toString(), ret.getRole());
					person1.expected(ret.getMember());
		
					sm = new SiteMember(person1.getId(), SiteRole.SiteCollaborator.toString());
					ret = sitesProxy.updateSiteMember(site.getSiteId(), sm);
					assertEquals(SiteRole.SiteCollaborator.toString(), ret.getRole());
					person1.expected(ret.getMember());
		
					// check site membership in GET
					expectedSiteMembers = site.getMembers();
					SiteMember toCheck = null;
					for(SiteMember sm1 : expectedSiteMembers)
					{
						if(sm1.getMemberId().equals(person1.getId()))
						{
							toCheck = sm1;
						}
					}
					assertNotNull(toCheck); // check that the update site membership is present
					assertEquals(sm.getRole(), toCheck.getRole()); // check that the role is correct
		
			    	int skipCount = 0;
					int maxItems = Integer.MAX_VALUE;
					Paging paging = getPaging(skipCount, maxItems, expectedSiteMembers.size(), null);
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
					ListResponse<SiteMember> siteMembers = sitesProxy.getSiteMembers(site.getSiteId(), createParams(paging, null));
					checkList(expectedSiteMembers.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), siteMembers);
				}
			}
		}
	}
}
