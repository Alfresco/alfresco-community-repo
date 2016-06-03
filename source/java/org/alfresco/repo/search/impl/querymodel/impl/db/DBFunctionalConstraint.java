package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.impl.BaseFunctionalConstraint;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 *
 */
public class DBFunctionalConstraint extends BaseFunctionalConstraint implements DBQueryBuilderComponent
{

    /**
     * @param function Function
     * @param arguments Map<String, Argument>
     */
    public DBFunctionalConstraint(Function function, Map<String, Argument> arguments)
    {
        super(function, arguments);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace.NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService, org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO)
     */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors, Map<String, Argument> functionArgs,  FunctionEvaluationContext functionContext, boolean supportBooleanFloatAndDouble)
    {
        Function function = getFunction();
        if(function != null)
        {
            if(function instanceof DBQueryBuilderComponent)
            {
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent)function;
                dbQueryBuilderComponent.prepare(namespaceService, dictionaryService, qnameDAO, nodeDAO, tenantService, selectors,  getFunctionArguments(), functionContext, supportBooleanFloatAndDouble);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map, java.util.List)
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        Function function = getFunction();
        if(function != null)
        {
            if(function instanceof DBQueryBuilderComponent)
            {
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent)function;
                dbQueryBuilderComponent.buildJoins(singleJoins, multiJoins);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
        
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util.List)
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        Function function = getFunction();
        if(function != null)
        {
            if(function instanceof DBQueryBuilderComponent)
            {
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent)function;
                dbQueryBuilderComponent.buildPredicateCommands(predicatePartCommands);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
    }

}
