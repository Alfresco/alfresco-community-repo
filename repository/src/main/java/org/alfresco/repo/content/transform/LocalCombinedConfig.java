/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2019 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.transform.client.model.config.Transformer;
import org.alfresco.transform.client.registry.CombinedConfig;
import org.alfresco.transform.client.registry.TransformServiceRegistryImpl;
import org.apache.commons.logging.Log;

import java.util.List;

import static org.alfresco.repo.content.transform.LocalTransformServiceRegistry.LOCAL_TRANSFORMER;
import static org.alfresco.repo.content.transform.LocalTransformServiceRegistry.URL;

/**
 * Extends the standard CombinedConfig to add in removal of overridden or invalid transforms.
 *
 * @author adavis
 */
public class LocalCombinedConfig extends CombinedConfig
{
    public LocalCombinedConfig(Log log)
    {
        super(log);
    }

    /**
     * Discards a transformer that is invalid (e.g. T-Engines with the same name, baseUrl has not been specified on a
     * T-Engine transform) or overridden an earlier transform with the same name). If the overridden transform is from
     * a T-Engine and the overriding transform is not a pipeline or a failover, we also copy the {@code baseUrl} from
     * the overridden transform so that the original T-Engine will still be called.
     *
     * @param i the current transform's index into combinedTransformers.
     * @param combinedTransformers the full list of transformers in the order they were read.
     * @param registry that wil hold the transforms.
     * @param transformAndItsOrigin the current combinedTransformers element.
     * @param transformer the current transformer.
     * @param name the current transformer's name.
     * @param readFrom where the current transformer was read from.
     * @param isPipeline if the current transform is a pipeline.
     * @param isFailover if the current transform is a failover.
     *
     * @returns the index of a transform to be removed. {@code -1} is returned if there should not be a remove.
     * @throws IllegalArgumentException if the current transform has a problem and should be removed.
     */
    @Override
    protected int removeInvalidTransformer(int i, List<TransformAndItsOrigin> combinedTransformers,
                                               TransformServiceRegistryImpl registry,
                                               TransformAndItsOrigin transformAndItsOrigin, Transformer transformer,
                                               String name, String readFrom, boolean isPipeline, boolean isFailover)
    {
        int indexToRemove = -1;

        if (name == null || "".equals(name.trim()))
        {
            throw new IllegalArgumentException("Local transformer names may not be null. Read from " + readFrom);
        }

        // Get the baseUrl - test code might change it
        String baseUrl = transformAndItsOrigin.getBaseUrl();
        String testBaseUrl = ((LocalTransformServiceRegistry)registry).getBaseUrlIfTesting(name, baseUrl);
        if ((baseUrl == null && testBaseUrl != null) || !baseUrl.equals(testBaseUrl))
        {
            baseUrl = testBaseUrl;
            transformAndItsOrigin = new TransformAndItsOrigin(transformer, baseUrl, readFrom);
            combinedTransformers.set(i, transformAndItsOrigin);
        }

        boolean isOneStepTransform = !isPipeline && !isFailover && !name.equals(LocalPassThroughTransform.NAME);

        // Check to see if the name has been used before.
        int j = lastIndexOf(name, combinedTransformers, i);
        if (j >= 0)
        {
            if (baseUrl != null) // If a T-Engine, else it is an override
            {
                throw new IllegalArgumentException("Local T-Engine transformer " + transformerName(name) +
                        " must be a unique name. Read from " + readFrom);
            }

            if (isOneStepTransform)
            {
                // We need to set the baseUrl of the original transform in the one overriding,
                // so we can talk to its T-Engine
                TransformAndItsOrigin overriddenTransform = combinedTransformers.get(j);
                String overriddenBaseUrl = overriddenTransform.getBaseUrl();
                Transformer overriddenTransformTransform = transformAndItsOrigin.getTransformer();
                TransformAndItsOrigin overridingTransform = new TransformAndItsOrigin(
                        overriddenTransformTransform, overriddenBaseUrl, readFrom);
                combinedTransformers.set(i, overridingTransform);
            }
            indexToRemove = j;
        }
        else if (isOneStepTransform && baseUrl == null)
        {
            throw new IllegalArgumentException("Local T-Engine transformer " + transformerName(name) +
                    " must have its baseUrl set in " + LOCAL_TRANSFORMER + name + URL + " Read from " +
                    readFrom);
        }
        return indexToRemove;
    }

    protected static int lastIndexOf(String name, List<TransformAndItsOrigin> combinedTransformers, int toIndex)
    {
        // Lists are short (< 100) entries and this is not a frequent or time critical step, so walking the list
        // should be okay.
        for (int j = toIndex-1; j >=0; j--)
        {
            TransformAndItsOrigin transformAndItsOrigin = combinedTransformers.get(j);
            Transformer transformer = transformAndItsOrigin.getTransformer();
            String transformerName = transformer.getTransformerName();
            if (name.equals(transformerName))
            {
                return j;
            }
        }
        return -1;
    }
}
