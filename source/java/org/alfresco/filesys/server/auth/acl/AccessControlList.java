/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.auth.acl;

import java.util.Vector;

/**
 * Access Control List Class
 * <p>
 * Contains a list of access controls for a shared filesystem.
 */
public class AccessControlList
{

    // Access control list

    private Vector<AccessControl> m_list;

    // Default access level applied when rules return a default status

    private int m_defaultAccess = AccessControl.ReadWrite;

    /**
     * Create an access control list.
     */
    public AccessControlList()
    {
        m_list = new Vector<AccessControl>();
    }

    /**
     * Get the default access level
     * 
     * @return int
     */
    public final int getDefaultAccessLevel()
    {
        return m_defaultAccess;
    }

    /**
     * Set the default access level
     * 
     * @param level int
     * @exception InvalidACLTypeException If the access level is invalid
     */
    public final void setDefaultAccessLevel(int level) throws InvalidACLTypeException
    {

        // Check the default access level

        if (level < AccessControl.NoAccess || level > AccessControl.MaxLevel)
            throw new InvalidACLTypeException();

        // Set the default access level for the access control list

        m_defaultAccess = level;
    }

    /**
     * Add an access control to the list
     * 
     * @param accCtrl AccessControl
     */
    public final void addControl(AccessControl accCtrl)
    {

        // Add the access control to the list

        m_list.add(accCtrl);
    }

    /**
     * Return the specified access control
     * 
     * @param idx int
     * @return AccessControl
     */
    public final AccessControl getControlAt(int idx)
    {
        if (idx < 0 || idx >= m_list.size())
            return null;
        return m_list.get(idx);
    }

    /**
     * Return the number of access controls in the list
     * 
     * @return int
     */
    public final int numberOfControls()
    {
        return m_list.size();
    }

    /**
     * Remove all access controls from the list
     */
    public final void removeAllControls()
    {
        m_list.removeAllElements();
    }

    /**
     * Remove the specified access control from the list.
     * 
     * @param idx int
     * @return AccessControl
     */
    public final AccessControl removeControl(int idx)
    {
        if (idx < 0 || idx >= m_list.size())
            return null;
        return m_list.remove(idx);
    }

    /**
     * Return the access control list as a string.
     * 
     * @return java.lang.String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(m_list.size());
        str.append(":");

        str.append(":");
        str.append(AccessControl.asAccessString(getDefaultAccessLevel()));
        str.append(":");

        for (int i = 0; i < m_list.size(); i++)
        {
            AccessControl ctrl = m_list.get(i);
            str.append(ctrl.toString());
            str.append(",");
        }
        str.append("]");

        return str.toString();
    }
}
