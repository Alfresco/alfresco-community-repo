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
package org.alfresco.rest.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;

/**
 * Extends {@link RestProperties} to be able to change/add properties
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
@Getter
@Configuration
@PropertySource(value = {"classpath:default.properties", "classpath:config.properties"})
@PropertySource(value = "classpath:module.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:local.properties", ignoreResourceNotFound = true)
public class RMRestProperties extends RestProperties
{
    @Value ("${alfresco.scheme}")
    private String scheme;

    @Value ("${alfresco.server}")
    private String server;

    @Value ("${alfresco.port}")
    private String port;

    @Value ("${rest.rmPath}")
    private String restRmPath;

    @Value ("${docker.host}")
    private String dockerHost;
}
