package org.alfresco.repo.activities.feed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailBean;

/**
 * Feed notifier tests.
 * 
 * @author steveglover
 *
 */
public class FeedNotifierTest
{
	private static ApplicationContext ctx = null;
	
	private PersonService personService;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	private SiteService siteService;
	private ActivityService activityService;
	private RepoAdminService repoAdminService;
	private FeedNotifierImpl feedNotifier;
	private TransactionService transactionService;
	private PostLookup postLookup;
	private FeedGenerator feedGenerator;
	private FileFolderService fileFolderService;
	private SubscriptionService subscriptionService;
	private ErrorProneActionExecutor errorProneActionExecutor;
	private ActivityPostDAO postDAO;
	private ActionService actionService;
	
	private ErrorProneUserNotifier userNotifier;

	private NodeRef failingPersonNodeRef;
	private NodeRef personNodeRef;
	private String userName1 = "user1." + GUID.generate();
	private String userName2 = "user2." + GUID.generate();

	@BeforeClass
	public static void init()
	{
       ApplicationContextHelper.setUseLazyLoading(false);
       ApplicationContextHelper.setNoAutoStart(true);

       ctx = ApplicationContextHelper.getApplicationContext();
	}

	@Before
	public void before() throws Exception
	{
		ChildApplicationContextFactory activitiesFeed = (ChildApplicationContextFactory)ctx.getBean("ActivitiesFeed");
        ApplicationContext activitiesFeedCtx = activitiesFeed.getApplicationContext();
		this.feedNotifier = (FeedNotifierImpl)activitiesFeedCtx.getBean("feedNotifier");
		this.activityService = (ActivityService)activitiesFeedCtx.getBean("activityService");
        this.postLookup = (PostLookup)activitiesFeedCtx.getBean("postLookup");
        this.feedGenerator = (FeedGenerator)activitiesFeedCtx.getBean("feedGenerator");

		Scheduler scheduler = (Scheduler)ctx.getBean("schedulerFactory");

	    JobDetailBean feedGeneratorJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("feedGeneratorJobDetail");
	    JobDetailBean postLookupJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("postLookupJobDetail");
        JobDetailBean feedCleanerJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("feedCleanerJobDetail");
        JobDetailBean postCleanerJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("postCleanerJobDetail");
        JobDetailBean feedNotifierJobDetail = (JobDetailBean)activitiesFeedCtx.getBean("feedNotifierJobDetail");

        // Pause activities jobs so that we aren't competing with their scheduled versions
        scheduler.pauseJob(feedGeneratorJobDetail.getName(), feedGeneratorJobDetail.getGroup());
        scheduler.pauseJob(postLookupJobDetail.getName(), postLookupJobDetail.getGroup());
        scheduler.pauseJob(feedCleanerJobDetail.getName(), feedCleanerJobDetail.getGroup());
        scheduler.pauseJob(postCleanerJobDetail.getName(), postCleanerJobDetail.getGroup());
        scheduler.pauseJob(feedNotifierJobDetail.getName(), feedNotifierJobDetail.getGroup());

		this.personService = (PersonService)ctx.getBean("personService");
		this.nodeService = (NodeService)ctx.getBean("nodeService");
		this.namespaceService = (NamespaceService)ctx.getBean("namespaceService");
		this.siteService = (SiteService)ctx.getBean("siteService");
		this.repoAdminService = (RepoAdminService)ctx.getBean("repoAdminService");
		this.transactionService = (TransactionService)ctx.getBean("transactionService");
		this.postDAO = (ActivityPostDAO)ctx.getBean("postDAO");
		this.fileFolderService = (FileFolderService)ctx.getBean("fileFolderService");
		this.subscriptionService = (SubscriptionService)ctx.getBean("SubscriptionService");
		this.errorProneActionExecutor = (ErrorProneActionExecutor)ctx.getBean("errorProneActionExecutor");
		this.actionService = (ActionService)ctx.getBean("ActionService");
		
		// create some users
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
        	@SuppressWarnings("synthetic-access")
        	public Void execute() throws Throwable
        	{
        		AuthenticationUtil.pushAuthentication();
        		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                // create person properties
                PropertyMap personProps = new PropertyMap();
                personProps.put(ContentModel.PROP_USERNAME, userName1);
                personProps.put(ContentModel.PROP_FIRSTNAME, userName1);
                personProps.put(ContentModel.PROP_LASTNAME, userName1);
                personProps.put(ContentModel.PROP_EMAIL, userName1+"@email.com");
                personNodeRef = personService.createPerson(personProps);

        		personProps = new PropertyMap();
                personProps.put(ContentModel.PROP_USERNAME, userName2);
                personProps.put(ContentModel.PROP_FIRSTNAME, userName2);
                personProps.put(ContentModel.PROP_LASTNAME, userName2);
                personProps.put(ContentModel.PROP_EMAIL, userName2+"@email.com");
                failingPersonNodeRef = personService.createPerson(personProps);

        		AuthenticationUtil.popAuthentication();

        		return null;
        	}
        }, false, true);

		// and some activities for those users
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
        	@SuppressWarnings("synthetic-access")
        	public Void execute() throws Throwable
        	{
        		AuthenticationUtil.pushAuthentication();
        		AuthenticationUtil.setFullyAuthenticatedUser(userName1);

        		NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        		NodeRef workingRootNodeRef = nodeService.createNode(
                        rootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NamespaceService.ALFRESCO_URI, "working root"),
                        ContentModel.TYPE_FOLDER).getChildRef();
        		FileInfo file1 = fileFolderService.create(workingRootNodeRef, GUID.generate(), ContentModel.TYPE_CONTENT);

				// ensure at least 3 activities
				JSONObject activityData = new JSONObject();
				activityData.put("title", GUID.generate());
				activityData.put("nodeRef", file1.getNodeRef());
		    	activityService.postActivity("org.alfresco.documentlibrary.file-added", null, "documentlibrary", activityData.toString(), userName1);

        		AuthenticationUtil.popAuthentication();

        		return null;
        	}
        }, false, true);
		
		// one of the users follows the other user (so that we can test that notification failures for one of the users does not
		// affect other users)
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
        	@SuppressWarnings("synthetic-access")
        	public Void execute() throws Throwable
        	{
        		AuthenticationUtil.pushAuthentication();
        		AuthenticationUtil.setFullyAuthenticatedUser(userName2);
        		
        		subscriptionService.follow(userName2, userName1);
        		
        		AuthenticationUtil.popAuthentication();

        		return null;
        	}
        }, false, true);

		generateActivities();
		
		// use our own user notifier for testing purposes
		this.userNotifier = new ErrorProneUserNotifier(failingPersonNodeRef);
		userNotifier.setNodeService(nodeService);
		userNotifier.setNamespaceService(namespaceService);
		userNotifier.setSiteService(siteService);
		userNotifier.setActivityService(activityService);
		userNotifier.setRepoAdminService(repoAdminService);
		userNotifier.setActionService(actionService);
		feedNotifier.setUserNotifier(userNotifier);
	}

	private void generateActivities() throws Exception
	{
		// generate the activities
    	postLookup.execute();

        Long maxSequence = postDAO.getMaxActivitySeq();
        while(maxSequence != null)
        {
        	feedGenerator.execute();

        	maxSequence = postDAO.getMaxActivitySeq();
        }
	}

	/**
	 * ALF-16155 test
	 */
	@Test
	public void testFailedNotifications()
	{
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

		// execute the notifier, counting the number of successful and unsuccessful notifications
		assertEquals(0, errorProneActionExecutor.getNumSuccess());
		assertEquals(0, errorProneActionExecutor.getNumFailed());
		feedNotifier.execute(1);
		assertEquals(1, errorProneActionExecutor.getNumFailed()); // should be a single notification failure
		assertTrue(errorProneActionExecutor.getNumSuccess() > 0); // others should have gone through ok
		int numSuccessfulNotifications = errorProneActionExecutor.getNumSuccess();

		// execute the notifier again, checking that there are no more notifications
		feedNotifier.execute(1);
		// should still be failing
		assertEquals(2, errorProneActionExecutor.getNumFailed());
		// there should not be any more because they have been processed already
		assertEquals(numSuccessfulNotifications, errorProneActionExecutor.getNumSuccess());
	}
}
