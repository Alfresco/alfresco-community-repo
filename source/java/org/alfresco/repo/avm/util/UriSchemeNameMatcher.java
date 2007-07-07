/*-----------------------------------------------------------------------------
*  Copyright 2007 Alfresco Inc.
*  
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  
*  This program is distributed in the hope that it will be useful, but
*  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
*  for more details.
*  
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.  As a special
*  exception to the terms and conditions of version 2.0 of the GPL, you may
*  redistribute this Program in connection with Free/Libre and Open Source
*  Software ("FLOSS") applications as described in Alfresco's FLOSS exception.
*  You should have received a copy of the text describing the FLOSS exception,
*  and it is also available here:   http://www.alfresco.com/legal/licensing
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    UriSchemeNameMatcher.java
*----------------------------------------------------------------------------*/

package org.alfresco.repo.avm.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.alfresco.util.NameMatcher;

/**
 * A NameMatcher that matches an incoming URL against list of schemes
 * (less formally known as "protocols"), case insensitively. 
 * The formal spec for parsing URIs is RFC-3986
 * <p>
 *  Perhaps someday, it might be worthwhile to create a specific
 *  parser for each registered scheme-specific part, and validate
 *  that;  for now, we'll just be be more lax, and assume the URI
 *  is alwasy scheme-qualified.  This matcher will look no further
 *  than the leading colon, and declare "no match" otherwise.
 *  The discussion below explains why.
 * <p>
 * See:  http://tools.ietf.org/html/rfc3986):
 * <pre>
 *  The following regex parses URIs:
 *       ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
 *
 *  Given the following URI:   
 *        http://www.ics.uci.edu/pub/ietf/uri/#Related
 *
 *  The captured subexpressions are:
 *
 *        $1 = http:
 *        $2 = http
 *        $3 = //www.ics.uci.edu
 *        $4 = www.ics.uci.edu
 *        $5 = /pub/ietf/uri/
 *        $6 = <undefined>
 *        $7 = <undefined>
 *        $8 = #Related
 *        $9 = Related   
 *
 *   N0TE:
 *      A URI can be non-scheme qualified because $1 is optional.  Therefore,
 *      the following are all exaples of valid non-scheme qualified URIS:
 *
 *         ""
 *         "moo@cow.com"
 *         "moo@cow.com?wow"
 *         "moo@cow.com?wow#zow"
 *         "moo@cow.com#zow"
 *         "/"
 *         "/moo/cow"
 *         "/moo/cow?wow"
 *         "/moo/cow?wow#zow"
 *         "/moo/cow#zow"
 *         "//moo/cow"
 *         "//moo.com/cow"
 *         "//moo.com/cow/"
 *         "//moo.com/cow?wow"
 *         "//moo.com/cow?wow#zow"
 *         "//moo.com/cow#zow"
 *         "//moo.com:8080/cow"
 *         "//moo.com:/cow"
 *         "//moo.com:8080/cow?wow"
 *         "//moo.com:8080/cow?wow#zow"
 *         "//moo.com:8080/cow#zow"
 *         "///moo/cow"
 *         "///moo/cow?wow"
 *         "///moo/cow?wow#zow"
 *         "///moo/cow#zow"
 *
 *      And so forth...
 *      
 * <pre>
 *
 *  Thus the business end of things as far as scheme matching is: $2,
 *  Most schemes will have a $3 that starts with '//', but not all.
 *  Specificially, the following have no "network path '//' segment,
 *  or aren't required to (source: http://en.wikipedia.org/wiki/URI_scheme):
 *  <pre>
 *
 *      cid data dns fax go h323 iax2 mailto mid news pres sip
 *      sips tel urn xmpp about aim callto feed magnet msnim 
 *      psyc skype sms stream xfire ymsgr
 *
 *  </pre>
 *  
 *  Visually the parts are as follows:
 * <pre>
 * 
 *         foo://example.com:10042/over/there?name=ferret#nose
 *         \_/   \_______________/\_________/ \_________/ \__/
 *          |           |            |            |        |
 *       scheme     authority       path        query   fragment
 *          |   _____________________|__
 *         / \ /                        \
 *         urn:example:animal:ferret:nose
 *
 * </pre>
 * 
 * This is useful for classifying URLs for things like whether or not 
 * they're supported by an application.   
 *
 * For example, the LinkValidationService supports http, and https, 
 * is willing to ignore certain well-formed URLs, but treats URLs 
 * will unknown and unsupported protocols as broken.   Concretely,
 * we'd like to avoid treating something like the following one
 * as being non-broken even though you can't apply GET or HEAD
 *  to it.
 *
 * <pre>
 * <a href="mailto:alice@example.com">Email</a>
 * </pre>
 * 
 * As of June 2007,IANA had over 70 registered and provisional protocols
 * listed at http://www.iana.org/assignments/uri-schemes.html but sometimes 
 * people create their own too (e.g.: cvs).  Here's the official list:
 * <pre>
 *
 *    aaa aaas acap afs cap cid crid data dav dict dns dtn fax file
 *    ftp go gopher h323 http https iax2 icap im imap info ipp iris
 *    iris.beep iris.lwz iris.xpc iris.xpcs ldap mailserver mailto
 *    mid modem msrp msrps mtqp mupdate news nfs nntp opaquelocktoken
 *    pop pres prospero rtsp service shttp sip sips snmp soap.beep
 *    soap.beeps tag tel telnet tftp thismessage tip tn3270 tv urn
 *    vemmi wais xmlrpc.beep xmlrpc.beeps xmpp z39.50r z39.50s
 * </pre>
 *
 */
public class UriSchemeNameMatcher implements NameMatcher, Serializable 
{
    /**
     * The extensions to match.
     */
    HashMap<String,String> scheme_;
    
    /**
     * Default constructor.
     */
    public UriSchemeNameMatcher()
    {
        scheme_ = new HashMap<String,String>();
    }
    
    /**
     * Set the protocols case insensitively (cannonicalized to lower-case).
     *
     * @param protocols
     */
    public void setExtensions(List<String> protocols)
    {
        for (String protocol : protocols)
        { 
            scheme_.put( protocol.toLowerCase(), null );
        }
    }
    
    /**
    *  Returns true if the URL's protocol is in the of
    *  being matched.  Everything up to but not including
    *  the intial colon is 
    */
    public boolean matches(String uri)
    {
        if ( uri == null ) { return false; }

        int colon_index = uri.indexOf(':');

        if ( colon_index >= 0)
        {
            String proto  = 
                uri.substring(0, colon_index).toLowerCase();

            return scheme_.containsKey( proto );
        }
        return false;
    }
}

