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

package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return 
 * the values for an RM constraint.
 */
public class RMConstraintGet extends DeclarativeWebScript
{   
    
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {     
        String extensionPath = req.getExtensionPath();
        
        String constraintName = extensionPath.replace('_', ':');
        
        List<String> values = caveatConfigService.getRMAllowedValues(constraintName);
        
        // create model object with the lists model
        Map<String, Object> model = new HashMap<>(1);
        model.put("allowedValuesForCurrentUser", values);
        model.put("constraintName", extensionPath);

        return model;
    }
 
    public void setCaveatConfigService(RMCaveatConfigService caveatConfigService)
    {
        this.caveatConfigService = caveatConfigService;
    }

    public RMCaveatConfigService getCaveatConfigService()
    {
        return caveatConfigService;
    }

    private RMCaveatConfigService caveatConfigService;
     
}
