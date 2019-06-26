/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import java.util.List;
import org.alfresco.transform.client.model.config.TransformServiceRegistry;

/**
 * Represents a set of transformations supported by the Transform Service or Local Transform Service Registry that
 * share the same transform options. Each may be an actual transformer or a pipeline of multiple transformers. It is
 * possible that more than one transformer may able to perform a transformation from one mimetype to another. The actual
 * selection of transformer is up to the Transform Service or Local Transform Service Registry to decide. Clients may
 * use {@link TransformServiceRegistry#isSupported(String, long, String, java.util.Map, String)} to decide
 * if they should send a request to the Transform Service. As a result clients have a simple generic view of
 * transformations which allows new transformations to be added without the need to change client data structures other
 * than to define new name value pairs. For this to work the Transform Service defines unique names for each option.
 * <ul>
 *     <li>transformerName - is optional but if supplied should be unique. The client should infer nothing from the name
 *     as it is simply a label, but the Local Transform Service Registry will use the name in pipelines.</lI>
 *     <li>transformOptions - a grouping of individual transformer transformOptions. The group may be optional and may
 *     contain nested transformOptions.</li>
 * </ul>
 * For local transforms, this structure is extended when defining a pipeline transform.
 * <ul>
 *     <li>transformerPipeline - an array of pairs of transformer name and target extension for each transformer in the
 *     pipeline. The last one should not have an extension as that is defined by the request and should be in the
 *     supported list.</li>
 * </ul>
 */
public class Transformer
{
    private String transformerName;
    private List<TransformStep> transformerPipeline;
    private List<TransformOption> transformOptions;
    private List<SupportedSourceAndTarget> supportedSourceAndTargetList;

    public Transformer()
    {
    }

    public Transformer(String transformerName, List<TransformOption> transformOptions, List<SupportedSourceAndTarget> supportedSourceAndTargetList)
    {
        setTransformerName(transformerName);
        setTransformOptions(transformOptions);
        setSupportedSourceAndTargetList(supportedSourceAndTargetList);
    }

    public Transformer(String transformerName, List<TransformStep> transformerPipeline, List<TransformOption> transformOptions, List<SupportedSourceAndTarget> supportedSourceAndTargetList)
    {
        this(transformerName, transformOptions, supportedSourceAndTargetList);
        setTransformerPipeline(transformerPipeline);
    }

    public String getTransformerName()
    {
        return transformerName;
    }

    public void setTransformerName(String transformerName)
    {
        this.transformerName = transformerName;
    }

    public List<TransformStep> getTransformerPipeline()
    {
        return transformerPipeline;
    }

    public void setTransformerPipeline(List<TransformStep> transformerPipeline)
    {
        this.transformerPipeline = transformerPipeline;
    }

    public List<TransformOption> getTransformOptions()
    {
        return transformOptions;
    }

    public void setTransformOptions(List<TransformOption> transformOptions)
    {
        this.transformOptions = transformOptions;
    }

    public List<SupportedSourceAndTarget> getSupportedSourceAndTargetList()
    {
        return supportedSourceAndTargetList;
    }

    public void setSupportedSourceAndTargetList(List<SupportedSourceAndTarget> supportedSourceAndTargetList)
    {
        this.supportedSourceAndTargetList = supportedSourceAndTargetList;
    }
}
