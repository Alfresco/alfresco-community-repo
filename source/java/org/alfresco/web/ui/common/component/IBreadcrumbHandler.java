/*
 * #%L
 * Alfresco Repository WAR Community
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.web.ui.common.component;

import java.io.Serializable;

/**
 * @author Kevin Roast
 */
public interface IBreadcrumbHandler extends Serializable
{
   /**
    * Override Object.toString()
    * 
    * @return the element display label for this handler instance.
    */
   public String toString();
   
   /**
    * Perform appropriate processing logic and then return a JSF navigation outcome.
    * This method will be called by the framework when the handler instance is selected by the user.
    * 
    * @param breadcrumb    The UIBreadcrumb component that caused the navigation
    * 
    * @return JSF navigation outcome
    */
   public String navigationOutcome(UIBreadcrumb breadcrumb);
}
