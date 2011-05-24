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
import java.util.Map;

import org.alfresco.repo.domain.avm.AbstractAVMLockDAOImpl;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * iBatis-specific implementation of the AVMLock DAO.
 * 
 * @author janv
 * @since 3.4
 */
public class AVMLockDAOImpl extends AbstractAVMLockDAOImpl
{
    private static final String DELETE_MATCHING_AVM_LOCKS_1_KV ="alfresco.avm.delete_PropertyUniqueContextByValuesWithOneKV";
    private static final String DELETE_MATCHING_AVM_LOCKS_0_KV ="alfresco.avm.delete_PropertyUniqueContextByValuesWithNoKV";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    @Override
    protected int deletePropertyUniqueContexts(Long avmLocksValueId, Long avmStoreNameId, String dirPathToMatch, String lockDataStoreKey, String lockDataStoreValue)
    {
        if (dirPathToMatch == null)
        {
            dirPathToMatch = "%";
        }
        else if (! dirPathToMatch.endsWith("%"))
        {
            dirPathToMatch = dirPathToMatch + "%";
        }
        
        if (lockDataStoreKey != null)
        {
            Map<String, Object> params = new HashMap<String, Object>(5);
            params.put("value1PropId", avmLocksValueId);
            params.put("value2PropId", avmStoreNameId);
            params.put("value3LikeStr", dirPathToMatch);
            params.put("lockDataStoreKey", lockDataStoreKey);
            params.put("lockDataStoreValue", lockDataStoreValue);
            
            return template.delete(DELETE_MATCHING_AVM_LOCKS_1_KV, params);
        }
        else
        {
            Map<String, Object> params = new HashMap<String, Object>(3);
            params.put("value1PropId", avmLocksValueId);
            params.put("value2PropId", avmStoreNameId);
            params.put("value3LikeStr", dirPathToMatch);
            
            return template.delete(DELETE_MATCHING_AVM_LOCKS_0_KV, params);
        }
    }
}
