package org.alfresco.repo.template;

import java.util.List;

import org.springframework.extensions.surf.util.ISO8601DateFormat;

import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author David Caruana
 * @author Kevin Roast
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Render Date to ISO8601 format.<br>
 * Or parse ISO6801 format string date to a Date object.
 * <p>
 * Usage: xmldate(Date date)
 *        xmldate(String date)
 */
public class ISO8601DateFormatMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        Object result = null;
        
        if (args.size() == 1)
        {
            Object arg0 = args.get(0);
            if (arg0 instanceof TemplateDateModel)
            {
                result = ISO8601DateFormat.format(((TemplateDateModel)arg0).getAsDate());
            }
            else if (arg0 instanceof TemplateScalarModel)
            {
                result = ISO8601DateFormat.parse(((TemplateScalarModel)arg0).getAsString());
            }
        }
        
        return result != null ? result : "";
    }
}