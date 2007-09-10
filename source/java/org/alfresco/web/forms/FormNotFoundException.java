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
package org.alfresco.web.forms;

import java.io.FileNotFoundException;
import java.text.MessageFormat;
import javax.faces.context.FacesContext;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.wcm.WebProject;

/**
 * Error when a form cannot be resolved.
 *
 * @author Ariel Backenroth
 */
public class FormNotFoundException
   extends FileNotFoundException
{
   private final String formName;
   private final WebProject webProject;
   private final FormInstanceData fid;

   public FormNotFoundException(final String formName)
   {
      super(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                                                        "form_not_found"),
                                 formName));
      this.formName = formName;
      this.webProject = null;
      this.fid = null;
   }

   public FormNotFoundException(final String formName, final FormInstanceData fid)
   {
      super(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                                                        "form_not_found_for_form_instance_data"),
                                 formName,
                                 fid.getPath()));
      this.formName = formName;
      this.fid = fid;
      this.webProject = null;
   }

   public FormNotFoundException(final String formName, final WebProject webProject)
   {
      super(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                                                        "form_not_found_in_web_project"),
                                 formName,
                                 webProject.getName()));
      this.formName = formName;
      this.webProject = webProject;
      this.fid = null;
   }

   public FormNotFoundException(final String formName, final WebProject webProject, final FormInstanceData fid)
   {
      super(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                                                        "form_not_found_for_form_instance_data_in_web_project"),
                                 formName,
                                 webProject.getName(),
                                 fid.getPath()));
      this.formName = formName;
      this.webProject = webProject;
      this.fid = fid;
   }

   public FormNotFoundException(final Form form, final WebProject webProject)
   {
      super(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                                                        "form_not_configured_for_web_project"),
                                 form.getName(),
                                 webProject.getName()));
      this.formName = form.getName();
      this.webProject = webProject;
      this.fid = null;
   }

   public FormNotFoundException(final Form form, final WebProject webProject, final FormInstanceData fid)
   {
      super(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(),
                                                        "form_associated_with_form_instance_data_not_configured_for_web_project"),
                                 form.getName(),
                                 fid.getPath(),
                                 webProject.getName()));
      this.formName = form.getName();
      this.webProject = webProject;
      this.fid = fid;
   }
}
