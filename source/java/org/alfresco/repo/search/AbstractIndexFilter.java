/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
