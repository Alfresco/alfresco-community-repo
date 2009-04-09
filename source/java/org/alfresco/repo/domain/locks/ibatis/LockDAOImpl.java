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
package org.alfresco.repo.domain.locks.ibatis;

import java.util.List;

import org.alfresco.repo.domain.locks.AbstractLockDAOImpl;
import org.alfresco.repo.domain.locks.LockEntity;
import org.alfresco.repo.domain.locks.LockResourceEntity;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the Locks DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockDAOImpl extends AbstractLockDAOImpl
{
    private static final Long CONST_LONG_ZERO = new Long(0L);
    private static final String SELECT_LOCKRESOURCE_BY_QNAME = "select.LockResourceByQName";
    private static final String SELECT_LOCK_BY_SHARED_IDS = "select.LockBySharedIds";
    private static final String INSERT_LOCKRESOURCE = "insert.LockResource";
    private static final String INSERT_LOCK = "insert.Lock";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected LockResourceEntity getLockResource(Long qnameNamespaceId, String qnameLocalName)
    {
        LockResourceEntity lockResource = new LockResourceEntity();
        lockResource.setQnameNamespaceId(qnameNamespaceId);
        lockResource.setQnameLocalName(qnameLocalName);
        lockResource = (LockResourceEntity) template.queryForObject(SELECT_LOCKRESOURCE_BY_QNAME, lockResource);
        // Could be null
        return lockResource;
    }

    @Override
    protected LockResourceEntity createLockResource(Long qnameNamespaceId, String qnameLocalName)
    {
        LockResourceEntity lockResource = new LockResourceEntity();
        lockResource.setVersion(CONST_LONG_ZERO);
        lockResource.setQnameNamespaceId(qnameNamespaceId);
        lockResource.setQnameLocalName(qnameLocalName);
        Long id = (Long) template.insert(INSERT_LOCKRESOURCE, lockResource);
        lockResource.setId(id);
        // Done
        return lockResource;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<LockEntity> getLocks(List<Long> lockResourceIds)
    {
        List<LockEntity> locks = template.queryForList(SELECT_LOCK_BY_SHARED_IDS, lockResourceIds);
        // Done
        return locks;
    }

    @Override
    protected LockEntity createLock(
            Long sharedResourceId,
            Long exclusiveResourceId,
            String lockApplicant,
            long timeToLive)
    {
        LockEntity lock = new LockEntity();
        lock.setVersion(CONST_LONG_ZERO);
        lock.setSharedResourceId(sharedResourceId);
        lock.setExclusiveResourceId(exclusiveResourceId);
        lock.setLockHolder(lockApplicant);
        long now = System.currentTimeMillis();
        long exp = now + timeToLive;
        lock.setStartTime(now);
        lock.setExpiryTime(exp);
        Long id = (Long) template.insert(INSERT_LOCK, lock);
        lock.setId(id);
        // Done
        return lock;
    }
}
