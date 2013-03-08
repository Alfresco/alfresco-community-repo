package org.alfresco.opencmis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.GUID;

/**
 * OpenCMIS tests.
 * 
 * @author steveglover
 *
 */
public class CMISTest
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private FileFolderService fileFolderService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private Repository repositoryHelper;

	private AlfrescoCmisServiceFactory factory;
	private SimpleCallContext context;

	private ActionService actionService;
	private RuleService ruleService;
	
    /**
     * Test class to provide the service factory
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    public static class TestCmisServiceFactory extends AbstractServiceFactory
    {
        private static AlfrescoCmisServiceFactory serviceFactory = (AlfrescoCmisServiceFactory) ctx.getBean("CMISServiceFactory");
        @Override
        public void init(Map<String, String> parameters)
        {
        	serviceFactory.init(parameters);
        }

        @Override
        public void destroy()
        {
        }

        @Override
        public CmisService getService(CallContext context)
        {
            return serviceFactory.getService(context);
        }
    }

    private static class SimpleCallContext implements CallContext
    {
    	private final Map<String, Object> contextMap = new HashMap<String, Object>();

    	public SimpleCallContext(String user, String password)
    	{
    		contextMap.put(USERNAME, user);
    		contextMap.put(PASSWORD, password);
    	}

    	public String getBinding()
    	{
    		return BINDING_LOCAL;
    	}

    	public Object get(String key)
    	{
    		return contextMap.get(key);
    	}

    	public String getRepositoryId()
    	{
    		return (String) get(REPOSITORY_ID);
    	}

    	public String getUsername()
    	{
    		return (String) get(USERNAME);
    	}

    	public String getPassword()
    	{
    		return (String) get(PASSWORD);
    	}

    	public String getLocale()
    	{
    		return null;
    	}

    	public BigInteger getOffset()
    	{
    		return (BigInteger) get(OFFSET);
    	}

    	public BigInteger getLength()
    	{
    		return (BigInteger) get(LENGTH);
    	}

    	public boolean isObjectInfoRequired()
    	{
    		return false;
    	}

    	public File getTempDirectory()
    	{
    		return null;
    	}

    	public int getMemoryThreshold()
    	{
    		return 0;
    	}

        public long getMaxContentSize()
        {
            return Long.MAX_VALUE;
        }
    }
	
    @Before
    public void before()
    {
        this.actionService = (ActionService)ctx.getBean("actionService");
        this.ruleService = (RuleService)ctx.getBean("ruleService");
    	this.fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
    	this.transactionService = (TransactionService)ctx.getBean("transactionService");
    	this.nodeService = (NodeService)ctx.getBean("NodeService");
        this.repositoryHelper = (Repository)ctx.getBean("repositoryHelper");
    	this.factory = (AlfrescoCmisServiceFactory)ctx.getBean("CMISServiceFactory");
        this.context = new SimpleCallContext("admin", "admin");
    }
    
    /**
     * ALF-18006 Test content mimetype auto-detection into CmisStreamInterceptor when "Content-Type" is not defined.
     */
    @Test
    public void testContentMimeTypeDetection()
    {
        String repositoryId = null;

        CmisService cmisService = factory.getService(context);
        try
        {
            // get repository id
            List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
            assertTrue(repositories.size() > 0);
            RepositoryInfo repo = repositories.get(0);
            repositoryId = repo.getId();

            // create content properties
            PropertiesImpl properties = new PropertiesImpl();
            String objectTypeId = "cmis:document";
            properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
            String fileName = "textFile" + GUID.generate();
            properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));

            // create content stream
            ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");

            // create simple text plain content
            String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().toString(), contentStream, VersioningState.MAJOR, null, null);

            Holder<String> objectIdHolder = new Holder<String>(objectId);

            // create content stream with undefined mimetype and file name
            ContentStreamImpl contentStreamHTML = new ContentStreamImpl(null, null, "<html><head><title> Hello </title></head><body><p> Test html</p></body></html></body></html>");
            cmisService.setContentStream(repositoryId, objectIdHolder, true, null, contentStreamHTML, null);

            // check mimetype
            boolean mimetypeHTML = cmisService.getObjectInfo(repositoryId, objectId).getContentType().equals(MimetypeMap.MIMETYPE_HTML);
            assertTrue("Mimetype is not defined correctly.", mimetypeHTML);
        }
        finally
        {
            cmisService.close();
        }
    }
    
    /**
     * Test for ALF-16310.
     * 
     * Check that, for AtomPub binding, cancel checkout on the originating checked out document i.e. not the working
     * copy throws an exception and does not delete the document.
     */
    @SuppressWarnings("unchecked")
	@Test
    public void testCancelCheckout()
    {
    	String repositoryId = null;
    	ObjectData objectData = null;
    	Holder<String> objectId = null;

    	final String folderName = "testfolder." + GUID.generate();
    	final String docName = "testdoc.txt." + GUID.generate();

    	AuthenticationUtil.pushAuthentication();
    	AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    	try
    	{
    		final FileInfo folderInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
	    	{
				@Override
				public FileInfo execute() throws Throwable
				{
					NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

			    	FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
			    	nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
			    	
			    	FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
			    	nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);
	
					return folderInfo;
				}
			});
    		
    		CmisService service = factory.getService(context);
    		try
    		{
				List<RepositoryInfo> repositories = service.getRepositoryInfos(null);
				assertTrue(repositories.size() > 0);
				RepositoryInfo repo = repositories.get(0);
				repositoryId = repo.getId();
		    	objectData = service.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true,
		    			IncludeRelationships.NONE, null, false, true, null);
	
		    	// checkout
	            objectId = new Holder<String>(objectData.getId());
		    	service.checkOut(repositoryId, objectId, null, new Holder<Boolean>(true));
    		}
    		finally
    		{
    			service.close();
    		}

	    	// AtomPub cancel checkout
	        try
	        {
	    		service = factory.getService(context);

	    		// check allowable actions
	        	ObjectData originalDoc = service.getObject(repositoryId, objectData.getId(), null, true, IncludeRelationships.NONE, null, false, true, null);
	        	AllowableActions allowableActions = originalDoc.getAllowableActions();
	        	assertNotNull(allowableActions);
	        	assertFalse(allowableActions.getAllowableActions().contains(Action.CAN_DELETE_OBJECT));

	        	// try to cancel the checkout
	        	service.deleteObjectOrCancelCheckOut(repositoryId, objectData.getId(), Boolean.TRUE, null);
	        	fail();
	        }
	        catch(CmisConstraintException e)
	        {
	        	// expected
	        }
    		finally
    		{
    			service.close();
    		}

	        try
	        {
	    		service = factory.getService(context);

	    		// cancel checkout on pwc
	    		service.deleteObjectOrCancelCheckOut(repositoryId, objectId.getValue(), Boolean.TRUE, null);
	        }
    		finally
    		{
    			service.close();
    		}

	        try
	        {
	    		service = factory.getService(context);
	    		
	        	// get original document
	        	ObjectData originalDoc = service.getObject(repositoryId, objectData.getId(), null, true, IncludeRelationships.NONE, null, false, true, null);
	        	Map<String, PropertyData<?>> properties = originalDoc.getProperties().getProperties();
	        	PropertyData<Boolean> isVersionSeriesCheckedOutProp = (PropertyData<Boolean>)properties.get(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
	        	assertNotNull(isVersionSeriesCheckedOutProp);
	        	Boolean isVersionSeriesCheckedOut = isVersionSeriesCheckedOutProp.getFirstValue();
	        	assertNotNull(isVersionSeriesCheckedOut);
	        	assertEquals(Boolean.FALSE, isVersionSeriesCheckedOut);
	        }
    		finally
    		{
    			service.close();
    		}
    		
	        try
	        {
	    		service = factory.getService(context);
	    		
	        	// delete original document
	        	service.deleteObject(repositoryId, objectData.getId(), true, null);
	        }
    		finally
    		{
    			service.close();
    		}
        	
        	List<FileInfo> children = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<FileInfo>>()
	    	{
				@Override
				public List<FileInfo> execute() throws Throwable
				{
			    	List<FileInfo> children = fileFolderService.list(folderInfo.getNodeRef());
					return children;
				}
			});
        	assertEquals(0, children.size());
    	}
    	finally
    	{
    		AuthenticationUtil.popAuthentication();
    	}
    }
   
    /**
     * Test for ALF-18151.
     */
    @Test
    public void testDeleteFolder()
    {
        String repositoryId = null;
        ObjectData objectData = null;
        Holder<String> objectId = null;

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        Map<FileInfo, Boolean> testFolderMap = new HashMap<FileInfo, Boolean>(4);

        try
        {
            // create folder with file
            String folderName = "testfolder" + GUID.generate();
            String docName = "testdoc.txt" + GUID.generate();
            FileInfo folder = createContent(folderName, docName, false);
            testFolderMap.put(folder, Boolean.FALSE);

            // create empty folder
            String folderNameEmpty = "testfolder_empty1" + GUID.generate();
            FileInfo folderEmpty = createContent(folderNameEmpty, null, false);
            testFolderMap.put(folderEmpty, Boolean.TRUE);

            // create folder with file
            String folderNameRule = "testfolde_rule" + GUID.generate();
            String docNameRule = "testdoc_rule.txt" + GUID.generate();
            FileInfo folderWithRule = createContent(folderNameRule, docNameRule, true);
            testFolderMap.put(folderWithRule, Boolean.FALSE);

            // create empty folder
            String folderNameEmptyRule = "testfolde_empty_rule1" + GUID.generate();
            FileInfo folderEmptyWithRule = createContent(folderNameEmptyRule, null, true);
            testFolderMap.put(folderEmptyWithRule, Boolean.TRUE);

            CmisService service = factory.getService(context);

            try
            {
                List<RepositoryInfo> repositories = service.getRepositoryInfos(null);
                RepositoryInfo repo = repositories.get(0);
                repositoryId = repo.getId();

                for (Map.Entry<FileInfo, Boolean> entry : testFolderMap.entrySet())
                {
                    objectData = service.getObjectByPath(repositoryId, "/" + entry.getKey().getName(), null, true, IncludeRelationships.NONE, null, false, true, null);

                    objectId = new Holder<String>(objectData.getId());

                    try
                    {
                        // delete folder
                        service.deleteObjectOrCancelCheckOut(repositoryId, objectId.getValue(), Boolean.TRUE, null);
                    }
                    catch (CmisConstraintException ex)
                    {
                        assertTrue(!entry.getValue());
                        continue;
                    }

                    assertTrue(entry.getValue());
                }
            }
            finally
            {
                service.close();
            }

        }
        finally
        {
            for (Map.Entry<FileInfo, Boolean> entry : testFolderMap.entrySet())
            {
                if (fileFolderService.exists(entry.getKey().getNodeRef()))
                {
                    fileFolderService.delete(entry.getKey().getNodeRef());
                }
            }

            AuthenticationUtil.popAuthentication();
        }
    }

    private FileInfo createContent(final String folderName, final String docName, final boolean isRule)
    {
        final FileInfo folderInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
        {
            @Override
            public FileInfo execute() throws Throwable
            {
                NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                assertNotNull(folderInfo);

                FileInfo fileInfo;
                if (docName != null)
                {
                    fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);
                    assertNotNull(fileInfo);
                }

                if (isRule)
                {
                    Rule rule = addRule(true, folderName);

                    assertNotNull(rule);

                    // Attach the rule to the node
                    ruleService.saveRule(folderInfo.getNodeRef(), rule);

                    assertTrue(ruleService.getRules(folderInfo.getNodeRef()).size() > 0);
                }

                return folderInfo;
            }
        });

        return folderInfo;
    }

    private Rule addRule(boolean isAppliedToChildren, String title)
    {

        // Rule properties
        Map<String, Serializable> conditionProps = new HashMap<String, Serializable>();
        conditionProps.put(ComparePropertyValueEvaluator.PARAM_VALUE, ".txt");

        Map<String, Serializable> actionProps = new HashMap<String, Serializable>();
        actionProps.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);

        List<String> ruleTypes = new ArrayList<String>(1);
        ruleTypes.add(RuleType.INBOUND);

        // Create the action
        org.alfresco.service.cmr.action.Action action = actionService.createAction(title);
        action.setParameterValues(conditionProps);

        ActionCondition actionCondition = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
        actionCondition.setParameterValues(conditionProps);
        action.addActionCondition(actionCondition);

        // Create the rule
        Rule rule = new Rule();
        rule.setRuleTypes(ruleTypes);
        rule.setTitle(title);
        rule.setDescription("description");
        rule.applyToChildren(isAppliedToChildren);
        rule.setAction(action);

        return rule;
    }
}
