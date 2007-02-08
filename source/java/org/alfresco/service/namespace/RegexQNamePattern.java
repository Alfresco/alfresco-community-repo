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
package org.alfresco.service.namespace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Provides matching between {@link org.alfresco.service.namespace.QName qnames} using
 * regular expression matching.
 * <p>
 * A simple {@link #MATCH_ALL convenience} pattern matcher is also provided that
 * will match any qname.
 * 
 * @see java.lang.String#matches(java.lang.String)
 * 
 * @author Derek Hulley
 */
public class RegexQNamePattern implements QNamePattern
{
    private static final Log logger = LogFactory.getLog(RegexQNamePattern.class);
    
    /** A helper pattern matcher that will match <i>all</i> qnames */
    public static final QNamePattern MATCH_ALL = new QNamePattern()
        {
            public boolean isMatch(QName qname)
            {
                return true;
            }

            @Override
            public boolean equals(Object obj)
            {
                // this is equal if the object's class is the same as this instances
                if (obj == null)
                {
                    return false;
                }
                else if (obj.getClass().equals(this.getClass()))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            
        };
    
    private String namespaceUriPattern;
    private String localNamePattern;
    private String combinedPattern;
    
    /**
     * @param namespaceUriPattern a regex pattern that will be applied to the namespace URI
     * @param localNamePattern a regex pattern that will be applied to the local name
     */
    public RegexQNamePattern(String namespaceUriPattern, String localNamePattern)
    {
        this.namespaceUriPattern = namespaceUriPattern;
        this.localNamePattern = localNamePattern;
        this.combinedPattern = null;
    }
    
    /**
     * @param combinedPattern a regex pattern that will be applied to the full qname
     *      string representation
     * 
     * @see QName#toString()
     */
    public RegexQNamePattern(String combinedPattern)
    {
        this.combinedPattern = combinedPattern;
        this.namespaceUriPattern = null;
        this.localNamePattern = null;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder(56);
        sb.append("RegexQNamePattern[");
        if (combinedPattern != null)
        {
            sb.append(" pattern=").append(combinedPattern);
        }
        else
        {
            sb.append(" uri=").append(namespaceUriPattern);
            sb.append(", localname=").append(localNamePattern);
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * @param qname the value to check against this pattern
     * @return Returns true if the regex pattern provided match thos of the provided qname
     */
    public boolean isMatch(QName qname)
    {
        boolean match = false;
        if (combinedPattern != null)
        {
            String qnameStr = qname.toString();
            match = qnameStr.matches(combinedPattern);
        }
        else
        {
            match = (qname.getNamespaceURI().matches(namespaceUriPattern) &&
                     qname.getLocalName().matches(localNamePattern));
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("QName matching: \n" +
                    "   matcher: " + this + "\n" +
                    "   qname: " + qname + "\n" +
                    "   result: " + match);
        }
        return match;
    }
}
