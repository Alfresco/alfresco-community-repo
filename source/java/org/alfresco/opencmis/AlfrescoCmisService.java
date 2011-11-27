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
package org.alfresco.opencmis;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;

/**
 * Extended interface for lifecycle management
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface AlfrescoCmisService extends CmisService
{
    /**
     * Called directly before any CMIS method is used
     */
    void beforeCall();
    
    /**
     * Called directly after any CMIS method is used
     */
    void afterCall();
    
    /**
     * Call before the work method and forms the opposite of {@link #close()}.
     * 
     * @param context               the context in which the service must operate
     */
    void open(CallContext context);
}
