package org.alfresco.repo.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.namespace.QName;

public class TypeIndexFilter extends AbstractIndexFilter
{
    private boolean ignorePathsForSpecificTypes = false;
    private Set<QName> typesForIgnoringPaths = new HashSet<QName>();
    private List<String> typesForIgnoringPathsString;

    public boolean isIgnorePathsForSpecificTypes()
    {
        return ignorePathsForSpecificTypes;
    }

    public void setIgnorePathsForSpecificTypes(boolean ignorePersonAndConfigurationPaths)
    {
        this.ignorePathsForSpecificTypes = ignorePersonAndConfigurationPaths;
    }

    public void setTypesForIgnoringPaths(List<String> typesForIgnoringPaths)
    {
        typesForIgnoringPathsString = typesForIgnoringPaths;
    }

    public void init()
    {
        super.init();
        initIgnoringPathsByCriterion(typesForIgnoringPathsString, typesForIgnoringPaths, new DefinitionExistChecker()
        {
            @Override
            public boolean isDefinitionExists(QName qName)
            {
                return (null != dictionaryService.getType(qName));
            }
        });
    }

    public boolean shouldBeIgnored(QName nodeType)
    {
        if (!ignorePathsForSpecificTypes)
        {
            return false;
        }

        if (null != nodeType)
        {
            if (typesForIgnoringPaths.contains(nodeType))
            {
                return true;
            }

            for (QName type : typesForIgnoringPaths)
            {
                if (dictionaryService.isSubClass(nodeType, type))
                {
                    return true;
                }
            }
        }

        return false;
    }
}
