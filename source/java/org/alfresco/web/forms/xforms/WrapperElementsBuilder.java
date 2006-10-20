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
package org.alfresco.web.forms.xforms;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This interface provides methods to create the "wrappers" elements that will contain the XForms document.
 * These elements can be:
 * - the first "enveloppe" element
 * - other elements specific to a destination language or platform (ex: XHTML tags)
 *
 * @author Sophie Ramel
 */
public interface WrapperElementsBuilder 
{

    /**
     * create the wrapper element of the form (exemple_ "body" for HTML)
     *
     * @param enveloppeElement the containing enveloppe
     * @return the wrapper element, already added in the enveloppe
     */
    public Element createFormWrapper(Element enveloppeElement);

    /**
     * create the wrapper element of the different controls
     *
     * @param controlElement the control element (input, select, repeat, group, ...)
     * @return the wrapper element, already containing the control element
     */
    public Element createControlsWrapper(Element controlElement);

    /**
     * creates the global enveloppe of the resulting document, and puts it in the document
     *
     * @return the enveloppe
     */
    public Element createEnvelope(Document xForm);

    /**
     * create the element that will contain the content of the group (or repeat) element
     *
     * @param groupElement the group or repeat element
     * @return - the wrapper element, already containing the content of the group element
     */
    public Element createGroupContentWrapper(Element groupElement);

    /**
     * create the wrapper element of the xforms:model element
     *
     * @param modelElement the xforms:model element
     * @return - the wrapper element, already containing the model
     */
    public Element createModelWrapper(Element modelElement);
}
