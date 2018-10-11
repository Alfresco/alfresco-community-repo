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
import java.util.List;

/**
 * Helper class that builds a {@link Transformer} given the source and target extensions and a pipeline of Transformers
 * for creating intermediary content, or a set of failover transformers.
 */
public class TransformBuilder
{
    public Transformer buildPipeLine(String name, String version, List<SupportedSourceAndTarget> sourceAndTargetList,
                                     List<ChildTransformer> transformerList)
    {
        List<TransformOption> options = new ArrayList<>(transformerList.size());
        transformerList.forEach(t ->
                {
                    // Avoid creating an enpty TransformOptionGroup if the transformer has no options.
                    // Works with an empty TransformOptionGroup but adds to the complexity.
                    if (t.getTransformer().getTransformOptions() != null)
                    {
                        options.add(new TransformOptionGroup(t.isRequired(), t.getTransformer().getTransformOptions()));
                    }
                });
        return new Transformer(name, version, options, sourceAndTargetList);
    }

    // TODO Commented out for now as it is unclear what the Transform service will support in terms of failover transformations.
    // Note: The use of a list of Transformers rather than ChildTransformers, as the supplied actual options would have
    //       to match one or more of the transformer's options. Matching one or more options is not currently
    //       implemented by the TransformServiceRegistry
    // public Transformer buildFailover(String name, String version, List<SupportedSourceAndTarget> sourceAndTargetList,
    //                                  List<Transformer> transformerList)
}
