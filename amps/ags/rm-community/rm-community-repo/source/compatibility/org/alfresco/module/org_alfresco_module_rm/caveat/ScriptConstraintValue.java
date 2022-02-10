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
import java.util.List;

public class ScriptConstraintValue implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -4659454215122271811L;
    private String value;
    private List<ScriptAuthority>authorities;
    
    public void setAuthorities(List<ScriptAuthority> values)
    {
        this.authorities = values;
    }
    public List<ScriptAuthority> getAuthorities()
    {
        return authorities;
    }
    public void setValueName(String authorityName)
    {
        this.value = authorityName;
    }
    public String getValueName()
    {
        return value;
    }
    public void setValueTitle(String authorityName)
    {
        this.value = authorityName;
    }
    public String getValueTitle()
    {
        return value;
    }
}
