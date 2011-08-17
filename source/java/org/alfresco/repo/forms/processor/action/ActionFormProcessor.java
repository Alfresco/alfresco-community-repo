/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.FilteredFormProcessor;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.repo.forms.processor.FormProcessor;
import org.alfresco.repo.forms.processor.node.FormFieldConstants;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * This class is a {@link FormProcessor} for {@link Action actions}.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class ActionFormProcessor extends FilteredFormProcessor<ActionDefinition, ActionFormResult>
{
    public static final String ITEM_KIND = "action";
    
    protected static final String EXECUTE_ASYNCHRONOUSLY = "executeAsynchronously";
    
    private static Log logger = LogFactory.getLog(ActionFormProcessor.class);
    private ActionService actionService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /*
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getLogger()
     */
    @Override
    protected Log getLogger()
    {
        return logger;
    }
    
    /*
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getTypedItem(org.alfresco.repo.forms.Item)
     */
    @Override
    protected ActionDefinition getTypedItem(Item item)
    {
        final String actionDefId = item.getId();

        ActionDefinition actionDef = null;
        try
        {
            actionDef = actionService.getActionDefinition(actionDefId);
        }
        catch (NoSuchBeanDefinitionException nsbdx)
        {
            // Intentionally empty.
        }
        
        if (actionDef == null)
        {
            throw new FormNotFoundException(item);
        }
        
        return actionDef;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemType(java.lang.Object)
     */
    @Override
    protected String getItemType(ActionDefinition item)
    {
        return item.getName();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemURI(java.lang.Object)
     */
    @Override
    protected String getItemURI(ActionDefinition item)
    {
        return "";
    }
    
    /*
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#internalPersist(java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected ActionFormResult internalPersist(ActionDefinition item, FormData data)
    {
        if (logger.isDebugEnabled()) 
            logger.debug("Persisting form for: " + item);
        
        final Action actionToExecute = createActionAndParams(item, data);
        final NodeRef actionedUponNodeRef = getActionedUponNodeRef(item, data);
        final boolean isAsync = isAsynchronousActionRequest(item, data);
        
        // execute the action
        actionService.executeAction(actionToExecute, actionedUponNodeRef, true, isAsync);
        
        // extract the result
        Object result = actionToExecute.getParameterValue(ActionExecuter.PARAM_RESULT);
        
        return new ActionFormResult(actionToExecute, result);
    }
    
    /**
     * This method creates the {@link Action} based on the submitted form data.
     * It then sets the action parameters based on the form data also.
     */
    private Action createActionAndParams(ActionDefinition item, FormData data)
    {
        // First create the action.
        Action action = null;
        try
        {
            action = actionService.createAction(item.getName());
        }
        catch (NoSuchBeanDefinitionException nsbdx)
        {
            throw new FormException("Unrecognised action name " + item.getName(), nsbdx);
        }
        
        Map<String, Serializable> actionParameters = new HashMap<String, Serializable>();
        
        List<ParameterDefinition> actionParamDefs = item.getParameterDefinitions();
        for (ParameterDefinition actionParamDef : actionParamDefs)
        {
            String paramDefinitionName = actionParamDef.getName();
            
            // We need to find the form field data for this action parameter definition name.
            // However the form service may have added "prop_", "assoc_", "_added" and "_removed"
            // prefixes/suffixes, which we are not interested in.
            FieldData fieldData = data.getFieldData(FormFieldConstants.PROP_DATA_PREFIX +
                                                    paramDefinitionName);
            if (fieldData == null)
            {
                fieldData = data.getFieldData(FormFieldConstants.ASSOC_DATA_PREFIX +
                                              paramDefinitionName +
                                              FormFieldConstants.ASSOC_DATA_ADDED_SUFFIX);
            }
            
            if (fieldData != null)
            {
                Object fieldValueObj = fieldData.getValue();
                
                QName expectedParamType = actionParamDef.getType();
                DataTypeDefinition typeDef = dictionaryService.getDataType(expectedParamType);
                
                // In order to allow short-form QNames, which are not handled by the default type converter,
                // we'll do QName conversion ourselves.
                Object convertedObj = null;
                if (DataTypeDefinition.QNAME.equals(expectedParamType))
                {
                    if (((String)fieldValueObj).charAt(0) == QName.NAMESPACE_BEGIN)
                    {
                        convertedObj = QName.createQName((String) fieldValueObj);
                    }
                    else
                    {
                        convertedObj = QName.createQName((String)fieldValueObj, namespaceService);
                    }
                }
                else
                {
                    convertedObj = DefaultTypeConverter.INSTANCE.convert(typeDef, fieldValueObj);
                }
                actionParameters.put(paramDefinitionName, (Serializable) convertedObj);
            }
        }
        
        // Now set the action parameter values on the action.
        action.setParameterValues(actionParameters);
        
        return action;
    }
    
    /**
     * This method returns the actionedUponNodeRef based on the submitted form data.
     */
    private NodeRef getActionedUponNodeRef(ActionDefinition item, FormData data)
    {
        FieldData actionedUponNodeRef = data.getFieldData(DESTINATION);
        
        NodeRef result = null;
        
        // We'll allow a null actionedUponNodeRef as the actionService allows it.
        // However most action-executers require an actionedUponNodeRef and so most
        // executer implementations will throw an exception on execution.
        if (actionedUponNodeRef != null)
        {
            String nodeRefString = (String)actionedUponNodeRef.getValue();
            if (NodeRef.isNodeRef(nodeRefString))
            {
                result = new NodeRef(nodeRefString);
            }
            else
            {
                // We will disallow a malformed actionedUponNodeRef string
                throw new FormException("Illegal actionedUponNodeRef: " + nodeRefString);
            }
        }
        
        return result;
    }
    
    /**
     * This method works out whether the submitted action should be executed asynchronously.
     */
    private boolean isAsynchronousActionRequest(ActionDefinition item, FormData data)
    {
        FieldData executeAsynchronously = data.getFieldData(EXECUTE_ASYNCHRONOUSLY);
        
        boolean result = false;
        if (executeAsynchronously != null)
        {
            result = Boolean.valueOf((String)executeAsynchronously.getValue());
        }
        
        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#generateDefaultFields(org.alfresco.repo.forms.processor.FormCreationData, java.util.List)
     */
    protected List<Field> generateDefaultFields(FormCreationData data, List<String> fieldsToIgnore)
    {
        List<Field> fields = new ArrayList<Field>();
        
        ActionItemData itemData = (ActionItemData)data.getItemData();
        
        // generate a field for each action parameter
        for (ParameterDefinition paramDef : itemData.getActionDefinition().getParameterDefinitions())
        {
            if (!fieldsToIgnore.contains(paramDef.getName()))
            {
                ActionParameterField actionParameterField = new ActionParameterField(paramDef, actionService);
                fields.add(actionParameterField);
            }
        }
        
        // And also generate the special case of "execute asynchronously", which is not defined
        // as an ActionParameter within the ActionExecuter, but is instead available for all actions.
        fields.add(new ActionNonParameterField(EXECUTE_ASYNCHRONOUSLY, DataTypeDefinition.BOOLEAN));
        
        return fields;
    }
    
    @Override
    protected List<Field> generateSelectedFields(List<String> requestedFields, FormCreationData data)
    {
        List<String> fieldsToIgnore = Collections.emptyList();
        List<Field> fields = this.generateDefaultFields(data, fieldsToIgnore);
        
        List<Field> results = new ArrayList<Field>();
        
        for (Field f : fields)
        {
            if (requestedFields.contains(f.getFieldName()))
            {
                results.add(f);
            }
        }
        
        return results;
    }

    @Override
    protected List<String> getDefaultIgnoredFields()
    {
        return Collections.emptyList();
    }

    @Override
    protected Object makeItemData(ActionDefinition item)
    {
        ActionItemData actionItemData = null;
        try
        {
            Action action = actionService.createAction(item.getName());
            actionItemData = new ActionItemData(item, action);
        }
        catch (NoSuchBeanDefinitionException nsbdx)
        {
            throw new FormException("Failed to create action '" + item.getName() + "'", nsbdx);
        }
        
        return actionItemData;
    }
    
    /**
     * DTO containing the ActionDefinition and Action representation of the item being requested.
     */
    class ActionItemData
    {
        private ActionDefinition actionDefinition;
        private Action action;
        
        public ActionItemData(ActionDefinition actionDef, Action action)
        {
            this.actionDefinition = actionDef;
            this.action = action;
        }
        
        public ActionDefinition getActionDefinition()
        {
            return this.actionDefinition;
        }
        
        public Action getAction()
        {
            return this.action;
        }
    }

}