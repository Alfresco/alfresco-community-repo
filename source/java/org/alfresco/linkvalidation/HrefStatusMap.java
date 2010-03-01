/*-----------------------------------------------------------------------------
*  Copyright 2007-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    HrefStatusMap.java
*----------------------------------------------------------------------------*/

package  org.alfresco.linkvalidation;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.springframework.extensions.surf.util.Pair;
  
/**
*  A synchronized wrapper for the ephemeral cache of href status results.
*  
*  The key is the raw url that was tested (not an md5sum),
*  the value is a pair consisting of the url's status code,
*  and the list of files accessed when the URL is requested,
*  if known.  Note that all url & file data are in the namespace
*  of the proposed changeset (e.g.: the workflow).
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

