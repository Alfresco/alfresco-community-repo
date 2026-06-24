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
package org.alfresco.opencmis.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

public class RuntimeSwitchingCMISFacadeTest
{
    private static final CMISFacade FALLBACK_FACADE = Mockito.mock(CMISFacade.class);

    private static final CMISFacade FACADE_A = Mockito.mock(CMISFacade.class);
    private static final CMISFacade FACADE_B = Mockito.mock(CMISFacade.class);

    private static final CMISFacadeProvider NULL_PROVIDER = () -> null;
    private static final CMISFacadeProvider FACADE_A_PROVIDER = () -> FACADE_A;
    private static final CMISFacadeProvider FACADE_B_PROVIDER = () -> FACADE_B;

    @Test
    public void shouldFailWhenFallbackNotSet()
    {
        RuntimeSwitchingCMISFacade facade = givenSwitchingFacade(null);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(facade::getTargetFacade);

        facade = givenSwitchingFacade(null, providers("null", NULL_PROVIDER));
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(facade::getTargetFacade);
    }

    @Test
    public void shouldReturnFallbackFacade()
    {
        RuntimeSwitchingCMISFacade facade = givenSwitchingFacade(FALLBACK_FACADE);
        assertThat(facade.getTargetFacade())
                .isEqualTo(FALLBACK_FACADE);

        facade = givenSwitchingFacade(FALLBACK_FACADE, providers("null", NULL_PROVIDER));
        assertThat(facade.getTargetFacade())
                .isEqualTo(FALLBACK_FACADE);
    }

    @Test
    public void shouldReturnProvidedFacade()
    {
        RuntimeSwitchingCMISFacade facade = givenSwitchingFacade(FALLBACK_FACADE, providers(
                "null", NULL_PROVIDER,
                "a", FACADE_A_PROVIDER));
        assertThat(facade.getTargetFacade())
                .isEqualTo(FACADE_A);
    }

    @Test
    public void shouldReturnFirstMatchingFacade()
    {
        RuntimeSwitchingCMISFacade facade = givenSwitchingFacade(FALLBACK_FACADE, providers(
                "null", NULL_PROVIDER,
                "b", FACADE_B_PROVIDER,
                "a", NULL_PROVIDER));

        assertThat(facade.getTargetFacade())
                .isEqualTo(FACADE_B);
    }

    @Test
    public void shouldNotFailOnConflictingFacadesWhenFailureReportingIsDisabled()
    {
        RuntimeSwitchingCMISFacade facade = givenSwitchingFacade(FALLBACK_FACADE, providers(
                "null", NULL_PROVIDER,
                "b", FACADE_B_PROVIDER,
                "a", FACADE_A_PROVIDER));
        facade.setFailOnManyFacades(false);

        assertThat(facade.getTargetFacade())
                .isEqualTo(FACADE_B);
    }

    @Test
    public void shouldFailOnConflictingFacadesWhenFailureReportingIsEnabled()
    {
        RuntimeSwitchingCMISFacade facade = givenSwitchingFacade(FALLBACK_FACADE, providers(
                "null", NULL_PROVIDER,
                "b", FACADE_B_PROVIDER,
                "a", FACADE_A_PROVIDER));
        facade.setFailOnManyFacades(true);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(facade::getTargetFacade);
    }

    private RuntimeSwitchingCMISFacade givenSwitchingFacade(CMISFacade fallback)
    {
        return givenSwitchingFacade(fallback, Map.of());
    }

    private RuntimeSwitchingCMISFacade givenSwitchingFacade(CMISFacade fallback, Map<String, CMISFacadeProvider> providers)
    {
        final RuntimeSwitchingCMISFacade facade = new RuntimeSwitchingCMISFacade(providers);
        if (fallback != null)
        {
            facade.setFallbackFacade(fallback);
        }
        return facade;
    }

    private LinkedHashMap<String, CMISFacadeProvider> providers(String name1, CMISFacadeProvider provider1, String name2, CMISFacadeProvider provider2, String name3, CMISFacadeProvider provider3)
    {
        LinkedHashMap<String, CMISFacadeProvider> result = providers(name1, provider1, name2, provider2);
        result.put(name3, provider3);
        return result;
    }

    private LinkedHashMap<String, CMISFacadeProvider> providers(String name1, CMISFacadeProvider provider1, String name2, CMISFacadeProvider provider2)
    {
        LinkedHashMap<String, CMISFacadeProvider> result = providers(name1, provider1);
        result.put(name2, provider2);
        return result;
    }

    private LinkedHashMap<String, CMISFacadeProvider> providers(String name, CMISFacadeProvider provider)
    {
        LinkedHashMap<String, CMISFacadeProvider> result = new LinkedHashMap<>();
        result.put(name, provider);
        return result;
    }

}
