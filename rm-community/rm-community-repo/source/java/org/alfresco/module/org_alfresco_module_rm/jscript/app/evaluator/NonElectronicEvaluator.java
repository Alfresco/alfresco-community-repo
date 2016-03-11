package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public class NonElectronicEvaluator extends BaseEvaluator
{
    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        QName qName = nodeService.getType(nodeRef);
        if (qName != null && dictionaryService.isSubClass(qName, TYPE_NON_ELECTRONIC_DOCUMENT))
        {
            result = true;
        }
        return result;
    }
}
