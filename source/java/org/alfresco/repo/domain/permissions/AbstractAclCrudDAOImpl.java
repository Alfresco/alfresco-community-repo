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
package org.alfresco.repo.domain.permissions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;
import org.alfresco.repo.domain.CrcHelper;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.permissions.ACEType;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.ParameterCheck;


/**
 * Abstract implementation for ACL crud DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations for:
 * 
 *     <b>alf_access_control_list</b>
 *     <b>alf_acl_member</b>
 *     <b>alf_acl_change_set</b>
 *     <b>alf_access_control_entry</b>
 *     <b>alf_permission</b>
 *     <b>alf_authority</b>
 *     
 * Also, following are currently unused:
 * 
 *     <b>alf_ace_context</b>
 *     <b>alf_authority_alias</b>
 *     
 *     
 * 
 * @author janv
 * @since 3.4
 */
public abstract class AbstractAclCrudDAOImpl implements AclCrudDAO
{
    private static final String CACHE_REGION_ACL = "Acl";
    private static final String CACHE_REGION_AUTHORITY = "Authority";
    private static final String CACHE_REGION_PERMISSION = "Permission";
    
    private final AclEntityCallbackDAO aclEntityDaoCallback;
    private final AuthorityEntityCallbackDAO authorityEntityDaoCallback;
    private final PermissionEntityCallbackDAO permissionEntityDaoCallback;
    
    private QNameDAO qnameDAO;
    private static int batchSize = 500;
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    public void setBatchSize(int batchSizeOverride)
    {
        batchSize = batchSizeOverride;
    }
    
    /**
     * Cache for the ACL entity:<br/>
     * KEY: ID (ACL)<br/>
     * VALUE: AclEntity<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<Long, AclEntity, Serializable> aclEntityCache;
    
    /**
     * Cache for the Authority entity:<br/>
     * KEY: ID (Authority)<br/>
     * VALUE: AuthorityEntity<br/>
     * VALUE KEY: Name<br/>
     */
    private EntityLookupCache<Long, AuthorityEntity, String> authorityEntityCache;
    
    /**
     * Cache for the Permission entity:<br/>
     * KEY: ID (Permission)<br/>
     * VALUE: PermissionEntity<br/>
     * VALUE KEY: PermissionEntity (compound key: qnameId + name)<br/>
     */
    private EntityLookupCache<Long, PermissionEntity, PermissionEntity> permissionEntityCache;
    
    /**
     * Set the cache to use for <b>alf_access_control_list</b> lookups (optional).
     * 
     * @param aclEntityCache            the cache of IDs to AclEntities
     */
    public void setAclEntityCache(SimpleCache<Serializable, Object> aclEntityCache)
    {
        this.aclEntityCache = new EntityLookupCache<Long, AclEntity, Serializable>(
                aclEntityCache,
                CACHE_REGION_ACL,
                aclEntityDaoCallback);
    }
    
    /**
     * Set the cache to use for <b>alf_authority</b> lookups (optional).
     * 
     * @param authorityEntityCache      the cache of IDs to AclEntities
     */
    public void setAuthorityEntityCache(SimpleCache<Serializable, Object> authorityEntityCache)
    {
        this.authorityEntityCache = new EntityLookupCache<Long, AuthorityEntity, String>(
                authorityEntityCache,
                CACHE_REGION_AUTHORITY,
                authorityEntityDaoCallback);
    }
    
    /**
     * Set the cache to use for <b>alf_permission</b> lookups (optional).
     * 
     * @param permissionEntityCache     the cache of IDs to PermissionEntities
     */
    public void setPermissionEntityCache(SimpleCache<Serializable, Object> permissionEntityCache)
    {
        this.permissionEntityCache = new EntityLookupCache<Long, PermissionEntity, PermissionEntity>(
                permissionEntityCache,
                CACHE_REGION_PERMISSION,
                permissionEntityDaoCallback);
    }
    
    
    /**
     * Default constructor.
     * <p>
     * This sets up the DAO accessors to bypass any caching to handle the case where the caches are not
     * supplied in the setters.
     */
    public AbstractAclCrudDAOImpl()
    {
        this.aclEntityDaoCallback = new AclEntityCallbackDAO();
        this.aclEntityCache = new EntityLookupCache<Long, AclEntity, Serializable>(aclEntityDaoCallback);
        
        this.authorityEntityDaoCallback = new AuthorityEntityCallbackDAO();
        this.authorityEntityCache = new EntityLookupCache<Long, AuthorityEntity, String>(authorityEntityDaoCallback);
        
        this.permissionEntityDaoCallback = new PermissionEntityCallbackDAO();
        this.permissionEntityCache = new EntityLookupCache<Long, PermissionEntity, PermissionEntity>(permissionEntityDaoCallback);
    }
    
    //
    // Access Control List (ACL)
    //
    
    public AclEntity createAcl(AclEntity entity)
    {
        ParameterCheck.mandatory("entity", entity);
        
        ParameterCheck.mandatory("entity.aclId", entity.getAclId());
        ParameterCheck.mandatory("entity.aclVersion", entity.getAclVersion());
        
        entity.setVersion(0L);
        
        Pair<Long, AclEntity> entityPair = aclEntityCache.getOrCreateByValue(entity);
        return entityPair.getSecond();
    }
    
    public Acl getAcl(long id)
    {
        return getAclImpl(id);
    }
    
    private AclEntity getAclImpl(long id)
    {
        Pair<Long, AclEntity> entityPair = aclEntityCache.getByKey(id);
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    public AclUpdateEntity getAclForUpdate(long id)
    {
        AclEntity acl = getAclImpl(id);
        if (acl == null)
        {
            return null;
        }
        
        // copy for update
        AclUpdateEntity aclEntity = new AclUpdateEntity();
        aclEntity.setId(acl.getId());
        aclEntity.setVersion(acl.getVersion());
        aclEntity.setAclChangeSetId(acl.getAclChangeSetId());
        aclEntity.setAclId(acl.getAclId());
        aclEntity.setAclType(acl.getAclType());
        aclEntity.setAclVersion(acl.getAclVersion());
        aclEntity.setInheritedAcl(acl.getInheritedAcl());
        aclEntity.setInherits(acl.getInherits());
        aclEntity.setInheritsFrom(acl.getInheritsFrom());
        aclEntity.setLatest(acl.isLatest());
        aclEntity.setVersioned(acl.isVersioned());
        aclEntity.setRequiresVersion(acl.getRequiresVersion());
        
        return aclEntity;
    }
    
    public List<Long> getAclsThatInheritFromAcl(long aclEntityId)
    {
        // not cached
        return getAclEntitiesThatInheritFromAcl(aclEntityId);
    }
    
    public Long getLatestAclByGuid(String aclGuid)
    {
        // not cached
        return getLatestAclEntityByGuid(aclGuid);
    }
    
    public List<Long> getADMNodesByAcl(long aclEntityId, int maxResults)
    {
        return getADMNodeEntityIdsByAcl(aclEntityId, maxResults);
    }
    
    public List<Long> getAVMNodesByAcl(long aclEntityId, int maxResults)
    {
        return getAVMNodeEntityIdsByAcl(aclEntityId, maxResults);
    }
    
    public void updateAcl(AclUpdateEntity entity)
    {
        ParameterCheck.mandatory("entity", entity);
        ParameterCheck.mandatory("entity.id", entity.getId());
        ParameterCheck.mandatory("entity.aclVersion", entity.getAclVersion());
        ParameterCheck.mandatory("entity.version", entity.getVersion());
        
        int updated = aclEntityCache.updateValue(entity.getId(), entity);
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("AclEntity with ID (" + entity.getId() + ") no longer exists or has been updated concurrently");
        }
    }
    
    public void deleteAcl(long id)
    {
        Pair<Long, AclEntity> entityPair = aclEntityCache.getByKey(id);
        if (entityPair == null)
        {
            return;
        }
        
        int deleted = aclEntityCache.deleteByKey(id);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AclEntity with ID " + id + " no longer exists");
        }
    }
    
    /**
     * Callback for <b>alf_access_control_list</b> DAO
     */
    private class AclEntityCallbackDAO implements EntityLookupCallbackDAO<Long, AclEntity, Serializable>
    {
        private final Pair<Long, AclEntity> convertEntityToPair(AclEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, AclEntity>(entity.getId(), entity);
            }
        }
        
        public Serializable getValueKey(AclEntity value)
        {
            return null;
        }
        
        public Pair<Long, AclEntity> createValue(AclEntity value)
        {
            AclEntity entity = createAclEntity(value);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AclEntity> findByKey(Long key)
        {
            AclEntity entity = getAclEntity(key);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AclEntity> findByValue(AclEntity value)
        {
            if ((value != null) && (value.getId() != null))
            {
                return findByKey(value.getId());
            }
            return null;
        }
        
        public int updateValue(Long key, AclEntity value)
        {
            return updateAclEntity(value);
        }
        
        public int deleteByKey(Long key)
        {
            return deleteAclEntity(key);
        }
        
        public int deleteByValue(AclEntity value)
        {
            throw new UnsupportedOperationException("deleteByValue");
        }
    }
    
    protected abstract AclEntity createAclEntity(AclEntity entity);
    protected abstract AclEntity getAclEntity(long id);
    protected abstract List<Long> getAclEntitiesThatInheritFromAcl(long idOfAcl);
    protected abstract Long getLatestAclEntityByGuid(String aclGuid);
    protected abstract int updateAclEntity(AclEntity entity);
    protected abstract int deleteAclEntity(long id);
    
    protected abstract List<Long> getADMNodeEntityIdsByAcl(long aclEntityId, int maxResults);
    protected abstract List<Long> getAVMNodeEntityIdsByAcl(long aclEntityId, int maxResults);
    
    //
    // ACL Member
    //
    
    public void addAclMembersToAcl(long aclId, List<Long> aceIds, int depth)
    {
        ParameterCheck.mandatory("aceIds", aceIds);
        
        List<AclMemberEntity> newMembers = new ArrayList<AclMemberEntity>(aceIds.size());
        
        for (Long aceId : aceIds)
        {
            AclMemberEntity newMember = new AclMemberEntity();
            newMember.setAclId(aclId);
            newMember.setAceId(aceId);
            newMember.setPos(depth);
            
            AclMemberEntity result = createAclMemberEntity(newMember);
            newMembers.add(result);
        }
    }
    
    public void addAclMembersToAcl(long aclId, List<Pair<Long, Integer>> aceIdsWithDepths)
    {
        ParameterCheck.mandatory("aceIdsWithDepths", aceIdsWithDepths);
        
        List<AclMemberEntity> newMembers = new ArrayList<AclMemberEntity>(aceIdsWithDepths.size());
        
        for (Pair<Long,Integer> aceIdWithDepth : aceIdsWithDepths)
        {
            AclMemberEntity newMember = new AclMemberEntity();
            newMember.setAclId(aclId);
            newMember.setAceId(aceIdWithDepth.getFirst());
            newMember.setPos(aceIdWithDepth.getSecond());
            
            AclMemberEntity result = createAclMemberEntity(newMember);
            newMembers.add(result);
        }
    }
    
    public List<AclMember> getAclMembersByAcl(long idOfAcl)
    {
        List<AclMemberEntity> entities = getAclMemberEntitiesByAcl(idOfAcl);
        List<AclMember> result = new ArrayList<AclMember>(entities.size());
        result.addAll(entities);
        return result;
    }
    
    public List<AclMemberEntity> getAclMembersByAclForUpdate(long idOfAcl)
    {
        List<AclMemberEntity> members = getAclMemberEntitiesByAcl(idOfAcl);
        List<AclMemberEntity> membersForUpdate = new ArrayList<AclMemberEntity>(members.size());
        for (AclMemberEntity member : members)
        {
            AclMemberEntity newMember = new AclMemberEntity();
            newMember.setId(member.getId());
            newMember.setVersion(member.getVersion());
            newMember.setAceId(member.getAceId());
            newMember.setAclId(member.getAclId());
            newMember.setPos(member.getPos());
            membersForUpdate.add(newMember);
        }
        return membersForUpdate;
    }
    
    public List<AclMember> getAclMembersByAuthority(String authorityName)
    {
        List<AclMemberEntity> entities = getAclMemberEntitiesByAuthority(authorityName);
        List<AclMember> result = new ArrayList<AclMember>(entities.size());
        result.addAll(entities);
        return result;
    }
    
    public void updateAclMember(AclMemberEntity entity)
    {
        ParameterCheck.mandatory("entity", entity);
        ParameterCheck.mandatory("entity.id", entity.getId());
        ParameterCheck.mandatory("entity.version", entity.getVersion());
        ParameterCheck.mandatory("entity.aceId", entity.getAceId());
        ParameterCheck.mandatory("entity.aclId", entity.getAclId());
        ParameterCheck.mandatory("entity.pos", entity.getPos());
        
        int updated = updateAclMemberEntity(entity);
        
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("AclMemberEntity with ID (" + entity.getId() + ") no longer exists or has been updated concurrently");
        }
    }
    
    public int deleteAclMembers(List<Long> aclMemberIds)
    {
        int totalDeletedCount = 0;
        
        if (aclMemberIds.size() == 0)
        {
            return 0;
        }
        else if (aclMemberIds.size() <=  batchSize)
        {
            totalDeletedCount = deleteAclMemberEntities(aclMemberIds);
        }
        else
        {
            Iterator<Long> idIterator = aclMemberIds.iterator();
            List<Long> batchIds = new ArrayList<Long>(batchSize);
            
            while (idIterator.hasNext())
            {
                Long id = idIterator.next();
                batchIds.add(id);
                
                if (batchIds.size() == batchSize || (! idIterator.hasNext()))
                {
                    int batchDeletedCount = deleteAclMemberEntities(batchIds);
                    
                    totalDeletedCount = totalDeletedCount + batchDeletedCount;
                    batchIds.clear();
                }
            }
        }
        
        // TODO manually update the cache
        
        return totalDeletedCount;
    }
    
    public int deleteAclMembersByAcl(long idOfAcl)
    {
        return deleteAclMemberEntitiesByAcl(idOfAcl);
    }
    
    protected abstract AclMemberEntity createAclMemberEntity(AclMemberEntity entity);
    protected abstract List<AclMemberEntity> getAclMemberEntitiesByAcl(long idOfAcl);
    protected abstract List<AclMemberEntity> getAclMemberEntitiesByAuthority(String authorityName);
    protected abstract int updateAclMemberEntity(AclMemberEntity entity);
    protected abstract int deleteAclMemberEntities(List<Long> aclMemberIds);
    protected abstract int deleteAclMemberEntitiesByAcl(long idOfAcl);
    
    //
    // ACL Change Set
    //
    
    public long createAclChangeSet()
    {
        return createAclChangeSetEntity();
    }
    
    public AclChangeSetEntity getAclChangeSet(long changeSetId)
    {
        return getAclChangeSetEntity(changeSetId);
    }
    
    public void deleteAclChangeSet(long changeSetId)
    {
        deleteAclChangeSetEntity(changeSetId);
    }
    
    protected abstract long createAclChangeSetEntity();
    protected abstract AclChangeSetEntity getAclChangeSetEntity(long changeSetId);
    protected abstract int deleteAclChangeSetEntity(long id);
    
    
    //
    // Access Control Entry (ACE)
    //
    
    public Ace createAce(Permission permission, Authority authority, ACEType type, AccessStatus accessStatus)
    {
        ParameterCheck.mandatory("permission", permission);
        ParameterCheck.mandatory("authority", authority);
        ParameterCheck.mandatory("type", type);
        ParameterCheck.mandatory("accessStatus", accessStatus);
        
        AceEntity entity = new AceEntity();
        
        entity.setApplies(type.getId()); // note: 'applies' stores the ACE type
        entity.setAllowed((accessStatus == AccessStatus.ALLOWED) ? true : false);
        entity.setAuthorityId(authority.getId());
        entity.setPermissionId(permission.getId());
        
        long aceId = createAceEntity(entity);
        
        entity.setVersion(0L);
        entity.setId(aceId);
        
        return entity;
    }
    
    public Ace getAce(Permission permission, Authority authority, ACEType type, AccessStatus accessStatus)
    {
        ParameterCheck.mandatory("permission", permission);
        ParameterCheck.mandatory("authority", authority);
        ParameterCheck.mandatory("type", type);
        ParameterCheck.mandatory("accessStatus", accessStatus);
        
        return getAceEntity(permission.getId(),
                            authority.getId(),
                            ((accessStatus == AccessStatus.ALLOWED) ? true : false), 
                            type);
    }
    
    public Ace getAce(long aceEntityId)
    {
        return getAceEntity(aceEntityId);
    }
    
    public Ace getOrCreateAce(Permission permission, Authority authority, ACEType type, AccessStatus accessStatus)
    {
        Ace entity = getAce(permission, authority, type, accessStatus);
        if (entity == null)
        {
            entity = createAce(permission, authority, type, accessStatus);
        }
        return entity;
    }
    
    public List<Ace> getAcesByAuthority(long authorityId)
    {
        return (List<Ace>)getAceEntitiesByAuthority(authorityId);
    }
    
    public List<Map<String, Object>> getAcesAndAuthoritiesByAcl(long idOfAcl)
    {
        return getAceAndAuthorityEntitiesByAcl(idOfAcl);
    }
    
    public int deleteAces(List<Long> aceIds)
    {
        int totalDeletedCount = 0;
        
        if (aceIds.size() == 0)
        {
            return 0;
        }
        else if (aceIds.size() <=  batchSize)
        {
            totalDeletedCount = deleteAceEntities(aceIds);
        }
        else
        {
            Iterator<Long> idIterator = aceIds.iterator();
            List<Long> batchIds = new ArrayList<Long>(batchSize);
            
            while (idIterator.hasNext())
            {
                Long id = idIterator.next();
                batchIds.add(id);
                
                if (batchIds.size() == batchSize || (! idIterator.hasNext()))
                {
                    int batchDeletedCount = deleteAceEntities(batchIds);
                    
                    totalDeletedCount = totalDeletedCount + batchDeletedCount;
                    batchIds.clear();
                }
            }
        }
        
        return totalDeletedCount;
    }
    
    protected abstract long createAceEntity(AceEntity entity);
    protected abstract AceEntity getAceEntity(long aceEntityId);
    protected abstract AceEntity getAceEntity(long permissionId, long authorityId, boolean allowed, ACEType type);
    protected abstract List<Ace> getAceEntitiesByAuthority(long authorityId);
    protected abstract List<Map<String, Object>> getAceAndAuthorityEntitiesByAcl(long idOfAcl);
    protected abstract int deleteAceEntities(List<Long> aceIds);
    
    //
    // Permission
    //
    
    public Permission createPermission(PermissionReference permissionReference)
    {
        ParameterCheck.mandatory("permissionReference", permissionReference);
        
        PermissionEntity entity = null;
        
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getOrCreateQName(permissionReference.getQName());
        if (qnamePair != null)
        {
            Long qnameId  = qnamePair.getFirst();
            entity = new PermissionEntity(qnameId, permissionReference.getName());
            
            entity.setVersion(0L);
            
            Pair<Long, PermissionEntity> entityPair = permissionEntityCache.getOrCreateByValue(entity);
            entity = entityPair.getSecond();
        }
        return entity;
    }
    
    public Permission getPermission(long id)
    {
        Pair<Long, PermissionEntity> entityPair = permissionEntityCache.getByKey(id);
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    public Permission getPermission(PermissionReference permissionReference)
    {
        return getPermissionImpl(permissionReference);
    }
    
    private PermissionEntity getPermissionImpl(PermissionReference permissionReference)
    {
        ParameterCheck.mandatory("permissionReference", permissionReference);
        
        PermissionEntity entity = null;
        
        // Get the persistent ID for the QName
        Pair<Long, QName> qnamePair = qnameDAO.getOrCreateQName(permissionReference.getQName());
        if (qnamePair != null)
        {
            Long qnameId  = qnamePair.getFirst();
            
            PermissionEntity permission = new PermissionEntity(qnameId, permissionReference.getName());
            Pair<Long, PermissionEntity> entityPair = permissionEntityCache.getByValue(permission);
            if (entityPair != null)
            {
                entity = entityPair.getSecond();
            }
        }
        
        return entity;
    }
    
    private PermissionEntity getPermissionForUpdate(PermissionReference permissionReference)
    {
        PermissionEntity perm = getPermissionImpl(permissionReference);
        
        PermissionEntity newPerm = new PermissionEntity();
        newPerm.setId(perm.getId());
        newPerm.setVersion(perm.getVersion());
        newPerm.setTypeQNameId(perm.getTypeQNameId());
        newPerm.setName(perm.getName());
        
        return newPerm;
    }
    
    public Permission getOrCreatePermission(PermissionReference permissionReference)
    {
        Permission entity = getPermission(permissionReference);
        if (entity == null)
        {
            entity = createPermission(permissionReference);
        }
        return entity;
    }
    
    public void renamePermission(QName oldTypeQName, String oldName, QName newTypeQName, String newName)
    {
        ParameterCheck.mandatory("oldTypeQName", oldTypeQName);
        ParameterCheck.mandatory("oldName", oldName);
        ParameterCheck.mandatory("newTypeQName", newTypeQName);
        ParameterCheck.mandatory("newName", newName);
        
        if (oldTypeQName.equals(newTypeQName) && oldName.equals(newName))
        {
            throw new IllegalArgumentException("Cannot move permission to itself: " + oldTypeQName + "-" + oldName);
        }
        
        SimplePermissionReference oldPermRef = SimplePermissionReference.getPermissionReference(oldTypeQName, oldName);
        PermissionEntity permission = getPermissionForUpdate(oldPermRef);
        if (permission != null)
        {
            Long newTypeQNameId = qnameDAO.getOrCreateQName(newTypeQName).getFirst();
            permission.setTypeQNameId(newTypeQNameId);
            permission.setName(newName);
            
            int updated = permissionEntityCache.updateValue(permission.getId(), permission);
            if (updated < 1)
            {
                throw new ConcurrencyFailureException("PermissionEntity with ID (" + permission.getId() + ") no longer exists or has been updated concurrently");
            }
        }
    }
    
    public void deletePermission(long id)
    {
        Pair<Long, PermissionEntity> entityPair = permissionEntityCache.getByKey(id);
        if (entityPair == null)
        {
            return;
        }
        
        int deleted = permissionEntityCache.deleteByKey(id);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("PermissionEntity with ID " + id + " no longer exists");
        }
    }
    
    /**
     * Callback for <b>alf_permission</b> DAO
     */
    private class PermissionEntityCallbackDAO implements EntityLookupCallbackDAO<Long, PermissionEntity, PermissionEntity>
    {
        private final Pair<Long, PermissionEntity> convertEntityToPair(PermissionEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, PermissionEntity>(entity.getId(), entity);
            }
        }
        
        public PermissionEntity getValueKey(PermissionEntity value)
        {
            return value;
        }
        
        public Pair<Long, PermissionEntity> createValue(PermissionEntity value)
        {
            PermissionEntity entity = createPermissionEntity(value);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, PermissionEntity> findByKey(Long key)
        {
            PermissionEntity entity = getPermissionEntity(key);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, PermissionEntity> findByValue(PermissionEntity value)
        {
            if ((value == null) || (value.getName() == null) || (value.getTypeQNameId() == null))
            {
                throw new AlfrescoRuntimeException("Unexpected: PermissionEntity / name / qnameId must not be null");
            }
            return convertEntityToPair(getPermissionEntity(value.getTypeQNameId(), value.getName()));
        }
        
        public int updateValue(Long key, PermissionEntity value)
        {
            return updatePermissionEntity(value);
        }
        
        public int deleteByKey(Long key)
        {
            return deletePermissionEntity(key);
        }
        
        public int deleteByValue(PermissionEntity value)
        {
            throw new UnsupportedOperationException("deleteByValue");
        }
    }
    
    protected abstract PermissionEntity createPermissionEntity(PermissionEntity entity);
    protected abstract PermissionEntity getPermissionEntity(long id);
    protected abstract PermissionEntity getPermissionEntity(long qnameId, String name);
    protected abstract int updatePermissionEntity(PermissionEntity updateEntity);
    protected abstract int deletePermissionEntity(long id);
    
    //
    // Authority
    //
    
    public Authority createAuthority(String authorityName)
    {
        ParameterCheck.mandatory("authorityName", authorityName);
        
        AuthorityEntity entity = new AuthorityEntity();
        entity.setAuthority(authorityName);
        entity.setCrc(CrcHelper.getStringCrcPair(authorityName, 32, true, true).getSecond());
        
        entity.setVersion(0L);
        
        Pair<Long, AuthorityEntity> entityPair = authorityEntityCache.getOrCreateByValue(entity);
        return entityPair.getSecond();
    }
    
    public Authority getAuthority(long id)
    {
        Pair<Long, AuthorityEntity> entityPair = authorityEntityCache.getByKey(id);
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    public Authority getAuthority(String authorityName)
    {
        return getAuthorityImpl(authorityName);
    }
    
    private AuthorityEntity getAuthorityImpl(String authorityName)
    {
        ParameterCheck.mandatory("authorityName", authorityName);
        
        AuthorityEntity authority = new AuthorityEntity();
        authority.setAuthority(authorityName);
        
        Pair<Long, AuthorityEntity> entityPair = authorityEntityCache.getByValue(authority);
        if (entityPair == null)
        {
            return null;
        }
        return entityPair.getSecond();
    }
    
    private AuthorityEntity getAuthorityForUpdate(String authorityName)
    {
        AuthorityEntity auth = getAuthorityImpl(authorityName);
        
        if (auth == null)
        {
            return null;
        }
        
        AuthorityEntity newAuth = new AuthorityEntity();
        newAuth.setId(auth.getId());
        newAuth.setVersion(auth.getVersion());
        newAuth.setAuthority(auth.getAuthority());
        newAuth.setCrc(auth.getCrc());
        return newAuth;
    }
    
    public Authority getOrCreateAuthority(String name)
    {
        Authority entity = getAuthority(name);
        
        if (entity == null)
        {
            entity = createAuthority(name);
        }
        
        return entity;
    }
    
    public void renameAuthority(String before, String after)
    {
        ParameterCheck.mandatory("before", before);
        ParameterCheck.mandatory("after", after);
        
        AuthorityEntity entity = getAuthorityForUpdate(before);
        
        if (entity != null)
        {
            entity.setAuthority(after);
            entity.setCrc(CrcHelper.getStringCrcPair(after, 32, true, true).getSecond());
            
            int updated = authorityEntityCache.updateValue(entity.getId(), entity);
            if (updated < 1)
            {
                throw new ConcurrencyFailureException("AuthorityEntity with ID (" + entity.getId() + ") no longer exists or has been updated concurrently");
            }
        }
    }
    
    public void deleteAuthority(long id)
    {
        Pair<Long, AuthorityEntity> entityPair = authorityEntityCache.getByKey(id);
        if (entityPair == null)
        {
            return;
        }
        
        int deleted = authorityEntityCache.deleteByKey(id);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("AuthorityEntity with ID " + id + " no longer exists");
        }
    }
    
    /**
     * Callback for <b>alf_authority</b> DAO
     */
    private class AuthorityEntityCallbackDAO implements EntityLookupCallbackDAO<Long, AuthorityEntity, String>
    {
        private final Pair<Long, AuthorityEntity> convertEntityToPair(AuthorityEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<Long, AuthorityEntity>(entity.getId(), entity);
            }
        }
        
        public String getValueKey(AuthorityEntity value)
        {
            return value.getAuthority();
        }
        
        public Pair<Long, AuthorityEntity> createValue(AuthorityEntity value)
        {
            AuthorityEntity entity = createAuthorityEntity(value);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AuthorityEntity> findByKey(Long key)
        {
            AuthorityEntity entity = getAuthorityEntity(key);
            return convertEntityToPair(entity);
        }
        
        public Pair<Long, AuthorityEntity> findByValue(AuthorityEntity value)
        {
            if ((value == null) || (value.getAuthority() == null))
            {
                throw new AlfrescoRuntimeException("Unexpected: AuthorityEntity / name must not be null");
            }
            return convertEntityToPair(getAuthorityEntity(value.getAuthority()));
        }
        
        public int updateValue(Long key, AuthorityEntity value)
        {
            return updateAuthorityEntity(value);
        }
        
        public int deleteByKey(Long key)
        {
            return deleteAuthorityEntity(key);
        }
        
        public int deleteByValue(AuthorityEntity value)
        {
            throw new UnsupportedOperationException("deleteByValue");
        }
    }
    
    protected abstract AuthorityEntity createAuthorityEntity(AuthorityEntity entity);
    protected abstract AuthorityEntity getAuthorityEntity(long id);
    protected abstract AuthorityEntity getAuthorityEntity(String authorityName);
    protected abstract int updateAuthorityEntity(AuthorityEntity updateEntity);
    protected abstract int deleteAuthorityEntity(long id);
    
    // ACE Context (NOTE: currently unused - intended for possible future enhancement)
    
    protected abstract long createAceContextEntity(AceContextEntity entity);
    protected abstract AceContextEntity getAceContextEntity(long aceContextId);
    protected abstract int deleteAceContextEntity(long aceContextId);
    
    
    //
    // Authority Alias (NOTE: currently unused - intended for possible future enhancement)
    //
    
    protected abstract long createAuthorityAliasEntity(AuthorityAliasEntity entity);
    protected abstract int deleteAuthorityAliasEntity(long id);
}
