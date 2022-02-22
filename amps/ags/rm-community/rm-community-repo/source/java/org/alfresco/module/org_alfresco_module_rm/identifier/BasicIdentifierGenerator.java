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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Basic identifier generator implementation.
 *
 * @author Roy Wetherall
 */
public class BasicIdentifierGenerator extends IdentifierGeneratorBase
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierGenerator#generateId(java.util.Map)
     */
    @Override
    public String generateId(Map<String, Serializable> context)
    {
        NodeRef nodeRef = (NodeRef)context.get(IdentifierService.CONTEXT_NODEREF);
        Long dbId = 0l;
        if (nodeRef != null)
        {
            dbId = (Long)nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
        }
        else
        {
            dbId = System.currentTimeMillis();
        }

        Calendar fileCalendar = Calendar.getInstance();
        String year = Integer.toString(fileCalendar.get(Calendar.YEAR));
        return year + "-" + padString(dbId.toString(), 10);
    }
}
