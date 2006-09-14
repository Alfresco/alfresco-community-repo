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
package org.alfresco.web.templating.xforms.schemabuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xs.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Source;
import java.util.Properties;

import org.alfresco.web.templating.*;

/**
 * An object that implements this interface can build an XForm that conforms to
 * the elements and attributes declared in an XML Schema.
 *
 * @author Brian Dueck
 * @version $Id: SchemaFormBuilder.java,v 1.16 2005/02/10 13:24:57 joernt Exp $
 */
public interface SchemaFormBuilder 
{

    ////////////////////////////////////////////////////////////////////////////

    public static class Occurs
    {
	public final static int UNBOUNDED = -1;
	
	public final int minimum;
	public final int maximum;

	public Occurs(final XSParticle particle)
	{
	    if (particle == null)
	    {
		this.minimum = 1;
		this.maximum = 1;
	    }
	    else
	    {
		this.minimum = particle.getMinOccurs();
		this.maximum = (particle.getMaxOccursUnbounded()
				? Occurs.UNBOUNDED
				: particle.getMaxOccurs());
	    }
	}

	public Occurs(final int minimum)
	{
	    this(minimum, UNBOUNDED);
	}

	public Occurs(final int minimum, final int maximum)
	{
	    this.minimum = minimum;
	    this.maximum = maximum;
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

    public final static Log LOGGER = 
	LogFactory.getLog(SchemaFormBuilder.class);

    /**
     * XMLSchema Instance Namespace declaration
     */
    public static final String XMLSCHEMA_INSTANCE_NAMESPACE_URI = 
	"http://www.w3.org/2001/XMLSchema-instance";

    /**
     * XMLNS Namespace declaration.
     */
    public static final String XMLNS_NAMESPACE_URI =
            "http://www.w3.org/2000/xmlns/";

    /**
     * XML Namespace declaration
     */
    public static final String XML_NAMESPACE_URI =
	"http://www.w3.org/XML/1998/namespace";

    /**
     * XForms namespace declaration.
     */
    public static final String XFORMS_NS = "http://www.w3.org/2002/xforms";

    /**
     * Chiba namespace declaration.
     */
    public static final String CHIBA_NS =
	"http://chiba.sourceforge.net/xforms";

    /**
     * XLink namespace declaration.
     */
    public static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    /**
     * XML Events namsepace declaration.
     */
    public static final String XMLEVENTS_NS = "http://www.w3.org/2001/xml-events";

    /**
     * Chiba prefix
     */
    public static final String chibaNSPrefix = "chiba:";

    /**
     * XForms prefix
     */
    public static final String xformsNSPrefix = "xforms:";

    /**
     * Xlink prefix
     */
    public static final String xlinkNSPrefix = "xlink:";

    /**
     * XMLSchema instance prefix *
     */
    public static final String xmlSchemaInstancePrefix = "xsi:";

    /**
     * XML Events prefix
     */
    public static final String xmleventsNSPrefix = "ev:";

    /**
     * Prossible values of the "@method" on the "submission" element
     */
    public static final String SUBMIT_METHOD_POST = "post";

    /**
     * __UNDOCUMENTED__
     */
    public static final String SUBMIT_METHOD_PUT = "put";

    /**
     * __UNDOCUMENTED__
     */
    public static final String SUBMIT_METHOD_GET = "get";

    /**
     * __UNDOCUMENTED__
     */
    public static final String SUBMIT_METHOD_FORM_DATA_POST = "form-data-post";

    /**
     * __UNDOCUMENTED__
     */
    public static final String SUBMIT_METHOD_URLENCODED_POST =
	"urlencoded-post";

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getAction();

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public Document getInstanceDocument();

    /**
     * Get the current set of properties used by implementations of SchemaFormBuilder.
     *
     * @return The list of properties.
     */
    public Properties getProperties();

    /**
     * Sets the property to the specified value. If the property exists, its value is overwritten.
     *
     * @param key   The implementation defined property key.
     * @param value The value for the property.
     */
    public void setProperty(String key, String value);

    /**
     * Gets the value for the specified property.
     *
     * @param key The implementation defined property key.
     * @return The property value if found, or null if the property cannot be located.
     */
    public String getProperty(String key);

    /**
     * Gets the value for the specified property, with a default if the property cannot be located.
     *
     * @param key          The implementation defined property key.
     * @param defaultValue This value will be returned if the property does not exists.
     * @return The property value if found, or defaultValue if the property cannot be located.
     */
    public String getProperty(String key, String defaultValue);

    /**
     * Properties choosed by the user
     */
    public String getRootTagName();

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getStylesheet();

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getSubmitMethod();

    /**
     * Generate the XForm based on a user supplied XML Schema.
     *
     * @param inputURI The document source for the XML Schema.
     * @return The Document containing the XForm.
     * @throws org.chiba.tools.schemabuilder.FormBuilderException
     *          If an error occurs building the XForm.
     */
    public Document buildForm(final TemplateType tt) 
	throws FormBuilderException;

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
    public String createCaption(String text);

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
    public String createCaption(XSAttributeDeclaration attribute);

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
    public String createCaption(XSElementDeclaration element);

    /**
     * Creates a form control for an XML Schema any type.
     * <p/>
     * This method is called when the form builder determines a form control is required for
     * an any type.
     * The implementation of this method is responsible for creating an XML element of the
     * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
     * for adding the returned element to the form and setting caption, bind, and other
     * standard elements and attributes.
     *
     * @param xForm       The XForm document.
     * @param controlType The XML Schema type for which the form control is to be created.
     * @return The element for the form control.
     */
    public Element createControlForAnyType(Document xForm,
                                           String caption,
                                           XSTypeDefinition controlType);

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
     * @param xForm       The XForm document.
     * @param controlType The XML Schema type for which the form control is to be created.
     * @return The element for the form control.
     */
    public Element createControlForAtomicType(Document xForm,
                                              String caption,
                                              XSSimpleTypeDefinition controlType);

    /**
     * Creates a form control for an XML Schema simple type restricted by an enumeration.
     * This method is called when the form builder determines a form control is required for
     * an enumerated type.
     * The implementation of this method is responsible for creating an XML element of the
     * appropriate type to receive a value for <b>controlType</b>. The caller is responsible
     * for adding the returned element to the form and setting caption, bind, and other
     * standard elements and attributes.
     *
     * @param xForm       The XForm document.
     * @param controlType The XML Schema type for which the form control is to be created.
     * @param caption     The caption for the form control. The caller The purpose of providing the caption
     *                    is to permit the implementation to add a <b>[Select1 .... ]</b> message that involves the caption.
     * @param bindElement The bind element for this control. The purpose of providing the bind element
     *                    is to permit the implementation to add a isValid attribute to the bind element that prevents
     *                    the <b>[Select1 .... ]</b> item from being selected.
     * @return The element for the form control.
     */
    public Element createControlForEnumerationType(Document xForm,
                                                   XSSimpleTypeDefinition controlType,
                                                   String caption,
                                                   Element bindElement);

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
     * @param xForm       The XForm document.
     * @param listType    The XML Schema list type for which the form control is to be created.
     * @param caption     The caption for the form control. The caller The purpose of providing the caption
     *                    is to permit the implementation to add a <b>[Select1 .... ]</b> message that involves the caption.
     * @param bindElement The bind element for this control. The purpose of providing the bind element
     *                    is to permit the implementation to add a isValid attribute to the bind element that prevents
     *                    the <b>[Select1 .... ]</b> item from being selected.
     * @return The element for the form control.
     */
    public Element createControlForListType(Document xForm,
                                            XSSimpleTypeDefinition listType,
                                            String caption,
                                            Element bindElement);

    /**
     * Creates a hint XML Schema annotated node (AttributeDecl or ElementDecl).
     * The implementation is responsible for providing an xforms:hint element for the
     * specified schemaNode suitable to be dsipalayed to users of the XForm. The caller
     * is responsible for adding the returned element to the form.
     * This typically includes extracting documentation from the element/attribute's
     * annotation/documentation elements and/or extracting the same information from the
     * element/attribute's type annotation/documentation.
     *
     * @param schemaNode The string value to be reformatted for use as a caption.
     * @return The xforms:hint element. If a null value is returned a hint is not added.
     */
    public Element createHint(Document xForm, XSObject schemaNode);

    /**
     * This method is invoked after the form builder is finished creating and processing
     * a bind element. Implementations may choose to use this method to add/inspect/modify
     * the bindElement prior to the builder moving onto the next bind element.
     *
     * @param bindElement The bind element being processed.
     */
    public void endBindElement(Element bindElement);

    /**
     * This method is invoked after the form builder is finished creating and processing
     * a form control. Implementations may choose to use this method to add/inspect/modify
     * the controlElement prior to the builder moving onto the next control.
     *
     * @param controlElement The form control element that was created.
     * @param controlType    The XML Schema type for which <b>controlElement</b> was created.
     */
    public void endFormControl(Element controlElement,
                               XSTypeDefinition controlType,
                               Occurs occurs);
    /**
     * __UNDOCUMENTED__
     *
     * @param groupElement __UNDOCUMENTED__
     */
    public void endFormGroup(Element groupElement,
                             XSTypeDefinition controlType,
                             Occurs occurs,
                             Element modelSection);

    /**
     * Reset the SchemaFormBuilder to default values.
     */
    public void reset();

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
     * @param controlType XML Schema type of the element/attribute this bind is for.
     * @param minOccurs   The minimum number of occurences for this element/attribute.
     * @param maxOccurs   The maximum number of occurences for this element/attribute.
     * @return The bind Element to use in the XForm - bindElement or a replacement.
     */
    public Element startBindElement(Element bindElement,
                                    XSTypeDefinition controlType,
                                    Occurs occurs);

    /**
     * This method is invoked after the form builder creates a form control
     * via a createControlForXXX() method but prior to decorating the form control
     * with common attributes such as a caption, hint, help text elements,
     * bind attributes, etc.
     * The returned element is used in the XForm in place of controlElement.
     * Implementations may choose to use this method to substitute controlElement
     * with a different element, or perform any other processing on controlElement
     * prior to it being added to the form.
     *
     * @param controlElement The form control element that was created.
     * @param controlType    The XML Schema type for which <b>controlElement</b> was created.
     * @return The Element to use in the XForm - controlElement or a replacement.
     */
    public Element startFormControl(Element controlElement,
                                    XSTypeDefinition controlType);

    /**
     * This method is invoked after an xforms:group element is created for the specified
     * ElementDecl. A group is created whenever an element is encountered in the XML Schema
     * that contains other elements and attributes (complex types or mixed content types).
     * The caller automatically adds the returned element to the XForm.
     *
     * @param groupElement  The groupElement being processed.
     * @param schemaElement The schemaElement for the group.
     * @return The group Element to use in the XForm - groupElement or a replacement. If a null
     *         value is returned, the group is not created.
     */
    public Element startFormGroup(Element groupElement,
                                  XSElementDeclaration schemaElement);
}
