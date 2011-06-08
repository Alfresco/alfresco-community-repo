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

import org.alfresco.repo.domain.patch.AbstractAppliedPatchDAOImpl;
import org.alfresco.repo.domain.patch.AppliedPatchEntity;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * iBatis-specific implementation of the AppliedPatch DAO.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class AppliedPatchDAOImpl extends AbstractAppliedPatchDAOImpl
{
    private static final String INSERT_APPLIED_PATCH = "alfresco.appliedpatch.insert_AppliedPatch";
    private static final String UPDATE_APPLIED_PATCH = "alfresco.appliedpatch.update_AppliedPatch";
    private static final String SELECT_APPLIED_PATCH_BY_ID = "alfresco.appliedpatch.select_AppliedPatchById";
    private static final String SELECT_ALL_APPLIED_PATCH = "alfresco.appliedpatch.select_AllAppliedPatches";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    @Override
    protected void createAppliedPatchEntity(AppliedPatchEntity entity)
    {
        template.insert(INSERT_APPLIED_PATCH, entity);
    }
    
    public void updateAppliedPatchEntity(AppliedPatchEntity appliedPatch)
    {
        template.update(UPDATE_APPLIED_PATCH, appliedPatch);
    }

    @Override
    protected AppliedPatchEntity getAppliedPatchEntity(String id)
    {
        AppliedPatchEntity entity = new AppliedPatchEntity();
        entity.setId(id);
        entity = (AppliedPatchEntity) template.selectOne(SELECT_APPLIED_PATCH_BY_ID, entity);
        // Could be null
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<AppliedPatchEntity> getAppliedPatchEntities()
    {
        return (List<AppliedPatchEntity>) template.selectList(SELECT_ALL_APPLIED_PATCH);
    }
}
