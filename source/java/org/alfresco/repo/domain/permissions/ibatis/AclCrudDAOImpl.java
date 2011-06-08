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
package org.alfresco.repo.domain.permissions.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.permissions.AbstractAclCrudDAOImpl;
import org.alfresco.repo.domain.permissions.Ace;
import org.alfresco.repo.domain.permissions.AceContextEntity;
import org.alfresco.repo.domain.permissions.AceEntity;
import org.alfresco.repo.domain.permissions.AclChangeSetEntity;
import org.alfresco.repo.domain.permissions.AclEntity;
import org.alfresco.repo.domain.permissions.AclMemberEntity;
import org.alfresco.repo.domain.permissions.AuthorityAliasEntity;
import org.alfresco.repo.domain.permissions.AuthorityEntity;
import org.alfresco.repo.domain.permissions.PermissionEntity;
import org.alfresco.repo.security.permissions.ACEType;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * iBatis-specific implementation of the ACL Crud DAO.
 * 
 * @author janv
 * @since 3.4
 */
public class AclCrudDAOImpl extends AbstractAclCrudDAOImpl
{
    private static final String INSERT_ACL = "alfresco.permissions.insert.insert_Acl";
    private static final String SELECT_ACL_BY_ID = "alfresco.permissions.select_AclById";
    private static final String SELECT_ACLS_THAT_INHERIT_FROM_ACL = "alfresco.permissions.select_AclsThatInheritFromAcl";
    private static final String SELECT_LATEST_ACL_BY_GUID = "alfresco.permissions.select_LatestAclByGuid";
    private static final String SELECT_ADM_NODES_BY_ACL = "alfresco.permissions.select_ADMNodesByAclId";
    private static final String SELECT_AVM_NODES_BY_ACL = "alfresco.permissions.select_AVMNodesByAclId";
    private static final String UPDATE_ACL = "alfresco.permissions.update_Acl";
    private static final String DELETE_ACL = "alfresco.permissions.delete_Acl";
    
    private static final String INSERT_ACL_MEMBER = "alfresco.permissions.insert.insert_AclMember";
    private static final String SELECT_ACL_MEMBERS_BY_ACL = "alfresco.permissions.select_AclMembersByAclId";
    private static final String SELECT_ACL_MEMBERS_BY_AUTHORITY = "alfresco.permissions.select_AclMembersByAuthorityName";
    private static final String UPDATE_ACL_MEMBER = "alfresco.permissions.update_AclMember";
    private static final String DELETE_ACL_MEMBERS_LIST = "alfresco.permissions.delete_AclMembersList";
    private static final String DELETE_ACL_MEMBERS_BY_ACL = "alfresco.permissions.delete_AclMembersByAclId";
    
    private static final String INSERT_ACL_CHANGESET = "alfresco.permissions.insert.insert_AclChangeSet";
    private static final String UPDATE_ACL_CHANGESET = "alfresco.permissions.update_AclChangeSet";
    private static final String SELECT_ACL_CHANGESET_BY_ID = "alfresco.permissions.select_AclChangeSetById";
    private static final String DELETE_ACL_CHANGESET = "alfresco.permissions.delete_AclChangeSet";
    
    private static final String INSERT_ACE = "alfresco.permissions.insert.insert_Ace";
    private static final String SELECT_ACE_BY_ID = "alfresco.permissions.select_AceById";
    private static final String SELECT_ACES_BY_AUTHORITY = "alfresco.permissions.select_AcesByAuthorityId";
    private static final String SELECT_ACES_AND_AUTHORIES_BY_ACL = "alfresco.permissions.select_AcesAndAuthoritiesByAclId";
    private static final String SELECT_ACE_WITH_NO_CONTEXT = "alfresco.permissions.select_AceWithNoContext";
    private static final String DELETE_ACES_LIST = "alfresco.permissions.delete_AcesList";
    
    private static final String INSERT_ACE_CONTEXT = "alfresco.permissions.insert.insert_AceContext";
    private static final String SELECT_ACE_CONTEXT_BY_ID = "alfresco.permissions.select_AceContextById";
    private static final String DELETE_ACE_CONTEXT = "alfresco.permissions.delete_AceContext";
    
    private static final String INSERT_PERMISSION = "alfresco.permissions.insert.insert_Permission";
    private static final String SELECT_PERMISSION_BY_ID = "alfresco.permissions.select_PermissionById";
    private static final String SELECT_PERMISSION_BY_TYPE_AND_NAME = "alfresco.permissions.select_PermissionByTypeAndName";
    private static final String UPDATE_PERMISSION = "alfresco.permissions.update_Permission";
    private static final String DELETE_PERMISSION = "alfresco.permissions.delete_Permission";
    
    private static final String INSERT_AUTHORITY = "alfresco.permissions.insert.insert_Authority";
    private static final String SELECT_AUTHORITY_BY_ID = "alfresco.permissions.select_AuthorityById";
    private static final String SELECT_AUTHORITY_BY_NAME = "alfresco.permissions.select_AuthorityByName";
    private static final String UPDATE_AUTHORITY = "alfresco.permissions.update_Authority";
    private static final String DELETE_AUTHORITY = "alfresco.permissions.delete_Authority";
    
    private static final String INSERT_AUTHORITY_ALIAS = "alfresco.permissions.insert.insert_AuthorityAlias";
    private static final String DELETE_AUTHORITY_ALIAS = "alfresco.permissions.delete_AuthorityAlias";
    
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    @Override
    protected AclEntity createAclEntity(AclEntity entity)
    {
        entity.setVersion(0L);
        template.insert(INSERT_ACL, entity);
        return entity;
    }
    
    @Override
    protected AclEntity getAclEntity(long aclEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclEntityId);
        
        return (AclEntity)template.selectOne(SELECT_ACL_BY_ID, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<Long> getAclEntitiesThatInheritFromAcl(long aclEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("id", aclEntityId);
        params.put("bool", true);
        
        return (List<Long>)template.selectList(SELECT_ACLS_THAT_INHERIT_FROM_ACL, params);
    }
    
    @Override
    protected Long getLatestAclEntityByGuid(String aclGuid)
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("name", aclGuid);
        params.put("bool", true);
        
        return (Long)template.selectOne(SELECT_LATEST_ACL_BY_GUID, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<Long> getADMNodeEntityIdsByAcl(long aclEntityId, int maxResults)
    {
        if (maxResults < 0)
        {
            maxResults = RowBounds.NO_ROW_LIMIT;
        }
        
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclEntityId);
        
        return (List<Long>)template.selectList(SELECT_ADM_NODES_BY_ACL, params, new RowBounds(0 , maxResults));
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<Long> getAVMNodeEntityIdsByAcl(long aclEntityId, int maxResults)
    {
        if (maxResults < 0)
        {
            maxResults = RowBounds.NO_ROW_LIMIT;
        }
        
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclEntityId);
        
        return (List<Long>)template.selectList(SELECT_AVM_NODES_BY_ACL, params, new RowBounds(0 , maxResults));
    }
    
    @Override
    protected int updateAclEntity(AclEntity updatedAclEntity)
    {
        updatedAclEntity.incrementVersion();
        
        return template.update(UPDATE_ACL, updatedAclEntity);
    }
    
    @Override
    protected int deleteAclEntity(long aclEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclEntityId);
        
        return template.delete(DELETE_ACL, params);
    }
    
    @Override
    protected AclMemberEntity createAclMemberEntity(AclMemberEntity entity)
    {
        entity.setVersion(0L);
        template.insert(INSERT_ACL_MEMBER, entity);
        return entity;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AclMemberEntity> getAclMemberEntitiesByAcl(long aclEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclEntityId);
        
        return (List<AclMemberEntity>) template.selectList(SELECT_ACL_MEMBERS_BY_ACL, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<AclMemberEntity> getAclMemberEntitiesByAuthority(String authorityName)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("name", authorityName);
        
        return (List<AclMemberEntity>) template.selectList(SELECT_ACL_MEMBERS_BY_AUTHORITY, params);
    }
    
    @Override
    protected int updateAclMemberEntity(AclMemberEntity updatedAclMemberEntity)
    {
        updatedAclMemberEntity.incrementVersion();
        
        return template.update(UPDATE_ACL_MEMBER, updatedAclMemberEntity);
    }
    
    @Override
    protected int deleteAclMemberEntities(List<Long> aclMemberEntityIds)
    {
        return template.delete(DELETE_ACL_MEMBERS_LIST, aclMemberEntityIds);
    }
    
    @Override
    protected int deleteAclMemberEntitiesByAcl(long aclEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclEntityId);
        
        return template.delete(DELETE_ACL_MEMBERS_BY_ACL, params);
    }
    
    @Override
    protected Long createAclChangeSetEntity()
    {
        AclChangeSetEntity entity = new AclChangeSetEntity();
        template.insert(INSERT_ACL_CHANGESET, entity);
        return entity.getId();
    }
    
    @Override
    protected AclChangeSetEntity getAclChangeSetEntity(Long aclChangeSetEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclChangeSetEntityId);
        
        return (AclChangeSetEntity)template.selectOne(SELECT_ACL_CHANGESET_BY_ID, params);
    }
    
    @Override
    protected int deleteAclChangeSetEntity(Long aclChangeSetEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclChangeSetEntityId);
        
        return template.delete(DELETE_ACL_CHANGESET, params);
    }
    
    @Override
    protected int updateChangeSetEntity(Long id, long commitTimeMs)
    {
        AclChangeSetEntity entity = new AclChangeSetEntity();
        entity.setId(id);
        entity.setCommitTimeMs(commitTimeMs);
        
        return template.update(UPDATE_ACL_CHANGESET, entity);
    }

    @Override
    protected long createAceEntity(AceEntity entity)
    {
        entity.setVersion(0L);
        template.insert(INSERT_ACE, entity);
        Long id = entity.getId();
        return (id != null ? id : -1);
    }
    
    @Override
    protected AceEntity getAceEntity(long aceEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aceEntityId);
        
        return (AceEntity)template.selectOne(SELECT_ACE_BY_ID, params);
    }
    
    @Override
    protected AceEntity getAceEntity(long permissionId, long authorityId, boolean allowed, ACEType type)
    {
        Map<String, Object> params = new HashMap<String, Object>(4);
        params.put("id1", permissionId);
        params.put("id2", authorityId);
        params.put("bool", allowed);
        params.put("int", type.getId());
        
        return (AceEntity)template.selectOne(SELECT_ACE_WITH_NO_CONTEXT, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<Ace> getAceEntitiesByAuthority(long authorityEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", authorityEntityId);
        
        return (List<Ace>) template.selectList(SELECT_ACES_BY_AUTHORITY, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<Map<String, Object>> getAceAndAuthorityEntitiesByAcl(long aclEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aclEntityId);
        
        return (List<Map<String, Object>>) template.selectList(SELECT_ACES_AND_AUTHORIES_BY_ACL, params);
    }
    
    @Override
    protected int deleteAceEntities(List<Long> aceEntityIds)
    {
        return template.delete(DELETE_ACES_LIST, aceEntityIds);
    }
    
    @Override
    protected long createAceContextEntity(AceContextEntity entity)
    {
        entity.setVersion(0L);
        template.insert(INSERT_ACE_CONTEXT, entity);
        Long id = entity.getId();
        return (id != null ? id : -1);
    }
    
    @Override
    protected AceContextEntity getAceContextEntity(long aceContextEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aceContextEntityId);
        
        return (AceContextEntity)template.selectOne(SELECT_ACE_CONTEXT_BY_ID, params);
    }
    
    @Override
    protected int deleteAceContextEntity(long aceContextEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", aceContextEntityId);
        
        return template.delete(DELETE_ACE_CONTEXT, params);
    }
    
    @Override
    protected PermissionEntity createPermissionEntity(PermissionEntity entity)
    {
        entity.setVersion(0L);
        template.insert(INSERT_PERMISSION, entity);
        return entity;
    }
    
    @Override
    protected PermissionEntity getPermissionEntity(long permissionEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", permissionEntityId);
        
        return (PermissionEntity)template.selectOne(SELECT_PERMISSION_BY_ID, params);
    }
    
    @Override
    protected PermissionEntity getPermissionEntity(long qnameId, String name)
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        params.put("id", qnameId);
        params.put("name", name);
        
        return (PermissionEntity)template.selectOne(SELECT_PERMISSION_BY_TYPE_AND_NAME, params);
    }
    
    @Override
    protected int updatePermissionEntity(PermissionEntity permissionEntity)
    {
        permissionEntity.incrementVersion();
        
        return template.update(UPDATE_PERMISSION, permissionEntity);
    }
    
    @Override
    protected int deletePermissionEntity(long permissionEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", permissionEntityId);
        
        return template.delete(DELETE_PERMISSION, params);
    }
    
    @Override
    protected AuthorityEntity createAuthorityEntity(AuthorityEntity entity)
    {
        entity.setVersion(0L);
        template.insert(INSERT_AUTHORITY, entity);
        return entity;
    }
    
    @Override
    protected AuthorityEntity getAuthorityEntity(long authorityEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", authorityEntityId);
        
        return (AuthorityEntity)template.selectOne(SELECT_AUTHORITY_BY_ID, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    
    protected AuthorityEntity getAuthorityEntity(String authorityName)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("name", authorityName);
        
        // note: allow for list (non-unique name) in case of upgrade of old schemas
        AuthorityEntity result = null;
        List<AuthorityEntity> authorities = (List<AuthorityEntity>)template.selectList(SELECT_AUTHORITY_BY_NAME, params);
        for (AuthorityEntity found : authorities)
        {
            if (found.getAuthority().equals(authorityName))
            {
                result = found;
                break;
            }
        }
        return result;
    }
    
    @Override
    protected int updateAuthorityEntity(AuthorityEntity authorityEntity)
    {
        authorityEntity.incrementVersion();
        
        return template.update(UPDATE_AUTHORITY, authorityEntity);
    }
    
    @Override
    protected int deleteAuthorityEntity(long authorityEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", authorityEntityId);
        
        return template.delete(DELETE_AUTHORITY, params);
    }
    
    @Override
    protected long createAuthorityAliasEntity(AuthorityAliasEntity entity)
    {
        entity.setVersion(0L);
        template.insert(INSERT_AUTHORITY_ALIAS, entity);
        Long id = entity.getId();
        return (id != null ? id : -1);
    }
    
    @Override
    protected int deleteAuthorityAliasEntity(long authorityAliasEntityId)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", authorityAliasEntityId);
        
        return template.delete(DELETE_AUTHORITY_ALIAS, params);
    }
}
