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
package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.forms.processor.node.PeriodDataTypeParameters;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

/**
 * Temporary FormProcessor implementation that can generate and persist 
 * Form objects for workflow definitions.
 *
 * @author Gavin Cornwell
 */
public class WorkflowFormProcessor extends ContentModelFormProcessor<WorkflowDefinition, WorkflowInstance>
{
    /** Logger */
    private static Log logger = LogFactory.getLog(WorkflowFormProcessor.class);
    
    public static final String TRANSIENT_PACKAGE_ITEMS = "packageItems";
    
    /** workflow service */
    protected WorkflowService workflowService;
    
    protected NodeService unprotectedNodeService;
    
    /**
     * Sets the workflow service
     * 
     * @param workflowService The WorkflowService instance
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    public void setSmallNodeService(NodeService nodeService)
    {
        this.unprotectedNodeService = nodeService;
    }
    
    /*
     * @see
     * org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getLogger
     * ()
     */
    @Override
    protected Log getLogger()
    {
        return logger;
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.node.NodeFormProcessor#getTypedItem
     * (org.alfresco.repo.forms.Item)
     */
    @Override
    protected WorkflowDefinition getTypedItem(Item item)
    {
        WorkflowDefinition workflowDef = null;
        
        try
        {
            // item id could be the raw workflow definition name or it could be
            // in a URL friendly format
            String workflowDefName = item.getId();
            if (workflowDefName.indexOf("$") == -1)
            {
                // decode the itemId
                workflowDefName = workflowDefName.replace("jbpm_", "jbpm$");
                workflowDefName = workflowDefName.replace('_', ':');
            }
            
            // Extract the workflow definition
            workflowDef = this.workflowService.getDefinitionByName(workflowDefName);

            if (workflowDef == null) 
            { 
                throw new FormNotFoundException(item, 
                            new IllegalArgumentException("Workflow definition does not exist: " + item.getId()));
            }
        }
        catch (WorkflowException we)
        {
            throw new FormNotFoundException(item, we);
        }
        
        // return the type definition object for the requested type
        return workflowDef;
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#internalGenerate
     * (java.lang.Object, java.util.List, java.util.List,
     * org.alfresco.repo.forms.Form, java.util.Map)
     */
    @Override
    protected void internalGenerate(WorkflowDefinition workflowDef, List<String> fields,
                List<String> forcedFields, Form form, Map<String, Object> context)
    {
        if (logger.isDebugEnabled()) logger.debug("Generating form for item: " + workflowDef);

        // generate the form for the workflow definition
        form.getItem().setType(workflowDef.name);
        form.getItem().setUrl("/api/workflow-definition/" + workflowDef.id);

        // get the type of the start task for the workflow definition
        TypeDefinition typeDef = workflowDef.getStartTaskDefinition().metadata;
        
        if (fields != null && fields.size() > 0)
        {
            generateSelectedFields(null, typeDef, fields, forcedFields, form);
        }
        else
        {
            // setup field definitions and data
            generateAllPropertyFields(typeDef, form);
            generateAllAssociationFields(typeDef, form);
        }
        
        // add the "packageItems" transient field
        generatePackageItemsTransientField(form);
        
        if (logger.isDebugEnabled()) logger.debug("Generating form: " + form);
    }

    /**
     * Generates the 'packageItems' field
     * 
     * @param form The Form instance to populate
     */
    protected void generatePackageItemsTransientField(Form form)
    {
        // setup basic field info
        AssociationFieldDefinition fieldDef = new AssociationFieldDefinition(TRANSIENT_PACKAGE_ITEMS, 
                    "cm:content", Direction.TARGET);
        fieldDef.setLabel("Items");
        fieldDef.setDescription("Items that are part of the workflow");
        fieldDef.setProtectedField(false);
        fieldDef.setEndpointMandatory(false);
        fieldDef.setEndpointMany(true);

        // define the data key name and set
        fieldDef.setDataKeyName(ASSOC_DATA_PREFIX + TRANSIENT_PACKAGE_ITEMS);

        // add definition to the form
        form.addFieldDefinition(fieldDef);
    }
    
    /**
     * Sets up the field definitions for all the type's properties.
     * 
     * @param typeDef The type being setup
     * @param form The Form instance to populate
     */
    protected void generateAllPropertyFields(TypeDefinition typeDef, Form form)
    {
        // iterate round the property defintions and setup field definition
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        for (PropertyDefinition propDef : propDefs.values())
        {
            generatePropertyField(propDef, form, this.namespaceService);
        }

        // get all default aspects for the type and iterate round their
        // property definitions too
        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            propDefs = aspect.getProperties();
            for (PropertyDefinition propDef : propDefs.values())
            {
                generatePropertyField(propDef, form, this.namespaceService);
            }
        }
    }

    /**
     * Sets up the field definitions for all the type's associations.
     * 
     * @param typeDef The type being setup
     * @param form The Form instance to populate
     */
    protected void generateAllAssociationFields(TypeDefinition typeDef, Form form)
    {
        // iterate round the association defintions and setup field definition
        Map<QName, AssociationDefinition> assocDefs = typeDef.getAssociations();
        for (AssociationDefinition assocDef : assocDefs.values())
        {
            generateAssociationField(assocDef, form, this.namespaceService);
        }

        // get all default aspects for the type and iterate round their
        // association definitions too
        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            assocDefs = aspect.getAssociations();
            for (AssociationDefinition assocDef : assocDefs.values())
            {
                generateAssociationField(assocDef, form, this.namespaceService);
            }
        }
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.node.NodeFormProcessor#internalPersist
     * (java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected WorkflowInstance internalPersist(WorkflowDefinition workflowDef, final FormData data)
    {
        if (logger.isDebugEnabled()) logger.debug("Persisting form for: " + workflowDef);

        WorkflowInstance workflow = null;
        Map<QName, Serializable> params = new HashMap<QName, Serializable>(8);

        // create a package for the workflow
        NodeRef workflowPackage = this.workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
        
        // TODO: iterate through form data to collect properties, for now
        //       just hardcode the ones we know
        params.put(WorkflowModel.PROP_DESCRIPTION, 
                    (Serializable)data.getFieldData("prop_bpm_workflowDescription").getValue());
        
        NodeRef assignee = new NodeRef(data.getFieldData("assoc_bpm_assignee_added").getValue().toString());
        ArrayList<NodeRef> assigneeList = new ArrayList<NodeRef>(1);
        assigneeList.add(assignee);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assigneeList);
        
        // add any package items
        Object items = data.getFieldData("assoc_packageItems_added").getValue();
        if (items != null)
        {
            String[] nodeRefs = StringUtils.tokenizeToStringArray(items.toString(), ",");
            for (int x = 0; x < nodeRefs.length; x++)
            {
                NodeRef item = new NodeRef(nodeRefs[x]);
                this.unprotectedNodeService.addChild(workflowPackage, item, 
                            WorkflowModel.ASSOC_PACKAGE_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                            QName.createValidLocalName((String)this.nodeService.getProperty(
                                  item, ContentModel.PROP_NAME))));
            }
        }
        
        // TODO: add any context (this could re-use alf_destination)
        
        // start the workflow to get access to the start task
        WorkflowPath path = this.workflowService.startWorkflow(workflowDef.getId(), params);
        if (path != null)
        {
            // get hold of the workflow instance for returning
            workflow = path.instance;
            
            // extract the start task
            List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
                WorkflowTask startTask = tasks.get(0);
              
                if (logger.isDebugEnabled())
                    logger.debug("Found start task:" + startTask);
              
                if (startTask.state == WorkflowTaskState.IN_PROGRESS)
                {
                    // end the start task to trigger the first 'proper'
                    // task in the workflow
                    this.workflowService.endTask(startTask.id, null);
                }
            }
        
            if (logger.isDebugEnabled())
                logger.debug("Started workflow: " + workflowDef.getId());
        }
        
        // return the workflow just started
        return workflow;
    }
    
    /**
     * Sets up a field definition for the given property.
     * <p>
     * NOTE: This method is static so that it can serve as a helper method for
     * FormFilter implementations as adding additional property fields is likely
     * to be a common extension.
     * </p>
     * 
     * @param propDef The PropertyDefinition of the field to generate
     * @param form The Form instance to populate
     * @param namespaceService NamespaceService instance
     */
    protected void generatePropertyField(PropertyDefinition propDef, Form form, NamespaceService namespaceService)
    {
        generatePropertyField(propDef, form, null, null, namespaceService);
    }

    /**
     * Sets up a field definition for the given property.
     * <p>
     * NOTE: This method is static so that it can serve as a helper method for
     * FormFilter implementations as adding additional property fields is likely
     * to be a common extension.
     * </p>
     * 
     * @param propDef The PropertyDefinition of the field to generate
     * @param form The Form instance to populate
     * @param propValue The value of the property field
     * @param namespaceService NamespaceService instance
     */
    protected void generatePropertyField(PropertyDefinition propDef, Form form, Serializable propValue,
                NamespaceService namespaceService)
    {
        generatePropertyField(propDef, form, propValue, null, namespaceService);
    }
    
    /**
     * Sets up a field definition for the given property.
     * <p>
     * NOTE: This method is static so that it can serve as a helper method for
     * FormFilter implementations as adding additional property fields is likely
     * to be a common extension.
     * </p>
     * 
     * @param propDef The PropertyDefinition of the field to generate
     * @param form The Form instance to populate
     * @param propValue The value of the property field
     * @param group The FieldGroup the property field belongs to, can be null
     * @param namespaceService NamespaceService instance
     */
    @SuppressWarnings("unchecked")
    protected void generatePropertyField(PropertyDefinition propDef, Form form, Serializable propValue,
                FieldGroup group, NamespaceService namespaceService)
    {
        String propName = propDef.getName().toPrefixString(namespaceService);
        String[] nameParts = QName.splitPrefixedQName(propName);
        PropertyFieldDefinition fieldDef = new PropertyFieldDefinition(propName, propDef.getDataType().getName()
                    .getLocalName());

        String title = propDef.getTitle();
        if (title == null)
        {
            title = propName;
        }
        fieldDef.setLabel(title);
        fieldDef.setDefaultValue(propDef.getDefaultValue());
        fieldDef.setDescription(propDef.getDescription());
        fieldDef.setMandatory(propDef.isMandatory());
        fieldDef.setProtectedField(propDef.isProtected());
        fieldDef.setRepeating(propDef.isMultiValued());
        fieldDef.setGroup(group);

        // any property from the system model (sys prefix) should be protected
        // the model doesn't currently enforce this so make sure they are not
        // editable
        if (NamespaceService.SYSTEM_MODEL_1_0_URI.equals(propDef.getName().getNamespaceURI()))
        {
            fieldDef.setProtectedField(true);
        }

        // define the data key name and set
        String dataKeyName = PROP_DATA_PREFIX + nameParts[0] + DATA_KEY_SEPARATOR + nameParts[1];
        fieldDef.setDataKeyName(dataKeyName);

        // setup any parameters requried for the data type
        if (propDef.getDataType().getName().equals(DataTypeDefinition.PERIOD))
        {
            // if the property data type is d:period we need to setup a data
            // type parameters object to represent the options and rules
            PeriodDataTypeParameters periodOptions = new PeriodDataTypeParameters();
            Set<String> providers = Period.getProviderNames();
            for (String provider : providers)
            {
                periodOptions.addPeriodProvider(Period.getProvider(provider));
            }

            fieldDef.setDataTypeParameters(periodOptions);
        }

        // setup constraints for the property
        List<ConstraintDefinition> constraints = propDef.getConstraints();
        if (constraints != null && constraints.size() > 0)
        {
            List<FieldConstraint> fieldConstraints = new ArrayList<FieldConstraint>(constraints.size());

            for (ConstraintDefinition constraintDef : constraints)
            {
                Constraint constraint = constraintDef.getConstraint();
                FieldConstraint fieldConstraint = new FieldConstraint(constraint.getType(), constraint.getParameters());
                fieldConstraints.add(fieldConstraint);
            }

            fieldDef.setConstraints(fieldConstraints);
        }

        form.addFieldDefinition(fieldDef);

        // add the property value to the form
        if (propValue != null)
        {
            if (propValue instanceof List)
            {
                // temporarily add repeating field data as a comma
                // separated list, this will be changed to using
                // a separate field for each value once we have full
                // UI support in place.
                propValue = StringUtils.collectionToCommaDelimitedString((List) propValue);
            }
            else if (propValue instanceof ContentData)
            {
                // for content properties retrieve the info URL rather than the
                // the object value itself
                propValue = ((ContentData)propValue).getInfoUrl();
            }

            form.addData(dataKeyName, propValue);
        }
    }

    /**
     * Sets up a field definition for the given association.
     * <p>
     * NOTE: This method is static so that it can serve as a helper method for
     * FormFilter implementations as adding additional association fields is
     * likely to be a common extension.
     * </p>
     * 
     * @param assocDef The AssociationDefinition of the field to generate
     * @param form The Form instance to populate
     * @param namespaceService NamespaceService instance
     */
    protected void generateAssociationField(AssociationDefinition assocDef, Form form,
                NamespaceService namespaceService)
    {
        generateAssociationField(assocDef, form, null, null, namespaceService);
    }

    /**
     * Sets up a field definition for the given association.
     * <p>
     * NOTE: This method is static so that it can serve as a helper method for
     * FormFilter implementations as adding additional association fields is
     * likely to be a common extension.
     * </p>
     * 
     * @param assocDef The AssociationDefinition of the field to generate
     * @param form The Form instance to populate
     * @param assocValues The values of the association field, can be null
     * @param namespaceService NamespaceService instance
     */
    @SuppressWarnings("unchecked")
    protected void generateAssociationField(AssociationDefinition assocDef, Form form, List assocValues,
                NamespaceService namespaceService)
    {
        generateAssociationField(assocDef, form, assocValues, null, namespaceService);
    }

    /**
     * Sets up a field definition for the given association.
     * <p>
     * NOTE: This method is static so that it can serve as a helper method for
     * FormFilter implementations as adding additional association fields is
     * likely to be a common extension.
     * </p>
     * 
     * @param assocDef The AssociationDefinition of the field to generate
     * @param form The Form instance to populate
     * @param assocValues The values of the association field, can be null
     * @param group The FieldGroup the association field belongs to, can be null
     * @param namespaceService NamespaceService instance
     */
    @SuppressWarnings("unchecked")
    protected static void generateAssociationField(AssociationDefinition assocDef, Form form, List assocValues,
                FieldGroup group, NamespaceService namespaceService)
    {
        String assocName = assocDef.getName().toPrefixString(namespaceService);
        String[] nameParts = QName.splitPrefixedQName(assocName);
        AssociationFieldDefinition fieldDef = new AssociationFieldDefinition(assocName, assocDef.getTargetClass()
                    .getName().toPrefixString(namespaceService), Direction.TARGET);
        String title = assocDef.getTitle();
        if (title == null)
        {
            title = assocName;
        }
        fieldDef.setLabel(title);
        fieldDef.setDescription(assocDef.getDescription());
        fieldDef.setProtectedField(assocDef.isProtected());
        fieldDef.setEndpointMandatory(assocDef.isTargetMandatory());
        fieldDef.setEndpointMany(assocDef.isTargetMany());
        fieldDef.setGroup(group);

        // define the data key name and set
        String dataKeyName = ASSOC_DATA_PREFIX + nameParts[0] + DATA_KEY_SEPARATOR + nameParts[1];
        fieldDef.setDataKeyName(dataKeyName);

        // add definition to the form
        form.addFieldDefinition(fieldDef);

        if (assocValues != null)
        {
            // add the association value to the form
            // determine the type of association values data and extract
            // accordingly
            List<String> values = new ArrayList<String>(4);
            for (Object value : assocValues)
            {
                if (value instanceof ChildAssociationRef)
                {
                    values.add(((ChildAssociationRef) value).getChildRef().toString());
                }
                else if (value instanceof AssociationRef)
                {
                    values.add(((AssociationRef) value).getTargetRef().toString());
                }
                else
                {
                    values.add(value.toString());
                }
            }

            // Add the list as the value for the association.
            form.addData(dataKeyName, values);
        }
    }

    /**
     * Sets up the field definitions for all the requested fields.
     * <p>
     * A NodeRef or TypeDefinition can be provided, however, if a NodeRef is
     * provided all type information will be derived from the NodeRef and the
     * TypeDefinition will be ignored.
     * </p>
     * <p>
     * If any of the requested fields are not present on the type and they
     * appear in the forcedFields list an attempt to find a model definition for
     * those fields is made so they can be included.
     * </p>
     * 
     * @param nodeRef The NodeRef of the item being generated
     * @param typeDef The TypeDefiniton of the item being generated
     * @param fields Restricted list of fields to include
     * @param forcedFields List of field names that should be included even if
     *            the field is not currently present
     * @param form The Form instance to populate
     */
    protected void generateSelectedFields(NodeRef nodeRef, TypeDefinition typeDef, List<String> fields,
                List<String> forcedFields, Form form)
    {
        // ensure a NodeRef or TypeDefinition is provided
        if (nodeRef == null && typeDef == null) { throw new IllegalArgumentException(
                    "A NodeRef or TypeDefinition must be provided"); }

        if (getLogger().isDebugEnabled())
            getLogger().debug("Generating selected fields: " + fields + " and forcing: " + forcedFields);

        // get data dictionary definition for node if it is provided
        QName type = null;
        Map<QName, Serializable> propValues = Collections.emptyMap();
        Map<QName, PropertyDefinition> propDefs = null;
        Map<QName, AssociationDefinition> assocDefs = null;

        if (nodeRef != null)
        {
            type = this.nodeService.getType(nodeRef);
            typeDef = this.dictionaryService.getAnonymousType(type, this.nodeService.getAspects(nodeRef));

            // NOTE: the anonymous type returns all property and association
            // defs
            // for all aspects applied as well as the type
            propDefs = typeDef.getProperties();
            assocDefs = typeDef.getAssociations();
            propValues = this.nodeService.getProperties(nodeRef);
        }
        else
        {
            type = typeDef.getName();

            // we only get the properties and associations of the actual type so
            // we also need to manually get properties and associations from any
            // mandatory aspects
            propDefs = new HashMap<QName, PropertyDefinition>(16);
            assocDefs = new HashMap<QName, AssociationDefinition>(16);
            propDefs.putAll(typeDef.getProperties());
            assocDefs.putAll(typeDef.getAssociations());

            List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
            for (AspectDefinition aspect : aspects)
            {
                propDefs.putAll(aspect.getProperties());
                assocDefs.putAll(aspect.getAssociations());
            }
        }

        for (String fieldName : fields)
        {
            // try and split the field name
            String[] parts = fieldName.split(":");
            if (parts.length == 2 || parts.length == 3)
            {
                boolean foundField = false;
                boolean tryProperty = true;
                boolean tryAssociation = true;
                String qNamePrefix = null;
                String localName = null;

                if (parts.length == 2)
                {
                    qNamePrefix = parts[0];
                    localName = parts[1];
                }
                else
                {
                    // if there are 3 parts to the field name the first one
                    // represents
                    // whether the field is a property or association i.e.
                    // prop:prefix:local
                    // or assoc:prefix:local, determine the prefix and ensure
                    // it's valid
                    if (PROP.equals(parts[0]))
                    {
                        tryAssociation = false;
                    }
                    else if (ASSOC.equals(parts[0]))
                    {
                        tryProperty = false;
                    }
                    else
                    {
                        if (getLogger().isWarnEnabled())
                            getLogger()
                                        .warn(
                                                    "\""
                                                                + parts[0]
                                                                + "\" is an invalid prefix for requesting a property or association");

                        continue;
                    }

                    qNamePrefix = parts[1];
                    localName = parts[2];
                }

                // create qname of field name
                QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);

                // try the field as a property
                if (tryProperty)
                {
                    // lookup property def on node
                    PropertyDefinition propDef = propDefs.get(fullQName);
                    if (propDef != null)
                    {
                        // generate the property field
                        generatePropertyField(propDef, form, propValues.get(fullQName), this.namespaceService);

                        // no need to try and find an association
                        tryAssociation = false;
                        foundField = true;
                    }
                }

                // try the field as an association
                if (tryAssociation)
                {
                    AssociationDefinition assocDef = assocDefs.get(fullQName);
                    if (assocDef != null)
                    {
                        // generate the association field
                        generateAssociationField(assocDef, form, (nodeRef != null) ? retrieveAssociationValues(nodeRef,
                                    assocDef) : null, this.namespaceService);

                        foundField = true;
                    }
                }

                // still not found the field, is it a force'd field?
                if (!foundField)
                {
                    if (forcedFields != null && forcedFields.size() > 0 && forcedFields.contains(fieldName))
                    {
                        generateForcedField(fieldName, form);
                    }
                    else if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug(
                                    "Ignoring field \"" + fieldName + "\" as it is not defined for the current "
                                                + ((nodeRef != null) ? "node" : "type")
                                                + " and it does not appear in the 'force' list");
                    }
                }
            }
            else
            {
                // see if the fieldName is a well known transient property
                if (TRANSIENT_MIMETYPE.equals(fieldName) || TRANSIENT_ENCODING.equals(fieldName)
                            || TRANSIENT_SIZE.equals(fieldName))
                {
                    // if the node type is content or sublcass thereof generate appropriate field
                    if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
                    {
                        ContentData content = null;
                        
                        if (nodeRef != null)
                        {
                            content = (ContentData) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                        }
                        
                        if (TRANSIENT_MIMETYPE.equals(fieldName))
                        {
                            generateMimetypePropertyField(content, form);
                        }
                        else if (TRANSIENT_ENCODING.equals(fieldName))
                        {
                            generateEncodingPropertyField(content, form);
                        }
                        else if (TRANSIENT_SIZE.equals(fieldName))
                        {
                            generateSizePropertyField(content, form);
                        }
                    }
                }
                else if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Ignoring unrecognised field \"" + fieldName + "\"");
                }
            }
        }
    }

    /**
     * Generates a field definition for the given field that is being forced to
     * show.
     * 
     * @param fieldName Name of the field to force
     * @param form The Form instance to populated
     */
    protected void generateForcedField(String fieldName, Form form)
    {
        if (getLogger().isDebugEnabled())
            getLogger().debug("Attempting to force the inclusion of field \"" + fieldName + "\"");

        String[] parts = fieldName.split(":");
        if (parts.length == 2 || parts.length == 3)
        {
            boolean foundField = false;
            boolean tryProperty = true;
            boolean tryAssociation = true;
            String qNamePrefix = null;
            String localName = null;

            if (parts.length == 2)
            {
                qNamePrefix = parts[0];
                localName = parts[1];
            }
            else
            {
                // if there are 3 parts to the field name the first one
                // represents
                // whether the field is a property or association i.e.
                // prop:prefix:local
                // or assoc:prefix:local, determine the prefix and ensure it's
                // valid
                if (PROP.equals(parts[0]))
                {
                    tryAssociation = false;
                }
                else if (ASSOC.equals(parts[0]))
                {
                    tryProperty = false;
                }
                else
                {
                    if (getLogger().isWarnEnabled())
                        getLogger().warn(
                                    "\"" + parts[0]
                                                + "\" is an invalid prefix for requesting a property or association");

                    return;
                }

                qNamePrefix = parts[1];
                localName = parts[2];
            }

            // create qname of field name
            QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);

            if (tryProperty)
            {
                // lookup the field as a property in the whole model
                PropertyDefinition propDef = this.dictionaryService.getProperty(fullQName);
                if (propDef != null)
                {
                    // generate the property field
                    generatePropertyField(propDef, form, this.namespaceService);

                    // no need to try and find an association
                    tryAssociation = false;
                    foundField = true;
                }
            }

            if (tryAssociation)
            {
                // lookup the field as an association in the whole model
                AssociationDefinition assocDef = this.dictionaryService.getAssociation(fullQName);
                if (assocDef != null)
                {
                    // generate the association field
                    generateAssociationField(assocDef, form, this.namespaceService);

                    foundField = true;
                }
            }

            if (!foundField && getLogger().isDebugEnabled())
            {
                getLogger()
                            .debug(
                                        "Ignoring field \""
                                                    + fieldName
                                                    + "\" as it is not defined for the current node and can not be found in any model");
            }
        }
        else if (getLogger().isWarnEnabled())
        {
            getLogger().warn("Ignoring unrecognised field \"" + fieldName + "\"");
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemType(java.lang.Object)
     */
    @Override
    protected String getItemType(WorkflowDefinition item)
    {
        return item.name;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemURI(java.lang.Object)
     */
    @Override
    protected String getItemURI(WorkflowDefinition item)
    {
        return "/api/workflow-definitions/" + item.id;
    }
    
    @Override
    protected Map<QName, Serializable> getAssociationValues(WorkflowDefinition item)
    {
        return null;
    }
    
    @Override
    protected Map<QName, Serializable> getPropertyValues(WorkflowDefinition item) 
    {
        return null;
    }
    
    @Override
    protected Map<String, Object> getTransientValues(WorkflowDefinition item)
    {
        return null;
    }

    @Override
    protected TypeDefinition getBaseType(WorkflowDefinition item)
    {
        return item.getStartTaskDefinition().metadata;
    }
}
