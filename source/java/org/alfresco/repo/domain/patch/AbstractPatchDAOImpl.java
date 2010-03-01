/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 */
package org.alfresco.repo.domain.patch;

import java.util.List;

import org.alfresco.repo.domain.avm.AVMNodeEntity;


/**
 * Abstract implementation for Patch DAO.
 * <p>
 * This provides additional queries used by patches.
 * 
 * @author janv
 * @since 3.2
 */
public abstract class AbstractPatchDAOImpl implements PatchDAO
{
    public Long getAVMNodesCountWhereNewInStore()
    {
        return getAVMNodeEntitiesCountWhereNewInStore();
    }
    
    protected abstract Long getAVMNodeEntitiesCountWhereNewInStore();
    
    public List<AVMNodeEntity> getEmptyGUIDS(int count)
    {
        // TODO limit results - count is currently ignored
        return getAVMNodeEntitiesWithEmptyGUID();
    }
    
    protected abstract List<AVMNodeEntity> getAVMNodeEntitiesWithEmptyGUID();
    
    public List<AVMNodeEntity> getNullVersionLayeredDirectories(int count)
    {
        // TODO limit results - count is currently ignored
        return getNullVersionLayeredDirectoryNodeEntities();
    }
    
    public List<AVMNodeEntity> getNullVersionLayeredFiles(int count)
    {
        // TODO limit results - count is currently ignored
        return getNullVersionLayeredFileNodeEntities();
    }
    
    protected abstract List<AVMNodeEntity> getNullVersionLayeredDirectoryNodeEntities();
    
    protected abstract List<AVMNodeEntity> getNullVersionLayeredFileNodeEntities();
}
