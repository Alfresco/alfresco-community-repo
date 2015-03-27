/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @deprecated as of 2.1 see {@link org.alfresco.module.org_alfresco_module_rm.admin.NotCustomisableMetadataException}
 */
public class NotCustomisableMetadataException extends CustomMetadataException
{
    private static final long serialVersionUID = -6194867814140009959L;
    public static final String MSG_NOT_CUSTOMISABLE = "rm.admin.not-customisable";
    
    public NotCustomisableMetadataException(String aspectName)
    {
        super(I18NUtil.getMessage(MSG_NOT_CUSTOMISABLE, aspectName));
    }
}
