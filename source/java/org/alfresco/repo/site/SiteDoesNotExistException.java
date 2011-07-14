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
package org.alfresco.repo.site;

/**
 * Site does not exist exception
 * 
 * @author Roy Wetherall
 */
public class SiteDoesNotExistException extends SiteServiceException
{
    /** Serial version UID */
    private static final long serialVersionUID = -58321344792182609L;
    /** The error message label for this */
    private static final String MSG_SITE_NO_EXIST = "site_service.site_no_exist";
    
    /**
     * Constructor
     */
    public SiteDoesNotExistException(String shortName)
    {
        super(MSG_SITE_NO_EXIST, new Object[]{shortName});
    }
}
