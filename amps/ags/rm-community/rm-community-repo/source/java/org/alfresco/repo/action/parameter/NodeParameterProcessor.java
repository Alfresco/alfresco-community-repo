/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.action.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Node parameter processor.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class NodeParameterProcessor extends ParameterProcessor implements ParameterSubstitutionSuggester
{
    /** Supported data types */
    private QName[] supportedDataTypes =
    {
            DataTypeDefinition.TEXT,
            DataTypeDefinition.BOOLEAN,
            DataTypeDefinition.DATE,
            DataTypeDefinition.DATETIME,
            DataTypeDefinition.DOUBLE,
            DataTypeDefinition.FLOAT,
            DataTypeDefinition.INT,
            DataTypeDefinition.MLTEXT
    };

    private int maximumNumberSuggestions = DEFAULT_MAXIMUM_NUMBER_SUGGESTIONS;

    /** Node service */
    private NodeService nodeService;

    /** Namespace service */
    private NamespaceService namespaceService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Records management admin service */
    private RecordsManagementAdminService recordsManagementAdminService;

    /** List of definitions (aspects and types) to use for substitution suggestions */
    private List<QName> suggestionDefinitions = null;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param recordsManagementAdminService Records management admin service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService recordsManagementAdminService)
    {
        this.recordsManagementAdminService = recordsManagementAdminService;
    }

    /**
     * @see org.alfresco.repo.action.parameter.ParameterProcessor#process(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String process(String value, NodeRef actionedUponNodeRef)
    {
        // the default position is to return the value un-changed
        String result = value;

        // strip the processor name from the value
        value = stripName(value);
        if (!value.isEmpty())
        {
            QName qname = QName.createQName(value, namespaceService);

            PropertyDefinition propertyDefinition = dictionaryService.getProperty(qname);
            if (propertyDefinition == null)
            {
                throw new AlfrescoRuntimeException("The property " + value + " does not have a property definition.");
            }

            QName type = propertyDefinition.getDataType().getName();
            if (ArrayUtils.contains(supportedDataTypes, type))
            {
                Serializable propertyValue = nodeService.getProperty(actionedUponNodeRef, qname);
                if (propertyValue != null)
                {
                    result = propertyValue.toString();
                }
                else
                {
                    // set the result to the empty string
                    result = "";
                }
            }
            else
            {
                throw new AlfrescoRuntimeException("The property " + value + " is of type " + type.toString() + " which is not supported by parameter substitution.");
            }
        }

        return result;
    }

    /**
     * Set the maxmimum number of suggestions returned  from the global property
     *
     * @param maximumNumberSuggestions
     */
    public void setMaximumNumberSuggestions(int maximumNumberSuggestions)
    {
        this.maximumNumberSuggestions = (maximumNumberSuggestions <= 0 ? DEFAULT_MAXIMUM_NUMBER_SUGGESTIONS: maximumNumberSuggestions);
    }

    /**
     * Add suggestion definition to the list used to get properties suggestions from.
     *
     * @param  definition  Type or aspect
     */
    public void addSuggestionDefinition(QName definition)
    {
        if(this.suggestionDefinitions == null)
        {
            this.suggestionDefinitions = Collections.synchronizedList(new ArrayList<>());
        }
        this.suggestionDefinitions.add(definition);
    }

    /**
     * Get a list of node substitution suggestions for the specified fragment.
     *
     * @param substitutionFragment  The fragment to search for
     * @returns  A list of node substitution suggestions, for example 'node.cm:title'
     *
     * @see org.alfresco.repo.action.parameter.ParameterSubstitutionSuggester#getSubstitutionSuggestions(java.lang.String)
     */
    @Override
    public List<String> getSubstitutionSuggestions(String substitutionFragment)
    {
        Set<String> suggestionSet = Collections.synchronizedSet(new HashSet<>());
        if(this.suggestionDefinitions != null)
        {
            for(QName definition : this.suggestionDefinitions)
            {
                if(getSubstitutionSuggestions(definition, substitutionFragment.toLowerCase(), suggestionSet))
                {
                    break;
                }
            }
        }
        List<String> suggestions = new ArrayList<>();
        suggestions.addAll(suggestionSet);
        Collections.sort(suggestions);
        return suggestions;
    }

    /**
     * Get a list of node substitution suggestions for the given definition and specified fragment.
     *
     * @param definitionName  Definition (aspect or type) to get properties of and the call this method for associated aspects
     * @param substitutionFragment  Substitution fragment to search for
     * @param suggestions  The current list of suggestions to which we will add newly found suggestions
     */
    private boolean getSubstitutionSuggestions(QName definitionName, String substitutionFragment, Set<String> suggestions)
    {
        boolean gotMaximumSuggestions = false;
        ClassDefinition definition = this.dictionaryService.getAspect(definitionName);
        if(definition == null)
        {
            definition = this.dictionaryService.getType(definitionName);
        }
        if(definition != null)
        {
            gotMaximumSuggestions = getSubstitutionSuggestionsForDefinition(definition, substitutionFragment, suggestions);
        }
        if(recordsManagementAdminService.isCustomisable(definitionName) && !gotMaximumSuggestions)
        {
            gotMaximumSuggestions = processPropertyDefinitions(recordsManagementAdminService.getCustomPropertyDefinitions(definitionName), substitutionFragment, suggestions);
        }
        return gotMaximumSuggestions;
    }

    /**
     * Get a list of node substitution suggestions for the given definition and specified fragment. Calls itself recursively for
     * associated aspects.
     *
     * @param definition  Definition (aspect or type) to get properties of and the call this method for associated aspects
     * @param substitutionFragment  Substitution fragment to search for
     * @param suggestions  The current list of suggestions to which we will add newly found suggestions
     */
    private boolean getSubstitutionSuggestionsForDefinition(ClassDefinition definition, String substitutionFragment, Set<String> suggestions)
    {
        boolean gotMaximumSuggestions = processPropertyDefinitions(definition.getProperties(), substitutionFragment, suggestions);
        if(!gotMaximumSuggestions)
        {
            for(QName defaultAspect : definition.getDefaultAspectNames())
            {
                gotMaximumSuggestions = getSubstitutionSuggestions(defaultAspect, substitutionFragment, suggestions);
                if(gotMaximumSuggestions)
                {
                    break;
                }
            }
        }
        return gotMaximumSuggestions;
    }

    /**
     * Process the supplied map of property definitions and add the ones that match the supplied fragment to the list of suggestions.
     *
     * @param definition  Definition (aspect or type) to get properties of and the call this method for associated aspects
     * @param substitutionFragment  Substitution fragment to search for
     * @param suggestions  The current list of suggestions to which we will add newly found suggestions
     */
    private boolean processPropertyDefinitions(Map<QName, PropertyDefinition> properties, String substitutionFragment, Set<String> suggestions)
    {
        boolean gotMaximumSuggestions = false;
        if (properties != null)
        {
            for (Map.Entry<QName, PropertyDefinition> entry : properties.entrySet())
            {
                PropertyDefinition propertyDefinition = entry.getValue();
                QName type = propertyDefinition.getDataType().getName();
                if(ArrayUtils.contains(supportedDataTypes, type))
                {
                    String suggestion = getName() + "." + entry.getKey().getPrefixString();
                    if(suggestion.toLowerCase().contains(substitutionFragment))
                    {
                        if(suggestions.size() < this.maximumNumberSuggestions)
                        {
                            suggestions.add(suggestion);
                        }
                        else
                        {
                            gotMaximumSuggestions = true;
                            break;
                        }
                    }
                }
            }
        }
        return gotMaximumSuggestions;
    }
}
