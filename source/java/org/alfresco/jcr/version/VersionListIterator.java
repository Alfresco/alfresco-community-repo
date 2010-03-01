/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
