/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.util.email;

import org.alfresco.repo.management.subsystems.ApplicationContextFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.mail.internet.MimeMessage;
import java.util.Map;

/**
 * A utility class to work with {@link ExtendedMailActionExecutor} in test mode.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class EmailUtil
{
    private final ExtendedMailActionExecutor mailActionExecutor;

    public EmailUtil(ApplicationContext applicationContext)
    {
        ApplicationContextFactory subsystem = (ApplicationContextFactory) applicationContext.getBean("OutboundSMTP");
        ConfigurableApplicationContext childContext = (ConfigurableApplicationContext) subsystem.getApplicationContext();

        this.mailActionExecutor = childContext.getBean("mail", ExtendedMailActionExecutor.class);
    }

    public Object getLastEmailActionParam(String name)
    {
        return mailActionExecutor.getLastMessageActionParam(name);
    }

    public Map<String, Object> getLastEmailTemplateModel()
    {
        return mailActionExecutor.getLastMessageTemplateModel();
    }

    public Object getLastEmailTemplateModelValue(String key)
    {
        return mailActionExecutor.getLastMessageTemplateModel().get(key);
    }

    public MimeMessage getLastEmail()
    {
        return mailActionExecutor.retrieveLastTestMessage();
    }

    public int getSentCount()
    {
        return mailActionExecutor.getTestSentCount();
    }

    public void reset()
    {
        mailActionExecutor.clearLastMessage();
    }
}
