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
package org.alfresco.repo.template;

import java.util.List;

import org.alfresco.i18n.I18NUtil;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Kevin Roast
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Returns an I18N message resolved for the current locale and specified message ID.
 * <p>
 * Usage: message(String id)
 */
public class I18NMessageMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        String result = "";
        int argSize = args.size();

        if (argSize > 0)
        {
           String id = "";
           Object arg0 = args.get(0);
           if (arg0 instanceof TemplateScalarModel)
           {
               id = ((TemplateScalarModel)arg0).getAsString();
           }

           if (argSize == 1)
           {
              // Shortcut for no additional params
                result = I18NUtil.getMessage(id);
           }
           else
           {
              Object arg;
              Object[] params = new Object[argSize - 1];
              for (int i = 0; i < argSize-1; i++)
              {
                 // Note: need to ignore first passed-in arg
                 arg = args.get(i+1);
                 if (arg instanceof TemplateScalarModel)
                 {
                     params[i] = ((TemplateScalarModel)arg).getAsString();
                 }
                 else
                 {
                    params[i] = new String("");
                 }
              }
              result = I18NUtil.getMessage(id, params);
           }
        }
        
        return result;
    }
}
