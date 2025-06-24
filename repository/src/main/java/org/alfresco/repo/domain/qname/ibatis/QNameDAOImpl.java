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
package org.alfresco.repo.domain.qname.ibatis;

import org.mybatis.spring.SqlSessionTemplate;

import org.alfresco.repo.domain.qname.AbstractQNameDAOImpl;
import org.alfresco.repo.domain.qname.NamespaceEntity;
import org.alfresco.repo.domain.qname.QNameEntity;

/**
 * iBatis-specific extension of the QName and Namespace abstract DAO
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class QNameDAOImpl extends AbstractQNameDAOImpl
{
    private static final String SELECT_NS_BY_ID = "alfresco.qname.select_NamespaceById";
    private static final String SELECT_NS_BY_URI = "alfresco.qname.select_NamespaceByUri";
    private static final String INSERT_NS = "alfresco.qname.insert.insert_Namespace";
    private static final String UPDATE_NS = "alfresco.qname.update_Namespace";
    private static final String SELECT_QNAME_BY_ID = "alfresco.qname.select_QNameById";
    private static final String SELECT_QNAME_BY_NS_AND_LOCALNAME = "alfresco.qname.select_QNameByNsAndLocalName";
    private static final String INSERT_QNAME = "alfresco.qname.insert.insert_QName";
    private static final String UPDATE_QNAME = "alfresco.qname.update_QName";
    private static final String DELETE_QNAME = "alfresco.qname.delete_QName";

    private SqlSessionTemplate template;

    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate)
    {
        this.template = sqlSessionTemplate;
    }

    @Override
    protected NamespaceEntity findNamespaceEntityById(Long id)
    {
        NamespaceEntity entity = new NamespaceEntity();
        entity.setId(id);
        entity = template.selectOne(SELECT_NS_BY_ID, entity);
        return entity;
    }

    @Override
    protected NamespaceEntity findNamespaceEntityByUri(String uri)
    {
        NamespaceEntity entity = new NamespaceEntity();
        entity.setUriSafe(uri);
        entity = template.selectOne(SELECT_NS_BY_URI, entity);
        return entity;
    }

    @Override
    protected NamespaceEntity createNamespaceEntity(String uri)
    {
        NamespaceEntity entity = new NamespaceEntity();
        entity.setVersion(NamespaceEntity.CONST_LONG_ZERO);
        entity.setUriSafe(uri);
        template.insert(INSERT_NS, entity);
        return entity;
    }

    @Override
    protected int updateNamespaceEntity(NamespaceEntity entity, String uri)
    {
        entity.setUriSafe(uri);
        entity.incrementVersion();
        return template.update(UPDATE_NS, entity);
    }

    @Override
    protected QNameEntity findQNameEntityById(Long id)
    {
        QNameEntity entity = new QNameEntity();
        entity.setId(id);
        entity = template.selectOne(SELECT_QNAME_BY_ID, entity);
        return entity;
    }

    @Override
    protected QNameEntity findQNameEntityByNamespaceAndLocalName(Long nsId, String localName)
    {
        QNameEntity entity = new QNameEntity();
        entity.setNamespaceId(nsId);
        entity.setLocalNameSafe(localName);
        entity = template.selectOne(SELECT_QNAME_BY_NS_AND_LOCALNAME, entity);
        return entity;
    }

    @Override
    protected QNameEntity createQNameEntity(Long nsId, String localName)
    {
        QNameEntity entity = new QNameEntity();
        entity.setVersion(QNameEntity.CONST_LONG_ZERO);
        entity.setNamespaceId(nsId);
        entity.setLocalNameSafe(localName);
        template.insert(INSERT_QNAME, entity);
        return entity;
    }

    @Override
    protected int updateQNameEntity(QNameEntity entity, Long nsId, String localName)
    {
        entity.setNamespaceId(nsId);
        entity.setLocalNameSafe(localName);
        entity.incrementVersion();
        return template.update(UPDATE_QNAME, entity);
    }

    @Override
    protected int deleteQNameEntity(QNameEntity entity)
    {
        return template.update(DELETE_QNAME, entity.getId());
    }
}
