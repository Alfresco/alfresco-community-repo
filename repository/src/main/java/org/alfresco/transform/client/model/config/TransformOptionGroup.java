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

/**
 * Represents a group of one or more options. If the group is optional, child options that are marked as required are
 * only required if any child in the group is supplied by the client. If the group is required, child options are
 * optional or required based on their own setting alone. The top
 */
public class TransformOptionGroup implements TransformOption
{
    private boolean required;
    List<TransformOption> transformOptions;

    public TransformOptionGroup()
    {
    }

    public TransformOptionGroup(boolean required, List<TransformOption> transformOptions)
    {
        setRequired(required);
        setTransformOptions(transformOptions);
    }

    @Override
    public boolean isRequired()
    {
        return required;
    }

    @Override
    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public List<TransformOption> getTransformOptions()
    {
        return transformOptions;
    }

    public void setTransformOptions(List<TransformOption> transformOptions)
    {
        this.transformOptions = transformOptions;
    }
}
