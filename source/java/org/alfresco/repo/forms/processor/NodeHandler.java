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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
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
    protected static final String ASSOC_ADD_SUFFIX = "_added";
    protected static final String ASSOC_REMOVE_SUFFIX = "_removed";
    
    protected static final String TRANSIENT_MIMETYPE = "mimetype";
    protected static final String TRANSIENT_SIZE = "size";
    protected static final String TRANSIENT_ENCODING = "encoding";
    
    protected static final String MSG_MIMETYPE_LABEL = "form_service.mimetype.label";
    protected static final String MSG_MIMETYPE_DESC = "form_service.mimetype.description";
    protected static final String MSG_ENCODING_LABEL = "form_service.encoding.label";
    protected static final String MSG_ENCODING_DESC = "form_service.encoding.description";
    protected static final String MSG_SIZE_LABEL = "form_service.size.label";
    protected static final String MSG_SIZE_DESC = "form_service.size.description";
    
    /** Services */
    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;

    /**
     * A regular expression which can be used to match property names.
     * These names will look like <code>"prop:cm:name"</code>.
     * The pattern can also be used to extract the "cm" and the "name" parts.
     */
    protected Pattern propertyNamePattern = Pattern.compile(PROP_PREFIX + "(.*){1}?:(.*){1}?");
    
    /**
     * A regular expression which can be used to match tranisent property names.
     * These names will look like <code>"prop:name"</code>.
     * The pattern can also be used to extract the "name" part.
     */
    protected Pattern transientPropertyPattern = Pattern.compile(PROP_PREFIX + "(.*){1}?");
    
    /**
     * A regular expression which can be used to match association names.
     * These names will look like <code>"assoc:cm:references_added"</code>.
     * The pattern can also be used to extract the "cm", the "name" and the suffix parts.
     */
    protected Pattern associationNamePattern = Pattern.compile(ASSOC_PREFIX + "(.*){1}?:(.*){1}?(_[a-zA-Z]+)");
    
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
     * Sets the file folder service 
     * 
     * @param fileFolderService The FileFolderService instance
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
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
     * @see org.alfresco.repo.forms.processor.Handler#handleGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form)
     */
    public Form handleGenerate(Object item, List<String> fields, List<String> forcedFields, Form form)
    {
        if (logger.isDebugEnabled())
            logger.debug("Generating form for: " + item);
        
        // cast to the expected NodeRef representation
        NodeRef nodeRef = (NodeRef)item;
        
        // generate the form for the node
        generateNode(nodeRef, fields, forcedFields, form);
        
        if (logger.isDebugEnabled())
            logger.debug("Returning form: " + form);
        
        return form;
    }

    /**
     * Sets up the Form object for the given NodeRef
     * 
     * @param nodeRef The NodeRef to generate a Form for
     * @param fields Restricted list of fields to include
     * @param forcedFields List of fields to forcibly include
     * @param form The Form instance to populate
     */
    protected void generateNode(NodeRef nodeRef, List<String> fields, List<String> forcedFields, Form form)
    {
        // set the type
        QName type = this.nodeService.getType(nodeRef);
        form.setType(type.toPrefixString(this.namespaceService));
        
        if (fields != null && fields.size() > 0)
        {
            generateSelectedFields(nodeRef, fields, forcedFields, form);
        }
        else
        {
            // setup field definitions and data
            generateAllPropertyFields(nodeRef, form);
            generateAllAssociationFields(nodeRef, form);
            generateAllChildAssociationFields(nodeRef, form);
            generateTransientFields(nodeRef, form);
        }
    }
    
    /**
     * Sets up the field definitions for all the requested fields.
     * If any of the requested fields are not present on the node and they
     * appear in the forcedFields list an attempt to find a model
     * definition for those fields is made so they can be included.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param fields Restricted list of fields to include
     * @param forcedFields List of field names that should be included
     *                     even if the field is not currently present
     * @param form The Form instance to populate
     */
    protected void generateSelectedFields(NodeRef nodeRef, List<String> fields, List<String> forcedFields, Form form)
    {
        if (logger.isDebugEnabled())
            logger.debug("Generating selected fields: " + fields + " and forcing: " + forcedFields);
        
        // get data dictionary definition for node
        QName type = this.nodeService.getType(nodeRef);
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(type,
                    this.nodeService.getAspects(nodeRef));
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        Map<QName, AssociationDefinition> assocDefs = typeDef.getAssociations();
        Map<QName, ChildAssociationDefinition> childAssocDefs = typeDef.getChildAssociations();
        Map<QName, Serializable> propValues = this.nodeService.getProperties(nodeRef);
        
        for (String fieldName : fields)
        {
            // try and split the field name
            String[] parts = fieldName.split(":");
            if (parts.length == 2)
            {
                // create qname of field name
                String qNamePrefix = parts[0];
                String localName = parts[1];
                QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);
            
                // lookup property def on node
                PropertyDefinition propDef = propDefs.get(fullQName);
                if (propDef != null)
                {
                    // generate the property field
                    generatePropertyField(propDef, propValues.get(propDef.getName()), form);
                }
                else
                {
                    // look for association defined for the type
                    AssociationDefinition assocDef = assocDefs.get(fullQName);
                    if (assocDef != null)
                    {
                        // generate the association field
                        generateAssociationField(assocDef, 
                                    this.nodeService.getTargetAssocs(nodeRef, fullQName), form);
                    }
                    else
                    {
                        ChildAssociationDefinition childAssocDef = childAssocDefs.get(fullQName);
                        if (childAssocDef != null)
                        {
                            // generate the association field
                            // TODO: see if we can get just the specific child assoc data
                            generateAssociationField(childAssocDef, 
                                        this.nodeService.getChildAssocs(nodeRef), form);
                        }
                        else
                        {
                            // still not found the field, is it a force'd field?
                            if (forcedFields != null && forcedFields.size() > 0 && 
                                forcedFields.contains(fieldName))
                            {
                                generateForcedField(fullQName, form);
                            }
                            else if (logger.isWarnEnabled())
                            {
                                logger.warn("Ignoring field \"" + fieldName + 
                                            "\" as it is not defined for the current node and it does not appear in the 'force' list");
                            }
                        }
                    }
                }
            }
            else
            {
                // see if the fieldName is a well known transient property
                if (TRANSIENT_MIMETYPE.equals(fieldName) || TRANSIENT_ENCODING.equals(fieldName) ||
                    TRANSIENT_SIZE.equals(fieldName))
                {
                    // if the node type is content or sublcass thereof generate appropriate field
                    if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
                    {
                        ContentData content = (ContentData)this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                        if (content != null)
                        {
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
                }
                else if (logger.isWarnEnabled())
                {
                    logger.warn("Ignoring unrecognised field \"" + fieldName + "\"");
                }
            }
        }
    }
    
    /**
     * Generates a field definition for the given field that is being forced
     * to show
     * 
     * @param fieldName QName of the field to force
     * @param form The Form instance to populated
     */
    protected void generateForcedField(QName fieldName, Form form)
    {
        if (logger.isDebugEnabled())
            logger.debug("Attempting to force the inclusion of field \"" + 
                        fieldName.toPrefixString(this.namespaceService) + "\"");
        
        // lookup the field as a property in the model
        PropertyDefinition propDef = this.dictionaryService.getProperty(fieldName);
        if (propDef != null)
        {
            // generate the property field
            generatePropertyField(propDef, null, form);
        }
        else
        {
            // lookup the field as an association in the model
            AssociationDefinition assocDef = this.dictionaryService.getAssociation(fieldName);
            if (assocDef != null)
            {
                // generate the association field
                generateAssociationField(assocDef, null, form);
            }
            else if (logger.isWarnEnabled())
            {
                logger.warn("Ignoring field \"" + fieldName.toPrefixString(this.namespaceService) + 
                            "\" as it is not defined for the current node and can not be found in any model");
            }
        }
    }
    
    /**
     * Sets up the field definitions for all the node's properties.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param form The Form instance to populate
     */
    protected void generateAllPropertyFields(NodeRef nodeRef, Form form)
    {
        // get data dictionary definition for node
        QName type = this.nodeService.getType(nodeRef);
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(type,
                    this.nodeService.getAspects(nodeRef));
        
        // iterate round the property definitions for the node and create 
        // the equivalent field definition and setup the data for the property
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        Map<QName, Serializable> propValues = this.nodeService.getProperties(nodeRef);
        for (PropertyDefinition propDef : propDefs.values())
        {
            generatePropertyField(propDef, propValues.get(propDef.getName()), form);
        }
    }
    
    /**
     * Sets up a field definition for the given property
     * 
     * @param propDef The PropertyDefinition of the field to generate
     * @param propValue The value of the property field
     * @param form The Form instance to populate
     */
    @SuppressWarnings("unchecked")
    protected void generatePropertyField(PropertyDefinition propDef, Serializable propValue, Form form)
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
                    // TODO: Just return the param value object, don't convert to String
                    fieldConstraintParams = new HashMap<String, String>(constraintParams.size());
                    for (String name : constraintParams.keySet())
                    {
                        Object paramValue = constraintParams.get(name);
                        
                        if (paramValue instanceof List)
                        {
                            paramValue = makeListString((List)paramValue);
                        }
                        
                        fieldConstraintParams.put(name, paramValue.toString());
                    }
                }
                FieldConstraint fieldConstraint = fieldDef.new FieldConstraint(
                            constraint.getType(), fieldConstraintParams);
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
                propValue = makeListString((List)propValue);
            }
            
            form.addData(PROP_PREFIX + fieldDef.getName(), propValue);
        }
    }

    /**
     * Sets up the field definitions for any transient fields that may be
     * useful, for example, 'mimetype', 'size' and 'encoding'.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param form The Form instance to populate
     */
    protected void generateTransientFields(NodeRef nodeRef, Form form)
    {
        // if the node is content add the 'mimetype', 'size' and 'encoding' fields.
        QName type = this.nodeService.getType(nodeRef);
        if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
        {
            ContentData content = (ContentData)this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            if (content != null)
            {
                // setup mimetype field
                generateMimetypePropertyField(content, form);
                
                // setup encoding field
                generateEncodingPropertyField(content, form);
                
                // setup size field
                generateSizePropertyField(content, form);
            }
        }
    }
    
    /**
     * Generates the field definition for the transient mimetype property
     * 
     * @param content The ContentData object to generate the field from
     * @param form The Form instance to populate
     */
    protected void generateMimetypePropertyField(ContentData content, Form form)
    {
        PropertyFieldDefinition mimetypeField = new PropertyFieldDefinition(
                    TRANSIENT_MIMETYPE, DataTypeDefinition.TEXT.toPrefixString(
                    this.namespaceService));
        mimetypeField.setLabel(I18NUtil.getMessage(MSG_MIMETYPE_LABEL));
        mimetypeField.setDescription(I18NUtil.getMessage(MSG_MIMETYPE_DESC));
        form.addFieldDefinition(mimetypeField);
        form.addData(PROP_PREFIX + TRANSIENT_MIMETYPE, content.getMimetype());
    }
    
    /**
     * Generates the field definition for the transient encoding property
     * 
     * @param content The ContentData object to generate the field from
     * @param form The Form instance to populate
     */
    protected void generateEncodingPropertyField(ContentData content, Form form)
    {
        PropertyFieldDefinition encodingField = new PropertyFieldDefinition(
                    TRANSIENT_ENCODING, DataTypeDefinition.TEXT.toPrefixString(
                    this.namespaceService));
        encodingField.setLabel(I18NUtil.getMessage(MSG_ENCODING_LABEL));
        encodingField.setDescription(I18NUtil.getMessage(MSG_ENCODING_DESC));
        form.addFieldDefinition(encodingField);
        form.addData(PROP_PREFIX + TRANSIENT_ENCODING, content.getEncoding());
    }
    
    /**
     * Generates the field definition for the transient size property
     * 
     * @param content The ContentData object to generate the field from
     * @param form The Form instance to populate
     */
    protected void generateSizePropertyField(ContentData content, Form form)
    {
        PropertyFieldDefinition sizeField = new PropertyFieldDefinition(
                    TRANSIENT_SIZE, DataTypeDefinition.LONG.toPrefixString(
                    this.namespaceService));
        sizeField.setLabel(I18NUtil.getMessage(MSG_SIZE_LABEL));
        sizeField.setDescription(I18NUtil.getMessage(MSG_SIZE_DESC));
        sizeField.setProtectedField(true);
        form.addFieldDefinition(sizeField);
        form.addData(PROP_PREFIX + TRANSIENT_SIZE, new Long(content.getSize()));
    }
    
    /**
     * Sets up the field definitions for all the node's associations.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param form The Form instance to populate
     */
    @SuppressWarnings("unchecked")
    protected void generateAllAssociationFields(NodeRef nodeRef, Form form)
    {
        // ********************************************************
        //  re-factor this to gen. all assocs defs and not values
        // ********************************************************
        
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
                        throw new FormException("Failed to find association definition for association: " + assocType);
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
                    
                String prefixedAssocName = ASSOC_PREFIX + assocName;
                
                if (fieldDef.isEndpointMany())
                {
                    List<String> targets = null;
                    
                    // add the value as a List (or add to the list if the form data
                    // is already present)
                    FieldData fieldData = form.getFormData().getData().get(prefixedAssocName);
                    if (fieldData == null)
                    {
                        targets = new ArrayList<String>(4);
                        form.addData(prefixedAssocName, targets);
                    }
                    else
                    {
                        targets = (List<String>)fieldData.getValue();
                    }
                    
                    // add the assoc value to the list
                    targets.add(assocValue);
                }
                else
                {
                    // there should only be one value
                    form.addData(prefixedAssocName, assocValue);
                }
            }
        }
    }
    
    /**
     * Sets up the field definitions for the node's child associations.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param form The Form instance to populate
     */
    protected void generateAllChildAssociationFields(NodeRef nodeRef, Form form)
    {
        List<ChildAssociationRef> childAssocs = this.nodeService.getChildAssocs(nodeRef);
        if (childAssocs.isEmpty())
        {
            return;
        }
        // create internal cache of association definitions created
        Map<String, AssociationFieldDefinition> childAssocFieldDefs = 
            new HashMap<String, AssociationFieldDefinition>(childAssocs.size());
        
        // All child associations are from the same parent node.
        // However, the type of the child associations may not all be the same.
        // This Map will have assocNames (e.g. sys:children) as a key and will
        // have a List of child noderefs as values.
        // So there will be one list for each of the child association *types*.
        Map<String, List<ChildAssociationRef>> childrenNodes = new HashMap<String, List<ChildAssociationRef>>();
        
        for (ChildAssociationRef childAssoc : childAssocs)
        {
            // get the name of the association
            QName assocTypeName = childAssoc.getTypeQName();
            String assocName = assocTypeName.toPrefixString(this.namespaceService);
            
            // setup the field definition for the association if it hasn't before
            AssociationFieldDefinition fieldDef = childAssocFieldDefs.get(assocName);
            if (fieldDef == null)
            {
                AssociationDefinition assocDef = this.dictionaryService.getAssociation(assocTypeName);
                if (assocDef == null || assocDef instanceof ChildAssociationDefinition == false)
                {
                    throw new FormException("Failed to find association definition for child association: "
                            + assocTypeName);
                }
                
                fieldDef = new AssociationFieldDefinition(assocName, 
                            assocDef.getTargetClass().getName().toPrefixString(
                            this.namespaceService), Direction.TARGET);
                String title = assocDef.getTitle();
                if (title == null)
                {
                    title = assocName.toString();
                }
                fieldDef.setLabel(title);
                fieldDef.setDescription(assocDef.getDescription());
                fieldDef.setProtectedField(assocDef.isProtected());
                fieldDef.setEndpointMandatory(assocDef.isTargetMandatory());
                fieldDef.setEndpointMany(assocDef.isTargetMany());
                
                // add definition to Form and to internal cache
                form.addFieldDefinition(fieldDef);
                childAssocFieldDefs.put(assocName, fieldDef);
            }

            if (childrenNodes.containsKey(assocName) == false)
            {
                childrenNodes.put(assocName, new ArrayList<ChildAssociationRef>());
            }
            childrenNodes.get(assocName).add(childAssoc);
        }
        for (String associationName : childrenNodes.keySet())
        {
            List<ChildAssociationRef> values = childrenNodes.get(associationName);
            
            // We don't want the whitespace or enclosing square brackets that come
            // with java.util.List.toString(), hence the custom String construction.
            /*StringBuilder assocValue = new StringBuilder();
            for (Iterator<ChildAssociationRef> iter = values.iterator(); iter.hasNext(); )
            {
                ChildAssociationRef nextChild = iter.next();
                assocValue.append(nextChild.getChildRef().toString());
                if (iter.hasNext())
                {
                    assocValue.append(",");
                }
            }
            form.addData(ASSOC_PREFIX + associationName, assocValue.toString());*/
            
            List<String> nodeRefs = new ArrayList<String>(4);
            for (Iterator<ChildAssociationRef> iter = values.iterator(); iter.hasNext(); )
            {
                ChildAssociationRef nextChild = iter.next();
                nodeRefs.add(nextChild.getChildRef().toString());
            }
            form.addData(ASSOC_PREFIX + associationName, nodeRefs);
        }
    }
    
    /**
     * Sets up a field definition for the given association
     * 
     * @param assocDef The AssociationDefinition of the field to generate
     * @param assocValues The values of the association field
     * @param form The Form instance to populate
     */
    @SuppressWarnings("unchecked")
    protected void generateAssociationField(AssociationDefinition assocDef, 
                List assocValues, Form form)
    {
        String assocName = assocDef.getName().toPrefixString(this.namespaceService);
        AssociationFieldDefinition fieldDef = new AssociationFieldDefinition(assocName, 
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
        
        // add definition to the form
        form.addFieldDefinition(fieldDef);
        
        // add the association value to the form
        String prefixedAssocName = ASSOC_PREFIX + assocName;
        
        // determine the type of association values data and extract accordingly
        List<String> values = new ArrayList<String>(4);
        for (Object value : assocValues)
        {
            if (value instanceof ChildAssociationRef)
            {
                values.add(((ChildAssociationRef)value).getChildRef().toString());
            }
            else if (value instanceof AssociationRef)
            {
                values.add(((AssociationRef)value).getTargetRef().toString());
            }
            else
            {
                values.add(value.toString());
            }
        }
        
        // Add the list as the value for the association.
        // TODO: Do we also return a well known named list of association names
        //       for each noderef so that clients do not have extra work to do
        //       to display the current values to the user
        form.addData(prefixedAssocName, values);
    }
    
    /*
     * @see org.alfresco.repo.forms.processor.FormProcessorHandler#handlePersist(java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    public void handlePersist(Object item, FormData data)
    {
        if (logger.isDebugEnabled())
            logger.debug("Persisting form for: " + item);
        
        // cast to the expected NodeRef representation
        NodeRef nodeRef = (NodeRef)item;
        
        // persist the node
        persistNode(nodeRef, data);
    }
    
    /**
     * Persists the given FormData on the given NodeRef
     * 
     * @param nodeRef The NodeRef to persist the form data on
     * @param data The FormData to persist
     */
    protected void persistNode(NodeRef nodeRef, FormData data)
    {
        // get the property definitions for the type of node being persisted
        QName type = this.nodeService.getType(nodeRef);
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(type,
                    this.nodeService.getAspects(nodeRef));
        Map<QName, AssociationDefinition> assocDefs = typeDef.getAssociations();
        Map<QName, ChildAssociationDefinition> childAssocDefs = typeDef.getChildAssociations();
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        
        Map<QName, Serializable> propsToPersist = new HashMap<QName, Serializable>(data.getData().size());
        List<AbstractAssocCommand> assocsToPersist = new ArrayList<AbstractAssocCommand>();
        
        for (String dataKey : data.getData().keySet())
        {
            FieldData fieldData = data.getData().get(dataKey);
            // NOTE: ignore file fields for now, not supported yet!
            if (fieldData.isFile() == false)
            {
                String fieldName = fieldData.getName();
                
                if (fieldName.startsWith(PROP_PREFIX))
                {
                    processPropertyPersist(nodeRef, propDefs, fieldData, propsToPersist);
                }
                else if (fieldName.startsWith(ASSOC_PREFIX))
                {
                    processAssociationPersist(nodeRef, assocDefs, childAssocDefs, fieldData, assocsToPersist);
                }
                else if (logger.isWarnEnabled())
                {
                    logger.warn("Ignoring unrecognised field '" + fieldName + "'");
                }
            }
        }
        
        // persist the properties using addProperties as this changes the repo values of 
        // those properties included in the Map, but leaves any other property values unchanged,
        // whereas setProperties causes the deletion of properties that are not included in the Map.
        this.nodeService.addProperties(nodeRef, propsToPersist);
        
        for (AbstractAssocCommand cmd : assocsToPersist)
        {
            //TODO If there is an attempt to add and remove the same assoc in one request,
            //     we could drop each request and do nothing.
            cmd.updateAssociations(nodeService);
        }
    }
    
    /**
     * Processes the given field data for persistence as a property.
     * 
     * @param nodeRef The NodeRef to persist the properties on
     * @param propDefs Map of PropertyDefinition's for the node being persisted
     * @param fieldData Data to persist for the property
     * @param propsToPersist Map of properties to be persisted
     */
    protected void processPropertyPersist(NodeRef nodeRef, Map<QName, PropertyDefinition> propDefs,
                FieldData fieldData, Map<QName, Serializable> propsToPersist)
    {
        if (logger.isDebugEnabled())
            logger.debug("Processing field " + fieldData + " for property persistence");
        
        // match and extract the prefix and name parts
        Matcher m = this.propertyNamePattern.matcher(fieldData.getName());
        if (m.matches())
        {
            String qNamePrefix = m.group(1);
            String localName = m.group(2);
            QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);
        
            // ensure that the property being persisted is defined in the model
            PropertyDefinition propDef = propDefs.get(fullQName);
            if (propDef != null)
            {                
                // look for properties that have well known handling requirements
                if (fullQName.equals(ContentModel.PROP_NAME))
                {
                    processNamePropertyPersist(nodeRef, fieldData);
                }
                else if (fullQName.equals(ContentModel.PROP_TITLE))
                {
                    processTitlePropertyPersist(nodeRef, fieldData, propsToPersist);
                }
                else if (fullQName.equals(ContentModel.PROP_DESCRIPTION))
                {
                    processDescriptionPropertyPersist(nodeRef, fieldData, propsToPersist);
                }
                else if (fullQName.equals(ContentModel.PROP_AUTHOR))
                {
                    processAuthorPropertyPersist(nodeRef, fieldData, propsToPersist);
                }
                else
                {
                    Object value = fieldData.getValue();
                    
                    // before persisting check data type of property, if it's numerical
                    // or a date ensure empty strings are changed to null and convert
                    // locale strings to locale objects
                    if ((value instanceof String) && ((String)value).length() == 0)
                    {
                       if (propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE) || 
                           propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT) ||
                           propDef.getDataType().getName().equals(DataTypeDefinition.INT) || 
                           propDef.getDataType().getName().equals(DataTypeDefinition.LONG) ||
                           propDef.getDataType().getName().equals(DataTypeDefinition.DATE) ||
                           propDef.getDataType().getName().equals(DataTypeDefinition.DATETIME))
                       {
                           value = null;
                       }
                    }
                    else if (propDef.isMultiValued())
                    {
                        // currently we expect comma separated lists to represent
                        // the values of a multi valued property, change into List
                        StringTokenizer tokenizer = new StringTokenizer((String)value, ",");
                        List<String> list = new ArrayList<String>(8);
                        while (tokenizer.hasMoreTokens())
                        {
                            list.add(tokenizer.nextToken());
                        }
                        
                        // persist the List
                        value = list;
                    }
                    else if (propDef.getDataType().getName().equals(DataTypeDefinition.LOCALE))
                    {
                        value = I18NUtil.parseLocale((String)value);
                    }
                    
                    // add the property to the map
                    propsToPersist.put(fullQName, (Serializable)value);
                }
            }
            else if (logger.isWarnEnabled())
            {
                logger.warn("Ignoring field '" + fieldData.getName() + "' as a property definition can not be found");
            }
        }
        else
        {
            // the field is potentially a well know transient property
            // check for the ones we know about, anything else is ignored
            Matcher tppm = this.transientPropertyPattern.matcher(fieldData.getName());
            if (tppm.matches())
            {
                String fieldName = tppm.group(1);
                
                if (fieldName.equals(TRANSIENT_MIMETYPE))
                {
                    processMimetypePropertyPersist(nodeRef, fieldData, propsToPersist);
                }
                else if (fieldName.equals(TRANSIENT_ENCODING))
                {
                    processEncodingPropertyPersist(nodeRef, fieldData, propsToPersist);
                }
                else if (fieldName.equals(TRANSIENT_SIZE))
                {
                    // the size property is well known but should never be persisted
                    // as it is calculated so this is intentionally ignored
                }
                else if (logger.isWarnEnabled())
                {
                    logger.warn("Ignoring unrecognised field '" + fieldData.getName() + "'");
                }
            }
            else if (logger.isWarnEnabled())
            {
                logger.warn("Ignoring unrecognised field '" + fieldData.getName() + "'");
            }
        }
    }
    
    /**
     * Processes the given field data for persistence as an association.
     * 
     * @param nodeRef The NodeRef to persist the associations on
     * @param fieldData Data to persist for the associations
     * @param assocCommands List of associations to be persisted
     */
    protected void processAssociationPersist(NodeRef nodeRef, Map<QName, AssociationDefinition> assocDefs,
            Map<QName, ChildAssociationDefinition> childAssocDefs,
            FieldData fieldData, List<AbstractAssocCommand> assocCommands)
    {
        if (logger.isDebugEnabled())
            logger.debug("Processing field " + fieldData + " for association persistence");
        
        String fieldName = fieldData.getName();
        Matcher m = this.associationNamePattern.matcher(fieldName);
        if (m.matches())
        {
            String qNamePrefix = m.group(1);
            String localName = m.group(2);
            String assocSuffix = m.group(3);
            
            QName fullQNameFromJSON = QName.createQName(qNamePrefix, localName, namespaceService);
        
            // ensure that the association being persisted is defined in the model
            AssociationDefinition assocDef = assocDefs.get(fullQNameFromJSON);
            
            if (assocDef == null)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Definition for association " + fullQNameFromJSON + " not recognised and not persisted.");
                }
                return;
            }

            String value = (String)fieldData.getValue();
            String[] nodeRefs = value.split(",");
            
            // Each element in this array will be a new target node in association
            // with the current node.
            for (String nextTargetNode : nodeRefs)
            {
                if (NodeRef.isNodeRef(nextTargetNode))
                {
                    if (assocSuffix.equals(ASSOC_ADD_SUFFIX))
                    {
                        if (assocDef.isChild())
                        {
                            assocCommands.add(new AddChildAssocCommand(nodeRef, new NodeRef(nextTargetNode),
                                    fullQNameFromJSON));
                        }
                        else
                        {
                            assocCommands.add(new AddAssocCommand(nodeRef, new NodeRef(nextTargetNode),
                                    fullQNameFromJSON));
                        }
                    }
                    else if (assocSuffix.equals(ASSOC_REMOVE_SUFFIX))
                    {
                        if (assocDef.isChild())
                        {
                            assocCommands.add(new RemoveChildAssocCommand(nodeRef, new NodeRef(nextTargetNode),
                                    fullQNameFromJSON));
                        }
                        else
                        {
                            assocCommands.add(new RemoveAssocCommand(nodeRef, new NodeRef(nextTargetNode),
                                    fullQNameFromJSON));
                        }
                    }
                    else
                    {
                        if (logger.isWarnEnabled())
                        {
                            StringBuilder msg = new StringBuilder();
                            msg.append("fieldName ")
                                .append(fieldName)
                                .append(" does not have one of the expected suffixes [")
                                .append(ASSOC_ADD_SUFFIX)
                                .append(", ")
                                .append(ASSOC_REMOVE_SUFFIX)
                                .append("] and has been ignored.");
                            logger.warn(msg.toString());
                        }
                    }
                }
                else
                {
                    if (logger.isWarnEnabled())
                    {
                        StringBuilder msg = new StringBuilder();
                        msg.append("targetNode ")
                            .append(nextTargetNode)
                            .append(" is not a valid NodeRef and has been ignored.");
                        logger.warn(msg.toString());
                    }
                }
            }
        }
    }

    /**
     * Persists the given field data as the name property
     *  
     * @param nodeRef The NodeRef to update the name for
     * @param fieldData The data representing the new name value
     */
    protected void processNamePropertyPersist(NodeRef nodeRef, FieldData fieldData)
    {
        try
        {
            // if the name property changes the rename method of the file folder
            // service should be called rather than updating the property directly
            this.fileFolderService.rename(nodeRef, (String)fieldData.getValue());
        }
        catch (FileExistsException fee)
        {
            throw new FormException("Failed to persist field '" + fieldData.getName() + "'", fee);
        }
        catch (FileNotFoundException fnne)
        {
            throw new FormException("Failed to persist field '" + fieldData.getName() + "'", fnne);
        }
    }
    
    /**
     * Persists the given field data as the title property
     *  
     * @param nodeRef The NodeRef to update the title for
     * @param fieldData The data representing the new title value
     * @param propsToPersist Map of properties to be persisted
     */
    protected void processTitlePropertyPersist(NodeRef nodeRef, FieldData fieldData,
                Map<QName, Serializable> propsToPersist)
    {
        // if a title property is present ensure the 'titled' aspect is applied
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
        {
           this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
        }
        
        propsToPersist.put(ContentModel.PROP_TITLE, (String)fieldData.getValue());
    }
    
    /**
     * Persists the given field data as the description property
     *  
     * @param nodeRef The NodeRef to update the description for
     * @param fieldData The data representing the new description value
     * @param propsToPersist Map of properties to be persisted
     */
    protected void processDescriptionPropertyPersist(NodeRef nodeRef, FieldData fieldData,
                Map<QName, Serializable> propsToPersist)
    {
        // if a description property is present ensure the 'titled' aspect is applied
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false)
        {
           this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
        }
        
        propsToPersist.put(ContentModel.PROP_DESCRIPTION, (String)fieldData.getValue());
    }
    
    /**
     * Persists the given field data as the author property
     *  
     * @param nodeRef The NodeRef to update the author for
     * @param fieldData The data representing the new author value
     * @param propsToPersist Map of properties to be persisted
     */
    protected void processAuthorPropertyPersist(NodeRef nodeRef, FieldData fieldData,
                Map<QName, Serializable> propsToPersist)
    {
        // if an author property is present ensure the 'author' aspect is applied
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUTHOR) == false)
        {
           this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, null);
        }
        
        propsToPersist.put(ContentModel.PROP_AUTHOR, (String)fieldData.getValue());
    }
    
    /**
     * Persists the given field data as the mimetype property
     *  
     * @param nodeRef The NodeRef to update the mimetype for
     * @param fieldData The data representing the new mimetype value
     * @param propsToPersist Map of properties to be persisted
     */
    protected void processMimetypePropertyPersist(NodeRef nodeRef, FieldData fieldData,
                Map<QName, Serializable> propsToPersist)
    {
        ContentData contentData = (ContentData)propsToPersist.get(ContentModel.PROP_CONTENT);
        if (contentData == null)
        {
            // content data has not been persisted yet so get it from the node
            contentData = (ContentData)this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        }
        
        if (contentData != null)
        {
            // update content data if we found the property
            contentData = ContentData.setMimetype(contentData, (String)fieldData.getValue());
            propsToPersist.put(ContentModel.PROP_CONTENT, contentData);
        }
    }
    
    /**
     * Persists the given field data as the encoding property
     *  
     * @param nodeRef The NodeRef to update the encoding for
     * @param fieldData The data representing the new encoding value
     * @param propsToPersist Map of properties to be persisted
     */
    protected void processEncodingPropertyPersist(NodeRef nodeRef, FieldData fieldData,
                Map<QName, Serializable> propsToPersist)
    {
        ContentData contentData = (ContentData)propsToPersist.get(ContentModel.PROP_CONTENT);
        if (contentData == null)
        {
            // content data has not been persisted yet so get it from the node
            contentData = (ContentData)this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        }
        
        if (contentData != null)
        {
            // update content data if we found the property
            contentData = ContentData.setEncoding(contentData, (String)fieldData.getValue());
            propsToPersist.put(ContentModel.PROP_CONTENT, contentData);
        }
    }
    
    /**
     * Returns a comma separated list string of the given list object.
     * 
     * @param list The List of values to convert
     * @return A comma separated list of values
     */
    @SuppressWarnings("unchecked")
    protected String makeListString(List list)
    {
        StringBuilder builder = new StringBuilder();
        for (int x = 0; x < list.size(); x++)
        {
            if (x > 0)
            {
                builder.append(",");
            }
            
            builder.append(list.get(x));
        }
        
        return builder.toString();
    }
}

/**
 * This class represents a request to update the value of a node association.
 * 
 * @author Neil McErlean
 */
abstract class AbstractAssocCommand
{
    protected final NodeRef sourceNodeRef;
    protected final NodeRef targetNodeRef;
    protected final QName assocQName;
    
    public AbstractAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef,
            QName assocQName)
    {
        this.sourceNodeRef = sourceNodeRef;
        this.targetNodeRef = targetNodeRef;
        this.assocQName = assocQName;
    }
    
    /**
     * This method should use the specified nodeService reference to effect the
     * update to the supplied associations.
     * @param nodeService
     */
    protected abstract void updateAssociations(NodeService nodeService);
}

/**
 * A class representing a request to add a new association between two nodes.
 * 
 * @author Neil McErlean
 */
class AddAssocCommand extends AbstractAssocCommand
{
    private static final Log logger = LogFactory.getLog(AddAssocCommand.class);
    public AddAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef,
            QName assocQName)
    {
        super(sourceNodeRef, targetNodeRef, assocQName);
    }

    @Override
    protected void updateAssociations(NodeService nodeService)
    {
        List<AssociationRef> existingAssocs = nodeService.getTargetAssocs(sourceNodeRef, assocQName);
        for (AssociationRef assoc : existingAssocs)
        {
            if (assoc.getTargetRef().equals(targetNodeRef))
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Attempt to add existing association prevented. " + assoc);
                }
                return;
            }
        }
        nodeService.createAssociation(sourceNodeRef, targetNodeRef, assocQName);
    }
}

/**
 * A class representing a request to remove an association between two nodes.
 * 
 * @author Neil McErlean
 */
class RemoveAssocCommand extends AbstractAssocCommand
{
    private static final Log logger = LogFactory.getLog(RemoveAssocCommand.class);
    public RemoveAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef,
            QName assocQName)
    {
        super(sourceNodeRef, targetNodeRef, assocQName);
    }
    
    @Override
    protected void updateAssociations(NodeService nodeService)
    {
        List<AssociationRef> existingAssocs = nodeService.getTargetAssocs(sourceNodeRef, assocQName);
        boolean assocDoesNotExist = true;
        for (AssociationRef assoc : existingAssocs)
        {
            if (assoc.getTargetRef().equals(targetNodeRef))
            {
                assocDoesNotExist = false;
                break;
            }
        }
        if (assocDoesNotExist)
        {
            if (logger.isWarnEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Attempt to remove non-existent association prevented. ")
                    .append(sourceNodeRef)
                    .append("|")
                    .append(targetNodeRef)
                .append(assocQName);
                logger.warn(msg.toString());
            }
            return;
        }

        nodeService.removeAssociation(sourceNodeRef, targetNodeRef, assocQName);
    }
}

/**
 * A class representing a request to add a new child association between two nodes.
 * 
 * @author Neil McErlean
 */
class AddChildAssocCommand extends AbstractAssocCommand
{
    private static final Log logger = LogFactory.getLog(AddChildAssocCommand.class);
    public AddChildAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef,
            QName assocQName)
    {
        super(sourceNodeRef, targetNodeRef, assocQName);
    }

    @Override
    protected void updateAssociations(NodeService nodeService)
    {
        List<ChildAssociationRef> existingChildren = nodeService.getChildAssocs(sourceNodeRef);

        for (ChildAssociationRef assoc : existingChildren)
        {
            if (assoc.getChildRef().equals(targetNodeRef))
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Attempt to add existing child association prevented. " + assoc);
                }
                return;
            }
        }
        // We are following the behaviour of the JSF client here in using the same
        // QName value for the 3rd and 4th parameters in the below call.
        nodeService.addChild(sourceNodeRef, targetNodeRef, assocQName, assocQName);
    }
}

/**
 * A class representing a request to remove a child association between two nodes.
 * 
 * @author Neil McErlean
 */
class RemoveChildAssocCommand extends AbstractAssocCommand
{
    private static final Log logger = LogFactory.getLog(RemoveChildAssocCommand.class);
    public RemoveChildAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef,
            QName assocQName)
    {
        super(sourceNodeRef, targetNodeRef, assocQName);
    }
    
    @Override
    protected void updateAssociations(NodeService nodeService)
    {
        List<ChildAssociationRef> existingChildren = nodeService.getChildAssocs(sourceNodeRef);
        boolean childAssocDoesNotExist = true;
        for (ChildAssociationRef assoc : existingChildren)
        {
            if (assoc.getChildRef().equals(targetNodeRef))
            {
                childAssocDoesNotExist = false;
                break;
            }
        }
        if (childAssocDoesNotExist)
        {
            if (logger.isWarnEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Attempt to remove non-existent child association prevented. ")
                    .append(sourceNodeRef)
                    .append("|")
                    .append(targetNodeRef)
                .append(assocQName);
                logger.warn(msg.toString());
            }
            return;
        }

        nodeService.removeChild(sourceNodeRef, targetNodeRef);
    }
}