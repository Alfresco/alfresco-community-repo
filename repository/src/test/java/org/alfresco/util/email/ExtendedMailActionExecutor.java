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

import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.InternetAddress;
import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Extension of {@link MailActionExecuter} for test purposes.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class ExtendedMailActionExecutor extends MailActionExecuter
{
    private static final Log LOGGER = LogFactory.getLog(ExtendedMailActionExecutor.class);

    private volatile Map<String, Serializable> parameterValues = Collections.emptyMap();

    @Override
    public MimeMessageHelper prepareEmail(Action ruleAction, NodeRef actionedUponNodeRef, Pair<String, Locale> recipient,
                Pair<InternetAddress, Locale> sender)
    {
        parameterValues = ruleAction.getParameterValues();
        return super.prepareEmail(ruleAction, actionedUponNodeRef, recipient, sender);
    }

    public Serializable getLastMessageActionParam(String name)
    {
        return parameterValues.get(name);
    }

    public Map<String, Object> getLastMessageTemplateModel()
    {
        Object model = parameterValues.get(PARAM_TEMPLATE_MODEL);
        if (model instanceof Map)
        {
            return (Map<String, Object>) model;
        }
        else
        {
            String className = model == null ? "" : model.getClass().getName();
            LOGGER.warn("Skipping unsupported email template model parameters of type " + className);
        }
        return Collections.emptyMap();
    }

    public void clearLastMessage()
    {
        parameterValues.clear();
        super.clearLastTestMessage();
        super.resetTestSentCount();
    }
}
