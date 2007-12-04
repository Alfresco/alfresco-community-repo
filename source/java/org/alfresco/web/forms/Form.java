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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.forms;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.namespace.QName;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Encapsulation of a form.
 *
 * @author Ariel Backenroth
 */
public interface Form
   extends Serializable
{

   /** the name of the form, which must be unique within the FormsService */
   public String getName();

   /** the title of the form */
   public String getTitle();

   /** the description of the form */
   public String getDescription();

   /** the root tag to use within the schema */
   public String getSchemaRootElementName();
   
   /** the output path pattern for form instance data */
   public String getOutputPathPattern();

   /** 
    * @return the default workflow associated with this form or <tt>null</tt> 
    * if none is configured. 
    */
   public WorkflowDefinition getDefaultWorkflow();

   /**
    * @return the parameters for the default workflow or <tt>null</tt>
    * if none were configured.
    */
   public Map<QName, Serializable> getDefaultWorkflowParameters();

   /** the xml schema for this template type */
   public Document getSchema()
      throws IOException, SAXException;

   /** 
    * provides the output path for the form instance data based on the 
    * configured output path pattern.
    *
    * @param formInstanceData the parsed xml content
    * @param formInstanceDataFileName the file name provided by the user.
    * @param parentAVMPath the parent avm path
    * @param webappName the current webapp name
    *
    * @return the path to use for writing the form instance data.
    */
   public String getOutputPathForFormInstanceData(final Document formInstanceData,
                                                  final String formInstanceDataFileName,
                                                  final String parentAVMPath,
                                                  final String webappName);

   //XXXarielb not used currently and not sure if it's necessary...
   //    public void addInputMethod(final TemplateInputMethod in);

   /**
    * Provides a set of input methods for this template.
    */
   public List<FormProcessor> getFormProcessors();

   /**
    * adds an output method to this template type.
    */
   public void addRenderingEngineTemplate(RenderingEngineTemplate output);

   /**
    * Provides the set of output methods for this template.
    */
   public List<RenderingEngineTemplate> getRenderingEngineTemplates();

   /**
    * Provides the rendering engine template by name.
    *
    * @param name the name of the rendering engine template.
    * @return the rendering engine template or <tt>null</tt> if not found.
    */
   public RenderingEngineTemplate getRenderingEngineTemplate(final String name);
   
   /**
    * @return true if WCM Form, false if ECM form
    */
   public boolean isWebForm();
}
