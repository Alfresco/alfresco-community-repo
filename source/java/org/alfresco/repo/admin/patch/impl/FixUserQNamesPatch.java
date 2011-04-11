/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Gives user store entries unique qnames to allow fast database lookup of local authentication information.
 * 
 * @author David Ward
 * @since 3.3.5
 */
public class FixUserQNamesPatch extends AbstractPatch implements ApplicationEventPublisherAware
{
    private static final Log logger = LogFactory.getLog(FixUserQNamesPatch.class);
    private static final String MSG_SUCCESS = "patch.fixUserQNames.result";

    private QNameDAO qnameDAO;
    private RuleService ruleService;
    private ImporterBootstrap userBootstrap;
    private ApplicationEventPublisher applicationEventPublisher;

    public FixUserQNamesPatch()
    {
    }

    /**
     * @param qnameDAO
     *            resolved QNames
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * @param ruleService
     *            the rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    public void setUserBootstrap(ImporterBootstrap userBootstrap)
    {
        this.userBootstrap = userBootstrap;
    }
    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context
     * .ApplicationEventPublisher)
     */
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(qnameDAO, "qnameDAO");
        checkPropertyNotNull(userBootstrap, "userBootstrap");
        checkPropertyNotNull(applicationEventPublisher, "applicationEventPublisher");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // Get the ChildAssociationRefs
        List<ChildAssociationRef> toProcess = nodeService.getChildAssocs(getUserFolderLocation(), ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_USER,
                    false);
        BatchProcessor<ChildAssociationRef> batchProcessor = new BatchProcessor<ChildAssociationRef>(
                "FixUserQNamesPatch", this.transactionService.getRetryingTransactionHelper(), toProcess, 2, 20,
                this.applicationEventPublisher, logger, 1000);

        int updated = batchProcessor.process(new BatchProcessWorker<ChildAssociationRef>()
        {
            public void beforeProcess() throws Throwable
            {
                // Disable rules
                ruleService.disableRules();
            }

            public void afterProcess() throws Throwable
            {
                // Enable rules
                ruleService.enableRules();
            }

            public String getIdentifier(ChildAssociationRef entry)
            {
                return entry.getChildRef().toString();
            }

            public void process(ChildAssociationRef entry) throws Throwable
            {
                QName userQName = QName.createQName(ContentModel.USER_MODEL_URI, (String) nodeService.getProperty(entry
                        .getChildRef(), ContentModel.PROP_USER_USERNAME));

                // Only a user called "user" will stay in place
                if (!userQName.equals(ContentModel.TYPE_USER))
                {
                    nodeService.moveNode(entry.getChildRef(), entry.getParentRef(), entry.getTypeQName(), userQName);
                }
            }
        }, true);
        return I18NUtil.getMessage(MSG_SUCCESS, updated);
    }

    private NodeRef getUserFolderLocation()
    {
        NodeRef rootNode = this.nodeService.getRootNode(this.userBootstrap.getStoreRef());
        QName qnameAssocSystem = QName.createQName("sys", "system", namespaceService);
        QName qnameAssocUsers = QName.createQName("sys", "people", namespaceService);
        List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNode, RegexQNamePattern.MATCH_ALL,
                qnameAssocSystem);
        NodeRef sysNodeRef = null;
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Required authority system folder path not found: "
                    + qnameAssocSystem);
        }
        else
        {
            sysNodeRef = results.get(0).getChildRef();
        }
        results = nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocUsers);
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Required user folder path not found: " + qnameAssocUsers);
        }
        else
        {
            return results.get(0).getChildRef();
        }
    }
}
