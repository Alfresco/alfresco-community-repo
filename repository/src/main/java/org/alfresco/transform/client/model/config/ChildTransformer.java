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

/**
 * Represents a single transformer in a pipeline of multiple transformers. A transformer's options may be optional or
 * required in the containing transformer. Historically in ACS only options for the final transformer were provided.
 */
public class ChildTransformer
{
    private boolean required;
    private Transformer transformer;

    public ChildTransformer()
    {
    }

    public ChildTransformer(boolean required, Transformer transformer)
    {
        this.required = required;
        this.transformer = transformer;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public Transformer getTransformer()
    {
        return transformer;
    }

    public void setTransformer(Transformer transformer)
    {
        this.transformer = transformer;
    }
}
