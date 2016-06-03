package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.namespace.QName;

/**
 * Simple class to recursively convert property values to bean objects accessable by Templates.
 * 
 * @author Kevin Roast
 */
public class PropertyConverter
{
    public Serializable convertProperty(
            Serializable value, QName name, ServiceRegistry services, TemplateImageResolver resolver)
    {
        // perform conversions from Java objects to template compatable instances
        if (value == null)
        {
            return null;
        }
        else if (value instanceof NodeRef)
        {
            // NodeRef object properties are converted to new TemplateNode objects
            // so they can be used as objects within a template
            value = new TemplateNode(((NodeRef)value), services, resolver);
        }
        else if (value instanceof List)
        {
            // recursively convert each value in the collection
            List<Serializable> list = (List<Serializable>)value;
            List<Serializable> result = new ArrayList<Serializable>(list.size());
            for (int i=0; i<list.size(); i++)
            {
                // add each item to a new list as the repo can return unmodifiable lists
                result.add(convertProperty(list.get(i), name, services, resolver));
            }
            value = (Serializable)result;
        }
        
        return value;
    }
}