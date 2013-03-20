/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.quickshare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporaryModels;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
import org.springframework.context.ApplicationContext;

/**
 * Quick share service tests.
 *
 * @author Alex Miller
 * @since Cloud/4.2
 */
public class QuickShareServiceIntegrationTest
{
    private static final ApplicationContextInit testContext = new ApplicationContextInit();
    
    private static final String MODEL = 
    	"<?xml version='1.0' encoding='UTF-8'?>" +
		"<model name='lx:lxmodel' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
            "<description>LX model</description>" +
            "<author>Peter Löfgren</author>" +
            "<version>1.0</version>" +
            "<imports>" +
	            "<import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d' />" +
                "<import uri='http://www.alfresco.org/model/content/1.0' prefix='cm' />" +
	        "</imports>" +
            "<namespaces>" +
	            "<namespace uri='http://bugtestmodel' prefix='lx' />" +
            "</namespaces>" +
	        "<constraints>" +
            "</constraints>" +
	        "<types>" +
                "<type name='lx:doc'>" +
		            "<title>LX dokument</title>" +
                    "<parent>cm:content</parent>" +
		            "<mandatory-aspects>" +
                        "<aspect>cm:generalclassifiable</aspect>" +
		            "</mandatory-aspects>" +
                "</type>" +
                "<type name='lx:doc2'>" +
		            "<title>LX dokument 2</title>" +
                    "<parent>cm:cmobject</parent>" +
                "</type>" +
		    "</types>" +
        "</model>";
    
    private static CopyService copyService;
    private static NodeService nodeService;
    private static QuickShareService quickShareService;
    private static DictionaryService dictionaryService;
    private static Repository repository;
    private static AttributeService attributeService;
    
    private static AlfrescoPerson user1 = new AlfrescoPerson(testContext, "UserOne");
    private static AlfrescoPerson user2 = new AlfrescoPerson(testContext, "UserTwo");
    
    // A rule to manage test nodes reused across all the test methods
    @Rule public TemporaryNodes testNodes = new TemporaryNodes(testContext);
    
    @Rule public TemporaryModels temporaryModels = new TemporaryModels(testContext);

    @ClassRule public static RuleChain classChain = RuleChain.outerRule(testContext)
                                                         .around(user1)
                                                         .around(user2);

    private NodeRef testNode;

    private NodeRef userHome;

    @BeforeClass public static void beforeClass() throws Exception
    {
        findServices();
    }    
    

    private static void findServices()
    {
        ApplicationContext ctx = testContext.getApplicationContext();
        
        copyService = ctx.getBean("CopyService", CopyService.class);
        dictionaryService = ctx.getBean("dictionaryService", DictionaryService.class);
        nodeService = ctx.getBean("NodeService", NodeService.class);
        quickShareService = ctx.getBean("QuickShareService", QuickShareService.class);
        repository = ctx.getBean("repositoryHelper", Repository.class);
        attributeService = ctx.getBean("AttributeService", AttributeService.class);
    }
    
    @Before public void createTestData()
    {
        userHome = repository.getUserHome(user1.getPersonNode());
        
        testNode = testNodes.createNodeWithTextContent(userHome,
                        "Quick Share Test Node",
                        ContentModel.TYPE_CONTENT, 
                        user1.getUsername(),
                        "Quick Share Test Node Content");
    }

    @Test public void getMetaDataFromNodeRefByOwner() 
    {
        Map<String, Object> metadata = AuthenticationUtil.runAs(new RunAsWork<Map<String,Object>>(){

            @Override
            public Map<String, Object> doWork() throws Exception
            {
                return quickShareService.getMetaData(testNode);    
            }
        }, user1.getUsername());
        
        assertNotNull(metadata);
        assertTrue(metadata.size() > 0);
    }
    
    @Test(expected=AccessDeniedException.class) 
    public void getMetaDataFromNodeRefByNonOwner() 
    {
        Map<String, Object> metadata = AuthenticationUtil.runAs(new RunAsWork<Map<String,Object>>(){

            @Override
            public Map<String, Object> doWork() throws Exception
            {
                return quickShareService.getMetaData(testNode);    
            }
        }, user2.getUsername());
        
    }

    @Test public void share() 
    {
        share(testNode, user1.getUsername());
        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>(){

            @Override
            public Void doWork() throws Exception
            {
                assertTrue( nodeService.getAspects(testNode).contains(QuickShareModel.ASPECT_QSHARE));
                assertNotNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));
                assertEquals(user1.getUsername(), nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDBY));
                return null;
            }
            
        });
    }
    
    @Test public void unshare() {
		final QuickShareDTO dto = share(testNode, user1.getUsername());
        unshare(dto.getId(), user1.getUsername());
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>(){

            @Override
            public Void doWork() throws Exception
            {
                assertFalse( nodeService.getAspects(testNode).contains(QuickShareModel.ASPECT_QSHARE));
                assertNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));
                assertNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDBY));
                return null;
            }
            
        });
    }


	private void unshare(final String sharedId, final String userName) {
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {

            @Override
            public Void doWork() throws Exception
            {
                quickShareService.unshareContent(sharedId);
                return null;
            }
        }, userName);
	}

    private QuickShareDTO share(final NodeRef nodeRef, String username)
    {
        return AuthenticationUtil.runAs(new RunAsWork<QuickShareDTO>()
        {
            @Override
            public QuickShareDTO doWork() throws Exception
            {
                return quickShareService.shareContent(nodeRef);
            }
        }, username);
    }
    
    @Test public void getMetadataFromShareId()
    {
        QuickShareDTO dto = share(testNode, user1.getUsername());
        
        Map<String, Object> metadata = quickShareService.getMetaData(dto.getId());
        
        assertNotNull(metadata);
        assertTrue(metadata.size() > 0);
    }
    
    @Test(expected=InvalidSharedIdException.class) public void getMetadataFromShareIdWithInvalidId()
    {
        UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
        String sharedId = Base64.encodeBase64URLSafeString(uuid.toByteArray()); // => 22 chars (eg. q3bEKPeDQvmJYgt4hJxOjw)

        Map<String, Object> metadata = quickShareService.getMetaData(sharedId);
    }

    @Test public void copyNode()
    {
        share(testNode, user1.getUsername());
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {

            @Override
            public Object doWork() throws Exception
            {
                
                Assert.assertTrue(nodeService.hasAspect(testNode, QuickShareModel.ASPECT_QSHARE));
                Assert.assertNotNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDBY));
                Assert.assertNotNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));

                Map<QName, Serializable> originalProps = nodeService.getProperties(testNode);
                
                NodeRef copyNodeRef = copyService.copyAndRename(testNode, userHome, ContentModel.ASSOC_CONTAINS, 
                            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "copy"), true);
                
                Map<QName, Serializable> copyProps = nodeService.getProperties(copyNodeRef);
                
                Assert.assertFalse(nodeService.hasAspect(copyNodeRef, QuickShareModel.ASPECT_QSHARE));
                Assert.assertNull(nodeService.getProperty(copyNodeRef, QuickShareModel.PROP_QSHARE_SHAREDBY));
                Assert.assertNull(nodeService.getProperty(copyNodeRef, QuickShareModel.PROP_QSHARE_SHAREDID));
                
                for (QName property : originalProps.keySet())
                {
                    if (property.equals(QuickShareModel.PROP_QSHARE_SHAREDBY) ||
                        property.equals(QuickShareModel.PROP_QSHARE_SHAREDID))
                    {
                        continue;
                    }
                    Assert.assertTrue("Mising property " + property, copyProps.containsKey(property));
                }
                return null;
            }
        }, user1.getUsername());
    }
    
    /**
     * Content types that extend cm:content should be shareable.
     * 
     * See https://issues.alfresco.com/jira/browse/ALF-16274.
     */
    @Test public void testWithCustomContentType() 
    {
    	ByteArrayInputStream modelStream = new ByteArrayInputStream(MODEL.getBytes());
    	temporaryModels.loadModel(modelStream);
        
        QName sharableType = QName.createQName("{http://bugtestmodel}doc");
        QName unsharableType = QName.createQName("{http://bugtestmodel}doc2");

    	final NodeRef sharableNode = testNodes.createNodeWithTextContent(userHome,
                           "Quick Share Custom Type Sharable Test Node",
                           sharableType, 
                           user1.getUsername(),
                           "Quick Share Test Node Content");
    	
        Map<String, Object> metadata = getMetadata(sharableNode, user1);
        
        assertTrue((Boolean)metadata.get("sharable"));
        
        QuickShareDTO dto = share(sharableNode, user1.getUsername());
        unshare(dto.getId(), user1.getUsername());        
        
    	final NodeRef unsharableNode = testNodes.createNodeWithTextContent(userHome,
                "Quick Share Custom Type Unsharable Test Node",
                unsharableType, 
                user1.getUsername(),
                "Quick Share Test Node Content");

		metadata = getMetadata(unsharableNode, user1);
        assertFalse((Boolean)metadata.get("sharable"));

		boolean exceptionThrown = false;
		try {
			// Prior to fixing ALF-16274, this would throw an InvalidNodeRefException. 
			share(unsharableNode, user1.getUsername());
		}
		catch(InvalidNodeRefException ex)
		{
			exceptionThrown = true;
		}
		assertTrue("InvalidNodeRefException not thrown on trying to share an unsharable content type", exceptionThrown);
    }


	@SuppressWarnings("unchecked")
	private Map<String, Object> getMetadata(final NodeRef nodeRef, AlfrescoPerson user) {
		Map<String, Object> container = AuthenticationUtil.runAs(new RunAsWork<Map<String, Object>>()
        {
            @Override
            public Map<String, Object> doWork() throws Exception
            {
            	return quickShareService.getMetaData(nodeRef);
            }
        }, user.getUsername());
		return (Map<String, Object>)container.get("item");
	}
	
    @Test public void cloud928()
    {
        final NodeRef node = testNodes.createNodeWithTextContent(userHome,
                "CLOUD-928 Test Node",
                ContentModel.TYPE_CONTENT, 
                user1.getUsername(),
                "Quick Share Test Node Content");
        
        QuickShareDTO dto = share(node, user1.getUsername());

        attributeService.removeAttribute(QuickShareServiceImpl.ATTR_KEY_SHAREDIDS_ROOT, dto.getId());
        
        AuthenticationUtil.runAs(new RunAsWork<Object>(){

            @Override
            public Object doWork() throws Exception {
                nodeService.deleteNode(node);
                return null;
            }
        }, user1.getUsername());
 
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        Assert.assertFalse(nodeService.exists(node));
    }
}
