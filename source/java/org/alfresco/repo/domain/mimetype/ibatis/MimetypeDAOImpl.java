package org.alfresco.repo.domain.mimetype.ibatis;

import org.alfresco.repo.domain.mimetype.AbstractMimetypeDAOImpl;
import org.alfresco.repo.domain.mimetype.MimetypeEntity;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * iBatis-specific implementation of the Mimetype DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class MimetypeDAOImpl extends AbstractMimetypeDAOImpl
{
    private static final String SELECT_MIMETYPE_BY_ID = "alfresco.content.select_MimetypeById";
    private static final String SELECT_MIMETYPE_BY_KEY = "alfresco.content.select_MimetypeByKey";
    private static final String INSERT_MIMETYPE = "alfresco.content.insert.insert_Mimetype";
    private static final String UPDATE_MIMETYPE = "alfresco.content.update_Mimetype";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    @Override
    protected MimetypeEntity getMimetypeEntity(Long id)
    {
        MimetypeEntity mimetypeEntity = new MimetypeEntity();
        mimetypeEntity.setId(id);
        mimetypeEntity = template.selectOne(SELECT_MIMETYPE_BY_ID, mimetypeEntity);
        // Done
        return mimetypeEntity;
    }

    @Override
    protected MimetypeEntity getMimetypeEntity(String mimetype)
    {
        MimetypeEntity mimetypeEntity = new MimetypeEntity();
        mimetypeEntity.setMimetype(mimetype == null ? null : mimetype.toLowerCase());
        mimetypeEntity = template.selectOne(SELECT_MIMETYPE_BY_KEY, mimetypeEntity);
        // Could be null
        return mimetypeEntity;
    }

    @Override
    protected MimetypeEntity createMimetypeEntity(String mimetype)
    {
        MimetypeEntity mimetypeEntity = new MimetypeEntity();
        mimetypeEntity.setVersion(MimetypeEntity.CONST_LONG_ZERO);
        mimetypeEntity.setMimetype(mimetype == null ? null : mimetype.toLowerCase());
        template.insert(INSERT_MIMETYPE, mimetypeEntity);
        // Done
        return mimetypeEntity;
    }

    @Override
    protected int updateMimetypeEntity(Long id, String newMimetype)
    {
        MimetypeEntity mimetypeEntity = getMimetypeEntity(id);
        if (mimetypeEntity == null)
        {
            throw new DataIntegrityViolationException(
                    "Cannot update mimetype as ID doesn't exist: " + id);
        }
        mimetypeEntity.incrementVersion();
        mimetypeEntity.setMimetype(newMimetype);
        return template.update(UPDATE_MIMETYPE, mimetypeEntity);
    }
}
