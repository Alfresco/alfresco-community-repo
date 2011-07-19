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
package org.alfresco.cmis.mapping;

import java.io.Serializable;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author dward
 */
public class VersionSeriesIdProperty extends AbstractVersioningProperty
{
    /**
     * Construct
     */
    public VersionSeriesIdProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, CMISDictionaryModel.PROP_VERSION_SERIES_ID);
    }

    @Override
    public Serializable getValue(NodeRef nodeRef)
    {
        CheckOutCheckInService checkOutCheckInService = getServiceRegistry().getCheckOutCheckInService();
        NodeRef result = null;
        if (checkOutCheckInService.isWorkingCopy(nodeRef))
        {
            result = checkOutCheckInService.getCheckedOut(nodeRef);
            if (result == null)
            {
                result = nodeRef;
            }
        }
        else
        {
            result = getVersionSeries(nodeRef);
        }
        return result.toString();
    }
}
