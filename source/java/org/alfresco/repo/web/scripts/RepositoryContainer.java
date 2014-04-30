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
package org.alfresco.repo.web.scripts;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TooBusyException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStreamFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.webscripts.AbstractRuntimeContainer;
import org.springframework.extensions.webscripts.Authenticator;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Description.RequiredTransaction;
import org.springframework.extensions.webscripts.Description.RequiredTransactionParameters;
import org.springframework.extensions.webscripts.Description.TransactionCapability;
import org.springframework.extensions.webscripts.ServerModel;
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;


/**
 * Repository (server-tier) container for Web Scripts
 * 
 * @author steveglover
 * @author davidc
 */
public class RepositoryContainer extends AbstractRuntimeContainer
{
    // Logger
    protected static final Log logger = LogFactory.getLog(RepositoryContainer.class);

    /** Component Dependencies */
    private Repository repository;
    private RepositoryImageResolver imageResolver;
    private TransactionService transactionService;
    private RetryingTransactionHelper fallbackTransactionHelper;
    private AuthorityService authorityService;
    private DescriptorService descriptorService;

    private boolean encryptTempFiles = false;
    private String tempDirectoryName = null;
    private int memoryThreshold = 4 * 1024 * 1024; // 4mb
    private long maxContentSize = (long) 4 * 1024 * 1024 * 1024; // 4gb
    private ThresholdOutputStreamFactory streamFactory = null;

    /*
     * Shame init is already used (by TenantRepositoryContainer).
     */
    public void setup()
    {
        File tempDirectory = new File(TempFileProvider.getTempDir(), tempDirectoryName);
    	this.streamFactory = ThresholdOutputStreamFactory.newInstance(tempDirectory, memoryThreshold, maxContentSize, encryptTempFiles);
    }

    public void setEncryptTempFiles(Boolean encryptTempFiles)
    {
		if(encryptTempFiles != null)
		{
			this.encryptTempFiles = encryptTempFiles.booleanValue();
		}
	}

	public void setTempDirectoryName(String tempDirectoryName)
	{
		this.tempDirectoryName = tempDirectoryName;
	}

	public void setMemoryThreshold(Integer memoryThreshold)
	{
		if(memoryThreshold != null)
		{
			this.memoryThreshold = memoryThreshold.intValue();
		}
	}

	public void setMaxContentSize(Long maxContentSize)
	{
		if(maxContentSize != null)
		{
			this.maxContentSize = maxContentSize.longValue();
		}
	}

	/**
     * @param repository
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    /**
     * @param imageResolver
     */
    public void setRepositoryImageResolver(RepositoryImageResolver imageResolver)
    {
        this.imageResolver = imageResolver;
    }
    
    /**
     * @param transactionService
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param fallbackTransactionHelper an unlimited transaction helper used to generate error responses
     */
    public void setFallbackTransactionHelper(RetryingTransactionHelper fallbackTransactionHelper)
    {
        this.fallbackTransactionHelper = fallbackTransactionHelper;
    }

    /**
     * @param descriptorService
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * @param authorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Container#getDescription()
     */
    public ServerModel getDescription()
    {
        return new RepositoryServerModel(descriptorService.getCurrentRepositoryDescriptor(), descriptorService.getServerDescriptor());
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractRuntimeContainer#getScriptParameters()
     */
    public Map<String, Object> getScriptParameters()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.putAll(super.getScriptParameters());
        addRepoParameters(params);
        return params;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractRuntimeContainer#getTemplateParameters()
     */
    public Map<String, Object> getTemplateParameters()
    {
        // Ensure we have a transaction - we might be generating the status template after the main transaction failed
        return fallbackTransactionHelper.doInTransaction(new RetryingTransactionCallback<Map<String, Object>>()
        {
            public Map<String, Object> execute() throws Throwable
            {
                Map<String, Object> params = new HashMap<String, Object>();
                params.putAll(RepositoryContainer.super.getTemplateParameters());
                params.put(TemplateService.KEY_IMAGE_RESOLVER, imageResolver.getImageResolver());
                addRepoParameters(params);
                return params;
            }
        }, true);
    }

    /**
     * Add Repository specific parameters
     * 
     * @param params
     */
    private void addRepoParameters(Map<String, Object> params)
    {
        if (AlfrescoTransactionSupport.getTransactionId() != null &&
            AuthenticationUtil.getFullAuthentication() != null)
        {
            NodeRef rootHome = repository.getRootHome();
            if (rootHome != null)
            {
                params.put("roothome", rootHome);
            }
            NodeRef companyHome = repository.getCompanyHome();
            if (companyHome != null)
            {
                params.put("companyhome", companyHome);
            }
            NodeRef person = repository.getFullyAuthenticatedPerson();
            if (person != null)
            {
                params.put("person", person);
                NodeRef userHome = repository.getUserHome(person);
                if (userHome != null)
                {
                    params.put("userhome", userHome);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.RuntimeContainer#executeScript(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse, org.alfresco.web.scripts.Authenticator)
     */
    public void executeScript(WebScriptRequest scriptReq, WebScriptResponse scriptRes, final Authenticator auth)
        throws IOException
    {
        final boolean debug = logger.isDebugEnabled();
        final WebScript script = scriptReq.getServiceMatch().getWebScript();
        final Description desc = script.getDescription();
        
        // Escalate the webscript declared level of authentication to the container required authentication
        // eg. must be guest if MT is enabled unless credentials are empty
        RequiredAuthentication containerRequiredAuthentication = getRequiredAuthentication();
        final RequiredAuthentication required = (desc.getRequiredAuthentication().compareTo(containerRequiredAuthentication) < 0 && !auth.emptyCredentials() ? containerRequiredAuthentication : desc.getRequiredAuthentication());
        final boolean isGuest = scriptReq.isGuest();
        
        if (required == RequiredAuthentication.none)
        {
            // TODO revisit - cleared here, in-lieu of WebClient clear
            //AuthenticationUtil.clearCurrentSecurityContext();
            
            transactionedExecuteAs(script, scriptReq, scriptRes);
        }
        else if ((required == RequiredAuthentication.user || required == RequiredAuthentication.admin) && isGuest)
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + desc.getId() + " requires user authentication; however, a guest has attempted access.");
        }
        else
        {
            try
            {
                AuthenticationUtil.pushAuthentication();
                
                //
                // Determine if user already authenticated
                //
                if (debug)
                {
                    String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
                    logger.debug("Current authentication: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
                    logger.debug("Authentication required: " + required);
                    logger.debug("Guest login requested: " + isGuest);
                }
                
                //
                // Apply appropriate authentication to Web Script invocation
                //
                RetryingTransactionCallback<Boolean> authWork = new RetryingTransactionCallback<Boolean>()
                {
                    public Boolean execute() throws Exception
                    {
                        if (auth == null || auth.authenticate(required, isGuest))
                        {
                            // The user will now have been authenticated, based on HTTP Auth, Ticket etc
                            // Check that the user they authenticated as has appropriate access to the script
                            
                            // Check to see if they supplied HTTP Auth or Ticket as guest, on a script that needs more
                            if (required == RequiredAuthentication.user || required == RequiredAuthentication.admin)
                            {
                                String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
                                String runAsUser = AuthenticationUtil.getRunAsUser();
                                
                                if ( (authenticatedUser == null) ||
                                     (authenticatedUser.equals(runAsUser) && authorityService.hasGuestAuthority()) ||
                                     (!authenticatedUser.equals(runAsUser) && authorityService.isGuestAuthority(authenticatedUser)) )
                                {
                                    throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + desc.getId() + " requires user authentication; however, a guest has attempted access.");
                                }
                            }
                            
                            // Check to see if they're admin or system on an Admin only script
                            if (required == RequiredAuthentication.admin && !(authorityService.hasAdminAuthority() || AuthenticationUtil.getFullyAuthenticatedUser().equals(AuthenticationUtil.getSystemUserName())))
                            {
                                throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + desc.getId() + " requires admin authentication; however, a non-admin has attempted access.");
                            }
                            
                            if (debug)
                            {
                                String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
                                logger.debug("Authentication: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
                            }
                            
                            return true;
                        }
                        return false;
                    }        
                };
                
                boolean readOnly = transactionService.isReadOnly();
                boolean requiresNew = !readOnly && AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY;
                if (transactionService.getRetryingTransactionHelper().doInTransaction(authWork, readOnly, requiresNew))
                {
                    // Execute Web Script if authentication passed
                    // The Web Script has its own txn management with potential runAs() user
                    transactionedExecuteAs(script, scriptReq, scriptRes);
                }
                else
                {
                    throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed for Web Script " + desc.getId());
                }
            }
            finally
            {
                //
                // Reset authentication for current thread
                //
                AuthenticationUtil.popAuthentication();
                
                if (debug)
                {
                    String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
                    logger.debug("Authentication reset: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
                }
            }
        }
    }

    /**
     * Execute script within required level of transaction
     * 
     * @param scriptReq
     * @param scriptRes
     * @throws IOException
     */
    protected void transactionedExecute(final WebScript script, final WebScriptRequest scriptReq, final WebScriptResponse scriptRes)
        throws IOException
    {
        try
        {
            final Description description = script.getDescription();
            if (description.getRequiredTransaction() == RequiredTransaction.none)
            {
                script.execute(scriptReq, scriptRes);
            }
            else
            {
                final BufferedRequest bufferedReq;
                final BufferedResponse bufferedRes;
                RequiredTransactionParameters trxParams = description.getRequiredTransactionParameters();
                if (trxParams.getCapability() == TransactionCapability.readwrite)
                {
                    if (trxParams.getBufferSize() > 0)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Creating Transactional Response for ReadWrite transaction; buffersize=" + trxParams.getBufferSize());

                        // create buffered request and response that allow transaction retrying
                        bufferedReq = new BufferedRequest(scriptReq, streamFactory);
                        bufferedRes = new BufferedResponse(scriptRes, trxParams.getBufferSize());
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Transactional Response bypassed for ReadWrite - buffersize=0");
                        bufferedReq = null;
                        bufferedRes = null;
                    }
                }
                else
                {
                    bufferedReq = null;
                    bufferedRes = null;
                }
            
                // encapsulate script within transaction
                RetryingTransactionCallback<Object> work = new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            if (logger.isDebugEnabled())
                                logger.debug("Begin retry transaction block: " + description.getRequiredTransaction() + "," 
                                        + description.getRequiredTransactionParameters().getCapability());

                            if (bufferedRes == null)
                            {
                                script.execute(scriptReq, scriptRes);                            
                            }
                            else
                            {
                                // Reset the request and response in case of a transaction retry
                                bufferedReq.reset();
                                bufferedRes.reset();
                                script.execute(bufferedReq, bufferedRes);
                            }
                        }
                        catch(Exception e)
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Transaction exception: " + description.getRequiredTransaction() + ": " + e.getMessage());
                                // Note: user transaction shouldn't be null, but just in case inside this exception handler
                                UserTransaction userTrx = RetryingTransactionHelper.getActiveUserTransaction();
                                if (userTrx != null)
                                {
                                    logger.debug("Transaction status: " + userTrx.getStatus());
                                }
                            }
                           
                            UserTransaction userTrx = RetryingTransactionHelper.getActiveUserTransaction();
                            if (userTrx != null)
                            {
                                if (userTrx.getStatus() != Status.STATUS_MARKED_ROLLBACK)
                                {
                                    if (logger.isDebugEnabled())
                                        logger.debug("Marking web script transaction for rollback");
                                    try
                                    {
                                        userTrx.setRollbackOnly();
                                    }
                                    catch(Throwable re)
                                    {
                                        if (logger.isDebugEnabled())
                                            logger.debug("Caught and ignoring exception during marking for rollback: " + re.getMessage());
                                    }
                                }
                            }
                        
                            // re-throw original exception for retry
                            throw e;
                        }
                        finally
                        {
                           if (logger.isDebugEnabled())
                                logger.debug("End retry transaction block: " + description.getRequiredTransaction() + "," 
                                        + description.getRequiredTransactionParameters().getCapability());
                        }
                    
                        return null;
                    }        
                };
        
                boolean readonly = description.getRequiredTransactionParameters().getCapability() == TransactionCapability.readonly;
                boolean requiresNew = description.getRequiredTransaction() == RequiredTransaction.requiresnew;
            
                // log a warning if we detect a GET webscript being run in a readwrite transaction, GET calls should
                // NOT have any side effects so this scenario as a warning sign something maybe amiss, see ALF-10179.
                if (logger.isDebugEnabled() && !readonly && "GET".equalsIgnoreCase(description.getMethod()))
                {
                    logger.debug("Webscript with URL '" + scriptReq.getURL() + 
                               "' is a GET request but it's descriptor has declared a readwrite transaction is required");
                }
            
                try
                {
                    transactionService.getRetryingTransactionHelper().doInTransaction(work, readonly, requiresNew);
                }
                catch (TooBusyException e)
                {
                    // Map TooBusyException to a 503 status code
                    throw new WebScriptException(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage(), e);
                }
                finally
                {
                    // Get rid of any temporary files
                    if (bufferedReq != null)
                    {
                        bufferedReq.close();
                    }
                }

                // Ensure a response is always flushed after successful execution
                if (bufferedRes != null)
                {
                    bufferedRes.writeResponse();
                }
            
            }
        }
        catch (IOException ioe)
        {
            Throwable socketException = ExceptionStackUtil.getCause(ioe, SocketException.class);
            if (socketException != null && socketException.getMessage().contains("Broken pipe"))
            {
                if (logger.isDebugEnabled())
                {
                    logger.warn("Client has cut off communication", ioe);
                }
                else
                {
                    logger.info("Client has cut off communication");
                }
            }
            else
            {
                throw ioe;
            }
        }
    }
    
    /**
     * Execute script within required level of transaction as required effective user.
     * 
     * @param scriptReq
     * @param scriptRes
     * @throws IOException
     */
    private void transactionedExecuteAs(final WebScript script, final WebScriptRequest scriptReq,
            final WebScriptResponse scriptRes) throws IOException
    {
        String runAs = script.getDescription().getRunAs();
        if (runAs == null)
        {
            transactionedExecute(script, scriptReq, scriptRes);
        }
        else
        {
            RunAsWork<Object> work = new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    transactionedExecute(script, scriptReq, scriptRes);
                    return null;
                }
            };
            AuthenticationUtil.runAs(work, runAs);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractRuntimeContainer#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent)event;
            ApplicationContext refreshContext = refreshEvent.getApplicationContext();
            if (refreshContext != null && refreshContext.equals(applicationContext))
            {
                RunAsWork<Object> work = new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        reset();
                        return null;
                    }
                };
                AuthenticationUtil.runAs(work, AuthenticationUtil.getSystemUserName());
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractRuntimeContainer#getRequiredAuthentication()
     */
    @Override
    public RequiredAuthentication getRequiredAuthentication()
    {
        if (AuthenticationUtil.isMtEnabled())
        {
            return RequiredAuthentication.guest; // user or guest (ie. at least guest)
        }
        
        return RequiredAuthentication.none;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.RuntimeContainer#authenticate(org.alfresco.web.scripts.Authenticator, org.alfresco.web.scripts.Description.RequiredAuthentication)
     */
    @Override
    public boolean authenticate(Authenticator auth, RequiredAuthentication required)
    {
        if (auth != null)
        {
            AuthenticationUtil.clearCurrentSecurityContext();
            
            return auth.authenticate(required, false);
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractRuntimeContainer#reset()
     */
    @Override
    public void reset() 
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                internalReset();
                return null;
            }
        }, true, false);
    }
    
    private void internalReset()
    {
        super.reset();
    }
}
