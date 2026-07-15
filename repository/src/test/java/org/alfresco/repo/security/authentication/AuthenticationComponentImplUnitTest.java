/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import net.sf.acegisecurity.AuthenticationManager;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import org.alfresco.repo.security.authentication.AuthenticationComponent.UserNameValidationMode;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;

/**
 * Unit test verifying that, when a user logs in with a different case to the stored user name, the password re-hash is scheduled against the normalised (stored) user name and not the raw login name (MNT-25684).
 */
public class AuthenticationComponentImplUnitTest
{
    private static final String STORED_USER_NAME = "Andy";
    private static final String LOGIN_USER_NAME = "aNdY";

    private MockedStatic<TenantUtil> tenantUtil;
    private MockedStatic<AuthenticationUtil> authUtil;
    private MockedStatic<AlfrescoTransactionSupport> txSupport;

    @Before
    public void setUp()
    {
        tenantUtil = mockStatic(TenantUtil.class);
        authUtil = mockStatic(AuthenticationUtil.class);
        txSupport = mockStatic(AlfrescoTransactionSupport.class);

        // run the "system tenant" work inline and resolve the login name to its default tenant
        tenantUtil.when(() -> TenantUtil.runAsSystemTenant(any(), any()))
                .thenAnswer(call -> ((TenantRunAsWork<?>) call.getArgument(0)).doWork());
        authUtil.when(() -> AuthenticationUtil.getUserTenant(LOGIN_USER_NAME))
                .thenReturn(new Pair<>(LOGIN_USER_NAME, TenantService.DEFAULT_DOMAIN));
    }

    @After
    public void tearDown()
    {
        tenantUtil.close();
        authUtil.close();
        txSupport.close();
    }

    @Test
    public void testReHashUpdatesStoredUserNameNotLoginName() throws Exception
    {
        List<String> hashIndicator = List.of("md4");
        RepositoryAuthenticatedUser storedUser = new RepositoryAuthenticatedUser(STORED_USER_NAME, "hash", true, true, true, true,
                new GrantedAuthorityImpl[]{new GrantedAuthorityImpl("ROLE_USER")}, hashIndicator, "salt");

        MutableAuthenticationDao authenticationDao = mock(MutableAuthenticationDao.class);
        when(authenticationDao.loadUserByUsername(STORED_USER_NAME)).thenReturn(storedUser);

        PersonService personService = mock(PersonService.class);
        when(personService.getUserIdentifier(LOGIN_USER_NAME)).thenReturn(STORED_USER_NAME);

        CompositePasswordEncoder passwordEncoder = mock(CompositePasswordEncoder.class);
        when(passwordEncoder.lastEncodingIsPreferred(hashIndicator)).thenReturn(false);

        RetryingTransactionHelper txHelper = mock(RetryingTransactionHelper.class);
        when(txHelper.doInTransaction(any(), anyBoolean()))
                .thenAnswer(call -> ((RetryingTransactionCallback<?>) call.getArgument(0)).execute());
        when(txHelper.doInTransaction(any(), anyBoolean(), anyBoolean()))
                .thenAnswer(call -> ((RetryingTransactionCallback<?>) call.getArgument(0)).execute());
        TransactionService transactionService = mock(TransactionService.class);
        when(transactionService.getRetryingTransactionHelper()).thenReturn(txHelper);

        AuthenticationComponentImpl authenticationComponent = spy(new AuthenticationComponentImpl());
        authenticationComponent.setAuthenticationManager(mock(AuthenticationManager.class));
        authenticationComponent.setAuthenticationDao(authenticationDao);
        authenticationComponent.setCompositePasswordEncoder(passwordEncoder);
        authenticationComponent.setPersonService(personService);
        authenticationComponent.setTransactionService(transactionService);
        doReturn(null).when(authenticationComponent).setCurrentUser(any(), any(UserNameValidationMode.class));

        authenticationComponent.authenticateImpl(LOGIN_USER_NAME, "auth1".toCharArray());

        ArgumentCaptor<TransactionListener> listener = ArgumentCaptor.forClass(TransactionListener.class);
        txSupport.verify(() -> AlfrescoTransactionSupport.bindListener(listener.capture()));

        // the deferred re-hash must run against the stored name (Andy), not the login name (aNdY)
        listener.getValue().afterCommit();
        verify(authenticationDao).updateUser(eq(STORED_USER_NAME), any());
    }
}
