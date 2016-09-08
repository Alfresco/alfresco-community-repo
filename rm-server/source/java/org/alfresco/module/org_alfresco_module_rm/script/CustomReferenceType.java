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
package org.alfresco.module.org_alfresco_module_rm.script;

/**
 * This enum represents the allowed types of custom references.
 * 
 * @author Neil McErlean
 */
public enum CustomReferenceType
{
	PARENT_CHILD("parentchild"),
	BIDIRECTIONAL("bidirectional");

	private final String printableString;
	
	private CustomReferenceType(String printableString)
	{
		this.printableString = printableString;
	}
	
	@Override
	public String toString()
	{
		return this.printableString;
	}
	
	public static CustomReferenceType getEnumFromString(String stg)
	{
		for (CustomReferenceType type : CustomReferenceType.values())
		{
			if (type.printableString.equals(stg))
			{
				return type;
			}
		}
		throw new IllegalArgumentException("Unrecognised CustomReferenceType: " + stg);
	}
}