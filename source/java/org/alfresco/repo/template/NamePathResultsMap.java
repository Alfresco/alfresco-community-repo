/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.template;

import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A special Map that executes an XPath against the parent Node as part of the get()
 * Map interface implementation.
 * 
 * @author Kevin Roast
 */
public class NamePathResultsMap extends BasePathResultsMap
{
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public NamePathResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }
    
    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        String path = key.toString();
        StringBuilder xpath = new StringBuilder(path.length() << 1);
        StringTokenizer t = new StringTokenizer(path, "/");
        int count = 0;
        QueryParameterDefinition[] params = new QueryParameterDefinition[t.countTokens()];
        DataTypeDefinition ddText =
            this.services.getDictionaryService().getDataType(DataTypeDefinition.TEXT);
        NamespaceService ns = this.services.getNamespaceService();
        while (t.hasMoreTokens())
        {
            if (xpath.length() != 0)
            {
                xpath.append('/');
            }
            String strCount = Integer.toString(count);
            xpath.append("*[@cm:name=$cm:name")
                 .append(strCount)
                 .append(']');
            params[count++] = new QueryParameterDefImpl(
                    QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "name" + strCount, ns),
                    ddText,
                    true,
                    t.nextToken());
        }
        
        List<TemplateNode> nodes = getChildrenByXPath(xpath.toString(), params, true);
        
        return (nodes.size() != 0) ? nodes.get(0) : null;
    }
}
