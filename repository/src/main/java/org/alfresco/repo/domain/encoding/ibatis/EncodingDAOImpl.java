/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.encoding.ibatis;

import org.mybatis.spring.SqlSessionTemplate;

import org.alfresco.repo.domain.encoding.AbstractEncodingDAOImpl;
import org.alfresco.repo.domain.encoding.EncodingEntity;
import org.alfresco.repo.domain.mimetype.MimetypeEntity;

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
