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

package org.alfresco.repo.forms.processor.task;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 */
public class FieldCreationData
{
    private final QName type;

    private final TypeDefinition typeDef;

    private final Map<QName, Serializable> propValues;

    private final Map<QName, PropertyDefinition> propDefs;

    private final Map<QName, AssociationDefinition> assocDefs;

    private final FieldGroup group;

    private final List<String> forcedFields;

    public FieldCreationData(NodeRef nodeRef, List<String> forcedFields, FieldGroup group, NodeService nodeService,
                DictionaryService dictionaryService)
    {
        this.forcedFields = forcedFields;
        this.group = group;
        this.type = nodeService.getType(nodeRef);
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        this.typeDef = dictionaryService.getAnonymousType(type, aspects);

        // NOTE: the anonymous type returns all property and association
        // defs for all aspects applied as well as the type
        this.propDefs = typeDef.getProperties();
        this.assocDefs = typeDef.getAssociations();
        this.propValues = nodeService.getProperties(nodeRef);
    }

    public FieldCreationData(TypeDefinition typeDef, List<String> forcedFields, FieldGroup group)
    {
        this.propValues = null;

        this.forcedFields = forcedFields;
        this.group = group;
        this.typeDef = typeDef;
        this.type = typeDef.getName();

        // we only get the properties and associations of the actual type so
        // we also need to manually get properties and associations from any
        // mandatory aspects
        this.propDefs = new HashMap<QName, PropertyDefinition>(16);
        this.assocDefs = new HashMap<QName, AssociationDefinition>(16);
        propDefs.putAll(typeDef.getProperties());
        assocDefs.putAll(typeDef.getAssociations());

        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            propDefs.putAll(aspect.getProperties());
            assocDefs.putAll(aspect.getAssociations());
        }
    }

    public FieldCreationData(WorkflowTask task, List<String> forcedFields, FieldGroup group)
    {
        this.forcedFields = forcedFields;
        this.group = group;
        this.typeDef = task.definition.metadata;
        this.type = typeDef.getName();

        this.propDefs = populateProperties(typeDef);
        this.assocDefs = populateAssociations(typeDef);
        this.propValues = task.properties;
    }

    private Map<QName, AssociationDefinition> populateAssociations(TypeDefinition typeDef2)
    {
        HashMap<QName, AssociationDefinition> allProps = new HashMap<QName, AssociationDefinition>();
        allProps.putAll(typeDef.getAssociations());
        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            allProps.putAll(aspect.getAssociations());
        }
        return Collections.unmodifiableMap(allProps);
    }

    private Map<QName, PropertyDefinition> populateProperties(TypeDefinition typeDef)
    {
        HashMap<QName, PropertyDefinition> allProps = new HashMap<QName, PropertyDefinition>();
        allProps.putAll(typeDef.getProperties());
        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            allProps.putAll(aspect.getProperties());
        }
        return Collections.unmodifiableMap(allProps);
    }

    /**
     * @return the propValues
     */
    public Map<QName, Serializable> getPropValues()
    {
        return this.propValues;
    }

    /**
     * @return the propDefs
     */
    public Map<QName, PropertyDefinition> getPropDefs()
    {
        return this.propDefs;
    }

    /**
     * @return the assocDefs
     */
    public Map<QName, AssociationDefinition> getAssocDefs()
    {
        return this.assocDefs;
    }

    /**
     * @return the group
     */
    public FieldGroup getGroup()
    {
        return this.group;
    }

    /**
     * @return the forcedFields
     */
    public List<String> getForcedFields()
    {
        return this.forcedFields;
    }
}
