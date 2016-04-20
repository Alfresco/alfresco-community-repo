package org.alfresco.repo.search.impl.querymodel.impl.db.functions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderJoinCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommand;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Descendant;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 */
public class DBDescendant extends Descendant implements DBQueryBuilderComponent
{

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace
     * .NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService,
     * org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO, java.util.Set, java.util.Map,
     * org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors,
            Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext, boolean supportBooleanFloatAndDouble)
    {
        throw new QueryModelException("Descendant/IN_TREE() is unsupported");
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map,
     * java.util.List)
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        throw new QueryModelException("Descendant/IN_TREE() is unsupported");
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util.List)
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        throw new QueryModelException("Descendant/IN_TREE() is unsupported");
    }

}
