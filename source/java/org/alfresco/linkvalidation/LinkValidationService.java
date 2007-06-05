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
    public void updateHrefInfo( String  storeNameOrWebappPath,
                                boolean incremental,
                                int     connectTimeout,
                                int     readTimeout,
                                int     nthreads,
                                UpdateHrefInfoStatus status
                              ) 
                throws AVMNotFoundException;


    /**
    *  Fetches information on broken hrefs within a store name or path 
    *  to a webapp.  This function is just a convenience wrapper for calling  
    *  getHrefConcordance with statusGTE=400 and statusLTE=599.
    */
    public List<HrefConcordanceEntry> getBrokenHrefConcordance( 
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
    *  <ul>
    *  <p>
    *  Example 2:<br>
    *  The following parameters will fetch all the links whose return status
    *  is "successful" (2xx) for all webapps contained by the staging area of
    *  the 'mysite' web project:
    *  <ul>
    *    <li> storeNameOrWebappPath="mysite"
    *    <li> statusGTE=200
    *    <li> statusLTE=299
    *  <ul>
    *  Example 3:<br>
    *  The following parameters will fetch all the links whose return status
    *  is 200 (OK) within the ROOT webapp in the staging area of the 'mysite' 
    *  web project
    *  <ul>
    *    <li> storeNameOrWebappPath="mysite:/www/avm_webapps/ROOT"
    *    <li> statusGTE=200
    *    <li> statusLTE=200
    *  <ul>
    *  <p>
    *  For details regarding HTTP status codes, see:
    *  http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
    *
    */
    public List<HrefConcordanceEntry> getHrefConcordance( 
                                         String  storeNameOrWebappPath,
                                         int     statusGTE,
                                         int     statusLTE
                                      )  throws AVMNotFoundException;


    /**
    *  This function is just a convenience wrapper for calling  
    *  getHrefManifests with statusGTE=400 and statusLTE=599.
    */
    public List<HrefManifest> getBrokenHrefManifests( 
                                 String storeNameOrWebappPath
                              )  throws AVMNotFoundException;

    /**
    *  Returns a manifest consisting of just the broken hrefs 
    *  within each file containing one or more broken href. 
    *  The HrefManifest list is sorted in increasing lexicographic 
    *  order by file name.  The hrefs within each HrefManifest
    *  are also sorted in increasing lexicographic order.
    */
    public List<HrefManifest> getHrefManifests( 
                                  String storeNameOrWebappPath,
                                  int    statusGTE,
                                  int    statusLTE) throws 
                                  AVMNotFoundException;

    /**
    *  This function is just a convenience wrapper for calling  
    *  getHrefManifest with statusGTE=400 and statusLTE=599.
    *  <p>
    *  Note:  If you want to get the broken links in every file in 
    *  a webapp or store, it's much more efficient to use 
    *  getBrokenHrefManifests instead of this function.
    */
    public HrefManifest getBrokenHrefManifest( String path) 
                                               throws AVMNotFoundException;

    /**
    *  Returns a manifest of all the hrefs within the file specified by 'path'
    *  whose response status is greater than or equal to statusGTE, 
    *  and less than or equal to statusLTE.
    *  <p>
    *  Note:  If you want to get a list of manifests of every file in a
    *  webapp or store, it's much more efficient to use getHrefManifests
    *  instead of this function.
    */
    public HrefManifest getHrefManifest( String path,
                                         int    statusGTE,
                                         int    statusLTE) throws 
                                         AVMNotFoundException;


    public List<String> getHrefsDependentUponFile(String path);
}

