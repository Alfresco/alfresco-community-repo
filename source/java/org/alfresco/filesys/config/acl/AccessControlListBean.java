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
package org.alfresco.filesys.config.acl;

import java.util.Collections;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.server.auth.acl.ACLParseException;
import org.alfresco.jlan.server.auth.acl.AccessControl;
import org.alfresco.jlan.server.auth.acl.AccessControlList;
import org.alfresco.jlan.server.auth.acl.AccessControlParser;
import org.alfresco.jlan.server.auth.acl.DefaultAccessControlManager;
import org.alfresco.jlan.server.auth.acl.InvalidACLTypeException;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.SecurityConfigSection;

/**
 * Simple description of a JLAN Access Control List that can be configured via JMX or a Spring bean definition. The
 * lists are partitioned by type to aid in the UI mapping.
 */
public class AccessControlListBean
{
    /** The default access level. */
    private int defaultAccessLevel = AccessControl.Default;

    /** The domain access controls. */
    private List<DomainAccessControlBean> domainAccessControls = Collections.emptyList();

    /** The gid access controls. */
    private List<GidAccessControlBean> gidAccessControls = Collections.emptyList();

    /** The ip address access controls. */
    private List<IpAddressAccessControlBean> ipAddressAccessControls = Collections.emptyList();

    /** The protocol access controls. */
    private List<ProtocolAccessControlBean> protocolAccessControls = Collections.emptyList();

    /** The uid access controls. */
    private List<UidAccessControlBean> uidAccessControls = Collections.emptyList();

    /** The user access controls. */
    private List<UserAccessControlBean> userAccessControls = Collections.emptyList();

    /**
     * Gets the default access level.
     * 
     * @return the default access level
     */
    public String getDefaultAccessLevel()
    {
        return AccessControl.asAccessString(defaultAccessLevel);
    }

    /**
     * Sets the default access level.
     * 
     * @param defaultAccessLevel
     *            the new default access level
     * @throws ACLParseException
     */
    public void setDefaultAccessLevel(String defaultAccessLevel) throws ACLParseException
    {
        if (defaultAccessLevel != null && defaultAccessLevel.length() > 0)
        {
            this.defaultAccessLevel = AccessControlParser.parseAccessTypeString(defaultAccessLevel);
        }
    }

    /**
     * Gets the domain access controls.
     * 
     * @return the domain access controls
     */
    public List<DomainAccessControlBean> getDomainAccessControls()
    {
        return this.domainAccessControls;
    }

    /**
     * Sets the domain access controls.
     * 
     * @param domainAccessControls
     *            the new domain access controls
     */
    public void setDomainAccessControls(List<DomainAccessControlBean> domainAccessControls)
    {
        this.domainAccessControls = domainAccessControls;
    }

    /**
     * Gets the gid access controls.
     * 
     * @return the gid access controls
     */
    public List<GidAccessControlBean> getGidAccessControls()
    {
        return this.gidAccessControls;
    }

    /**
     * Sets the gid access controls.
     * 
     * @param gidAccessControls
     *            the new gid access controls
     */
    public void setGidAccessControls(List<GidAccessControlBean> gidAccessControls)
    {
        this.gidAccessControls = gidAccessControls;
    }

    /**
     * Gets the ip address access controls.
     * 
     * @return the ip address access controls
     */
    public List<IpAddressAccessControlBean> getIpAddressAccessControls()
    {
        return this.ipAddressAccessControls;
    }

    /**
     * Sets the ip address access controls.
     * 
     * @param ipAddressAccessControls
     *            the new ip address access controls
     */
    public void setIpAddressAccessControls(List<IpAddressAccessControlBean> ipAddressAccessControls)
    {
        this.ipAddressAccessControls = ipAddressAccessControls;
    }

    /**
     * Gets the protocol access controls.
     * 
     * @return the protocol access controls
     */
    public List<ProtocolAccessControlBean> getProtocolAccessControls()
    {
        return this.protocolAccessControls;
    }

    /**
     * Sets the protocol access controls.
     * 
     * @param protocolAccessControls
     *            the new protocol access controls
     */
    public void setProtocolAccessControls(List<ProtocolAccessControlBean> protocolAccessControls)
    {
        this.protocolAccessControls = protocolAccessControls;
    }

    /**
     * Gets the uid access controls.
     * 
     * @return the uid access controls
     */
    public List<UidAccessControlBean> getUidAccessControls()
    {
        return this.uidAccessControls;
    }

    /**
     * Sets the uid access controls.
     * 
     * @param uidAccessControls
     *            the new uid access controls
     */
    public void setUidAccessControls(List<UidAccessControlBean> uidAccessControls)
    {
        this.uidAccessControls = uidAccessControls;
    }

    /**
     * Gets the user access controls.
     * 
     * @return the user access controls
     */
    public List<UserAccessControlBean> getUserAccessControls()
    {
        return this.userAccessControls;
    }

    /**
     * Sets the user access controls.
     * 
     * @param userAccessControls
     *            the new user access controls
     */
    public void setUserAccessControls(List<UserAccessControlBean> userAccessControls)
    {
        this.userAccessControls = userAccessControls;
    }

    /**
     * Converts the description to a JLAN ACL
     * 
     * @param secConfig
     *            the security config
     * @return the access control list
     * @throws InvalidConfigurationException
     */
    public AccessControlList toAccessControlList(SecurityConfigSection secConfig) throws InvalidConfigurationException
    {
        // Create the access control list

        AccessControlList acls = new AccessControlList();

        // Check if there is a default access level for the ACL group
        if (defaultAccessLevel != AccessControl.Default)
        {
            try
            {
                // Set the default access level for the access control list

                acls.setDefaultAccessLevel(defaultAccessLevel);
            }
            catch (InvalidACLTypeException ex)
            {
                throw new AlfrescoRuntimeException("Default access level error", ex);
            }
        }

        // Create the access controls
        for (AccessControlBean accessControlBean : getDomainAccessControls())
        {
            acls.addControl(accessControlBean.toAccessControl());
        }

        // Create the access controls
        for (AccessControlBean accessControlBean : getGidAccessControls())
        {
            acls.addControl(accessControlBean.toAccessControl());
        }

        // Create the access controls
        for (AccessControlBean accessControlBean : getIpAddressAccessControls())
        {
            acls.addControl(accessControlBean.toAccessControl());
        }

        // Create the access controls
        for (AccessControlBean accessControlBean : getProtocolAccessControls())
        {
            acls.addControl(accessControlBean.toAccessControl());
        }

        // Create the access controls
        for (AccessControlBean accessControlBean : getUidAccessControls())
        {
            acls.addControl(accessControlBean.toAccessControl());
        }

        // Create the access controls
        for (AccessControlBean accessControlBean : getUserAccessControls())
        {
            acls.addControl(accessControlBean.toAccessControl());
        }

        // Check if there are no access control rules but the default access level is set to 'None',
        // this is not allowed as the share would not be accessible or visible.

        if (acls.numberOfControls() == 0)
        {
            if (defaultAccessLevel == AccessControl.Default)
            {
                // No access level or controls set. No need to enforce ACLs
                return null;
            }
            else if (acls.getDefaultAccessLevel() == AccessControl.NoAccess)
            {
                throw new AlfrescoRuntimeException("Empty access control list and default access 'None' not allowed");
            }
        }

        // Check if there is an access control manager configured
        if (secConfig.getAccessControlManager() == null)
        {
            secConfig.setAccessControlManager(new DefaultAccessControlManager());
        }

        // Return the access control list
        return acls;
    }

}
