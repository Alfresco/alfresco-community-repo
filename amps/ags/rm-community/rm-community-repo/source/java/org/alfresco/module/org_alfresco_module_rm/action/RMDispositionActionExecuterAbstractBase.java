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

package org.alfresco.module.org_alfresco_module_rm.action;

import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Roy Wetherall
 */
public abstract class RMDispositionActionExecuterAbstractBase extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_RECORD_NOT_DECLARED = "rm.action.record-not-declared";
    private static final String MSG_EXPECTED_RECORD_LEVEL = "rm.action.expected-record-level";
    private static final String MSG_NOT_ALL_RECORDS_DECLARED = "rm.action.not-all-records-declared";
    private static final String MSG_NOT_ELIGIBLE = "rm.action.not-eligible";
    private static final String MSG_NO_DISPOITION_INSTRUCTIONS = "rm.action.no-disposition-instructions";
    private static final String MSG_NO_DIS_LIFECYCLE_SET = "rm.action.no-disposition-lisfecycle-set";
    private static final String MSG_NEXT_DISP_NOT_SET = "rm.action.next-disp-not-set";
    private static final String MSG_NOT_NEXT_DISP = "rm.action.not-next-disp";
    private static final String MSG_NOT_RECORD_FOLDER = "rm.action.not-record-folder";

    /** Parameter value indicating whether we should be doing non-error raising state checks */
    public static final String PARAM_NO_ERROR_CHECK = "rm.no-error-check";

    /**
     * All children of this implementation are disposition actions.
     *
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#isDispositionAction()
     */
    @Override
    public boolean isDispositionAction()
    {
        return true;
    }

    /**
     * Indicates whether the disposition is marked complete
     *
     * @return <code>true</code> if marked complete, <code>false</code> otherwise
     */
    public boolean getSetDispositionActionComplete()
    {
        return true;
    }

    /**
     * Indicates whether we should validate the next disposition action is the action we are
     * trying to execute.
     *
     * @return
     */
    protected boolean checkNextDispositionAction(NodeRef actionedUponNodeRef)
    {
        return true;
    }

    /**
     * Indicated whether we should validate the disposition action is eligible or not.
     *
     * @param actionedUponNodeRef
     * @return
     */
    protected boolean checkEligibility(NodeRef actionedUponNodeRef)
    {
        return true;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
    	NodeRef nextDispositionActionNodeRef = getNextDispostionAction(actionedUponNodeRef);

        // determine whether we should be raising errors during state checking or not
    	boolean checkError = true;
    	Boolean checkErrorValue = (Boolean)action.getParameterValue(PARAM_NO_ERROR_CHECK);
    	if (checkErrorValue != null)
    	{
    	    checkError = checkErrorValue.booleanValue();
    	}

        // Check the validity of the action (is it the next action, are we dealing with the correct type of object for
        // the disposition level?
        DispositionSchedule di = checkDispositionActionExecutionValidity(actionedUponNodeRef, nextDispositionActionNodeRef, checkError);
        if (di != null)
        {
            // Check the eligibility of the action
            if (!checkEligibility(actionedUponNodeRef) ||
                    getDispositionService().isNextDispositionActionEligible(actionedUponNodeRef))
            {
                if (di.isRecordLevelDisposition())
                {
                    // Check that we do indeed have a record
                    if (getRecordService().isRecord(actionedUponNodeRef))
                    {
                        // Can only execute disposition action on record if declared
                        if (getRecordService().isDeclared(actionedUponNodeRef))
                        {
                            // Indicate that the disposition action is underway
                            getNodeService().setProperty(nextDispositionActionNodeRef, PROP_DISPOSITION_ACTION_STARTED_AT, new Date());
                            getNodeService().setProperty(nextDispositionActionNodeRef, PROP_DISPOSITION_ACTION_STARTED_BY, AuthenticationUtil.getRunAsUser());

                            // Execute record level disposition
                            executeRecordLevelDisposition(action, actionedUponNodeRef);

                            if (getNodeService().exists(nextDispositionActionNodeRef) &&
                                getSetDispositionActionComplete())
                            {
                                getNodeService().setProperty(nextDispositionActionNodeRef, PROP_DISPOSITION_ACTION_COMPLETED_AT, new Date());
                                getNodeService().setProperty(nextDispositionActionNodeRef, PROP_DISPOSITION_ACTION_COMPLETED_BY, AuthenticationUtil.getRunAsUser());
                            }
                        }
                        else
                        {
                            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORD_NOT_DECLARED, getName(), actionedUponNodeRef.toString()));
                        }
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EXPECTED_RECORD_LEVEL, getName(), actionedUponNodeRef.toString()));
                    }
                }
                else
                {
                    if (getRecordFolderService().isRecordFolder(actionedUponNodeRef))
                    {
                        if (getRecordFolderService().isRecordFolderDeclared(actionedUponNodeRef))
                        {
                            // Indicate that the disposition action is underway
                            getNodeService().setProperty(nextDispositionActionNodeRef, PROP_DISPOSITION_ACTION_STARTED_AT, new Date());
                            getNodeService().setProperty(nextDispositionActionNodeRef, PROP_DISPOSITION_ACTION_STARTED_BY, AuthenticationUtil.getRunAsUser());

                            executeRecordFolderLevelDisposition(action, actionedUponNodeRef);

                            // Indicate that the disposition action is compelte
                            if (getNodeService().exists(nextDispositionActionNodeRef) &&
                                getSetDispositionActionComplete())
                            {
                                getNodeService().setProperty(nextDispositionActionNodeRef, PROP_DISPOSITION_ACTION_COMPLETED_AT, new Date());
                                getNodeService().setProperty(nextDispositionActionNodeRef, PROP_DISPOSITION_ACTION_COMPLETED_BY, AuthenticationUtil.getRunAsUser());
                            }

                        }
                        else
                        {
                            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_ALL_RECORDS_DECLARED, getName(), actionedUponNodeRef.toString()));
                        }
                    }
                    else
                    {
                        throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_RECORD_FOLDER, getName(), actionedUponNodeRef.toString()));
                    }

                }

                if (getNodeService().exists(actionedUponNodeRef) && getSetDispositionActionComplete())
                {
                    // Update the disposition schedule
                    getDispositionService().updateNextDispositionAction(actionedUponNodeRef);
                }
            }
            else
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_ELIGIBLE, getName(), actionedUponNodeRef.toString()));
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // TODO add the "checkEligibility" parameter
    }

    /**
     * @param action
     * @param record
     */
    protected abstract void executeRecordLevelDisposition(Action action, NodeRef record);

    /**
     * @param action
     * @param recordFolder
     */
    protected abstract void executeRecordFolderLevelDisposition(Action action, NodeRef recordFolder);

    /**
     * @param nodeRef
     * @return
     */
    protected DispositionSchedule checkDispositionActionExecutionValidity(NodeRef nodeRef, NodeRef nextDispositionActionNodeRef, boolean throwError)
    {
        // Check the node has associated disposition instructions
        DispositionSchedule di = getDispositionService().getDispositionSchedule(nodeRef);
        if (di == null)
        {
            if (throwError)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NO_DISPOITION_INSTRUCTIONS, getName(), nodeRef.toString()));
            }
            else
            {
                return null;
            }
        }

        // Check the node has the disposition schedule aspect applied
        if (!getNodeService().hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE))
        {
            if (throwError)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NO_DIS_LIFECYCLE_SET, getName(), nodeRef.toString()));
            }
            else
            {
                return null;
            }
        }

        if (checkNextDispositionAction(nodeRef))
        {
            // Check this the next disposition action
            NodeRef nextDispositionAction = nextDispositionActionNodeRef;
            if (nextDispositionAction == null)
            {
                if (throwError)
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NEXT_DISP_NOT_SET, getName(), nodeRef.toString()));
                }
                else
                {
                    return null;
                }
            }
            String actionName = (String) getNodeService().getProperty(nextDispositionAction, PROP_DISPOSITION_ACTION);
            if (actionName == null || !actionName.equals(getName()))
            {
                if (throwError)
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NOT_NEXT_DISP, getName(), nodeRef.toString()));
                }
                else
                {
                    return null;
                }
            }
        }

        return di;
    }

    /**
     * Get the next disposition action node. Null if none present.
     *
     * @param nodeRef
     *            the disposable node reference
     * @return NodeRef the next disposition action, null if none
     */
    private NodeRef getNextDispostionAction(NodeRef nodeRef)
    {
        NodeRef result = null;
        List<ChildAssociationRef> assocs = getNodeService().getChildAssocs(nodeRef, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL);
        if (assocs.size() != 0)
        {
            result = assocs.get(0).getChildRef();
        }
        return result;
    }
}
