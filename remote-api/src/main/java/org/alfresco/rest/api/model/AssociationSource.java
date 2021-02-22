/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.rest.api.model;

import java.util.Objects;

public class AssociationSource {
    private String role = null;
    private String cls = null;
    private Boolean isMany = null;
    private Boolean isMandatory = null;
    private Boolean isMandatoryEnforced = null;

    public AssociationSource()
    {
    }

    public AssociationSource(String role, String cls, Boolean isMany, Boolean isMandatory, Boolean isMandatoryEnforced)
    {
        this.role = role;
        this.cls = cls;
        this.isMany = isMany;
        this.isMandatory = isMandatory;
        this.isMandatoryEnforced = isMandatoryEnforced;
    }

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

    public Boolean getIsMany()
    {
        return isMany;
    }

    public void setIsMany(Boolean isMany)
    {
        this.isMany = isMany;
    }

    public Boolean getIsMandatory()
    {
        return isMandatory;
    }

    public void setIsMandatory(Boolean isMandatory)
    {
        this.isMandatory = isMandatory;
    }

    public Boolean getIsMandatoryEnforced()
    {
        return isMandatoryEnforced;
    }

    public void setIsMandatoryEnforced(Boolean isMandatoryEnforced)
    {
        this.isMandatoryEnforced = isMandatoryEnforced;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AssociationSource other = (AssociationSource) obj;
        return Objects.equals(role, other.getRole()) &&
                        Objects.equals(cls, other.getCls()) &&
                        Objects.equals(isMany, other.getIsMany()) &&
                        Objects.equals(isMandatory, other.getIsMandatory()) &&
                        Objects.equals(isMandatoryEnforced, other.getIsMandatoryEnforced());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(512);
        builder.append("AssociationSource [role=").append(this.role)
                .append(", cls=").append(this.cls)
                .append(", isMany=").append(this.isMany)
                .append(", isMandatory=").append(isMandatory)
                .append(", isMandatoryEnforced=").append(isMandatoryEnforced)
                .append(']');
        return builder.toString();
    }
}