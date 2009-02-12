/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms.processor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handler to handle the generation and persistence of a Form object for a repository node.
 * <p>
 * This handler will add all properties (including those of any aspects applied) and 
 * associations of the node to the Form.
 *
 * @author Gavin Cornwell
 */
public class NodeHandler extends AbstractHandler
{
    private static final Log logger = LogFactory.getLog(NodeHandler.class);

    protected static final String PROP_PREFIX = "prop:";
    protected static final String ASSOC_PREFIX = "assoc:";
    
    /** Services */
    protected NodeService nodeService;
    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;

    /**
     * A regular expression which can be used to match property/association names.
     * These names will look like <code>"prop:cm:name"</code>.
     * The pattern can also be used to extract the "cm" and the "name" parts.
     */
    private Pattern propertyNamePattern = Pattern.compile("prop:(.*){1}?:(.*){1}?");
    
    /**
     * Sets the node service 
     * 
     * @param nodeService The NodeService instance
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the data dictionary service
     * 
     * @param dictionaryService The DictionaryService instance
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Sets the namespace service
     * 
     * @param namespaceService The NamespaceService instance
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /*
     * @see org.alfresco.repo.forms.processor.FormProcessorHandler#handleGenerate(java.lang.Object, org.alfresco.repo.forms.Form)
     */
    public Form handleGenerate(Object item, Form form)
    {
        if (logger.isDebugEnabled())
            logger.debug("Generating form for: " + item);
        
        // cast to the expected NodeRef representation
        NodeRef nodeRef = (NodeRef)item;
        
        // set the type
        QName type = this.nodeService.getType(nodeRef);
        form.setType(type.toPrefixString(this.namespaceService));
        
        // setup field definitions and data
        setupFields(nodeRef, form);
        
        if (logger.isDebugEnabled())
            logger.debug("Returning form: " + form);
        
        return form;
    }

    /*
     * @see org.alfresco.repo.forms.processor.FormProcessorHandler#handlePersist(java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    public void handlePersist(Object item, FormData data)
    {
        // cast to the expected NodeRef representation
        NodeRef nodeRef = (NodeRef)item;
        
        Map<QName, Serializable> submittedProperties = extractSubmittedPropsFrom(data);
        
        // The call to addProperties changes the repo values of those properties
        // included in the Map, but leaves any other property values unchanged.
        //
        // Compare setProperties(..), which causes the deletion of properties that
        // are not included in the Map.
        this.nodeService.addProperties(nodeRef, submittedProperties);
    }

    private Map<QName, Serializable> extractSubmittedPropsFrom(FormData data)
    {
        Map<QName, Serializable> result = new HashMap<QName, Serializable>();
        for (String dataKey : data.getData().keySet())
        {
            FieldData nextFieldData = data.getData().get(dataKey);
            if (nextFieldData.isFile())
            {
                //TODO Implement support for file-based submits.
            }
            else
            {
                String nextDataName = nextFieldData.getName();
                
                Matcher m = this.propertyNamePattern.matcher(nextDataName);
                // Only "prop:" properties are handled here
                if (m.matches())
                {
                    String qNamePrefix = m.group(1);
                    String localName = m.group(2);
                    QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);
                    // These values are all Strings. The repo does most of the data
                    // conversion work for us.
                    Object nextDataObject = nextFieldData.getValue();
                    
                    // This cast should be safe as all dataObjects are Strings.
                    result.put(fullQName, (Serializable)nextDataObject);
                }
                //TODO Implement support for "assoc:" properties.
            }
        }
        return result;
    }
    
    /**
     * Sets up the field definitions for the form
     */
    @SuppressWarnings("unchecked")
    private void setupFields(NodeRef nodeRef, Form form)
    {
        FormData formData = new FormData();
        
        // get data dictionary definition for node
        QName type = this.nodeService.getType(nodeRef);
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(
                    type, this.nodeService.getAspects(nodeRef));
        
        // iterate round the property definitions, create the equivalent
        // field definition and setup the data for the property
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        Map<QName, Serializable> propValues = this.nodeService.getProperties(nodeRef);
        for (PropertyDefinition propDef : propDefs.values())
        {
            String propName = propDef.getName().toPrefixString(this.namespaceService);
            PropertyFieldDefinition fieldDef = new PropertyFieldDefinition(
                        propName, propDef.getDataType().getName().toPrefixString(
                        this.namespaceService));
            
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
            
            // setup constraints for the property
            List<ConstraintDefinition> constraints = propDef.getConstraints();
            if (constraints != null && constraints.size() > 0)
            {
                List<FieldConstraint> fieldConstraints = 
                    new ArrayList<FieldConstraint>(constraints.size());
                
                for (ConstraintDefinition constraintDef : constraints)
                {
                    Constraint constraint = constraintDef.getConstraint();
                    Map<String, String> fieldConstraintParams = null;
                    Map<String, Object> constraintParams = constraint.getParameters();
                    if (constraintParams != null)
                    {
                        fieldConstraintParams = new HashMap<String, String>(constraintParams.size());
                        for (String name : constraintParams.keySet())
                        {
                            fieldConstraintParams.put(name, constraintParams.get(name).toString());
                        }
                    }
                    FieldConstraint fieldConstraint = fieldDef.new FieldConstraint(
                                constraint.getType(), fieldConstraintParams);
                    fieldConstraints.add(fieldConstraint);
                }
                
                fieldDef.setConstraints(fieldConstraints);
            }
            
            form.addFieldDefinition(fieldDef);
            
            // get the field value and add to the form data object
            Serializable fieldData = propValues.get(propDef.getName());
            if (fieldData != null)
            {
                if (fieldData instanceof List)
                {
                    List list = (List)fieldData;
                    String fieldName = PROP_PREFIX + fieldDef.getName();
                    for (int x = 0; x < list.size(); x++)
                    {
                        Object repeatingVal = list.get(x);
                        formData.addData(fieldName + "_" + x, repeatingVal);
                    }
                }
                else
                {
                    formData.addData(PROP_PREFIX + fieldDef.getName(), fieldData);
                }
            }
        }
        
        // add target association data
        List<AssociationRef> associations = this.nodeService.getTargetAssocs(nodeRef, 
                    RegexQNamePattern.MATCH_ALL);
        if (associations.size() > 0)
        {
            // create internal cache of association definitions created
            Map<String, AssociationFieldDefinition> assocFieldDefs = 
                new HashMap<String, AssociationFieldDefinition>(associations.size());

            for (AssociationRef assoc : associations)
            {
                // get the name of the association
                QName assocType = assoc.getTypeQName();
                String assocName = assocType.toPrefixString(this.namespaceService);
                String assocValue = assoc.getTargetRef().toString();
                
                // setup the field definition for the association if it hasn't before
                AssociationFieldDefinition fieldDef = assocFieldDefs.get(assocName);
                if (fieldDef == null)
                {
                    AssociationDefinition assocDef = this.dictionaryService.getAssociation(assocType);
                    if (assocDef == null)
                    {
                        throw new FormException("Failed to find associaton definition for association: " + assocType);
                    }
                    
                    fieldDef = new AssociationFieldDefinition(assocName, 
                                assocDef.getTargetClass().getName().toPrefixString(
                                this.namespaceService), Direction.TARGET);
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
                    
                    // add definition to Form and to internal cache
                    form.addFieldDefinition(fieldDef);
                    assocFieldDefs.put(assocName, fieldDef);
                }
                    
                if (fieldDef.isEndpointMany())
                {
                    // add the value as a List (or add to the list if the form data
                    // is already present)
                    List<String> targets = (List<String>)formData.getData().get(assocName);
                    if (targets == null)
                    {
                        targets = new ArrayList<String>(4);
                        formData.addData(ASSOC_PREFIX + assocName, targets);
                    }
                    
                    // add the assoc value to the list
                    targets.add(assocValue);
                }
                else
                {
                    // there should only be one value
                    formData.addData(ASSOC_PREFIX + assocName, assocValue);
                }
            }
        }
        
        // TODO: Add source association definitions and data
        
        // set the form data
        form.setFormData(formData);
    }
}
