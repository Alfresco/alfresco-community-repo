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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spring helper that selects the {@link UserTokenProvider} implementation to expose to consumers based on the {@code identity-service.authentication.userTokenCache.enabled} flag.
 *
 * <p>
 * Both the direct and the caching providers are constructed by Spring at startup; this helper merely returns one of them. Switching the flag therefore requires only a context reload, not a code change. Consumers receive {@code UserTokenProvider} and remain unaware of which implementation backs them.
 * </p>
 */
public final class UserTokenProviders
{
    private static final Log LOGGER = LogFactory.getLog(UserTokenProviders.class);

    private UserTokenProviders()
    {
        // Utility class
    }

    /**
     * @param cacheEnabled
     *            value of {@code identity-service.authentication.userTokenCache.enabled}
     * @param direct
     *            the always-go-to-Keycloak provider
     * @param caching
     *            the local-JVM caching decorator wrapping {@code direct}
     * @return {@code caching} when the flag is on, otherwise {@code direct}
     */
    public static UserTokenProvider chooseProvider(boolean cacheEnabled,
            UserTokenProvider direct,
            UserTokenProvider caching)
    {
        if (cacheEnabled)
        {
            LOGGER.info("Identity Service user-token cache is ENABLED.");
            return caching;
        }
        LOGGER.info("Identity Service user-token cache is DISABLED.");
        return direct;
    }
}
