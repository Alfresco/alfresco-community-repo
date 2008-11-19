/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;

public class ConstraintHandlersConfigElement extends ConfigElementAdapter
{
    public static final String CONFIG_ELEMENT_ID = "constraint-handlers";
    private List<String> types = new ArrayList<String>();
    private Map<String, String> handlers = new HashMap<String, String>();
    private Map<String, String> messages = new HashMap<String, String>();
    private Map<String, String> messageIDs = new HashMap<String, String>();

    private static final long serialVersionUID = 1L;

    /**
     * This constructor creates an instance with the default name.
     */
    public ConstraintHandlersConfigElement()
    {
        super(CONFIG_ELEMENT_ID);
    }

    /**
     * This constructor creates an instance with the specified name.
     * 
     * @param name the name for the ConfigElement.
     */
    public ConstraintHandlersConfigElement(String name)
    {
        super(name);
    }

    /**
     * @see org.alfresco.config.ConfigElement#getChildren()
     */
    public List<ConfigElement> getChildren()
    {
        throw new ConfigException(
                "Reading the constraint-handlers config via the generic interfaces is not supported");
    }

    /**
     * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
     */
    public ConfigElement combine(ConfigElement configElement)
    {
        // There is an assumption here that it is only like-with-like combinations
        // that are allowed. i.e. Only an instance of a ConstraintHandlersConfigElement
        // can be combined with this.
        ConstraintHandlersConfigElement otherCHCElement = (ConstraintHandlersConfigElement) configElement;

        ConstraintHandlersConfigElement result = new ConstraintHandlersConfigElement();

        for (String nextType : types)
        {
            String nextValidationHandler = getValidationHandlerFor(nextType);
            String nextMessage = getMessageFor(nextType);
            String nextMessageId = getMessageIdFor(nextType);
            result.addDataMapping(nextType, nextValidationHandler, nextMessage,
                    nextMessageId);
        }

        for (String nextType : otherCHCElement.types)
        {
            String nextValidationHandler = otherCHCElement
                    .getValidationHandlerFor(nextType);
            String nextMessage = otherCHCElement.getMessageFor(nextType);
            String nextMessageId = otherCHCElement.getMessageIdFor(nextType);
            result.addDataMapping(nextType, nextValidationHandler, nextMessage,
                    nextMessageId);
        }

        return result;
    }

    /* package */void addDataMapping(String type, String validationHandler,
            String message, String messageID)
    {
        types.add(type);
        handlers.put(type, validationHandler);
        messages.put(type, message);
        messageIDs.put(type, messageID);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return types.hashCode() + 7 * handlers.hashCode() + 13
                * messages.hashCode() + 17 * messageIDs.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object otherObj)
    {
        if (otherObj == null || !otherObj.getClass().equals(this.getClass()))
        {
            return false;
        }
        ConstraintHandlersConfigElement otherCHCE = (ConstraintHandlersConfigElement) otherObj;
        return this.types.equals(otherCHCE.types)
                && this.handlers.equals(otherCHCE.handlers)
                && this.messages.equals(otherCHCE.messages)
                && this.messageIDs.equals(otherCHCE.messageIDs);
    }

    /**
     * This method returns the registered constraint types.
     * @return an unmodifiable List of the constraint types.
     */
    public List<String> getConstraintTypes()
    {
        return Collections.unmodifiableList(this.types);
    }

    /**
     * This method returns a String identifier for the validation-handler
     * associated with the specified constraint type.
     * 
     * @param type the constraint type.
     * @return a String identifier for the validation-handler.
     */
    public String getValidationHandlerFor(String type)
    {
        return handlers.get(type);
    }

    /**
     * This method returns a message String  associated with the specified constraint
     * type.
     * 
     * @param type the constraint type.
     * @return the message String for the validation-handler.
     */
    public String getMessageFor(String type)
    {
        return messages.get(type);
    }

    /**
     * This method returns a message-id String  associated with the specified constraint
     * type.
     * 
     * @param type the constraint type.
     * @return the message-id String for the validation-handler.
     */
    public String getMessageIdFor(String type)
    {
        return messageIDs.get(type);
    }
}
