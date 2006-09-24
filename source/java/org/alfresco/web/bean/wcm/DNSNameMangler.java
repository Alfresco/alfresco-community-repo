/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.web.bean.wcm;

import java.util.regex.Pattern;

import org.alfresco.util.GUID;

/**
 * Utility to convert sandbox store names into DNS save names.
 * @author britt
 */
class DNSNameMangler
{
    // Component Separator.
    private static final String SEPARATOR = "--";

    // Regular expressions.
    private static final Pattern RX_DNS_LEGAL =
        Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-]{0,57}[a-zA-Z0-9]$");
    private static final Pattern RX_ILLEGAL_CHARS =
        Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern RX_HYPHENS =
        Pattern.compile("\\-+");
    private static final Pattern RX_LEADING_HYPHEN =
        Pattern.compile("^\\-");
    private static final Pattern RX_TRAILING_HYPHEN =
        Pattern.compile("\\-$");
    
    /**
     * Make a DNS readable name related to the components passed in.
     * @param components The Strings from which to synthesize the DNS name.
     * @return A Valid DNS name.
     */
    static String MakeDNSName(String... components)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < components.length - 1; i++)
        {
            builder.append(MangleOne(components[i]));
            builder.append(SEPARATOR);
        }
        builder.append(MangleOne(components[components.length - 1]));
        String result = builder.toString();
        if (RX_DNS_LEGAL.matcher(result).matches())
        {
            return result;
        }
        // Otherwise more drastic measures are needed.
        result = components[0] + "--" + GUID.generate();
        result = MangleOne(result);
        if (RX_DNS_LEGAL.matcher(result).matches())
        {
            return result;
        }
        // Finally this cannot fail.
        return MangleOne(GUID.generate());
    }
    
    /**
     * Mangle one component of a DNS legal name.
     * @param name The component.
     * @return The mangled component.
     */
    static String MangleOne(String name)
    {
        // Preserve case for prettier auto-generated URLs 
        // name = name.toLowerCase();

        name = RX_ILLEGAL_CHARS.matcher(name).replaceAll("-");
        name = RX_HYPHENS.matcher(name).replaceAll("-");
        name = RX_LEADING_HYPHEN.matcher(name).replaceAll("x");
        name = RX_TRAILING_HYPHEN.matcher(name).replaceAll("x");
        return name;
    }
}
