/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

public class RestClassAssociationDefinitionModel extends TestModel
{
    public String role = null;
    public String cls = null;
    public Boolean isMany = null;
    public Boolean isMandatory = null;
    public Boolean isMandatoryEnforced = null;

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getCls()
    {
        return cls;
    }

    public void setCls(String cls)
    {
        this.cls = cls;
    }

    public Boolean getMany()
    {
        return isMany;
    }

    public void setMany(Boolean many)
    {
        isMany = many;
    }

    public Boolean getMandatory()
    {
        return isMandatory;
    }

    public void setMandatory(Boolean mandatory)
    {
        isMandatory = mandatory;
    }

    public Boolean getMandatoryEnforced()
    {
        return isMandatoryEnforced;
    }

    public void setMandatoryEnforced(Boolean mandatoryEnforced)
    {
        isMandatoryEnforced = mandatoryEnforced;
    }
}
