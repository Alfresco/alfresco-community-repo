 
package org.alfresco.module.org_alfresco_module_rm.script;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipType;

/**
 * This enum represents the allowed types of custom references.
 *
 * @author Neil McErlean
 * @deprecated as of RM 2.3, please use {@link RelationshipType} instead.
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