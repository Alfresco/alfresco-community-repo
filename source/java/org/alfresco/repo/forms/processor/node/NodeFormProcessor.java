/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.forms.processor.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.PropertyFieldDefinition.FieldConstraint;
import org.alfresco.repo.forms.processor.FilteredFormProcessor;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
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
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * FormProcessor implementation that can generate and persist Form objects
 * for repository nodes.
 *
 * @author Gavin Cornwell
 */
public class NodeFormProcessor extends FilteredFormProcessor
{
    /** Logger */
    private static Log logger = LogFactory.getLog(NodeFormProcessor.class);
    
    protected static final String MSG_MIMETYPE_LABEL = "form_service.mimetype.label";
    protected static final String MSG_MIMETYPE_DESC = "form_service.mimetype.description";
    protected static final String MSG_ENCODING_LABEL = "form_service.encoding.label";
    protected static final String MSG_ENCODING_DESC = "form_service.encoding.description";
    protected static final String MSG_SIZE_LABEL = "form_service.size.label";
    protected static final String MSG_SIZE_DESC = "form_service.size.description";
    
    /** Public constants */
    public static final String ON = "on";
    public static final String PROP = "prop";
    public static final String ASSOC = "assoc";
    public static final String DATA_KEY_SEPARATOR = "_";
    public static final String PROP_DATA_PREFIX = PROP + DATA_KEY_SEPARATOR;
    public static final String ASSOC_DATA_PREFIX = ASSOC + DATA_KEY_SEPARATOR;
    public static final String ASSOC_DATA_ADDED_SUFFIX = DATA_KEY_SEPARATOR + "added";
    public static final String ASSOC_DATA_REMOVED_SUFFIX = DATA_KEY_SEPARATOR + "removed";
    
    public static final String TRANSIENT_MIMETYPE = "mimetype";
    public static final String TRANSIENT_SIZE = "size";
    public static final String TRANSIENT_ENCODING = "encoding";
    
    /** Services */
    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;

    /**
     * A regular expression which can be used to match property names.
     * These names will look like <code>"prop_cm_name"</code>.
     * The pattern can also be used to extract the "cm" and the "name" parts.
     */
    protected Pattern propertyNamePattern = Pattern.compile(PROP_DATA_PREFIX + "(.*){1}?_(.*){1}?");
    
    /**
     * A regular expression which can be used to match tranisent property names.
     * These names will look like <code>"prop_name"</code>.
     * The pattern can also be used to extract the "name" part.
     */
    protected Pattern transientPropertyPattern = Pattern.compile(PROP_DATA_PREFIX + "(.*){1}?");
    
    /**
     * A regular expression which can be used to match association names.
     * These names will look like <code>"assoc_cm_references_added"</code>.
     * The pattern can also be used to extract the "cm", the "name" and the suffix parts.
     */
    protected Pattern associationNamePattern = Pattern.compile(ASSOC_DATA_PREFIX + "(.*){1}?_(.*){1}?(_[a-zA-Z]+)");
    
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
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getTypedItem(org.alfresco.repo.forms.Item)
     */
    @Override
    protected Object getTypedItem(Item item)
    {
        // create NodeRef representation, the id could already be in a valid
        // NodeRef format or it may be in a URL friendly format
        NodeRef nodeRef = null;
        if (NodeRef.isNodeRef(item.getId()))
        {
            nodeRef = new NodeRef(item.getId());
        }
        else
        {
            // split the string into the 3 required parts
            String[] parts = item.getId().split("/");
            if (parts.length == 3)
            {
                try
                {
                    nodeRef = new NodeRef(parts[0], parts[1], parts[2]);
                }
                catch (IllegalArgumentException iae)
                {
                    // ignored for now, dealt with below
                    
                    if (logger.isDebugEnabled())
                        logger.debug("NodeRef creation failed for: " + item.getId(), iae);
                }
            }
        } 
        
        // check we have a valid node ref
        if (nodeRef == null)
        {
            throw new FormNotFoundException(item, 
                        new IllegalArgumentException(item.getId()));
        }
        
        // check the node itself exists
        if (this.nodeService.exists(nodeRef) == false)
        {
            throw new FormNotFoundException(item, 
                        new InvalidNodeRefException("Node does not exist: " + nodeRef, nodeRef));
        }
        else
        {
            // all Node based filters can expect to get a NodeRef
            return nodeRef;
        }
    }

    /*
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#internalGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form)
     */
    @Override
    protected void internalGenerate(Object item, List<String> fields, List<String> forcedFields, Form form)
    {
        if (logger.isDebugEnabled())
            logger.debug("Generating form for: " + item);
        
        // cast to the expected NodeRef representation
        NodeRef nodeRef = (NodeRef)item;
        
        // generate the form for the node
        generateNode(nodeRef, fields, forcedFields, form);
        
        if (logger.isDebugEnabled())
            logger.debug("Generated form: " + form);
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
        // set the type and URL of the item
        QName type = this.nodeService.getType(nodeRef);
        form.getItem().setType(type.toPrefixString(this.namespaceService));
        StringBuilder builder = new StringBuilder("/api/node/");
        builder.append(nodeRef.getStoreRef().getProtocol()).append("/");
        builder.append(nodeRef.getStoreRef().getIdentifier()).append("/");
        builder.append(nodeRef.getId());
        form.getItem().setUrl(builder.toString());
        
        if (fields != null && fields.size() > 0)
        {
            generateSelectedFields(nodeRef, null, fields, forcedFields, form);
        }
        else
        {
            // setup field definitions and data
            generateAllPropertyFields(nodeRef, form);
            generateAllAssociationFields(nodeRef, form);
            generateTransientFields(nodeRef, form);
        }
    }
    
    /**
     * Sets up the field definitions for all the requested fields.
     * <p>
     * A NodeRef or TypeDefinition can be provided, however, if a NodeRef 
     * is provided all type information will be derived from the NodeRef
     * and the TypeDefinition will be ignored.
     * </p><p>
     * If any of the requested fields are not present on the type and 
     * they appear in the forcedFields list an attempt to find a model
     * definition for those fields is made so they can be included.
     * </p>
     * 
     * @param nodeRef The NodeRef of the item being generated
     * @param typeDef The TypeDefiniton of the item being generated
     * @param fields Restricted list of fields to include
     * @param forcedFields List of field names that should be included
     *                     even if the field is not currently present
     * @param form The Form instance to populate
     */
    protected void generateSelectedFields(NodeRef nodeRef, TypeDefinition typeDef, 
                List<String> fields, List<String> forcedFields, Form form)
    {
        // ensure a NodeRef or TypeDefinition is provided
        if (nodeRef == null && typeDef == null)
        {
            throw new IllegalArgumentException("A NodeRef or TypeDefinition must be provided");
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Generating selected fields: " + fields + " and forcing: " + forcedFields);
        
        // get data dictionary definition for node if it is provided
        QName type = null;
        Map<QName, Serializable> propValues = Collections.emptyMap();
        Map<QName, PropertyDefinition> propDefs = null;
        Map<QName, AssociationDefinition> assocDefs = null;
        
        if (nodeRef != null)
        {
            type = this.nodeService.getType(nodeRef);
            typeDef = this.dictionaryService.getAnonymousType(type, this.nodeService.getAspects(nodeRef));
            
            // NOTE: the anonymous type returns all property and association defs 
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
                    // if there are 3 parts to the field name the first one represents
                    // whether the field is a property or association i.e. prop:prefix:local
                    // or assoc:prefix:local, determine the prefix and ensure it's valid
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
                        if (logger.isWarnEnabled())
                            logger.warn("\"" + parts[0] + "\" is an invalid prefix for requesting a property or association");

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
                        generatePropertyField(propDef, propValues.get(fullQName), form);
                        
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
                        generateAssociationField(assocDef, 
                                    (nodeRef != null) ? retrieveAssociationValues(nodeRef, assocDef) : null, 
                                    form);
                        
                        foundField = true;
                    }
                }
                
                // still not found the field, is it a force'd field?
                if (!foundField)
                {
                    if (forcedFields != null && forcedFields.size() > 0 && 
                        forcedFields.contains(fieldName))
                    {
                        generateForcedField(fieldName, form);
                    }
                    else if (logger.isDebugEnabled())
                    {
                        logger.debug("Ignoring field \"" + fieldName + 
                                    "\" as it is not defined for the current " + ((nodeRef != null) ? "node" : "type") + 
                                    " and it does not appear in the 'force' list");
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
                    if (nodeRef != null && this.dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
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
     * to show.
     * 
     * @param fieldName Name of the field to force
     * @param form The Form instance to populated
     */
    protected void generateForcedField(String fieldName, Form form)
    {
        if (logger.isDebugEnabled())
            logger.debug("Attempting to force the inclusion of field \"" + fieldName + "\"");
        
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
                // if there are 3 parts to the field name the first one represents
                // whether the field is a property or association i.e. prop:prefix:local
                // or assoc:prefix:local, determine the prefix and ensure it's valid
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
                    if (logger.isWarnEnabled())
                        logger.warn("\"" + parts[0] + "\" is an invalid prefix for requesting a property or association");

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
                    generatePropertyField(propDef, null, form);
                    
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
                    generateAssociationField(assocDef, null, form);
                    
                    foundField = true;
                }
            }
            
            if (!foundField && logger.isDebugEnabled())
            {
                logger.debug("Ignoring field \"" + fieldName + 
                            "\" as it is not defined for the current node and can not be found in any model");
            }
        }
        else if (logger.isWarnEnabled())
        {
            logger.warn("Ignoring unrecognised field \"" + fieldName + "\"");
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
        String[] nameParts = QName.splitPrefixedQName(propName);
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
        
        // any property from the system model (sys prefix) should be protected
        // the model doesn't currently enforce this so make sure they are not editable
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
            List<FieldConstraint> fieldConstraints = 
                new ArrayList<FieldConstraint>(constraints.size());
            
            for (ConstraintDefinition constraintDef : constraints)
            {
                Constraint constraint = constraintDef.getConstraint();
                FieldConstraint fieldConstraint = fieldDef.new FieldConstraint(
                            constraint.getType(), constraint.getParameters());
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
            
            form.addData(dataKeyName, propValue);
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
        String dataKeyName = PROP_DATA_PREFIX + TRANSIENT_MIMETYPE;
        PropertyFieldDefinition mimetypeField = new PropertyFieldDefinition(
                    TRANSIENT_MIMETYPE, DataTypeDefinition.TEXT.toPrefixString(
                    this.namespaceService));
        mimetypeField.setLabel(I18NUtil.getMessage(MSG_MIMETYPE_LABEL));
        mimetypeField.setDescription(I18NUtil.getMessage(MSG_MIMETYPE_DESC));
        mimetypeField.setDataKeyName(dataKeyName);
        form.addFieldDefinition(mimetypeField);
        form.addData(dataKeyName, content.getMimetype());
    }
    
    /**
     * Generates the field definition for the transient encoding property
     * 
     * @param content The ContentData object to generate the field from
     * @param form The Form instance to populate
     */
    protected void generateEncodingPropertyField(ContentData content, Form form)
    {
        String dataKeyName = PROP_DATA_PREFIX + TRANSIENT_ENCODING;
        PropertyFieldDefinition encodingField = new PropertyFieldDefinition(
                    TRANSIENT_ENCODING, DataTypeDefinition.TEXT.toPrefixString(
                    this.namespaceService));
        encodingField.setLabel(I18NUtil.getMessage(MSG_ENCODING_LABEL));
        encodingField.setDescription(I18NUtil.getMessage(MSG_ENCODING_DESC));
        encodingField.setDataKeyName(dataKeyName);
        form.addFieldDefinition(encodingField);
        form.addData(dataKeyName, content.getEncoding());
    }
    
    /**
     * Generates the field definition for the transient size property
     * 
     * @param content The ContentData object to generate the field from
     * @param form The Form instance to populate
     */
    protected void generateSizePropertyField(ContentData content, Form form)
    {
        String dataKeyName = PROP_DATA_PREFIX + TRANSIENT_SIZE;
        PropertyFieldDefinition sizeField = new PropertyFieldDefinition(
                    TRANSIENT_SIZE, DataTypeDefinition.LONG.toPrefixString(
                    this.namespaceService));
        sizeField.setLabel(I18NUtil.getMessage(MSG_SIZE_LABEL));
        sizeField.setDescription(I18NUtil.getMessage(MSG_SIZE_DESC));
        sizeField.setDataKeyName(dataKeyName);
        sizeField.setProtectedField(true);
        form.addFieldDefinition(sizeField);
        form.addData(dataKeyName, new Long(content.getSize()));
    }
    
    /**
     * Sets up the field definitions for all the node's associations.
     * 
     * @param nodeRef The NodeRef of the node being setup
     * @param form The Form instance to populate
     */
    protected void generateAllAssociationFields(NodeRef nodeRef, Form form)
    {
        // get data dictionary definition for the node
        QName type = this.nodeService.getType(nodeRef);
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(type,
                    this.nodeService.getAspects(nodeRef));
        
        // iterate round the association defintions and setup field definition
        Map<QName, AssociationDefinition> assocDefs = typeDef.getAssociations();
        for (AssociationDefinition assocDef : assocDefs.values())
        {
            this.generateAssociationField(assocDef, 
                        retrieveAssociationValues(nodeRef, assocDef), form);
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
        String[] nameParts = QName.splitPrefixedQName(assocName);
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
        
        // define the data key name and set
        String dataKeyName = ASSOC_DATA_PREFIX + nameParts[0] + DATA_KEY_SEPARATOR + nameParts[1];
        fieldDef.setDataKeyName(dataKeyName);
        
        // add definition to the form
        form.addFieldDefinition(fieldDef);
        
        if (assocValues != null)
        {
            // add the association value to the form
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
            form.addData(dataKeyName, values);
        }
    }
    
    /**
     * Retrieves the values of the given association definition on the given node.
     * 
     * @param nodeRef The node to get the association values for
     * @param assocDef The association definition to look for values for
     * @return List of values for association or null of the association does
     *         not exist for the given node.
     */
    @SuppressWarnings("unchecked")
    protected List retrieveAssociationValues(NodeRef nodeRef, AssociationDefinition assocDef)
    {
        List assocValues = null;
        
        // get the list of values (if any) for the association
        if (assocDef instanceof ChildAssociationDefinition)
        {
            assocValues = this.nodeService.getChildAssocs(nodeRef, assocDef.getName(), 
                        RegexQNamePattern.MATCH_ALL);
        }
        else
        {
            assocValues = this.nodeService.getTargetAssocs(nodeRef, assocDef.getName());
        }
        
        return assocValues;
    }

    /*
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#internalPersist(java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected Object internalPersist(Object item, FormData data)
    {
        if (logger.isDebugEnabled())
            logger.debug("Persisting form for: " + item);
        
        // cast to the expected NodeRef representation
        NodeRef nodeRef = (NodeRef)item;
        
        // persist the node
        persistNode(nodeRef, data);
        
        return item;
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
        
        Map<QName, Serializable> propsToPersist = new HashMap<QName, Serializable>(data.getNumberOfFields());
        List<AbstractAssocCommand> assocsToPersist = new ArrayList<AbstractAssocCommand>();
        
        for (FieldData fieldData : data)
        {
            // NOTE: ignore file fields for now, not supported yet!
            if (fieldData.isFile() == false)
            {
                String fieldName = fieldData.getName();
                
                if (fieldName.startsWith(PROP_DATA_PREFIX))
                {
                    processPropertyPersist(nodeRef, propDefs, fieldData, propsToPersist);
                }
                else if (fieldName.startsWith(ASSOC_DATA_PREFIX))
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
            
            // if the property is not defined on the node, check for the property in all models
            if (propDef == null)
            {
                propDef = this.dictionaryService.getProperty(fullQName);
            }
            
            // if we have a property definition attempt the persist
            if (propDef != null)
            {                
                // look for properties that have well known handling requirements
                if (fullQName.equals(ContentModel.PROP_NAME))
                {
                    processNamePropertyPersist(nodeRef, fieldData);
                }
                else
                {
                    Object value = fieldData.getValue();
                    
                    // before persisting check data type of property
                    if (propDef.isMultiValued())
                    {
                        // depending on client the value could be a comma separated
                        // string, a List object or a JSONArray object
                        if (value instanceof String)
                        {
                            if (((String)value).length() == 0)
                            {
                                // empty string for multi-valued properties
                                // should be stored as null
                                value = null;
                            }
                            else
                            {
                                // if value is a String convert to List of String
                                StringTokenizer tokenizer = new StringTokenizer((String)value, ",");
                                List<String> list = new ArrayList<String>(8);
                                while (tokenizer.hasMoreTokens())
                                {
                                    list.add(tokenizer.nextToken());
                                }
                                
                                // persist the List
                                value = list;
                            }
                        }
                        else if (value instanceof JSONArray)
                        {
                            // if value is a JSONArray convert to List of Object
                            JSONArray jsonArr = (JSONArray)value;
                            int arrLength = jsonArr.length();
                            List<Object> list = new ArrayList<Object>(arrLength);
                            try
                            {
                                for (int x = 0; x < arrLength; x++)
                                {
                                    list.add(jsonArr.get(x));
                                }
                            }
                            catch (JSONException je)
                            {
                                throw new FormException("Failed to convert JSONArray to List", je);
                            }
                            
                            // persist the list
                            value = list;
                        }
                    }
                    else if (propDef.getDataType().getName().equals(DataTypeDefinition.BOOLEAN))
                    {
                        // check for browser representation of true, that being "on"
                        if (value instanceof String && ON.equals(value))
                        {
                            value = Boolean.TRUE;
                        }
                    }
                    else if (propDef.getDataType().getName().equals(DataTypeDefinition.LOCALE))
                    {
                        value = I18NUtil.parseLocale((String)value);
                    }
                    else if ((value instanceof String) && ((String)value).length() == 0)
                    {
                        // make sure empty strings stay as empty strings, everything else
                        // should be represented as null
                        if (!propDef.getDataType().getName().equals(DataTypeDefinition.TEXT) && 
                            !propDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
                        {
                            value = null;
                        }
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
            
            QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);
        
            // ensure that the association being persisted is defined in the model
            AssociationDefinition assocDef = assocDefs.get(fullQName);
            
            // TODO: if the association is not defined on the node, check for the association 
            //       in all models, however, the source of an association can be critical so we
            //       can't just look up the association in the model regardless. We need to
            //       either check the source class of the node and the assoc def match or we
            //       check that the association was defined as part of an aspect (where by it's
            //       nature can have any source type)
            
            if (assocDef == null)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Definition for association " + fullQName + " not recognised and not persisted.");
                }
                return;
            }

            String value = (String)fieldData.getValue();
            String[] nodeRefs = value.split(",");
            
            // Each element in this array will be a new target node in association
            // with the current node.
            for (String nextTargetNode : nodeRefs)
            {
                if (nextTargetNode.length() > 0)
                {
                    if (NodeRef.isNodeRef(nextTargetNode))
                    {
                        if (assocSuffix.equals(ASSOC_DATA_ADDED_SUFFIX))
                        {
                            if (assocDef.isChild())
                            {
                                assocCommands.add(new AddChildAssocCommand(nodeRef, new NodeRef(nextTargetNode),
                                        fullQName));
                            }
                            else
                            {
                                assocCommands.add(new AddAssocCommand(nodeRef, new NodeRef(nextTargetNode),
                                        fullQName));
                            }
                        }
                        else if (assocSuffix.equals(ASSOC_DATA_REMOVED_SUFFIX))
                        {
                            if (assocDef.isChild())
                            {
                                assocCommands.add(new RemoveChildAssocCommand(nodeRef, new NodeRef(nextTargetNode),
                                        fullQName));
                            }
                            else
                            {
                                assocCommands.add(new RemoveAssocCommand(nodeRef, new NodeRef(nextTargetNode),
                                        fullQName));
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
                                    .append(ASSOC_DATA_ADDED_SUFFIX)
                                    .append(", ")
                                    .append(ASSOC_DATA_REMOVED_SUFFIX)
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
