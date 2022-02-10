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

package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * rma:filePlan behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:filePlan"
)
public class FilePlanType extends    BaseBehaviourBean
                          implements NodeServicePolicies.OnCreateChildAssociationPolicy,
                                     NodeServicePolicies.OnCreateNodePolicy,
                                     NodeServicePolicies.OnDeleteNodePolicy,
                                     NodeServicePolicies.BeforeDeleteNodePolicy
{
    private final static List<QName> ACCEPTED_UNIQUE_CHILD_TYPES = Arrays.asList(TYPE_HOLD_CONTAINER, TYPE_TRANSFER_CONTAINER, TYPE_UNFILED_RECORD_CONTAINER);
    private final static List<QName> ACCEPTED_NON_UNIQUE_CHILD_TYPES = Arrays.asList(TYPE_RECORD_CATEGORY);
    private static final String BEHAVIOUR_NAME = "onDeleteFilePlan";

    /** file plan service */
    private FilePlanService filePlanService;

    /** record folder service */
    private RecordFolderService recordFolderService;

    /** identifier service */
    private IdentifierService identifierService;

    /** file plan role service */
    private FilePlanRoleService filePlanRoleService;

    /**
     * Unfiled Record Container Type behaviour bean
     */
    private UnfiledRecordContainerType unfiledRecordContainerType;

    /**
     * Transfer Container Type behaviour bean
     */
    private TransferContainerType transferContainerType;

    /**
     * Hold Container Type behaviour bean
     */
    private HoldContainerType holdContainerType;

    /**
     * @return File plan service
     */
    protected FilePlanService getFilePlanService()
    {
        return this.filePlanService;
    }

    /**
     * @return Record folder service
     */
    protected RecordFolderService getRecordFolderService()
    {
        return this.recordFolderService;
    }

    /**
     * @return Identifier service
     */
    protected IdentifierService getIdentifierService()
    {
        return this.identifierService;
    }

    /**
     * @return File plan role service
     */
    protected FilePlanRoleService getFilePlanRoleService()
    {
        return this.filePlanRoleService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param recordFolderService   record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param identifierService identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }

    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * @param unfiledRecordContainerType - unfiled record container type behaviour bean
     */
    public void setUnfiledRecordContainerType(UnfiledRecordContainerType unfiledRecordContainerType)
    {
        this.unfiledRecordContainerType = unfiledRecordContainerType;
    }

    /**
     * @param transferContainerType - transfer container type behaviour bean
     */
    public void setTransferContainerType(TransferContainerType transferContainerType)
    {
        this.transferContainerType = transferContainerType;
    }

    /**
     * @param holdContainerType - hold container type behaviour bean
     */
    public void setHoldContainerType(HoldContainerType holdContainerType)
    {
        this.holdContainerType = holdContainerType;
    }

    /**
     * Disable the behaviours for this transaction
     *
     */
    public void disable()
    {
        getBehaviour(BEHAVIOUR_NAME).disable();
    }

    /**
     * Enable behaviours for this transaction
     *
     */
    public void enable()
    {
        getBehaviour(BEHAVIOUR_NAME).enable();
    }


    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Behaviour
    (
       kind = BehaviourKind.ASSOCIATION
    )
    @Override
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean bNew)
    {
        // We need to automatically cast the created folder to category if it is a plain folder
        // This occurs if the RM folder has been created via IMap, WebDav, etc. Don't check subtypes.
        // Some modules use hidden files to store information (see RM-3283)
        if (nodeService.getType(childAssocRef.getChildRef()).equals(ContentModel.TYPE_FOLDER))
        {
            nodeService.setType(childAssocRef.getChildRef(), TYPE_RECORD_CATEGORY);
        }

        // check the created child is of an accepted type
        validateNewChildAssociation(childAssocRef.getParentRef(), childAssocRef.getChildRef(), ACCEPTED_UNIQUE_CHILD_TYPES, ACCEPTED_NON_UNIQUE_CHILD_TYPES);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    @Override
    public void onCreateNode(final ChildAssociationRef childAssocRef)
    {
        final NodeRef filePlan = childAssocRef.getChildRef();

        AuthenticationUtil.runAsSystem(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                // ensure rules are not inherited
                nodeService.addAspect(filePlan, RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);

                // set the identifier
                if (nodeService.getProperty(filePlan, PROP_IDENTIFIER) == null)
                {
                    String id = getIdentifierService().generateIdentifier(filePlan);
                    nodeService.setProperty(filePlan, RecordsManagementModel.PROP_IDENTIFIER, id);
                }

                return null;
            }
        });

        // setup the file plan roles
        getFilePlanRoleService().setupFilePlanRoles(filePlan);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnDeleteNodePolicy#onDeleteNode(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.FIRST_EVENT,
       name = BEHAVIOUR_NAME
    )
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean archived)
    {
        unfiledRecordContainerType.enable();
        transferContainerType.enable();
        holdContainerType.enable();
        throw new IntegrityException("Operation failed. Deletion of File Plan is not allowed.", null);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        unfiledRecordContainerType.disable();
        transferContainerType.disable();
        holdContainerType.disable();
    }

    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       policy = "alf:onDeleteNode",
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onDeleteNodeOnCommit(ChildAssociationRef childAssocRef, boolean archived)
    {
        // tear down the file plan roles
        getFilePlanRoleService().tearDownFilePlanRoles(childAssocRef.getChildRef());
        unfiledRecordContainerType.enable();
        transferContainerType.enable();
        holdContainerType.enable();
    }
}
