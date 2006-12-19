/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
