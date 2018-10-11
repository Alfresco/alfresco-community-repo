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

import java.util.List;
import java.util.Objects;

/**
 * Represents a set of transformations supported by the Transform Service that share the same transform options. Each
 * may be an actual transformer or the amalgamation of multiple transformers. It is possible that more than one
 * transformer may able to perform a transformation from one mimetype to another. The actual selection of transformer
 * is up to the Transform Service to decide. Clients may use {@link TransformServiceRegistry#isSupported} to decide
 * if they should send a request to the Transform Service. As a result clients have a simple generic view of
 * transformations which allows new transformations to be added without the need change client data structures other
 * than to define new name value pairs. For this to work the Transform Service defines unique names for each option.
 * <ul>
 *     <lI>name - is unique. The client should infer nothing from the name as it is simply a label.</lI>
 *     <li>version - of the transformer. The client should infer nothing from the value and should only use it
 *     in messages. There should only be one version supplied to the client for each name.</li>
 *     <li>transformOptions - a grouping of individual transformer transformOptions. The group may be optional and may
 *     contain nested transformOptions.</li>
 * </ul>
 */
public class Transformer
{
    private String name;
    private String version;
    private List<TransformOption> transformOptions;
    private List<SupportedSourceAndTarget> supportedSourceAndTargetList;

    public Transformer()
    {
    }

    public Transformer(String name, String version, List<TransformOption> transformOptions, List<SupportedSourceAndTarget> supportedSourceAndTargetList)
    {
        setName(name);
        setVersion(version);
        setTransformOptions(transformOptions);
        setSupportedSourceAndTargetList(supportedSourceAndTargetList);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
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
