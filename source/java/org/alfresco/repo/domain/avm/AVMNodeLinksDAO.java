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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.avm;

import java.util.List;

/**
 * DAO services for 
 *     <b>avm_child_entries</b>,
 *     <b>avm_history_links</b>,
 *     <b>avm_merge_links</b>
 * tables
 *
 * @author janv
 * @since 3.2
 */
public interface AVMNodeLinksDAO
{
    //
    // Node Entries (parent/child)
    //
    
    /**
     * Get an entry by name and parent
     */
    public void createChildEntry(long parentNodeId, String name, long childNodeId);
    
    /**
     * Get all the children of a given parent
     */
    public List<AVMChildEntryEntity> getChildEntriesByParent(long parentNodeId);
    
    /**
     * Get all the ChildEntries corresponding to the given child
     */
    public List<AVMChildEntryEntity> getChildEntriesByChild(long childNodeId);
            
    /**
     * Get an entry by name and parent
     */
    public AVMChildEntryEntity getChildEntry(long parentNodeId, String name);
    
    /**
     * Get all the children of a given parent
     * 
     * NOTE: pattern can use * or % (TBC)
     */
    //public List<AVMChildEntryEntity> findChildEntriesByParent(String parentNodeId, String childNamePattern);
    
    /**
     * Get the entry for a given child in a given parent
     */
    public AVMChildEntryEntity getChildEntry(long parentNodeId, long childNodeId);
    
    /**
     * Delete one
     */
    public void deleteChildEntry(AVMChildEntryEntity childEntryEntity);
    
    /**
     * Delete all children of the given parent
     */
    public void deleteChildEntriesByParent(long parentNodeId);
    
    //
    // MergeLink Entries
    //
    
    public void createMergeLink(long mergeFromNodeId, long mergeToNodeId);
    
    public void deleteMergeLink(long mergeFromNodeId, long mergeToNodeId);
    
    public AVMMergeLinkEntity getMergeLinkByTo(long mergeToNodeId);
    
    public List<AVMMergeLinkEntity> getMergeLinksByFrom(long mergeFromNodeId);
    
    //
    // HistoryLink Entries
    //
    
    public void createHistoryLink(long ancestorNodeId, long descendentNodeId);
    
    public void deleteHistoryLink(long ancestorNodeId, long descendentNodeId);
    
    public AVMHistoryLinkEntity getHistoryLinkByDescendent(long descendentNodeId);
    
    public List<AVMHistoryLinkEntity> getHistoryLinksByAncestor(long ancestorNodeId);
}
