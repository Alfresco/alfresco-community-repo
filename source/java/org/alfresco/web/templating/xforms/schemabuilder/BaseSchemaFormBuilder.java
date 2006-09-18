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

import org.apache.xerces.xs.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.Vector;

/*
 * Search for TODO for things remaining to-do in this implementation.
 *
 * TODO: i18n/l10n of messages, hints, captions. Possibly leverage org.chiba.i18n classes.
 * TODO: When Chiba supports itemset, use schema keyref and key constraints for validation.
 * TODO: Add support for default and fixed values.
 * TODO: Add support for use=prohibited.
 */

/**
 * A concrete base implementation of the SchemaFormBuilder interface allowing
 * an XForm to be automatically generated for an XML Schema definition.
 *
 * @author Brian Dueck
 * @version $Id: BaseSchemaFormBuilder.java,v 1.19 2005/03/29 14:24:34 unl Exp $
 */
public class BaseSchemaFormBuilder
    extends AbstractSchemaFormBuilder
    implements SchemaFormBuilder {

    /**
     * Creates a new BaseSchemaFormBuilder object.
     *
     * @param rootTagName    __UNDOCUMENTED__
     * @param instanceSource __UNDOCUMENTED__
     * @param action         __UNDOCUMENTED__
     * @param submitMethod   __UNDOCUMENTED__
     * @param wrapper        __UNDOCUMENTED__
     * @param stylesheet     __UNDOCUMENTED__
     */
    public BaseSchemaFormBuilder(String rootTagName,
                                 Document instanceDocument,
                                 String action,
                                 String submitMethod,
                                 WrapperElementsBuilder wrapper,
                                 String stylesheet,
                                 String base,
                                 boolean userSchemaTypes) {
        super(rootTagName,
	      instanceDocument,
	      action,
	      submitMethod,
	      wrapper,
	      stylesheet,
	      base,
	      userSchemaTypes);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param text __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(String text) {
        // if the word is all upper case, then set to lower case and continue
        if (text.equals(text.toUpperCase()))
            text = text.toLowerCase();
	String[] s = text.split("[-_\\ ]");
	StringBuffer result = new StringBuffer();
	for (int i = 0; i < s.length; i++)
        {
	    if (i != 0)
		result.append(' ');
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
     * __UNDOCUMENTED__
     *
     * @param attribute __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSAttributeDeclaration attribute) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(attribute.getName());
    }

    public String createCaption(XSAttributeUse attribute) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(attribute.getAttrDeclaration().getName());
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param element __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSElementDeclaration element) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        return createCaption(element.getName());
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param element __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public String createCaption(XSObject element) {
        // TODO: Improve i18n/l10n of caption - may have to use
        //       a custom <appinfo> element in the XML Schema to do this.
        //
        if (element instanceof XSElementDeclaration) {
            return createCaption(((XSElementDeclaration) element).getName());
        } else if (element instanceof XSAttributeDeclaration) {
            return createCaption(((XSAttributeDeclaration) element).getName());
        } else if (element instanceof XSAttributeUse) {
            return createCaption(((XSAttributeUse) element).getAttrDeclaration().getName());
        } else
            LOGGER.warn("WARNING: createCaption: element is not an attribute nor an element: "
                    + element.getClass().getName());

        return null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForAnyType(Document xForm,
                                           String caption,
                                           XSTypeDefinition controlType) {
        Element control = xForm.createElementNS(SchemaFormBuilder.XFORMS_NS, 
						SchemaFormBuilder.XFORMS_NS_PREFIX + "textarea");
        this.setXFormsId(control);
        control.setAttributeNS(SchemaFormBuilder.CHIBA_NS, 
			       SchemaFormBuilder.CHIBA_NS_PREFIX + "height", 
			       "3");

        //label
        Element captionElement = (Element)
	    control.appendChild(xForm.createElementNS(SchemaFormBuilder.XFORMS_NS,
						      SchemaFormBuilder.XFORMS_NS_PREFIX + "label"));
        this.setXFormsId(captionElement);
        captionElement.appendChild(xForm.createTextNode(caption));

        return control;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForAtomicType(Document xForm,
                                              String caption,
                                              XSSimpleTypeDefinition controlType) {
        Element control;

        //remove while select1 do not work correctly in repeats
        if ("boolean".equals(controlType.getName()))
	{
            control = xForm.createElementNS(XFORMS_NS,
					    SchemaFormBuilder.XFORMS_NS_PREFIX + "select1");
            control.setAttributeNS(XFORMS_NS,
				   SchemaFormBuilder.XFORMS_NS_PREFIX + "appearance",
				   "full");
            this.setXFormsId(control);

            Element item_true =
                    xForm.createElementNS(XFORMS_NS, SchemaFormBuilder.XFORMS_NS_PREFIX + "item");
            this.setXFormsId(item_true);
            Element label_true =
                    xForm.createElementNS(XFORMS_NS, SchemaFormBuilder.XFORMS_NS_PREFIX + "label");
            this.setXFormsId(label_true);
            Text label_true_text = xForm.createTextNode("true");
            label_true.appendChild(label_true_text);
            item_true.appendChild(label_true);

            Element value_true =
                    xForm.createElementNS(XFORMS_NS, SchemaFormBuilder.XFORMS_NS_PREFIX + "value");
            this.setXFormsId(value_true);
            Text value_true_text = xForm.createTextNode("true");
            value_true.appendChild(value_true_text);
            item_true.appendChild(value_true);
            control.appendChild(item_true);

            Element item_false =
                    xForm.createElementNS(XFORMS_NS, SchemaFormBuilder.XFORMS_NS_PREFIX + "item");
            this.setXFormsId(item_false);
            Element label_false =
                    xForm.createElementNS(XFORMS_NS, SchemaFormBuilder.XFORMS_NS_PREFIX + "label");
            this.setXFormsId(label_false);
            Text label_false_text = xForm.createTextNode("false");
            label_false.appendChild(label_false_text);
            item_false.appendChild(label_false);

            Element value_false =
                    xForm.createElementNS(XFORMS_NS, SchemaFormBuilder.XFORMS_NS_PREFIX + "value");
            this.setXFormsId(value_false);
            Text value_false_text = xForm.createTextNode("false");
            value_false.appendChild(value_false_text);
            item_false.appendChild(value_false);
            control.appendChild(item_false);
        } 
	else 
	{
            control = xForm.createElementNS(XFORMS_NS, SchemaFormBuilder.XFORMS_NS_PREFIX + "input");
            this.setXFormsId(control);
        }

        //label
        Element captionElement = (Element)
	    control.appendChild(xForm.createElementNS(XFORMS_NS,
						      SchemaFormBuilder.XFORMS_NS_PREFIX + "label"));
        this.setXFormsId(captionElement);
        captionElement.appendChild(xForm.createTextNode(caption));

        return control;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param bindElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForEnumerationType(Document xForm,
                                                   XSSimpleTypeDefinition controlType,
                                                   String caption,
                                                   Element bindElement) {
        // TODO: Figure out an intelligent or user determined way to decide between
        // selectUI style (listbox, menu, combobox, radio) (radio and listbox best apply)
        // Possibly look for special appInfo section in the schema and if not present default to comboBox...
        //
        // For now, use radio if enumValues < DEFAULT_LONG_LIST_MAX_SIZE otherwise use combobox
        //
        StringList enumFacets = controlType.getLexicalEnumeration();
        int nbFacets = enumFacets.getLength();
        if (nbFacets <= 0)
	    return null;
        Vector enumValues = new Vector();

        Element control = xForm.createElementNS(XFORMS_NS,
						    SchemaFormBuilder.XFORMS_NS_PREFIX + "select1");
        this.setXFormsId(control);

        //label
        Element captionElement1 = (Element)
		control.appendChild(xForm.createElementNS(XFORMS_NS,
							  SchemaFormBuilder.XFORMS_NS_PREFIX + "label"));
        this.setXFormsId(captionElement1);
        captionElement1.appendChild(xForm.createTextNode(caption));

        Element choices = xForm.createElementNS(XFORMS_NS,
						    SchemaFormBuilder.XFORMS_NS_PREFIX + "choices");
        this.setXFormsId(choices);

        for (int i = 0; i < nbFacets; i++) {
            String facet = enumFacets.item(i);
            enumValues.add(facet);
        }

        if (nbFacets < Long.parseLong(getProperty(SELECTONE_LONG_LIST_SIZE_PROP))) {
            control.setAttributeNS(XFORMS_NS,
				       SchemaFormBuilder.XFORMS_NS_PREFIX + "appearance",
				       getProperty(SELECTONE_UI_CONTROL_SHORT_PROP));
        } else {
            control.setAttributeNS(XFORMS_NS,
				       SchemaFormBuilder.XFORMS_NS_PREFIX + "appearance",
				       getProperty(SELECTONE_UI_CONTROL_LONG_PROP));

            // add the "Please select..." instruction item for the combobox
            // and set the isValid attribute on the bind element to check for the "Please select..."
            // item to indicate that is not a valid value
            //
            {
                String pleaseSelect = "[Select1 " + caption + "]";
                Element item = xForm.createElementNS(XFORMS_NS,
							 SchemaFormBuilder.XFORMS_NS_PREFIX + "item");
                this.setXFormsId(item);
                choices.appendChild(item);

                Element captionElement = 
			xForm.createElementNS(XFORMS_NS,
					      SchemaFormBuilder.XFORMS_NS_PREFIX + "label");
                this.setXFormsId(captionElement);
                item.appendChild(captionElement);
                captionElement.appendChild(xForm.createTextNode(pleaseSelect));

                Element value =
                        xForm.createElementNS(XFORMS_NS,
						  SchemaFormBuilder.XFORMS_NS_PREFIX + "value");
                this.setXFormsId(value);
                item.appendChild(value);
                value.appendChild(xForm.createTextNode(pleaseSelect));

                // not(purchaseOrder/state = '[Choose State]')
                //String isValidExpr = "not(" + bindElement.getAttributeNS(XFORMS_NS,"nodeset") + " = '" + pleaseSelect + "')";
                // ->no, not(. = '[Choose State]')
                String isValidExpr = "not( . = '" + pleaseSelect + "')";

                //check if there was a constraint
                String constraint = bindElement.getAttributeNS(XFORMS_NS, "constraint");

                constraint = (constraint != null && constraint.length() != 0
			      ? constraint + " and " + isValidExpr
			      : isValidExpr);

                bindElement.setAttributeNS(XFORMS_NS,
					   SchemaFormBuilder.XFORMS_NS_PREFIX + "constraint",
					   constraint);
            }
        }

        control.appendChild(choices);

        addChoicesForSelectControl(xForm, choices, enumValues);

        return control;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm       __UNDOCUMENTED__
     * @param listType    __UNDOCUMENTED__
     * @param caption     __UNDOCUMENTED__
     * @param bindElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createControlForListType(Document xForm,
                                            XSSimpleTypeDefinition listType,
                                            String caption,
                                            Element bindElement) {
        XSSimpleTypeDefinition controlType = listType.getItemType();

        StringList enumFacets = controlType.getLexicalEnumeration();
        int nbFacets = enumFacets.getLength();
        if (nbFacets <= 0) 
	    return null;
        Element control = xForm.createElementNS(XFORMS_NS,
						SchemaFormBuilder.XFORMS_NS_PREFIX + "select");
        this.setXFormsId(control);

        //label
        Element captionElement = (Element)
	    control.appendChild(xForm.createElementNS(XFORMS_NS,
						      SchemaFormBuilder.XFORMS_NS_PREFIX + "label"));
        this.setXFormsId(captionElement);
        captionElement.appendChild(xForm.createTextNode(caption));

        Vector enumValues = new Vector();
        for (int i = 0; i < nbFacets; i++) {
            enumValues.add(enumFacets.item(i));
        }

        // TODO: Figure out an intelligent or user determined way to decide between
        // selectUI style (listbox, menu, combobox, radio) (radio and listbox best apply)
        // Possibly look for special appInfo section in the schema and if not present default to checkBox...
        //
        // For now, use checkbox if there are < DEFAULT_LONG_LIST_MAX_SIZE items, otherwise use long control
        //
        if (enumValues.size() < Long.parseLong(getProperty(SELECTMANY_LONG_LIST_SIZE_PROP))) 
	{
            control.setAttributeNS(XFORMS_NS,
				   SchemaFormBuilder.XFORMS_NS_PREFIX + "appearance",
				   getProperty(SELECTMANY_UI_CONTROL_SHORT_PROP));
        } 
	else 
	    {
            control.setAttributeNS(XFORMS_NS,
				   SchemaFormBuilder.XFORMS_NS_PREFIX + "appearance",
				   getProperty(SELECTMANY_UI_CONTROL_LONG_PROP));
        }

        Element choices =
                xForm.createElementNS(XFORMS_NS,
				      SchemaFormBuilder.XFORMS_NS_PREFIX + "choices");
        this.setXFormsId(choices);
        control.appendChild(choices);

        this.addChoicesForSelectControl(xForm, choices, enumValues);

        return control;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param xForm __UNDOCUMENTED__
     * @param node  __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element createHint(Document xForm, XSObject node) 
    {
        XSAnnotation annotation = null;
        if (node instanceof XSElementDeclaration)
            annotation = ((XSElementDeclaration) node).getAnnotation();
        else if (node instanceof XSAttributeDeclaration)
            annotation = ((XSAttributeDeclaration) node).getAnnotation();
        else if (node instanceof XSAttributeUse)
            annotation =
                    ((XSAttributeUse) node).getAttrDeclaration().getAnnotation();

        return (annotation != null
		? addHintFromDocumentation(xForm, annotation)
		: null);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param bindElement __UNDOCUMENTED__
     */
    public void endBindElement(Element bindElement) 
    {
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param controlElement __UNDOCUMENTED__
     * @param controlType    __UNDOCUMENTED__
     */
    public void endFormControl(final Element controlElement,
                               final XSTypeDefinition controlType,
                               final Occurs o) 
    {
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param groupElement __UNDOCUMENTED__
     */
    public void endFormGroup(final Element groupElement,
                             final XSTypeDefinition controlType,
                             final Occurs o,
                             final Element modelSection) 
    {
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param bindElement __UNDOCUMENTED__
     * @param controlType __UNDOCUMENTED__
     * @param minOccurs   __UNDOCUMENTED__
     * @param maxOccurs   __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startBindElement(Element bindElement,
                                    XSTypeDefinition controlType,
                                    final Occurs o)
    {
        // START WORKAROUND
        // Due to a Chiba bug, anyType is not a recognized type name.
        // so, if this is an anyType, then we'll just skip the type
        // setting.
        //
        // type.getName() may be 'null' for anonymous types, so compare against
        // static string (see bug #1172541 on sf.net)
        if (!"anyType".equals(controlType.getName())) 
	{
            Element enveloppe = bindElement.getOwnerDocument().getDocumentElement();
            String typeName = this.getXFormsTypeName(enveloppe, controlType);
            if (typeName != null && typeName.length() != 0)
                bindElement.setAttributeNS(XFORMS_NS,
					   SchemaFormBuilder.XFORMS_NS_PREFIX + "type",
					   typeName);
        }

	bindElement.setAttributeNS(XFORMS_NS,
				   SchemaFormBuilder.XFORMS_NS_PREFIX + "required",
				   o.minimum == 0 ? "false()" : "true()");
	

        //no more minOccurs & maxOccurs element: add a constraint if maxOccurs>1:
        //count(.) <= maxOccurs && count(.) >= minOccurs
        String minConstraint = null;
        String maxConstraint = null;

        if (o.minimum > 1) 
            //if 0 or 1 -> no constraint (managed by "required")
            minConstraint = "count(.) >= " + o.minimum;

        if (o.maximum > 1) 
            //if 1 or unbounded -> no constraint
            maxConstraint = "count(.) <= " + o.maximum;

        final String constraint = (minConstraint != null && maxConstraint != null
				   ? minConstraint + " and " + maxConstraint
				   : (minConstraint != null
				      ? minConstraint
				      : maxConstraint));
        if (constraint != null && constraint.length() != 0)
            bindElement.setAttributeNS(XFORMS_NS,
				       SchemaFormBuilder.XFORMS_NS_PREFIX + "constraint",
				       constraint);
        return bindElement;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param controlElement __UNDOCUMENTED__
     * @param controlType    __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startFormControl(Element controlElement,
                                    XSTypeDefinition controlType) 
    {
        return controlElement;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param groupElement  __UNDOCUMENTED__
     * @param schemaElement __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public Element startFormGroup(Element groupElement,
                                  XSElementDeclaration schemaElement) 
    {
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "box-align",getProperty(GROUP_BOX_ALIGN_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "box-orient",getProperty(GROUP_BOX_ORIENT_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "caption-width",getProperty(GROUP_CAPTION_WIDTH_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "width",getProperty(GROUP_WIDTH_PROP));
        //groupElement.setAttributeNS(CHIBA_NS,getChibaNSPrefix() + "border",getProperty(GROUP_BORDER_PROP));
        return groupElement;
    }
}
