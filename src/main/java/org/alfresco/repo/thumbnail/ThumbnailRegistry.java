/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.thumbnail;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.rendition2.RenditionDefinition2;
import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2;
import org.alfresco.repo.rendition2.TransformationOptionsConverter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.thumbnail.ThumbnailException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.transform.client.registry.TransformServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all the thumbnail details available
 * 
 * @author Roy Wetherall
 * @author Neil McErlean
 *
 * @deprecated The thumbnails code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class ThumbnailRegistry implements ApplicationContextAware, ApplicationListener<ApplicationContextEvent>
{
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "org.alfresco.repo.thumbnail.ThumbnailRegistry");
    private static final long LOCK_TTL = 60000;

    /** Logger */
    private static Log logger = LogFactory.getLog(ThumbnailRegistry.class);
    
    /** Transaction service */
    protected TransactionService transactionService;
    
    /** Rendition service */
    protected RenditionService renditionService;
    
    protected TenantAdminService tenantAdminService;

    private JobLockService jobLockService;

    private TransformServiceRegistry transformServiceRegistry;

    private TransformServiceRegistry localTransformServiceRegistry;

    private TransformationOptionsConverter converter;

    private RenditionDefinitionRegistry2 renditionDefinitionRegistry2;

    private boolean redeployStaticDefsOnStartup;
    
    /** Map of thumbnail definition */
    private Map<String, ThumbnailDefinition> thumbnailDefinitions = new HashMap<String, ThumbnailDefinition>();
    
    /** Cache to store mimetype to thumbnailDefinition mapping with max size limit */
    private Map<String, List<ThumbnailDefinitionLimits>> mimetypeMap = new HashMap<String, List<ThumbnailDefinitionLimits>>(17);

    private ThumbnailRenditionConvertor thumbnailRenditionConvertor;
    
    private RegistryLifecycle lifecycle = new RegistryLifecycle();
    
    public void setThumbnailRenditionConvertor(
            ThumbnailRenditionConvertor thumbnailRenditionConvertor)
    {
        this.thumbnailRenditionConvertor = thumbnailRenditionConvertor;
    }

    public ThumbnailRenditionConvertor getThumbnailRenditionConvertor()
    {
        return thumbnailRenditionConvertor;
    }
    
    /**
     * Transaction service
     * 
     * @param transactionService    transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Rendition service
     * 
     * @param renditionService    rendition service
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }
    
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    public void setRedeployStaticDefsOnStartup(boolean redeployStaticDefsOnStartup)
    {
        this.redeployStaticDefsOnStartup = redeployStaticDefsOnStartup;
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void setTransformServiceRegistry(TransformServiceRegistry transformServiceRegistry)
    {
        this.transformServiceRegistry = transformServiceRegistry;
        mimetypeMap.clear();
    }

    public void setLocalTransformServiceRegistry(TransformServiceRegistry localTransformServiceRegistry)
    {
        this.localTransformServiceRegistry = localTransformServiceRegistry;
    }

    public void setConverter(TransformationOptionsConverter converter)
    {
        this.converter = converter;
    }

    public void setRenditionDefinitionRegistry2(RenditionDefinitionRegistry2 renditionDefinitionRegistry2)
    {
        this.renditionDefinitionRegistry2 = renditionDefinitionRegistry2;
    }

    /**
     * This method is used to inject the thumbnail definitions.
     */
    public void setThumbnailDefinitions(final List<ThumbnailDefinition> thumbnailDefinitions)
    {
        for (ThumbnailDefinition td : thumbnailDefinitions)
        {
            String thumbnailName = td.getName();
            if (thumbnailName == null)
            {
                throw new ThumbnailException("When adding a thumbnail details object make sure the name is set.");
            }
            td.getTransformationOptions().setUse(thumbnailName);
            this.thumbnailDefinitions.put(thumbnailName, td);
        }
    }
    
    // Otherwise we should go ahead and persist the thumbnail definitions.
    // This is done during system startup. It needs to be done as the system user (see callers) to ensure the thumbnail definitions get saved
    // and also needs to be done within a transaction in order to support concurrent startup. See c for details.
    // MNT-19986 executing initialisation with a db lock for not creating duplicates in a cluster
    public void initThumbnailDefinitions()
    {
        String lockToken = null;
        try
        {
            // Get a lock
            lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
            initThumbnailDefinitionsTransaction();
        }
        catch (LockAcquisitionException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping initialisation from thumbnail definitions (could not get lock): " + e.getMessage());
            }
        }
        finally
        {
            if (lockToken != null)
            {
                try
                {
                    jobLockService.releaseLock(lockToken, LOCK_QNAME);
                }
                catch (LockAcquisitionException e)
                {
                    // Ignore
                }
            }
        }
    }

    private void initThumbnailDefinitionsTransaction()
    {
        RetryingTransactionHelper transactionHelper = transactionService.getRetryingTransactionHelper();
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                for (String thumbnailDefName : thumbnailDefinitions.keySet())
                {
                    final ThumbnailDefinition thumbnailDefinition = thumbnailDefinitions.get(thumbnailDefName);

                    // Built-in thumbnailDefinitions do not provide any non-standard values
                    // for the ThumbnailParentAssociationDetails object. Hence the null
                    RenditionDefinition renditionDef = thumbnailRenditionConvertor.convert(thumbnailDefinition, null);

                    // Thumbnail definitions are saved into the repository as actions
                    renditionService.saveRenditionDefinition(renditionDef);
                }
                return null;
            }
        });
    }

    
    /**
     * Get a list of all the thumbnail definitions
     * 
     * @return collection of thumbnail definitions
     */
    public List<ThumbnailDefinition> getThumbnailDefinitions()
    {
        return new ArrayList<ThumbnailDefinition>(this.thumbnailDefinitions.values());
    }
    
    /**
     * @deprecated use overloaded version with sourceSize parameter.
     */
    public List<ThumbnailDefinition> getThumbnailDefinitions(String mimetype)
    {
        return getThumbnailDefinitions(mimetype, -1);
    }
    
    public List<ThumbnailDefinition> getThumbnailDefinitions(String mimetype, long sourceSize)
    {
        List<ThumbnailDefinitionLimits> thumbnailDefinitionsLimitsForMimetype = this.mimetypeMap.get(mimetype);
        
        if (thumbnailDefinitionsLimitsForMimetype == null)
        {
            boolean foundAtLeastOneTransformer = false;
            thumbnailDefinitionsLimitsForMimetype = new ArrayList<ThumbnailDefinitionLimits>(7);
            
            for (ThumbnailDefinition thumbnailDefinition : this.thumbnailDefinitions.values())
            {
                long maxSourceSizeBytes = getMaxSourceSizeBytes(mimetype, thumbnailDefinition);
                if (maxSourceSizeBytes != 0)
                {
                    thumbnailDefinitionsLimitsForMimetype.add(new ThumbnailDefinitionLimits(thumbnailDefinition, maxSourceSizeBytes));
                    foundAtLeastOneTransformer = true;
                }
            }
            
            // If we have found no transformers for the given MIME type then we do
            // not cache the empty list. We prevent this because we want to allow for
            // transformers only coming online *during* system operation - as opposed
            // to coming online during startup.
            //
            // An example of such a transient transformer would be those that use OpenOffice.org.
            // It is possible that the system might start without OOo-based transformers
            // being available. Therefore we must not cache an empty list for the relevant
            // MIME types - otherwise this class would hide the fact that OOo (soffice) has
            // been launched and that new transformers are available.
            if (foundAtLeastOneTransformer)
            {
                this.mimetypeMap.put(mimetype, thumbnailDefinitionsLimitsForMimetype);
            }
        }
        
        // Only return ThumbnailDefinition for this specific source - may be limited on size.
        List<ThumbnailDefinition> result = new ArrayList<ThumbnailDefinition>(thumbnailDefinitionsLimitsForMimetype.size());
        for (ThumbnailDefinitionLimits thumbnailDefinitionLimits: thumbnailDefinitionsLimitsForMimetype)
        {
            long maxSourceSizeBytes = thumbnailDefinitionLimits.getMaxSourceSizeBytes();
            if (sourceSize <= 0 || maxSourceSizeBytes < 0 || maxSourceSizeBytes >= sourceSize)
            {
                result.add(thumbnailDefinitionLimits.getThumbnailDefinition());
            }
        }
        
        return result;
    }
    
    /**
     * 
     * @param mimetype String
     * @deprecated Use {@link #getThumbnailDefinitions(String)} instead.
     */
    @Deprecated
    public List<ThumbnailDefinition> getThumnailDefintions(String mimetype)
    {
        return this.getThumbnailDefinitions(mimetype);
    }
    
    /**
     * Checks to see if at this moment in time, the specified {@link ThumbnailDefinition}
     *  is able to thumbnail the source mimetype. Typically used with Thumbnail Definitions
     *  retrieved by name, and/or when dealing with transient {@link ContentTransformer}s.
     * @param sourceUrl The URL of the source (optional)
     * @param sourceMimetype The source mimetype
     * @param sourceSize the size (in bytes) of the source. Use -1 if unknown.
     * @param sourceNodeRef which is set in a copy of the thumbnailDefinition transformation options,
     *        so that it may be used by transformers and debug.
     * @param thumbnailDefinition The {@link ThumbnailDefinition} to check for
     */
    public boolean isThumbnailDefinitionAvailable(String sourceUrl, String sourceMimetype, long sourceSize, NodeRef sourceNodeRef, ThumbnailDefinition thumbnailDefinition)
    {
        // Use RenditionService2 if it knows about the definition, otherwise use localTransformServiceRegistry.
        // Needed as disabling local transforms should not disable thumbnails if they can be done remotely.
        boolean supported = false;
        String targetMimetype = thumbnailDefinition.getMimetype();
        RenditionDefinition2 renditionDefinition = getEquivalentRenditionDefinition2(thumbnailDefinition);
        if (renditionDefinition != null)
        {
            Map<String, String> options = renditionDefinition.getTransformOptions();
            String renditionName = renditionDefinition.getRenditionName();
            supported = transformServiceRegistry.isSupported(sourceMimetype, sourceSize, targetMimetype,
                    options, renditionName);
        }
        else
        {
            boolean orig = TransformerDebug.setDebugOutput(false);
            try
            {
                TransformationOptions transformationOptions = thumbnailDefinition.getTransformationOptions();
                String renditionName = thumbnailDefinition.getName();
                Map<String, String> options = converter.getOptions(transformationOptions);
                supported = localTransformServiceRegistry.isSupported(sourceMimetype, sourceSize, targetMimetype,
                        options, renditionName);
            }
            finally
            {
                TransformerDebug.setDebugOutput(orig);
            }
        }
        return supported;
    }
    
    /**
     * Checks to see if at this moment in time, the specified {@link ThumbnailDefinition}
     *  is able to thumbnail the source mimetype. Typically used with Thumbnail Definitions
     *  retrieved by name, and/or when dealing with transient {@link ContentTransformer}s.
     * @param sourceUrl The URL of the source (optional)
     * @param sourceMimeType The source mimetype
     * @param sourceSize the size (in bytes) of the source. Use -1 if unknown.
     * @param thumbnailDefinition The {@link ThumbnailDefinition} to check for
     */
    public boolean isThumbnailDefinitionAvailable(String sourceUrl, String sourceMimeType, long sourceSize, ThumbnailDefinition thumbnailDefinition)
    {
        return isThumbnailDefinitionAvailable(sourceUrl, sourceMimeType, sourceSize, null, thumbnailDefinition);
    }

    /**
     * Returns the maximum source size of any content that may transformed between the supplied
     * sourceMimetype and thumbnailDefinition's targetMimetype using its transformation options.
     * @param sourceMimetype String
     * @param thumbnailDefinition ThumbnailDefinition
     * @return 0 if there are no transformers, -1 if there is no limit or if positive the size in bytes.
     */
    public long getMaxSourceSizeBytes(String sourceMimetype, ThumbnailDefinition thumbnailDefinition)
    {
        // Use RenditionService2 if it knows about the definition, otherwise use localTransformServiceRegistry.
        // Needed as disabling local transforms should not disable thumbnails if they can be done remotely.
        long maxSize = 0;
        String targetMimetype = thumbnailDefinition.getMimetype();
        RenditionDefinition2 renditionDefinition = getEquivalentRenditionDefinition2(thumbnailDefinition);
        if (renditionDefinition != null)
        {
            Map<String, String> options = renditionDefinition.getTransformOptions();
            String renditionName = renditionDefinition.getRenditionName();
            maxSize = transformServiceRegistry.findMaxSize(sourceMimetype, targetMimetype, options, renditionName);
        }
        else
        {
            boolean orig = TransformerDebug.setDebugOutput(false);
            try
            {
                TransformationOptions transformationOptions = thumbnailDefinition.getTransformationOptions();
                String renditionName = thumbnailDefinition.getName();
                Map<String, String> options = converter.getOptions(transformationOptions);
                maxSize = localTransformServiceRegistry.findMaxSize(sourceMimetype, targetMimetype, options, renditionName);
            }
            finally
            {
                TransformerDebug.setDebugOutput(orig);
            }
        }
        return maxSize;
    }

    private RenditionDefinition2 getEquivalentRenditionDefinition2(ThumbnailDefinition thumbnailDefinition)
    {
        String renditionName = thumbnailDefinition.getName();
        RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
        if (renditionDefinition != null)
        {
            String thumbnailTargetMimetype = thumbnailDefinition.getMimetype();
            String renditionTargetMimetype = renditionDefinition.getTargetMimetype();
            if (!renditionTargetMimetype.equals(thumbnailTargetMimetype))
            {
                renditionDefinition = null;
            }
        }
        return renditionDefinition;
    }

    /**
     * Add a thumbnail details
     * 
     * @param thumbnailDetails  thumbnail details
     */
    public void addThumbnailDefinition(ThumbnailDefinition thumbnailDetails)
    {
        String thumbnailName = thumbnailDetails.getName();
        if (thumbnailName == null)
        {
            throw new ThumbnailException("When adding a thumbnail details object make sure the name is set.");
        }
        
        this.thumbnailDefinitions.put(thumbnailName, thumbnailDetails);
    }
    
    /**
     * Get the definition of a named thumbnail
     * 
     * @param  thumbnailName         the thumbnail name
     * @return ThumbnailDetails     the details of the thumbnail
     */
    public ThumbnailDefinition getThumbnailDefinition(String thumbnailName)
    {
        return this.thumbnailDefinitions.get(thumbnailName);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        lifecycle.setApplicationContext(applicationContext);
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationContextEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }
    
    protected boolean redeploy()
    {
        return AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                return ((getThumbnailDefinitions().size() > 0) && (redeployStaticDefsOnStartup || renditionService.loadRenditionDefinitions().size() == 0));
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * This class hooks in to the spring application lifecycle and ensures that any
     * ThumbnailDefinitions injected by spring are converted to RenditionDefinitions
     * and saved.
     */
    protected class RegistryLifecycle extends AbstractLifecycleBean
    {
        /* (non-Javadoc)
         * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
         */
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            if (redeploy())
            {
                long start = System.currentTimeMillis();
                
                // If the database is in read-only mode, then do not persist the thumbnail definitions.
                if (transactionService.isReadOnly())
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("TransactionService is in read-only mode. Therefore no thumbnail definitions have been initialised.");
                    }
                    return;
                }
                
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        initThumbnailDefinitions();
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
                
                if (tenantAdminService.isEnabled())
                {
                    List<Tenant> tenants = tenantAdminService.getAllTenants();
                    for (Tenant tenant : tenants)
                    {
                        AuthenticationUtil.runAs(new RunAsWork<Object>()
                        {
                            public Object doWork() throws Exception
                            {
                                initThumbnailDefinitions();
                                return null;
                            }
                        }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenant.getTenantDomain()));
                    }
                }
                
                if (logger.isInfoEnabled())
                {
                    logger.info("Init'ed thumbnail defs in "+(System.currentTimeMillis()-start)+" ms");
                }
            }
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
         */
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
            // Intentionally empty
        }
    }
    
    /**
     * Links transformer limits (such as maximum size) to a ThumbnailDefinition.
     *
     */
    private class ThumbnailDefinitionLimits
    {
        private ThumbnailDefinition thumbnailDefinition;
        private long maxSourceSizeBytes;

        public ThumbnailDefinitionLimits(ThumbnailDefinition thumbnailDefinition, long maxSourceSizeBytes)
        {
            this.thumbnailDefinition = thumbnailDefinition;
            this.maxSourceSizeBytes = maxSourceSizeBytes;
        }

        public ThumbnailDefinition getThumbnailDefinition()
        {
            return thumbnailDefinition;
        }

        public long getMaxSourceSizeBytes()
        {
            return maxSourceSizeBytes;
        }
    }
}
