/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.web.bean.actions;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Interface definition for a handler, classes that are responsible
 * for marshalling data between the repository and the action based 
 * wizards.
 * 
 * @author gavinc
 */
public interface IHandler extends Serializable
{
   /**
    * Adds any properties to the given map that need default values
    * before the UI is displayed to the user.
    * 
    * @param props The current properties map
    */
   public void setupUIDefaults(Map<String, Serializable> props);
   
   /**
    * By default, a JSP with the same name as the handler will
    * be loaded from a default location. If the handler has stored 
    * it's UI somewhere else the view id can be returned from this method.
    * 
    * @return The path to the JSP for the handler
    */
   public String getJSPPath();
   
   /**
    * Called at the end of the wizard. The properties relevant to 
    * this handler have to be placed in the repository properties 
    * map in the correct form for sending to the node service.
    * 
    * @param props The current properties map
    * @param repoProps The repository properties map to prepare
    */
   public void prepareForSave(Map<String, Serializable> props,
                              Map<String, Serializable> repoProps);
   
   /**
    * Called at the start of the edit wizard. The repository properties
    * map holds the current state of this rule. Any properties relevant
    * to this handler should be retrieved and setup in the current 
    * properties map.
    * 
    * @param props The current properties map
    * @param repoProps The properties currently in the repository
    */
   public void prepareForEdit(Map<String, Serializable> props,
                              Map<String, Serializable> repoProps);
   
   /**
    * Generates a summary string for this handler. The current state of 
    * the wizard is passed as well as the current properties map.
    * 
    * @param context Faces context
    * @param wizard The current wizard
    * @param props The properties map
    * @return The summary string
    */
   public String generateSummary(FacesContext context, IWizardBean wizard, 
                                 Map<String, Serializable> props);
   
   /**
    * Returns value of possibility of repeated performance of action
    * 
    * @return true is allow multiple
    */
   public boolean isAllowMultiple();
}
