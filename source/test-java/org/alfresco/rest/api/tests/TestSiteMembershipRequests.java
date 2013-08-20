package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.SiteMembershipRequests;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.SiteMembershipRequest;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused")
public class TestSiteMembershipRequests extends EnterpriseTestApi
{
	private TestNetwork network1;
	private TestNetwork network2;

	private String person11Id; // network1
	private String person12Id; // network1
	private String person13Id; // network1
	private String person14Id; // network1
	private String person15Id; // network1
	
	private String person24Id; // network2

	private List<TestSite> personModeratedSites = new ArrayList<TestSite>();
	private List<TestSite> personPublicSites = new ArrayList<TestSite>();
	private List<TestSite> person1PrivateSites = new ArrayList<TestSite>();
	private List<TestSite> person1ModeratedSites = new ArrayList<TestSite>();
	private List<TestSite> person1MixedCaseModeratedSites = new ArrayList<TestSite>();
	private List<TestSite> person1PublicSites = new ArrayList<TestSite>();
	private List<TestSite> person4ModeratedSites = new ArrayList<TestSite>();
	private List<NodeRef> personDocs = new ArrayList<NodeRef>();
	private List<NodeRef> personFolders = new ArrayList<NodeRef>();
	private List<NodeRef> person1Docs = new ArrayList<NodeRef>();
	private List<NodeRef> person1Folders = new ArrayList<NodeRef>();
	
	private SiteMembershipRequests siteMembershipRequestsProxy;
	
	private Random random = new Random(System.currentTimeMillis());
	
	@Before
	public void setup() throws Exception
	{
		Iterator<TestNetwork> networksIt = getTestFixture().networksIterator();
		this.network1 = networksIt.next();
		Iterator<String> personIt = network1.getPersonIds().iterator();
    	this.person11Id = personIt.next();
    	assertNotNull(person11Id);
    	this.person12Id = personIt.next();
    	assertNotNull(person12Id);
    	this.person13Id = personIt.next();
    	assertNotNull(person13Id);
    	this.person14Id = personIt.next();
    	assertNotNull(person14Id);
    	this.person15Id = personIt.next();
    	assertNotNull(person15Id);

		this.network2 = networksIt.next();
		Iterator<String> person1It = network2.getPersonIds().iterator();
    	this.person24Id = person1It.next();
    	assertNotNull(person24Id);

    	// Create some sites, files and folders
    	TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				String guid = GUID.generate();
				String[] siteNames = new String[] {"sitex" + guid, "sitea" + guid, "sitef" + guid, "site234" + guid,
						"sitey" + guid, "siteb" + guid, "site643" + guid, "site24" + guid, "site8d6sc" + guid};

				String siteName = siteNames[0];
				SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.MODERATED);
				TestSite site = network1.createSite(siteInfo);
				person1ModeratedSites.add(site);
				
				for(int i = 1; i < siteNames.length; i++)
				{
					siteName = siteNames[i];
					siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.MODERATED);
					site = network1.createSite(siteInfo);
					person1ModeratedSites.add(site);
				}

				String[] mixedCaseSiteNames = new String[] {"MixedCase" + guid, "mixedCaseA" + guid};

				for(int i = 0; i < mixedCaseSiteNames.length; i++)
				{
					siteName = mixedCaseSiteNames[i];
					siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.MODERATED);
					site = network1.createSite(siteInfo);
					person1MixedCaseModeratedSites.add(site);
				}

				for(int i = 0; i < 1; i++)
				{
					siteName = "privatesite" + GUID.generate();
					siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PRIVATE);
					site = network1.createSite(siteInfo);
					person1PrivateSites.add(site);
				}

				NodeRef nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc1", "Test Content");
				person1Docs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder1");
				person1Folders.add(nodeRef);
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc2", "Test Content");
				person1Docs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder2");
				person1Folders.add(nodeRef);
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc3", "Test Content");
				person1Docs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder3");
				person1Folders.add(nodeRef);

				siteName = "site" + GUID.generate();
				siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				site = network1.createSite(siteInfo);
				person1PublicSites.add(site);

				return null;
			}
		}, person12Id, network1.getId());
		
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				String siteName = "site" + System.currentTimeMillis();
				SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				TestSite site = network1.createSite(siteInfo);

				NodeRef nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc1", "Test Content");
				personDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder1");
				personFolders.add(nodeRef);
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc2", "Test Content");
				personDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder2");
				personFolders.add(nodeRef);
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc3", "Test Content");
				personDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder3");
				personFolders.add(nodeRef);

				return null;
			}
		}, person11Id, network1.getId());

    	TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				String siteName = "site" + GUID.generate();
				SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				TestSite site = network1.createSite(siteInfo);
				personPublicSites.add(site);

		    	site.inviteToSite(person12Id, SiteRole.SiteCollaborator);

				siteName = "site" + GUID.generate();
				siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				site = network1.createSite(siteInfo);
				personPublicSites.add(site);

				return null;
			}
		}, person11Id, network1.getId());
    	
    	TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				String siteName = "site" + GUID.generate();
				SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.MODERATED);
				TestSite site = network1.createSite(siteInfo);
				person4ModeratedSites.add(site);

				return null;
			}
		}, person24Id, network2.getId());

		this.siteMembershipRequestsProxy = publicApiClient.siteMembershipRequests();
	}

	private SiteMembershipRequest getSiteMembershipRequest(String networkId, String runAsUserId, String personId) throws PublicApiException, ParseException
	{
		publicApiClient.setRequestContext(new RequestContext(networkId, runAsUserId));

		int skipCount = 0;
		int maxItems = Integer.MAX_VALUE;
		Paging paging = getPaging(skipCount, maxItems);
		ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(personId, createParams(paging, null));
		List<SiteMembershipRequest> list = resp.getList();
		int size = list.size();
		assertTrue(size > 0);
		int idx = random.nextInt(size);
		SiteMembershipRequest request = list.get(idx);
		return request;
	}

	@Test
	public void testInvalidRequests() throws Exception
	{
		{
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12Id));
				
				// unknown invitee
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(GUID.generate(), siteMembershipRequest);

				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12Id));
				
				// unknown siteId
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(GUID.generate());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);

				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12Id));

				// create site membership for another user
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);

				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}

			// cloud-2506
			// get requests for another user
			try
			{
				log("cloud-2506");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// get site membership requests for another user
				int skipCount = 0;
				int maxItems = 4;
				Paging paging = getPaging(skipCount, maxItems);
				siteMembershipRequestsProxy.getSiteMembershipRequests(person12Id, createParams(paging, null));

				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}
			
			// get site membership requests for unknown user
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));
				
				int skipCount = 0;
				int maxItems = 4;
				Paging paging = getPaging(skipCount, maxItems);
				siteMembershipRequestsProxy.getSiteMembershipRequests(GUID.generate(), createParams(paging, null));

				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}
			
			// DELETEs
			{
				{
					// cloud-2524
					// runAs user != target user
					log("cloud-2524");
					
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));

					// create moderated site invitation to delete
					SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
					siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
					siteMembershipRequest.setMessage("Please can I join your site?");
					siteMembershipRequestsProxy.createSiteMembershipRequest(person14Id, siteMembershipRequest);
	
					SiteMembershipRequest request = getSiteMembershipRequest(network1.getId(), person14Id, person14Id);
	
					// user from another network
					try
					{
						publicApiClient.setRequestContext(new RequestContext(network1.getId(), person24Id));
						
						siteMembershipRequestsProxy.cancelSiteMembershipRequest(person14Id, request.getId());
						
						fail("");
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());				
					}
					
					// cloud-2525
					// unknown personId
					try
					{
						log("cloud-2525");

						publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));
						
						siteMembershipRequestsProxy.cancelSiteMembershipRequest(GUID.generate(), request.getId());
						
						fail("");
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
					}
				}
				
				// cloud-2526
				// cloud-2527
				// unknown siteId
				try
				{
					log("cloud-2526");
					log("cloud-2527");

					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));
					
					SiteMembershipRequest request = new SiteMembershipRequest();
					request.setId(GUID.generate());

					siteMembershipRequestsProxy.cancelSiteMembershipRequest(person14Id, request.getId());

					fail("");
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
				}

				// unknown request id
				try
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));
					
					siteMembershipRequestsProxy.cancelSiteMembershipRequest(person14Id, GUID.generate());

					fail("");
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
				}
			}
			
			// PUTs

			// cloud-2519 - PUT to site membership requests
			try
			{
				log("cloud-2519");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				SiteMembershipRequest request = new SiteMembershipRequest();
				request.setId(GUID.generate());
				request.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.update("people", person11Id, "favorites", null, request.toJSON().toString(), "Unable to PUT site membership requests");

				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());				
			}

			// cloud-2520 - unknown request/site id
			try
			{
				log("cloud-2516");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				SiteMembershipRequest request = new SiteMembershipRequest();
				request.setId(GUID.generate());
				request.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.updateSiteMembershipRequest(person11Id, request);
				
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}
		}
	}
	
	@Test
	public void testValidRequests() throws Exception
	{
		final List<SiteMembershipRequest> expectedSiteMembershipRequests = new ArrayList<SiteMembershipRequest>();

		{
			// GET
			// cloud-2531
			// user has no site membership requests
			{
				log("cloud-2531");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
				ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
				checkList(expectedSiteMembershipRequests.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
			
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				int skipCount = 0;
				Paging paging = getPaging(skipCount, null, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
				ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
				checkList(expectedSiteMembershipRequests.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}

			// POSTs
			// cloud-2502
			// cloud-2510
			{
				log("cloud-2502");
				log("cloud-2510");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// moderated site
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				final SiteMembershipRequest moderatedSiteResponse = siteMembershipRequestsProxy.createSiteMembershipRequest("-me-", siteMembershipRequest);
				expectedSiteMembershipRequests.add(moderatedSiteResponse);
				siteMembershipRequest.expected(moderatedSiteResponse);

				// public site
				siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1PublicSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				SiteMembershipRequest ret = siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);
				siteMembershipRequest.expected(ret);

				// test we have a moderated site request only
				// cloud-2532
				{
					log("cloud-2532");

					int skipCount = 0;
					int maxItems = 4;
					Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
					ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
					checkList(expectedSiteMembershipRequests, paging.getExpectedPaging(), resp);
				}

				// test against the underlying invitation service
				List<Invitation> invitations = repoService.getModeratedSiteInvitations(network1.getId(), person11Id, person11Id, null);
				assertEquals(1, invitations.size());
				Invitation invitation = invitations.get(0);
				assertTrue(invitation instanceof ModeratedInvitation);
				ModeratedInvitation moderatedInvitation = (ModeratedInvitation)invitation;
				String siteId = moderatedInvitation.getResourceName();
				Invitation.InvitationType invitationType = moderatedInvitation.getInvitationType();
				Invitation.ResourceType resourceType = moderatedInvitation.getResourceType();
				String inviteeId = moderatedInvitation.getInviteeUserName();
				assertEquals(person11Id, inviteeId);
				assertEquals(Invitation.ResourceType.WEB_SITE, resourceType);
				assertEquals(Invitation.InvitationType.MODERATED, invitationType);
				assertEquals(person1ModeratedSites.get(0).getSiteId(), siteId);

				// test that personId is a member of the public site
				assertTrue(person1PublicSites.get(0).isMember(person11Id));

				// cloud-2534
				// approve the moderated site invitation and check that it is gone from the list
				{
					log("cloud-2534");

					TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
					{
						@Override
						public Void doWork() throws Exception
						{
							repoService.approveSiteInvitation(person11Id, moderatedSiteResponse.getId());
							expectedSiteMembershipRequests.remove(0);
							return null;
						}
					}, person12Id, network1.getId());
	
					// make sure the outstanding request has gone
					int skipCount = 0;
					int maxItems = 4;
					Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
					ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
					checkList(expectedSiteMembershipRequests, paging.getExpectedPaging(), resp);
				}
			}
			
			// user from another network - un-authorised
			try
			{
				log("cloud-2511");
				
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person24Id));

				// moderated site
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person24Id, siteMembershipRequest);

				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());				
			}

			// cloud-2512
			// cloud-2535
			// invitee from another network
			{
				log("cloud-2512");
				log("cloud-2535");
				log("cloud-2536");

				final List<SiteMembershipRequest> person4ExpectedSiteMembershipRequests = new ArrayList<SiteMembershipRequest>();

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person24Id));

				{
					// public site
					try
					{
						SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
						siteMembershipRequest.setId(person1PublicSites.get(0).getSiteId());
						siteMembershipRequest.setMessage("Please can I join your site?");
						siteMembershipRequestsProxy.createSiteMembershipRequest(person24Id, siteMembershipRequest);
						
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());				
					}
				}
				
				{
					// moderated site
					try
					{
						SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
						siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
						siteMembershipRequest.setMessage("Please can I join your site?");
						siteMembershipRequestsProxy.createSiteMembershipRequest(person24Id, siteMembershipRequest);
						
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());				
					}
					
					try
					{
						int skipCount = 0;
						int maxItems = 2;
						Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
						siteMembershipRequestsProxy.getSiteMembershipRequests(person24Id, createParams(paging, null));
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());				
					}
				}

				{
					// private site
					try
					{
						SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
						siteMembershipRequest.setId(person1PrivateSites.get(0).getSiteId());
						siteMembershipRequest.setMessage("Please can I join your site?");
						siteMembershipRequestsProxy.createSiteMembershipRequest(person24Id, siteMembershipRequest);
						
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());				
					}

					try
					{
						int skipCount = 0;
						int maxItems = 2;
						Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
						siteMembershipRequestsProxy.getSiteMembershipRequests(person24Id, createParams(paging, null));
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());				
					}
				}
			}
			
			// cloud-2513
			try
			{
				log("cloud-2513");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// unknown site
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(GUID.generate());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);
				
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// private site
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1PrivateSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);
				
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}
			
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// moderated site in another network
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person4ModeratedSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);
				
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}
			
			// cloud-2514
			try
			{
				log("cloud-2514");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// already joined the site
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);

				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());				
			}
			
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// already requested to join the site but not yet joined
				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);

				siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1ModeratedSites.get(0).getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);

				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());				
			}

			// cloud-2538
			// blank message
			{
				log("cloud-2538");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(person1ModeratedSites.get(1).getSiteId());
				siteMembershipRequest.setMessage("");
				SiteMembershipRequest ret = siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);
				expectedSiteMembershipRequests.add(ret);
			}

			// GETs

			// cloud-2501
			// cloud-2509
			// test paging
			{
				log("cloud-2501");
				log("cloud-2509");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// add some more site membership requests to moderated sites
				for(int i = 1; i < person1ModeratedSites.size(); i++)
				{
					SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
					siteMembershipRequest.setId(person1ModeratedSites.get(i).getSiteId());
					siteMembershipRequest.setMessage("Please can I join your site?");
					try
					{
						SiteMembershipRequest ret = siteMembershipRequestsProxy.createSiteMembershipRequest(person11Id, siteMembershipRequest);
						expectedSiteMembershipRequests.add(ret);
						siteMembershipRequest.expected(ret);
					}
					catch(PublicApiException e)
					{
						// this is ok, already created
					}

				}

				Collections.sort(expectedSiteMembershipRequests);

				{
					int skipCount = 0;
					int maxItems = 2;
					Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
					ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
					checkList(expectedSiteMembershipRequests.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);

					skipCount = 2;
					maxItems = 5;
					paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
					resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
					checkList(expectedSiteMembershipRequests.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
					
					skipCount = 5;
					maxItems = 10;
					paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
					resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
					checkList(expectedSiteMembershipRequests.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
					
					skipCount = 0;
					maxItems = expectedSiteMembershipRequests.size();
					paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
					resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
					checkList(expectedSiteMembershipRequests.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}

				// skipCount is greater than the number of site membership requests in the list
				{
					int skipCount = 1000;
					int maxItems = 2;
					Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
					ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
					checkList(sublist(expectedSiteMembershipRequests, skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}

				// cloud-2537
				// -me- user
				{
					log("cloud-2537");

					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));
	
					int skipCount = 0;
					int maxItems = 2;
					Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
					ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests("-me-", createParams(paging, null));
					checkList(expectedSiteMembershipRequests.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				}
			}

			// DELETEs
			// cloud-2504
			{
				log("cloud-2504");
				
				SiteMembershipRequest request = getSiteMembershipRequest(network1.getId(), person11Id, person11Id);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				siteMembershipRequestsProxy.cancelSiteMembershipRequest(person11Id, request.getId());
				expectedSiteMembershipRequests.remove(request);
				Collections.sort(expectedSiteMembershipRequests);

				// cloud-2533
				// check that the site membership request has gone
				log("cloud-2533");

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
				ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
				checkList(expectedSiteMembershipRequests, paging.getExpectedPaging(), resp);

				// cloud-2528
				// try to cancel the same request
				try
				{
					log("cloud-2528");

					siteMembershipRequestsProxy.cancelSiteMembershipRequest(person11Id, request.getId());
					
					fail("");
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
				}
			}
			
			// cloud-2529
			// cancel a site membership request that has been rejected
			{
				log("cloud-2529");

				final String siteId = person1ModeratedSites.get(1).getSiteId();

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(siteId);
				siteMembershipRequest.setMessage("Please can I join your site?");
				SiteMembershipRequest moderatedSiteResponse = siteMembershipRequestsProxy.createSiteMembershipRequest(person14Id, siteMembershipRequest);
				expectedSiteMembershipRequests.add(moderatedSiteResponse);
				Collections.sort(expectedSiteMembershipRequests);
				siteMembershipRequest.expected(moderatedSiteResponse);

				TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
				{
					@Override
					public Void doWork() throws Exception
					{
						Invitation invitation = repoService.rejectSiteInvitation(person14Id, siteId);
						assertNotNull(invitation);

						return null;
					}
				}, person12Id, network1.getId());

				// try to cancel the request
				try
				{
					siteMembershipRequestsProxy.cancelSiteMembershipRequest(person14Id, siteId);
					
					fail("");
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
				}
			}

			// cloud-2530
			// cancel a site membership request that has been approved
			{
				log("cloud-2530");

				final String siteId = person1ModeratedSites.get(2).getSiteId();

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(siteId);
				siteMembershipRequest.setMessage("Please can I join your site?");
				SiteMembershipRequest moderatedSiteResponse = siteMembershipRequestsProxy.createSiteMembershipRequest(person14Id, siteMembershipRequest);
				expectedSiteMembershipRequests.add(moderatedSiteResponse);
				Collections.sort(expectedSiteMembershipRequests);
				siteMembershipRequest.expected(moderatedSiteResponse);

				TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
				{
					@Override
					public Void doWork() throws Exception
					{
						Invitation invitation = repoService.approveSiteInvitation(person14Id, siteId);
						assertNotNull(invitation);

						return null;
					}
				}, person12Id, network1.getId());

				// try to cancel the request
				try
				{
					siteMembershipRequestsProxy.cancelSiteMembershipRequest(person14Id, siteId);
					
					fail("");
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
				}
			}

			// PUTs

			// cloud-2503
			// cloud-2517
			// cloud-2518
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));

				// merged these tests
				// cloud-2503: use -me- pseudo user
				// cloud-2517: initially no message
				log("cloud-2503");
				log("cloud-2517");

				// create a request without a message
				String siteId = person1ModeratedSites.get(7).getSiteId();

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(siteId);
				SiteMembershipRequest request = siteMembershipRequestsProxy.createSiteMembershipRequest(person14Id, siteMembershipRequest);
				assertNotNull(request);
				
				// update it, with a message
				request.setMessage("Please can I join your site?");
				SiteMembershipRequest updated = siteMembershipRequestsProxy.updateSiteMembershipRequest(person14Id, request);
				request.expected(updated);

				// check it's updated
				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
				ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person14Id, createParams(paging, null));
				List<SiteMembershipRequest> requests = resp.getList();
				assertTrue(requests.size() > 0);
				int idx = requests.indexOf(request);
				SiteMembershipRequest toCheck = requests.get(idx);
				updated.expected(toCheck);
				
				// cloud-2518
				// update it again, with ammended message
				
				log("cloud-2518");

				request.setMessage("Please can I join your site, pretty please?");
				updated = siteMembershipRequestsProxy.updateSiteMembershipRequest(person14Id, request);
				request.expected(updated);

				// check it's updated
				skipCount = 0;
				maxItems = 2;
				paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
				resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person14Id, createParams(paging, null));
				requests = resp.getList();
				assertTrue(requests.size() > 0);
				idx = requests.indexOf(request);
				toCheck = requests.get(idx);
				updated.expected(toCheck);
			}
			
			// cloud-2515 - no changes
			{
				log("cloud-2515");
				
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));

				// create a request
				String siteId = person1ModeratedSites.get(8).getSiteId();

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(siteId);
				siteMembershipRequest.setMessage("Please can I join your site?");
				SiteMembershipRequest request = siteMembershipRequestsProxy.createSiteMembershipRequest(person14Id, siteMembershipRequest);
				assertNotNull(request);
				
				// update it, with no changes
				SiteMembershipRequest updated = siteMembershipRequestsProxy.updateSiteMembershipRequest(person14Id, request);
				request.expected(updated); // should not have changed
			}

			// cloud-2516 - unknown person id
			try
			{
				log("cloud-2516");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				// get an outstanding request
				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
				ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person11Id, createParams(paging, null));
				List<SiteMembershipRequest> requests = resp.getList();
				assertTrue(requests.size() > 0);
				SiteMembershipRequest request = requests.get(0);
				siteMembershipRequestsProxy.updateSiteMembershipRequest(GUID.generate(), request);
				
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}

			// cloud-2521 - unknown site membership request
			try
			{
				log("cloud-2521");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

				SiteMembershipRequest request = new SiteMembershipRequest();
				request.setId(GUID.generate());
				siteMembershipRequestsProxy.updateSiteMembershipRequest(person11Id, request);
				
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
			}

			
			// cloud-2522
			// update a site membership request that has been rejected
			{
				log("cloud-2522");

				String siteId = person1ModeratedSites.get(5).getSiteId();

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(siteId);
				siteMembershipRequest.setMessage("Please can I join your site?");
				SiteMembershipRequest moderatedSiteResponse = siteMembershipRequestsProxy.createSiteMembershipRequest(person14Id, siteMembershipRequest);
				expectedSiteMembershipRequests.add(moderatedSiteResponse);
				Collections.sort(expectedSiteMembershipRequests);
				siteMembershipRequest.expected(moderatedSiteResponse);

				repoService.rejectSiteInvitation(person14Id, siteId);

				// try to update the request
				try
				{
					siteMembershipRequestsProxy.updateSiteMembershipRequest(siteId, moderatedSiteResponse);
					
					fail("");
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
				}
			}
			
			// cloud-2523
			// update a site membership request that has been approved
			{
				log("cloud-2523");

				String siteId = person1ModeratedSites.get(6).getSiteId();

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(siteId);
				siteMembershipRequest.setMessage("Please can I join your site?");
				SiteMembershipRequest moderatedSiteResponse = siteMembershipRequestsProxy.createSiteMembershipRequest(person14Id, siteMembershipRequest);
				expectedSiteMembershipRequests.add(moderatedSiteResponse);
				Collections.sort(expectedSiteMembershipRequests);
				siteMembershipRequest.expected(moderatedSiteResponse);

				repoService.approveSiteInvitation(person14Id, siteId);

				// try to update the request
				try
				{
					siteMembershipRequestsProxy.updateSiteMembershipRequest(siteId, moderatedSiteResponse);
					
					fail("");
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
				}
			}
			
			{
				// cloud-2539 - probing attack tests
				
				log("cloud-2539");
				
				// i) create site membership request to a moderated site
				// ii) site owner changes the site to a private site
				// iii) re-issue create site membership request should be a 404
				{
					final List<SiteMembershipRequest> person2ExpectedSiteMembershipRequests = new ArrayList<SiteMembershipRequest>();

					final TestSite site = person1ModeratedSites.get(0);

					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person13Id));

					SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
					siteMembershipRequest.setId(site.getSiteId());
					siteMembershipRequest.setMessage("Please can I join your site?");
					siteMembershipRequestsProxy.createSiteMembershipRequest(person13Id, siteMembershipRequest);

					TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
					{
						@Override
						public Void doWork() throws Exception
						{
							site.setSiteVisibility(SiteVisibility.PRIVATE);
							return null;
						}
					}, person12Id, network1.getId());

					// Can we still GET it? Should be a 404 (private site)
					try
					{
						siteMembershipRequestsProxy.getSiteMembershipRequest(person13Id, siteMembershipRequest.getId());
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
					}
					
					// GET should not contain the site
					{
						int skipCount = 0;
						int maxItems = 10;
						assertEquals(0, person2ExpectedSiteMembershipRequests.size());
						Paging paging = getPaging(skipCount, maxItems, person2ExpectedSiteMembershipRequests.size(), person2ExpectedSiteMembershipRequests.size());
						ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person13Id, createParams(paging, null));
						checkList(sublist(person2ExpectedSiteMembershipRequests, skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
					}

					try
					{
						siteMembershipRequestsProxy.createSiteMembershipRequest(person13Id, siteMembershipRequest);

						fail("");
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
					}
				}

				// i) create site membership request to a public site
				// ii) site owner changes the site to a private site
				// iii) re-issue create site membership request should be a 404
				{
					final TestSite site = person1PublicSites.get(0);

					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person13Id));

					SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
					siteMembershipRequest.setId(site.getSiteId());
					siteMembershipRequest.setMessage("Please can I join your site?");
					siteMembershipRequestsProxy.createSiteMembershipRequest(person13Id, siteMembershipRequest);

					TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
					{
						@Override
						public Void doWork() throws Exception
						{
							site.setSiteVisibility(SiteVisibility.PRIVATE);
							return null;
						}
					}, person12Id, network1.getId());
					
					try
					{
						siteMembershipRequestsProxy.createSiteMembershipRequest(person13Id, siteMembershipRequest);

						fail("");
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
					}
					
					try
					{
						siteMembershipRequestsProxy.updateSiteMembershipRequest(person13Id, siteMembershipRequest);

						fail("");
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
					}
					
					try
					{
						siteMembershipRequestsProxy.cancelSiteMembershipRequest(person13Id, siteMembershipRequest.getId());

						fail("");
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
					}
				}
			}

			// i) create site membership request to a moderated site
			// ii) site owner accepts the request -> user is now a member of the site
			// iii) site owner changes the site to a private site
			// iv) re-issue create site membership request should be a 404
			{
				final TestSite site = person1ModeratedSites.get(1);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person13Id));

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(site.getSiteId());
				siteMembershipRequest.setMessage("Please can I join your site?");
				siteMembershipRequestsProxy.createSiteMembershipRequest(person13Id, siteMembershipRequest);

				// approve the site invitation request and convert the site to a private site
				TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
				{
					@Override
					public Void doWork() throws Exception
					{
						repoService.approveSiteInvitation(person13Id, site.getSiteId());

						site.setSiteVisibility(SiteVisibility.PRIVATE);
						return null;
					}
				}, person12Id, network1.getId());
				
				try
				{
					siteMembershipRequestsProxy.createSiteMembershipRequest(person13Id, siteMembershipRequest);

					fail("");
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());				
				}
			}

			// blank message in POST and PUT
			{
				final TestSite site = person1ModeratedSites.get(2);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person13Id));

				SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
				siteMembershipRequest.setId(site.getSiteId());
				siteMembershipRequest.setMessage("");
				SiteMembershipRequest created = siteMembershipRequestsProxy.createSiteMembershipRequest(person13Id, siteMembershipRequest);
				SiteMembershipRequest updated = siteMembershipRequestsProxy.updateSiteMembershipRequest(person13Id, siteMembershipRequest);

				assertTrue(updated.getModifiedAt().after(created.getCreatedAt()));
			}
		}
	}
	
	// PUBLICAPI-126, PUBLICAPI-132
	@Test
	public void testMixedCase() throws Exception
	{
		final List<SiteMembershipRequest> expectedSiteMembershipRequests = new ArrayList<SiteMembershipRequest>();

		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person15Id));
			
			// moderated site
			String siteId = person1MixedCaseModeratedSites.get(0).getSiteId().toLowerCase();
			
			SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
			siteMembershipRequest.setId(siteId); // lower case to test mixed case
			siteMembershipRequest.setMessage("Please can I join your site?");
			final SiteMembershipRequest moderatedSiteResponse = siteMembershipRequestsProxy.createSiteMembershipRequest("-me-", siteMembershipRequest);
			expectedSiteMembershipRequests.add(moderatedSiteResponse);
			Collections.sort(expectedSiteMembershipRequests);
			siteMembershipRequest.expected(moderatedSiteResponse);

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedSiteMembershipRequests.size(), expectedSiteMembershipRequests.size());
			ListResponse<SiteMembershipRequest> resp = siteMembershipRequestsProxy.getSiteMembershipRequests(person15Id, createParams(paging, null));
			checkList(expectedSiteMembershipRequests.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);

			SiteMembershipRequest ret = siteMembershipRequestsProxy.getSiteMembershipRequest(person15Id, siteId);
			siteMembershipRequest.expected(ret);

			siteMembershipRequestsProxy.cancelSiteMembershipRequest(person15Id, ret.getId());

			try
			{
				siteMembershipRequestsProxy.getSiteMembershipRequest(person15Id, siteId);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
		}
	}

	@Test
	public void testALF19332() throws Exception
	{
		String networkId = network1.getId();
		
		final TestNetwork systemNetwork = getRepoService().getSystemNetwork();
		long time = System.currentTimeMillis();
		// note: username for site creator is of the form user@network
		PersonInfo personInfo = new PersonInfo("test", "test", "test" + time, "password", null, "test", "test", "test", "test", "test", "test");
		TestPerson person = network1.createUser(personInfo);
		personInfo = new PersonInfo("test", "test", "test1" + time, "password", null, "test", "test", "test", "test", "test", "test");
		TestPerson person1 = network1.createUser(personInfo);

		TestSite site = TenantUtil.runAsUserTenant(new TenantRunAsWork<TestSite>()
		{
			@Override
			public TestSite doWork() throws Exception
			{
				TestSite site = systemNetwork.createSite(SiteVisibility.PUBLIC);
				return site;
			}
		}, person.getId(), networkId);

		publicApiClient.setRequestContext(new RequestContext("-default-", person1.getId()));		
		SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
		siteMembershipRequest.setId(site.getSiteId());
		siteMembershipRequest.setMessage("Please can I join your site?");
		SiteMembershipRequest ret = siteMembershipRequestsProxy.createSiteMembershipRequest(person1.getId(), siteMembershipRequest);
	}
}
