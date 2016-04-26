package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class TypeSupport implements DBQueryBuilderComponent
{
    List<Long> qnameIds = new ArrayList<Long>();
    
    DBQueryBuilderPredicatePartCommandType commandType;

    private boolean leftOuter;

    /**
     * @param qnameIds
     *            the qnameIds to set
     */
    public void setQnameIds(List<Long> qnameIds)
    {
        this.qnameIds = qnameIds;
    }

    /**
     * @param commandType the commandType to set
     */
    public void setCommandType(DBQueryBuilderPredicatePartCommandType commandType)
    {
        this.commandType = commandType;
    }

    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        return true;
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
        
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map,
     * java.util.List)
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        // Nothing to do

    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util
     * .List)
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        DBQueryBuilderPredicatePartCommand command = new DBQueryBuilderPredicatePartCommand();
        command.setJoinCommandType(DBQueryBuilderJoinCommandType.NODE);
        command.setAlias("node");
        command.setFieldName("type_qname_id");
        command.setType(commandType);
        if(qnameIds.size() > 0)
        {
            command.setValues(qnameIds.toArray(new Long[]{}));
        }
        else
        {
            command.setValues(new Long[]{-1l});
        }
        predicatePartCommands.add(command);

    }

    /**
     * @param leftOuter boolean
     */
    public void setLeftOuter(boolean leftOuter)
    {
        this.leftOuter = leftOuter;
    }
    
}