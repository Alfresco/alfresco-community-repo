/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.CMISNodeInfoImpl;
import org.alfresco.query.PageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.activities.feed.FeedGenerator;
import org.alfresco.repo.activities.feed.cleanup.FeedCleaner;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.content.cleanup.ContentStoreCleaner;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.repo.invitation.InvitationSearchCriteriaImpl;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.node.index.NodeIndexer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteDoesNotExistException;
import org.alfresco.repo.tenant.Network;
import org.alfresco.repo.tenant.NetworksService;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.impl.node.ratings.RatingScheme;
import org.alfresco.rest.api.tests.client.data.Activity;
import org.alfresco.rest.api.tests.client.data.Comment;
import org.alfresco.rest.api.tests.client.data.Company;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.FavouriteSite;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.MemberOfSite;
import org.alfresco.rest.api.tests.client.data.NetworkImpl;
import org.alfresco.rest.api.tests.client.data.NodeRating;
import org.alfresco.rest.api.tests.client.data.NodeRating.Aggregate;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.rest.api.tests.client.data.PersonNetwork;
import org.alfresco.rest.api.tests.client.data.SiteContainer;
import org.alfresco.rest.api.tests.client.data.SiteImpl;
import org.alfresco.rest.api.tests.client.data.SiteMember;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.rest.api.tests.client.data.Tag;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria.InvitationType;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.rating.Rating;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.service.cmr.wiki.WikiService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailBean;

import com.google.common.collect.AbstractIterator;

/**
 * A convenient proxy into the repository services to support the tests.
 * 
 * @author steveglover
 *
 */
public class RepoService
{
	private static final Log logger = LogFactory.getLog(RepoService.class);

    protected static final String TEST_SITE_PRESET = "testSitePreset";

    private static final String FAVOURITE_SITES_PREFIX = "org.alfresco.share.sites.favourites.";
    private static final int FAVOURITE_SITES_PREFIX_LENGTH = FAVOURITE_SITES_PREFIX.length();
    
    protected ApplicationContext applicationContext;

    protected MutableAuthenticationService authenticationService;
    protected DictionaryService dictionaryService;
    protected SiteService siteService;
    protected ActivityService activityService;
    protected PostLookup postLookup;
    protected FeedGenerator feedGenerator;
    protected FileFolderService fileFolderService;
    protected ContentService contentService;
    protected CommentService commentService;
    protected NodeService nodeService;
    protected PreferenceService preferenceService;
    protected TaggingService taggingService;
    protected RatingService ratingService;
    protected TenantService tenantService;
    protected TenantAdminService tenantAdminService;
    protected ActivityPostDAO postDAO;
    protected JobDetailBean feedGeneratorJobDetail;
    protected JobDetailBean postLookupJobDetail;
    protected JobDetailBean feedCleanerJobDetail;
    protected JobDetailBean postCleanerJobDetail;
    protected JobDetailBean feedNotifierJobDetail;
    protected ContentStoreCleaner contentStoreCleaner;
    protected FeedCleaner feedCleaner;
    protected PersonService personService;
    protected NamedObjectRegistry<RatingScheme> nodeRatingSchemeRegistry;
	protected VersionService versionService;
	protected CheckOutCheckInService cociService;
	protected FavouritesService favouritesService;
	protected InvitationService invitationService;
	protected LockService lockService;
	protected WikiService wikiService;
	protected CMISConnector cmisConnector;
	protected NodeIndexer nodeIndexer;
	protected HiddenAspect hiddenAspect;
	protected NetworksService networksService;
	protected NamespaceService namespaceService;
	protected RetryingTransactionHelper transactionHelper;

	protected Activities activities;

	protected PublicApiTestContext publicApiContext;

	protected Random random = new Random(System.currentTimeMillis());
	
	protected static int numNetworks = 0;

	/*
	 * Represents the "system" tenant/network
	 */
	protected TestNetwork systemNetwork;

    /*
     * A map of user names (stored in the map in lower case) and TestPersons
     */
	protected Map<String, TestPerson> allPeople = new HashMap<String, TestPerson>();

	// Needed for CGLIB to create proxy, see CloudRepoService
	public RepoService()
	{
	}

    @SuppressWarnings("unchecked")
	public RepoService(ApplicationContext applicationContext) throws Exception
    {
    	this.applicationContext = applicationContext;
    	this.publicApiContext = new PublicApiTestContext(applicationContext);
    	this.authenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
    	this.siteService = (SiteService)applicationContext.getBean("SiteService");
    	this.activityService = (ActivityService)applicationContext.getBean("activityService");
    	this.fileFolderService = (FileFolderService)applicationContext.getBean("FileFolderService");
    	this.contentService = (ContentService)applicationContext.getBean("ContentService");
    	this.commentService = (CommentService)applicationContext.getBean("CommentService");
    	this.nodeService = (NodeService)applicationContext.getBean("NodeService");
    	this.preferenceService = (PreferenceService)applicationContext.getBean("PreferenceService");
    	this.taggingService = (TaggingService)applicationContext.getBean("TaggingService");
    	this.ratingService = (RatingService)applicationContext.getBean("RatingService");
    	this.tenantService = (TenantService)applicationContext.getBean("tenantService");
    	this.tenantAdminService = (TenantAdminService)applicationContext.getBean("tenantAdminService");
    	this.personService = (PersonService)applicationContext.getBean("PersonService");
    	this.contentStoreCleaner = (ContentStoreCleaner)applicationContext.getBean("contentStoreCleaner");
    	this.postDAO = (ActivityPostDAO)applicationContext.getBean("postDAO");
    	this.nodeRatingSchemeRegistry = (NamedObjectRegistry<RatingScheme>)applicationContext.getBean("nodeRatingSchemeRegistry");
    	this.cociService = (CheckOutCheckInService)applicationContext.getBean("CheckoutCheckinService");
    	this.favouritesService = (FavouritesService)applicationContext.getBean("FavouritesService");
    	this.dictionaryService =  (DictionaryService)applicationContext.getBean("dictionaryService");
    	this.invitationService = (InvitationService)applicationContext.getBean("InvitationService");
    	this.lockService = (LockService)applicationContext.getBean("LockService");
    	this.wikiService = (WikiService)applicationContext.getBean("WikiService");
    	this.cmisConnector = (CMISConnector)applicationContext.getBean("CMISConnector");
    	this.nodeIndexer = (NodeIndexer)applicationContext.getBean("nodeIndexer");
    	this.activities = (Activities)applicationContext.getBean("activities");
    	this.hiddenAspect = (HiddenAspect)applicationContext.getBean("hiddenAspect");
    	this.networksService = (NetworksService)applicationContext.getBean("networksService");
    	this.namespaceService = (NamespaceService)applicationContext.getBean("namespaceService"); 
    	this.transactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");

        Scheduler scheduler = (Scheduler)applicationContext.getBean("schedulerFactory");
    	
    	JobDetailBean contentStoreCleanerJobDetail = (JobDetailBean)applicationContext.getBean("contentStoreCleanerJobDetail");
        scheduler.pauseJob(contentStoreCleanerJobDetail.getName(), contentStoreCleanerJobDetail.getGroup());

        ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory)applicationContext.getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
        this.postLookup = (PostLookup)activitiesFeedCtx.getBean("postLookup");
        this.feedGenerator = (FeedGenerator)activitiesFeedCtx.getBean("feedGenerator");
        this.feedGeneratorJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("feedGeneratorJobDetail");
        this.postLookupJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("postLookupJobDetail");
        this.feedCleanerJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("feedCleanerJobDetail");
        this.postCleanerJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("postCleanerJobDetail");
        this.feedNotifierJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("feedNotifierJobDetail");
    	this.feedCleaner = (FeedCleaner)activitiesFeedCtx.getBean("feedCleaner");

        // Pause activities jobs so that we aren't competing with their scheduled versions
        scheduler.pauseJob(feedGeneratorJobDetail.getName(), feedGeneratorJobDetail.getGroup());
        scheduler.pauseJob(postLookupJobDetail.getName(), postLookupJobDetail.getGroup());
        scheduler.pauseJob(feedCleanerJobDetail.getName(), feedCleanerJobDetail.getGroup());
        scheduler.pauseJob(postCleanerJobDetail.getName(), postCleanerJobDetail.getGroup());
        scheduler.pauseJob(feedNotifierJobDetail.getName(), feedNotifierJobDetail.getGroup());

        this.systemNetwork = new TestNetwork(TenantService.DEFAULT_DOMAIN, true);
	}
    
    public TestNetwork getSystemNetwork()
    {
    	return systemNetwork;
    }
    
	public PublicApiTestContext getPublicApiContext()
	{
		return publicApiContext;
	}

	public void addPerson(TestPerson person)
	{
		allPeople.put(person.getId().toLowerCase(), person);
	}
	
	public TestPerson getPerson(String username)
	{
		return allPeople.get(username.toLowerCase());
	}
	
    public void disableInTxnIndexing()
    {
        nodeIndexer.setDisabled(true);
    }
    
    public int getClientVisibilityMask(Client client, Visibility visibility)
    {
        return hiddenAspect.getClientVisibilityMask(client, visibility);
    }
    
	public ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}
	
	public AssociationRef createAssociation(NodeRef sourceRef, NodeRef targetRef, QName assocTypeQName)
	{
		return nodeService.createAssociation(sourceRef, targetRef, assocTypeQName);
	}

    public String toPrefixString(QName qname)
    {
    	return qname.toPrefixString(namespaceService);
    }

	public Invitation approveSiteInvitation(String personId, String siteId)
	{
		Invitation ret = null;

		List<Invitation> invitations = invitationService.listPendingInvitationsForInvitee(personId);
		for(Invitation invitation : invitations)
		{
			if(invitation.getResourceName().equals(siteId))
			{
				ret = invitationService.approve(invitation.getInviteId(), "I accept you");
			}
		}

		return ret;
	}
	
	public void moveNode(NodeRef nodeToMoveRef, NodeRef newParentNodeRef) throws FileExistsException, FileNotFoundException
	{
		fileFolderService.move(nodeToMoveRef, newParentNodeRef, GUID.generate());
	}

	public void deleteNode(NodeRef nodeRef)
	{
		nodeService.deleteNode(nodeRef);
	}
	
	public void lockNode(NodeRef nodeRef)
	{
		lockService.lock(nodeRef, LockType.NODE_LOCK);
	}
	
	public void unlockNode(NodeRef nodeRef)
	{
		lockService.unlock(nodeRef);
	}
	
	public WikiPageInfo createWiki(String siteId, String title, String contents)
	{
		WikiPageInfo info = wikiService.createWikiPage(siteId, title, contents);
		return info;
	}
	
	public TestPerson createUser(final PersonInfo personInfo, final String username, final TestNetwork network)
	{
		return AuthenticationUtil.runAsSystem(new RunAsWork<TestPerson>()
		{
			@Override
			public TestPerson doWork() throws Exception
			{
				final TestPerson testPerson = new TestPerson(personInfo.getFirstName(), personInfo.getLastName(), username, personInfo.getPassword(),
						personInfo.getCompany(), network, personInfo.getSkype(), personInfo.getLocation(), personInfo.getTel(),
						personInfo.getMob(), personInfo.getInstantmsg(), personInfo.getGoogle());
				final Map<QName, Serializable> props = testPerson.getProperties();

				if(personService.personExists(testPerson.getId()))
				{
					AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
					{
						@Override
						public Void doWork() throws Exception
						{
							personService.deletePerson(testPerson.getId());
							return null;
						}
					});
				}

				personService.createPerson(props);

		        // create authentication to represent user
		        authenticationService.createAuthentication(username, personInfo.getPassword().toCharArray());

				log("Created person " + testPerson.getId() + (network != null ? " in network " + network : ""));

				publicApiContext.addUser(testPerson.getId());
				addPerson(testPerson);
				
				return testPerson;
			}
		});
	}

	public TestSite createSite(TestNetwork network, final SiteInformation site)
    {
		SiteInfo siteInfo = null;

		if(siteService.hasSite(site.getShortName()))
		{
			AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					siteService.deleteSite(site.getShortName());
					return null;
				}
			});
		}

    	siteInfo = siteService.createSite(TEST_SITE_PRESET, site.getShortName(), site.getTitle(), site.getDescription(), site.getSiteVisibility());
    	siteService.createContainer(site.getShortName(), "documentLibrary", ContentModel.TYPE_FOLDER, null);

    	final TestSite testSite = new TestSite(network, siteInfo);

		log("Created site " + testSite + (network != null ? " in network " + network : ""));

		return testSite;
    }

	public Invitation rejectSiteInvitation(String personId, String siteId)
	{
		Invitation ret = null;

		List<Invitation> invitations = invitationService.listPendingInvitationsForInvitee(personId);
		for(Invitation invitation : invitations)
		{
			if(invitation.getResourceName().equals(siteId))
			{
				ret = invitationService.reject(invitation.getInviteId(), "I reject you");
			}
		}
		
		return ret;
	}

	public List<Invitation> getModeratedSiteInvitations(String networkId, String runAsUserId, final String inviteeId, final String siteId)
	{
		List<Invitation> invitations = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Invitation>>()
		{
			@Override
			public List<Invitation> doWork() throws Exception
			{
				InvitationSearchCriteriaImpl searchCriteria = new InvitationSearchCriteriaImpl();
				searchCriteria.setInvitee(inviteeId);
				if(siteId != null)
				{
					searchCriteria.setResourceName(siteId);
				}
				searchCriteria.setResourceType(Invitation.ResourceType.WEB_SITE);
				searchCriteria.setInvitationType(InvitationType.MODERATED);
				List<Invitation> invitations = invitationService.searchInvitation(searchCriteria);
				return invitations;
			}
		}, runAsUserId, networkId);
		return invitations;
	}
	
	public Version getCurrentVersion(NodeRef nodeRef)
	{
		return versionService.getCurrentVersion(nodeRef);
	}

	protected void log(String msg)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug(msg);
		}
	}
	
	public SiteInfo getSiteInfo(String siteId)
	{
		return siteService.getSite(siteId);
	}
	
	public boolean isCheckedOut(NodeRef nodeRef)
	{
		return cociService.isCheckedOut(nodeRef);
	}

	public Serializable getProperty(NodeRef nodeRef, QName propertyName)
	{
		Serializable value = nodeService.getProperty(nodeRef, propertyName);
		return value;
	}
	
	public Set<QName> getAspects(NodeRef nodeRef)
	{
		Set<QName> aspects = nodeService.getAspects(nodeRef);
		return aspects;
	}

	public ContentReader getContent(NodeRef nodeRef, QName propertyQName)
	{
		ContentReader reader = contentService.getReader(nodeRef, propertyQName);
		return reader;
	}
	
	private void cleanupContent()
	{
//        eagerCleaner.setEagerOrphanCleanup(false);

		log("Cleaning up feeds...");
		
        // fire the cleaner
        feedCleaner.setMaxAgeMins(1);
        feedCleaner.setMaxFeedSize(1);
        try
        {
        	feedCleaner.execute();
        }
        catch(JobExecutionException e)
        {
        	// TODO
        }
		
		log("...done");

		log("Cleaning up content...");

        // fire the cleaner
        contentStoreCleaner.setProtectDays(0);
        contentStoreCleaner.execute();

		log("...done");
	}
	
	public void shutdown()
	{
		cleanupContent();
		publicApiContext.cleanup();
	}
	
	public Version createVersion(NodeRef nodeRef, String history, boolean majorVersion)
	{
        Map<String, Serializable> props = new HashMap<String, Serializable>(2, 1.0f);
        props.put(Version.PROP_DESCRIPTION, history);
        props.put(VersionModel.PROP_VERSION_TYPE, majorVersion ? VersionType.MAJOR : VersionType.MINOR);
		return versionService.createVersion(nodeRef, props);
	}
	
	public VersionHistory getVersionHistory(NodeRef nodeRef)
	{
		return versionService.getVersionHistory(nodeRef);
	}
	
	public Pair<String, String> splitSiteNetwork(String siteNetwork)
	{
		int idx = siteNetwork.indexOf("@");
		String siteId = siteNetwork;
		String networkId = null;
		if(idx > -1)
		{
			siteId = siteNetwork.substring(0, idx - 1);
			networkId = siteNetwork.substring(idx + 1);
		}
		return new Pair<String, String>(siteId, networkId);
	}

	private Map<String, Object> parseActivitySummary(ActivityFeedEntity entity)
	{
		String activityType = entity.getActivityType();
		String activitySummary = entity.getActivitySummary();
		JSONObject json = (JSONObject)JSONValue.parse(activitySummary);
		return Activity.getActivitySummary(json, activityType);
	}

	public List<Activity> getActivities(String personId, String siteId, boolean excludeUser, boolean excludeOthers)
	{
		List<ActivityFeedEntity> feedEntities = activityService.getUserFeedEntries(personId, siteId, excludeUser, excludeOthers, 0);
		List<Activity> activities = new ArrayList<Activity>(feedEntities.size());
		for(ActivityFeedEntity entity : feedEntities)
		{
			String siteNetwork = entity.getSiteNetwork();
			Pair<String, String> pair = splitSiteNetwork(siteNetwork);
			siteId = pair.getFirst();
			String networkId = pair.getSecond();
			String postDateStr = PublicApiDateFormat.getDateFormat().format(entity.getPostDate());
			Activity activity = new Activity(entity.getId(), networkId, siteId, entity.getFeedUserId(), entity.getPostUserId(), postDateStr, entity.getActivityType(), parseActivitySummary(entity));
			activities.add(activity);
		}

		return activities;
	}
	
	public Rating getRating(NodeRef targetNode, String scheme)
	{
		return ratingService.getRatingByCurrentUser(targetNode, scheme);
	}
	
	public List<Rating> getRatings(NodeRef targetNode)
	{
		return ratingService.getRatingsByCurrentUser(targetNode);
	}

	public RatingScheme getApiRatingScheme(String ratingSchemeName)
	{
		return nodeRatingSchemeRegistry.getNamedObject(ratingSchemeName);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getActivitySummary(ActivityFeedEntity entity) throws JSONException
	{
		Map<String, Object> activitySummary = activities.getActivitySummary(entity);
		JSONObject json = new JSONObject();
		for(String key : activitySummary.keySet())
		{
			Object value = activitySummary.get(key);
			if(value instanceof NodeRef)
			{
				value = ((NodeRef)value).getId();
			}
			json.put(key, value);
		}
		return json;
	}
	
    public void checkSiteMember(final TestPerson person, final TestSite site, SiteRole siteRole) throws Exception
    {
		assertTrue(siteService.isMember(site.getSiteId(), person.getId()));
		assertEquals(siteRole.toString(), siteService.getMembersRole(site.getSiteId(), person.getId()));
    }
    
    public void addFavouriteSite(final String username, final String siteShortName)
    {
    	SiteInfo siteInfo = siteService.getSite(siteShortName);
    	if(siteInfo != null)
    	{
    		favouritesService.addFavourite(username, siteInfo.getNodeRef());
    	}
    	else
    	{
    		throw new SiteDoesNotExistException(siteShortName);
    	}
    }

    private PagingResults<SiteInfo> getFavouriteSites(String userName, PagingRequest pagingRequest)
    {
    	final Collator collator = Collator.getInstance();

        final Set<SiteInfo> sortedFavouriteSites = new TreeSet<SiteInfo>(new Comparator<SiteInfo>()
        {
			@Override
			public int compare(SiteInfo o1, SiteInfo o2)
			{
				return collator.compare(o1.getTitle(), o2.getTitle());
			}
		});

        Map<String, Serializable> prefs = preferenceService.getPreferences(userName, FAVOURITE_SITES_PREFIX);
        for(String key : prefs.keySet())
        {
        	boolean isFavourite = false;
        	Serializable s = prefs.get(key);
        	if(s instanceof Boolean)
        	{
        		isFavourite = (Boolean)s;
        	}
        	if(isFavourite)
        	{
	        	String siteShortName = key.substring(FAVOURITE_SITES_PREFIX_LENGTH);
	        	SiteInfo siteInfo = siteService.getSite(siteShortName);
	        	if(siteInfo != null)
	        	{
	        		sortedFavouriteSites.add(siteInfo);
	        	}
        	}
        }

        int totalSize = sortedFavouriteSites.size();
        final PageDetails pageDetails = PageDetails.getPageDetails(pagingRequest, totalSize);

		final List<SiteInfo> page = new ArrayList<SiteInfo>(pageDetails.getPageSize());
		Iterator<SiteInfo> it = sortedFavouriteSites.iterator();
        for(int counter = 0; counter < pageDetails.getEnd() && it.hasNext(); counter++)
        {
        	SiteInfo favouriteSite = it.next();

			if(counter < pageDetails.getSkipCount())
			{
				continue;
			}
			
			if(counter > pageDetails.getEnd() - 1)
			{
				break;
			}

			page.add(favouriteSite);
        }

        return new PagingResults<SiteInfo>()
        {
			@Override
			public List<SiteInfo> getPage()
			{
				return page;
			}

			@Override
			public boolean hasMoreItems()
			{
				return pageDetails.hasMoreItems();
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount()
			{
				Integer total = Integer.valueOf(sortedFavouriteSites.size());
				return new Pair<Integer, Integer>(total, total);
			}

			@Override
			public String getQueryExecutionId()
			{
				return null;
			}
        };
    }
    
    public List<FavouriteSite> getFavouriteSites(TestPerson user)
    {
    	List<FavouriteSite> favouriteSites = new ArrayList<FavouriteSite>();

    	PagingResults<SiteInfo> pagingResults = getFavouriteSites(user.getId(), new PagingRequest(0, Integer.MAX_VALUE));
		for(SiteInfo siteInfo : pagingResults.getPage())
		{
			String siteId = siteInfo.getShortName();
			String siteGuid = siteInfo.getNodeRef().getId();

			TestSite site = user.getDefaultAccount().getSite(siteId);
			FavouriteSite favouriteSite = null;
			if(site.isMember(user.getId()))
			{
				favouriteSite = new FavouriteSite(site);
			}
			else
			{
				favouriteSite = new FavouriteSite(null, siteId, siteGuid, null, null, null, null, null);
			}
			favouriteSites.add(favouriteSite);
		}

		return favouriteSites;
    }
    
    public void addPreference(final String username, final String Key, final Serializable value)
    {
		Map<String, Serializable> preferences = new HashMap<String, Serializable>(1);
		preferences.put(Key, value);
    	preferenceService.setPreferences(username, preferences);
    }
    
    public List<MemberOfSite> getSiteMemberships(String personId)
    {
    	List<SiteInfo> sites = siteService.listSites(personId);
    	List<MemberOfSite> memberships = new ArrayList<MemberOfSite>();
    	for(SiteInfo siteInfo : sites)
    	{
    		String roleStr = siteService.getMembersRole(siteInfo.getShortName(), personId);
    		SiteRole role = SiteRole.valueOf(roleStr);
    		SiteImpl site = new SiteImpl(siteInfo, role, true);
    		memberships.add(new MemberOfSite(site, role));
    	}
    	return memberships;
    }

    public void generateFeed() throws JobExecutionException, SQLException
    {
    	// process all outstanding activity posts
        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                postLookup.execute();
                return null;
            }
        }, false, true);

        Long maxSequence = getMaxActivitySeq();
        while(maxSequence != null)
        {
        	feedGenerator.execute();

            maxSequence = getMaxActivitySeq();
        }
    }
    
    private Long getMaxActivitySeq() throws SQLException
    {
        Long maxSequence = transactionHelper.doInTransaction(new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                return postDAO.getMaxActivitySeq();
            }
        }, true, true);

        return maxSequence;
    }
    
    public Tag addTag(NodeRef nodeRef, String tag)
    {
		NodeRef tagNodeRef = taggingService.addTag(nodeRef, tag);
		return new Tag(nodeRef.getId(), tagNodeRef.getId(), tag);
    }
    
    public List<Tag> getTags()
    {
    	Set<Tag> tags = new TreeSet<Tag>();
    	for(String tag : taggingService.getTags(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE))
    	{
    		tags.add(new Tag(null, taggingService.getTagNodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, tag).getId(), tag));
    	}
    	return new ArrayList<Tag>(tags);
    }
    
    public List<Tag> getTags(NodeRef nodeRef)
    {
    	Set<Tag> tags = new TreeSet<Tag>();
    	for(String tag : taggingService.getTags(nodeRef))
    	{
    		tags.add(new Tag(null, taggingService.getTagNodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, tag).getId(), tag));
    	}
    	return new ArrayList<Tag>(tags);
    }
    
    /*
     * Get (CMIS) node properties
     */
    private Properties getProperties(NodeRef nodeRef)
    {
		CMISNodeInfoImpl nodeInfo = cmisConnector.createNodeInfo(nodeRef);
		final Properties properties = cmisConnector.getNodeProperties(nodeInfo, null);
		// fake the title property, which CMIS doesn't give us
		String title = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
		final PropertyStringImpl titleProp = new PropertyStringImpl(ContentModel.PROP_TITLE.toString(), title);
		Properties wrapProperties = new Properties()
		{
			@Override
			public List<CmisExtensionElement> getExtensions()
			{
				return properties.getExtensions();
			}

			@Override
			public void setExtensions(List<CmisExtensionElement> extensions)
			{
				properties.setExtensions(extensions);
			}

			@Override
			public Map<String, PropertyData<?>> getProperties()
			{
				Map<String, PropertyData<?>> updatedProperties = new HashMap<String, PropertyData<?>>(properties.getProperties());
				updatedProperties.put(titleProp.getId(), titleProp);
				return updatedProperties;
			}

			@Override
			public List<PropertyData<?>> getPropertyList()
			{
				List<PropertyData<?>> propertyList = new ArrayList<PropertyData<?>>(properties.getPropertyList());
				propertyList.add(titleProp);
				return propertyList;
			}
		};
		return wrapProperties;
    }
    
    public Document getDocument(String networkId, final NodeRef nodeRef)
    {
    	return TenantUtil.runAsSystemTenant(new TenantRunAsWork<Document>()
		{
			@Override
			public Document doWork() throws Exception
			{
				Document document = null;

		    	QName type = nodeService.getType(nodeRef);
		    	if(dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
		    	{
		    		Properties properties = getProperties(nodeRef);
		    		document = Document.getDocument(nodeRef.getId(), nodeRef.getId(), properties);
		    	}
		    	else
		    	{
		    		throw new IllegalArgumentException("Not a document node");
		    	}

		    	return document;
			}
		}, networkId);
    }
    
    public Folder getFolder(String networkId, final NodeRef nodeRef)
    {
    	return TenantUtil.runAsSystemTenant(new TenantRunAsWork<Folder>()
		{
			@Override
			public Folder doWork() throws Exception
			{
				Folder folder = null;

		    	QName type = nodeService.getType(nodeRef);
		    	if(dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER))
		    	{
		    		Properties properties = getProperties(nodeRef);
		    		folder = Folder.getFolder(nodeRef.getId(), nodeRef.getId(), properties);
		    	}
		    	else
		    	{
		    		throw new IllegalArgumentException("Not a folder node");
		    	}

		    	return folder;
			}
		}, networkId);
    }

    public List<NodeRating> getNodeRatings(String personId, String networkId, final NodeRef nodeRef)
    {
    	List<NodeRating> ratings = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<NodeRating>>()
    	{
			@Override
			public List<NodeRating> doWork() throws Exception
			{
		    	List<NodeRating> ratings = new ArrayList<NodeRating>();

		    	{
			    	Rating likesRating = ratingService.getRatingByCurrentUser(nodeRef, "likesRatingScheme");
			    	Object myRating = null;
			    	String ratedAt = null;
			    	if(likesRating != null)
			    	{
				    	myRating = likesRating.getScore() == 1.0f ? Boolean.TRUE : Boolean.FALSE;
				    	Date rateTime = likesRating.getAppliedAt();
			    		ratedAt = PublicApiDateFormat.getDateFormat().format(rateTime);
			    	}
		    		int ratingsCount = ratingService.getRatingsCount(nodeRef, "likesRatingScheme");
			    	Aggregate aggregate = new Aggregate(ratingsCount, null);
			    	ratings.add(new NodeRating(nodeRef.getId(), "likes", ratedAt, myRating, aggregate));
		    	}

		    	{
		    		Rating fiveStarRating = ratingService.getRatingByCurrentUser(nodeRef, "fiveStarRatingScheme");
			    	Object myRating = null;
			    	String ratedAt = null;
			    	if(fiveStarRating != null)
			    	{
			    		myRating = fiveStarRating.getScore();
			    		Date rateTime = fiveStarRating.getAppliedAt();
			    		ratedAt = PublicApiDateFormat.getDateFormat().format(rateTime);
			    	}
			    	int ratingsCount = ratingService.getRatingsCount(nodeRef, "fiveStarRatingScheme");
			    	float averageRating = ratingService.getAverageRating(nodeRef, "fiveStarRatingScheme");
			    	Aggregate aggregate = new Aggregate(ratingsCount, averageRating == -1 ? null : averageRating);
			    	ratings.add(new NodeRating(nodeRef.getId(), "fiveStar", ratedAt, myRating, aggregate));
		    	}

		    	return ratings;
			}
    		
    	}, personId, networkId);

    	Collections.sort(ratings);

    	return ratings;
    }

    public int numRatingSchemes()
    {
    	return ratingService.getRatingSchemes().size();
    }
    
    public NodeRef addToDocumentLibrary(TestSite site, String name, QName type)
    {
		NodeRef documentLibraryContainerNodeRef = siteService.getContainer(site.getSiteId(), "documentLibrary");
		NodeRef nodeRef = fileFolderService.create(documentLibraryContainerNodeRef, name, type).getNodeRef();
		return nodeRef;
    }

    public String getSiteNetwork(final String siteShortName, String tenantDomain)
    {
		return TenantUtil.runAsTenant(new TenantRunAsWork<String>()
		{
			@Override
			public String doWork() throws Exception
			{
		    	return tenantService.getName(siteShortName);
			}
		}, tenantDomain);
    }

    public void postActivity(final String activityType, final String siteId, final JSONObject activityData) throws JobExecutionException
    {
    	activityService.postActivity(activityType, siteId, "documentlibrary", activityData.toString());
    }
    
    public NodeRef createDocument(final NodeRef parentNodeRef, final String name, final String content)
    {
		NodeRef nodeRef = fileFolderService.create(parentNodeRef, name, ContentModel.TYPE_CONTENT).getNodeRef();
		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		writer.putContent(content);

		return nodeRef;
    }
    
    public NodeRef createDocument(final NodeRef parentNodeRef, final String name, final String title, final String description, final String content)
    {
		NodeRef nodeRef = fileFolderService.create(parentNodeRef, name, ContentModel.TYPE_CONTENT).getNodeRef();
		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		writer.putContent(content);
		nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, title);
		nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, description);

		return nodeRef;
    }
    
    public NodeRef createFolder(final NodeRef parentNodeRef, final String name)
    {
		NodeRef nodeRef = fileFolderService.create(parentNodeRef, name, ContentModel.TYPE_FOLDER).getNodeRef();
		return nodeRef;
    }
    
    public NodeRef createFolder(final NodeRef parentNodeRef, final String name, final String title, final String description)
    {
		NodeRef nodeRef = fileFolderService.create(parentNodeRef, name, ContentModel.TYPE_FOLDER).getNodeRef();
		nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, title);
		nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, description);

		return nodeRef;
    }
    
    public NodeRef createCmObject(final NodeRef parentNodeRef, final String name)
    {
        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name));
        NodeRef nodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_CMOBJECT).getChildRef();

        return nodeRef;
    }
    
    public Visibility getVisibility(Client client, NodeRef nodeRef)
    {
    	return hiddenAspect.getVisibility(client, nodeRef);
    }

    public void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> aspectProperties)
    {
    	nodeService.addAspect(nodeRef, aspectTypeQName, aspectProperties);
    }
    
    public void createComment(NodeRef nodeRef, final Comment comment)
    {
		NodeRef commentNodeRef = commentService.createComment(nodeRef, comment.getTitle(), comment.getContent(), false);
		comment.setId(commentNodeRef.getId());
		Date created = (Date)nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATED);
		comment.setCreatedAt(PublicApiDateFormat.getDateFormat().format(created));
		TestPerson person = getPerson((String)nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR));
		comment.setCreatedBy(person);
    }
    
    public TestNetwork createNetworkWithAlias(String alias, boolean enabled)
    {
        String networkId = alias + "-" + System.currentTimeMillis();
    	TestNetwork network = new TestNetwork(networkId, enabled);
    	return network;
    }

    public TestNetwork createNetwork(String networkId, boolean enabled)
    {
    	TestNetwork network = new TestNetwork(networkId, enabled);
    	return network;
    }
    
    public class TestSite extends SiteImpl
    {
		private static final long serialVersionUID = 5317623044880374281L;

		private TestNetwork account;
		private SiteInfo siteInfo;
		
		public TestSite(TestNetwork account, SiteInfo siteInfo)
		{
			this(account, siteInfo.getShortName(), siteInfo.getNodeRef().getId(), siteInfo.getTitle(), siteInfo.getDescription(), siteInfo.getVisibility());
			this.account = account;
			this.siteInfo = siteInfo;
		}
		
		public TestSite(TestNetwork account, String siteId, String guid, String title, String description, SiteVisibility siteVisibility)
		{
			super((account == null ? null : account.getId()), siteId, guid);
			setTitle(title);
			setDescription(description);
			setVisibility(siteVisibility.toString());
			this.siteInfo = siteService.getSite(siteId);
		}
		
		public SiteInfo getSiteInfo()
		{
			return siteInfo;
		}
		public NodeRef getContainerNodeRef(String containerId)
		{
			return siteService.getContainer(siteId, containerId);
		}

	    public NodeRef createContainer(String containerName)
	    {
	    	return siteService.createContainer(getSiteId(), containerName, ContentModel.TYPE_FOLDER, null);
	    }
	    
	    public void inviteToSite(final String toInvite, final SiteRole siteRole)
	    {
	    	siteService.setMembership(getSiteId(), toInvite, siteRole.toString());
	    	log(toInvite + " invited to site " + getSiteId());
	    }

		public TestNetwork getAccount()
		{
			return account;
		}

		public Map<String, String> getMembers(final String roleFilter)
		{
			Map<String, String> members = TenantUtil.runAsSystemTenant(new TenantRunAsWork<Map<String, String>>()
			{
				@Override
				public Map<String, String> doWork() throws Exception
				{
					Map<String, String> members = siteService.listMembers(getSiteId(), null, roleFilter, 0);
					return members;
				}
			}, getAccount().getId());
			return members;
		}
		
		public List<SiteMember> getMembers()
		{
			Map<String, String> members = TenantUtil.runAsSystemTenant(new TenantRunAsWork<Map<String, String>>()
			{
				@Override
				public Map<String, String> doWork() throws Exception
				{
					// get all site members
					Map<String, String> members = siteService.listMembers(getSiteId(), null, null, 0);
					return members;
				}
			}, getAccount().getId());
			List<SiteMember> ret = new ArrayList<SiteMember>(members.size());
			for(String userId : members.keySet())
			{
				String role = members.get(userId);
				TestPerson person = RepoService.this.getPerson(userId);
				SiteMember sm = new SiteMember(userId, person, getSiteId(), role);
				ret.add(sm);
			}
			Collections.sort(ret);
			return ret;
		}
		
		public boolean isMember(final String personId)
		{
			Boolean isMember = TenantUtil.runAsSystemTenant(new TenantRunAsWork<Boolean>()
			{
				@Override
				public Boolean doWork() throws Exception
				{
					return siteService.isMember(getSiteId(), personId);
				}
			}, getAccount().getId());
			return isMember;
		}

		public SiteRole getMember(String personId)
		{
			return SiteRole.valueOf(siteService.getMembersRole(getSiteId(), personId));
		}

		public void updateMember(String personId, SiteRole role)
		{
			siteService.setMembership(getSiteId(), personId, role.toString());
		}

		public void removeMember(String personId)
		{
			if(siteService.isMember(getSiteId(), personId))
			{
				siteService.removeMembership(getSiteId(), personId);
			}
		}

		public void setSiteVisibility(SiteVisibility siteVisibility)
		{
			this.visibility = siteVisibility.toString();
			siteInfo.setVisibility(siteVisibility);
			siteService.updateSite(siteInfo);
		}

		@Override
		public String toString() {
			return "TestSite [siteid=" + getSiteId() + ", title=" + getTitle()
					+ ", description=" + getDescription() + ", siteVisibility="
					+ getVisibility() + "]";
		}
    }
    
	public static <T extends Object> Iterator<T> getWrappingIterator(final int startIdx, final List<T> list)
	{
		return new AbstractIterator<T>()
		{
			private int idx = Math.max(0, Math.min(startIdx, list.size() - 1));

			@Override
			protected T computeNext()
			{
				T o = list.get(idx);

				idx++;
				if(idx >= list.size())
				{
					idx = 0;
				}

				return o;
			}
		};
	}

    public class TestNetwork extends NetworkImpl implements Comparable<TestNetwork>
	{
		private static final long serialVersionUID = -107881141652228471L;

		protected Map<String, TestPerson> people = new TreeMap<String, TestPerson>();
		protected TreeMap<String, TestSite> sites = new TreeMap<String, TestSite>();
		protected Set<TestSite> publicSites = new TreeSet<TestSite>();

		public TestNetwork(String domain, boolean enabled)
		{
			super(domain, enabled);
		}

		public void create()
		{
			if(!getId().equals(TenantService.DEFAULT_DOMAIN))
			{
				tenantAdminService.createTenant(getId(), "admin".toCharArray());
			}
	    	numNetworks++;
			log("Created network " + getId());
		}

		public TestSite createSite(SiteVisibility siteVisibility)
		{
			String shortName = "TESTSITE" + GUID.generate();
			SiteInformation siteInfo = new SiteInformation(shortName, shortName, shortName, siteVisibility);
			return createSite(siteInfo);
	    }

		public TestSite createSite(final SiteInformation site)
	    {
	    	TestSite testSite = RepoService.this.createSite(this, site);
			addSite(testSite);

			return testSite;
	    }
		
		public TestPerson createUser()
		{
			String username = "user" + System.currentTimeMillis();
			PersonInfo personInfo = new PersonInfo("FirstName", "LastName", username, "password", null, "skype", "location",
					"telephone", "mob", "instant", "google");
			TestPerson person = createUser(personInfo);
			return person;
		}

		public TestPerson createUser(final PersonInfo personInfo)
		{
			final String username = publicApiContext.createUserName(personInfo.getUsername(), getId());
			TestPerson testPerson = TenantUtil.runAsTenant(new TenantRunAsWork<TestPerson>()
            {
                public TestPerson doWork() throws Exception
                {
                	TestPerson person = RepoService.this.createUser(personInfo, username, TestNetwork.this);
                    
                    return person;
                }
            }, getId());
			addPerson(testPerson);
			return testPerson;
		}

		public List<String> peopleSample(int sampleSize)
		{
			final List<String> p = new ArrayList<String>();

			if(sampleSize < 0)
			{
				p.addAll(people.keySet());
			}
			else
			{
				int startIdx = random.nextInt(people.size());
				List<String> peopleList = new ArrayList<String>(people.keySet());
				Iterator<String> it = getWrappingIterator(startIdx, peopleList);

				for(int i = 0; i < sampleSize && it.hasNext(); i++)
				{
					p.add(it.next());
				}
			}
			
			return p;
		}

		// Return a site of which person is not a member
		public TestSite getSiteNonMember(final String personId)
		{
			return TenantUtil.runAsSystemTenant(new TenantRunAsWork<TestSite>()
			{
				@Override
				public TestSite doWork() throws Exception
				{
					TestSite ret = null;
					SiteInfo match = null;
					for(SiteInfo info : siteService.listSites(null, null))
					{
						boolean isMember = siteService.isMember(info.getShortName(), personId);
						if(!isMember)
						{
							match = info;
							break;
						}
					}
					
					if(match != null)
					{
						ret = new TestSite(TestNetwork.this, match);
					}
					
					return ret;
				}
			}, getId());
		}
		
		public void addSite(TestSite site)
		{
			sites.put(site.getSiteId(), site);
			if(site.getVisibility().equals(SiteVisibility.PUBLIC.toString()))
			{
				publicSites.add(site);
			}
		}
		
		public Set<TestSite> getPublicSites()
		{
			return publicSites;
		}
		
		public TestSite getSite(final String siteShortName)
		{
			TestSite site = TenantUtil.runAsTenant(new TenantRunAsWork<TestSite>()
			{
				@Override
				public TestSite doWork() throws Exception
				{
					SiteInfo siteInfo = siteService.getSite(siteShortName);
					return new TestSite(TestNetwork.this, siteInfo);
				}
			}, getId());
			return site;
		}
		
		/*
		 * List sites in this account that person can see
		 */
		public List<TestSite> getSites(String personId)
		{
			List<TestSite> sites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<TestSite>>()
			{
				@Override
				public List<TestSite> doWork() throws Exception
				{
					List<SiteInfo> results = siteService.listSites(null, null);
					TreeMap<String, TestSite> ret = new TreeMap<String, TestSite>();
					for(SiteInfo siteInfo : results)
					{
						TestSite site = new TestSite(TestNetwork.this, siteInfo/*, null*/);
						ret.put(site.getSiteId(), site);
					}

					return new ArrayList<TestSite>(ret.values());
				}
			}, personId, getId());
			return sites;
		}
		
		public List<TestSite> getAllSites()
		{
			List<TestSite> sites = TenantUtil.runAsSystemTenant(new TenantRunAsWork<List<TestSite>>()
			{
				@Override
				public List<TestSite> doWork() throws Exception
				{
					List<SiteInfo> results = siteService.listSites(null, null);
					TreeMap<String, TestSite> ret = new TreeMap<String, TestSite>();
					for(SiteInfo siteInfo : results)
					{
						TestSite site = new TestSite(TestNetwork.this, siteInfo/*, null*/);
						ret.put(site.getSiteId(), site);
					}

					return new ArrayList<TestSite>(ret.values());
				}
			}, getId());
			return sites;
		}
		
		public List<SiteContainer> getSiteContainers(final String siteId, TestPerson runAs)
		{
			List<SiteContainer> siteContainers = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<SiteContainer>>()
			{
				@Override
				public List<SiteContainer> doWork() throws Exception
				{
					PagingResults<FileInfo> results = siteService.listContainers(siteId, new PagingRequest(0, Integer.MAX_VALUE));
					List<SiteContainer> ret = new ArrayList<SiteContainer>(results.getPage().size());
					for(FileInfo fileInfo : results.getPage())
					{
						SiteContainer siteContainer = new SiteContainer(siteId, fileInfo.getName(), fileInfo.getNodeRef().getId());
						ret.add(siteContainer);
					}

					return ret;
				}
			}, runAs.getId(), getId());

			return siteContainers;
		}

		public Map<String, TestSite> getSitesForUser(String username)
		{
			if(username == null)
			{
				username = AuthenticationUtil.getAdminUserName();
			}
			List<SiteInfo> sites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<SiteInfo>>()
			{
				@Override
				public List<SiteInfo> doWork() throws Exception
				{
					List<SiteInfo> results = siteService.listSites(null, null);
					return results;
				}
			}, username, getId());
			TreeMap<String, TestSite> ret = new TreeMap<String, TestSite>();
			for(SiteInfo siteInfo : sites)
			{
				TestSite site = new TestSite(TestNetwork.this, siteInfo/*, null*/);
				ret.put(site.getSiteId(), site);
			}
			return ret;
		}
		
		public List<MemberOfSite> getSiteMemberships(String username)
		{
			if(username == null)
			{
				username = AuthenticationUtil.getAdminUserName();
			}
			final String userId = username;
			List<MemberOfSite> sites = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<MemberOfSite>>()
			{
				@Override
				public List<MemberOfSite> doWork() throws Exception
				{
					List<SiteInfo> sites = siteService.listSites(userId);
					Set<MemberOfSite> personSites = new TreeSet<MemberOfSite>();
					for(SiteInfo siteInfo : sites)
					{
						TestSite site = getSite(siteInfo.getShortName());
						String role = siteService.getMembersRole(siteInfo.getShortName(), userId);
						MemberOfSite ms = new MemberOfSite(site, SiteRole.valueOf(role));
						personSites.add(ms);
					}

					return new ArrayList<MemberOfSite>(personSites);
				}
			}, userId, getId());
			return sites;
		}
		
		public Map<String, TestSite> getSites(SiteVisibility siteVisibility)
		{
			Map<String, TestSite> ret = new HashMap<String, TestSite>();
			for(String key : getSitesForUser(null).keySet())
			{
				TestSite site = getSitesForUser(null).get(key);
				if(site.getVisibility().equals(siteVisibility.toString()))
				{
					ret.put(key, site);
				}
			}
			return ret;
		}
		
		public void addPerson(TestPerson person)
		{
			people.put(person.getId(), person);
		}
		
		public List<String> getPersonIds()
		{
			return new ArrayList<String>(people.keySet());
		}
		
		public List<TestPerson> getPeople()
		{
			return new ArrayList<TestPerson>(people.values());
		}

		public int compareTo(TestNetwork o)
		{
			return getId().compareTo(o.getId());
		}

		public void addExternalUser(String personId)
		{
			throw new UnsupportedOperationException();
		}
		
		public TestSite homeSite(TestPerson person)
		{
			throw new UnsupportedOperationException();
		}

		public void inviteUser(final String user)
		{
			throw new UnsupportedOperationException();
		}
		
		public Collection<TestSite> getHomeSites()
		{
			throw new UnsupportedOperationException();
		}
		
		public Long getAccountId()
		{
			throw new UnsupportedOperationException();
		}

		public void setAccountId(Long accountId)
		{
			throw new UnsupportedOperationException();
		}

		public int getType()
		{
			throw new UnsupportedOperationException();
		}

		public void addNetworkAdmin(String email)
		{
			throw new UnsupportedOperationException();
		}

		public Set<String> getNetworkAdmins()
		{
			throw new UnsupportedOperationException();
		}
		
		public Set<String> getNonNetworkAdmins()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((getId() == null) ? 0 : getId().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
			{
				return true;
			}

			if(obj == null)
			{
				return false;
			}
			
			if (getClass() != obj.getClass())
			{
				return false;
			}
			
			TestNetwork other = (TestNetwork) obj;
			return getId().equals(other.getId());
		}

		public TestSite getSite(SiteVisibility siteVisibility)
		{
			TestSite ret = null;
			for(TestSite site : getSitesForUser(null).values())
			{
				if(site.getVisibility().equals(siteVisibility))
				{
					ret = site;
					break;
				}
			}
			return ret;
		}

		@Override
		public String toString()
		{
			return "TestNetwork [people=" + people + ", sites=" + sites
					+ ", publicSites=" + publicSites + "]";
		}
	}
	
    public static class SiteMembership implements Comparable<SiteMembership>
    {
    	private TestSite site;
    	private SiteRole role;

		public SiteMembership(TestSite site, SiteRole role)
		{
			super();
			if(site == null)
			{
				throw new IllegalArgumentException();
			}
			if(role == null)
			{
				throw new IllegalArgumentException();
			}
			this.site = site;
			this.role = role;
		}

		public TestSite getSite() 
		{
			return site;
		}

		public SiteRole getRole()
		{
			return role;
		}
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((role == null) ? 0 : role.hashCode());
			result = prime * result + ((site == null) ? 0 : site.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
			{
				return true;
			}
			
			if (obj == null)
			{
				return false;
			}
			
			if (getClass() != obj.getClass())
			{
				return false;
			}
			
			SiteMembership other = (SiteMembership) obj;
			if (role != other.role)
			{
				return false;
			}

			return site.equals(other.site);
		}

		@Override
		public int compareTo(SiteMembership siteMembership)
		{
			int ret = site.getSiteId().compareTo(siteMembership.getSite().getSiteId());
			if(ret == 0)
			{
				// arbitrary sorting on site role
				ret = role.compareTo(siteMembership.getRole());
			}
			return ret;
		}
    }

	// order by last name, first name, role
	public class TestPerson extends Person
    {
		private static final long serialVersionUID = 4038390056182705588L;

		protected boolean enabled;
		protected String password;
		protected TestNetwork defaultAccount;
		protected TreeMap<TestSite, SiteRole> siteMemberships = new TreeMap<TestSite, SiteRole>();

		public TestPerson(String firstName, String lastName, String username, String password, Company company, TestNetwork defaultAccount, String skype, String location, String tel,
				String mob, String instantmsg, String google)
		{
			super(username, username, true, firstName, lastName, company, skype, location, tel, mob, instantmsg, google);
			this.password = password;
			this.enabled = true;
			this.defaultAccount = defaultAccount;
		}

		public boolean isNetworkAdmin()
		{
			return false; // cloud only
		}
		
		public String getDefaultDomain()
		{
			return defaultAccount == null ? null : defaultAccount.getId();
		}

		public boolean isEnabled()
		{
			return enabled;
		}

		public void addSiteMembership(TestSite site, SiteRole siteRole)
		{
			siteMemberships.put(site, siteRole);
		}

		public TestNetwork getDefaultAccount()
		{
			return defaultAccount;
		}
		
		public String getPassword()
		{
			return password;
		}
		
		public List<PersonNetwork> getNetworkMemberships()
		{
			String personId = getId();
			String runAsNetworkId = Person.getNetworkId(personId);
			return TenantUtil.runAsUserTenant(new TenantRunAsWork<List<PersonNetwork>>()
			{
				@Override
				public List<PersonNetwork> doWork() throws Exception
				{
					List<PersonNetwork> members = new ArrayList<PersonNetwork>();
					PagingResults<Network> networks = networksService.getNetworks(new PagingRequest(0, Integer.MAX_VALUE));
					for(Network network : networks.getPage())
					{
			            NetworkImpl restNetwork = new NetworkImpl(network);
			            PersonNetwork personNetwork = new PersonNetwork(network.getIsHomeNetwork(), restNetwork);
						members.add(personNetwork);
					}
					return members;
				}
			}, personId, runAsNetworkId);
		}

		@Override
		public String toString()
		{
			return "TestPerson [enabled=" + enabled + ", password=" + password
					+ ", defaultAccount=" + defaultAccount.getId() + ", siteMemberships=" + siteMemberships
					+ ", getId()=" + getId() + ", getFirstName()="
					+ getFirstName() + ", getCompany()=" + getCompany()
					+ ", getLastName()=" + getLastName() + "]";
		}
    }

    public static class SiteInformation implements Comparable<SiteInformation>
    {
    	private String shortName;
    	private String title;
    	private String sitePreset;
    	private String description;
    	private SiteVisibility siteVisibility;

		public SiteInformation(String shortName, String title, String description, SiteVisibility siteVisibility)
		{
			super();
			if(shortName == null)
			{
				throw new IllegalArgumentException();
			}
			this.shortName = shortName;
			this.title = title;
			this.description = description;
			this.siteVisibility = siteVisibility;
		}

		public String getShortName()
		{
			return shortName;
		}

		public String getTitle()
		{
			return title;
		}

		public String getSitePreset()
		{
			return sitePreset;
		}

		public String getDescription()
		{
			return description;
		}

		public SiteVisibility getSiteVisibility()
		{
			return siteVisibility;
		}

		public boolean equals(Object other)
		{
			if(this == other)
			{
				return true;
			}

			if(!(other instanceof SiteInformation))
			{
				return false;
			}
			
			SiteInformation site = (SiteInformation)other;
			return shortName == site.getShortName();
		}

		@Override
		public int compareTo(SiteInformation site)
		{
			return shortName.compareTo(site.getShortName());
		}

	    public int hashCode()
		{
			return getShortName().hashCode();
		}
    }
}
