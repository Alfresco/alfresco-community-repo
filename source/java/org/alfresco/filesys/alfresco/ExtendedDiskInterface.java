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
package org.alfresco.filesys.alfresco;

import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.DiskInterface;

/**
 * @author dward
 */
public interface ExtendedDiskInterface extends DiskInterface
{
    /**
     * Register an independently created device context object for this instance of the shared device. Useful, e.g. when
     * context singleton configuration managed by a container.
     * 
     * @param context
     *            the device context
     * @param serverConfig ServerConfigurationBean
     * @exception DeviceContextException
     */
    public void registerContext(DeviceContext ctx, ServerConfigurationBean serverConfig) throws DeviceContextException;
}
