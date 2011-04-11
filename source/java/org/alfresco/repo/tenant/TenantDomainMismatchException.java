/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.tenant;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception thrown when an operation that requires 
 *  two things to be in the same tenant domain discovers
 *  that they belong to different ones.
 */
@SuppressWarnings("serial")
public class TenantDomainMismatchException extends AlfrescoRuntimeException
{
    private String tenantA;
    private String tenantB;
    
    public TenantDomainMismatchException(String tenantA, String tenantB)
    {
        super(
                "domain mismatch: expected = " + renderTenent(tenantA) + 
                ", actual = " + renderTenent(tenantB)
        );
                
        this.tenantA = tenantA;
        this.tenantB = tenantB;
    }
    private static String renderTenent(String tenant)
    {
        if(tenant == null)
            return "<none>";
        return tenant;
    }
    
    public String getTenantA()
    {
        return tenantA;
    }
    public String getTenantB()
    {
        return tenantB;
    }
}
