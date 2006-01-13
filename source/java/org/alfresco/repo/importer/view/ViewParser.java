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
import java.util.Stack;

import org.alfresco.repo.importer.Importer;
import org.alfresco.repo.importer.Parser;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.security.AccessStatus;
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
    
    
    // XML Pull Parser Factory
    private XmlPullParserFactory factory;
    
    // Supporting services
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    
    
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
            Stack<ElementContext> contextStack = new Stack<ElementContext>();
            
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
                                processRoot(xpp, importer, contextStack);
                            }
                            else
                            {
                                processStartElement(xpp, contextStack);
                            }
                            break;
                        }
                        case XmlPullParser.END_TAG:
                        {
                            processEndElement(xpp, contextStack);
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
    private void processStartElement(XmlPullParser xpp, Stack<ElementContext> contextStack)
        throws XmlPullParserException, IOException
    {
        // Extract qualified name
        QName defName = getName(xpp);

        // Process the element
        Object context = contextStack.peek();

        // Handle special view directives
        if (defName.equals(VIEW_METADATA))
        {
            contextStack.push(new MetaDataContext(defName, (ElementContext)context));
        }
        else if (defName.equals(VIEW_ASPECTS) || defName.equals(VIEW_PROPERTIES) || defName.equals(VIEW_ASSOCIATIONS) || defName.equals(VIEW_ACL))
        {
            if (context instanceof NodeItemContext)
            {
                throw new ImporterException("Cannot nest element " + defName + " within " + ((NodeItemContext)context).getElementName());
            }
            if (!(context instanceof NodeContext))
            {
                throw new ImporterException("Element " + defName + " can only be declared within a node");
            }
            NodeContext nodeContext = (NodeContext)context;
            contextStack.push(new NodeItemContext(defName, nodeContext));

            // process ACL specific attributes
            if (defName.equals(VIEW_ACL))
            {
                processACL(xpp, contextStack);
            }
        }
        else
        {
            if (context instanceof MetaDataContext)
            {
                processMetaData(xpp, defName, contextStack);
            }
            else if (context instanceof ParentContext)
            {
                // Process type definition 
                TypeDefinition typeDef = dictionaryService.getType(defName);
                if (typeDef == null)
                {
                    throw new ImporterException("Type " + defName + " has not been defined in the Repository dictionary");
                }
                processStartType(xpp, typeDef, contextStack);
                return;
            }
            else if (context instanceof NodeContext)
            {
                // Process children of node
                // Note: Process in the following order: aspects, properties and associations
                Object def = ((NodeContext)context).determineDefinition(defName);
                if (def == null)
                {
                    throw new ImporterException("Definition " + defName + " is not valid; cannot find in Repository dictionary");
                }
                
                if (def instanceof AspectDefinition)
                {
                    processAspect(xpp, (AspectDefinition)def, contextStack);
                    return;
                }
                else if (def instanceof PropertyDefinition)
                {
                    processProperty(xpp, ((PropertyDefinition)def).getName(), contextStack);
                    return;
                }
                else if (def instanceof ChildAssociationDefinition)
                {
                    processStartChildAssoc(xpp, (ChildAssociationDefinition)def, contextStack);
                    return;
                }
                else
                {
                    // TODO: general association
                }
            }
            else if (context instanceof NodeItemContext)
            {
                NodeItemContext nodeItemContext = (NodeItemContext)context;
                NodeContext nodeContext = nodeItemContext.getNodeContext();
                QName itemName = nodeItemContext.getElementName();
                if (itemName.equals(VIEW_ASPECTS))
                {
                    AspectDefinition def = nodeContext.determineAspect(defName);
                    if (def == null)
                    {
                        throw new ImporterException("Aspect name " + defName + " is not valid; cannot find in Repository dictionary");
                    }
                    processAspect(xpp, def, contextStack);
                }
                else if (itemName.equals(VIEW_PROPERTIES))
                {
                    // Note: Allow properties which do not have a data dictionary definition
                    processProperty(xpp, defName, contextStack);
                }
                else if (itemName.equals(VIEW_ASSOCIATIONS))
                {
                    // TODO: Handle general associations...  
                    ChildAssociationDefinition def = (ChildAssociationDefinition)nodeContext.determineAssociation(defName);
                    if (def == null)
                    {
                        throw new ImporterException("Association name " + defName + " is not valid; cannot find in Repository dictionary");
                    }
                    processStartChildAssoc(xpp, def, contextStack);
                }
                else if (itemName.equals(VIEW_ACL))
                {
                    processAccessControlEntry(xpp, contextStack);
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
    private void processRoot(XmlPullParser xpp, Importer importer, Stack<ElementContext> contextStack)
        throws XmlPullParserException, IOException
    {
        ParentContext parentContext = new ParentContext(getName(xpp), dictionaryService, importer);
        contextStack.push(parentContext);
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Pushed " + parentContext, contextStack.size() -1));
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
    private void processMetaData(XmlPullParser xpp, QName metaDataName, Stack<ElementContext> contextStack)
        throws XmlPullParserException, IOException
    {
        MetaDataContext context = (MetaDataContext)contextStack.peek();

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

        context.setProperty(metaDataName, value);
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
    private void processStartType(XmlPullParser xpp, TypeDefinition typeDef, Stack<ElementContext> contextStack)
        throws XmlPullParserException, IOException
    {
        ParentContext parentContext = (ParentContext)contextStack.peek();
        NodeContext context = new NodeContext(typeDef.getName(), parentContext, typeDef);
        
        // Extract child name if explicitly defined
        String childName = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_CHILD_NAME_ATTR);
        if (childName != null && childName.length() > 0)
        {
            context.setChildName(childName);
        }

        contextStack.push(context);
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Pushed " + context, contextStack.size() -1));
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
    private void processAspect(XmlPullParser xpp, AspectDefinition aspectDef, Stack<ElementContext> contextStack)
        throws XmlPullParserException, IOException
    {
        NodeContext context = peekNodeContext(contextStack);
        context.addAspect(aspectDef);
        
        int eventType = xpp.next();
        if (eventType != XmlPullParser.END_TAG)
        {
            throw new ImporterException("Aspect " + aspectDef.getName() + " definition is not valid - it cannot contain any elements");
        }
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Processed aspect " + aspectDef.getName(), contextStack.size()));
    }
    
    /**
     * Process ACL definition
     * 
     * @param xpp
     * @param contextStack
     */
    private void processACL(XmlPullParser xpp, Stack<ElementContext> contextStack)
    {
        NodeContext context = peekNodeContext(contextStack);
        
        String strInherit = xpp.getAttributeValue(NamespaceService.REPOSITORY_VIEW_1_0_URI, VIEW_INHERIT_PERMISSIONS_ATTR);
        if (strInherit != null)
        {
            Boolean inherit = Boolean.valueOf(strInherit);
            if (!inherit)
            {
                context.setInheritPermissions(false);
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
    private void processAccessControlEntry(XmlPullParser xpp, Stack<ElementContext> contextStack)
        throws XmlPullParserException, IOException
    {
        NodeContext context = peekNodeContext(contextStack);

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
        context.addAccessControlEntry(accessStatus, authority, permission);
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
    private void processProperty(XmlPullParser xpp, QName propertyName, Stack<ElementContext> contextStack)
        throws XmlPullParserException, IOException
    {
        NodeContext context = peekNodeContext(contextStack);

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
            context.addProperty(propertyName, value);
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
                    context.addPropertyCollection(propertyName);
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
                    context.addProperty(propertyName, decoratedValue);
                    if (datatype != null)
                    {
                        context.addDatatype(propertyName, dictionaryService.getDataType(datatype));
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
            logger.debug(indentLog("Processed property " + propertyName, contextStack.size()));
    }

    /**
     * Process start of child association definition
     * 
     * @param xpp
     * @param childAssocDef
     * @param contextStack
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void processStartChildAssoc(XmlPullParser xpp, ChildAssociationDefinition childAssocDef, Stack<ElementContext> contextStack)
        throws XmlPullParserException, IOException
    {
        NodeContext context = peekNodeContext(contextStack);
    
        if (context.getNodeRef() == null)
        {
            // Create Node
            NodeRef nodeRef = context.getImporter().importNode(context);
            context.setNodeRef(nodeRef);
        }
        
        // Construct Child Association Context
        ParentContext parentContext = new ParentContext(childAssocDef.getName(), context, childAssocDef);
        contextStack.push(parentContext);
        
        if (logger.isDebugEnabled())
            logger.debug(indentLog("Pushed " + parentContext, contextStack.size() -1));
    }

    /**
     * Process end of xml element
     * 
     * @param xpp
     * @param contextStack
     */
    private void processEndElement(XmlPullParser xpp, Stack<ElementContext> contextStack)
    {
        ElementContext context = contextStack.peek();
        if (context.getElementName().getLocalName().equals(xpp.getName()) &&
            context.getElementName().getNamespaceURI().equals(xpp.getNamespace()))
        {
            context = contextStack.pop();
            
            if (logger.isDebugEnabled())
                logger.debug(indentLog("Popped " + context, contextStack.size()));

            if (context instanceof NodeContext)
            {
                processEndType((NodeContext)context);
            }
            else if (context instanceof ParentContext)
            {
                processEndChildAssoc((ParentContext)context);
            }
            else if (context instanceof MetaDataContext)
            {
                processEndMetaData((MetaDataContext)context);
            }
        }
    }
    
    /**
     * Process end of the type definition
     * 
     * @param context
     */
    private void processEndType(NodeContext context)
    {
        NodeRef nodeRef = context.getNodeRef();
        if (nodeRef == null)
        {
            nodeRef = context.getImporter().importNode(context);
            context.setNodeRef(nodeRef);
        }
        context.getImporter().childrenImported(nodeRef);
    }

    /**
     * Process end of the child association
     * 
     * @param context
     */
    private void processEndChildAssoc(ParentContext context)
    {
    }

    /**
     * Process end of meta data
     * 
     * @param context
     */
    private void processEndMetaData(MetaDataContext context)
    {
        context.getImporter().importMetaData(context.getProperties());
    }
    
    /**
     * Get parent Node Context
     * 
     * @param contextStack  context stack
     * @return  node context
     */
    private NodeContext peekNodeContext(Stack<ElementContext> contextStack)
    {
        ElementContext context = contextStack.peek();
        if (context instanceof NodeContext)
        {
            return (NodeContext)context;
        }
        else if (context instanceof NodeItemContext)
        {
            return ((NodeItemContext)context).getNodeContext();
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
