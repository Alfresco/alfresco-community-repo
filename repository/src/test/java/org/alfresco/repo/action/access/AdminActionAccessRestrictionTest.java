/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.action.access;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.util.BaseSpringTest;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThrows;

public class AdminActionAccessRestrictionTest extends BaseSpringTest {

    private static final String MAIL_ACTION = "mail";
    private static final String CONTROLLED_CONTEXT = ActionAccessRestriction.V1_ACTION_CONTEXT;

    private ActionAccessRestriction adminActionAccessRestriction;

    @Before
    public void setup() {
        adminActionAccessRestriction = applicationContext.getBean("adminActionAccessRestriction", AdminActionAccessRestriction.class);
    }

    @Test
    public void adminCanExecute() {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        Action action = createMailAction();
        adminActionAccessRestriction.verifyAccessRestriction(action);
    }

    @Test
    public void systemCanExecute() {
        AuthenticationUtil.setRunAsUserSystem();

        Action action = createMailAction();
        adminActionAccessRestriction.verifyAccessRestriction(action);
    }

    @Test
    public void userCantExecute() {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());

        Action action = createMailAction();
        assertThrows(ActionAccessException.class, () -> adminActionAccessRestriction.verifyAccessRestriction(action));
    }

    private Action createMailAction() {
        Map<String, Serializable> params = new HashMap<>();
        params.put("from", "admin@alfresco.com");
        params.put("to", "test@wp.pl");
        params.put("subject", "test");
        params.put("text", "test");

        Action action = new ActionImpl(null, "123", MAIL_ACTION, params);
        ActionAccessRestriction.setActionContext(action, CONTROLLED_CONTEXT);

        return action;
    }
}
