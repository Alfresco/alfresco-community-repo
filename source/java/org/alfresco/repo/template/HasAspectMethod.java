package org.alfresco.repo.template;

import java.util.List;


import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Kevin Roast
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Method returns whether a TemplateNode has a particular aspect applied to it. The aspect
 * name can be either the fully qualified QName or the short prefixed name string.
 * <p>
 * Usage: hasAspect(TemplateNode node, String aspect) - 1 on true, 0 on false
 */
public class HasAspectMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        int result = 0;
        
        if (args.size() == 2)
        {
            // arg 0 must be a wrapped TemplateNode object
            BeanModel arg0 = (BeanModel)args.get(0);
            
            // arg 1 can be either wrapped QName object or a String 
            String arg1String = null;
            Object arg1 = args.get(1);
            if (arg1 instanceof BeanModel)
            {
                arg1String = ((BeanModel)arg1).getWrappedObject().toString();
            }
            else if (arg1 instanceof TemplateScalarModel)
            {
                arg1String = ((TemplateScalarModel)arg1).getAsString();
            }
            if (arg0.getWrappedObject() instanceof TemplateNode)
            {
                // test to see if this node has the aspect
                if ( ((TemplateNode)arg0.getWrappedObject()).hasAspect(arg1String) )
                {
                    result = 1;
                }
            }
        }
        
        return Integer.valueOf(result);
    }
}
