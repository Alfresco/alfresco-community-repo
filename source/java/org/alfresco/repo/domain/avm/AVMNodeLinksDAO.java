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
     * Get all the children of a given parent (with optional child name pattern)
     */
    public List<AVMChildEntryEntity> getChildEntriesByParent(long parentNodeId, String childNamePattern);
    
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
     * Specific rename 'case' only
     */
    public void updateChildEntry(AVMChildEntryEntity childEntryEntity);
    
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
