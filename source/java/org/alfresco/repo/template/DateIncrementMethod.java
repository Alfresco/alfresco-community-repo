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
package org.alfresco.repo.template;

import java.util.Date;
import java.util.List;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

/**
 * @author Roy Wetherall
 * 
 
 */
public final class DateIncrementMethod implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        Date result = null;
        
        if (args.size() == 2)
        {
            Object arg0 = args.get(0);
            Object arg1 = args.get(1);
            
            if (arg0 instanceof TemplateDateModel && arg1 instanceof TemplateNumberModel)
            {
                Date origionalDate = (Date)((TemplateDateModel)arg0).getAsDate();
                Number number = ((TemplateNumberModel)arg1).getAsNumber();
                long increment = number.longValue();
            
                long modified = origionalDate.getTime() + increment;
                result = new Date(modified);
            }
        }
        
        return result;
    }
}
