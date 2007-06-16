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
*  File    LinkValidationService.java
*----------------------------------------------------------------------------*/

package org.alfresco.linkvalidation;

import java.util.List;

import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.util.NameMatcher;

public interface LinkValidationService
{
    /**
    * Updates href status and href file dependencies for path.
    * 
    *
    * @param storeNameOrWebappPath 
    *            The store name or path to webapp
    *                        
    * @param incremental     
    *            If true, updates information incrementally, based on the 
    *            files that have changed and prior calculations regarding 
    *            url-to-file dependencies.  If false, first deletes all URL
    *            info associated with the store/webapp (if any), then does 
    *            a full rescan to update info.
    * 
    * @param connectTimeout  
    *            Amount of time in milliseconds that this function will wait
    *            before declaring that the connection has failed 
    *            (e.g.: 10000 ms).
    *
    * @param readTimeout     
    *            time in milliseconds that this function will wait before
    *            declaring that a read on the connection has failed
    *            (e.g.:  30000 ms).
    * 
    * @param nthreads
    *             Number of threads to use when fetching URLs (e.g.: 5)
    *
    * @param status
    *             While updateHrefInfo() is a synchronous function, 
    *             'status' may be polled in a separate thread to 
    *             observe its progress.
    */
    public void updateHrefInfo( String   storeNameOrWebappPath,
                                boolean  incremental,
                                int      connectTimeout,
                                int      readTimeout,
                                int      nthreads,
                                HrefValidationProgress progress
                              ) 
                throws AVMNotFoundException;


    /**
    *  Fetches information on broken hrefs within a store name or path 
    *  to a webapp.  This function is just a convenience wrapper for calling  
    *  getHrefConcordance with statusGTE=400 and statusLTE=599.
    */
    public List<HrefConcordanceEntry> getBrokenHrefConcordanceEntries( 
                                          String  storeNameOrWebappPath 
                                      ) throws AVMNotFoundException;


    /**
    *  Returns information regarding the hrefs within storeNameOrWebappPath
    *  whose return status is greater than or equal to 'statusGTE', and 
    *  less than or equal to 'statusLTE'.  The List<HrefConcordanceEntry>
    *  is sorted in increasing lexicographic order by href.  Within each
    *  HrefConcordanceEntry, the files retrieved via getLocations()
    *  are also sorted in increasing lexicographic order.
    * 
    *  <p>
    *  Example 1:<br>
    *  The following parameters will fetch all the broken links
    *  within the ROOT webapp in the staging area of the 'mysite' web project:
    *  <ul>
    *    <li> storeNameOrWebappPath="mysite:/www/avm_webapps/ROOT"
    *    <li> statusGTE=400
    *    <li> statusLTE=599
    *  </ul>
    *  <p>
    *  Example 2:<br>
    *  The following parameters will fetch all the links whose return status
    *  is "successful" (2xx) for all webapps contained by the staging area of
    *  the 'mysite' web project:
    *  <ul>
    *    <li> storeNameOrWebappPath="mysite"
    *    <li> statusGTE=200
    *    <li> statusLTE=299
    *  </ul>
    *  <p>
    *  Example 3:<br>
    *  The following parameters will fetch all the links whose return status
    *  is 200 (OK) within the ROOT webapp in the staging area of the 'mysite' 
    *  web project:
    *  <ul>
    *    <li> storeNameOrWebappPath="mysite:/www/avm_webapps/ROOT"
    *    <li> statusGTE=200
    *    <li> statusLTE=200
    *  </ul>
    *  <p>
    *  For details regarding HTTP status codes, see:
    *  http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
    *
    */
    public List<HrefConcordanceEntry> getHrefConcordanceEntries( 
                                         String  storeNameOrWebappPath,
                                         int     statusGTE,
                                         int     statusLTE
                                      )  throws AVMNotFoundException;


    /**
    *  This function is just a convenience wrapper for calling  
    *  getHrefManifestEntries with statusGTE=400 and statusLTE=599.
    */
    public List<HrefManifestEntry> getBrokenHrefManifestEntries( 
                                      String storeNameOrWebappPath
                                   )  throws AVMNotFoundException;

    /**
    *  Returns a manifest consisting of just the broken hrefs 
    *  within each file containing one or more broken href. 
    *  The HrefManifestEntry list is sorted in increasing lexicographic 
    *  order by file name.  The hrefs within each HrefManifestEntry
    *  are also sorted in increasing lexicographic order.
    */
    public List<HrefManifestEntry> getHrefManifestEntries( 
                                  String storeNameOrWebappPath,
                                  int    statusGTE,
                                  int    statusLTE) throws 
                                  AVMNotFoundException;


    /**
    *  Fetch all hyperlinks that rely upon the existence of the file specified
    *  by 'path', directly or indirectly.  The list of hrefs returnd is 
    *  sorted in increasing lexicographic order.  For example, in 
    *   alfresco-sample-website.war, the hrefs dependent upon
    *  <code>mysite:/www/avm_webapps/ROOT/assets/footer.html</code> are:
    *  <pre>
    *     http://mysite.www--sandbox.version--v-1.127-0-0-1.ip.alfrescodemo.net:8180/
    *     http://mysite.www--sandbox.version--v-1.127-0-0-1.ip.alfrescodemo.net:8180/assets/footer.html
    *     http://mysite.www--sandbox.version--v-1.127-0-0-1.ip.alfrescodemo.net:8180/index.jsp
    *     http://mysite.www--sandbox.version--v-1.127-0-0-1.ip.alfrescodemo.net:8180/media/releases/index.jsp
    *  </pre>
    *  Note that this list may contain links that are functionally equivalent
    * (e.g.: the first and third links), and may also contain links that 
    * don't actually appear an any web page, but are implicitly present
    * in the site because any asset can be "dead reckoned".
    * 
    *
    */
    public List<String> getHrefsDependentUponFile(String path);

    public HrefDifference getHrefDifference( 
                              String                 srcWebappPath, 
                              String                 dstWebappPath, 
                              int                    connectTimeout,
                              int                    readTimeout,
                              int                    nthreads,
                              HrefValidationProgress progress
                          ) throws AVMNotFoundException;


    public HrefDifference getHrefDifference( int                    srcVersion,
                                             String                 srcWebappPath,
                                             int                    dstVersion,
                                             String                 dstWebappPath,
                                             int                    connectTimeout,
                                             int                    readTimeout,
                                             int                    nthreads,
                                             HrefValidationProgress progress)
                                           throws AVMNotFoundException;

}

