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
package org.alfresco.service.cmr.security;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

/**
 * Evaluate public service entry conditions as defined in the security interceptors.
 * Decouples any understanding of the security model from asking can I invoke the method and expect it to work.
 * @author andyh
 *
 */
public interface PublicServiceAccessService
{
    /**
     * @param publicService - the name of the public service
     * @param method - the method call
     * @param args - the arguments to the method as you woud call the method
     * @return AccessStatus
     */
    @Auditable(parameters = { "publicService", "method" })
    public AccessStatus hasAccess(String publicService, String method, Object ... args);

}
