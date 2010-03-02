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
