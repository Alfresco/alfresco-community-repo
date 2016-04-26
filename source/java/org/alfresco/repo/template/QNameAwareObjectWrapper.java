package org.alfresco.repo.template;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * @author Kevin Roast
 * @author dward
 */
public class QNameAwareObjectWrapper implements ObjectWrapper
{
    private final ThreadLocal<ObjectWrapper> threadDelegates = new ThreadLocal<ObjectWrapper>()
    {
        /* (non-Javadoc)
         * @see java.lang.ThreadLocal#initialValue()
         */
        @Override
        protected ObjectWrapper initialValue()
        {
            return new DefaultObjectWrapper()
            {
                /* (non-Javadoc)
                 * @see freemarker.template.DefaultObjectWrapper#wrap(java.lang.Object)
                 */
                @Override
                public TemplateModel wrap(Object obj) throws TemplateModelException
                {
                    if (obj instanceof QNameMap)
                    {
                        return new QNameHash((QNameMap)obj, this);
                    }
                    else
                    {
                        return super.wrap(obj);
                    }
                }
            };
        }
    };
    
    public QNameAwareObjectWrapper()
    {
        // Force initialization/introspection of core classes in advance to avoid lock ups later
        threadDelegates.get();
        try
        {
            Introspector.getBeanInfo(TemplateNode.class);
            Introspector.getBeanInfo(QName.class);
        }
        catch (IntrospectionException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Override to support wrapping of a QNameNodeMap by our custom wrapper object
     */
    public TemplateModel wrap(Object obj) throws TemplateModelException
    {
        return threadDelegates.get().wrap(obj);
    }
    
    /**
     * Inner class to support clone of QNameNodeMap
     */
    class QNameHash extends SimpleHash
    {
        /**
         * Constructor
         * 
         * @param map
         * @param wrapper
         */
        public QNameHash(QNameMap map, ObjectWrapper wrapper)
        {
            super(map, wrapper);
        }
        
        /**
         * Override to support clone of a QNameNodeMap object
         */
        protected Map copyMap(Map map)
        {
            if (map instanceof QNameMap)
            {
                return (Map)((QNameMap)map).clone();
            }
            else
            {
                return super.copyMap(map);
            }
        }
    }
}
