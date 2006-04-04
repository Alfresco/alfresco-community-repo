/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
    */
   public void execute(ServiceRegistry serviceRegistry, Map<String, Object> properties);
   
   /**
    * @return the names of the properties required for this command
    */
   public String[] getPropertyNames();
}
