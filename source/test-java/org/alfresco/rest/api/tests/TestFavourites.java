package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.Favourites;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.SiteMembershipRequests;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Favourite;
import org.alfresco.rest.api.tests.client.data.FavouritesTarget;
import org.alfresco.rest.api.tests.client.data.FileFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.FolderFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.InvalidFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.JSONAble;
import org.alfresco.rest.api.tests.client.data.Site;
import org.alfresco.rest.api.tests.client.data.SiteFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.SiteImpl;
import org.alfresco.rest.api.tests.client.data.SiteMembershipRequest;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.httpclient.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class TestFavourites extends EnterpriseTestApi
{
	private static enum TARGET_TYPE
	{
		file, folder, site;
	};
	
	private TestNetwork network1;
	private TestPerson person10;
	private String person10Id;
	private TestPerson person11;
	private String person11Id;
	private TestPerson person12;
	private String person12Id;
	private TestPerson person14;
	private String person14Id;

	private TestNetwork network2;
	private TestPerson person21;
	private String person21Id;

	private List<TestSite> personSites = new ArrayList<TestSite>();
	private List<TestSite> person1PublicSites = new ArrayList<TestSite>();
	private List<TestSite> person1PrivateSites = new ArrayList<TestSite>();

	private List<NodeRef> personDocs = new ArrayList<NodeRef>();
	private List<NodeRef> personFolders = new ArrayList<NodeRef>();
	private List<NodeRef> person1PublicDocs = new ArrayList<NodeRef>();
	private List<NodeRef> person1PublicFolders = new ArrayList<NodeRef>();
	private List<NodeRef> person1PrivateDocs = new ArrayList<NodeRef>();
	private List<NodeRef> person1PrivateFolders = new ArrayList<NodeRef>();
	
	private Favourites favouritesProxy;
	private SiteMembershipRequests siteMembershipRequestsProxy;

	private void sort(List<Favourite> favourites, final List<Pair<FavouritesService.SortFields, Boolean>> sortProps)
	{
    	Comparator<Favourite> comparator = new Comparator<Favourite>()
    	{
			@Override
			public int compare(Favourite o1, Favourite o2)
			{
				int ret = 0;
				for(Pair<FavouritesService.SortFields, Boolean> sort : sortProps)
				{
					FavouritesService.SortFields field = sort.getFirst();
					Boolean ascending = sort.getSecond();
					if(field.equals(FavouritesService.SortFields.username))
					{
						if(ascending)
						{
							if(o1.getUsername() != null && o2.getUsername() != null)
							{
								ret = collator.compare(o1.getUsername(), o2.getUsername());
							}
						}
						else
						{
							if(o1.getUsername() != null && o2.getUsername() != null)
							{
								ret = o2.getUsername().compareTo(o1.getUsername());
							}
						}

						if(ret != 0)
						{
							break;
						}
					}
					else if(field.equals(FavouritesService.SortFields.type))
					{
						if(ascending)
						{
							ret = o1.getType().compareTo(o2.getType());							
						}
						else
						{
							ret = o2.getType().compareTo(o1.getType());
						}

						if(ret != 0)
						{
							break;
						}
					}
					else if(field.equals(FavouritesService.SortFields.createdAt))
					{
						if(ascending)
						{
							ret = o1.getCreatedAt().compareTo(o2.getCreatedAt());							
						}
						else
						{
							ret = o2.getCreatedAt().compareTo(o1.getCreatedAt());
						}

						if(ret != 0)
						{
							break;
						}
					}
				}

				return ret;
			}
    	};
    	Collections.sort(favourites, comparator);
	}

	/**
	 * Returns a new list.
	 * 
	 * @param favourites
	 * @param types
	 * @return
	 */
	private ArrayList<Favourite> filter(List<Favourite> favourites, final Set<Type> types)
	{
		Predicate<Favourite> predicate = new Predicate<Favourite>()
		{
			@Override
			public boolean apply(Favourite other)
			{
				Type type = null;
				if(other.getTarget() instanceof FileFavouriteTarget)
				{
					type = Type.FILE;
				}
				else if(other.getTarget() instanceof FolderFavouriteTarget)
				{
					type = Type.FOLDER;
				}
				else if(other.getTarget() instanceof SiteFavouriteTarget)
				{
					type = Type.SITE;
				}
				
				boolean ret = (type != null && types.contains(type));
				return ret;
			}
		};
		ArrayList<Favourite> ret = Lists.newArrayList(Collections2.filter(favourites, predicate));
		return ret;
	}
	
	@Before
	public void setup() throws Exception
	{
		final Iterator<TestNetwork> networksIt = getTestFixture().networksIterator();

		// Workaround for domain name mismatch in lucene indexing that occurs when this test runs.
		repoService.disableInTxnIndexing();

		transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
        	@SuppressWarnings("synthetic-access")
        	public Void execute() throws Throwable
        	{
        		try
        		{
        			AuthenticationUtil.pushAuthentication();
        			AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        	    	// create some users
        			TestFavourites.this.network1 = networksIt.next();
	    	        
        			String name = GUID.generate();
        			PersonInfo personInfo = new PersonInfo(name, name, name, "password", null, null, null, null, null, null, null);
	    			TestFavourites.this.person10 = network1.createUser(personInfo);
	    			assertNotNull(TestFavourites.this.person10);
	    			TestFavourites.this.person10Id = TestFavourites.this.person10.getId();
	    			name = GUID.generate();
	    			personInfo = new PersonInfo(name, name, name, "password", null, null, null, null, null, null, null);
	    			TestFavourites.this.person11 = network1.createUser(personInfo);
	    			assertNotNull(TestFavourites.this.person11);
	    			TestFavourites.this.person11Id = TestFavourites.this.person11.getId();
	    			name = GUID.generate();
	    			personInfo = new PersonInfo(name, name, name, "password", null, null, null, null, null, null, null);
	    			TestFavourites.this.person12 = network1.createUser(personInfo);
	    			assertNotNull(TestFavourites.this.person12);
	    			TestFavourites.this.person12Id = TestFavourites.this.person12.getId();
	    			name = GUID.generate();
	    			personInfo = new PersonInfo(name, name, name, "password", null, null, null, null, null, null, null);
	    			TestFavourites.this.person14 = network1.createUser(personInfo);
	    			assertNotNull(TestFavourites.this.person14);
	    			TestFavourites.this.person14Id = TestFavourites.this.person14.getId();

	    			TestFavourites.this.network2 = networksIt.next();
	    			name = GUID.generate();
	    			personInfo = new PersonInfo(name, name, name, "password", null, null, null, null, null, null, null);
	    			TestFavourites.this.person21 = network2.createUser(personInfo);
	    			assertNotNull(TestFavourites.this.person21);
	    			TestFavourites.this.person21Id = TestFavourites.this.person21.getId();
	    			
	    	        return null;
        		}
        		finally
        		{
        			AuthenticationUtil.popAuthentication();
        		}
        	}
        }, false, true);
		
    	// Create some favourite targets, sites, files and folders
    	TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				String siteName = "site" + GUID.generate();
				SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				TestSite site = network1.createSite(siteInfo);
				person1PublicSites.add(site);

				NodeRef nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc1", "Test Doc1 Title", "Test Doc1 Description", "Test Content");
				person1PublicDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder1", "Test Folder1 Title", "Test Folder1 Description");
				person1PublicFolders.add(nodeRef);
				nodeRef = repoService.createDocument(nodeRef, "Test Doc2",  "Test Doc2 Title", "Test Doc2 Description", "Test Content");
				person1PublicDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder2", "Test Folder2 Title", "Test Folder2 Description");
				person1PublicFolders.add(nodeRef);
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc3",  "Test Doc3 Title", "Test Doc3 Description", "Test Content");
				person1PublicDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder3", "Test Folder3 Title", "Test Folder3 Description");
				person1PublicFolders.add(nodeRef);

				siteName = "site" + GUID.generate();
				siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				site = network1.createSite(siteInfo);
				person1PublicSites.add(site);
				
				siteName = "site" + GUID.generate();
				siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PRIVATE);
				site = network1.createSite(siteInfo);
				person1PrivateSites.add(site);
				
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc1", "Test Doc1 Title", "Test Doc1 Description", "Test Content");
				person1PrivateDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder1", "Test Folder1 Title", "Test Folder1 Description");
				person1PrivateFolders.add(nodeRef);
				nodeRef = repoService.createDocument(nodeRef, "Test Doc2",  "Test Doc2 Title", "Test Doc2 Description", "Test Content");
				person1PrivateDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder2", "Test Folder2 Title", "Test Folder2 Description");
				person1PrivateFolders.add(nodeRef);
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc3",  "Test Doc3 Title", "Test Doc3 Description", "Test Content");
				person1PrivateDocs.add(nodeRef);
				nodeRef = repoService.createFolder(site.getContainerNodeRef("documentLibrary"), "Test Folder3", "Test Folder3 Title", "Test Folder3 Description");
				person1PrivateFolders.add(nodeRef);

				return null;
			}
		}, person11Id, network1.getId());
		
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				String siteName = "site" + System.currentTimeMillis();
				SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				TestSite site = network1.createSite(siteInfo);
				person1PublicSites.add(site);

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
		}, person10Id, network1.getId());

    	TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				String siteName = "site" + GUID.generate();
				SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				TestSite site = network1.createSite(siteInfo);
				personSites.add(site);

		    	site.inviteToSite(person11Id, SiteRole.SiteCollaborator);

				siteName = "site" + GUID.generate();
				siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
				site = network1.createSite(siteInfo);
				personSites.add(site);

				return null;
			}
		}, person10Id, network1.getId());

		this.favouritesProxy = publicApiClient.favourites();
		this.siteMembershipRequestsProxy = publicApiClient.siteMembershipRequests();
	}

	private void updateFavourite(String networkId, String runAsUserId, String personId, TARGET_TYPE type) throws Exception
	{
		{
			int size = 0;

			try
			{
				// get a favourite id
				ListResponse<Favourite> resp = getFavourites(networkId, runAsUserId, personId, 0, Integer.MAX_VALUE, null, null, type);
				List<Favourite> favourites = resp.getList();
				size = favourites.size();
				assertTrue(size > 0);
				Favourite favourite = favourites.get(0);

				favouritesProxy.update("people", personId, "favorites", favourite.getTargetGuid(), favourite.toJSON().toString(), "Unable to update favourite");

				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			// check nothing has changed
			ListResponse<Favourite> resp = getFavourites(networkId, runAsUserId, personId, 0, Integer.MAX_VALUE, null, null, type);
			List<Favourite> favourites = resp.getList();
			assertEquals(size, favourites.size());
		}
	}
	
	private Favourite deleteFavourite(String networkId, String runAsUserId, String personId, TARGET_TYPE type) throws Exception
	{
		Exception e = null;

		publicApiClient.setRequestContext(new RequestContext(networkId, runAsUserId));

		// get a favourite id
		ListResponse<Favourite> resp = getFavourites(networkId, runAsUserId, personId, 0, Integer.MAX_VALUE, null, null, type);
		List<Favourite> favourites = resp.getList();
		int size = favourites.size();
		assertTrue(size > 0);
		Favourite favourite = favourites.get(0);

		try
		{
			// catch 404's
			favouritesProxy.removeFavourite(personId, favourite.getTargetGuid());
		}
		catch(PublicApiException exc)
		{
			e = exc;
		}

		// check favourite has been removed
		resp = getFavourites(networkId, runAsUserId, personId, 0, Integer.MAX_VALUE, null, null, type);
		favourites = resp.getList();
		boolean stillExists = false;
		for(Favourite f : favourites)
		{
			if(f.getTargetGuid().equals(favourite.getTargetGuid()))
			{
				stillExists = true;
				break;
			}
		}
		assertFalse(stillExists);

		if(e != null)
		{
			throw e;
		}
		
		return favourite;
	}

	private ListResponse<Favourite> getFavourites(String networkId, String runAsUserId, String personId, int skipCount, int maxItems, Integer total,
			Integer expectedTotal, TARGET_TYPE type) throws PublicApiException, ParseException
	{
		publicApiClient.setRequestContext(new RequestContext(networkId, runAsUserId));

		Paging paging = null;
		if(total == null && expectedTotal == null)
		{
			paging = getPaging(skipCount, maxItems);
		}
		else
		{
			paging = getPaging(skipCount, maxItems, total, expectedTotal);
		}
		Map<String, String> params = null;
		if(type != null)
		{
			params = Collections.singletonMap("where", "(EXISTS(target/" + type + "))");
		}
		ListResponse<Favourite> resp = favouritesProxy.getFavourites(personId, createParams(paging, params));
		return resp;
	}
	
	private Favourite makeFolderFavourite(String targetGuid) throws ParseException
	{
		Folder folder = new Folder(targetGuid);
		FolderFavouriteTarget target = new FolderFavouriteTarget(folder);
		Date creationData = new Date();
		Favourite favourite = new Favourite(creationData, null, target);
		return favourite;
	}

	private Favourite makeFileFavourite(String targetGuid) throws ParseException
	{
		Document document = new Document(targetGuid);
		FileFavouriteTarget target = new FileFavouriteTarget(document);
		Date creationData = new Date();
		Favourite favourite = new Favourite(creationData, null, target);
		return favourite;
	}
	
	private Favourite makeSiteFavourite(Site site) throws ParseException
	{
		SiteFavouriteTarget target = new SiteFavouriteTarget(site);
		Date creationDate = new Date();
		Favourite favourite = new Favourite(creationDate, null, target);
		return favourite;
	}

	@Test
	public void testInvalidRequests() throws Exception
	{
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			Favourite favourite = makeSiteFavourite(person1PublicSites.get(0));
			Favourite ret = favouritesProxy.createFavourite(person11Id, favourite);
			favourite.expected(ret);
			fail();
		}
		catch(PublicApiException e)
		{
			// Note: un-authorized comes back as 404
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// cloud-2468
		// invalid type
		
		try
		{
			log("cloud-2468");

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
			
			final TestSite site = personSites.get(0);
			final WikiPageInfo wiki = TenantUtil.runAsUserTenant(new TenantRunAsWork<WikiPageInfo>()
			{
				@Override
				public WikiPageInfo doWork() throws Exception
				{
					WikiPageInfo wiki = repoService.createWiki(site.getSiteId(), GUID.generate(), GUID.generate());
					return wiki;
				}
			}, person10Id, network1.getId());

			final String guid = wiki.getNodeRef().getId();
			JSONAble wikiJSON = new JSONAble()
			{
				@SuppressWarnings("unchecked")
				@Override
				public JSONObject toJSON()
				{
					JSONObject json = new JSONObject();
					json.put("guid", guid);
					return json;
				}
			};

			FavouritesTarget target = new InvalidFavouriteTarget("wiki", wikiJSON, guid);
			Favourite favourite = new Favourite(target);

			favouritesProxy.createFavourite(person10Id, favourite);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}

		try
		{
			log("cloud-2468");

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			Site site = person1PublicSites.get(0);
			FavouritesTarget target = new InvalidFavouriteTarget(GUID.generate(), site, site.getGuid());
			Favourite favourite = new Favourite(target);

			favouritesProxy.createFavourite(person10Id, favourite);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}

		// type = file, target is a site
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			String siteGuid = person1PublicSites.get(0).getGuid();
	    	Document document = new Document(siteGuid);
			Favourite favourite = makeFileFavourite(document.getGuid());
			Favourite ret = favouritesProxy.createFavourite(person10Id, favourite);
			favourite.expected(ret);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// type = folder, target is a site
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			String siteGuid = person1PublicSites.get(0).getGuid();
	    	Folder folder = new Folder(siteGuid);
	    	Favourite favourite = makeFolderFavourite(folder.getGuid());
			Favourite ret = favouritesProxy.createFavourite(person10Id, favourite);
			favourite.expected(ret);
			
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// type = folder, target is a file
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			Folder folder = new Folder(person1PublicDocs.get(0).getId());
	    	Favourite favourite = makeFolderFavourite(folder.getGuid());
			Favourite ret = favouritesProxy.createFavourite(person10Id, favourite);
			favourite.expected(ret);
			
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// type = file, target is a folder
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			Document document = new Document(person1PublicFolders.get(0).getId());
			Favourite favourite = makeFileFavourite(document.getGuid());
			Favourite ret = favouritesProxy.createFavourite(person10Id, favourite);
			favourite.expected(ret);
			
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
    	// make sure that a user can't favourite on behalf of another user
		// 2471
		{
			log("cloud-2471");

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
				
				Document document = new Document(person1PublicDocs.get(0).getId());
				Favourite favourite = makeFileFavourite(document.getGuid());
				favouritesProxy.createFavourite(person11Id, favourite);
	
				fail();
			}
			catch(PublicApiException e)
			{
				// Note: un-authorized comes back as 404
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			// person1 should have no favourites
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));
			ListResponse<Favourite> response = favouritesProxy.getFavourites(person11Id, createParams(null, null));
			assertEquals(0, response.getList().size());
		}
		
		// invalid/non-existent user
		// 2469
		try
		{
			log("cloud-2469");

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			Favourite favourite = makeSiteFavourite(personSites.get(0));
			Favourite ret = favouritesProxy.createFavourite(GUID.generate(), favourite);
			favourite.expected(ret);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
    	// make sure that a user can't see other user's favourites.
		// 2465
		try
		{
			log("cloud-2465");

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
			favouritesProxy.getFavourites(person11Id, null);
			fail();
		}
		catch(PublicApiException e)
		{
			// Note: un-authorized comes back as 404
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// 2464, unknown user
		try
		{
			log("cloud-2464");

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
			favouritesProxy.getFavourites(GUID.generate(), null);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// non-existent entity for a given type
		// 2480
		{
			log("cloud-2480");

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
	
				SiteImpl site = new SiteImpl();
				site.setGuid(GUID.generate());
				Favourite favourite = makeSiteFavourite((Site)site);
				favouritesProxy.createFavourite(person10Id, favourite);
				
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			ListResponse<Favourite> response = favouritesProxy.getFavourites(person10Id, createParams(null, null));
			assertEquals(0, response.getList().size());
		}
		
		{
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
	
				Document document = new Document(GUID.generate());
				Favourite favourite = makeFileFavourite(document.getGuid());
				favouritesProxy.createFavourite(person10Id, favourite);
	
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			ListResponse<Favourite> response = favouritesProxy.getFavourites(person10Id, createParams(null, null));
			assertEquals(0, response.getList().size());
		}

		{
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
	
				Folder folder = new Folder(GUID.generate());
		    	Favourite favourite = makeFolderFavourite(folder.getGuid());
				favouritesProxy.createFavourite(person10Id, favourite);
	
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			ListResponse<Favourite> response = favouritesProxy.getFavourites(person10Id, createParams(null, null));
			assertEquals(0, response.getList().size());
		}
		
		// 2470
		// incorrect type for a given favourite target
		{
			log("cloud-2470");

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Site site = person1PublicSites.get(0);
				FavouritesTarget target = new InvalidFavouriteTarget("folder", site, site.getGuid());
				Favourite favourite = new Favourite(target);
	
				favouritesProxy.createFavourite(person10Id, favourite);

				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Site site = person1PublicSites.get(0);
				FavouritesTarget target = new InvalidFavouriteTarget("file", site, site.getGuid());
				Favourite favourite = new Favourite(target);
	
				favouritesProxy.createFavourite(person10Id, favourite);
				
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			Document document = new Document(person1PublicDocs.get(0).getId());

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				FavouritesTarget target = new InvalidFavouriteTarget("site", document, document.getGuid());
				Favourite favourite = new Favourite(target);
	
				favouritesProxy.createFavourite(person10Id, favourite);
	
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				FavouritesTarget target = new InvalidFavouriteTarget("folder", document, document.getGuid());
				Favourite favourite = new Favourite(target);
	
				favouritesProxy.createFavourite(person10Id, favourite);
	
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			Folder folder = new Folder(person1PublicFolders.get(0).getId());

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				FavouritesTarget target = new InvalidFavouriteTarget("site", folder, folder.getGuid());
				Favourite favourite = new Favourite(target);
	
				favouritesProxy.createFavourite(person10Id, favourite);
	
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				FavouritesTarget target = new InvalidFavouriteTarget("file", folder, folder.getGuid());
				Favourite favourite = new Favourite(target);
	
				favouritesProxy.createFavourite(person10Id, favourite);
	
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			// none of these POSTs should have resulted in favourites being created...
			ListResponse<Favourite> response = favouritesProxy.getFavourites(person10Id, createParams(null, null));
			assertEquals(0, response.getList().size());
		}
		
		// invalid methods
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			try
			{
				Favourite favourite = new Favourite(null);
				favouritesProxy.update("people", "-me-", "favorites", null, favourite.toJSON().toString(), "Unable to PUT favourites");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
		}
	}
	
	@Test
	public void testValidRequests() throws Exception
	{
		List<Favourite> expectedFavourites = new ArrayList<Favourite>();

		{
			// add some favourites
			// 2467

			log("cloud-2467");
			
			Favourite siteFavourite1 = makeSiteFavourite(person1PublicSites.get(0));
			
			Document document = repoService.getDocument(network1.getId(), person1PublicDocs.get(0));
			Favourite fileFavourite1 = makeFileFavourite(document.getGuid());
			
			Folder folder = repoService.getFolder(network1.getId(), person1PublicFolders.get(0));
	    	Favourite folderFavourite1 = makeFolderFavourite(folder.getGuid());
	    	
			Favourite siteFavourite2 = makeSiteFavourite(person1PublicSites.get(1));
			
			document = repoService.getDocument(network1.getId(), person1PublicDocs.get(1));
			Favourite fileFavourite2 = makeFileFavourite(document.getGuid());
			
			folder = repoService.getFolder(network1.getId(), person1PublicFolders.get(1));
	    	Favourite folderFavourite2 = makeFolderFavourite(folder.getGuid());
			
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Favourite ret = favouritesProxy.createFavourite(person10Id, siteFavourite1);
				expectedFavourites.add(ret);
				siteFavourite1.expected(ret);
			}

			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Favourite ret = favouritesProxy.createFavourite(person10Id, fileFavourite1);
				expectedFavourites.add(ret);
				fileFavourite1.expected(ret);
			}

			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Favourite ret = favouritesProxy.createFavourite(person10Id, folderFavourite1);
				expectedFavourites.add(ret);
				folderFavourite1.expected(ret);
			}

			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
	
				Favourite ret = favouritesProxy.createFavourite(person10Id, siteFavourite2);
				expectedFavourites.add(ret);
				siteFavourite2.expected(ret);
			}
			
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Favourite ret = favouritesProxy.createFavourite(person10Id, fileFavourite2);
				expectedFavourites.add(ret);
				fileFavourite2.expected(ret);
			}
			
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Favourite ret = favouritesProxy.createFavourite(person10Id, folderFavourite2);
				expectedFavourites.add(ret);
				folderFavourite2.expected(ret);
			}
			
			// already a favourite - 201
			{
				log("cloud-2472");

				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
		
					Favourite ret = favouritesProxy.createFavourite(person10Id, siteFavourite1);
					siteFavourite1.expected(ret);
				}

				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
		
					Favourite ret = favouritesProxy.createFavourite(person10Id, folderFavourite1);
					folderFavourite1.expected(ret);
				}
				
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

					Favourite ret = favouritesProxy.createFavourite(person10Id, fileFavourite1);
					fileFavourite1.expected(ret);
				}
			}
			
			{
				// cloud-2498
				// cloud-2499
				// create and list favourites across networks
				
				List<Favourite> person21ExpectedFavourites = new ArrayList<Favourite>();

				log("cloud-2498");
				log("cloud-2499");

				{
					// favourite a site in another network

					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21Id));

					Favourite favourite = makeSiteFavourite(person1PrivateSites.get(0));
					try
					{
						favouritesProxy.createFavourite("-me-", favourite);
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
					}

					int skipCount = 0;
					int maxItems = 10;
					Paging paging = getPaging(skipCount, maxItems, person21ExpectedFavourites.size(), person21ExpectedFavourites.size());
					try
					{
						favouritesProxy.getFavourites("-me-", createParams(paging, null));
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
					}
				}

				// favourite a document in another network
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21Id));

					Document document1 = new Document(person1PrivateDocs.get(0).getId());
					Favourite favourite = makeFileFavourite(document1.getGuid());
					try
					{
						favouritesProxy.createFavourite("-me-", favourite);
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
					}

					sort(person21ExpectedFavourites, FavouritesService.DEFAULT_SORT_PROPS);

					int skipCount = 0;
					int maxItems = 10;
					Paging paging = getPaging(skipCount, maxItems, person21ExpectedFavourites.size(), person21ExpectedFavourites.size());
					try
					{
						favouritesProxy.getFavourites("-me-", createParams(paging, null));
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
					}
				}
				
				// favourite a folder in another network
				{
					publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21Id));

					Folder folder1 = new Folder(person1PrivateFolders.get(0).getId());
			    	Favourite favourite = makeFolderFavourite(folder1.getGuid());
			    	try
			    	{
			    		favouritesProxy.createFavourite("-me-", favourite);
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
					}

					sort(person21ExpectedFavourites, FavouritesService.DEFAULT_SORT_PROPS);

					int skipCount = 0;
					int maxItems = 10;
					Paging paging = getPaging(skipCount, maxItems, person21ExpectedFavourites.size(), person21ExpectedFavourites.size());
					try
					{
						favouritesProxy.getFavourites("-me-", createParams(paging, null));
						fail();
					}
					catch(PublicApiException e)
					{
						assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
					}
				}
			}
		}

		// GET favourites
		// test paging and sorting
		{
			// cloud-2458
			// cloud-2462
			// cloud-2461
			{
				log("cloud-2458");
				log("cloud-2461");
				log("cloud-2462");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
	
				List<Favourite> expected = new ArrayList<Favourite>(expectedFavourites);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedFavourites.size(), expectedFavourites.size());
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, null));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
			
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
	
				List<Favourite> expected = new ArrayList<Favourite>(expectedFavourites);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 2;
				int maxItems = 4;
				Paging paging = getPaging(skipCount, maxItems, expectedFavourites.size(), expectedFavourites.size());
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, null));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
			
			// 2466
			// GET favourites for "-me-"
			{
				log("cloud-2466");

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
	
				List<Favourite> expected = new ArrayList<Favourite>(expectedFavourites);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavourites.size(), expectedFavourites.size());
				ListResponse<Favourite> resp = favouritesProxy.getFavourites("-me-", createParams(paging, null));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
		}

		// 2459
		{
			log("cloud-2459");

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));

			int skipCount = 0;
			int maxItems = Integer.MAX_VALUE;
			Paging paging = getPaging(skipCount, maxItems, 0, 0);
			ListResponse<Favourite> resp = favouritesProxy.getFavourites(person11Id, createParams(paging, null));
			List<Favourite> empty = Collections.emptyList();
			checkList(empty, paging.getExpectedPaging(), resp);
		}

		// cloud-2460: filtering by target type
		{
			log("cloud-2460");

			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Set<Type> types = new HashSet<Type>(Arrays.asList(Type.FILE));
				List<Favourite> expected = filter(expectedFavourites, types);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);
				
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expected.size(), expected.size());
				Map<String, String> params = Collections.singletonMap("where", "(EXISTS(target/file))");
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, params));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
			
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Set<Type> types = new HashSet<Type>(Arrays.asList(Type.FOLDER));
				List<Favourite> expected = filter(expectedFavourites, types);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expected.size(), expected.size());
				Map<String, String> params = Collections.singletonMap("where", "(EXISTS(target/folder))");
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, params));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}

			// target/file
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Set<Type> types = new HashSet<Type>(Arrays.asList(Type.FILE));
				List<Favourite> expected = filter(expectedFavourites, types);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expected.size(), expected.size());
				Map<String, String> params = Collections.singletonMap("where", "(EXISTS(target/file))");
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, params));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}

			// target/folder
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Set<Type> types = new HashSet<Type>(Arrays.asList(Type.FOLDER));
				List<Favourite> expected = filter(expectedFavourites, types);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expected.size(), expected.size());
				Map<String, String> params = Collections.singletonMap("where", "(EXISTS(target/folder))");
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, params));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}

			// target/site
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Set<Type> types = new HashSet<Type>(Arrays.asList(Type.SITE));
				List<Favourite> expected = filter(expectedFavourites, types);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expected.size(), expected.size());
				Map<String, String> params = Collections.singletonMap("where", "(EXISTS(target/site))");
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, params));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}

			// target/folder OR target/file.
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Set<Type> types = new HashSet<Type>(Arrays.asList(Type.FOLDER, Type.FILE));
				List<Favourite> expected = filter(expectedFavourites, types);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expected.size(), expected.size());
				Map<String, String> params = Collections.singletonMap("where", "(EXISTS(target/file) OR EXISTS(target/folder))");
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, params));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}

			// target/site OR target/file.
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Set<Type> types = new HashSet<Type>(Arrays.asList(Type.SITE, Type.FILE));
				List<Favourite> expected = filter(expectedFavourites, types);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expected.size(), expected.size());
				Map<String, String> params = Collections.singletonMap("where", "(EXISTS(target/file) OR EXISTS(target/site))");
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, params));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}

			// target/site OR target/folder.
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

				Set<Type> types = new HashSet<Type>(Arrays.asList(Type.SITE, Type.FOLDER));
				List<Favourite> expected = filter(expectedFavourites, types);
				sort(expected, FavouritesService.DEFAULT_SORT_PROPS);

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expected.size(), expected.size());
				Map<String, String> params = Collections.singletonMap("where", "(EXISTS(target/site) OR EXISTS(target/folder))");
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person10Id, createParams(paging, params));
				checkList(expected.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
		}

		// GET a favourite
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			favouritesProxy.getFavourite(person10Id, GUID.generate());
			
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			Favourite favourite = expectedFavourites.get(0);

			favouritesProxy.getFavourite(GUID.generate(), favourite.getTargetGuid());

			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			Favourite favourite = expectedFavourites.get(0);

			Favourite resp = favouritesProxy.getFavourite(person10Id, favourite.getTargetGuid());
			favourite.expected(resp);
		}
		
		// cloud-2479, PUT case
		{
			log("cloud-2479.1");
			updateFavourite(network1.getId(), person10Id, person10Id, TARGET_TYPE.site);
			
			log("cloud-2479.2");
			updateFavourite(network1.getId(), person10Id, person10Id, TARGET_TYPE.file);
			
			log("cloud-2479.3");
			updateFavourite(network1.getId(), person10Id, person10Id, TARGET_TYPE.folder);
		}
		
		try
		{
			// cloud-2474
			// non-existent personId
			log("cloud-2474");

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			favouritesProxy.removeFavourite(GUID.generate(), GUID.generate());
			
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// cloud-2475
		// try delete a non-existent favourite for a node that exists
		{
			log("cloud-2475");

			NodeRef doc = TenantUtil.runAsUserTenant(new TenantRunAsWork<NodeRef>()
			{
				@Override
				public NodeRef doWork() throws Exception
				{
					NodeRef containerNodeRef = person1PublicSites.get(0).getContainerNodeRef("documentLibrary");
					NodeRef doc = repoService.createDocument(containerNodeRef, GUID.generate(), "");
					return doc;
				}
			}, person11Id, network1.getId());

			String favouriteId = doc.getId();

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

			ListResponse<Favourite> before = getFavourites(network1.getId(), person10Id, person10Id, 0, Integer.MAX_VALUE, null, null, null);
			List<Favourite> beforeList = before.getList();
			assertTrue(beforeList.size() > 0);

			try
			{
				favouritesProxy.removeFavourite(person10Id, favouriteId);
				fail("Should be a 404");
			}
			catch(PublicApiException e)
			{
				// expected
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			// check no favourites have been removed
			ListResponse<Favourite> after = getFavourites(network1.getId(), person10Id, person10Id, 0, Integer.MAX_VALUE, null, null, null);
			assertEquals(beforeList.size(), after.getList().size());
		}
		
		// cloud-2473, DELETE case
		{
			log("cloud-2473.1");
			deleteFavourite(network1.getId(), person10Id, person10Id, TARGET_TYPE.site);

			log("cloud-2473.2");
			deleteFavourite(network1.getId(), person10Id, person10Id, TARGET_TYPE.file);

			log("cloud-2473.3");
			Favourite favourite = deleteFavourite(network1.getId(), person10Id, person10Id, TARGET_TYPE.folder);
			
			// try to delete non-existent favourite
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));
				favouritesProxy.removeFavourite(person10Id, favourite.getTargetGuid());
				
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
		}

		// cloud-2476
		// try delete another user's favourite
		{
			log("cloud-2476");

			// make sure there are favourites to delete
//			publicApiClient.setRequestContext(new RequestContext(network1.getId(), personId));
//			SiteFavouriteTarget target = new SiteFavouriteTarget(person1Sites.get(0));
//			Favourite favourite = new Favourite(target);
//			favouritesProxy.createFavourite(personId, favourite);

			ListResponse<Favourite> before = getFavourites(network1.getId(), person10Id, person10Id, 0, Integer.MAX_VALUE, null, null, null);
			assertTrue(before.getList().size() > 0);
			Favourite favourite = before.getList().get(0);

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11Id));
				favouritesProxy.removeFavourite(person10Id, favourite.getTargetGuid());

				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			ListResponse<Favourite> after = getFavourites(network1.getId(), person10Id, person10Id, 0, Integer.MAX_VALUE, null, null, null);
			assertEquals(before.getList().size(), after.getList().size());
		}
	}

	@Test
	public void testPUBLICAPI141() throws Exception
	{
		final TestSite publicSite = person1PublicSites.get(0); // person1's public site
		final TestSite publicSite1 = person1PublicSites.get(1); // person1's public site
		final TestSite privateSite = person1PrivateSites.get(0); // person1's private site
		final NodeRef folderNodeRef = person1PublicFolders.get(0); // person1's folder
		final NodeRef nodeRef = person1PublicDocs.get(1); // a file in the folder
		final List<Favourite> expectedFavourites = new ArrayList<Favourite>();

		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12Id));

			// invite to another user's public site
			SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
			siteMembershipRequest.setId(publicSite.getSiteId());
			siteMembershipRequest.setMessage("Please can I join your site?");
			siteMembershipRequestsProxy.createSiteMembershipRequest(person12Id, siteMembershipRequest);

			// favourite other users site, folder and file
			Favourite folderFavourite = makeFolderFavourite(folderNodeRef.getId());
			favouritesProxy.createFavourite(person12Id, folderFavourite);

			Favourite fileFavourite = makeFileFavourite(nodeRef.getId());
			favouritesProxy.createFavourite(person12Id, fileFavourite);
			
			final Favourite siteFavourite = makeSiteFavourite(publicSite);
			favouritesProxy.createFavourite(person12Id, siteFavourite);
			expectedFavourites.add(siteFavourite);

			final Favourite siteFavourite1 = makeSiteFavourite(publicSite1);
			favouritesProxy.createFavourite(person12Id, siteFavourite1);
			expectedFavourites.add(siteFavourite1);

			sort(expectedFavourites, FavouritesService.DEFAULT_SORT_PROPS);

			// move the folder and file to person1's private site
			TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					NodeRef documentLibraryNodeRef = privateSite.getContainerNodeRef("documentLibrary");
					repoService.moveNode(folderNodeRef, documentLibraryNodeRef);

					return null;
				}
			}, person11Id, network1.getId());
			
			try
			{
				favouritesProxy.getFavourite(person12Id, folderFavourite.getTargetGuid());
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			try
			{
				favouritesProxy.getFavourite(person12Id, fileFavourite.getTargetGuid());
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}
			
			{
				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavourites.size(), expectedFavourites.size());
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person12Id, createParams(paging, null));
				checkList(sublist(expectedFavourites, skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
			
			// make the public sites private
			TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					publicSite.setSiteVisibility(SiteVisibility.PRIVATE);
					publicSite1.setSiteVisibility(SiteVisibility.PRIVATE);
					
					return null;
				}
			}, person11Id, network1.getId());
			expectedFavourites.remove(siteFavourite1);

			// Given that person2Id is still a member of 'publicSite', they should still have access and therefore
			// it should show up in their favourites. But person2Id is not a member of 'publicSite1', they should 
			// not have access and therefore it should not show up in their favourites.
			{
				Favourite actual = favouritesProxy.getFavourite(person12Id, siteFavourite.getTargetGuid());
				siteFavourite.expected(actual);

				try
				{
					favouritesProxy.getFavourite(person12Id, siteFavourite1.getTargetGuid());
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
				}

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavourites.size(), expectedFavourites.size());
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person12Id, createParams(paging, null));
				checkList(sublist(expectedFavourites, skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
		}
	}
	
	@Test
	public void testPUBLICAPI156() throws Exception
	{
		final TestSite publicSite = person1PublicSites.get(0); // person1's public site
		final TestSite publicSite1 = person1PublicSites.get(1); // person1's public site
		final NodeRef folderNodeRef = person1PublicFolders.get(0); // person1's folder
		final NodeRef nodeRef = person1PublicDocs.get(1); // a file in the folder
		final List<Favourite> expectedFavourites = new ArrayList<Favourite>();

		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14Id));

			// invite to another user's public site
			SiteMembershipRequest siteMembershipRequest = new SiteMembershipRequest();
			siteMembershipRequest.setId(publicSite.getSiteId());
			siteMembershipRequest.setMessage("Please can I join your site?");
			siteMembershipRequestsProxy.createSiteMembershipRequest(person14Id, siteMembershipRequest);

			// favourite other users site, folder and file
			Favourite folderFavourite = makeFolderFavourite(folderNodeRef.getId());
			favouritesProxy.createFavourite(person14Id, folderFavourite);
			expectedFavourites.add(folderFavourite);

			Favourite fileFavourite = makeFileFavourite(nodeRef.getId());
			favouritesProxy.createFavourite(person14Id, fileFavourite);
			expectedFavourites.add(fileFavourite);
			
			final Favourite siteFavourite = makeSiteFavourite(publicSite);
			favouritesProxy.createFavourite(person14Id, siteFavourite);
			expectedFavourites.add(siteFavourite);

			final Favourite siteFavourite1 = makeSiteFavourite(publicSite1);
			favouritesProxy.createFavourite(person14Id, siteFavourite1);
			expectedFavourites.add(siteFavourite1);

			sort(expectedFavourites, FavouritesService.DEFAULT_SORT_PROPS);

			// remove the folder and file
			TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					repoService.deleteNode(nodeRef);
					repoService.deleteNode(folderNodeRef);

					return null;
				}
			}, person11Id, network1.getId());

			expectedFavourites.remove(folderFavourite);
			expectedFavourites.remove(fileFavourite);
			sort(expectedFavourites, FavouritesService.DEFAULT_SORT_PROPS);

			// GETs should not return the favourites nor error
			{
				try
				{
					favouritesProxy.getFavourite(person14Id, folderFavourite.getTargetGuid());
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
				}
				
				try
				{
					favouritesProxy.getFavourite(person14Id, fileFavourite.getTargetGuid());
				}
				catch(PublicApiException e)
				{
					assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
				}

				int skipCount = 0;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedFavourites.size(), expectedFavourites.size());
				ListResponse<Favourite> resp = favouritesProxy.getFavourites(person14Id, createParams(paging, null));
				checkList(sublist(expectedFavourites, skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
		}
	}
}