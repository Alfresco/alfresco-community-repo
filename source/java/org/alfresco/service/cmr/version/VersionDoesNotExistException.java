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
package org.alfresco.service.cmr.version;

import java.text.MessageFormat;


/**
 * Version does not exist exception class.
 * 
 * @author Roy Wetherall
 */
public class VersionDoesNotExistException extends VersionServiceException
{
    private static final long serialVersionUID = 3258133548417233463L;
    private static final String ERROR_MESSAGE = "The version with label {0} does not exisit in the version store.";

    /**
     * Constructor
     */
    public VersionDoesNotExistException(String versionLabel)
    {
        super(MessageFormat.format(ERROR_MESSAGE, new Object[]{versionLabel}));
    }
}
