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
package org.alfresco.repo.descriptor;

import java.lang.reflect.Constructor;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.alfresco.service.license.LicenseException;
import org.alfresco.service.license.LicenseService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.util.VersionNumber;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Implementation of Descriptor Service.
 * 
 * @author David Caruana
 */
public class DescriptorServiceImpl extends AbstractLifecycleBean implements DescriptorService, InitializingBean
{
    /** The server descriptor DAO. */
    private DescriptorDAO serverDescriptorDAO;

    /** The current repo descriptor DAO. */
    private DescriptorDAO currentRepoDescriptorDAO;

    /** The installed repo descriptor DAO. */
    private DescriptorDAO installedRepoDescriptorDAO;

    /** The transaction service. */
    private TransactionService transactionService;

    /** The license service. */
    private LicenseService licenseService;

    /** The heart beat service. */
    @SuppressWarnings("unused")
    private Object heartBeat;

    /** The server descriptor. */
    private Descriptor serverDescriptor;

    /** The current repo descriptor. */
    private Descriptor currentRepoDescriptor;

    /** The installed repo descriptor. */
    private Descriptor installedRepoDescriptor;

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

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorService#getDescriptor()
     */
    public Descriptor getServerDescriptor()
    {
        return this.serverDescriptor;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorService#getCurrentRepositoryDescriptor()
     */
    public Descriptor getCurrentRepositoryDescriptor()
    {
        return this.currentRepoDescriptor;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorService#getRepositoryDescriptor()
     */
    public Descriptor getInstalledRepositoryDescriptor()
    {
        return this.installedRepoDescriptor;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorService#getLicenseDescriptor()
     */
    public LicenseDescriptor getLicenseDescriptor()
    {
        return this.licenseService == null ? null : this.licenseService.getLicense();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                final boolean initialiseHeartBeat;

                // Initialize license service (if installed)
                DescriptorServiceImpl.this.licenseService = DescriptorServiceImpl.this.transactionService
                        .getRetryingTransactionHelper().doInTransaction(
                                new RetryingTransactionCallback<LicenseService>()
                                {
                                    public LicenseService execute()
                                    {
                                        return (LicenseService) constructSpecialService("org.alfresco.enterprise.license.LicenseComponent");
                                    }
                                }, DescriptorServiceImpl.this.transactionService.isReadOnly(), false);
                if (DescriptorServiceImpl.this.licenseService == null)
                {
                    DescriptorServiceImpl.this.licenseService = new NOOPLicenseService();
                    initialiseHeartBeat = true;
                }
                else
                {
                    initialiseHeartBeat = false;
                }

                // Make the license service available through the application context as a singleton for other beans
                // that need it (e.g. the HeartBeat).
                ApplicationContext applicationContext = getApplicationContext();
                if (applicationContext instanceof ConfigurableApplicationContext)
                {
                    ((ConfigurableApplicationContext) applicationContext).getBeanFactory().registerSingleton(
                            "licenseService", DescriptorServiceImpl.this.licenseService);
                }

                DescriptorServiceImpl.this.installedRepoDescriptor = DescriptorServiceImpl.this.transactionService
                        .getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Descriptor>()
                        {
                            public Descriptor execute() throws ClassNotFoundException
                            {

                                // verify license, but only if license component is installed
                                try
                                {
                                    DescriptorServiceImpl.this.licenseService.verifyLicense();
                                    LicenseDescriptor l = DescriptorServiceImpl.this.licenseService.getLicense();
                                    // Initialize the heartbeat unless it is disabled by the license
                                    if (initialiseHeartBeat || l == null || !l.isHeartBeatDisabled())
                                    {
                                        DescriptorServiceImpl.this.heartBeat = constructSpecialService("org.alfresco.enterprise.heartbeat.HeartBeat");
                                    }
                                }
                                catch (LicenseException e)
                                {
                                    // Initialize heart beat anyway
                                    DescriptorServiceImpl.this.heartBeat = constructSpecialService("org.alfresco.enterprise.heartbeat.HeartBeat");
                                    throw e;
                                }

                                // persist the server descriptor values
                                DescriptorServiceImpl.this.currentRepoDescriptor = DescriptorServiceImpl.this.currentRepoDescriptorDAO
                                        .updateDescriptor(DescriptorServiceImpl.this.serverDescriptor);

                                // create the installed descriptor
                                Descriptor installed = DescriptorServiceImpl.this.installedRepoDescriptorDAO
                                        .getDescriptor();
                                return installed == null ? new UnknownDescriptor() : installed;
                            }
                        }, DescriptorServiceImpl.this.transactionService.isReadOnly(), false);
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        ((ApplicationContext) event.getSource()).publishEvent(new DescriptorServiceAvailableEvent(this));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
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
     * Constructs a special service whose dependencies cannot or should not be injected declaratively. Examples include
     * the license component and heartbeat service that are intentionally left unconfigurable.
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
            Constructor<?> constructor = componentClass.getConstructor(new Class[]
            {
                ApplicationContext.class
            });
            return constructor.newInstance(new Object[]
            {
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
        /*
         * (non-Javadoc)
         * @see org.alfresco.service.license.LicenseService#verifyLicense(org.alfresco.service.descriptor.Descriptor,
         * org.alfresco.service.descriptor.DescriptorDAO)
         */
        public void verifyLicense() throws LicenseException
        {
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.license.LicenseService#isLicenseValid()
         */
        public boolean isLicenseValid()
        {
            return true;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.license.LicenseService#getLicense()
         */
        public LicenseDescriptor getLicense() throws LicenseException
        {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.license.LicenseService#shutdown()
         */
        public void shutdown()
        {
        }

    }

    /**
     * Unknown descriptor.
     * 
     * @author David Caruana
     */
    private class UnknownDescriptor implements Descriptor
    {
        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getId()
         */
        public String getId()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getName()
         */
        public String getName()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMajor()
         */
        public String getVersionMajor()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMinor()
         */
        public String getVersionMinor()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionRevision()
         */
        public String getVersionRevision()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionLabel()
         */
        public String getVersionLabel()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionBuild()
         */
        public String getVersionBuild()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionNumber()
         */
        public VersionNumber getVersionNumber()
        {
            return new VersionNumber("1.0.0");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersion()
         */
        public String getVersion()
        {
            return "Unknown (pre 1.0.0 RC2)";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getEdition()
         */
        public String getEdition()
        {
            return "Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getSchema()
         */
        public int getSchema()
        {
            return 0;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptorKeys()
         */
        public String[] getDescriptorKeys()
        {
            return new String[0];
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptor(java.lang.String)
         */
        public String getDescriptor(String key)
        {
            return null;
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

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionNumber()
         */
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

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersion()
         */
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

}
