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
            sb.append(", localname=").append(namespaceUriPattern);
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
