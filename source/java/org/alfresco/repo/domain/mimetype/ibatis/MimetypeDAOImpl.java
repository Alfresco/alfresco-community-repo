/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.mimetype.ibatis;

import org.alfresco.repo.domain.mimetype.AbstractMimetypeDAOImpl;
import org.alfresco.repo.domain.mimetype.MimetypeEntity;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

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
    private static final String INSERT_MIMETYPE = "alfresco.content.insert_Mimetype";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected MimetypeEntity getMimetypeEntity(Long id)
    {
        MimetypeEntity mimetypeEntity = new MimetypeEntity();
        mimetypeEntity.setId(id);
        mimetypeEntity = (MimetypeEntity) template.queryForObject(SELECT_MIMETYPE_BY_ID, mimetypeEntity);
        // Done
        return mimetypeEntity;
    }

    @Override
    protected MimetypeEntity getMimetypeEntity(String mimetype)
    {
        MimetypeEntity mimetypeEntity = new MimetypeEntity();
        mimetypeEntity.setMimetype(mimetype == null ? null : mimetype.toLowerCase());
        mimetypeEntity = (MimetypeEntity) template.queryForObject(SELECT_MIMETYPE_BY_KEY, mimetypeEntity);
        // Could be null
        return mimetypeEntity;
    }

    @Override
    protected MimetypeEntity createMimetypeEntity(String mimetype)
    {
        MimetypeEntity mimetypeEntity = new MimetypeEntity();
        mimetypeEntity.setVersion(MimetypeEntity.CONST_LONG_ZERO);
        mimetypeEntity.setMimetype(mimetype == null ? null : mimetype.toLowerCase());
        Long id = (Long) template.insert(INSERT_MIMETYPE, mimetypeEntity);
        mimetypeEntity.setId(id);
        // Done
        return mimetypeEntity;
    }
}
