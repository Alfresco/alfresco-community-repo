/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
 */
package org.alfresco.license;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.PatchDaoService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.AppliedPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseException;
import org.alfresco.service.license.LicenseService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.helpers.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import de.schlichtherle.license.LicenseContent;
import de.schlichtherle.license.ftp.LicenseManager;
import de.schlichtherle.license.ftp.LicenseParam;


/**
 * Alfresco Enterprise Network implementation of License Service
 *  
 * @author davidc
 */
public class LicenseComponent implements LicenseService
{
    protected AlfrescoLicenseManager licenseManager;
    private LicenseDescriptor licenseDescriptor = null;
    
    // dependencies
    private TransactionComponent transactionComponent;
    private ImporterBootstrap systemBootstrap;
    private SearchService searchService;
    private NamespaceService namespaceService;
    protected NodeService nodeService;
    protected ContentService contentService;
    private Scheduler verifyScheduler = null;
    private boolean failed = false;
        
    // logger
    private static final Log logger = LogFactory.getLog(DescriptorService.class);
    private static final Log loggerInternal = LogFactory.getLog(LicenseComponent.class);

    
    /**
     * Construct
     * 
     * @param context  application context
     */
    public LicenseComponent(ApplicationContext context)
    {
        transactionComponent = (TransactionComponent)context.getBean("transactionComponent");
        systemBootstrap = (ImporterBootstrap)context.getBean("systemBootstrap");
        nodeService = (NodeService)context.getBean("nodeService");
        searchService = (SearchService)context.getBean("searchService");
        contentService = (ContentService)context.getBean("contentService");
        namespaceService = (NamespaceService)context.getBean(ServiceRegistry.NAMESPACE_SERVICE.getLocalName());
        
        // construct license manager
        boolean trialEligibility = getTrialEligibility(context);
        licenseManager = new AlfrescoLicenseManager(new AlfrescoLicenseParam(trialEligibility));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseService#verifyLicense()
     */
    public void verifyLicense()
    {
        // check to see if there's a license to install
        File licenseFile = getLicenseFile();
        if (licenseFile != null)
        {
            if (logger.isInfoEnabled())
                logger.info("Alfresco license: Installing license file " + licenseFile.getName());
            
            try
            {
                LicenseContent licenseContent = licenseManager.install(licenseFile);
                renameLicenseFile(licenseFile);
                licenseDescriptor = new LicenseContentDescriptor(licenseContent);
            }
            catch(Exception e)
            {
                throw new LicenseException("Failed to install license file " + licenseFile.getName(), e);
            }
        }

        // verify existing license
        if (licenseDescriptor == null)
        {
            verify();
            
            // construct scheduler for period license verify
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            try
            {
                verifyScheduler = schedulerFactory.getScheduler();
                JobDetail jobDetail = new JobDetail("vlj", Scheduler.DEFAULT_GROUP, VerifyLicenseJob.class);
                jobDetail.getJobDataMap().put("licenseComponent", this);
                Trigger trigger = TriggerUtils.makeHourlyTrigger();
                trigger.setStartTime(new Date(System.currentTimeMillis() + (60L * 1000L)));  // one minute from now
                trigger.setName("vlt");
                trigger.setGroup(Scheduler.DEFAULT_GROUP);
                verifyScheduler.scheduleJob(jobDetail, trigger);    
                verifyScheduler.start();
            }
            catch(SchedulerException e)
            {
                throw new LicenseException("Failed to initialise License Component");
            }
        }
    }

    /**
     * Verify License
     */
    protected void verify()
    {
        // note: if a license hasn't been already been installed then install 
        //       a free trial period
        TransactionWork<LicenseDescriptor> verifyLicense = new TransactionUtil.TransactionWork<LicenseDescriptor>()
        {
            public LicenseDescriptor doWork()
            {
                LicenseDescriptor descriptor = null;
                try
                {
                    LicenseContent licenseContent = licenseManager.verify();
                    descriptor = new LicenseContentDescriptor(licenseContent);
                }
                catch(Exception e)
                {
                    // handle license failure case
                    licenseFailed(e);
                }
                return descriptor;
            }
        };
        licenseDescriptor = TransactionUtil.executeInUserTransaction(transactionComponent, verifyLicense);
    }
    
    /**
     * Handle case where license is found to be invalid
     */
    protected void licenseFailed(Exception e)
        throws LicenseException
    {
        if (!failed)
        {
            // Mark transactions as read-only
            transactionComponent.setAllowWrite(false);
    
            if (logger.isWarnEnabled())
            {
                logger.warn("Alfresco license: Failed to verify license - Invalid License!");
                logger.warn("Alfresco license: Restricted Alfresco Repository to read-only capability");
            }
            if (loggerInternal.isDebugEnabled())
            {
                loggerInternal.debug("Alfresco license: Failed due to " + e.toString());
            }
            failed = true;
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.license.LicenseService#getLicense()
     */
    public LicenseDescriptor getLicense()
    {
        return licenseDescriptor;
    }
    
    /**
     * Determine if a license file is to be installed
     * 
     * @return  the license file (or null, if one is not to be installed)
     */
    private File getLicenseFile()
    {
        File licenseFile = null;
        
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try
        {
            Resource[] resources = resolver.getResources("classpath*:/alfresco/extension/license/*.lic");
            if (resources.length > 1)
            {
                String resMsg = "";
                for (Resource resource : resources)
                {
                    resMsg += "[" + resource.getURL().toExternalForm() + "] ";
                }
                throw new LicenseException("Found more than one license file to install. The licenses found are: " + resMsg);
            }
            if (resources.length > 0)
            {
                licenseFile = resources[0].getFile();
            }
        }
        catch (IOException e)
        {
            // Note: Ignore: license not found
        }
        
        return licenseFile;
    }
    
    /**
     * Rename installed license file
     *  
     * @param licenseFile  license file to rename
     */
    private void renameLicenseFile(File licenseFile)
    {
        File dest = new File(licenseFile.getAbsolutePath() + ".installed");
        boolean success = false; 
        try
        {
            success = licenseFile.renameTo(dest);
        }
        catch(Exception e)
        {
        }
        
        if (!success)
        {
            if (logger.isWarnEnabled())
                logger.warn("Alfresco license: Failed to rename installed license file " + licenseFile.getName() + " to " + dest.getName());
        }
    }
    
    /**
     * Determine if eligible for trial license creation
     *
     * @param context  application context
     * @return  true => trial license may be created
     */
    private boolean getTrialEligibility(ApplicationContext context)
    {
        // from clean  (open)
        // from clean  (enterprise)
        // ==> systemBootstrap == true 
        
        // upgrade from 1.2.1 open to 1.2.1 enterprise
        // ==> patch = true, schema >= 12, versionEdition = open
        
        // upgrade from 1.2.1 open to 1.3.0 enterprise
        // ==>  patch = true, schema >= 12, versionEdition = open
        
        // upgrade from 1.2.1 enterprise to 1.3.0 enterprise
        // ==>  patch = true, schema >= 12, versionEdition = license
        
        // upgrade from 1.2 open to 1.2.1+ enterprise
        // ==> patch = false, schema < 12, versionEdition = null
        
        // upgrade from 1.2 enterprise to 1.2.1+ enterprise
        // ==> patch = false, schema < 12, versionEdition = null
        

        // first determine if the system store has been bootstrapped in this startup sequence
        // if so, a trial license may be created
        boolean trialEligibility = systemBootstrap.hasPerformedBootstrap();

        if (loggerInternal.isDebugEnabled())
            loggerInternal.debug("Alfresco license: System store bootstrapped: " + trialEligibility);
        
        // if not, then this could be a pre-installed repository that has yet to be patched with a license
        if (!trialEligibility)
        {
            NodeRef descriptorRef = getDescriptor();
            if (descriptorRef != null)
            {
                PatchDaoService patchDao = (PatchDaoService)context.getBean("patchDaoComponent");
                AppliedPatch patch = patchDao.getAppliedPatch("patch.descriptorUpdate");
                
                // versionEdition = open
                //  patch = true, schema >= 12
                
                // versionEdition = null
                //  patch = false, schema < 12
                
                // versionEdition = license
                //  not eligible
                
                int schema = (Integer)nodeService.getProperty(descriptorRef, ContentModel.PROP_SYS_VERSION_SCHEMA);
                Serializable value = nodeService.getProperty(descriptorRef, ContentModel.PROP_SYS_VERSION_EDITION);
                
                if (loggerInternal.isDebugEnabled())
                {
                    loggerInternal.debug("Alfresco license: patch applied: " + (patch != null));
                    loggerInternal.debug("Alfresco license: schema: " + schema);
                    loggerInternal.debug("Alfresco license: edition: " + value);
                }
                
                if (value == null)
                {
                    trialEligibility = (patch == null) && schema < 12;
                }
                else if (value instanceof Collection)
                {
                    Collection editions = (Collection)value;
                    Object editionsValue = (editions.size() > 0) ? editions.iterator().next() : null;
                    String edition = (editionsValue instanceof String) ? (String)editionsValue : "";
                    trialEligibility = (patch != null) && schema >=12 && edition.equals("Community Network");
                }
            }
        }
        
        if (loggerInternal.isDebugEnabled())
            loggerInternal.debug("Alfresco license: trial eligibility: " + trialEligibility);
        
        return trialEligibility;
    }

    /**
     * Get System Store Descriptor
     * 
     * @return  node reference of system store descriptor
     */
    protected NodeRef getDescriptor()
    {
        StoreRef storeRef = systemBootstrap.getStoreRef();
        List<NodeRef> nodeRefs = null;
        
        if (nodeService.exists(storeRef))
        {
            Properties systemProperties = systemBootstrap.getConfiguration();
            String path = systemProperties.getProperty("system.descriptor.current.childname");
            String searchPath = "/" + path;
            NodeRef rootNodeRef = nodeService.getRootNode(storeRef);
            nodeRefs = searchService.selectNodes(rootNodeRef, searchPath, null, namespaceService, false);
        }
        
        return (nodeRefs == null || nodeRefs.size() == 0) ? null : nodeRefs.get(0);
    }

    
    /**
     * Job for period license verification
     * 
     * @author davidc
     */
    public static class VerifyLicenseJob implements Job
    {
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            LicenseComponent license = (LicenseComponent)context.getJobDetail().getJobDataMap().get("licenseComponent");
            license.verify();
        }
    }
    
    
    /**
     * Alfresco implementation of License Manager
     *
     * Note: Stores verified license files in Alfresco Repository
     * 
     * @author davidc
     */
    public class AlfrescoLicenseManager extends LicenseManager
    {
        /**
         * Construct
         * 
         * @param context  application context
         * @param param  license parameters
         */
        public AlfrescoLicenseManager(LicenseParam param)
        {
            super(param);
        }

        /* (non-Javadoc)
         * @see de.schlichtherle.license.LicenseManager#getLicenseKey()
         */
        protected byte[] getLicenseKey()
        {
            byte[] key = null;

            try
            {
                NodeRef descriptorRef = getDescriptor();
                if (descriptorRef == null)
                {
                    throw new LicenseException("Failed to find system descriptor");
                }
                ContentReader reader = contentService.getReader(descriptorRef, ContentModel.PROP_SYS_VERSION_EDITION);
                if (reader != null && reader.exists())
                {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    reader.getContent(os);
                    key = os.toByteArray();
                }
            }
            catch(Exception e)
            {
                throw new LicenseException("Failed to load license", e);
            }
            return key;
        }

        /* (non-Javadoc)
         * @see de.schlichtherle.license.LicenseManager#setLicenseKey(byte[])
         */
        protected synchronized void setLicenseKey(final byte[] key)
        {
            try
            {
                NodeRef descriptorRef = getDescriptor();
                if (descriptorRef == null)
                {
                    throw new LicenseException("Failed to find system descriptor");
                }
                if (key == null)
                {
                    nodeService.setProperty(descriptorRef, ContentModel.PROP_SYS_VERSION_EDITION, null);
                }
                else
                {
                    ContentWriter writer = contentService.getWriter(descriptorRef, ContentModel.PROP_SYS_VERSION_EDITION, true);
                    InputStream is = new ByteArrayInputStream(key);
                    writer.setMimetype(MimetypeMap.MIMETYPE_BINARY);
                    writer.putContent(is);
                }
            }
            catch(Exception e)
            {
                throw new LicenseException("Failed to save license", e);
            }
        }        

    }    
}
