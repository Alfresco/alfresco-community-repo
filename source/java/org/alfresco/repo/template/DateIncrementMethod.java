package org.alfresco.repo.template;

import java.util.Date;
import java.util.List;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

/**
 * @author Roy Wetherall
 */
public class DateIncrementMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
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
