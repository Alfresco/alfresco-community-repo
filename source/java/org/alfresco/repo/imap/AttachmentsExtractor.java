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
package org.alfresco.repo.imap;
 
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
 
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
 
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.config.RepositoryFolderConfigBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.FileCopyUtils;
 
/**
 * Extract attachments according to provided AttachmentsExtractorMode
 * 
 * @since 3.4.7
 */
public class AttachmentsExtractor
{
   public static enum AttachmentsExtractorMode
   {
       /**
         * Attachments will be extracted to the same folder where email lies.
         */
       SAME,
       /**
         * All attachments for all emails will be extracted to one folder.
         */
       COMMON,
       /**
         * All attachments for each email will be extracted to separate folder.
         */
       SEPARATE
   }
 
   private Log logger = LogFactory.getLog(AttachmentsExtractor.class);
 
   private FileFolderService fileFolderService;
   private NodeService nodeService;
    private ImapService imapService;
   private ServiceRegistry serviceRegistry;
   private RepositoryFolderConfigBean attachmentsFolder;
   private NodeRef attachmentsFolderRef;
   private AttachmentsExtractorMode attachmentsExtractorMode;
 
   public void setFileFolderService(FileFolderService fileFolderService)
   {
       this.fileFolderService = fileFolderService;
   }
 
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
    }

    public void setImapService(ImapService imapService)
    {
        this.imapService = imapService;
   }
 
   public void setAttachmentsFolder(RepositoryFolderConfigBean attachmentsFolder)
   {
       this.attachmentsFolder = attachmentsFolder;
   }
 
   public void setServiceRegistry(ServiceRegistry serviceRegistry)
   {
       this.serviceRegistry = serviceRegistry;
   }
 
   public void setAttachmentsExtractorMode(String attachmentsExtractorMode)
   {
       this.attachmentsExtractorMode = AttachmentsExtractorMode.valueOf(attachmentsExtractorMode);
   }
 
   public void init()
   {
       attachmentsFolderRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
       {
           public NodeRef doWork() throws Exception
           {
               NodeRef attFolderRef = attachmentsFolder.getOrCreateFolderPath(serviceRegistry.getNamespaceService(), nodeService, serviceRegistry.getSearchService(), fileFolderService);
               serviceRegistry.getPermissionService().setPermission(attFolderRef , PermissionService.ALL_AUTHORITIES, PermissionService.FULL_CONTROL, true);
               return attFolderRef;
           }
       }, AuthenticationUtil.getSystemUserName());
   }
 
   public void extractAttachments(NodeRef messageRef, MimeMessage originalMessage) throws IOException, MessagingException
   {
       NodeRef attachmentsFolderRef = null;
       switch (attachmentsExtractorMode)
       {
       case SAME:
           attachmentsFolderRef = nodeService.getPrimaryParent(messageRef).getParentRef();
           break;
       case COMMON:
           attachmentsFolderRef = this.attachmentsFolderRef;
           break;
       case SEPARATE:
       default:
           NodeRef parentFolder = nodeService.getPrimaryParent(messageRef).getParentRef();
           String messageName = (String) nodeService.getProperty(messageRef, ContentModel.PROP_NAME);
           String attachmentsFolderName = messageName + "-attachments";
           attachmentsFolderRef = fileFolderService.create(parentFolder, attachmentsFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
           break;
       }
 
       nodeService.createAssociation(messageRef, attachmentsFolderRef, ImapModel.ASSOC_IMAP_ATTACHMENTS_FOLDER);
 
       Object content = originalMessage.getContent();
       if (content instanceof Multipart)
       {
           Multipart multipart = (Multipart) content;
 
           for (int i = 0, n = multipart.getCount(); i < n; i++)
           {
               Part part = multipart.getBodyPart(i);
               if ("attachment".equalsIgnoreCase(part.getDisposition()))
               {
                   createAttachment(messageRef, attachmentsFolderRef, part);
               }
           }
       }
 
   }
 
   private void createAttachment(NodeRef messageFile, NodeRef attachmentsFolderRef, Part part) throws MessagingException, IOException
   {
       String fileName = part.getFileName();
       if (fileName == null || fileName.isEmpty())
       {
    	   fileName = "unnamed";
       }
       try
       {
           fileName = MimeUtility.decodeText(fileName);
       }
       catch (UnsupportedEncodingException e)
       {
           if (logger.isWarnEnabled())
           {
               logger.warn("Cannot decode file name '" + fileName + "'", e);
           }
       }
 
       ContentType contentType = new ContentType(part.getContentType());
       NodeRef attachmentFile = fileFolderService.searchSimple(attachmentsFolderRef, fileName);
       // The one possible behaviour
       /*
        if (result.size() > 0)
        {
            for (FileInfo fi : result)
            {
                fileFolderService.delete(fi.getNodeRef());
            }
        }
        */
       // And another one behaviour which will overwrite the content of the existing file. It is performance preferable.
       if (attachmentFile == null)
       {
           FileInfo createdFile = fileFolderService.create(attachmentsFolderRef, fileName, ContentModel.TYPE_CONTENT);
           nodeService.createAssociation(messageFile, createdFile.getNodeRef(), ImapModel.ASSOC_IMAP_ATTACHMENT);
           attachmentFile = createdFile.getNodeRef();
       }
       else
       {

            
            String newFileName = imapService.generateUniqueFilename(attachmentsFolderRef, fileName); 
            
            FileInfo createdFile = fileFolderService.create(attachmentsFolderRef, newFileName, ContentModel.TYPE_CONTENT);
           nodeService.createAssociation(messageFile, createdFile.getNodeRef(), ImapModel.ASSOC_IMAP_ATTACHMENT);
           attachmentFile = createdFile.getNodeRef();
 
       }
       
       nodeService.setProperty(attachmentFile, ContentModel.PROP_DESCRIPTION, nodeService.getProperty(messageFile, ContentModel.PROP_NAME));
 
       ContentWriter writer = fileFolderService.getWriter(attachmentFile);
       writer.setMimetype(contentType.getBaseType());
       OutputStream os = writer.getContentOutputStream();
       FileCopyUtils.copy(part.getInputStream(), os);
   }
 
}