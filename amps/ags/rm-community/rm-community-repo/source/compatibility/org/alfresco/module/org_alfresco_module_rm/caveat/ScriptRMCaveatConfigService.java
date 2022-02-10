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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.security.AuthorityService;

/**
 * Script projection of RM Caveat Config Service
 *
 * @author Mark Rogers
 */
public class ScriptRMCaveatConfigService extends BaseScopableProcessorExtension
{
    private RMCaveatConfigService caveatConfigService;
    private AuthorityService authorityService;

    public void setCaveatConfigService(RMCaveatConfigService rmCaveatConfigService)
    {
        this.caveatConfigService = rmCaveatConfigService;
    }

    public RMCaveatConfigService getRmCaveatConfigService()
    {
        return caveatConfigService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public AuthorityService getAuthorityService()
    {
        return authorityService;
    }

    public ScriptConstraint getConstraint(String listName)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");

        RMConstraintInfo info = caveatConfigService.getRMConstraint(xxx);

        if(info != null)
        {
            return new ScriptConstraint(info, caveatConfigService, getAuthorityService());
        }

        return null;
    }

    public ScriptConstraint[] getAllConstraints()
    {
    	return getConstraints(true);
    }

    public ScriptConstraint[] getConstraintsWithoutEmptyList()
    {
    	return getConstraints(false);
    }

    private ScriptConstraint[] getConstraints(boolean includeEmptyList)
    {
        Set<RMConstraintInfo> values = caveatConfigService.getAllRMConstraints();

        List<ScriptConstraint> vals = new ArrayList<>(values.size());
        for(RMConstraintInfo value : values)
        {
            ScriptConstraint c = new ScriptConstraint(value, caveatConfigService, getAuthorityService());
            if (includeEmptyList)
            {
            	vals.add(c);
            }
            else
            {
            	if (c.getValues().length > 0)
            	{
            		vals.add(c);
            	}
            }
        }

        return vals.toArray(new ScriptConstraint[vals.size()]);
    }

    /**
     * Delete list
     * @param listName

     */
    public void deleteConstraintList(String listName)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");
        caveatConfigService.deleteRMConstraint(xxx);
    }



    /**
     * Update value
     */
    public void updateConstraintValues(String listName, String authorityName, String[]values)
    {
        List<String> vals = new ArrayList<>();
        caveatConfigService.updateRMConstraintListAuthority(listName, authorityName, vals);
    }

    /**
     * Delete the constraint values.   i.e remove an authority from a constraint list
     */
    public void deleteRMConstraintListAuthority(String listName, String authorityName)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");

        caveatConfigService.removeRMConstraintListAuthority(xxx, authorityName);
    }

    /**
     * Delete the constraint values.   i.e remove a value from a constraint list
     */
    public void deleteRMConstraintListValue(String listName, String valueName)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");

        caveatConfigService.removeRMConstraintListValue(xxx, valueName);

    }

    public ScriptConstraint createConstraint(String listName, String title, String[] allowedValues)
    {
        //TODO Temporary conversion
        if(listName != null)
        {
            listName = listName.replace("_", ":");
        }

        RMConstraintInfo info = caveatConfigService.addRMConstraint(listName, title, allowedValues);
        return new ScriptConstraint(info, caveatConfigService, getAuthorityService());
    }

}
