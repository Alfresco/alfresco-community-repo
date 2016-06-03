package org.alfresco.repo.mode;

public enum ServerMode 
{
	/**
	 * This server is in an unknown mode,  no value has been set in alfresco-global.properties
	 */
	UNKNOWN,
	/**
	 * This is a test server
	 */
	TEST,
	/**
	 * This is a backup of a production server
	 */
	BACKUP,
	/**
	 * This is a production server
	 */
	PRODUCTION
}
