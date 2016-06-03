package org.alfresco.repo.activities.feed;

import java.io.Serializable;

import org.alfresco.repo.admin.SysAdminParams;

/**
 * Repository context passed from grid task to grid job
 */
public class RepoCtx implements Serializable
{
    private SysAdminParams sysAdminParams;
    private String repoEndPoint;  
    private boolean userNamesAreCaseSensitive = false;
    
	private String ticket;
	
	public static final long serialVersionUID = -3896042917378679686L;
	
    public RepoCtx(SysAdminParams sysAdminParams, String repoEndPoint)
    {
        this.sysAdminParams = sysAdminParams;
	    this.repoEndPoint = repoEndPoint.endsWith("/") ? repoEndPoint.substring(0, repoEndPoint.length()-1) : repoEndPoint;
    }

	public String getRepoEndPoint() {
        String base = sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort() + "/" + sysAdminParams.getAlfrescoContext();
        return base + repoEndPoint;
	}

    public String getTicket()
    {
        return ticket;
    }

    public void setTicket(String ticket)
    {
        this.ticket = ticket;
    }

    public boolean isUserNamesAreCaseSensitive()
    {
        return userNamesAreCaseSensitive;
    }

    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }
}
