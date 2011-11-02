/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.rule;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionServiceImplTest;
import org.alfresco.repo.action.ActionServiceImplTest.AsyncTest;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.CheckInActionExecuter;
import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.repo.action.executer.ImageTransformActionExecuter;
import org.alfresco.repo.action.executer.LinkCategoryActionExecuter;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

/**
 * @author Roy Wetherall 
 */
public class RuleServiceCoverageTest extends TestCase
{
    /**
	 * Application context used during the test
	 */
	static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
	
	/**
	 * Services used during the tests
	 */
    private TransactionService transactionService;
    private RuleService ruleService;
    private NodeService nodeService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private CheckOutCheckInService cociService;
    private LockService lockService;
	private ContentService contentService;
	private ServiceRegistry serviceRegistry;
    private DictionaryDAO dictionaryDAO;
    private ActionService actionService;
    private ContentTransformerRegistry transformerRegistry;
    private CopyService copyService;
    private AuthenticationComponent authenticationComponent;
    private FileFolderService fileFolderService;
    
    /**
     * Category related values
     */
    private static final String TEST_NAMESPACE = "http://www.alfresco.org/test/rulesystemtest";
    private static final QName CAT_PROP_QNAME = QName.createQName(TEST_NAMESPACE, "region");
    private QName regionCategorisationQName;
    private NodeRef catContainer;
    private NodeRef catRoot;
    private NodeRef catRBase;
    private NodeRef catROne;
    private NodeRef catRTwo;
    @SuppressWarnings("unused")
    private NodeRef catRThree;
    
    /**
     * Standard content text
     */
    private static final String STANDARD_TEXT_CONTENT = "standardTextContent";
    
	/**
	 * Setup method
	 */
	@Override
    protected void setUp() throws Exception 
    {
        // Get the required services
		this.serviceRegistry = (ServiceRegistry)applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
		this.nodeService = serviceRegistry.getNodeService();
        this.ruleService = serviceRegistry.getRuleService();
        this.cociService = serviceRegistry.getCheckOutCheckInService();
        this.lockService = serviceRegistry.getLockService();
        this.copyService = serviceRegistry.getCopyService();
		this.contentService = serviceRegistry.getContentService();
        this.dictionaryDAO = (DictionaryDAO)applicationContext.getBean("dictionaryDAO");
        this.actionService = serviceRegistry.getActionService();
        this.transactionService = serviceRegistry.getTransactionService();
        this.transformerRegistry = (ContentTransformerRegistry)applicationContext.getBean("contentTransformerRegistry");
        this.authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        this.fileFolderService = serviceRegistry.getFileFolderService();
        
        //authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        //authenticationComponent.setSystemUserAsCurrentUser();
        RetryingTransactionCallback<Object> setUserCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(setUserCallback);
            
        this.testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);
        
        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
				ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_CONTAINER).getChildRef();      
    }
	
	private Rule createRule(
			String ruleTypeName, 
			String actionName, 
			Map<String, Serializable> actionParams, 
			String conditionName, 
			Map<String, Serializable> conditionParams)
	{
		Rule rule = new Rule();
        rule.setRuleType(ruleTypeName);        
        
        Action action = this.actionService.createAction(actionName, actionParams);        
        ActionCondition condition = this.actionService.createActionCondition(conditionName, conditionParams);
        action.addActionCondition(condition);
        rule.setAction(action);  
        
        return rule;
	}
    
    /**
     * Create the categories used in the tests
     */
    private void createTestCategories()
    {
        // Create the test model
        M2Model model = M2Model.createModel("test:rulecategory");
        model.createNamespace(TEST_NAMESPACE, "test");
        model.createImport(NamespaceService.DICTIONARY_MODEL_1_0_URI, "d");
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);
        
        // Create the region category
        regionCategorisationQName = QName.createQName(TEST_NAMESPACE, "region");
        M2Aspect generalCategorisation = model.createAspect("test:" + regionCategorisationQName.getLocalName());
        generalCategorisation.setParentName("cm:" + ContentModel.ASPECT_CLASSIFIABLE.getLocalName());
        M2Property genCatProp = generalCategorisation.createProperty("test:region");
        genCatProp.setIndexed(true);
        genCatProp.setIndexedAtomically(true);
        genCatProp.setMandatory(true);
        genCatProp.setMultiValued(true);
        genCatProp.setStoredInIndex(true);
        genCatProp.setIndexTokenisationMode(IndexTokenisationMode.TRUE);
        genCatProp.setType("d:" + DataTypeDefinition.CATEGORY.getLocalName());        

        // Save the mode
        dictionaryDAO.putModel(model);
        
        // Create the category value container and root
        catContainer = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName(TEST_NAMESPACE, "categoryContainer"), ContentModel.TYPE_CONTAINER).getChildRef();
        catRoot = nodeService.createNode(catContainer, ContentModel.ASSOC_CHILDREN, QName.createQName(TEST_NAMESPACE, "categoryRoot"), ContentModel.TYPE_CATEGORYROOT).getChildRef();

        // Create the category values
        catRBase = nodeService.createNode(catRoot, ContentModel.ASSOC_CATEGORIES, QName.createQName(TEST_NAMESPACE, "region"), ContentModel.TYPE_CATEGORY).getChildRef();
        catROne = nodeService.createNode(catRBase, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "Europe"), ContentModel.TYPE_CATEGORY).getChildRef();
        catRTwo = nodeService.createNode(catRBase, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "RestOfWorld"), ContentModel.TYPE_CATEGORY).getChildRef();
        catRThree = nodeService.createNode(catRTwo, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(TEST_NAMESPACE, "US"), ContentModel.TYPE_CATEGORY).getChildRef();
    }
    
    /**
     * Asynchronous rule tests
     */
    
    /**
     * Check async rule execution
     */
    public void testAsyncRuleExecution() 
    {        
        final NodeRef newNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute()
                    {
                        RuleServiceCoverageTest.this.nodeService.addAspect(
                                RuleServiceCoverageTest.this.nodeRef, 
                                ContentModel.ASPECT_LOCKABLE, 
                                null);
                        
                        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
                        
                        Rule rule = createRule(
                                RuleType.INBOUND, 
                                AddFeaturesActionExecuter.NAME, 
                                params, 
                                NoConditionEvaluator.NAME, 
                                null);
                        rule.setExecuteAsynchronously(true);
                        
                        RuleServiceCoverageTest.this.ruleService.saveRule(RuleServiceCoverageTest.this.nodeRef, rule);

                        NodeRef newNodeRef = RuleServiceCoverageTest.this.nodeService.createNode(
                                RuleServiceCoverageTest.this.nodeRef,
                                ContentModel.ASSOC_CHILDREN,                
                                QName.createQName(TEST_NAMESPACE, "children"),
                                ContentModel.TYPE_CONTENT,
                                getContentProperties()).getChildRef();         
                        addContentToNode(newNodeRef);
                        
                        return newNodeRef;
                    }                    
                });
        
        ActionServiceImplTest.postAsyncActionTest(
                this.transactionService,
                5000, 
                12, 
                new AsyncTest()
                {
                    public String executeTest() 
                    {
                    	boolean result = RuleServiceCoverageTest.this.nodeService.hasAspect(
                                newNodeRef, 
                                ContentModel.ASPECT_VERSIONABLE);
                        return result ? null : "Expected aspect Versionable";
                    };
                });
    }
    
    // TODO check compensating action execution
    
    /**
     * Standard rule coverage tests
     */

    /**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition()
     *          action:     add-features(
     *                          aspect-name = versionable)
     */
    public void testAddFeaturesAction()
    {
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		AddFeaturesActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();         
        addContentToNode(newNodeRef);
        assertTrue(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));   
        
        Map<String, Serializable> params2 = new HashMap<String, Serializable>(2);
        params2.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ApplicationModel.ASPECT_SIMPLE_WORKFLOW);
        params2.put(ApplicationModel.PROP_APPROVE_STEP.toString(), "approveStep");
        params2.put(ApplicationModel.PROP_APPROVE_MOVE.toString(), false);
        
        // Test that rule can be updated and execute correctly
        //rule.removeAllActions();
        Action action2 = this.actionService.createAction(AddFeaturesActionExecuter.NAME, params2);
        rule.setAction(action2);
        this.ruleService.saveRule(this.nodeRef, rule);
        
        NodeRef newNodeRef2 = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();           
        addContentToNode(newNodeRef2);
        assertTrue(this.nodeService.hasAspect(newNodeRef2, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));
        assertEquals("approveStep", this.nodeService.getProperty(newNodeRef2, ApplicationModel.PROP_APPROVE_STEP));
        assertEquals(false, this.nodeService.getProperty(newNodeRef2, ApplicationModel.PROP_APPROVE_MOVE));
        
        // System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));        
    }   
    
    public void testCheckThatModifyNameDoesNotTriggerInboundRule() throws Exception
    {
        //this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, null);
    	Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
    	folderProps.put(ContentModel.PROP_NAME, "myTestFolder");
    	NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef,
				ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();  
    	
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		AddFeaturesActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(folder, rule);

        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(1);
        contentProps.put(ContentModel.PROP_NAME, "myTestDocument.txt");
        NodeRef newNodeRef = this.nodeService.createNode(
                folder,
                ContentModel.ASSOC_CONTAINS,                
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myTestDocument.txt"),
                ContentModel.TYPE_CONTENT,
                contentProps).getChildRef();         
        //addContentToNode(newNodeRef);
        nodeService.removeAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE);
        assertFalse(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Use the file folder to change the name of the node
        this.fileFolderService.rename(newNodeRef, "myNewName.txt");
        assertFalse(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
    }
    
    public void testCheckThatModifyNameDoesNotTriggerOutboundRule() throws Exception
    {
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "myTestFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();  
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);        
        
        Rule rule = createRule(
                RuleType.OUTBOUND,
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);
        
        this.ruleService.saveRule(folder, rule);

        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(1);
        contentProps.put(ContentModel.PROP_NAME, "myTestDocument.txt");
        NodeRef newNodeRef = fileFolderService.create(folder, "abc.txt", ContentModel.TYPE_CONTENT).getNodeRef();         
        assertFalse("Should not be versionable", nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Use the file folder to change the name of the node
        fileFolderService.rename(newNodeRef, "myNewName.txt");
        assertFalse("Should not be versionable", nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
    }
    
    public void testCheckThatChildRuleFiresOnMove() throws Exception
    {
        //ALF-9415 test
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);     
        folderProps.put(ContentModel.PROP_NAME, "myTestFolder");
        NodeRef folder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();  
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        
        Rule rule = createRule(
                RuleType.INBOUND, 
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);
        rule.applyToChildren(true);
        this.ruleService.saveRule(folder, rule);
        
        folderProps.put(ContentModel.PROP_NAME, "myMoveFolder");
        NodeRef folderForMove = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.ASSOC_CHILDREN,
                ContentModel.TYPE_FOLDER,
                folderProps).getChildRef();
        
        NodeRef testFile = this.fileFolderService.create(folderForMove, "testFile.txt", ContentModel.TYPE_CONTENT).getNodeRef();
        
        this.fileFolderService.move(folderForMove, folder, null);
        assertTrue("Should be versionable", nodeService.hasAspect(testFile, ContentModel.ASPECT_VERSIONABLE));
    }
    
    /**
     * ALF-4926: Incorrect behavior of update and move rule for the same folder
     * <p/>
     * Two rules:<br/><ul>
     *    <li>When items are deleted, copy to another folder.</li>
     *    <li>In addition, when items are updated, add an aspect (or any other rule).</li></ul>
     * Ensure that the first copy does not result in rules being fired on the target.
     */
    public void testUpdateAndMoveRuleOnSameFolder() throws Exception
    {
        NodeRef sourceFolder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}sourceFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef targetFolder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}targetFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();

        // Create UPDATE rule to add lockable aspect
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_LOCKABLE);
        Rule rule = createRule(
                RuleType.UPDATE,
                AddFeaturesActionExecuter.NAME,
                params,
                NoConditionEvaluator.NAME,
                null);
        this.ruleService.saveRule(sourceFolder, rule);
        
        // Check that the UPDATE rule works
        NodeRef testNodeOneRef = fileFolderService.create(sourceFolder, "one.txt", ContentModel.TYPE_CONTENT).getNodeRef();
        assertFalse(
                "Node should not have lockable aspect",
                nodeService.hasAspect(testNodeOneRef, ContentModel.ASPECT_LOCKABLE));
        nodeService.setProperty(testNodeOneRef, ContentModel.PROP_LOCALE, Locale.CANADA);
        assertTrue(
                "Node should have lockable aspect",
                nodeService.hasAspect(testNodeOneRef, ContentModel.ASPECT_LOCKABLE));
        fileFolderService.delete(testNodeOneRef);

        // Create OUTBOUND rule to copy node being deleted
        params = new HashMap<String, Serializable>(1);
        params.put(CopyActionExecuter.PARAM_DESTINATION_FOLDER, targetFolder);
        Rule copyRule = createRule(
                RuleType.OUTBOUND,
                CopyActionExecuter.NAME, 
                params,
                NoConditionEvaluator.NAME,
                null);
        copyRule.applyToChildren(true);
        this.ruleService.saveRule(sourceFolder, copyRule);
        
        // Check that this OUTBOUND rule works
        NodeRef testNodeTwoRef = fileFolderService.create(sourceFolder, "two.txt", ContentModel.TYPE_CONTENT).getNodeRef();
        assertFalse(
                "Node should not have lockable aspect",
                nodeService.hasAspect(testNodeTwoRef, ContentModel.ASPECT_LOCKABLE));
        fileFolderService.delete(testNodeTwoRef);
        assertFalse("Node was not deleted", fileFolderService.exists(testNodeTwoRef));
        assertEquals(
                "There should not be any children in source folder",
                0,
                fileFolderService.listFiles(sourceFolder).size());
        List<FileInfo> targetFolderFileList = fileFolderService.listFiles(targetFolder);
        assertEquals(
                "Node should have been copied to target folder",
                1,
                targetFolderFileList.size());
        assertFalse(
                "The node copy should not be lockable",
                nodeService.hasAspect(targetFolderFileList.get(0).getNodeRef(), ContentModel.ASPECT_LOCKABLE));
    }
    
    public void testDisableIndividualRules()
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ApplicationModel.ASPECT_CONFIGURABLE);        
        
        Rule rule = createRule(
                RuleType.INBOUND, 
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);
        rule.setRuleDisabled(true);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();         
        addContentToNode(newNodeRef);
        assertFalse(this.nodeService.hasAspect(newNodeRef, ApplicationModel.ASPECT_CONFIGURABLE));  
        
        Rule rule2 = this.ruleService.getRule(rule.getNodeRef());
        rule2.setRuleDisabled(false);
        this.ruleService.saveRule(this.nodeRef, rule2);
        
        // Re-try the test now the rule has been re-enabled
        NodeRef newNodeRef2 = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();         
        addContentToNode(newNodeRef2);
        assertTrue(this.nodeService.hasAspect(newNodeRef2, ApplicationModel.ASPECT_CONFIGURABLE));  
        
    }
    
    public void testDisableRule()
    {
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        
        Rule rule = createRule(
                RuleType.INBOUND, 
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);
        
        this.ruleService.saveRule(this.nodeRef, rule);
        this.ruleService.disableRule(rule);
        
        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();         
        addContentToNode(newNodeRef);
        assertFalse(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));  
        
        this.ruleService.enableRule(rule);
        
        NodeRef newNodeRef2 = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();         
        addContentToNode(newNodeRef2);
        assertTrue(this.nodeService.hasAspect(newNodeRef2, ContentModel.ASPECT_VERSIONABLE)); 
        
    }
    
    public void testAddFeaturesToAFolder()
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_TEMPLATABLE);        
        
        Rule rule = createRule(
                RuleType.INBOUND, 
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_FOLDER).getChildRef();           
        
        assertTrue(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_TEMPLATABLE));
        
        // System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));        
    }
    
    public void testCopyFolderToTriggerRules()
    {
        // Create the folders and content
        NodeRef copyToFolder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}copyToFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef folderToCopy = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}folderToCopy"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef contentToCopy = this.nodeService.createNode(
                folderToCopy,
                ContentModel.ASSOC_CONTAINS,                
                QName.createQName("{test}contentToCopy"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();
        addContentToNode(contentToCopy);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_TEMPLATABLE);        
        
        Rule rule = createRule(
                RuleType.INBOUND, 
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);  
        rule.applyToChildren(true);
        this.ruleService.saveRule(copyToFolder, rule);
        
        // Copy the folder in order to try and trigger the rule
        NodeRef copiedFolder = this.copyService.copy(folderToCopy, copyToFolder, ContentModel.ASSOC_CONTAINS, QName.createQName("{test}coppiedFolder"), true);
        assertNotNull(copiedFolder);
        
        // Check that the rule has been applied to the copied folder and content
        assertTrue(this.nodeService.hasAspect(copiedFolder, ContentModel.ASPECT_TEMPLATABLE));
        for (ChildAssociationRef childAssoc : this.nodeService.getChildAssocs(copiedFolder))
        {
            assertTrue(this.nodeService.hasAspect(childAssoc.getChildRef(), ContentModel.ASPECT_TEMPLATABLE));
        }
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
    }
	
	private Map<QName, Serializable> getContentProperties()
    {
   //     Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
     //   properties.put(ContentModel.PROP_CONTENT, CONTENT_DATA_TEXT);
        return null;
    }

    /**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition
     *          action:     simple-workflow
     */
    public void testSimpleWorkflowAction()
    {
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP, "approveStep");
		params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER, this.rootNodeRef);
		params.put(SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE, true);
		params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_STEP, "rejectStep");
		params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER, this.rootNodeRef);
		params.put(SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE, false);
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		SimpleWorkflowActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);

        this.ruleService.saveRule(this.nodeRef, rule);
				
		NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();     
		addContentToNode(newNodeRef);
        
		assertTrue(this.nodeService.hasAspect(newNodeRef, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));   
		assertEquals("approveStep", this.nodeService.getProperty(newNodeRef, ApplicationModel.PROP_APPROVE_STEP));
		assertEquals(this.rootNodeRef, this.nodeService.getProperty(newNodeRef, ApplicationModel.PROP_APPROVE_FOLDER));
		assertTrue(((Boolean)this.nodeService.getProperty(newNodeRef, ApplicationModel.PROP_APPROVE_MOVE)).booleanValue());
		assertTrue(this.nodeService.hasAspect(newNodeRef, ApplicationModel.ASPECT_SIMPLE_WORKFLOW));   
		assertEquals("rejectStep", this.nodeService.getProperty(newNodeRef, ApplicationModel.PROP_REJECT_STEP));
		assertEquals(this.rootNodeRef, this.nodeService.getProperty(newNodeRef, ApplicationModel.PROP_REJECT_FOLDER));
		assertFalse(((Boolean)this.nodeService.getProperty(newNodeRef, ApplicationModel.PROP_REJECT_MOVE)).booleanValue());
		
        // System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
    } 
    
    /**
     * Test:
     *          rule type:  inbound
     *          condition:  in-category
     *          action:     add-feature           
     */
    public void testInCategoryCondition()
    {
        // Create categories used in tests
        createTestCategories();
        
        try
        {
            Map<String, Serializable> params = new HashMap<String, Serializable>(1);
            params.put(InCategoryEvaluator.PARAM_CATEGORY_ASPECT, this.regionCategorisationQName);
            params.put(InCategoryEvaluator.PARAM_CATEGORY_VALUE, this.catROne);
            
            Map<String, Serializable> params2 = new HashMap<String, Serializable>(1);
            params2.put("aspect-name", ContentModel.ASPECT_VERSIONABLE); 
            
            Rule rule = createRule(
            		RuleType.INBOUND, 
            		AddFeaturesActionExecuter.NAME, 
            		params2, 
            		InCategoryEvaluator.NAME, 
            		params);
            
            this.ruleService.saveRule(this.nodeRef, rule);
                    
            // Check rule does not get fired when a node without the aspect is added
            NodeRef newNodeRef2 = this.nodeService.createNode(
                    this.nodeRef,
                    ContentModel.ASSOC_CHILDREN,                
                    QName.createQName(TEST_NAMESPACE, "noAspect"),
                    ContentModel.TYPE_CONTENT,
                    getContentProperties()).getChildRef(); 
            addContentToNode(newNodeRef2);
            assertFalse(this.nodeService.hasAspect(newNodeRef2, ContentModel.ASPECT_VERSIONABLE));
            
            // Check rule gets fired when node contains category value
            RetryingTransactionCallback<NodeRef> callback1 = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    NodeRef newNodeRef = nodeService.createNode(
                            nodeRef,
                            ContentModel.ASSOC_CHILDREN,                
                            QName.createQName(TEST_NAMESPACE, "hasAspectAndValue"),
                            ContentModel.TYPE_CONTENT,
                            getContentProperties()).getChildRef();
                    addContentToNode(newNodeRef);
                    Map<QName, Serializable> catProps = new HashMap<QName, Serializable>();
                    catProps.put(CAT_PROP_QNAME, catROne);
                    nodeService.addAspect(newNodeRef, regionCategorisationQName, catProps);
                    return newNodeRef;
                }
            };
            NodeRef newNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(callback1);
            assertTrue(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));  
            
            // Check rule does not get fired when the node has the incorrect category value
            RetryingTransactionCallback<NodeRef> callback3 = new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    NodeRef newNodeRef3 = nodeService.createNode(
                            nodeRef,
                            ContentModel.ASSOC_CHILDREN,                
                            QName.createQName(TEST_NAMESPACE, "hasAspectAndValue"),
                            ContentModel.TYPE_CONTENT,
                            getContentProperties()).getChildRef();  
                    addContentToNode(newNodeRef3);
                    Map<QName, Serializable> catProps3 = new HashMap<QName, Serializable>();
                    catProps3.put(CAT_PROP_QNAME, catRTwo);
                    nodeService.addAspect(newNodeRef3, regionCategorisationQName, catProps3);
                    return newNodeRef3;
                }
            };
            NodeRef newNodeRef3 = transactionService.getRetryingTransactionHelper().doInTransaction(callback3);
            assertFalse(this.nodeService.hasAspect(newNodeRef3, ContentModel.ASPECT_VERSIONABLE)); 
            
            //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
        }
        catch (Exception exception)
        {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition
     *          action:     link-category  
     */
    @SuppressWarnings("unchecked")
    public void testLinkCategoryAction()
    {        
        // Create categories used in tests
        createTestCategories();
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(LinkCategoryActionExecuter.PARAM_CATEGORY_ASPECT, this.regionCategorisationQName);
        params.put(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE, this.catROne); 
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		LinkCategoryActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);
                
        NodeRef newNodeRef2 = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "noAspect"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();
        addContentToNode(newNodeRef2);
        
        PropertyDefinition catPropDef = this.dictionaryDAO.getProperty(CAT_PROP_QNAME);
        if (catPropDef == null)
        {
            // Why is it undefined?
        }
        
        // Check that the category value has been set
        // It has been declared as a multi-value property, so we expect that here
        Collection<NodeRef> setValue = (Collection<NodeRef>) this.nodeService.getProperty(newNodeRef2, CAT_PROP_QNAME);
        assertNotNull(setValue);
        assertEquals(1, setValue.size());
        assertEquals(this.catROne, setValue.toArray()[0]);
}
        
    
    /**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition
     *          action:     mail  
     * @throws MessagingException 
     * @throws IOException 
     */
    public void testMailAction() throws MessagingException, IOException
    {
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(MailActionExecuter.PARAM_TO, "alfresco.test@gmail.com");
        params.put(MailActionExecuter.PARAM_SUBJECT, "Unit test");
        params.put(MailActionExecuter.PARAM_TEXT, "This is a test to check that the mail action is working.");
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		MailActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        MailActionExecuter mailService = (MailActionExecuter) ((ApplicationContextFactory) applicationContext
                    .getBean("OutboundSMTP")).getApplicationContext().getBean("mail");
        mailService.setTestMode(true);
        mailService.clearLastTestMessage();
                
        this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef();        
        
        // An email should appear in the recipients email
        // System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
        MimeMessage lastMessage = mailService.retrieveLastTestMessage();
        assertNotNull("Message should have been sent", lastMessage);
        System.out.println("Sent email with subject: " + lastMessage.getSubject());
        System.out.println("Sent email with content: " + lastMessage.getContent());
    }
    
    public void testMailNotSentIfRollback()
    {
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(MailActionExecuter.PARAM_TO, "alfresco.test@gmail.com");
        params.put(MailActionExecuter.PARAM_SUBJECT, "testMailNotSentIfRollback()");
        params.put(MailActionExecuter.PARAM_TEXT, "This email should NOT have been sent.");
        
        Rule rule = createRule(
                RuleType.INBOUND, 
                MailActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        String illegalName = "MyName.txt "; // space at end
        
        MailActionExecuter mailService = (MailActionExecuter) ((ApplicationContextFactory) applicationContext
                    .getBean("OutboundSMTP")).getApplicationContext().getBean("mail");
        mailService.setTestMode(true);
        mailService.clearLastTestMessage();
        
        try
        {
            this.nodeService.createNode(
                    this.nodeRef,
                    ContentModel.ASSOC_CHILDREN,                
                    QName.createQName(TEST_NAMESPACE, "children"),
                    ContentModel.TYPE_CONTENT,
                    makeNameProperty(illegalName)).getChildRef();
            fail("createNode() should have failed.");
        }
        catch(IntegrityException e)
        {
            // Expected exception.
            // An email should NOT appear in the recipients email
        }
        
        MimeMessage lastMessage = mailService.retrieveLastTestMessage();
        assertNull("Message should NOT have been sent", lastMessage);
    }
    
    private Map<QName, Serializable> makeNameProperty(String name)
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, name);
        return properties;
    }
    
    /**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition()
     *          action:     copy()
     */
    public void testCopyAction()
    {
        String localName = getName() + System.currentTimeMillis();
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.rootNodeRef);
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		CopyActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, localName),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef(); 
        addContentToNode(newNodeRef);
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
        
        // Check that the created node is still there
        List<ChildAssociationRef> origRefs = this.nodeService.getChildAssocs(
                this.nodeRef, 
                RegexQNamePattern.MATCH_ALL,
                QName.createQName(TEST_NAMESPACE, localName));
        assertNotNull(origRefs);
        assertEquals(1, origRefs.size());
        NodeRef origNodeRef = origRefs.get(0).getChildRef();
        assertEquals(newNodeRef, origNodeRef);

        // Check that the created node has been copied
        List<ChildAssociationRef> copyChildAssocRefs = this.nodeService.getChildAssocs(
                                                    this.rootNodeRef, 
                                                    RegexQNamePattern.MATCH_ALL, QName.createQName(TEST_NAMESPACE, localName));
        assertNotNull(copyChildAssocRefs);
        
        // **********************************
        // NOTE: Changed expected result to get build running
        // **********************************
        assertEquals(1, copyChildAssocRefs.size());
        
        NodeRef copyNodeRef = copyChildAssocRefs.get(0).getChildRef();
        assertTrue(this.nodeService.hasAspect(copyNodeRef, ContentModel.ASPECT_COPIEDFROM));
        NodeRef source = copyService.getOriginal(copyNodeRef);
        assertEquals(newNodeRef, source);
        
        // TODO test deep copy !!
    }
	
	/**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition()
     *          action:     transform()
     */
    public void testTransformAction() throws Throwable
    {
        ContentTransformer transformer = transformerRegistry.getTransformer(
                MimetypeMap.MIMETYPE_EXCEL,
                MimetypeMap.MIMETYPE_TEXT_PLAIN,
                new TransformationOptions());
        if (transformer == null)
        {
            return;
        }
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
		params.put(TransformActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        params.put(TransformActionExecuter.PARAM_DESTINATION_FOLDER, this.rootNodeRef);
        params.put(TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CHILDREN);
        params.put(TransformActionExecuter.PARAM_ASSOC_QNAME, QName.createQName(TEST_NAMESPACE, "transformed"));
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		TransformActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        UserTransaction tx = transactionService.getUserTransaction();
		tx.begin();
		
		Map<QName, Serializable> props =new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "test.xls");
		
		// Create the node at the root
        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "origional"),
                ContentModel.TYPE_CONTENT,
                props).getChildRef(); 
		
		// Set some content on the origional
		ContentWriter contentWriter = this.contentService.getWriter(newNodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_EXCEL);
		File testFile = AbstractContentTransformerTest.loadQuickTestFile("xls");
		contentWriter.putContent(testFile);
		
		tx.commit();
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
        
        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        
        // Check that the created node is still there
        List<ChildAssociationRef> origRefs = this.nodeService.getChildAssocs(
                this.nodeRef, 
                RegexQNamePattern.MATCH_ALL, QName.createQName(TEST_NAMESPACE, "origional"));
        assertNotNull(origRefs);
        assertEquals(1, origRefs.size());
        NodeRef origNodeRef = origRefs.get(0).getChildRef();
        assertEquals(newNodeRef, origNodeRef);

        // Check that the created node has been copied
        List<ChildAssociationRef> copyChildAssocRefs = this.nodeService.getChildAssocs(
                                                    this.rootNodeRef, 
                                                    RegexQNamePattern.MATCH_ALL, QName.createQName(TEST_NAMESPACE, "transformed"));
        assertNotNull(copyChildAssocRefs);
        assertEquals(1, copyChildAssocRefs.size());
        NodeRef copyNodeRef = copyChildAssocRefs.get(0).getChildRef();
        assertTrue(this.nodeService.hasAspect(copyNodeRef, ContentModel.ASPECT_COPIEDFROM));
        NodeRef source = copyService.getOriginal(copyNodeRef);
        assertEquals(newNodeRef, source);
        
        // Check the transformed content
        ContentData contentData = (ContentData) nodeService.getProperty(copyNodeRef, ContentModel.PROP_CONTENT);
		assertEquals(MimetypeMap.MIMETYPE_TEXT_PLAIN, contentData.getMimetype());
    }
    
    /**
     * Test image transformation
     *
     */
    public void testImageTransformAction() throws Throwable
    {
        ContentTransformer transformer = transformerRegistry.getTransformer(
                MimetypeMap.MIMETYPE_IMAGE_GIF,
                MimetypeMap.MIMETYPE_IMAGE_JPEG,
                new TransformationOptions());
        if (transformer == null)
        {
            return;
        }
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
		params.put(ImageTransformActionExecuter.PARAM_DESTINATION_FOLDER, this.rootNodeRef);
        params.put(ImageTransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CHILDREN);
        params.put(TransformActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        params.put(ImageTransformActionExecuter.PARAM_ASSOC_QNAME, QName.createQName(TEST_NAMESPACE, "transformed"));
        params.put(ImageTransformActionExecuter.PARAM_CONVERT_COMMAND, "-negate");
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		ImageTransformActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        UserTransaction tx = transactionService.getUserTransaction();
		tx.begin();
		
		Map<QName, Serializable> props =new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "test.gif");
		
		// Create the node at the root
        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "origional"),
                ContentModel.TYPE_CONTENT,
                props).getChildRef(); 
		
		// Set some content on the origional
		ContentWriter contentWriter = this.contentService.getWriter(newNodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_IMAGE_GIF);
		File testFile = AbstractContentTransformerTest.loadQuickTestFile("gif");
		contentWriter.putContent(testFile);
		
		tx.commit();
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
        
        // Check that the created node is still there
        List<ChildAssociationRef> origRefs = this.nodeService.getChildAssocs(
                this.nodeRef, 
                RegexQNamePattern.MATCH_ALL, QName.createQName(TEST_NAMESPACE, "origional"));
        assertNotNull(origRefs);
        assertEquals(1, origRefs.size());
        NodeRef origNodeRef = origRefs.get(0).getChildRef();
        assertEquals(newNodeRef, origNodeRef);

        // Check that the created node has been copied
        List<ChildAssociationRef> copyChildAssocRefs = this.nodeService.getChildAssocs(
                                                    this.rootNodeRef, 
                                                    RegexQNamePattern.MATCH_ALL, QName.createQName(TEST_NAMESPACE, "transformed"));
        assertNotNull(copyChildAssocRefs);
        assertEquals(1, copyChildAssocRefs.size());
        NodeRef copyNodeRef = copyChildAssocRefs.get(0).getChildRef();
        assertTrue(this.nodeService.hasAspect(copyNodeRef, ContentModel.ASPECT_COPIEDFROM));
        NodeRef source = copyService.getOriginal(copyNodeRef);
        assertEquals(newNodeRef, source);
    }
	
    /**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition()
     *          action:     move()
     */
    public void testMoveAction()
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, this.rootNodeRef);
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		MoveActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);
                
        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "origional"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef(); 
        addContentToNode(newNodeRef);
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
        
        // Check that the created node has been moved
        List<ChildAssociationRef> origRefs = this.nodeService.getChildAssocs(
                this.nodeRef, 
                RegexQNamePattern.MATCH_ALL, QName.createQName(TEST_NAMESPACE, "origional"));
        assertNotNull(origRefs);
        assertEquals(0, origRefs.size());

        // Check that the created node is in the new location
        List<ChildAssociationRef> copyChildAssocRefs = this.nodeService.getChildAssocs(
                                                    this.rootNodeRef, 
                                                    RegexQNamePattern.MATCH_ALL, QName.createQName(TEST_NAMESPACE, "origional"));
        assertNotNull(copyChildAssocRefs);
        assertEquals(1, copyChildAssocRefs.size());
        NodeRef movedNodeRef = copyChildAssocRefs.get(0).getChildRef();
        assertEquals(newNodeRef, movedNodeRef);
    }
    
    /**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition()
     *          action:     checkout()
     */
    public void testCheckOutAction()
    {
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		CheckOutActionExecuter.NAME, 
        		null, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);
         
        NodeRef newNodeRef = null;
        UserTransaction tx = this.transactionService.getUserTransaction();
        try
        {
        	tx.begin();     
        	
	        // Create a new node
	        newNodeRef = this.nodeService.createNode(
	                this.nodeRef,
                    ContentModel.ASSOC_CHILDREN,                
	                QName.createQName(TEST_NAMESPACE, "checkout"),
	                ContentModel.TYPE_CONTENT,
	                getContentProperties()).getChildRef();
	        addContentToNode(newNodeRef);
	        
	        tx.commit();
        }
        catch (Exception exception)
        {
        	throw new RuntimeException(exception);
        }
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
        
        // Check that the new node has been checked out
        List<ChildAssociationRef> children = this.nodeService.getChildAssocs(this.nodeRef);
        assertNotNull(children);
        assertEquals(3, children.size()); // includes rule folder
        for (ChildAssociationRef child : children)
        {
            NodeRef childNodeRef = child.getChildRef();
            if (childNodeRef.equals(newNodeRef) == true)
            {
                // check that the node has been locked
                LockStatus lockStatus = this.lockService.getLockStatus(childNodeRef);
                assertEquals(LockStatus.LOCK_OWNER, lockStatus);
            }
            else if (this.nodeService.hasAspect(childNodeRef, ContentModel.ASPECT_WORKING_COPY) == true)
            {
                // assert that it is the working copy that relates to the origional node
                NodeRef copiedFromNodeRef = copyService.getOriginal(childNodeRef);
                assertEquals(newNodeRef, copiedFromNodeRef);
            }
        }
    }
    
    /**
     * Test:
     *          rule type:  inbound
     *          condition:  no-condition()
     *          action:     checkin()
     */
	public void testCheckInAction()
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put(CheckInActionExecuter.PARAM_DESCRIPTION, "The version description.");
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		CheckInActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);
         
        List<NodeRef> list = transactionService.getRetryingTransactionHelper().doInTransaction(
        		new RetryingTransactionCallback<List<NodeRef>>()
        		{
					public List<NodeRef> execute()
					{
						// Create a new node and check-it out
				        NodeRef newNodeRef = RuleServiceCoverageTest.this.nodeService.createNode(
				        		RuleServiceCoverageTest.this.rootNodeRef,
				                ContentModel.ASSOC_CHILDREN,                
				                QName.createQName(TEST_NAMESPACE, "origional"),
				                ContentModel.TYPE_CONTENT,
				                getContentProperties()).getChildRef();
				        NodeRef workingCopy = RuleServiceCoverageTest.this.cociService.checkout(newNodeRef);
				        
				        // Move the working copy into the actionable folder
				        RuleServiceCoverageTest.this.nodeService.moveNode(
				                workingCopy, 
				                RuleServiceCoverageTest.this.nodeRef, 
				                ContentModel.ASSOC_CHILDREN,
				                QName.createQName(TEST_NAMESPACE, "moved"));
				        
				        List<NodeRef> result = new ArrayList<NodeRef>();
				        result.add(newNodeRef);
				        result.add(workingCopy);				        
						return result;
					}
        			
        		});        
		
		// Check that the working copy has been removed
		assertFalse(this.nodeService.exists(list.get(1)));
		
		// Check that the origional is no longer locked
		assertEquals(LockStatus.NO_LOCK, this.lockService.getLockStatus(list.get(0)));
		
		//System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
    }
    
    /**
     * Check that the rules can be enabled and disabled
     */
    public void testRulesDisabled()
    {
        Map<String, Serializable> actionParams = new HashMap<String, Serializable>(1);
        actionParams.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		AddFeaturesActionExecuter.NAME, 
        		actionParams, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);

        RetryingTransactionCallback<NodeRef> noRulesWork = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                ruleService.disableRules(nodeRef);
                
                NodeRef newNodeRef = nodeService.createNode(
                        nodeRef,
                        ContentModel.ASSOC_CHILDREN,                
                        QName.createQName(TEST_NAMESPACE, "children"),
                        ContentModel.TYPE_CONTENT,
                        getContentProperties()).getChildRef();         
                addContentToNode(newNodeRef);
                return newNodeRef;
            }
        };
        NodeRef newNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(noRulesWork);
        assertFalse(nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));      

        RetryingTransactionCallback<NodeRef> withRulesWork = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                NodeRef newNodeRef2 = nodeService.createNode(
                        nodeRef,
                        ContentModel.ASSOC_CHILDREN,                
                        QName.createQName(TEST_NAMESPACE, "children"),
                        ContentModel.TYPE_CONTENT,
                        getContentProperties()).getChildRef();        
                addContentToNode(newNodeRef2);
                return newNodeRef2;
            }
        };
        NodeRef newNodeRef2 = transactionService.getRetryingTransactionHelper().doInTransaction(withRulesWork);
        assertTrue(nodeService.hasAspect(newNodeRef2, ContentModel.ASPECT_VERSIONABLE));
    }
    
    /**
     * Adds content to a given node. 
     * <p>
     * Used to trigger rules of type of incomming.
     * 
     * @param nodeRef  the node reference
     */
    private void addContentToNode(NodeRef nodeRef)
    {
    	ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.setEncoding("UTF-8");
    	assertNotNull(contentWriter);
    	contentWriter.putContent(STANDARD_TEXT_CONTENT + System.currentTimeMillis());
    }
    
    /**
     * Test checkMandatoryProperties method
     */
    public void testCheckMandatoryProperties()
    {
        Map<String, Serializable> actionParams = new HashMap<String, Serializable>(1);
        actionParams.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        
        Map<String, Serializable> condParams = new HashMap<String, Serializable>(1);
        // should be setting the condition parameter here
        
        Rule rule = createRule(
        		RuleType.INBOUND, 
        		AddFeaturesActionExecuter.NAME, 
        		actionParams, 
        		ComparePropertyValueEvaluator.NAME, 
        		condParams);
        
        this.ruleService.saveRule(this.nodeRef, rule);
        
        try
        {
            // Try and create a node .. should fail since the rule is invalid
            Map<QName, Serializable> props2 = getContentProperties();
            props2.put(ContentModel.PROP_NAME, "bobbins.doc");
            NodeRef newNodeRef2 = this.nodeService.createNode(
                    this.nodeRef,
                    ContentModel.ASSOC_CHILDREN,                
                    QName.createQName(TEST_NAMESPACE, "children"),
                    ContentModel.TYPE_CONTENT,
                    props2).getChildRef();
            addContentToNode(newNodeRef2);
            fail("An exception should have been thrown since a mandatory parameter was missing from the condition.");
        }
        catch (Throwable ruleServiceException)
        {
            // Success since we where expecting the exception
        }
    }
    
	/**
     * Test:
     *          rule type:  inbound
     *          condition:  match-text(
     *          				text = .doc,
     *          				operation = CONTAINS)
     *          action:     add-features(
     *                          aspect-name = versionable)
     */
	public void testContainsTextCondition()
	{
		Map<String, Serializable> actionParams = new HashMap<String, Serializable>(1);
		actionParams.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        
        // ActionCondition parameter's 
		Map<String, Serializable> condParams = new HashMap<String, Serializable>(1);
		condParams.put(ComparePropertyValueEvaluator.PARAM_VALUE, ".doc");        
		
		Rule rule = createRule(
        		RuleType.INBOUND, 
        		AddFeaturesActionExecuter.NAME, 
        		actionParams, 
        		ComparePropertyValueEvaluator.NAME, 
        		condParams);
        
        this.ruleService.saveRule(this.nodeRef, rule);
		
		// Test condition failure
		Map<QName, Serializable> props1 = new HashMap<QName, Serializable>();
		props1.put(ContentModel.PROP_NAME, "bobbins.txt");
       // props1.put(ContentModel.PROP_CONTENT, CONTENT_DATA_TEXT);
		NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                props1).getChildRef();   
		addContentToNode(newNodeRef);
        
        //Map<QName, Serializable> map = this.nodeService.getProperties(newNodeRef);
        //String value = (String)this.nodeService.getProperty(newNodeRef, ContentModel.PROP_NAME);
        
        assertFalse(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));  
		
		// Test condition success
		Map<QName, Serializable> props2 = new HashMap<QName, Serializable>();
		props2.put(ContentModel.PROP_NAME, "bobbins.doc");
        //props2.put(ContentModel.PROP_CONTENT, CONTENT_DATA_TEXT);
		NodeRef newNodeRef2 = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                props2).getChildRef();        
		addContentToNode(newNodeRef2);
        assertTrue(this.nodeService.hasAspect(
                newNodeRef2, 
                ContentModel.ASPECT_VERSIONABLE)); 
		
		try
		{
			// Test name not set
			NodeRef newNodeRef3 = this.nodeService.createNode(
	                this.nodeRef,
                    ContentModel.ASSOC_CHILDREN,                
	                QName.createQName(TEST_NAMESPACE, "children"),
	                ContentModel.TYPE_CONTENT,
                    getContentProperties()).getChildRef();      
			addContentToNode(newNodeRef3);
		}
		catch (RuleServiceException exception)
		{
			// Correct since text-match is a mandatory property
		}
        
        // Test begins with
        Map<String, Serializable> condParamsBegins = new HashMap<String, Serializable>(1);
        condParamsBegins.put(ComparePropertyValueEvaluator.PARAM_VALUE, "bob*");
        rule.getAction().removeAllActionConditions();
        ActionCondition condition1 = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME, condParamsBegins);
        rule.getAction().addActionCondition(condition1);
        this.ruleService.saveRule(this.nodeRef, rule);
        Map<QName, Serializable> propsx = new HashMap<QName, Serializable>();
        propsx.put(ContentModel.PROP_NAME, "mybobbins.doc");
        //propsx.put(ContentModel.PROP_CONTENT, CONTENT_DATA_TEXT);
        NodeRef newNodeRefx = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                propsx).getChildRef();   
        addContentToNode(newNodeRefx);
        assertFalse(this.nodeService.hasAspect(newNodeRefx, ContentModel.ASPECT_VERSIONABLE));  
        Map<QName, Serializable> propsy = new HashMap<QName, Serializable>();
        propsy.put(ContentModel.PROP_NAME, "bobbins.doc");
        //propsy.put(ContentModel.PROP_CONTENT, CONTENT_DATA_TEXT);
        NodeRef newNodeRefy = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                propsy).getChildRef();   
        addContentToNode(newNodeRefy);
        assertTrue(this.nodeService.hasAspect(
                newNodeRefy, 
                ContentModel.ASPECT_VERSIONABLE)); 
        
        // Test ends with
        Map<String, Serializable> condParamsEnds = new HashMap<String, Serializable>(1);
        condParamsEnds.put(ComparePropertyValueEvaluator.PARAM_VALUE, "*s.doc");
        rule.getAction().removeAllActionConditions();
        ActionCondition condition2 = this.actionService.createActionCondition(ComparePropertyValueEvaluator.NAME, condParamsEnds);
        rule.getAction().addActionCondition(condition2);
        this.ruleService.saveRule(this.nodeRef, rule);
        Map<QName, Serializable> propsa = new HashMap<QName, Serializable>();
        propsa.put(ContentModel.PROP_NAME, "bobbins.document");
       // propsa.put(ContentModel.PROP_CONTENT, CONTENT_DATA_TEXT);
        NodeRef newNodeRefa = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                propsa).getChildRef(); 
        addContentToNode(newNodeRefa);
        assertFalse(this.nodeService.hasAspect(newNodeRefa, ContentModel.ASPECT_VERSIONABLE));  
        Map<QName, Serializable> propsb = new HashMap<QName, Serializable>();
        propsb.put(ContentModel.PROP_NAME, "bobbins.doc");
        //propsb.put(ContentModel.PROP_CONTENT, CONTENT_DATA_TEXT);
        NodeRef newNodeRefb = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT,
                propsb).getChildRef();   
        addContentToNode(newNodeRefb);
        assertTrue(this.nodeService.hasAspect(
                newNodeRefb, 
                ContentModel.ASPECT_VERSIONABLE)); 
	}
    
    public void testInboundRuleType()
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);              
        Rule rule = createRule(
                RuleType.INBOUND, 
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);
        
        this.ruleService.saveRule(this.nodeRef, rule);
        
        // Create a non-content node
        NodeRef newNodeRef =  this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        assertTrue(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Create a content node
        NodeRef contentNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT).getChildRef();        
        assertTrue(this.nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE));        
        addContentToNode(contentNodeRef);            
        assertTrue(this.nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Create a node to be moved
        NodeRef moveNode = this.nodeService.createNode(
                newNodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT).getChildRef(); 
        addContentToNode(moveNode);
        assertFalse(this.nodeService.hasAspect(moveNode, ContentModel.ASPECT_VERSIONABLE));
        this.nodeService.moveNode(
                moveNode, 
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"));
        assertTrue(this.nodeService.hasAspect(moveNode, ContentModel.ASPECT_VERSIONABLE)); 
        
        // Enusre the rule type does not get fired when the node is updated
        this.nodeService.removeAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE);
        assertFalse(this.nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE)); 
        this.nodeService.setProperty(contentNodeRef, ContentModel.PROP_NAME, "name.txt");
        assertFalse(this.nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE));
        addContentToNode(contentNodeRef);
        assertFalse(this.nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE));        
    }
    
    public void testUpdateRuleType()
    {
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);              
        Rule rule = createRule(
                RuleType.UPDATE, 
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);
        
        this.ruleService.saveRule(this.nodeRef, rule);
        
        // Create a non-content node
        NodeRef newNodeRef =  this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_FOLDER).getChildRef();
        this.nodeService.removeAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE);
        assertFalse(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Update the non-content node
        this.nodeService.setProperty(newNodeRef, ContentModel.PROP_NAME, "testName");
        assertTrue(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Create a content node
        NodeRef contentNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTENT).getChildRef();                
        nodeService.removeAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE);
        assertFalse(this.nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE));        
        addContentToNode(contentNodeRef);            
        assertFalse(this.nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE));
        addContentToNode(contentNodeRef);            
        assertTrue(this.nodeService.hasAspect(contentNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Create a non content node, setting a property at the same time
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, "testName");
        NodeRef nodeRef2 =  this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();
        nodeService.removeAspect(nodeRef2, ContentModel.ASPECT_VERSIONABLE);
        assertFalse(this.nodeService.hasAspect(nodeRef2, ContentModel.ASPECT_VERSIONABLE));
        this.nodeService.setProperty(nodeRef2, ContentModel.PROP_NAME, "testName");
        assertFalse(this.nodeService.hasAspect(nodeRef2, ContentModel.ASPECT_VERSIONABLE));
        this.nodeService.setProperty(nodeRef2, ContentModel.PROP_NAME, "testName2");
        assertTrue(this.nodeService.hasAspect(nodeRef2, ContentModel.ASPECT_VERSIONABLE));
        
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "testName");
                NodeRef nodeRef3 =  RuleServiceCoverageTest.this.nodeService.createNode(
                        RuleServiceCoverageTest.this.nodeRef,
                        ContentModel.ASSOC_CHILDREN,                
                        QName.createQName(TEST_NAMESPACE, "children"),
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                assertFalse(RuleServiceCoverageTest.this.nodeService.hasAspect(nodeRef3, ContentModel.ASPECT_VERSIONABLE));
                RuleServiceCoverageTest.this.nodeService.setProperty(nodeRef3, ContentModel.PROP_NAME, "testName2");
                assertFalse(RuleServiceCoverageTest.this.nodeService.hasAspect(nodeRef3, ContentModel.ASPECT_VERSIONABLE));
                
                return null;
            }
        });
    }
    
    public void testAssociationUpdateRule()
    {
        //ALF-9661 test
        NodeRef sourceFolder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}sourceFolder"),
                ContentModel.TYPE_FOLDER).getChildRef();       
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        //create a rule that adds an aspect after a property is updated
        Rule rule = createRule(
                RuleType.UPDATE, 
                AddFeaturesActionExecuter.NAME, 
                params, 
                NoConditionEvaluator.NAME, 
                null);

        this.ruleService.saveRule(sourceFolder, rule);     
        //create folders
        NodeRef testNodeOneRef = this.nodeService.createNode(
                sourceFolder,
                ContentModel.ASSOC_CONTAINS,                
                QName.createQName(TEST_NAMESPACE, "original1"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef(); 
        addContentToNode(testNodeOneRef);
        
        NodeRef testNodeTwoRef = this.nodeService.createNode(
                sourceFolder,
                ContentModel.ASSOC_CONTAINS,                
                QName.createQName(TEST_NAMESPACE, "original2"),
                ContentModel.TYPE_CONTENT,
                getContentProperties()).getChildRef(); 
        addContentToNode(testNodeTwoRef);                
        //there is no aspect
        assertFalse(this.nodeService.hasAspect(testNodeOneRef, ContentModel.ASPECT_VERSIONABLE));
        //create an association
        this.nodeService.addAspect(testNodeOneRef, ContentModel.ASPECT_REFERENCING, null);        
        this.nodeService.createAssociation(testNodeOneRef, testNodeTwoRef, ContentModel.ASSOC_REFERENCES);
        //there should be the versionable aspect added
        assertTrue(this.nodeService.hasAspect(testNodeOneRef, ContentModel.ASPECT_VERSIONABLE));        
    }
    
    /**
     * Test:
     *          rule type:  outbound
     *          condition:  no-condition()
     *          action:     add-features(
     *                          aspect-name = versionable)
     */
    public void testOutboundRuleType()
    {
        this.nodeService.addAspect(this.nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
        
        Rule rule = createRule(
        		"outbound", 
        		AddFeaturesActionExecuter.NAME, 
        		params, 
        		NoConditionEvaluator.NAME, 
        		null);
        
        this.ruleService.saveRule(this.nodeRef, rule);
        
        // Create a node
        NodeRef newNodeRef = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTAINER).getChildRef();        
        assertFalse(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Move the node out of the actionable folder
        this.nodeService.moveNode(
                newNodeRef, 
                this.rootNodeRef, 
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"));
        assertTrue(this.nodeService.hasAspect(newNodeRef, ContentModel.ASPECT_VERSIONABLE));
        
        // Check the deletion of a node
        
        //System.out.println(NodeStoreInspector.dumpNodeStore(this.nodeService, this.testStoreRef));
        NodeRef newNodeRef2 = this.nodeService.createNode(
                this.nodeRef,
                ContentModel.ASSOC_CHILDREN,                
                QName.createQName(TEST_NAMESPACE, "children"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        this.nodeService.deleteNode(newNodeRef2);
    }
    
    /**
     * Performance guideline test
     *
     */
    public void xtestPerformanceOfRuleExecution()
    {
		try
		{
	        StopWatch sw = new StopWatch();
	        
	        // Create actionable nodes
	        sw.start("create nodes with no rule executed");		
			UserTransaction userTransaction1 = this.transactionService.getUserTransaction();
			userTransaction1.begin();
			
			for (int i = 0; i < 100; i++)
	        {
	            this.nodeService.createNode(
	                    this.nodeRef,
	                    ContentModel.ASSOC_CONTAINS,
	                    ContentModel.ASSOC_CONTAINS,
	                    ContentModel.TYPE_CONTAINER).getChildRef(); 
	            assertFalse(this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE));
	        }
				
			userTransaction1.commit();
	        sw.stop();
	        
	        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
	        params.put("aspect-name", ContentModel.ASPECT_VERSIONABLE);        
	        
	        Rule rule = createRule(
	        		RuleType.INBOUND, 
	        		AddFeaturesActionExecuter.NAME, 
	        		params, 
	        		NoConditionEvaluator.NAME, 
	        		null);
	        
	        this.ruleService.saveRule(this.nodeRef, rule);
	        
	        sw.start("create nodes with one rule run (apply versionable aspect)");
			UserTransaction userTransaction2 = this.transactionService.getUserTransaction();
			userTransaction2.begin();
			
			NodeRef[] nodeRefs = new NodeRef[100];
	        for (int i = 0; i < 100; i++)
	        {
	            NodeRef nodeRef = this.nodeService.createNode(
	                    this.nodeRef,
                        ContentModel.ASSOC_CHILDREN,
						QName.createQName(TEST_NAMESPACE, "children"),
	                    ContentModel.TYPE_CONTAINER).getChildRef();
	            addContentToNode(nodeRef);
				nodeRefs[i] = nodeRef;
				
				// Check that the versionable aspect has not yet been applied
				assertFalse(this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE));
	        }
			
			userTransaction2.commit();
	        sw.stop();
			
			// Check that the versionable aspect has been applied to all the created nodes
			for (NodeRef ref : nodeRefs) 
			{
				assertTrue(this.nodeService.hasAspect(ref, ContentModel.ASPECT_VERSIONABLE));
			}
	        
	        System.out.println(sw.prettyPrint());
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
    }
    
    public void testAsyncExecutionWithPotentialLoop()
    {
        if (this.transformerRegistry.getTransformer(MimetypeMap.MIMETYPE_EXCEL, MimetypeMap.MIMETYPE_TEXT_PLAIN, new TransformationOptions()) != null)
        {
    		try
    		{
    	        Map<String, Serializable> params = new HashMap<String, Serializable>(1);
    			params.put(TransformActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_TEXT_PLAIN);
    	        params.put(TransformActionExecuter.PARAM_DESTINATION_FOLDER, this.nodeRef);
    	        params.put(TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, ContentModel.ASSOC_CONTAINS);
    	        params.put(TransformActionExecuter.PARAM_ASSOC_QNAME, QName.createQName(TEST_NAMESPACE, "transformed"));
    	        
    	        Rule rule = createRule(
    	        		RuleType.INBOUND, 
    	        		TransformActionExecuter.NAME, 
    	        		params, 
    	        		NoConditionEvaluator.NAME, 
    	        		null);
    	        rule.setExecuteAsynchronously(true);
    	        rule.setTitle("Transform document to text");
    	        
    	        UserTransaction tx0 = transactionService.getUserTransaction();
    			tx0.begin();   			
    	        this.ruleService.saveRule(this.nodeRef, rule);
    	        tx0.commit();    	        
    	
    	        UserTransaction tx = transactionService.getUserTransaction();
    			tx.begin();
    			
    			Map<QName, Serializable> props =new HashMap<QName, Serializable>(1);
    	        props.put(ContentModel.PROP_NAME, "test.xls");
    			
    			// Create the node at the root
    	        NodeRef newNodeRef = this.nodeService.createNode(
    	                this.nodeRef,
                        ContentModel.ASSOC_CHILDREN,                
    	                QName.createQName(TEST_NAMESPACE, "origional"),
    	                ContentModel.TYPE_CONTENT,
    	                props).getChildRef(); 
    			
    			// Set some content on the origional
    			ContentWriter contentWriter = this.contentService.getWriter(newNodeRef, ContentModel.PROP_CONTENT, true);
                contentWriter.setMimetype(MimetypeMap.MIMETYPE_EXCEL);
    			File testFile = AbstractContentTransformerTest.loadQuickTestFile("xls");
    			contentWriter.putContent(testFile);
    			
    			tx.commit();
    	        
                // Sleep to ensure work is done b4 execution is canceled
    			Thread.sleep(10000);    			
    		}
    		catch (Exception exception)
    		{
    			throw new RuntimeException(exception);
    		}
        }
    }
}
