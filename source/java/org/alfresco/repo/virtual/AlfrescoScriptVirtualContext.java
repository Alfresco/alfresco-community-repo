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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;

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

    // TODO: extract placeholder interface. make placeholders configurable.
    private Map<String, String> createPlaceHolders()
    {
        Map<String, String> newPlaceholders = new HashMap<>();
        String user = AuthenticationUtil.getFullyAuthenticatedUser();

        // TODO: extract open-close-configurable placeholders
        newPlaceholders.put(CURRENT_USER_PH,
                            user);
        // TODO: can we replace getQnamePath usage
        newPlaceholders.put(ACTUAL_PATH_PH,
                            getActualNode().getQnamePath());
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
