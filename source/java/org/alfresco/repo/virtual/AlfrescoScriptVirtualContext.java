/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

/**
 * JavaScript API {@link VirtualContext} adapter.
 * 
 * @author Bogdan Horje
 */
public class AlfrescoScriptVirtualContext extends BaseScopableProcessorExtension
{
    // TODO: extract placeholder interface. make placeholders configurable.

    public static final String CURRENT_USER_PH = "CURRENT_USER";

    public static final String ACTUAL_PATH_PH = "ACTUAL_PATH";

    public static final String ACTUAL_ISO9075_PATH_PH = "ACTUAL_ISO9075_PATH";

    private VirtualContext context;

    private ServiceRegistry serviceRegistry;

    private Map<String, String> placeholders;

    public AlfrescoScriptVirtualContext(VirtualContext context, ServiceRegistry serviceRegistry)
    {
        super();
        this.context = context;
        this.serviceRegistry = serviceRegistry;
        this.placeholders = createPlaceHolders();
    }

    /**
     * @return an array containing the plain qname path at index 0 and the
     *         ISO9075 element-encoded qname path at index 1
     */
    private String[] createQNamePaths()
    {
        final NamespaceService ns = serviceRegistry.getNamespaceService();
        final Map<String, String> cache = new HashMap<String, String>();
        final StringBuilder bufPlain = new StringBuilder(128);
        final StringBuilder bufISO9075 = new StringBuilder(128);

        final Path path = serviceRegistry.getNodeService().getPath(context.getActualNodeRef());
        for (final Path.Element e : path)
        {
            if (e instanceof Path.ChildAssocElement)
            {
                final QName qname = ((Path.ChildAssocElement) e).getRef().getQName();
                if (qname != null)
                {
                    String prefix = cache.get(qname.getNamespaceURI());
                    if (prefix == null)
                    {
                        // first request for this namespace prefix, get and
                        // cache result
                        Collection<String> prefixes = ns.getPrefixes(qname.getNamespaceURI());
                        prefix = prefixes.size() != 0 ? prefixes.iterator().next() : "";
                        cache.put(qname.getNamespaceURI(),
                                  prefix);
                    }
                    bufISO9075.append('/').append(prefix).append(':').append(ISO9075.encode(qname.getLocalName()));
                    bufPlain.append('/').append(prefix).append(':').append(qname.getLocalName());
                }
            }
            else
            {
                bufISO9075.append('/').append(e.toString());
                bufPlain.append('/').append(e.toString());
            }
        }
        String[] qnamePaths = new String[] { bufPlain.toString(), bufISO9075.toString() };

        return qnamePaths;
    }

    // TODO: extract placeholder interface. make placeholders configurable.
    // TODO: extract open-close-configurable placeholders
    private Map<String, String> createPlaceHolders()
    {
        Map<String, String> newPlaceholders = new HashMap<>();
        String user = AuthenticationUtil.getFullyAuthenticatedUser();

        newPlaceholders.put(CURRENT_USER_PH,
                            user);

        String[] paths = createQNamePaths();

        // the actual path will contain the ISO9075 encoded qname path
        // this was reverted from a dual placeholder implementation (see CM-523)
        newPlaceholders.put(ACTUAL_PATH_PH,
                            paths[1]);

        // newPlaceholders.put(ACTUAL_ISO9075_PATH_PH,
        // paths[1]);

        return newPlaceholders;
    }

    public Map<String, String> getPlaceholders()
    {
        return placeholders;
    }

    public synchronized ScriptNode getActualNode()
    {
        return new ScriptNode(context.getActualNodeRef(),
                              serviceRegistry,
                              getScope());
    }

    public Object getParameter(String param)
    {
        return context.getParameter(param);
    }
}
