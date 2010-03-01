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
import org.alfresco.repo.avm.HistoryLink;
import org.alfresco.repo.avm.HistoryLinkDAO;
import org.alfresco.repo.avm.HistoryLinkImpl;
import org.alfresco.repo.domain.avm.AVMHistoryLinkEntity;

/**
 * iBATIS DAO wrapper for HistoryLink
 * 
 * @author janv
 */
class HistoryLinkDAOIbatis implements HistoryLinkDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.HistoryLinkDAO#save(org.alfresco.repo.avm.HistoryLink)
     */
    public void save(HistoryLink link)
    {
        AVMDAOs.Instance().newAVMNodeLinksDAO.createHistoryLink(link.getAncestor().getId(), link.getDescendent().getId());
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.HistoryLinkDAO#getByDescendent(org.alfresco.repo.avm.AVMNode)
     */
    public HistoryLink getByDescendent(AVMNode descendent)
    {
        AVMHistoryLinkEntity hlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getHistoryLinkByDescendent(descendent.getId());
        
        if (hlEntity == null)
        {
            return null;
        }
        
        AVMNode ancestor = AVMDAOs.Instance().fAVMNodeDAO.getByID(hlEntity.getAncestorNodeId());
        
        HistoryLink hl = new HistoryLinkImpl();
        hl.setAncestor(ancestor);
        hl.setDescendent(descendent);
        return hl;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.HistoryLinkDAO#getByAncestor(org.alfresco.repo.avm.AVMNode)
     */
    public List<HistoryLink> getByAncestor(AVMNode ancestor)
    {
        List<AVMHistoryLinkEntity> hlEntities = AVMDAOs.Instance().newAVMNodeLinksDAO.getHistoryLinksByAncestor(ancestor.getId());
        
        List<HistoryLink> hls = new ArrayList<HistoryLink>(hlEntities.size());
        for (AVMHistoryLinkEntity hlEntity : hlEntities)
        {
            AVMNode descendent = AVMDAOs.Instance().fAVMNodeDAO.getByID(hlEntity.getDescendentNodeId());
            
            HistoryLink hl = new HistoryLinkImpl();
            hl.setAncestor(ancestor);
            hl.setDescendent(descendent);
            
            hls.add(hl);
        }
        
        return hls;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.HistoryLinkDAO#delete(org.alfresco.repo.avm.HistoryLink)
     */
    public void delete(HistoryLink link)
    {
        AVMDAOs.Instance().newAVMNodeLinksDAO.deleteHistoryLink(link.getAncestor().getId(), link.getDescendent().getId());
    }
}
