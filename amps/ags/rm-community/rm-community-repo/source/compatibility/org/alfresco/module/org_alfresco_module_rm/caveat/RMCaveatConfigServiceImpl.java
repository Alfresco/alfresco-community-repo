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

package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint.MatchLogic;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RM Caveat Config Service impl
 *
 * @author janv
 */
public class RMCaveatConfigServiceImpl implements RMCaveatConfigService
{
    private static Log logger = LogFactory.getLog(RMCaveatConfigServiceImpl.class);

    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    private RMCaveatConfigComponent rmCaveatConfigComponent;
    private RecordsManagementAdminService recordsManagementAdminService;


    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setCaveatConfigComponent(RMCaveatConfigComponent rmCaveatConfigComponent)
    {
        this.rmCaveatConfigComponent = rmCaveatConfigComponent;
    }

    public void setRecordsManagementAdminService(RecordsManagementAdminService recordsManagementAdminService)
    {
        this.recordsManagementAdminService = recordsManagementAdminService;
    }

    public RecordsManagementAdminService getRecordsManagementAdminService()
    {
        return recordsManagementAdminService;
    }

    public void init()
    {
        rmCaveatConfigComponent.init();
    }

    public NodeRef updateOrCreateCaveatConfig(InputStream is)
    {
        return rmCaveatConfigComponent.updateOrCreateCaveatConfig(is);
    }

    public NodeRef updateOrCreateCaveatConfig(File jsonFile)
    {
        return rmCaveatConfigComponent.updateOrCreateCaveatConfig(jsonFile);
    }

    public NodeRef updateOrCreateCaveatConfig(String jsonString)
    {
        return rmCaveatConfigComponent.updateOrCreateCaveatConfig(jsonString);
    }

    // Get allowed values for given caveat (for current user)
    public List<String> getRMAllowedValues(String constraintName)
    {
        return rmCaveatConfigComponent.getRMAllowedValues(constraintName);
    }

    /**
     * Check whether access to 'record component' node is vetoed for current user due to caveat(s)
     *
     * @param nodeRef
     * @return false, if caveat(s) veto access otherwise return true
     */
    public boolean hasAccess(NodeRef nodeRef)
    {
        return rmCaveatConfigComponent.hasAccess(nodeRef);
    }

    /**
     * add RM constraint list
     * @param listName the name of the RMConstraintList
     */
    public RMConstraintInfo addRMConstraint(String listName, String title, String[] values)
    {
        return addRMConstraint(listName, title, values, MatchLogic.AND);
    }

    public RMConstraintInfo addRMConstraint(String listName, String title, String[] values, MatchLogic matchLogic)
    {
        if (listName == null)
        {
            // Generate a list name
            StringBuilder sb = new StringBuilder();
            sb.append(RecordsManagementCustomModel.RM_CUSTOM_PREFIX);
            sb.append(QName.NAMESPACE_PREFIX);
            sb.append(UUID.randomUUID().toString());
            listName = sb.toString();
        }

        List<String>allowedValues = new ArrayList<>();
        for(String value : values)
        {
            allowedValues.add(value);
        }

        QName listQName = QName.createQName(listName, namespaceService);

        // TEMP review - if it already exists then change it for now
        try
        {
            recordsManagementAdminService.addCustomConstraintDefinition(listQName, title, true, allowedValues, matchLogic);
        }
        catch (AlfrescoRuntimeException e)
        {
            if (e.getMessage().contains("Constraint already exists"))
            {
                recordsManagementAdminService.changeCustomConstraintValues(listQName, allowedValues);
                recordsManagementAdminService.changeCustomConstraintTitle(listQName, title);
            }
        }

        rmCaveatConfigComponent.addRMConstraint(listName);

        RMConstraintInfo info = new RMConstraintInfo();
        info.setName(listQName.toPrefixString());
        info.setTitle(title);
        info.setAllowedValues(values);
        info.setCaseSensitive(true);
        return info;
    }

    /**
     * delete RM Constraint List
     *
     * @param listName the name of the RMConstraintList
     */
    public void deleteRMConstraint(String listName)
    {
        rmCaveatConfigComponent.deleteRMConstraint(listName);

        QName listQName = QName.createQName(listName, namespaceService);

        recordsManagementAdminService.removeCustomConstraintDefinition(listQName);
    }

    /**
     * Add a single value to an authority in a list.   The existing values of the list remain.
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param value
     * @throws AlfrescoRuntimeException if either the list or the authority do not already exist.
     */
    public void addRMConstraintListValue(String listName, String authorityName, String value)
    {
        rmCaveatConfigComponent.addRMConstraintListValue(listName, authorityName, value);
    }

    /**
     * Get the details of the specified list
     * @param listName
     * @return the details of the specified list
     */
    public Map<String, List<String>> getListDetails(String listName)
    {
        return rmCaveatConfigComponent.getListDetails(listName);
    }

    /**
     * Replace the values for an authority in a list.
     * The existing values are removed.
     *
     * If the authority does not already exist in the list, it will be added
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param values
     */
    public void updateRMConstraintListAuthority(String listName, String authorityName, List<String>values)
    {
        rmCaveatConfigComponent.updateRMConstraintListAuthority(listName, authorityName, values);
    }

    /**
     * Replace the authorities for a value in a list
     *
     * @param listName
     * @param valueName
     * @param authorities
     */
    public void updateRMConstraintListValue(String listName, String valueName, List<String>authorities)
    {
        rmCaveatConfigComponent.updateRMConstraintListValue(listName, valueName, authorities);
    }

    /**
     * Remove an authority from a list
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     */
    public void removeRMConstraintListAuthority(String listName, String authorityName)
    {
        rmCaveatConfigComponent.removeRMConstraintListAuthority(listName, authorityName);
    }

    /**
     * Get all Constraint Lists
     */
    public Set<RMConstraintInfo> getAllRMConstraints()
    {
        Set<RMConstraintInfo> info = new HashSet<>();

        List<ConstraintDefinition> defs = new ArrayList<>(10);
        for (QName caveatModelQName : rmCaveatConfigComponent.getRMCaveatModels())
        {
            defs.addAll(recordsManagementAdminService.getCustomConstraintDefinitions(caveatModelQName));
        }

        for(ConstraintDefinition dictionaryDef : defs)
        {
            Constraint con = dictionaryDef.getConstraint();
            if (con instanceof RMListOfValuesConstraint)
            {
                final RMListOfValuesConstraint def = (RMListOfValuesConstraint)con;
                RMConstraintInfo i = new RMConstraintInfo();
                i.setName(def.getShortName());
                i.setTitle(def.getTitle());

                // note: assumes only one caveat/LOV against a given property
                List<String> allowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
                {
                    public List<String> doWork()
                    {
                        return def.getAllowedValues();
                    }
                }, AuthenticationUtil.getSystemUserName());

                i.setAllowedValues(allowedValues.toArray(new String[allowedValues.size()]));
                i.setCaseSensitive(def.isCaseSensitive());
                info.add(i);
            }

        }

        return info;
    }

    /**
     * Get an RMConstraintInfo
     * @param listQName
     * @return the constraint or null if it does not exist
     */
    public RMConstraintInfo getRMConstraint(QName listQName)
    {
        ConstraintDefinition dictionaryDef = dictionaryService.getConstraint(listQName);
        if(dictionaryDef != null)
        {
            Constraint con = dictionaryDef.getConstraint();
            if (con instanceof RMListOfValuesConstraint)
            {
                final RMListOfValuesConstraint def = (RMListOfValuesConstraint)con;

                RMConstraintInfo info = new RMConstraintInfo();
                info.setName(listQName.toPrefixString());
                info.setTitle(con.getTitle());
                List<String> allowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
                {
                    public List<String> doWork()
                    {
                        return def.getAllowedValues();
                    }
                }, AuthenticationUtil.getSystemUserName());

                info.setAllowedValues(allowedValues.toArray(new String[allowedValues.size()]));
                info.setCaseSensitive(def.isCaseSensitive());
                return info;
            }
        }
        return null;
    }

    /**
     * Get RM Constraint detail.
     *
     * @return the constraintInfo or null
     */
    public RMConstraintInfo getRMConstraint(String listName)
    {
        QName listQName = QName.createQName(listName, namespaceService);
        return getRMConstraint(listQName);

    }

    /**
     * Update The allowed values for an RM Constraint.
     *
     * @param listName  The name of the list.
     * @param allowedValues the new alowed values
     *
     */
    public RMConstraintInfo updateRMConstraintAllowedValues(String listName, String[] allowedValues)
    {
        QName listQName = QName.createQName(listName, namespaceService);

        if(allowedValues != null)
        {
            List<String>allowedValueList = new ArrayList<>();
            for(String value : allowedValues)
            {
                allowedValueList.add(value);
            }

            ConstraintDefinition dictionaryDef = dictionaryService.getConstraint(listQName);
            Constraint con = dictionaryDef.getConstraint();
            if (con instanceof RMListOfValuesConstraint)
            {
                final RMListOfValuesConstraint def = (RMListOfValuesConstraint)con;
                List<String> oldAllowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
                {
                    public List<String> doWork()
                    {
                       return def.getAllowedValues();
                    }
                }, AuthenticationUtil.getSystemUserName());

                /**
                 * Deal with any additions
                 */
                for(String newValue : allowedValueList)
                {
                    if(!oldAllowedValues.contains(newValue) && logger.isDebugEnabled())
                    {
                        // This is an addition
                        logger.debug("value added to list:" + listQName + ":" + newValue);
                    }
                }

                /**
                 * Deal with any deletions
                 */
                for(String oldValue : oldAllowedValues)
                {
                    if(!allowedValueList.contains(oldValue))
                    {
                        // This is a deletion
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("value removed from list:" + listQName + ":" + oldValue);
                        }
                        removeRMConstraintListValue(listName, oldValue);
                    }
                }
            }

            recordsManagementAdminService.changeCustomConstraintValues(listQName, allowedValueList);
        }

        return getRMConstraint(listName);
    }

    /**
     * Remove a value from a list and cascade delete.
     */
    public void removeRMConstraintListValue(String listName, String valueName)
    {
        //TODO need to update the rm constraint definition
        // recordsManagementAdminService.

        rmCaveatConfigComponent.removeRMConstraintListValue(listName, valueName);
    }

    /**
     * Update the title of this RM Constraint.
     */
    public RMConstraintInfo updateRMConstraintTitle(String listName, String newTitle)
    {
        QName listQName = QName.createQName(listName, namespaceService);

        recordsManagementAdminService.changeCustomConstraintTitle(listQName, newTitle);
        return getRMConstraint(listName);
    }
}
