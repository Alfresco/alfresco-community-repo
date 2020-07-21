/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Favourites;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.SiteMembershipRequests;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Comment;
import org.alfresco.rest.api.tests.client.data.Favourite;
import org.alfresco.rest.api.tests.client.data.FavouriteDocument;
import org.alfresco.rest.api.tests.client.data.FavouriteFolder;
import org.alfresco.rest.api.tests.client.data.FavouriteNode;
import org.alfresco.rest.api.tests.client.data.FavouritesTarget;
import org.alfresco.rest.api.tests.client.data.FileFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.FolderFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.InvalidFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.JSONAble;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.PathInfo;
import org.alfresco.rest.api.tests.client.data.Site;
import org.alfresco.rest.api.tests.client.data.SiteFavouriteTarget;
import org.alfresco.rest.api.tests.client.data.SiteImpl;
import org.alfresco.rest.api.tests.client.data.SiteMembershipRequest;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
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
public class TestFavourites extends AbstractBaseApiTest
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
    private TestPerson person15;
    private String person15Id;

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

    @Override
    @Before
    public void setup() throws Exception
    {
        // init networks
        super.setup();

        final Iterator<TestNetwork> networksIt = getTestFixture().networksIterator();

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
                    name = GUID.generate();
                    personInfo = new PersonInfo(name, name, name, "password", null, null, null, null, null, null, null);
                    TestFavourites.this.person15 = network1.createUser(personInfo);
                    assertNotNull(TestFavourites.this.person15);
                    TestFavourites.this.person15Id = TestFavourites.this.person15.getId();

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
     * @param favourites List<Favourite>
     * @param types Set<Type>
     * @return ArrayList<Favourite>
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
        FavouriteFolder folder = new FavouriteFolder(targetGuid);
        FolderFavouriteTarget target = new FolderFavouriteTarget(folder);
        Date creationData = new Date();
        Favourite favourite = new Favourite(creationData, null, target, null);
        return favourite;
    }

    private Favourite makeFileFavourite(String targetGuid) throws ParseException
    {
        FavouriteDocument document = new FavouriteDocument(targetGuid);
        FileFavouriteTarget target = new FileFavouriteTarget(document);
        Date creationData = new Date();
        Favourite favourite = new Favourite(creationData, null, target, null);
        return favourite;
    }

    private Favourite makeSiteFavourite(Site site) throws ParseException
    {
        SiteFavouriteTarget target = new SiteFavouriteTarget(site);
        Date creationDate = new Date();
        Favourite favourite = new Favourite(creationDate, null, target, null);
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
        // NOTE: The test below has swapped to attempt to favorite a comment rather than a
        //       a wiki page as the WikiService has moved to the Share Services AMP in 5.1

        try
        {
            log("cloud-2468");

            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

            final NodeRef document = personDocs.get(0);
            final NodeRef comment = TenantUtil.runAsUserTenant(new TenantRunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork() throws Exception
                {
                    NodeRef comment = repoService.createComment(document, new Comment("Title", "Content"));
                    return comment;
                }
            }, person10Id, network1.getId());

            final String guid = comment.getId();
            JSONAble commentJSON = new JSONAble()
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

            FavouritesTarget target = new InvalidFavouriteTarget("comment", commentJSON, guid);
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
            FavouriteDocument document = new FavouriteDocument(siteGuid);
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
            FavouriteFolder folder = new FavouriteFolder(siteGuid);
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

            FavouriteFolder folder = new FavouriteFolder(person1PublicDocs.get(0).getId());
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

            FavouriteDocument document = new FavouriteDocument(person1PublicFolders.get(0).getId());
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

                FavouriteDocument document = new FavouriteDocument(person1PublicDocs.get(0).getId());
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

                FavouriteDocument document = new FavouriteDocument(GUID.generate());
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

                FavouriteFolder folder = new FavouriteFolder(GUID.generate());
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
                assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
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
                assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
            }

            FavouriteDocument document = new FavouriteDocument(person1PublicDocs.get(0).getId());

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

            FavouriteFolder folder = new FavouriteFolder(person1PublicFolders.get(0).getId());

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

        // invalid orderBy param
        {
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person10Id));

            try
            {
                Map<String, String> params = new HashMap<>();
                params.put("orderBy", "invalid ASC");
                favouritesProxy.getFavourites(person10Id, createParams(null, params));
            }
            catch(PublicApiException e)
            {
                assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
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

            FavouriteDocument document = repoService.getDocument(network1.getId(), person1PublicDocs.get(0));
            Favourite fileFavourite1 = makeFileFavourite(document.getGuid());

            FavouriteFolder folder = repoService.getFolder(network1.getId(), person1PublicFolders.get(0));
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

                    FavouriteDocument document1 = new FavouriteDocument(person1PrivateDocs.get(0).getId());
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

                    FavouriteFolder folder1 = new FavouriteFolder(person1PrivateFolders.get(0).getId());
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

    /**
     * Tests get favourites with 'include' parameter.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/people/<userName>/favorites?include=path}
     */
    @Test
    public void testGetFavouritesWithPath() throws Exception
    {
        // As person12 user
        setRequestContext(network1.getId(), person12Id, "password");

        final NodeRef folderNodeRef = person1PublicFolders.get(0); // person1's folder (Test Folder1)
        final NodeRef nodeRef = person1PublicDocs.get(1); // a file (Test Doc2) in the folder (Test Folder1)
        final TestSite publicSite = person1PublicSites.get(0); // person1's public site

        // Favourite the doc (Test Doc2)
        Favourite fileFavourite = makeFileFavourite(nodeRef.getId());
        favouritesProxy.createFavourite(person12Id, fileFavourite);

        //Favourite the folder (Test Folder1)
        Favourite folderFavourite = makeFolderFavourite(folderNodeRef.getId());
        favouritesProxy.createFavourite(person12Id, folderFavourite);

        // Favourite the public site
        final Favourite siteFavourite = makeSiteFavourite(publicSite);
        favouritesProxy.createFavourite(person12Id, siteFavourite);

        Paging paging = getPaging(0, 100);
        Map<String, String> otherParams = Collections.singletonMap("include", "path");

        ListResponse<Favourite> resp = favouritesProxy.getFavourites(person12Id, createParams(paging, otherParams));
        List<Favourite> actualFavouritesList = resp.getList();
        assertEquals("Incorrect number of entries returned", 3, actualFavouritesList.size());

        actualFavouritesList.forEach(fav ->
        {
            FavouriteNode node;
            switch (fav.getType())
            {
                case FILE:
                {
                    node = ((FileFavouriteTarget) fav.getTarget()).getDocument();
                    assertNotNull("node is null.", node);
                    assertPathInfo(node.getPath(), "/Company Home/Sites/" + publicSite.getSiteId() + "/documentLibrary/Test Folder1", true);
                    break;
                }
                case FOLDER:
                {
                    node = ((FolderFavouriteTarget) fav.getTarget()).getFolder();
                    assertNotNull("node is null.", node);
                    assertPathInfo(node.getPath(), "/Company Home/Sites/" + publicSite.getSiteId() + "/documentLibrary", true);
                    break;
                }
                case SITE:
                {
                    JSONObject siteJsonObject = fav.getTarget().toJSON();
                    assertNotNull("There should be a site JSON object.", siteJsonObject);
                    assertNull("Path info should not be returned for sites.", siteJsonObject.get("path"));
                    break;
                }
            }
        });

        // Get favourites without 'include' option
        resp = favouritesProxy.getFavourites(person12Id, createParams(paging, null));
        actualFavouritesList = resp.getList();
        assertEquals("Incorrect number of entries returned", 3, actualFavouritesList.size());

        actualFavouritesList.forEach(fav ->
        {
            FavouriteNode node;
            switch (fav.getType())
            {
                case FILE:
                {
                    node = ((FileFavouriteTarget) fav.getTarget()).getDocument();
                    assertNotNull("node is null.", node);
                    assertNull("Path info should not be returned by default", node.getPath());
                    break;
                }
                case FOLDER:
                {
                    node = ((FolderFavouriteTarget) fav.getTarget()).getFolder();
                    assertNotNull("node is null.", node);
                    assertNull("Path info should not be returned by default", node.getPath());
                    break;
                }
                case SITE:
                {
                    JSONObject siteJsonObject = fav.getTarget().toJSON();
                    assertNotNull("There should be a site JSON object.", siteJsonObject);
                    assertNull("Path info should not be returned for sites.", siteJsonObject.get("path"));
                    break;
                }
            }
        });
    }

    /**
     * REPO-1147 Tests create and get favourite with 'include' parameter and properties.
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/people/<userName>/favorites?include=properties}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/people/<userName>/favorites/<targetId>?include=properties}
     */
    @Test
    public void testCreateAndGetFavouriteWithIncludeProperties() throws Exception
    {
        setRequestContext(network1.getId(), person11Id, "password");
        final NodeRef nodeRef1= person1PublicDocs.get(0); // a file in the site's document library (Test Doc1)

        // Favourite the doc (Test Doc1) using POST
        Favourite file1Favourite = makeFileFavourite(nodeRef1.getId());
        Favourite file1FavouriteResponse = favouritesProxy.createFavourite(person11Id, file1Favourite, null);
        assertNull("Properties should be null because they wasn't requested via include=properties", file1FavouriteResponse.getProperties());
        // Same results for GET
        file1FavouriteResponse = favouritesProxy.getFavourite(person11Id, file1FavouriteResponse.getTargetGuid(), null);
        assertNull("Properties should be null because they wasn't requested via include=properties", file1FavouriteResponse.getProperties());

        // create Favourite with include=properties in the result using POST
        Map<String, String> include = Collections.singletonMap("include", "properties");
        file1FavouriteResponse = favouritesProxy.createFavourite(person11Id, file1Favourite, include);
        assertNull("Properties should be null because all of the properties are already in the favourite target and will not be listed twice!", file1FavouriteResponse.getProperties());
        // Same results for GET
        file1FavouriteResponse = favouritesProxy.getFavourite(person11Id, file1FavouriteResponse.getTargetGuid(), include);
        assertNull("Properties should be null because all of the properties are already in the favourite target and will not be listed twice!", file1FavouriteResponse.getProperties());

        // Lock node for creating lock properties
        TenantUtil.runAsUserTenant((TenantRunAsWork<Void>) () -> {
            repoService.lockNode(nodeRef1);
            return null;
        }, person11Id, network1.getId());

        // create Favourite with include=properties in the result using POST
        file1FavouriteResponse = favouritesProxy.createFavourite(person11Id, file1Favourite, include);
        assertNotNull("Properties shouldn't be null because we created some properties while locking the file", file1FavouriteResponse.getProperties());
        // Same results for GET
        file1FavouriteResponse = favouritesProxy.getFavourite(person11Id, file1FavouriteResponse.getTargetGuid(), include);
        assertNotNull("Properties shouldn't be null because we created some properties while locking the file", file1FavouriteResponse.getProperties());
    }

    /**
     * Tests create and get favourite with 'include' parameter.
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/people/<userName>/favorites?include=path}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/people/<userName>/favorites/<targetId>?include=path}
     */
    @Test
    public void testCreateAndGetFavouriteWithPath() throws Exception
    {
        Map<String, String> includePath = Collections.singletonMap("include", "path");

        // As person12 user
        setRequestContext(network1.getId(), person12Id, "password");

        final NodeRef folderNodeRef = person1PublicFolders.get(0); // person1's folder (Test Folder1)
        final NodeRef nodeRef1= person1PublicDocs.get(0); // a file in the site's document library (Test Doc1)
        final NodeRef nodeRef2 = person1PublicDocs.get(1); // a file (Test Doc2) in the folder (Test Folder1)
        final TestSite publicSite = person1PublicSites.get(0); // person1's public site

        // Favourite the doc (Test Doc1)
        Favourite file1Favourite = makeFileFavourite(nodeRef1.getId());
        file1Favourite = favouritesProxy.createFavourite(person12Id, file1Favourite, includePath);
        FavouriteNode node = ((FileFavouriteTarget) file1Favourite.getTarget()).getDocument();
        assertPathInfo(node.getPath(), "/Company Home/Sites/" + publicSite.getSiteId() + "/documentLibrary", true);
        // Check the basic properties (REPO-2827)
        assertEquals("Test Doc1", node.getName());
        assertEquals("Test Doc1 Title", node.getTitle());
        assertEquals("Test Doc1 Description", node.getDescription());


        // Favourite the doc (Test Doc2)
        Favourite file2Favourite = makeFileFavourite(nodeRef2.getId());
        file2Favourite = favouritesProxy.createFavourite(person12Id, file2Favourite);
        node = ((FileFavouriteTarget) file2Favourite.getTarget()).getDocument();
        assertNull("Path info should not be returned by default", node.getPath());

        //Favourite the folder (Test Folder1)
        Favourite folderFavourite = makeFolderFavourite(folderNodeRef.getId());
        folderFavourite = favouritesProxy.createFavourite(person12Id, folderFavourite, includePath);
        node = ((FolderFavouriteTarget) folderFavourite.getTarget()).getFolder();
        assertPathInfo(node.getPath(), "/Company Home/Sites/" + publicSite.getSiteId() + "/documentLibrary", true);

        // Favourite the public site
        Favourite siteFavourite = makeSiteFavourite(publicSite);
        siteFavourite = favouritesProxy.createFavourite(person12Id, siteFavourite);
        JSONObject siteJsonObject = siteFavourite.getTarget().toJSON();
        assertNotNull("There should be a site JSON object.", siteJsonObject);
        assertNull("Path info should not be returned for sites.", siteJsonObject.get("path"));

        // Get single favourite (Test Doc2) with include path
        Favourite favouriteResp = favouritesProxy.getFavourite(person12Id, file2Favourite.getTargetGuid(), includePath);
        node = ((FileFavouriteTarget) favouriteResp.getTarget()).getDocument();
        assertPathInfo(node.getPath(), "/Company Home/Sites/" + publicSite.getSiteId() + "/documentLibrary/Test Folder1", true);

        favouriteResp = favouritesProxy.getFavourite(person12Id, folderFavourite.getTargetGuid(), includePath);
        node = ((FolderFavouriteTarget) favouriteResp.getTarget()).getFolder();
        assertPathInfo(node.getPath(), "/Company Home/Sites/" + publicSite.getSiteId() + "/documentLibrary", true);

        favouriteResp = favouritesProxy.getFavourite(person12Id, siteFavourite.getTargetGuid(), includePath);
        siteJsonObject = favouriteResp.getTarget().toJSON();
        assertNotNull("There should be a site JSON object.", siteJsonObject);
        assertNull("Path info should not be returned for sites.", siteJsonObject.get("path"));
    }
        
    @Test
    public void testVerifyFavorite() throws Exception
    {
        setRequestContext(network1.getId(), person12Id, "password");

        final NodeRef folderNodeRef = person1PublicFolders.get(0); // person1's folder (Test Folder1)
        final NodeRef nodeRef1= person1PublicDocs.get(0); // a file in the site's document library (Test Doc1)
        final NodeRef nodeRef2 = person1PublicDocs.get(1); // a file (Test Doc2) in the folder (Test Folder1)

        // Favourite the doc (Test Doc1)
        Favourite file1Favourite = makeFileFavourite(nodeRef1.getId());
        favouritesProxy.createFavourite(person12Id, file1Favourite);

        // Favourite the doc (Test Doc2)
        Favourite file2Favourite = makeFileFavourite(nodeRef2.getId());
        favouritesProxy.createFavourite(person12Id, file2Favourite);

        Map<String, String> params = new HashMap<>();
        params.put("include", "isFavorite");

        HttpResponse response = getAll(getNodeChildrenUrl(folderNodeRef.getId()), null, params, 200);
        List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
        assertTrue(nodes.size() == 1);
        assertTrue(nodes.get(0).getIsFavorite());
        response = getAll(getNode(nodeRef1.getId()), null, params, 200);
        Node node1 = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class);
        assertTrue(node1.getIsFavorite());
    }

    /**
     * Test sort favourites using 'orderBy' parameter.
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/people/<userName>/favorites?orderBy}
     */
    @Test
    public void testSortFavourites() throws Exception
    {
        setRequestContext(network1.getId(), person15Id, "password");

        final NodeRef folderNodeRef1 = person1PublicFolders.get(0); // person1's folder (Test Folder1)
        final NodeRef folderNodeRef2 = person1PublicFolders.get(1); // person1's folder (Test Folder2)
        final NodeRef folderNodeRef3 = person1PublicFolders.get(2); // person1's folder (Test Folder3)
        final NodeRef nodeRef1= person1PublicDocs.get(0); // a file (Test Doc1)
        final NodeRef nodeRef2 = person1PublicDocs.get(1); // a file (Test Doc2)

        // Favourite the docs and folders
        Favourite folder1Favourite = makeFolderFavourite(folderNodeRef1.getId());
        favouritesProxy.createFavourite(person15Id, folder1Favourite);
        Favourite folder2Favourite = makeFolderFavourite(folderNodeRef2.getId());
        favouritesProxy.createFavourite(person15Id, folder2Favourite);
        Favourite folder3Favourite = makeFolderFavourite(folderNodeRef3.getId());
        favouritesProxy.createFavourite(person15Id, folder3Favourite);
        Favourite file1Favourite = makeFileFavourite(nodeRef1.getId());
        favouritesProxy.createFavourite(person15Id, file1Favourite);
        Favourite file2Favourite = makeFileFavourite(nodeRef2.getId());
        favouritesProxy.createFavourite(person15Id, file2Favourite);

        // Order by title ASC
        Map<String, String> params = new HashMap<>();
        params.put("orderBy", "title ASC");

        List<Favourite> favourites = favouritesProxy.getFavourites(person15Id, createParams(null,params)).getList();
        assertTrue(favourites.size() == 5);
        assertTrue(favourites.get(0).getTargetGuid().equals(nodeRef1.getId()));
        assertTrue(favourites.get(1).getTargetGuid().equals(nodeRef2.getId()));
        assertTrue(favourites.get(2).getTargetGuid().equals(folderNodeRef1.getId()));
        assertTrue(favourites.get(3).getTargetGuid().equals(folderNodeRef2.getId()));
        assertTrue(favourites.get(4).getTargetGuid().equals(folderNodeRef3.getId()));

        // Order by type ASC, title DESC
        params = new HashMap<>();
        params.put("orderBy", "type DESC, title DESC");

        favourites = favouritesProxy.getFavourites(person15Id, createParams(null,params)).getList();
        assertTrue(favourites.size() == 5);
        assertTrue(favourites.get(0).getTargetGuid().equals(folderNodeRef3.getId()));
        assertTrue(favourites.get(1).getTargetGuid().equals(folderNodeRef2.getId()));
        assertTrue(favourites.get(2).getTargetGuid().equals(folderNodeRef1.getId()));
        assertTrue(favourites.get(3).getTargetGuid().equals(nodeRef2.getId()));
        assertTrue(favourites.get(4).getTargetGuid().equals(nodeRef1.getId()));
    }

    private void assertPathInfo(PathInfo expectedPathInfo, String expectedPathName, boolean expectedIsComplete)
    {
        assertNotNull("Path info was requested.", expectedPathInfo);
        assertEquals("IsComplete should have been true.", expectedIsComplete, expectedPathInfo.getIsComplete());
        assertEquals("Incorrect path name.", expectedPathName, expectedPathInfo.getName());

        // substring(1) -> so we can ignore the first '/'
        List<String> expectedPathElements = Arrays.asList(expectedPathName.substring(1).split("/"));
        assertEquals("Incorrect number of path elements.", expectedPathElements.size(), expectedPathInfo.getElements().size());

        AtomicInteger i = new AtomicInteger(0);
        expectedPathElements.forEach(path -> assertEquals("Incorrect path element.", path,
                    expectedPathInfo.getElements().get(i.getAndIncrement()).getName()));
    }

    @Override
    public String getScope()
    {
        return "public";
    }

}