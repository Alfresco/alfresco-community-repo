package org.alfresco.repo.mode;

public interface ServerModeProvider 
{
	/**
	 * Get the Server Mode which is a configuration property set via alfresco-global.propertues.   
	 * 
	 * It is not persisted anywhere in the database.
	 * 
	 * @return the server mode
	 */
	ServerMode getServerMode();
}
