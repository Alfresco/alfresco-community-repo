/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.authority.script;

import java.io.Serializable;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * The Script user is a USER authority exposed to the scripting API
 * 
 * @author mrogers
 */
public class ScriptUser implements Authority, Serializable
{
	private transient AuthorityService authorityService;
    private ScriptAuthorityType authorityType = ScriptAuthorityType.USER;
    private String shortName;
    private String fullName;
    private String displayName;
    
    public ScriptUser(String fullName, AuthorityService authorityService)
    {
    	this.authorityService = authorityService;
        this.fullName = fullName;	
        shortName = authorityService.getShortName(fullName);
        displayName = authorityService.getAuthorityDisplayName(fullName);
        //isInternal = authorityService.
    }
    
	public void setAuthorityType(ScriptAuthorityType authorityType) {
		this.authorityType = authorityType;
	}

	public ScriptAuthorityType getAuthorityType() {
		return authorityType;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
}
