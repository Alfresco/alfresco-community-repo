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
package org.alfresco.repo.domain.locks.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.locks.AbstractLockDAOImpl;
import org.alfresco.repo.domain.locks.LockEntity;
import org.alfresco.repo.domain.locks.LockResourceEntity;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * iBatis-specific implementation of the Locks DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockDAOImpl extends AbstractLockDAOImpl
{
    private static final String SELECT_LOCKRESOURCE_BY_QNAME = "alfresco.lock.select_LockResourceByQName";
    private static final String SELECT_LOCK_BY_ID = "alfresco.lock.select_LockByID";
    private static final String SELECT_LOCK_BY_KEY = "alfresco.lock.select_LockByKey";
    private static final String SELECT_LOCK_BY_SHARED_IDS = "alfresco.lock.select_LockBySharedIds";
    private static final String INSERT_LOCKRESOURCE = "alfresco.lock.insert.insert_LockResource";
    private static final String INSERT_LOCK = "alfresco.lock.insert.insert_Lock";
    private static final String UPDATE_LOCK = "alfresco.lock.update_Lock";
    private static final String UPDATE_EXCLUSIVE_LOCK = "alfresco.lock.update_ExclusiveLock";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    @Override
    protected LockResourceEntity getLockResource(Long qnameNamespaceId, String qnameLocalName)
    {
        LockResourceEntity lockResource = new LockResourceEntity();
        lockResource.setQnameNamespaceId(qnameNamespaceId);
        lockResource.setQnameLocalName(qnameLocalName == null ? null : qnameLocalName.toLowerCase());
        lockResource = (LockResourceEntity) template.selectOne(SELECT_LOCKRESOURCE_BY_QNAME, lockResource);
        // Could be null
        return lockResource;
    }

    @Override
    protected LockResourceEntity createLockResource(Long qnameNamespaceId, String qnameLocalName)
    {
        LockResourceEntity lockResource = new LockResourceEntity();
        lockResource.setVersion(LockEntity.CONST_LONG_ZERO);
        lockResource.setQnameNamespaceId(qnameNamespaceId);
        lockResource.setQnameLocalName(qnameLocalName == null ? null : qnameLocalName.toLowerCase());
        template.insert(INSERT_LOCKRESOURCE, lockResource);
        // Done
        return lockResource;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<LockEntity> getLocksBySharedResourceIds(List<Long> sharedLockResourceIds)
    {
        List<LockEntity> locks = (List<LockEntity>) template.selectList(SELECT_LOCK_BY_SHARED_IDS, sharedLockResourceIds);
        // Done
        return locks;
    }
    
    @Override
    protected LockEntity getLock(Long id)
    {
        LockEntity lock = new LockEntity();
        lock.setId(id);
        lock = (LockEntity) template.selectOne(SELECT_LOCK_BY_ID, lock);
        // Done
        return lock;
    }

    @Override
    protected LockEntity getLock(Long sharedResourceId, Long exclusiveResourceId)
    {
        LockEntity lock = new LockEntity();
        lock.setSharedResourceId(sharedResourceId);
        lock.setExclusiveResourceId(exclusiveResourceId);
        lock = (LockEntity) template.selectOne(SELECT_LOCK_BY_KEY, lock);
        // Done
        return lock;
    }

    @Override
    protected LockEntity createLock(
            Long sharedResourceId,
            Long exclusiveResourceId,
            String lockToken,
            long timeToLive)
    {
        LockEntity lock = new LockEntity();
        lock.setVersion(LockEntity.CONST_LONG_ZERO);
        lock.setSharedResourceId(sharedResourceId);
        lock.setExclusiveResourceId(exclusiveResourceId);
        lock.setLockToken(lockToken == null ? null : lockToken.toLowerCase());
        long now = System.currentTimeMillis();
        long exp = now + timeToLive;
        lock.setStartTime(now);
        lock.setExpiryTime(exp);
        template.insert(INSERT_LOCK, lock);
        // Done
        return lock;
    }

    @Override
    protected LockEntity updateLock(LockEntity lockEntity, String lockToken, long timeToLive)
    {
        LockEntity updateLockEntity = new LockEntity();
        updateLockEntity.setId(lockEntity.getId());
        updateLockEntity.setVersion(lockEntity.getVersion());
        updateLockEntity.incrementVersion();            // Increment the version number
        updateLockEntity.setSharedResourceId(lockEntity.getSharedResourceId());
        updateLockEntity.setExclusiveResourceId(lockEntity.getExclusiveResourceId());
        updateLockEntity.setLockToken(lockToken == null ? null : lockToken.toLowerCase());
        long now = (timeToLive > 0) ? System.currentTimeMillis() : 0L;
        long exp = (timeToLive > 0) ? (now + timeToLive) : 0L;
        updateLockEntity.setStartTime(new Long(now));
        updateLockEntity.setExpiryTime(new Long(exp));
        
        int updated = template.update(UPDATE_LOCK, updateLockEntity);
        if (updated != 1)
        {
            // unexpected number of rows affected
            throw new ConcurrencyFailureException("Incorrect number of rows affected for updateLock: " + updateLockEntity + ": expected 1, actual " + updated);
        }
        
        // Done
        return updateLockEntity;
    }

    @Override
    protected int updateLocks(
            Long exclusiveLockResourceId,
            String oldLockToken,
            String newLockToken,
            long timeToLive)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("exclusiveLockResourceId", exclusiveLockResourceId);
        params.put("oldLockToken", oldLockToken);
        params.put("newLockToken", newLockToken == null ? null : newLockToken.toLowerCase());
        long now = (timeToLive > 0) ? System.currentTimeMillis() : 0L;
        long exp = (timeToLive > 0) ? (now + timeToLive) : 0L;
        params.put("newStartTime", new Long(now));
        params.put("newExpiryTime", new Long(exp));
        int updateCount = template.update(UPDATE_EXCLUSIVE_LOCK, params);
        // Done
        return updateCount;
    }
}
