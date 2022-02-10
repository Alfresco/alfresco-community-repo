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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.webscript;

import static org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService.DEFAULT_RM_SITE_ID;

import java.io.IOException;
import java.text.MessageFormat;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;

public class AuditRestApiTest extends BaseRMWebScriptTestCase
{
    /** URL for the REST APIs */
    protected static final String GET_NODE_AUDITLOG_URL_FORMAT = "/api/node/{0}/rmauditlog";

    private static final String USER_WITHOUT_AUDIT_CAPABILITY = GUID.generate();

    private NodeRef record;

    public void testAuditAccessCapability() throws IOException
    {

        String recordAuditUrl = MessageFormat.format(GET_NODE_AUDITLOG_URL_FORMAT,record.toString().replace("://", "/"));

        sendRequest(new GetRequest(recordAuditUrl), Status.STATUS_OK, AuthenticationUtil.getAdminUserName() );

        sendRequest(new GetRequest(recordAuditUrl), Status.STATUS_FORBIDDEN, USER_WITHOUT_AUDIT_CAPABILITY );
    }

    @Override
    protected void setupTestData()
    {
        super.setupTestData();

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {

                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil
                        .getSystemUserName());

                createUser(USER_WITHOUT_AUDIT_CAPABILITY);

                record = utils.createRecord(recordFolder, GUID.generate());


                return null;
            }
        });
    }

    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();

        deleteUser(USER_WITHOUT_AUDIT_CAPABILITY);
    }

    protected String getRMSiteId()
    {
    	return DEFAULT_RM_SITE_ID;
    }


}
