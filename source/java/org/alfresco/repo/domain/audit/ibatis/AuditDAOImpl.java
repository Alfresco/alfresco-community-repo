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
package org.alfresco.repo.domain.audit.ibatis;

import org.alfresco.repo.domain.audit.AbstractAuditDAOImpl;
import org.alfresco.repo.domain.audit.AuditConfigEntity;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the DAO for <b>alf_audit_XXX</b> tables.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditDAOImpl extends AbstractAuditDAOImpl
{
    private static final String SELECT_CONFIG_BY_CRC = "select.AuditConfigByCrc";
    private static final String INSERT_CONFIG = "insert.AuditConfig";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected AuditConfigEntity getAuditConfigByCrc(long crc)
    {
        AuditConfigEntity entity = new AuditConfigEntity();
        entity.setContentCrc(crc);
        entity = (AuditConfigEntity) template.queryForObject(
                SELECT_CONFIG_BY_CRC,
                entity);
        // Done
        return entity;
    }

    @Override
    protected AuditConfigEntity createAuditConfig(Long contentDataId, long crc)
    {
        AuditConfigEntity entity = new AuditConfigEntity();
        entity.setContentDataId(contentDataId);
        entity.setContentCrc(crc);
        Long id = (Long) template.insert(INSERT_CONFIG, entity);
        entity.setId(id);
        return entity;
    }
}
