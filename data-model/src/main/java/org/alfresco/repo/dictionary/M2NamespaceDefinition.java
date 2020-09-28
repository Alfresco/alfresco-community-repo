/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    static List<M2ModelDiff> diffNamespaceDefinitionLists(Collection<NamespaceDefinition> previousNamespaces, Collection<NamespaceDefinition> newNamespaces)
    {
        List<M2ModelDiff> modelDiffs = new ArrayList<M2ModelDiff>();

        for (NamespaceDefinition previousNamespace: previousNamespaces)
        {
            boolean found = false;
            for (NamespaceDefinition newNamespace : newNamespaces)
            {
                if (newNamespace.getUri().equals(previousNamespace.getUri()))
                {
                    if(!newNamespace.getPrefix().equals(previousNamespace.getPrefix()))
                    {
                        modelDiffs.add(new M2ModelDiff(newNamespace.getModel().getName(), newNamespace, M2ModelDiff.TYPE_NAMESPACE, M2ModelDiff.DIFF_UPDATED));
                    }
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                modelDiffs.add(new M2ModelDiff(previousNamespace.getModel().getName(), M2ModelDiff.TYPE_NAMESPACE, M2ModelDiff.DIFF_DELETED));
            }
        }

        for (NamespaceDefinition newNamespace : newNamespaces)
        {
            boolean found = false;
            for (NamespaceDefinition previousNamespace : previousNamespaces)
            {
                if (newNamespace.getUri().equals(previousNamespace.getUri()))
                {
                    found = true;
                    break;
                }
            }
            
            if (!found)
            {
                modelDiffs.add(new M2ModelDiff(newNamespace.getModel().getName(), newNamespace, M2ModelDiff.TYPE_NAMESPACE, M2ModelDiff.DIFF_CREATED));
            }
        }
        return modelDiffs;
    }
}
