package org.alfresco.repo.action.executer;

import org.alfresco.error.AlfrescoRuntimeException;

public class MailActionExecuterMonitor
{
    private MailActionExecuter mailActionExceuter;
    
    public String sendTestMessage()
    {
        try
        {
            mailActionExceuter.sendTestMessage();
            return "email message sent";
        }
        catch
        (AlfrescoRuntimeException are)
        {
            return (are.getMessage());
        }
    }
    public int getNumberFailedSends()
    {
        return mailActionExceuter.getNumberFailedSends();
    }
    
    public int getNumberSuccessfulSends()
    {
        return mailActionExceuter.getNumberSuccessfulSends();
    }
    
    public void setMailActionExecuter(MailActionExecuter mailActionExceuter)
    {
        this.mailActionExceuter = mailActionExceuter;
    }
}
