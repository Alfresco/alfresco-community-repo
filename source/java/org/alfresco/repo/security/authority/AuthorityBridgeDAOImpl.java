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
package org.alfresco.repo.security.authority;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * @author Andy
 */
public class AuthorityBridgeDAOImpl extends AbstractAuthorityBridgeDAO
{
    private static final String QUERY_SELECT_GET_AUTHORITY_BRIDGE_ENTRIES = "alfresco.query.authorities.select_GetAuthorityBridgeEntries";
    
    private static final String QUERY_SELECT_GET_DIRECT_AUTHORITIES_FOR_UESR = "alfresco.query.authorities.select_GetDirectAuthoritiesForUser";
   
    private Log logger = LogFactory.getLog(getClass());

    private SqlSessionTemplate template;

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate)
    {
        this.template = sqlSessionTemplate;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.authority.AbstractAuthorityBridgeDAO#selectAuthorityBridgeLinks(java.lang.Long,
     * java.lang.Long, java.lang.Long, java.lang.Long)
     */
    @Override
    protected List<AuthorityBridgeLink> selectAuthorityBridgeLinks(Long authorityContainerTypeQNameId, Long memberAssocQNameId, Long authorityNameQNameId, Long storeId)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        AuthorityBridgeParametersEntity authorityBridgeParametersEntity = new AuthorityBridgeParametersEntity(authorityContainerTypeQNameId, memberAssocQNameId, authorityNameQNameId, storeId);
        
        List<AuthorityBridgeLink> links = template.selectList(QUERY_SELECT_GET_AUTHORITY_BRIDGE_ENTRIES, authorityBridgeParametersEntity);
        
        if (start != null)
        {
            logger.debug("Authority bridge query: "+links.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return links;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.authority.AbstractAuthorityBridgeDAO#selectDirectAuthoritiesForUser(java.lang.Long, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String)
     */
    @Override
    protected List<AuthorityBridgeLink> selectDirectAuthoritiesForUser(Long authorityContainerTypeQNameId, Long memberAssocQNameId, Long authorityNameQNameId, Long storeId,
            Long nodeId)
    {
        
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        AuthorityBridgeParametersEntity authorityBridgeParametersEntity = new AuthorityBridgeParametersEntity(authorityContainerTypeQNameId, memberAssocQNameId, authorityNameQNameId, storeId, nodeId);
        
        List<AuthorityBridgeLink> links = template.selectList(QUERY_SELECT_GET_DIRECT_AUTHORITIES_FOR_UESR, authorityBridgeParametersEntity);
        
        if (start != null)
        {
            logger.debug("Direct authority: "+links.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return links;
    }

}
