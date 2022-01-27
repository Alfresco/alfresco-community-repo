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

package org.alfresco.module.org_alfresco_module_rm.test.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;

import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * A helper to initialise a mock {@link AuthenticationUtil}.
 *
 * @author tpage
 */
public class MockAuthenticationUtilHelper
{
    public static final String SYSTEM_USER = "system";
    public static final String ADMIN_USER = "admin";
    public static final String GUEST_USER = "guest";

    /**
     * Set up a Mockito mock <code>AuthenticationUtil</code> so that it executes all methods assuming the user has
     * permissions. If the mock is asked for details about the user then it assumes the currently authenticated user is
     * "admin".
     *
     * @param mockAuthenticationUtil The mock to initialise.
     */
    public static void setup(AuthenticationUtil mockAuthenticationUtil)
    {
        setup(mockAuthenticationUtil, "admin");
    }

    /**
     * Set up a Mockito mock <code>AuthenticationUtil</code> so that it executes all methods assuming the user has
     * permissions.
     *
     * @param mockAuthenticationUtil The mock to initialise.
     * @param fullyAuthenticatedUser The name of the user that last authenticated.
     */
    @SuppressWarnings("unchecked")
    public static void setup(AuthenticationUtil mockAuthenticationUtil, String fullyAuthenticatedUser)
    {
        reset(mockAuthenticationUtil);

        // just do the work
        lenient().doAnswer(new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                RunAsWork work = (RunAsWork) invocation.getArguments()[0];
                return work.doWork();
            }

        }).when(mockAuthenticationUtil).<Object> runAsSystem(any(RunAsWork.class));

        // just do the work
        lenient().doAnswer(new Answer<Object>()
        {
            @SuppressWarnings("rawtypes")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                RunAsWork work = (RunAsWork) invocation.getArguments()[0];
                return work.doWork();
            }

        }).when(mockAuthenticationUtil).<Object> runAs(any(RunAsWork.class), anyString());

        lenient().when(mockAuthenticationUtil.getAdminUserName()).thenReturn(ADMIN_USER);
        lenient().when(mockAuthenticationUtil.getFullyAuthenticatedUser()).thenReturn(fullyAuthenticatedUser);
        lenient().when(mockAuthenticationUtil.getRunAsUser()).thenReturn(fullyAuthenticatedUser);
        lenient().when(mockAuthenticationUtil.getSystemUserName()).thenReturn(SYSTEM_USER);
        lenient().when(mockAuthenticationUtil.getGuestUserName()).thenReturn(GUEST_USER);
    }
}
