/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
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
