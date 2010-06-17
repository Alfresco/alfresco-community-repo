/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.locale.ibatis;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.domain.locale.AbstractLocaleDAOImpl;
import org.alfresco.repo.domain.locale.LocaleEntity;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the Locale DAO.
 * 
 * @author janv
 * @since 3.4
 */
public class LocaleDAOImpl extends AbstractLocaleDAOImpl
{
    private static final String SELECT_LOCALE_BY_ID = "alfresco.locale.select_LocaleById";
    private static final String SELECT_LOCALE_BY_NAME = "alfresco.locale.select_LocaleByName";
    private static final String INSERT_LOCALE = "alfresco.locale.insert_Locale";
    
    private SqlMapClientTemplate template;
    
    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }
    
    @Override
    protected LocaleEntity getLocaleEntity(Long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", id);
        
        return (LocaleEntity) template.queryForObject(SELECT_LOCALE_BY_ID, params);
    }

    @Override
    protected LocaleEntity getLocaleEntity(String localeStr)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("str", localeStr);
        
        return (LocaleEntity) template.queryForObject(SELECT_LOCALE_BY_NAME, params);
    }

    @Override
    protected LocaleEntity createLocaleEntity(String localeStr)
    {
        LocaleEntity localeEntity = new LocaleEntity();
        localeEntity.setVersion(LocaleEntity.CONST_LONG_ZERO);
        localeEntity.setLocaleStr(localeStr);
        
        Long id = (Long) template.insert(INSERT_LOCALE, localeEntity);
        localeEntity.setId(id);
        return localeEntity;
    }
}
