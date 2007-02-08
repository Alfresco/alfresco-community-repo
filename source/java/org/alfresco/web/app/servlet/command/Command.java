/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.service.ServiceRegistry;

/**
 * Simple servlet command pattern interface.
 * 
 * @author Kevin Roast
 */
public interface Command
{
   /**
    * Execute the command
    * 
    * @param serviceRegistry     The ServiceRegistry instance
    * @param properties          Bag of named properties for the command
    * 
    * @return return value from the command if any
    */
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties);
   
   /**
    * @return the names of the properties required for this command
    */
   public String[] getPropertyNames();
}
