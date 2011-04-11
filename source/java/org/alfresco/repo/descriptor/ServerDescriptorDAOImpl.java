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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.alfresco.repo.descriptor.DescriptorServiceImpl.BaseDescriptor;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.descriptor.Descriptor;
import org.springframework.core.io.Resource;

/**
 * Manages retrieval of the server Descriptor from a read-only resource file.
 */
public class ServerDescriptorDAOImpl implements DescriptorDAO
{

    /** The repository name. */
    private String repositoryName;

    /** The server properties. */
    protected Properties serverProperties;

    /**
     * Sets the repository properties from a resource file.
     * 
     * @param repositoryName
     *            the repository name
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setRepositoryName(final String repositoryName) throws IOException
    {
        this.repositoryName = repositoryName;
    }

    /**
     * Sets the server descriptor from a resource file.
     * 
     * @param descriptorResource
     *            resource containing server descriptor meta-data
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void setResource(final Resource descriptorResource) throws IOException
    {
        this.serverProperties = new Properties();
        InputStream is = descriptorResource.getInputStream();
        try
        {
            this.serverProperties.load(is);
        }
        finally
        {
            if (is != null) try { is.close(); } catch (IOException e) {}
        }
    }

    public Descriptor getDescriptor()
    {
        return new ServerDescriptor();
    }

    public byte[] getLicenseKey()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws UnsupportedOperationException
     */
    public Descriptor updateDescriptor(final Descriptor serverDescriptor, LicenseMode licenseMode)
    {
        throw new UnsupportedOperationException();
    }

    public void updateLicenseKey(final byte[] key)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Server Descriptor whose meta-data is retrieved from run-time environment.
     */
    private class ServerDescriptor extends BaseDescriptor
    {
        public String getId()
        {
            return "Unknown";
        }

        public String getName()
        {
            return ServerDescriptorDAOImpl.this.repositoryName == null ? "<Unknown>"
                    : ServerDescriptorDAOImpl.this.repositoryName;
        }

        public String getVersionMajor()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.major");
        }

        public String getVersionMinor()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.minor");
        }

        public String getVersionRevision()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.revision");
        }

        public String getVersionLabel()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.label");
        }

        public String getVersionBuild()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.build");
        }

        public String getEdition()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.edition");
        }

        public int getSchema()
        {
            return getSchema(ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.schema"));
        }

        public String[] getDescriptorKeys()
        {
            final String[] keys = new String[ServerDescriptorDAOImpl.this.serverProperties.size()];
            ServerDescriptorDAOImpl.this.serverProperties.keySet().toArray(keys);
            return keys;
        }

        public String getDescriptor(final String key)
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty(key, "");
        }

        @Override
        public LicenseMode getLicenseMode()
        {
            return LicenseMode.UNKNOWN;
        }
    }
}