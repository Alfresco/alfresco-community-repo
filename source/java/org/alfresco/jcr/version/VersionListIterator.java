/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.version;

import java.util.List;

import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.alfresco.jcr.util.AbstractRangeIterator;


/**
 * Alfresco implementation of a Property Iterator
 * 
 * @author David Caruana
 */
public class VersionListIterator extends AbstractRangeIterator
    implements VersionIterator
{
    private VersionHistoryImpl versionHistory;
    private List<org.alfresco.service.cmr.version.Version> versions;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param versions  version list
     */
    public VersionListIterator(VersionHistoryImpl versionHistory, List<org.alfresco.service.cmr.version.Version> versions)
    {
        this.versionHistory = versionHistory;
        this.versions = versions;
    }
    
    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.VersionIterator#nextVersion()
     */
    public Version nextVersion()
    {
        long position = skip();
        org.alfresco.service.cmr.version.Version version = versions.get((int)position);
        return new VersionImpl(versionHistory, version).getProxy();
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return versions.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextVersion();
    }

}
