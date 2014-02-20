/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.BaseQuery;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.stringtemplate.v4.compiler.CodeGenerator.subtemplate_return;

/**
 * @author Andy
 */
public class DBQuery extends BaseQuery implements DBQueryBuilderComponent
{

    private Long storeId;

    private Long sysDeletedType;

    private Long sinceTxId;
    
    Set<String> selectorGroup;

    /**
     * @param columns
     * @param source
     * @param constraint
     * @param orderings
     */
    public DBQuery(List<Column> columns, Source source, Constraint constraint, List<Ordering> orderings)
    {
        super(columns, source, constraint, orderings);
    }

    /**
     * @return the storeId
     */
    public Long getStoreId()
    {
        return storeId;
    }

    /**
     * @param storeId
     *            the storeId to set
     */
    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }

    /**
     * @return the sysDeletedType
     */
    public Long getSysDeletedType()
    {
        return sysDeletedType;
    }

    /**
     * @param sysDeletedType
     *            the sysDeletedType to set
     */
    public void setSysDeletedType(Long sysDeletedType)
    {
        this.sysDeletedType = sysDeletedType;
    }

    /**
     * @return the sinceTxId
     */
    public Long getSinceTxId()
    {
        return this.sinceTxId;
    }

    /**
     * @param sinceTxId the sinceTxId to set
     */
    public void setSinceTxId(Long sinceTxId)
    {
        this.sinceTxId = sinceTxId;
    }

    public List<DBQueryBuilderJoinCommand> getJoins()
    {
        HashMap<QName, DBQueryBuilderJoinCommand> singleJoins = new HashMap<QName, DBQueryBuilderJoinCommand>();
        ArrayList<DBQueryBuilderJoinCommand> multipleJoins = new ArrayList<DBQueryBuilderJoinCommand>();
        buildJoins(singleJoins, multipleJoins);
        ArrayList<DBQueryBuilderJoinCommand> allJoins = new ArrayList<DBQueryBuilderJoinCommand>();
        allJoins.addAll(singleJoins.values());
        allJoins.addAll(multipleJoins);
        return allJoins;
    }

    public boolean getHasPredicate()
    {
        return (getConstraint() != null) || (getSource().getSelectors().size() > 0);
    }
    
    public boolean getHasOrderBy()
    {
        return (getOrderings() != null) && (getOrderings().size() > 0);
     

    }

    public List<DBQueryBuilderPredicatePartCommand> getPredicateParts()
    {
        ArrayList<DBQueryBuilderPredicatePartCommand> predicatePartCommands = new ArrayList<DBQueryBuilderPredicatePartCommand>();
        buildPredicateCommands(predicatePartCommands);
        return predicatePartCommands;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        if (getConstraint() instanceof DBQueryBuilderComponent)
        {
            return ((DBQueryBuilderComponent) getConstraint()).isSupported();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.cmr.dictionary
     * .DictionaryService, org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO)
     */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors,
            Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext)
    {
        selectorGroup = selectors;
        if (selectorGroup != null)
        {
            for (String selector : selectorGroup)
            {
                Selector current = getSource().getSelector(selector);
                if (current instanceof DBQueryBuilderComponent)
                {
                    ((DBQueryBuilderComponent) current).prepare(namespaceService, dictionaryService, qnameDAO, nodeDAO, tenantService, selectorGroup, functionArgs, functionContext);
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
        }

        if (getConstraint() != null)
        {
            if (getConstraint() instanceof DBQueryBuilderComponent)
            {
                ((DBQueryBuilderComponent) getConstraint()).prepare(namespaceService, dictionaryService, qnameDAO, nodeDAO, tenantService, selectorGroup, functionArgs, functionContext);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
        
        if(getOrderings() != null)
        {
            for(Ordering ordering : getOrderings())
            {
                if(ordering instanceof DBQueryBuilderComponent)
                {
                    ((DBQueryBuilderComponent) ordering).prepare(namespaceService, dictionaryService, qnameDAO, nodeDAO, tenantService, selectorGroup, functionArgs, functionContext);
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins()
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        if (selectorGroup != null)
        {
            for (String selector : selectorGroup)
            {
                Selector current = getSource().getSelector(selector);
                if (current instanceof DBQueryBuilderComponent)
                {
                    ((DBQueryBuilderComponent) current).buildJoins(singleJoins, multiJoins);
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
        }

        if (getConstraint() != null)
        {
            if (getConstraint() instanceof DBQueryBuilderComponent)
            {
                ((DBQueryBuilderComponent) getConstraint()).buildJoins(singleJoins, multiJoins);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
        if(getOrderings() != null)
        {
            for(Ordering ordering : getOrderings())
            {
                if(ordering instanceof DBQueryBuilderComponent)
                {
                    ((DBQueryBuilderComponent) ordering).buildJoins(singleJoins, multiJoins);
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands()
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        boolean requiresAnd = false;

        if (selectorGroup != null)
        {
            for (String selector : selectorGroup)
            {
                Selector current = getSource().getSelector(selector);
                if (current instanceof DBQueryBuilderComponent)
                {
                    if (requiresAnd)
                    {
                        DBQueryBuilderPredicatePartCommand and = new DBQueryBuilderPredicatePartCommand();
                        and.setType(DBQueryBuilderPredicatePartCommandType.AND);
                        predicatePartCommands.add(and);
                    }
                    ((DBQueryBuilderComponent) current).buildPredicateCommands(predicatePartCommands);
                    requiresAnd = true;
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
        }

        if (getConstraint() != null)
        {
            if (getConstraint() instanceof DBQueryBuilderComponent)
            {
                if (requiresAnd)
                {
                    DBQueryBuilderPredicatePartCommand and = new DBQueryBuilderPredicatePartCommand();
                    and.setType(DBQueryBuilderPredicatePartCommandType.AND);
                    predicatePartCommands.add(and);
                }
                ((DBQueryBuilderComponent) getConstraint()).buildPredicateCommands(predicatePartCommands);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
        
        if(getOrderings() != null)
        {
            for(Ordering ordering : getOrderings())
            {
                if(ordering instanceof DBQueryBuilderComponent)
                {
                    ((DBQueryBuilderComponent) ordering).buildPredicateCommands(predicatePartCommands);
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }

    public static String[] getUUIDs(String[] source)
    {
        String[] uuids = new String[source.length];
        for(int i = 0; i < source.length; i++)
        {
            uuids[i] = getUUID(source[i]);
        }
        return uuids;
    }
    
    public static String getUUID(String source)
    {
     // Ignore version label  for now
        String ref;
        String versionLabel = null;
        String[] split = source.split(";");
        if(split.length == 1)
        {
            ref = source;
        }
        else
        {
            if(split[1].equalsIgnoreCase("PWC"))
            {
                throw new UnsupportedOperationException("Query for PWC is not supported");
            }
            
            ref = split[0];
            versionLabel = split[1];
        }
        
        
        if (NodeRef.isNodeRef(ref))
        {
            NodeRef nodeRef = new NodeRef(ref);
            return nodeRef.getId();
        }

        else
        {
           return ref;
        }
    }
    
    public static Long getDbid(String source, NodeDAO nodeDAO, TenantService tenantService)
    {
        // Ignore version label  for now
        String ref;
        String versionLabel = null;
        String[] split = source.split(";");
        if(split.length == 1)
        {
            ref = source;
        }
        else
        {
            if(split[1].equalsIgnoreCase("PWC"))
            {
                throw new UnsupportedOperationException("Query for PWC is not supported");
            }
            
            ref = split[0];
            versionLabel = split[1];
        }
        
        
        if (NodeRef.isNodeRef(ref))
        {
            NodeRef nodeRef = tenantService.getName(new NodeRef(ref));
            Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeRef);
            if (pair == null)
            {
                throw new QueryModelException("Invalid Object Id " + ref);
            }
            else
            {
                return pair.getFirst();
            }
        }

        else
        {
            NodeRef nodeRef = new NodeRef(tenantService.getName(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), ref);
            Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeRef);
            if (pair == null)
            {
                throw new QueryModelException("Invalid Object Id " + ref);
            }
            else
            {
                return pair.getFirst();
            }
        }
    }

    public static List<Long> findTypeIds(String type, NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO,  boolean exact)
    {
        ArrayList<Long> qnameIds = new ArrayList<Long>();
        TypeDefinition target = matchTypeDefinition(type, namespaceService, dictionaryService);
        if (target == null)
        {
            throw new QueryModelException("Invalid type: " + type);
        }

        if(exact)
        {
            Pair<Long, QName> pair = qnameDAO.getQName(target.getName());
            if (pair != null)
            {
                Long qnameId = pair.getFirst();
                qnameIds.add(qnameId);
            }
        }
        else
        {
            Collection<QName> subclasses = dictionaryService.getSubTypes(target.getName(), true);
            for (QName qname : subclasses)
            {
                TypeDefinition current = dictionaryService.getType(qname);
                if (target.getName().equals(current.getName()) || current.getIncludedInSuperTypeQuery())
                {
                    Pair<Long, QName> pair = qnameDAO.getQName(qname);
                    if (pair != null)
                    {
                        Long qnameId = pair.getFirst();
                        qnameIds.add(qnameId);
                    }
                }
            }
        }
        return qnameIds;
    }

    public static List<Long> findAspectIds(String aspect, NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO,  boolean exact)
    {
        ArrayList<Long> qnameIds = new ArrayList<Long>();
        AspectDefinition target = matchAspectDefinition(aspect, namespaceService, dictionaryService);
        if (target == null)
        {
            throw new QueryModelException("Invalid aspect: " + aspect);
        }

        if(exact)
        {
            Pair<Long, QName> pair = qnameDAO.getQName(target.getName());
            if (pair != null)
            {
                Long qnameId = pair.getFirst();
                qnameIds.add(qnameId);
            }
        }
        else
        {
            Collection<QName> subaspects = dictionaryService.getSubAspects(target.getName(), true);
            for (QName qname : subaspects)
            {
                AspectDefinition current = dictionaryService.getAspect(qname);
                if (target.getName().equals(current.getName()) || current.getIncludedInSuperTypeQuery())
                {
                    Pair<Long, QName> pair = qnameDAO.getQName(qname);
                    if (pair != null)
                    {
                        Long qnameId = pair.getFirst();
                        qnameIds.add(qnameId);
                    }
                }
            }
        }
        return qnameIds;
    }

    public static String[] toStringValues(Collection<?> objects)
    {
        String[] answer = new String[objects.size()];
        int i = 0;
        for (Iterator<?> it = objects.iterator(); it.hasNext(); /**/)
        {
            answer[i++] = it.next().toString();
        }
        return answer;
    }
    
    public static String[] toStringValues(Object[] objects)
    {
        String[] answer = new String[objects.length];
        for (int i = 0; i < objects.length; i++)
        {
            answer[i] = objects[i].toString();
        }
        return answer;
    }

    public static String expandQName(String toStrip, NamespacePrefixResolver namespacePrefixResolver)
    {
        String qnameString;
        if(toStrip.endsWith(".size"))
        {
            qnameString = toStrip.substring(0, toStrip.length()-5);
        }
        else if(toStrip.endsWith(".mimetype"))
        {
            qnameString = toStrip.substring(0, toStrip.length()-9);
        }
        else
        {
            qnameString = toStrip;
        }
        
        
        String fieldName = qnameString;
        if(fieldName.startsWith("@"))
        {
            fieldName = fieldName.substring(1);
        }
        // Check for any prefixes and expand to the full uri
        if (fieldName.charAt(0) != '{')
        {
            int colonPosition = fieldName.indexOf(':');
            if (colonPosition == -1)
            {
                
                
                // use the default namespace
                fieldName = "{" + NamespaceService.CONTENT_MODEL_1_0_URI + "}" + fieldName;
            }
            else
            {
                String prefix = fieldName.substring(0, colonPosition);
                String uri = matchURI(prefix, namespacePrefixResolver);
                if (uri == null)
                {
                    fieldName = "{" + NamespaceService.CONTENT_MODEL_1_0_URI + "}" + fieldName;
                }
                else
                {
                    fieldName = "{" + uri + "}" + fieldName.substring(colonPosition + 1);
                }

            }
        }
        return fieldName;
    }

    public static String matchURI(String prefix, NamespacePrefixResolver namespacePrefixResolver)
    {
        HashSet<String> prefixes = new HashSet<String>(namespacePrefixResolver.getPrefixes());
        if (prefixes.contains(prefix))
        {
            return namespacePrefixResolver.getNamespaceURI(prefix);
        }
        String match = null;
        for (String candidate : prefixes)
        {
            if (candidate.equalsIgnoreCase(prefix))
            {
                if (match == null)
                {
                    match = candidate;
                }
                else
                {

                    throw new QueryModelException("Ambiguous namespace prefix " + prefix);

                }
            }
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return namespacePrefixResolver.getNamespaceURI(match);
        }
    }

    public static TypeDefinition matchTypeDefinition(String string, NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService)
    {
        QName search = QName.createQName(expandQName(string, namespacePrefixResolver));
        TypeDefinition typeDefinition = dictionaryService.getType(search);
        QName match = null;
        if (typeDefinition == null)
        {
            for (QName definition : dictionaryService.getAllTypes())
            {
                if (definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if (definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if (match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new QueryModelException("Ambiguous data datype " + string);
                        }
                    }
                }
            }
        }
        else
        {
            return typeDefinition;
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getType(match);
        }
    }

    public static AspectDefinition matchAspectDefinition(String string, NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService)
    {
        QName search = QName.createQName(expandQName(string, namespacePrefixResolver));
        AspectDefinition aspectDefinition = dictionaryService.getAspect(search);
        QName match = null;
        if (aspectDefinition == null)
        {
            for (QName definition : dictionaryService.getAllAspects())
            {
                if (definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if (definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if (match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new QueryModelException("Ambiguous data datype " + string);
                        }
                    }
                }
            }
        }
        else
        {
            return aspectDefinition;
        }
        if (match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getAspect(match);
        }
    }

    /**
     * @param propertyQName
     * @return
     */
    public static DBQueryBuilderJoinCommandType getJoinCommandType(QName propertyQName)
    {
        if(propertyQName.equals(ContentModel.PROP_CREATED)
                || propertyQName.equals(ContentModel.PROP_CREATOR) || propertyQName.equals(ContentModel.PROP_MODIFIED) || propertyQName.equals(ContentModel.PROP_MODIFIER))
        {
            return DBQueryBuilderJoinCommandType.NODE;
        }
        else if(propertyQName.toString().endsWith(".mimetype"))
        {
            return DBQueryBuilderJoinCommandType.CONTENT_MIMETYPE;
        }
        else if(propertyQName.toString().endsWith(".size"))
        {
            return DBQueryBuilderJoinCommandType.CONTENT_URL;
        }
        else
        {
            return DBQueryBuilderJoinCommandType.PROPERTY;
        }
    }

    /**
     * @param dictionaryService
     * @param propertyQName
     * @return
     */
    public static String getFieldName(DictionaryService dictionaryService, QName propertyQName)
    {
        if (propertyQName.equals(ContentModel.PROP_CREATED))
        {
            return "audit_created";
        }
        else if (propertyQName.equals(ContentModel.PROP_CREATOR))
        {
            return "audit_creator";
        }
        else if (propertyQName.equals(ContentModel.PROP_MODIFIED))
        {
            return "audit_modified";
        }
        else if (propertyQName.equals(ContentModel.PROP_MODIFIER))
        {
            return "audit_modifier";
        }
        else
        {
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            if (propDef == null)
            {
                throw new QueryModelException("Unknown property " + propertyQName);
            }
            DataTypeDefinition dataType = propDef.getDataType();
            if (dataType.getName().equals(DataTypeDefinition.ASSOC_REF))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.CATEGORY))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.DATE))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.DATETIME))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.INT))
            {
                return "long_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.LOCALE))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.LONG))
            {
                return "long_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.MLTEXT))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.NODE_REF))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.PERIOD))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.QNAME))
            {
                return "string_value";
            }
            else if (dataType.getName().equals(DataTypeDefinition.TEXT))
            {
                return "string_value";
            }
            else
            {
                throw new QueryModelException("Unsupported property type " + dataType.getName());
            }
        }
    }

    public static DataTypeDefinition getDataTypeDefinition(DictionaryService dictionaryService, QName propertyQname)
    {
        if(propertyQname == null)
        {
            return null;
        }
        PropertyDefinition propDef = dictionaryService.getProperty(propertyQname);
        if(propDef == null)
        {
            return null;
        }
        return propDef.getDataType();
    }
    
    /**
     * @param stringValues
     * @param nodeDAO
     * @return
     */
    public static Long[] getDbids(String[] stringValues, NodeDAO nodeDAO, TenantService tenantService)
    {
        Long[] dbids = new Long[stringValues.length];
        for(int i = 0; i < stringValues.length; i++)
        {
            dbids[i] = getDbid(stringValues[i], nodeDAO, tenantService);
        }
        return dbids;
    }
}
