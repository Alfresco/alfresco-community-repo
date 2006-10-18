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
package org.alfresco.web.templating;

import org.alfresco.service.cmr.repository.NodeRef;
import org.w3c.dom.Document;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

/**
 * Encapsulation of a template type.
 */
public interface TemplateType
    extends Serializable
{

    /** the name of the template, which must be unique within the TemplatingService */
    public String getName();

    /** the root tag to use within the schema */
    public String getRootTagName();

    /** the xml schema for this template type */
    public Document getSchema();

   public NodeRef getNodeRef();

    //XXXarielb not used currently and not sure if it's necessary...
    //    public void addInputMethod(final TemplateInputMethod in);

    /**
     * Provides a set of input methods for this template.
     */
    public List<TemplateInputMethod> getInputMethods();

    /**
     * adds an output method to this template type.
     */
    public void addOutputMethod(TemplateOutputMethod output);

    /**
     * Provides the set of output methods for this template.
     */
    public List<TemplateOutputMethod> getOutputMethods();
}
