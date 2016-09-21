/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StringUtils;

/**
 * Records management action executer base class
 *
 * @author Roy Wetherall
 */
public abstract class RMActionExecuterAbstractBase  extends PropertySubActionExecuterAbstractBase
                                                    implements RecordsManagementAction,
                                                               RecordsManagementModel,
                                                               BeanNameAware
{
    /** Namespace service */
    private NamespaceService namespaceService;

    /** Used to control transactional behaviour including post-commit auditing */
    private TransactionService transactionService;

    /** Node service */
    private NodeService nodeService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Content service */
    private ContentService contentService;

    /** Action service */
    private ActionService actionService;

    /** Records management action service */
    private RecordsManagementAuditService recordsManagementAuditService;

    /** Records management action service */
    private RecordsManagementActionService recordsManagementActionService;

    /** Record service */
    private RecordService recordService;

    /** Disposition service */
    private DispositionService dispositionService;

    /** Vital record service */
    private VitalRecordService vitalRecordService;

    /** Records management event service */
    private RecordsManagementEventService recordsManagementEventService;

    /** Records management action service */
    private RecordsManagementAdminService recordsManagementAdminService;

    /** Ownable service **/
    private OwnableService ownableService;

    /** Freeze service */
    private FreezeService freezeService;

    /** Model security service */
    private ModelSecurityService modelSecurityService;

    /** Record folder service */
    private RecordFolderService recordFolderService;

    /** Hold service */
    private HoldService holdService;

    /** List of kinds for which this action is applicable */
    protected Set<FilePlanComponentKind> applicableKinds = new HashSet<FilePlanComponentKind>();

    /**
     * Get the transaction service
     */
    protected TransactionService getTransactionService()
    {
        return this.transactionService;
    }

    /**
     * Set the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Gets the namespace service
     */
    protected NamespaceService getNamespaceService()
    {
        return this.namespaceService;
    }

    /**
     * Set the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Gets the node service
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * Set node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Gets the dictionary service
     */
    protected DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
    }

    /**
     * Set the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Gets the content service
     */
    protected ContentService getContentService()
    {
        return this.contentService;
    }

    /**
     * Set the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Gets the action service
     */
    protected ActionService getActionService()
    {
        return this.actionService;
    }

    /**
     * Set action service
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Gets the records management audit service
     */
    protected RecordsManagementAuditService getRecordsManagementAuditService()
    {
        return this.recordsManagementAuditService;
    }

    /**
     * Set the audit service that action details will be sent to
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
    }

    /**
     * Gets the records management action service
     */
    protected RecordsManagementActionService getRecordsManagementActionService()
    {
        return this.recordsManagementActionService;
    }

    /**
     * Set records management service
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    /**
     * Gets the disposition service
     */
    protected DispositionService getDispositionService()
    {
        return this.dispositionService;
    }

    /**
     * Set the disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * Gets the vital record service
     */
    protected VitalRecordService getVitalRecordService()
    {
        return this.vitalRecordService;
    }

    /**
     * @param vitalRecordService    vital record service
     */
    public void setVitalRecordService(VitalRecordService vitalRecordService)
    {
        this.vitalRecordService = vitalRecordService;
    }

    /**
     * Gets the records management event service
     */
    protected RecordsManagementEventService getRecordsManagementEventService()
    {
        return this.recordsManagementEventService;
    }

    /**
     * Set records management event service
     */
    public void setRecordsManagementEventService(RecordsManagementEventService recordsManagementEventService)
    {
        this.recordsManagementEventService = recordsManagementEventService;
    }

    /**
     * Gets the ownable service
     */
    protected OwnableService getOwnableService()
    {
        return this.ownableService;
    }

    /**
     * Set the ownable service
     * @param ownableSerice
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * Gets the freeze service
     */
    protected FreezeService getFreezeService()
    {
        return this.freezeService;
    }

    /**
     * Set freeze service
     *
     * @param freezeService freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
    }

    /**
     * Gets the record service
     */
    protected RecordService getRecordService()
    {
        return this.recordService;
    }

    /**
     * Set record service
     *
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @return  records management admin service
     */
    protected RecordsManagementAdminService getRecordsManagementAdminService()
    {
        return recordsManagementAdminService;
    }

    /**
     * @param recordsManagementAdminService records management admin service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService recordsManagementAdminService)
    {
        this.recordsManagementAdminService = recordsManagementAdminService;
    }

    /**
     * Gets the model security service
     */
    protected ModelSecurityService getModelSecurityService()
    {
        return this.modelSecurityService;
    }

    /**
     * @param modelSecurityService  model security service
     */
    public void setModelSecurityService(ModelSecurityService modelSecurityService)
    {
        this.modelSecurityService = modelSecurityService;
    }

    /**
     * Gets the record folder service
     */
    protected RecordFolderService getRecordFolderService()
    {
        return this.recordFolderService;
    }

    /**
     * @param recordFolderService record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * Gets the hold service
     */
    protected HoldService getHoldService()
    {
        return this.holdService;
    }

    /**
     * @param holdService hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    /**
     * @param applicableKinds   kinds that this action is applicable for
     */
    public void setApplicableKinds(String[] applicableKinds)
    {
        for(String kind : applicableKinds)
        {
            this.applicableKinds.add(FilePlanComponentKind.valueOf(kind));
        }
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#createActionDefinition(java.lang.String)
     */
    @Override
    protected ActionDefinition createActionDefinition(String name)
    {
        return new RecordsManagementActionDefinitionImpl(name);
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#getActionDefinition()
     */
    @Override
    public ActionDefinition getActionDefinition()
    {
        ActionDefinition actionDefinition = super.getActionDefinition();
        ((RecordsManagementActionDefinitionImpl)this.actionDefinition).setApplicableKinds(applicableKinds);
        return actionDefinition;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getRecordsManagementActionDefinition()
     */
    @Override
    public RecordsManagementActionDefinition getRecordsManagementActionDefinition()
    {
        return (RecordsManagementActionDefinition)getActionDefinition();
    }

    /**
     * Init method
     */
    @Override
    public void init()
    {
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "actionService", actionService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "recordsManagementAuditService", recordsManagementAuditService);
        PropertyCheck.mandatory(this, "recordsManagementActionService", recordsManagementActionService);
        PropertyCheck.mandatory(this, "recordsManagementAdminService", recordsManagementAdminService);
        PropertyCheck.mandatory(this, "recordsManagementEventService", recordsManagementEventService);

        super.init();
    }

    /**
     * Indicates whether this records management action is public or not
     *
     * @return  boolean true if public, false otherwise
     */
    @Override
    public boolean isPublicAction()
    {
        return publicAction;
    }

    /**
     * @see org.alfresco.repo.action.CommonResourceAbstractBase#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAction#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getLabel()
     */
    public String getLabel()
    {
        String label = I18NUtil.getMessage(this.getTitleKey());

        if (label == null)
        {
            // default to the name of the action with first letter capitalised
            label = StringUtils.capitalize(this.name);
        }

        return label;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction#getDescription()
     */
    public String getDescription()
    {
        String desc = I18NUtil.getMessage(this.getDescriptionKey());

        if (desc == null)
        {
            // default to the name of the action with first letter capitalised
            desc = StringUtils.capitalize(this.name);
        }

        return desc;
    }

    /**
     * By default an action is not a disposition action
     *
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAction#isDispositionAction()
     */
    public boolean isDispositionAction()
    {
        return false;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAction#execute(org.alfresco.service.cmr.repository.NodeRef, java.util.Map)
     */
    public RecordsManagementActionResult execute(NodeRef filePlanComponent, Map<String, Serializable> parameters)
    {
        // Create the action
        Action action = this.actionService.createAction(name);
        action.setParameterValues(parameters);

        // disable model security whilst we execute the RM rule
        modelSecurityService.disable();
        try
        {
            // Execute the action
            actionService.executeAction(action, filePlanComponent);
        }
        finally
        {
            modelSecurityService.enable();
        }

        // Get the result
        Object value = action.getParameterValue(ActionExecuterAbstractBase.PARAM_RESULT);
        return new RecordsManagementActionResult(value);
    }

    /**
     * Function to pad a string with zero '0' characters to the required length
     *
     * @param s     String to pad with leading zero '0' characters
     * @param len   Length to pad to
     *
     * @return padded string or the original if already at >=len characters
     *
     * @deprecated As of 2.1, replaced by {@link org.apache.commons.lang.StringUtils.leftPad}
     */
    @Deprecated
    protected String padString(String s, int len)
    {
       String result = s;
       for (int i=0; i<(len - s.length()); i++)
       {
           result = "0" + result;
       }
       return result;
    }

    /**
     * By default there are no parameters.
     *
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // No parameters
    }

    /**
     * By default, rmActions do not provide an implicit target nodeRef.
     */
    public NodeRef getImplicitTargetNodeRef()
    {
        return null;
    }
}
