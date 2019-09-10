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
package org.alfresco.transform.client.model.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class that builds a {@link InlineTransformer} given the source and target types and a pipeline of Transformers
 * for creating intermediary content.
 */
public class TransformBuilder
{
    public InlineTransformer buildPipeLine(String name, Set<SupportedSourceAndTarget> sourceAndTargetList,
                                     List<ChildTransformer> transformerList)
    {
        Set<TransformOption> options = new HashSet<>(transformerList.size());
        transformerList.forEach(t ->
                {
                    // Avoid creating an enpty TransformOptionGroup if the transformer has no options.
                    // Works with an empty TransformOptionGroup but adds to the complexity.
                    if (t.getTransformer().getTransformOptions() != null)
                    {
                        options.add(new TransformOptionGroup(t.isRequired(), t.getTransformer().getTransformOptions()));
                    }
                });
        return new InlineTransformer(name, options, sourceAndTargetList);
    }
}
