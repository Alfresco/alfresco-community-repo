/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.domain.tenant;


/**
 * Entity for <b>alf_tenant</b> update.
 * 
 * @author janv
 * @since 4.0 (thor)
 */
public class TenantUpdateEntity extends TenantEntity
{
    public TenantUpdateEntity(String tenantDomain)
    {
        super(tenantDomain);
    }
    
    @Override
    public void  setTenantDomain(String tenantDomain)
    {
        throw new UnsupportedOperationException("Cannot update tenantDomain: "+getTenantDomain());
    }
}
