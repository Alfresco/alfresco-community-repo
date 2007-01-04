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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.zip.ZipException;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

/**
 * Backing bean for the Import Website Content dialog.
 * 
 * This dialog manages the upload of a ZIP archive file, which is then unpacked and loaded into
 * the AVM store with the complete folder and file structure.
 * 
 * @author Kevin Roast
 */
public class ImportWebsiteDialog
{
   private static final int BUFFER_SIZE = 16384;
   
   protected File file;
   protected String fileName;
   protected boolean isFinished = false;
   
   protected FileFolderService fileFolderService;
   protected ContentService contentService;
   protected AVMBrowseBean avmBrowseBean;
   protected AVMService avmService;
   protected NodeService nodeService;
   
   
   /**
    * @param contentService      The ContentService to set.
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }

   /**
    * @param fileFolderService   The FileFolderService to set.
    */
   public void setFileFolderService(FileFolderService fileFolderService)
   {
      this.fileFolderService = fileFolderService;
   }
   
   /**
    * @param avmBrowseBean       The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param avmService          The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param nodeService         The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @return Returns the name of the file
    */
   public String getFileName()
   {
      // try and retrieve the file and filename from the file upload bean
      // representing the file we previously uploaded.
      FacesContext ctx = FacesContext.getCurrentInstance();
      FileUploadBean fileBean = (FileUploadBean)ctx.getExternalContext().getSessionMap().
         get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
      if (fileBean != null)
      {
         this.file = fileBean.getFile();
         this.fileName = fileBean.getFileName();
      }
      
      return this.fileName;
   }

   /**
    * @param fileName The name of the file
    */
   public void setFileName(String fileName)
   {
      this.fileName = fileName;
      
      // we also need to keep the file upload bean in sync
      FacesContext ctx = FacesContext.getCurrentInstance();
      FileUploadBean fileBean = (FileUploadBean)ctx.getExternalContext().getSessionMap().
         get(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
      if (fileBean != null)
      {
         fileBean.setFileName(this.fileName);
      }
   }
   
   public boolean getFinishButtonDisabled()
   {
      return (this.fileName == null || this.fileName.length() == 0);
   }
   
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Action listener called when the add content dialog is called
    */
   public void start(ActionEvent event)
   {
      clearUpload();
      this.fileName = null;
   }
   
   /**
    * Action handler called when the Finish button is pressed
    */
   public String finish()
   {
      String outcome = null;
      
      // check the isFinished flag to stop the finish button
      // being pressed multiple times
      if (this.isFinished == false)
      {
         this.isFinished = true;
         
         UserTransaction tx = null;
         
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // get the AVM path that will contain the imported content
            String rootPath = this.avmBrowseBean.getCurrentPath();
            
            // convert the AVM path to a NodeRef so we can use the NodeService to perform import
            NodeRef importRef = AVMNodeConverter.ToNodeRef(-1, rootPath);
            processZipImport(this.file, importRef);
            
            // After a bulk import it's a good idea to snapshot the store
            this.avmService.createSnapshot(
                  AVMConstants.getStoreName(rootPath),
                  "Import of file: " + this.fileName, null);
            
            // Reload virtualisation server as required
            AVMConstants.updateVServerWebapp(rootPath, true);
            
            tx.commit();
            
            UIContextService.getInstance(context).notifyBeans();
            
            outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
         }
         catch (Throwable e)
         {
            // rollback the transaction
            try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
            Utils.addErrorMessage(MessageFormat.format(
                  Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), 
                  e.getMessage()), e);
         }
         finally
         {
            // reset the flag so we can re-attempt the operation
            this.isFinished = false;
         }
      }
      
      return outcome;
   }
   
   /**
    * Action handler called when the user wishes to remove an uploaded file
    */
   public String removeUploadedFile()
   {
      clearUpload();
      
      // also clear the file name
      this.fileName = null;
      
      // refresh the current page
      return null;
   }
   
   /**
    * Action handler called when the dialog is cancelled
    */
   public String cancel()
   {
      clearUpload();
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper Methods
   
   /**
    * Deletes the uploaded file and removes the FileUploadBean from the session
    */
   protected void clearUpload()
   {
      // delete the temporary file we uploaded earlier
      if (this.file != null)
      {
         this.file.delete();
      }
      
      this.file = null;
      
      // remove the file upload bean from the session
      FacesContext ctx = FacesContext.getCurrentInstance();
      ctx.getExternalContext().getSessionMap().remove(FileUploadBean.FILE_UPLOAD_BEAN_NAME);
   }

   /**
    * Process ZIP file for import into an AVM repository store location
    *  
    * @param file       ZIP format file
    * @param rootRef    Root reference of the AVM location to import into
    */
   public void processZipImport(File file, NodeRef rootRef)
   {
      try
      {
         // NOTE: This encoding allows us to workaround bug:
         //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
         ZipFile zipFile = new ZipFile(file, "Cp437"); 
         File alfTempDir = TempFileProvider.getTempDir();
         // build a temp dir name based on the name of the file we are importing
         File tempDir = new File(alfTempDir.getPath() + File.separatorChar + file.getName() + "_unpack");
         try
         {
            // TODO: improve this code to directly pipe the zip stream output into the repo objects - 
            //       to remove the need to expand to the filesystem first.
            extractFile(zipFile, tempDir.getPath());
            importDirectory(tempDir.getPath(), rootRef);
         }
         finally
         {
            deleteDir(tempDir);
         }
      }
      catch (IOException e)
      {
         throw new AlfrescoRuntimeException("Unable to process Zip file. File may not be of the expected format.", e);
      }
   }
   
   /**
    * Extract the file and folder structure of a ZIP file into the specified directory
    * 
    * @param archive       The ZIP archive to extract
    * @param extractDir    The directory to extract into
    */
   private void extractFile(ZipFile archive, String extractDir)
   {
      String fileName;
      String destFileName;
      byte[] buffer = new byte[BUFFER_SIZE];
      extractDir = extractDir + File.separator;
      try
      {
         for (Enumeration e = archive.getEntries(); e.hasMoreElements();)
         {
            ZipEntry entry = (ZipEntry) e.nextElement();
            if (!entry.isDirectory())
            {
               fileName = entry.getName();
               fileName = fileName.replace('/', File.separatorChar);
               destFileName = extractDir + fileName;
               File destFile = new File(destFileName);
               String parent = destFile.getParent();
               if (parent != null)
               {
                  File parentFile = new File(parent);
                  if (!parentFile.exists()) parentFile.mkdirs();
               }
               InputStream in = new BufferedInputStream(archive.getInputStream(entry), BUFFER_SIZE);
               OutputStream out = new BufferedOutputStream(new FileOutputStream(destFileName), BUFFER_SIZE);
               int count;
               while ((count = in.read(buffer)) != -1)
               {
                  out.write(buffer, 0, count);
               }
               in.close();
               out.close();
            }
            else
            {
               File newdir = new File(extractDir + entry.getName());
               newdir.mkdir();
            }
         }
      }
      catch (ZipException e)
      {
         throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
      }
      catch (FileNotFoundException e)
      {
         throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
      }
      catch (IOException e)
      {
         throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
      }
   }
   
   /**
    * Recursively import a directory structure into the specified root node
    * 
    * @param dir     The directory of files and folders to import
    * @param root    The root node to import into
    */
   private void importDirectory(String dir, NodeRef root)
   {
      ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      MimetypeService mimetypeService = services.getMimetypeService();
      File topdir = new File(dir);
      for (File file : topdir.listFiles())
      {
         try
         {
            if (file.isFile())
            {
               // Create a file in the AVM store
               String avmPath = AVMNodeConverter.ToAVMVersionPath(root).getSecond();
               String fileName = file.getName();
               this.avmService.createFile(
                     avmPath, fileName,new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE));
               
               // TODO: restore this code once performance is acceptable
               // NodeRef fileRef = AVMNodeConverter.ToNodeRef(-1, filePath);
               //       see AVMBrowseBean.setAVMNodeDescriptor
               // add titled aspect for the read/edit properties screens
               // Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
               // titledProps.put(ContentModel.PROP_TITLE, fileName);
               // this.nodeService.addAspect(fileRef, ContentModel.ASPECT_TITLED, titledProps);
               
               // for now use the avm service directly
               String filePath = avmPath + '/' + fileName;
               this.avmService.addAspect(filePath, ContentModel.ASPECT_TITLED);
               this.avmService.setNodeProperty(filePath, ContentModel.PROP_TITLE,
                                               new PropertyValue(DataTypeDefinition.TEXT, fileName));
               
               // create content node based on the filename
               /*FileInfo contentFile = fileFolderService.create(root, fileName, ContentModel.TYPE_AVM_PLAIN_CONTENT);
               NodeRef content = contentFile.getNodeRef();
               
               InputStream contentStream = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
               
               ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
               writer.setMimetype(mimetypeService.guessMimetype(file.getAbsolutePath()));
               // TODO: what should we set this too? (definitely not Cp437...!)
               //writer.setEncoding("Cp437");
               writer.putContent(contentStream);*/
            }
            else
            {
               //FileInfo fileInfo = fileFolderService.create(root, file.getName(), ContentModel.TYPE_AVM_PLAIN_FOLDER);
               
               // Create a directory in the AVM store 
               String avmPath = AVMNodeConverter.ToAVMVersionPath(root).getSecond();
               this.avmService.createDirectory(avmPath, file.getName());
               
               String folderPath = avmPath + '/' + file.getName();
               NodeRef folderRef = AVMNodeConverter.ToNodeRef(-1, folderPath);
               importDirectory(file.getPath(), folderRef);
               
               // TODO: restore this code once performance is acceptable
               //       see AVMBrowseBean.setAVMNodeDescriptor
               // add the uifacets aspect for the read/edit properties screens
               // this.nodeService.addAspect(folderRef, ContentModel.ASPECT_UIFACETS, null);
               
               // for now use the AVM service directly
               this.avmService.addAspect(folderPath, ApplicationModel.ASPECT_UIFACETS);
            }
         }
         catch (FileNotFoundException e)
         {
            // TODO: add failed file info to status message?
            throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
         }
         catch (FileExistsException e)
         {
            // TODO: add failed file info to status message?
            throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
         }
      }
   }
   
   /**
    * Recursively delete a dir of files and directories
    * 
    * @param dir directory to delete
    */
   private void deleteDir(File dir)
   {
      File elenco = new File(dir.getPath());
      for (File file : elenco.listFiles())
      {
         if (file.isFile()) file.delete();
         else deleteDir(file);
      }
      dir.delete();
   }
}
