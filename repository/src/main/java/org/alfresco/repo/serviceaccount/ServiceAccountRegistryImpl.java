/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.serviceaccount;

import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.PropertyCheck;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Processes the <b>alfresco-global</b> properties file and applies a naming convention to distinguish the service
 * account's name and role.
 * <p>
 * The naming convention adheres to the following format:
 * <p>
 * <pre>
 *   {@code
 *     serviceaccount.role.<service-account-name>=<service-account-role>
 *   }
 * </pre>
 * <p>
 * Please note, any property with an invalid role value will be disregarded and the corresponding service account
 * will not be registered.
 * <p>
 * For instance, to register a service account named 'custom-app-sa' with the 'Editor' role (which allows it to
 * update node properties), the following should be defined in the <b>alfresco-global</b> properties file:
 * <ul>
 *   <li>serviceaccount.role.custom-app-sa=EDITOR_SERVICE_ACCOUNT</li>
 *   <li>or</li>
 *   <li>serviceaccount.role.custom-app-sa=ROLE_EDITOR_SERVICE_ACCOUNT</li>
 * </ul>
 *
 * @author Jamal Kaabi-Mofrad
 */
public class ServiceAccountRegistryImpl implements ServiceAccountRegistry, InitializingBean
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccountRegistryImpl.class);

    public static final String KEY_PREFIX = "serviceaccount.role.";

    private Properties globalProperties;
    private final ConcurrentMap<String, String> saRoleMap = new ConcurrentHashMap<>();

    public void setGlobalProperties(Properties globalProperties)
    {
        this.globalProperties = globalProperties;
    }

    @Override
    public void register(String serviceAccountName, String serviceAccountRole)
    {
        saRoleMap.put(serviceAccountName, serviceAccountRole);
        LOGGER.info("Service account '{}' is registered with the role '{}'.", serviceAccountName, serviceAccountRole);
    }

    @Override
    public Optional<String> getServiceAccountRole(String serviceAccountName)
    {
        return Optional.ofNullable(saRoleMap.get(serviceAccountName));
    }

    @Override
    public Set<String> getServiceAccountNames()
    {
        return Set.copyOf(saRoleMap.keySet());
    }

    private void init()
    {
        globalProperties.stringPropertyNames()
                .stream()
                .filter(key -> key.startsWith(KEY_PREFIX))
                .forEach(key -> {
                    String name = key.substring(KEY_PREFIX.length());
                    if (isNotValidProperty(key, name, "name"))
                    {
                        return;
                    }
                    String role = globalProperties.getProperty(key);
                    if (isNotValidProperty(key, role, "role"))
                    {
                        return;
                    }
                    // Ensure the role is in uppercase and has the prefix
                    role = role.toUpperCase(Locale.ENGLISH);
                    role = getRoleWithPrefix(role);
                    if (!PermissionService.SVC_AUTHORITIES_SET.contains(role))
                    {
                        LOGGER.warn("Invalid service account role '{}'. The role is not recognized.", role);
                        return;
                    }

                    // Register the service account name with the corresponding role.
                    register(name, role);
                });
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "globalProperties", globalProperties);
        init();
    }

    private String getRoleWithPrefix(String saRole)
    {
        if (!saRole.startsWith(PermissionService.ROLE_PREFIX))
        {
            saRole = PermissionService.ROLE_PREFIX + saRole;
        }
        return saRole;
    }

    private boolean isNotValidProperty(String key, String value, String valueType)
    {
        if (StringUtils.isBlank(value))
        {
            LOGGER.warn("Invalid service account {} defined in the property '{}'. The {} cannot be an empty string.",
                        valueType, key, valueType);
            return true;
        }
        return false;
    }
}
