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

package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.avm.AVMNodeDAO;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.patch.PatchDAO;

/**
 * Patch for changes to Layered Node path traversal.
 * @author britt
 */
public class AVMLayeredSnapshotPatch extends AbstractPatch
{
    private AVMNodeDAO fAVMNodeDAO;
    private PatchDAO patchDAO;
    
    private static final String MSG_SUCCESS = "patch.AVMLayeredSnapshot.result";
    
    public AVMLayeredSnapshotPatch()
    {
    }
    
    public void setAvmNodeDao(AVMNodeDAO dao)
    {
        fAVMNodeDAO = dao;
    }
    
    public void setPatchDao(PatchDAO dao)
    {
        patchDAO = dao;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        while (true)
        {
            List<AVMNodeEntity> batch = patchDAO.getNullVersionLayeredDirectories(200);
            for (AVMNodeEntity nodeEntity : batch)
            {
                nodeEntity.setIndirectionVersion(-1);
                
                fAVMNodeDAO.updateNode(nodeEntity);
            }
            if (batch.size() == 0)
            {
                break;
            }
        }
        while (true)
        {
            List<AVMNodeEntity> batch = patchDAO.getNullVersionLayeredFiles(200);
            for (AVMNodeEntity nodeEntity : batch)
            {
                nodeEntity.setIndirectionVersion(-1);
                
                fAVMNodeDAO.updateNode(nodeEntity);
            }
            if (batch.size() == 0)
            {
                break;
            }
        }
        return I18NUtil.getMessage(MSG_SUCCESS);
    }
}
