/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.processor.node.TypeFormProcessor;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TypeAndAspectsFormProcessor extends TypeFormProcessor
{

    /** Logger */
    private static Log logger = LogFactory.getLog(TypeAndAspectsFormProcessor.class);

    private Item typeItem = null;

    private List<String> aspectsItems = new ArrayList<String>();

    private MimetypeService mimetypeService;

    private Repository repository;

    private static final String PHYSICAL_STRUCTURE = "physical_structure";

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    @Override
    public Form generate(Item item, List<String> fields, List<String> forcedFields, Map<String, Object> context)
    {
        parseItems(item);
        return super.generate(typeItem,
                              fields,
                              forcedFields,
                              context);
    }

    @Override
    public Object persist(Item item, FormData data)
    {
        parseItems(item);
        return super.persist(typeItem,
                             data);
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

    @Override
    protected Set<QName> getAspectNames(TypeDefinition item)
    {
        Set<QName> requestedAspects = new HashSet<QName>();
        // default aspects from type definition
        requestedAspects.addAll(super.getAspectNames(item));
        // additional requested aspects
        requestedAspects.addAll(getRequestedAspects());
        return requestedAspects;
    }

    private Set<QName> getRequestedAspects()
    {
        Set<QName> requestedAspects = new HashSet<QName>();
        for (String aspectItem : aspectsItems)
        {
            QName aspectQname = getAspectQname(aspectItem);
            requestedAspects.add(aspectQname);
        }
        return requestedAspects;
    }

    private List<String> parseIds(String id)
    {
        String[] ids = id.split(",");
        List<String> parsedIds = new ArrayList<String>(Arrays.asList(ids));
        return parsedIds;
    }

    private void parseItems(Item item)
    {
        typeItem = null;
        aspectsItems = new ArrayList<String>();
        String id = item.getId();
        List<String> parsedIds = parseIds(id);
        // typeItem
        typeItem = new Item("type",
                            parsedIds.get(0));
        Iterator<String> iterator = parsedIds.iterator();
        iterator.next();
        while (iterator.hasNext())
        {
            String nextAspect = iterator.next();
            aspectsItems.add(nextAspect);
        }
    }

    private QName getAspectQname(String aspectName)
    {
        AspectDefinition aspectDef = null;
        QName aspect = null;

        try
        {
            // the itemId could either be the full form qname i.e.
            // {http://www.alfresco.org/model/content/1.0}content
            // or it could be the prefix form i.e. cm:name

            if (aspectName.startsWith("{"))
            {
                // item id looks like a full qname
                aspect = QName.createQName(aspectName);
            }
            else
            {
                // try and create the QName using the item id as is
                aspect = QName.createQName(aspectName,
                                           this.namespaceService);
            }

            // retrieve the aspect from the dictionary
            aspectDef = this.dictionaryService.getAspect(aspect);

            if (aspectDef == null)
            {
                throw new FormException(aspectName,
                                        new IllegalArgumentException("Aspect does not exist: " + aspectName));
            }
        }
        catch (InvalidQNameException iqne)
        {
            throw new FormException(aspectName,
                                    iqne);
        }

        // return the QName object for the requested aspect
        return aspect;

    }

    @Override
    protected void persistNode(NodeRef nodeRef, FormData data)
    {
        super.persistNode(nodeRef,
                          data);

        QName type = this.nodeService.getType(nodeRef);
        Set<QName> aspectNames = getAspectNames(getTypedItem(typeItem));
        TypeDefinition typeDef = this.dictionaryService.getAnonymousType(type,
                                                                         aspectNames);
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        Map<QName, Serializable> propsToPersist = new HashMap<QName, Serializable>();

        for (FieldData fieldData : data)
        {
            String fieldName = fieldData.getName();
            if (fieldName.startsWith(PROP_DATA_PREFIX))
            {
                processPropertyPersist(nodeRef,
                                       propDefs,
                                       fieldData,
                                       propsToPersist,
                                       data);
            }
        }

        this.nodeService.addProperties(nodeRef,
                                       propsToPersist);

    }

    @Override
    protected void processContentPropertyPersist(NodeRef nodeRef, FieldData fieldData,
                Map<QName, Serializable> propsToPersist, FormData data)
    {

        if (fieldData.isFile() == true)
        {
            ContentWriter writer = this.contentService.getWriter(nodeRef,
                                                                 ContentModel.PROP_CONTENT,
                                                                 true);
            ContentData contentData = null;

            if (writer != null)
            {
                // determine whether there is any content for the node yet i.e.
                // it's a create
                boolean defaultMimetypeRequired = (this.nodeService.getProperty(nodeRef,
                                                                                ContentModel.PROP_CONTENT) == null);

                // write the content
                InputStream inputStream = fieldData.getInputStream();
                writer.putContent(inputStream);

                // if there was no content set a sensible default mimetype if
                // necessary
                if (defaultMimetypeRequired)
                {
                    // if the transient mimetype property has already set the
                    // mimetype don't do anything
                    contentData = (ContentData) propsToPersist.get(ContentModel.PROP_CONTENT);
                    if (contentData != null)
                    {
                        String mimetype = contentData.getMimetype();
                        if (mimetype == null)
                        {
                            contentData = ContentData.setMimetype(contentData,
                                                                  determineDefaultMimetype(data));
                        }
                    }
                    else
                    {
                        // content data has not been persisted yet so get it
                        // from the node
                        contentData = (ContentData) this.nodeService.getProperty(nodeRef,
                                                                                 ContentModel.PROP_CONTENT);
                        if (contentData != null)
                        {
                            String fileName = (String) fieldData.getValue();
                            contentData = ContentData
                                        .setMimetype(contentData,
                                                     mimetypeService.getMimetype(fileName.substring(fileName
                                                                 .lastIndexOf(".") + 1)));
                        }
                    }

                }
                else
                {
                    contentData = (ContentData) this.nodeService.getProperty(nodeRef,
                                                                             ContentModel.PROP_CONTENT);

                    if (contentData != null)
                    {
                        // if the ContentData object already exists in
                        // propsToPersist extract the mimetype
                        // and encoding and set on the ContentData object just
                        // retrieved
                        if (propsToPersist.containsKey(ContentModel.PROP_CONTENT))
                        {
                            ContentData mimetypeEncoding = (ContentData) propsToPersist.get(ContentModel.PROP_CONTENT);
                            contentData = ContentData.setMimetype(contentData,
                                                                  mimetypeEncoding.getMimetype());
                            contentData = ContentData.setEncoding(contentData,
                                                                  mimetypeEncoding.getEncoding());
                        }
                    }
                }

                // add the potentially changed content data object back to
                // property map for persistence
                if (contentData != null)
                {
                    propsToPersist.put(ContentModel.PROP_CONTENT,
                                       contentData);
                }
            }
        }
        else
        {
            super.processContentPropertyPersist(nodeRef,
                                                fieldData,
                                                propsToPersist,
                                                data);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected NodeRef createNode(TypeDefinition typeDef, FormData data)
    {
        NodeRef nodeRef = null;

        if (data != null)
        {
            // firstly, ensure we have a destination to create the node in
            NodeRef parentRef = null;
            FieldData destination = data.getFieldData(DESTINATION);
            if (destination == null)
            {
                throw new FormException("Failed to persist form for '"
                            + typeDef.getName().toPrefixString(this.namespaceService) + "' as '" + DESTINATION
                            + "' data was not provided.");
            }

            // create the parent NodeRef
            String destinationValue = (String) destination.getValue();
            parentRef = getParentNodeRef(destinationValue);
            if (parentRef == null)
            {
                throw new FormException("Failed to persist form for '"
                            + typeDef.getName().toPrefixString(this.namespaceService) + "' as '" + destinationValue
                            + "' does not exist as path or NodeRef in the system.");
            }

            data.removeFieldData(DESTINATION);

            // if a name property is present in the form data use it as the node
            // name,
            // otherwise generate a guid
            String nodeName = null;
            FieldData nameData = data.getFieldData(NAME_PROP_DATA);
            if (nameData != null)
            {
                nodeName = (String) nameData.getValue();

                // remove the name data otherwise 'rename' gets called in
                // persistNode
                data.removeFieldData(NAME_PROP_DATA);
            }
            if (nodeName == null || nodeName.length() == 0)
            {
                nodeName = GUID.generate();
            }

            // create the node
            Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>(1);
            nodeProps.put(ContentModel.PROP_NAME,
                          nodeName);
            nodeRef = this.nodeService.createNode(parentRef,
                                                  ContentModel.ASSOC_CONTAINS,
                                                  QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                                    QName.createValidLocalName(nodeName)),
                                                  typeDef.getName(),
                                                  nodeProps).getChildRef();
            // adding additional aspects for now with empty properties...this
            // will not be needed anymore when we'll use form processor for
            // generating the form
            for (QName qname : getRequestedAspects())
            {
                this.nodeService.addAspect(nodeRef,
                                           qname,
                                           new HashMap<QName, Serializable>());
            }
            // if the physical structure is configured we'll create for now
            // simple folders
            FieldData physicalStructure = data.getFieldData(PHYSICAL_STRUCTURE);
            if (physicalStructure != null)
            {
                List<String> folderPhysicalStructure = (List<String>) physicalStructure.getValue();
                createPhysicalStructure(nodeRef,
                                        folderPhysicalStructure);
                data.removeFieldData(PHYSICAL_STRUCTURE);
            }
        }

        return nodeRef;
    }

    private void createPhysicalStructure(NodeRef nodeRef, List<String> folderNames)
    {
        for (String folderName : folderNames)
        {
            Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>(1);
            nodeProps.put(ContentModel.PROP_NAME,
                          folderName);
            this.nodeService.createNode(nodeRef,
                                        ContentModel.ASSOC_CONTAINS,
                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                          QName.createValidLocalName(folderName)),
                                        ContentModel.TYPE_FOLDER,
                                        nodeProps);
        }
    }

    private NodeRef getParentNodeRef(String destination)
    {
        NodeRef parentRef = null;
        if (destination.contains("://"))
        {
            String node = destination.replace("://",
                                              "/");
            parentRef = repository.findNodeRef("node",
                                               node.split("/"));
        }
        else
        {
            String path = "workspace/SpacesStore/Company Home/" + destination;
            parentRef = repository.findNodeRef("path",
                                               path.split("/"));
        }
        return parentRef;
    }
}
