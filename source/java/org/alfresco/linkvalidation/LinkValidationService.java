/*-----------------------------------------------------------------------------
*  Copyright 2007-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
*
*
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    LinkValidationService.java
*----------------------------------------------------------------------------*/

package org.alfresco.linkvalidation;

import java.net.SocketException;
import java.util.List;

import javax.net.ssl.SSLException;

import org.alfresco.service.cmr.avm.AVMNotFoundException;

public interface LinkValidationService
{
    public void onBootstrap();
    public void onShutdown();

    /**
    *  Returns the poll interval (in milliseconds) used to check 
    *  for new snapshots in staging.
    */
    public int getPollInterval();


    //-------------------------------------------------------------------------
    /**
    *  This function is applied to a webapp in staging, and is just a 
    *  convenience wrapper for calling getHrefManifestEntries 
    *  with statusGTE=400 and statusLTE=599.
    *  <p>
    *  Note: Files and urls within this list of manifests pertain to
    *        the latest validated snapshot of staging (which may be
    *        somewhat older than the very latest snapshot).   The
    *        validation service attempts to keep the latest validated
    *        snapshot as new as possible, automatically.
    */
    //-------------------------------------------------------------------------
    public HrefManifest  getBrokenHrefManifest( String  webappPath)
                                        throws  AVMNotFoundException,
                                                SocketException,
                                                IllegalArgumentException;


    //-------------------------------------------------------------------------
    /**
    *  This function is applied to webapps in staging and returns a manifest 
    *  consisting of just the broken hrefs within each file containing 
    *  one or more broken href.  The HrefManifestEntry list is sorted in 
    *  increasing lexicographic order by file name.  The hrefs within each 
    *  HrefManifestEntry are also sorted in increasing lexicographic order.
    */
    //-------------------------------------------------------------------------
    public HrefManifest  getHrefManifest( String webappPath,
                                          int    statusGTE,
                                          int    statusLTE)
                                  throws  AVMNotFoundException,
                                          SocketException,
                                          IllegalArgumentException;


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
                                   HrefValidationProgress progress)
                          throws   AVMNotFoundException,
                                   SocketException,
                                   SSLException,
                                   LinkValidationAbortedException;


    //-------------------------------------------------------------------------
    /**
    *   This function is applied to difference objects created by comparing
    *   webapps in an author or workflow store to the staging area they 
    *   overlay.
    */
    //-------------------------------------------------------------------------
    public HrefManifest  getBrokenHrefManifest( HrefDifference hdiff ) 
                                        throws  AVMNotFoundException,
                                                SocketException;


    //-------------------------------------------------------------------------
    /**
    *  Fetches a manifest of all hyperlinks broken in new or modified files in
    *  an HrefDifference.  The entries in this manifest are in the 'src' 
    *  namespace of the HrefDifference operation 
    * (i.e.:  files & urls from alice, not staging).
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
    * @param progress
    *             While updateHrefInfo() is a synchronous function,
    *             'status' may be polled in a separate thread to
    *             observe its progress.
    */
    //-------------------------------------------------------------------------
    public void updateHrefInfo( String                 path,
                                boolean                incremental,
                                boolean                validateExternal,
                                HrefValidationProgress progress)
                throws          AVMNotFoundException,
                                SocketException,
                                SSLException,
                                LinkValidationAbortedException;


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
    */
    //-------------------------------------------------------------------------
    public List<String> getHrefsDependentUponFile(String path);

    public boolean isLinkValidationDisabled();
}
