/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.util.List;

import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.util.NameMatcher;

//Sparta: disable WCM/AVM - temporary (until WCM/AVM has been fully removed)
public class AVMSyncServiceNOOPImpl implements AVMSyncService
{
    /**
     * Basic constructor for the service.
     */
    public AVMSyncServiceNOOPImpl()
    {
    }

    @Override
    public List<AVMDifference> compare(int srcVersion, String srcPath,
            int dstVersion, String dstPath, NameMatcher excluder)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AVMDifference> compare(int srcVersion, String srcPath,
            int dstVersion, String dstPath, NameMatcher excluder,
            boolean expandDirs)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void update(List<AVMDifference> diffList, NameMatcher excluder,
            boolean ignoreConflicts, boolean ignoreOlder,
            boolean overrideConflicts, boolean overrideOlder, String tag,
            String description)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void flatten(String layerPath, String underlyingPath)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resetLayer(String layerPath)
    {
        // TODO Auto-generated method stub
        
    }
}
