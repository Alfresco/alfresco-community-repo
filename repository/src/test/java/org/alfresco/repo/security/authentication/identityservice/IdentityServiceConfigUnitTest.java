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
package org.alfresco.repo.security.authentication.identityservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

public class IdentityServiceConfigUnitTest
{
    private final IdentityServiceConfig config = new IdentityServiceConfig();

    @Test
    public void shouldNotConfigureAdditionalParametersByDefault()
    {
        assertThat(config.getAuthorizationAdditionalParams()).isEmpty();

        config.setAuthorizationAdditionalParams(null);
        assertThat(config.getAuthorizationAdditionalParams()).isEmpty();

        config.setAuthorizationAdditionalParams("  ");
        assertThat(config.getAuthorizationAdditionalParams()).isEmpty();
    }

    @Test
    public void shouldParseAdditionalAuthorizationParameters()
    {
        config.setAuthorizationAdditionalParams("resourceServer=Identity Provider unencrypted, foo = bar,expression=a=b,empty=");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(
                Map.entry("resourceServer", "Identity Provider unencrypted"),
                Map.entry("foo", "bar"),
                Map.entry("expression", "a=b"),
                Map.entry("empty", ""));
    }

    @Test
    public void shouldIgnoreMalformedAdditionalAuthorizationParameters()
    {
        config.setAuthorizationAdditionalParams("valid=one,missingSeparator,=missingName,   ,alsoValid=two");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(
                Map.entry("valid", "one"),
                Map.entry("alsoValid", "two"));
    }

    @Test
    public void shouldUseLastValueWhenParameterIsConfiguredMoreThanOnce()
    {
        config.setAuthorizationAdditionalParams("duplicate=first,duplicate=last");

        assertThat(config.getAuthorizationAdditionalParams()).containsExactly(Map.entry("duplicate", "last"));
    }
}
