package org.alfresco.repo.template;

import java.util.List;

import org.springframework.extensions.surf.util.I18NUtil;

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
        
        if (argSize != 0)
        {
            String id = "";
            Object arg0 = args.get(0);
            if (arg0 instanceof TemplateScalarModel)
            {
                id = ((TemplateScalarModel)arg0).getAsString();
            }
            
            if (id != null)
            {
                if (argSize == 1)
                {
                    // shortcut for no additional msg params
                    result = I18NUtil.getMessage(id);
                }
                else
                {
                    Object[] params = new Object[argSize - 1];
                    for (int i = 0; i < argSize-1; i++)
                    {
                        // ignore first passed-in arg which is the msg id
                        Object arg = args.get(i + 1);
                        if (arg instanceof TemplateScalarModel)
                        {
                            params[i] = ((TemplateScalarModel)arg).getAsString();
                        }
                        else if (arg instanceof TemplateNumberModel)
                        {
                            params[i] = ((TemplateNumberModel)arg).getAsNumber();
                        }
                        else if (arg instanceof TemplateDateModel)
                        {
                            params[i] = ((TemplateDateModel)arg).getAsDate();
                        }
                        else
                        {
                            params[i] = "";
                        }
                    }
                    result = I18NUtil.getMessage(id, params);
                }
            }
        }
        
        return result;
    }
}
