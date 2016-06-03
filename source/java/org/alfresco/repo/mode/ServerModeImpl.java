package org.alfresco.repo.mode;

public class ServerModeImpl implements ServerModeProvider
{
	public ServerMode serverMode;
	
	public void setServerModeAsString(String s)
	{
		serverMode = ServerMode.valueOf(s.toUpperCase().trim());
	}

	@Override
	public ServerMode getServerMode() 
	{	
		return serverMode;
	}

}
