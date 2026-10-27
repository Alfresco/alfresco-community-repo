/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static org.junit.Assert.assertThrows;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.BaseSpringTest;

public class ConfigurableActionAccessRestrictionTest extends BaseSpringTest
{

    private static final String MAIL_ACTION = "mail";
    private static final String SCRIPT_ACTION = "script";
    private static final String UNLISTED_ACTION = "copy";
    private static final String CONTROLLED_CONTEXT = ActionAccessRestriction.V1_ACTION_CONTEXT;

    private static final String GROUP_SHORT_NAME = "CONFIG_RESTRICTION_TEST";
    private static final String GROUP_FULL_NAME = "GROUP_" + GROUP_SHORT_NAME;

    private ConfigurableActionAccessRestriction configurableActionAccessRestriction;
    private AuthorityService authorityService;
    private NodeService nodeService;

    @Before
    public void setup()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        configurableActionAccessRestriction = applicationContext.getBean("configurableActionAccessRestriction", ConfigurableActionAccessRestriction.class);
        authorityService = applicationContext.getBean("AuthorityService", AuthorityService.class);
        nodeService = applicationContext.getBean("NodeService", NodeService.class);

        if (!authorityService.authorityExists(GROUP_FULL_NAME))
        {
            authorityService.createAuthority(AuthorityType.GROUP, GROUP_SHORT_NAME);
        }
    }

    @After
    public void teardown()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        if (authorityService.authorityExists(GROUP_FULL_NAME))
        {
            authorityService.deleteAuthority(GROUP_FULL_NAME);
        }
    }

    @Test
    public void adminCanExecuteMail()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        configurableActionAccessRestriction.verifyAccessRestriction(createAction(MAIL_ACTION));
    }

    @Test
    public void systemCanExecuteMail()
    {
        AuthenticationUtil.setRunAsUserSystem();

        configurableActionAccessRestriction.verifyAccessRestriction(createAction(MAIL_ACTION));
    }

    @Test
    public void guestCannotExecuteMailByDefault()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());

        assertThrows(ActionAccessException.class, () -> configurableActionAccessRestriction.verifyAccessRestriction(createAction(MAIL_ACTION)));
    }

    @Test
    public void actionWithoutSettingIsUnrestricted()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());

        configurableActionAccessRestriction.verifyAccessRestriction(createAction(UNLISTED_ACTION));
    }

    @Test
    public void blankSettingIsAdminOnly()
    {
        ConfigurableActionAccessRestriction restriction = restrictionWithSetting(SCRIPT_ACTION, "");
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());

        assertThrows(ActionAccessException.class, () -> restriction.verifyAccessRestriction(createAction(SCRIPT_ACTION)));
    }

    @Test
    public void groupMemberCanExecute()
    {
        authorityService.addAuthority(GROUP_FULL_NAME, AuthenticationUtil.getGuestUserName());
        ConfigurableActionAccessRestriction restriction = restrictionWithSetting(SCRIPT_ACTION, GROUP_SHORT_NAME);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());

        restriction.verifyAccessRestriction(createAction(SCRIPT_ACTION));
    }

    @Test
    public void nonMemberCannotExecute()
    {
        ConfigurableActionAccessRestriction restriction = restrictionWithSetting(SCRIPT_ACTION, GROUP_SHORT_NAME);
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());

        assertThrows(ActionAccessException.class, () -> restriction.verifyAccessRestriction(createAction(SCRIPT_ACTION)));
    }

    @Test
    public void spacesAroundGroupNamesAreIgnored()
    {
        authorityService.addAuthority(GROUP_FULL_NAME, AuthenticationUtil.getGuestUserName());
        ConfigurableActionAccessRestriction restriction = restrictionWithSetting(SCRIPT_ACTION, " OTHER_GROUP , " + GROUP_SHORT_NAME + " ");
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());

        restriction.verifyAccessRestriction(createAction(SCRIPT_ACTION));
    }

    @Test
    public void actionOutsideControlledContextIsNotChecked()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());

        Action action = createAction(MAIL_ACTION);
        ActionAccessRestriction.setActionContext(action, null);

        configurableActionAccessRestriction.verifyAccessRestriction(action);
    }

    private ConfigurableActionAccessRestriction restrictionWithSetting(String actionName, String allowedGroups)
    {
        Properties properties = new Properties();
        properties.setProperty("action." + actionName + ".allowedGroups", allowedGroups);

        ConfigurableActionAccessRestriction restriction = new ConfigurableActionAccessRestriction();
        restriction.setAuthorityService(authorityService);
        restriction.setNodeService(nodeService);
        restriction.setConfigProperties(properties);
        return restriction;
    }

    private Action createAction(String actionName)
    {
        Map<String, Serializable> params = new HashMap<>();
        Action action = new ActionImpl(null, "123", actionName, params);
        ActionAccessRestriction.setActionContext(action, CONTROLLED_CONTEXT);
        return action;
    }
}
