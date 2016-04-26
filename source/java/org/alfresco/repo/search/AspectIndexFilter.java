package org.alfresco.repo.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.namespace.QName;

public class AspectIndexFilter extends AbstractIndexFilter
{

    private boolean ignorePathsForSpecificAspects = false;
    private Set<QName> aspectsForIgnoringPaths = new HashSet<QName>();
    private List<String> aspectsForIgnoringPathsString;

    public boolean isIgnorePathsForSpecificAspects()
    {
        return ignorePathsForSpecificAspects;
    }

    public void setIgnorePathsForSpecificAspects(boolean ignorePathsForSpecificAspects)
    {
        this.ignorePathsForSpecificAspects = ignorePathsForSpecificAspects;
    }

    public void setAspectsForIgnoringPaths(List<String> aspectsForIgnoringPaths)
    {
        this.aspectsForIgnoringPathsString = aspectsForIgnoringPaths;
    }

    public void init()
    {
        super.init();
        initIgnoringPathsByCriterion(aspectsForIgnoringPathsString, aspectsForIgnoringPaths, new DefinitionExistChecker()
        {
            @Override
            public boolean isDefinitionExists(QName qName)
            {
                return (null != dictionaryService.getAspect(qName));
            }
        });
    }

    public boolean shouldBeIgnored(Set<QName> aspects)
    {
        if (!ignorePathsForSpecificAspects)
        {
            return false;
        }
        
        if ((null != aspects) && !aspects.isEmpty())
        {
            for (QName aspectForIgnoringPaths : aspectsForIgnoringPaths)
            {
                if (aspects.contains(aspectForIgnoringPaths))
                {
                    return true;
                }
                for (QName nodeAspect : aspects)
                {
                    if (dictionaryService.isSubClass(nodeAspect, aspectForIgnoringPaths))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
