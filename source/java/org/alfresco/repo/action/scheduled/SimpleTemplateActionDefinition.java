/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.action.scheduled;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This class defines the template used to build a single action.
 * 
 * @author Andy Hind
 */
public class SimpleTemplateActionDefinition extends AbstractTemplateActionDefinition implements ApplicationContextAware
{
    /*
     * The name of the action
     */
    private String actionName;

    /*
     * The parameters used by the action
     */
    private Map<String, String> parameterTemplates;

    /*
     * The model factory to build models appropriate to the template language used to define
     * templated parameters.
     */
    private TemplateActionModelFactory templateActionModelFactory;

    /*
     * The dictionary service.
     */
    private DictionaryService dictionaryService;

    /*
     * The application context
     * (Some actions are not publicly exposed via the action service.
     * They can always be obtained via the appropriate action excecutor.) 
     */
    private ApplicationContext applicationContext;

    /**
     * Simple constructor.
     *
     */
    public SimpleTemplateActionDefinition()
    {
        super();
    }

    /**
     * Get the template model factory.
     * 
     * @return
     */
    public TemplateActionModelFactory getTemplateActionModelFactory()
    {
        return templateActionModelFactory;
    }

    /** 
     * Set the template model factory IOC.
     * 
     * @param templateActionModelFactory
     */
    public void setTemplateActionModelFactory(TemplateActionModelFactory templateActionModelFactory)
    {
        this.templateActionModelFactory = templateActionModelFactory;
    }

    /**
     * Get the dictionary service.
     * 
     * @return
     */
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    /**
     * Set the dictionary service - IOC.
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the name of the action.
     * 
     * @param actionName
     */
    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    /**
     * Get the name of the action.
     * 
     * @return
     */
    public String getActionName()
    {
        return actionName;
    }

    /**
     * Set the map of parameters used by the template.
     * These are processed via the template service to produce the actual poarameters.
     * 
     * @param parameterTemplates
     */
    public void setParameterTemplates(Map<String, String> parameterTemplates)
    {
        this.parameterTemplates = parameterTemplates;
    }


    /**
     * Get the templates that define the parameters for the action.
     * 
     * @return
     */
    public Map<String, String> getParameterTemplates()
    {
        return parameterTemplates;
    }

    /**
     * Generate the action from the template using the context node.
     */
    public Action getAction(NodeRef nodeRef)
    {
        // Get the action definition. We can not go to the service are some are not exposed.
        // So we find them from the application context.
        ActionExecuter actionExecutor = (ActionExecuter)applicationContext.getBean(getActionName());
        ActionDefinition actionDefinition =  actionExecutor.getActionDefinition();
        
     
        // Build the base action
        Action action = actionService.createAction(getActionName());

        // Go through the template definitions and set the values.
        for (String paramName : parameterTemplates.keySet())
        {
            // Transform the template
            String template = parameterTemplates.get(paramName);
            String stringValue = templateService.processTemplateString(getTemplateActionModelFactory()
                    .getTemplateEngine(), template, getTemplateActionModelFactory().getModel(nodeRef));

            // Find the data type from the action defintion
            DataTypeDefinition dataTypeDef;
            if (actionDefinition.getParameterDefintion(paramName) != null)
            {
                dataTypeDef = dictionaryService
                        .getDataType(actionDefinition.getParameterDefintion(paramName).getType());
            }
            // Fall back to the DD using the property name of it is not defined
            // This is sometimes used for setting a property to a value.
            // There can be no definition for such an ad hoc property.
            else
            {
                dataTypeDef = dictionaryService.getProperty(QName.createQName(paramName)).getDataType();
            }
            
            // Convert the template result into the correct type and set the parameter
            Object value = DefaultTypeConverter.INSTANCE.convert(dataTypeDef, stringValue);
            if (value instanceof Serializable)
            {
                action.setParameterValue(paramName, (Serializable) value);
            }

        }

        // If there is a compensating action then set it.
        if (getCompensatingTemplateCompositeActionDefinition() != null)
        {
            action.setCompensatingAction(getCompensatingTemplateCompositeActionDefinition().getAction(nodeRef));
        }

        return action;
    }

    /**
     * ApplciationContextAware - get the application context.
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
        
    }
}
