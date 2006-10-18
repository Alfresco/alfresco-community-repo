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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.BaseContentWizard;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.templating.OutputUtil;
import org.alfresco.web.templating.TemplateType;
import org.alfresco.web.templating.TemplatingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Create Web Content Wizard" dialog
 */
public class CreateWebContentWizard extends BaseContentWizard
{
   private static final Log logger = LogFactory.getLog(CreateWebContentWizard.class);
   
   protected String content = null;
   protected String templateTypeName;
   protected List<SelectItem> createMimeTypes;
   protected String createdPath = null;
   
   /** AVM service bean reference */
   protected AVMService avmService;
   
   /** AVM Browse Bean reference */
   protected AVMBrowseBean avmBrowseBean;
   
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome)
      throws Exception
   {
      if (logger.isDebugEnabled())
         logger.debug("saving file content to " + this.fileName);
      saveContent(null, this.content);
      
      if (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.templateTypeName != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("generating template output for " + this.templateTypeName);
         final TemplateType tt = this.getTemplateType();
         final TemplatingService ts = TemplatingService.getInstance();

         final Map<QName, Serializable> props = new HashMap<QName, Serializable>();
         props.put(WCMModel.PROP_TEMPLATE_DERIVED_FROM, tt.getNodeRef());
         props.put(WCMModel.PROP_TEMPLATE_DERIVED_FROM_NAME, tt.getName());
         this.nodeService.addAspect(AVMNodeConverter.ToNodeRef(-1, this.createdPath), 
                                    WCMModel.ASPECT_TEMPLATE_DERIVED,
                                    props);

         OutputUtil.generate(this.createdPath.substring(0, this.createdPath.lastIndexOf('/')),
                             ts.parseXML(this.content),
                             tt,
                             this.fileName,
                             this.contentService,
                             this.nodeService,
                             this.avmService);
      }
      
      // return the default outcome
      return outcome;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods

   /**
    * Save the specified content using the currently set wizard attributes
    * 
    * @param fileContent      File content to save
    * @param strContent       String content to save
    */
   protected void saveContent(File fileContent, String strContent) throws Exception
   {
      // get the parent path of the location to save the content
      String path = this.avmBrowseBean.getCurrentPath();
      
      // put the content of the file into the AVM store
      if (fileContent != null)
      {
         avmService.createFile(path, this.fileName, new BufferedInputStream(new FileInputStream(fileContent)));
      }
      else 
      {
         avmService.createFile(path, this.fileName, new ByteArrayInputStream((strContent == null ? "" : strContent).getBytes()));
      }
      
      // remember the created path
      this.createdPath = path + '/' + this.fileName;
      
      // add titled aspect for the read/edit properties screens
      NodeRef fileRef = AVMNodeConverter.ToNodeRef(-1, this.createdPath);
      Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, this.fileName);
      this.nodeService.addAspect(fileRef, ContentModel.ASPECT_TITLED, titledProps);
   }
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.content = null;
      this.inlineEdit = true;
      this.templateTypeName = null;
      this.mimeType = MimetypeMap.MIMETYPE_XML;
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
            disabled = (this.fileName == null || this.fileName.length() == 0);
            break;
         }
      }
      
      return disabled;
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * @param fileName The name of the file
    */
   public void setFileName(String fileName)
   {
      super.setFileName(fileName != null && fileName.indexOf('.') == -1 
                        ? fileName + ".xml" 
                        : fileName);
   }
   
   /**
    * @return Returns the content from the edited form.
    */
   public String getContent()
   {
      return this.content;
   }
   
   /**
    * @param content The content to edit (should be clear initially)
    */
   public void setContent(String content)
   {
      this.content = content;
   }
   
   public List<SelectItem> getCreateTemplateTypes()
   {
      Collection<TemplateType> ttl = TemplatingService.getInstance().getTemplateTypes();
      List<SelectItem> sil = new ArrayList<SelectItem>(ttl.size());
      for (TemplateType tt : ttl)
      {
         sil.add(new SelectItem(tt.getName(), tt.getName()));
      }
      
      QuickSort sorter = new QuickSort(sil, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      return sil;
   }
   
   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getCreateMimeTypes()
   {
      if (this.createMimeTypes == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // add the well known object type to start with
         this.createMimeTypes = new ArrayList<SelectItem>(5);
         
         // add the configured create mime types to the list
         ConfigService svc = Application.getConfigService(context);
         Config wizardCfg = svc.getConfig("Content Wizards");
         if (wizardCfg != null)
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("create-mime-types");
            if (typesCfg != null)
            {
               for (ConfigElement child : typesCfg.getChildren())
               {
                  String currentMimeType = child.getAttribute("name");
                  if (currentMimeType != null)
                  {
                     String label = getSummaryMimeType(currentMimeType);
                     this.createMimeTypes.add(new SelectItem(currentMimeType, label));
                  }
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.objectTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
            else
            {
               logger.warn("Could not find 'create-mime-types' configuration element");
            }
         }
         else
         {
            logger.warn("Could not find 'Content Wizards' configuration section");
         }
         
      }
      
      return this.createMimeTypes;
   }
   
   public String getTemplateTypeName()
   {
      return this.templateTypeName;
   }
   
   public TemplateType getTemplateType()
   {
      final TemplatingService ts = TemplatingService.getInstance();
      return ts.getTemplateType(this.getTemplateTypeName());
   }
   
   /**
    * @param templateType Sets the currently selected template type
    */
   public void setTemplateTypeName(final String templateTypeName)
   {
      this.templateTypeName = templateTypeName;
   }
   
   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      // TODO: show first few lines of content here?
      return buildSummary(
            new String[] {bundle.getString("file_name"), 
                  bundle.getString("type"), 
                  bundle.getString("content_type")},
                  new String[] {this.fileName, getSummaryObjectType(), 
                  getSummaryMimeType(this.mimeType)});
   }
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Create content type value changed by the user
    */
   public void createContentChanged(ValueChangeEvent event)
   {
      // clear the content as HTML is not compatible with the plain text box etc.
      this.content = null;
   }
}
