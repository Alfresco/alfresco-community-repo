/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.descriptor;

import java.io.IOException;
import java.util.Properties;

import org.alfresco.repo.descriptor.DescriptorServiceImpl.BaseDescriptor;
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
        this.serverProperties.load(descriptorResource.getInputStream());
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorPersistence#getDescriptor()
     */
    public Descriptor getDescriptor()
    {
        return new ServerDescriptor();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorPersistence#getLicenseKey()
     */
    public byte[] getLicenseKey()
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.service.descriptor.DescriptorPersistence#updateDescriptor(org.alfresco.service.descriptor.Descriptor
     * )
     */
    public Descriptor updateDescriptor(final Descriptor serverDescriptor)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.descriptor.DescriptorPersistence#updateLicenseKey(byte[])
     */
    public void updateLicenseKey(final byte[] key)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Server Descriptor whose meta-data is retrieved from run-time environment.
     */
    private class ServerDescriptor extends BaseDescriptor
    {
        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getId()
         */
        public String getId()
        {
            return "<Unknown";
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getName()
         */
        public String getName()
        {
            return ServerDescriptorDAOImpl.this.repositoryName == null ? "<Unknown>"
                    : ServerDescriptorDAOImpl.this.repositoryName;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMajor()
         */
        public String getVersionMajor()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.major");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionMinor()
         */
        public String getVersionMinor()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.minor");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionRevision()
         */
        public String getVersionRevision()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.revision");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionLabel()
         */
        public String getVersionLabel()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.label");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getVersionBuild()
         */
        public String getVersionBuild()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.build");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getEdition()
         */
        public String getEdition()
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.edition");
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getSchema()
         */
        public int getSchema()
        {
            return getSchema(ServerDescriptorDAOImpl.this.serverProperties.getProperty("version.schema"));
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptorKeys()
         */
        public String[] getDescriptorKeys()
        {
            final String[] keys = new String[ServerDescriptorDAOImpl.this.serverProperties.size()];
            ServerDescriptorDAOImpl.this.serverProperties.keySet().toArray(keys);
            return keys;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.descriptor.Descriptor#getDescriptor(java.lang.String)
         */
        public String getDescriptor(final String key)
        {
            return ServerDescriptorDAOImpl.this.serverProperties.getProperty(key, "");
        }
    }
}