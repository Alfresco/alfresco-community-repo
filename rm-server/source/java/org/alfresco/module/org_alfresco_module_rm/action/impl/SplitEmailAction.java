/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.FileCopyUtils;

/**
 * Split Email Action
 * 
 * Splits the attachments for an email message out to independent records.
 * 
 * @author Mark Rogers
 */
public class SplitEmailAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_NO_READ_MIME_MESSAGE = "rm.action.no-read-mime-message";
    private static final String MSG_EMAIL_DECLARED = "rm.action.email-declared";
    private static final String MSG_EMAIL_NOT_RECORD = "rm.action.email-not-record";
    private static final String MSG_EMAIL_CREATE_CHILD_ASSOC = "rm.action.email-create-child-assoc";
    
    /** Logger */
    private static Log logger = LogFactory.getLog(SplitEmailAction.class);

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // get node type
        nodeService.getType(actionedUponNodeRef);
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("split email:" + actionedUponNodeRef);
        }

        if (recordsManagementService.isRecord(actionedUponNodeRef) == true)
        {
            if (recordsManagementService.isRecordDeclared(actionedUponNodeRef) == false)
            {
                ChildAssociationRef parent = nodeService.getPrimaryParent(actionedUponNodeRef);
                
                /** 
                 * Check whether the email message has already been split - do nothing if it has already been split
                 */
                List<AssociationRef> refs = nodeService.getTargetAssocs(actionedUponNodeRef, ImapModel.ASSOC_IMAP_ATTACHMENT);
                if(refs.size() > 0)
                {
                    if (logger.isDebugEnabled() == true)
                    {
                        logger.debug("mail message has already been split - do nothing");
                    }
                    return;
                }
                
                /**
                 * Get the content and if its a mime message then create atachments for each part
                 */
                try
                {
                    ContentReader reader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
                    InputStream is = reader.getContentInputStream();
                    MimeMessage mimeMessage = new MimeMessage(null, is);
                    Object content = mimeMessage.getContent();
                    if (content instanceof Multipart)
                    {
                        Multipart multipart = (Multipart)content;

                        for (int i = 0, n = multipart.getCount(); i < n; i++)
                        {
                            Part part = multipart.getBodyPart(i);
                            if ("attachment".equalsIgnoreCase(part.getDisposition()))
                            {
                                createAttachment(actionedUponNodeRef, parent.getParentRef(), part);
                            }
                        }
                    } 
                } 
                catch (Exception e)
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NO_READ_MIME_MESSAGE, e.toString()), e);
                }                
           }
            else
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EMAIL_DECLARED, actionedUponNodeRef.toString()));
            }
        }
        else
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EMAIL_NOT_RECORD, actionedUponNodeRef.toString()));
        }
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        if (recordsManagementService.isRecord(filePlanComponent) == true)
        {
            if (recordsManagementService.isRecordDeclared(filePlanComponent))
            {
                if (throwException)
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EMAIL_DECLARED, filePlanComponent.toString()));
                }     
                else
                {
                    return false;
                }
            }
        }
        else
        {
            if (throwException)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EMAIL_NOT_RECORD, filePlanComponent.toString()));
            }
            else
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Create attachment from Mime Message Part
     * @param messageNodeRef - the node ref of the mime message
     * @param parentNodeRef - the node ref of the parent folder
     * @param part
     * @throws MessagingException
     * @throws IOException
     */
    private void createAttachment(NodeRef messageNodeRef, NodeRef parentNodeRef, Part part) throws MessagingException, IOException
    {
        String fileName = part.getFileName();
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
        
        Map<QName, Serializable> messageProperties = nodeService.getProperties(messageNodeRef);
        String messageTitle = (String)messageProperties.get(ContentModel.PROP_NAME);
        if(messageTitle == null)
        {
            messageTitle = fileName;
        }
        else
        {
            messageTitle = messageTitle + " - " + fileName; 
        }

        ContentType contentType = new ContentType(part.getContentType());
  
        Map<QName, Serializable> docProps = new HashMap<QName, Serializable>(1);
        docProps.put(ContentModel.PROP_NAME, messageTitle + " - " + fileName);
        docProps.put(ContentModel.PROP_TITLE, fileName);
        
        /**
         * Create an attachment node in the same folder as the message
         */
        ChildAssociationRef attachmentRef = nodeService.createNode(parentNodeRef, 
                        ContentModel.ASSOC_CONTAINS, 
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName), 
                        ContentModel.TYPE_CONTENT,
                        docProps);
        
        /**
         * Write the content into the new attachment node
         */
        ContentWriter writer = contentService.getWriter(attachmentRef.getChildRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype(contentType.getBaseType());
        OutputStream os = writer.getContentOutputStream();
        FileCopyUtils.copy(part.getInputStream(), os);
        
        /**
         * Create a link from the message to the attachment
         */       
        createRMReference(messageNodeRef, attachmentRef.getChildRef());
                
        
    }
    
    QName assocDef = null;
    
    /**
     * Create a link from the message to the attachment
     */       
    private void createRMReference(NodeRef parentRef, NodeRef childRef)
    {
        String sourceId = "message";
        String targetId = "attachment";
        
        String compoundId = recordsManagementAdminService.getCompoundIdFor(sourceId, targetId);
        
        Map<QName, AssociationDefinition> refs = recordsManagementAdminService.getCustomReferenceDefinitions();
        for(QName name : refs.keySet())
        {
            // TODO how to find assocDef?    
            // Refs seems to be null
        }  
        
        if(assocDef == null)
        {
           assocDef = createReference();
        }

        recordsManagementAdminService.addCustomReference(parentRef, childRef, assocDef);      

        // add the IMAP attachment aspect
        nodeService.createAssociation(
                parentRef,
                childRef,
                ImapModel.ASSOC_IMAP_ATTACHMENT);
    }
    
    /**
     * Create the custom reference - need to jump through hoops with the transaction handling here 
     * since the association is created in the post commit phase, so it can't be used within the 
     * current transaction.
     *
     * @return
     */
    private QName createReference()
    {
        UserTransaction txn = null;
        
        try
        {
            txn = transactionService.getNonPropagatingUserTransaction();
            txn.begin();
            RetryingTransactionHelper helper = transactionService.getRetryingTransactionHelper();
            RetryingTransactionCallback<QName> addCustomChildAssocDefinitionCallback = new RetryingTransactionCallback<QName>()
            {
                public QName execute() throws Throwable
                {
                    String sourceId = "message";
                    String targetId = "attachment";
                    QName assocDef = recordsManagementAdminService.addCustomChildAssocDefinition(sourceId, targetId);
                    return assocDef;
                }
            };
            QName ret = helper.doInTransaction(addCustomChildAssocDefinitionCallback);
 
            txn.commit();
            return ret;
        }
        catch (Exception e)
        {
            if(txn != null)
            {
                try 
                {
                    txn.rollback();
                }
                catch (Exception se)
                {
                    logger.error("error during creation of custom child association", se);
                    // we can do nothing with this rollback exception.
                }
            }
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EMAIL_CREATE_CHILD_ASSOC), e);
        }
    }
}
