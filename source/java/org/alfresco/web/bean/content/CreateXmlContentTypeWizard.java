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
package org.alfresco.web.bean.content;

import java.io.*;
import java.util.*;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;
import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.templating.*;
import org.alfresco.web.templating.xforms.*;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Bean implementation for the "Create Content Wizard" dialog
 * 
 * @author arielb
 */
public class CreateXmlContentTypeWizard extends BaseWizardBean
{
   
    private final static Log LOGGER = 
	LogFactory.getLog(CreateXmlContentTypeWizard.class);

    private String presentationTemplateType;
    protected ContentService contentService;

    // ------------------------------------------------------------------------------
    // Wizard implementation
    
    @Override
    protected String finishImpl(FacesContext context, String outcome)
	throws Exception
    {
	// get the node ref of the node that will contain the content
	NodeRef containerNodeRef = this.getContainerNodeRef();

	FileInfo fileInfo = 
	    this.fileFolderService.create(containerNodeRef,
					  this.getSchemaFileName(),
					  ContentModel.TYPE_CONTENT);
	NodeRef fileNodeRef = fileInfo.getNodeRef();
      
	if (LOGGER.isDebugEnabled())
	    LOGGER.debug("Created file node for file: " + 
			 this.getSchemaFileName());
	
	// get a writer for the content and put the file
	ContentWriter writer = contentService.getWriter(fileNodeRef, 
							ContentModel.PROP_CONTENT, true);
	// set the mimetype and encoding
	writer.setMimetype("text/xml");
	writer.setEncoding("UTF-8");
	writer.putContent(this.getSchemaFile());

	fileInfo = this.fileFolderService.create(containerNodeRef,
						 this.getPresentationTemplateFileName(),
						 ContentModel.TYPE_CONTENT);
	fileNodeRef = fileInfo.getNodeRef();
      
	if (LOGGER.isDebugEnabled())
	    LOGGER.debug("Created file node for file: " + 
			 this.getPresentationTemplateFileName());
	
	// get a writer for the content and put the file
	writer = contentService.getWriter(fileNodeRef, 
					  ContentModel.PROP_CONTENT, true);
	// set the mimetype and encoding
	writer.setMimetype("text/xml");
	writer.setEncoding("UTF-8");
	writer.putContent(this.getPresentationTemplateFile());

	final TemplatingService ts = TemplatingService.getInstance();
	ts.registerTemplateType(this.getTemplateType());

	// return the default outcome
	return outcome;
    }
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      this.removeUploadedSchemaFile();
      this.removeUploadedPresentationTemplateFile();
   }

   @Override
   public String cancel()
   {
       this.removeUploadedSchemaFile();
       this.removeUploadedPresentationTemplateFile();
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
   
//   @Override
//   protected String doPostCommitProcessing(FacesContext context, String outcome)
//   {
//      // as we were successful, go to the set properties dialog if asked
//      // to otherwise just return
//      if (this.showOtherProperties)
//      {
//         // we are going to immediately edit the properties so we need
//         // to setup the BrowseBean context appropriately
//         this.browseBean.setDocument(new Node(this.createdNode));
//      
//         return getDefaultFinishOutcome() + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
//                "dialog:setContentProperties";
//      }
//      else
//      {
//         return outcome;
//      }
//   }

   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedSchemaFile()
   {
      clearUpload("schema");
      
      // refresh the current page
      return null;
   }

   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedPresentationTemplateFile()
   {
      clearUpload("pt");
      
      // refresh the current page
      return null;
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * @return Returns the mime type currenty selected
    */
    public String getPresentationTemplateType()
    {
	return this.presentationTemplateType;
    }
    
    /**
     * @param presentationTemplateType Sets the currently selected mime type
     */
    public void setPresentationTemplateType(String presentationTemplateType)
    {
	this.presentationTemplateType = presentationTemplateType;
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
	return this.getFile("schema");
    }

    /**
     * @return Returns the schema file or <tt>null</tt>
     */
    public String getSchemaFileName()
    {
	// try and retrieve the file and filename from the file upload bean
	// representing the file we previously uploaded.
	return this.getFileName("schema");
    }

    /**
     * @return Returns the schema file or <tt>null</tt>
     */
    public String getPresentationTemplateFileName()
    {
	return this.getFileName("pt");
    }

    /**
     * @return Returns the presentationTemplate file or <tt>null</tt>
     */
    public File getPresentationTemplateFile()
    {
	return this.getFile("pt");
    }

    public TemplateType getTemplateType()
	throws ParserConfigurationException,
	       SAXException,
	       IOException
    {
	if (this.getSchemaFile() == null)
	    return null;
	final TemplatingService ts = TemplatingService.getInstance();
	final String rootTagName = 
	    this.getSchemaFileName().replaceAll("([^\\.])\\..+", "$1");
	final Document d = ts.parseXML(this.getSchemaFile());
	final TemplateType result = ts.newTemplateType(rootTagName, d);
	if (this.getPresentationTemplateFile() != null)
	{
	    result.addOutputMethod(new XSLTOutputMethod(this.getPresentationTemplateFile()));
	}
	return result;
    }
   
   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getCreatePresentationTemplateTypes()
   {
       return (List<SelectItem>)Arrays.asList(new SelectItem[] {
	       new SelectItem("freemarker", "FreeMarker"),
	       new SelectItem("xslt", "XSLT")
	   });
   }

   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      // TODO: show first few lines of content here?
      return buildSummary(new String[] {
	      "Schema File", 
	      "Presentation Template Type",
	      "Presentation Template"
	  },
	  new String[] {
	      this.getSchemaFileName(),
	      this.getPresentationTemplateType(),
	      this.getPresentationTemplateFileName()
	  });
   }
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
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
    */
    protected void clearUpload(final String id)
    {
      // remove the file upload bean from the session
	FacesContext ctx = FacesContext.getCurrentInstance();
	FileUploadBean fileBean = (FileUploadBean)
	    ctx.getExternalContext().getSessionMap().
	    get(FileUploadBean.getKey(id));
	if (fileBean != null)
	    fileBean.setFile(null);
    }
}
