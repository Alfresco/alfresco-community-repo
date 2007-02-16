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
package org.alfresco.repo.template;

import java.util.Date;
import java.util.List;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

/**
 * @author Kevin Roast
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Compare two dates to see if they differ by the specified number of miliseconds
 * <p>
 * Usage: 
 *    dateCompare(dateA, dateB) - 1 if dateA if greater than dateB
 *    dateCompare(dateA, dateB, millis) - 1 if dateA is greater than dateB by at least millis, else 0 
 */
public final class DateCompareMethod implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        int result = 0;
        
        if (args.size() >= 2)
        {
            Object arg0 = args.get(0);
            Object arg1 = args.get(1);
            long diff = 0;
            if (args.size() == 3)
            {
               Object arg2 = args.get(2);
               if (arg2 instanceof TemplateNumberModel)
               {
                  Number number = ((TemplateNumberModel)arg2).getAsNumber();
                  diff = number.longValue();
               }
            }
            if (arg0 instanceof TemplateDateModel && arg1 instanceof TemplateDateModel)
            {
                Date dateA = (Date)((TemplateDateModel)arg0).getAsDate();
                Date dateB = (Date)((TemplateDateModel)arg1).getAsDate();
                if (dateA.getTime() > (dateB.getTime() - diff)) 
                {
                   result = 1;
                }
            }
        }
        
        return result;
    }
}
