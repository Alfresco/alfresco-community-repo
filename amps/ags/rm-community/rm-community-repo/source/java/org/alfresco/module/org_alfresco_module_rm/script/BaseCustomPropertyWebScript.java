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

package org.alfresco.module.org_alfresco_module_rm.script;

import org.alfresco.module.org_alfresco_module_rm.compatibility.CompatibilityModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.namespace.QName;

/**
 * Base class for all custom property webscripts.
 *
 * @author Roy Wetherall
 */
public class BaseCustomPropertyWebScript extends AbstractRmWebScript
{
    /**
     * Takes the element name and maps it to the QName of the customisable type.  The passed element name should be a prefixed
     * qname string, but to support previous versions of this API a couple of hard coded checks are made first.
     *
     * @param elementName
     * @return
     */
    protected QName mapToTypeQName(String elementName)
    {
        // Direct matching provided for backward compatibility with RM 1.0
        if ("recordFolder".equalsIgnoreCase(elementName))
        {
            return RecordsManagementModel.TYPE_RECORD_FOLDER;
        }
        else if ("record".equalsIgnoreCase(elementName))
        {
            return RecordsManagementModel.ASPECT_RECORD;
        }
        else if ("recordCategory".equalsIgnoreCase(elementName))
        {
            return RecordsManagementModel.TYPE_RECORD_CATEGORY;
        }
        else if ("recordSeries".equalsIgnoreCase(elementName))
        {
            return CompatibilityModel.TYPE_RECORD_SERIES;
        }
        else
        {
            // Try and convert the string to a qname
            return QName.createQName(elementName, getNamespaceService());
        }
    }
}
