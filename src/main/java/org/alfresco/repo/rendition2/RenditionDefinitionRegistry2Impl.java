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

import org.alfresco.transform.client.model.config.TransformServiceRegistry;
import org.alfresco.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A registry of rendition definitions.
 *
 * @author adavis
 */
public class RenditionDefinitionRegistry2Impl implements RenditionDefinitionRegistry2
{
    private TransformServiceRegistry transformServiceRegistry;

    private final Map<String, RenditionDefinition2> renditionDefinitions = new HashMap();
    private final Map<String, Set<Pair<String, Long>>> renditionsFor = new HashMap<>();

    public void setTransformServiceRegistry(TransformServiceRegistry transformServiceRegistry)
    {
        this.transformServiceRegistry = transformServiceRegistry;
        renditionsFor.clear();
    }

    /**
     * Obtains a {@link RenditionDefinition2} by name.
     * @param renditionName to be returned
     * @return the {@link RenditionDefinition2} or null if not registered.
     * @deprecated use {@link #getRenditionDefinition(String)}
     */
    public RenditionDefinition2 getDefinition(String renditionName)
    {
        return getRenditionDefinition(renditionName);
    }

    public void register(RenditionDefinition2 renditionDefinition)
    {
        String renditionName = renditionDefinition.getRenditionName();
        RenditionDefinition2 original = getDefinition(renditionName);
        if (original != null)
        {
            throw new IllegalArgumentException("RenditionDefinition "+renditionName+" was already registered.");
        }
        renditionDefinitions.put(renditionName, renditionDefinition);
    }

    public void unregister(String renditionName)
    {
        if (renditionDefinitions.remove(renditionName) == null)
        {
            throw new IllegalArgumentException("RenditionDefinition "+renditionName+" was not registered.");
        }
    }

    @Override
    public Set<String> getRenditionNames()
    {
        return renditionDefinitions.keySet();
    }

    @Override
    public Set<String> getRenditionNamesFrom(String sourceMimetype, long size)
    {
        Set<Pair<String, Long>> renditionNamesWithMaxSize;
        synchronized (renditionsFor)
        {
            renditionNamesWithMaxSize = renditionsFor.get(sourceMimetype);
            if (renditionNamesWithMaxSize == null)
            {
                renditionNamesWithMaxSize = getRenditionNamesWithMaxSize(sourceMimetype);
                renditionsFor.put(sourceMimetype, renditionNamesWithMaxSize);
            }
        }

        if (renditionNamesWithMaxSize.isEmpty())
        {
            return Collections.emptySet();
        }

        Set<String> renditionNames = new HashSet<>();
        for (Pair<String, Long> pair : renditionNamesWithMaxSize)
        {
            Long maxSize = pair.getSecond();
            if (maxSize != 0 && (maxSize == -1L || maxSize >= size))
            {
                String renditionName = pair.getFirst();
                renditionNames.add(renditionName);
            }
        }
        return renditionNames;
    }

    // Gets a list of rendition names that can be created from the given sourceMimetype.
    // Includes the maxSize for each.
    private Set<Pair<String,Long>> getRenditionNamesWithMaxSize(String sourceMimetype)
    {
        Set<Pair<String,Long>> renditions = new HashSet();
        for (Map.Entry<String, RenditionDefinition2> entry : renditionDefinitions.entrySet())
        {
            RenditionDefinition2 renditionDefinition2 = entry.getValue();
            String targetMimetype = renditionDefinition2.getTargetMimetype();
            String renditionName = renditionDefinition2.getRenditionName();
            Map<String, String> options = renditionDefinition2.getTransformOptions();
            Long maxSize = transformServiceRegistry.getMaxSize(sourceMimetype, targetMimetype, options, renditionName);
            if (maxSize != null)
            {
                String renditionNameMaxSizePair = entry.getKey();
                Pair<String, Long> pair = new Pair<>(renditionNameMaxSizePair, maxSize);
                renditions.add(pair);
            }
        }
        return renditions;
    }

    @Override
    public RenditionDefinition2 getRenditionDefinition(String renditionName)
    {
        return renditionDefinitions.get(renditionName);
    }
}
