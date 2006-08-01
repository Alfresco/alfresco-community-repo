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

import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.templating.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;


/**
 * Bean implementation for the "Create Content Wizard" dialog
 * 
 * @author arielb
 */
public class CreateXmlContentTypeWizard extends BaseContentWizard
{
   
    private final static Log logger = LogFactory.getLog(CreateXmlContentTypeWizard.class);
    private TemplateType tt;

    // ------------------------------------------------------------------------------
    // Wizard implementation
    
    @Override
    protected String finishImpl(FacesContext context, String outcome)
	throws Exception
    {
	
	saveContent(this.getSchemaFile(), null);
	final TemplatingService ts = TemplatingService.getInstance();
	ts.registerTemplateType(tt);
	// return the default outcome
	return outcome;
    }
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      this.mimeType = "text/xml";
      this.clearUpload();
   }

   @Override
   public String cancel()
   {
       this.clearUpload();
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
            disabled = (this.fileName == null || this.fileName.length() == 0);
            break;
         }
      }
      
      return disabled;
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // as we were successful, go to the set properties dialog if asked
      // to otherwise just return
      if (this.showOtherProperties)
      {
         // we are going to immediately edit the properties so we need
         // to setup the BrowseBean context appropriately
         this.browseBean.setDocument(new Node(this.createdNode));
      
         return getDefaultFinishOutcome() + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
                "dialog:setContentProperties";
      }
      else
      {
         return outcome;
      }
   }

   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedFile()
   {
      clearUpload();
      
      // refresh the current page
      return null;
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
    private FileUploadBean getFileUploadBean()
    {
	final FacesContext ctx = FacesContext.getCurrentInstance();
	final Map sessionMap = ctx.getExternalContext().getSessionMap();
	return (FileUploadBean)sessionMap.get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
    }

   /**
    * @return Returns the name of the file
    */
    public String getFileName()
    {
	// try and retrieve the file and filename from the file upload bean
	// representing the file we previously uploaded.
	final FileUploadBean fileBean = this.getFileUploadBean();
	if (fileBean != null)
	    this.fileName = fileBean.getFileName();
	return this.fileName;
    }

    /**
     * @param fileName The name of the file
     */
    public void setFileName(String fileName)
    {
	this.fileName = fileName;
	
	// we also need to keep the file upload bean in sync
	final FileUploadBean fileBean = this.getFileUploadBean();
	if (fileBean != null)
	    fileBean.setFileName(this.fileName);
    }

    /**
     * @return Returns the schema file or <tt>null</tt>
     */
    public File getSchemaFile()
    {
	// try and retrieve the file and filename from the file upload bean
	// representing the file we previously uploaded.
	final FileUploadBean fileBean = this.getFileUploadBean();
	return fileBean != null ? fileBean.getFile() : null;
    }

    public void setSchemaFile(File f)
    {
	// we also need to keep the file upload bean in sync
	final FileUploadBean fileBean = this.getFileUploadBean();
	if (fileBean != null)
	    fileBean.setFile(f);
    }
    /**
     * @return Returns the schema file or <tt>null</tt>
     */
    public String getSchemaFileName()
    {
	// try and retrieve the file and filename from the file upload bean
	// representing the file we previously uploaded.
	return getFileName();
    }

    public void setSchemaFileName(String s)
    {
	throw new UnsupportedOperationException();
    }

    public String getFormURL()
    {
	try
        {
	    final TemplatingService ts = TemplatingService.getInstance();
	    final String rootTagName = 
		this.getSchemaFileName().replaceAll("([^\\.])\\..+", "$1");
	    final Document d = ts.parseXML(this.getSchemaFile());
	    this.tt = ts.newTemplateType(rootTagName, d);
	    final TemplateInputMethod tim = tt.getInputMethods()[0];
	    return tim.getInputURL(tt.getSampleXml(rootTagName), tt);
	}
	catch (Throwable t)
	{
	    t.printStackTrace();
	    return null;
	}
    }

    public String getSchemaFormURL()
    {
	try
        {
	    final TemplatingService ts = TemplatingService.getInstance();
	    final String rootTagName = 
		this.getSchemaFileName().replaceAll("([^\\.])\\..+", "$1");
	    final Document d = ts.parseXML(this.getSchemaFile());
	    this.tt = ts.newTemplateType(rootTagName, d);
	    final TemplateInputMethod tim = tt.getInputMethods()[0];
	    return tim.getSchemaInputURL(tt);
	}
	catch (Throwable t)
	{
	    t.printStackTrace();
	    return null;
	}
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
   
   // ------------------------------------------------------------------------------
   // Service Injection

   
   // ------------------------------------------------------------------------------
   // Helper Methods
   
   /**
    */
    protected void clearUpload()
    {
      // remove the file upload bean from the session
	FacesContext ctx = FacesContext.getCurrentInstance();
	FileUploadBean fileBean = (FileUploadBean)ctx.getExternalContext().getSessionMap().
	    get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
	if (fileBean != null)
	    fileBean.setFile(null);
    }

}
