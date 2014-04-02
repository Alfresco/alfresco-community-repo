/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ExtensionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
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
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(new String[]{ApplicationContextHelper.CONFIG_LOCATIONS[0],"classpath:test-cmisinteger_modell-context.xml"});

    private FileFolderService fileFolderService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private Repository repositoryHelper;
    private VersionService versionService;
    private LockService lockService;
    private TaggingService taggingService;
    private NamespaceService namespaceService;

	private AlfrescoCmisServiceFactory factory;

	private ActionService actionService;
	private RuleService ruleService;
	
    private CMISConnector cmisConnector;
	
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
    
    /**
     * Test class to provide the service factory
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    public static class TestCmisServiceFactory11 extends AbstractServiceFactory
    {
        private static AlfrescoCmisServiceFactory serviceFactory = (AlfrescoCmisServiceFactory) ctx.getBean("CMISServiceFactory1.1");
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
    	private CmisVersion cmisVersion;

    	public SimpleCallContext(String user, String password, CmisVersion cmisVersion)
    	{
    		contextMap.put(USERNAME, user);
    		contextMap.put(PASSWORD, password);
    		this.cmisVersion = cmisVersion;
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

        @Override
        public boolean encryptTempFiles()
        {
            return false;
        }

        @Override
        public CmisVersion getCmisVersion()
        {
            return cmisVersion;
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
    	this.contentService = (ContentService)ctx.getBean("ContentService");
        this.versionService = (VersionService) ctx.getBean("versionService");
        this.lockService = (LockService) ctx.getBean("lockService");
        this.taggingService = (TaggingService) ctx.getBean("TaggingService");
        this.namespaceService = (NamespaceService) ctx.getBean("namespaceService");
        this.repositoryHelper = (Repository)ctx.getBean("repositoryHelper");
    	this.factory = (AlfrescoCmisServiceFactory)ctx.getBean("CMISServiceFactory");
    	this.cmisConnector = (CMISConnector) ctx.getBean("CMISConnector");
    }
    
    /**
     * MNT-10868 CMIS: Incorrect value of Latest Major version on Versions and Properties tabs.
     */
    @Test
    public void testIsLatestMajorVersionMNT10868()
    {
    	 String repositoryId = null;
    	 
    	 AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

         CmisService cmisService = factory.getService(context);
         try
         {
             // get repository id
             List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
             assertTrue(repositories.size() > 0);
             RepositoryInfo repo = repositories.get(0);
             repositoryId = repo.getId();
             final String folderName = "testfolder" + GUID.generate();
             final String docName = "testdoc.txt" + GUID.generate();
             final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
             {
                 @Override
                 public FileInfo execute() throws Throwable
                 {
                     NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                     FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                     nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                    
                     FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                     nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);
                     nodeService.addAspect(fileInfo.getNodeRef(), ContentModel.ASPECT_VERSIONABLE, null);

                     return fileInfo;
                 }
             });
             
             ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true, IncludeRelationships.NONE, null, false, true, null);
             
             PropertyData<?> pd = getPropIsLatestMajorVersion(objectData);
             
             if (pd != null)
             {
                 assertTrue("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION should be true as major version was created", (Boolean) pd.getValues().get(0));
             }
                
             nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_TITLE, docName);
             
             // Create minor version   
             transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
             {
                   public Object execute() throws Throwable
                   {
                       // get an updating writer
                       ContentWriter writer = contentService.getWriter(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                      
                       writer.setMimetype("text/plain");
                      
                       writer.putContent("New Version");
                       return null;
                   }
            });
                
            objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true, IncludeRelationships.NONE, null, false, true, null);
            
            pd = getPropIsLatestMajorVersion(objectData);
            
            if (pd != null)
            {
                assertFalse("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION should be false as minor version was created", (Boolean) pd.getValues().get(0));
            }
        }
        finally
        {
            cmisService.close();
        }
    }
    
    private PropertyData<?> getPropIsLatestMajorVersion(ObjectData objectData)
    {
        List<PropertyData<?>> properties = objectData.getProperties().getPropertyList();
        boolean found = false;
        PropertyData<?> propIsLatestMajorVersion = null;
        for (PropertyData<?> property : properties)
        {
            if (property.getId().equals(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION))
            {
                found = true;
                propIsLatestMajorVersion = property;
                break;
            }
        }
        //properties..contains(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION);
        assertTrue("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION property was not found", found);
        if (found)
        {
            return propIsLatestMajorVersion;
        }
        
        return null;
    }

    /**
     * Test for MNT-9203.
     */
    @Test
    public void testCeheckIn()
    {
        String repositoryId = null;
        ObjectData objectData = null;
        Holder<String> objectId = null;
        CallContext context = new SimpleCallContext("admin", "admin", CmisVersion.CMIS_1_0);

        final String folderName = "testfolder." + GUID.generate();
        final String docName = "testdoc.txt." + GUID.generate();
        final String customModel = "my.new.model";

        final QName testCustomTypeQName = QName.createQName(customModel, "sop");
        final QName authorisedByQname = QName.createQName(customModel, "authorisedBy");
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        try
        {
            final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
            {
                @Override
                public FileInfo execute() throws Throwable
                {
                    NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                    FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);

                    FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, testCustomTypeQName);
                    Map<QName, Serializable> customProperties = new HashMap<QName, Serializable>();

                    customProperties.put(authorisedByQname, "customPropertyString");
                    customProperties.put(ContentModel.PROP_NAME, docName);
                    nodeService.setProperties(fileInfo.getNodeRef(), customProperties);

                    return fileInfo;
                }
            });

            CmisService service = factory.getService(context);
            try
            {
                List<RepositoryInfo> repositories = service.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                repositoryId = repo.getId();
                objectData = service.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true, IncludeRelationships.NONE, null, false, true, null);

                // checkout
                objectId = new Holder<String>(objectData.getId());
                service.checkOut(repositoryId, objectId, null, new Holder<Boolean>(true));
            }
            finally
            {
                service.close();
            }

            try
            {
                service = factory.getService(context);

                PropertyStringImpl prop = new PropertyStringImpl();
                prop.setId("my:" + authorisedByQname.toPrefixString());
                prop.setValue(null);

                Collection<PropertyData<?>> propsList = new ArrayList<PropertyData<?>>();
                propsList.add(prop);

                Properties properties = new PropertiesImpl(propsList);

                // checkIn on pwc
                service.checkIn(repositoryId, objectId, false, properties, null, null, null, null, null, null);
            }
            finally
            {
                service.close();
            }
            // check that value is null
            assertTrue(nodeService.getProperty(fileInfo.getNodeRef(), authorisedByQname) == null);
        }
        finally
        {
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

    private <T extends Object> T withCmisService(CmisServiceCallback<T> callback)
    {
        return withCmisService(callback, CmisVersion.CMIS_1_0);
    }

    private <T extends Object> T withCmisService(CmisServiceCallback<T> callback, CmisVersion cmisVersion)
    {
        CmisService cmisService = null;

        try
        {
            CallContext context = new SimpleCallContext("admin", "admin", cmisVersion);
            cmisService = factory.getService(context);
            T ret = callback.execute(cmisService);
            return ret;
        }
        finally
        {
            if(cmisService != null)
            {
                cmisService.close();
            }
        }
    }
    
    private static interface CmisServiceCallback<T>
    {
    	T execute(CmisService cmisService);
    }

    /**
     * ALF-18006 Test content mimetype auto-detection into CmisStreamInterceptor when "Content-Type" is not defined.
     */
    @Test
    public void testContentMimeTypeDetection()
    {
        // get repository id
    	List<RepositoryInfo> repositories = withCmisService(new CmisServiceCallback<List<RepositoryInfo>>()
    	{
			@Override
			public List<RepositoryInfo> execute(CmisService cmisService)
			{
	            List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
				return repositories;
			}
    	});

        assertTrue(repositories.size() > 0);
        RepositoryInfo repo = repositories.get(0);
        final String repositoryId = repo.getId();

        // create simple text plain content
        final PropertiesImpl properties = new PropertiesImpl();
        String objectTypeId = "cmis:document";
        properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
        String fileName = "textFile" + GUID.generate();
        properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
        final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");
        
        String objectId = withCmisService(new CmisServiceCallback<String>()
        {
			@Override
			public String execute(CmisService cmisService)
			{
				String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.MAJOR, null, null);
				return objectId;
			}
        });

        final Holder<String> objectIdHolder = new Holder<String>(objectId);
        final String path = "/" + fileName;

        // create content stream with undefined mimetype and file name
        {
            final ContentStreamImpl contentStreamHTML = new ContentStreamImpl(null, null, "<html><head><title> Hello </title></head><body><p> Test html</p></body></html></body></html>");
            withCmisService(new CmisServiceCallback<Void>()
            {
				@Override
				public Void execute(CmisService cmisService)
				{
					cmisService.setContentStream(repositoryId, objectIdHolder, true, null, contentStreamHTML, null);
					return null;
				}
            });

            // check mimetype
            ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
				@Override
				public ObjectData execute(CmisService cmisService)
				{
					return cmisService.getObjectByPath(repositoryId, path, null, false, IncludeRelationships.NONE, null, false, false, null);
				}
            });

            final String objectId1 = objectData.getId();
            String contentType = withCmisService(new CmisServiceCallback<String>()
            {
				@Override
				public String execute(CmisService cmisService)
				{
					String contentType = cmisService.getObjectInfo(repositoryId, objectId1).getContentType();
					return contentType;
				}
            });
            assertEquals("Mimetype is not defined correctly.", MimetypeMap.MIMETYPE_HTML, contentType);
        }

        // create content stream with mimetype and encoding
        {
            String mimeType = MimetypeMap.MIMETYPE_TEXT_PLAIN + "; charset=UTF-8";
            final ContentStreamImpl contentStreamHTML = new ContentStreamImpl(null, mimeType, "<html><head><title> Hello </title></head><body><p> Test html</p></body></html></body></html>");
            withCmisService(new CmisServiceCallback<Void>()
            {
				@Override
				public Void execute(CmisService cmisService)
				{
					cmisService.setContentStream(repositoryId, objectIdHolder, true, null, contentStreamHTML, null);
					return null;
				}
            });

            // check mimetype
            final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
				@Override
				public ObjectData execute(CmisService cmisService)
				{
					ObjectData objectData = cmisService.getObjectByPath(repositoryId, path, null, false, IncludeRelationships.NONE, null, false, false, null);
					return objectData;
				}
            });
            String contentType = withCmisService(new CmisServiceCallback<String>()
            {
				@Override
				public String execute(CmisService cmisService)
				{
					String contentType = cmisService.getObjectInfo(repositoryId, objectData.getId()).getContentType();
					return contentType;
				}
            });
            assertEquals("Mimetype is not defined correctly.", MimetypeMap.MIMETYPE_TEXT_PLAIN, contentType);
        }

        // checkout/checkin object with mimetype and encoding
        {
            ObjectData objectDa = withCmisService(new CmisServiceCallback<ObjectData>()
            {
                @Override
                public ObjectData execute(CmisService cmisService)
                {
                    return cmisService.getObjectByPath(repositoryId, path, null, false, IncludeRelationships.NONE, null, false, false, null);
                }
            });


            objectIdHolder.setValue(objectDa.getId());
            withCmisService(new CmisServiceCallback<Void>()
            {
    			@Override
    			public Void execute(CmisService cmisService)
    			{
    				cmisService.checkOut(repositoryId, objectIdHolder, null, new Holder<Boolean>());
    				return null;
    			}
            });
            String mimeType = MimetypeMap.MIMETYPE_HTML + "; charset=UTF-8";
            final ContentStreamImpl contentStreamHTML = new ContentStreamImpl(null, mimeType, "<html><head><title> Hello </title></head><body><p> Test html</p></body></html></body></html>");
            withCmisService(new CmisServiceCallback<Void>()
            {
    			@Override
    			public Void execute(CmisService cmisService)
    			{
    				cmisService.checkIn(repositoryId, objectIdHolder, false, null, contentStreamHTML, "checkin", null, null, null, null);
    				return null;
    			}
            });

            // check mimetype
            final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
    			@Override
    			public ObjectData execute(CmisService cmisService)
    			{
    				ObjectData objectData = cmisService.getObjectByPath(repositoryId, path, null, false, IncludeRelationships.NONE, null, false, false, null);
    				return objectData;
    			}
            });
            String contentType = withCmisService(new CmisServiceCallback<String>()
            {
    			@Override
    			public String execute(CmisService cmisService)
    			{
    				String contentType = cmisService.getObjectInfo(repositoryId, objectData.getId()).getContentType();
    				return contentType;
    			}
            });
            assertEquals("Mimetype is not defined correctly.", MimetypeMap.MIMETYPE_HTML, contentType);
        }
    }

    /**
     * ALF-20389 Test Alfresco cmis stream interceptor that checks content stream for mimetype. Only ContentStreamImpl extensions should take palace.
     */
    @Test
    public void testGetRepositoryInfos()
    {
        boolean cmisEx = false;
        List<RepositoryInfo> infoDataList = null;
        try
        {
            infoDataList = withCmisService(new CmisServiceCallback<List<RepositoryInfo>>()
            {
                @Override
                public List<RepositoryInfo> execute(CmisService cmisService)
                {
                    ExtensionDataImpl result = new ExtensionDataImpl();
                    List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();
                    result.setExtensions(extensions);

                    return cmisService.getRepositoryInfos(result);
                }
            });
        }
        catch (CmisRuntimeException e)
        {
            cmisEx = true;
        }

        assertNotNull(cmisEx ? "CmisRuntimeException was thrown. Please, take a look on ALF-20389" : "No CMIS repository information was retrieved", infoDataList);
    }

    private static class TestContext
    {
        private String repositoryId = null;
        @SuppressWarnings("unused")
        private ObjectData objectData = null;
        private Holder<String> objectId = null;
        
        public TestContext()
        {
            super();
        }
        
        public String getRepositoryId()
        {
            return repositoryId;
        }
        
        public void setRepositoryId(String repositoryId)
        {
            this.repositoryId = repositoryId;
        }
        
        public void setObjectData(ObjectData objectData)
        {
            this.objectData = objectData;
        }
        
        public Holder<String> getObjectId()
        {
            return objectId;
        }
        
        public void setObjectId(Holder<String> objectId)
        {
            this.objectId = objectId;
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
        final TestContext testContext = new TestContext();

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

    		final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
		    {
		        @Override
		        public ObjectData execute(CmisService cmisService)
		        {
		            List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
		            assertTrue(repositories.size() > 0);
		            RepositoryInfo repo = repositories.get(0);
		            String repositoryId = repo.getId();
		            testContext.setRepositoryId(repositoryId);
		            ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true,
		                    IncludeRelationships.NONE, null, false, true, null);
		            testContext.setObjectData(objectData);

		            // checkout
		            Holder<String> objectId = new Holder<String>(objectData.getId());
		            testContext.setObjectId(objectId);
		            cmisService.checkOut(repositoryId, objectId, null, new Holder<Boolean>(true));

		            return objectData;
		        }
		    });

	    	// AtomPub cancel checkout
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    try
                    {
        	    		// check allowable actions
        	        	ObjectData originalDoc = cmisService.getObject(testContext.getRepositoryId(), objectData.getId(), null, true, IncludeRelationships.NONE, null, false, true, null);
        	        	AllowableActions allowableActions = originalDoc.getAllowableActions();
        	        	assertNotNull(allowableActions);
        	        	assertFalse(allowableActions.getAllowableActions().contains(Action.CAN_DELETE_OBJECT));
        
        	        	// try to cancel the checkout
        	        	cmisService.deleteObjectOrCancelCheckOut(testContext.getRepositoryId(), objectData.getId(), Boolean.TRUE, null);
        	        	fail();
        	        }
        	        catch(CmisConstraintException e)
        	        {
        	        	// expected
        	        }

        	        return null;
                }
            });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    // cancel checkout on pwc
                    cmisService.deleteObjectOrCancelCheckOut(testContext.getRepositoryId(), testContext.getObjectId().getValue(), Boolean.TRUE, null);
                    return null;
                }
	        });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
    	        	// get original document
    	        	ObjectData originalDoc = cmisService.getObject(testContext.getRepositoryId(), objectData.getId(), null, true, IncludeRelationships.NONE, null, false, true, null);
    	        	Map<String, PropertyData<?>> properties = originalDoc.getProperties().getProperties();
    	        	PropertyData<Boolean> isVersionSeriesCheckedOutProp = (PropertyData<Boolean>)properties.get(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
    	        	assertNotNull(isVersionSeriesCheckedOutProp);
    	        	Boolean isVersionSeriesCheckedOut = isVersionSeriesCheckedOutProp.getFirstValue();
    	        	assertNotNull(isVersionSeriesCheckedOut);
    	        	assertEquals(Boolean.FALSE, isVersionSeriesCheckedOut);
    	        	
    	        	return null;
                }
            });
    		
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
    	        	// delete original document
                    cmisService.deleteObject(testContext.getRepositoryId(), objectData.getId(), true, null);
    	        	return null;
                }
            });
        	
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
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        final Map<FileInfo, Boolean> testFolderMap = new HashMap<FileInfo, Boolean>(4);

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

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
    
                    for (Map.Entry<FileInfo, Boolean> entry : testFolderMap.entrySet())
                    {
                        ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + entry.getKey().getName(), null, true, IncludeRelationships.NONE, null, false, true, null);
    
                        Holder<String> objectId = new Holder<String>(objectData.getId());
    
                        try
                        {
                            // delete folder
                            cmisService.deleteObjectOrCancelCheckOut(repositoryId, objectId.getValue(), Boolean.TRUE, null);
                        }
                        catch (CmisConstraintException ex)
                        {
                            assertTrue(!entry.getValue());
                            continue;
                        }
    
                        assertTrue(entry.getValue());
                    }

                    return null;
                }
            });
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

    /**
     * Test
     * <ul>
     *   <li>MNT-8825: READ_ONLYLOCK prevent getAllVersions via new CMIS enpoint.</li>
     *   <li>ACE-762: BM-0012: NodeLockedException not handled by CMIS</li>
     * </ul>
     */
    @Test
    public void testOperationsOnReadOnlyLockedNode()
    {
        final String folderName = "testfolder." + GUID.generate();
        final String docName = "testdoc.txt." + GUID.generate();

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        try
        {
            final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
            {
                @Override
                public FileInfo execute() throws Throwable
                {
                    NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                    FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);

                    FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);

                    versionService.createVersion(fileInfo.getNodeRef(), new HashMap<String, Serializable>());
                    lockService.lock(fileInfo.getNodeRef(), LockType.READ_ONLY_LOCK, 0, true);

                    return fileInfo;
                }
            });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
                    ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true,
                            IncludeRelationships.NONE, null, false, true, null);

                    // Expect no failure
                    cmisService.getAllVersions(repositoryId, objectData.getId(), fileInfo.getNodeRef().getId(), null, true, null);

                    return null;
                }
            });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
                    ObjectData objectData = cmisService.getObjectByPath(
                            repositoryId, "/" + folderName + "/" + docName, null, true,
                            IncludeRelationships.NONE, null, false, true, null);
                    String objectId = objectData.getId();

                    // Expect failure as the node is locked
                    try
                    {
                        cmisService.deleteObject(repositoryId, objectId, true, null);
                        fail("Locked node should not be deletable.");
                    }
                    catch (CmisUpdateConflictException e)
                    {
                        // Expected
                    }

                    return null;
                }
            });

        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * ALF-18455
     */
    @Test
    public void testOrderByCreationAndModificationDate()
    {
        final List<FileInfo> nodes = new ArrayList<FileInfo>(10);
        final List<FileInfo> expectedChildrenByCreationDate = new ArrayList<FileInfo>(10);
        final List<FileInfo> expectedChildrenByModificationDate = new ArrayList<FileInfo>(10);

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        try
        {
	        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
	        {
	            @Override
	            public Void execute() throws Throwable
	            {
	                NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
	
	                String folderName = GUID.generate();
	                FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
	                nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
	                assertNotNull(folderInfo);
	                nodes.add(folderInfo);

	                for(int i = 0; i < 5; i++)
	                {
	                    String docName = GUID.generate();
	                    FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
	                    assertNotNull(fileInfo);
	                    nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);

	                    expectedChildrenByCreationDate.add(0, fileInfo);
		                nodes.add(fileInfo);

	                    // make sure there is some difference in creation times
	                    Thread.sleep(400);
	                }

	                // make modifications
	                for(int i = 5; i > 0; i--)
	                {
	                	FileInfo fileInfo = nodes.get(i);
	                    assertNotNull(fileInfo);
	                    nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_DESCRIPTION, GUID.generate());

	                    // "refresh" fileInfo
	                    fileInfo = fileFolderService.getFileInfo(fileInfo.getNodeRef());
	                    assertNotNull(fileInfo);
	                    expectedChildrenByModificationDate.add(0, fileInfo);

	                    // make sure there is some difference in modification times
	                    Thread.sleep(400);
	                }
	                
	                return null;
	            }
	        });
        }
        finally
        {
        	AuthenticationUtil.popAuthentication();
        }

        withCmisService(new CmisServiceCallback<Void>()
        {
            @Override
            public Void execute(CmisService cmisService)
            {
                // get repository id
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                String repositoryId = repo.getId();
    
                String folderId = nodes.get(0).getNodeRef().getId();
                String orderBy = PropertyIds.CREATION_DATE + " DESC";
                ObjectInFolderList children = cmisService.getChildren(repositoryId, folderId, null, orderBy, false, IncludeRelationships.NONE,
                		null, false, BigInteger.valueOf(Integer.MAX_VALUE), BigInteger.valueOf(0), null);
                int i = 0;
                for(ObjectInFolderData child : children.getObjects())
                {
                	Map<String, PropertyData<?>> properties = child.getObject().getProperties().getProperties();
    
                	PropertyData<?> pObjectId = properties.get(PropertyIds.VERSION_SERIES_ID);
                	String actualObjectId = (String)pObjectId.getFirstValue();
                	PropertyData<?> pCreationDate = properties.get(PropertyIds.CREATION_DATE);
                	GregorianCalendar actualCreationDate = (GregorianCalendar)pCreationDate.getFirstValue();
    
                	FileInfo expectedChild = expectedChildrenByCreationDate.get(i++);
                	assertEquals(expectedChild.getNodeRef().toString(), actualObjectId);
                	assertEquals(expectedChild.getCreatedDate().getTime(), actualCreationDate.getTimeInMillis());
                }
    
                orderBy = PropertyIds.LAST_MODIFICATION_DATE + " DESC";
                children = cmisService.getChildren(repositoryId, folderId, null, orderBy, false, IncludeRelationships.NONE,
                		null, false, BigInteger.valueOf(Integer.MAX_VALUE), BigInteger.valueOf(0), null);
                i = 0;
                for(ObjectInFolderData child : children.getObjects())
                {
                	Map<String, PropertyData<?>> properties = child.getObject().getProperties().getProperties();
    
                	PropertyData<?> pObjectId = properties.get(PropertyIds.VERSION_SERIES_ID);
                	String actualObjectId = (String)pObjectId.getFirstValue();
                	PropertyData<?> pModificationDate = properties.get(PropertyIds.LAST_MODIFICATION_DATE);
                	GregorianCalendar actualModificationDate = (GregorianCalendar)pModificationDate.getFirstValue();
    
                	FileInfo expectedChild = expectedChildrenByModificationDate.get(i++);
                	assertEquals(expectedChild.getNodeRef().toString(), actualObjectId);
                	assertEquals(expectedChild.getModifiedDate().getTime(), actualModificationDate.getTimeInMillis());
                }

                return null;
            }
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testSecondaryTypes()
    {
        final String aspectName = "P:cm:indexControl";

        // get repository id
        final String repositoryId = withCmisService(new CmisServiceCallback<String>()
        {
            @Override
            public String execute(CmisService cmisService)
            {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                final String repositoryId = repo.getId();
                return repositoryId;
            }
        }, CmisVersion.CMIS_1_1);

        final String objectId = withCmisService(new CmisServiceCallback<String>()
        {
            @Override
            public String execute(CmisService cmisService)
            {
                final PropertiesImpl properties = new PropertiesImpl();
                String objectTypeId = "cmis:document";
                properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
                String fileName = "textFile" + GUID.generate();
                properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
                final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");

                String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.MAJOR, null, null);
                return objectId;
            }
        }, CmisVersion.CMIS_1_1);

        final Holder<String> objectIdHolder = new Holder<String>(objectId);

        withCmisService(new CmisServiceCallback<Void>()
        {
            @Override
            public Void execute(CmisService cmisService)
            {
                final PropertiesImpl properties = new PropertiesImpl();
                properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(aspectName)));

                cmisService.updateProperties(repositoryId, objectIdHolder, null, properties, null);
                return null;
            }
        }, CmisVersion.CMIS_1_1);

        final Properties currentProperties = withCmisService(new CmisServiceCallback<Properties>()
        {
            @Override
            public Properties execute(CmisService cmisService)
            {
                Properties properties = cmisService.getProperties(repositoryId, objectIdHolder.getValue(), null, null);
                return properties;
            }
        }, CmisVersion.CMIS_1_1);

        List secondaryTypeIds = currentProperties.getProperties().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();

        secondaryTypeIds.remove(aspectName);
        final PropertiesImpl newProperties = new PropertiesImpl();
        newProperties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypeIds));

        withCmisService(new CmisServiceCallback<Void>()
        {
            @Override
            public Void execute(CmisService cmisService)
            {
                cmisService.updateProperties(repositoryId, objectIdHolder, null, newProperties, null);
                return null;
            }
        }, CmisVersion.CMIS_1_1);

        Properties currentProperties1 = withCmisService(new CmisServiceCallback<Properties>()
        {
            @Override
            public Properties execute(CmisService cmisService)
            {
                Properties properties = cmisService.getProperties(repositoryId, objectIdHolder.getValue(), null, null);
                return properties;
            }
        }, CmisVersion.CMIS_1_1);
        secondaryTypeIds = currentProperties1.getProperties().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();

    }

    /**
     * Test for MNT-9089
     */
    @Test
    public void testIntegerBoudaries() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        try
        {
	    	final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
	        {
	            @Override
	            public FileInfo execute() throws Throwable
	            {
	                NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
	                
	                QName testIntTypeQName = QName.createQName("http://testCMISIntegersModel/1.0/", "testintegerstype");
	
	                String folderName = GUID.generate();
	                FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
	                nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
	                assertNotNull(folderInfo);
	
	                String docName = GUID.generate();
	                FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, testIntTypeQName);
	                assertNotNull(fileInfo);
	                nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);

	                return fileInfo;
	            }
	        });

            // get repository id
	    	withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();

                    String objectIdStr = fileInfo.getNodeRef().toString();
                    
                    TypeDefinition typeDef = cmisService.getTypeDefinition(repositoryId, "D:tcim:testintegerstype", null);
                    
                    PropertyIntegerDefinitionImpl intNoBoundsTypeDef = 
                            (PropertyIntegerDefinitionImpl)typeDef.getPropertyDefinitions().get("tcim:int");
                    PropertyIntegerDefinitionImpl longNoBoundsTypeDef = 
                            (PropertyIntegerDefinitionImpl)typeDef.getPropertyDefinitions().get("tcim:long");
                    
                    PropertyIntegerDefinitionImpl intWithBoundsTypeDef = 
                            (PropertyIntegerDefinitionImpl)typeDef.getPropertyDefinitions().get("tcim:intwithbounds");
                    PropertyIntegerDefinitionImpl longWithBoundsTypeDef = 
                            (PropertyIntegerDefinitionImpl)typeDef.getPropertyDefinitions().get("tcim:longwithbounds");
                    
                    BigInteger minInteger = BigInteger.valueOf(Integer.MIN_VALUE);
                    BigInteger maxInteger = BigInteger.valueOf(Integer.MAX_VALUE);
                    
                    BigInteger minLong = BigInteger.valueOf(Long.MIN_VALUE);
                    BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
                    
                    // test for default boundaries
                    assertTrue(intNoBoundsTypeDef.getMinValue().equals(minInteger));
                    assertTrue(intNoBoundsTypeDef.getMaxValue().equals(maxInteger));
                    
                    assertTrue(longNoBoundsTypeDef.getMinValue().equals(minLong));
                    assertTrue(longNoBoundsTypeDef.getMaxValue().equals(maxLong));
                    
                    // test for pre-defined boundaries
                    assertTrue(intWithBoundsTypeDef.getMinValue().equals(BigInteger.valueOf(-10L)));
                    assertTrue(intWithBoundsTypeDef.getMaxValue().equals(BigInteger.valueOf(10L)));
                    
                    assertTrue(longWithBoundsTypeDef.getMinValue().equals(BigInteger.valueOf(-10L)));
                    assertTrue(longWithBoundsTypeDef.getMaxValue().equals(BigInteger.valueOf(10L)));
                    
                    try // try to overfloat long without boundaries
                    {
                        BigInteger aValue = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(1L));
                        setProperiesToObject(cmisService, repositoryId, objectIdStr, "tcim:long", aValue);
                        fail();
                    }
                    catch(Exception e)
                    {
                        assertTrue(e instanceof CmisConstraintException);
                    }
                    
                    try // try to overfloat int without boundaries
                    {
                        BigInteger aValue = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.valueOf(1L));
                        setProperiesToObject(cmisService, repositoryId, objectIdStr, "tcim:int", aValue);
                        fail();
                    }
                    catch(Exception e)
                    {
                        assertTrue(e instanceof CmisConstraintException);
                    }
                    
                    try // try to overfloat int with boundaries
                    {
                        BigInteger aValue = BigInteger.valueOf( 11l );
                        setProperiesToObject(cmisService, repositoryId, objectIdStr, "tcim:intwithbounds", aValue);
                        fail();
                    }
                    catch(Exception e)
                    {
                        assertTrue(e instanceof CmisConstraintException);
                    }
                    
                    try // try to overfloat long with boundaries
                    {
                        BigInteger aValue = BigInteger.valueOf( 11l );
                        setProperiesToObject(cmisService, repositoryId, objectIdStr, "tcim:longwithbounds", aValue);
                        fail();
                    }
                    catch(Exception e)
                    {
                        assertTrue(e instanceof CmisConstraintException);
                    }

                    return null;
                }
            }, CmisVersion.CMIS_1_0);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    private void setProperiesToObject(CmisService cmisService, String repositoryId, String objectIdStr, String propertyStr, BigInteger bigIntValue) throws CmisConstraintException{
        Properties properties = cmisService.getProperties(repositoryId, objectIdStr, null, null);
        PropertyIntegerImpl pd = (PropertyIntegerImpl)properties.getProperties().get(propertyStr);
        pd.setValue(bigIntValue);
        
        Collection<PropertyData<?>> propsList = new ArrayList<PropertyData<?>>();
        propsList.add(pd);
        
        Properties newProps = new PropertiesImpl(propsList);
        
        cmisService.updateProperties(repositoryId, new Holder<String>(objectIdStr), null, newProps, null);
    }
    
    @Test
    public void testMNT9090() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        try
        {
	    	final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
	        {
	            @Override
	            public FileInfo execute() throws Throwable
	            {
	                NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
	
	                String folderName = GUID.generate();
	                FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
	                nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
	                assertNotNull(folderInfo);
	
	                String docName = GUID.generate();
	                FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
	                assertNotNull(fileInfo);
	                nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);
	                
	                QName ASPECT_AUDIO = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, "audio");
	                Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
	                nodeService.addAspect(fileInfo.getNodeRef(), ASPECT_AUDIO, aspectProperties);

	                return fileInfo;
	            }
	        });

	    	withCmisService(new CmisServiceCallback<Void>()
	    	{
	    	    @Override
	    	    public Void execute(CmisService cmisService)
	    	    {
	    	        // get repository id
	    	        List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
	    	        assertTrue(repositories.size() > 0);
	    	        RepositoryInfo repo = repositories.get(0);
	    	        String repositoryId = repo.getId();

	    	        String objectIdStr = fileInfo.getNodeRef().toString();
	    	        Holder<String> objectId = new Holder<String>(objectIdStr);

	    	        // try to overflow the value
	    	        Object value = BigInteger.valueOf(Integer.MAX_VALUE + 1l);

	    	        Properties properties = new PropertiesImpl();
	    	        List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

	    	        CmisExtensionElement valueElem = new CmisExtensionElementImpl(CMISConnector.ALFRESCO_EXTENSION_NAMESPACE, "value", null, value.toString());
	    	        List<CmisExtensionElement> valueElems = new ArrayList<CmisExtensionElement>();
	    	        valueElems.add(valueElem);

	    	        List<CmisExtensionElement> children = new ArrayList<CmisExtensionElement>();
	    	        Map<String, String> attributes = new HashMap<String, String>();
	    	        attributes.put("propertyDefinitionId", "audio:trackNumber");
	    	        children.add(new CmisExtensionElementImpl(CMISConnector.ALFRESCO_EXTENSION_NAMESPACE, "propertyInteger", attributes, valueElems));

	    	        List<CmisExtensionElement> propertyValuesExtension = new ArrayList<CmisExtensionElement>();
	    	        propertyValuesExtension.add(new CmisExtensionElementImpl(CMISConnector.ALFRESCO_EXTENSION_NAMESPACE, CMISConnector.PROPERTIES, null, children));

	    	        CmisExtensionElement setAspectsExtension = new CmisExtensionElementImpl(CMISConnector.ALFRESCO_EXTENSION_NAMESPACE, CMISConnector.SET_ASPECTS, null, propertyValuesExtension);
	    	        extensions.add(setAspectsExtension);
	    	        properties.setExtensions(extensions);

	    	        // should throw a CMISConstraintException
	    	        cmisService.updateProperties(repositoryId, objectId, null, properties, null);
	    	        fail();

	    	        return null;
	    	    }
	    	}, CmisVersion.CMIS_1_0);
        }
        catch(CmisConstraintException e)
        {
        	assertTrue(e.getMessage().startsWith("Value is out of range for property"));
        	// ok
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    public void testGetContentChanges()
    {
        // create folder with file
        String folderName = "testfolder" + GUID.generate();
        String docName = "testdoc.txt" + GUID.generate();
        createContent(folderName, docName, false);
        folderName = "testfolder" + GUID.generate();
        docName = "testdoc.txt" + GUID.generate();
        createContent(folderName, docName, false);
        Holder<String> changeLogToken = new Holder<String>();
        ObjectList ol = this.cmisConnector.getContentChanges(changeLogToken, new BigInteger("2"));
        assertEquals(2, ol.getNumItems());
        assertEquals("3", changeLogToken.getValue());
    }

    /**
     * MNT-10223
     * Check the IsLatestMajorVersion for a doc with minor version.
     */
    @SuppressWarnings("unused")
    @Test
    public void testIsLatestMajorVersion()
    {
        final TestContext testContext = new TestContext();

        // create simple text plain content
        final PropertiesImpl properties = new PropertiesImpl();
        String objectTypeId = "cmis:document";
        properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
        String fileName = "textFile" + GUID.generate();
        properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
        final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");

        withCmisService(new CmisServiceCallback<String>() {
            @Override
            public String execute(CmisService cmisService) {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                String repositoryId = repo.getId();

                String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.MINOR, null, null);

                ObjectData cmidDoc = cmisService.getObject(repositoryId, objectId, null, true, IncludeRelationships.NONE, null, false, false, null);
                List<PropertyData<?>> properties = cmidDoc.getProperties().getPropertyList();
                boolean found = false;
                PropertyData<?> propIsLatestMajorVersion = null;
                for (PropertyData<?> property : properties)
                {
                    if (property.getId().equals(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION))
                    {
                        found = true;
                        propIsLatestMajorVersion = property;
                        break;
                    }
                }
                //properties..contains(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION);
                assertTrue("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION property was not found", found);
                if (found)
                {
                    assertFalse("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION should be false as minor version was created", (Boolean) propIsLatestMajorVersion.getValues().get(0));
                }
                return objectId;
            }
        });
    }
    
    /**
     * ACE-33
     * 
     * Cmis Item support
     */
    @Test
    public void testItems()
    {

        withCmisService(new CmisServiceCallback<String>() {
            @Override
            public String execute(CmisService cmisService) {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                String repositoryId = repo.getId();
                
            	TypeDefinition def = cmisService.getTypeDefinition(repositoryId, "cmis:item", null);
            	assertNotNull("the cmis:item type is not defined", def); 
                
            	@SuppressWarnings("unused")
                TypeDefinition p = cmisService.getTypeDefinition(repositoryId, "I:cm:person", null);
            	assertNotNull("the I:cm:person type is not defined", def); 
            	
            	ObjectList result = cmisService.query(repositoryId, "select * from cm:person", Boolean.FALSE, Boolean.TRUE, IncludeRelationships.NONE, "", BigInteger.TEN, BigInteger.ZERO, null);
            	assertTrue("", result.getNumItems().intValue() > 0);
            	return "";
        
            };
        }, CmisVersion.CMIS_1_1);
    	
    }

    @Test
    public void testMNT10529() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        try
        {
            final Pair<FileInfo, FileInfo> folderAndDocument = transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<Pair<FileInfo, FileInfo>>()
                    {
                        @Override
                        public Pair<FileInfo, FileInfo> execute() throws Throwable
                        {
                            NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                            String folderName = GUID.generate();
                            FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                            nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                            assertNotNull(folderInfo);

                            String docName = GUID.generate();
                            FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                            assertNotNull(document);
                            nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, docName);

                            return new Pair<FileInfo, FileInfo>(folderInfo, document);
                        }
                    });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertNotNull(repositories);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.iterator().next();
                    String repositoryId = repo.getId();

                    String objectIdStr = folderAndDocument.getFirst().getNodeRef().toString();

                    ObjectInFolderList children = cmisService.getChildren(repositoryId, objectIdStr, null, "cmis:name ASC", false, IncludeRelationships.NONE, null, false, null,
                            null, null);
                    assertChildren(folderAndDocument, children);

                    children = cmisService.getChildren(repositoryId, objectIdStr, null, "cmis:name ASC, cmis:creationDate ASC", false, IncludeRelationships.NONE, null, false,
                            null, null, null);
                    assertChildren(folderAndDocument, children);

                    children = cmisService.getChildren(repositoryId, objectIdStr, null, "    cmis:name ASC", false, IncludeRelationships.NONE, null, false, null, null, null);
                    assertChildren(folderAndDocument, children);

                    children = cmisService.getChildren(repositoryId, objectIdStr, null, "    cmis:name ASC, cmis:creationDate ASC   ", false, IncludeRelationships.NONE, null,
                            false, null, null, null);
                    assertChildren(folderAndDocument, children);

                    return null;
                }

                private void assertChildren(final Pair<FileInfo, FileInfo> folderAndDocument, ObjectInFolderList children)
                {
                    assertNotNull(children);
                    assertTrue(1 == children.getNumItems().longValue());

                    PropertyData<?> nameData = children.getObjects().iterator().next().getObject().getProperties().getProperties().get("cmis:name");
                    assertNotNull(nameData);
                    Object name = nameData.getValues().iterator().next();
                    assertEquals(folderAndDocument.getSecond().getName(), name);
                }
            }, CmisVersion.CMIS_1_0);
        }
        catch (CmisConstraintException e)
        {
            fail(e.toString());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    @Test
    public void mnt10548test() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final Pair<FileInfo, FileInfo> folderAndDocument = transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Pair<FileInfo, FileInfo>>()
                {
                    private final static String TEST_NAME = "mnt10548test-";
                    
                    @Override
                    public Pair<FileInfo, FileInfo> execute() throws Throwable
                    {
                        NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
                        
                        /* Create folder within companyHome */
                        String folderName = TEST_NAME + GUID.generate();
                        FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                        nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                        assertNotNull(folderInfo);
                        
                        /* Create content */
                        String docName = TEST_NAME + GUID.generate();
                        FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                        assertNotNull(document);
                        nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, docName);
                        
                        /* Add some tags */
                        NodeRef nodeRef = document.getNodeRef();
                        taggingService.addTag(nodeRef, "tag1");
                        taggingService.addTag(nodeRef, "tag2");
                        taggingService.addTag(nodeRef, "tag3");

                        return new Pair<FileInfo, FileInfo>(folderInfo, document);
                    }
                });
        
        ObjectData objData = withCmisService(
                new CmisServiceCallback<ObjectData>() 
                {
                    private static final String FILE_FOLDER_SEPARATOR = "/";
                    
                    @Override
                    public ObjectData execute(CmisService cmisService) 
                    {
                        List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                        assertTrue(repositories.size() > 0);
                        RepositoryInfo repo = repositories.get(0);
                        String repositoryId = repo.getId();
                        
                        String path = FILE_FOLDER_SEPARATOR + folderAndDocument.getFirst().getName() + FILE_FOLDER_SEPARATOR + folderAndDocument.getSecond().getName();
                        /* get CMIS object of document */
                        ObjectData objData = cmisService.getObjectByPath(repositoryId, path, null, false, null, null, false, false, null);
                        return objData;
                    }
                }, CmisVersion.CMIS_1_1);
        
        Map<String, PropertyData<?>> cmisProps = objData.getProperties().getProperties();
        
        String taggable = ContentModel.ASPECT_TAGGABLE.getPrefixedQName(namespaceService).toPrefixString();
        PropertyData<?> propData = cmisProps.get(taggable);
        assertNotNull(propData);
        
        List<?> props = propData.getValues();
        assertTrue(props.size() == 3);
        
        /* MNT-10548 fix : CMIS should return List of String, not List of NodeRef */
        for(Object o : props)
        {
            assertTrue(o.getClass() + " found but String expected", o instanceof String);
        }
    }
}
