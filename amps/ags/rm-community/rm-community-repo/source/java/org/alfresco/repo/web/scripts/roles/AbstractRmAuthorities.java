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

package org.alfresco.repo.web.scripts.roles;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.script.admin.RoleDeclarativeWebScript;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Abstract class for adding/removing a user/group to/from a role
 * This class contains the common methods needed in the sub classes.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class AbstractRmAuthorities extends RoleDeclarativeWebScript
{
    /** Constants for the url parameters */
    private static final String ROLE_ID = "roleId";
    private static final String AUTHORITY_NAME = "authorityName";

    /**
     * Util method for getting the nodeRef from the request
     *
     * @param req The webscript request
     * @return The nodeRef passed in the request
     */
    protected NodeRef getFilePlan(WebScriptRequest req)
    {
        NodeRef filePlan = super.getFilePlan(req);
        if (filePlan == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "No filePlan was provided on the URL.");
        }
        return filePlan;
    }

    /**
     * Util method for getting the roleId from the request
     *
     * @param req The webscript request
     * @return The role id passed in the request
     */
    protected String getRoleId(WebScriptRequest req)
    {
        return getParamValue(req, ROLE_ID);
    }

    /**
     * Util method for getting the authorityName from the request
     *
     * @param req The webscript request
     * @return The authorityName passed in the request
     */
    protected String getAuthorityName(WebScriptRequest req)
    {
        return getParamValue(req, AUTHORITY_NAME);
    }

    /**
     * Helper method to get the value of parameter from the request
     *
     * @param req The webscript request
     * @param param The name of the parameter for which the value is requested
     * @return The value for the requested parameter
     */
    private String getParamValue(WebScriptRequest req, String param)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

        String authorityName = templateVars.get(param);
        if (StringUtils.isBlank(authorityName))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "No '" + param + "' was provided on the URL.");
        }
        return authorityName;
    }
}
