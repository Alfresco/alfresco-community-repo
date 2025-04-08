/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.descriptor;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.usage.RepoUsageComponent;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.cmr.admin.RepoUsage.UsageType;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseException;
import org.alfresco.service.license.LicenseService;
import org.alfresco.service.license.LicenseService.LicenseChangeHandler;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.VersionNumber;

/**
 * Implementation of Descriptor Service.
 * 
 * @author David Caruana
 */
public class DescriptorServiceImpl extends AbstractLifecycleBean
        implements DescriptorService, InitializingBean, LicenseChangeHandler
{
    private DescriptorDAO serverDescriptorDAO;
    private DescriptorDAO currentRepoDescriptorDAO;
    private DescriptorDAO installedRepoDescriptorDAO;

    private TransactionService transactionService;
    private LicenseService licenseService;
    private RepoUsageComponent repoUsageComponent;
    private HBDataCollectorService hbDataCollectorService;
    @SuppressWarnings("unused")

    private boolean isBootstrapped;

    /**
     * The version of the software
     */
    private Descriptor serverDescriptor;
    private Descriptor currentRepoDescriptor;
    private Descriptor installedRepoDescriptor;

    // License changes must hold a synchronized lock on licenseLock so that these changes can by synchronized on
    // startup (in a single JVM) BUT at the point of setting the currentRepoDescriptor they must also obtain the
    // currentRepoDescriptorLock.writeLock. This is required for the situation where the database change takes a long
    // time or there are reties. This update has been known to clash with authorize user updates which also updates the
    // current user count. To avoid HTTP requests from hanging, when looking up the currentRepoDescriptor they now
    // obtain the currentRepoDescriptorLock.readLock rather than the same synchronized lock. The writeLock is only held
    // for as long as it takes to update the currentRepoDescriptor reference. The database update takes place inside
    // the synchronized lock but more importantly in the database as there may be a cluster. This change was introduced
    // for MNT-18894 where there were approximately 240,000 user and 100,000 group authorities.
    private ReentrantReadWriteLock currentRepoDescriptorLock = new ReentrantReadWriteLock();
    private Object licenseLock = new Object();

    private static final Log logger = LogFactory.getLog(DescriptorServiceImpl.class);

    /**
     * Sets the server descriptor DAO.
     * 
     * @param serverDescriptorDAO
     *            the new server descriptor DAO
     */
    public void setServerDescriptorDAO(DescriptorDAO serverDescriptorDAO)
    {
        this.serverDescriptorDAO = serverDescriptorDAO;
    }

    /**
     * Sets the current repo descriptor DAO.
     * 
     * @param currentRepoDescriptorDAO
     *            the new current repo descriptor DAO
     */
    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    /**
     * Sets the installed repo descriptor DAO.
     * 
     * @param installedRepoDescriptorDAO
     *            the new installed repo descriptor DAO
     */
    public void setInstalledRepoDescriptorDAO(DescriptorDAO installedRepoDescriptorDAO)
    {
        this.installedRepoDescriptorDAO = installedRepoDescriptorDAO;
    }

    /**
     * Sets the transaction service.
     * 
     * @param transactionService
     *            transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setHbDataCollectorService(HBDataCollectorService hbDataCollectorService)
    {
        this.hbDataCollectorService = hbDataCollectorService;
    }

    public void setRepoUsageComponent(RepoUsageComponent repoUsageComponent)
    {
        this.repoUsageComponent = repoUsageComponent;
    }

    @Override
    public Descriptor getServerDescriptor()
    {
        return this.serverDescriptor;
    }

    @Override
    public Descriptor getCurrentRepositoryDescriptor()
    {
        currentRepoDescriptorLock.readLock().lock();
        try
        {
            // this should be updated when the License is verified - but it is possible that no License at all
            // is available and this will be in an known state - do not allow a null value to be returned
            return this.currentRepoDescriptor != null ? this.currentRepoDescriptor : new UnknownDescriptor();
        }
        finally
        {
            currentRepoDescriptorLock.readLock().unlock();
        }
    }

    private void setCurrentRepoDescriptor(Descriptor newRepoDescriptor)
    {
        currentRepoDescriptorLock.writeLock().lock();
        try
        {
            currentRepoDescriptor = newRepoDescriptor;
        }
        finally
        {
            currentRepoDescriptorLock.writeLock().unlock();
        }
    }

    private boolean isCurrentRepoDescriptorIsNull()
    {
        // Should not be needed as all updates take place within "synchronized (licenseLock)" but this is clearer
        currentRepoDescriptorLock.readLock().lock();
        try
        {
            return currentRepoDescriptor == null;
        }
        finally
        {
            currentRepoDescriptorLock.readLock().unlock();
        }
    }

    private boolean currentRepoDescriptorNullOrModeHasChanged(LicenseMode newMode)
    {
        // Should not be needed as all updates take place within "synchronized (licenseLock)" but this is clearer
        currentRepoDescriptorLock.readLock().lock();
        try
        {
            return currentRepoDescriptor == null || newMode != currentRepoDescriptor.getLicenseMode();
        }
        finally
        {
            currentRepoDescriptorLock.readLock().unlock();
        }
    }

    @Override
    public Descriptor getInstalledRepositoryDescriptor()
    {
        if (this.installedRepoDescriptor == null)
        {
            throw new IllegalStateException("Not bootstrapped");
        }
        return this.installedRepoDescriptor;
    }

    @Override
    public LicenseDescriptor getLicenseDescriptor()
    {
        if (this.licenseService == null)
        {
            throw new IllegalStateException("Not bootstrapped");
        }
        return this.licenseService.getLicense();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String loadLicense()
    {
        // Ensure that we force a writable txn for this operation
        final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setForceWritable(true);

        final RetryingTransactionCallback<String> loadCallback = new RetryingTransactionCallback<String>() {
            @Override
            public String execute() throws Throwable
            {
                return licenseService.loadLicense();
            }
        };
        // ... and we have to be 'system' for this, too
        String result = AuthenticationUtil.runAs(new RunAsWork<String>() {
            public String doWork() throws Exception
            {
                return txnHelper.doInTransaction(loadCallback, false, true);
            }
        }, AuthenticationUtil.getSystemUserName());

        if (logger.isDebugEnabled())
        {
            logger.debug("Load license call returning: " + result);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String loadLicense(final InputStream licenseStream)
    {
        // Ensure that we force a writable txn for this operation
        final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setForceWritable(true);

        final RetryingTransactionCallback<String> loadCallback = new RetryingTransactionCallback<String>() {
            @Override
            public String execute() throws Throwable
            {
                return licenseService.loadLicense(licenseStream);
            }
        };
        // ... and we have to be 'system' for this, too
        String result = AuthenticationUtil.runAs(new RunAsWork<String>() {
            public String doWork() throws Exception
            {
                return txnHelper.doInTransaction(loadCallback, false, true);
            }
        }, AuthenticationUtil.getSystemUserName());

        if (logger.isDebugEnabled())
        {
            logger.debug("Load license call returning: " + result);
        }
        return result;
    }

    /**
     * On bootstrap load the special services for LicenseComponent
     * 
     * Also set installedRepoDescriptor and update current
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        AuthenticationUtil.RunAsWork<Void> bootstrapWork = new RunAsWork<Void>() {
            @Override
            public Void doWork() throws Exception
            {
                bootstrap();
                return null;
            }
        };
        AuthenticationUtil.runAs(bootstrapWork, AuthenticationUtil.getSystemUserName());
        isBootstrapped = true;
        // Broadcast that the descriptor service is now available
        ((ApplicationContext) event.getSource()).publishEvent(new DescriptorServiceAvailableEvent(this));
    }

    private void bootstrap()
    {
        logger.debug("onBootstrap");

        // We force write mode
        RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
        helper.setForceWritable(true);

        // create the initial installed descriptor
        RetryingTransactionCallback<Descriptor> getDescriptorCallback = new RetryingTransactionCallback<Descriptor>() {
            public Descriptor execute()
            {
                return installedRepoDescriptorDAO.getDescriptor();
            }
        };
        Descriptor installed = helper.doInTransaction(getDescriptorCallback, false, false);
        if (installed != null)
        {
            installedRepoDescriptor = installed;
        }
        else
        {
            installedRepoDescriptor = new UnknownDescriptor();
        }

        /* Initialize license service if on classpath. If no class exists a dummy license service is used. Make the LicenseService available in the context. */
        licenseService = (LicenseService) constructSpecialService("org.alfresco.enterprise.license.LicenseComponent");
        if (licenseService == null)
        {
            // No license server code - install a dummy license service instead
            licenseService = new NOOPLicenseService();
        }
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext instanceof ConfigurableApplicationContext)
        {
            ((ConfigurableApplicationContext) applicationContext).getBeanFactory().registerSingleton(
                    "licenseService", licenseService);
        }

        // Register HeartBeat with LicenseService
        licenseService.registerOnLicenseChange((LicenseChangeHandler) hbDataCollectorService);

        // Now listen for future license changes
        licenseService.registerOnLicenseChange(this);

        try
        {
            // Verify license has side effect of loading any new licenses
            licenseService.verifyLicense();
        }
        catch (LicenseException e)
        {
            // Swallow Licence Exception Here
            // Don't log error: It'll be reported by other means
        }
        ;
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        if (this.licenseService != null)
        {
            this.licenseService.shutdown();
        }
    }

    /**
     * Initialise Descriptors.
     * 
     * @throws Exception
     *             the exception
     */
    public void afterPropertiesSet() throws Exception
    {
        // initialise server descriptor
        this.serverDescriptor = this.serverDescriptorDAO.getDescriptor();
    }

    /**
     * Constructs a special service whose dependencies cannot or should not be injected declaratively. Examples include the license component and heartbeat service that are intentionally left unconfigurable.
     * 
     * @param className
     *            the class name
     * @return the object
     */
    private Object constructSpecialService(String className)
    {
        try
        {
            Class<?> componentClass = Class.forName(className);
            Constructor<?> constructor = componentClass.getConstructor(new Class[]{
                    ApplicationContext.class
            });
            return constructor.newInstance(new Object[]{
                    getApplicationContext()
            });
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to initialise " + className, e);
        }
    }

    /**
     * Dummy License Service.
     */
    private class NOOPLicenseService implements LicenseService
    {
        /**
         * Ensures that a repository Descriptor is available. This is done here to prevent flip-flopping on the license mode during startup i.e. the Enterprise mode will be set once when the real license is validated.
         */
        public void verifyLicense() throws LicenseException
        {
            synchronized (licenseLock)
            {
                if (isCurrentRepoDescriptorIsNull())
                {
                    final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

                    RetryingTransactionCallback<Void> nopLoadLicense = new RetryingTransactionCallback<Void>() {
                        @Override
                        public Void execute()
                        {
                            Descriptor newRepoDescriptor = currentRepoDescriptorDAO.updateDescriptor(serverDescriptor, LicenseMode.UNKNOWN);
                            setCurrentRepoDescriptor(newRepoDescriptor);
                            return null;
                        }
                    };

                    txnHelper.doInTransaction(nopLoadLicense, false, false);
                }
            }
        }

        /**
         * @return <tt>true</tt> always
         */
        public boolean isLicenseValid()
        {
            return true;
        }

        /**
         * @return <tt>null</tt> always
         */
        public LicenseDescriptor getLicense() throws LicenseException
        {
            return null;
        }

        /**
         * NO NOP
         */
        public void shutdown()
        {}

        @Override
        public void registerOnLicenseChange(LicenseChangeHandler callback)
        {

        }

        @Override
        public String loadLicense()
        {
            return "Done";
        }

        @Override
        public String loadLicense(InputStream licenseStream)
        {
            return loadLicense();
        }

    }

    /**
     * Unknown descriptor.
     * 
     * @author David Caruana
     */
    private class UnknownDescriptor implements Descriptor
    {
        /**
         * @return <b>Unknown</b> always
         */
        public String getId()
        {
            return "Unknown";
        }

        /**
         * @return <b>Unknown</b> always
         */
        public String getName()
        {
            return "Unknown";
        }

        /**
         * @return <b>Unknown</b> always
         */
        public String getVersionMajor()
        {
            return "Unknown";
        }

        /**
         * @return <b>Unknown</b> always
         */
        public String getVersionMinor()
        {
            return "Unknown";
        }

        /**
         * @return <b>Unknown</b> always
         */
        public String getVersionRevision()
        {
            return "Unknown";
        }

        /**
         * @return <b>Unknown</b> always
         */
        public String getVersionLabel()
        {
            return "Unknown";
        }

        /**
         * @return <b>Unknown</b> always
         */
        public String getVersionBuild()
        {
            return "Unknown";
        }

        /**
         * @return <b>0.0.0</b> always
         */
        public VersionNumber getVersionNumber()
        {
            return new VersionNumber("0.0.0");
        }

        /**
         * @return <b>Unknown</b> always
         */
        public String getVersion()
        {
            return "Unknown";
        }

        /**
         * @return <b>Unknown</b> always
         */
        public String getEdition()
        {
            return "Unknown";
        }

        /**
         * @return <b>0</b> always
         */
        public int getSchema()
        {
            return 0;
        }

        /**
         * @return Empty <tt>String[]</tt> always
         */
        public String[] getDescriptorKeys()
        {
            return new String[0];
        }

        /**
         * @return <tt>null</tt> always
         */
        public String getDescriptor(String key)
        {
            return null;
        }

        /**
         * @return <b>Unknown</b> always
         */
        @Override
        public LicenseMode getLicenseMode()
        {
            return LicenseMode.UNKNOWN;
        }
    }

    /**
     * Base class for Descriptor implementations, provides a default getVersion() implementation.
     * 
     * @author gavinc
     */
    public static abstract class BaseDescriptor implements Descriptor
    {

        /** The version number. */
        private VersionNumber versionNumber = null;

        /** The number as a string. */
        private String strVersion = null;

        public VersionNumber getVersionNumber()
        {
            if (this.versionNumber == null)
            {
                StringBuilder version = new StringBuilder();
                version.append(getVersionMajor());
                version.append(".");
                version.append(getVersionMinor());
                version.append(".");
                version.append(getVersionRevision());
                this.versionNumber = new VersionNumber(version.toString());
            }
            return this.versionNumber;
        }

        public String getVersion()
        {
            if (this.strVersion == null)
            {
                StringBuilder version = new StringBuilder(getVersionMajor());
                version.append(".");
                version.append(getVersionMinor());
                version.append(".");
                version.append(getVersionRevision());

                String label = getVersionLabel();
                String build = getVersionBuild();

                boolean hasLabel = label != null && label.length() > 0;
                boolean hasBuild = build != null && build.length() > 0;

                // add opening bracket if either a label or build number is present
                if (hasLabel || hasBuild)
                {
                    version.append(" (");
                }

                // add label if present
                if (hasLabel)
                {
                    version.append(label);
                }

                // add build number is present
                if (hasBuild)
                {
                    // if there is also a label we need a separating space
                    if (hasLabel)
                    {
                        version.append(" ");
                    }

                    version.append(build);
                }

                // add closing bracket if either a label or build number is present
                if (hasLabel || hasBuild)
                {
                    version.append(")");
                }

                this.strVersion = version.toString();
            }
            return this.strVersion;
        }

        /**
         * Returns the int representation of the given schema string.
         * 
         * @param schemaStr
         *            The schema number as a string
         * @return The schema number as an int
         */
        protected int getSchema(String schemaStr)
        {
            if (schemaStr == null)
            {
                return 0;
            }
            try
            {
                int schema = Integer.parseInt(schemaStr);
                if (schema < 0)
                {
                    throw new NumberFormatException();
                }
                return schema;
            }
            catch (NumberFormatException e)
            {
                throw new AlfrescoRuntimeException("Schema must be a positive integer '" + schemaStr + "' is not!");
            }
        }
    }

    @Override
    public void onLicenseChange(final LicenseDescriptor licenseDescriptor)
    {
        synchronized (licenseLock)
        {
            logger.debug("Received changed license descriptor: " + licenseDescriptor);

            RetryingTransactionCallback<RepoUsage> updateLicenseCallback = new RetryingTransactionCallback<RepoUsage>() {
                public RepoUsage execute()
                {
                    // Configure the license restrictions
                    RepoUsage.LicenseMode newMode = licenseDescriptor.getLicenseMode();
                    Long expiryTime = licenseDescriptor.getValidUntil() == null ? null : licenseDescriptor.getValidUntil().getTime();
                    RepoUsage restrictions = new RepoUsage(
                            System.currentTimeMillis(),
                            licenseDescriptor.getMaxUsers(),
                            licenseDescriptor.getMaxDocs(),
                            newMode,
                            expiryTime,
                            false);
                    repoUsageComponent.setRestrictions(restrictions);

                    // Reset usage upon loading the unlimited license
                    if (restrictions.getUsers() == null)
                    {
                        repoUsageComponent.resetUsage(UsageType.USAGE_USERS);
                    }
                    if (restrictions.getDocuments() == null)
                    {
                        repoUsageComponent.resetUsage(UsageType.USAGE_DOCUMENTS);
                    }

                    // persist the server descriptor values in the current repository descriptor
                    if (currentRepoDescriptorNullOrModeHasChanged(newMode))
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Changing license mode in current repo descriptor to: " + newMode);
                        }
                        Descriptor newRepoDescriptor = currentRepoDescriptorDAO.updateDescriptor(
                                serverDescriptor,
                                newMode);
                        setCurrentRepoDescriptor(newRepoDescriptor);
                    }
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("License restrictions updated: " + restrictions);
                    }
                    return null;
                }
            };
            RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
            txnHelper.setForceWritable(true);
            txnHelper.doInTransaction(updateLicenseCallback);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Restrictions are not changed; previous restrictions remain in place.
     */
    @Override
    public void onLicenseFail()
    {
        synchronized (licenseLock)
        {
            // Current restrictions remain in place
            // Make sure that the repo descriptor is updated the first time
            if (isCurrentRepoDescriptorIsNull())
            {
                final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                RetryingTransactionCallback<Void> nopLoadLicense = new RetryingTransactionCallback<Void>() {
                    @Override
                    public Void execute()
                    {
                        Descriptor newRepoDescriptor = currentRepoDescriptorDAO.updateDescriptor(serverDescriptor, LicenseMode.UNKNOWN);
                        setCurrentRepoDescriptor(newRepoDescriptor);
                        return null;
                    }
                };
                txnHelper.setForceWritable(true);
                txnHelper.doInTransaction(nopLoadLicense, false, false);

            }
        }
    }

    @Override
    public boolean isBootstrapped()
    {

        return isBootstrapped;
    }
}
