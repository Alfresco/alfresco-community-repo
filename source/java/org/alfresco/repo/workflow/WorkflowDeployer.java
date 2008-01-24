/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.AbstractLifecycleBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.ClassPathResource;


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
    
    // Dependencies
    private boolean allowWrite = true;
    private TransactionService transactionService;
    private WorkflowService workflowService;
    private AuthenticationComponent authenticationComponent;
    private DictionaryDAO dictionaryDAO;
    private List<Properties> workflowDefinitions;
    private List<String> models = new ArrayList<String>();
    private List<String> resourceBundles = new ArrayList<String>();
    private TenantService tenantService;
    
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private SearchService searchService;
    private RepositoryLocation repoWorkflowDefsLocation;

    public final static String CRITERIA_ALL = "/*"; // immediate children only
    public final static String defaultSubtypeOfWorkflowDefinitionType = "subtypeOf('bpm:workflowDefinition')";
    
    /**
     * Set whether we write or not
     * 
     * @param write true (default) if the import must go ahead, otherwise no import will occur
     */
    public void setAllowWrite(boolean write)
    {
        this.allowWrite = write;
    }

    /**
     * Sets the Transaction Service
     * 
     * @param userTransaction the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Sets the namespace service
     * 
     * @param namespaceService the namespace service
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /**
     * Set the authentication component
     * 
     * @param authenticationComponent
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Sets the Dictionary DAO
     * 
     * @param dictionaryDAO
     */
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
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
     * @param workflowDefinitions
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
     * @param modelResources the model names
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
        if (transactionService == null)
        {
            throw new ImporterException("Transaction Service must be provided");
        }
        if (authenticationComponent == null)
        {
            throw new ImporterException("Authentication Component must be provided");
        }
        if (workflowService == null)
        {
            throw new ImporterException("Workflow Service must be provided");
        }

        String currentUser = authenticationComponent.getCurrentUserName();
        if (currentUser == null)
        {
            authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        }
        
        UserTransaction userTransaction = transactionService.getUserTransaction();

        try
        {
            userTransaction.begin();
        
            // bootstrap the workflow models and static labels (from classpath)
            if (models != null && resourceBundles != null)
            {
            	DictionaryBootstrap dictionaryBootstrap = new DictionaryBootstrap();
            	dictionaryBootstrap.setDictionaryDAO(dictionaryDAO);
                dictionaryBootstrap.setTenantService(tenantService);
            	dictionaryBootstrap.setModels(models);
            	dictionaryBootstrap.setLabels(resourceBundles);
            	dictionaryBootstrap.bootstrap(); // also registers with dictionary
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
                    Boolean redeploy = Boolean.valueOf(workflowDefinition.getProperty(REDEPLOY));
                    String mimetype = workflowDefinition.getProperty(MIMETYPE);

                    // retrieve input stream on workflow definition
                    ClassPathResource workflowResource = new ClassPathResource(location);
                    
                    // deploy workflow definition
                    if (!allowWrite)
                    {
                        // we're in read-only node
                        logger.warn("Repository is in read-only mode; not deploying workflow " + location);
                    }
                    else
                    {
                        if (!redeploy && workflowService.isDefinitionDeployed(engineId, workflowResource.getInputStream(), mimetype))
                        {
                            if (logger.isDebugEnabled())
                                logger.debug("Workflow deployer: Definition '" + location + "' already deployed");
                        }
                        else
                        {
                            WorkflowDeployment deployment = workflowService.deployDefinition(engineId, workflowResource.getInputStream(), mimetype);
                            if (logger.isInfoEnabled())
                                logger.info("Workflow deployer: Deployed process definition '" + deployment.definition.title + "' (version " + deployment.definition.version + ") from '" + location + "' with " + deployment.problems.length + " problems");
                        }
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
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("Workflow deployment failed", e);
        }
        finally
        {
            if (currentUser == null)
            {
                authenticationComponent.clearCurrentSecurityContext();
            }
        }
    }

    public void deploy(NodeRef nodeRef, boolean redeploy)
    {
        // Ignore if the node is a working copy 
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
        {
            Boolean value = (Boolean)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED);
            if ((value != null) && (value.booleanValue() == true))
            {            
            	 if (!redeploy && workflowService.isDefinitionDeployed(nodeRef))
                 {
                     if (logger.isDebugEnabled())
                     {
                         logger.debug("Workflow deployer: Definition '" + nodeRef + "' already deployed");
                     }
                 }
                 else
                 {
                	 // deploy / re-deploy
                     WorkflowDeployment deployment = workflowService.deployDefinition(nodeRef);
                     if (logger.isInfoEnabled())
                     {
                         logger.info("Workflow deployer: Deployed process definition '" + deployment.definition.title + "' (version " + deployment.definition.version + ") from '" + nodeRef + "' with " + deployment.problems.length + " problems");
                     }
	                 if (deployment != null)
	                 {
	                	 WorkflowDefinition def = deployment.definition;
	                    
	                	 // Update the meta data for the model
	                	 Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
						
	                	 props.put(WorkflowModel.PROP_WORKFLOW_DEF_NAME, def.getName());
						
	                	 // TODO - ability to return and handle deployment problems / warnings
	                	 if (deployment.problems.length > 0)
	                	 {
	                		 for (String problem : deployment.problems)
	                		 {
	                			 logger.warn(problem);
	                		 }
	                	 }
						
	                	 nodeService.setProperties(nodeRef, props);
	                }
	            }
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
            	logger.debug("Workflow deployer: Definition '" + nodeRef + "' not deployed since it is a working copy");
            }
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
                // Undeploy the workflow definition - all versions in JBPM
                List<WorkflowDefinition> defs = workflowService.getAllDefinitionsByName(defName);
                for (WorkflowDefinition def: defs)
                {
                	if (logger.isInfoEnabled())
                    {
                		logger.info("Undeploying workflow '" + defName + "' ...");
                    }
                    workflowService.undeployDefinition(def.getId());
                	if (logger.isInfoEnabled())
                    {
                		logger.info("... undeployed '" + def.getId() + "' v" + def.getVersion());
                    }
                }
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
            	logger.debug("Workflow deployer: Definition '" + nodeRef + "' not undeployed since it is a working copy");
            }
        }
    }
    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // run as System on bootstrap
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {            
                init();
                return null;
            }                               
        }, AuthenticationUtil.getSystemUserName());
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }

}
