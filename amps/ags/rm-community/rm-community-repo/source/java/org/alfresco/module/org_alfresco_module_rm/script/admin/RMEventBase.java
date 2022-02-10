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

package org.alfresco.module.org_alfresco_module_rm.script.admin;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Base class for Records management event web scripts
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RMEventBase extends DeclarativeWebScript
{
    /**
     * Helper method for getting the value for a key from a JSON object
     *
     * @param json The request content as JSON object
     * @param key The key for which the value should be retrieved (e.g. "eventName")
     * @return String The value for the provided key if the key exists, null otherwise
     * @throws JSONException If there is no string value for the key
     */
    protected String getValue(JSONObject json, String key) throws JSONException
    {
        String result = null;
        if (json.has(key))
        {
            result = json.getString(key);
        }
        return result;
    }

    /**
     * Helper method for checking the key (e.g. "eventName")
     *
     * @param key String The key which will be checked
     * @param msg String The error message to throw if the key doesn't have a value
     */
    protected void doCheck(String key, String msg)
    {
        if (StringUtils.isBlank(key))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, msg);
        }
    }
}
