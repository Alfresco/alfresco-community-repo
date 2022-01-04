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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMWebScriptTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Test of GET RM Constraint  (User facing scripts)
 *
 * @author Mark Rogers
 */
public class RMConstraintScriptTest extends BaseRMWebScriptTestCase
{
    protected final static String RM_LIST          = "rmc:smListTest";
    protected final static String RM_LIST_URI_ELEM = "rmc_smListTest";

    private static final String URL_RM_CONSTRAINTS = "/api/rma/rmconstraints";

    /**
     *
     * @throws Exception
     */
    public void testGetRMConstraint() throws Exception
    {
        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        /**
         * Delete the list to remove any junk then recreate it.
         */
        if (caveatConfigService.getRMConstraint(RM_LIST) != null)
        {
            caveatConfigService.deleteRMConstraint(RM_LIST);
        }
        caveatConfigService.addRMConstraint(RM_LIST, "my title", new String[0]);


        createUser("fbloggs");
        createUser("jrogers");
        createUser("jdoe");


        List<String> values = new ArrayList<>();
        values.add("NOFORN");
        values.add("FGI");
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "fbloggs", values);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jrogers", values);
        caveatConfigService.updateRMConstraintListAuthority(RM_LIST, "jdoe", values);

        AuthenticationUtil.setFullyAuthenticatedUser("jdoe");
        /**
         * Positive test Get the constraint
         */
        {
            String url = URL_RM_CONSTRAINTS + "/" + RM_LIST_URI_ELEM;
            Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);
            JSONObject top = new JSONObject(response.getContentAsString());

            JSONObject data = top.getJSONObject("data");
            System.out.println(response.getContentAsString());

            data.getJSONArray("allowedValuesForCurrentUser");

        }


        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        deleteUser("fbloggs");
        deleteUser("jrogers");
        deleteUser("jdoe");

    }

}


