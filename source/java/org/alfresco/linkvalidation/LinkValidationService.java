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
import java.net.SocketException;
import javax.net.ssl.SSLException;

public interface LinkValidationService
{
    public void onBootstrap();
    public void onShutdown();

    //-------------------------------------------------------------------------
    /**
    *  This function is just a convenience wrapper for calling  
    *  getHrefManifestEntries with statusGTE=400 and statusLTE=599.
    *  <p>
    *  Note: Files and urls within this list of manifests pertain to
    *        the latest validated snapshot of staging (which may be
    *        somewhat older than the very latest snapshot).   The
    *        validation service attempts to keep the latest validated
    *        snapshot as new as possible, automatically.
    */
    //-------------------------------------------------------------------------
    public List<HrefManifestEntry> getBrokenHrefManifestEntries( 
                                      String storeNameOrWebappPath
                                   )  throws AVMNotFoundException;

    //-------------------------------------------------------------------------
    /**
    *  Returns a manifest consisting of just the broken hrefs 
    *  within each file containing one or more broken href. 
    *  The HrefManifestEntry list is sorted in increasing lexicographic 
    *  order by file name.  The hrefs within each HrefManifestEntry
    *  are also sorted in increasing lexicographic order.
    */
    //-------------------------------------------------------------------------
    public List<HrefManifestEntry> getHrefManifestEntries( 
                                  String storeNameOrWebappPath,
                                  int    statusGTE,
                                  int    statusLTE) throws 
                                  AVMNotFoundException;


    //-------------------------------------------------------------------------
    /**
    *  Fetch the difference between two areas.
    *  Version -1 is assumed for src; dst relies on the state of the
    *  link validation service updating link validity tables.
    *  Typically, this will be for some version close to the latest
    *  snapshot, but it's async, so it might be older.
    */
    //-------------------------------------------------------------------------
    public HrefDifference getHrefDifference( 
                                   String                 srcWebappPath, 
                                   String                 dstWebappPath, 
                                   int                    connectTimeout,
                                   int                    readTimeout,
                                   int                    nthreads,
                                   HrefValidationProgress progress)
                          throws   AVMNotFoundException,
                                   SocketException,
                                   SSLException,
                                   LinkValidationAbortedException;



    //-------------------------------------------------------------------------
    /**
    *  Fetches a manifest of all hyperlinks broken by files 
    *  deleted in a HrefDifference.   Files and hrefs in this
    *  manifest will be in the namespace of the src in the
    *  HrefDifference.  For example, suppose the "test"
    *  web project had a ROOT webapp with a link within 
    *  "moo.html" that pointed to: "hamlet.html".
    *  Now suppose that user 'alice' proposes to delete "hamlet.html".
    *  Because 'alice' is the 'src' and staging is the 'dst'
    *  in the HrefDifference, all files and hyperlinks appear from
    *  the perspective of the main working store within 
    *  alice's sandbox.  Thus, the broken link info is as follows:
    *
    * <pre>
    *  File containing broken link:
    *     test--alice:/www/avm_webapps/ROOT/moo.html
    *
    *  Broken link:
    *   http://alice.test.www--sandbox.version--v-1.127-0-0-1.ip.alfrescodemo.net:8180/hamlet.html
    * </pre>
    *
    * @param hdiff The difference between two webapps obtained 
    *              by calling getHrefDifference().
    */
    //-------------------------------------------------------------------------
    public HrefManifest getHrefManifestBrokenByDelete(HrefDifference hdiff);




    //-------------------------------------------------------------------------
    /**
    *  Fetches a manifest of all hyperlinks broken in new or modified files in
    *  an HrefDifference.  Similar to getHrefManifestBrokenByDelete(), 
    *  the entries in this manifest are in the 'src' namespace of the
    *  HrefDifference operation (i.e.:  files & urls from alice, not staging).
    *
    * @param hdiff The difference between two webapps obtained 
    *              by calling getHrefDifference().
    */
    //-------------------------------------------------------------------------
    public HrefManifest getHrefManifestBrokenByNewOrMod(HrefDifference hdiff);




    //-------------------------------------------------------------------------
    /**
    * WARNING: this function won't be part of the public interface for long.   
    * Updates href status and href file dependencies for path.
    *
    * @param path 
    *            <ul>
    *              <li>  If null, do all stores & all webapps in them.
    *              <li>  If store, do all webapps in store
    *              <li>  If webapp, do webapp.
    *            </ul>
    *                        
    * @param incremental     
    *            If true, updates information incrementally, based on the 
    *            files that have changed and prior calculations regarding 
    *            url-to-file dependencies.  If false, first deletes all URL
    *            info associated with the store/webapp (if any), then does 
    *            a full rescan to update info.
    * 
    * @validateExternal
    *            Currently does nothing.  Perhaps one day you'll be able to
    *            turn off validation of external links.
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
    * @param progress
    *             While updateHrefInfo() is a synchronous function, 
    *             'status' may be polled in a separate thread to 
    *             observe its progress.
    */
    //-------------------------------------------------------------------------
    public void updateHrefInfo( String                 path,              
                                boolean                incremental,             
                                boolean                validateExternal,        
                                int                    connectTimeout,          
                                int                    readTimeout,             
                                int                    nthreads,                
                                HrefValidationProgress progress)                
                throws          AVMNotFoundException,                           
                                SocketException,                                
                                SSLException,                                   
                                LinkValidationAbortedException;                 






    //-------------------------------------------------------------------------
    /**
    *  Merges an HrefDifference into the master href info table.
    *  WARNING: This function won't be part of the public interface for long.
    */ 
    //-------------------------------------------------------------------------
    public void mergeHrefDiff( HrefDifference hdiff)
                throws         AVMNotFoundException,
                               SocketException,
                               SSLException,
                               LinkValidationAbortedException;
    


    //-------------------------------------------------------------------------
    /**
    *  Fetches information on broken hrefs within a store name or path 
    *  to a webapp.  This function is just a convenience wrapper for calling  
    *  getHrefConcordance with statusGTE=400 and statusLTE=599.
    */
    //-------------------------------------------------------------------------
    public List<HrefConcordanceEntry> getBrokenHrefConcordanceEntries( 
                                          String  storeNameOrWebappPath 
                                      ) throws AVMNotFoundException;


    //-------------------------------------------------------------------------
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
    //-------------------------------------------------------------------------
    public List<HrefConcordanceEntry> getHrefConcordanceEntries( 
                                         String  storeNameOrWebappPath,
                                         int     statusGTE,
                                         int     statusLTE
                                      )  throws AVMNotFoundException;



    //-------------------------------------------------------------------------
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
    //-------------------------------------------------------------------------
    public List<String> getHrefsDependentUponFile(String path);





    //-------------------------------------------------------------------------
    // NEARLY OBSOLETE!
    // NEARLY OBSOLETE!
    // NEARLY OBSOLETE!
    // NEARLY OBSOLETE!
    //-------------------------------------------------------------------------
    public void updateHrefInfo( 
                       String   storeNameOrWebappPath,     // NEARLY OBSOLETE!
                       boolean  incremental,               // NEARLY OBSOLETE!
                       int      connectTimeout,            // NEARLY OBSOLETE!
                       int      readTimeout,               // NEARLY OBSOLETE!
                       int      nthreads,                  // NEARLY OBSOLETE!
                       HrefValidationProgress progress)    // NEARLY OBSOLETE!
                throws AVMNotFoundException,               // NEARLY OBSOLETE!
                       SocketException,                    // NEARLY OBSOLETE!
                       SSLException,                       // NEARLY OBSOLETE!
                       LinkValidationAbortedException;     // NEARLY OBSOLETE!
}
