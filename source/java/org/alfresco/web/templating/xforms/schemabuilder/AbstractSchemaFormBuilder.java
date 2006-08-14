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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.xerces.xs.*;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.NamespaceCtx;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/*
 * Search for TODO for things remaining to-do in this implementation.
 *
 * TODO: Support configuration mechanism to allow properties to be set without programming.
 * TODO: i18n/l10n of messages, hints, captions. Possibly leverage org.chiba.i18n classes.
 * TODO: When Chiba supports itemset, use schema keyref and key constraints for validation.
 * TODO: Support namespaces in instance documents. Currently can't do this due to Chiba bugs.
 * TODO: Place default values for list and enumeration types at the beginning of the item list.
 *
 */

/**
 * An abstract implementation of the SchemaFormBuilder interface allowing
 * an XForm to be automatically generated for an XML Schema definition.
 * This abstract class implements the buildForm and buildFormAsString methods
 * and associated helper but relies on concrete subclasses to implement other
 * required interface methods (createXXX, startXXX, and endXXX methods).
 *
 * @author $Author: unl $
 * @version $Id: AbstractSchemaFormBuilder.java,v 1.25 2005/03/29 14:12:06 unl Exp $
 */
public abstract class AbstractSchemaFormBuilder implements SchemaFormBuilder {

    private final Comparator typeExtensionSorter = new Comparator() 
    {
	public int compare(Object obj1, Object obj2) 
	{
	    if (obj1 == null && obj2 != null)
		return -1;
	    else if (obj1 != null && obj2 == null)
		return 1;
	    else if (obj1 == obj2 || (obj1 == null && obj2 == null))
		return 0;
	    else 
	    {
		try
		{
		    final XSTypeDefinition type1 = (XSTypeDefinition) obj1;
		    final XSTypeDefinition type2 = (XSTypeDefinition) obj2;
		    return (type1.derivedFromType(type2, XSConstants.DERIVATION_EXTENSION)
			    ? 1
			    : (type2.derivedFromType(type1, XSConstants.DERIVATION_EXTENSION)
			       ? -1
			       : 0));
		}
		catch (ClassCastException ex) 
		{
		    String s = "ClassCastException in typeExtensionSorter: one of the types is not a type !";
		    s = s + "\n obj1 class = " + obj1.getClass().getName() + ", toString=" + obj1.toString();
		    s = s + "\n obj2 class = " + obj2.getClass().getName() + ", toString=" + obj2.toString();
		    SchemaFormBuilder.LOGGER.error(s, ex);
		    return 0;
		}
	    }
	}
    };

    private static final String PROPERTY_PREFIX =
            "http://www.chiba.org/properties/schemaFormBuilder/";
    /**
     * Property to control the cascading style sheet used for the XForm - corresponds to envelope@chiba:css-style.
     */
    public static final String CSS_STYLE_PROP =
            PROPERTY_PREFIX + "envelope@css-style";
    private static final String DEFAULT_CSS_STYLE_PROP = "style.css";

    /**
     * Property to control the selection of UI control for a selectOne control.
     * If a selectOne control has >= the number of values specified in this property,
     * it is considered a <b>long</b> list, and the UI control specified by
     * SELECTONE_UI_CONTROL_LONG_PROP is used. Otherwise, the value of SELECTONE_UI_CONTROL_SHORT_PROP
     * is used.
     */
    public static final String SELECTONE_LONG_LIST_SIZE_PROP =
            PROPERTY_PREFIX + "select1@longListSize";

    /**
     * Property to specify the selectMany UI control to be used when there are releatively few items
     * to choose from.
     */
    public static final String SELECTONE_UI_CONTROL_SHORT_PROP =
            PROPERTY_PREFIX + "select1@appearance/short";

    /**
     * Property to specify the selectMany UI control to be used when there are large numbers of items
     * to choose from.
     */
    public static final String SELECTONE_UI_CONTROL_LONG_PROP =
            PROPERTY_PREFIX + "select1@appearance/long";
    private static final String DEFAULT_SELECTONE_UI_CONTROL_SHORT_PROP =
            "full";
    private static final String DEFAULT_SELECTONE_UI_CONTROL_LONG_PROP =
            "minimal";

    /**
     * Property to control the selection of UI control for a selectMany control.
     * If a selectMany control has >= the number of values specified in this property,
     * it is considered a <b>long</b> list, and the UI control specified by
     * SELECTMANY_UI_CONTROL_LONG_PROP is used. Otherwise, the value of SELECTMANY_UI_CONTROL_SHORT_PROP
     * is used.
     */
    public static final String SELECTMANY_LONG_LIST_SIZE_PROP =
            PROPERTY_PREFIX + "select@longListSize";

    /**
     * Property to specify the selectMany UI control to be used when there are releatively few items
     * to choose from.
     */
    public static final String SELECTMANY_UI_CONTROL_SHORT_PROP =
            PROPERTY_PREFIX + "select@appearance/short";

    /**
     * Property to specify the selectMany UI control to be used when there are large numbers of items
     * to choose from.
     */
    public static final String SELECTMANY_UI_CONTROL_LONG_PROP =
            PROPERTY_PREFIX + "select@appearance/long";
    private static final String DEFAULT_SELECTMANY_UI_CONTROL_SHORT_PROP =
            "full";
    private static final String DEFAULT_SELECTMANY_UI_CONTROL_LONG_PROP =
            "compact";
    private static final String DEFAULT_LONG_LIST_MAX_SIZE = "6";

    /**
     * Property to control the box alignment of a group - corresponds to xforms:group@chiba:box-align.
     * There are four valid values for this property - right, left, top, and bottom.
     * The default value is <b>right</b>.
     */
    public static final String GROUP_BOX_ALIGN_PROP =
            PROPERTY_PREFIX + "group@box-align";
    private static final String DEFAULT_GROUP_BOX_ALIGN = "right";

    /**
     * Property to control the box orientation of a group - corresponds to xforms:group@chiba:box-orient.
     * There are two valid values for this property - vertical and horizontal.
     * The default value is <b>vertical</b>.
     */
    public static final String GROUP_BOX_ORIENT_PROP =
            PROPERTY_PREFIX + "group@box-orient";
    private static final String DEFAULT_GROUP_BOX_ORIENT = "vertical";

    /**
     * Property to control the width of a group - corresponds to xforms:group/@chiba:width.
     * This value may be expressed as a percentage value or as an absolute size.
     */
    public static final String GROUP_WIDTH_PROP =
            PROPERTY_PREFIX + "group@width";
    private static final String DEFAULT_GROUP_WIDTH = "60%";

    /**
     * Property to control the caption width of a group - corresponds to xforms:group/@chiba:caption-width.
     * This value may be expressed as a percentage value or as an absolute size.
     */
    public static final String GROUP_CAPTION_WIDTH_PROP =
            PROPERTY_PREFIX + "group@caption-width";
    private static final String DEFAULT_GROUP_CAPTION_WIDTH = "30%";

    /**
     * Property to control the border of a group - corresponds to xforms:group/@chiba:border.
     * A value of <b>0</b> indicates no border, a value of <b>1</b> indicates a border is provided.
     */
    public static final String GROUP_BORDER_PROP =
            PROPERTY_PREFIX + "group@border";
    private static final String DEFAULT_GROUP_BORDER = "0";

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
     */
    protected Document _instanceDocument;

    /**
     * __UNDOCUMENTED__
     */
    protected String _action;

    /**
     * Properties choosed by the user
     */
    protected String _rootTagName;

    /**
     * __UNDOCUMENTED__
     */
    protected String _stylesheet;

    /**
     * __UNDOCUMENTED__
     */
    protected String _submitMethod;

    /**
     * __UNDOCUMENTED__
     */
    protected String _base;

    /**
     * __UNDOCUMENTED__
     */
    protected WrapperElementsBuilder _wrapper = new XHTMLWrapperElementsBuilder();

    /**
     * __UNDOCUMENTED__
     */
    protected boolean _useSchemaTypes = false;

    private DocumentBuilder documentBuilder;

    /**
     * generic counter -> replaced by an hashMap with:
     * keys: name of the elements
     * values: "Long" representing the counter for this element
     */

    //private long refCounter;
    private HashMap counter;
    private final Properties properties = new Properties();
    protected XSModel schema;
    private String targetNamespace;

    private final Map namespacePrefixes = new HashMap();

    // typeTree
    // each entry is keyed by the type name
    // value is an ArrayList that contains the XSTypeDefinition's which
    // are compatible with the specific type. Compatible means that
    // can be used as a substituted type using xsi:type
    // In order for it to be compatible, it cannot be abstract, and
    // it must be derived by extension.
    // The ArrayList does not contain its own type + has the other types only once
    private final TreeMap typeTree = new TreeMap();

    /**
     * Creates a new AbstractSchemaFormBuilder object.
     *
     * @param rootTagName    __UNDOCUMENTED__
     * @param instanceSource __UNDOCUMENTED__
     * @param action         __UNDOCUMENTED__
     * @param submitMethod   __UNDOCUMENTED__
     * @param wrapper        __UNDOCUMENTED__
     * @param stylesheet     __UNDOCUMENTED__
     */
    public AbstractSchemaFormBuilder(String rootTagName,
                                     Document instanceDocument,
                                     String action,
                                     String submitMethod,
                                     WrapperElementsBuilder wrapper,
                                     String stylesheet,
                                     String base,
                                     boolean userSchemaTypes) {
        this._rootTagName = rootTagName;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException x) {
            x.printStackTrace();
        }

        reset();
        this._instanceDocument = instanceDocument;

        this._action = action;
        this._stylesheet = stylesheet;
        this._base = base;
        this._useSchemaTypes = userSchemaTypes;

        //control if it is one of the SUBMIT_METHOD attributes?
        this._submitMethod = submitMethod;
        this._wrapper = wrapper;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getAction() {
        return _action;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public Document getInstanceDocument() {
        return _instanceDocument;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param key   __UNDOCUMENTED__
     * @param value __UNDOCUMENTED__
     */
    public void setProperty(String key, String value) {
        getProperties().setProperty(key, value);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param key __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param key          __UNDOCUMENTED__
     * @param defaultValue __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getRootTagName() {
        return _rootTagName;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getStylesheet() {
        return _stylesheet;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @return __UNDOCUMENTED__
     */
    public String getSubmitMethod() {
        return _submitMethod;
    }

    private void loadSchema(String inputURI)
	throws ClassNotFoundException,
	       InstantiationException,
	       IllegalAccessException 
    {

	// Get DOM Implementation using DOM Registry
        System.setProperty(DOMImplementationRegistry.PROPERTY,
			   "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
        DOMImplementationRegistry registry =
                DOMImplementationRegistry.newInstance();
        Object o = registry.getDOMImplementation("XS-Loader");
        if (o instanceof XSImplementation) 
	{
            XSImplementation impl = (XSImplementation) o;
            XSLoader schemaLoader = impl.createXSLoader(null);
            this.schema = schemaLoader.loadURI(inputURI);
        } 
	else if (o != null) 
	{
            if (LOGGER.isDebugEnabled()) 
                LOGGER.debug("DOMImplementation is not a XSImplementation: "
			     + o.getClass().getName());
            throw new RuntimeException(o.getClass().getName() + " is not a XSImplementation");
        }
    }

    /**
     * builds a form from a XML schema.
     *
     * @param inputURI the URI of the Schema to be used
     * @return __UNDOCUMENTED__
     * @throws FormBuilderException __UNDOCUMENTED__
     */
    public Document buildForm(String inputFile) 
	throws FormBuilderException {
        try {
            this.loadSchema(new File(inputFile).toURI().toString());
            this.buildTypeTree(schema);

            //refCounter = 0;
            counter = new HashMap();

            Document xForm = createFormTemplate(_rootTagName,
						_rootTagName + " Form",
						getProperty(CSS_STYLE_PROP, 
							    DEFAULT_CSS_STYLE_PROP));

            //this.buildInheritenceTree(schema);
            Element envelopeElement = xForm.getDocumentElement();

            //Element formSection = (Element) envelopeElement.getElementsByTagNameNS(CHIBA_NS, "form").item(0);
            //Element formSection =(Element) envelopeElement.getElementsByTagName("body").item(0);
            //find form element: last element created
            NodeList children = xForm.getDocumentElement().getChildNodes();

            Element formSection = (Element)children.item(children.getLength() - 1);
            Element modelSection = (Element)
		envelopeElement.getElementsByTagNameNS(XFORMS_NS, "model").item(0);

            //add XMLSchema if we use schema types
            if (_useSchemaTypes && modelSection != null)
                modelSection.setAttributeNS(XFORMS_NS,
					    this.getXFormsNSPrefix() + "schema",
					    new File(inputFile).toURI().toString());

            //change stylesheet
            String stylesheet = this.getStylesheet();

            if (stylesheet != null && stylesheet.length() != 0)
                envelopeElement.setAttributeNS(CHIBA_NS,
					       this.getChibaNSPrefix() + "stylesheet",
					       stylesheet);

            // TODO: Commented out because comments aren't output properly by the Transformer.
            //String comment = "This XForm was automatically generated by " + this.getClass().getName() + " on " + (new Date()) + System.getProperty("line.separator") + "    from the '" + rootElementName + "' element from the '" + schema.getSchemaTargetNS() + "' XML Schema.";
            //xForm.insertBefore(xForm.createComment(comment),envelopeElement);
            //xxx XSDNode node = findXSDNodeByName(rootElementTagName,schemaNode.getElementSet());

            //check if target namespace
            //no way to do this with XS API ? load DOM document ?
            //TODO: find a better way to find the targetNamespace
            try 
	    {
                Document domDoc = DOMUtil.parseXmlFile(inputFile, true, false);
                if (domDoc != null) 
		{
                    Element root = domDoc.getDocumentElement();
                    targetNamespace = root.getAttribute("targetNamespace");
                    if (targetNamespace != null && targetNamespace.length() == 0)
                        targetNamespace = null;
                }
		LOGGER.debug("using targetNamespace " + targetNamespace);
            } catch (Exception ex) {
                LOGGER.error("Schema not loaded as DOM document: " + ex.getMessage());
            }

            //if target namespace & we use the schema types: add it to form ns declarations
            if (_useSchemaTypes && 
		targetNamespace != null && targetNamespace.length() != 0)
                envelopeElement.setAttributeNS(XMLNS_NAMESPACE_URI,
					       "xmlns:schema",
					       targetNamespace);

            //TODO: WARNING: in Xerces 2.6.1, parameters are switched !!! (name, namespace)
            //XSElementDeclaration rootElementDecl =schema.getElementDeclaration(targetNamespace, _rootTagName);
            XSElementDeclaration rootElementDecl =
		this.schema.getElementDeclaration(_rootTagName, targetNamespace);

            if (rootElementDecl == null) {
                //DEBUG
                rootElementDecl = this.schema.getElementDeclaration(targetNamespace,  
								    _rootTagName);
                if (rootElementDecl != null && LOGGER.isDebugEnabled())
                    LOGGER.debug("getElementDeclaration: inversed parameters OK !!!");

                throw new FormBuilderException("Invalid root element tag name ["
					       + _rootTagName
					       + ", targetNamespace="
					       + targetNamespace
					       + "]");
            }

            Element instanceElement = (Element)
		modelSection.appendChild(xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "instance"));
            this.setXFormsId(instanceElement);

            Element rootElement;
	    if (_instanceDocument != null)
	    {
		Element instanceDocumentElement = _instanceDocument.getDocumentElement();
		if (!instanceDocumentElement.getNodeName().equals(_rootTagName)) 
		    throw new IllegalArgumentException("instance document root tag name invalid.  " +
						       "expected " + _rootTagName +
						       ", got " + instanceDocumentElement.getNodeName());
		LOGGER.debug("importing rootElement from other document");
		rootElement = (Element)xForm.importNode(instanceDocumentElement, true);
		instanceElement.appendChild(rootElement);
		
		//add XMLSchema instance NS
		String prefix = xmlSchemaInstancePrefix.substring(0, xmlSchemaInstancePrefix.length() - 1);
		if (!rootElement.hasAttributeNS(XMLNS_NAMESPACE_URI, prefix))
		    rootElement.setAttributeNS(XMLNS_NAMESPACE_URI, "xmlns:" + prefix, XMLSCHEMA_INSTANCE_NAMESPACE_URI);
		
		//possibility abandonned for the moment:
		//modify the instance to add the correct "xsi:type" attributes wherever needed
		//this.addXSITypeAttributes(rootElement);
	    }
	    else
	    {
		rootElement = (Element)
		    instanceElement.appendChild(xForm.createElement(_rootTagName));
	    }

            Element formContentWrapper =
                    _wrapper.createGroupContentWrapper(formSection);
            addElement(xForm,
		       modelSection,
		       formContentWrapper,
		       rootElementDecl,
		       rootElementDecl.getTypeDefinition(),
		       "/" + getElementName(rootElementDecl, xForm));

            Element submitInfoElement = (Element)
		modelSection.appendChild(xForm.createElementNS(XFORMS_NS,
							       getXFormsNSPrefix() + "submission"));

            //submitInfoElement.setAttributeNS(XFORMS_NS,getXFormsNSPrefix()+"id","save");
            String submissionId = this.setXFormsId(submitInfoElement);

            //action
	    submitInfoElement.setAttributeNS(XFORMS_NS,
					     getXFormsNSPrefix() + "action",
					     _action == null ? "" : _action);

            //method
	    submitInfoElement.setAttributeNS(XFORMS_NS,
					     getXFormsNSPrefix() + "method",
					     (_submitMethod != null && _submitMethod.length() != 0
					      ? _submitMethod
					      :  AbstractSchemaFormBuilder.SUBMIT_METHOD_POST));

            //Element submitButton = (Element) formSection.appendChild(xForm.createElementNS(XFORMS_NS,getXFormsNSPrefix()+"submit"));
            Element submitButton =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "submit");
            Element submitControlWrapper =
                    _wrapper.createControlsWrapper(submitButton);
            formContentWrapper.appendChild(submitControlWrapper);
            submitButton.setAttributeNS(XFORMS_NS,
					getXFormsNSPrefix() + "submission",
					submissionId);
            this.setXFormsId(submitButton);

            Element submitButtonCaption = (Element)
		submitButton.appendChild(xForm.createElementNS(XFORMS_NS,
							       getXFormsNSPrefix() + "label"));
            submitButtonCaption.appendChild(xForm.createTextNode("Submit"));
            this.setXFormsId(submitButtonCaption);
            return xForm;
        } catch (ParserConfigurationException x) {
            throw new FormBuilderException(x);
        } catch (ClassNotFoundException x) {
            throw new FormBuilderException(x);
        } catch (InstantiationException x) {
            throw new FormBuilderException(x);
        } catch (IllegalAccessException x) {
            throw new FormBuilderException(x);
        }
    }

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
                               int minOccurs,
                               int maxOccurs) 
    {
    }

    /**
     * __UNDOCUMENTED__
     */
    public void reset() 
    {
        //refCounter = 0;
        counter = new HashMap();
        setProperty(CSS_STYLE_PROP, DEFAULT_CSS_STYLE_PROP);
        setProperty(SELECTMANY_LONG_LIST_SIZE_PROP, DEFAULT_LONG_LIST_MAX_SIZE);
        setProperty(SELECTMANY_UI_CONTROL_SHORT_PROP,
                DEFAULT_SELECTMANY_UI_CONTROL_SHORT_PROP);
        setProperty(SELECTMANY_UI_CONTROL_LONG_PROP,
                DEFAULT_SELECTMANY_UI_CONTROL_LONG_PROP);
        setProperty(SELECTONE_LONG_LIST_SIZE_PROP, DEFAULT_LONG_LIST_MAX_SIZE);
        setProperty(SELECTONE_UI_CONTROL_SHORT_PROP,
                DEFAULT_SELECTONE_UI_CONTROL_SHORT_PROP);
        setProperty(SELECTONE_UI_CONTROL_LONG_PROP,
                DEFAULT_SELECTONE_UI_CONTROL_LONG_PROP);
        setProperty(GROUP_BOX_ALIGN_PROP, DEFAULT_GROUP_BOX_ALIGN);
        setProperty(GROUP_BOX_ORIENT_PROP, DEFAULT_GROUP_BOX_ORIENT);
        setProperty(GROUP_CAPTION_WIDTH_PROP, DEFAULT_GROUP_CAPTION_WIDTH);
        setProperty(GROUP_WIDTH_PROP, DEFAULT_GROUP_WIDTH);
        setProperty(GROUP_BORDER_PROP, DEFAULT_GROUP_BORDER);
    }

    /**
     * Returns the most-specific built-in base type for the provided type.
     */
    protected short getBuiltInType(XSTypeDefinition type) {
        // type.getName() may be 'null' for anonymous types, so compare against
        // static string (see bug #1172541 on sf.net)
        if (("anyType").equals(type.getName())) {
            return XSConstants.ANYSIMPLETYPE_DT;
        } else {
            XSSimpleTypeDefinition simpleType = (XSSimpleTypeDefinition) type;

            //get built-in type
            //only working method found: getBuiltInKind, but it returns a short !
            //XSTypeDefinition builtIn = simpleType.getPrimitiveType();
            /*XSTypeDefinition builtIn = type.getBaseType();
            if (builtIn == null) {
                // always null for a ListType
                if (simpleType.getItemType() != null) //if not null it's a list
                    return getBuiltInType(simpleType.getItemType());
                else
                    return simpleType;
            }
            else if(LOGGER.isDebugEnabled())
                LOGGER.debug(" -> builtinType="+builtIn.getName());
            return builtIn;*/

            short result = simpleType.getBuiltInKind();
            if (result == XSConstants.LIST_DT) {
                result = getBuiltInType(simpleType.getItemType());
            }
            return result;
        }
    }

    /**
     * get the name of a datatype defined by its value in XSConstants
     * TODO: find an automatic way to do this !
     *
     * @param dt the short representating this datatype from XSConstants
     * @return the name of the datatype
     */
    public String getDataTypeName(short dt) {
        String name = "";
        switch (dt) {
            case XSConstants.ANYSIMPLETYPE_DT:
                name = "anyType";
                break;
            case XSConstants.ANYURI_DT:
                name = "anyURI";
                break;
            case XSConstants.BASE64BINARY_DT:
                name = "base64Binary";
                break;
            case XSConstants.BOOLEAN_DT:
                name = "boolean";
                break;
            case XSConstants.BYTE_DT:
                name = "byte";
                break;
            case XSConstants.DATE_DT:
                name = "date";
                break;
            case XSConstants.DATETIME_DT:
                name = "dateTime";
                break;
            case XSConstants.DECIMAL_DT:
                name = "decimal";
                break;
            case XSConstants.DOUBLE_DT:
                name = "double";
                break;
            case XSConstants.DURATION_DT:
                name = "duration";
                break;
            case XSConstants.ENTITY_DT:
                name = "ENTITY";
                break;
            case XSConstants.FLOAT_DT:
                name = "float";
                break;
            case XSConstants.GDAY_DT:
                name = "gDay";
                break;
            case XSConstants.GMONTH_DT:
                name = "gMonth";
                break;
            case XSConstants.GMONTHDAY_DT:
                name = "gMonthDay";
                break;
            case XSConstants.GYEAR_DT:
                name = "gYear";
                break;
            case XSConstants.GYEARMONTH_DT:
                name = "gYearMonth";
                break;
            case XSConstants.ID_DT:
                name = "ID";
                break;
            case XSConstants.IDREF_DT:
                name = "IDREF";
                break;
            case XSConstants.INT_DT:
                name = "int";
                break;
            case XSConstants.INTEGER_DT:
                name = "integer";
                break;
            case XSConstants.LANGUAGE_DT:
                name = "language";
                break;
            case XSConstants.LONG_DT:
                name = "long";
                break;
            case XSConstants.NAME_DT:
                name = "Name";
                break;
            case XSConstants.NCNAME_DT:
                name = "NCName";
                break;
            case XSConstants.NEGATIVEINTEGER_DT:
                name = "negativeInteger";
                break;
            case XSConstants.NMTOKEN_DT:
                name = "NMTOKEN";
                break;
            case XSConstants.NONNEGATIVEINTEGER_DT:
                name = "nonNegativeInteger";
                break;
            case XSConstants.NONPOSITIVEINTEGER_DT:
                name = "nonPositiveInteger";
                break;
            case XSConstants.NORMALIZEDSTRING_DT:
                name = "normalizedString";
                break;
            case XSConstants.NOTATION_DT:
                name = "NOTATION";
                break;
            case XSConstants.POSITIVEINTEGER_DT:
                name = "positiveInteger";
                break;
            case XSConstants.QNAME_DT:
                name = "QName";
                break;
            case XSConstants.SHORT_DT:
                name = "short";
                break;
            case XSConstants.STRING_DT:
                name = "string";
                break;
            case XSConstants.TIME_DT:
                name = "time";
                break;
            case XSConstants.TOKEN_DT:
                name = "TOKEN";
                break;
            case XSConstants.UNSIGNEDBYTE_DT:
                name = "unsignedByte";
                break;
            case XSConstants.UNSIGNEDINT_DT:
                name = "unsignedInt";
                break;
            case XSConstants.UNSIGNEDLONG_DT:
                name = "unsignedLong";
                break;
            case XSConstants.UNSIGNEDSHORT_DT:
                name = "unsignedShort";
                break;
        }
        return name;
    }

    /**
     * Returns the prefix for the Chiba namespace.
     */
    protected String getChibaNSPrefix() {
        return chibaNSPrefix;
    }

    protected String setXFormsId(Element el) {
        //remove the eventuel "id" attribute
        if (el.hasAttributeNS(SchemaFormBuilder.XFORMS_NS, "id"))
            el.removeAttributeNS(SchemaFormBuilder.XFORMS_NS, "id");

        //long count=this.incIdCounter();
        long count = 0;
        String name = el.getLocalName();
        Long l = (Long) counter.get(name);

        if (l != null)
            count = l.longValue();

        String id = name + "_" + count;

        //increment the counter
        counter.put(name, new Long(count + 1));
        el.setAttributeNS(SchemaFormBuilder.XFORMS_NS,
			  this.getXFormsNSPrefix() + "id",
			  id);
        return id;
    }

    /**
     * method to set an Id to this element and to all XForms descendants of this element
     */
    private void resetXFormIds(Element newControl) {
        if (newControl.getNamespaceURI() != null
                && newControl.getNamespaceURI().equals(XFORMS_NS))
            this.setXFormsId(newControl);

        //recursive call
        NodeList children = newControl.getChildNodes();
        int nb = children.getLength();
        for (int i = 0; i < nb; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE)
                this.resetXFormIds((Element) child);
        }
    }

    /**
     * Returns the prefix for the XForms namespace.
     */
    protected String getXFormsNSPrefix() {
        return xformsNSPrefix;
    }

    /**
     * Returns the prefix for the XLink namespace.
     */
    protected String getXLinkNSPrefix() {
        return xlinkNSPrefix;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm          __UNDOCUMENTED__
     * @param choicesElement __UNDOCUMENTED__
     * @param choiceValues   __UNDOCUMENTED__
     */
    protected void addChoicesForSelectControl(Document xForm,
                                              Element choicesElement,
                                              Vector choiceValues) {
        // sort the enums values and then add them as choices
        //
        // TODO: Should really put the default value (if any) at the top of the list.
        //
        List sortedList = choiceValues.subList(0, choiceValues.size());
        Collections.sort(sortedList);

        Iterator iterator = sortedList.iterator();

        while (iterator.hasNext()) 
	{
            String textValue = (String) iterator.next();
            Element item =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "item");
            this.setXFormsId(item);
            choicesElement.appendChild(item);

            Element captionElement =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "label");
            this.setXFormsId(captionElement);
            item.appendChild(captionElement);
            captionElement.appendChild(xForm.createTextNode(createCaption(textValue)));

            Element value =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "value");
            this.setXFormsId(value);
            item.appendChild(value);
            value.appendChild(xForm.createTextNode(textValue));
        }
    }

    //protected void addChoicesForSelectSwitchControl(Document xForm, Element choicesElement, Vector choiceValues, String bindIdPrefix) {
    protected void addChoicesForSelectSwitchControl(Document xForm,
                                                    Element choicesElement,
                                                    Vector choiceValues,
                                                    HashMap case_types) 
    {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addChoicesForSelectSwitchControl, values=");
            Iterator it = choiceValues.iterator();
            while (it.hasNext()) {
//String name=(String) it.next();
                XSTypeDefinition type = (XSTypeDefinition) it.next();
                String name = type.getName();
                LOGGER.debug("  - " + name);
            }
        }


        // sort the enums values and then add them as choices
        //
        // TODO: Should really put the default value (if any) at the top of the list.
        //
        /*List sortedList = choiceValues.subList(0, choiceValues.size());
        Collections.sort(sortedList);
        Iterator iterator = sortedList.iterator();*/
// -> no, already sorted
        Iterator iterator = choiceValues.iterator();
        while (iterator.hasNext()) {
            XSTypeDefinition type = (XSTypeDefinition) iterator.next();
            String textValue = type.getName();
            //String textValue = (String) iterator.next();

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("addChoicesForSelectSwitchControl, processing " + textValue);

            Element item =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "item");
            this.setXFormsId(item);
            choicesElement.appendChild(item);

            Element captionElement =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "label");
            this.setXFormsId(captionElement);
            item.appendChild(captionElement);
            captionElement.appendChild(xForm.createTextNode(createCaption(textValue)));

            Element value =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "value");
            this.setXFormsId(value);
            item.appendChild(value);
            value.appendChild(xForm.createTextNode(textValue));

/// action in the case

            Element action =
                    xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "action");
            this.setXFormsId(action);
            item.appendChild(action);

            action.setAttributeNS(XMLEVENTS_NS, xmleventsNSPrefix + "event", "xforms-select");

            Element toggle = xForm.createElementNS(XFORMS_NS,
						   getXFormsNSPrefix() + "toggle");
            this.setXFormsId(toggle);

            //build the case element
            Element caseElement =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "case");
            String case_id = this.setXFormsId(caseElement);
            case_types.put(textValue, caseElement);

            toggle.setAttributeNS(XFORMS_NS,
                    getXFormsNSPrefix() + "case",
                    case_id);

            //toggle.setAttributeNS(XFORMS_NS,getXFormsNSPrefix() + "case",bindIdPrefix + "_" + textValue +"_case");
            action.appendChild(toggle);
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm      __UNDOCUMENTED__
     * @param annotation __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    protected Element addHintFromDocumentation(Document xForm,
                                               XSAnnotation annotation) {
        if (annotation != null) {
            Element hintElement =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "hint");
            this.setXFormsId(hintElement);

            Text hintText =
                    (Text) hintElement.appendChild(xForm.createTextNode(""));

            //write annotation to empty doc
            Document doc = DOMUtil.newDocument(true, false);
            annotation.writeAnnotation(doc, XSAnnotation.W3C_DOM_DOCUMENT);

            //get "annotation" element
            NodeList annots =
                    doc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema",
                            "annotation");
            if (annots.getLength() > 0) {
                Element annotEl = (Element) annots.item(0);

                //documentation
                NodeList docos =
                        annotEl.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema",
                                "documentation");
                int nbDocos = docos.getLength();
                for (int j = 0; j < nbDocos; j++) {
                    Element doco = (Element) docos.item(j);

                    //get text value
                    String text = DOMUtil.getTextNodeAsString(doco);
                    hintText.appendData(text);

                    if (j < nbDocos - 1) {
                        hintText.appendData(" ");
                    }
                }
                return hintElement;
            }
            return null;
        }

        return null;
    }

    public XSParticle findCorrespondingParticleInComplexType(XSElementDeclaration elDecl) {
        XSParticle thisParticle = null;

        XSComplexTypeDefinition complexType = elDecl.getEnclosingCTDefinition();
        if (complexType != null) {
            XSParticle particle = complexType.getParticle();
            XSTerm term = particle.getTerm();
            XSObjectList particles;
            if (term instanceof XSModelGroup) {
                XSModelGroup group = (XSModelGroup) term;
                particles = group.getParticles();
                if (particles != null) {
                    int nb = particles.getLength();
                    int i = 0;
                    while (i < nb && thisParticle == null) {
                        XSParticle part = (XSParticle) particles.item(i);
                        //test term
                        XSTerm thisTerm = part.getTerm();
                        if (thisTerm == elDecl)
                            thisParticle = part;

                        i++;
                    }
                }
            }
        }
        return thisParticle;
    }

    /**
     * finds the minOccurs and maxOccurs of an element declaration
     *
     * @return a table containing minOccurs and MaxOccurs
     */
    public int[] getOccurance(XSElementDeclaration elDecl) {
        int minOccurs = 1;
        int maxOccurs = 1;

        //get occurance on encosing element declaration
        XSParticle particle =
                this.findCorrespondingParticleInComplexType(elDecl);
        if (particle != null) {
            minOccurs = particle.getMinOccurs();
            if (particle.getMaxOccursUnbounded())
                maxOccurs = -1;
            else
                maxOccurs = particle.getMaxOccurs();
        }

        //if not set, get occurance of model group content
        //no -> this is made in "addGroup" directly !
        /*if (minOccurs == 1 && maxOccurs == 1) {
            XSTypeDefinition type = elDecl.getTypeDefinition();
            if (type != null
                && type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                XSComplexTypeDefinition complexType =
                    (XSComplexTypeDefinition) type;
                XSParticle thisParticle = complexType.getParticle();
                if (thisParticle != null) {
                    minOccurs = thisParticle.getMinOccurs();
                    if (thisParticle.getMaxOccursUnbounded())
                        maxOccurs = -1;
                    else
                        maxOccurs = thisParticle.getMaxOccurs();
                }
            }
        }*/

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("getOccurance for "
                    + elDecl.getName()
                    + ", minOccurs="
                    + minOccurs
                    + ", maxOccurs="
                    + maxOccurs);

        int[] result = new int[2];
        result[0] = minOccurs;
        result[1] = maxOccurs;
        return result;
    }

    private void addAnyType(Document xForm,
                            Element modelSection,
                            Element formSection,
                            XSTypeDefinition controlType,
                            XSElementDeclaration owner,
                            String pathToRoot) {

        int[] occurance = this.getOccurance(owner);

        addSimpleType(xForm,
                modelSection,
                formSection,
                controlType,
                owner.getName(),
                owner,
                pathToRoot,
                occurance[0],
                occurance[1]);
    }

    private void addAttributeSet(Document xForm,
                                 Element modelSection,
                                 Element formSection,
                                 XSComplexTypeDefinition controlType,
                                 XSElementDeclaration owner,
                                 String pathToRoot,
                                 boolean checkIfExtension) {
        XSObjectList attrUses = controlType.getAttributeUses();

        if (attrUses != null) {
            int nbAttr = attrUses.getLength();
            for (int i = 0; i < nbAttr; i++) {
                XSAttributeUse currentAttributeUse =
                        (XSAttributeUse) attrUses.item(i);
                XSAttributeDeclaration currentAttribute =
                        currentAttributeUse.getAttrDeclaration();

//test if extended !
                if (checkIfExtension && this.doesAttributeComeFromExtension(currentAttributeUse, controlType)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("This attribute comes from an extension: recopy form controls. \n Model section: ");
                        DOMUtil.prettyPrintDOM(modelSection);
                    }

                    String attributeName = currentAttributeUse.getName();
                    if (attributeName == null || attributeName.equals(""))
                        attributeName = currentAttributeUse.getAttrDeclaration().getName();

//find the existing bind Id
//(modelSection is the enclosing bind of the element)
                    NodeList binds = modelSection.getElementsByTagNameNS(XFORMS_NS, "bind");
                    int j = 0;
                    int nb = binds.getLength();
                    String bindId = null;
                    while (j < nb && bindId == null) {
                        Element bind = (Element) binds.item(j);
                        String nodeset = bind.getAttributeNS(XFORMS_NS, "nodeset");
                        if (nodeset != null) {
                            String name = nodeset.substring(1); //remove "@" in nodeset
                            if (name.equals(attributeName))
                                bindId = bind.getAttributeNS(XFORMS_NS, "id");
                        }
                        j++;
                    }

//find the control
                    Element control = null;
                    if (bindId != null) {
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug("bindId found: " + bindId);

                        JXPathContext context = JXPathContext.newContext(formSection.getOwnerDocument());
                        Pointer pointer = context.getPointer("//*[@" + this.getXFormsNSPrefix() + "bind='" + bindId + "']");
                        if (pointer != null)
                            control = (Element) pointer.getNode();
                    }

//copy it
                    if (control == null) {
                        LOGGER.warn("Corresponding control not found");
                    } else {
                        Element newControl = (Element) control.cloneNode(true);
//set new Ids to XForm elements
                        this.resetXFormIds(newControl);

                        formSection.appendChild(newControl);
                    }

                } else {
                    String newPathToRoot;

                    if ((pathToRoot == null) || pathToRoot.equals("")) {
                        newPathToRoot = "@" + currentAttribute.getName();
                    } else if (pathToRoot.endsWith("/")) {
                        newPathToRoot =
                                pathToRoot + "@" + currentAttribute.getName();
                    } else {
                        newPathToRoot =
                                pathToRoot + "/@" + currentAttribute.getName();
                    }

                    XSSimpleTypeDefinition simpleType =
                            currentAttribute.getTypeDefinition();
                    //TODO SRA: UrType ?
                    /*if(simpleType==null){
                        simpleType=new UrType();
                    }*/

                    addSimpleType(xForm,
                            modelSection,
                            formSection,
                            simpleType,
                            currentAttributeUse,
                            newPathToRoot);
                }
            }
        }
    }

    private void addComplexType(Document xForm,
                                Element modelSection,
                                Element formSection,
                                XSComplexTypeDefinition controlType,
                                XSElementDeclaration owner,
                                String pathToRoot,
                                boolean relative,
                                boolean checkIfExtension) {

        if (controlType != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("addComplexType for " + controlType.getName());
                if (owner != null)
                    LOGGER.debug("	owner=" + owner.getName());
            }

            // add a group node and recurse
            //
            Element groupElement =
                    createGroup(xForm, modelSection, formSection, owner);
            Element groupWrapper = groupElement;

            if (groupElement != modelSection) {
                groupWrapper = _wrapper.createGroupContentWrapper(groupElement);
            }

            int occurance[] = this.getOccurance(owner);
            int minOccurs = occurance[0];
            int maxOccurs = occurance[1];

            Element repeatSection =
                    addRepeatIfNecessary(xForm,
                            modelSection,
                            groupWrapper,
                            controlType,
                            minOccurs,
                            maxOccurs,
                            pathToRoot);
            Element repeatContentWrapper = repeatSection;

            /*if(repeatSection!=groupWrapper)
               //we have a repeat
               {
                   repeatContentWrapper=_wrapper.createGroupContentWrapper(repeatSection);
                   addComplexTypeChildren(xForm,modelSection,repeatContentWrapper,controlType,owner,pathToRoot, true);
               }
               else
                   addComplexTypeChildren(xForm,modelSection,repeatContentWrapper,controlType,owner,pathToRoot, false);
             */
            if (repeatSection != groupWrapper) { //we have a repeat
                repeatContentWrapper =
                        _wrapper.createGroupContentWrapper(repeatSection);
                relative = true;
            }

            addComplexTypeChildren(xForm,
                    modelSection,
                    repeatContentWrapper,
                    controlType,
                    owner,
                    pathToRoot,
                    relative,
                    checkIfExtension);

            Element realModel = modelSection;
            if (relative) {
                //modelSection: find the last element put in the modelSection = bind
                realModel = DOMUtil.getLastChildElement(modelSection);
            }

            endFormGroup(groupElement,
                    controlType,
                    minOccurs,
                    maxOccurs,
                    realModel);

        } else if (LOGGER.isDebugEnabled())
            LOGGER.debug("addComplexType: control type is null for pathToRoot="
                    + pathToRoot);
    }

    private void addComplexTypeChildren(Document xForm,
                                        Element modelSection,
                                        Element formSection,
                                        XSComplexTypeDefinition controlType,
                                        XSElementDeclaration owner,
                                        String pathToRoot,
                                        boolean relative,
                                        boolean checkIfExtension) {

        if (controlType != null) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("addComplexTypeChildren for " + controlType.getName());
                if (owner != null)
                    LOGGER.debug("	owner=" + owner.getName());
            }
            if (controlType.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_MIXED
                    || (controlType.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE
                    && controlType.getAttributeUses() != null && controlType.getAttributeUses().getLength() > 0)
            ) {
                XSTypeDefinition base = controlType.getBaseType();
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("	Control type is mixed . base type=" + base.getName());

                if (base != null && base != controlType) {
                    if (base.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                        addSimpleType(xForm,
                                modelSection,
                                formSection,
                                (XSSimpleTypeDefinition) base,
                                owner,
                                pathToRoot);
                    } else
                        LOGGER.warn("addComplexTypeChildren for mixed type with basic type complex !");
                }
            } else if (LOGGER.isDebugEnabled())
                LOGGER.debug("	Content type = " + controlType.getContentType());


            // check for compatible subtypes
            // of controlType.
            // add a type switch if there are any
            // compatible sub-types (i.e. anything
            // that derives from controlType)
            // add child elements
            if (relative) {
                pathToRoot = "";

                //modelSection: find the last element put in the modelSection = bind
                modelSection = DOMUtil.getLastChildElement(modelSection);
            }

            //attributes
            addAttributeSet(xForm,
                    modelSection,
                    formSection,
                    controlType,
                    owner,
                    pathToRoot,
                    checkIfExtension);

            //process group
            XSParticle particle = controlType.getParticle();
            if (particle != null) {
                XSTerm term = particle.getTerm();
                if (term instanceof XSModelGroup) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("	Particle of "
                                + controlType.getName()
                                + " is a group --->");

                    XSModelGroup group = (XSModelGroup) term;

                    //get maxOccurs
                    int maxOccurs = particle.getMaxOccurs();
                    if (particle.getMaxOccursUnbounded()) {
                        maxOccurs = -1;
                    }
                    int minOccurs = particle.getMinOccurs();

                    //call addGroup on this group
                    this.addGroup(xForm,
                            modelSection,
                            formSection,
                            group,
                            controlType,
                            owner,
                            pathToRoot,
                            minOccurs,
                            maxOccurs,
                            checkIfExtension);

                } else if (LOGGER.isDebugEnabled())
                    LOGGER.debug("	Particle of "
                            + controlType.getName()
                            + " is not a group: "
                            + term.getClass().getName());
            }

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("--->end of addComplexTypeChildren for "
                        + controlType.getName());
        }
    }

    /**
     * add an element to the XForms document: the bind + the control
     * (only the control if "withBind" is false)
     */
    private void addElement(Document xForm,
                            Element modelSection,
                            Element formSection,
                            XSElementDeclaration elementDecl,
                            XSTypeDefinition controlType,
                            String pathToRoot) {

        if (controlType == null) {
            // TODO!!! Figure out why this happens... for now just warn...
            // seems to happen when there is an element of type IDREFS
            LOGGER.warn("WARNING!!! controlType is null for "
                    + elementDecl
                    + ", "
                    + elementDecl.getName());

            return;
        }

        switch (controlType.getTypeCategory()) {
            case XSTypeDefinition.SIMPLE_TYPE:
                {
                    addSimpleType(xForm,
                            modelSection,
                            formSection,
                            (XSSimpleTypeDefinition) controlType,
                            elementDecl,
                            pathToRoot);

                    break;
                }
            case XSTypeDefinition.COMPLEX_TYPE:
                {

                    if (controlType.getName() != null
                            && controlType.getName().equals("anyType")) {
                        addAnyType(xForm,
                                modelSection,
                                formSection,
                                (XSComplexTypeDefinition) controlType,
                                elementDecl,
                                pathToRoot);

                        break;
                    } else {

                        // find the types which are compatible(derived from) the parent type.
                        //
                        // This is used if we encounter a XML Schema that permits the xsi:type
                        // attribute to specify subtypes for the element.
                        //
                        // For example, the <address> element may be typed to permit any of
                        // the following scenarios:
                        // <address xsi:type="USAddress">
                        // </address>
                        // <address xsi:type="CanadianAddress">
                        // </address>
                        // <address xsi:type="InternationalAddress">
                        // </address>
                        //
                        // What we want to do is generate an XForm' switch element with cases
                        // representing any valid non-abstract subtype.
                        //
                        // <xforms:select1 xforms:bind="xsi_type_13"
                        //		  <xforms:label>Address</xforms:label>
                        //        <xforms:choices>
                        //                <xforms:item>
                        //                        <xforms:label>US Address Type</xforms:label>
                        //                        <xforms:value>USAddressType</xforms:value>
                        //                        <xforms:action ev:event="xforms-select">
                        //                                <xforms:toggle xforms:case="USAddressType-case"/>
                        //                        </xforms:action>
                        //                </xforms:item>
                        //                <xforms:item>
                        //                        <xforms:label>Canadian Address Type</xforms:label>
                        //                        <xforms:value>CanadianAddressType</xforms:value>
                        //                        <xforms:action ev:event="xforms-select">
                        //                                <xforms:toggle xforms:case="CanadianAddressType-case"/>
                        //                        </xforms:action>
                        //                </xforms:item>
                        //                <xforms:item>
                        //                        <xforms:label>International Address Type</xforms:label>
                        //                        <xforms:value>InternationalAddressType</xforms:value>
                        //                        <xforms:action ev:event="xforms-select">
                        //                                <xforms:toggle xforms:case="InternationalAddressType-case"/>
                        //                        </xforms:action>
                        //                </xforms:item>
                        //
                        //          </xforms:choices>
                        // <xforms:select1>
                        // <xforms:trigger>
                        //	<xforms:label>validate Address type</xforms:label>
                        //	<xforms:action>
                        //		<xforms:dispatch id="dispatcher" xforms:name="xforms-activate" xforms:target="select1_0"/>
                        //	</xforms:action>
                        //</xforms:trigger>
                        //
                        // <xforms:switch id="address_xsi_type_switch">
                        //      <xforms:case id="USAddressType-case" selected="false">
                        //          <!-- US Address Type sub-elements here-->
                        //      </xforms:case>
                        //      <xforms:case id="CanadianAddressType-case" selected="false">
                        //          <!-- US Address Type sub-elements here-->
                        //      </xforms:case>
                        //      ...
                        // </xforms:switch>
                        //
                        //   + change bindings to add:
                        //	- a bind for the "@xsi:type" attribute
                        //	- for each possible element that can be added through the use of an inheritance, add a "relevant" attribute:
                        //	ex: xforms:relevant="../@xsi:type='USAddress'"

                        // look for compatible types
                        //
                        String typeName = controlType.getName();
                        boolean relative = true;

                        if (typeName != null) {
                            TreeSet compatibleTypes = (TreeSet) typeTree.get(controlType.getName());
                            //TreeSet compatibleTypes = (TreeSet) typeTree.get(controlType);

                            if (compatibleTypes != null) {
                                relative = false;

                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("compatible types for "
                                            + typeName
                                            + ":");
                                    Iterator it1 = compatibleTypes.iterator();
                                    while (it1.hasNext()) {
                                        //String name = (String) it1.next();
                                        XSTypeDefinition compType = (XSTypeDefinition) it1.next();
                                        LOGGER.debug("          compatible type name=" + compType.getName());
                                    }
                                }

                                Element control =
                                        xForm.createElementNS(XFORMS_NS,
                                                getXFormsNSPrefix() + "select1");
                                String select1_id = this.setXFormsId(control);

                                Element choices =
                                        xForm.createElementNS(XFORMS_NS,
                                                getXFormsNSPrefix() + "choices");
                                this.setXFormsId(choices);

                                //get possible values
                                Vector enumValues = new Vector();
                                //add the type (if not abstract)
                                if (!((XSComplexTypeDefinition) controlType).getAbstract())
                                    enumValues.add(controlType);
                                //enumValues.add(typeName);

                                //add compatible types
                                Iterator it = compatibleTypes.iterator();
                                while (it.hasNext()) {
                                    enumValues.add(it.next());
                                }

                                if (enumValues.size() > 1) {

                                    String caption =
                                            createCaption(elementDecl.getName() + " Type");
                                    Element controlCaption =
                                            (Element) control.appendChild(xForm.createElementNS(XFORMS_NS,
                                                    getXFormsNSPrefix() + "label"));
                                    this.setXFormsId(controlCaption);
                                    controlCaption.appendChild(xForm.createTextNode(caption));

                                    // multiple compatible types for this element exist
                                    // in the schema - allow the user to choose from
                                    // between compatible non-abstract types
                                    Element bindElement =
                                            xForm.createElementNS(XFORMS_NS,
                                                    getXFormsNSPrefix() + "bind");
                                    String bindId =
                                            this.setXFormsId(bindElement);

                                    bindElement.setAttributeNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "nodeset",
                                            pathToRoot + "/@xsi:type");

                                    modelSection.appendChild(bindElement);
                                    control.setAttributeNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "bind",
                                            bindId);

                                    //add the "element" bind, in addition
                                    Element bindElement2 =
                                            xForm.createElementNS(XFORMS_NS,
                                                    getXFormsNSPrefix() + "bind");
                                    String bindId2 =
                                            this.setXFormsId(bindElement2);
                                    bindElement2.setAttributeNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "nodeset",
                                            pathToRoot);

                                    modelSection.appendChild(bindElement2);

                                    if (enumValues.size()
                                            < Long.parseLong(getProperty(SELECTONE_LONG_LIST_SIZE_PROP))) {
                                        control.setAttributeNS(XFORMS_NS,
                                                getXFormsNSPrefix() + "appearance",
                                                getProperty(SELECTONE_UI_CONTROL_SHORT_PROP));
                                    } else {
                                        control.setAttributeNS(XFORMS_NS,
                                                getXFormsNSPrefix() + "appearance",
                                                getProperty(SELECTONE_UI_CONTROL_LONG_PROP));

                                        // add the "Please select..." instruction item for the combobox
                                        // and set the isValid attribute on the bind element to check for the "Please select..."
                                        // item to indicate that is not a valid value
                                        //
                                        String pleaseSelect =
                                                "[Select1 " + caption + "]";
                                        Element item =
                                                xForm.createElementNS(XFORMS_NS,
                                                        getXFormsNSPrefix()
                                                + "item");
                                        this.setXFormsId(item);
                                        choices.appendChild(item);

                                        Element captionElement =
                                                xForm.createElementNS(XFORMS_NS,
                                                        getXFormsNSPrefix()
                                                + "label");
                                        this.setXFormsId(captionElement);
                                        item.appendChild(captionElement);
                                        captionElement.appendChild(xForm.createTextNode(pleaseSelect));

                                        Element value =
                                                xForm.createElementNS(XFORMS_NS,
                                                        getXFormsNSPrefix()
                                                + "value");
                                        this.setXFormsId(value);
                                        item.appendChild(value);
                                        value.appendChild(xForm.createTextNode(pleaseSelect));

                                        // not(purchaseOrder/state = '[Choose State]')
                                        //String isValidExpr = "not(" + bindElement.getAttributeNS(XFORMS_NS, "nodeset") + " = '" + pleaseSelect + "')";
                                        // ->no, not(. = '[Choose State]')
                                        String isValidExpr =
                                                "not( . = '"
                                                + pleaseSelect
                                                + "')";

                                        //check if there was a constraint
                                        String constraint =
                                                bindElement.getAttributeNS(XFORMS_NS,
                                                        "constraint");

                                        if ((constraint != null)
                                                && !constraint.equals("")) {
                                            constraint =
                                                    constraint
                                                    + " && "
                                                    + isValidExpr;
                                        } else {
                                            constraint = isValidExpr;
                                        }

                                        bindElement.setAttributeNS(XFORMS_NS,
                                                getXFormsNSPrefix()
                                                + "constraint",
                                                constraint);
                                    }

                                    Element choicesControlWrapper =
                                            _wrapper.createControlsWrapper(choices);
                                    control.appendChild(choicesControlWrapper);

                                    Element controlWrapper =
                                            _wrapper.createControlsWrapper(control);
                                    formSection.appendChild(controlWrapper);

                                    /////////////////                                      ///////////////
                                    // add content to select1
                                    HashMap case_types = new HashMap();
                                    addChoicesForSelectSwitchControl(xForm,
                                            choices,
                                            enumValues,
                                            case_types);

                                    /////////////////
                                    //add a trigger for this control (is there a way to not need it ?)
                                    Element trigger = xForm.createElementNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "trigger");
                                    formSection.appendChild(trigger);
                                    this.setXFormsId(trigger);
                                    Element label_trigger = xForm.createElementNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "label");
                                    this.setXFormsId(label_trigger);
                                    trigger.appendChild(label_trigger);
                                    String trigger_caption = createCaption("validate choice");
                                    label_trigger.appendChild(xForm.createTextNode(trigger_caption));
                                    Element action_trigger = xForm.createElementNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "action");
                                    this.setXFormsId(action_trigger);
                                    trigger.appendChild(action_trigger);
                                    Element dispatch_trigger = xForm.createElementNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "dispatch");
                                    this.setXFormsId(dispatch_trigger);
                                    action_trigger.appendChild(dispatch_trigger);
                                    dispatch_trigger.setAttributeNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "name",
                                            "DOMActivate");
                                    dispatch_trigger.setAttributeNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "target",
                                            select1_id);

                                    /////////////////
                                    //add switch
                                    Element switchElement =
                                            xForm.createElementNS(XFORMS_NS,
                                                    getXFormsNSPrefix() + "switch");
                                    this.setXFormsId(switchElement);

                                    Element switchControlWrapper =
                                            _wrapper.createControlsWrapper(switchElement);
                                    formSection.appendChild(switchControlWrapper);
                                    //formSection.appendChild(switchElement);

                                    /////////////// add this type //////////////
                                    Element firstCaseElement = (Element) case_types.get(controlType.getName());
                                    switchElement.appendChild(firstCaseElement);
                                    addComplexType(xForm,
                                            modelSection,
                                            firstCaseElement,
                                            (XSComplexTypeDefinition) controlType,
                                            elementDecl,
                                            pathToRoot,
                                            true,
                                            false);

                                    /////////////// add sub types //////////////
                                    it = compatibleTypes.iterator();
                                    // add each compatible type within
                                    // a case statement
                                    while (it.hasNext()) {
                                        /*String compatibleTypeName = (String) it.next();
                                        //WARNING: order of parameters inversed from the doc for 2.6.0 !!!
                                        XSTypeDefinition type =getSchema().getTypeDefinition(
                                                compatibleTypeName,
                                                targetNamespace);*/
                                        XSTypeDefinition type = (XSTypeDefinition) it.next();
                                        String compatibleTypeName = type.getName();

                                        if (LOGGER.isDebugEnabled()) {
                                            if (type == null)
                                                LOGGER.debug(">>>addElement: compatible type is null!! type="
                                                        + compatibleTypeName
                                                        + ", targetNamespace="
                                                        + targetNamespace);
                                            else
                                                LOGGER.debug("   >>>addElement: adding compatible type "
                                                        + type.getName());
                                        }

                                        if (type != null
                                                && type.getTypeCategory()
                                                == XSTypeDefinition
                                                .COMPLEX_TYPE) {

                                            //Element caseElement = (Element) xForm.createElementNS(XFORMS_NS,getXFormsNSPrefix() + "case");
                                            //caseElement.setAttributeNS(XFORMS_NS,getXFormsNSPrefix() + "id",bindId + "_" + type.getName() +"_case");
                                            //String case_id=this.setXFormsId(caseElement);
                                            Element caseElement =
                                                    (Element) case_types.get(type.getName());
                                            switchElement.appendChild(caseElement);

                                            addComplexType(xForm,
                                                    modelSection,
                                                    caseElement,
                                                    (XSComplexTypeDefinition) type,
                                                    elementDecl,
                                                    pathToRoot,
                                                    true,
                                                    true);

                                            //////
                                            // modify bind to add a "relevant" attribute that checks the value of @xsi:type
                                            //
                                            if (LOGGER.isDebugEnabled())
                                                DOMUtil.prettyPrintDOM(bindElement2);
                                            NodeList binds = bindElement2.getElementsByTagNameNS(XFORMS_NS, "bind");
                                            Element thisBind = null;
                                            int nb_binds = binds.getLength();
                                            int i = 0;
                                            while (i < nb_binds && thisBind == null) {
                                                Element subBind = (Element) binds.item(i);
                                                String name = subBind.getAttributeNS(XFORMS_NS, "nodeset");

                                                if (LOGGER.isDebugEnabled())
                                                    LOGGER.debug("Testing sub-bind with nodeset " + name);

                                                if (this.isElementDeclaredIn(name, (XSComplexTypeDefinition) type, false)
                                                        || this.isAttributeDeclaredIn(name, (XSComplexTypeDefinition) type, false)
                                                ) {
                                                    if (LOGGER.isDebugEnabled())
                                                        LOGGER.debug("Element/Attribute " + name + " declared in type " + type.getName() + ": adding relevant attribute");

                                                    //test sub types of this type
                                                    TreeSet subCompatibleTypes = (TreeSet) typeTree.get(type.getName());
                                                    //TreeSet subCompatibleTypes = (TreeSet) typeTree.get(type);

                                                    String newRelevant = null;
                                                    if (subCompatibleTypes == null || subCompatibleTypes.isEmpty()) {
                                                        //just add ../@xsi:type='type'
                                                        newRelevant = "../@xsi:type='" + type.getName() + "'";
                                                    } else {
                                                        //add ../@xsi:type='type' or ../@xsi:type='otherType' or ...
                                                        newRelevant = "../@xsi:type='" + type.getName() + "'";
                                                        Iterator it_ct = subCompatibleTypes.iterator();
                                                        while (it_ct.hasNext()) {
                                                            //String otherTypeName = (String) it_ct.next();
                                                            XSTypeDefinition otherType = (XSTypeDefinition) it_ct.next();
                                                            String otherTypeName = otherType.getName();
                                                            newRelevant = newRelevant + " or ../@xsi:type='" + otherTypeName + "'";
                                                        }
                                                    }

                                                    //change relevant attribute
                                                    String relevant = subBind.getAttributeNS(XFORMS_NS, "relevant");
                                                    if (relevant != null && !relevant.equals("")) {
                                                        newRelevant = "(" + relevant + ") and " + newRelevant;
                                                    }
                                                    if (newRelevant != null && !newRelevant.equals(""))
                                                        subBind.setAttributeNS(XFORMS_NS, getXFormsNSPrefix() + "relevant", newRelevant);
                                                }

                                                i++;
                                            }
                                        }
                                    }

                                    /*if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug(
                                            "###addElement for derived type: bind created:");
                                        DOMUtil.prettyPrintDOM(bindElement2);
                                    }*/

                                    // we're done
                                    //
                                    break;

                                } else if (enumValues.size() == 1) {
                                    // only one compatible type, set the controlType value
                                    // and fall through
                                    //
                                    //controlType = getSchema().getComplexType((String)enumValues.get(0));
                                    controlType =
                                            this.schema.getTypeDefinition((String) enumValues.get(0),
                                                    targetNamespace);
                                }
                            } else if (LOGGER.isDebugEnabled())
                                LOGGER.debug("No compatible type found for " + typeName);

                            //name not null but no compatibleType?
                            relative = true;
                        }

                        if (relative) //create the bind in case it is a repeat
                        {
                            if (LOGGER.isDebugEnabled())
                                LOGGER.debug(">>>Adding empty bind for " + typeName);

                            // create the <xforms:bind> element and add it to the model.
                            Element bindElement =
                                    xForm.createElementNS(XFORMS_NS,
                                            getXFormsNSPrefix() + "bind");
                            String bindId = this.setXFormsId(bindElement);
                            bindElement.setAttributeNS(XFORMS_NS,
                                    getXFormsNSPrefix() + "nodeset",
                                    pathToRoot);

                            modelSection.appendChild(bindElement);
                        } else if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("addElement: bind is not relative for "
                                    + elementDecl.getName());
                        }

                        //addComplexType(xForm,modelSection, formSection,(ComplexType)controlType,elementDecl,pathToRoot, relative);
                        addComplexType(xForm,
                                modelSection,
                                formSection,
                                (XSComplexTypeDefinition) controlType,
                                elementDecl,
                                pathToRoot,
                                true,
                                false);

                        break;
                    }
                }

            default : // TODO: add wildcard support
                LOGGER.warn("\nWARNING!!! - Unsupported type ["
                        + elementDecl.getType()
                        + "] for node ["
                        + controlType.getName()
                        + "]");
        }
    }

    /**
     * check that the element defined by this name is declared directly in the type
     */
    private boolean isElementDeclaredIn(String name, XSComplexTypeDefinition type, boolean recursive) {
        boolean found = false;

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("isElement " + name + " declared in " + type.getName());

//test if extension + declared in parent + not recursive -> NOK
        if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) {
            XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("testing if it is not on parent " + parent.getName());
            if (this.isElementDeclaredIn(name, parent, true))
                return false;
        }

        XSParticle particle = type.getParticle();
        if (particle != null) {
            XSTerm term = particle.getTerm();
            if (term instanceof XSModelGroup) {
                XSModelGroup group = (XSModelGroup) term;
                found = this.isElementDeclaredIn(name, group);
            }
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("isElement " + name + " declared in " + type.getName() + ": " + found);

        return found;
    }

    /**
     * private recursive method called by isElementDeclaredIn(String name, XSComplexTypeDefinition type)
     */
    private boolean isElementDeclaredIn(String name, XSModelGroup group) {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("isElement " + name + " declared in group " + group.getName());

        boolean found = false;
        XSObjectList particles = group.getParticles();
        int i = 0;
        int nb = particles.getLength();
        while (i < nb) {
            XSParticle subPart = (XSParticle) particles.item(i);
            XSTerm subTerm = subPart.getTerm();
            if (subTerm instanceof XSElementDeclaration) {
                XSElementDeclaration elDecl = (XSElementDeclaration) subTerm;
                if (name.equals(elDecl.getName()))
                    found = true;
            } else if (subTerm instanceof XSModelGroup) { //recursive
                found = this.isElementDeclaredIn(name, (XSModelGroup) subTerm);
            }

            i++;
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("isElement " + name + " declared in group " + group.getName() + ": " + found);
        return found;
    }

    private boolean doesElementComeFromExtension(XSElementDeclaration element, XSComplexTypeDefinition controlType) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("doesElementComeFromExtension for " + element.getName() + " and controlType=" + controlType.getName());
        boolean comesFromExtension = false;
        if (controlType.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) {
            XSTypeDefinition baseType = controlType.getBaseType();
            if (baseType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) baseType;
                if (this.isElementDeclaredIn(element.getName(), complexType, true)) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("doesElementComeFromExtension: yes");
                    comesFromExtension = true;
                } else { //recursive call
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("doesElementComeFromExtension: recursive call on previous level");
                    comesFromExtension = this.doesElementComeFromExtension(element, complexType);
                }
            }
        }
        return comesFromExtension;
    }

    /**
     * check that the element defined by this name is declared directly in the type
     */
    private boolean isAttributeDeclaredIn(XSAttributeUse attr, XSComplexTypeDefinition type, boolean recursive) {
        boolean found = false;

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("is Attribute " + attr.getAttrDeclaration().getName() + " declared in " + type.getName());

//check on parent if not recursive
        if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) {
            XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("testing if it is not on parent " + parent.getName());
            if (this.isAttributeDeclaredIn(attr, parent, true))
                return false;
        }

//check on this type  (also checks recursively)
        XSObjectList attrs = type.getAttributeUses();
        int nb = attrs.getLength();
        int i = 0;
        while (i < nb && !found) {
            XSAttributeUse anAttr = (XSAttributeUse) attrs.item(i);
            if (anAttr == attr)
                found = true;
            i++;
        }

//recursive call
/*if(!found && recursive &&
                type.getDerivationMethod()==XSConstants.DERIVATION_EXTENSION){
                    XSComplexTypeDefinition base=(XSComplexTypeDefinition) type.getBaseType();
                    if(base!=null && base!=type)
                        found = this.isAttributeDeclaredIn(attr, base, true);
                }*/

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("is Attribute " + attr.getName() + " declared in " + type.getName() + ": " + found);

        return found;
    }

    /**
     * check that the element defined by this name is declared directly in the type
     * -> idem with string
     */
    private boolean isAttributeDeclaredIn(String attrName, XSComplexTypeDefinition type, boolean recursive) {
        boolean found = false;

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("is Attribute " + attrName + " declared in " + type.getName());

        if (attrName.startsWith("@"))
            attrName = attrName.substring(1);

//check on parent if not recursive
        if (!recursive && type.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) {
            XSComplexTypeDefinition parent = (XSComplexTypeDefinition) type.getBaseType();
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("testing if it is not on parent " + parent.getName());
            if (this.isAttributeDeclaredIn(attrName, parent, true))
                return false;
        }

//check on this type (also checks recursively)
        XSObjectList attrs = type.getAttributeUses();
        int nb = attrs.getLength();
        int i = 0;
        while (i < nb && !found) {
            XSAttributeUse anAttr = (XSAttributeUse) attrs.item(i);
            if (anAttr != null) {
                String name = anAttr.getName();
                if (name == null || name.equals(""))
                    name = anAttr.getAttrDeclaration().getName();
                if (attrName.equals(name))
                    found = true;
            }
            i++;
        }

//recursive call -> no need
/*if(!found && recursive &&
                type.getDerivationMethod()==XSConstants.DERIVATION_EXTENSION){
                    XSComplexTypeDefinition base=(XSComplexTypeDefinition) type.getBaseType();
                    if(base!=null && base!=type)
                        found = this.isAttributeDeclaredIn(attrName, base, true);
                }*/

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("is Attribute " + attrName + " declared in " + type.getName() + ": " + found);

        return found;
    }

    private boolean doesAttributeComeFromExtension(XSAttributeUse attr, XSComplexTypeDefinition controlType) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("doesAttributeComeFromExtension for " + attr.getAttrDeclaration().getName() + " and controlType=" + controlType.getName());
        boolean comesFromExtension = false;
        if (controlType.getDerivationMethod() == XSConstants.DERIVATION_EXTENSION) {
            XSTypeDefinition baseType = controlType.getBaseType();
            if (baseType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) baseType;
                if (this.isAttributeDeclaredIn(attr, complexType, true)) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("doesAttributeComeFromExtension: yes");
                    comesFromExtension = true;
                } else { //recursive call
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("doesAttributeComeFromExtension: recursive call on previous level");
                    comesFromExtension = this.doesAttributeComeFromExtension(attr, complexType);
                }
            }
        }
        return comesFromExtension;
    }

    /**
     * checkIfExtension: if false, addElement is called wether it is an extension or not
     * if true, if it is an extension, element is recopied (and no additional bind)
     */
    private void addGroup(Document xForm,
                          Element modelSection,
                          Element formSection,
                          XSModelGroup group,
                          XSComplexTypeDefinition controlType,
                          XSElementDeclaration owner,
                          String pathToRoot,
                          int minOccurs,
                          int maxOccurs,
                          boolean checkIfExtension) {
        if (group != null) {

            Element repeatSection =
                    addRepeatIfNecessary(xForm,
                            modelSection,
                            formSection,
                            owner.getTypeDefinition(),
                            minOccurs,
                            maxOccurs,
                            pathToRoot);
            Element repeatContentWrapper = repeatSection;

            if (repeatSection != formSection) {
                //selector -> no more needed?
                //this.addSelector(xForm, repeatSection);
                //group wrapper
                repeatContentWrapper =
                        _wrapper.createGroupContentWrapper(repeatSection);
            }

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("addGroup from owner=" + owner.getName() + " and controlType=" + controlType.getName());

            XSObjectList particles = group.getParticles();
            for (int counter = 0; counter < particles.getLength(); counter++) {
                XSParticle currentNode = (XSParticle) particles.item(counter);
                XSTerm term = currentNode.getTerm();

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("	: next term = " + term.getName());

                int childMaxOccurs = currentNode.getMaxOccurs();
                if (currentNode.getMaxOccursUnbounded())
                    childMaxOccurs = -1;
                int childMinOccurs = currentNode.getMinOccurs();

                if (term instanceof XSModelGroup) {

                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("	term is a group");

                    addGroup(xForm,
                            modelSection,
                            repeatContentWrapper,
                            ((XSModelGroup) term),
                            controlType,
                            owner,
                            pathToRoot,
                            childMinOccurs,
                            childMaxOccurs,
                            checkIfExtension);
                } else if (term instanceof XSElementDeclaration) {
                    XSElementDeclaration element = (XSElementDeclaration) term;

                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("	term is an element declaration: "
                                + term.getName());

                    //special case for types already added because used in an extension
                    //do not add it when it comes from an extension !!!
                    //-> make a copy from the existing form control
                    if (checkIfExtension && this.doesElementComeFromExtension(element, controlType)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("This element comes from an extension: recopy form controls.\n Model Section=");
                            DOMUtil.prettyPrintDOM(modelSection);
                        }

                        //find the existing bind Id
                        //(modelSection is the enclosing bind of the element)
                        NodeList binds = modelSection.getElementsByTagNameNS(XFORMS_NS, "bind");
                        int i = 0;
                        int nb = binds.getLength();
                        String bindId = null;
                        while (i < nb && bindId == null) {
                            Element bind = (Element) binds.item(i);
                            String nodeset = bind.getAttributeNS(XFORMS_NS, "nodeset");
                            if (nodeset != null && nodeset.equals(element.getName()))
                                bindId = bind.getAttributeNS(XFORMS_NS, "id");
                            i++;
                        }

                        //find the control
                        Element control = null;
                        if (bindId != null) {
                            if (LOGGER.isDebugEnabled())
                                LOGGER.debug("bindId found: " + bindId);

                            JXPathContext context = JXPathContext.newContext(formSection.getOwnerDocument());
                            Pointer pointer = context.getPointer("//*[@" + this.getXFormsNSPrefix() + "bind='" + bindId + "']");
                            if (pointer != null)
                                control = (Element) pointer.getNode();
                        }

                        //copy it
                        if (control == null) {
                            LOGGER.warn("Corresponding control not found");
                        } else {
                            Element newControl = (Element) control.cloneNode(true);
                            //set new Ids to XForm elements
                            this.resetXFormIds(newControl);

                            repeatContentWrapper.appendChild(newControl);
                        }

                    } else { //add it normally
                        String elementName = getElementName(element, xForm);

                        String path = pathToRoot + "/" + elementName;

                        if (pathToRoot.equals("")) { //relative
                            path = elementName;
                        }

                        addElement(xForm,
                                modelSection,
                                repeatContentWrapper,
                                element,
                                element.getTypeDefinition(),
                                path);
                    }
                } else { //XSWildcard -> ignore ?
                    //LOGGER.warn("XSWildcard found in group from "+owner.getName()+" for pathToRoot="+pathToRoot);
                }
            }

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("--- end of addGroup from owner=" + owner.getName());
        }
    }

    /**
     * Add a repeat section if maxOccurs > 1.
     */
    private Element addRepeatIfNecessary(Document xForm,
                                         Element modelSection,
                                         Element formSection,
                                         XSTypeDefinition controlType,
                                         int minOccurs,
                                         int maxOccurs,
                                         String pathToRoot) {
        Element repeatSection = formSection;

        // add xforms:repeat section if this element re-occurs
        //
        if (maxOccurs != 1) 
	{
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("DEBUG: AddRepeatIfNecessary for multiple element for type "
			     + controlType.getName()
			     + ", maxOccurs=" + maxOccurs);

            //repeatSection = (Element) formSection.appendChild(xForm.createElementNS(XFORMS_NS,getXFormsNSPrefix() + "repeat"));
            repeatSection = xForm.createElementNS(XFORMS_NS,
						  getXFormsNSPrefix() + "repeat");

            //bind instead of repeat
            //repeatSection.setAttributeNS(XFORMS_NS,getXFormsNSPrefix() + "nodeset",pathToRoot);
            // bind -> last element in the modelSection
            Element bind = DOMUtil.getLastChildElement(modelSection);
            String bindId = null;

            if (bind != null && 
		bind.getLocalName() != null && 
		"bind".equals(bind.getLocalName())) {
                bindId = bind.getAttributeNS(SchemaFormBuilder.XFORMS_NS, "id");
            } else {
                LOGGER.warn("addRepeatIfNecessary: bind not found: "
			    + bind
			    + " (model selection name="
			    + modelSection.getNodeName()
			    + ")");

                //if no bind is found -> modelSection is already a bind, get its parent last child
                bind = DOMUtil.getLastChildElement(modelSection.getParentNode());

                if (bind != null &&
		    bind.getLocalName() != null && 
		    "bind".equals(bind.getLocalName())) {
                    bindId = bind.getAttributeNS(SchemaFormBuilder.XFORMS_NS, "id");
                } else {
                    LOGGER.warn("addRepeatIfNecessary: bind really not found");
                }
            }

            repeatSection.setAttributeNS(XFORMS_NS,
					 getXFormsNSPrefix() + "bind",
					 bindId);
            this.setXFormsId(repeatSection);

            //appearance=full is more user friendly
            repeatSection.setAttributeNS(XFORMS_NS,
                    getXFormsNSPrefix() + "appearance",
                    "full");

            //triggers
            this.addTriggersForRepeat(xForm,
				      formSection,
				      repeatSection,
				      minOccurs,
				      maxOccurs,
				      bindId);
	    
            Element controlWrapper =
                    _wrapper.createControlsWrapper(repeatSection);
            formSection.appendChild(controlWrapper);

            //add a group inside the repeat?
            Element group = xForm.createElementNS(XFORMS_NS,
						  this.getXFormsNSPrefix() + "group");
            this.setXFormsId(group);
            repeatSection.appendChild(group);
            repeatSection = group;
        }

        return repeatSection;
    }

    /**
     * if "createBind", a bind is created, otherwise bindId is used
     */
    private void addSimpleType(Document xForm,
                               Element modelSection,
                               Element formSection,
                               XSTypeDefinition controlType,
                               String owningElementName,
                               XSObject owner,
                               String pathToRoot,
                               int minOccurs,
                               int maxOccurs) {

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("addSimpleType for " + controlType.getName() + 
			 " (owningElementName=" + owningElementName + ")");

        // create the <xforms:bind> element and add it to the model.
        Element bindElement = xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "bind");
        String bindId = this.setXFormsId(bindElement);
        bindElement.setAttributeNS(XFORMS_NS,
                getXFormsNSPrefix() + "nodeset",
                pathToRoot);
        bindElement = (Element) modelSection.appendChild(bindElement);
        bindElement = startBindElement(bindElement, controlType, minOccurs, maxOccurs);

        // add a group if a repeat !
        if (owner instanceof XSElementDeclaration
                && maxOccurs != 1
        ) {
            Element groupElement = createGroup(xForm, modelSection, formSection, (XSElementDeclaration) owner);
            //set content
            Element groupWrapper = groupElement;
            if (groupElement != modelSection) {
                groupWrapper = _wrapper.createGroupContentWrapper(groupElement);
            }
            formSection = groupWrapper;
        }

        //eventual repeat
        Element repeatSection = addRepeatIfNecessary(xForm,
						     modelSection,
						     formSection,
						     controlType,
						     minOccurs,
						     maxOccurs,
						     pathToRoot);

        // create the form control element
        //put a wrapper for the repeat content, but only if it is really a repeat
        Element contentWrapper = repeatSection;

        if (repeatSection != formSection) {
            //content of repeat
            contentWrapper = _wrapper.createGroupContentWrapper(repeatSection);

            //if there is a repeat -> create another bind with "."
            Element bindElement2 =
                    xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "bind");
            String bindId2 = this.setXFormsId(bindElement2);
            bindElement2.setAttributeNS(XFORMS_NS,
                    getXFormsNSPrefix() + "nodeset",
                    ".");

            //recopy other attributes: required  and type
            // ->no, attributes shouldn't be copied
            /*String required = "required";
            String type = "type";
            if (bindElement.hasAttributeNS(XFORMS_NS, required)) {
                bindElement2.setAttributeNS(XFORMS_NS, getXFormsNSPrefix() + required,
                                            bindElement.getAttributeNS(XFORMS_NS, required));
            }
            if (bindElement.hasAttributeNS(XFORMS_NS, type)) {
                bindElement2.setAttributeNS(XFORMS_NS, getXFormsNSPrefix() + type,
                                            bindElement.getAttributeNS(XFORMS_NS, type));
            }*/

            bindElement.appendChild(bindElement2);
            bindId = bindId2;
        }

        String caption = createCaption(owningElementName);

        //Element formControl = (Element) contentWrapper.appendChild(createFormControl(xForm,caption,controlType,bindId,bindElement,minOccurs,maxOccurs));
        Element formControl = createFormControl(xForm,
						caption,
						controlType,
						bindId,
						bindElement,
						minOccurs,
						maxOccurs);
        Element controlWrapper = _wrapper.createControlsWrapper(formControl);
        contentWrapper.appendChild(controlWrapper);

        // if this is a repeatable then set ref to point to current element
        // not sure if this is a workaround or this is just the way XForms works...
        //
        if (!repeatSection.equals(formSection)) {
            formControl.setAttributeNS(XFORMS_NS,
				       getXFormsNSPrefix() + "ref",
				       ".");
        }

        Element hint = createHint(xForm, owner);

        if (hint != null) {
            formControl.appendChild(hint);
        }

        //add selector if repeat
        //if (repeatSection != formSection)
        //this.addSelector(xForm, (Element) formControl.getParentNode());
        //
        // TODO: Generate help message based on datatype and restrictions
        endFormControl(formControl, controlType, minOccurs, maxOccurs);
        endBindElement(bindElement);
    }

    private void addSimpleType(Document xForm,
                               Element modelSection,
                               Element formSection,
                               XSSimpleTypeDefinition controlType,
                               XSElementDeclaration owner,
                               String pathToRoot) {

        int[] occurance = this.getOccurance(owner);
        addSimpleType(xForm,
		      modelSection,
		      formSection,
		      controlType,
		      owner.getName(),
		      owner,
		      pathToRoot,
		      occurance[0],
		      occurance[1]);
    }

    private void addSimpleType(Document xForm,
                               Element modelSection,
                               Element formSection,
                               XSSimpleTypeDefinition controlType,
                               XSAttributeUse owningAttribute,
                               String pathToRoot) {

        addSimpleType(xForm,
		      modelSection,
		      formSection,
		      controlType,
		      owningAttribute.getAttrDeclaration().getName(),
		      owningAttribute,
		      pathToRoot,
		      owningAttribute.getRequired() ? 1 : 0,
		      1);
    }

    /**
     * add triggers to use the repeat elements (allow to add an element, ...)
     */
    private void addTriggersForRepeat(Document xForm,
                                      Element formSection,
                                      Element repeatSection,
                                      int minOccurs,
                                      int maxOccurs,
                                      String bindId) {
        ///////////// insert //////////////////
        //trigger insert
        Element trigger_insert =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "trigger");
        this.setXFormsId(trigger_insert);

        //label insert
        Element triggerLabel_insert =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "label");
        this.setXFormsId(triggerLabel_insert);
        trigger_insert.appendChild(triggerLabel_insert);
        triggerLabel_insert.setAttributeNS(SchemaFormBuilder.XLINK_NS,
                SchemaFormBuilder.xlinkNSPrefix + "href",
                "images/add_new.gif");

        Text label_insert = xForm.createTextNode("Insert after selected");
        triggerLabel_insert.appendChild(label_insert);

        //hint insert
        Element hint_insert =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "hint");
        this.setXFormsId(hint_insert);
        Text hint_insert_text =
                xForm.createTextNode("inserts a new entry in this collection");
        hint_insert.appendChild(hint_insert_text);
        trigger_insert.appendChild(hint_insert);

        //insert action
        Element action_insert =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "action");
        trigger_insert.appendChild(action_insert);
        this.setXFormsId(action_insert);

        Element insert =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "insert");
        action_insert.appendChild(insert);
        this.setXFormsId(insert);

        //insert: bind & other attributes
        if (bindId != null) {
            insert.setAttributeNS(SchemaFormBuilder.XFORMS_NS,
                    SchemaFormBuilder.xformsNSPrefix + "bind",
                    bindId);
        }

        insert.setAttributeNS(SchemaFormBuilder.XFORMS_NS,
                SchemaFormBuilder.xformsNSPrefix + "position",
                "after");

        //xforms:at = xforms:index from the "id" attribute on the repeat element
        String repeatId =
                repeatSection.getAttributeNS(SchemaFormBuilder.XFORMS_NS, "id");

        if (repeatId != null) {
            insert.setAttributeNS(SchemaFormBuilder.XFORMS_NS,
                    SchemaFormBuilder.xformsNSPrefix + "at",
                    SchemaFormBuilder.xformsNSPrefix + "index('" + repeatId + "')");
        }

        ///////////// delete //////////////////
        //trigger delete
        Element trigger_delete =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "trigger");
        this.setXFormsId(trigger_delete);

        //label delete
        Element triggerLabel_delete =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "label");
        this.setXFormsId(triggerLabel_delete);
        trigger_delete.appendChild(triggerLabel_delete);
        triggerLabel_delete.setAttributeNS(SchemaFormBuilder.XLINK_NS,
                SchemaFormBuilder.xlinkNSPrefix + "href",
                "images/delete.gif");

        Text label_delete = xForm.createTextNode("Delete selected");
        triggerLabel_delete.appendChild(label_delete);

        //hint delete
        Element hint_delete =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "hint");
        this.setXFormsId(hint_delete);
        Text hint_delete_text =
                xForm.createTextNode("deletes selected entry from this collection");
        hint_delete.appendChild(hint_delete_text);
        trigger_delete.appendChild(hint_delete);

        //delete action
        Element action_delete =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "action");
        trigger_delete.appendChild(action_delete);
        this.setXFormsId(action_delete);

        Element delete =
                xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
                        SchemaFormBuilder.xformsNSPrefix + "delete");
        action_delete.appendChild(delete);
        this.setXFormsId(delete);

        //delete: bind & other attributes
        if (bindId != null) {
            delete.setAttributeNS(SchemaFormBuilder.XFORMS_NS,
                    SchemaFormBuilder.xformsNSPrefix + "bind",
                    bindId);
        }

        //xforms:at = xforms:index from the "id" attribute on the repeat element
        if (repeatId != null) {
            delete.setAttributeNS(SchemaFormBuilder.XFORMS_NS,
                    SchemaFormBuilder.xformsNSPrefix + "at",
                    SchemaFormBuilder.xformsNSPrefix + "index('" + repeatId + "')");
        }

        //add the triggers
        Element wrapper_triggers =
                _wrapper.createControlsWrapper(trigger_insert);

        if (wrapper_triggers == trigger_insert) { //no wrapper
            formSection.appendChild(trigger_insert);
            formSection.appendChild(trigger_delete);
        } else {
            formSection.appendChild(wrapper_triggers);

            Element insert_parent = (Element) trigger_insert.getParentNode();

            if (insert_parent != null) {
                insert_parent.appendChild(trigger_delete);
            }
        }
    }

    private void buildTypeTree(XSTypeDefinition type, TreeSet descendents) {
        if (type != null) {

            if (descendents.size() > 0) {
                //TreeSet compatibleTypes = (TreeSet) typeTree.get(type.getName());
                TreeSet compatibleTypes = (TreeSet) typeTree.get(type.getName());

                if (compatibleTypes == null) {
                    //compatibleTypes = new TreeSet(descendents);
                    compatibleTypes = new TreeSet(this.typeExtensionSorter);
                    compatibleTypes.addAll(descendents);
                    //typeTree.put(type.getName(), compatibleTypes);
                    typeTree.put(type.getName(), compatibleTypes);
                } else {
                    compatibleTypes.addAll(descendents);
                }
            }

            XSTypeDefinition parentType = type.getBaseType();

            if (parentType != null
                    && type.getTypeCategory() == parentType.getTypeCategory()) {
                /*String typeName = type.getName();
                String parentTypeName = parentType.getName();
                if ((typeName == null && parentTypeName != null)
                    || (typeName != null && parentTypeName == null)
                    || (typeName != null
                        && parentTypeName != null
                        && !type.getName().equals(parentType.getName())
                        && !parentType.getName().equals("anyType"))) {*/
                if (type != parentType
                        && (parentType.getName() == null
                        || !parentType.getName().equals("anyType"))) {

		    //TreeSet newDescendents=new TreeSet(descendents);
                    TreeSet newDescendents = new TreeSet(this.typeExtensionSorter);
                    newDescendents.addAll(descendents);

//extension (we only add it to "newDescendants" because we don't want
//to have a type descendant to itself, but to consider it for the parent
                    if (type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                        XSComplexTypeDefinition complexType =
                                (XSComplexTypeDefinition) type;
                        if (complexType.getDerivationMethod()
                                == XSConstants.DERIVATION_EXTENSION
                                && !complexType.getAbstract()
                                && !descendents.contains(type) //to be tested
                        //&& !descendents.contains(type.getName()) //to be tested
                        ) {
//newDescendents.add(type.getName());
                            newDescendents.add(type);
                        }
                    }
//note: extensions are impossible on simpleTypes !

                    buildTypeTree(parentType, newDescendents);
                }
            }
        }
    }

    private void buildTypeTree(XSModel schema) {
        // build the type tree for complex types
        //
        XSNamedMap types = schema.getComponents(XSConstants.TYPE_DEFINITION);
        int nb = types.getLength();
        for (int i = 0; i < nb; i++) {
            XSTypeDefinition t = (XSTypeDefinition) types.item(i);
            if (t.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                XSComplexTypeDefinition type = (XSComplexTypeDefinition) t;
                buildTypeTree(type, new TreeSet(this.typeExtensionSorter));
            }
        }

        // build the type tree for simple types
        for (int i = 0; i < nb; i++) {
            XSTypeDefinition t = (XSTypeDefinition) types.item(i);
            if (t.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                XSSimpleTypeDefinition type = (XSSimpleTypeDefinition) t;
                buildTypeTree(type, new TreeSet(this.typeExtensionSorter));
            }
        }

        // print out type hierarchy for debugging purposes
        if (LOGGER.isDebugEnabled()) {
            Iterator keys = typeTree.keySet().iterator();
            while (keys.hasNext()) {
                String typeName = (String) keys.next();
                TreeSet descendents = (TreeSet) typeTree.get(typeName);
                LOGGER.debug(">>>> for " + typeName + " Descendants=\n ");
                Iterator it = descendents.iterator();
                while (it.hasNext()) {
                    XSTypeDefinition desc = (XSTypeDefinition) it.next();
                    LOGGER.debug("      " + desc.getName());
                }
            }
        }
    }

    private Element createFormControl(Document xForm,
                                      String caption,
                                      XSTypeDefinition controlType,
                                      String bindId,
                                      Element bindElement,
                                      int minOccurs,
                                      int maxOccurs) {
        // Select1 xform control to use:
        // Will use one of the following: input, textarea, selectOne, selectBoolean, selectMany, range
        // secret, output, button, do not apply
        //
        // select1: enumeration or keyref constrained value
        // select: list
        // range: union (? not sure about this)
        // textarea : ???
        // input: default
        //
        Element formControl = null;

        if (controlType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            XSSimpleTypeDefinition simpleType =
                    (XSSimpleTypeDefinition) controlType;
            if (simpleType.getItemType() != null) //list
            {
                formControl =
                        createControlForListType(xForm,
                                simpleType,
                                caption,
                                bindElement);
            } else { //other simple type
                // need to check constraints to determine which form control to use
                //
                // use the selectOne control
                //
                //XSObjectList enumerationFacets = simpleType.getFacets(XSSimpleTypeDefinition.FACET_ENUMERATION);
                //if(enumerationFacets.getLength()>0){
                if (simpleType
                        .isDefinedFacet(XSSimpleTypeDefinition.FACET_ENUMERATION)) {
                    formControl =
                            createControlForEnumerationType(xForm,
                                    simpleType,
                                    caption,
                                    bindElement);
                }
                /*if (enumerationFacets.hasMoreElements()) {
                formControl = createControlForEnumerationType(xForm, (SimpleType)controlType, caption,
                                                      bindElement);
                } */
            }
        } else if (
                controlType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE
                && controlType.getName().equals("anyType")) {
            formControl = createControlForAnyType(xForm, caption, controlType);
        }

        if (formControl == null)
            formControl = createControlForAtomicType(xForm,
						     caption,
						     (XSSimpleTypeDefinition)controlType);

        startFormControl(formControl, controlType);
        formControl.setAttributeNS(XFORMS_NS, getXFormsNSPrefix() + "bind", bindId);

        //put the label before
        // no -> put in the "createControlFor..." methods

        /*Element captionElement=xForm.createElementNS(XFORMS_NS,getXFormsNSPrefix() + "label");
           this.setXFormsId(captionElement);
           captionElement.appendChild(xForm.createTextNode(caption));
           if(formControl.hasChildNodes())
           {
           Node first=formControl.getFirstChild();
           captionElement = (Element) formControl.insertBefore(captionElement, first);
           }
           else
           captionElement = (Element) formControl.appendChild(captionElement);        */

        // TODO: Enhance alert statement based on facet restrictions.
        // TODO: Enhance to support minOccurs > 1 and maxOccurs > 1.
        // TODO: Add i18n/l10n suppport to this - use java MessageFormatter...
        //
        //       e.g. Please provide a valid value for 'Address'. 'Address' is a mandatory decimal field.
        //
        Element alertElement =
                (Element) formControl.appendChild(xForm.createElementNS(XFORMS_NS,
                        getXFormsNSPrefix() + "alert"));
        this.setXFormsId(alertElement);

        StringBuffer alert =
                new StringBuffer("Please provide a valid value for '" + caption + "'.");

        Element enveloppe = xForm.getDocumentElement();

        if (minOccurs != 0) {
            alert.append(" '"
                    + caption
                    + "' is a required '"
                    + createCaption(this.getXFormsTypeName(enveloppe, controlType))
                    + "' value.");
        } else {
            alert.append(" '"
                    + caption
                    + "' is an optional '"
                    + createCaption(this.getXFormsTypeName(enveloppe, controlType))
                    + "' value.");
        }

        alertElement.appendChild(xForm.createTextNode(alert.toString()));

        return formControl;
    }

    /**
     * used to get the type name that will be used in the XForms document
     *
     * @param context     the element which will serve as context for namespaces
     * @param controlType the type from which we want the name
     * @return the complete type name (with namespace prefix) of the type in the XForms doc
     */
    protected String getXFormsTypeName(Element context, XSTypeDefinition controlType) {
        String result = null;

        String typeName = controlType.getName();
        String typeNS = controlType.getNamespace();

        //if we use XMLSchema types:
        //first check if it is a simple type named in the XMLSchema
        if (_useSchemaTypes &&
	    controlType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE &&
	    typeName != null && typeName.length() != 0 && 
	    schema.getTypeDefinition(typeName, typeNS) != null)
        { 
	    //type is globally defined
	    //use schema type

            //local type name
            String localTypeName = typeName;
            int index = typeName.indexOf(":");
            if (index > -1 && typeName.length() > index)
                localTypeName = typeName.substring(index + 1);

            //namespace prefix in this document
            String prefix = NamespaceCtx.getPrefix(context, typeNS);

            //completeTypeName = new prefix + local name
            result = localTypeName;
            if (prefix != null && prefix.length() != 0)
                result = prefix + ":" + localTypeName;

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("getXFormsTypeName: typeName=" + typeName + ", typeNS=" + typeNS + ", result=" + result);
        } 
	else
	{
	    //use built in type
            result = this.getDataTypeName(getBuiltInType(controlType));
        }
        return result;
    }

    private Document createFormTemplate(String formId)
            throws IOException, ParserConfigurationException {
        return createFormTemplate(formId,
				  "Form " + formId,
				  getProperty(CSS_STYLE_PROP, DEFAULT_CSS_STYLE_PROP));
    }

    private Document createFormTemplate(String formId,
                                        String formName,
                                        String stylesheet)
	throws ParserConfigurationException {
        Document xForm = documentBuilder.newDocument();

        Element envelopeElement = _wrapper.createEnvelope(xForm);

        // set required namespace attributes
        envelopeElement.setAttributeNS(XMLNS_NAMESPACE_URI,
				       "xmlns:" + getChibaNSPrefix().substring(0, getChibaNSPrefix().length() - 1),
				       CHIBA_NS);
        envelopeElement.setAttributeNS(XMLNS_NAMESPACE_URI,
				       "xmlns:" + getXFormsNSPrefix().substring(0, getXFormsNSPrefix().length() - 1),
				       XFORMS_NS);
        envelopeElement.setAttributeNS(XMLNS_NAMESPACE_URI,
				       "xmlns:" + getXLinkNSPrefix().substring(0, getXLinkNSPrefix().length() - 1),
				       XLINK_NS);
	//XMLEvent
        envelopeElement.setAttributeNS(XMLNS_NAMESPACE_URI,
				       "xmlns:" + xmleventsNSPrefix.substring(0, xmleventsNSPrefix.length() - 1), 
				       XMLEVENTS_NS);
	//XML Schema Instance
        envelopeElement.setAttributeNS(XMLNS_NAMESPACE_URI,
				       "xmlns:" + xmlSchemaInstancePrefix.substring(0, xmlSchemaInstancePrefix.length() - 1),
				       XMLSCHEMA_INSTANCE_NAMESPACE_URI);
	//base
        if (_base != null && _base.length() != 0)
            envelopeElement.setAttributeNS(XML_NAMESPACE_URI, "xml:base", _base);

        //model element
        Element modelElement = xForm.createElementNS(XFORMS_NS, 
						     getXFormsNSPrefix() + "model");
        this.setXFormsId(modelElement);
        Element modelWrapper = _wrapper.createModelWrapper(modelElement);
        envelopeElement.appendChild(modelWrapper);

        //form control wrapper -> created by wrapper
        //Element formWrapper = xForm.createElement("body");
        //envelopeElement.appendChild(formWrapper);
        Element formWrapper = _wrapper.createFormWrapper(envelopeElement);

        return xForm;
    }

    private Element createGroup(Document xForm,
                                Element modelSection,
                                Element formSection,
                                XSElementDeclaration owner) {
        // add a group node and recurse
        //
        Element groupElement =
                xForm.createElementNS(XFORMS_NS, getXFormsNSPrefix() + "group");
        groupElement = startFormGroup(groupElement, owner);

        if (groupElement == null)
	    groupElement = modelSection;
	else
	{
            this.setXFormsId(groupElement);

            Element controlsWrapper =
                    _wrapper.createControlsWrapper(groupElement);

            //groupElement = (Element) formSection.appendChild(groupElement);
            formSection.appendChild(controlsWrapper);

            Element captionElement =
                    (Element) groupElement.appendChild(xForm.createElementNS(XFORMS_NS,
                            getXFormsNSPrefix() + "label"));
            this.setXFormsId(captionElement);
            captionElement.appendChild(xForm.createTextNode(createCaption(owner)));
        }
        return groupElement;
    }

    /**
     * Get a fully qualified name for this element, and eventually declares a new prefix for the namespace if
     * it was not declared before
     *
     * @param element
     * @param xForm
     * @return
     */
    private String getElementName(XSElementDeclaration element, Document xForm) {
        String elementName = element.getName();
        String namespace = element.getNamespace();
        if (namespace != null && namespace.length() != 0) 
	{
            String prefix;
            if ((prefix = (String) namespacePrefixes.get(namespace)) == null) {
                String basePrefix = (namespace.substring(namespace.lastIndexOf('/', namespace.length()-2)+1));
                int i=1;
                prefix = basePrefix;
                while (namespacePrefixes.containsValue(prefix)) {
                    prefix = basePrefix + (i++);
                }
                namespacePrefixes.put(namespace, prefix);
                Element envelope = xForm.getDocumentElement();
                envelope.setAttributeNS(XMLNS_NAMESPACE_URI, "xmlns:"+prefix, namespace);
            }
            elementName = prefix + ":" + elementName;
        }
        return elementName;
    }
}
