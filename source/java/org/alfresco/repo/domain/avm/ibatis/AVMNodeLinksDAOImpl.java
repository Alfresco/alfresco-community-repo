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
package org.alfresco.repo.domain.avm.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.avm.AVMChildEntryEntity;
import org.alfresco.repo.domain.avm.AVMHistoryLinkEntity;
import org.alfresco.repo.domain.avm.AVMMergeLinkEntity;
import org.alfresco.repo.domain.avm.AbstractAVMNodeLinksDAOImpl;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * iBatis-specific implementation of the AVMNodeLinks DAO.
 * 
 * @author janv
 * @since 3.2
 */
public class AVMNodeLinksDAOImpl extends AbstractAVMNodeLinksDAOImpl
{
    private static final String SELECT_AVM_NODE_CHILD_ENTRY ="alfresco.avm.select_AVMChildEntry"; // parent + name + child
    private static final String SELECT_AVM_NODE_CHILD_ENTRY_L ="alfresco.avm.select_AVMChildEntryL"; // parent + lower(name) + child
    
    private static final String SELECT_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_NAME ="alfresco.avm.select_AVMChildEntryByParentAndName"; // parent + name
    private static final String SELECT_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_NAME_L ="alfresco.avm.select_AVMChildEntryByParentAndNameL"; // parent + lower(name)
    
    private static final String SELECT_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_CHILD ="alfresco.avm.select_AVMChildEntryByParentAndChild"; // parent + child
    private static final String SELECT_AVM_NODE_CHILD_ENTRIES_BY_PARENT ="alfresco.avm.select_AVMNodeChildEntriesByParent"; // parent
    
    private static final String SELECT_AVM_NODE_CHILD_ENTRIES_BY_PARENT_AND_NAME_PATTERN ="alfresco.avm.select_AVMNodeChildEntriesByParentAndNamePattern"; // parent + name pattern
    private static final String SELECT_AVM_NODE_CHILD_ENTRIES_BY_PARENT_AND_NAME_PATTERN_L ="alfresco.avm.select_AVMNodeChildEntriesByParentAndNamePatternL"; // parent + lower(name pattern)
    
    private static final String SELECT_AVM_NODE_CHILD_ENTRIES_BY_CHILD ="alfresco.avm.select_AVMNodeChildEntriesByChild"; // child
    
    private static final String INSERT_AVM_NODE_CHILD_ENTRY ="alfresco.avm.insert.insert_AVMChildEntry"; // parent + name + child
    
    private static final String UPDATE_AVM_NODE_CHILD_ENTRY ="alfresco.avm.update_AVMChildEntry"; // parent + child (update name)
    
    private static final String DELETE_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_NAME ="alfresco.avm.delete_AVMChildEntryByParentAndName"; // parent + name
    private static final String DELETE_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_NAME_L ="alfresco.avm.delete_AVMChildEntryByParentAndNameL"; // parent + lower(name)
    
    private static final String DELETE_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_CHILD ="alfresco.avm.delete_AVMChildEntryByParentAndChild"; // parent + child
    private static final String DELETE_AVM_NODE_CHILD_ENTRIES_BY_PARENT ="alfresco.avm.delete_AVMNodeChildEntriesByParent"; // parent
    
    private static final String INSERT_AVM_MERGE_LINK ="alfresco.avm.insert.insert_AVMMergeLink";
    private static final String DELETE_AVM_MERGE_LINK ="alfresco.avm.delete_AVMMergeLink";
    
    private static final String SELECT_AVM_MERGE_LINKS_BY_FROM ="alfresco.avm.select_AVMMergeLinksByFrom";
    private static final String SELECT_AVM_MERGE_LINK_BY_TO ="alfresco.avm.select_AVMMergeLinkByTo";
    
    private static final String INSERT_AVM_HISTORY_LINK ="alfresco.avm.insert.insert_AVMHistoryLink";
    private static final String DELETE_AVM_HISTORY_LINK ="alfresco.avm.delete_AVMHistoryLink";
    
    private static final String SELECT_AVM_HISTORY_LINKS_BY_ANCESTOR ="alfresco.avm.select_AVMHistoryLinksByAncestor";
    private static final String SELECT_AVM_HISTORY_LINK_BY_DESCENDENT ="alfresco.avm.select_AVMHistoryLinkByDescendent";
    private static final String SELECT_AVM_HISTORY_LINK ="alfresco.avm.select_AVMHistoryLink";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    // Initial generic fix for ALF-1940 (pending SAIL-349)
    // Note: in order to override to false DB must be setup to be case-insensitive (at least on column avm_child_entries.name)
    private boolean toLower = true;
    
    public void setToLower(boolean toLower)
    {
        this.toLower = toLower;
    }
    
    @Override
    protected AVMChildEntryEntity getChildEntryEntity(AVMChildEntryEntity childEntryEntity)
    {
        if (toLower)
        {
            return (AVMChildEntryEntity) template.selectOne(SELECT_AVM_NODE_CHILD_ENTRY_L, childEntryEntity);
        }
        return (AVMChildEntryEntity) template.selectOne(SELECT_AVM_NODE_CHILD_ENTRY, childEntryEntity);
    }
    
    @Override
    protected AVMChildEntryEntity getChildEntryEntity(long parentNodeId, String name)
    {
        AVMChildEntryEntity childEntryEntity = new AVMChildEntryEntity(parentNodeId, name);
        
        if (toLower)
        {
            return (AVMChildEntryEntity) template.selectOne(SELECT_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_NAME_L, childEntryEntity);
        }
        return (AVMChildEntryEntity) template.selectOne(SELECT_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_NAME, childEntryEntity);
    }
    
    @Override
    protected AVMChildEntryEntity getChildEntryEntity(long parentNodeId, long childNodeId)
    {
        AVMChildEntryEntity childEntryEntity = new AVMChildEntryEntity(parentNodeId, childNodeId);
        return (AVMChildEntryEntity) template.selectOne(SELECT_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_CHILD, childEntryEntity);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMChildEntryEntity> getChildEntryEntitiesByParent(long parentNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", parentNodeId);
        return (List<AVMChildEntryEntity>) template.selectList(SELECT_AVM_NODE_CHILD_ENTRIES_BY_PARENT, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMChildEntryEntity> getChildEntryEntitiesByParent(long parentNodeId, String childNamePattern)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", parentNodeId);
        params.put("pattern", childNamePattern);
        
        if (toLower)
        {
            return (List<AVMChildEntryEntity>) template.selectList(SELECT_AVM_NODE_CHILD_ENTRIES_BY_PARENT_AND_NAME_PATTERN_L, params);
        }
        return (List<AVMChildEntryEntity>) template.selectList(SELECT_AVM_NODE_CHILD_ENTRIES_BY_PARENT_AND_NAME_PATTERN, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMChildEntryEntity> getChildEntryEntitiesByChild(long childNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", childNodeId);
        return (List<AVMChildEntryEntity>) template.selectList(SELECT_AVM_NODE_CHILD_ENTRIES_BY_CHILD, params);
    }
    
    @Override
    protected void createChildEntryEntity(AVMChildEntryEntity childEntryEntity)
    {
        template.insert(INSERT_AVM_NODE_CHILD_ENTRY, childEntryEntity);
    }
    
    @Override
    protected int updateChildEntryEntity(AVMChildEntryEntity childEntryEntity)
    {
        // TODO: concurrency control - note: specific rename 'case' only
        //childEntryEntity.incrementVers();
        
        return template.update(UPDATE_AVM_NODE_CHILD_ENTRY, childEntryEntity);
    }
    
    @Override
    protected int deleteChildEntryEntity(long parentNodeId, String name)
    {
        AVMChildEntryEntity childEntryEntity = new AVMChildEntryEntity(parentNodeId, name);
        
        if (toLower)
        {
            return template.delete(DELETE_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_NAME_L, childEntryEntity);
        }
        return template.delete(DELETE_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_NAME, childEntryEntity);
    }
    
    @Override
    protected int deleteChildEntryEntity(long parentNodeId, long childNodeId)
    {
        AVMChildEntryEntity childEntryEntity = new AVMChildEntryEntity(parentNodeId, childNodeId);
        return template.delete(DELETE_AVM_NODE_CHILD_ENTRY_BY_PARENT_AND_CHILD, childEntryEntity);
    }
    
    @Override
    protected int deleteChildEntryEntities(long parentNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", parentNodeId);
        return template.delete(DELETE_AVM_NODE_CHILD_ENTRIES_BY_PARENT, params);
    }
    
    
    @Override
    protected void createMergeLinkEntity(long mergeFromNodeId, long mergeToNodeId)
    {
        AVMMergeLinkEntity mergeLinkEntity = new AVMMergeLinkEntity(mergeFromNodeId, mergeToNodeId);
        template.insert(INSERT_AVM_MERGE_LINK, mergeLinkEntity);
    }
    
    @Override
    protected int deleteMergeLinkEntity(long mergeFromNodeId, long mergeToNodeId)
    {
        AVMMergeLinkEntity mLinkEntity = new AVMMergeLinkEntity(mergeFromNodeId, mergeToNodeId);
        return template.delete(DELETE_AVM_MERGE_LINK, mLinkEntity);
    }
    
    @Override
    protected AVMMergeLinkEntity getMergeLinkEntityByTo(long mergeToNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", mergeToNodeId);
        return (AVMMergeLinkEntity) template.selectOne(SELECT_AVM_MERGE_LINK_BY_TO, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMMergeLinkEntity> getMergeLinkEntitiesByFrom(long mergeFromNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", mergeFromNodeId);
        return (List<AVMMergeLinkEntity>) template.selectList(SELECT_AVM_MERGE_LINKS_BY_FROM, params);
    }
    
    
    @Override
    protected void createHistoryLinkEntity(long ancestorNodeId, long mergeToNodeId)
    {
        AVMHistoryLinkEntity hLinkEntity = new AVMHistoryLinkEntity(ancestorNodeId, mergeToNodeId);
        template.insert(INSERT_AVM_HISTORY_LINK, hLinkEntity);
    }
    
    @Override
    protected int deleteHistoryLinkEntity(long ancestorNodeId, long descendentNodeId)
    {
        AVMHistoryLinkEntity hLinkEntity = new AVMHistoryLinkEntity(ancestorNodeId, descendentNodeId);
        return template.delete(DELETE_AVM_HISTORY_LINK, hLinkEntity);
    }
    
    @Override
    protected AVMHistoryLinkEntity getHistoryLinkEntityByDescendent(long descendentNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", descendentNodeId);
        return (AVMHistoryLinkEntity) template.selectOne(SELECT_AVM_HISTORY_LINK_BY_DESCENDENT, params);
    }
    
    @Override
    protected AVMHistoryLinkEntity getHistoryLinkEntity(long ancestorNodeId, long descendentNodeId)
    {
        AVMHistoryLinkEntity hLinkEntity = new AVMHistoryLinkEntity(ancestorNodeId, descendentNodeId);
        return (AVMHistoryLinkEntity) template.selectOne(SELECT_AVM_HISTORY_LINK, hLinkEntity);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMHistoryLinkEntity> getHistoryLinkEntitiesByAncestor(long ancestorNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", ancestorNodeId);
        return (List<AVMHistoryLinkEntity>) template.selectList(SELECT_AVM_HISTORY_LINKS_BY_ANCESTOR, params);
    }
}
