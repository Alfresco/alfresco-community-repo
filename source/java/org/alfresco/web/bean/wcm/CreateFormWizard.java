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
package org.alfresco.web.bean.wcm;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.templating.xforms.*;
import org.alfresco.web.templating.TemplatingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xs.*;
import org.w3c.dom.Document;

/**
 * Bean implementation for the "Create XML Form" dialog
 * 
 * @author arielb
 */
public class CreateFormWizard extends BaseWizardBean
{

   /////////////////////////////////////////////////////////////////////////////

   /**
    * Simple wrapper class to represent a template output method
    */
   public class TemplateOutputMethodData
   {
      private final String fileName;
      private final File file;
      private final String fileExtension;
      private final Class templateOutputMethodType;

      public TemplateOutputMethodData(final String fileName, 
                                      final File file,
                                      final String fileExtension,
                                      final Class templateOutputMethodType)
      {
         this.fileName = fileName;
         this.file = file;
         this.fileExtension = fileExtension;
         this.templateOutputMethodType = templateOutputMethodType;
      }
      
      public String getFileExtension()
      {
         return this.fileExtension;
      }
      
      public String getFileName()
      {
         return this.fileName;
      }

      public File getFile()
      {
         return this.file;
      }

      public Class getTemplateOutputMethodType()
      {
         return this.templateOutputMethodType;
      }
      
      public String getTemplateOutputMethodTypeName()
      {
         return CreateFormWizard.this.getTemplateOutputMethodTypeName(this.getTemplateOutputMethodType());
      }
   }

   /////////////////////////////////////////////////////////////////////////////
   
   private static final String FILE_TEMPLATEOUTPUT = "template-output-method";

   private static final String FILE_SCHEMA = "schema";

   private final static Log LOGGER = 
      LogFactory.getLog(CreateFormWizard.class);
   
   private String schemaRootTagName;
   private String templateName;
   private Class templateOutputMethodType = null;
   protected ContentService contentService;
   private DataModel templateOutputMethodsDataModel;
   private List<TemplateOutputMethodData> templateOutputMethods = null;
   private String fileExtension = null;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
      throws Exception
   {
      final TemplatingService ts = TemplatingService.getInstance();
      // get the node ref of the node that will contain the content
      final NodeRef contentFormsNodeRef = ts.getContentFormsNodeRef();

      final FileInfo folderInfo = 
         this.fileFolderService.create(contentFormsNodeRef,
                                       this.getTemplateName(),
                                       ContentModel.TYPE_FOLDER);
      FileInfo fileInfo = 
         this.fileFolderService.create(folderInfo.getNodeRef(),
                                       this.getSchemaFileName(),
                                       ContentModel.TYPE_CONTENT);
      final NodeRef schemaNodeRef = fileInfo.getNodeRef();
      
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("Created file node for file: " + 
                      this.getSchemaFileName());

      // get a writer for the content and put the file
      ContentWriter writer = this.contentService.getWriter(schemaNodeRef, 
                                                           ContentModel.PROP_CONTENT,
                                                           true);
      // set the mimetype and encoding
      writer.setMimetype("text/xml");
      writer.setEncoding("UTF-8");
      writer.putContent(this.getSchemaFile());

      // apply the titled aspect - title and description
      Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
      props.put(ContentModel.PROP_TITLE, this.getTemplateName());
      props.put(ContentModel.PROP_DESCRIPTION, "");
      this.nodeService.addAspect(schemaNodeRef, ContentModel.ASPECT_TITLED, props);

      props = new HashMap<QName, Serializable>(1, 1.0f);
      props.put(WCMModel.PROP_SCHEMA_ROOT_TAG_NAME, this.getSchemaRootTagName());
      this.nodeService.addAspect(schemaNodeRef, WCMModel.ASPECT_TEMPLATE, props);
         
      for (TemplateOutputMethodData tomd : this.templateOutputMethods)
      {
         fileInfo = this.fileFolderService.create(folderInfo.getNodeRef(),
                                                  tomd.getFileName(),
                                                  ContentModel.TYPE_CONTENT);
         final NodeRef templateOutputMethodNodeRef = fileInfo.getNodeRef();
      
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("Created file node for file: " + tomd.getFileName());
      
         // get a writer for the content and put the file
         writer = this.contentService.getWriter(templateOutputMethodNodeRef, 
                                                ContentModel.PROP_CONTENT, 
                                                true);
         // set the mimetype and encoding
         writer.setMimetype("text/xml");
         writer.setEncoding("UTF-8");
         writer.putContent(tomd.getFile());

         this.nodeService.createAssociation(schemaNodeRef,
                                            templateOutputMethodNodeRef,
                                            WCMModel.ASSOC_TEMPLATE_OUTPUT_METHODS);                         
      
         props = new HashMap<QName, Serializable>(3, 1.0f);
         props.put(WCMModel.PROP_TEMPLATE_OUTPUT_METHOD_TYPE, tomd.getTemplateOutputMethodType().getName());
         props.put(WCMModel.PROP_TEMPLATE_SOURCE, schemaNodeRef);
         props.put(WCMModel.PROP_TEMPLATE_OUTPUT_METHOD_DERIVED_FILE_EXTENSION, tomd.getFileExtension());
         this.nodeService.addAspect(templateOutputMethodNodeRef, WCMModel.ASPECT_TEMPLATE_OUTPUT_METHOD, props);
      }
      // return the default outcome
      return outcome;
   }

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.removeUploadedSchemaFile();
      this.removeUploadedTemplateOutputMethodFile();
      this.schemaRootTagName = null;
      this.templateName = null;
      this.templateOutputMethodType = null;
      this.templateOutputMethods = new ArrayList<TemplateOutputMethodData>();
      this.fileExtension = null;
   }
   
   @Override
   public String cancel()
   {
      this.removeUploadedSchemaFile();
      this.removeUploadedTemplateOutputMethodFile();
      return super.cancel();
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      // TODO: Allow the next button state to be configured so that
      //       wizard implementations don't have to worry about 
      //       checking step numbers
      
      boolean disabled = false;
      int step = Application.getWizardManager().getCurrentStep();
      switch(step)
      {
         case 1:
         {
            disabled = (this.getSchemaFileName() == null || 
                        this.getSchemaFileName().length() == 0);
            break;
         }
      }
      
      return disabled;
   }
   
   /**
    * @return true if the Add To List button on the Template Output Methods should be disabled
    */
   public boolean getAddToListDisabled()
   {
      return (getTemplateOutputMethodFileName() == null || 
              fileExtension == null || 
              fileExtension.length() == 0);
   }

   /**
    * @return Returns the fileExtension.
    */
   public String getFileExtension()
   {
      return this.fileExtension;
   }

   /**
    * @param fileExtension The fileExtension to set.
    */
   public void setFileExtension(String fileExtension)
   {
      this.fileExtension = fileExtension;
   }

   /**
    * Add the selected template output method to the list
    */
   public void addSelectedTemplateOutputMethod(ActionEvent event)
   {
      for (TemplateOutputMethodData tomd : this.templateOutputMethods)
      {
         if (tomd.getFileExtension().equals(this.fileExtension))
         {
            throw new AlfrescoRuntimeException("template output method with extension " + this.fileExtension +
                                               " already exists");
         }
      }

      final TemplateOutputMethodData data = 
         this.new TemplateOutputMethodData(this.getTemplateOutputMethodFileName(),
                                           this.getTemplateOutputMethodFile(),
                                           this.fileExtension,
                                           this.templateOutputMethodType);
      this.templateOutputMethods.add(data);
      this.removeUploadedTemplateOutputMethodFile();
      this.templateOutputMethodType = null;
      this.fileExtension = null;
   }
   
   /**
    * Action handler called when the Remove button is pressed to remove a 
    * template output method
    */
   public void removeSelectedTemplateOutputMethod(ActionEvent event)
   {
      final TemplateOutputMethodData wrapper = (TemplateOutputMethodData)
         this.templateOutputMethodsDataModel.getRowData();
      if (wrapper != null)
      {
         this.templateOutputMethods.remove(wrapper);
      }
   }
   
   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedSchemaFile()
   {
      clearUpload(FILE_SCHEMA);
      
      // refresh the current page
      return null;
   }
   
   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedTemplateOutputMethodFile()
   {
      clearUpload(FILE_TEMPLATEOUTPUT);
      
      // refresh the current page
      return null;
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * Returns the properties for current configured output methods JSF DataModel
    * 
    * @return JSF DataModel representing the current configured output methods
    */
   public DataModel getTemplateOutputMethodsDataModel()
   {
      if (this.templateOutputMethodsDataModel == null)
      {
         this.templateOutputMethodsDataModel = new ListDataModel();
      }
      
      this.templateOutputMethodsDataModel.setWrappedData(this.templateOutputMethods);
      
      return this.templateOutputMethodsDataModel;
   }
   
   /**
    * @return Returns the mime type currenty selected
    */
   public String getTemplateOutputMethodType()
   {
      if (this.templateOutputMethodType == null &&
          this.getTemplateOutputMethodFileName() != null)
      {
         this.templateOutputMethodType =
            (this.getTemplateOutputMethodFileName().endsWith(".xsl")
             ? XSLTOutputMethod.class
             : (this.getTemplateOutputMethodFileName().endsWith(".ftl")
                ? FreeMarkerOutputMethod.class
                : null));
      }
      return (this.templateOutputMethodType == null
              ? null
              : this.templateOutputMethodType.getName());
   }
   
   /**
    * @param templateOutputMethodType Sets the currently selected mime type
    */
   public void setTemplateOutputMethodType(final String templateOutputMethodType)
      throws ClassNotFoundException
   {
      this.templateOutputMethodType = (templateOutputMethodType == null
                                       ? null
                                       : Class.forName(templateOutputMethodType));
   }
   
   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getTemplateOutputMethodTypeChoices()
   {
      return (List<SelectItem>)Arrays.asList(new SelectItem[] 
      {
         new SelectItem(FreeMarkerOutputMethod.class.getName(), 
                        getTemplateOutputMethodTypeName(FreeMarkerOutputMethod.class)),
         new SelectItem(XSLTOutputMethod.class.getName(), 
                        getTemplateOutputMethodTypeName(XSLTOutputMethod.class))
      });
   }

   private String getTemplateOutputMethodTypeName(Class type)
   {
      return (FreeMarkerOutputMethod.class.equals(type)
              ? "FreeMarker"
              : (XSLTOutputMethod.class.equals(type)
                 ? "XSLT"
                 : null));
   }
   
   private FileUploadBean getFileUploadBean(final String id)
   {
      final FacesContext ctx = FacesContext.getCurrentInstance();
      final Map sessionMap = ctx.getExternalContext().getSessionMap();
      return (FileUploadBean)sessionMap.get(FileUploadBean.getKey(id));
   }
   
   /**
    * @return Returns the name of the file
    */
   private String getFileName(final String id)
   {
      // try and retrieve the file and filename from the file upload bean
      // representing the file we previously uploaded.
      final FileUploadBean fileBean = this.getFileUploadBean(id);
      return fileBean == null ? null : fileBean.getFileName();
   }
   
   /**
    * @return Returns the schema file or <tt>null</tt>
    */
   private File getFile(final String id)
   {
      // try and retrieve the file and filename from the file upload bean
      // representing the file we previously uploaded.
      final FileUploadBean fileBean = this.getFileUploadBean(id);
      return fileBean != null ? fileBean.getFile() : null;
   }
   
   /**
    * @return Returns the schema file or <tt>null</tt>
    */
   public File getSchemaFile()
   {
      return this.getFile(FILE_SCHEMA);
   }
   
   /**
    * @return Returns the schema file or <tt>null</tt>
    */
   public String getSchemaFileName()
   {
      // try and retrieve the file and filename from the file upload bean
      // representing the file we previously uploaded.
      return this.getFileName(FILE_SCHEMA);
   }
   
   /**
    * @return Returns the schema file or <tt>null</tt>
    */
   public String getTemplateOutputMethodFileName()
   {
      return this.getFileName(FILE_TEMPLATEOUTPUT);
   }
   
   /**
    * @return Returns the presentationTemplate file or <tt>null</tt>
    */
   public File getTemplateOutputMethodFile()
   {
      return this.getFile(FILE_TEMPLATEOUTPUT);
   }

   /**
    * Sets the root tag name to use when processing the schema.
    */
   public void setSchemaRootTagName(final String schemaRootTagName)
   {
      this.schemaRootTagName = schemaRootTagName;
   }

   /**
    * Returns the root tag name to use when processing the schema.
    */
   public String getSchemaRootTagName()
   {
      return this.schemaRootTagName;
   }
   
   /**
    * @return the possible root tag names for use with the schema based on 
    * the element declarations it defines.
    */
   public List<SelectItem> getSchemaRootTagNameChoices()
   {
      final List<SelectItem> result = new LinkedList<SelectItem>();
      if (this.getSchemaFile() != null)
      {
         try
         {
            final TemplatingService ts = TemplatingService.getInstance();
            final Document d = ts.parseXML(this.getSchemaFile());
            final XSModel xsm = SchemaFormBuilder.loadSchema(d);
            final XSNamedMap elementsMap = xsm.getComponents(XSConstants.ELEMENT_DECLARATION);
            for (int i = 0; i < elementsMap.getLength(); i++)
            {
               final XSElementDeclaration e = (XSElementDeclaration)elementsMap.item(i);
               result.add(new SelectItem(e.getName(), e.getName()));
            }
         }
         catch (Exception e)
         {
            final String msg = "unable to parse " + this.getSchemaFileName();
            this.removeUploadedSchemaFile();
            throw new AlfrescoRuntimeException(msg, e);
         }
      }
      return result;
   }
   
   /**
    * Sets the human friendly name for this template.
    */
   public void setTemplateName(final String templateName)
   {
      this.templateName = templateName;
   }

   /**
    * @return the human friendly name for this template.
    */
   public String getTemplateName()
   {
      return (this.templateName == null && this.getSchemaFileName() != null
              ? this.getSchemaFileName().replaceAll("(.+)\\..*", "$1")
              : this.templateName);
   }
   
   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      final String[] labels = new String[2 + this.templateOutputMethods.size()];
      final String[] values = new String[2 + this.templateOutputMethods.size()];
      labels[0] = "Schema File";
      values[0] = this.getSchemaFileName();
      labels[1] = "Template output method type";
      values[1] = this.getTemplateOutputMethodType();
      for (int i = 0; i < this.templateOutputMethods.size(); i++)
      {
         final TemplateOutputMethodData tomd = this.templateOutputMethods.get(i);
         labels[2 + i] = "Template output method for " + tomd.getFileExtension();
         values[2 + i] = tomd.getFileName();
      }

      return this.buildSummary(labels, values);
   }
   
   
   // ------------------------------------------------------------------------------
   // Service Injection
   
   /**
    * @param contentService The contentService to set.
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper Methods
   
   /**
    * Clear the uploaded form, clearing the specific Upload component by Id
    */
   protected void clearUpload(final String id)
   {
      // remove the file upload bean from the session
      FacesContext ctx = FacesContext.getCurrentInstance();
      FileUploadBean fileBean =
         (FileUploadBean)ctx.getExternalContext().getSessionMap().get(FileUploadBean.getKey(id));
      if (fileBean != null)
      {
         fileBean.setFile(null);
         fileBean.setFileName(null);
      }
   }
}
