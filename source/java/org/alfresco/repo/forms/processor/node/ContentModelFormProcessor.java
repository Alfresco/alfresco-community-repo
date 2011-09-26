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
package org.alfresco.repo.forms.processor.node;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC_DATA_ADDED_SUFFIX;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC_DATA_PREFIX;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ASSOC_DATA_REMOVED_SUFFIX;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DEFAULT_CONTENT_MIMETYPE;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.ON;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DOT_CHARACTER;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DOT_CHARACTER_REPLACEMENT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.FilteredFormProcessor;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Abstract FormProcessor implementation that provides common functionality for
 * form processors that deal with Alfresco content models i.e. types and nodes.
 * 
 * @author Gavin Cornwell
 * @author Nick Smith
 */
public abstract class ContentModelFormProcessor<ItemType, PersistType> extends
            FilteredFormProcessor<ItemType, PersistType>
{
    /** Services */
    protected NodeService nodeService;

    protected FileFolderService fileFolderService;

    protected DictionaryService dictionaryService;

    protected NamespaceService namespaceService;
    
    protected ContentService contentService;

    /**
     * A regular expression which can be used to match property names. These
     * names will look like <code>"prop_cm_name"</code>. The pattern can also be
     * used to extract the "cm" and the "name" parts.
     */
    protected Pattern propertyNamePattern = Pattern.compile(PROP_DATA_PREFIX + "([a-zA-Z0-9-]+)_(.*)");

    /**
     * A regular expression which can be used to match tranisent property names.
     * These names will look like <code>"prop_name"</code>. The pattern can also
     * be used to extract the "name" part.
     */
    protected Pattern transientPropertyPattern = Pattern.compile(PROP_DATA_PREFIX + "(.*){1}?");

    /**
     * A regular expression which can be used to match association names. These
     * names will look like <code>"assoc_cm_references_added"</code>. The
     * pattern can also be used to extract the "cm", the "name" and the suffix
     * parts.
     */
    protected Pattern associationNamePattern = Pattern.compile(ASSOC_DATA_PREFIX + "([a-zA-Z0-9-]+)_(.*)(_[a-zA-Z]+)");

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
    
    /**
     * Sets the content service
     * 
     * @param contentService The ContentService instance
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    protected void addPropertyDataIfRequired(QName propName, Form form, ContentModelItemData<?> itemData)
    {
        String dataKey = makePropDataKey(propName);
        if (form.dataExists(dataKey) == false)
        {
            PropertyFieldProcessor processor = new PropertyFieldProcessor(namespaceService, dictionaryService);
            Object value = processor.getValue(propName, itemData);
            form.addData(dataKey, value);
        }
    }

    private String makePropDataKey(QName propName)
    {
        String propPrefixName = propName.toPrefixString(namespaceService);
        String dataKey = FormFieldConstants.PROP_DATA_PREFIX + propPrefixName.replace(':', '_');
        return dataKey;
    }

    @Override
    protected List<Field> generateDefaultFields(FormCreationData data, List<String> fieldsToIgnore)
    {
        DefaultFieldBuilder defaultFieldBuilder = 
            new DefaultFieldBuilder(data, fieldProcessorRegistry, namespaceService, fieldsToIgnore);
        return defaultFieldBuilder.buildDefaultFields();
    }

    @Override
    protected ContentModelItemData<ItemType> makeItemData(ItemType item)
    {
        TypeDefinition baseType = getBaseType(item);
        Set<QName> aspects = getAspectNames(item);
        TypeDefinition anonType = dictionaryService.getAnonymousType(baseType.getName(), aspects);
        Map<QName, PropertyDefinition> propDefs = anonType.getProperties();
        Map<QName, AssociationDefinition> assocDefs = anonType.getAssociations();
        Map<QName, Serializable> propValues = getPropertyValues(item);
        Map<QName, Serializable> assocValues = getAssociationValues(item);
        Map<String, Object> transientValues = getTransientValues(item);
        return new ContentModelItemData<ItemType>(item, propDefs, assocDefs, propValues, assocValues, transientValues);
    }
    
    protected List<String> getDefaultIgnoredFields()
    {
        ArrayList<String> fields = new ArrayList<String>(8);
        
        // ignore system properties by default
        fields.add(ContentModel.PROP_NODE_DBID.toPrefixString(this.namespaceService));
        fields.add(ContentModel.PROP_NODE_UUID.toPrefixString(this.namespaceService));
        fields.add(ContentModel.PROP_STORE_IDENTIFIER.toPrefixString(this.namespaceService));
        fields.add(ContentModel.PROP_STORE_PROTOCOL.toPrefixString(this.namespaceService));
        
        // ignore associations that are system maintained
        fields.add(RenditionModel.ASSOC_RENDITION.toPrefixString(this.namespaceService));
        
        return fields;
    }

    protected Set<QName> getAspectNames(ItemType item)
    {
        return getBaseType(item).getDefaultAspectNames();
    }

    protected abstract Map<QName, Serializable> getAssociationValues(ItemType item);

    protected abstract Map<QName, Serializable> getPropertyValues(ItemType item);

    protected abstract Map<String, Object> getTransientValues(ItemType item);

    protected abstract TypeDefinition getBaseType(ItemType item);

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
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(type, this.nodeService.getAspects(nodeRef));
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
                    processPropertyPersist(nodeRef, propDefs, fieldData, propsToPersist, data);
                }
                else if (fieldName.startsWith(ASSOC_DATA_PREFIX))
                {
                    processAssociationPersist(nodeRef, assocDefs, childAssocDefs, fieldData, assocsToPersist);
                }
                else if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Ignoring unrecognised field '" + fieldName + "'");
                }
            }
        }

        // persist the properties using addProperties as this changes the repo
        // values of
        // those properties included in the Map, but leaves any other property
        // values unchanged,
        // whereas setProperties causes the deletion of properties that are not
        // included in the Map.
        this.nodeService.addProperties(nodeRef, propsToPersist);

        for (AbstractAssocCommand cmd : assocsToPersist)
        {
            // TODO If there is an attempt to add and remove the same assoc in
            // one request,
            // we could drop each request and do nothing.
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
     * @param data The FormData to persist
     */
    protected void processPropertyPersist(NodeRef nodeRef, Map<QName, PropertyDefinition> propDefs,
                FieldData fieldData, Map<QName, Serializable> propsToPersist, FormData data)
    {
        if (getLogger().isDebugEnabled())
            getLogger().debug("Processing field " + fieldData + " for property persistence");

        // match and extract the prefix and name parts
        Matcher m = this.propertyNamePattern.matcher(fieldData.getName().replaceAll(DOT_CHARACTER_REPLACEMENT, DOT_CHARACTER));
        if (m.matches())
        {
            String qNamePrefix = m.group(1);
            String localName = m.group(2);
            QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);

            // ensure that the property being persisted is defined in the model
            PropertyDefinition propDef = propDefs.get(fullQName);

            // if the property is not defined on the node, check for the
            // property in all models
            if (propDef == null)
            {
                propDef = this.dictionaryService.getProperty(fullQName);
            }

            // if we have a property definition attempt the persist
            if (propDef != null)
            {
                // look for properties that have well known handling
                // requirements
                if (fullQName.equals(ContentModel.PROP_NAME))
                {
                    processNamePropertyPersist(nodeRef, fieldData, propsToPersist);
                }
                else if (propDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                {
                    processContentPropertyPersist(nodeRef, fieldData, propsToPersist, data);
                }
                else
                {
                    Object value = fieldData.getValue();

                    // before persisting check data type of property
                    if (propDef.isMultiValued())
                    {
                        // depending on client the value could be a comma
                        // separated
                        // string, a List object or a JSONArray object
                        if (value instanceof String)
                        {
                            if (((String) value).length() == 0)
                            {
                                // empty string for multi-valued properties
                                // should be stored as null
                                value = null;
                            }
                            else
                            {
                                // if value is a String convert to List of
                                // String
                                StringTokenizer tokenizer = new StringTokenizer((String) value, ",");
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
                            JSONArray jsonArr = (JSONArray) value;
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
                        value = I18NUtil.parseLocale((String) value);
                    }
                    else if ((value instanceof String) && ((String) value).length() == 0)
                    {
                        // make sure empty strings stay as empty strings,
                        // everything else should be represented as null
                        if (!propDef.getDataType().getName().equals(DataTypeDefinition.TEXT) && 
                            !propDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
                        {
                            value = null;
                        }
                        else
                        {
                            // if the text property has a regex constraint set the empty
                            // string to null otherwise the integrity checker will reject it
                            List<ConstraintDefinition> constraints = propDef.getConstraints();
                            if (constraints != null && constraints.size() > 0)
                            {
                                for (ConstraintDefinition constraintDef : constraints)
                                {
                                    if ("REGEX".equals(constraintDef.getConstraint().getType()))
                                    {
                                        value = null;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    // add the property to the map
                    propsToPersist.put(fullQName, (Serializable) value);
                }
            }
            else if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Ignoring field '" + fieldData.getName() + "' as a property definition can not be found");
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

                if (fieldName.equals(MimetypeFieldProcessor.KEY))
                {
                    processMimetypePropertyPersist(nodeRef, fieldData, propsToPersist);
                }
                else if (fieldName.equals(EncodingFieldProcessor.KEY))
                {
                    processEncodingPropertyPersist(nodeRef, fieldData, propsToPersist);
                }
                else if (fieldName.equals(SizeFieldProcessor.KEY))
                {
                    // the size property is well known but should never be persisted
                    // as it is calculated so this is intentionally ignored
                }
                else if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Ignoring unrecognised field '" + fieldData.getName() + "'");
                }
            }
            else if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Ignoring unrecognised field '" + fieldData.getName() + "'");
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
                Map<QName, ChildAssociationDefinition> childAssocDefs, FieldData fieldData,
                List<AbstractAssocCommand> assocCommands)
    {
        if (getLogger().isDebugEnabled())
            getLogger().debug("Processing field " + fieldData + " for association persistence");

        String fieldName = fieldData.getName();
        Matcher m = this.associationNamePattern.matcher(fieldName.replaceAll(DOT_CHARACTER_REPLACEMENT, DOT_CHARACTER));
        if (m.matches())
        {
            String qNamePrefix = m.group(1);
            String localName = m.group(2);
            String assocSuffix = m.group(3);

            QName fullQName = QName.createQName(qNamePrefix, localName, namespaceService);

            // ensure that the association being persisted is defined in the model
            AssociationDefinition assocDef = assocDefs.get(fullQName);

            // TODO: if the association is not defined on the node, check for the association
            // in all models, however, the source of an association can be critical so we
            // can't just look up the association in the model regardless. We need to
            // either check the source class of the node and the assoc def match or we
            // check that the association was defined as part of an aspect (where by it's
            // nature can have any source type)

            if (assocDef == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Ignoring field '" + fieldName + "' as an association definition can not be found");
                }
                
                return;
            }

            String value = (String) fieldData.getValue();
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
                            if (getLogger().isWarnEnabled())
                            {
                                StringBuilder msg = new StringBuilder();
                                msg.append("Ignoring 'fieldName ").append(fieldName).append(
                                            "' as it does not have one of the expected suffixes (").append(
                                            ASSOC_DATA_ADDED_SUFFIX).append(" or ").append(ASSOC_DATA_REMOVED_SUFFIX)
                                            .append(")");
                                getLogger().warn(msg.toString());
                            }
                        }
                    }
                    else
                    {
                        if (getLogger().isWarnEnabled())
                        {
                            StringBuilder msg = new StringBuilder();
                            msg.append("targetNode ").append(nextTargetNode).append(
                                        " is not a valid NodeRef and has been ignored.");
                            getLogger().warn(msg.toString());
                        }
                    }
                }
            }
        }
        else if (getLogger().isWarnEnabled())
        {
            getLogger().warn("Ignoring unrecognised field '" + fieldName + "'");
        }
    }

    /**
     * Persists the given field data as the name property
     * 
     * @param nodeRef The NodeRef to update the name for
     * @param fieldData The data representing the new name value
     * @param propsToPersist Map of properties to be persisted
     */
    protected void processNamePropertyPersist(NodeRef nodeRef, FieldData fieldData, 
                Map<QName, Serializable> propsToPersist)
    {
        // determine whether the file folder service can handle the current node
        FileInfo fileInfo = this.fileFolderService.getFileInfo(nodeRef);
        if (fileInfo != null)
        {
            try
            {
                // if the name property changes the rename method of the file folder
                // service should be called rather than updating the property directly
                this.fileFolderService.rename(nodeRef, (String) fieldData.getValue());
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
        else
        {
            // as the file folder service can not be used just set the name property,
            // the node service will deal with the details of renaming.
            propsToPersist.put(ContentModel.PROP_NAME, (Serializable)fieldData.getValue());
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
        ContentData contentData = (ContentData) propsToPersist.get(ContentModel.PROP_CONTENT);
        if (contentData == null)
        {
            // content data has not been persisted yet so get it from the node
            contentData = (ContentData) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        }

        if (contentData != null)
        {
            // update content data if we found the property
            contentData = ContentData.setMimetype(contentData, (String) fieldData.getValue());
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
        ContentData contentData = (ContentData) propsToPersist.get(ContentModel.PROP_CONTENT);
        if (contentData == null)
        {
            // content data has not been persisted yet so get it from the node
            contentData = (ContentData) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        }

        if (contentData != null)
        {
            // update content data if we found the property
            contentData = ContentData.setEncoding(contentData, (String) fieldData.getValue());
            propsToPersist.put(ContentModel.PROP_CONTENT, contentData);
        }
    }
    
    /**
     * Persists the given field data as the content
     * 
     * @param nodeRef The NodeRef to update the content for
     * @param fieldData The data representing the new content
     * @param propsToPersist Map of properties to be persisted
     * @param data The form data being persisted
     */
    protected void processContentPropertyPersist(NodeRef nodeRef, FieldData fieldData,
                Map<QName, Serializable> propsToPersist, FormData data)
    {
        ContentWriter writer = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        ContentData contentData = null;
        
        if (writer != null)
        {
            // determine whether there is any content for the node yet i.e. it's a create
            boolean defaultMimetypeRequired = (this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT) == null);
            
            // write the content
            writer.putContent((String)fieldData.getValue());
            
            // if there was no content set a sensible default mimetype if necessary
            if (defaultMimetypeRequired)
            {
                // if the transient mimetype property has already set the mimetype don't do anything
                contentData = (ContentData) propsToPersist.get(ContentModel.PROP_CONTENT);
                if (contentData != null)
                {
                    String mimetype = contentData.getMimetype();
                    if (mimetype == null)
                    {
                        contentData = ContentData.setMimetype(contentData, determineDefaultMimetype(data));
                    }
                }
                else
                {
                    // content data has not been persisted yet so get it from the node
                    contentData = (ContentData) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                    if (contentData != null)
                    {
                        contentData = ContentData.setMimetype(contentData, determineDefaultMimetype(data));
                    }
                }
                
            }
            else
            {
                contentData = (ContentData) this.nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                
                if (contentData != null)
                {
                    // if the ContentData object already exists in propsToPersist extract the mimetype
                    // and encoding and set on the ContentData object just retrieved
                    if (propsToPersist.containsKey(ContentModel.PROP_CONTENT))
                    {
                        ContentData mimetypeEncoding = (ContentData)propsToPersist.get(ContentModel.PROP_CONTENT);
                        contentData = ContentData.setMimetype(contentData, mimetypeEncoding.getMimetype());
                        contentData = ContentData.setEncoding(contentData, mimetypeEncoding.getEncoding());
                    }
                }
            }

            // add the potentially changed content data object back to property map for persistence
            if (contentData != null)
            {
                propsToPersist.put(ContentModel.PROP_CONTENT, contentData);
            }
        }
    }
    
    /**
     * Looks through the form data for the 'mimetype' transient field
     * and returns it's value if found, otherwise the default 'text/plain'
     * is returned
     * 
     * @param data Form data being persisted
     * @return The default mimetype
     */
    protected String determineDefaultMimetype(FormData data)
    {
        String mimetype = DEFAULT_CONTENT_MIMETYPE;
        
        if (data != null)
        {
            FieldData mimetypeField = data.getFieldData(PROP_DATA_PREFIX + MimetypeFieldProcessor.KEY);
            if (mimetypeField != null)
            {
                String mimetypeFieldValue = (String)mimetypeField.getValue();
                if (mimetypeFieldValue != null && mimetypeFieldValue.length() > 0)
                {
                    mimetype = mimetypeFieldValue;
                }
            }
        }
        
        return mimetype;
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

    public AbstractAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef, QName assocQName)
    {
        this.sourceNodeRef = sourceNodeRef;
        this.targetNodeRef = targetNodeRef;
        this.assocQName = assocQName;
    }

    /**
     * This method should use the specified nodeService reference to effect the
     * update to the supplied associations.
     * 
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

    public AddAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef, QName assocQName)
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

    public RemoveAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef, QName assocQName)
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
                msg.append("Attempt to remove non-existent association prevented. ").append(sourceNodeRef).append("|")
                            .append(targetNodeRef).append(assocQName);
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

    public AddChildAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef, QName assocQName)
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

    public RemoveChildAssocCommand(NodeRef sourceNodeRef, NodeRef targetNodeRef, QName assocQName)
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
                msg.append("Attempt to remove non-existent child association prevented. ").append(sourceNodeRef)
                            .append("|").append(targetNodeRef).append(assocQName);
                logger.warn(msg.toString());
            }
            return;
        }

        nodeService.removeChild(sourceNodeRef, targetNodeRef);
    }
}
