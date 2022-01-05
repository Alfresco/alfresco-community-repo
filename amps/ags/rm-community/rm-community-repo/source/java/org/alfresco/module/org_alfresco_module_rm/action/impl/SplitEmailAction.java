/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
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

    /** Relationship Labels */
    private static final String REL_FROM = "Message";
    private static final String REL_TO = "Attachment";

    /** Logger */
    private static Log logger = LogFactory.getLog(SplitEmailAction.class);

    /** Relationship service */
    private RelationshipService relationshipService;

    /**
     * Gets the relationship service instance
     *
     * @return The relationship service instance
     */
    protected RelationshipService getRelationshipService()
    {
        return this.relationshipService;
    }

    /**
     * Sets the relationship service instance
     *
     * @param relationshipService The relationship service instance
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    /** Unique name of the relationship definition */
    private String relationshipUniqueName;

    public void bootstrap()
    {
        Set<RelationshipDefinition> relationshipDefinitions = getRelationshipService().getRelationshipDefinitions();
        for (RelationshipDefinition relationshipDefinition : relationshipDefinitions)
        {
            RelationshipDisplayName displayName = relationshipDefinition.getDisplayName();
            String sourceText = displayName.getSourceText();
            String targetText = displayName.getTargetText();

            if (sourceText.equals(REL_FROM) && targetText.equals(REL_TO))
            {
                relationshipUniqueName = relationshipDefinition.getUniqueName();
            }
        }

        if (isBlank(relationshipUniqueName))
        {
            RelationshipDisplayName displayName = new RelationshipDisplayName(REL_FROM, REL_TO);
            RelationshipDefinition relationshipDefinition = getRelationshipService().createRelationshipDefinition(displayName);
            relationshipUniqueName = relationshipDefinition.getUniqueName();
        }
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // get node type
        getNodeService().getType(actionedUponNodeRef);

        if (logger.isDebugEnabled())
        {
            logger.debug("split email:" + actionedUponNodeRef);
        }

        if (getRecordService().isRecord(actionedUponNodeRef))
        {
            if (!getRecordService().isDeclared(actionedUponNodeRef))
            {
                ChildAssociationRef parent = getNodeService().getPrimaryParent(actionedUponNodeRef);

                /**
                 * Check whether the email message has already been split - do nothing if it has already been split
                 */
                List<AssociationRef> refs = getNodeService().getTargetAssocs(actionedUponNodeRef, ImapModel.ASSOC_IMAP_ATTACHMENT);
                if(refs.size() > 0)
                {
                    if (logger.isDebugEnabled())
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
                    ContentReader reader = getContentService().getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
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

        Map<QName, Serializable> messageProperties = getNodeService().getProperties(messageNodeRef);
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

        Map<QName, Serializable> docProps = new HashMap<>(1);
        docProps.put(ContentModel.PROP_NAME, messageTitle + " - " + fileName);
        docProps.put(ContentModel.PROP_TITLE, fileName);

        /**
         * Create an attachment node in the same folder as the message
         */
        ChildAssociationRef attachmentRef = getNodeService().createNode(parentNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName),
                        ContentModel.TYPE_CONTENT,
                        docProps);

        /**
         * Write the content into the new attachment node
         */
        ContentWriter writer = getContentService().getWriter(attachmentRef.getChildRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype(contentType.getBaseType());
        OutputStream os = writer.getContentOutputStream();
        FileCopyUtils.copy(part.getInputStream(), os);

        /**
         * Create a link from the message to the attachment
         */
        createRMReference(messageNodeRef, attachmentRef.getChildRef());


    }

    /**
     * Create a link from the message to the attachment
     */
    private void createRMReference(final NodeRef parentRef, final NodeRef childRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // add the relationship
                getRelationshipService().addRelationship(relationshipUniqueName, parentRef, childRef);

                // add the IMAP attachment aspect
                getNodeService().createAssociation(
                        parentRef,
                        childRef,
                        ImapModel.ASSOC_IMAP_ATTACHMENT);

                return null;
            }
        });
    }
}
