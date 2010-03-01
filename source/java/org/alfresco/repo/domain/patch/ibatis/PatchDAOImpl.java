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
package org.alfresco.repo.domain.patch.ibatis;

import java.util.List;

import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.patch.AbstractPatchDAOImpl;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the AVMPatch DAO.
 * 
 * @author janv
 * @since 3.2
 */
public class PatchDAOImpl extends AbstractPatchDAOImpl
{
    private static final String SELECT_AVM_NODE_ENTITIES_COUNT_WHERE_NEW_IN_STORE = "alfresco.avm.select_AVMNodeEntitiesCountWhereNewInStore";
    private static final String SELECT_AVM_NODE_ENTITIES_WITH_EMPTY_GUID = "alfresco.avm.select_AVMNodesWithEmptyGUID";
    private static final String SELECT_AVM_LD_NODE_ENTITIES_NULL_VERSION = "alfresco.avm.select_AVMNodes_nullVersionLayeredDirectories";
    private static final String SELECT_AVM_LF_NODE_ENTITIES_NULL_VERSION = "alfresco.avm.select_AVMNodes_nullVersionLayeredFiles";
    
    private SqlMapClientTemplate template;
    
    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected Long getAVMNodeEntitiesCountWhereNewInStore()
    {
        return (Long) template.queryForObject(SELECT_AVM_NODE_ENTITIES_COUNT_WHERE_NEW_IN_STORE);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getAVMNodeEntitiesWithEmptyGUID()
    {
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_NODE_ENTITIES_WITH_EMPTY_GUID);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getNullVersionLayeredDirectoryNodeEntities()
    {
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_LD_NODE_ENTITIES_NULL_VERSION);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AVMNodeEntity> getNullVersionLayeredFileNodeEntities()
    {
        return (List<AVMNodeEntity>) template.queryForList(SELECT_AVM_LF_NODE_ENTITIES_NULL_VERSION);
    }
}
