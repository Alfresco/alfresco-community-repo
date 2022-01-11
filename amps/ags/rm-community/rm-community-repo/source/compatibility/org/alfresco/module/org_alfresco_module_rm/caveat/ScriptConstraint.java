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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class ScriptConstraint implements Serializable
{
   /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RMConstraintInfo info;

    private RMCaveatConfigService rmCaveatconfigService;

    private AuthorityService authorityService;

    ScriptConstraint(RMConstraintInfo info, RMCaveatConfigService rmCaveatconfigService, AuthorityService authorityService)
    {
        this.info = info;
        this.rmCaveatconfigService = rmCaveatconfigService;
        this.authorityService = authorityService;
    }

    public void setTitle(String title)
    {
        info.setTitle(title);
    }
    public String getTitle()
    {
        return info.getTitle();
    }
    public void setName(String name)
    {
        info.setName(name);
    }

    public String getName()
    {
        return info.getName().replace(":", "_");
    }

    public boolean isCaseSensitive()
    {
        return info.isCaseSensitive();
    }

    public String[] getAllowedValues()
    {
        return info.getAllowedValues();
    }

    public ScriptConstraintAuthority[] getAuthorities()
    {
         Map<String, List<String>> values = rmCaveatconfigService.getListDetails(info.getName());

         if (values == null)
         {
             return new ScriptConstraintAuthority[0];
         }

         ArrayList<ScriptConstraintAuthority> constraints = new ArrayList<>(values.size());
        for (Map.Entry<String, List<String>> entry : values.entrySet())
         {
              ScriptConstraintAuthority constraint = new ScriptConstraintAuthority();
              constraint.setAuthorityName(entry.getKey());
              constraint.setValues(entry.getValue());
              constraints.add(constraint);
         }
         return constraints.toArray(new ScriptConstraintAuthority[constraints.size()]);
    }

    /**
     * updateTitle
     */
    public void updateTitle(String newTitle)
    {
        info.setTitle(newTitle);
        rmCaveatconfigService.updateRMConstraintTitle(info.getName(), newTitle)  ;
    }

    /**
     * updateAllowedValues
     */
    public void updateAllowedValues(String[] allowedValues)
    {
        info.setAllowedValues(allowedValues);
        rmCaveatconfigService.updateRMConstraintAllowedValues(info.getName(), allowedValues);
    }

    /**
     * Update a value
     * @param bodge
     */
    public void updateValues(JSONArray bodge) throws Exception
    {
        for(int i = 0; i < bodge.length(); i++)
        {

            JSONObject obj = bodge.getJSONObject(i);
            String value = obj.getString("value");
            JSONArray authorities = obj.getJSONArray("authorities");
            List<String> aList = new ArrayList<>();
            for(int j = 0; j < authorities.length();j++)
            {
                aList.add(authorities.getString(j));
            }
            rmCaveatconfigService.updateRMConstraintListValue(info.getName(), value, aList);
        }
    }

    /**
     * Update a value
     * @param value
     * @param authorities
     */
    public void updateValues(String value, String[] authorities)
    {
        List<String> list = Arrays.asList(authorities);
        rmCaveatconfigService.updateRMConstraintListValue(info.getName(), value, list);
    }

    /**
     * Cascade delete an authority
     * @param authority
     */
    public void deleteAuthority(String authority)
    {
        //Do nothing
    }

    /**
     * Cascade delete a value
     * @param value
     */
    public void deleteValue(String value)
    {
        //Do nothing
    }


    /**
     * Get a single value
     * @param value
     * @return
     */
    public ScriptConstraintValue getValue(String value)
    {
        ScriptConstraintValue[] values = getValues();

        for(ScriptConstraintValue val : values)
        {
            if(val.getValueName().equalsIgnoreCase(value))
            {
                return val;
            }
        }
        return null;
    }

    public ScriptConstraintValue[] getValues()
    {
        // authority, values
        Map<String, List<String>> details = rmCaveatconfigService.getListDetails(info.getName());

        if (details == null)
        {
            details = new HashMap<>();
        }

        // values, authorities
        Map<String, List<String>> pivot = PivotUtil.getPivot(details);

        ArrayList<ScriptConstraintValue> constraints = new ArrayList<>(pivot.size());
        for (Map.Entry<String, List<String>> entry : pivot.entrySet())
        {
             ScriptConstraintValue constraint = new ScriptConstraintValue();
             constraint.setValueName(entry.getKey());
             constraint.setValueTitle(entry.getKey());

            List<String> authorities = entry.getValue();
             List<ScriptAuthority> sauth = new ArrayList<>();
             for(String authority : authorities)
             {
                 ScriptAuthority a = new ScriptAuthority();
                 a.setAuthorityName(authority);

                 String displayName = authorityService.getAuthorityDisplayName(authority);
                 if(StringUtils.isNotBlank(displayName))
                 {
                     a.setAuthorityTitle(displayName);
                 }
                 else
                 {
                     a.setAuthorityTitle(authority);
                 }
                 sauth.add(a);
             }
             constraint.setAuthorities(sauth);
             constraints.add(constraint);
        }

        /**
         * Now go through and add any "empty" values
         */
        Set<String> values = pivot.keySet();
        for(String value : info.getAllowedValues())
        {
            if(!values.contains(value))
            {
                ScriptConstraintValue constraint = new ScriptConstraintValue();
                constraint.setValueName(value);
                constraint.setValueTitle(value);
                List<ScriptAuthority> sauth = new ArrayList<>();
                constraint.setAuthorities(sauth);
                constraints.add(constraint);
            }
        }

        return constraints.toArray(new ScriptConstraintValue[constraints.size()]);
    }

}
