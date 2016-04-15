package org.alfresco.repo.template;

import java.util.List;

import org.springframework.extensions.surf.util.URLDecoder;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Kevin Roast
 * @since 4.2
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Decodes a URL encoded string. The empty string is returned for invalid input values.
 * <p>
 * Usage: urldecode(String s)
 */
public class URLDecodeMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        String result = "";
        
        if (args.size() != 0)
        {
            String s = "";
            Object arg0 = args.get(0);
            if (arg0 instanceof TemplateScalarModel)
            {
                s = ((TemplateScalarModel)arg0).getAsString();
            }
            
            if (s != null)
            {
                result = URLDecoder.decode(s);
            }
        }
        
        return result;
    }
}
