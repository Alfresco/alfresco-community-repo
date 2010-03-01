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
*  File    HrefConcordanceEntry.java
*----------------------------------------------------------------------------*/

package  org.alfresco.linkvalidation;

import java.io.Serializable;
  
/**
*  Contains every location of a given href within a webapp, 
*  along with its response status.
*/
public class HrefConcordanceEntry 
       implements Comparable<HrefConcordanceEntry>, 
                  Serializable
{
    static final long serialVersionUID = -8102602003366089726L;

    String    href_;
    String [] locations_;
    int       response_status_;

    public  HrefConcordanceEntry( String    href, 
                                  String [] locations,
                                  int       responseStatus 
                                )
    {
        href_            = href;
        locations_       = locations;
        response_status_ = responseStatus;
    }

    public String     getHref()           { return href_;            }
    public String []  getLocations()      { return locations_;       }
    public int        getResponseStatus() { return response_status_; }

    public int compareTo(HrefConcordanceEntry  other)
    {
        return href_.compareTo( other.href_ ); 
    }
}

