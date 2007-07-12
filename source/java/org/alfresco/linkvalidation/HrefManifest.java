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
*  File    HrefManifestEntry.java
*----------------------------------------------------------------------------*/

package  org.alfresco.linkvalidation;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
*  Contains a (possibly filtered) list of the hrefs within a file.
*  Common uses of this class are to fetch the links in a web page
*  or just the broken ones (i.e.: response status 400-599).
*/
public class HrefManifest 
{
    List<HrefManifestEntry> manifest_entries_;
    int base_snapshot_version_;
    int latest_snapshot_version_;
    int base_file_count_;
    int base_link_count_;

    public  HrefManifest()
    {
        manifest_entries_ = new ArrayList<HrefManifestEntry>();
    }

    public  HrefManifest(List<HrefManifestEntry> entries, 
                         int                     base_snapshot_version,
                         int                     latest_snapshot_version,
                         int                     base_file_count,
                         int                     base_link_count)
    {
        manifest_entries_         = entries;
        base_snapshot_version_    = base_snapshot_version;
        latest_snapshot_version_  = latest_snapshot_version;
        base_file_count_          = base_file_count;
        base_link_count_          = base_link_count;
    }

    public int getLatestSnapshotVersion() { return latest_snapshot_version_; }
    public int getBaseSnapshotVersion()   { return base_snapshot_version_; }

    public int getBaseFileCount() { return base_file_count_;}
    public int getBaseLinkCount() { return base_link_count_;}

    public List<HrefManifestEntry>  getManifestEntries() { return manifest_entries_;}

    synchronized void add( HrefManifestEntry entry )
    {
        manifest_entries_.add( entry );
    }
}
