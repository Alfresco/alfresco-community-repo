/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.util;

import static org.alfresco.util.collections.CollectionUtils.filterKeys;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.Function;

import java.io.Serializable;
import java.util.Map;

/**
 * Provides additional methods of general use that could (in principle) be moved to the core services.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class CoreServicesExtras
{
    private DictionaryService dictionaryService;
    private NodeService       nodeService;

    public void setDictionaryService(DictionaryService service)
    {
        this.dictionaryService = service;
    }

    public void setNodeService(NodeService service)
    {
        this.nodeService = service;
    }

    /**
     * This method copies the property values for the specified aspect from one node to another.
     * All associations are ignored. Inherited properties are not copied.
     *
     * @param from the node whose property values are to be read.
     * @param to   the node to which the property values are to be written.
     * @return a Map of the property values which were copied.
     */
    public Map<QName, Serializable> copyAspect(final NodeRef from, final NodeRef to, final QName aspectQName)
    {
        final AspectDefinition aspectDefn = dictionaryService.getAspect(aspectQName);

        if (aspectDefn == null) { throw new DictionaryException("Unknown aspect: " + aspectQName); }

        final Map<QName, PropertyDefinition> aspectProperties = aspectDefn.getProperties();

        final Map<QName, Serializable> nodeProperties = nodeService.getProperties(from);
        final Map<QName, Serializable> relevantPropVals = filterKeys(nodeProperties, new Function<QName, Boolean>()
        {
            @Override public Boolean apply(QName value)
            {
                // Only copy property values that are defined on the provided aspect.
                final PropertyDefinition propDef = aspectProperties.get(value);
                return propDef != null && propDef.getContainerClass().getName().equals(aspectQName);
            }
        });
        nodeService.addProperties(to, relevantPropVals);
        return relevantPropVals;
    }
}
