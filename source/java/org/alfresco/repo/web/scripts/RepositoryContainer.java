/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.StringBuilderWriter;
import org.alfresco.web.scripts.AbstractRuntimeContainer;
import org.alfresco.web.scripts.Authenticator;
import org.alfresco.web.scripts.Cache;
import org.alfresco.web.scripts.Description;
import org.alfresco.web.scripts.Registry;
import org.alfresco.web.scripts.Runtime;
import org.alfresco.web.scripts.ServerModel;
import org.alfresco.web.scripts.WebScript;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.WebScriptResponse;
import org.alfresco.web.scripts.WrappingWebScriptResponse;
import org.alfresco.web.scripts.Description.RequiredAuthentication;
import org.alfresco.web.scripts.Description.RequiredTransaction;
import org.alfresco.web.scripts.Description.RequiredTransactionParameters;
import org.alfresco.web.scripts.Description.TransactionCapability;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;


/**
 * Repository (server-tier) container for Web Scripts
 * 
 * @author davidc
 */
public class RepositoryContainer extends AbstractRuntimeContainer implements TenantDeployer
{
    // Logger
    protected static final Log logger = LogFactory.getLog(RepositoryContainer.class);

    // Transaction key for buffered response
    private static String BUFFERED_RESPONSE_KEY = RepositoryContainer.class.getName() + ".bufferedresponse";
    
    /** Component Dependencies */
    private Repository repository;
    private RepositoryImageResolver imageResolver;
    private RetryingTransactionHelper retryingTransactionHelper;
    private AuthorityService authorityService;
    private DescriptorService descriptorService;
    private TenantAdminService tenantAdminService;
    private ObjectFactory registryFactory;
    private SimpleCache<String, Registry> webScriptsRegistryCache;

    /**
     * @param webScriptsRegistryCache
     */
    public void setWebScriptsRegistryCache(SimpleCache<String, Registry> webScriptsRegistryCache)
    {
        this.webScriptsRegistryCache = webScriptsRegistryCache;
    }
    
    /**
     * @param registryFactory
     */
    public void setRegistryFactory(ObjectFactory registryFactory)
    {
        this.registryFactory = registryFactory;
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
     * @param retryingTransactionHelper
     */
    public void setTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
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

    /**
     * @param tenantAdminService
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
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
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Map<String, Object>>()
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
            NodeRef person = repository.getPerson();
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
    public void executeScript(WebScriptRequest scriptReq, WebScriptResponse scriptRes, Authenticator auth)
        throws IOException
    {
        WebScript script = scriptReq.getServiceMatch().getWebScript();
        Description desc = script.getDescription();
        
        // Escalate the webscript declared level of authentication to the container required authentication
        // eg. must be guest if MT is enabled unless credentials are empty
        RequiredAuthentication required = desc.getRequiredAuthentication();
        RequiredAuthentication containerRequiredAuthentication = getRequiredAuthentication();
        
        if ((required.compareTo(containerRequiredAuthentication) < 0) && (! auth.emptyCredentials()))
        {
            required = containerRequiredAuthentication;
        }
        boolean isGuest = scriptReq.isGuest();
        
        if (required == RequiredAuthentication.none)
        {
            // TODO revisit - cleared here, in-lieu of WebClient clear
            AuthenticationUtil.clearCurrentSecurityContext();
            
            transactionedExecuteAs(script, scriptReq, scriptRes);
        }
        else if ((required == RequiredAuthentication.user || required == RequiredAuthentication.admin) && isGuest)
        {
            throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + desc.getId() + " requires user authentication; however, a guest has attempted access.");
        }
        else
        {
            String currentUser = null;

            try
            {
                AuthenticationUtil.pushAuthentication();
                //
                // Determine if user already authenticated
                //
                currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
                if (logger.isDebugEnabled())
                {
                    logger.debug("Current authentication: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
                    logger.debug("Authentication required: " + required);
                    logger.debug("Guest login requested: " + isGuest);
                }

                //
                // Apply appropriate authentication to Web Script invocation
                //
                if (auth == null || auth.authenticate(required, isGuest))
                {
                    if (required == RequiredAuthentication.admin && !(authorityService.hasAdminAuthority() || AuthenticationUtil.getFullyAuthenticatedUser().equals(AuthenticationUtil.getSystemUserName())))
                    {
                        throw new WebScriptException(HttpServletResponse.SC_UNAUTHORIZED, "Web Script " + desc.getId() + " requires admin authentication; however, a non-admin has attempted access.");
                    }
                    
                    if (logger.isDebugEnabled())
                    {
                    	currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
                        logger.debug("Authentication: " + (currentUser == null ? "unauthenticated" : "authenticated as " + currentUser));
                    }
                    
                    // Execute Web Script
                    transactionedExecuteAs(script, scriptReq, scriptRes);
                }
            }
            finally
            {
                //
                // Reset authentication for current thread
                //
                AuthenticationUtil.popAuthentication();
                
                if (logger.isDebugEnabled())
                {
                    String user = AuthenticationUtil.getFullyAuthenticatedUser();
                    logger.debug("Authentication reset: " + (user == null ? "unauthenticated" : "authenticated as " + user));
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
        final Description description = script.getDescription();
        if (description.getRequiredTransaction() == RequiredTransaction.none)
        {
            script.execute(scriptReq, scriptRes);
        }
        else
        {
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

                        WebScriptResponse redirectedRes = scriptRes;
                        RequiredTransactionParameters trxParams = description.getRequiredTransactionParameters();
                        if (trxParams.getCapability() == TransactionCapability.readwrite)
                        {
                            if (trxParams.getBufferSize() > 0)
                            {
                                if (logger.isDebugEnabled())
                                    logger.debug("Creating Transactional Response for ReadWrite transaction; buffersize=" + trxParams.getBufferSize());
    
                                // create buffered response that's sensitive transaction boundary
                                BufferedResponse bufferedRes = new BufferedResponse(scriptRes, trxParams.getBufferSize());
                                AlfrescoTransactionSupport.bindResource(BUFFERED_RESPONSE_KEY, bufferedRes); 
                                AlfrescoTransactionSupport.bindListener(bufferedRes);
                                redirectedRes = bufferedRes;
                            }
                            else
                            {
                                if (logger.isDebugEnabled())
                                    logger.debug("Transactional Response bypassed for ReadWrite - buffersize=0");
                            }
                        }
                        
                        script.execute(scriptReq, redirectedRes);
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
            retryingTransactionHelper.doInTransaction(work, readonly, requiresNew); 
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
     * @see org.alfresco.web.scripts.AbstractRuntimeContainer#getRegistry()
     */
    @Override
    public Registry getRegistry()
    {
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        Registry registry = webScriptsRegistryCache.get(tenantDomain);
        if (registry == null)
        {
            registry = (Registry)registryFactory.getObject();
            registry.reset();
            webScriptsRegistryCache.put(tenantDomain, registry);
        }
        return registry;
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
        destroy();
        init();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onEnableTenant()
     */
    public void onEnableTenant()
    {
        init();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onDisableTenant()
     */
    public void onDisableTenant()
    {
        destroy();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#init()
     */
    public void init()
    {
        tenantAdminService.register(this);
        
        super.reset();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#destroy()
     */
    public void destroy()
    {
        webScriptsRegistryCache.remove(tenantAdminService.getCurrentUserDomain());
    }
    
    
    /**
     * Transactional Buffered Response
     */
    private static class BufferedResponse implements TransactionListener, WrappingWebScriptResponse
    {
        private WebScriptResponse res;
        private int bufferSize;
        private ByteArrayOutputStream outputStream = null;
        private StringBuilderWriter outputWriter = null;
        

        /**
         * Construct
         * 
         * @param res
         * @param bufferSize
         */
        public BufferedResponse(WebScriptResponse res, int bufferSize)
        {
            this.res = res;
            this.bufferSize = bufferSize;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WrappingWebScriptResponse#getNext()
         */
        public WebScriptResponse getNext()
        {
            return res;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#addHeader(java.lang.String, java.lang.String)
         */
        public void addHeader(String name, String value)
        {
            res.addHeader(name, value);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#encodeScriptUrl(java.lang.String)
         */
        public String encodeScriptUrl(String url)
        {
            return res.encodeScriptUrl(url);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#getEncodeScriptUrlFunction(java.lang.String)
         */
        public String getEncodeScriptUrlFunction(String name)
        {
            return res.getEncodeScriptUrlFunction(name);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#getOutputStream()
         */
        public OutputStream getOutputStream() throws IOException
        {
            if (outputStream == null)
            {
                if (outputWriter != null)
                {
                    throw new AlfrescoRuntimeException("Already buffering output writer");
                }
                this.outputStream = new ByteArrayOutputStream(bufferSize);
            }
            return outputStream;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#getRuntime()
         */
        public Runtime getRuntime()
        {
            return res.getRuntime();
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#getWriter()
         */
        public Writer getWriter() throws IOException
        {
            if (outputWriter == null)
            {
                if (outputStream != null)
                {
                    throw new AlfrescoRuntimeException("Already buffering output stream");
                }
                outputWriter = new StringBuilderWriter(bufferSize);
            }
            return outputWriter;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#reset()
         */
        public void reset()
        {
            if (outputStream != null)
            {
                outputStream.reset();
            }
            else if (outputWriter != null)
            {
                outputWriter = null;
            }
            res.reset();
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#setCache(org.alfresco.web.scripts.Cache)
         */
        public void setCache(Cache cache)
        {
            res.setCache(cache);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#setContentType(java.lang.String)
         */
        public void setContentType(String contentType)
        {
            res.setContentType(contentType);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#setContentEncoding(java.lang.String)
         */
        public void setContentEncoding(String contentEncoding)
        {
            res.setContentEncoding(contentEncoding);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#setHeader(java.lang.String, java.lang.String)
         */
        public void setHeader(String name, String value)
        {
            res.setHeader(name, value);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.web.scripts.WebScriptResponse#setStatus(int)
         */
        public void setStatus(int status)
        {
            res.setStatus(status);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
         */
        public void afterCommit()
        {
            writeResponse();
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
         */
        public void afterRollback()
        {
            writeResponse();
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
         */
        public void beforeCommit(boolean readOnly)
        {
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
         */
        public void beforeCompletion()
        {
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.TransactionListener#flush()
         */
        public void flush()
        {
        }

        /**
         * Write buffered response to underlying response
         */
        private void writeResponse()
        {
            try
            {
                if (logger.isDebugEnabled())
                    logger.debug("Writing Transactional response: size=" + outputStream.size());
                
                if (outputWriter != null)
                {
                    outputWriter.flush();
                    res.getWriter().write(outputWriter.toString());
                }
                else if (outputStream != null)
                {
                    outputStream.flush();
                    outputStream.writeTo(res.getOutputStream());
                }
            }
            catch (IOException e)
            {
                throw new AlfrescoRuntimeException("Failed to commit buffered response", e);
            }
        }
    }
}
