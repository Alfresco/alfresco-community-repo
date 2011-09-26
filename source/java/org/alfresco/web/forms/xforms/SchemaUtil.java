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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.web.forms.xforms;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import org.alfresco.util.EqualsHelper;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;

/**
 * Provides utility functions for xml schema parsing.
 */
public class SchemaUtil
{

   ////////////////////////////////////////////////////////////////////////////

   public static class Occurrence
   {
      public final static int UNBOUNDED = -1;
	
      public final int minimum;
      public final int maximum;

      public Occurrence(final XSParticle particle)
      {
         this(particle.getMinOccurs(), (particle.getMaxOccursUnbounded()
                                        ? Occurrence.UNBOUNDED
                                        : particle.getMaxOccurs()));
      }

      public Occurrence(final int minimum)
      {
         this(minimum, Occurrence.UNBOUNDED);
      }

      public Occurrence(final int minimum, final int maximum)
      {
         this.minimum = minimum;
         this.maximum = maximum;
      }

      public boolean isRepeated()
      {
         return this.isUnbounded() || this.maximum > 1;
      }
      
      public boolean isOptional()
      {
          return this.minimum == 0 && this.maximum == 1;
      }
      
      public boolean isUnbounded()
      {
         return this.maximum == UNBOUNDED;
      }

      public String toString()
      {
         return "minimum=" + minimum + ", maximum=" + maximum;
      }
   }

   ////////////////////////////////////////////////////////////////////////////

   private final static Comparator<XSTypeDefinition> TYPE_EXTENSION_SORTER = 
      new Comparator<XSTypeDefinition>() 
   {
      public int compare(final XSTypeDefinition type1, final XSTypeDefinition type2) 
      {
         int result = 0;
         if (type1 == null && type2 != null)
         {
            result = -1;
         }
         else if (type1 != null && type2 == null)
         {
            result = 1;
         }
         else if (type1 == type2 || (type1 == null && type2 == null))
         {
            result = 0;
         }
         else 
         {
            result = (type1.derivedFromType(type2, XSConstants.DERIVATION_EXTENSION)
                      ? 1
                      : (type2.derivedFromType(type1, XSConstants.DERIVATION_EXTENSION)
                         ? -1
                         : type1.getName().compareTo(type2.getName())));
         }
         if (LOGGER.isDebugEnabled() && false)
            LOGGER.debug("compare(" + type1 + ", " + type2 + ") = " + result);
         return result;
      }
   };

   ////////////////////////////////////////////////////////////////////////////

   private final static Log LOGGER = LogFactory.getLog(SchemaUtil.class);

   private final static HashMap<Short, String> DATA_TYPE_TO_NAME = 
      new HashMap<Short, String>();
   static
   {
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.ANYSIMPLETYPE_DT, "anyType");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.ANYURI_DT, "anyURI");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.BASE64BINARY_DT, "base64Binary");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.BOOLEAN_DT, "boolean");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.BYTE_DT, "byte");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DATETIME_DT, "dateTime");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DATE_DT, "date");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DECIMAL_DT, "decimal");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DOUBLE_DT, "double");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.DURATION_DT, "duration");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.ENTITY_DT, "ENTITY");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.FLOAT_DT, "float");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GDAY_DT, "gDay");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GMONTHDAY_DT, "gMonthDay");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GMONTH_DT, "gMonth");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GYEARMONTH_DT, "gYearMonth");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.GYEAR_DT, "gYear");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.IDREF_DT, "IDREF");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.ID_DT, "ID");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.INTEGER_DT, "integer");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.INT_DT, "int");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.LANGUAGE_DT, "language");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.LONG_DT, "long");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NAME_DT, "Name");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NCNAME_DT, "NCName");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NEGATIVEINTEGER_DT, "negativeInteger");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NMTOKEN_DT, "NMTOKEN");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NONNEGATIVEINTEGER_DT, "nonNegativeInteger");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NONPOSITIVEINTEGER_DT, "nonPositiveInteger");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NORMALIZEDSTRING_DT, "normalizedString");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.NOTATION_DT, "NOTATION");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.POSITIVEINTEGER_DT, "positiveInteger");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.QNAME_DT, "QName");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.SHORT_DT, "short");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.STRING_DT, "string");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.TIME_DT, "time");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.TOKEN_DT, "TOKEN");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.UNSIGNEDBYTE_DT, "unsignedByte");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.UNSIGNEDINT_DT, "unsignedInt");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.UNSIGNEDLONG_DT, "unsignedLong");
      SchemaUtil.DATA_TYPE_TO_NAME.put(XSConstants.UNSIGNEDSHORT_DT, "unsignedShort");
   };

   /**
    * Returns the most-specific built-in base type for the provided type.
    */
   public static short getBuiltInType(final XSTypeDefinition type) 
   {
      // type.getName() may be 'null' for anonymous types, so compare against
      // static string (see bug #1172541 on sf.net)
      if (SchemaUtil.DATA_TYPE_TO_NAME.get(XSConstants.ANYSIMPLETYPE_DT).equals(type.getName())) 
      {
         return XSConstants.ANYSIMPLETYPE_DT;
      }
      final XSSimpleTypeDefinition simpleType = (XSSimpleTypeDefinition) type;
      final short result = simpleType.getBuiltInKind();
      return (result == XSConstants.LIST_DT 
              ? SchemaUtil.getBuiltInType(simpleType.getItemType()) 
              : result);
   }

   public static String getBuiltInTypeName(final XSTypeDefinition type)
   {
      final short s = SchemaUtil.getBuiltInType(type);
      return SchemaUtil.getBuiltInTypeName(s);
   }

   public static String getBuiltInTypeName(final short type)
   {
      return SchemaUtil.DATA_TYPE_TO_NAME.get(type);
   }

   public static XSModel parseSchema(final Document schemaDocument,
                                     final boolean failOnError)
      throws FormBuilderException
   {
      try
      {
         // Get DOM Implementation using DOM Registry
         System.setProperty(DOMImplementationRegistry.PROPERTY,
                            "org.apache.xerces.dom.DOMXSImplementationSourceImpl");

         final DOMImplementationRegistry registry =
            DOMImplementationRegistry.newInstance();

         final DOMImplementationLS lsImpl = (DOMImplementationLS)
            registry.getDOMImplementation("XML 1.0 LS 3.0");
         if (lsImpl == null)
         {
            throw new FormBuilderException("unable to create DOMImplementationLS using " + registry);
         }
         final LSInput in = lsImpl.createLSInput();
         in.setStringData(XMLUtil.toString(schemaDocument));

         final XSImplementation xsImpl = (XSImplementation)
            registry.getDOMImplementation("XS-Loader");
         final XSLoader schemaLoader = xsImpl.createXSLoader(null);
         final DOMConfiguration config = (DOMConfiguration)schemaLoader.getConfig();
         final LinkedList<DOMError> errors = new LinkedList<DOMError>();
         config.setParameter("error-handler", new DOMErrorHandler()
         {
            public boolean handleError(final DOMError domError)
            {
               errors.add(domError);
               return true;
            }
         });

         final XSModel result = schemaLoader.load(in);
         if (failOnError && errors.size() != 0)
         {
            final HashSet<String> messages = new HashSet<String>();
            StringBuilder message = null;
            for (DOMError e : errors)
            {
               message = new StringBuilder();
               final DOMLocator dl = e.getLocation();
               if (dl != null)
               {
                  message.append("at line ").append(dl.getLineNumber())
                     .append(" column ").append(dl.getColumnNumber());
                  if (dl.getRelatedNode() != null)
                  {
                     message.append(" node ").append(dl.getRelatedNode().getNodeName());
                  }
                  message.append(": ").append(e.getMessage());
               }
               messages.add(message.toString());
            }
            
            message = new StringBuilder();
            message.append(messages.size() > 1 ? "errors" : "error").append(" parsing schema: \n");
            for (final String s : messages)
            {
               message.append(s).append("\n");
            }

            throw new FormBuilderException(message.toString());
         }

         if (result == null)
         {
            throw new FormBuilderException("invalid schema");
         }
         return result;
      } 
      catch (ClassNotFoundException x) 
      {
         throw new FormBuilderException(x);
      } 
      catch (InstantiationException x) 
      {
         throw new FormBuilderException(x);
      }
      catch (IllegalAccessException x) 
      {
         throw new FormBuilderException(x);
      }
   }

   private static void buildTypeTree(final XSTypeDefinition type, 
                                     final TreeSet<XSTypeDefinition> descendents,
                                     final TreeMap<String, TreeSet<XSTypeDefinition>> typeTree) 
   {
      if (type == null) 
      {
         return;
      }
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("buildTypeTree(" + type.getName() + ", " + descendents.size() + " descendents)");
      if (descendents.size() > 0) 
      {
         TreeSet<XSTypeDefinition> compatibleTypes = typeTree.get(type.getName());
	    
         if (compatibleTypes == null) 
         {
            if (LOGGER.isDebugEnabled())
               LOGGER.debug("no compatible types found for " + type.getName() + ", creating a new set");
            compatibleTypes = new TreeSet<XSTypeDefinition>(TYPE_EXTENSION_SORTER);
            typeTree.put(type.getName(), compatibleTypes);
         }
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("adding " + descendents.size() + " descendents to " + type.getName());
         compatibleTypes.addAll(descendents);
      }

      final XSTypeDefinition parentType = type.getBaseType();
      if (parentType == null ||
          type.getTypeCategory() != parentType.getTypeCategory()) 
      {
         return;
      }

      if (type != parentType && parentType.getName() != null && !parentType.getName().equals("anyType")) 
      {
         TreeSet<XSTypeDefinition> newDescendents = typeTree.get(parentType.getName());
         if (newDescendents == null)
         {
            if (LOGGER.isDebugEnabled())
               LOGGER.debug("type tree doesn't contain " + parentType.getName() +
                            ", creating a new descendant set");
            newDescendents = new TreeSet<XSTypeDefinition>(TYPE_EXTENSION_SORTER);
         }
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("adding " + descendents.size() + " descendants to existing " + newDescendents.size() +
                         " descendants of " + parentType.getName());
         newDescendents.addAll(descendents);
         
         //extension (we only add it to "newDescendants" because we don't want
         //to have a type descendant to itself, but to consider it for the parent
         if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) 
         {
            final XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)type;
            if (complexType.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION && 
                !complexType.getAbstract() && 
                !descendents.contains(type)) 
            {
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("adding " + type.getName() + 
                               " to existing " + newDescendents.size() + 
                               " descendents of " + parentType.getName());
               newDescendents.add(type);
            }
         }

         //note: extensions are impossible on simpleTypes !
         SchemaUtil.buildTypeTree(parentType, newDescendents, typeTree);
      }
   }

   public static TreeMap<String, TreeSet<XSTypeDefinition>>
      buildTypeTree(final XSModel schema) 
   {
      final TreeMap<String, TreeSet<XSTypeDefinition>> result = new TreeMap<String, TreeSet<XSTypeDefinition>>();
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("buildTypeTree " + schema);
      // build the type tree for complex types
      final XSNamedMap types = schema.getComponents(XSConstants.TYPE_DEFINITION);
      for (int i = 0; i < types.getLength(); i++) 
      {
         final XSTypeDefinition t = (XSTypeDefinition)types.item(i);
         if (t.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) 
         {
            final XSComplexTypeDefinition type = (XSComplexTypeDefinition)t;
            SchemaUtil.buildTypeTree(type, 
                                     new TreeSet<XSTypeDefinition>(TYPE_EXTENSION_SORTER),
                                     result);
         }
      }

      // build the type tree for simple types
      for (int i = 0; i < types.getLength(); i++) 
      {
         final XSTypeDefinition t = (XSTypeDefinition)types.item(i);
         if (t.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) 
         {
            SchemaUtil.buildTypeTree((XSSimpleTypeDefinition)t, 
                                      new TreeSet<XSTypeDefinition>(TYPE_EXTENSION_SORTER),
                                      result);
         }
      }

      // print out type hierarchy for debugging purposes
      if (LOGGER.isDebugEnabled()) 
      {
         for (String typeName : result.keySet())
         {
            TreeSet<XSTypeDefinition> descendents = result.get(typeName);
            LOGGER.debug(">>>> for " + typeName + " Descendants=\n ");
            Iterator<XSTypeDefinition> it = descendents.iterator();
            while (it.hasNext()) 
            {
               XSTypeDefinition desc = it.next();
               LOGGER.debug("      " + desc.getName());
            }
         }
      }
      return result;
   }

   public static XSParticle findCorrespondingParticleInComplexType(final XSElementDeclaration elDecl) 
   {
      final XSComplexTypeDefinition complexType = elDecl.getEnclosingCTDefinition();
      if (complexType == null)
      {
         LOGGER.warn("unable to find corresponding particle for " + elDecl.getName() +
                     ".  no enclosing complex type.");
         return null;
      }

      final XSParticle particle = complexType.getParticle();
      final XSTerm term = particle.getTerm();
      if (! (term instanceof XSModelGroup)) 
      {
         LOGGER.warn("unable to find corresponding particle for " + elDecl.getName() +
                     ".  term " + term + " is not a model group.");
         return null;
      }
      final XSParticle result = SchemaUtil.findCorrespondingParticleInModelGroup((XSModelGroup)term, elDecl);
      if (result == null)
      {
         final String msg = ("unable to find corresponding particle for " + elDecl.getName() +
                             " in term " + term);
         if (LOGGER.isDebugEnabled())
         {
            throw new NullPointerException(msg);
         }
         else
         {
            LOGGER.warn(msg);
         }
      }
      return result;
   }

   private static XSParticle findCorrespondingParticleInModelGroup(final XSModelGroup modelGroup, 
                                                                   final XSElementDeclaration elDecl)
   {
      final XSObjectList particles = modelGroup.getParticles();
      if (particles == null || particles.getLength() == 0)
      {
         LOGGER.debug("unable to find corresponding particle for " + elDecl.getName() +
                     ".  group " + modelGroup + " contains no particles.");
         return null;
      }

      for (int i = 0; i < particles.getLength(); i++) 
      {
         XSParticle part = (XSParticle)particles.item(i);
         final XSTerm thisTerm = part.getTerm();
         if (thisTerm instanceof XSModelGroup)
         {
            part = SchemaUtil.findCorrespondingParticleInModelGroup((XSModelGroup)thisTerm, elDecl);
            if (part != null)
            {
               return part;
            }
         }
         else
         {
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("checking term " + thisTerm.getName() + 
                            " in " + modelGroup.getName() + "(" + modelGroup.getClass().getName() + ")");
            }
            if (thisTerm == elDecl)
            {
               return part;
            }
         }
      }
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("unable to find corresponding particle for " + elDecl.getName() +
                      ".  " + elDecl.getName() + 
                      " not found in " + particles.getLength() + " particles");
      }
      return null;
   }
    
   /**
    * check that the element defined by this name is declared directly in the type
    */
   public static boolean isElementDeclaredIn(String name, String namespace,
                                             XSComplexTypeDefinition type, 
                                             boolean recursive) 
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("isElement " + name + " declared in " + type.getName());
      }

      //test if extension + declared in parent + not recursive -> NOK
      if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) 
      {
         XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("testing if it is not on parent " + parent.getName());
         if (SchemaUtil.isElementDeclaredIn(name, namespace, parent, true))
            return false;
      }

      boolean found = false;
      XSParticle particle = type.getParticle();
      if (particle != null) 
      {
         XSTerm term = particle.getTerm();
         if (term instanceof XSModelGroup) 
         {
            XSModelGroup group = (XSModelGroup) term;
            found = SchemaUtil.isElementDeclaredIn(name, namespace, group);
         }
      }

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("isElement " + name + 
                      " declared in " + type.getName() + ": " + found);

      return found;
   }

   /**
    * private recursive method called by isElementDeclaredIn(String name, XSComplexTypeDefinition type)
    */
   public static boolean isElementDeclaredIn(String name, String namespace, XSModelGroup group) 
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("isElement " + name + " declared in group " + group.getName());

      boolean found = false;
      XSObjectList particles = group.getParticles();
      for (int i = 0; i < particles.getLength(); i++) 
      {
         XSParticle subPart = (XSParticle)particles.item(i);
         XSTerm subTerm = subPart.getTerm();
         if (subTerm instanceof XSElementDeclaration) 
         {
            XSElementDeclaration elDecl = (XSElementDeclaration) subTerm;
            if (EqualsHelper.nullSafeEquals(namespace, elDecl.getNamespace()) && name.equals(elDecl.getName()))
               found = true;
         } 
         else if (subTerm instanceof XSModelGroup)
         {
            found = SchemaUtil.isElementDeclaredIn(name, namespace, (XSModelGroup) subTerm);
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("isElement " + name + " declared in group " + group.getName() + ": " + found);
      }
      return found;
   }

   public static boolean doesElementComeFromExtension(final XSElementDeclaration element, 
                                                      final XSComplexTypeDefinition controlType) 
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("doesElementComeFromExtension for " + element.getName() + 
                      " and controlType=" + controlType.getName());
      }
      if (controlType.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) 
      {
         final XSTypeDefinition baseType = controlType.getBaseType();
         if (baseType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) 
         {
            final XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) baseType;
            if (SchemaUtil.isElementDeclaredIn(element.getName(), element.getNamespace(), complexType, true)) 
            {
               if (LOGGER.isDebugEnabled())
               {
                  LOGGER.debug("doesElementComeFromExtension: yes");
               }
               return true;
            } 
            if (LOGGER.isDebugEnabled())
            {
               LOGGER.debug("doesElementComeFromExtension: recursive call on previous level");
            }
            return SchemaUtil.doesElementComeFromExtension(element, complexType);
         }
      }
      return false;
   }

   /**
    * check that the element defined by this name is declared directly in the type
    */
   public static boolean isAttributeDeclaredIn(final XSAttributeUse attr, 
                                               final XSComplexTypeDefinition type, 
                                               final boolean recursive) 
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("is Attribute " + attr.getAttrDeclaration().getName() + " declared in " + type.getName());
      }

      //check on parent if not recursive
      if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) 
      {
         XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("testing if it is not on parent " + parent.getName());
         }
         if (SchemaUtil.isAttributeDeclaredIn(attr, parent, true))
         {
            return false;
         }
      }

      //check on this type  (also checks recursively)
      boolean found = false;
      final XSObjectList attrs = type.getAttributeUses();
      for (int i = 0; i < attrs.getLength(); i++) 
      {
         if ((XSAttributeUse)attrs.item(i) == attr)
         {
            found = true;
            break;
         }
      }

      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("is Attribute " + attr.getName() +
                      " declared in " + type.getName() + ": " + found);
      }

      return found;
   }

   /**
    * check that the element defined by this name is declared directly in the type
    * -> idem with string
    */
   public static boolean isAttributeDeclaredIn(String attrName, String namespace, XSComplexTypeDefinition type, boolean recursive) 
   {
      boolean found = false;

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("is Attribute " + attrName + " declared in " + type.getName());

      if (attrName.startsWith("@"))
         attrName = attrName.substring(1);

      //check on parent if not recursive
      if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) 
      {
         XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("testing if it is not on parent " + parent.getName());
         if (SchemaUtil.isAttributeDeclaredIn(attrName, namespace, parent, true))
            return false;
      }

      //check on this type (also checks recursively)
      final XSObjectList attrs = type.getAttributeUses();
      for (int i = 0; i < attrs.getLength() && !found; i++) 
      {
         final XSAttributeUse anAttr = (XSAttributeUse) attrs.item(i);
         if (anAttr != null) 
         {
            String name = anAttr.getName();
            if (name == null || name.length() == 0)
               name = anAttr.getAttrDeclaration().getName();
            if (EqualsHelper.nullSafeEquals(namespace, anAttr.getAttrDeclaration().getNamespace()) && attrName.equals(name))
               found = true;
         }
      }

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("is Attribute " + attrName + " declared in " + type.getName() + ": " + found);

      return found;
   }

   public static boolean doesAttributeComeFromExtension(XSAttributeUse attr, 
                                                        XSComplexTypeDefinition controlType) 
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("doesAttributeComeFromExtension for " + attr.getAttrDeclaration().getName() + 
                      " and controlType=" + controlType.getName());
      }

      if (controlType.getDerivationMethod() != XSConstants.DERIVATION_EXTENSION) 
      {
         return false;
      }

      final XSTypeDefinition baseType = controlType.getBaseType();
      if (baseType.getTypeCategory() != XSTypeDefinition.COMPLEX_TYPE) 
      {
         return false;
      }

      final XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) baseType;
      if (SchemaUtil.isAttributeDeclaredIn(attr, complexType, true)) 
      {
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("doesAttributeComeFromExtension: yes");
         return true;
      }

      //recursive call
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("doesAttributeComeFromExtension: recursive call on previous level");
      return SchemaUtil.doesAttributeComeFromExtension(attr, complexType);
   }

   /**
    * finds the minOccurs and maxOccurs of an element declaration
    *
    * @return a table containing minOccurs and MaxOccurs
    */
   public static Occurrence getOccurrence(final XSElementDeclaration elDecl) 
   {
      //get occurance on encosing element declaration
      final XSParticle particle =
         SchemaUtil.findCorrespondingParticleInComplexType(elDecl);
      final Occurrence result = particle == null ? new Occurrence(1, 1) : new Occurrence(particle);
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("getOccurrence for " + elDecl.getName() + 
                      ", " + result);
      }
      return result;
   }
}
