package org.alfresco.repo.action.executer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;

public class MailActionExecuterMonitor
{
    private MailActionExecuter mailActionExceuter;
    
    public String sendTestMessage()
    {
        try
        {
            mailActionExceuter.sendTestMessage();
            Object[] params = {mailActionExceuter.getTestMessageTo()};
            String message = I18NUtil.getMessage("email.outbound.test.send.success", params);
            return message;
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
