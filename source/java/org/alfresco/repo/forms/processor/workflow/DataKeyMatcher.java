
package org.alfresco.repo.forms.processor.workflow;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.*;

/**
 * Utility class used for matching data keys.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class DataKeyMatcher
{
    /**
     * A regular expression which can be used to match property names. These
     * names will look like <code>"prop_cm_name"</code>. The pattern can also be
     * used to extract the "cm" and the "name" parts.
     */
    private final static Pattern propertyNamePattern = Pattern.compile("(^[a-zA-Z0-9-]+)_([a-zA-Z0-9_]+$)");

    /**
     * A regular expression which can be used to match association names. These
     * names will look like <code>"assoc_cm_references_added"</code>. The
     * pattern can also be used to extract the "cm", the "name" and the suffix
     * parts.
     */
    private final static Pattern associationNamePattern = Pattern.compile("(^[a-zA-Z0-9-]+)_([a-zA-Z0-9_]+)(_[a-zA-Z]+$)");

    private final static Pattern transientAssociationPattern = Pattern.compile("(^[a-zA-Z0-9]+)(_[a-zA-Z]+$)");

    private final NamespaceService namespaceService;

    public DataKeyMatcher(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * Attempts to match the <code>dataKey</code> to either a property or association pattern.
     * If no match can be found then returns <code>null</code>.
     * @param dataKey the dataKey to be matched.
     * @return a {@link DataKeyInfo} representation or <code>null</code>.
     */
    public DataKeyInfo match(String dataKey)
    {
        if (dataKey.startsWith(PROP_DATA_PREFIX))
        {
            return matchProperty(dataKey);
        }
        else if (dataKey.startsWith(ASSOC_DATA_PREFIX))
        {
            return matchAssociation(dataKey);
        }
        // No match found.
        return null;
    }

    private DataKeyInfo matchAssociation(String dataKey)
    {
        String keyName = dataKey.substring(ASSOC_DATA_PREFIX.length());
        Matcher matcher = associationNamePattern.matcher(keyName);
        if (matcher.matches())
        {
            QName qName = getQName(matcher);
            boolean isAdd = isAdd(matcher, 3);
            String name = qName.toPrefixString(namespaceService);
            return DataKeyInfo.makeAssociationDataKeyInfo(name, qName, isAdd);
        }
        return matchTransientAssociation(keyName);
    }

    private DataKeyInfo matchTransientAssociation(String keyName)
    {
        Matcher matcher = transientAssociationPattern.matcher(keyName);
        if (matcher.matches())
        {
            boolean isAdd = isAdd(matcher, 2);
            String name = matcher.group(1);
            return DataKeyInfo.makeTransientAssociationDataKeyInfo(name, isAdd);
        }
        return null;
    }

    private boolean isAdd(Matcher matcher, int suffixPos)
    {
        String suffix = matcher.group(suffixPos);
        boolean isAdd = !(ASSOC_DATA_REMOVED_SUFFIX.equals(suffix));
        return isAdd;
    }

    private DataKeyInfo matchProperty(String dataKey)
    {
        String keyName = dataKey.substring(PROP_DATA_PREFIX.length());
        Matcher matcher = propertyNamePattern.matcher(keyName);
        if (matcher.matches())
        {
            QName qName = getQName(matcher);
            String name = qName.toPrefixString(namespaceService);
            return DataKeyInfo.makePropertyDataKeyInfo(name, qName);
        }
        return DataKeyInfo.makeTransientPropertyDataKeyInfo(keyName);
    }

    private QName getQName(Matcher matcher)
    {
        String prefix = matcher.group(1);
        String localName = matcher.group(2);
        QName qName = QName.createQName(prefix, localName, namespaceService);
        return qName;
    }
}
