/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.web.scripts;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TooBusyException;
import org.alfresco.repo.web.scripts.bean.LoginPost;
import org.alfresco.service.cmr.repository.ArchivedIOException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
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
    private Supplier<TempOutputStream> streamFactory = null;
    private String preserveHeadersPattern = null;

    private Class<?>[] notPublicExceptions = new Class<?>[] {};
    private Class<?>[] publicExceptions = new Class<?>[] {};

    /*
     * Shame init is already used (by TenantRepositoryContainer).
     */
    public void setup()
    {
        streamFactory = TempOutputStream.factory(
            TempFileProvider.getTempDir(tempDirectoryName),
            memoryThreshold, maxContentSize, encryptTempFiles);
    }

    public void setEncryptTempFiles(Boolean encryptTempFiles)
    {
		if(encryptTempFiles != null)
		{
			this.encryptTempFiles = encryptTempFiles;
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
			this.memoryThreshold = memoryThreshold;
		}
	}

	public void setMaxContentSize(Long maxContentSize)
	{
		if(maxContentSize != null)
		{
			this.maxContentSize = maxContentSize;
		}
	}

    public void setPreserveHeadersPattern(String preserveHeadersPattern)
    {
        this.preserveHeadersPattern = preserveHeadersPattern;
    }

	/**
     * @param repository Repository
     */
    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    /**
     * @param imageResolver RepositoryImageResolver
     */
    public void setRepositoryImageResolver(RepositoryImageResolver imageResolver)
    {
        this.imageResolver = imageResolver;
    }
    
    /**
     * @param transactionService TransactionService
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
     * @param descriptorService DescriptorService
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * @param authorityService AuthorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Exceptions which may contain information that cannot be displayed in UI 
     * 
     * @param notPublicExceptions - {@link Class}&lt;?&gt;[] instance which contains list of not public exceptions
     */
    public void setNotPublicExceptions(List<Class<?>> notPublicExceptions)
    {
        this.notPublicExceptions = new Class<?>[] {};
        if((null != notPublicExceptions) && !notPublicExceptions.isEmpty())
        {
            this.notPublicExceptions = notPublicExceptions.toArray(this.notPublicExceptions);
        }
    }

    public Class<?>[] getNotPublicExceptions()
    {
        return notPublicExceptions;
    }

    /**
     * Exceptions which may contain information that need to display in UI
     *
     * @param publicExceptions - {@link Class}&lt;?&gt;[] instance which contains list of public exceptions
     */
    public void setPublicExceptions(List<Class<?>> publicExceptions)
    {
        this.publicExceptions = new Class<?>[] {};
        if((null != publicExceptions) && !publicExceptions.isEmpty())
        {
            this.publicExceptions = publicExceptions.toArray(this.publicExceptions);
        }
    }

    public Class<?>[] getPublicExceptions()
    {
        return publicExceptions;
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
        Map<String, Object> params = new HashMap<>(super.getScriptParameters());
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
        return fallbackTransactionHelper.doInTransaction(() -> {
            Map<String, Object> params = new HashMap<>(RepositoryContainer.super.getTemplateParameters());
            params.put(TemplateService.KEY_IMAGE_RESOLVER, imageResolver.getImageResolver());
            addRepoParameters(params);
            return params;
        }, true);
    }

    /**
     * Add Repository specific parameters
     * 
     * @param params Map<String, Object>
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
        try
        {
            executeScriptInternal(scriptReq, scriptRes, auth);
        }
        catch (RuntimeException e)
        {
            Throwable hideCause = ExceptionStackUtil.getCause(e, notPublicExceptions);
            Throwable displayCause = ExceptionStackUtil.getCause(e, publicExceptions);
            if (displayCause == null && hideCause != null)
            {
                final AlfrescoRuntimeException alf;
                if (e instanceof AlfrescoRuntimeException)
                {
                    alf = (AlfrescoRuntimeException) e;
                }
                else
                {
                    // The message will not have a numerical identifier
                    alf = new AlfrescoRuntimeException("WebScript execution failed", e);
                }
                String num = alf.getNumericalId();
                logger.error("Server error (" + num + ")", e);
                throw new RuntimeException("Server error (" + num + ").  Details can be found in the server logs.");
            }
            else
            {
                throw e;
            }
        }
    }
    
    protected void executeScriptInternal(final WebScriptRequest scriptReq, final WebScriptResponse scriptRes, final Authenticator auth)
        throws IOException
    {
        final WebScript script = scriptReq.getServiceMatch().getWebScript();
        final Description desc = script.getDescription();
        final boolean debug = logger.isDebugEnabled();

        // Escalate the webscript declared level of authentication to the container required authentication
        // eg. must be guest if MT is enabled unless credentials are empty
        final RequiredAuthentication containerRequiredAuthentication = getRequiredAuthentication();
        final RequiredAuthentication required = (desc.getRequiredAuthentication().compareTo(containerRequiredAuthentication) < 0 && !auth.emptyCredentials() ? containerRequiredAuthentication : desc.getRequiredAuthentication());
        final boolean isGuest = scriptReq.isGuest();

        if (required == RequiredAuthentication.none)
        {
            // TODO revisit - cleared here, in-lieu of WebClient clear
            //AuthenticationUtil.clearCurrentSecurityContext();

            transactionedExecuteAs(script, scriptReq, scriptRes);
            return;
        }

        // if the required authentication is not equal to guest, then it should be one of the following:
        // user | sysadmin | admin (the 'none' authentication is handled above)
        // in this case the guest user should not be able to execute those scripts.
        if (required != RequiredAuthentication.guest && isGuest)
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + desc.getId() + " requires user authentication; however, a guest has attempted access.");
        }

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
            final RetryingTransactionCallback<Boolean> authWork = () -> {
                if (auth != null && !auth.authenticate(required, isGuest))
                {
                    return false;
                }
                // The user will now have been authenticated, based on HTTP Auth, Ticket, etc.
                // Check that the user they authenticated as has appropriate access to the script
                checkScriptAccess(required, desc.getId());

                if (debug)
                {
                    String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
                    logger.debug("Authentication: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
                }

                return true;
            };

            final boolean readOnly = transactionService.isReadOnly();
            final boolean requiresNew = !readOnly && AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY;
            if (!transactionService.getRetryingTransactionHelper().doInTransaction(authWork, readOnly, requiresNew))
            {
                throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed for Web Script " + desc.getId());
            }

            // Execute Web Script if authentication passed
            // The Web Script has its own txn management with potential runAs() user
            transactionedExecuteAs(script, scriptReq, scriptRes, required);
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

    private boolean isSystemUser()
    {
        return Objects.equals(AuthenticationUtil.getFullyAuthenticatedUser(), AuthenticationUtil.getSystemUserName());
    }

    private boolean isSysAdminUser()
    {
        return authorityService.hasSysAdminAuthority();
    }

    private boolean isAdmin()
    {
        return authorityService.hasAdminAuthority();
    }

    public final boolean isAdminOrSystemUser()
    {
        return isAdmin() || isSystemUser();
    }

    /**
     * Check to see if they supplied HTTP Auth or Ticket as guest, on a script that needs more
     */
    private void checkGuestAccess(RequiredAuthentication required, String scriptDescriptorId)
    {
        if (required == RequiredAuthentication.user || required == RequiredAuthentication.admin
                    || required == RequiredAuthentication.sysadmin)
        {
            final String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
            final String runAsUser = AuthenticationUtil.getRunAsUser();

            if ((authenticatedUser == null) || (authenticatedUser.equals(runAsUser)
                        && authorityService.hasGuestAuthority()) || (!authenticatedUser.equals(runAsUser)
                        && authorityService.isGuestAuthority(authenticatedUser)))
            {
                throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + scriptDescriptorId
                            + " requires user authentication; however, a guest has attempted access.");
            }
        }
    }

    private void checkScriptAccess(RequiredAuthentication required, String scriptDescriptorId)
    {
        // first, check guest access
        checkGuestAccess(required, scriptDescriptorId);

        // Check to see if the user is sysAdmin, admin or system on a sysadmin scripts
        if (required == RequiredAuthentication.sysadmin && !(isSysAdminUser() || isAdminOrSystemUser()))
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + scriptDescriptorId
                        + " requires system-admin authentication; however, a non-system-admin has attempted access.");
        }
        else if (required == RequiredAuthentication.admin && !isAdminOrSystemUser())
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + scriptDescriptorId
                        + " requires admin authentication; however, a non-admin has attempted access.");
        }
    }

    /**
     * Execute script within required level of transaction
     * 
     * @param script WebScript
     * @param scriptReq WebScriptRequest
     * @param scriptRes WebScriptResponse
     * @throws IOException
     */
    protected void transactionedExecute(final WebScript script, final WebScriptRequest scriptReq, final WebScriptResponse scriptRes)
        throws IOException
    {
        final Description description = script.getDescription();

        try
        {
            if (description.getRequiredTransaction() == RequiredTransaction.none)
            {
                script.execute(scriptReq, scriptRes);
                return;
            }
        }
        catch (ArchivedIOException e) // handle ArchivedIOException to lower log pollution
        {
            handleArchivedIOException(e);
        }
        catch (IOException e)
        {
            handleIOException(e);
        }

        final RequiredTransactionParameters trxParams = description.getRequiredTransactionParameters();

        try (final BufferedRequest bufferedReq = newBufferedRequest(trxParams, scriptReq, streamFactory);
             final BufferedResponse bufferedRes = newBufferedResponse(trxParams, scriptRes, streamFactory))
        {
            boolean readonly = description.getRequiredTransactionParameters().getCapability() == TransactionCapability.readonly;
            boolean requiresNew = description.getRequiredTransaction() == RequiredTransaction.requiresnew;

            // log a warning if we detect a GET webscript being run in a readwrite transaction, GET calls should
            // NOT have any side effects so this scenario as a warning sign something maybe amiss, see ALF-10179.
            if (logger.isDebugEnabled() && !readonly && "GET".equalsIgnoreCase(
                description.getMethod()))
            {
                logger.debug("Webscript with URL '" + scriptReq.getURL() +
                             "' is a GET request but it's descriptor has declared a readwrite transaction is required");
            }

            try
            {
                final RetryingTransactionHelper transactionHelper = transactionService.getRetryingTransactionHelper();
                if (script instanceof LoginPost)
                {
                    //login script requires read-write transaction because of authorization interceptor
                    transactionHelper.setForceWritable(true);
                }
                transactionHelper.doInTransaction(() -> {
                    try
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Begin retry transaction block: " + description.getRequiredTransaction() + ","
                                + description.getRequiredTransactionParameters().getCapability());

                        if (bufferedReq == null || bufferedRes == null)
                        {
                            script.execute(scriptReq, scriptRes);
                        }
                        else
                        {
                            // Reset the request and response in case of a transaction retry
                            bufferedReq.reset();
                            // REPO-4388 don't reset specified headers
                            bufferedRes.reset(preserveHeadersPattern);
                            script.execute(bufferedReq, bufferedRes);
                        }
                    }
                    catch (Exception e)
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

                        final UserTransaction userTrx = RetryingTransactionHelper.getActiveUserTransaction();
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
                                catch (Throwable re)
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
                }, readonly, requiresNew);
            }
            catch (TooBusyException e)
            {
                // Map TooBusyException to a 503 status code
                throw new WebScriptException(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage(), e);
            }
            catch (ArchivedIOException e) // handle ArchivedIOException to lower log pollution
            {
                handleArchivedIOException(e);
            }

            // Ensure a response is always flushed after successful execution
            if (bufferedRes != null)
            {
                bufferedRes.writeResponse();
            }
        }
    }

    private void handleArchivedIOException(ArchivedIOException e)
    {
        if (logger.isDebugEnabled()) // log with stack trace at debug level
        {
            logger.debug("ArchivedIOException error ", e);
        }
        else if (logger.isInfoEnabled()) // log without stack trace at info level
        {
            logger.info("ArchivedIOException error. Message: " + e.getMessage());
        }
        throw new WebScriptException(HttpServletResponse.SC_PRECONDITION_FAILED, "Content is archived and not accessible.");
    }

    private static void handleIOException(final IOException ioe) throws IOException
    {
        Throwable socketException = ExceptionStackUtil.getCause(ioe, SocketException.class);
        Class<?> clientAbortException = null;
        try
        {
            clientAbortException = Class.forName("org.apache.catalina.connector.ClientAbortException");
        }
        catch (ClassNotFoundException e)
        {
            // do nothing
        }
        // Note: if you need to look for more exceptions in the stack, then create a static array and pass it in
        if ((socketException != null && socketException.getMessage().contains("Broken pipe")) ||
            (clientAbortException != null && ExceptionStackUtil.getCause(ioe, clientAbortException) != null))
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

    /**
     * Execute script within required level of transaction as required effective user.
     *
     * @param script    WebScript
     * @param scriptReq WebScriptRequest
     * @param scriptRes WebScriptResponse
     * @throws IOException
     */
    private void transactionedExecuteAs(final WebScript script, final WebScriptRequest scriptReq,
            final WebScriptResponse scriptRes) throws IOException
    {
        final String runAs = script.getDescription().getRunAs();
        if (runAs == null)
        {
            transactionedExecute(script, scriptReq, scriptRes);
        }
        else
        {
            AuthenticationUtil.runAs(() -> {
                transactionedExecute(script, scriptReq, scriptRes);
                return null;
            }, runAs);
        }
    }

    /**
     * Execute script within required level of transaction as required effective user.
     *
     * @param script    WebScript
     * @param scriptReq WebScriptRequest
     * @param scriptRes WebScriptResponse
     * @param requiredAuthentication Required authentication
     * @throws IOException
     */
    private void transactionedExecuteAs(final WebScript script, final WebScriptRequest scriptReq,
                                        final WebScriptResponse scriptRes, RequiredAuthentication requiredAuthentication) throws IOException
    {
        // Execute as System if and only if, the current user is a member of System-Admin group, and he is not a super admin.
        // E.g. if 'jdoe' is a member of ALFRESCO_SYSTEM_ADMINISTRATORS group, then the work should be executed as System to satisfy the ACL checks.
        // But, if the current user is Admin (i.e. super admin, which by default he is a member fo the ALFRESCO_SYSTEM_ADMINISTRATORS group)
        // then don't wrap the work as RunAs, since he can do anything!
        if (requiredAuthentication == RequiredAuthentication.sysadmin && isSysAdminUser() && !isAdmin())
        {
            AuthenticationUtil.runAs(() -> {
                transactionedExecute(script, scriptReq, scriptRes);
                return null;
            }, AuthenticationUtil.SYSTEM_USER_NAME);
        }
        else
        {
            transactionedExecuteAs(script, scriptReq, scriptRes);
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
            if (refreshContext.equals(applicationContext))
            {
                AuthenticationUtil.runAs(() -> {
                    reset();
                    return null;
                }, AuthenticationUtil.getSystemUserName());
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
        transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            internalReset();
            return null;
        }, true, false);
    }

    private void internalReset()
    {
        super.reset();
    }

    private static BufferedRequest newBufferedRequest(
        final RequiredTransactionParameters trxParams,
        final WebScriptRequest scriptReq,
        final Supplier<TempOutputStream> streamFactory)
    {
        if (trxParams.getCapability() != TransactionCapability.readwrite)
        {
            return null;
        }
        if (trxParams.getBufferSize() <= 0)
        {
            return null;
        }

        // create buffered request that allow transaction retrying
        return new BufferedRequest(scriptReq, streamFactory);
    }

    private static BufferedResponse newBufferedResponse(
        final RequiredTransactionParameters trxParams,
        final WebScriptResponse scriptRes,
        final Supplier<TempOutputStream> streamFactory)
    {
        if (trxParams.getCapability() != TransactionCapability.readwrite)
        {
            return null;
        }
        if (trxParams.getBufferSize() <= 0)
        {
            if (logger.isDebugEnabled())
                logger.debug("Transactional Response bypassed for ReadWrite - buffersize=0");
            return null;
        }
        if (logger.isDebugEnabled())
            logger.debug("Creating Transactional Response for ReadWrite transaction; buffersize=" + trxParams.getBufferSize());

        // create buffered response that allow transaction retrying
        return new BufferedResponse(scriptRes, trxParams.getBufferSize(), streamFactory);
    }
}
