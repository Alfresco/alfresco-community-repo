/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.wcm;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.content.CreateContentWizard;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.wcm.component.UIUserSandboxes;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.w3c.dom.Document;

/**
 * Bean implementation for the "Create Web Content Wizard" dialog
 */
public class CreateWebContentWizard extends CreateContentWizard
{
   private static final long serialVersionUID = -4090370304405270047L;

   private static final Log logger = LogFactory.getLog(CreateWebContentWizard.class);

   transient private List<SelectItem> formChoices;
   protected String createdPath = null;
   protected List<Rendition> renditions = null;
   protected FormInstanceData formInstanceData = null;
   protected boolean formSelectDisabled = false;
   protected boolean startWorkflow = false;
   protected String formDescriptionAttribute;

   transient private AVMLockingService avmLockingService;
   transient private AVMService avmService;
   transient private AVMSyncService avmSyncService;
   protected AVMBrowseBean avmBrowseBean;
   protected FilePickerBean filePickerBean;

   /**
    * @param avmService The AVMService to set.
    */
   public void setAvmService(final AVMService avmService)
   {
      this.avmService = avmService;
   }

   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMLockingAwareService();
      }
      return avmService;
   }

   /**
    * @param avmLockingService The AVMLockingService to set.
    */
   public void setAvmLockingService(final AVMLockingService avmLockingService)
   {
      this.avmLockingService = avmLockingService;
   }

   protected AVMLockingService getAvmLockingService()
   {
      if (avmLockingService == null)
      {
         avmLockingService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMLockingService();
      }
      return avmLockingService;
   }

   /**
    * @param avmSyncService The AVMSyncService to set.
    */
   public void setAvmSyncService(final AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }
   
   protected AVMSyncService getAvmSyncService()
   {
      if (avmSyncService == null)
      {
         avmSyncService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMSyncService();
      }
      return avmSyncService;
   }

   /**
    * @param avmBrowseBean The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param filePickerBean The FilePickerBean to set.
    */
   public void setFilePickerBean(final FilePickerBean filePickerBean)
   {
      this.filePickerBean = filePickerBean;
   }

   /**
    * @param formsService The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }

   protected FormsService getFormsService()
   {
      if (formsService == null)
      {
         formsService = (FormsService) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "FormsService");
      }
      return formsService;
   }

   // ------------------------------------------------------------------------------
   // Wizard implementation

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      this.content = null;
      this.inlineEdit = true;
      this.formName = null;
      this.mimeType = MimetypeMap.MIMETYPE_XML;
      this.formInstanceData = null;
      
      this.renditions = null;
      this.startWorkflow = false;
      this.formSelectDisabled = false;
      this.createMimeTypes = null;
      this.formChoices = null;
      this.filePickerBean.clearUploadedFiles();

      // check for a form ID being passed in as a parameter
      if (this.parameters.get(UIUserSandboxes.PARAM_FORM_NAME) != null)
      {
         // it is used to init the dialog to a specific template
         final String formName = parameters.get(UIUserSandboxes.PARAM_FORM_NAME);
         try
         {
            final Form form = this.avmBrowseBean.getWebProject().getForm(formName);
            if (form != null)
            {
               this.formName = form.getName();
               this.formSelectDisabled = true;
            }
         }
         catch (FormNotFoundException fnfe)
         {
            Utils.addErrorMessage(fnfe.getMessage(), fnfe);
         }
      }

      // this.formDescriptionAttribute = buildFormDescriptionAttribute();
      
      // reset the preview layer
      String storeName = AVMUtil.getStoreName(this.avmBrowseBean.getCurrentPath());
      storeName = AVMUtil.getCorrespondingPreviewStoreName(storeName);
      final String path = AVMUtil.buildStoreRootPath(storeName);

      FacesContext context = FacesContext.getCurrentInstance();
      RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
      RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
      {
         public String execute() throws Throwable
         {
            if (logger.isDebugEnabled())
               logger.debug("reseting layer " + path);
            
            // call the actual implementation
            getAvmSyncService().resetLayer(path);
            return null;
         }
      };
      
      try
      {
         // Execute
         txnHelper.doInTransaction(callback);
      }
      catch (Exception e)
      {
         Utils.addErrorMessage(e.getMessage(), e);
      }
   }

   @Override
   public String next()
   {
      if ("summary".equals(Application.getWizardManager().getCurrentStepName()))
      {
         // if rendering a form, then save the content now to generate the renditions
         if (MimetypeMap.MIMETYPE_XML.equals(this.mimeType))
         {
            FacesContext context = FacesContext.getCurrentInstance();
            RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
            RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
            {
               public String execute() throws Throwable
               {
                  // call the actual implementation
                  saveContent();
                  return null;
               }
            };
            
            try
            {
               // Execute
               txnHelper.doInTransaction(callback);
            }
            catch (Exception e)
            {
               Application.getWizardManager().getState().setCurrentStep(Application.getWizardManager().getCurrentStep() - 1);
               Utils.addErrorMessage(e.getMessage(), e);
            }
         }
      }
      return super.next();
   }

   @Override
   public String back()
   {
      if ("content".equals(Application.getWizardManager().getCurrentStepName()))
      {
         FacesContext context = FacesContext.getCurrentInstance();
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
         RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
         {
            public String execute() throws Throwable
            {
               if (formInstanceData != null)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("clearing form instance data: " + formInstanceData.getPath());
                  
                  getAvmService().removeNode(formInstanceData.getPath());
               }
               
               if (renditions != null)
               {
                  for (Rendition r : renditions)
                  {
                     getAvmService().removeNode(r.getPath());
                  }
               }
               
               return null;
            }
         };
         
         try
         {
            // Execute
            txnHelper.doInTransaction(callback);
         }
         catch (Exception e)
         {
            Utils.addErrorMessage(e.getMessage(), e);
         }
         
         this.formInstanceData = null;
         this.renditions = null;
      }
      return super.back();
   }
   
   @Override
   protected String finishImpl(final FacesContext context, String outcome)
      throws Exception
   {
      if (this.formInstanceData == null || this.renditions == null)
      {
         this.saveContent();
      }
      
      final NodeRef[] uploadedFiles = this.filePickerBean.getUploadedFiles();
      final List<AVMDifference> diffList = new ArrayList<AVMDifference>(1 + this.renditions.size() + uploadedFiles.length);
      diffList.add(new AVMDifference(-1, this.createdPath, -1, AVMUtil.getCorrespondingPathInMainStore(this.createdPath), AVMDifference.NEWER));
      for (Rendition rendition : this.renditions)
      {
         final String path = rendition.getPath();
         diffList.add(new AVMDifference(-1, path, -1, AVMUtil.getCorrespondingPathInMainStore(path), AVMDifference.NEWER));
      }

      for (NodeRef uploadedFile : uploadedFiles)
      {
         final String path = AVMNodeConverter.ToAVMVersionPath(uploadedFile).getSecond();
         diffList.add(new AVMDifference(-1, path, -1, AVMUtil.getCorrespondingPathInMainStore(path), AVMDifference.NEWER));
      }

      if (logger.isDebugEnabled())
      {
         for (final AVMDifference diff : diffList)
         {
            logger.debug("updating main store with " + diff.getSourcePath());
         }
      }
      this.getAvmSyncService().update(diffList, null, true, true, true, true, null, null);

      for (final AVMDifference diff : diffList)
      {
         final String path = diff.getDestinationPath();
         String storeId = AVMUtil.getStoreId(path);
         String storePath = AVMUtil.getStoreRelativePath(path);
         String storeName = AVMUtil.getStoreName(path);

         String lockOwner = getAvmLockingService().getLockOwner(storeId, storePath);
         Map<String, String> lockData = getAvmLockingService().getLockData(storeId, storePath);
         String fromStoreName = lockData.get(WCMUtil.LOCK_KEY_STORE_NAME);
         lockData.put(WCMUtil.LOCK_KEY_STORE_NAME, storeName);

         if (logger.isDebugEnabled())
         {
            logger.debug("modifying lock on " + path + ".  chaging store from " + fromStoreName + " to " + storeName);
         }

         this.getAvmLockingService().modifyLock(storeId, AVMUtil.getStoreRelativePath(diff.getSourcePath()), lockOwner, storeId, AVMUtil.getStoreRelativePath(path), lockData);
      }
      
      if (this.startWorkflow)
      {
         final List<AVMNodeDescriptor> submitNodes = new ArrayList<AVMNodeDescriptor>(1 + this.getUploadedFiles().size() + this.getRenditions().size());
         for (final AVMDifference d : diffList)
         {
            submitNodes.add(getAvmService().lookup(-1, d.getDestinationPath()));
         }
         this.avmBrowseBean.setNodesForSubmit(submitNodes);
         final Map<String, String> dialogParams = new HashMap<String, String>(1);
         dialogParams.put(SubmitDialog.PARAM_LOAD_SELECTED_NODES_FROM_BROWSE_BEAN, Boolean.TRUE.toString());
         Application.getDialogManager().setupParameters(dialogParams);
         outcome = (outcome + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + AlfrescoNavigationHandler.DIALOG_PREFIX + "submitSandboxItems");
      }
      if (this.formProcessorSession != null)
      {
         this.formProcessorSession.destroy();
      }
      this.filePickerBean.clearUploadedFiles();

      // return the default outcome
      return outcome;
   }

   @Override
   protected String doPostCommitProcessing(final FacesContext facesContext, final String outcome)
   {
      // reset all paths and structures to the main store
      this.createdPath = AVMUtil.getCorrespondingPathInMainStore(this.createdPath);
      if (logger.isDebugEnabled())
         logger.debug("reset path " + this.createdPath + " to main store");

      if (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.formName != null)
      {
         try
         {
            this.formInstanceData = getFormsService().getFormInstanceData(-1, this.createdPath);
            this.renditions = this.formInstanceData.getRenditions();
            
            if (logger.isDebugEnabled())
            {
               logger.debug("reset form instance data " + this.formInstanceData.getName() + 
                            " and " + this.renditions.size() + " rendition(s) to main store");
            }
         }
         catch (FormNotFoundException fnfe)
         {
            logger.warn(fnfe);
         }
      }
      
      this.avmBrowseBean.setAvmActionNode(new AVMNode(this.getAvmService().lookup(-1, this.createdPath)));
      
      return outcome;
   }

   @Override
   public boolean getNextButtonDisabled()
   {
      // TODO: Allow the next button state to be configured so that
      // wizard implementations don't have to worry about
      // checking step numbers

      boolean disabled = false;
      if ("details".equals(Application.getWizardManager().getCurrentStepName()))
      {
         disabled = (this.fileName == null || this.fileName.length() == 0);
      }

      return disabled;
   }

   /**
    * Save the specified content using the currently set wizard attributes
    */
   @SuppressWarnings("unchecked")
   protected void saveContent() throws Exception
   {
      // get the parent path of the location to save the content
      String fileName = this.getFileName();
      String contentName = fileName;
      if (logger.isDebugEnabled())
         logger.debug("saving file content to " + fileName);

      final String cwd = AVMUtil.getCorrespondingPathInPreviewStore(this.avmBrowseBean.getCurrentPath());
      final Form form = (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) ? this.getForm() : null);
      String path = cwd;
      
      final Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(1, 1.0f);
      final List<QName> aspects = new ArrayList<QName>(4);

      if (form != null)
      {
         path = form.getOutputPathForFormInstanceData(this.getInstanceDataDocument(), fileName, cwd, this.avmBrowseBean.getWebapp());
         this.content = XMLUtil.toString(this.getInstanceDataDocument(), false);
         final String[] sb = AVMNodeConverter.SplitBase(path);
         path = sb[0];
         fileName = sb[1];
         props.put(WCMAppModel.PROP_PARENT_FORM_NAME, new PropertyValue(null, form.getName()));
         props.put(WCMAppModel.PROP_ORIGINAL_PARENT_PATH, new PropertyValue(null, cwd));
         aspects.add(WCMAppModel.ASPECT_FORM_INSTANCE_DATA);
      }
      props.put(ContentModel.PROP_TITLE, new PropertyValue(null, fileName));
      aspects.add(ContentModel.ASPECT_TITLED);

      if (logger.isDebugEnabled())
         logger.debug("creating all directories in path " + path);

      AVMUtil.makeAllDirectories(path);

      if (logger.isDebugEnabled())
         logger.debug("creating file " + fileName + " in " + path);

      // put the content of the file into the AVM store
      String filePath = AVMNodeConverter.ExtendAVMPath(path, fileName); 
      try
      {
         /**
          *  create the new file 
          */
         getAvmService().createFile(path, fileName, 
                  new ByteArrayInputStream((this.content == null ? "" : this.content).getBytes("UTF-8")),
                  aspects,
                  props);
      }
      catch (AVMExistsException avmee)
      {
         String msg = Application.getMessage(FacesContext.getCurrentInstance(), "error_exists");
         msg = MessageFormat.format(msg, fileName);
         throw new AlfrescoRuntimeException(msg, avmee);
      }

      // remember the created path
      this.createdPath = filePath;

      // add titled aspect for the read/edit properties screens
      final NodeRef formInstanceDataNodeRef = AVMNodeConverter.ToNodeRef(-1, this.createdPath);

      /**
       * Generate form renditions.
       */
      if (form != null)
      {

         this.formInstanceData = getFormsService().getFormInstanceData(formInstanceDataNodeRef);
         this.renditions = new LinkedList<Rendition>();
         for (RenderingEngineTemplate ret : form.getRenderingEngineTemplates())
         {
            try
            {
               path = ret.getOutputPathForRendition(this.formInstanceData, cwd, contentName);
               
               if (logger.isDebugEnabled())
                  logger.debug("About to render path: " + path);
               
               // generate the rendition
               this.renditions.add(ret.render(this.formInstanceData, path));
            }
            catch (Exception e)
            {
               // TODO - improve error handling, e.g. render could return list of errors rather than splitting on newline character
               StringTokenizer st = new StringTokenizer(e.getMessage(), "\n");
               if (st.hasMoreElements())
               {
                  Utils.addErrorMessage("Error generating rendition using " + ret.getName() + ": " + st.nextToken(), e);
                  while (st.hasMoreElements()) 
                  {
                     Utils.addErrorMessage(st.nextToken(), e);
                  }
               }
               else
               {
                  Utils.addErrorMessage("Error generating rendition using " + ret.getName() +
                                        ": " + e.getMessage(), e);
               }
            }
         }
      }
      else
      {
         this.renditions = Collections.EMPTY_LIST;
      }
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /** Overrides in order to strip an xml extension if the user entered it */
   @Override
   public String getFileName()
   {
      final String result = super.getFileName();
      return (result != null && MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.getFormName() != null && "xml".equals(FilenameUtils.getExtension(result).toLowerCase()) ? FilenameUtils
            .removeExtension(result)
            : result);
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

   /**
    * @return the available forms from this web project that can be created.
    */
   public List<SelectItem> getFormChoices()
   {
      if (this.formChoices == null)
      {
         final List<Form> forms = this.avmBrowseBean.getWebProject().getForms();
         this.formChoices = new ArrayList<SelectItem>(forms.size());
         for (final Form f : forms)
         {
            this.formChoices.add(new SelectItem(f.getName(), f.getTitle()));
         }

         final QuickSort sorter = new QuickSort(this.formChoices, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();

      }
      return this.formChoices;
   }

   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getCreateMimeTypes()
   {
      if ((this.createMimeTypes == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         final FacesContext context = FacesContext.getCurrentInstance();

         // add the well known object type to start with
         this.createMimeTypes = new ArrayList<SelectItem>(5);

         // add the configured create mime types to the list
         final ConfigService svc = Application.getConfigService(context);
         final Config wizardCfg = svc.getConfig("Content Wizards");
         if (wizardCfg == null)
         {
            logger.warn("Could not find 'Content Wizards' configuration section");
         }
         else
         {
            final ConfigElement typesCfg = wizardCfg.getConfigElement("create-mime-types");
            if (typesCfg == null)
            {
               logger.warn("Could not find 'create-mime-types' configuration element");
            }
            else
            {
               for (ConfigElement child : typesCfg.getChildren())
               {
                  final String currentMimeType = child.getAttribute("name");
                  if (currentMimeType == null || (MimetypeMap.MIMETYPE_XML.equals(currentMimeType) && this.getFormChoices().size() == 0))
                  {
                     continue;
                  }

                  final String label = this.getSummaryMimeType(currentMimeType);
                  this.createMimeTypes.add(new SelectItem(currentMimeType, label));
               }

               // make sure the list is sorted by the label
               final QuickSort sorter = new QuickSort(this.objectTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
         }
      }

      return this.createMimeTypes;
   }

   /**
    * @return the current seleted form's name or <tt>null</tt>.
    */
   public String getFormName()
   {
      return this.formName;
   }

   public Form getForm() throws FormNotFoundException
   {
      return (this.getFormName() != null ? this.avmBrowseBean.getWebProject().getForm(this.getFormName()) : null);
   }

   /**
    * @param form Sets the currently selected form
    */
   public void setFormName(final String formName)
   {
      this.formName = formName;
   }

   /**
    * @return Returns the wrapper instance data for feeding the xml content to the form processor.
    */
   public Document getInstanceDataDocument()
   {
      if (this.instanceDataDocument == null)
      {
         final String content = this.getContent();
         try
         {
            this.instanceDataDocument = (content != null ? XMLUtil.parse(content) : XMLUtil.newDocument());
         }
         catch (Exception e)
         {
            Utils.addErrorMessage("error parsing document", e);
            this.instanceDataDocument = XMLUtil.newDocument();
         }
      }
      return this.instanceDataDocument;
   }
   
   /**
    * @return List of UI items to represent the full list of available Web Forms
    */
   public List<SelectItem> getFormsList()
   {
      Collection<Form> forms = getFormsService().getWebForms();
      List<SelectItem> items = new ArrayList<SelectItem>(forms.size()+1);
      items.add(new SelectItem("", ""));
      for (Form form : forms)
      {
       items.add(new SelectItem(form.getName(), form.getTitle()));
      }
      return items;
   }

   /**
    * Returns the generated form instance data.
    */
   public FormInstanceData getFormInstanceData()
   {
      return this.formInstanceData;
   }

   /**
    * Returns the generated renditions
    */
   public List<Rendition> getRenditions()
   {
      return this.renditions;
   }

   /**
    * Returns the files uploaded using the form
    */
   @SuppressWarnings("unchecked")
   public List<UIListItem> getUploadedFiles()
   {
      if (this.formProcessorSession == null)
      {
         return Collections.EMPTY_LIST;
      }

      final NodeRef[] uploadedFiles = this.filePickerBean.getUploadedFiles();
      final List<UIListItem> result = new ArrayList<UIListItem>(uploadedFiles.length);

      for (NodeRef nodeRef : uploadedFiles)
      {
         final UIListItem item = new UIListItem();
         final String name = (String) this.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);
         item.setValue(name);
         item.setLabel((String) this.getNodeService().getProperty(nodeRef, ContentModel.PROP_TITLE));
         item.setDescription((String) this.getNodeService().getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
         item.setImage(FileTypeImageUtils.getFileTypeImage(name, false));
         result.add(item);
      }
      return result;
   }

   /**
    * Returns the number of submittable files which is the total number of uploaded files, renditions, and the form instance data.
    */
   public int getNumberOfSubmittableFiles()
   {
      return 1 + this.getUploadedFiles().size() + this.getRenditions().size();
   }

   public boolean getFormSelectDisabled()
   {
      return this.formSelectDisabled;
   }

   public void setFormSelectDisabled(boolean formSelectDisabled)
   {
      this.formSelectDisabled = formSelectDisabled;
   }

   public void setStartWorkflow(final boolean startWorkflow)
   {
      this.startWorkflow = startWorkflow;
   }

   public boolean getStartWorkflow()
   {
      return this.startWorkflow;
   }

   public boolean getSubmittable()
   {
      return true;
   }

   /**
    * Provides path to current WCM webApp folder.
    */
   public String getPreviewSandboxUrl()
   {
      return AVMUtil.buildWebappUrl(AVMUtil.getCorrespondingPreviewStoreName(this.avmBrowseBean.getSandbox()), this.avmBrowseBean.getWebapp());
   }

   /**
    * Provides name of current WCM webApp folder.
    */
   
   public String getAvmWebappPrefix()
   {
       return AVMUtil.getPreviewURI(AVMUtil.getCorrespondingPreviewStoreName(this.avmBrowseBean.getSandbox()));
   }

   /**
    * Provides the url to the preview sandbox containing the asset currently being edited.
    */
   public String getAvmWebappName()
   {
      return this.avmBrowseBean.getWebapp();
   }

   public String getSummary()
   {
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());

      // TODO: show first few lines of content here?
      return this.buildSummary(
            new String[] 
            {
               bundle.getString("file_name"), 
               bundle.getString("type"), 
               bundle.getString("content_type")
            },
            new String[] 
            {
               Utils.encode(this.getFileName()), 
               this.getSummaryObjectType(), 
               this.getSummaryMimeType(this.mimeType)
            });
   }

   public boolean getEditMode()
   {
      return false;
   }

   public String getFormDescriptionAttribute()
   {
       this.formDescriptionAttribute = buildFormDescriptionAttribute();
       return this.formDescriptionAttribute;
   }
   
   public String getFormLabelAttribute()
   {
      StringBuilder builder = new StringBuilder("<b>");
      builder.append(Utils.encode(this.getFormInstanceData().getName()));
      builder.append("</b>");
      return builder.toString();
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
   
   private String buildFormDescriptionAttribute()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       String contextPath = fc.getExternalContext().getRequestContextPath();
       StringBuilder attribute = new StringBuilder(255);
       attribute.append("<span style=\"float:right;\">");
       attribute.append("<a id=\"preview_fid\" href=\"").append(getFormInstanceData().getUrl()).append("\" ");
       attribute.append("style=\"text-decoration: none;\" ");
       attribute.append("target=\"window_").append(getFormInstanceData().getName()).append("\">");
       attribute.append("<img src=\"").append(contextPath).append("/images/icons/preview_website.gif\" ");
       attribute.append("align=\"absmiddle\" style=\"border: 0px\" ");
       attribute.append("alt=").append(getFormInstanceData().getName()).append("\">");
       attribute.append("</a></span>\n");
       attribute.append(DescriptionAttributeHelper.getTableBegin());
       String formTitle = null;
       try
       {
           formTitle = getForm().getTitle();
       }
       catch (FormNotFoundException e)
       {
           formTitle = Application.getMessage(FacesContext.getCurrentInstance(),"form_not_found");
       }
       attribute.append(DescriptionAttributeHelper.getTableLine(fc, "form", formTitle));
       attribute.append(DescriptionAttributeHelper.getTableLine(fc, "location", 
                getFormInstanceData().getSandboxRelativePath()));
       attribute.append(DescriptionAttributeHelper.getTableEnd());
       return attribute.toString();
   }
}
