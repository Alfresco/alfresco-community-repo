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
package org.alfresco.repo.template;

import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.security.AccessStatus;
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
        final StringTokenizer t = new StringTokenizer(path, "/");

        // optimization
        if (this.services.getDictionaryService().isSubClass(parent.getType(), org.alfresco.model.ContentModel.TYPE_FOLDER))
        {
            NodeRef result = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork() throws Exception
                {
                    NodeRef child = parent.nodeRef;
                    while (t.hasMoreTokens() && child != null)
                    {
                        String name = t.nextToken();
                        child = services.getNodeService().getChildByName(child, org.alfresco.model.ContentModel.ASSOC_CONTAINS, name);
                    }
                    return child;
                }
            }, AuthenticationUtil.getSystemUserName());

            // final node must be accessible to the user via the usual ACL permission checks
            if (result != null
                    && services.getPublicServiceAccessService().hasAccess("NodeService", "getProperties", result) != AccessStatus.ALLOWED)
            {
                result = null;
            }

            return (result != null ? new TemplateNode(result, this.services, this.parent.getImageResolver()) : null);
        }

        StringBuilder xpath = new StringBuilder(path.length() << 1);
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
