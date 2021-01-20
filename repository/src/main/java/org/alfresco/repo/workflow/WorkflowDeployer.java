/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.workflow;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Alfresco bootstrap Process deployment.
 * 
 * @author davidc
 */
public class WorkflowDeployer extends AbstractLifecycleBean
{
    // Logging support
    private static Log logger = LogFactory.getLog("org.alfresco.repo.workflow");

    // Workflow Definition Properties (used in setWorkflowDefinitions)
    public static final String ENGINE_ID = "engineId";
    public static final String LOCATION = "location";
    public static final String MIMETYPE = "mimetype";
    public static final String REDEPLOY = "redeploy";
    
    public static final String CATEGORY_ALFRESCO_INTERNAL = "http://alfresco.org/workflows/internal";
    
    // Dependencies
    private TransactionService transactionService;
    private WorkflowService workflowService;
    private WorkflowAdminService workflowAdminService;
    private AuthenticationContext authenticationContext;
    private DictionaryDAO dictionaryDAO;
    private List<Properties> workflowDefinitions;
    private List<String> models = new ArrayList<String>();
    private List<String> resourceBundles = new ArrayList<String>();
    private TenantAdminService tenantAdminService;
    private TenantService tenantService;
    
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private RepositoryLocation repoWorkflowDefsLocation;

    private WorkflowDeployerTransactionListener workflowDeployerTransactionListener = new WorkflowDeployerTransactionListener();

    public final static String CRITERIA_ALL = "/*"; // immediate children only
    public final static String defaultSubtypeOfWorkflowDefinitionType = "subtypeOf('bpm:workflowDefinition')";
    
    /**
     * Sets the Transaction Service
     * 
     * @param transactionService the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Sets the workflow service
     * 
     * @param workflowService the workflow service
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    /**
     * Sets the workflow admin service
     * 
     * @param workflowAdminService the workflow admin service
     */
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService)
    {
        this.workflowAdminService = workflowAdminService;
    }

    /**
     * Set the authentication component
     * 
     * @param authenticationContext AuthenticationContext
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    /**
     * Sets the Dictionary DAO
     * 
     * @param dictionaryDAO DictionaryDAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }
    
    /**
     * Sets the tenant admin service
     * 
     * @param tenantAdminService the tenant admin service
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }

    /**
     * Sets the tenant service
     * 
     * @param tenantService the tenant service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Sets the Workflow Definitions
     * 
     */
    public void setWorkflowDefinitions(List<Properties> workflowDefinitions)
    {
        this.workflowDefinitions = workflowDefinitions;
    }

    /**
     * Sets the initial list of Workflow models to bootstrap with
     * 
     * @param modelResources the model names
     */
    public void setModels(List<String> modelResources)
    {
        this.models = modelResources;
    }
    
    /**
     * Sets the initial list of Workflow resource bundles to bootstrap with
     * 
     * @param labels the list of labels
     */
    public void setLabels(List<String> labels)
    {
        this.resourceBundles = labels;
    }
       
    // used by TenantAdminService when creating a new tenant and bootstrapping the pre-defined workflows
    public List<Properties> getWorkflowDefinitions()
    {
        return this.workflowDefinitions;
    }
    
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setRepositoryWorkflowDefsLocations(RepositoryLocation repoWorkflowDefsLocation)
    {
        this.repoWorkflowDefsLocation = repoWorkflowDefsLocation;
    }
    
        
    /**
     * Deploy the Workflow Definitions
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "authenticationContext", authenticationContext);
        PropertyCheck.mandatory(this, "workflowService", workflowService);

        String currentUser = authenticationContext.getCurrentUserName();
        if (currentUser == null)
        {
            authenticationContext.setSystemUserAsCurrentUser();
        }
        if ((!transactionService.getAllowWrite()) && (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE))
        {
            if (logger.isWarnEnabled())
                logger.warn("Repository is in read-only mode; not deploying workflows.");
            
            return;
        }
        
        UserTransaction userTransaction = transactionService.getUserTransaction();
        try
        {
            userTransaction.begin();
        
            // bootstrap the workflow models and static labels (from classpath)
            if (models != null && resourceBundles != null && ((models.size() > 0) || (resourceBundles.size() > 0)))
            {
                DictionaryBootstrap dictionaryBootstrap = new DictionaryBootstrap();
                dictionaryBootstrap.setDictionaryDAO(dictionaryDAO);
                dictionaryBootstrap.setTenantService(tenantService);
                dictionaryBootstrap.setModels(models);
                dictionaryBootstrap.setLabels(resourceBundles);
                dictionaryBootstrap.bootstrap(); // also registers with dictionary
                
                // MNT-10537 fix, since new model was deployed we need to destroy dictionary to force lazy re-init 
                AlfrescoTransactionSupport.bindListener(this.workflowDeployerTransactionListener);
            }
            
            // bootstrap the workflow definitions (from classpath)
            if (workflowDefinitions != null)
            {
                for (Properties workflowDefinition : workflowDefinitions)
                {
                    // retrieve workflow specification
                    String engineId = workflowDefinition.getProperty(ENGINE_ID);
                    if (engineId == null || engineId.length() == 0)
                    {
                        throw new WorkflowException("Workflow Engine Id must be provided");
                    }
                    
                    String location = workflowDefinition.getProperty(LOCATION);
                    if (location == null || location.length() == 0)
                    {
                        throw new WorkflowException("Workflow definition location must be provided");
                    }
                    
                    if (workflowAdminService.isEngineEnabled(engineId))
                    {
                        Boolean redeploy = Boolean.valueOf(workflowDefinition.getProperty(REDEPLOY));
                        String mimetype = workflowDefinition.getProperty(MIMETYPE);

                        // retrieve input stream on workflow definition
                        ClassPathResource workflowResource = new ClassPathResource(location);
                        
                        // deploy workflow definition
                        if (!redeploy && workflowService.isDefinitionDeployed(engineId, workflowResource.getInputStream(), mimetype))
                        {
                            if (logger.isDebugEnabled())
                                logger.debug("Workflow deployer: Definition '" + location + "' already deployed");
                        }
                        else
                        {
                            WorkflowDeployment deployment = workflowService.deployDefinition(engineId, workflowResource.getInputStream(), 
                                        mimetype, workflowResource.getFilename());
                            logDeployment(location, deployment);
                        }
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Workflow deployer: Definition '" + location + "' not deployed as the '" + engineId + "' engine is disabled");
                    }
                }
            }
            
            // deploy workflow definitions from repository (if any)
            if (repoWorkflowDefsLocation != null)
            {
                StoreRef storeRef = repoWorkflowDefsLocation.getStoreRef();
                NodeRef rootNode = nodeService.getRootNode(storeRef);
                List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, repoWorkflowDefsLocation.getPath()+CRITERIA_ALL+"["+defaultSubtypeOfWorkflowDefinitionType+"]", null, namespaceService, false);

                if (nodeRefs.size() > 0)
                {
                    for (NodeRef nodeRef : nodeRefs)
                    {
                        deploy(nodeRef, false);
                    }
                }
            }
                
            userTransaction.commit();
        }
        catch(Throwable e)
        {
            // rollback the transaction
            try
            { 
                if (userTransaction != null) 
                {
                    userTransaction.rollback();
                }
            }
            catch (Exception ex)
            {
                // NOOP 
            }
            throw new AlfrescoRuntimeException("Workflow deployment failed", e);
        }
        finally
        {
            if (currentUser == null)
            {
                authenticationContext.clearCurrentSecurityContext();
            }
        }
    }

    /**
     * Deploy a workflow definition from a node in the repository.
     */
    public void deploy(NodeRef nodeRef, boolean redeploy)
    {
        // Ignore if the node is a working copy 
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
        {
            if (!isValidLocation(nodeRef))
            {
                if (logger.isDebugEnabled())
                {
                    Path nodePath = nodeService.getPath(nodeRef);
                    logger.debug("Workflow deployer: Definition '" + nodeRef +
                            "' ("+nodePath+") not deployed as it is not in the correct location.");
                }
                return;
            }
            Boolean value = (Boolean)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED);
            if ((value != null) && (value.booleanValue() == true))
            {
                String engineId = (String) nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID);
                
                if (workflowAdminService.isEngineEnabled(engineId))
                {
                    if (!redeploy && workflowService.isDefinitionDeployed(nodeRef))
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Workflow deployer: Definition '" + nodeRef + "' already deployed");
                    }
                    else
                    {
                        // deploy / re-deploy
                        WorkflowDeployment deployment = workflowService.deployDefinition(nodeRef);
                        logDeployment(nodeRef, deployment);
                        if (deployment != null)
                        {
                            WorkflowDefinition def = deployment.getDefinition();
                            
                            // Update the meta data for the model
                            Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

                            props.put(WorkflowModel.PROP_WORKFLOW_DEF_NAME, def.getName());

                            // TODO - ability to return and handle deployment problems / warnings
                            if (deployment.getProblems().length > 0)
                            {
                                for (String problem : deployment.getProblems())
                                {
                                    if (logger.isWarnEnabled())
                                        logger.warn(problem);
                                }
                            }

                           nodeService.setProperties(nodeRef, props);
                         }
                     }
                }
                else
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Workflow deployer: Definition '" + nodeRef + "' not deployed as the '" + engineId + "' engine is disabled");
                }
            }
        }
        else
        {
            if (logger.isDebugEnabled())
                logger.debug("Workflow deployer: Definition '" + nodeRef + "' not deployed since it is a working copy");
        }
    }

    /**
     * Validate that the workflow definition node is a child of the correct
     * workflow location node, e.g. "/Company Home/Data Dictionary/Workflows"
     */
    private boolean isValidLocation(NodeRef definitionNode)
    {
        StoreRef storeRef = repoWorkflowDefsLocation.getStoreRef();
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        List<NodeRef> nodeRefs = searchService.selectNodes(
                rootNode,
                repoWorkflowDefsLocation.getPath(),
                null,
                namespaceService,
                false);
        
        if (nodeRefs.isEmpty() || nodeRefs.size() > 1)
        {
            throw new IllegalStateException("Incorrect number of nodes ("+nodeRefs.size()+")" +
                    " found for workflow location: "+repoWorkflowDefsLocation.getPath());
        }
        
        NodeRef workflowParent = nodeRefs.get(0);

        for (ChildAssociationRef assoc : nodeService.getParentAssocs(definitionNode))
        {
            if (assoc.getParentRef().equals(workflowParent))
            {
                // The workflow definition is contained in the correct location
                return true;
            }
        }
        
        // Invalid location
        return false;
    }

    private void logDeployment(Object location, WorkflowDeployment deployment)
    {
        if (logger.isDebugEnabled())
         {
            String title = deployment.getDefinition().getTitle();
            String version = deployment.getDefinition().getVersion();
            int problemLength = deployment.getProblems().length;
            logger.debug("Workflow deployer: Deployed process definition '" + title + "' (version " + version + ") from '" + location + "' with " + problemLength + " problems");
         }
    }
    
    public void undeploy(NodeRef nodeRef)
    {
        // Ignore if the node is a working copy 
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
        {
            String defName = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_NAME);
            if (defName != null)
            {
                // Undeploy the workflow definition
                List<WorkflowDefinition> defs = workflowService.getAllDefinitionsByName(defName);
                for (WorkflowDefinition def: defs)
                {
                    if (logger.isDebugEnabled())
                        logger.debug("Undeploying workflow '" + defName + "' ...");

                    workflowService.undeployDefinition(def.getId());

                    if (logger.isDebugEnabled())
                        logger.debug("... undeployed '" + def.getId() + "' v" + def.getVersion());
                }
            }
        }
        else
        {
            if (logger.isDebugEnabled())
                logger.debug("Workflow deployer: Definition '" + nodeRef + "' not undeployed since it is a working copy");
        }
    }
    
    public int undeploy(List<String> workflowNames)
    {
        int undeployed = 0;
        for(String workflowName : workflowNames)
        {
            String engineId = BPMEngineRegistry.getEngineId(workflowName);
            if (workflowAdminService.isEngineEnabled(engineId))
            {
                // Undeploy the workflow definition
                List<WorkflowDefinition> defs = workflowService.getAllDefinitionsByName(workflowName);
                if(defs.size() > 0)
                {
                    undeployed++;
                }
                for (WorkflowDefinition def: defs)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Undeploying workflow '" + workflowName + "' ...");
                    }

                    workflowService.undeployDefinition(def.getId());

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("... undeployed '" + def.getId() + "' v" + def.getVersion());
                    }
                }
            }
            else
            {
                logger.debug("Workflow deployer: Definition '" + workflowName + "' cannot be undeployed as the '" + engineId + "' engine is disabled");
            }
        }
        return undeployed;
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
    	RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
    	txnHelper.setForceWritable(true);
    	txnHelper.doInTransaction(new RetryingTransactionCallback<Object>() {

			@Override
			public Object execute() throws Throwable {
				// run as System on bootstrap
		        return AuthenticationUtil.runAs(new RunAsWork<Object>()
		        {
		            public Object doWork()
		            {
		                init();
		                return null;
		            }
		        }, AuthenticationUtil.getSystemUserName());
			}
    		
		}, false, true);
        
        tenantAdminService.register(this);
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }

    
    /**
     * Workflow deployer transaction listener class.
     */
    public class WorkflowDeployerTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            RetryingTransactionCallback<Void> work = new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                            {
                                public Object doWork()
                                {
                                    // force refresh of the dictionary
                                    dictionaryDAO.init();

                                    if (logger.isTraceEnabled())
                                    {
                                        logger.trace("Workflow deployer afterCommit: Dictionary destroyed ["+AlfrescoTransactionSupport.getTransactionId()+"]");
                                    }
                                    
                                    return null; 
                                }
                            }, AuthenticationUtil.getSystemUserName());
                    
                    return null;
                }
            };
            
            transactionService.getRetryingTransactionHelper().doInTransaction(work, true, true);
        }
    }
}
