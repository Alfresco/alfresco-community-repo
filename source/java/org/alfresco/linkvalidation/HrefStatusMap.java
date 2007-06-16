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
*  File    HrefStatusMap.java
*----------------------------------------------------------------------------*/

package  org.alfresco.linkvalidation;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.alfresco.util.Pair;
  
/**
*  A synchronized wrapper for the ephemeral cache of href status results.
*  The key is a url, the value is a pair consisting of the url's status code
*  and the list of files accessed when the URL is requested, if known.
*
*  This class also allows the non-synchronized map it wraps to be extracted. 
*/
public class HrefStatusMap 
{
    Map<  String,  Pair<Integer,List<String>>> status_;

    public  HrefStatusMap()
    { 
        status_ = new HashMap<String,Pair<Integer,List<String>>>();
    }

    public  HrefStatusMap( Map<String,Pair<Integer,List<String>>> status ) 
    { status_ = status; }


    /**
    *  Takes the url and the Pair: status code, file dependency list
    */
    public synchronized void put( String url, Pair<Integer,List<String>> status)
    {
        status_.put( url, status );
    }

    public synchronized  Pair<Integer,List<String>> get( String url)
    {
        return status_.get( url );
    }

    Map<  String,  Pair<Integer,List<String>>>  getStatusMap() { return status_;}
}

