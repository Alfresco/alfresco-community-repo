/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.action.executer;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.rm.enterprise.service.SecurityGroupService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.rest.rm.enterprise.model.classification.configure.SecurityGroupAPI;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;

/**
 * Add features action executor implementation.
 *
 * @author Roy Wetherall
 */
public class ApplySecurityMarksExecutor extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
    public static final String NAME = "apply-security-marks";
    public static final String PARAM_SECURITY_MARK_GROUP_NAME = "securityMarkGroupName";
    public static final String PARAM_SECURITY_MARK_NAME = "securityMarkName";

    public static final String SC_URI = "http://www.alfresco.org/model/securitymarks/1.0";
    public static final QName TYPE_MARK = createQName(SC_URI, "mark");
    public static final QName SECURITY_MARK = createQName(SC_URI, "securityMark");
    public static final QName PROP_MARK_ID = createQName(SC_URI, "markId");
    public static final QName PROP_MARK_DISPLAY_LABEL = createQName(SC_URI, "markDisplayLabel");
    public static final QName PROP_MARK_DISPLAY_LABEL_KEY = createQName(SC_URI, "markDisplayLabelKey");



    /**
     * The node service
     */
    private NodeService nodeService;

    /** Transaction Service, used for retrying operations */

    private SecurityGroupService securityGroupService;

    private SecurityGroupAPI securityGroupAPI;
    private TransactionService transactionService;

    /**
     * Set the node service
     *
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the transaction service
     *
     * @param transactionService    the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Adhoc properties are allowed for this executor
     */
    @Override
    protected boolean getAdhocPropertiesAllowed()
    {
        return true;
    }

    /**
     * @see ActionExecuter#execute(Action, NodeRef)
     */

    public void executeImpl(final Action ruleAction, NodeRef actionedUponNodeRef)
    {
        executeImpl(ruleAction, actionedUponNodeRef, "","");
    }
    public void executeImpl(final Action ruleAction, final NodeRef actionedUponNodeRef,String securityGroupId, String securityMarkId) {
        if (!nodeService.exists(actionedUponNodeRef)) {
            return securityGroupAPI.processModel(RestSecurityMarkModel.class, simpleRequest(
                    securityGroupId, securityMarkId, getRmEnterpriseRestWrapper().getParameters()));
        }


        ImmutableMap.Builder<QName, Serializable> markProperties = ImmutableMap.builder();
        markProperties.put(PROP_MARK_ID, securityControl.getId());
        markProperties.put(PROP_MARK_DISPLAY_LABEL, securityControl.getDisplayLabel());
        markProperties.put(PROP_MARK_DISPLAY_LABEL_KEY, securityControl.getDisplayLabelKey());
        Map<String, Serializable> paramValues = ruleAction.getParameterValues();
        paramValues.put(markProperties);
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_SECURITY_MARK_GROUP_NAME, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_SECURITY_MARK_GROUP_NAME), false, "securityMarksGroupName"));
        paramList.add(new ParameterDefinitionImpl(PARAM_SECURITY_MARK_NAME, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_SECURITY_MARK_NAME), false, "securityMarksName"));

    }

}
