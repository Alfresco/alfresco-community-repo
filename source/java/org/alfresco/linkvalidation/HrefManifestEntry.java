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
*  File    HrefManifestEntry.java
*----------------------------------------------------------------------------*/

package  org.alfresco.linkvalidation;

import java.io.Serializable;
import java.util.List;

/**
*  Contains a (possibly filtered) list of the hrefs within a file.
*  Common uses of this class are to fetch the links in a web page
*  or just the broken ones (i.e.: response status 400-599).
*/
public class HrefManifestEntry implements Serializable
{
    static final long serialVersionUID = 6532525229716576911L;

    protected String       file_;
    protected List<String> hrefs_;

    public  HrefManifestEntry( String         file, 
                                List<String>  hrefs
                             )
    {
        file_   = file;
        hrefs_  = hrefs;
    }

    public String       getFileName()  { return file_; }
    public List<String> getHrefs()     { return hrefs_;}
}
