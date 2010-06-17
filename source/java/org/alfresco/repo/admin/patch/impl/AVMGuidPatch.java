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

package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.avm.AVMNodeDAO;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.util.GUID;

/**
 * This makes sure that all GUIDs in AVM nodes are set.
 * @author britt
 */
public class AVMGuidPatch extends AbstractPatch
{
    private AVMNodeDAO fAVMNodeDAO;
    private PatchDAO patchDAO;
    
    private static final String MSG_SUCCESS = "patch.AVMGuidPatch.result";
    
    public AVMGuidPatch()
    {
    }
    
    public void setAvmNodeDao(AVMNodeDAO dao)
    {
        fAVMNodeDAO = dao;
    }
    
    public void setPatchDAO(PatchDAO dao)
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
            List<AVMNodeEntity> batch = patchDAO.getEmptyGUIDS(200);
            for (AVMNodeEntity nodeEntity : batch)
            {
                nodeEntity.setGuid(GUID.generate());
                
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
