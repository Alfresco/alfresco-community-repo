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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.dictionary.DictionaryBootstrap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowService;
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
     * Sets the initial list of Workflow reosurce bundles to bootstrap with
     * 
     * @param modelResources the model names
     */
    public void setLabels(List<String> labels)
    {
        this.resourceBundles = labels;
    }
        
    /**
     * Deploy the Workflow Definitions
     */
    public void deploy()
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

        UserTransaction userTransaction = transactionService.getUserTransaction();
        authenticationComponent.setSystemUserAsCurrentUser();

        try
        {
            userTransaction.begin();
        
            // bootstrap the workflow models and labels
            if (models != null && resourceBundles != null)
            {
            	DictionaryBootstrap dictionaryBootstrap = new DictionaryBootstrap();
            	dictionaryBootstrap.setDictionaryDAO(dictionaryDAO);
            	dictionaryBootstrap.setModels(models);
            	dictionaryBootstrap.setLabels(resourceBundles);
            	dictionaryBootstrap.bootstrap();
            }
            
            // bootstrap the workflow definitions
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
                
            userTransaction.commit();
        }
        catch(Throwable e)
        {
            // rollback the transaction
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("Workflow deployment failed", e);
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        deploy();
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }

}
