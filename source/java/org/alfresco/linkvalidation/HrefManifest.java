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
