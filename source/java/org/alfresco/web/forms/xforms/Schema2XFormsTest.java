/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.forms.xforms;

import java.io.*;
import java.util.*;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.util.BaseTest;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.chiba.xml.ns.NamespaceConstants;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JUnit tests to exercise the the schema to xforms converter
 * 
 * @author ariel backenroth
 */
public class Schema2XFormsTest 
   extends BaseTest
{

   private final static Log LOGGER = LogFactory.getLog(Schema2XFormsTest.class);

   public void testOneStringTestWithEmptyInstanceDocument()
      throws Exception
   {
      final Document schemaDocument = this.loadTestResourceDocument("xforms/unit-tests/automated/one-string-test.xsd");
      final Document xformsDocument = this.buildXForm(null, schemaDocument, "one-string-test");
      final JXPathContext xpathContext = JXPathContext.newContext(xformsDocument);
      Pointer pointer = xpathContext.getPointer("//*[@id='input_0']");
      assertNotNull(pointer);
      String s = ((Element)pointer.getNode()).getAttributeNS(NamespaceConstants.XFORMS_NS, "bind");
      assertNotNull(s);
      pointer = xpathContext.getPointer("//*[@id='" + s + "']");
      assertNotNull(pointer);
      assertEquals("true()", ((Element)pointer.getNode()).getAttributeNS(NamespaceConstants.XFORMS_NS, "required"));
      pointer = xpathContext.getPointer("//" + NamespaceConstants.XFORMS_PREFIX + ":instance[@id='instance_0']/one-string-test/string");
      assertNotNull(pointer);
      assertEquals("default-value", ((Element)pointer.getNode()).getTextContent());
   }

   public void testOneStringTestWithInstanceDocument()
      throws Exception
   {
      final Document instanceDocument = XMLUtil.parse("<one-string-test><string>test</string></one-string-test>");
      final Document schemaDocument = this.loadTestResourceDocument("xforms/unit-tests/automated/one-string-test.xsd");
      final Document xformsDocument = this.buildXForm(instanceDocument, schemaDocument, "one-string-test");
      final JXPathContext xpathContext = JXPathContext.newContext(xformsDocument);
      Pointer pointer = xpathContext.getPointer("//*[@id='input_0']");
      assertNotNull(pointer);
      String s = ((Element)pointer.getNode()).getAttributeNS(NamespaceConstants.XFORMS_NS, "bind");
      pointer = xpathContext.getPointer("//*[@id='" + s + "']");
      assertNotNull(pointer);
      assertEquals("true()", ((Element)pointer.getNode()).getAttributeNS(NamespaceConstants.XFORMS_NS, "required"));
      pointer = xpathContext.getPointer("//" + NamespaceConstants.XFORMS_PREFIX + ":instance[@id='instance_0']/one-string-test/string");
      assertNotNull(pointer);
      assertEquals("test", ((Element)pointer.getNode()).getTextContent());
   }

   public void testRepeatConstraintsTest()
      throws Exception
   {
      final Document schemaDocument = this.loadTestResourceDocument("xforms/unit-tests/automated/repeat-constraints-test.xsd");
      final Document xformsDocument = this.buildXForm(null, schemaDocument, "repeat-constraints-test");
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/one-to-inf", 
                                  new SchemaUtil.Occurrence(1, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/zero-to-inf", 
                                  new SchemaUtil.Occurrence(0, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/one-to-five", 
                                  new SchemaUtil.Occurrence(1, 5));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/three-to-five", 
                                  new SchemaUtil.Occurrence(3, 5));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/zero-to-five", 
                                  new SchemaUtil.Occurrence(0, 5));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/referenced-string", 
                                  new SchemaUtil.Occurrence(1, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-zero-to-inf", 
                                  new SchemaUtil.Occurrence(0, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-zero-to-inf/nested-zero-to-inf-inner-zero-to-inf", 
                                  new SchemaUtil.Occurrence(0, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-zero-to-inf/nested-zero-to-inf-inner-one-to-inf", 
                                  new SchemaUtil.Occurrence(1, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-one-to-inf", 
                                  new SchemaUtil.Occurrence(1, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-one-to-inf/nested-one-to-inf-inner-zero-to-inf", 
                                  new SchemaUtil.Occurrence(0, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-one-to-inf/nested-one-to-inf-inner-one-to-inf", 
                                  new SchemaUtil.Occurrence(1, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-three-to-five", 
                                  new SchemaUtil.Occurrence(3, 5));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-three-to-five/nested-three-to-five-inner-zero-to-inf", 
                                  new SchemaUtil.Occurrence(0, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-three-to-five/nested-three-to-five-inner-one-to-inf", 
                                  new SchemaUtil.Occurrence(1, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-outer-three-to-inf",
                                  new SchemaUtil.Occurrence(3, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-outer-three-to-inf/nested-outer-inner-five-to-inf",
                                  new SchemaUtil.Occurrence(5, SchemaUtil.Occurrence.UNBOUNDED));
      this.assertRepeatProperties(xformsDocument, 
                                  "/repeat-constraints-test/nested-outer-outer-three-to-inf/nested-outer-inner-five-to-inf/nested-inner-inner-seven-to-inf",
                                  new SchemaUtil.Occurrence(7, SchemaUtil.Occurrence.UNBOUNDED));
   }

   public void testRootElementWithExtension()
      throws Exception
   {
      final Document schemaDocument = this.loadTestResourceDocument("xforms/unit-tests/automated/root-element-with-extension-test.xsd");
      Document xformsDocument = this.buildXForm(null, schemaDocument, "without-extension-test");
      assertEquals(3, xformsDocument.getElementsByTagNameNS(NamespaceConstants.XFORMS_NS, "input").getLength());
      
      try
      {
         xformsDocument = this.buildXForm(null, schemaDocument, "with-extension-test");
         fail("expected failure creating xform with root element with-extension-test in schema " + XMLUtil.toString(schemaDocument));
      }
      catch (FormBuilderException fbe)
      {
      }
   }

   public void testSwitch()
      throws Exception
   {
      final Document schemaDocument = this.loadTestResourceDocument("xforms/unit-tests/automated/switch-test.xsd");
      final Document xformsDocument = this.buildXForm(null, schemaDocument, "switch-test");
      LOGGER.debug("generated xforms " + XMLUtil.toString(xformsDocument));
//      assertEquals(3, xformsDocument.getElementsByTagNameNS(NamespaceConstants.XFORMS_NS, "input").getLength());
//      
//      try
//      {
//         xformsDocument = this.buildXForm(null, schemaDocument, "with-extension-test");
//         fail("expected failure creating xform with root element with-extension-test in schema " + XMLUtil.toString(schemaDocument));
//      }
//      catch (FormBuilderException fbe)
//      {
//      }
   }

   private void assertRepeatProperties(final Document xformsDocument, final String nodeset, final SchemaUtil.Occurrence o)
   {
      final Element[] bindElements = this.resolveBind(xformsDocument, nodeset);
      assertNotNull("unable to resolve bind for nodeset " + nodeset, bindElements);
      assertFalse("unable to resolve bind for nodeset " + nodeset, 0 == bindElements.length);
      final Element nodesetBindElement = bindElements[bindElements.length - 1];
      assertEquals("unexpected minimum value for nodeset " + nodeset,
                   o.minimum, 
                   Integer.parseInt(nodesetBindElement.getAttributeNS(NamespaceConstants.XFORMS_NS, "minOccurs")));
      if (o.isUnbounded())
      {
         assertEquals("unexpected maximum value for nodeset " + nodeset,
                      "unbounded", 
                      nodesetBindElement.getAttributeNS(NamespaceConstants.XFORMS_NS, "maxOccurs"));
      }
      else
      {
         assertEquals("unexpected maximum value for nodeset " + nodeset,
                      o.maximum, 
                      Integer.parseInt(nodesetBindElement.getAttributeNS(NamespaceConstants.XFORMS_NS, "maxOccurs")));
      }
      assertEquals("unexpected required value for nodeset " + nodeset,
                   (o.minimum != 0 && nodesetBindElement.hasAttributeNS(NamespaceConstants.XFORMS_NS, "type")) + "()",
                   nodesetBindElement.getAttributeNS(NamespaceConstants.XFORMS_NS, "required"));

      JXPathContext xpathContext = JXPathContext.newContext(xformsDocument);
      String xpath = "//*[@" + NamespaceConstants.XFORMS_PREFIX + ":bind='" + nodesetBindElement.getAttribute("id") + "']";
      assertEquals(4, xpathContext.selectNodes(xpath).size());
      xpath = ("//" + NamespaceConstants.XFORMS_PREFIX + 
               ":repeat[@" + NamespaceConstants.XFORMS_PREFIX + 
               ":bind='" + nodesetBindElement.getAttribute("id") + "']");
      assertEquals(1, xpathContext.selectNodes(xpath).size());
      xpath = ("//" + NamespaceConstants.XFORMS_PREFIX + 
               ":trigger[@" + NamespaceConstants.XFORMS_PREFIX + 
               ":bind='" + nodesetBindElement.getAttribute("id") + "']");
      assertEquals(3, xpathContext.selectNodes(xpath).size());

      int nestingFactor = 1;
      for (int i = 0; i < bindElements.length - 1; i++)
      {
         final SchemaUtil.Occurrence parentO = this.occuranceFromBind(bindElements[i]);
         if (parentO.isRepeated())
         {
            nestingFactor = nestingFactor * (1 + parentO.minimum);
         }
      }
      final Pointer instance0 = xpathContext.getPointer("//" + NamespaceConstants.XFORMS_PREFIX + ":instance[@id='instance_0']");
      assertNotNull(instance0);
      assertNotNull(instance0.getNode());
      xpathContext = xpathContext.getRelativeContext(instance0);
      xpath = nodeset.substring(1);
      assertEquals("unexpected result for instance nodeset " + xpath + " in " + instance0.getNode(), 
                   nestingFactor * (o.minimum + 1), 
                   xpathContext.selectNodes(xpath).size());
      xpath = nodeset.substring(1) + "[@" + NamespaceService.ALFRESCO_PREFIX + ":prototype='true']";
      assertEquals("unexpected result for instance prototype nodeset " + nodeset + " in " + instance0.getNode(), 
                   nestingFactor, 
                   xpathContext.selectNodes(xpath).size());
   }

   /**
    * Returns the resolved bind and all parents binds for the nodeset.
    */
   private Element[] resolveBind(final Document xformsDocument, final String nodeset)
   {
      JXPathContext xpathContext = JXPathContext.newContext(xformsDocument);
      assertNotNull(nodeset);
      assertEquals('/', nodeset.charAt(0));
      final String rootNodePath = nodeset.replaceFirst("(\\/[^\\/]+).*", "$1");
      assertNotNull(rootNodePath);
      String xpath = ("//" + NamespaceConstants.XFORMS_PREFIX + 
                      ":bind[@" + NamespaceConstants.XFORMS_PREFIX + 
                      ":nodeset='" + rootNodePath + "']");
      Pointer pointer = xpathContext.getPointer(xpath);
      assertNotNull("unable to resolve xpath for root node " + xpath, pointer);
      assertNotNull("unable to resolve xpath for root node " + xpath, pointer.getNode());
      if (nodeset.equals(rootNodePath))
      {
         return new Element[] { (Element)pointer.getNode() };
      }
      xpathContext = xpathContext.getRelativeContext(pointer);
      // substring the path to the next slash and split it
      final LinkedList<Element> result = new LinkedList<Element>();
      result.add((Element)pointer.getNode());
      for (String p : nodeset.substring(rootNodePath.length() + 1).split("/"))
      {
         xpath = NamespaceConstants.XFORMS_PREFIX + ":bind[starts-with(@" + NamespaceConstants.XFORMS_PREFIX + ":nodeset, '" + p + "')]";
         pointer = xpathContext.getPointer(xpath);
         assertNotNull("unable to resolve path " + xpath + 
                       " on bind with nodeset " + result.getLast().getAttributeNS(NamespaceConstants.XFORMS_NS, "nodeset"),
                       pointer);
         assertNotNull("unable to resolve path " + xpath + 
                       " on bind with nodeset " + result.getLast().getAttributeNS(NamespaceConstants.XFORMS_NS, "nodeset"),
                       pointer.getNode());
         xpathContext = xpathContext.getRelativeContext(pointer);
         result.add((Element)pointer.getNode());
      }
      return (Element[])result.toArray(new Element[result.size()]);
   }

   private Document loadTestResourceDocument(final String path)
      throws IOException, SAXException
   {
      File f = new File(this.getResourcesDir());
      for (final String p : path.split("/"))
      {
         f = new File(f, p);
      }
      return XMLUtil.parse(f);
   }

   private Document buildXForm(final Document instanceDocument,
                               final Document schemaDocument,
                               final String rootElementName)
      throws FormBuilderException
   {
      final Schema2XForms s2xf = new Schema2XForms("/test_action",
                                                   Schema2XForms.SubmitMethod.POST,
                                                   "http://fake.base.url");
      return s2xf.buildXForm(instanceDocument, 
                             schemaDocument, 
                             rootElementName, 
                             new ResourceBundle()
                             {
                                public Object handleGetObject(final String key)
                                {
                                   if (key == null)
                                   {
                                      throw new NullPointerException();
                                   }
                                   return null;
                                }
                                
                                public Enumeration<String> getKeys()
                                {
                                   return new Vector<String>().elements();
                                }
                             });
   }


   private SchemaUtil.Occurrence occuranceFromBind(final Element bindElement)
   {
      return new SchemaUtil.Occurrence(bindElement.hasAttributeNS(NamespaceConstants.XFORMS_NS, "minOccurs")
                                       ? Integer.parseInt(bindElement.getAttributeNS(NamespaceConstants.XFORMS_NS, "minOccurs"))
                                       : 1,
                                       bindElement.hasAttributeNS(NamespaceConstants.XFORMS_NS, "maxOccurs")
                                       ? ("unbounded".equals(bindElement.getAttributeNS(NamespaceConstants.XFORMS_NS, "maxOccurs"))
                                          ? SchemaUtil.Occurrence.UNBOUNDED
                                          : Integer.parseInt(bindElement.getAttributeNS(NamespaceConstants.XFORMS_NS, "maxOccurs")))
                                       : 1);
   }
}