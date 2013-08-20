package org.alfresco.rest.workflow.api.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.ProcessEngine;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.model.FormModelElement;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;

/**
 * Base class for rest-implementations related to workflow. Contains utility-methods that
 * can be used, regardless of the type of resources the implementing class can handle.
 *
 * @author Frederik Heremans
 */
public class WorkflowRestImpl
{
    protected TenantService tenantService;
    protected AuthorityService authorityService;
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;
    protected ProcessEngine activitiProcessEngine;
    protected boolean deployWorkflowsInTenant;
    
    static 
    {
        // Register a custom date-converter to cope with ISO8601-parameters
        ISO8601Converter dateConverter = new ISO8601Converter();
        ConvertUtils.register(dateConverter, Date.class);
        ConvertUtils.register(dateConverter, Calendar.class);
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setActivitiProcessEngine(ProcessEngine activitiProcessEngine)
    {
        this.activitiProcessEngine = activitiProcessEngine;
    }
    
    public void setDeployWorkflowsInTenant(boolean deployWorkflowsInTenant)
    {
        this.deployWorkflowsInTenant = deployWorkflowsInTenant;
    }
    
    /**
     * Get the first parameter value, converted to the requested type.
     * @param parameters used to extract parameter value from
     * @param parameterName name of the parameter
     * @param returnType type of object to return
     * @return the converted parameter value. Null, if the parameter has no value.
     * @throws IllegalArgumentException when no conversion for the given returnType is available or if returnType is null.
     * @throws InvalidArgumentException when conversion to the given type was not possible
     */
    @SuppressWarnings("unchecked")
    public <T extends Object> T getParameter(Parameters parameters, String parameterName, Class<T> returnType) {
        if(returnType == null) 
        {
            throw new IllegalArgumentException("ReturnType cannot be null");
        }
        try
        {
            Object result = null;
            String stringValue = parameters.getParameter(parameterName);
            if(stringValue != null) 
            {
                result = ConvertUtils.convert(stringValue, returnType);
                if(result instanceof String)
                {
                    // If a string is returned, no converter has been found
                    throw new IllegalArgumentException("Unable to convert parameter to type: " + returnType.getName());
                }
            }
            return (T) result;
        }
        catch(ConversionException ce)
        {
            // Conversion failed, wrap in Illegal
            throw new InvalidArgumentException("Parameter value for '" + parameterName + "' should be a valid " + returnType.getSimpleName());
        }
    }

    /**
     * @param type the type to get the elements for
     * @param paging 
     * @return collection with all valid form-model elements for the given type.
     */
    public CollectionWithPagingInfo<FormModelElement> getFormModelElements(TypeDefinition type, Paging paging)
    {
        Map<QName, PropertyDefinition> taskProperties = type.getProperties();
        Set<QName> typesToExclude = getTypesToExclude(type);
        
        List<FormModelElement> page = new ArrayList<FormModelElement>();
        for (Entry<QName, PropertyDefinition> entry : taskProperties.entrySet())
        {
            // Only add properties which are not part of an excluded type
            if(!typesToExclude.contains(entry.getValue().getContainerClass().getName()))
            {
                FormModelElement element = new FormModelElement();
                element.setName(entry.getKey().toPrefixString(namespaceService).replace(':', '_'));
                element.setQualifiedName(entry.getKey().toString());
                element.setTitle(entry.getValue().getTitle(dictionaryService));
                element.setRequired(entry.getValue().isMandatory());
                element.setDataType(entry.getValue().getDataType().getName().toPrefixString(namespaceService));
                element.setDefaultValue(entry.getValue().getDefaultValue());
                page.add(element);
            }
        }
        
        Map<QName, AssociationDefinition> taskAssociations = type.getAssociations();
        for (Entry<QName, AssociationDefinition> entry : taskAssociations.entrySet())
        {
            // Only add associations which are not part of an excluded type
            if(!typesToExclude.contains(entry.getValue().getSourceClass().getName()))
            {
                FormModelElement element = new FormModelElement();
                element.setName(entry.getKey().toPrefixString(namespaceService).replace(':', '_'));
                element.setQualifiedName(entry.getKey().toString());
                element.setTitle(entry.getValue().getTitle(dictionaryService));
                element.setRequired(entry.getValue().isTargetMandatory());
                element.setDataType(entry.getValue().getTargetClass().getName().toPrefixString(namespaceService));
                page.add(element);
            }
        }
        
        return CollectionWithPagingInfo.asPaged(paging, page, false, page.size());
    }
    
    /**
     * @param taskType type of the task
     * @return all types (and aspects) which properties should not be used for form-model elements
     */
    protected Set<QName> getTypesToExclude(TypeDefinition taskType)
    {
        HashSet<QName> typesToExclude = new HashSet<QName>();
        
        ClassDefinition parentClassDefinition = taskType.getParentClassDefinition();
        boolean contentClassFound = false;
        while(parentClassDefinition != null) {
            if(contentClassFound)
            {
                typesToExclude.add(parentClassDefinition.getName());
            }
            else if(ContentModel.TYPE_CONTENT.equals(parentClassDefinition.getName()))
            {
                // All parents of "cm:content" should be ignored as well for fetching start-properties 
                typesToExclude.add(ContentModel.TYPE_CONTENT);
                typesToExclude.addAll(parentClassDefinition.getDefaultAspectNames());
                contentClassFound = true;
            }
            parentClassDefinition = parentClassDefinition.getParentClassDefinition();
        }
        return typesToExclude;
    }
}
