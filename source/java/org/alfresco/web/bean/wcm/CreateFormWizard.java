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
import org.alfresco.web.forms.*;
import org.alfresco.web.forms.xforms.SchemaFormBuilder;
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
    * Simple wrapper class to represent a form data renderer
    */
   public class RenderingEngineData
   {
      private final String fileName;
      private final File file;
      private final String fileExtension;
      private final Class renderingEngineType;

      public RenderingEngineData(final String fileName, 
                                      final File file,
                                      final String fileExtension,
                                      final Class renderingEngineType)
      {
         this.fileName = fileName;
         this.file = file;
         this.fileExtension = fileExtension;
         this.renderingEngineType = renderingEngineType;
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

      public Class getRenderingEngineType()
      {
         return this.renderingEngineType;
      }
      
      public String getRenderingEngineTypeName()
      {
         return CreateFormWizard.this.getRenderingEngineTypeName(this.getRenderingEngineType());
      }
   }

   /////////////////////////////////////////////////////////////////////////////
   
   public static final String FILE_RENDERING_ENGINE = "rendering-engine";

   public static final String FILE_SCHEMA = "schema";

   private final static Log LOGGER = LogFactory.getLog(CreateFormWizard.class);
   
   private String schemaRootElementName;
   private String formName;
   private String formDescription;
   private Class renderingEngineType = null;
   protected ContentService contentService;
   private DataModel renderingEnginesDataModel;
   private List<RenderingEngineData> renderingEngines = null;
   private String fileExtension = null;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
      throws Exception
   {
      final FormsService ts = FormsService.getInstance();
      // get the node ref of the node that will contain the content
      final NodeRef contentFormsNodeRef = ts.getContentFormsNodeRef();

      final FileInfo folderInfo = 
         this.fileFolderService.create(contentFormsNodeRef,
                                       this.getFormName(),
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
      props.put(ContentModel.PROP_TITLE, this.getFormName());
      props.put(ContentModel.PROP_DESCRIPTION, this.getFormDescription());
      this.nodeService.addAspect(schemaNodeRef, ContentModel.ASPECT_TITLED, props);

      props = new HashMap<QName, Serializable>(1, 1.0f);
      props.put(WCMModel.PROP_SCHEMA_ROOT_ELEMENT_NAME, this.getSchemaRootElementName());
      this.nodeService.addAspect(schemaNodeRef, WCMModel.ASPECT_FORM, props);
         
      for (RenderingEngineData tomd : this.renderingEngines)
      {
         fileInfo = this.fileFolderService.create(folderInfo.getNodeRef(),
                                                  tomd.getFileName(),
                                                  ContentModel.TYPE_CONTENT);
         final NodeRef renderingEngineNodeRef = fileInfo.getNodeRef();
      
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("Created file node for file: " + tomd.getFileName());
      
         // get a writer for the content and put the file
         writer = this.contentService.getWriter(renderingEngineNodeRef, 
                                                ContentModel.PROP_CONTENT, 
                                                true);
         // set the mimetype and encoding
         writer.setMimetype("text/xml");
         writer.setEncoding("UTF-8");
         writer.putContent(tomd.getFile());

         this.nodeService.createAssociation(schemaNodeRef,
                                            renderingEngineNodeRef,
                                            WCMModel.ASSOC_RENDERING_ENGINES);
      
         props = new HashMap<QName, Serializable>(3, 1.0f);
         props.put(WCMModel.PROP_RENDERING_ENGINE_TYPE, 
                   tomd.getRenderingEngineType().getName());
         props.put(WCMModel.PROP_FORM_SOURCE, schemaNodeRef);
         props.put(WCMModel.PROP_FILE_EXTENSION_FOR_RENDITION, 
                   tomd.getFileExtension());
         this.nodeService.addAspect(renderingEngineNodeRef, 
                                    WCMModel.ASPECT_RENDERING_ENGINE, 
                                    props);
      }
      // return the default outcome
      return outcome;
   }

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.removeUploadedSchemaFile();
      this.removeUploadedRenderingEngineFile();
      this.schemaRootElementName = null;
      this.formName = null;
      this.formDescription = null;
      this.renderingEngineType = null;
      this.renderingEngines = new ArrayList<RenderingEngineData>();
      this.fileExtension = null;
   }
   
   @Override
   public String cancel()
   {
      this.removeUploadedSchemaFile();
      this.removeUploadedRenderingEngineFile();
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
    * @return true if the Add To List button on the configure rendering engines 
    * page should be disabled
    */
   public boolean getAddToListDisabled()
   {
      return getRenderingEngineFileName() == null;
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
    * Add the selected rendering engine to the list
    */
   public void addSelectedRenderingEngine(ActionEvent event)
   {
      for (RenderingEngineData tomd : this.renderingEngines)
      {
         if (tomd.getFileExtension().equals(this.fileExtension))
         {
            throw new AlfrescoRuntimeException("rendering engine with extension " + this.fileExtension +
                                               " already exists");
         }
      }

      final RenderingEngineData data = 
         this.new RenderingEngineData(this.getRenderingEngineFileName(),
                                           this.getRenderingEngineFile(),
                                           this.fileExtension,
                                           this.renderingEngineType);
      this.renderingEngines.add(data);
      this.removeUploadedRenderingEngineFile();
      this.renderingEngineType = null;
      this.fileExtension = null;
   }
   
   /**
    * Action handler called when the Remove button is pressed to remove a 
    * rendering engine
    */
   public void removeSelectedRenderingEngine(ActionEvent event)
   {
      final RenderingEngineData wrapper = (RenderingEngineData)
         this.renderingEnginesDataModel.getRowData();
      if (wrapper != null)
      {
         this.renderingEngines.remove(wrapper);
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
   public String removeUploadedRenderingEngineFile()
   {
      clearUpload(FILE_RENDERING_ENGINE);
      
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
   public DataModel getRenderingEnginesDataModel()
   {
      if (this.renderingEnginesDataModel == null)
      {
         this.renderingEnginesDataModel = new ListDataModel();
      }
      
      this.renderingEnginesDataModel.setWrappedData(this.renderingEngines);
      
      return this.renderingEnginesDataModel;
   }
   
   /**
    * @return Returns the mime type currenty selected
    */
   public String getRenderingEngineType()
   {
      if (this.renderingEngineType == null &&
          this.getRenderingEngineFileName() != null)
      {
         this.renderingEngineType =
            (this.getRenderingEngineFileName().endsWith(".xsl")
             ? XSLTRenderingEngine.class
             : (this.getRenderingEngineFileName().endsWith(".ftl")
                ? FreeMarkerRenderingEngine.class
                : null));
      }
      return (this.renderingEngineType == null
              ? null
              : this.renderingEngineType.getName());
   }
   
   /**
    * @param renderingEngineType Sets the currently selected mime type
    */
   public void setRenderingEngineType(final String renderingEngineType)
      throws ClassNotFoundException
   {
      this.renderingEngineType = (renderingEngineType == null
                                       ? null
                                       : Class.forName(renderingEngineType));
   }
   
   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getRenderingEngineTypeChoices()
   {
      return (List<SelectItem>)Arrays.asList(new SelectItem[] 
      {
         new SelectItem(FreeMarkerRenderingEngine.class.getName(), 
                        getRenderingEngineTypeName(FreeMarkerRenderingEngine.class)),
         new SelectItem(XSLTRenderingEngine.class.getName(), 
                        getRenderingEngineTypeName(XSLTRenderingEngine.class))
      });
   }

   private String getRenderingEngineTypeName(Class type)
   {
      return (FreeMarkerRenderingEngine.class.equals(type)
              ? "FreeMarker"
              : (XSLTRenderingEngine.class.equals(type)
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
   public String getRenderingEngineFileName()
   {
      return this.getFileName(FILE_RENDERING_ENGINE);
   }
   
   /**
    * @return Returns the rendering engine file or <tt>null</tt>
    */
   public File getRenderingEngineFile()
   {
      return this.getFile(FILE_RENDERING_ENGINE);
   }

   /**
    * Sets the root element name to use when processing the schema.
    */
   public void setSchemaRootElementName(final String schemaRootElementName)
   {
      this.schemaRootElementName = schemaRootElementName;
   }

   /**
    * Returns the root element name to use when processing the schema.
    */
   public String getSchemaRootElementName()
   {
      return this.schemaRootElementName;
   }
   
   /**
    * @return the possible root element names for use with the schema based on 
    * the element declarations it defines.
    */
   public List<SelectItem> getSchemaRootElementNameChoices()
   {
      final List<SelectItem> result = new LinkedList<SelectItem>();
      if (this.getSchemaFile() != null)
      {
         try
         {
            final FormsService ts = FormsService.getInstance();
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
    * Sets the human friendly name for this form.
    */
   public void setFormName(final String formName)
   {
      this.formName = formName;
   }

   /**
    * @return the human friendly name for this form.
    */
   public String getFormName()
   {
      return (this.formName == null && this.getSchemaFileName() != null
              ? this.getSchemaFileName().replaceAll("(.+)\\..*", "$1")
              : this.formName);
   }

   /**
    * Sets the description for this form.
    */
   public void setFormDescription(final String formDescription)
   {
      this.formDescription = formDescription;
   }

   /**
    * @return the description for this form.
    */
   public String getFormDescription()
   {
      return this.formDescription;
   }
   
   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      final String[] labels = new String[1 + this.renderingEngines.size()];
      final String[] values = new String[1 + this.renderingEngines.size()];
      labels[0] = "Schema File";
      values[0] = this.getSchemaFileName();
      for (int i = 0; i < this.renderingEngines.size(); i++)
      {
         final RenderingEngineData tomd = this.renderingEngines.get(i);
         labels[1 + i] = "Form Data Renderer for " + tomd.getFileExtension();
         values[1 + i] = tomd.getFileName();
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
