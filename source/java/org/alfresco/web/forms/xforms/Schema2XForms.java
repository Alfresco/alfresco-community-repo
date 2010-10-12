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
package org.alfresco.web.forms.xforms;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.ns.NamespaceConstants;
import org.chiba.xml.ns.NamespaceResolver;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * An abstract implementation of the Schema2XForms interface allowing
 * an XForm to be automatically generated for an XML Schema definition.
 * This abstract class implements the buildForm and buildFormAsString methods
 * and associated helper but relies on concrete subclasses to implement other
 * required interface methods (createXXX, startXXX, and endXXX methods).
 *
 * @author $Author: unl $
 */
public class Schema2XForms implements Serializable
{
   private static final long serialVersionUID = -2751398323635817643L;

   /////////////////////////////////////////////////////////////////////////////

   public enum SubmitMethod
   {
      POST("post"),
      GET("get"),
      PUT("put"),
      FORM_DATA_POST("form-data-post"),
      URL_ENCODED_POST("urlencoded-post");

      private final String value;

      SubmitMethod(final String value)
      {
         this.value = value;
      }

      public String toString()
      {
         return this.value;
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   private final static Log LOGGER = LogFactory.getLog(Schema2XForms.class);

   private static final Pattern repeatableNamePattern = Pattern.compile("\\[.+\\]");

   private final static int LONG_LIST_SIZE = 5;

   private final String action;
   private final SubmitMethod submitMethod;
   private final String base;
   private final boolean formatCaption;
   @SuppressWarnings("unchecked")
   private final Stack parentStack = new Stack();

   /**
    * generic counter -> replaced by an hashMap with:
    * keys: name of the elements
    * values: "Long" representing the counter for this element
    */
   private final Map<String, Long> counter = new HashMap<String, Long>();
   private String targetNamespace;

   /**
    * each entry is keyed by the type name
    * value is an ArrayList that contains the XSTypeDefinition's which
    * are compatible with the specific type. Compatible means that
    * can be used as a substituted type using xsi:type
    * In order for it to be compatible, it cannot be abstract, and
    * it must be derived by extension.
    * The ArrayList does not contain its own type + has the other types only once
    */
   private TreeMap<String, TreeSet<XSTypeDefinition>> typeTree;

   /**
    * Creates a new Schema2XForms object.
    *
    * @param action         _UNDOCUMENTED_
    * @param submitMethod   _UNDOCUMENTED_
    * @param formatCaption 
    */
   public Schema2XForms(final String action,
                        final SubmitMethod submitMethod,
                        final String base, final boolean formatCaption)
   {
      reset();

      this.formatCaption = formatCaption;
      this.action = action;
      this.submitMethod = submitMethod;
      this.base = base;
   }

   /**
    * Generate the XForm based on a user supplied XML Schema.
    *
    * @param instanceDocument The document source for the XML Schema.
    * @param schemaDocument Schema document
    * @param rootElementName Name of the root element
    * @param resourceBundle Strings to use
    * @return The Document containing the XForm.
    * @throws org.chiba.tools.schemabuilder.FormBuilderException
    *          If an error occurs building the XForm.
    */
   public Pair<Document, XSModel> buildXForm(final Document instanceDocument,
                              final Document schemaDocument,
                              String rootElementName,
                              final ResourceBundle resourceBundle)
      throws FormBuilderException
   {
      final XSModel schema = SchemaUtil.parseSchema(schemaDocument, true);
      this.typeTree = SchemaUtil.buildTypeTree(schema);

      //refCounter = 0;
      this.counter.clear();

      final Document xformsDocument = this.createFormTemplate(rootElementName);

      //find form element: last element created
      final Element formSection = (Element)
         xformsDocument.getDocumentElement().getLastChild();
      final Element modelSection = (Element)
         xformsDocument.getDocumentElement().getElementsByTagNameNS(NamespaceConstants.XFORMS_NS,
                                                                    "model").item(0);

      //add XMLSchema if we use schema types      
      final Element importedSchemaDocumentElement = (Element)
         xformsDocument.importNode(schemaDocument.getDocumentElement(), true);
      importedSchemaDocumentElement.setAttributeNS(null, "id", "schema-1");

      NodeList nl = importedSchemaDocumentElement.getChildNodes();
      
      for (int i = 0; i < nl.getLength(); i++)
      {
          Node current = nl.item(i);
          if (current.getNamespaceURI() != null && current.getNamespaceURI().equals(NamespaceConstants.XMLSCHEMA_NS))
          {
              String localName = current.getLocalName();
              if (localName.equals("include") || localName.equals("import"))
              {
                  importedSchemaDocumentElement.removeChild(current);
              }
          }
      }

      modelSection.appendChild(importedSchemaDocumentElement);

      //check if target namespace
      final StringList schemaNamespaces = schema.getNamespaces();
      final HashMap<String, String> schemaNamespacesMap = new HashMap<String, String>();
      if (schemaNamespaces.getLength() != 0)
      {
         // will return null if no target namespace was specified
         this.targetNamespace = schemaNamespaces.item(0);
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("[buildXForm] using targetNamespace " + this.targetNamespace);

         for (int i = 0; i < schemaNamespaces.getLength(); i++)
         {
            if (schemaNamespaces.item(i) == null)
            {
               continue;
            }
            final String prefix = addNamespace(xformsDocument.getDocumentElement(),
                                                    schemaDocument.lookupPrefix(schemaNamespaces.item(i)),
                                                    schemaNamespaces.item(i));
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[buildXForm] adding namespace " + schemaNamespaces.item(i) +
                            " with prefix " + prefix +
                            " to xform and default instance element");
            }
            schemaNamespacesMap.put(prefix, schemaNamespaces.item(i));
         }
      }

      //if target namespace & we use the schema types: add it to form ns declarations
//	if (this.targetNamespace != null && this.targetNamespace.length() != 0)
//	    envelopeElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
//					   "xmlns:schema",
//					   this.targetNamespace);


      final XSElementDeclaration rootElementDecl =
         schema.getElementDeclaration(rootElementName, this.targetNamespace);
      if (rootElementDecl == null)
      {
         throw new FormBuilderException("Invalid root element tag name ["
                                        + rootElementName
                                        + ", targetNamespace = "
                                        + this.targetNamespace
                                        + "]");
      }

      rootElementName = this.getElementName(rootElementDecl, xformsDocument);
      final Element instanceElement =
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":instance");
      modelSection.appendChild(instanceElement);
      this.setXFormsId(instanceElement);

      final Element defaultInstanceDocumentElement = xformsDocument.createElement(rootElementName);
      addNamespace(defaultInstanceDocumentElement,
                        NamespaceConstants.XMLSCHEMA_INSTANCE_PREFIX,
                        NamespaceConstants.XMLSCHEMA_INSTANCE_NS);
      if (this.targetNamespace != null)
      {
         final String targetNamespacePrefix = schemaDocument.lookupPrefix(this.targetNamespace);
         
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[buildXForm] adding target namespace " + this.targetNamespace +
                      " with prefix " + targetNamespacePrefix +
                      " to xform and default instance element");
         }
         
         addNamespace(defaultInstanceDocumentElement,
                           targetNamespacePrefix,
                           this.targetNamespace);
         addNamespace(xformsDocument.getDocumentElement(),
                           targetNamespacePrefix,
                           this.targetNamespace);
      }

      Element prototypeInstanceElement = null;
      if (instanceDocument == null || instanceDocument.getDocumentElement() == null)
      {
         instanceElement.appendChild(defaultInstanceDocumentElement);
      }
      else
      {         
         Element instanceDocumentElement = instanceDocument.getDocumentElement();
         if (!instanceDocumentElement.getNodeName().equals(rootElementName))
         {
            throw new IllegalArgumentException("instance document root tag name invalid.  " +
                                               "expected " + rootElementName +
                                               ", got " + instanceDocumentElement.getNodeName());
         }
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("[buildXForm] importing rootElement from other document");

         prototypeInstanceElement =
            xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                           NamespaceConstants.XFORMS_PREFIX + ":instance");
         modelSection.appendChild(prototypeInstanceElement);
         this.setXFormsId(prototypeInstanceElement, "instance_prototype");
         prototypeInstanceElement.appendChild(defaultInstanceDocumentElement);
      }

      final Element rootGroup = this.addElement(xformsDocument,
                                                modelSection,
                                                defaultInstanceDocumentElement,
                                                formSection,
                                                schema,
                                                rootElementDecl,
                                                "/" + this.getElementName(rootElementDecl, xformsDocument),
                                                new SchemaUtil.Occurrence(1, 1),
                                                resourceBundle);
      if (rootGroup.getNodeName() != NamespaceConstants.XFORMS_PREFIX + ":group")
      {
         throw new FormBuilderException("Expected root form element to be a " + NamespaceConstants.XFORMS_PREFIX + 
                                        ":group, not a " + rootGroup.getNodeName() + 
                                        ".  Ensure that " + this.getElementName(rootElementDecl, xformsDocument) +
                                        " is a concrete type that has no extensions.  " +
                                        "Types with extensions are not supported for " +
                                        "the root element of a form.");
      }
      this.setXFormsId(rootGroup, "alfresco-xforms-root-group");

      if (prototypeInstanceElement != null)
      {
         Schema2XForms.rebuildInstance(prototypeInstanceElement, instanceDocument,
               instanceElement, schemaNamespacesMap);
      }

      this.createSubmitElements(xformsDocument, modelSection, rootGroup);
      this.createTriggersForRepeats(xformsDocument, rootGroup);

      final Comment comment =
         xformsDocument.createComment("This XForm was generated by " + this.getClass().getName() +
                                      " on " + (new Date()) + " from the '" + rootElementName +
                                      "' element of the '" + this.targetNamespace + "' XML Schema.");
      xformsDocument.getDocumentElement().insertBefore(comment,
                                                       xformsDocument.getDocumentElement().getFirstChild());
      xformsDocument.normalizeDocument();
      
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[buildXForm] Returning XForm =\n" + XMLUtil.toString(xformsDocument));
      
      return new Pair<Document, XSModel>(xformsDocument, schema);
   }

   /**
    * Reset the Schema2XForms to default values.
    */
   public void reset()
   {
      this.counter.clear();
   }

   @SuppressWarnings("unchecked")
   public static void rebuildInstance(final Node prototypeNode, final Node oldInstanceNode,
         final Node newInstanceNode,

         final HashMap<String, String> schemaNamespaces)
   {
      final JXPathContext prototypeContext = JXPathContext.newContext(prototypeNode);
      prototypeContext.registerNamespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI);
      final JXPathContext instanceContext = JXPathContext.newContext(oldInstanceNode);
      instanceContext.registerNamespace(NamespaceService.ALFRESCO_PREFIX, NamespaceService.ALFRESCO_URI);

      for (final String prefix : schemaNamespaces.keySet())
      {
         prototypeContext.registerNamespace(prefix, schemaNamespaces.get(prefix));
         instanceContext.registerNamespace(prefix, schemaNamespaces.get(prefix));
      }

      // Evaluate non-recursive XPaths for all prototype elements at this level
      final Iterator<Pointer> it = prototypeContext.iteratePointers("*");
      while (it.hasNext())
      {
         final Pointer p = it.next();
         Element proto = (Element) p.getNode();
         String path = p.asPath();
         // check if this is a prototype element with the attribute set
         boolean isPrototype = proto.hasAttributeNS(NamespaceService.ALFRESCO_URI, "prototype")
               && proto.getAttributeNS(NamespaceService.ALFRESCO_URI, "prototype").equals("true");

         // We shouldn't locate a repeatable child with a fixed path
         if (isPrototype)
         {
            path = path.replaceAll("\\[(\\d+)\\]", "[position() >= $1]");
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[rebuildInstance] evaluating prototyped nodes " + path);
            }
         }
         else
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[rebuildInstance] evaluating child node with positional path " + path);
            }            
         }

         Document newInstanceDocument = newInstanceNode.getOwnerDocument(); 

         // Locate the corresponding nodes in the instance document
         List<Node> l = (List<Node>) instanceContext.selectNodes(path);
         
         // If the prototype node isn't a prototype element, copy it in as a missing node, complete with all its children. We won't need to recurse on this node
         if (l.isEmpty())
         {
            if (!isPrototype)
            {
               LOGGER.debug("[rebuildInstance] copying in missing node " + proto.getNodeName() + " to "
                     + XMLUtil.buildXPath(newInstanceNode, newInstanceDocument.getDocumentElement()));

               // Clone the prototype node and all its children
               Element clone = (Element)proto.cloneNode(true);
               newInstanceNode.appendChild(clone);
               
               if (oldInstanceNode instanceof Document)
               {
                  // add XMLSchema instance NS
                  addNamespace(clone, NamespaceConstants.XMLSCHEMA_INSTANCE_PREFIX,
                        NamespaceConstants.XMLSCHEMA_INSTANCE_NS);
               }               
            }
         }
         else
         {
            // Otherwise, append the matches from the old instance document in order
            for (Node old : l)
            {
               Element oldEl = (Element)old;
   
               // Copy the old instance element rather than cloning it, so we don't copy over attributes
               Element clone = null; 
               String nSUri = oldEl.getNamespaceURI(); 
               if (nSUri == null) 
               { 
                  clone = newInstanceDocument.createElement(oldEl.getTagName()); 
               } 
               else 
               {  
                  clone = newInstanceDocument.createElementNS(nSUri, oldEl.getTagName()); 
               }
               newInstanceNode.appendChild(clone);
               
               if (oldInstanceNode instanceof Document)
               {
                  // add XMLSchema instance NS
                  addNamespace(clone, NamespaceConstants.XMLSCHEMA_INSTANCE_PREFIX,
                        NamespaceConstants.XMLSCHEMA_INSTANCE_NS);
               }
               
               // Copy over child text if this is not a complex type
               boolean isEmpty = true;
               for (Node n = old.getFirstChild(); n != null; n = n.getNextSibling())
               {
                  if (n instanceof Text)
                  {
                     clone.appendChild(newInstanceDocument.importNode(n, false));
                     isEmpty = false;
                  }
                  else if (n instanceof Element)
                  {
                     break;
                  }
               }

               // Check the nil attribute
               if (oldEl.getAttributeNS(NamespaceConstants.XMLSCHEMA_INSTANCE_NS, "nil").equals("true"))
               {
                  clone.setAttributeNS(NamespaceConstants.XMLSCHEMA_INSTANCE_NS,
                        NamespaceConstants.XMLSCHEMA_INSTANCE_PREFIX + ":nil", "true");
               }
               
               // Copy over attributes present in the prototype
               NamedNodeMap attributes = proto.getAttributes();
               for (int i = 0; i < attributes.getLength(); i++)
               {
                  Attr attribute = (Attr) attributes.item(i);
                  String localName = attribute.getLocalName();
                  if (localName == null)
                  {
                     String name = attribute.getName();
                     if (oldEl.hasAttribute(name))
                     {
                        clone.setAttributeNode((Attr) newInstanceDocument.importNode(oldEl.getAttributeNode(name),
                              false));
                     }
                     else
                     {
                        LOGGER.debug("[rebuildInstance] copying in missing attribute " + attribute.getNodeName()
                              + " to " + XMLUtil.buildXPath(clone, newInstanceDocument.getDocumentElement()));

                        clone.setAttributeNode((Attr) attribute.cloneNode(false));
                     }
                  }
                  else
                  {
                     String namespace = attribute.getNamespaceURI();
                     if (!((!isEmpty
                           && (namespace.equals(NamespaceConstants.XMLSCHEMA_INSTANCE_NS) && localName.equals("nil")) || (namespace
                           .equals(NamespaceService.ALFRESCO_URI) && localName.equals("prototype")))))
                     {
                        if (oldEl.hasAttributeNS(namespace, localName))
                        {
                           clone.setAttributeNodeNS((Attr) newInstanceDocument.importNode(oldEl.getAttributeNodeNS(
                                 namespace, localName), false));
                        }
                        else
                        {
                           LOGGER.debug("[rebuildInstance] copying in missing attribute " + attribute.getNodeName()
                                 + " to " + XMLUtil.buildXPath(clone, newInstanceDocument.getDocumentElement()));

                           clone.setAttributeNodeNS((Attr) attribute.cloneNode(false));
                        }
                     }
                  }
               }

               // recurse on children
               rebuildInstance(proto, oldEl, clone, schemaNamespaces);
            }
         }

         // Now add in a new copy of the prototype
         if (isPrototype)
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[rebuildInstance] appending "
                     + proto.getNodeName()
                     + " to "
                     + XMLUtil.buildXPath(newInstanceNode, newInstanceDocument
                           .getDocumentElement()));
            }
            newInstanceNode.appendChild(proto.cloneNode(true));
         }         
      }
   }

   public static void removePrototypeNodes(final Node instanceDocumentElement)
   {
      final Map<String, LinkedList<Element>> prototypes =
         new HashMap<String, LinkedList<Element>>();
      final NodeList children = instanceDocumentElement.getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         if (! (children.item(i) instanceof Element))
         {
            continue;
         }
         final String nodeName = children.item(i).getNodeName();
         if (! prototypes.containsKey(nodeName))
         {
            prototypes.put(nodeName, new LinkedList<Element>());
         }
         prototypes.get(nodeName).add((Element)children.item(i));
      }

      for (LinkedList<Element> l : prototypes.values())
      {
         for (Element e : l)
         {
            if (e.hasAttributeNS(NamespaceService.ALFRESCO_URI, "prototype"))
            {
               assert "true".equals(e.getAttributeNS(NamespaceService.ALFRESCO_URI,
                                                     "prototype"));
               e.removeAttributeNS(NamespaceService.ALFRESCO_URI, "prototype");

               if (l.getLast().equals(e))
               {
                  e.getParentNode().removeChild(e);
               }
            }
            if (e.getParentNode() != null)
            {
               Schema2XForms.removePrototypeNodes(e);
            }
         }
      }
   }

   protected String setXFormsId(final Element el)
   {
      return this.setXFormsId(el, null);
   }

   protected String setXFormsId(final Element el, String id)
   {
      if (el.hasAttributeNS(null, "id"))
      {
         el.removeAttributeNS(null, "id");
      }
      if (id == null)
      {
         final String name = el.getLocalName();
         final Long l = this.counter.get(name);
         final long count = (l != null) ? l : 0;

         // increment the counter
         this.counter.put(name, new Long(count + 1));

         id = name + "_" + count;
      }
      el.setAttributeNS(null, "id", id);
      return id;
   }

   /**
    * method to set an Id to this element and to all XForms descendants of this element
    */
   private void resetXFormIds(final Element newControl)
   {
      if (newControl.getNamespaceURI() != null &&
          newControl.getNamespaceURI().equals(NamespaceConstants.XFORMS_NS))
      {
         this.setXFormsId(newControl);
      }

      //recursive call
      final NodeList children = newControl.getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         final Node child = children.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
         {
            this.resetXFormIds((Element) child);
         }
      }
   }

   /**
    * _UNDOCUMENTED_
    *
    * @param xForm          _UNDOCUMENTED_
    * @param choicesElement _UNDOCUMENTED_
    * @param choiceValues   _UNDOCUMENTED_
    */
   protected void addChoicesForSelectControl(final Document xForm,
                                             final Element choicesElement,
                                             final Map<String, XSAnnotation> choiceValues,
                                             final ResourceBundle resourceBundle)
   {
      for (Map.Entry<String, XSAnnotation> choice : choiceValues.entrySet())
      {
         final Element item = this.createXFormsItem(xForm,
                                                    this.createCaption(choice.getKey(),
                                                                       choice.getValue(),
                                                                       resourceBundle),
                                                    choice.getKey());
         choicesElement.appendChild(item);
      }
   }

   /**
    */
   protected Map<String, Element> addChoicesForSelectSwitchControl(final Document xformsDocument,
                                                                   final Element formSection,
                                                                   final List<XSTypeDefinition> choiceValues,
                                                                   final String typeBindId)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addChoicesForSelectSwitchControl] values = ");
         for (XSTypeDefinition type : choiceValues)
         {
            LOGGER.debug("  - " + type.getName());
         }
      }

      final Map<String, Element> result = new HashMap<String, Element>();
      for (XSTypeDefinition type : choiceValues)
      {
         final String textValue = type.getName();
         
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addChoicesForSelectSwitchControl] processing " + textValue);
         }

         //build the case element
         final Element caseElement = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                                    NamespaceConstants.XFORMS_PREFIX + ":case");
         caseElement.appendChild(this.createLabel(xformsDocument, textValue));
         final String caseId = this.setXFormsId(caseElement);
         result.put(textValue, caseElement);

         final Element toggle = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                               NamespaceConstants.XFORMS_PREFIX + ":toggle");
         this.setXFormsId(toggle);
         toggle.setAttributeNS(NamespaceConstants.XFORMS_NS,
                               NamespaceConstants.XFORMS_PREFIX + ":case",
                               caseId);

         final Element setValue = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                                 NamespaceConstants.XFORMS_PREFIX + ":setvalue");
         setValue.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                 NamespaceConstants.XFORMS_PREFIX + ":bind",
                                 typeBindId);
         setValue.appendChild(xformsDocument.createTextNode(textValue));

         formSection.appendChild(this.createTrigger(xformsDocument,
                                                    null,
                                                    typeBindId,
                                                    textValue,
                                                    toggle,
                                                    setValue));
      }
      return result;
   }

   private static String extractPropertyFromAnnotation(final String namespace,
                                                       final String elementName,
                                                       final XSAnnotation annotation,
                                                       final ResourceBundle resourceBundle)
   {
      if (annotation == null)
      {
         return null;
      }
      // write annotation to empty doc
      final Document doc = XMLUtil.newDocument();
      annotation.writeAnnotation(doc, XSAnnotation.W3C_DOM_DOCUMENT);

      final NodeList d = doc.getElementsByTagNameNS(namespace, elementName);
      if (d.getLength() == 0)
      {
         return null;
      }
      if (d.getLength() > 1)
      {
         LOGGER.warn("[extractPropertyFromAnnotation] expected exactly one value for " + namespace +
                     ":" + elementName +
                     ". found " + d.getLength());
      }
      String result = DOMUtil.getTextNodeAsString(d.item(0));
      
      if (LOGGER.isDebugEnabled())
         LOGGER.debug(namespace + ":" + elementName + " = " + result);
      
      if (result.startsWith("${") && result.endsWith("}") && resourceBundle != null)
      {
         result = result.substring("${".length(), result.length() - "}".length());
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("[extractPropertyFromAnnotation] looking up key " + result + " in bundle " + resourceBundle);
         
         try
         {
            result = resourceBundle.getString(result);
         }
         catch (MissingResourceException mse)
         {
            if (LOGGER.isDebugEnabled())
               LOGGER.debug("[extractPropertyFromAnnotation] unable to find key " + result, mse);
            
            result = "$$" + result + "$$";
         }
      }
      return result;
   }

   private void addAttributeSet(final Document xformsDocument,
                                final Element modelSection,
                                final Element defaultInstanceElement,
                                final Element formSection,
                                final XSModel schema,
                                final XSComplexTypeDefinition controlType,
                                final XSElementDeclaration owner,
                                final String pathToRoot,
                                final boolean checkIfExtension,
                                final ResourceBundle resourceBundle)
      throws FormBuilderException
   {
      XSObjectList attrUses = controlType.getAttributeUses();

      if (attrUses == null)
      {
         return;
      }
      for (int i = 0; i < attrUses.getLength(); i++)
      {
         final XSAttributeUse currentAttributeUse = (XSAttributeUse)attrUses.item(i);
         final XSAttributeDeclaration currentAttribute =
            currentAttributeUse.getAttrDeclaration();

         String attributeName = currentAttributeUse.getName();
         if (attributeName == null || attributeName.length() == 0)
         {
            attributeName = currentAttributeUse.getAttrDeclaration().getName();
         }

         //test if extended !
         if (checkIfExtension &&
             SchemaUtil.doesAttributeComeFromExtension(currentAttributeUse, controlType))
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[addAttributeSet] This attribute comes from an extension: recopy form controls. Model section =\n" + 
                            XMLUtil.toString(modelSection));
            }

            //find the existing bind Id
            //(modelSection is the enclosing bind of the element)
            final NodeList binds = modelSection.getElementsByTagNameNS(NamespaceConstants.XFORMS_NS, "bind");
            String bindId = null;
            for (int j = 0; j < binds.getLength() && bindId == null; j++)
            {
               Element bind = (Element) binds.item(j);
               String nodeset = bind.getAttributeNS(NamespaceConstants.XFORMS_NS, "nodeset");
               if (nodeset != null)
               {
                  //remove "@" in nodeset
                  String name = nodeset.substring(1);
                  if (name.equals(attributeName))
                  {
                     bindId = bind.getAttributeNS(null, "id");
                  }
               }
            }

            //find the control
            Element control = null;
            if (bindId != null)
            {
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("[addAttributeSet] bindId found: " + bindId);

               JXPathContext context = JXPathContext.newContext(formSection.getOwnerDocument());
               final Pointer pointer =
                  context.getPointer("//*[@" + NamespaceConstants.XFORMS_PREFIX + ":bind='" + bindId + "']");
               if (pointer != null)
               {
                  control = (Element)pointer.getNode();
               }
               else if (LOGGER.isDebugEnabled())
               {
                  LOGGER.debug("[addAttributeSet] unable to resolve pointer for: //*[@" + NamespaceConstants.XFORMS_PREFIX + ":bind='" + bindId + "']");
               }
            }
            
            if (LOGGER.isDebugEnabled())
            {
               if (control == null)
               {
                  LOGGER.debug("[addAttributeSet] control = <not found>");
               }
               else
               {
                  LOGGER.debug("[addAttributeSet] control = " + control.getTagName());
               }
            }
            
            //copy it
            if (control == null)
            {
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("Corresponding control not found");
            }
            else
            {
               Element newControl = (Element) control.cloneNode(true);
               //set new Ids to XForm elements
               this.resetXFormIds(newControl);

               formSection.appendChild(newControl);
            }
         }
         else
         {
            String attrNamespace = currentAttribute.getNamespace();
            String namespacePrefix = "";
            if (attrNamespace != null && attrNamespace.length() > 0)
            {
               String prefix =NamespaceResolver.getPrefix(xformsDocument.getDocumentElement(), attrNamespace);
               if (prefix!= null && prefix.length() > 0)
               {
                  namespacePrefix =  prefix + ":";
               }
            }
            
            final String newPathToRoot =
               (pathToRoot == null || pathToRoot.length() == 0
                ? "@" + namespacePrefix + currentAttribute.getName()
                : (pathToRoot.endsWith("/")
                   ? pathToRoot + "@" + namespacePrefix + currentAttribute.getName()
                   : pathToRoot + "/@"+ namespacePrefix + currentAttribute.getName()));

            if (LOGGER.isDebugEnabled())
               LOGGER.debug("[addAttributeSet] adding attribute " + attributeName +" at " + newPathToRoot);
            
            try
            {
               String defaultValue = (currentAttributeUse.getConstraintType() == XSConstants.VC_NONE
                                            ? null
                                            : currentAttributeUse.getConstraintValue());
               // make sure boolean attributes have a default value
               if (defaultValue == null && "boolean".equals(currentAttribute.getTypeDefinition().getName()))
               {
                   defaultValue = "false";
               }
               
               if (namespacePrefix.length() > 0)
               {
                  defaultInstanceElement.setAttributeNS(this.targetNamespace,
                                                     attributeName,
                                                     defaultValue);
               }
               else
               {
                  defaultInstanceElement.setAttribute(attributeName, defaultValue);
               }
            }
            catch (Exception e)
            {
               throw new FormBuilderException("error retrieving default value for attribute " +
                                              attributeName + " at " + newPathToRoot, e);
            }
            this.addSimpleType(xformsDocument,
                               modelSection,
                               formSection,
                               schema,
                               currentAttribute.getTypeDefinition(),
                               currentAttributeUse,
                               newPathToRoot,
                               resourceBundle);
         }
      }
   }

   private Element addComplexType(final Document xformsDocument,
                                  Element modelSection,
                                  final Element defaultInstanceElement,
                                  final Element formSection,
                                  final XSModel schema,
                                  final XSComplexTypeDefinition controlType,
                                  final XSElementDeclaration owner,
                                  String pathToRoot,
                                  final SchemaUtil.Occurrence occurs,
                                  boolean relative,
                                  final boolean checkIfExtension,
                                  final ResourceBundle resourceBundle)
      throws FormBuilderException
   {
      if (controlType == null)
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addComplexType] addComplexType control type is null for pathToRoot = "
                         + pathToRoot);
         }
         return null;
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addComplexType] Start addComplexType for " + controlType.getName() + " (" + pathToRoot + ")," +
                      " owner = " + (owner == null ? "null" : owner.getName()));
      }

      // add a group node and recurse
      final Element groupElement = this.createGroup(xformsDocument,
                                                    modelSection,
                                                    formSection,
                                                    owner,
                                                    resourceBundle);
//      final SchemaUtil.Occurrence o = SchemaUtil.getOccurrence(owner);
      final Element repeatSection = this.addRepeatIfNecessary(xformsDocument,
                                                              modelSection,
                                                              groupElement,
                                                              controlType,
                                                              pathToRoot,
                                                              occurs);
      if (repeatSection != groupElement)
      {
         groupElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                     NamespaceConstants.XFORMS_PREFIX + ":appearance",
                                     "repeated");

         // we have a repeat
         relative = true;
      }

      if (controlType.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED ||
          (controlType.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE &&
           controlType.getAttributeUses() != null &&
           controlType.getAttributeUses().getLength() > 0))
      {
         XSTypeDefinition base = controlType.getBaseType();
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addComplexType] Control type is mixed, base type = " + base.getName());
         }

         if (base != null && base != controlType)
         {
            if (base.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE)
            {
               this.addSimpleType(xformsDocument,
                                  modelSection,
                                  repeatSection,
                                  schema,
                                  (XSSimpleTypeDefinition)base,
                                  owner.getName(),
                                  owner,
                                  pathToRoot,
                                  occurs,
                                  resourceBundle);
            }
            else
            {
               LOGGER.warn("[addComplexType] addComplexTypeChildren for mixed type with basic type complex!");
            }
         }
      }
      else if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addComplexType] Control content type = " + controlType.getContentType());
      }

      // check for compatible subtypes
      // of controlType.
      // add a type switch if there are any
      // compatible sub-types (i.e. anything
      // that derives from controlType)
      // add child elements
      if (relative)
      {
         pathToRoot = "";

         //modelSection: find the last element put in the modelSection = bind
         modelSection = DOMUtil.getLastChildElement(modelSection);
      }

      //attributes
      this.addAttributeSet(xformsDocument,
                           modelSection,
                           defaultInstanceElement,
                           repeatSection,
                           schema,
                           controlType,
                           owner,
                           pathToRoot,
                           checkIfExtension,
                           resourceBundle);

      //process group
      final XSParticle particle = controlType.getParticle();
      if (particle != null)
      {
         final XSTerm term = particle.getTerm();
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addComplexType] Particle of " + controlType.getName() +
                         " is" + (term instanceof XSModelGroup ? "" : " not") +
                         " a group: " + term.getClass().getName());
         }

         if (term instanceof XSModelGroup)
         {

            switch (((XSModelGroup)term).getCompositor())
            {
            case XSModelGroup.COMPOSITOR_CHOICE:
               LOGGER.warn("term " + term.getName() + " of particle " + particle.getName() +
                           " of type " + controlType.getName() + " in " + owner.getName() +
                           " describes a " + NamespaceConstants.XMLSCHEMA_PREFIX + 
                           ":choice which is not yet supported, adding it as a " +
                           NamespaceConstants.XMLSCHEMA_PREFIX + ":sequence");
               break;
            case XSModelGroup.COMPOSITOR_ALL:
               LOGGER.warn("term " + term.getName() + " of particle " + particle.getName() +
                           " of type " + controlType.getName() + " in " + owner.getName() +
                           " describes a " + NamespaceConstants.XMLSCHEMA_PREFIX + 
                           ":all which is not yet supported, adding it as a " +
                           NamespaceConstants.XMLSCHEMA_PREFIX + ":sequence");
               break;
            case XSModelGroup.COMPOSITOR_SEQUENCE:
               break;
            }
            //call addGroup on this group
            this.addGroup(xformsDocument,
                          modelSection,
                          defaultInstanceElement,
                          repeatSection,
                          schema,
                          (XSModelGroup)term,
                          controlType,
                          owner,
                          pathToRoot,
                          new SchemaUtil.Occurrence(particle),
                          checkIfExtension,
                          resourceBundle);
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addComplexType] End of addComplexType for " + controlType.getName());
      }
      
      return groupElement;
   }

   /**
    * add an element to the XForms document: the bind + the control
    * (only the control if "withBind" is false)
    */
   private Element addElement(final Document xformsDocument,
                              final Element modelSection,
                              final Element defaultInstanceElement,
                              final Element formSection,
                              final XSModel schema,
                              final XSElementDeclaration elementDecl,
                              final String pathToRoot,
                              final SchemaUtil.Occurrence occurs,
                              final ResourceBundle resourceBundle)
      throws FormBuilderException
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[addElement] adding element " + elementDecl + " at path " + pathToRoot);

      XSTypeDefinition controlType = elementDecl.getTypeDefinition();
      if (controlType == null)
      {
         // TODO!!! Figure out why this happens... for now just warn...
         // seems to happen when there is an element of type IDREFS
         LOGGER.warn("WARNING!!! controlType is null for " + elementDecl +
                     ", " + elementDecl.getName());
         return null;
      }

      if (controlType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE &&
          ((XSComplexTypeDefinition)controlType).getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE)
      {
         controlType = ((XSComplexTypeDefinition)controlType).getSimpleType();
      }

      if (controlType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE)
      {
         return this.addSimpleType(xformsDocument,
                                   modelSection,
                                   formSection,
                                   schema,
                                   (XSSimpleTypeDefinition) controlType,
                                   elementDecl.getName(),
                                   elementDecl,
                                   pathToRoot,
                                   occurs,
                                   resourceBundle);
      }

      if (controlType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE &&
          "anyType".equals(controlType.getName()))
      {
         return this.addSimpleType(xformsDocument,
                                   modelSection,
                                   formSection,
                                   schema,
                                   (XSComplexTypeDefinition)controlType,
                                   elementDecl.getName(),
                                   elementDecl,
                                   pathToRoot,
                                   occurs,
                                   resourceBundle);
      }

      if (controlType.getTypeCategory() != XSTypeDefinition.COMPLEX_TYPE)
      {
         throw new FormBuilderException("Unsupported type [" + elementDecl.getType() +
                                        "] for node [" + controlType.getName() + "]");
      }

      boolean relative = true;
      final String typeName = controlType.getName();
      final boolean typeIsAbstract = ((XSComplexTypeDefinition)controlType).getAbstract();
      final TreeSet<XSTypeDefinition> compatibleTypes =
         typeName != null ? this.typeTree.get(typeName) : null;

      if (compatibleTypes == null && typeName != null && LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addElement] No compatible type found for " + typeName);
      }

      if (typeName != null && compatibleTypes != null)
      {
         relative = false;

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addElement] compatible types for " + typeName + ":");
            for (XSTypeDefinition compType : compatibleTypes)
            {
               LOGGER.debug("[addElement]   compatible type name = " + compType.getName());
            }
         }

         if (!typeIsAbstract && compatibleTypes.size() == 0)
         {
            relative = true;
         }
         else if (typeIsAbstract && compatibleTypes.size() == 1)
         {
            // only one compatible type, set the controlType value
            // and fall through
            controlType = compatibleTypes.first();
            relative = true;
         }
         else if (typeIsAbstract && compatibleTypes.size() == 0)
         {
            // name not null but no compatibleType?
            relative = false;
         }
         else
         {
            return this.addElementWithMultipleCompatibleTypes(xformsDocument,
                                                              modelSection,
                                                              defaultInstanceElement,
                                                              formSection,
                                                              schema,
                                                              elementDecl,
                                                              compatibleTypes,
                                                              pathToRoot,
                                                              resourceBundle,
                                                              occurs);
         }
      }

      if (!relative)
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("[addElement] bind is not relative for " + elementDecl.getName());
      }
      else
      {
//         final SchemaUtil.Occurrence occurs = SchemaUtil.getOccurrence(elementDecl);
         //create the bind in case it is a repeat
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addElement] Adding empty bind for control " + controlType +
                         " type " + typeName + 
                         " nodeset " + pathToRoot +
                         " occurs " + occurs);
         }
         
         // create the <xforms:bind> element and add it to the model.
         boolean isRepeated = isRepeated(occurs, controlType);
         final Element bindElement = 
            this.createBind(xformsDocument, 
                            pathToRoot + (isRepeated ? "[position() != last()]" : ""));

         modelSection.appendChild(bindElement);
         this.startBindElement(bindElement,
                               schema,
                               controlType,
                               null,
                               occurs);
      }
      return this.addComplexType(xformsDocument,
                                 modelSection,
                                 defaultInstanceElement,
                                 formSection,
                                 schema,
                                 (XSComplexTypeDefinition)controlType,
                                 elementDecl,
                                 pathToRoot,
                                 occurs,
                                 true,
                                 false,
                                 resourceBundle);
   }

   private Element addElementWithMultipleCompatibleTypes(final Document xformsDocument,
                                                         final Element modelSection,
                                                         final Element defaultInstanceElement,
                                                         final Element formSection,
                                                         final XSModel schema,
                                                         final XSElementDeclaration elementDecl,
                                                         final TreeSet<XSTypeDefinition> compatibleTypes,
                                                         final String pathToRoot,
                                                         final ResourceBundle resourceBundle,
                                                         final SchemaUtil.Occurrence occurs)
      throws FormBuilderException
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[addElementWithMultipleCompatibleTypes] adding element " + elementDecl + " at path " + pathToRoot);
      
      // look for compatible types
      final XSTypeDefinition controlType = elementDecl.getTypeDefinition();

      //get possible values
      final List<XSTypeDefinition> enumValues = new LinkedList<XSTypeDefinition>();
      //add the type (if not abstract)
      if (!((XSComplexTypeDefinition)controlType).getAbstract())
      {
         enumValues.add(controlType);
      }

      //add compatible types
      enumValues.addAll(compatibleTypes);

      // multiple compatible types for this element exist
      // in the schema - allow the user to choose from
      // between compatible non-abstract types
      boolean isRepeated = isRepeated(occurs, controlType);
      Element bindElement = this.createBind(xformsDocument, pathToRoot + "/@xsi:type");
      String bindId = bindElement.getAttributeNS(null, "id");
      modelSection.appendChild(bindElement);
      this.startBindElement(bindElement, schema, controlType, null, occurs);

      //add the "element" bind, in addition
      final Element bindElement2 = this.createBind(xformsDocument, 
                  pathToRoot + (isRepeated ? "[position() != last()]" : ""));
      modelSection.appendChild(bindElement2);
      this.startBindElement(bindElement2, schema, controlType, null, occurs);

      // add content to select1
      final Map<String, Element> caseTypes =
         this.addChoicesForSelectSwitchControl(xformsDocument, formSection, enumValues, bindId);

      //add switch
      final Element switchElement = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                                   NamespaceConstants.XFORMS_PREFIX + ":switch");
      switchElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                   NamespaceConstants.XFORMS_PREFIX + ":bind",
                                   bindId);
      switchElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                   NamespaceConstants.XFORMS_PREFIX + ":appearance",
                                   "full");

      formSection.appendChild(switchElement);

      if (!((XSComplexTypeDefinition)controlType).getAbstract())
      {
         final Element firstCaseElement = caseTypes.get(controlType.getName());
         switchElement.appendChild(firstCaseElement);
         final Element firstGroupElement = this.addComplexType(xformsDocument,
                                                               modelSection,
                                                               defaultInstanceElement,
                                                               firstCaseElement,
                                                               schema,
                                                               (XSComplexTypeDefinition)controlType,
                                                               elementDecl,
                                                               pathToRoot,
                                                               SchemaUtil.getOccurrence(elementDecl),
                                                               true,
                                                               false,
                                                               resourceBundle);
         firstGroupElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                          NamespaceConstants.XFORMS_PREFIX + ":appearance",
                                          "");
      }

      defaultInstanceElement.setAttributeNS(NamespaceConstants.XMLSCHEMA_INSTANCE_NS,
                                            NamespaceConstants.XMLSCHEMA_INSTANCE_PREFIX + ":type",
                                            (((XSComplexTypeDefinition)controlType).getAbstract()
                                             ? compatibleTypes.first().getName()
                                             : controlType.getName()));
      defaultInstanceElement.setAttributeNS(NamespaceConstants.XMLSCHEMA_INSTANCE_NS,
                                            NamespaceConstants.XMLSCHEMA_INSTANCE_PREFIX + ":nil",
                                            "true");

      /////////////// add sub types //////////////
      // add each compatible type within
      // a case statement
      for (final XSTypeDefinition type : compatibleTypes)
      {
         final String compatibleTypeName = type.getName();

         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug(type == null
                         ? ("[addElementWithMultipleCompatibleTypes] compatible type is null!! type = " +
                            compatibleTypeName + ", targetNamespace = " + this.targetNamespace)
                         : ("[addElementWithMultipleCompatibleTypes] adding compatible type " + type.getName()));
         }
         
         if (type == null || type.getTypeCategory() != XSTypeDefinition.COMPLEX_TYPE)
         {
            continue;
         }

         final Element caseElement = caseTypes.get(type.getName());
         switchElement.appendChild(caseElement);

         final Element groupElement = this.addComplexType(xformsDocument,
                                                          modelSection,
                                                          defaultInstanceElement,
                                                          caseElement,
                                                          schema,
                                                          (XSComplexTypeDefinition) type,
                                                          elementDecl,
                                                          pathToRoot,
                                                          SchemaUtil.getOccurrence(elementDecl),
                                                          true,
                                                          true,
                                                          resourceBundle);
         groupElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                     NamespaceConstants.XFORMS_PREFIX + ":appearance",
                                     "");

         // modify bind to add a "relevant" attribute that checks the value of @xsi:type
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addElementWithMultipleCompatibleTypes] Model section =\n" + XMLUtil.toString(bindElement2));
         }

         final NodeList binds = bindElement2.getElementsByTagNameNS(NamespaceConstants.XFORMS_NS, "bind");
         for (int i = 0; i < binds.getLength(); i++)
         {
            final Element subBind = (Element) binds.item(i);
            String name = subBind.getAttributeNS(NamespaceConstants.XFORMS_NS, "nodeset");

            // ETHREEOH-3308 fix
            name = repeatableNamePattern.matcher(name).replaceAll("");
            
            if (!subBind.getParentNode().getAttributes().getNamedItem("id").getNodeValue().equals(
                    bindElement2.getAttribute("id")))
            {
                continue;
            }

            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[addElementWithMultipleCompatibleTypes] Testing sub-bind with nodeset " + name);
            }

            if (!SchemaUtil.isElementDeclaredIn(name, (XSComplexTypeDefinition) type, false) &&
                !SchemaUtil.isAttributeDeclaredIn(name, (XSComplexTypeDefinition) type, false))
            {
               continue;
            }
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[addElementWithMultipleCompatibleTypes] Element/Attribute " + name +
                            " declared in type " + type.getName() +
                            ": adding relevant attribute");
            }

            //test sub types of this type
            //TreeSet subCompatibleTypes = (TreeSet) typeTree.get(type);

            String newRelevant = "../@xsi:type='" + type.getName() + "'";
            if (this.typeTree.containsKey(type.getName()))
            {
               for (XSTypeDefinition otherType : this.typeTree.get(type.getName()))
               {
                  newRelevant = newRelevant + " or ../@xsi:type='" + otherType.getName() + "'";
               }
            }

            //change relevant attribute
            final String relevant = subBind.getAttributeNS(NamespaceConstants.XFORMS_NS, "relevant");
            if (relevant != null && relevant.length() != 0)
            {
               newRelevant = ("(" + relevant +
                              ") and " + newRelevant);
            }
            subBind.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                   NamespaceConstants.XFORMS_PREFIX + ":relevant",
                                   newRelevant);
         }
      }
      return switchElement;
   }

   /**
    */
   private void addGroup(final Document xformsDocument,
                         final Element modelSection,
                         final Element defaultInstanceElement,
                         final Element formSection,
                         final XSModel schema,
                         final XSModelGroup group,
                         final XSComplexTypeDefinition controlType,
                         final XSElementDeclaration owner,
                         final String pathToRoot,
                         final SchemaUtil.Occurrence occurs,
                         final boolean checkIfExtension,
                         final ResourceBundle resourceBundle)
      throws FormBuilderException
   {
      if (group == null)
      {
         return;
      }
      
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addGroup] Start of addGroup, from owner = " + owner.getName() +
                      " and controlType = " + controlType.getName());
         LOGGER.debug("[addGroup] group before =\n" + XMLUtil.toString(formSection));
      }
      
      final Element repeatSection = this.addRepeatIfNecessary(xformsDocument,
                                                              modelSection,
                                                              formSection,
                                                              owner.getTypeDefinition(),
                                                              pathToRoot,
                                                              occurs);
      
      final XSObjectList particles = group.getParticles();
      for (int counter = 0; counter < particles.getLength(); counter++)
      {
         final XSParticle currentNode = (XSParticle)particles.item(counter);
         XSTerm term = currentNode.getTerm();
         final SchemaUtil.Occurrence childOccurs = new SchemaUtil.Occurrence(currentNode);
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addGroup] next term = " + term.getName() +
                         ", occurs = " + childOccurs);
         }

         if (term instanceof XSModelGroup)
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[addGroup] term is a group");
            }

            this.addGroup(xformsDocument,
                          modelSection,
                          defaultInstanceElement,
                          repeatSection,
                          schema,
                          ((XSModelGroup) term),
                          controlType,
                          owner,
                          pathToRoot,
                          childOccurs,
                          checkIfExtension,
                          resourceBundle);
         }
         else if (term instanceof XSElementDeclaration)
         {
            XSElementDeclaration element = (XSElementDeclaration) term;

            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("[addGroup] term is an element declaration: "
                            + term.getName());
            }

            //special case for types already added because used in an extension
            //do not add it when it comes from an extension !!!
            //-> make a copy from the existing form control
            if (checkIfExtension &&
                SchemaUtil.doesElementComeFromExtension(element, controlType))
            {
               if (LOGGER.isDebugEnabled())
               {
                  LOGGER.debug("[addGroup] This element comes from an extension: recopy form controls. Model Section =\n" +
                               XMLUtil.toString(modelSection));
               }

               //find the existing bind Id
               //(modelSection is the enclosing bind of the element)
               NodeList binds = modelSection.getElementsByTagNameNS(NamespaceConstants.XFORMS_NS, "bind");
               String bindId = null;
               for (int i = 0; i < binds.getLength() && bindId == null; i++)
               {
                  Element bind = (Element)binds.item(i);
                  String nodeset = bind.getAttributeNS(NamespaceConstants.XFORMS_NS, "nodeset");
                  if (nodeset != null && nodeset.equals(element.getName()))
                  {
                     bindId = bind.getAttributeNS(null, "id");
                  }
               }
               
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("[addGroup] found bindId " + bindId + " for element " + element.getName());
               
               //find the control
               Element control = null;
               if (bindId != null)
               {
                  if (LOGGER.isDebugEnabled())
                  {
                     LOGGER.debug("[addGroup] bindId found: " + bindId);
                  }

                  final JXPathContext context =
                     JXPathContext.newContext(formSection.getOwnerDocument());
                  final Pointer pointer =
                     context.getPointer("//*[@" + NamespaceConstants.XFORMS_PREFIX + ":bind='" + bindId + "']");
                  if (pointer != null)
                  {
                     control = (Element) pointer.getNode();
                  }
                  else if (LOGGER.isDebugEnabled())
                  {
                     LOGGER.debug("[addGroup] unable to resolve pointer for: //*[@" + NamespaceConstants.XFORMS_PREFIX + ":bind='" + bindId + "']");
                  }
               }

               if (LOGGER.isDebugEnabled())
               {
                  if (control == null)
                  {
                     LOGGER.debug("[addGroup] control = <not found>");
                  }
                  else
                  {
                     LOGGER.debug("[addGroup] control = " + control.getTagName());
                  }
               }
               
               //copy it
               if (control == null)
               {
                  if (LOGGER.isDebugEnabled())
                     LOGGER.debug("Corresponding control not found");
                  
                  this.addElementToGroup(xformsDocument,
                                         modelSection,
                                         defaultInstanceElement,
                                         repeatSection,
                                         schema,
                                         element,
                                         pathToRoot,
                                         childOccurs,
                                         resourceBundle);
               }
               else
               {
                  Element newControl = (Element)control.cloneNode(true);
                  //set new Ids to XForm elements
                  this.resetXFormIds(newControl);

                  repeatSection.appendChild(newControl);
               }
            }
            else
            {
               this.addElementToGroup(xformsDocument,
                                      modelSection,
                                      defaultInstanceElement,
                                      repeatSection,
                                      schema,
                                      element,
                                      pathToRoot,
                                      childOccurs,
                                      resourceBundle);
            }
         }
         else
         {
            LOGGER.warn("Unhandled term " + term + 
                        " found in group from " + owner.getName() + 
                        " for pathToRoot = " + pathToRoot);
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addGroup] group after =\n" + XMLUtil.toString(formSection));
         LOGGER.debug("[addGroup] End of addGroup, from owner = " + owner.getName() +
                      " and controlType = " + controlType.getName());
      }
   }
 
   @SuppressWarnings("unchecked")
   private void addElementToGroup(final Document xformsDocument,
                                  final Element modelSection,
                                  final Element defaultInstanceElement,
                                  final Element formSection,
                                  final XSModel schema,
                                  final XSElementDeclaration element,
                                  final String pathToRoot,
                                  final SchemaUtil.Occurrence occurs,
                                  final ResourceBundle resourceBundle)
      throws FormBuilderException   
   {
      //add it normally
      final String elementName = this.getElementName(element, xformsDocument);
      final String path = (pathToRoot.length() == 0
                           ? elementName
                           : pathToRoot + "/" + elementName);
      
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[addElementToGroup] Start addElement to group " + elementName + " at "  + path + " parentStack " + this.parentStack);
      
      if (this.parentStack.contains(element))
      {
         throw new FormBuilderException("recursion detected at element " + elementName);
      }
      
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[addElementToGroup] pushing element " + element + " onto parent stack");
      
      this.parentStack.push(element);

      final Element newDefaultInstanceElement = xformsDocument.createElement(elementName);
      if (element.getConstraintType() != XSConstants.VC_NONE)
      {
         Node value = xformsDocument.createTextNode(element.getConstraintValue());
         newDefaultInstanceElement.appendChild(value);
      }
      else if ("boolean".equals(element.getTypeDefinition().getName()))
      {
          // we have a boolean element without a default value, default to false
          Node value = xformsDocument.createTextNode("false");
          newDefaultInstanceElement.appendChild(value);
      }

      this.addElement(xformsDocument,
                      modelSection,
                      newDefaultInstanceElement,
                      formSection,
                      schema,
                      element,
                      path,
                      occurs,
                      resourceBundle);
					  
      Object poppedElement = this.parentStack.pop();
	  
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addElementToGroup] popped element " + poppedElement + " from parent stack");
         LOGGER.debug("[addElementToGroup] adding " + (occurs.maximum == 1
                                ? 1
                                : occurs.minimum + 1) +
                      " default instance element for " + elementName +
                      " at path " + path);
      }
      
      // update the default instance
      if (isRepeated(occurs, element.getTypeDefinition()))
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addElementToGroup] adding " + (occurs.minimum + 1) +
                         " default instance elements for " + elementName +
                         " at path " + path);
         }
      
         for (int i = 0; i < occurs.minimum + 1; i++)
         {
            final Element e = (Element)newDefaultInstanceElement.cloneNode(true);
            if (i == occurs.minimum)
            {
               e.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                NamespaceService.ALFRESCO_PREFIX + ":prototype",
                                "true");
            }
            defaultInstanceElement.appendChild(e);
         }
      }
      else
      {
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[addElementToGroup] adding one default instance element for " + elementName +
                         " at path " + path);
         }
         
         if (occurs.minimum == 0)
         {
            newDefaultInstanceElement.setAttributeNS(NamespaceConstants.XMLSCHEMA_INSTANCE_NS,
                                                     NamespaceConstants.XMLSCHEMA_INSTANCE_PREFIX + ":nil",
                                                     "true");
         }
         defaultInstanceElement.appendChild(newDefaultInstanceElement);
      }
      
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addGroup] End of addElementToGroup, group = " + elementName);
      }
   }

   /**
    * Add a repeat section if maxOccurs > 1.
    */
   private Element addRepeatIfNecessary(final Document xformsDocument,
                                        final Element modelSection,
                                        final Element formSection,
                                        final XSTypeDefinition controlType,
                                        final String pathToRoot,
                                        final SchemaUtil.Occurrence o)
   {

      // add xforms:repeat section if this element re-occurs
      if ((o.isOptional() && (controlType instanceof XSSimpleTypeDefinition || "anyType".equals(controlType.getName()))) ||
          (o.maximum == 1 && o.minimum == 1) || (controlType instanceof XSComplexTypeDefinition && pathToRoot.equals("")))
      {
         return formSection;
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addRepeatIfNecessary] for multiple element for type " +
                      controlType.getName() + ", maxOccurs = " + o.maximum);
      }

      final Element repeatSection =
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":repeat");

      //bind instead of repeat
      //repeatSection.setAttributeNS(NamespaceConstants.XFORMS_NS,NamespaceConstants.XFORMS_PREFIX + ":nodeset",pathToRoot);
      // bind -> last element in the modelSection
      Element bind = DOMUtil.getLastChildElement(modelSection);
      String bindId = null;

      if (bind != null &&
          bind.getLocalName() != null &&
          "bind".equals(bind.getLocalName()))
      {
         bindId = bind.getAttributeNS(null, "id");
      }
      else
      {
         LOGGER.warn("[addRepeatIfNecessary] bind not found: " + bind
                     + " (model selection name = " + modelSection.getNodeName() + ")");

         //if no bind is found -> modelSection is already a bind, get its parent last child
         bind = DOMUtil.getLastChildElement(modelSection.getParentNode());

         if (bind != null &&
             bind.getLocalName() != null &&
             "bind".equals(bind.getLocalName()))
         {
            bindId = bind.getAttributeNS(null, "id");
         }
         else
         {
            LOGGER.warn("[addRepeatIfNecessary] bind really not found");
         }
      }

      repeatSection.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                   NamespaceConstants.XFORMS_PREFIX + ":bind",
                                   bindId);
      this.setXFormsId(repeatSection);

      //appearance=full is more user friendly
      repeatSection.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                   NamespaceConstants.XFORMS_PREFIX + ":appearance",
                                   "full");

      formSection.appendChild(repeatSection);

      //add a group inside the repeat?
      final Element group = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                           NamespaceConstants.XFORMS_PREFIX + ":group");
      group.setAttributeNS(NamespaceConstants.XFORMS_NS,
                           NamespaceConstants.XFORMS_PREFIX + ":appearance",
                           "repeated");
      this.setXFormsId(group);
      repeatSection.appendChild(group);
      return group;
   }

   /**
    */
   private Element addSimpleType(final Document xformsDocument,
                                 final Element modelSection,
                                 Element formSection,
                                 final XSModel schema,
                                 final XSTypeDefinition controlType,
                                 final String owningElementName,
                                 final XSObject owner,
                                 final String pathToRoot,
                                 final SchemaUtil.Occurrence occurs,
                                 final ResourceBundle resourceBundle)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[addSimpleType] for " + controlType.getName() +
                      " (owningElementName = " + owningElementName + ")," +
                      " occurs = [" + occurs + "]");
         if (owner != null)
         {
            LOGGER.debug("[addSimpleType] owner is " + owner.getClass() + ", name is " + owner.getName());
         }
      }

      // create the <xforms:bind> element and add it to the model.
      boolean isRepeated = isRepeated(occurs, controlType);
      Element bindElement = 
         this.createBind(xformsDocument, pathToRoot + (isRepeated ? "[position() != last()]" : ""));
      String bindId = bindElement.getAttributeNS(null, "id");
      modelSection.appendChild(bindElement);
      bindElement = this.startBindElement(bindElement, schema, controlType, owner, occurs);

      // add a group if a repeat !
      if (owner instanceof XSElementDeclaration && occurs.isRepeated())
      {
         final Element groupElement = this.createGroup(xformsDocument,
                                                       modelSection,
                                                       formSection,
                                                       (XSElementDeclaration)owner,
                                                       resourceBundle);
         groupElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                     NamespaceConstants.XFORMS_PREFIX + ":appearance",
                                     "repeated");

         //set content
         formSection = groupElement;
      }

      //eventual repeat
      final Element repeatSection = this.addRepeatIfNecessary(xformsDocument,
                                                              modelSection,
                                                              formSection,
                                                              controlType,
                                                              pathToRoot,
                                                              occurs);

      // create the form control element
      //put a wrapper for the repeat content, but only if it is really a repeat
      if (repeatSection != formSection)
      {
         //if there is a repeat -> create another bind with "."
         final Element bindElement2 = this.createBind(xformsDocument, ".");
         final String bindId2 = bindElement2.getAttributeNS(null, "id");
         bindElement.appendChild(bindElement2);
         bindElement = bindElement2;
         bindId = bindId2;
      }

      final String caption = (owner != null
                              ? this.createCaption(owner, resourceBundle)
                              : this.createCaption(owningElementName));
      final Element formControl = this.createFormControl(xformsDocument,
                                                         schema,
                                                         caption,
                                                         controlType,
                                                         owner,
                                                         bindId,
                                                         bindElement,
                                                         occurs,
                                                         resourceBundle);
      repeatSection.appendChild(formControl);

      // if this is a repeatable then set ref to point to current element
      // not sure if this is a workaround or this is just the way XForms works...
      //
      //if (!repeatSection.equals(formSection))
      //formControl.setAttributeNS(NamespaceConstants.XFORMS_NS,
      //NamespaceConstants.XFORMS_PREFIX + ":ref",
      //".");

      //add selector if repeat
      //if (repeatSection != formSection)
      //this.addSelector(xformsDocument, (Element) formControl.getParentNode());

      return formSection;
   }

   private Element addSimpleType(final Document xformsDocument,
                                 final Element modelSection,
                                 final Element formSection,
                                 final XSModel schema,
                                 final XSSimpleTypeDefinition controlType,
                                 final XSAttributeUse owningAttribute,
                                 final String pathToRoot,
                                 final ResourceBundle resourceBundle)
   {
      return this.addSimpleType(xformsDocument,
                                modelSection,
                                formSection,
                                schema,
                                controlType,
                                owningAttribute.getAttrDeclaration().getName(),
                                owningAttribute,
                                pathToRoot,
                                new SchemaUtil.Occurrence(owningAttribute.getRequired() ? 1 : 0, 1),
                                resourceBundle);
   }

   private Element createFormControl(final Document xformsDocument,
                                     final XSModel schema,
                                     final String caption,
                                     final XSTypeDefinition controlType,
                                     final XSObject owner,
                                     final String bindId,
                                     final Element bindElement,
                                     final SchemaUtil.Occurrence o,
                                     final ResourceBundle resourceBundle)
   {
      Element formControl = null;
      if (controlType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE &&
          ((XSSimpleTypeDefinition)controlType).getItemType() != null)
      {
            formControl = this.createControlForListType(xformsDocument,
                                                        (XSSimpleTypeDefinition)controlType,
                                                        owner,
                                                        caption,
                                                        bindElement,
                                                        resourceBundle);
      }
      else if (controlType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE &&
               ((XSSimpleTypeDefinition)controlType).isDefinedFacet(XSSimpleTypeDefinition.FACET_ENUMERATION))
      {
         formControl = this.createControlForEnumerationType(xformsDocument,
                                                            (XSSimpleTypeDefinition)controlType,
                                                            owner,
                                                            caption,
                                                            bindElement,
                                                            resourceBundle);
      }
      else if (controlType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE &&
               "anyType".equals(controlType.getName()))
      {
         formControl = this.createControlForAnyType(xformsDocument, caption, controlType);
      }
      else
      {
         formControl = this.createControlForAtomicType(xformsDocument,
                                                       (XSSimpleTypeDefinition)controlType,
                                                       owner,
                                                       caption,
                                                       resourceBundle);
      }

      formControl.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                 NamespaceConstants.XFORMS_PREFIX + ":bind",
                                 bindId);

      // TODO: Enhance alert statement based on facet restrictions.
      // TODO: Enhance to support minOccurs > 1 and maxOccurs > 1.
      // TODO: Add i18n/l10n suppport to this - use java MessageFormatter...
      //
      //       e.g. Please provide a valid value for 'Address'. 'Address' is a mandatory decimal field.
      //
      final Element alertElement = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                                  NamespaceConstants.XFORMS_PREFIX + ":alert");
      formControl.appendChild(alertElement);
      this.setXFormsId(alertElement);

      String alert = Schema2XForms.extractPropertyFromAnnotation(NamespaceService.ALFRESCO_URI,
                                                                 "alert",
                                                                 this.getAnnotation(owner),
                                                                 resourceBundle);
      if (alert == null)
      {
         alert = ("Please provide a valid value for '" + caption + "'." +
                  " '" + caption +
                  "' is " + (o.minimum == 0 ? "an optional" : "a required") + " '" +
                  this.createCaption(this.getXFormsTypeName(xformsDocument, schema, controlType)) +
                  "' value.");
      }
      alertElement.appendChild(xformsDocument.createTextNode(alert));

      final String hint = Schema2XForms.extractPropertyFromAnnotation(NamespaceService.ALFRESCO_URI,
                                                                      "hint",
                                                                      this.getAnnotation(owner),
                                                                      resourceBundle);
      if (hint != null)
      {
         final Element hintElement = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                                    NamespaceConstants.XFORMS_PREFIX + ":hint");
         formControl.appendChild(hintElement);
         this.setXFormsId(hintElement);
         hintElement.appendChild(xformsDocument.createTextNode(hint));
      }
      return formControl;
   }

   /**
    * used to get the type name that will be used in the XForms document
    *
    * @param xformsDocument
    * @param schema
    * @param controlType the type from which we want the name
    * @return the complete type name (with namespace prefix) of the type in the XForms doc
    */
   protected String getXFormsTypeName(final Document xformsDocument,
                                      final XSModel schema,
                                      final XSTypeDefinition controlType)
   {
      final Element context = xformsDocument.getDocumentElement();
      final String typeName = controlType.getName();
      final String typeNS = controlType.getNamespace();

      //if we use XMLSchema types:
      //first check if it is a simple type named in the XMLSchema
      if (controlType.getTypeCategory() != XSTypeDefinition.SIMPLE_TYPE ||
          typeName == null ||
          typeName.length() == 0 ||
          schema.getTypeDefinition(typeName, typeNS) == null ||
          (typeNS != null && NamespaceConstants.XMLSCHEMA_NS.equals(typeNS)))
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("[getXFormsTypeName] using built in type for " + typeName);
         
         //use built in type
         return SchemaUtil.getBuiltInTypeName(controlType);
      }

      //type is globally defined
      //use schema type

      //local type name
      String localTypeName = typeName;
      final int index = typeName.indexOf(':');
      if (index > -1 && typeName.length() > index)
      {
         localTypeName = typeName.substring(index + 1);
      }

      //completeTypeName = new prefix + local name
      String result = localTypeName;
      if (typeNS != null)
      {
         //namespace prefix in this document
         final String prefix = NamespaceResolver.getPrefix(context, typeNS);
         if (prefix != null && prefix.length() != 0)
         {
            result = prefix + ":" + localTypeName;
         }
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("[getXFormsTypeName] resolved namespace prefix for uri " + typeNS + 
                         " to " + prefix +
                         " using document element " + context);
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[getXFormsTypeName] typeName = " + typeName +
                      ", typeNS = " + typeNS +
                      ", result = " + result);
      }
      return result;
   }

   private Document createFormTemplate(final String formId)
   {
      final Document xformsDocument = XMLUtil.newDocument();

      final Element envelopeElement = xformsDocument.createElementNS(NamespaceConstants.XHTML_NS,
                                                                     NamespaceConstants.XHTML_PREFIX + ":html");
      xformsDocument.appendChild(envelopeElement);

      //set namespace attribute
      addNamespace(envelopeElement,
                        NamespaceConstants.XHTML_PREFIX,
                        NamespaceConstants.XHTML_NS);
      addNamespace(envelopeElement,
                        NamespaceConstants.XFORMS_PREFIX,
                        NamespaceConstants.XFORMS_NS);
      addNamespace(envelopeElement,
                        NamespaceConstants.XMLEVENTS_PREFIX,
                        NamespaceConstants.XMLEVENTS_NS);
      addNamespace(envelopeElement,
                        NamespaceConstants.XMLSCHEMA_INSTANCE_PREFIX,
                        NamespaceConstants.XMLSCHEMA_INSTANCE_NS);
      addNamespace(envelopeElement,
                        NamespaceService.ALFRESCO_PREFIX,
                        NamespaceService.ALFRESCO_URI);

      //base
      if (this.base != null && this.base.length() != 0)
      {
         envelopeElement.setAttributeNS(NamespaceConstants.XML_NS,
                                        NamespaceConstants.XML_PREFIX + ":base",
                                        this.base);
      }

      //model element
      final Element modelElement = 
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":model");
      modelElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                  NamespaceConstants.XFORMS_PREFIX + ":functions",
                                  NamespaceConstants.CHIBA_PREFIX + ":match");
      this.setXFormsId(modelElement);
      final Element modelWrapper =
         xformsDocument.createElementNS(NamespaceConstants.XHTML_NS,
                                        NamespaceConstants.XHTML_PREFIX + ":head");
      modelWrapper.appendChild(modelElement);
      envelopeElement.appendChild(modelWrapper);

      //form control wrapper -> created by wrapper
      //Element formWrapper = xformsDocument.createElement("body");
      //envelopeElement.appendChild(formWrapper);
      final Element formWrapper = 
         xformsDocument.createElementNS(NamespaceConstants.XHTML_NS,
                                        NamespaceConstants.XHTML_PREFIX + ":body");
      envelopeElement.appendChild(formWrapper);
      return xformsDocument;
   }

   private Element createGroup(final Document xformsDocument,
                               final Element modelSection,
                               final Element formSection,
                               final XSElementDeclaration owner,
                               final ResourceBundle resourceBundle)
   {
      // add a group node and recurse
      final Element result =
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":group");
      this.setXFormsId(result);
      final String appearance = extractPropertyFromAnnotation(NamespaceService.ALFRESCO_URI,
                                                              "appearance",
                                                              this.getAnnotation(owner),
                                                              resourceBundle);
      result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":appearance",
                            appearance == null || appearance.length() == 0 ? "full" : appearance);
      
      formSection.appendChild(result);
      result.appendChild(this.createLabel(xformsDocument,
                                          this.createCaption(owner, resourceBundle)));
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[createGroup] group =\n" + XMLUtil.toString(result));
      }
      return result;
   }

   public String createCaption(final String text,
                               final XSObject o,
                               final ResourceBundle resourceBundle)
   {
      return this.createCaption(text, this.getAnnotation(o), resourceBundle);
   }

   public String createCaption(final String text,
                               final XSAnnotation annotation,
                               final ResourceBundle resourceBundle)
   {
      final String s = Schema2XForms.extractPropertyFromAnnotation(NamespaceService.ALFRESCO_URI,
                                                                   "label",
                                                                   annotation,
                                                                   resourceBundle);
      return s != null ? s : this.createCaption(text);
   }

   /**
    * Creates a caption for the provided text extracted from the XML Schema.
    * The implementation is responsible for reformatting the provided string to make it
    * suitable to be displayed to users of the XForm. This typically includes translating
    * XML tag name style identifiers (e.g. customerStreetAddress) into more reader friendly
    * captions (e.g. Customer Street Address).
    *
    * @param text The string value to be reformatted for use as a caption.
    * @return The caption.
    */
   public String createCaption(String text)
   {
      if (formatCaption == false)
      {
         return text;
      }
      // if the word is all upper case, then set to lower case and continue
      if (text.equals(text.toUpperCase()))
      {
         text = text.toLowerCase();
      }
      final String[] s = text.split("[-_\\ ]");
      final StringBuffer result = new StringBuffer();
      for (int i = 0; i < s.length; i++)
      {
         if (i != 0)
         {
            result.append(' ');
         }
         if (s[i].length() > 1)
         {
            result.append(Character.toUpperCase(s[i].charAt(0)) +
                          s[i].substring(1, s[i].length()));
         }
         else
         {
            result.append(s[i]);
         }
      }
      return result.toString();
   }

   /**
    * Creates a caption for the provided XML Schema attribute.
    * The implementation is responsible for providing an appropriate caption
    * suitable to be displayed to users of the XForm. This typically includes translating
    * XML tag name style identifiers (e.g. customerStreetAddress) into more reader friendly
    * captions (e.g. Customer Street Address).
    *
    * @param attribute The XML schema attribute for which a caption is required.
    * @return The caption.
    */
   public String createCaption(final XSAttributeDeclaration attribute,
                               final ResourceBundle resourceBundle)
   {
      return this.createCaption(attribute.getName(), attribute, resourceBundle);
   }

   public String createCaption(final XSAttributeUse attribute,
                               final ResourceBundle resourceBundle)
   {
      return this.createCaption(attribute.getAttrDeclaration().getName(), attribute, resourceBundle);
   }

   /**
    * Creates a caption for the provided XML Schema element.
    * The implementation is responsible for providing an appropriate caption
    * suitable to be displayed to users of the XForm. This typically includes translating
    * XML tag name style identifiers (e.g. customerStreetAddress) into more reader friendly
    * captions (e.g. Customer Street Address).
    *
    * @param element The XML schema element for which a caption is required.
    * @return The caption.
    */
   public String createCaption(final XSElementDeclaration element,
                               final ResourceBundle resourceBundle)
   {
      return this.createCaption(element.getName(), element, resourceBundle);
   }

   public String createCaption(final XSObject element,
                               final ResourceBundle resourceBundle)
   {
      if (element instanceof XSElementDeclaration)
      {
         return this.createCaption((XSElementDeclaration)element, resourceBundle);
      }
      if (element instanceof XSAttributeDeclaration)
      {
         return this.createCaption((XSAttributeDeclaration)element, resourceBundle);
      }
      if (element instanceof XSAttributeUse)
      {
         return this.createCaption((XSAttributeUse)element, resourceBundle);
      }
      else
      {
         LOGGER.warn("WARNING: createCaption: element is not an attribute nor an element: "
                     + element.getClass().getName());
         return null;
      }
   }

   /**
    * Creates a form control for an XML Schema any type.
    * <br/>
    * This method is called when the form builder determines a form control is required for
    * an any type.
    * The implementation of this method is responsible for creating an XML element of the
    * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
    * for adding the returned element to the form and setting caption, bind, and other
    * standard elements and attributes.
    *
    * @param xformsDocument       The XForm document.
    * @param controlType The XML Schema type for which the form control is to be created.
    * @return The element for the form control.
    */
   public Element createControlForAnyType(final Document xformsDocument,
                                          final String caption,
                                          final XSTypeDefinition controlType)
   {
      final Element control = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                             NamespaceConstants.XFORMS_PREFIX + ":textarea");
      this.setXFormsId(control);
      control.setAttributeNS(NamespaceConstants.XFORMS_NS,
                             NamespaceConstants.XFORMS_PREFIX + ":appearance",
                             "compact");
      control.appendChild(this.createLabel(xformsDocument, caption));
      return control;
   }

   /**
    * Creates a form control for an XML Schema simple atomic type.
    * <p/>
    * This method is called when the form builder determines a form control is required for
    * an atomic type.
    * The implementation of this method is responsible for creating an XML element of the
    * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
    * for adding the returned element to the form and setting caption, bind, and other
    * standard elements and attributes.
    *
    * @param xformsDocument       The XForm document.
    * @param controlType The XML Schema type for which the form control is to be created.
    * @return The element for the form control.
    */
   public Element createControlForAtomicType(final Document xformsDocument,
                                             final XSSimpleTypeDefinition controlType,
                                             final XSObject owner,
                                             final String caption,
                                             final ResourceBundle resourceBundle)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("[createControlForAtomicType] {name: " + controlType.getName() +
                      ", numeric: " + controlType.getNumeric() +
                      ", bounded: " + controlType.getBounded() +
                      ", finite: " + controlType.getFinite() +
                      ", ordered: " + controlType.getOrdered() +
                      ", final: " + controlType.getFinal() +
                      ", minInc: " + controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MININCLUSIVE) +
                      ", maxInc: " + controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXINCLUSIVE) +
                      ", minExc: " +  controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MINEXCLUSIVE) +
                      ", maxExc: " +  controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE) +
                      ", totalDigits: " + controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_TOTALDIGITS) +
                      ", length: " + controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_LENGTH) +
                      ", minLength: " + controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MINLENGTH) +
                      ", maxLength: " + controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXLENGTH) +
                      ", fractionDigits: " +  controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_FRACTIONDIGITS) +
                      ", builtInTypeName: " + SchemaUtil.getBuiltInTypeName(controlType) +
                      ", builtInType: " + SchemaUtil.getBuiltInType(controlType) +
                      "}");
      }
      String appearance = extractPropertyFromAnnotation(NamespaceService.ALFRESCO_URI,
                                                        "appearance",
                                                        this.getAnnotation(owner),
                                                        resourceBundle);
      Element result = null;
      if (controlType.getNumeric())
      {
         if (controlType.getBounded() &&
             controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXINCLUSIVE) != null &&
             controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MININCLUSIVE) != null)
         {
            result = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                    NamespaceConstants.XFORMS_PREFIX + ":range");
            result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                  NamespaceConstants.XFORMS_PREFIX + ":start",
                                  controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MININCLUSIVE));
            result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                  NamespaceConstants.XFORMS_PREFIX + ":end",
                                  controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXINCLUSIVE));
         }
         else
         {
            result = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS, 
                                                    NamespaceConstants.XFORMS_PREFIX + ":input");
         }
         if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_FRACTIONDIGITS))
         {
            String fractionDigits = controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_FRACTIONDIGITS);
            if (fractionDigits == null || fractionDigits.length() == 0)
            {
               final short builtInType = SchemaUtil.getBuiltInType(controlType);
               fractionDigits = (builtInType >= XSConstants.INTEGER_DT && builtInType <= XSConstants.POSITIVEINTEGER_DT
                                 ? "0"
                                 : null);
            }
            if (fractionDigits != null)
            {
               result.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                     NamespaceService.ALFRESCO_PREFIX + ":fractionDigits",
                                     fractionDigits);
            }
         }
         if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_TOTALDIGITS))
         {
            result.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                  NamespaceService.ALFRESCO_PREFIX + ":totalDigits",
                                  controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_TOTALDIGITS));
            
         }
      }
      else
      {
         switch (SchemaUtil.getBuiltInType(controlType))
         {
         case XSConstants.BOOLEAN_DT:
         {
            result = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                    NamespaceConstants.XFORMS_PREFIX + ":select1");
            final String[] values = { "true", "false" };
            for (String v : values)
            {
               final Element item = this.createXFormsItem(xformsDocument, v, v);
               result.appendChild(item);
            }
            break;
         }
         case XSConstants.STRING_DT:
         {
            result = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                    NamespaceConstants.XFORMS_PREFIX + ":textarea");            
            if (appearance == null || appearance.length() == 0)
            {
               appearance = "compact";
            }
            break;
         }
         case XSConstants.ANYURI_DT:
         {
            result = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                    NamespaceConstants.XFORMS_PREFIX + ":upload");
            final Element e = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                             NamespaceConstants.XFORMS_PREFIX + ":filename");
            this.setXFormsId(e);
            result.appendChild(e);
            e.setAttributeNS(NamespaceConstants.XFORMS_NS,
                             NamespaceConstants.XFORMS_PREFIX + ":ref",
                             ".");
            break;
         }
         default:
         {
            result = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS, 
                                                    NamespaceConstants.XFORMS_PREFIX + ":input");
            if ((appearance == null || appearance.length() == 0) &&
                SchemaUtil.getBuiltInType(controlType) == XSConstants.NORMALIZEDSTRING_DT)
            {
               appearance = "full";
            }
            if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_LENGTH))
            {
               result.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                     NamespaceService.ALFRESCO_PREFIX + ":length",
                                     controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_LENGTH));
            }
            else if(controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MINLENGTH) ||
                    controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MAXLENGTH))
            {
               if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MINLENGTH))
               {
                  result.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                        NamespaceService.ALFRESCO_PREFIX + ":minLength",
                                        controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MINLENGTH));
               }
               if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MAXLENGTH))
               {
                  result.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                        NamespaceService.ALFRESCO_PREFIX + ":maxLength",
                                        controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXLENGTH));
               }
            }
            if (SchemaUtil.getBuiltInType(controlType) == XSConstants.DATE_DT)
            {
               String minInclusive = null;
               String maxInclusive = null;
               final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
               final Calendar calendar = Calendar.getInstance();
               if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE))
               {
                  minInclusive = controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MINEXCLUSIVE);
                  try
                  {
                     final Date d = sdf.parse(minInclusive);
                     calendar.setTime(d);
                  }
                  catch (ParseException pe)
                  {
                     LOGGER.error(pe);
                  }
                  calendar.roll(Calendar.DATE, true);
                  minInclusive = sdf.format(calendar.getTime());
               }
               else if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MININCLUSIVE))
               {
                  minInclusive = controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MININCLUSIVE);
               }
               if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE))
               {
                  maxInclusive = controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE);
                  try
                  {
                     final Date d = sdf.parse(maxInclusive);
                     calendar.setTime(d);
                  }
                  catch (ParseException pe)
                  {
                     LOGGER.error(pe);
                  }
                  calendar.roll(Calendar.DATE, false);
                  maxInclusive = sdf.format(calendar.getTime());
               }
               else if (controlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MAXINCLUSIVE))
               {
                  maxInclusive = controlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXINCLUSIVE);
               }
               if (minInclusive != null)
               {
                  result.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                        NamespaceService.ALFRESCO_PREFIX + ":minInclusive",
                                        minInclusive);
               }
               if (maxInclusive != null)
               {
                  result.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                        NamespaceService.ALFRESCO_PREFIX + ":maxInclusive",
                                        maxInclusive);
               }
            }
         }
         }
      }
      this.setXFormsId(result);
      result.appendChild(this.createLabel(xformsDocument, caption));

      if (appearance != null && appearance.length() != 0)
      {
         result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                               NamespaceConstants.XFORMS_PREFIX + ":appearance",
                               appearance);
      }
      return result;
   }

   /**
    * Creates a form control for an XML Schema simple type restricted by an enumeration.
    * This method is called when the form builder determines a form control is required for
    * an enumerated type.
    * The implementation of this method is responsible for creating an XML element of the
    * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
    * for adding the returned element to the form and setting caption, bind, and other
    * standard elements and attributes.
    *
    * @param xformsDocument       The XForm document.
    * @param controlType The XML Schema type for which the form control is to be created.
    * @param caption     The caption for the form control. The caller The purpose of providing the caption
    *                    is to permit the implementation to add a <b>[Select1 .... ]</b> message that involves the caption.
    * @param bindElement The bind element for this control. The purpose of providing the bind element
    *                    is to permit the implementation to add a isValid attribute to the bind element that prevents
    *                    the <b>[Select1 .... ]</b> item from being selected.
    * @return The element for the form control.
    */
   public Element createControlForEnumerationType(final Document xformsDocument,
                                                  final XSSimpleTypeDefinition controlType,
                                                  final XSObject owner,
                                                  final String caption,
                                                  final Element bindElement,
                                                  final ResourceBundle resourceBundle)
   {
      // TODO: Figure out an intelligent or user determined way to decide between
      // selectUI style (listbox, menu, combobox, radio) (radio and listbox best apply)
      // Possibly look for special appInfo section in the schema and if not present default to comboBox...
      //
      // For now, use radio if enumValues < DEFAULT_LONG_LIST_MAX_SIZE otherwise use combobox
      //
      final StringList enumFacets = controlType.getLexicalEnumeration();
      if (enumFacets.getLength() <= 0)
      {
         return null;
      }

      final Element control = 
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":select1");
      this.setXFormsId(control);

      //label
      control.appendChild(this.createLabel(xformsDocument, caption));

      final Element choices = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                             NamespaceConstants.XFORMS_PREFIX + ":choices");
      this.setXFormsId(choices);

      final XSObjectList mvFacets = controlType.getMultiValueFacets();
      if (mvFacets.getLength() != 1)
      {
         throw new RuntimeException("expected exactly one MultiValueFacet for " + controlType);
      }

      final XSObjectList annotations =
         ((XSMultiValueFacet)mvFacets.item(0)).getAnnotations();

      final Map<String, XSAnnotation> enumValues =
         new LinkedHashMap<String, XSAnnotation>(enumFacets.getLength());
      
      final String nullValue = Application.getMessage(FacesContext.getCurrentInstance(), "please_select");
      enumValues.put(nullValue, null);
      for (int i = 0; i < enumFacets.getLength(); i++)
      {
         enumValues.put(enumFacets.item(i),
                        (annotations.getLength() == enumFacets.getLength()
                         ? (XSAnnotation)annotations.item(i)
                         : null));
      }

      String appearance = extractPropertyFromAnnotation(NamespaceService.ALFRESCO_URI,
                                                        "appearance",
                                                        this.getAnnotation(owner),
                                                        resourceBundle);
      if (appearance == null || appearance.length() == 0)
      {
         appearance = enumFacets.getLength() < Schema2XForms.LONG_LIST_SIZE ? "full" : "compact";
      }
      control.setAttributeNS(NamespaceConstants.XFORMS_NS,
                             NamespaceConstants.XFORMS_PREFIX + ":appearance",
                             appearance);

      control.appendChild(choices);
      this.addChoicesForSelectControl(xformsDocument, choices, enumValues, resourceBundle);
      return control;
   }

   /**
    * Creates a form control for an XML Schema simple list type.
    * <p/>
    * This method is called when the form builder determines a form control is required for
    * a list type.
    * The implementation of this method is responsible for creating an XML element of the
    * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
    * for adding the returned element to the form and setting caption, bind, and other
    * standard elements and attributes.
    *
    * @param xformsDocument       The XForm document.
    * @param listType    The XML Schema list type for which the form control is to be created.
    * @param owner
    * @param caption     The caption for the form control. The caller The purpose of providing the caption
    *                    is to permit the implementation to add a <b>[Select1 .... ]</b> message that involves the caption.
    * @param bindElement The bind element for this control. The purpose of providing the bind element
    *                    is to permit the implementation to add a isValid attribute to the bind element that prevents
    *                    the <b>[Select1 .... ]</b> item from being selected.
    * @param resourceBundle
    * @return The element for the form control.
    */
   public Element createControlForListType(final Document xformsDocument,
                                           final XSSimpleTypeDefinition listType,
                                           final XSObject owner,
                                           final String caption,
                                           final Element bindElement,
                                           final ResourceBundle resourceBundle)
   {
      XSSimpleTypeDefinition controlType = listType.getItemType();

      final StringList enumFacets = controlType.getLexicalEnumeration();
      if (enumFacets.getLength() <= 0)
      {
         return null;
      }
      Element control = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                              NamespaceConstants.XFORMS_PREFIX + ":select");
      this.setXFormsId(control);
      control.appendChild(this.createLabel(xformsDocument, caption));

      final XSObjectList mvFacets = controlType.getMultiValueFacets();
      if (mvFacets.getLength() != 1)
      {
         throw new RuntimeException("expected exactly one MultiValueFacet for " + controlType);
      }

      final XSObjectList annotations =
         ((XSMultiValueFacet)mvFacets.item(0)).getAnnotations();

      final Map<String, XSAnnotation> enumValues =
         new LinkedHashMap<String, XSAnnotation>(enumFacets.getLength());
      for (int i = 0; i < enumFacets.getLength(); i++)
      {
         enumValues.put(enumFacets.item(i),
                        (annotations.getLength() == enumFacets.getLength()
                         ? (XSAnnotation)annotations.item(i)
                         : null));
      }

      // TODO: Figure out an intelligent or user determined way to decide between
      // selectUI style (listbox, menu, combobox, radio) (radio and listbox best apply)
      // Possibly look for special appInfo section in the schema and if not present default to checkBox...
      //
      // For now, use checkbox if there are < DEFAULT_LONG_LIST_MAX_SIZE items, otherwise use long control
      String appearance = extractPropertyFromAnnotation(NamespaceService.ALFRESCO_URI,
                                                        "appearance",
                                                        this.getAnnotation(owner),
                                                        resourceBundle);
      if (appearance == null || appearance.length() == 0)
      {
         appearance = enumValues.size() < Schema2XForms.LONG_LIST_SIZE ? "full" : "compact";
      }
      control.setAttributeNS(NamespaceConstants.XFORMS_NS,
                             NamespaceConstants.XFORMS_PREFIX + ":appearance",
                             appearance);
      final Element choices = 
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":choices");
      this.setXFormsId(choices);
      control.appendChild(choices);
      this.addChoicesForSelectControl(xformsDocument, choices, enumValues, resourceBundle);
      return control;
   }

   private XSAnnotation getAnnotation(final XSObject o)
   {
      return (o instanceof XSElementDeclaration
              ? ((XSElementDeclaration)o).getAnnotation()
              :  (o instanceof XSAttributeDeclaration
                  ? ((XSAttributeDeclaration)o).getAnnotation()
                  : (o instanceof XSAttributeUse
                     ? ((XSAttributeUse)o).getAttrDeclaration().getAnnotation()
                     : null)));
   }

   /**
    * This method is invoked after an xforms:bind element is created for the specified SimpleType.
    * The implementation is responsible for setting setting any/all bind attributes
    * except for <b>id</b> and <b>ref</b> - these have been automatically set
    * by the caller (and should not be touched by implementation of startBindElement)
    * prior to invoking startBindElement.
    * The caller automatically adds the returned element to the model section of
    * the form.
    *
    * @param bindElement The bindElement being processed.
    * @param schema XML Schema type of the element/attribute this bind is for.
    * @param controlType
    * @param owner
    * @param o
    * @return The bind Element to use in the XForm - bindElement or a replacement.
    */
   public Element startBindElement(final Element bindElement,
                                   final XSModel schema,
                                   final XSTypeDefinition controlType,
                                   final XSObject owner,
                                   final SchemaUtil.Occurrence o)
   {
      // START WORKAROUND
      // Due to a Chiba bug, anyType is not a recognized type name.
      // so, if this is an anyType, then we'll just skip the type
      // setting.
      //
      // type.getName() may be 'null' for anonymous types, so compare against
      // static string (see bug #1172541 on sf.net)
      final List<String> constraints = new LinkedList<String>();
      if (controlType instanceof XSSimpleTypeDefinition &&
          ((XSSimpleTypeDefinition)controlType).getBuiltInKind() != XSConstants.ANYSIMPLETYPE_DT)
      {
         String typeName = this.getXFormsTypeName(bindElement.getOwnerDocument(),
                                                  schema,
                                                  controlType);
         if (typeName != null && typeName.length() != 0)
         {
            bindElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                       NamespaceConstants.XFORMS_PREFIX + ":type",
                                       typeName);
         }

         typeName = SchemaUtil.getBuiltInTypeName(controlType);
         if (typeName != null && typeName.length() != 0)
         {
            bindElement.setAttributeNS(NamespaceService.ALFRESCO_URI,
                                       NamespaceService.ALFRESCO_PREFIX + ":builtInType",
                                       typeName);
         }
         final StringList lexicalPatterns = ((XSSimpleTypeDefinition)controlType).getLexicalPattern();
                           
         // NOTE: from glen.johnson@alfresco.com
         // Workaround to fix issue WCM-952
         //
         // I added expression '&& !typeName.equals(SchemaSymbols.ATTVAL_INTEGER')
         // onto the end of loop condition expression below.
         //
         // This is to stop the pattern matching constraint (using deprecated chiba:match() function)
         // from being generated into binding elements that are linked to "xs:integer"
         // elements (in the xform instance)
         //
         // If this pattern match constraint is indeed added to the binding element linked to
         // a "xs:integer" element in the xform instance, then a value is always required
         // for that element - even if the corresponding schema has minOccurs="0" for
         // that element i.e. it causes a value to be required for "optional" xs:integer
         // elements.
         //
         // Note that the chiba:match() function is unsupported and will be removed from Chiba
         // in the future, so a solution enabling its complete removal will need to be found.
         // I do not see why it has been included here. The Schema inside the xform 
         // model should take care of most validation needs.
         // In the past, when I have completely removed this constraint (see CHK-2333), restrictions
         // using <xs:pattern> in the Schema fail to get enforced -
         // Causing the failure of org.alfresco.web.forms.xforms.Schema2XFormsTest.testConstraint()
         //
         for (int i = 0; lexicalPatterns != null && i < lexicalPatterns.getLength()
               && !SchemaSymbols.ATTVAL_INTEGER.equals(typeName); i++)
         {
            String pattern = lexicalPatterns.item(i);
            if (o.isOptional())
            {
                pattern= "(" + pattern + ")?";
            }
            constraints.add("chiba:match(., '" + pattern + "',null)");
         }
     
         XSSimpleTypeDefinition simpleControlType = ((XSSimpleTypeDefinition) controlType);

         if (simpleControlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MAXLENGTH))
         {
             constraints.add("string-length(.) <= " + simpleControlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MAXLENGTH));
         }

         if (simpleControlType.isDefinedFacet(XSSimpleTypeDefinition.FACET_MINLENGTH))
         {
             constraints.add("string-length(.) >= " + simpleControlType.getLexicalFacetValue(XSSimpleTypeDefinition.FACET_MINLENGTH));
         }
                               
      }
          
      
      final short constraintType =
         (owner != null && owner instanceof XSElementDeclaration
          ? ((XSElementDeclaration)owner).getConstraintType()
          : (owner != null && owner instanceof XSAttributeDeclaration
             ? ((XSAttributeDeclaration)owner).getConstraintType()
             : (owner != null && owner instanceof XSAttributeUse
                ? ((XSAttributeUse)owner).getConstraintType()
                : XSConstants.VC_NONE)));

      bindElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                 NamespaceConstants.XFORMS_PREFIX + ":readonly",
                                 (constraintType == XSConstants.VC_FIXED) + "()");

      if (controlType instanceof XSSimpleTypeDefinition)
      {
         bindElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                    NamespaceConstants.XFORMS_PREFIX + ":required",
                                    (o.minimum != 0) + "()");
      }
      else if (controlType instanceof XSComplexTypeDefinition)
      {
         // make all complex types not required since it helps with validation - otherwise
         // chiba seems to expect a nodevalue for the container element
         bindElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                    NamespaceConstants.XFORMS_PREFIX + ":required",
                                    "false()");

      }

      //no more minOccurs & maxOccurs element: add a constraint if maxOccurs>1:
      //count(.) <= maxOccurs && count(.) >= minOccurs
      final String nodeset = bindElement.getAttributeNS(NamespaceConstants.XFORMS_NS,
                                                        "nodeset");
      if (o.minimum > 1)
      {
         //if 0 or 1 -> no constraint (managed by "required")
         constraints.add("count(../" + nodeset + ") >= " + o.minimum);
      }
      bindElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                 NamespaceConstants.XFORMS_PREFIX + ":minOccurs",
                                 String.valueOf(o.minimum));
      if (o.maximum > 1)
      {
         //if 1 or unbounded -> no constraint
         constraints.add("count(../" + nodeset + ") <= " + o.maximum);
      }

      bindElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                 NamespaceConstants.XFORMS_PREFIX + ":maxOccurs",
                                 o.isUnbounded() ? "unbounded" : String.valueOf(o.maximum));

      if (constraints.size() != 0)
      {
         bindElement.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                    NamespaceConstants.XFORMS_PREFIX + ":constraint",
                                    StringUtils.join((String[])constraints.toArray(new String[constraints.size()]), " and "));
      }
      return bindElement;
   }

   /**
    * Get a fully qualified name for this element, and eventually declares a new prefix for the namespace if
    * it was not declared before
    *
    * @param element
    * @param xformsDocument
    * @return The element name
    */
   private String getElementName(final XSElementDeclaration element,
                                 final Document xformsDocument)
   {
      String elementName = element.getName();
      String namespace = element.getNamespace();
      if (namespace != null && namespace.length() != 0)
      {
         final String prefix = NamespaceResolver.getPrefix(xformsDocument.getDocumentElement(), namespace);
         elementName = prefix + ":" + elementName;
      }
      
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[getElementName] " + element.getName() + "," + namespace + " = " + elementName);
      
      return elementName;
   }

   private static String addNamespace(final Element e, String nsPrefix, final String ns)
   {
      String prefix;
      if ((prefix = NamespaceResolver.getPrefix(e, ns)) != null)
      {
         return prefix;
      }
      
      if (nsPrefix == null || e.hasAttributeNS(NamespaceConstants.XMLNS_NS, nsPrefix))
      {
         // Generate a unique prefix
         int suffix = 1;
         while (e.hasAttributeNS(NamespaceConstants.XMLNS_NS, nsPrefix = "ns" + suffix))
         {
            suffix++;
         }
      }

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[addNamespace] adding namespace " + ns + " with prefix " + nsPrefix + " to " + e.getNodeName());
      
      e.setAttributeNS(NamespaceConstants.XMLNS_NS,
                       NamespaceConstants.XMLNS_PREFIX + ':' + nsPrefix,
                       ns);
      
      return nsPrefix;
   }

   private void createTriggersForRepeats(final Document xformsDocument, final Element rootGroup)
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[createTriggersForRepeats] start");
      
      final HashMap<String, Element> bindIdToBind = new HashMap<String, Element>();
      final NodeList binds = xformsDocument.getElementsByTagNameNS(NamespaceConstants.XFORMS_NS, "bind");
      for (int i = 0; i < binds.getLength(); i++)
      {
         final Element b = (Element)binds.item(i);
         bindIdToBind.put(b.getAttributeNS(null, "id"), b);
      }

      final NodeList repeats = xformsDocument.getElementsByTagNameNS(NamespaceConstants.XFORMS_NS, "repeat");
      final HashMap<Element, Element> bindToRepeat = new HashMap<Element, Element>();
      for (int i = 0; i < repeats.getLength(); i++)
      {
         Element r = (Element)repeats.item(i);
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("[createTriggersForRepeats] processing repeat " + r.getAttributeNS(null, "id"));
         
         Element bind = bindIdToBind.get(r.getAttributeNS(NamespaceConstants.XFORMS_NS, "bind"));
         bindToRepeat.put(bind, r);

         String xpath = "";

         do
         {
            if (xpath.length() != 0)
            {
               xpath = '/' + xpath;
            }

            if (LOGGER.isDebugEnabled())
               LOGGER.debug("[createTriggersForRepeats] walking bind " + bind.getAttributeNS(null, "id"));
            
            String s = bind.getAttributeNS(NamespaceConstants.XFORMS_NS, "nodeset");
            s = s.replaceAll("^([^\\[]+).*$", "$1");
            if (bindToRepeat.containsKey(bind) && !r.equals(bindToRepeat.get(bind)))
            {
               s += "[index(\'" + bindToRepeat.get(bind).getAttributeNS(null, "id") + "\')]";
            }
            xpath = s + xpath;
            bind = ((NamespaceConstants.XFORMS_PREFIX + ":bind").equals(bind.getParentNode().getNodeName())
                    ? (Element)bind.getParentNode()
                    : null);
         }
         while (bind != null);
         this.createTriggersForRepeat(xformsDocument,
                                      rootGroup,
                                      r.getAttributeNS(null, "id"),
                                      xpath,
                                      r.getAttributeNS(NamespaceConstants.XFORMS_NS, "bind"));
      }
   }

   private Element createTrigger(final Document xformsDocument,
                                 final String id,
                                 final String bindId,
                                 final String label,
                                 final Element... actions)
   {
      final Element trigger =
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":trigger");
      this.setXFormsId(trigger, id != null ? id : null);

      //copy the bind attribute
      if (bindId != null)
      {
         trigger.setAttributeNS(NamespaceConstants.XFORMS_NS,
                                NamespaceConstants.XFORMS_PREFIX + ":bind",
                                bindId);
      }
      trigger.appendChild(this.createLabel(xformsDocument, label));

      //insert action
      final Element actionWrapper =
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":action");
      trigger.appendChild(actionWrapper);

      for (final Element action : actions)
      {
         actionWrapper.appendChild(action);
         this.setXFormsId(action);
      }
      return trigger;
   }


   /**
    * add triggers to use the repeat elements (allow to add an element, ...)
    */
   private void createTriggersForRepeat(final Document xformsDocument,
                                        final Element rootGroup,
                                        final String repeatId,
                                        final String nodeset,
                                        final String bindId)
   {
      //xforms:at = xforms:index from the "id" attribute on the repeat element
      //trigger insert
      Element action =
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":insert");
      action.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":nodeset",
                            nodeset);
      action.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":position",
                            "before");
      action.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":at",
                            "1");

      final Element trigger_insert_before = this.createTrigger(xformsDocument,
                                                               repeatId + "-insert_before",
                                                               bindId,
                                                               "insert at beginning",
                                                               action);
      
      action = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                     NamespaceConstants.XFORMS_PREFIX + ":insert");
      action.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":nodeset",
                            nodeset);
      action.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":position",
                            "after");
      action.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":at",
                            NamespaceConstants.XFORMS_PREFIX + ":index('" + repeatId + "')");

      final Element trigger_insert_after = this.createTrigger(xformsDocument,
                                                              repeatId + "-insert_after",
                                                              bindId,
                                                              "insert after selected",
                                                              action);

      //trigger delete
      action = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                              NamespaceConstants.XFORMS_PREFIX + ":delete");
      action.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":nodeset",
                            nodeset);
      action.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":at",
                            NamespaceConstants.XFORMS_PREFIX + ":index('" + repeatId + "')");

      final Element trigger_delete = this.createTrigger(xformsDocument,
                                                        repeatId != null ? repeatId + "-delete" : null,
                                                        bindId,
                                                        "delete selected",
                                                        action);

      //add the triggers
      rootGroup.appendChild(trigger_insert_before);
      rootGroup.appendChild(trigger_insert_after);
      rootGroup.appendChild(trigger_delete);
   }

   private Element createSubmissionElement(final Document xformDocument,
                                           final String id,
                                           final boolean validate)
   {
      final Element result = xformDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                           NamespaceConstants.XFORMS_PREFIX + ":submission");

      this.setXFormsId(result, id);

      result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":validate",
                            validate ? "true" : "false");

      result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":action",
                            this.action == null ? "" : this.base + this.action);

      result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":method",
                            (this.submitMethod != null
                             ? this.submitMethod
                             : Schema2XForms.SubmitMethod.POST).toString());
      result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":encoding",
                            "UTF-8");
      return result;
   }

   private Element createSubmitControl(final Document xformsDocument,
                                       final Element submission,
                                       final String id,
                                       final String label)
   {
      final Element result = xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                                            NamespaceConstants.XFORMS_PREFIX + ":submit");
      result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":submission",
                            submission.getAttributeNS(null, "id"));
      this.setXFormsId(result, id);

      result.appendChild(this.createLabel(xformsDocument, label));

      return result;
   }

   private void createSubmitElements(final Document xformsDocument,
                                     final Element modelSection,
                                     final Element rootGroup)
   {

      Element submission = 
         this.createSubmissionElement(xformsDocument, "submission-validate", true);
      modelSection.appendChild(submission);

      Element submit = this.createSubmitControl(xformsDocument, submission, "submit", "Submit");
      rootGroup.appendChild(submit);

      submission = this.createSubmissionElement(xformsDocument, "submission-draft", false);
      modelSection.appendChild(submission);

      submit = this.createSubmitControl(xformsDocument,
                                        submission,
                                        "save-draft",
                                        "Save Draft");
      rootGroup.appendChild(submit);
   }

   private Element createXFormsItem(final Document xformsDocument,
                                    final String label,
                                    final String value)
   {
      final Element item = 
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":item");
      this.setXFormsId(item);
      item.appendChild(this.createLabel(xformsDocument, label));

      final Element e = 
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":value");
      this.setXFormsId(e);
      e.appendChild(xformsDocument.createTextNode(value));
      item.appendChild(e);
      return item;
   }

   private Element createLabel(final Document xformsDocument,
                               final String label)
   {
      final Element e = 
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":label");
      this.setXFormsId(e);
      e.appendChild(xformsDocument.createTextNode(label));
      return e;
   }

   private Element createBind(final Document xformsDocument,
                              final String nodeset)
   {
      final Element result =
         xformsDocument.createElementNS(NamespaceConstants.XFORMS_NS,
                                        NamespaceConstants.XFORMS_PREFIX + ":bind");
      final String id = this.setXFormsId(result);
      result.setAttributeNS(NamespaceConstants.XFORMS_NS,
                            NamespaceConstants.XFORMS_PREFIX + ":nodeset",
                            nodeset);
      
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("[createBind] created bind " + id + " for nodeset " + nodeset);
      
      return result;
   }
   
   private boolean isRepeated(SchemaUtil.Occurrence occurs, XSTypeDefinition type)
   {
      // return immediately if occurs signifies repeat
      if (occurs.isRepeated())
      {
         return true;
      }
      
      boolean repeated = false;
      
      if (occurs.isOptional())
      {
         // if element is optional check the type, for
         // simple and 'any' types return false
             
         if ((type instanceof XSSimpleTypeDefinition == false) && 
             ("anyType".equals(type.getName()) == false))
         {
             repeated = true;
         }
      }
      
      return repeated;
   }
}
