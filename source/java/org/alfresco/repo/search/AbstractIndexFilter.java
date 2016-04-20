package org.alfresco.repo.search;

import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

public abstract class AbstractIndexFilter
{
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
    }

    protected interface DefinitionExistChecker
    {
        boolean isDefinitionExists(QName qName);
    }

    protected void initIgnoringPathsByCriterion(List<String> initDataInString, Set<QName> dataForIgnoringPaths, DefinitionExistChecker dec)
    {
        if (null != initDataInString)
        {
            for (String qNameInString : initDataInString)
            {
                if ((null != qNameInString) && !qNameInString.isEmpty())
                {
                    try
                    {
                        QName qname = QName.resolveToQName(namespaceService, qNameInString);
                        if (dec.isDefinitionExists(qname))
                        {
                            dataForIgnoringPaths.add(qname);
                        }
                    }
                    catch (InvalidQNameException e)
                    {
                        // Just ignore
                    }
                }
            }
        }
    }
}
