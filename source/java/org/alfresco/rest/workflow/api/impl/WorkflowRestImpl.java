package org.alfresco.rest.workflow.api.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiWorkflowEngine;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.model.FormModelElement;
import org.alfresco.rest.workflow.api.model.Item;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
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
    protected static final String BPM_PACKAGE = "bpm_package";
    
    protected TenantService tenantService;
    protected AuthorityService authorityService;
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;
    protected NodeService nodeService;
    protected ProcessEngine activitiProcessEngine;
    protected boolean deployWorkflowsInTenant;
    protected List<String> excludeModelTypes = new ArrayList<String>(Arrays.asList("bpm_priority", "bpm_description", "bpm_dueDate"));
    private ActivitiWorkflowEngine activitiWorkflowEngine;
    
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
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
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
     * Create NodeRef from item id String
     */
    public NodeRef getNodeRef(String itemId)
    {
        NodeRef nodeRef = null;
        if (NodeRef.isNodeRef(itemId) == false)
        {
            nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, itemId);
        }
        else
        {
            nodeRef = new NodeRef(itemId);
        }
        return nodeRef;
    }
    
    /**
     * Get all items from the process package variable
     */
    public CollectionWithPagingInfo<Item> getItemsFromProcess(String processId, Paging paging)
    {
        ActivitiScriptNode packageScriptNode = null;
        try 
        {
            HistoricVariableInstance variableInstance = activitiProcessEngine.getHistoryService()
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processId)
                    .variableName(BPM_PACKAGE)
                    .singleResult();
            
            if (variableInstance != null)
            {
                packageScriptNode = (ActivitiScriptNode) variableInstance.getValue();
            }
            else
            {
                throw new EntityNotFoundException(processId);
            }
        } 
        catch (ActivitiObjectNotFoundException e)
        {
            throw new EntityNotFoundException(processId);
        }
        
        List<Item> page = new ArrayList<Item>();
        if (packageScriptNode != null)
        {
            List<ChildAssociationRef> documentList = nodeService.getChildAssocs(packageScriptNode.getNodeRef());
            for (ChildAssociationRef childAssociationRef : documentList)
            {
                Item item = createItemForNodeRef(childAssociationRef.getChildRef());
                page.add(item);
            }
        }
        
        return CollectionWithPagingInfo.asPaged(paging, page, false, page.size());
    }
    
    /**
     * Get an item from the process package variable
     */
    public Item getItemFromProcess(String itemId, String processId)
    {
        NodeRef nodeRef = getNodeRef(itemId);
        ActivitiScriptNode packageScriptNode = null;
        try 
        {
            HistoricVariableInstance variableInstance = activitiProcessEngine.getHistoryService()
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processId)
                    .variableName(BPM_PACKAGE)
                    .singleResult();
            
            if (variableInstance != null)
            {
                packageScriptNode = (ActivitiScriptNode) variableInstance.getValue();
            }
            else
            {
                throw new EntityNotFoundException(processId);
            }
        } 
        catch (ActivitiObjectNotFoundException e)
        {
            throw new EntityNotFoundException(processId);
        }
        
        Item item = null;
        if (packageScriptNode != null)
        {
            List<ChildAssociationRef> documentList = nodeService.getChildAssocs(packageScriptNode.getNodeRef());
            for (ChildAssociationRef childAssociationRef : documentList)
            {
                if (childAssociationRef.getChildRef().equals(nodeRef)) 
                {
                    item = createItemForNodeRef(childAssociationRef.getChildRef());
                    break;
                }
            }
        }
        
        if (item == null) {
            throw new EntityNotFoundException(itemId);
        }
        
        return item;
    }
    
    /**
     *  Create a new item in the process package variable
     */
    public Item createItemInProcess(String itemId, String processId)
    {
        NodeRef nodeRef = getNodeRef(itemId);
        
        ActivitiScriptNode packageScriptNode = null;
        try 
        {
            packageScriptNode = (ActivitiScriptNode) activitiProcessEngine.getRuntimeService().getVariable(processId, BPM_PACKAGE);
        } 
        catch (ActivitiObjectNotFoundException e)
        {
            throw new EntityNotFoundException(processId);
        }
        
        if (packageScriptNode == null)
        {
            throw new InvalidArgumentException("process doesn't contain a workflow package variable");
        }
        
        // check if noderef exists
        try 
        {
            nodeService.getProperties(nodeRef);
        }
        catch (Exception e)
        {
            throw new EntityNotFoundException("item with id " + nodeRef.toString() + " not found");
        }
        
        try 
        {
            QName workflowPackageItemId = QName.createQName("wpi", nodeRef.toString());
            nodeService.addChild(packageScriptNode.getNodeRef(), nodeRef, 
                    WorkflowModel.ASSOC_PACKAGE_CONTAINS, workflowPackageItemId);
        }
        catch (Exception e)
        {
            throw new ApiException("could not add item to process " + e.getMessage(), e);
        }
        
        Item responseItem = createItemForNodeRef(nodeRef);
        activitiWorkflowEngine.dispatchPackageUpdatedEvent(packageScriptNode, null, null, processId, null);
        return responseItem;
    }
    
    /**
     *  Delete an item from the process package variable
     */
    public void deleteItemFromProcess(String itemId, String processId)
    {
        NodeRef nodeRef = getNodeRef(itemId);
        ActivitiScriptNode packageScriptNode = null;
        try 
        {
            packageScriptNode = (ActivitiScriptNode) activitiProcessEngine.getRuntimeService().getVariable(processId, BPM_PACKAGE);
        } 
        catch (ActivitiObjectNotFoundException e)
        {
            throw new EntityNotFoundException(processId);
        }
        
        if (packageScriptNode == null)
        {
            throw new InvalidArgumentException("process doesn't contain a workflow package variable");
        }
        
        boolean itemIdFoundInPackage = false;
        List<ChildAssociationRef> documentList = nodeService.getChildAssocs(packageScriptNode.getNodeRef());
        for (ChildAssociationRef childAssociationRef : documentList)
        {
            if (childAssociationRef.getChildRef().equals(nodeRef)) 
            {
                itemIdFoundInPackage = true;
                break;
            }
        }
        
        if (itemIdFoundInPackage == false)
        {
            throw new EntityNotFoundException("Item " + itemId + " not found in the process package variable");
        }
        
        try 
        {
            nodeService.removeChild(packageScriptNode.getNodeRef(), nodeRef);
            activitiWorkflowEngine.dispatchPackageUpdatedEvent(packageScriptNode, null, null, processId, null);
        }
        catch (InvalidNodeRefException e)
        {
            throw new EntityNotFoundException("Item " + itemId + " not found");
        }
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
        	String name = entry.getKey().toPrefixString(namespaceService).replace(':', '_');
            
            // Only add properties which are not part of an excluded type
            if(!typesToExclude.contains(entry.getValue().getContainerClass().getName()) && excludeModelTypes.contains(name) == false)
            {
                FormModelElement element = new FormModelElement();
                element.setName(name);
                element.setQualifiedName(entry.getKey().toString());
                element.setTitle(entry.getValue().getTitle(dictionaryService));
                element.setRequired(entry.getValue().isMandatory());
                element.setDataType(entry.getValue().getDataType().getName().toPrefixString(namespaceService));
                element.setDefaultValue(entry.getValue().getDefaultValue());
                if (entry.getValue().getConstraints() != null)
                {
                    for (ConstraintDefinition constraintDef : entry.getValue().getConstraints())
                    {
                    	Constraint constraint = constraintDef.getConstraint();
                    	if (constraint != null && constraint instanceof ListOfValuesConstraint)
                    	{
                    		ListOfValuesConstraint valuesConstraint = (ListOfValuesConstraint) constraint;
                    		if (valuesConstraint.getAllowedValues() != null && valuesConstraint.getAllowedValues().size() > 0)
                    		{
                    			element.setAllowedValues(valuesConstraint.getAllowedValues());
                    		}
                    	}
                    }
                }
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
        while(parentClassDefinition != null) 
        {
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
    
    /**
     * Validates if the logged in user is allowed to get information about a specific process instance.
     * If the user is not allowed an exception is thrown.
     * 
     * @param processId identifier of the process instance
     */
    protected List<HistoricVariableInstance> validateIfUserAllowedToWorkWithProcess(String processId)
    {
        List<HistoricVariableInstance> variableInstances = activitiProcessEngine.getHistoryService()
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processId)
                .list();
        
        Map<String, Object> variableMap = new HashMap<String, Object>();
        if (variableInstances != null && variableInstances.size() > 0) 
        {
            for (HistoricVariableInstance variableInstance : variableInstances)
            {
                variableMap.put(variableInstance.getVariableName(), variableInstance.getValue());
            }
        }
        else
        {
            throw new EntityNotFoundException(processId);
        }
        
        if (tenantService.isEnabled())
        {
            String tenantDomain = (String) variableMap.get(ActivitiConstants.VAR_TENANT_DOMAIN);
            if (TenantUtil.getCurrentDomain().equals(tenantDomain) == false)
            {
                throw new PermissionDeniedException("Process is running in another tenant");
            }
        }
        
        ActivitiScriptNode initiator = (ActivitiScriptNode) variableMap.get(WorkflowConstants.PROP_INITIATOR);
        if (initiator != null && AuthenticationUtil.getRunAsUser().equals(initiator.getNodeRef().getId()))
        {
            // user is allowed
            return variableInstances;
        }
        
        if (authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser())) 
        {
            // Admin is allowed to read all processes in the current tenant
            return variableInstances;
        }
        else
        {
            // If non-admin user, involvement in the task is required (either owner, assignee or externally involved).
            HistoricTaskInstanceQuery query = activitiProcessEngine.getHistoryService()
                    .createHistoricTaskInstanceQuery()
                    .processInstanceId(processId)
                    .taskInvolvedUser(AuthenticationUtil.getRunAsUser());
            
            List<HistoricTaskInstance> taskList = query.list();
            
            if (org.apache.commons.collections.CollectionUtils.isEmpty(taskList)) 
            {
                throw new PermissionDeniedException("user is not allowed to access information about process " + processId);
            }
        }
        
        return variableInstances;
    }
    
    protected Item createItemForNodeRef(NodeRef nodeRef) {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        Item item = new Item();
        String name = (String) properties.get(ContentModel.PROP_NAME);
        String title = (String) properties.get(ContentModel.PROP_TITLE);
        String description = (String) properties.get(ContentModel.PROP_DESCRIPTION);
        Date createdAt = (Date) properties.get(ContentModel.PROP_CREATED);
        String createdBy = (String) properties.get(ContentModel.PROP_CREATOR);
        Date modifiedAt = (Date) properties.get(ContentModel.PROP_MODIFIED);
        String modifiedBy = (String) properties.get(ContentModel.PROP_MODIFIER);
        
        ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        
        item.setId(nodeRef.getId());
        item.setName(name);
        item.setTitle(title);
        item.setDescription(description);
        item.setCreatedAt(createdAt);
        item.setCreatedBy(createdBy);
        item.setModifiedAt(modifiedAt);
        item.setModifiedBy(modifiedBy);
        if (contentData != null) 
        {
            item.setMimeType(contentData.getMimetype());
            item.setSize(contentData.getSize());
        }
        return item;
    }

    public void setActivitiWorkflowEngine(ActivitiWorkflowEngine activitiWorkflowEngine)
    {
        this.activitiWorkflowEngine = activitiWorkflowEngine;
    }
}
