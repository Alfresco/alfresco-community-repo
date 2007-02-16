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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.app.servlet.ajax;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for all Ajax commands executed by this servlet.
 * 
 * The method is responsible for invoking the underlying managed bean 
 * and dealing with the response.
 * 
 * @author gavinc
 */
public interface AjaxCommand
{
   /**
    * Invokes the relevant method on the bean represented by the given
    * expression. Parameters required to call the method can be retrieved
    * from the request. 
    * 
    * Currently the content type of the response will always be text/xml, in the 
    * future sublcasses may provide a mechanism to allow the content type to be set
    * dynamically.
    * 
    * @param facesContext FacesContext
    * @param expression The binding expression
    * @param request The request
    * @param response The response
    */
   public abstract void execute(FacesContext facesContext, String expression,
         HttpServletRequest request, HttpServletResponse response) 
         throws ServletException, IOException;
}
