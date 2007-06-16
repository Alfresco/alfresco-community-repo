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
*  File    HrefDifference.java
*----------------------------------------------------------------------------*/

package org.alfresco.linkvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HrefDifference
{
    HrefStatusMap  href_status_map_;    //status of links + maybe dep info
    HrefManifest   href_manifest_;      // overall manifest in of change

    // Hrefs no longer used by the system anywhere
    HashMap<String,String> obsolete_href_md5_;

    HrefManifest broken_in_newmod_;     // errors in new files 
    HrefManifest broken_by_del_;        // errors caused by new deletions
    HrefManifest repaired_by_delmod_;   // fix by removing links (mod or del)
    HrefManifest repaired_by_new_;      // new file satisfies broken dep


    public HrefDifference()
    {
        href_manifest_      = new HrefManifest();
        href_status_map_    = new HrefStatusMap();
        obsolete_href_md5_  = new HashMap<String,String>();

        broken_by_del_      = new HrefManifest();
        broken_in_newmod_   = new HrefManifest();
        repaired_by_delmod_ = new HrefManifest();
        repaired_by_new_    = new HrefManifest();
    }

    public HrefManifest getHrefManifest()   { return href_manifest_;     }
    public HrefStatusMap getHrefStatusMap() { return href_status_map_;   }
    Map<String,String> getObsoleteHrefMd5() { return obsolete_href_md5_; }

    public HrefManifest getBrokenByDeletionHrefManifest( )
    {
        return broken_by_del_;
    }

    public HrefManifest getBrokenInNewModHrefManifest()
    {
        return broken_in_newmod_;
    }

    public HrefManifest getRepairedByDeletionAndModHrefManifest()
    {
        return repaired_by_delmod_;
    }

    public HrefManifest  getRepairedByNewHreManifest()
    {
        return repaired_by_new_;
    }
}

