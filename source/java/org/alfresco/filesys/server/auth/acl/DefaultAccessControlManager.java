/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.filesys.server.auth.acl;

import java.util.Enumeration;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default Access Control Manager Class
 * <p>
 * Default access control manager implementation.
 * 
 * @author Gary K. Spencer
 */
public class DefaultAccessControlManager implements AccessControlManager
{

    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");

    // Access control factory

    private AccessControlFactory m_factory;

    // Debug enable flag

    private boolean m_debug;

    /**
     * Class constructor
     */
    public DefaultAccessControlManager()
    {

        // Create the access control factory

        m_factory = new AccessControlFactory();
    }

    /**
     * Check if the session has access to the shared device.
     * 
     * @param sess SrvSession
     * @param share SharedDevice
     * @return int
     */
    public int checkAccessControl(SrvSession sess, SharedDevice share)
    {

        // Check if the shared device has any access control configured

        if (share.hasAccessControls() == false)
        {

            // DEBUG

            if (logger.isDebugEnabled() && hasDebug())
                logger.debug("Check access control for " + share.getName() + ", no ACLs");

            // Allow full access to the share

            return AccessControl.ReadWrite;
        }

        // Process the access control list

        AccessControlList acls = share.getAccessControls();
        int access = AccessControl.Default;

        // DEBUG

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("Check access control for " + share.getName() + ", ACLs=" + acls.numberOfControls());

        for (int i = 0; i < acls.numberOfControls(); i++)
        {

            // Get the current access control and run

            AccessControl acl = acls.getControlAt(i);
            int curAccess = acl.allowsAccess(sess, share, this);

            // Debug

            if (logger.isDebugEnabled() && hasDebug())
                logger.debug("  Check access ACL=" + acl + ", access=" + AccessControl.asAccessString(curAccess));

            // Update the allowed access

            if (curAccess != AccessControl.Default)
                access = curAccess;
        }

        // Check if the default access level is still selected, if so then get the default level
        // from the
        // access control list

        if (access == AccessControl.Default)
        {

            // Use the default access level

            access = acls.getDefaultAccessLevel();

            // Debug

            if (logger.isDebugEnabled() && hasDebug())
                logger.debug("Access defaulted=" + AccessControl.asAccessString(access) + ", share=" + share);
        }
        else if (logger.isDebugEnabled() && hasDebug())
            logger.debug("Access allowed=" + AccessControl.asAccessString(access) + ", share=" + share);

        // Return the access type

        return access;
    }

    /**
     * Filter the list of shared devices to return a list that contains only the shares that are
     * visible or accessible by the session.
     * 
     * @param sess SrvSession
     * @param shares SharedDeviceList
     * @return SharedDeviceList
     */
    public SharedDeviceList filterShareList(SrvSession sess, SharedDeviceList shares)
    {

        // Check if the share list is valid or empty

        if (shares == null || shares.numberOfShares() == 0)
            return shares;

        // Debug

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("Filter share list for " + sess + ", shares=" + shares);

        // For each share in the list check the access, remove any shares that the session does not
        // have access to.

        SharedDeviceList filterList = new SharedDeviceList();
        Enumeration<SharedDevice> enm = shares.enumerateShares();

        while (enm.hasMoreElements())
        {

            // Get the current share

            SharedDevice share = enm.nextElement();

            // Check if the share has any access controls

            if (share.hasAccessControls())
            {

                // Check if the session has access to this share

                int access = checkAccessControl(sess, share);
                if (access != AccessControl.NoAccess)
                    filterList.addShare(share);
            }
            else
            {

                // Add the share to the filtered list

                filterList.addShare(share);
            }
        }

        // Debug

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("Filtered share list " + filterList);

        // Return the filtered share list

        return filterList;
    }

    /**
     * Initialize the access control manager
     * 
     * @param config ServerConfiguration
     * @param params ConfigElement
     */
    public void initialize(ServerConfiguration config, ConfigElement params)
    {

        // Check if debug output is enabled

        if (params != null && params.getChild("debug") != null)
            setDebug(true);

        // Add the default access control types

        addAccessControlType(new UserAccessControlParser());
        addAccessControlType(new ProtocolAccessControlParser());
        addAccessControlType(new DomainAccessControlParser());
        addAccessControlType(new IpAddressAccessControlParser());
    }

    /**
     * Create an access control.
     * 
     * @param type String
     * @param params ConfigElement
     * @return AccessControl
     * @throws ACLParseException
     * @throws InvalidACLTypeException
     */
    public AccessControl createAccessControl(String type, ConfigElement params) throws ACLParseException,
            InvalidACLTypeException
    {

        // Use the access control factory to create the access control instance

        return m_factory.createAccessControl(type, params);
    }

    /**
     * Add an access control parser to the list of available access control types.
     * 
     * @param parser AccessControlParser
     */
    public void addAccessControlType(AccessControlParser parser)
    {

        // Debug

        if (logger.isDebugEnabled() && hasDebug())
            logger.debug("AccessControlManager Add rule type " + parser.getType());

        // Add the new access control type to the factory

        m_factory.addParser(parser);
    }

    /**
     * Determine if debug output is enabled
     * 
     * @return boolean
     */
    public final boolean hasDebug()
    {
        return m_debug;
    }

    /**
     * Enable/disable debug output
     * 
     * @param dbg boolean
     */
    public final void setDebug(boolean dbg)
    {
        m_debug = dbg;
    }
}
