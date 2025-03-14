/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.filesys.config;

import org.alfresco.filesys.config.acl.AccessControlListBean;
import org.alfresco.jlan.server.core.ShareMapper;

// TODO: Auto-generated Javadoc
/**
 * The Class SecurityConfigBean.
 * 
 * @author dward
 */
public class SecurityConfigBean
{
    /** The global access control. */
    private AccessControlListBean globalAccessControl;

    /** The jce provider. */
    private String jceProvider;

    /** The share mapper. */
    private ShareMapper shareMapper;

    /**
     * Gets the global access control.
     * 
     * @return the global access control
     */
    public AccessControlListBean getGlobalAccessControl()
    {
        return globalAccessControl;
    }

    /**
     * Sets the global access control.
     * 
     * @param globalAccessControl
     *            the new global access control
     */
    public void setGlobalAccessControl(AccessControlListBean globalAccessControl)
    {
        this.globalAccessControl = globalAccessControl;
    }

    /**
     * Gets the jCE provider.
     * 
     * @return the jCE provider
     */
    public String getJCEProvider()
    {
        return jceProvider;
    }

    /**
     * Sets the jCE provider.
     * 
     * @param provider
     *            the new jCE provider
     */
    public void setJCEProvider(String provider)
    {
        jceProvider = provider;
    }

    /**
     * Gets the share mapper.
     * 
     * @return the share mapper
     */
    public ShareMapper getShareMapper()
    {
        return shareMapper;
    }

    /**
     * Sets the share mapper.
     * 
     * @param shareMapper
     *            the new share mapper
     */
    public void setShareMapper(ShareMapper shareMapper)
    {
        this.shareMapper = shareMapper;
    }
}
