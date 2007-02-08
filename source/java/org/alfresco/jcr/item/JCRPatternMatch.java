/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.jcr.item;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

/**
 * Checks if the QName matches the passed JCR pattern.
 * 
 * The pattern may be a full name or a partial name with one or more wildcard
 * characters ("*"), or a disjunction (using the "|" character to represent 
 * logical OR) of these. For example,
 *
 * isMatch("jcr:* | myapp:report | my doc")
 *
 * Note that leading and trailing whitespace around a disjunct is ignored, 
 * but whitespace within a disjunct forms part of the pattern to be matched.
 *
 * The EBNF for namePattern is:
 *
 * namePattern ::= disjunct {'|' disjunct}
 * disjunct ::= name [':' name]
 * name ::= '*' | ['*'] fragment {'*' fragment} ['*']
 * fragment ::= char {char}
 * char ::= nonspace | ' '
 * nonspace ::= (* Any Unicode character except: '/', ':', '[', ']', '*', ''', '"', '|' or any whitespace character *) 
 */
public class JCRPatternMatch implements QNamePattern
{
    private List<String> searches = new ArrayList<String>();
    private NamespacePrefixResolver resolver;
    
    
    /**
     * Construct 
     * @param pattern  JCR Pattern
     * @param resolver  Namespace Prefix Resolver
     */
    public JCRPatternMatch(String pattern, NamespacePrefixResolver resolver)
    {
        // TODO: Check for valid pattern
        
        // Convert to regular expression
        String regexPattern = pattern.replaceAll("\\*", ".*");

        // Split into independent search strings
        StringTokenizer tokenizer = new StringTokenizer(regexPattern, "|", false);
        while (tokenizer.hasMoreTokens())
        {
            String disjunct = tokenizer.nextToken().trim();
            this.searches.add(disjunct);
        }
        
        this.resolver = resolver;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.QNamePattern#isMatch(org.alfresco.service.namespace.QName)
     */
    public boolean isMatch(QName qname)
    {
        String prefixedName = qname.toPrefixString(resolver);
        for (String search : searches)
        {
            if (prefixedName.matches(search))
            {
                return true;
            }
        }
        return false;
    }

}
