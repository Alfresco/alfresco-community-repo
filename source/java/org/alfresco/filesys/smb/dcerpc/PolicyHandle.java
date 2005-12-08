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
package org.alfresco.filesys.smb.dcerpc;

/**
 * Policy Handle Class
 */
public class PolicyHandle
{

    // Length of a policy handle

    public static final int POLICY_HANDLE_SIZE = 20;

    // Policy handle bytes

    private byte[] m_handle;

    // Handle name

    private String m_name;

    /**
     * Default constructor
     */
    public PolicyHandle()
    {
        setName("");
    }

    /**
     * Class constructor
     * 
     * @param buf byte[]
     * @param off int
     */
    public PolicyHandle(byte[] buf, int off)
    {
        initialize(buf, off);
        setName("");
    }

    /**
     * Class constructor
     * 
     * @param name String
     * @param buf byte[]
     * @param off int
     */
    public PolicyHandle(String name, byte[] buf, int off)
    {
        initialize(buf, off);
        setName(name);
    }

    /**
     * Determine if the policy handle is valid
     * 
     * @return boolean
     */
    public final boolean isValid()
    {
        return m_handle != null ? true : false;
    }

    /**
     * Return the policy handle bytes
     * 
     * @return byte[]
     */
    public final byte[] getBytes()
    {
        return m_handle;
    }

    /**
     * Return the policy handle name
     * 
     * @return String
     */
    public final String getName()
    {
        return m_name;
    }

    /**
     * Set the policy handle name
     * 
     * @param name String
     */
    public final void setName(String name)
    {
        m_name = name;
    }

    /**
     * Store the policy handle into the specified buffer
     * 
     * @param buf byte[]
     * @param off int
     * @return int
     */
    public final int storePolicyHandle(byte[] buf, int off)
    {

        // Check if the policy handle is valid

        if (isValid() == false)
            return -1;

        // Copy the policy handle bytes to the user buffer

        for (int i = 0; i < POLICY_HANDLE_SIZE; i++)
            buf[off + i] = m_handle[i];

        // Return the new buffer position

        return off + POLICY_HANDLE_SIZE;
    }

    /**
     * Load the policy handle from the specified buffer
     * 
     * @param buf byte[]
     * @param off int
     * @return int
     */
    public final int loadPolicyHandle(byte[] buf, int off)
    {

        // Load the policy handle from the buffer

        initialize(buf, off);
        return off + POLICY_HANDLE_SIZE;
    }

    /**
     * Clear the handle
     */
    protected final void clearHandle()
    {
        m_handle = null;
    }

    /**
     * Initialize the policy handle
     * 
     * @param buf byte[]
     * @param off int
     */
    private final void initialize(byte[] buf, int off)
    {

        // Copy the policy handle bytes

        if ((off + POLICY_HANDLE_SIZE) <= buf.length)
        {

            // Allocate the policy handle buffer

            m_handle = new byte[POLICY_HANDLE_SIZE];

            // Copy the policy handle

            for (int i = 0; i < POLICY_HANDLE_SIZE; i++)
                m_handle[i] = buf[off + i];
        }
    }

    /**
     * Return the policy handle as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");

        if (getName() != null)
            str.append(getName());
        str.append(":");

        if (isValid())
        {
            for (int i = 0; i < POLICY_HANDLE_SIZE; i++)
            {
                int val = (int) (m_handle[i] & 0xFF);
                if (val <= 16)
                    str.append("0");
                str.append(Integer.toHexString(val).toUpperCase());
                str.append("-");
            }
            str.setLength(str.length() - 1);
            str.append("]");
        }

        return str.toString();
    }
}
