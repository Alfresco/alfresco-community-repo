package org.alfresco.rest.api.tests.client;


public class UserAuthenticationDetailsProviderImpl implements AuthenticationDetailsProvider
{
    private String adminUserName;
    private String adminPassword;
    
    private UserDataService userDataService;
    
    /**
     * @param userDataService service to use for {@link UserData} related operations
     */
    public UserAuthenticationDetailsProviderImpl(UserDataService userDataService, String adminUserName, String adminPassword)
    {
        this.userDataService = userDataService;
        this.adminUserName = adminUserName;
        this.adminPassword = adminPassword;
    }

    
    public String getPasswordForUser(String userName)
    {
        UserData user = userDataService.findUserByUserName(userName);
        if(user != null)
        {
            return user.getPassword();
        }
        return null;
    }

    public String getTicketForUser(String userName)
    {
        UserData user = userDataService.findUserByUserName(userName);
        if(user != null)
        {
            return user.getTicket();
        }
        return null;
    }

    public String getAdminUserName()
    {
        return this.adminUserName;
    }

    public String getAdminPassword()
    {
        return this.adminPassword;
    }


	@Override
	public void updateTicketForUser(String userName, String ticket) throws IllegalArgumentException
	{
        UserData user = userDataService.findUserByUserName(userName);
        if(user != null)
        {
            user.setTicket(ticket);
        }
        else
        {
        	// TODO
        }
	}

}
