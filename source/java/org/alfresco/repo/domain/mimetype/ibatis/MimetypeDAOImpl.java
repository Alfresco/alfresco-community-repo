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
        mimetypeEntity = (MimetypeEntity) template.selectOne(SELECT_MIMETYPE_BY_ID, mimetypeEntity);
        // Done
        return mimetypeEntity;
    }

    @Override
    protected MimetypeEntity getMimetypeEntity(String mimetype)
    {
        MimetypeEntity mimetypeEntity = new MimetypeEntity();
        mimetypeEntity.setMimetype(mimetype == null ? null : mimetype.toLowerCase());
        mimetypeEntity = (MimetypeEntity) template.selectOne(SELECT_MIMETYPE_BY_KEY, mimetypeEntity);
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
