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
package org.alfresco.repo.avm.util;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * A utility to build a (possibly long) String representation of
 * a collection of AVM path,versions. The path,versions can be recovered
 * by VersionPathUnStuffer.
 * @author britt
 */
public final class VersionPathStuffer 
{
    /**
     * The internal buffer.
     */
    private StringBuilder fBuilder;
    
    /**
     * Whether any paths have been added yet.
     */
    private boolean fAnyAdded;
    
    /**
     * Make up one.
     */
    public VersionPathStuffer()
    {
        fBuilder = new StringBuilder();
        fAnyAdded = false;
    }
    
    /**
     * Add a version path expressed by the version and path.
     */
    public VersionPathStuffer add(int version, String path)
    {
        if (fAnyAdded)
        {
            fBuilder.append(';');
        }
        fBuilder.append(path);
        fBuilder.append('@');
        fBuilder.append(version);
        fAnyAdded = true;
        return this;
    }
    
    /**
     * Add a version path expressed as a NodeRef.
     */
    public VersionPathStuffer add(NodeRef nodeRef)
    {
        Pair<Integer, String> versionPath =
            AVMNodeConverter.ToAVMVersionPath(nodeRef);
        add(versionPath.getFirst(), versionPath.getSecond());
        return this;
    }
    
    /**
     * Get the stuffed String version of the Version/Paths contained in this.
     */
    public String toString()
    {
        return fBuilder.toString();
    }
}
