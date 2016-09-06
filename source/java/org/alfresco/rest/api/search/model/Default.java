/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.search.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * POJO class representing the search Defaults
 */
public class Default
{
    private final List<String> textAttributes;
    private final String defaultFTSOperator;
    private final String defaultFTSFieldOperator;
    private final String namespace;
    private final String defaultFieldName;

    @JsonCreator
    public Default(@JsonProperty("textAttributes")  List<String> textAttributes,
                   @JsonProperty("defaultFTSOperator")  String defaultFTSOperator,
                   @JsonProperty("defaultFTSFieldOperator")  String defaultFTSFieldOperator,
                   @JsonProperty("namespace")  String namespace,
                   @JsonProperty("defaultFieldName")  String defaultFieldName)
    {
        this.textAttributes = textAttributes;
        this.defaultFTSOperator = defaultFTSOperator;
        this.defaultFTSFieldOperator = defaultFTSFieldOperator;
        this.namespace = namespace;
        this.defaultFieldName = defaultFieldName;
    }

    public List<String> getTextAttributes()
    {
        return textAttributes;
    }

    public String getDefaultFTSOperator()
    {
        return defaultFTSOperator;
    }

    public String getDefaultFTSFieldOperator()
    {
        return defaultFTSFieldOperator;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public String getDefaultFieldName()
    {
        return defaultFieldName;
    }
}
