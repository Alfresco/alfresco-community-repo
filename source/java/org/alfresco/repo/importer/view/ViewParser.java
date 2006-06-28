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
package org.alfresco.repo.importer.view;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.alfresco.repo.importer.Importer;
import org.alfresco.repo.importer.Parser;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * Importer for parsing and importing nodes given the Repository View schema.
 * 
 * @author David Caruana
 */
public class ViewParser implements Parser
{
    // Logger
    private static final Log logger = LogFactory.getLog(ViewParser.class);
    
    // View schema elements and attributes
    private static final String VIEW_CHILD_NAME_ATTR = "childName";    
    private static final String VIEW_DATATYPE_ATTR = "datatype";
    private static final String VIEW_ISNULL_ATTR = "isNull";
    private static final String VIEW_INHERIT_PERMISSIONS_ATTR = "inherit";
    private static final String VIEW_ACCESS_STATUS_ATTR = "access";
    private static final String VIEW_ID_ATTR = "id";
    private static final String VIEW_IDREF_ATTR = "idref";
    private static final String VIEW_PATHREF_ATTR = "pathref";
    private static final String VIEW_NODEREF_ATTR = "noderef";
    private static final QName VIEW_METADATA = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "metadata");
    private static final QName VIEW_VALUE_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "value");
    private static final QName VIEW_VALUES_QNAME = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "values");
    private static final QName VIEW_ASPECTS = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "aspects");
    private static final QName VIEW_PROPERTIES = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "properties");
    private static final QName VIEW_ASSOCIATIONS = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "associations");
    private static final QName VIEW_ACL = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "acl");
    private static final QName VIEW_ACE = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "ace");
    private static final QName VIEW_AUTHORITY = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "authority");
    private static final QName VIEW_PERMISSION = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "permission");
    private static final QName VIEW_REFERENCE = QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "reference");
    
    
    // XML Pull Parser Factory
    private XmlPullParserFactory factory;
    
    // Supporting services
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    // Parser Context maintained during each parse
    private class ParserContext
    {
        Importer importer;
        DictionaryService dictionaryService;
        Stack<ElementContext> elementStack;
        Map<String, NodeRef> importIds = new HashMap<String, NodeRef>();
    }
    

    /**
     * Construct
     */
    public ViewParser()
    {
        try
        {
            // Construct Xml Pull Parser Factory
            factory = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), this.getClass());
            factory.setNamespaceAware(true);
        }
        catch (XmlPullParserException e)
        {
            throw new ImporterException("Failed to initialise view importer", e);
        }
    }
    
    
    /**
     * @param namespaceService  the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param dictionaryService  the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.importer.Parser#parse(java.io.Reader, org.alfresco.repo.importer.Importer)
     */
    public void parse(Reader viewReader, Importer importer)
    {
        try
        {
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(viewReader);
            
            ParserContext parserContext = new ParserContext();
            parserContext.importer = importer;
            parserContext.dictionaryService = dictionaryService;
            parserContext.elementStack = new Stack<ElementContext>();
            
            try
            {
                for (int eventType = xpp.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xpp.next())
                {
                    switch (eventType)
                    {
                        case XmlPullParser.START_TAG:
                        {
                            if (xpp.getDepth() == 1)
                            {
                                processRoot(xpp, parserContext);
                            }
                            else
                            {
                                processStartElement(xpp, parserContext);
                            }
                            break;
                        }
                        case XmlPullParser.END_TAG:
                        {
                            processEndElement(xpp, parserContext);
                            break;
                        }
                    }
                }
            }
            catch(Exception e)
            {
                throw new ImporterException("Failed to import package at line " + xpp.getLineNumber() + "; column " + xpp.getColumnNumber() + " due to error: " + e.getMessage(), e);
            }
        }
        catch(XmlPullParserException e)
        {
            throw new ImporterException("Failed to parse view", e);
        }
    }
    
    /**
     * Process start of xml element
     * 
     * @param xpp
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processStartElement(XmlPullParser xpp, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        // Extract qualified name
        QName defName = getName(xpp);

        // Process the element
        Object element = parserContext.elementStack.peek();

        // Handle special view directives
        if (defName.equals(VIEW_METADATA))
        {
        	MetaDataContext metaDataContext = new MetaDataContext(defName, (ElementContext)element); 
            parserContext.elementStack.push(metaDataContext);
            
            if (logger.isDebugEnabled())
                logger.debug(indentLog("Pushed " + metaDataContext, parserContext.elementStack.size() -1));
        }
        else if (defName.equals(VIEW_ASPECTS) || defName.equals(VIEW_PROPERTIES) || defName.equals(VIEW_ASSOCIATIONS) || defName.equals(VIEW_ACL))
        {
            if (element instanceof NodeItemContext)
            {
                throw new ImporterException("Cannot nest element " + defName + " within " + ((NodeItemContext)element).getElementName());
            }
            if (!(element instanceof NodeContext))
            {
                throw new ImporterException("Element " + defName + " can only be declared within a node");
            }
            NodeContext node = (NodeContext)element;
            NodeItemContext nodeItemContext = new NodeItemContext(defName, node);
            parserContext.elementStack.push(nodeItemContext);

            if (logger.isDebugEnabled())
                logger.debug(indentLog("Pushed " + nodeItemContext, parserContext.elementStack.size() -1));

            // process ACL specific attributes
            if (defName.equals(VIEW_ACL))
            {
                processACL(xpp, parserContext);
            }
        }
        else
        {
            if (element instanceof MetaDataContext)
            {
                processMetaData(xpp, defName, parserContext);
            }
            else if (element instanceof ParentContext)
            {
                if (defName.equals(VIEW_REFERENCE))
                {
                    // Process reference
                    processStartReference(xpp, defName, parserContext);
                }
                else
                {
                    // Process type definition 
                    TypeDefinition typeDef = dictionaryService.getType(defName);
                    if (typeDef == null)
                    {
                        throw new ImporterException("Type " + defName + " has not been defined in the Repository dictionary");
                    }
                    processStartType(xpp, typeDef, parserContext);
                }
                return;
            }
            else if (element instanceof NodeContext)
            {
                // Process children of node
                // Note: Process in the following order: aspects, properties and associations
                Object def = ((NodeContext)element).determineDefinition(defName);
                if (def == null)
                {
                    throw new ImporterException("Definition " + defName + " is not valid; cannot find in Repository dictionary");
                }
                
                if (def instanceof AspectDefinition)
                {
                    processAspect(xpp, (AspectDefinition)def, parserContext);
                    return;
                }
                else if (def instanceof PropertyDefinition)
                {
                    processProperty(xpp, ((PropertyDefinition)def).getName(), parserContext);
                    return;
                }
                else if (def instanceof AssociationDefinition)
                {
                    processStartAssoc(xpp, (AssociationDefinition)def, parserContext);
                    return;
                }
            }
            else if (element instanceof NodeItemContext)
            {
                NodeItemContext nodeItem = (NodeItemContext)element;
                NodeContext node = nodeItem.getNodeContext();
                QName itemName = nodeItem.getElementName();
                if (itemName.equals(VIEW_ASPECTS))
                {
                    AspectDefinition def = node.determineAspect(defName);
                    if (def == null)
                    {
                        throw new ImporterException("Aspect name " + defName + " is not valid; cannot find in Repository dictionary");
                    }
                    processAspect(xpp, def, parserContext);
                }
                else if (itemName.equals(VIEW_PROPERTIES))
                {
                    // Note: Allow properties which do not have a data dictionary definition
                    processProperty(xpp, defName, parserContext);
                }
                else if (itemName.equals(VIEW_ASSOCIATIONS))
                {
                    AssociationDefinition def = (AssociationDefinition)node.determineAssociation(defName);
                    if (def == null)
                    {
                        throw new ImporterException("Association name " + defName + " is not valid; cannot find in Repository dictionary");
                    }
                    processStartAssoc(xpp, (AssociationDefinition)def, parserContext);
                }
                else if (itemName.equals(VIEW_ACL))
                {
                    processAccessControlEntry(xpp, parserContext);
                }
            }
        }
    }

    /**
     * Process Root
     * 
     * @param xpp
     * @param parentRef
     * @param childAssocType
     * @param configuration
     * @param progress
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processRoot(XmlPullParser xpp, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        ParentContext parent = new ParentContext(getName(xpp), parserContext.dictionaryService, parserContext.importer);
        parserContext.elementStack.push(parent);
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Pushed " + parent, parserContext.elementStack.size() -1));
    }

    /**
     * Process meta-data
     * 
     * @param xpp
     * @param metaDataName
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processMetaData(XmlPullParser xpp, QName metaDataName, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        MetaDataContext metaData = (MetaDataContext)parserContext.elementStack.peek();

        String value = null;
        
        int eventType = xpp.next();
        if (eventType == XmlPullParser.TEXT)
        {
            // Extract value
            value = xpp.getText();
            eventType = xpp.next();
        }
        if (eventType != XmlPullParser.END_TAG)
        {
            throw new ImporterException("Meta data element " + metaDataName + " is missing end tag");
        }

        metaData.setProperty(metaDataName, value);
    }
    
    /**
     * Process start of a node definition
     * 
     * @param xpp
     * @param typeDef
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processStartType(XmlPullParser xpp, TypeDefinition typeDef, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        ParentContext parent = (ParentContext)parserContext.elementStack.peek();
        NodeContext node = new NodeContext(typeDef.getName(), parent, typeDef);

        // Extract child name if explicitly defined
        String childName = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_CHILD_NAME_ATTR);
        if (childName != null && childName.length() > 0)
        {
            node.setChildName(childName);
        }

        // Extract import id if explicitly defined
        String importId = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_ID_ATTR);
        if (importId != null && importId.length() > 0)
        {
            node.setImportId(importId);
        }
        
        parserContext.elementStack.push(node);
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Pushed " + node, parserContext.elementStack.size() -1));
    }

    /**
     * Process start reference
     * 
     * @param xpp
     * @param typeDef
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processStartReference(XmlPullParser xpp, QName refName, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        ParentContext parent = (ParentContext)parserContext.elementStack.peek();
        NodeContext node = new NodeContext(refName, parent, null);
        node.setReference(true);
        
        // Extract Import scoped reference Id if explicitly defined
        String idRefAttr = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_IDREF_ATTR);
        String pathRefAttr = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_PATHREF_ATTR);
        String nodeRefAttr = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_NODEREF_ATTR);

        if ((idRefAttr != null && idRefAttr.length() > 0) && (pathRefAttr != null && pathRefAttr.length() > 0) && (nodeRefAttr != null && nodeRefAttr.length() > 0))
        {
            // Do not support both IDREF and PATHREF
            throw new ImporterException("Only one of " + VIEW_IDREF_ATTR + " or " + VIEW_PATHREF_ATTR + " or " + VIEW_NODEREF_ATTR + " can be specified.");
        }
        if (nodeRefAttr != null)
        {
            NodeRef nodeRef = new NodeRef(nodeRefAttr);
            node.setUUID(nodeRef.getId());
        }
        else if (idRefAttr != null && idRefAttr.length() > 0)
        {
            // retrieve uuid from previously imported node
            NodeRef nodeRef = getImportReference(parserContext, idRefAttr);
            if (nodeRef == null)
            {
                throw new ImporterException("Cannot find node referenced by id " + idRefAttr);
            }
            node.setUUID(nodeRef.getId());
        }
        else if (pathRefAttr != null && pathRefAttr.length() > 0)
        {
            NodeRef referencedRef = parserContext.importer.resolvePath(pathRefAttr);
            if (referencedRef == null)
            {
                throw new ImporterException("Cannot find node referenced by path " + pathRefAttr);
            }
            node.setUUID(referencedRef.getId());
        }
        
        // Extract child name if explicitly defined
        String childName = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_CHILD_NAME_ATTR);
        if (childName != null && childName.length() > 0)
        {
            node.setChildName(childName);
        }

        // Extract import id if explicitly defined
        String importId = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_ID_ATTR);
        if (importId != null && importId.length() > 0)
        {
            node.setImportId(importId);
        }

        parserContext.elementStack.push(node);
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Pushed Reference " + node, parserContext.elementStack.size() -1));
    }
    
    /**
     * Process aspect definition
     * 
     * @param xpp
     * @param aspectDef
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processAspect(XmlPullParser xpp, AspectDefinition aspectDef, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        NodeContext node = peekNodeContext(parserContext.elementStack);
        node.addAspect(aspectDef);
        
        int eventType = xpp.next();
        if (eventType != XmlPullParser.END_TAG)
        {
            throw new ImporterException("Aspect " + aspectDef.getName() + " definition is not valid - it cannot contain any elements");
        }
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Processed aspect " + aspectDef.getName(), parserContext.elementStack.size()));
    }
    
    /**
     * Process ACL definition
     * 
     * @param xpp
     * @param contextStack
     */
    private void processACL(XmlPullParser xpp, ParserContext parserContext)
    {
        NodeContext node = peekNodeContext(parserContext.elementStack);
        
        String strInherit = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_INHERIT_PERMISSIONS_ATTR);
        if (strInherit != null)
        {
            Boolean inherit = Boolean.valueOf(strInherit);
            if (!inherit)
            {
                node.setInheritPermissions(false);
            }
        }
    }
    
    /**
     * Process ACE definition
     * 
     * @param xpp
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processAccessControlEntry(XmlPullParser xpp, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        NodeContext node = peekNodeContext(parserContext.elementStack);

        QName defName = getName(xpp);
        if (!defName.equals(VIEW_ACE))
        {
            throw new ImporterException("Expected start element " + VIEW_ACE);
        }

        // extract Access Status
        String access = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_ACCESS_STATUS_ATTR);
        AccessStatus accessStatus = (access == null) ? AccessStatus.ALLOWED : AccessStatus.valueOf(AccessStatus.class, access);
        if (accessStatus == null)
        {
            throw new ImporterException("Permission access status '" + access + "' is not recognised.");
        }

        // extract authority and permission
        String authority = null;
        String permission = null;
        int eventType = xpp.next();
        while (eventType != XmlPullParser.END_TAG)
        {
            if (eventType == XmlPullParser.START_TAG)
            {
                defName = getName(xpp);
                if (defName.equals(VIEW_AUTHORITY))
                {
                    eventType = xpp.next();
                    if (eventType != XmlPullParser.TEXT)
                    {
                        throw new ImporterException("Element " + VIEW_AUTHORITY + " must have a value");
                    }
                    authority = xpp.getText();
                }
                else if (defName.equals(VIEW_PERMISSION))
                {
                    eventType = xpp.next();
                    if (eventType != XmlPullParser.TEXT)
                    {
                        throw new ImporterException("Element " + VIEW_PERMISSION + " must have a value");
                    }
                    permission = xpp.getText();
                }
                else
                {
                    throw new ImporterException("Expected start element " + VIEW_AUTHORITY + " or " + VIEW_PERMISSION);
                }
                
                eventType = xpp.next();
                if (eventType != XmlPullParser.END_TAG)
                {
                    throw new ImporterException("Expected end element " + defName);
                }
                QName endDefName = getName(xpp);
                if (!defName.equals(endDefName))
                {
                    throw new ImporterException("Expected end element " + defName);
                }
            }
            
            eventType = xpp.next();
        }

        // validate authority and permission
        if (authority == null || authority.length() == 0)
        {
            throw new ImporterException("Authority must be specified");
        }
        if (permission == null || permission.length() == 0)
        {
            throw new ImporterException("Permisssion must be specified");
        }

        // extract end of ace
        defName = getName(xpp);
        if (!defName.equals(VIEW_ACE))
        {
            throw new ImporterException("Expected end element " + VIEW_ACE);
        }
        
        // update node context
        node.addAccessControlEntry(accessStatus, authority, permission);
    }
    
    /**
     * Process property definition
     * 
     * @param xpp
     * @param propDef
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processProperty(XmlPullParser xpp, QName propertyName, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        NodeContext node = peekNodeContext(parserContext.elementStack);

        // Extract single value
        String value = "";
        int eventType = xpp.next();
        if (eventType == XmlPullParser.TEXT)
        {
            value = xpp.getText();
            eventType = xpp.next();
        }            
        if (eventType == XmlPullParser.END_TAG)
        {
            node.addProperty(propertyName, value);
        }
        else
        {
            // Extract collection, if specified
            boolean isCollection = false;
            if (eventType == XmlPullParser.START_TAG)
            {
                QName name = getName(xpp);
                if (name.equals(VIEW_VALUES_QNAME))
                {
                    node.addPropertyCollection(propertyName);
                    isCollection = true;
                    eventType = xpp.next();
                    if (eventType == XmlPullParser.TEXT)
                    {
                        eventType = xpp.next();
                    }
                }
            }
            
            // Extract decorated value
            while (eventType == XmlPullParser.START_TAG)
            {
                QName name = getName(xpp);
                if (!name.equals(VIEW_VALUE_QNAME))
                {
                    throw new ImporterException("Invalid view structure - expected element " + VIEW_VALUE_QNAME + " for property " + propertyName);
                }
                QName datatype = QName.createQName(xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_DATATYPE_ATTR), namespaceService);
                Boolean isNull = Boolean.valueOf(xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_ISNULL_ATTR));
                String decoratedValue = isNull ? null : "";
                eventType = xpp.next();
                if (eventType == XmlPullParser.TEXT)
                {
                    decoratedValue = xpp.getText();
                    eventType = xpp.next();
                }
                if (eventType == XmlPullParser.END_TAG)
                {
                    node.addProperty(propertyName, decoratedValue);
                    if (datatype != null)
                    {
                        node.addDatatype(propertyName, dictionaryService.getDataType(datatype));
                    }
                }
                else
                {
                    throw new ImporterException("Value for property " + propertyName + " has not been defined correctly - missing end tag");
                }
                eventType = xpp.next();
                if (eventType == XmlPullParser.TEXT)
                {
                    eventType = xpp.next();
                }
            }
    
            // End of value
            if (eventType != XmlPullParser.END_TAG)
            {
                throw new ImporterException("Invalid view structure - property " + propertyName + " definition is invalid");
            }
            
            // End of collection
            if (isCollection)
            {
                eventType = xpp.next();
                if (eventType == XmlPullParser.TEXT)
                {
                    eventType = xpp.next();
                }
                if (eventType != XmlPullParser.END_TAG)
                {
                    throw new ImporterException("Invalid view structure - property " + propertyName + " definition is invalid");
                }
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Processed property " + propertyName, parserContext.elementStack.size()));
    }

    /**
     * Process start of association definition
     * 
     * @param xpp
     * @param AssocDef
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processStartAssoc(XmlPullParser xpp, AssociationDefinition assocDef, ParserContext parserContext)
        throws XmlPullParserException, IOException
    {
        NodeContext node = peekNodeContext(parserContext.elementStack);
        importNode(parserContext, node);
    
        // Construct Child Association Context
        ParentContext parent = new ParentContext(assocDef.getName(), node, assocDef);
        parserContext.elementStack.push(parent);
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Pushed " + parent, parserContext.elementStack.size() -1));
    }
    
    /**
     * Process end of xml element
     * 
     * @param xpp
     * @param contextStack
     */
    private void processEndElement(XmlPullParser xpp, ParserContext parserContext)
    {
        ElementContext element = parserContext.elementStack.peek();
        if (element.getElementName().getLocalName().equals(xpp.getName()) &&
            element.getElementName().getNamespaceURI().equals(xpp.getNamespace()))
        {
            element = parserContext.elementStack.pop();
            
            if (logger.isDebugEnabled())
                logger.debug(indentLog("Popped " + element, parserContext.elementStack.size()));

            if (element instanceof NodeContext)
            {
                processEndType(parserContext, (NodeContext)element);
            }
            else if (element instanceof ParentContext)
            {
                processEndAssoc(parserContext, (ParentContext)element);
            }
            else if (element instanceof MetaDataContext)
            {
                processEndMetaData(parserContext, (MetaDataContext)element);
            }
        }
    }
    
    /**
     * Process end of the type definition
     * 
     * @param node
     */
    private void processEndType(ParserContext parserContext, NodeContext node)
    {
        importNode(parserContext, node);
        NodeRef nodeRef = node.getNodeRef();
        node.getImporter().childrenImported(nodeRef);
    }

    /**
     * Process end of the child association
     * 
     * @param context
     */
    private void processEndAssoc(ParserContext parserContext, ParentContext parent)
    {
    }

    /**
     * Process end of meta data
     * 
     * @param context
     */
    private void processEndMetaData(ParserContext parserContext, MetaDataContext context)
    {
        context.getImporter().importMetaData(context.getProperties());
    }
    

    /**
     * Import node
     * 
     * @param parserContext  parser context
     * @param node  node context
     */
    private void importNode(ParserContext parserContext, NodeContext node)
    {
        if (node.getNodeRef() == null)
        {
            // Import Node
            NodeRef nodeRef = node.getImporter().importNode(node);
            node.setNodeRef(nodeRef);
            
            // Maintain running list of "import" scoped ids
            String importId = node.getImportId();
            if (importId != null && importId.length() > 0)
            {
                createImportReference(parserContext, importId, nodeRef);
            }
        }
    }
    
    /**
     * Maps an Import Id to a Node Reference
     * 
     * @param importId  import Id
     * @param nodeRef  node reference
     */
    private void createImportReference(ParserContext parserContext, String importId, NodeRef nodeRef)
    {
        if (parserContext.importIds.containsKey(importId))
        {
            throw new ImporterException("Import id " + importId + " already specified within import file");
        }
        parserContext.importIds.put(importId, nodeRef);
    }

    /**
     * Gets the Node Reference for the specified Import Id
     * 
     * @param importId  the import id
     * @return  the node reference
     */
    private NodeRef getImportReference(ParserContext parserContext, String importId)
    {
        return parserContext.importIds.get(importId);
    }

    /**
     * Get parent Node Context
     * 
     * @param contextStack  context stack
     * @return  node context
     */
    private NodeContext peekNodeContext(Stack<ElementContext> contextStack)
    {
        ElementContext element = contextStack.peek();
        if (element instanceof NodeContext)
        {
            return (NodeContext)element;
        }
        else if (element instanceof NodeItemContext)
        {
            return ((NodeItemContext)element).getNodeContext();
        }
        throw new ImporterException("Internal error: Failed to retrieve node context");
    }
    
    /**
     * Helper to create Qualified name from current xml element
     * 
     * @param xpp
     * @return
     */
    private QName getName(XmlPullParser xpp)
    {
        // Ensure namespace is valid
        String uri = xpp.getNamespace();
        if (namespaceService.getURIs().contains(uri) == false)
        {
            throw new ImporterException("Namespace URI " + uri + " has not been defined in the Repository dictionary");
        }
        
        // Construct name
        String name = xpp.getName();
        return QName.createQName(uri, name);
    }

    /**
     * Helper to indent debug output
     * 
     * @param msg
     * @param depth
     * @return
     */
    private String indentLog(String msg, int depth)
    {
        StringBuffer buf = new StringBuffer(1024);
        for (int i = 0; i < depth; i++)
        {
            buf.append(' ');
        }
        buf.append(msg);
        return buf.toString();
    }
    
}
