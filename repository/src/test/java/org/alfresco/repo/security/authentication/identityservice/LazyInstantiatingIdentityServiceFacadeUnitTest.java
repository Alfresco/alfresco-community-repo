/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacadeFactoryBean.LazyInstantiatingIdentityServiceFacade;
import org.junit.Test;

public class LazyInstantiatingIdentityServiceFacadeUnitTest
{
    private static final String USER_NAME = "marlon";
    private static final String PASSWORD = "brando";
    private static final String TOKEN = "token";
    @Test
    public void shouldRecoverFromInitialAuthorizationServerUnavailability()
    {
        final IdentityServiceFacade targetFacade = mock(IdentityServiceFacade.class);
        final LazyInstantiatingIdentityServiceFacade facade = new LazyInstantiatingIdentityServiceFacade(faultySupplier(3, targetFacade));

        assertThatExceptionOfType(IdentityServiceFacadeException.class)
                .isThrownBy(() -> facade.extractUsernameFromToken(TOKEN))
                .havingCause().withNoCause().withMessage("Expected failure #1");
        verifyNoInteractions(targetFacade);

        assertThatExceptionOfType(IdentityServiceFacadeException.class)
                .isThrownBy(() -> facade.verifyCredentials(USER_NAME, PASSWORD))
                .havingCause().withNoCause().withMessage("Expected failure #2");
        verifyNoInteractions(targetFacade);

        assertThatExceptionOfType(IdentityServiceFacadeException.class)
                .isThrownBy(() -> facade.extractUsernameFromToken(TOKEN))
                .havingCause().withNoCause().withMessage("Expected failure #3");
        verifyNoInteractions(targetFacade);

        facade.verifyCredentials(USER_NAME, PASSWORD);
        verify(targetFacade).verifyCredentials(USER_NAME, PASSWORD);
    }

    @Test
    public void shouldAvoidCreatingMultipleInstanceOfOAuth2AuthorizedClientManager()
    {
        final IdentityServiceFacade targetFacade = mock(IdentityServiceFacade.class);
        final Supplier<IdentityServiceFacade> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn(targetFacade);

        final LazyInstantiatingIdentityServiceFacade facade = new LazyInstantiatingIdentityServiceFacade(supplier);

        facade.verifyCredentials(USER_NAME, PASSWORD);
        facade.extractUsernameFromToken(TOKEN);
        facade.verifyCredentials(USER_NAME, PASSWORD);
        facade.extractUsernameFromToken(TOKEN);
        facade.verifyCredentials(USER_NAME, PASSWORD);
        verify(supplier, times(1)).get();
        verify(targetFacade, times(3)).verifyCredentials(USER_NAME, PASSWORD);
        verify(targetFacade, times(2)).extractUsernameFromToken(TOKEN);
    }

    private Supplier<IdentityServiceFacade> faultySupplier(int numberOfInitialFailures, IdentityServiceFacade facade)
    {
        final int[] counter = new int[]{0};
        return () -> {
            if (counter[0]++ < numberOfInitialFailures)
            {
                throw new RuntimeException("Expected failure #" + counter[0]);
            }
            return facade;
        };
    }
}
