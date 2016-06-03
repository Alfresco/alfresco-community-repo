
package org.alfresco.repo.web.scripts.dictionary;

import java.util.Comparator;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;

/**
 * Comparators used when ordering dictionary elements
 * 
 * @author Roy Wetherall
 */
public interface DictionaryComparators
{
    /**
     * Class definition comparator.
     * 
     * Used to order class definitions by title.
     */
    public class ClassDefinitionComparator implements Comparator<ClassDefinition>
    {
        private final MessageLookup messageLookup;

        public ClassDefinitionComparator(MessageLookup messageLookup)
        {
            this.messageLookup = messageLookup;            
        }

        public int compare(ClassDefinition arg0, ClassDefinition arg1)
        {
            int result = 0;
            
            String title0 = arg0.getTitle(messageLookup);
            if (title0 == null)
            {
                title0 = arg0.getName().toPrefixString();
            }
            String title1 = arg1.getTitle(messageLookup);
            if (title1 == null)
            {
                title1 = arg1.getName().getPrefixString();
            }
            
            if (title0 == null && title1 != null)
            {
                result = 1;
            }
            else if (title0 != null && title1 == null)
            {
                result = -1;
            }
            else if (title0 != null && title1 != null)
            {
                result = String.CASE_INSENSITIVE_ORDER.compare(title0, title1);
            }
            
            return result;
        }        
    }
    
    /**
     * Property definition comparator.
     * 
     * Used to order property definitions by title.
     */
    public class PropertyDefinitionComparator implements Comparator<PropertyDefinition>
    {
        private final MessageLookup messageLookup;

        public PropertyDefinitionComparator(MessageLookup messageLookup)
        {
            this.messageLookup = messageLookup;            
        }

        public int compare(PropertyDefinition arg0, PropertyDefinition arg1)
        {
            int result = 0;
            
            String title0 = arg0.getTitle(messageLookup);
            if (title0 == null)
            {
                title0 = arg0.getName().toPrefixString();
            }
            String title1 = arg1.getTitle(messageLookup);
            if (title1 == null)
            {
                title1 = arg1.getName().getPrefixString();
            }
            
            if (title0 == null && title1 != null)
            {
                result = 1;
            }
            else if (title0 != null && title1 == null)
            {
                result = -1;
            }
            else if (title0 != null && title1 != null)
            {
                result = String.CASE_INSENSITIVE_ORDER.compare(title0, title1);
            }
            
            return result;
        }        
    }
}
