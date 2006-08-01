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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Basic implementation of WrapperElementsBuilder, with no additional design
 *
 * @author - Sophie Ramel
 */
public class BaseWrapperElementsBuilder implements WrapperElementsBuilder {
    /**
     * Creates a new instance of BaseWrapperElementsBuilder
     */
    public BaseWrapperElementsBuilder() {
    }

    /**
     * create the wrapper element of the different controls
     *
     * @param controlElement the control element (input, select, repeat, group, ...)
     * @return the wrapper element, already containing the control element
     */
    public Element createControlsWrapper(Element controlElement) {
        return controlElement;
    }

    /**
     * creates the global enveloppe of the resulting document, and puts it in the document
     *
     * @return the enveloppe
     */
    public Element createEnvelope(Document xForm) {
        Element envelopeElement = xForm.createElement("envelope");

        //Element envelopeElement = xForm.createElementNS(CHIBA_NS, this.getChibaNSPrefix()+"envelope");
        xForm.appendChild(envelopeElement);

        return envelopeElement;
    }

    /**
     * create the wrapper element of the form
     *
     * @param enveloppeElement the form element (chiba:form or other)
     * @return the wrapper element
     */

    public Element createFormWrapper(Element enveloppeElement) {
        //add a "body" element without NS
        Document doc = enveloppeElement.getOwnerDocument();
        Element body = doc.createElement("body");
        //body.appendChild(formElement);
        enveloppeElement.appendChild(body);
        return body;
    }

    /**
     * create the element that will contain the content of the group (or repeat) element
     *
     * @param groupElement the group or repeat element
     * @return the wrapper element, already containing the content of the group element
     */
    public Element createGroupContentWrapper(Element groupElement) {
        return groupElement;
    }

    /**
     * create the wrapper element of the xforms:model element
     *
     * @param modelElement the xforms:model element
     * @return the wrapper element, already containing the model
     */
    public Element createModelWrapper(Element modelElement) {
        return modelElement;
    }
}
