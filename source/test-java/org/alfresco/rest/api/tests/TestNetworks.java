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
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.People;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.rest.api.tests.client.data.PersonNetwork;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

public class TestNetworks extends EnterpriseTestApi
{
	private List<TestPerson> people = new ArrayList<TestPerson>(3);
	private List<TestNetwork> networks = new ArrayList<TestNetwork>();
	
	private  TestNetwork network1;
	private  TestNetwork network2;
	private  TestNetwork network3;
	
	private TestPerson person11;
	private TestPerson person12;
	private TestPerson person21;
	private TestPerson person31;
	
	@Before
	public void setup()
	{
		// create some networks
		for(int i = 0; i < 2; i++)
		{
			final TestNetwork network = repoService.createNetworkWithAlias("network" + i, true);
			network.create();
			networks.add(network);
		}

		final TestNetwork network = repoService.createNetworkWithAlias("cmisnew.test", true);
		network.create();
		networks.add(network);

		// do we have all the networks created?
		assertEquals(3, networks.size());

		this.network1 = networks.get(0);
		this.network2 = networks.get(1);
		this.network3 = networks.get(2);

		// create a couple of users in one of the networks
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
		
		// create a user in another network
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

		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network3.createUser();
				people.add(person);
				return null;
			}
		}, network3.getId());

		Iterator<TestPerson> peopleIt = people.iterator();
		this.person11 = peopleIt.next();
		this.person12 = peopleIt.next();
		this.person21 = peopleIt.next();
		this.person31 = peopleIt.next();
	}
	
	@Test
	public void testPersonNetworks() throws Exception
	{
		People peopleProxy = publicApiClient.people();

		{
			/**
			 * Test http://<host>:<port>/alfresco/a i.e. tenant servlet root - should return user's networks
			 *
			 */

			final TestNetwork testAccount = getTestFixture().getRandomNetwork();
	    	Iterator<TestPerson> personIt = testAccount.getPeople().iterator();
	    	final TestPerson person = personIt.next();

	    	RequestContext rc = new RequestContext(null, person.getId());
	    	publicApiClient.setRequestContext(rc);

			HttpResponse response = publicApiClient.delete(null, null, null, null, null);
			assertEquals(404, response.getStatusCode());
			
			response = publicApiClient.put(null, null, null, null, null, null, null);
			assertEquals(404, response.getStatusCode());

			response = publicApiClient.post(null, null, null, null, null, null);
			assertEquals(404, response.getStatusCode());

			List<PersonNetwork> expectedNetworkMembers = person.getNetworkMemberships();

			int expectedTotal = expectedNetworkMembers.size();

			{
				// GET / - users networks
				Paging paging = getPaging(0, Integer.MAX_VALUE, expectedTotal, expectedTotal);
				publicApiClient.setRequestContext(new RequestContext("-default-", person.getId()));
				response = publicApiClient.index(createParams(paging, null));
				ListResponse<PersonNetwork> resp = PersonNetwork.parseNetworkMembers(response.getJsonResponse());
				assertEquals(200, response.getStatusCode());
		
				checkList(new ArrayList<PersonNetwork>(expectedNetworkMembers), paging.getExpectedPaging(), resp);
			}
		}

		// user from another network
		{
			publicApiClient.setRequestContext(new RequestContext("-default-", person21.getId()));
			
			List<PersonNetwork> networksMemberships = Collections.emptyList();
			
			try
			{
				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, networksMemberships.size(), networksMemberships.size());
				peopleProxy.getNetworkMemberships(person11.getId(), createParams(paging, null));
				
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
		}

		// user from the same network
		try
		{
			List<PersonNetwork> networksMemberships = person12.getNetworkMemberships();

			publicApiClient.setRequestContext(new RequestContext("-default-", person12.getId()));
			
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, networksMemberships.size(), networksMemberships.size());
			peopleProxy.getNetworkMemberships(person11.getId(), createParams(paging, null));

			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		List<PersonNetwork> networksMemberships = person11.getNetworkMemberships();

		// Test Case cloud-2203
		// Test Case cloud-1498
		// test paging
		{
			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, networksMemberships.size(), networksMemberships.size());
			ListResponse<PersonNetwork> resp = peopleProxy.getNetworkMemberships(person11.getId(), createParams(paging, null));
			checkList(networksMemberships.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}
		
		// "-me-" user
		{
			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));

			int skipCount = 0;
			int maxItems = Integer.MAX_VALUE;
			Paging paging = getPaging(skipCount, maxItems, networksMemberships.size(), networksMemberships.size());
			ListResponse<PersonNetwork> resp = peopleProxy.getNetworkMemberships(org.alfresco.rest.api.People.DEFAULT_USER, createParams(paging, null));
			checkList(networksMemberships.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}
		
		// unknown person id
		try
		{
			List<PersonNetwork> networkMemberships = person11.getNetworkMemberships();

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			
			int skipCount = 0;
			int maxItems = 2;
			Paging expectedPaging = getPaging(skipCount, maxItems, networkMemberships.size(), networkMemberships.size());
			peopleProxy.getNetworkMemberships("invalidUser", createParams(expectedPaging, null));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// invalid caller authentication
		try
		{
			List<PersonNetwork> networkMemberships = person11.getNetworkMemberships();

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId(), GUID.generate()));
			
			int skipCount = 0;
			int maxItems = 2;
			Paging expectedPaging = getPaging(skipCount, maxItems, networkMemberships.size(), networkMemberships.size());
			peopleProxy.getNetworkMemberships(person11.getId(), createParams(expectedPaging, null));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}
		
		// Test Case cloud-1499
		// unknown person id
		try
		{
			List<PersonNetwork> networkMemberships = person11.getNetworkMemberships();
			
			assertTrue(networkMemberships.size() > 0);
			PersonNetwork network = networkMemberships.get(0);

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			peopleProxy.getNetworkMembership("invalidUser", network.getId());
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// invalid caller authentication
		try
		{
			List<PersonNetwork> networkMemberships = person11.getNetworkMemberships();
			
			assertTrue(networkMemberships.size() > 0);
			PersonNetwork network = networkMemberships.get(0);

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId(), GUID.generate()));
			peopleProxy.getNetworkMembership(person11.getId(), network.getId());
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}
		
		// incorrect network id
		try
		{
			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			peopleProxy.getNetworkMembership(person11.getId(), GUID.generate());
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// 1969
		// not allowed methods
		// POST, POST networkId, PUT, PUT networkId, DELETE, DELETE networkId
		try
		{
			PersonNetwork pn = new PersonNetwork(GUID.generate());

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			peopleProxy.create("people", person11.getId(), "networks", null, pn.toJSON().toString(), "Unable to POST to person networks");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			PersonNetwork pn = networksMemberships.get(0);

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			peopleProxy.create("people", person11.getId(), "networks", pn.getId(), pn.toJSON().toString(), "Unable to POST to a person network");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		try
		{
			PersonNetwork pn = new PersonNetwork(GUID.generate());

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			peopleProxy.update("people", person11.getId(), "networks", null, pn.toJSON().toString(), "Unable to PUT person networks");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			PersonNetwork pn = networksMemberships.get(0);

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			peopleProxy.update("people", person11.getId(), "networks", pn.getId(), pn.toJSON().toString(), "Unable to PUT a person network");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			peopleProxy.remove("people", person11.getId(), "networks", null, "Unable to DELETE person networks");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		try
		{
			PersonNetwork pn = networksMemberships.get(0);

			publicApiClient.setRequestContext(new RequestContext("-default-", person11.getId()));
			peopleProxy.remove("people", person11.getId(), "networks", pn.getId(), "Unable to DELETE a person network");
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		// user not a member of the network
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21.getId()));

			int skipCount = 0;
			int maxItems = 2;
			Paging expectedPaging = getPaging(skipCount, maxItems);
			peopleProxy.getNetworkMemberships(person11.getId(), createParams(expectedPaging, null));
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}
	}
	
	/*
	 * CLOUD-1856: test that a network id of the form "cmis*" works
	 */
	@Test
	public void testCLOUD1856() throws Exception
	{
		People peopleProxy = publicApiClient.people();

		publicApiClient.setRequestContext(new RequestContext(network3.getId(), person31.getId()));
		Person ret = peopleProxy.getPerson(person31.getId());
		person31.expected(ret);
	}
	
	// ALF-20216, ALF-20217, ALF-20098
	// http://localhost:8080/alfresco/api/-default-
	@Test
	public void testALF20098() throws Exception
	{
        final TestNetwork testAccount = getTestFixture().getRandomNetwork();
        Iterator<TestPerson> personIt = testAccount.getPeople().iterator();
        final TestPerson person = personIt.next();

        RequestContext rc = new RequestContext("-default-", person.getId());
        publicApiClient.setRequestContext(rc);

        HttpResponse response = publicApiClient.get("-default-", null);
        assertEquals(200, response.getStatusCode());
	}

}
