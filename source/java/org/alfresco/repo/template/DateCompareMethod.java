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

import java.util.Date;
import java.util.List;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * @author Kevin Roast
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Perform a test to see how two dates compare, optionally offset by a specified number of milliseconds.
 * <p>
 * Usage: 
 *    dateCompare(dateA, dateB) - 1 if dateA if greater than dateB
 *    dateCompare(dateA, dateB, millis) - 1 if dateA is greater than dateB by at least millis, else 0
 *    dateCompare(dateA, dateB, millis, test) - same as above, but the 'test' variable is one of the
 *          following strings ">", "<", "==" - greater than, less than or equal - as the test to perform.
 */
public class DateCompareMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
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
            String test = null;
            if (args.size() == 4)
            {
                Object arg3 = args.get(3);
                if (arg3 instanceof TemplateScalarModel)
                {
                    test = ((TemplateScalarModel)arg3).getAsString();
                }
            }
            if (arg0 instanceof TemplateDateModel && arg1 instanceof TemplateDateModel)
            {
                Date dateA = (Date)(((TemplateDateModel)arg0).getAsDate()).clone();
                Date dateB = (Date)(((TemplateDateModel)arg1).getAsDate()).clone();
                switch (((TemplateDateModel)arg0).getDateType())
                {
                    case TemplateDateModel.DATE:
                        // clear time part
                        dateA.setHours(0);
                        dateA.setMinutes(0);
                        dateA.setSeconds(0);
                        dateA = new Date(dateA.getTime() / 1000L * 1000L);
                        break;
                    case TemplateDateModel.TIME:
                        // clear date part
                        dateA.setDate(1);
                        dateA.setMonth(0);
                        dateA.setYear(0);
                        break;
                }
                switch (((TemplateDateModel)arg1).getDateType())
                {
                    case TemplateDateModel.DATE:
                        // clear time part
                        dateB.setHours(0);
                        dateB.setMinutes(0);
                        dateB.setSeconds(0);
                        dateB = new Date(dateB.getTime() / 1000L * 1000L);
                        break;
                    case TemplateDateModel.TIME:
                        // clear date part
                        dateB.setDate(1);
                        dateB.setMonth(0);
                        dateB.setYear(0);
                        break;
                }
                if (test == null || test.equals(">"))
                {
                    if (dateA.getTime() > (dateB.getTime() - diff)) 
                    {
                       result = 1;
                    }
                }
                else if (test.equals("<"))
                {
                    if (dateA.getTime() < (dateB.getTime() - diff)) 
                    {
                       result = 1;
                    }
                }
                else if (test.equals("=="))
                {
                    if (dateA.getTime() == (dateB.getTime() - diff)) 
                    {
                       result = 1;
                    }
                }
            }
        }
        
        return result;
    }
}
