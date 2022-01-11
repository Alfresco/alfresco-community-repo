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

package org.alfresco.module.org_alfresco_module_rm.disposition.property;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Disposition property implementation bean.
 *
 * @author Roy Wetherall
 */
@BehaviourBean
public class DispositionProperty extends BaseBehaviourBean
                                 implements NodeServicePolicies.OnUpdatePropertiesPolicy
{
    /** Property QName */
    private QName propertyName;

    /** Namespace service */
    private NamespaceService namespaceService;

    /** Disposition service */
    private DispositionService dispositionService;

    /** Indicates whether this disposition property applies to a folder level disposition */
    private boolean appliesToFolderLevel = true;

    /** Indicates whether this disposition property applies to a record level disposition */
    private boolean appliesToRecordLevel = true;

    /** Set of disposition actions this property does not apply to */
    private Set<String> excludedDispositionActions;

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param propertyName property name (as string)
     */
    public void setName(String propertyName)
    {
        this.propertyName = QName.createQName(propertyName, namespaceService);
    }

    /**
     * @return  property QName
     */
    public QName getQName()
    {
        return this.propertyName;
    }

    /**
     * @return  property definition
     */
    public PropertyDefinition getPropertyDefinition()
    {
        return dictionaryService.getProperty(propertyName);
    }

    /**
     * @param excludedDispositionActions    list of excluded disposition actions
     */
    public void setExcludedDispositionActions(Set<String> excludedDispositionActions)
    {
        this.excludedDispositionActions = excludedDispositionActions;
    }

    /**
     * @param appliesToFolderLevel
     */
    public void setAppliesToFolderLevel(boolean appliesToFolderLevel)
    {
        this.appliesToFolderLevel = appliesToFolderLevel;
    }

    /**
     * @param appliesToRecordLevel
     */
    public void setAppliesToRecordLevel(boolean appliesToRecordLevel)
    {
        this.appliesToRecordLevel = appliesToRecordLevel;
    }

    /**
     * Bean initialisation method
     */
    public void init()
    {
        // register with disposition service
        dispositionService.registerDispositionProperty(this);
    }

    /**
     * Indicates whether the disposition property applies given the context.
     *
     * @param isRecordLevel      true if record level disposition schedule, false otherwise
     * @param dispositionAction  disposition action name
     * @return boolean           true if applies, false otherwise
     */
    public boolean applies(boolean isRecordLevel, String dispositionAction)
    {
        boolean result = false;

        if ((isRecordLevel && appliesToRecordLevel) ||
            (!isRecordLevel && appliesToFolderLevel))
        {
            if (excludedDispositionActions != null && excludedDispositionActions.size() != 0)
            {
                if (!excludedDispositionActions.contains(dispositionAction))
                {
                    result = true;
                }
            }
            else
            {
                result = true;
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:dispositionLifecycle",
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onUpdateProperties(
            final NodeRef nodeRef,
            final Map<QName, Serializable> before,
            final Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef))
        {
            // has the property we care about changed?
            if (isPropertyUpdated(before, after))
            {
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    @Override
                    public Void doWork()
                    {
                        Date updatedDateValue = (Date)after.get(propertyName);
                        if (updatedDateValue != null)
                        {
                            DispositionAction dispositionAction = dispositionService.getNextDispositionAction(nodeRef);
                            if (dispositionAction != null)
                            {
                                DispositionActionDefinition daDefinition = dispositionAction.getDispositionActionDefinition();
                                // check whether the next disposition action matches this disposition property
                                if (daDefinition != null && propertyName.equals(daDefinition.getPeriodProperty()))
                                {
                                    Period period = daDefinition.getPeriod();
                                    Date updatedAsOf = period.getNextDate(updatedDateValue);

                                    // update asOf date on the disposition action based on the new property value
                                    NodeRef daNodeRef = dispositionAction.getNodeRef();
                                    // Don't overwrite a manually set "disposition as of" date.
                                    if (isNotTrue((Boolean) nodeService.getProperty(daNodeRef, PROP_MANUALLY_SET_AS_OF)))
                                    {
                                        nodeService.setProperty(daNodeRef, PROP_DISPOSITION_AS_OF, updatedAsOf);
                                    }
                                }
                            }
                        }
                        else
                        {
                            // throw an exception if the property is being 'cleared'
                            if (before.get(propertyName) != null)
                            {
                                throw new AlfrescoRuntimeException(
                                        "Error updating property " + propertyName.toPrefixString(namespaceService) +
                                        " to null, because property is being used to determine a disposition date.");
                            }
                        }

                        return null;
                    }

                }, AuthenticationUtil.getSystemUserName());
            }
        }
    }

    /**
     * Indicates whether the property has been updated or not.
     *
     * @param before
     * @param after
     * @return
     */
    private boolean isPropertyUpdated(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Serializable beforeValue = before.get(propertyName);
        Serializable afterValue = after.get(propertyName);

        return !Objects.equals(beforeValue, afterValue);
    }
}
