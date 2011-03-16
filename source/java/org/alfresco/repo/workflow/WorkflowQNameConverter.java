/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public class WorkflowQNameConverter
{
    private final NamespacePrefixResolver prefixResolver;
    
    public WorkflowQNameConverter(NamespacePrefixResolver prefixResolver)
    {
        this.prefixResolver = prefixResolver;
    }

    /**
     * Map QName to jBPM variable name
     * 
     * @param name  QName
     * @return  jBPM variable name
     */
    public String mapQNameToName(QName name)
    {
        // NOTE: Map names using old conversion scheme (i.e. : -> _) as well as new scheme (i.e. } -> _)
        // NOTE: Use new scheme 
        String nameStr = name.toPrefixString(prefixResolver);
        if (nameStr.indexOf('_') != -1 && nameStr.indexOf('_') < nameStr.indexOf(':'))
        {
            return nameStr.replace(':', '}');
        }
        return nameStr.replace(':', '_');
    }
    
    /**
     * Map QName to jBPM variable name
     * 
     * @param name  QName
     * @return  jBPM variable name
     */
    public QName mapNameToQName(String name)
    {
        if(name.indexOf(QName.NAMESPACE_BEGIN)==0)
        {
            return QName.createQName(name);
        }
        String qName = name;
        if(name.indexOf(QName.NAMESPACE_PREFIX)==-1)
        {
            if(name.indexOf('_')==-1)
            {
                return QName.createQName(NamespaceService.DEFAULT_URI, name);
            }
            qName = name.replaceFirst("_", ":");
        }
        return QName.createQName(qName, prefixResolver);
    }
    
}
