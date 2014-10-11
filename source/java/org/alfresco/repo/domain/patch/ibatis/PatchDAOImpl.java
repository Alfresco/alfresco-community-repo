/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.domain.patch.ibatis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.ibatis.IdsEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.locale.LocaleDAO;
import org.alfresco.repo.domain.node.ChildAssocEntity;
import org.alfresco.repo.domain.patch.AbstractPatchDAOImpl;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * iBatis-specific implementation of the PatchDAO.
 * 
 * @author janv
 * @since 3.2
 */
public class PatchDAOImpl extends AbstractPatchDAOImpl
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(PatchDAOImpl.class);
    
    private static final String SELECT_ADM_MAX_NODE_ID = "alfresco.patch.select_admMaxNodeId";
    private static final String SELECT_NODES_BY_TYPE_AND_NAME_PATTERN = "alfresco.patch.select_nodesByTypeAndNamePattern";
    
    private static final String UPDATE_CONTENT_MIMETYPE_ID = "alfresco.patch.update_contentMimetypeId";
    
    private static final String DROP_OLD_ATTR_LIST = "alfresco.patch.drop_oldAttrAlfListAttributeEntries";
    private static final String DROP_OLD_ATTR_MAP = "alfresco.patch.drop_oldAttrAlfMapAttributeEntries";
    private static final String DROP_OLD_ATTR_GLOBAL = "alfresco.patch.drop_oldAttrAlfGlobalAttributes";
    private static final String DROP_OLD_ATTR = "alfresco.patch.drop_oldAttrAlfAttributes";
    private static final String DROP_OLD_ATTR_SEQ = "alfresco.patch.drop_oldAttrAlfAttributes_seq";
    
    private static final String SELECT_ACLS_THAT_INHERIT_FROM_NON_PRIMARY_PARENT = "alfresco.patch.select_aclsThatInheritFromNonPrimaryParent";
    private static final String SELECT_ACLS_THAT_INHERIT_WITH_INHERITANCE_UNSET = "alfresco.patch.select_aclsThatInheritWithInheritanceUnset";
    private static final String SELECT_DEFINING_ACLS_THAT_DO_NOT_INHERIT_CORRECTLY_FROM_THE_PRIMARY_PARENT = "alfresco.patch.select_definingAclsThatDoNotInheritCorrectlyFromThePrimaryParent";
    private static final String SELECT_SHARED_ACLS_THAT_DO_NOT_INHERIT_CORRECTLY_FROM_THE_PRIMARY_PARENT = "alfresco.patch.select_sharedAclsThatDoNotInheritCorrectlyFromThePrimaryParent";
    private static final String SELECT_SHARED_ACLS_THAT_DO_NOT_INHERIT_CORRECTLY_FROM_THEIR_DEFINING_ACL = "alfresco.patch.select_sharedAclsThatDoNotInheritCorrectlyFromTheirDefiningAcl";
   
    private static final String SELECT_COUNT_NODES_WITH_ASPECTS = "alfresco.patch.select_CountNodesWithAspectIds";
    
    private static final String SELECT_NODES_BY_TYPE_QNAME = "alfresco.patch.select_NodesByTypeQName";
    private static final String SELECT_NODES_BY_TYPE_URI = "alfresco.patch.select_NodesByTypeUriId";
    private static final String SELECT_NODES_BY_ASPECT_QNAME = "alfresco.patch.select_NodesByAspectQName";
    private static final String SELECT_NODES_BY_CONTENT_MIMETYPE = "alfresco.patch.select_NodesByContentMimetype";
    
    private static final String SELECT_COUNT_NODES_WITH_TYPE_ID = "alfresco.patch.select_CountNodesWithTypeId";
    private static final String SELECT_CHILDREN_OF_THE_SHARED_SURFCONFIG_FOLDER = "alfresco.patch.select_ChildrenOfTheSharedSurfConfigFolder";

    private QNameDAO qnameDAO;
    @SuppressWarnings("unused")
    private LocaleDAO localeDAO;
    @SuppressWarnings("unused")
    private ContentDataDAO contentDataDAO;
    
    protected SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    public void setLocaleDAO(LocaleDAO localeDAO)
    {
        this.localeDAO = localeDAO;
    }
    public void setContentDataDAO(ContentDataDAO contentDataDAO)
    {
        this.contentDataDAO = contentDataDAO;
    }
    
    @Override
    public void startBatch()
    {
        // TODO
        /*
        try
        {
            template.getSqlMapClient().startBatch();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to start batch", e);
        }
        */
    }

    @Override
    public void executeBatch()
    {
        // TODO
        /*
        try
        {
            template.getSqlMapClient().executeBatch();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Failed to start batch", e);
        }
        */
    }

    @Override
    public long getMaxAdmNodeID()
    {
        Long count = template.selectOne(SELECT_ADM_MAX_NODE_ID);
        return count == null ? 0L : count;
    }

    @Override
    public int updateContentMimetypeIds(Long oldMimetypeId, Long newMimetypeId)
    {
        Map<String, Object> params = new HashMap<String, Object>(11);
        params.put("newMimetypeId", newMimetypeId);
        params.put("oldMimetypeId", oldMimetypeId);
        return template.update(UPDATE_CONTENT_MIMETYPE_ID, params);
    }
    
    @Override
    public List<Pair<NodeRef, String>> getNodesOfTypeWithNamePattern(QName typeQName, String namePattern)
    {
        Pair<Long, QName> typeQNamePair = qnameDAO.getQName(typeQName);
        if (typeQNamePair == null)
        {
            // No point querying
            return Collections.emptyList();
        }
        Long typeQNameId = typeQNamePair.getFirst();
        
        Pair<Long, QName> propQNamePair = qnameDAO.getQName(ContentModel.PROP_NAME);
        if (propQNamePair == null)
        {
            return Collections.emptyList();
        }
        Long propQNameId = propQNamePair.getFirst();
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("typeQNameId", typeQNameId);
        params.put("propQNameId", propQNameId);
        params.put("namePattern", namePattern);
        
        final List<Pair<NodeRef, String>> results = new ArrayList<Pair<NodeRef, String>>(500);
        ResultHandler resultHandler = new ResultHandler()
        {
            @SuppressWarnings("unchecked")
            public void handleResult(ResultContext context)
            {
                Map<String, Object> row = (Map<String, Object>) context.getResultObject();
                String protocol = (String) row.get("protocol");
                String identifier = (String) row.get("identifier");
                String uuid = (String) row.get("uuid");
                NodeRef nodeRef = new NodeRef(new StoreRef(protocol, identifier), uuid);
                String name = (String) row.get("name");
                Pair<NodeRef, String> pair = new Pair<NodeRef, String>(nodeRef, name);
                results.add(pair);
            }
        };
        template.select(SELECT_NODES_BY_TYPE_AND_NAME_PATTERN, params, resultHandler);
        return results;
    }
    
    @Override
    public void migrateOldAttrDropTables()
    {
        template.update(DROP_OLD_ATTR_LIST);
        template.update(DROP_OLD_ATTR_MAP);
        template.update(DROP_OLD_ATTR_GLOBAL);
        template.update(DROP_OLD_ATTR);
    }
    
    @Override
    public List<Map<String, Object>> getAclsThatInheritFromNonPrimaryParent()
    {
        List<Map<String, Object>> rows = template.selectList(
                SELECT_ACLS_THAT_INHERIT_FROM_NON_PRIMARY_PARENT,
                Boolean.TRUE);
        return rows;
    }

    @Override
    public List<Map<String, Object>> getAclsThatInheritWithInheritanceUnset()
    {
        List<Map<String, Object>> rows = template.selectList(
                SELECT_ACLS_THAT_INHERIT_WITH_INHERITANCE_UNSET,
                Boolean.TRUE);
        return rows;
    }

    @Override
    public List<Map<String, Object>> getDefiningAclsThatDoNotInheritCorrectlyFromThePrimaryParent()
    {
        List<Map<String, Object>> rows = template.selectList(
                SELECT_DEFINING_ACLS_THAT_DO_NOT_INHERIT_CORRECTLY_FROM_THE_PRIMARY_PARENT,
                Boolean.TRUE);
        return rows;
    }

    @Override
    public List<Map<String, Object>> getSharedAclsThatDoNotInheritCorrectlyFromThePrimaryParent()
    {
        List<Map<String, Object>> rows = template.selectList(
                SELECT_SHARED_ACLS_THAT_DO_NOT_INHERIT_CORRECTLY_FROM_THE_PRIMARY_PARENT,
                Boolean.TRUE);
        return rows;
    }

    @Override
    public List<Map<String, Object>> getSharedAclsThatDoNotInheritCorrectlyFromTheirDefiningAcl()
    {
        List<Map<String, Object>> rows = template.selectList(
                SELECT_SHARED_ACLS_THAT_DO_NOT_INHERIT_CORRECTLY_FROM_THEIR_DEFINING_ACL,
                Boolean.TRUE);
        return rows;
    }

    @Override
    public long getCountNodesWithAspects(Set<QName> qnames)
    {
        // Resolve QNames
        Set<Long> qnameIds = qnameDAO.convertQNamesToIds(qnames, false);
        if (qnameIds.size() == 0)
        {
            return 0L;
        }
        IdsEntity params = new IdsEntity();
        params.setIds(new ArrayList<Long>(qnameIds));
        Long count = template.selectOne(SELECT_COUNT_NODES_WITH_ASPECTS, params);
        if (count == null)
        {
            return 0L;
        }
        else
        {
            return count;
        }
    }

    @Override
    public List<Long> getNodesByTypeQNameId(Long typeQNameId, Long minNodeId, Long maxNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qnameId", typeQNameId);
        params.put("minNodeId", minNodeId);
        params.put("maxNodeId", maxNodeId);
        return template.selectList(SELECT_NODES_BY_TYPE_QNAME, params);
    }
  
    @Override
    public List<Long> getNodesByTypeUriId(Long nsId, Long minNodeId, Long maxNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("nsId", nsId);
        params.put("minNodeId", minNodeId);
        params.put("maxNodeId", maxNodeId);
        return template.selectList(SELECT_NODES_BY_TYPE_URI, params);
    }
  
    @Override
    public List<Long> getNodesByAspectQNameId(Long aspectQNameId, Long minNodeId, Long maxNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("qnameId", aspectQNameId);
        params.put("minNodeId", minNodeId);
        params.put("maxNodeId", maxNodeId);
        return template.selectList(SELECT_NODES_BY_ASPECT_QNAME, params);
    }

    @Override
    public List<Long> getNodesByContentPropertyMimetypeId(Long mimetypeId, Long minNodeId, Long maxNodeId)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("mimetypeId", mimetypeId);
        params.put("minNodeId", minNodeId);
        params.put("maxNodeId", maxNodeId);
        return template.selectList(SELECT_NODES_BY_CONTENT_MIMETYPE, params);
    }

    @Override
    public long getCountNodesWithTypId(QName typeQName)
    {
        // Resolve the QName
        Pair<Long, QName> qnameId = qnameDAO.getQName(typeQName);
        if (qnameId == null)
        {
            return 0L;
        }
        IdsEntity params = new IdsEntity();
        params.setIdOne(qnameId.getFirst());
        Long count = (Long) template.selectOne(SELECT_COUNT_NODES_WITH_TYPE_ID, params);
        if (count == null)
        {
            return 0L;
        }
        else
        {
            return count;
        }
    }

    @Override
    public List<NodeRef> getChildrenOfTheSharedSurfConfigFolder(Long minNodeId, Long maxNodeId)
    {
        Pair<Long, QName> containsAssocQNamePair = qnameDAO.getQName(ContentModel.ASSOC_CONTAINS);
        if (containsAssocQNamePair == null)
        {
            return Collections.emptyList();
        }
        
        Map<String, Object> params = new HashMap<String, Object>(7);
        
        // Get qname CRC
        Long qnameCrcSites = ChildAssocEntity.getQNameCrc(QName.createQName(SiteModel.SITE_MODEL_URL, "sites"));
        Long qnameCrcSurfConfig = ChildAssocEntity.getQNameCrc(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "surf-config"));
        Long qnameCrcPages = ChildAssocEntity.getQNameCrc(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "pages"));
        Long qnameCrcUser = ChildAssocEntity.getQNameCrc(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "user"));
        
        params.put("qnameCrcSites", qnameCrcSites);
        params.put("qnameCrcSurfConfig", qnameCrcSurfConfig);
        params.put("qnameCrcPages", qnameCrcPages);
        params.put("qnameCrcUser", qnameCrcUser);
        params.put("qnameTypeIdContains", containsAssocQNamePair.getFirst());
        params.put("minNodeId", minNodeId);
        params.put("maxNodeId", maxNodeId);

        final List<NodeRef> results = new ArrayList<NodeRef>(1000);
        ResultHandler resultHandler = new ResultHandler()
        {
            @SuppressWarnings("unchecked")
            public void handleResult(ResultContext context)
            {
                Map<String, Object> row = (Map<String, Object>) context.getResultObject();
                String protocol = (String) row.get("protocol");
                String identifier = (String) row.get("identifier");
                String uuid = (String) row.get("uuid");
                NodeRef nodeRef = new NodeRef(new StoreRef(protocol, identifier), uuid);
                results.add(nodeRef);
            }
        };
        template.select(SELECT_CHILDREN_OF_THE_SHARED_SURFCONFIG_FOLDER, params, resultHandler);
        return results;
    }

    /**
     * PostgreSQL-specific DAO
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    public static class PostgreSQL extends PatchDAOImpl
    {
        @Override
        public void migrateOldAttrDropTables()
        {
            super.migrateOldAttrDropTables();
            template.update(DROP_OLD_ATTR_SEQ);
        }
    }
    
    /**
     * Oracle-specific DAO
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    public static class Oracle extends PatchDAOImpl
    {
        @Override
        public void migrateOldAttrDropTables()
        {
            super.migrateOldAttrDropTables();
            template.update(DROP_OLD_ATTR_SEQ);
        }
    }
}
