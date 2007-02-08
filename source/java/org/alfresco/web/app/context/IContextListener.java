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
package org.alfresco.web.app.context;

/**
 * Interface used to allow Beans to register themselves as interested in UI context events.
 * <p>
 * Beans supporting this interface should be register against the UIContextService. Then Beans
 * which wish to indicate that the UI should refresh itself i.e. dump all cached data and settings,
 * call the UIContextService.notifyBeans() to inform all registered instances of the change.
 * <p>
 * Registered beans will also be informed of changes in location, for example when the current
 * space changes or when the user has changed area i.e. from company home to my home.
 * 
 * @author Kevin Roast
 */
public interface IContextListener
{
   /**
    * Method called by UIContextService.notifyBeans() to inform all registered beans that
    * all UI Beans should refresh dump all cached data and settings. 
    */
   public void contextUpdated();
   
   /**
    * Method called by UIContextService.spaceChanged() to inform all registered beans that
    * the current space has changed.
    */
   public void spaceChanged();
   
   /**
    * Method called by UIContextService.areaChanged() to inform all registered beans that
    * the user has changed area i.e. from company home to my home.
    */
   public void areaChanged();
}
