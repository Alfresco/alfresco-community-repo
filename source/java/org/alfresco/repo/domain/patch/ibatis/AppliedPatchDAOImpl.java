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
        entity = template.selectOne(SELECT_APPLIED_PATCH_BY_ID, entity);
        // Could be null
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<AppliedPatchEntity> getAppliedPatchEntities()
    {
        return template.selectList(SELECT_ALL_APPLIED_PATCH);
    }
}
