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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm.ibatis;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.MergeLink;
import org.alfresco.repo.avm.MergeLinkDAO;
import org.alfresco.repo.avm.MergeLinkImpl;
import org.alfresco.repo.domain.avm.AVMMergeLinkEntity;

/**
 * iBATIS DAO wrapper for MergeLink
 * 
 * @author janv
 */
class MergeLinkDAOIbatis implements MergeLinkDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.MergeLinkDAO#save(org.alfresco.repo.avm.MergeLink)
     */
    public void save(MergeLink link)
    {
        AVMDAOs.Instance().newAVMNodeLinksDAO.createMergeLink(link.getMfrom().getId(), link.getMto().getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.MergeLinkDAO#getByTo(org.alfresco.repo.avm.AVMNode)
     */
    public MergeLink getByTo(AVMNode to)
    {
        AVMMergeLinkEntity mlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getMergeLinkByTo(to.getId());
        
        if (mlEntity == null)
        {
            return null;
        }
        
        AVMNode from = AVMDAOs.Instance().fAVMNodeDAO.getByID(mlEntity.getMergeFromNodeId());
        
        MergeLink ml = new MergeLinkImpl();
        ml.setMfrom(from);
        ml.setMto(to);
        return ml;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.MergeLinkDAO#getByFrom(org.alfresco.repo.avm.AVMNode)
     */
    public List<MergeLink> getByFrom(AVMNode from)
    {
        List<AVMMergeLinkEntity> mlEntities = AVMDAOs.Instance().newAVMNodeLinksDAO.getMergeLinksByFrom(from.getId());
        
        List<MergeLink> mls = new ArrayList<MergeLink>(mlEntities.size());
        for (AVMMergeLinkEntity mlEntity : mlEntities)
        {
            AVMNode to = AVMDAOs.Instance().fAVMNodeDAO.getByID(mlEntity.getMergeToNodeId());
            
            MergeLink ml = new MergeLinkImpl();
            ml.setMfrom(from);
            ml.setMto(to);
            
            mls.add(ml);
        }
        
        return mls;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.MergeLinkDAO#delete(org.alfresco.repo.avm.MergeLink)
     */
    public void delete(MergeLink link)
    {
        AVMDAOs.Instance().newAVMNodeLinksDAO.deleteMergeLink(link.getMfrom().getId(), link.getMto().getId());
    }
}
