/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.identifier;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public abstract class IdentifierGeneratorBase implements IdentifierGenerator
{
    /** Identifier service */
    private IdentifierService identifierService;
    
    /** Node service */
    protected NodeService nodeService;
    
    /** Content type */
    private QName type;    
    
    /**
     * Initialisation method
     */
    public void init()
    {
        identifierService.register(this);
    }
    
    /**
     * Set identifier service.
     * 
     * @param identifierService     identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set type.
     * 
     * @param type  content type
     */
    public void setTypeAsString(String type)
    {
        this.type = QName.createQName(type);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierGenerator#getType()
     */
    @Override
    public QName getType()
    {
        return type;
    }
    
    /**
     * Function to pad a string with zero '0' characters to the required length
     * 
     * @param s String to pad with leading zero '0' characters
     * @param len Length to pad to
     * @return padded string or the original if already at &gt;=len characters
     */
    protected String padString(String s, int len)
    {
        String result = s;

        for (int i = 0; i < (len - s.length()); i++)
        {
            result = "0" + result;
        }

        return result;
    }
}
