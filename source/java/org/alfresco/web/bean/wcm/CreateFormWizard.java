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
package org.alfresco.web.bean.wcm;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngine;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.forms.RenderingEngineTemplateImpl;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.forms.xforms.FormBuilderException;
import org.alfresco.web.forms.xforms.SchemaUtil;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.wcm.WebResources;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.w3c.dom.Document;

/**
 * Bean implementation for the "Create XML Form" dialog
 * 
 * @author arielb
 */
public class CreateFormWizard 
   extends BaseWizardBean
{

   /////////////////////////////////////////////////////////////////////////////

   /**
    * Simple wrapper class to represent a form data renderer
    */
   public class RenderingEngineTemplateData
      implements Serializable
   {
      private final NodeRef nodeRef;
      private final File file;
      private final String name;
      private final String title;
      private final String description;
      private final String mimetypeForRendition;
      private final String outputPathPatternForRendition;
      private final RenderingEngine renderingEngine;

      public RenderingEngineTemplateData(final RenderingEngineTemplate ret)
      {
         this.file = null;
         this.nodeRef = ((RenderingEngineTemplateImpl)ret).getNodeRef();
         this.name = ret.getName();
         this.title = ret.getTitle();
         this.description = ret.getDescription();
         this.outputPathPatternForRendition = ret.getOutputPathPattern();
         this.mimetypeForRendition = ret.getMimetypeForRendition();
         this.renderingEngine = ret.getRenderingEngine();
      }

      public RenderingEngineTemplateData(final File file,
                                         final String name,
                                         final String title,
                                         final String description,
                                         final String outputPathPatternForRendition,
                                         final String mimetypeForRendition,
                                         final RenderingEngine renderingEngine)
      {
         this.nodeRef = null;
         this.file = file;
         this.name = name;
         this.title = title;
         this.description = description;
         this.outputPathPatternForRendition = outputPathPatternForRendition;
         this.mimetypeForRendition = mimetypeForRendition;
         this.renderingEngine = renderingEngine;
      }
      
      public String getOutputPathPatternForRendition()
      {
         return this.outputPathPatternForRendition;
      }

      public String getMimetypeForRendition()
      {
         return this.mimetypeForRendition;
      }
      
      public File getFile()
      {
         return this.file;
      }
      
      public NodeRef getNodeRef()
      {
         return this.nodeRef;
      }

      public String getName()
      {
         return this.name;
      }

      public String getTitle()
      {
         return this.title;
      }

      public String getDescription()
      {
         return this.description;
      }

      public RenderingEngine getRenderingEngine()
      {
         return this.renderingEngine;
      }

      public String toString()
      {
         return (this.getClass().getName() + "{" +
                 "name: " + this.getName() + "," +
                 "mimetypeForRendition: " + this.getMimetypeForRendition() + "," +
                 "outputPathPatternForRendition: " + this.getOutputPathPatternForRendition() + "," +
                 "renderingEngine: " + this.getRenderingEngine().getName() + 
                 "}");
      }
   }

   /////////////////////////////////////////////////////////////////////////////
   
   public static final String FILE_RENDERING_ENGINE_TEMPLATE = "rendering-engine-template";

   public static final String FILE_SCHEMA = "schema";

   private final static String DEFAULT_EXTENSION_PATTERN = "${extension}";
   private final static String DEFAULT_NAME_PATTERN = "${name}";

   private final static Log LOGGER = LogFactory.getLog(CreateFormWizard.class);
   
   protected String defaultWorkflowName = null;
   protected boolean applyDefaultWorkflow = true;
   protected List<RenderingEngineTemplateData> renderingEngineTemplates = null;
   protected transient XSModel schema;
   protected String schemaFileName;
   protected ContentService contentService;
   protected MimetypeService mimetypeService;
   protected WorkflowService workflowService;

   private String schemaRootElementName = null;
   private String formName = null;
   private String formTitle = null;
   private String formDescription = null;
   private String outputPathPatternForFormInstanceData = null;
   private String renderingEngineTemplateFileName = null;
   private String renderingEngineTemplateName = null;
   private String renderingEngineTemplateTitle = null;
   private String renderingEngineTemplateDescription = null;

   private RenderingEngine renderingEngine = null;
   protected transient DataModel renderingEngineTemplatesDataModel;

   private String outputPathPatternForRendition = null;
   private String mimetypeForRendition = null;
   private transient List<SelectItem> mimetypeChoices = null;
   private transient List<SelectItem> schemaRootElementNameChoices = null;
   private transient List<UIListItem> defaultWorkflowChoices = null;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   protected String finishImpl(final FacesContext context, final String outcome)
      throws Exception
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("creating form " + this.getFormName());

      // get the node ref of the node that will contain the content
      final NodeRef contentFormsNodeRef = FormsService.getInstance().getContentFormsNodeRef();

      final FileInfo folderInfo = 
         this.fileFolderService.create(contentFormsNodeRef,
                                       this.getFormName(),
                                       WCMAppModel.TYPE_FORMFOLDER);
      
      final FileInfo fileInfo = 
         this.fileFolderService.create(folderInfo.getNodeRef(),
                                       this.getSchemaFileName(),
                                       ContentModel.TYPE_CONTENT);
      
      // get a writer for the content and put the file
      final ContentWriter writer = this.contentService.getWriter(fileInfo.getNodeRef(),
                                                                 ContentModel.PROP_CONTENT,
                                                                 true);
      // set the mimetype and encoding
      writer.setMimetype(MimetypeMap.MIMETYPE_XML);
      writer.setEncoding("UTF-8");
      writer.putContent(this.getSchemaFile());

      // apply the titled aspect - title and description
      final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
      props.put(ContentModel.PROP_TITLE, this.getFormTitle());
      props.put(ContentModel.PROP_DESCRIPTION, this.getFormDescription());
      this.nodeService.addAspect(folderInfo.getNodeRef(), ContentModel.ASPECT_TITLED, props);
      
      props.clear();
      props.put(WCMAppModel.PROP_XML_SCHEMA, fileInfo.getNodeRef());
      props.put(WCMAppModel.PROP_XML_SCHEMA_ROOT_ELEMENT_NAME, 
                this.getSchemaRootElementName());
      this.nodeService.addAspect(folderInfo.getNodeRef(), WCMAppModel.ASPECT_FORM, props);
      if (this.applyDefaultWorkflow)
      {
         props.clear();
         props.put(WCMAppModel.PROP_WORKFLOW_NAME, this.getDefaultWorkflowName()[0]);
         this.nodeService.createNode(folderInfo.getNodeRef(),
                                     WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                     WCMAppModel.ASSOC_FORM_WORKFLOW_DEFAULTS,
                                     WCMAppModel.TYPE_WORKFLOW_DEFAULTS,
                                     props);
      }

      props.clear();
      props.put(WCMAppModel.PROP_OUTPUT_PATH_PATTERN, 
                this.getOutputPathPatternForFormInstanceData());
      this.nodeService.addAspect(folderInfo.getNodeRef(),
                                 WCMAppModel.ASPECT_OUTPUT_PATH_PATTERN, props);
      for (RenderingEngineTemplateData retd : this.renderingEngineTemplates)
      {
         this.saveRenderingEngineTemplate(retd, folderInfo.getNodeRef());
      }
      // return the default outcome
      return outcome;
   }

   protected void saveRenderingEngineTemplate(final RenderingEngineTemplateData retd,
                                              final NodeRef formNodeRef)
   {
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("adding rendering engine template " + retd + 
                      " to form " + this.getFormName());
      
      NodeRef renderingEngineTemplateNodeRef = 
         this.fileFolderService.searchSimple(formNodeRef, retd.getName());
      final HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
      if (renderingEngineTemplateNodeRef == null)
      {
         try
         {
            final FileInfo fileInfo = this.fileFolderService.create(formNodeRef,
                                                                    retd.getName(),
                                                                    ContentModel.TYPE_CONTENT);
            if (LOGGER.isDebugEnabled())
               LOGGER.debug("Created file node for file: " + retd.getName());
            renderingEngineTemplateNodeRef = fileInfo.getNodeRef();            
         }
         catch (final FileExistsException fee)
         {
            LOGGER.error(fee.getName() + " already exists in " + 
                         fee.getParentNodeRef());
            throw fee;
         }

         // get a writer for the content and put the file
         final ContentWriter writer = this.contentService.getWriter(renderingEngineTemplateNodeRef, 
                                                                    ContentModel.PROP_CONTENT, 
                                                                    true);
         // set the mimetype and encoding
         // XXXarielb mime type of template isn't known
         // writer.setMimetype("text/xml");
         writer.setEncoding("UTF-8");
         writer.putContent(retd.getFile());

         this.nodeService.createAssociation(formNodeRef,
                                            renderingEngineTemplateNodeRef,
                                            WCMAppModel.ASSOC_RENDERING_ENGINE_TEMPLATES);
         props.clear();
         props.put(WCMAppModel.PROP_PARENT_RENDERING_ENGINE_NAME, 
                   retd.getRenderingEngine().getName());
         props.put(WCMAppModel.PROP_FORM_SOURCE, formNodeRef);
         this.nodeService.addAspect(renderingEngineTemplateNodeRef, 
                                    WCMAppModel.ASPECT_RENDERING_ENGINE_TEMPLATE, 
                                    props);

         // apply the titled aspect - title and description
         props.clear();
         props.put(ContentModel.PROP_TITLE, retd.getTitle());
         props.put(ContentModel.PROP_DESCRIPTION, retd.getDescription());
         this.nodeService.addAspect(renderingEngineTemplateNodeRef, 
                                    ContentModel.ASPECT_TITLED, 
                                    props);
      }

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("adding rendition properties to " + renderingEngineTemplateNodeRef);
      props.clear();
      props.put(WCMAppModel.PROP_MIMETYPE_FOR_RENDITION, 
                retd.getMimetypeForRendition());

      final NodeRef rpNodeRef = this.nodeService.createNode(renderingEngineTemplateNodeRef,
                                                            WCMAppModel.ASSOC_RENDITION_PROPERTIES,
                                                            WCMAppModel.ASSOC_RENDITION_PROPERTIES,
                                                            WCMAppModel.TYPE_RENDITION_PROPERTIES,
                                                            props).getChildRef();
      props.clear();
      props.put(WCMAppModel.PROP_OUTPUT_PATH_PATTERN, 
                retd.getOutputPathPatternForRendition());
      this.nodeService.addAspect(rpNodeRef, WCMAppModel.ASPECT_OUTPUT_PATH_PATTERN, props);
   }

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.removeUploadedSchemaFile();
      this.removeUploadedRenderingEngineTemplateFile();
      this.schema = null;
      this.schemaFileName = null;
      this.schemaRootElementName = null;
      this.schemaRootElementNameChoices = null;
      this.formName = null;
      this.formTitle = null;
      this.formDescription = null;
      this.renderingEngineTemplateFileName = null;
      this.renderingEngineTemplateName = null;
      this.renderingEngineTemplateTitle = null;
      this.renderingEngineTemplateDescription = null; 
      this.renderingEngine = null;
      this.renderingEngineTemplates = new ArrayList<RenderingEngineTemplateData>();
      this.outputPathPatternForFormInstanceData = null;
      this.outputPathPatternForRendition = null;
      this.mimetypeForRendition = null;
      this.defaultWorkflowName = null;
      this.defaultWorkflowChoices = null;
      this.applyDefaultWorkflow = true;
   }
   
   @Override
   public String cancel()
   {
      this.removeUploadedSchemaFile();
      this.removeUploadedRenderingEngineTemplateFile();
      return super.cancel();
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      // TODO: Allow the next button state to be configured so that
      //       wizard implementations don't have to worry about 
      //       checking step numbers
      
      final int step = Application.getWizardManager().getCurrentStep();
      switch(step)
      {
      case 1:
      {
         return (this.getSchemaFileName() == null || 
                 this.getSchemaFileName().length() == 0);
      }
      default:
      {
         return false;
      }
      }
   }

   @Override
   public String getStepDescription()
   {
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      final String stepName = Application.getWizardManager().getCurrentStepName();
      if ("configure_rendering_engine_templates".equals(stepName))
      {
         return MessageFormat.format(bundle.getString("create_form_configure_rendering_engine_templates_desc"), 
                                     this.getFormName());
      }
      else if ("select_default_workflow".equals(stepName))
      {

         return MessageFormat.format(bundle.getString("create_form_select_default_workflow_desc"), 
                                     this.getFormName());
      }
      else
      {
         return super.getContainerDescription();
      }
   }
   
   /**
    * @return true if the Add To List button on the configure rendering engines 
    * page should be disabled
    */
   public boolean getAddToListDisabled()
   {
      return this.getRenderingEngineTemplateFileName() == null;
   }

   /**
    * @return Returns the output path for the rendition.
    */
   public String getOutputPathPatternForRendition()
   {
      return (this.outputPathPatternForRendition == null
              ? DEFAULT_NAME_PATTERN + '.' + DEFAULT_EXTENSION_PATTERN
              : this.outputPathPatternForRendition);
   }

   /**
    * @param outputPathPatternForRendition The output path for the rendition.
    */
   public void setOutputPathPatternForRendition(final String outputPathPatternForRendition)
   {
      this.outputPathPatternForRendition = outputPathPatternForRendition;
   }

   /**
    * @return Returns the mimetype.
    */
   public String getMimetypeForRendition()
   {
      String result = null;
      if (this.mimetypeForRendition != null)
      {
         result = this.mimetypeForRendition;
      }
      else
      {
         if (this.outputPathPatternForRendition != null && 
             !this.outputPathPatternForRendition.endsWith(DEFAULT_EXTENSION_PATTERN))
         {
            result = this.mimetypeService.guessMimetype(this.outputPathPatternForRendition);
         }
         if (result == null)
         {
            result = MimetypeMap.MIMETYPE_HTML;
         }
      }
      return result;
   }

   /**
    * @param mimetypeForRendition The mimetype to set.
    */
   public void setMimetypeForRendition(final String mimetypeForRendition)
   {
      this.mimetypeForRendition = mimetypeForRendition;
   }

   /**
    * Add the selected rendering engine to the list
    */
   public void addSelectedRenderingEngineTemplate(final ActionEvent event)
   {
      final String name = this.getRenderingEngineTemplateName();
      if (name == null || name.length() == 0)
      {
         Utils.addErrorMessage("Please provide a name for the rendering engine template");
         return;
      }
      final String opp = this.getOutputPathPatternForRendition();
      final String mimetype = this.getMimetypeForRendition();
      for (RenderingEngineTemplateData retd : this.renderingEngineTemplates)
      {
         if (name.equals(retd.getName()))
         {
            Utils.addErrorMessage("A rendering engine template with the name " + name +
                                  " already exists");
            return;
         }
         if (opp.equals(retd.getOutputPathPatternForRendition()) &&
             opp.indexOf(DEFAULT_EXTENSION_PATTERN) >= 0 &&
             mimetype.equals(retd.getMimetypeForRendition()))
         {
            Utils.addErrorMessage("A rendering engine template with the output path pattern " + opp +
                                  " and mimetype " + mimetype + " already exists");
            return;
         }
      }
      final RenderingEngineTemplateData data = 
         this.new RenderingEngineTemplateData(this.getRenderingEngineTemplateFile(),
                                              this.getRenderingEngineTemplateName(),
                                              this.getRenderingEngineTemplateTitle(),
                                              this.getRenderingEngineTemplateDescription(),
                                              opp,
                                              mimetype,
                                              this.renderingEngine);
      this.renderingEngineTemplates.add(data);
      this.removeUploadedRenderingEngineTemplateFile();
      this.renderingEngine = null;
      this.outputPathPatternForRendition = null;
      this.mimetypeForRendition = null;
      this.renderingEngineTemplateFileName = null;
      this.renderingEngineTemplateName = null;
      this.renderingEngineTemplateTitle = null;
      this.renderingEngineTemplateDescription = null;
   }
   
   /**
    * Action handler called when the Remove button is pressed to remove a 
    * rendering engine
    */
   public void removeSelectedRenderingEngineTemplate(final ActionEvent event)
   {
      final RenderingEngineTemplateData wrapper = (RenderingEngineTemplateData)
         this.renderingEngineTemplatesDataModel.getRowData();
      if (wrapper != null)
      {
         this.renderingEngineTemplates.remove(wrapper);
      }
   }

   /**
    * Action handler called when the user changes the selected mimetype
    */
   public String mimetypeForRenditionChanged(final ValueChangeEvent vce)
   {
      // refresh the current page
      return null;
   }
    
   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedSchemaFile()
   {
      this.clearUpload(FILE_SCHEMA);
      this.schemaRootElementNameChoices = null;
      this.schema = null;
      this.schemaFileName = null;
      
      // refresh the current page
      return null;
   }
   
   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedRenderingEngineTemplateFile()
   {
      clearUpload(FILE_RENDERING_ENGINE_TEMPLATE);
      
      // refresh the current page
      return null;
   }
   
   /**
    * Action handler called when the schema has been uploaded.
    */
   public String schemaFileValueChanged(final ValueChangeEvent vce)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("schemaFileValueChanged(" + this.getFileName(FILE_SCHEMA) + 
                      "[" + this.getSchemaFile() + "])");
      }
      if (this.getSchemaFile() != null)
      {
         try
         {
            final Document d = XMLUtil.parse(this.getSchemaFile());
            try
            {
               this.schema = SchemaUtil.parseSchema(d, true);
            }
            catch (FormBuilderException fbe)
            {
               this.schema = SchemaUtil.parseSchema(d, false);
               LOGGER.warn("non fatal errors encountered parsing schema " + this.getFileName(FILE_SCHEMA) + 
                           "\n " + fbe.getMessage());
            }
         }
         catch (Exception e)
         {
            final String msg = "unable to parse " + this.getFileName(FILE_SCHEMA);
            this.removeUploadedSchemaFile();
            Utils.addErrorMessage(msg + ": " + e.getMessage(), e);
         }
      }
      return null;
   }

   /**
    * Action handler called when a rendering engine template file has been uploaded.
    */
   public String renderingEngineTemplateFileValueChanged(final ValueChangeEvent vce)
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("renderingEngineTemplateFileValueChanged(" + this.getFileName(FILE_RENDERING_ENGINE_TEMPLATE) + 
                      "[" + this.getRenderingEngineTemplateFile() + "])");
      }
      return null;
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /** Indicates whether or not the wizard is currently in edit mode */
   public boolean getEditMode()
   {
      return false;
   }

   /**
    * Returns the properties for current configured output methods JSF DataModel
    * 
    * @return JSF DataModel representing the current configured output methods
    */
   public DataModel getRenderingEngineTemplatesDataModel()
   {
      if (this.renderingEngineTemplatesDataModel == null)
      {
         this.renderingEngineTemplatesDataModel = new ListDataModel();
      }
      
      this.renderingEngineTemplatesDataModel.setWrappedData(this.renderingEngineTemplates);
      
      return this.renderingEngineTemplatesDataModel;
   }

   /**
    * Returns all configured rendering engine templates.
    */
   public List<RenderingEngineTemplateData> getRenderingEngineTemplates()
   {
      return this.renderingEngineTemplates;
   }
   
   /**
    * @return Returns the mime type currenty selected
    */
   public String getRenderingEngineName()
   {
      if (this.renderingEngine == null &&
          this.getRenderingEngineTemplateFileName() != null)
      {
         final FormsService fs = FormsService.getInstance();
         this.renderingEngine = 
            fs.guessRenderingEngine(this.getRenderingEngineTemplateFileName());
      }
      return (this.renderingEngine == null
              ? null
              : this.renderingEngine.getName());
   }
   
   /**
    * @param renderingEngineName Sets the currently selected rendering engine name
    */
   public void setRenderingEngineName(final String renderingEngineName)
   {
      final FormsService fs = FormsService.getInstance();
      this.renderingEngine = (renderingEngineName == null
                              ? null
                              : fs.getRenderingEngine(renderingEngineName));
   }
   
   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getRenderingEngineChoices()
   {
      final FormsService fs = FormsService.getInstance();
      final List<SelectItem>  result = new LinkedList<SelectItem>();
      for (RenderingEngine re : fs.getRenderingEngines())
      {
         result.add(new SelectItem(re.getName(), re.getName()));
      }
      return result;
   }
   
   /**
    * Returns a list of mime types in the system
    * 
    * @return List of mime types
    */
   public List<SelectItem> getMimeTypeChoices()
   {
       if (this.mimetypeChoices == null)
       {
           this.mimetypeChoices = new ArrayList<SelectItem>(50);
           
           final Map<String, String> mimetypes = this.mimetypeService.getDisplaysByMimetype();
           for (String mimetype : mimetypes.keySet())
           {
              this.mimetypeChoices.add(new SelectItem(mimetype, 
                                                      mimetypes.get(mimetype)));
           }
           
           // make sure the list is sorted by the values
           final QuickSort sorter = new QuickSort(this.mimetypeChoices, 
                                                  "label", 
                                                  true, 
                                                  IDataContainer.SORT_CASEINSENSITIVE);
           sorter.sort();
       }
       
       return this.mimetypeChoices;
   }
   
   /**
    * @return Returns the schema file or <tt>null</tt>
    */
   public File getSchemaFile()
   {
      return this.getFile(FILE_SCHEMA);
   }

   /**
    * Sets the schema file name
    */
   public void setSchemaFileName(final String schemaFileName)
   {
      this.schemaFileName = (schemaFileName != null && schemaFileName.length() != 0
                             ? schemaFileName
                             : null);
   }
   
   /**
    * @return Returns the schema file or <tt>null</tt>
    */
   public String getSchemaFileName()
   {
      return this.schemaFileName;
   }
   /**
    * Sets the rendering engine template file name
    */
   public void setRenderingEngineTemplateFileName(final String renderingEngineTemplateFileName)
   {
      this.renderingEngineTemplateFileName = 
         (renderingEngineTemplateFileName != null && renderingEngineTemplateFileName.length() != 0
          ? renderingEngineTemplateFileName
          : null);
   }
   
   /**
    * @return Returns the rendering engine template file name or <tt>null</tt>
    */
   public String getRenderingEngineTemplateFileName()
   {
      return this.renderingEngineTemplateFileName;
   }
   
   /**
    * @return Returns the rendering engine template file or <tt>null</tt>
    */
   public File getRenderingEngineTemplateFile()
   {
      return this.getFile(FILE_RENDERING_ENGINE_TEMPLATE);
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
      List<SelectItem> result = Collections.EMPTY_LIST;
      if (this.schema != null)
      {
         if (this.schemaRootElementNameChoices == null)
         {
            this.schemaRootElementNameChoices = new LinkedList<SelectItem>();
            final XSNamedMap elementsMap = this.schema.getComponents(XSConstants.ELEMENT_DECLARATION);
            for (int i = 0; i < elementsMap.getLength(); i++)
            {
               final XSElementDeclaration e = (XSElementDeclaration)elementsMap.item(i);
               this.schemaRootElementNameChoices.add(new SelectItem(e.getName(), e.getName()));
            }
         }
         result = this.schemaRootElementNameChoices;
      }
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("getSchemaRootElementNameChoices(" + this.schema + ") = " + result.size());
      }
      return result;
   }
   
   /**
    * Sets the human friendly name for this form.
    */
   public void setFormName(final String formName)
   {
      this.formName = formName != null && formName.length() != 0 ? formName : null;
   }

   /**
    * @return the human friendly name for this form.
    */
   public String getFormName()
   {
      return (this.formName == null && this.getSchemaFileName() != null
              ? FilenameUtils.removeExtension(this.getSchemaFileName())
              : this.formName);
   }

   /**
    * @return Returns the output path for form instance data.
    */
   public String getOutputPathPatternForFormInstanceData()
   {
      if (this.outputPathPatternForFormInstanceData == null)
      {
         this.outputPathPatternForFormInstanceData = DEFAULT_NAME_PATTERN + ".xml";
      }
      return this.outputPathPatternForFormInstanceData;
   }

   /**
    * @param outputPathPatternForFormInstanceData the output path for form instance data
    */
   public void setOutputPathPatternForFormInstanceData(final String outputPathPatternForFormInstanceData)
   {
      this.outputPathPatternForFormInstanceData = outputPathPatternForFormInstanceData;
   }

   /**
    * Sets the title for this form.
    */
   public void setFormTitle(final String formTitle)
   {
      this.formTitle = formTitle != null && formTitle.length() != 0 ? formTitle : null;
   }

   /**
    * @return the title for this form.
    */
   public String getFormTitle()
   {
      return (this.formTitle == null && this.getSchemaFileName() != null
              ? FilenameUtils.removeExtension(this.getSchemaFileName())
              : this.formTitle);
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
    * Sets the name for this renderingEngineTemplate.
    */
   public void setRenderingEngineTemplateName(final String renderingEngineTemplateName)
   {
      this.renderingEngineTemplateName = 
         (renderingEngineTemplateName != null && renderingEngineTemplateName.length() != 0
          ? renderingEngineTemplateName
          : null);
   }

   /**
    * @return the name for this renderingEngineTemplate.
    */
   public String getRenderingEngineTemplateName()
   {
      return (this.renderingEngineTemplateName == null && this.getRenderingEngineTemplateFileName() != null
              ? this.getRenderingEngineTemplateFileName()
              : this.renderingEngineTemplateName);
   }

   /**
    * Sets the title for this renderingEngineTemplate.
    */
   public void setRenderingEngineTemplateTitle(final String renderingEngineTemplateTitle)
   {
      this.renderingEngineTemplateTitle = 
         (renderingEngineTemplateTitle != null && renderingEngineTemplateTitle.length() != 0
          ? renderingEngineTemplateTitle
          : null);
   }

   /**
    * @return the title for this renderingEngineTemplate.
    */
   public String getRenderingEngineTemplateTitle()
   {
      return (this.renderingEngineTemplateTitle == null && this.getRenderingEngineTemplateFileName() != null
              ? FilenameUtils.removeExtension(this.getRenderingEngineTemplateFileName())
              : this.renderingEngineTemplateTitle);
   }

   /**
    * Sets the description for this renderingEngineTemplate.
    */
   public void setRenderingEngineTemplateDescription(final String renderingEngineTemplateDescription)
   {
      this.renderingEngineTemplateDescription = renderingEngineTemplateDescription;
   }

   /**
    * @return the description for this renderingEngineTemplate.
    */
   public String getRenderingEngineTemplateDescription()
   {
      return this.renderingEngineTemplateDescription;
   }

   /**
    * @return the default workflow
    */
   public WorkflowDefinition getDefaultWorkflowDefinition()
   {
      return (this.defaultWorkflowName == null || !this.applyDefaultWorkflow
              ? null
              : this.workflowService.getDefinitionByName(this.defaultWorkflowName));
   }

   /**
    * Sets the default workflow name
    */
   public void setDefaultWorkflowName(final String[] defaultWorkflowName)
   {
      assert defaultWorkflowName.length == 1;
      this.defaultWorkflowName = defaultWorkflowName[0];
   }

   /**
    * Returns the default workflow name
    */
   public String[] getDefaultWorkflowName()
   {
      if (this.defaultWorkflowName == null && this.getDefaultWorkflowChoices().size() != 0)
      {
         this.defaultWorkflowName = (String)this.getDefaultWorkflowChoices().get(0).getValue();
      }
      return new String[] { this.defaultWorkflowName };
   }

   /**
    * Indicates whether or not to configure a default workflow
    */
   public void setApplyDefaultWorkflow(final boolean applyDefaultWorkflow)
   {
      this.applyDefaultWorkflow = applyDefaultWorkflow;
   }

   /**
    * @return whether or not to configure a default workflow
    */
   public boolean getApplyDefaultWorkflow()
   {
      return this.applyDefaultWorkflow;
   }
   
   /**
    * @return List of UI items to represent the available Workflows for all websites
    */
   public List<UIListItem> getDefaultWorkflowChoices()
   {
      if (this.defaultWorkflowChoices == null)
      {
         // get list of workflows from config definitions
         final List<WorkflowDefinition> workflowDefs = AVMWorkflowUtil.getConfiguredWorkflows();
         this.defaultWorkflowChoices = new ArrayList<UIListItem>(workflowDefs.size() + 1);
         
         for (WorkflowDefinition workflowDef : workflowDefs)
         {
            final UIListItem item = new UIListItem();
            item.setValue(workflowDef.getName());
            item.setLabel(workflowDef.getTitle());
            item.setDescription(workflowDef.getDescription());
            item.setImage(WebResources.IMAGE_WORKFLOW_32);
            this.defaultWorkflowChoices.add(item);
         }
      }
      return this.defaultWorkflowChoices;
   }

   // ------------------------------------------------------------------------------
   // Service Injection
   
   /**
    * @param contentService The contentService to set.
    */
   public void setContentService(final ContentService contentService)
   {
      this.contentService = contentService;
   }

   /**
    * @param mimetypeService The mimetypeService to set.
    */
   public void setMimetypeService(final MimetypeService mimetypeService)
   {
      this.mimetypeService = mimetypeService;
   }

   /**
    * @param workflowService The workflowService to set.
    */
   public void setWorkflowService(final WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper Methods
   
   /**
    * Clear the uploaded form, clearing the specific Upload component by Id
    */
   protected void clearUpload(final String id)
   {
      // remove the file upload bean from the session
      final FacesContext ctx = FacesContext.getCurrentInstance();
      FileUploadBean fileBean = (FileUploadBean)
         ctx.getExternalContext().getSessionMap().get(FileUploadBean.getKey(id));
      if (fileBean != null)
      {
         fileBean.setFile(null);
         fileBean.setFileName(null);
      }
   }

   /**
    * Gets the file upload bean given with the given id.
    *
    * @return a file upload bean or <tt>null</tt>
    */
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
}
