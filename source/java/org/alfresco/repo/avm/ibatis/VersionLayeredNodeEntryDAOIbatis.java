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

package org.alfresco.repo.avm.ibatis;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.VersionLayeredNodeEntry;
import org.alfresco.repo.avm.VersionLayeredNodeEntryDAO;
import org.alfresco.repo.avm.VersionLayeredNodeEntryImpl;
import org.alfresco.repo.avm.VersionRoot;
import org.alfresco.repo.domain.avm.AVMVersionLayeredNodeEntryEntity;

/**
 * iBATIS DAO wrapper for VersionLayeredNodeEntry
 * 
 * @author janv
 */
class VersionLayeredNodeEntryDAOIbatis implements VersionLayeredNodeEntryDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntryDAO#delete(org.alfresco.repo.avm.VersionRoot)
     */
    public void delete(VersionRoot version)
    {
        AVMDAOs.Instance().newAVMVersionRootDAO.deleteVersionLayeredNodeEntries(version.getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntryDAO#get(org.alfresco.repo.avm.VersionRoot)
     */
    public List<VersionLayeredNodeEntry> get(VersionRoot version)
    {
        List<AVMVersionLayeredNodeEntryEntity> vlneEntities = AVMDAOs.Instance().newAVMVersionRootDAO.getVersionLayeredNodeEntries(version.getId());
        
        List<VersionLayeredNodeEntry> vlnes = new ArrayList<VersionLayeredNodeEntry>(vlneEntities.size());
        for(AVMVersionLayeredNodeEntryEntity vlneEntity : vlneEntities)
        {
            VersionLayeredNodeEntryImpl vlne = new VersionLayeredNodeEntryImpl();
            vlne.setVersion(version);
            vlne.setMd5Sum(vlneEntity.getMd5sum());
            vlne.setPath(vlneEntity.getPath());
            
            vlnes.add(vlne);
        }
        
        return vlnes;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.VersionLayeredNodeEntryDAO#save(org.alfresco.repo.avm.VersionLayeredNodeEntry)
     */
    public void save(VersionLayeredNodeEntry entry)
    {
        AVMDAOs.Instance().newAVMVersionRootDAO.createVersionLayeredNodeEntry(entry.getVersion().getId(), entry.getMd5Sum(), entry.getPath());
    }
}
