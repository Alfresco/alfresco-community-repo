/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.dictionary;

import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;


/**
 * Namespace Definition.
 * 
 *
 */
public class M2NamespaceDefinition implements NamespaceDefinition
{
    ModelDefinition model = null;
    private String uri = null;
    private String prefix = null;
   
    
    /*package*/ M2NamespaceDefinition(ModelDefinition model, String uri, String prefix)
    {
        this.model = model;
        this.uri = uri;
        this.prefix = prefix;
    }

    public ModelDefinition getModel()
    {
        return model;
    }
    
    public String getUri()
    {
        return uri;
    }

    public String getPrefix()
    {
        return prefix;
    }
}
