package org.alfresco.repo.domain.locale.ibatis;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.domain.locale.AbstractLocaleDAOImpl;
import org.alfresco.repo.domain.locale.LocaleEntity;
import org.mybatis.spring.SqlSessionTemplate;

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
    private static final String INSERT_LOCALE = "alfresco.locale.insert.insert_Locale";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    @Override
    protected LocaleEntity getLocaleEntity(Long id)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", id);
        
        return template.selectOne(SELECT_LOCALE_BY_ID, params);
    }

    @Override
    protected LocaleEntity getLocaleEntity(String localeStr)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("str", localeStr);
        
        return template.selectOne(SELECT_LOCALE_BY_NAME, params);
    }

    @Override
    protected LocaleEntity createLocaleEntity(String localeStr)
    {
        LocaleEntity localeEntity = new LocaleEntity();
        localeEntity.setVersion(LocaleEntity.CONST_LONG_ZERO);
        localeEntity.setLocaleStr(localeStr);
        
        template.insert(INSERT_LOCALE, localeEntity);
        return localeEntity;
    }
}
