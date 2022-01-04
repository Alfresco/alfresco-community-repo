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

package org.alfresco.repo.rule;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.CompositeActionImpl;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Extended rule service implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedRuleServiceImpl extends RuleServiceImpl
{
    private static final String MSG_RETENTION_PERIOD_NOT_VALID = "rm.action.worm-retention-period-not-valid";
    private static final String PARAM_RETENTION_PERIOD = "retentionPeriod";
    private static final String POSITIVE_INTEGERS_PATTERN = "^\\+?([1-9]\\d*)$";
    private static final String WORM_LOCK_ACTION = "wormLock";

	/** indicates whether the rules should be run as admin or not */
    private boolean runAsAdmin = true;

    /** ignore types */
    private Set<QName> ignoredTypes = new HashSet<>();

    /** file plan service */
    private FilePlanService filePlanService;

    /** node service */
    protected NodeService nodeService;

    /** Record service */
    protected RecordService recordService;

    /**
     * @param nodeService	node service
     */
    public void setNodeService2(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param recordService   record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param runAsAdmin  true if run rules as admin, false otherwise
     */
    public void setRunAsAdmin(boolean runAsAdmin)
    {
        this.runAsAdmin = runAsAdmin;
    }

    /**
     * Init method
     */
    @Override
    public void init()
    {
        super.init();

        // Specify a set of system types to be ignored by rule executions
        ignoredTypes.add(RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE);
        ignoredTypes.add(RecordsManagementModel.TYPE_DISPOSITION_ACTION);
        ignoredTypes.add(RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION);
        ignoredTypes.add(RecordsManagementModel.TYPE_EVENT_EXECUTION);
    }

    private void validateWormLockRuleAction(final Rule rule)
    {
        try
        {
            CompositeActionImpl compositeAction = (CompositeActionImpl) rule.getAction();
            Pattern pattern = Pattern.compile(POSITIVE_INTEGERS_PATTERN);
            for (Action action : compositeAction.getActions())
            {
                if (WORM_LOCK_ACTION.equals(action.getActionDefinitionName()))
                {
                    String retentionPeriodParamValue = (String) action.getParameterValue(PARAM_RETENTION_PERIOD);
                    if (retentionPeriodParamValue != null)
                    {
                        Matcher matcher = pattern.matcher(retentionPeriodParamValue);
                        if (!matcher.matches())
                        {
                            throw new IntegrityException(I18NUtil.getMessage(MSG_RETENTION_PERIOD_NOT_VALID), null);
                        }
                    }
                }
            }
        } catch (PatternSyntaxException ex)
        {
            throw new IntegrityException (I18NUtil.getMessage(MSG_RETENTION_PERIOD_NOT_VALID), null);
        } catch (ClassCastException ex1)
        {
            //do nothing, not a composite action for validation
        }
    }

    /**
     * @see org.alfresco.repo.rule.RuleServiceImpl#saveRule(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.rule.Rule)
     */
    @Override
    public void saveRule(final NodeRef nodeRef, final Rule rule)
    {
        validateWormLockRuleAction(rule);
        if (filePlanService.isFilePlanComponent(nodeRef))
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    ExtendedRuleServiceImpl.super.saveRule(nodeRef, rule);
                    return null;
                }

            });
        }
        else
        {
            super.saveRule(nodeRef, rule);
        }
    }

    /**
     * @see org.alfresco.repo.rule.RuleServiceImpl#removeRule(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.rule.Rule)
     */
    @Override
    public void removeRule(final NodeRef nodeRef, final Rule rule)
    {
        if (filePlanService.isFilePlanComponent(nodeRef))
        {
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    ExtendedRuleServiceImpl.super.removeRule(nodeRef, rule);
                    return null;
                }

            });
        }
        else
        {
            super.removeRule(nodeRef, rule);
        }
    }

    /**
     * @see org.alfresco.repo.rule.RuleServiceImpl#executeRule(org.alfresco.service.cmr.rule.Rule, org.alfresco.service.cmr.repository.NodeRef, java.util.Set)
     */
    @Override
    public void executeRule(final Rule rule, final NodeRef nodeRef, final Set<ExecutedRuleData> executedRules)
    {
        if (nodeService.exists(nodeRef))
        {
            QName typeQName = nodeService.getType(nodeRef);

            // check if this is a rm rule on a rm artifact
            if (filePlanService.isFilePlanComponent(nodeRef) &&
            	isFilePlanComponentRule(rule))
            {
            	// ignore and
                if (!isIgnoredType(typeQName))
    	        {
    	        	if (runAsAdmin)
    	            {
    	            	AuthenticationUtil.runAs(new RunAsWork<Void>()
                        {
                            @Override
                            public Void doWork()
                            {
                                ExtendedRuleServiceImpl.super.executeRule(rule, nodeRef, executedRules);
                                return null;
                            }
                        }, AuthenticationUtil.getAdminUserName());
                	}
                	else
                	{
                		// run as current user
                		super.executeRule(rule, nodeRef, executedRules);
                	}
    	        }
            }
            else
            {
                // just execute the rule as the current user
                super.executeRule(rule, nodeRef, executedRules);
            }
        }
    }

    /**
     * Indicates whether the rule is a file plan component
     *
     * @param rule		rule
     * @return boolean	true if rule is set on a file plan component, false otherwise
     */
    private boolean isFilePlanComponentRule(Rule rule)
    {
        NodeRef nodeRef = getOwningNodeRef(rule);
        return filePlanService.isFilePlanComponent(nodeRef);
    }

    /**
     * @param  typeQName	type qname
     * @return boolean		true if ignore type, false otherwise
     */
    private boolean isIgnoredType(QName typeQName)
    {
        return ignoredTypes.contains(typeQName);
    }
}
