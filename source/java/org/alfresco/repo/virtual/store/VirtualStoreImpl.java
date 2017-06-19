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

package org.alfresco.repo.virtual.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.AlfrescoEnviroment;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.page.PageCollationException;
import org.alfresco.repo.virtual.page.PageCollator;
import org.alfresco.repo.virtual.page.PageCollator.PagingResultsSource;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.GetChildByIdMethod;
import org.alfresco.repo.virtual.ref.GetParentReferenceMethod;
import org.alfresco.repo.virtual.ref.GetReferenceType;
import org.alfresco.repo.virtual.ref.GetTemplatePathMethod;
import org.alfresco.repo.virtual.ref.Protocol;
import org.alfresco.repo.virtual.ref.ProtocolMethodException;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.ReferenceEncodingException;
import org.alfresco.repo.virtual.ref.ReferenceParseException;
import org.alfresco.repo.virtual.template.ApplyTemplateMethod;
import org.alfresco.repo.virtual.template.BasicConstraint;
import org.alfresco.repo.virtual.template.FilesFoldersConstraint;
import org.alfresco.repo.virtual.template.FilingData;
import org.alfresco.repo.virtual.template.FilingParameters;
import org.alfresco.repo.virtual.template.FilingRule;
import org.alfresco.repo.virtual.template.NamePatternPropertyValueConstraint;
import org.alfresco.repo.virtual.template.NullFilingRule;
import org.alfresco.repo.virtual.template.PropertyValueConstraint;
import org.alfresco.repo.virtual.template.VirtualFolderDefinition;
import org.alfresco.repo.virtual.template.VirtualQuery;
import org.alfresco.repo.virtual.template.VirtualQueryConstraint;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VirtualStoreImpl implements VirtualStore, VirtualFolderDefinitionResolver
{
    private static Log logger = LogFactory.getLog(VirtualStoreImpl.class);

    private static final String VIRTUAL_FOLDER_DEFINITION = "virtualfolder.definition";

    private List<VirtualizationMethod> virtualizationMethods = null;

    private ActualEnvironment environment;

    /** User permissions */
    private VirtualUserPermissions userPermissions;

    public void setVirtualizationMethods(List<VirtualizationMethod> methdods)
    {
        this.virtualizationMethods = methdods;
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    public boolean isVirtual(NodeRef nodeRef) throws VirtualizationException
    {
        if (Reference.isReference(nodeRef))
        {
            Protocol protocol = Reference.fromNodeRef(nodeRef).getProtocol();
            return Protocols.VIRTUAL.protocol.equals(protocol) || Protocols.VANILLA.protocol.equals(protocol);
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean canVirtualize(NodeRef nodeRef) throws VirtualizationException
    {
        String runAsUser = AuthenticationUtil.getRunAsUser();
        if (runAsUser == null)
        {
            if (logger.isTraceEnabled())
            {

                RuntimeException stackTracingException = new RuntimeException("Stack trace.");
                logger.trace("Virtualization check call in unauthenticated-context - stack trace follows:",
                             stackTracingException);
            }

            return false;
        }

        if (Reference.isReference(nodeRef))
        {
            return true;
        }
        else
        {
            for (VirtualizationMethod vMethod : virtualizationMethods)
            {
                if (vMethod.canVirtualize(environment,
                                          nodeRef))
                {
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    public boolean canMaterialize(Reference reference) throws VirtualizationException
    {
        // TODO: extract a protocol method, push into virtualization methods

        if (Protocols.NODE.protocol.equals(reference.getProtocol()))
        {
            return true;
        }
        else
        {
            String templatePath = reference.execute(new GetTemplatePathMethod());
            return templatePath.trim().equals("/");
        }
    }

    private NodeRef nodeProtocolNodeRef(NodeRef nodeRef) throws ProtocolMethodException, ReferenceParseException,
                ReferenceEncodingException
    {
        NodeRef theNodeRef = nodeRef;
        if (Reference.isReference(nodeRef))
        {
            Reference ref = Reference.fromNodeRef(theNodeRef);
            if (Protocols.NODE.protocol.equals(ref.getProtocol()))
            {
                theNodeRef = ref.execute(new GetActualNodeRefMethod(environment));
            }
        }
        return theNodeRef;
    }

    @Override
    public Reference virtualize(NodeRef nodeRef) throws VirtualizationException
    {
        Reference reference = null;

        if (isVirtual(nodeRef))
        {
            reference = Reference.fromNodeRef(nodeRef);
        }
        else
        {
            NodeRef theNodeRef = nodeProtocolNodeRef(nodeRef);

            for (VirtualizationMethod vMethod : virtualizationMethods)
            {
                if (vMethod.canVirtualize(environment,
                                          theNodeRef))
                {
                    reference = vMethod.virtualize(environment,
                                                   theNodeRef);
                }
            }

            if (reference == null)
            {
                if (Reference.isReference(nodeRef))
                {
                    reference = Reference.fromNodeRef(nodeRef);
                }
                else
                {
                    throw new VirtualizationException("No virtualization method for " + nodeRef);
                }
            }

        }

        return reference;
    }

    @Override
    public NodeRef materialize(Reference reference) throws VirtualizationException
    {
        return reference.execute(new GetActualNodeRefMethod(environment));
    }

    @Override
    public Collection<NodeRef> materializeIfPossible(Collection<NodeRef> nodeRefs) throws VirtualizationException
    {
        List<NodeRef> nodeRefList = new LinkedList<>();
        for (NodeRef nodeRef : nodeRefs)
        {
            nodeRefList.add(materializeIfPossible(nodeRef));
        }

        return nodeRefList;
    }

    @Override
    public NodeRef materializeIfPossible(NodeRef nodeRef) throws VirtualizationException
    {
        if (Reference.isReference(nodeRef))
        {
            Reference ref = Reference.fromNodeRef(nodeRef);
            if (canMaterialize(ref))
            {
                return materialize(ref);
            }

        }
        return nodeRef;
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(Reference parentReference, QNamePattern typeQNamePattern,
                QNamePattern qnamePattern, int maxResults, boolean preload) throws InvalidNodeRefException
    {
        if (typeQNamePattern.isMatch(ContentModel.ASSOC_CONTAINS))
        {
            return parentReference.execute(new GetChildAssocsMethod(this,
                                                                    environment,
                                                                    preload,
                                                                    maxResults,
                                                                    qnamePattern,
                                                                    typeQNamePattern));
        }
        else
        {
            return Collections.emptyList();
        }
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(Reference parentReference, Set<QName> childNodeTypeQNames)
    {
        List<ChildAssociationRef> allAssociations = getChildAssocs(parentReference,
                                                                   RegexQNamePattern.MATCH_ALL,
                                                                   RegexQNamePattern.MATCH_ALL,
                                                                   Integer.MAX_VALUE,
                                                                   false);

        List<ChildAssociationRef> associations = new LinkedList<>();

        for (ChildAssociationRef childAssociationRef : allAssociations)
        {
            QName childType = environment.getType(childAssociationRef.getChildRef());
            if (childNodeTypeQNames.contains(childType))
            {
                associations.add(childAssociationRef);
            }
        }

        return associations;
    }

    @Override
    public Collection<ChildAssociationRef> getChildAssocsWithoutParentAssocsOfType(Reference parentReference,
                QName assocTypeQName)
    {
        return Collections.emptyList();
    }

    @Override
    public List<ChildAssociationRef> getChildAssocsByPropertyValue(Reference parentReference, QName propertyQName,
                Serializable value)
    {
        List<ChildAssociationRef> allAssociations = getChildAssocs(parentReference,
                                                                   RegexQNamePattern.MATCH_ALL,
                                                                   RegexQNamePattern.MATCH_ALL,
                                                                   Integer.MAX_VALUE,
                                                                   false);

        List<ChildAssociationRef> associations = new LinkedList<>();

        for (ChildAssociationRef childAssociationRef : allAssociations)
        {
            Serializable propertyValue = environment.getProperty(childAssociationRef.getChildRef(),
                                                                 propertyQName);

            if ((value == null && propertyValue == null) || (value != null && value.equals(propertyValue)))
            {
                associations.add(childAssociationRef);
            }
        }

        return associations;
    }

    @Override
    public Reference getChildByName(Reference reference, QName assocTypeQName, String childName)
                throws VirtualizationException
    {
        VirtualFolderDefinition structure = resolveVirtualFolderDefinition(reference);
        VirtualFolderDefinition theChild = structure.findChildByName(childName);

        if (theChild != null)
        {
            return reference.execute(new GetChildByIdMethod(theChild.getId()));
        }
        else
        {
            final VirtualQuery query = structure.getQuery();

            if (query != null)
            {
                PropertyValueConstraint constraint = new PropertyValueConstraint(new FilesFoldersConstraint(BasicConstraint.INSTANCE,
                                                                                                            true,
                                                                                                            true),
                                                                                 ContentModel.PROP_NAME,
                                                                                 childName,
                                                                                 environment
                                                                                             .getNamespacePrefixResolver());
                PagingResults<Reference> result = query.perform(environment,
                                                                constraint,
                                                                null,
                                                                reference);
                List<Reference> page = result.getPage();

                return page == null || page.isEmpty() ? null : page.get(0);
            }
            else
            {
                return null;
            }
        }
    }

    private List<Reference> createChildReferences(Reference parent, VirtualFolderDefinition structure)
                throws ProtocolMethodException
    {

        List<VirtualFolderDefinition> structureChildren = structure.getChildren();
        List<Reference> childReferences = new LinkedList<Reference>();

        for (VirtualFolderDefinition child : structureChildren)
        {
            childReferences.add(parent.execute(new GetChildByIdMethod(child.getId())));
        }

        return childReferences;
    }

    public VirtualFolderDefinition resolveVirtualFolderDefinition(final Reference reference)
                throws VirtualizationException
    {
        ServiceRegistry serviceRegistry = ((AlfrescoEnviroment) environment).getServiceRegistry();
        RetryingTransactionHelper transactionHelper = serviceRegistry.getRetryingTransactionHelper();

        return transactionHelper.doInTransaction(new RetryingTransactionCallback<VirtualFolderDefinition>()
                                                 {

                                                     @Override
                                                     public VirtualFolderDefinition execute() throws Throwable
                                                     {
                                                         NodeRef key = reference.toNodeRef();

                                                         Map<NodeRef, VirtualFolderDefinition> definitionsCache = TransactionalResourceHelper
                                                                     .getMap(VIRTUAL_FOLDER_DEFINITION);

                                                         VirtualFolderDefinition virtualFolderDefinition = definitionsCache
                                                                     .get(key);

                                                         if (virtualFolderDefinition == null)
                                                         {

                                                             virtualFolderDefinition = reference
                                                                         .execute(new ApplyTemplateMethod(environment));
                                                             definitionsCache.put(key,
                                                                                  virtualFolderDefinition);
                                                         }

                                                         return virtualFolderDefinition;
                                                     }
                                                 },
                                                 true,
                                                 false);
    }

    @Override
    public PagingResults<Reference> list(final Reference ref, boolean actual, boolean virtual, final boolean files,
                final boolean folders, final String pattern, final Set<QName> searchTypeQNames,
                final Set<QName> ignoreTypeQNames, final Set<QName> ignoreAspectQNames,
                final List<Pair<QName, Boolean>> sortProps, final PagingRequest pagingRequest)
                throws VirtualizationException
    {

        VirtualFolderDefinition structure = resolveVirtualFolderDefinition(ref);

        List<Reference> virtualRefs = null;
        // TODO: this blocks file exclusive virtual listing - complex query
        // constraints should handle folder & file filtering
        if (virtual && folders)
        {
            virtualRefs = createChildReferences(ref,
                                                structure);
        }
        else
        {
            virtualRefs = Collections.emptyList();
        }

        if (actual)
        {
            final VirtualQuery query = structure.getQuery();
            if (query != null)
            {
                // we have a query we must collate results

                PagingResultsSource<Reference> querySourc = new PagingResultsSource<Reference>()
                {

                    @Override
                    public PagingResults<Reference> retrieve(PagingRequest pr) throws PageCollationException
                    {
                        try
                        {
                            return query.perform(environment,
                                                 files,
                                                 folders,
                                                 pattern,
                                                 searchTypeQNames,
                                                 ignoreTypeQNames,
                                                 ignoreAspectQNames,
                                                 sortProps,
                                                 pr,
                                                 ref);
                        }
                        catch (VirtualizationException e)
                        {
                            throw new PageCollationException(e);
                        }
                    }

                };

                PageCollator<Reference> collator = new PageCollator<>();

                try
                {
                    return collator.collate(virtualRefs,
                                            querySourc,
                                            pagingRequest,
                                            new ReferenceComparator(this,
                                                                    sortProps));
                }
                catch (PageCollationException e)
                {
                    throw new VirtualizationException(e);
                }
            }

        }

        return new ListBackedPagingResults<>(virtualRefs);
    }

    @Override
    public PagingResults<Reference> list(Reference ref, boolean actual, boolean virtual, boolean files,
                boolean folders, String pattern, Set<QName> ignoreTypeQNames, Set<QName> ignoreAspectQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest) throws VirtualizationException
    {
        return list(ref,
                    actual,
                    virtual,
                    files,
                    folders,
                    pattern,
                    Collections.<QName> emptySet(),
                    ignoreTypeQNames,
                    ignoreAspectQNames,
                    sortProps,
                    pagingRequest);
    }

    @Override
    public PagingResults<Reference> list(Reference ref, boolean actual, boolean virtual, Set<QName> searchTypeQNames,
                Set<QName> ignoreTypeQNames, Set<QName> ignoreAspectQNames, List<Pair<QName, Boolean>> sortProps,
                PagingRequest pagingRequest) throws VirtualizationException
    {
        // TODO: find null string value for pattern
        return list(ref,
                    actual,
                    virtual,
                    true,
                    true,
                    null,
                    searchTypeQNames,
                    Collections.<QName> emptySet(),
                    ignoreAspectQNames,
                    sortProps,
                    pagingRequest);
    }

    @Override
    public List<Reference> list(Reference reference) throws VirtualizationException
    {
        VirtualFolderDefinition structure = resolveVirtualFolderDefinition(reference);
        List<Reference> result = createChildReferences(reference,
                                                       structure);
        final VirtualQuery query = structure.getQuery();
        if (query != null)
        {
            PagingResults<Reference> queryNodes = query.perform(environment,
                                                                new FilesFoldersConstraint(BasicConstraint.INSTANCE,
                                                                                           true,
                                                                                           true),
                                                                null,
                                                                reference);
            result.addAll(queryNodes.getPage());

        }

        return result;
    }

    @Override
    public List<Reference> search(Reference reference, String namePattern, boolean fileSearch, boolean folderSearch,
                boolean includeSubFolders) throws VirtualizationException
    {
        VirtualFolderDefinition structure = resolveVirtualFolderDefinition(reference);
        List<Reference> result = new LinkedList<Reference>();
        List<Reference> childReferences = createChildReferences(reference,
                                                                structure);
        if (folderSearch)
        {
            result.addAll(childReferences);
        }
        if (includeSubFolders)
        {
            for (Reference childRef : childReferences)
            {
                List<Reference> childResults = search(childRef,
                                                      namePattern,
                                                      fileSearch,
                                                      folderSearch,
                                                      includeSubFolders);
                result.addAll(childResults);
            }
        }
        if (fileSearch)
        {
            final VirtualQuery query = structure.getQuery();
            if (query != null)
            {
                VirtualQueryConstraint vqConstraint = null;
                if (namePattern == null)
                {
                    vqConstraint = BasicConstraint.INSTANCE;
                }
                else
                {
                    vqConstraint = new NamePatternPropertyValueConstraint(new FilesFoldersConstraint(BasicConstraint.INSTANCE,
                                                                                                     true,
                                                                                                     true),
                                                                          ContentModel.PROP_NAME,
                                                                          namePattern,
                                                                          environment.getNamespacePrefixResolver());
                }
                PagingResults<Reference> queryNodes = query.perform(environment,
                                                                    vqConstraint,
                                                                    null,
                                                                    reference);
                result.addAll(queryNodes.getPage());
            }
        }
        return result;
    }

    @Override
    public Map<QName, Serializable> getProperties(Reference reference) throws VirtualizationException
    {
        final Protocol protocol = reference.getProtocol();
        if (Protocols.VIRTUAL.protocol.equals(protocol) || Protocols.VANILLA.protocol.equals(protocol))
        {

            VirtualFolderDefinition folderDefinition = resolveVirtualFolderDefinition(reference);

            Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

            // We first set default property values. They might be overridden by
            // folder definition properties.

            properties.put(ContentModel.PROP_NAME,
                           folderDefinition.getName());

            StoreRef storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

            properties.put(ContentModel.PROP_STORE_IDENTIFIER,
                           storeRef.getIdentifier());

            properties.put(ContentModel.PROP_STORE_PROTOCOL,
                           storeRef.getProtocol());

            properties.put(ContentModel.PROP_LOCALE,
                           Locale.UK.toString());


            properties.put(ContentModel.PROP_MODIFIED,
                           new Date());
            properties.put(ContentModel.PROP_MODIFIER,
                           AuthenticationUtil.SYSTEM_USER_NAME);
            properties.put(ContentModel.PROP_CREATED,
                           new Date());
            properties.put(ContentModel.PROP_CREATOR,
                           AuthenticationUtil.SYSTEM_USER_NAME);

            properties.put(ContentModel.PROP_NODE_DBID,
                           0);

            properties.put(ContentModel.PROP_DESCRIPTION,
                           folderDefinition.getDescription());
            // ACE-5303 : ContentModel.PROP_TITLE remains unset 

            // We add virtual folder definition structure properties. They might
            // override the above defaults.

            Map<String, String> nodeProperties = folderDefinition.getProperties();

            if (nodeProperties != null)
            {
                Set<Entry<String, String>> propertyEntries = nodeProperties.entrySet();
                NamespacePrefixResolver nsPrefixResolver = environment.getNamespacePrefixResolver();

                for (Entry<String, String> propertyValueEntry : propertyEntries)
                {
                    QName propertyQName = QName.createQName(propertyValueEntry.getKey(),
                                                            nsPrefixResolver);
                    properties.put(propertyQName,
                                   propertyValueEntry.getValue().toString());
                }
            }

            return properties;
        }
        else
        {
            NodeRef actual = reference.execute(new GetActualNodeRefMethod(environment));
            Map<QName, Serializable> properties = environment.getProperties(actual);
            properties.put(VirtualContentModel.PROP_ACTUAL_NODE_REF,
                           actual.toString());
            return properties;

        }
    }

    @Override
    public QName getType(Reference ref) throws VirtualizationException
    {
        return ref.execute(new GetReferenceType(environment));
    }

    @Override
    public FilingData createFilingData(Reference parentReference, QName assocTypeQName, QName assocQName,
                QName nodeTypeQName, Map<QName, Serializable> properties) throws VirtualizationException
    {
        VirtualFolderDefinition structure = resolveVirtualFolderDefinition(parentReference);
        FilingRule filingRule = structure.getFilingRule();
        if (filingRule == null)
        {
            filingRule = new NullFilingRule(environment);
        }

        FilingParameters filingParameters = new FilingParameters(parentReference,
                                                                 assocTypeQName,
                                                                 assocQName,
                                                                 nodeTypeQName,
                                                                 properties);
        FilingData filingData = filingRule.createFilingData(filingParameters);

        return filingData;
    }

    @Override
    public AccessStatus hasPermission(Reference reference, String perm) throws VirtualizationException
    {
        return reference.execute(new HasPermissionMethod(this,
                                                         userPermissions,
                                                         perm));
    }

    @Override
    public AccessStatus hasPermission(Reference reference, PermissionReference perm) throws VirtualizationException
    {
        return hasPermission(reference,
                             perm.getName());
    }

    /**
     * @param userPermissions user permissions
     */
    public void setUserPermissions(VirtualUserPermissions userPermissions)
    {
        this.userPermissions = userPermissions;
    }

    /**
     * @return a {@link VirtualUserPermissions} clone
     */
    public VirtualUserPermissions getUserPermissions()
    {
        return new VirtualUserPermissions(this.userPermissions);
    }

    @Override
    public NodePermissionEntry getSetPermissions(Reference reference) throws VirtualizationException
    {
        return reference.execute(new GetSetPermissionsMethod(this,
                                                             userPermissions,
                                                             PermissionService.ALL_AUTHORITIES));
    }

    @Override
    public Set<AccessPermission> getAllSetPermissions(Reference reference)
    {
        return reference.execute(new GetAllSetPermissionsMethod(this,
                                                                userPermissions,
                                                                PermissionService.ALL_AUTHORITIES));
    }

    @Override
    public Path getPath(Reference reference) throws VirtualizationException
    {
        Reference virtualPathElement = reference;
        Reference virtualPathParent = reference.execute(new GetParentReferenceMethod());

        Path virtualPath = new Path();

        while (virtualPathElement != null && virtualPathParent != null)
        {
            NodeRef parentNodeRef;

            parentNodeRef = virtualPathParent.toNodeRef();

            NodeRef parent = parentNodeRef;

            NodeRef virtualPathNodeRef = virtualPathElement.toNodeRef();

            // TODO: extract node reference name into protocol method in order
            // to enforce path processing code reuse and consistency

            String templatePath = virtualPathElement.execute(new GetTemplatePathMethod()).trim();
            final String pathSeparator = "/";
            if (pathSeparator.equals(templatePath))
            {
                // found root
                break;
            }
            else if (templatePath.endsWith(pathSeparator))
            {
                templatePath = templatePath.substring(0,
                                                      templatePath.length() - 1);
            }
            int lastSeparator = templatePath.lastIndexOf(pathSeparator);
            String childId = templatePath.substring(lastSeparator + 1);
            VirtualFolderDefinition structure = resolveVirtualFolderDefinition(virtualPathParent);
            VirtualFolderDefinition child = structure.findChildById(childId);
            if (child == null)
            {
                throw new VirtualizationException("Invalid reference: " + reference.encode());
            }
            String childName = child.getName();
            QName childQName = QName.createQName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI,
                                                 childName);

            ChildAssociationRef assocRef = new ChildAssociationRef(ContentModel.ASSOC_CHILDREN,
                                                                   parent,
                                                                   childQName,
                                                                   virtualPathNodeRef,
                                                                   true,
                                                                   -1);
            ChildAssocElement assocRefElement = new ChildAssocElement(assocRef);
            virtualPath.prepend(assocRefElement);

            virtualPathElement = virtualPathParent;
            virtualPathParent = virtualPathParent.execute(new GetParentReferenceMethod());
        }

        return virtualPath;
    }

    @Override
    public NodeRef adhere(Reference reference, int mode) throws VirtualizationException
    {
        switch (mode)
        {
            case MATERIAL_ADHERENCE:
            {
                return materialize(reference);
            }
            case FILING_OR_MATERIAL_ADHERENCE:
            {
                VirtualFolderDefinition vfDefinition = resolveVirtualFolderDefinition(reference);
                FilingRule filingRule = vfDefinition.getFilingRule();
                if (filingRule.isNullFilingRule())
                {
                    return materialize(reference);
                }
                else
                {
                    return filingRule.filingNodeRefFor(new FilingParameters(reference));
                }
            }

            default:
                throw new VirtualizationException("Invalid adherence mode " + mode);
        }
    }

}
