/*
 * Copyright (C) 2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.alfresco.web.bean.wcm;

import java.util.regex.Pattern;

import org.alfresco.util.GUID;

/**
 * Utility to convert sandbox store names into DNS save names.
 *
 *   Host labels appear in the same order as transparent overlays
 *   are viewed (highest first).  For example:
 *
 *   The "preview" layer on the "alice" layer on the "mysite" layer
 *   within the domain www--sandbox.127-0-0-1.ip.alfrescodemo.net   
 *   is encoded as:
 *
 *     http://preview.alice.mysite.www--sandbox.127-0-0-1.ip.alfrescodemo.net
 *
 *   Note that the "virtualization" domain config just refers to
 *   where the wildcard DNS entry begins.  Here,  both domains
 *   "127-0-0-1.ip.alfrescodemo.net" and  "*.127-0-0-1.ip.alfrescodemo.net"
 *   resolve to 127.0.0.1, so effectively "127-0-0-1.ip.alfrescodemo.net"
 *   is the "virtualization domain".   The "www--sandbox" part just
 *   delmits the end of the dns-name-mangled store.
 *
 *   This manging scheme was also designed to be fully compatible with
 *   the future use of  I18N-encoded DNS names;  the relevant  standard
 *   is IDNA ("Internationalizing Domain Names In Applications").
 *   See RFC 3490 and 3492.
 *
 * @author Jon Cox
 * @author Britt Park
 *
 */
class DNSNameMangler
{
    // Component Separator.
    private static final String SEPARATOR = ".";

    // DNS rules allow up to 255 chars, but limiting 
    // MAX_INTERNAL_DNS_NAME_LENGTH to less in order 
    // to provide plenty of extra room for:
    //
    //
    //   o  The AVMUrlValve's end-of-info-bearing-part-of-DNS-name delimiter:
    //      (i.e.: ".www--sandbox").  For example:  
    //
    //         http://<info-bearing-dns-name>.www--sandbox:<port>/
    //
    //   o  Other AVMUrlValve args after "www--sandbox".  For example:
    //         http://alice.mysite.www--sandbox.version--v44.<domain>:<port>/
    //         http://alice.mysite.www--sandbox.gmt--2006-12-31-23-59.<domain>
    //         ...
    //

    private static final int MAX_INTERNAL_DNS_NAME_LENGTH = 150;

    // Regular expressions.
    private static final Pattern RX_DNS_LEGAL =
        Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]" + 
                        "(?:\\.[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9])*");

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

        // Make domain name order the reverse 
        // of the file system ordering.

        for (int i = components.length - 1; i > 0;  i--)
        {
            builder.append(MangleOne(components[i]));
            builder.append(SEPARATOR);
        }
        builder.append(MangleOne(components[0]));
        String result = builder.toString();

        if ( (result.length() <= MAX_INTERNAL_DNS_NAME_LENGTH) &&
              RX_DNS_LEGAL.matcher(result).matches()
           )
        {
            return result;
        }

        // Otherwise more drastic measures are needed.
        result = components[0] +  SEPARATOR +  "guid-" + GUID.generate();
        result = MangleOne(result);

        if ( (result.length() <= MAX_INTERNAL_DNS_NAME_LENGTH) &&
              RX_DNS_LEGAL.matcher(result).matches()
           )
        {
            return result;
        }

        // Finally this cannot fail.
        return MangleOne("guid-" + GUID.generate());
    }
    
    /**
     * Mangle one component of a DNS legal name.
     * @param name The component.
     * @return The mangled component.
     */
    static String MangleOne(String name)
    {
        // Even if the name is IDNA-encoded, the result is never
        // a string that contains chars outside of [a-zA-Z0-9-]
        // Replace bad chars with "-", rather than throwing 
        // an error.

        name = RX_ILLEGAL_CHARS.matcher(name).replaceAll("-");

        // While it's tempting to reserve "--" as our own
        // mangling delimiter, IDNA has already clamed it.
        // Therefore, doing something like this would be bad:
        //
        //      name = RX_HYPHENS.matcher(name).replaceAll("-");
        //
        // Any IDNA I18N-encoded host label ("xn--...") would
        // be corrupted.

        // However, leading/trailing hyphens are always illegal,
        // so we can still check for that:
        
        name = RX_LEADING_HYPHEN.matcher(name).replaceAll("x");
        name = RX_TRAILING_HYPHEN.matcher(name).replaceAll("x");

        return name;
    }
}
