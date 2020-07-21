/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import java.util.Map;
import java.util.Objects;

/**
 * Standard implementation.
 *
 * @author adavis
 */
public class RenditionDefinition2Impl implements RenditionDefinition2
{
    private final String renditionName;
    private final String targetMimetype;
    private final Map<String, String> transformOptions;
    private final boolean dynamicallyLoaded;

    /**
     * Constructor used by statically (e.g. XML Spring beans) defined renditions.
     */
    public RenditionDefinition2Impl(String renditionName, String targetMimetype, Map<String, String> transformOptions,
                                    RenditionDefinitionRegistry2Impl registry)
    {
        this(renditionName, targetMimetype, transformOptions, false, registry);
    }

    /**
     * Constructor used by dynamically defined renditions that may be changed without restarting.
     */
    public RenditionDefinition2Impl(String renditionName, String targetMimetype, Map<String, String> transformOptions,
                                    boolean dynamicallyLoaded, RenditionDefinitionRegistry2Impl registry)
    {
        this.renditionName = renditionName;
        this.targetMimetype = targetMimetype;
        this.transformOptions = transformOptions;
        this.dynamicallyLoaded = dynamicallyLoaded;
        if (registry != null)
        {
            registry.register(this);
        }
    }

    public boolean isDynamicallyLoaded()
    {
        return dynamicallyLoaded;
    }

    @Override
    public String getRenditionName()
    {
        return renditionName;
    }

    @Override
    public String getTargetMimetype()
    {
        return targetMimetype;
    }

    @Override
    public Map<String, String> getTransformOptions()
    {
        return transformOptions;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof RenditionDefinition2Impl))
        {
            return false;
        }
        RenditionDefinition2Impl that = (RenditionDefinition2Impl) o;
        return Objects.equals(renditionName, that.renditionName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(renditionName);
    }
}
