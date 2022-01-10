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
package org.alfresco.rest.v0;

import static org.testng.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.audit.AuditEntry;
import org.alfresco.rest.rm.community.util.PojoUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The v0 REST API for rm audit logs
 *
 * @author Rodica Sutu
 * @since 2.7
 */
@Component
public class RMAuditAPI extends BaseAPI
{
    /** Logger for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RMAuditAPI.class);

    /** The URI for the audit API. */
    private static final String RM_AUDIT_API = "{0}rma/admin/rmauditlog";
    private static final String RM_AUDIT_LOG_API = RM_AUDIT_API + "?{1}";

    /**
     * Returns a list of rm audit entries .
     *
     * @param user     The username of the user to use.
     * @param password The password of the user.
     * @param size     Maximum number of log entries to return
     * @param event    The name of audit event to be retrieved
     * @return return Only return log entries matching this event
     */
    public List<AuditEntry> getRMAuditLog(String user, String password, final int size, final String event)
    {
        String parameters = null;
        try
        {
            parameters = "size=" + size + (event != null ? "&event=" + URLEncoder.encode(event, "UTF-8"):"");
        }
        catch (UnsupportedEncodingException e)
        {
            LOGGER.error("Unable to encode the event name {}", e.getMessage());
        }
        JSONArray auditEntries =  doGetRequest(user, password,
                MessageFormat.format(RM_AUDIT_LOG_API,"{0}", parameters)).getJSONObject("data").getJSONArray("entries");

        return PojoUtility.jsonToObject(auditEntries, AuditEntry.class);
    }

    /**
     * Clear the list of audit entries.
     *
     * @param username The username of the user to use.
     * @param password The password of the user.
     * @throws AssertionError If the API call didn't clear the audit log.
     */
    public void clearAuditLog(String username, String password)
    {
        JSONObject deleteStatus = doDeleteRequest(username, password, RM_AUDIT_API);

        assertTrue(deleteStatus != null
                //audit clear and login events are returned
                && getRMAuditLog(username, password, 100, null).size() == 2);
    }


}
