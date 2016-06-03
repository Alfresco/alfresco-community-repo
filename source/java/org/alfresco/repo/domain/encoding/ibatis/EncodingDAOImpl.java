package org.alfresco.repo.domain.encoding.ibatis;

import org.alfresco.repo.domain.encoding.AbstractEncodingDAOImpl;
import org.alfresco.repo.domain.encoding.EncodingEntity;
import org.alfresco.repo.domain.mimetype.MimetypeEntity;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * iBatis-specific implementation of the Mimetype DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class EncodingDAOImpl extends AbstractEncodingDAOImpl
{
    private static final String SELECT_ENCODING_BY_ID = "alfresco.content.select_EncodingById";
    private static final String SELECT_ENCODING_BY_KEY = "alfresco.content.select_EncodingByKey";
    private static final String INSERT_ENCODING = "alfresco.content.insert.insert_Encoding";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    @Override
    protected EncodingEntity getEncodingEntity(Long id)
    {
        EncodingEntity encodingEntity = new EncodingEntity();
        encodingEntity.setId(id);
        encodingEntity = template.selectOne(SELECT_ENCODING_BY_ID, encodingEntity);
        // Done
        return encodingEntity;
    }

    @Override
    protected EncodingEntity getEncodingEntity(String encoding)
    {
        EncodingEntity encodingEntity = new EncodingEntity();
        encodingEntity.setEncoding(encoding == null ? null : encoding.toLowerCase());
        encodingEntity = template.selectOne(SELECT_ENCODING_BY_KEY, encodingEntity);
        // Could be null
        return encodingEntity;
    }

    @Override
    protected EncodingEntity createEncodingEntity(String encoding)
    {
        EncodingEntity encodingEntity = new EncodingEntity();
        encodingEntity.setVersion(MimetypeEntity.CONST_LONG_ZERO);
        encodingEntity.setEncoding(encoding == null ? null : encoding.toLowerCase());
        template.insert(INSERT_ENCODING, encodingEntity);
        // Done
        return encodingEntity;
    }
}
