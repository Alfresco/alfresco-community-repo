/*
 * Copyright (C) 2007 Alfresco, Inc.
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
package org.alfresco.repo.admin.registry;

import java.io.Serializable;

/**
 * Key for looking up registry metadata.
 * 
 * @author Derek Hulley
 */
public class RegistryKey implements Serializable
{
    private static final long serialVersionUID = 1137822242292626854L;

    static final String REGISTRY_1_0_URI = "http://www.alfresco.org/system/registry/1.0";
    
    private String namespaceUri;
    private String[] path;
    private String property;
    
    /**
     * Build a registry key from a given array of elements.
     */
    private static String buildPathString(String... elements)
    {
        if (elements.length == 0)
        {
            return "/";
        }
        StringBuilder sb = new StringBuilder();
        for (String element : elements)
        {
            if (element == null || element.length() == 0)
            {
                throw new IllegalArgumentException("Key elements may not be empty or null");
            }
            sb.append("/").append(element);
        }
        return sb.toString();
    }
    
    /**
     * For path /a/b/c and property 'x', put in <pre>"a", "b", "c", "x"</pre>
     * The property can also be <tt>null</tt> as in <pre>"a", "b", "c", null</pre>
     * 
     * @param namespaceUri the key namespace to use.  If left <tt>null</tt> then the
     *      {@link #REGISTRY_1_0_URI default} will be used.
     * @param key the path elements followed by the property name.
     */
    public RegistryKey(String namespaceUri, String... key)
    {
        if (namespaceUri == null)
        {
            namespaceUri = REGISTRY_1_0_URI;
        }
        this.namespaceUri = namespaceUri;
        // The last value is the property
        int length = key.length;
        if (length == 0)
        {
            throw new IllegalArgumentException("No value supplied for the RegistryKey property");
        }
        this.property = key[length - 1];
        this.path = new String[length - 1];
        System.arraycopy(key, 0, path, 0, length - 1);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("RegistryKey")
          .append("[ ").append(RegistryKey.buildPathString(path)).append("/").append(property)
          .append(" ]");
        return sb.toString();
    }

    public String getNamespaceUri()
    {
        return namespaceUri;
    }

    public String[] getPath()
    {
        return path;
    }

    public String getProperty()
    {
        return property;
    }

}
