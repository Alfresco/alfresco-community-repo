/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.query;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Records management query DAO implementation
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordsManagementQueryDAOImpl implements RecordsManagementQueryDAO, RecordsManagementModel
{
    private static final String COUNT_IDENTIFIER = "alfresco.query.rm.select_CountRMIndentifier";
    
    /** SQL session template */
    protected SqlSessionTemplate template;
    
    /** QName DAO */
    protected QNameDAO qnameDAO;
    
    /**
     * @param sqlSessionTemplate    SQL session template
     */
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    /**
     * @param qnameDAO  qname DAO
     */
    public final void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO#getCountRmaIdentifier(java.lang.String)
     */
    @Override
    public int getCountRmaIdentifier(String identifierValue)
    {
        int result = 0;
        
        // lookup the id of the identifier property qname
        Pair<Long, QName> pair = qnameDAO.getQName(PROP_IDENTIFIER);
        if (pair != null)
        {        
            // create query params
            Map<String, Object> params = new HashMap<String, Object>(2);
            params.put("qnameId", pair.getFirst());
            params.put("idValue", identifierValue);
            
            // return the number of rma identifiers found that match the passed value
            Integer count = (Integer)template.selectOne(COUNT_IDENTIFIER, params);
            
            if (count != null)
            {
                result = count;
            }
        }
        
        return result;
    }

}
