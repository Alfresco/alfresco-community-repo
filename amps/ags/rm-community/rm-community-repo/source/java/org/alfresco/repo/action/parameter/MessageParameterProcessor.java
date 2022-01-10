/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.action.parameter;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Message parameter processor.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class MessageParameterProcessor extends ParameterProcessor
{
    /**
     * @see org.alfresco.repo.action.parameter.ParameterProcessor#process(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String process(String value, NodeRef actionedUponNodeRef)
    {
        // the default position is to return the value un-changed
        String result = value;

        // strip the processor name from the value
        value = stripName(value);
        if (!value.isEmpty())
        {
            result = I18NUtil.getMessage(value);
            if (result == null)
            {
                throw new AlfrescoRuntimeException("The message parameter processor could not resolve the message for the id " + value);
            }
        }

        return result;
    }
}
