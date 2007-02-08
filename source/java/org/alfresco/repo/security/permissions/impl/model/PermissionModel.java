/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.permissions.impl.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.RequiredPermission;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;

/**
 * The implementation of the model DAO Reads and stores the top level model information Encapsulates access to this
 * information
 * 
 * @author andyh
 */
public class PermissionModel implements ModelDAO, InitializingBean
{
    // IOC

    private NodeService nodeService;

    private DictionaryService dictionaryService;

    // XML Constants

    private static final String NAMESPACES = "namespaces";

    private static final String NAMESPACE = "namespace";

    private static final String NAMESPACE_URI = "uri";

    private static final String NAMESPACE_PREFIX = "prefix";

    private static final String PERMISSION_SET = "permissionSet";

    private static final String GLOBAL_PERMISSION = "globalPermission";

    private static final String DENY = "deny";

    private static final String ALLOW = "allow";

    private static final String DEFAULT_PERMISSION = "defaultPermission";

    // Instance variables

    private String model;

    
    // Aprrox 6 - default size OK
    private Map<QName, PermissionSet> permissionSets = new HashMap<QName, PermissionSet>();

    // Global permissions - default size OK
    private Set<GlobalPermissionEntry> globalPermissions = new HashSet<GlobalPermissionEntry>();

    private AccessStatus defaultPermission;

    // Cache granting permissions
    private HashMap<PermissionReference, Set<PermissionReference>> grantingPermissions = new HashMap<PermissionReference, Set<PermissionReference>>(128, 1.0f);

    // Cache grantees
    private HashMap<PermissionReference, Set<PermissionReference>> granteePermissions = new HashMap<PermissionReference, Set<PermissionReference>>(128, 1.0f);

    // Cache the mapping of extended groups to the base
    private HashMap<PermissionGroup, PermissionGroup> groupsToBaseGroup = new HashMap<PermissionGroup, PermissionGroup>(128, 1.0f);

    private HashMap<String, PermissionReference> uniqueMap;

    private HashMap<PermissionReference, Permission> permissionMap;

    private HashMap<PermissionReference, PermissionGroup> permissionGroupMap;

    private HashMap<String, PermissionReference> permissionReferenceMap;

    private Map<QName, LinkedHashSet<PermissionReference>> cachedTypePermissionsExposed = new HashMap<QName, LinkedHashSet<PermissionReference>>(
            128, 1.0f);

    private Map<QName, LinkedHashSet<PermissionReference>> cachedTypePermissionsUnexposed = new HashMap<QName, LinkedHashSet<PermissionReference>>(
            128, 1.0f);

    public PermissionModel()
    {
        super();
    }

    // IOC

    public void setModel(String model)
    {
        this.model = model;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /*
     * Initialise from file (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */

    public void afterPropertiesSet()
    {
        Document document = createDocument(model);
        Element root = document.getRootElement();

        Attribute defaultPermissionAttribute = root.attribute(DEFAULT_PERMISSION);
        if (defaultPermissionAttribute != null)
        {
            if (defaultPermissionAttribute.getStringValue().equalsIgnoreCase(ALLOW))
            {
                defaultPermission = AccessStatus.ALLOWED;
            }
            else if (defaultPermissionAttribute.getStringValue().equalsIgnoreCase(DENY))
            {
                defaultPermission = AccessStatus.DENIED;
            }
            else
            {
                throw new PermissionModelException("The default permission must be deny or allow");
            }
        }
        else
        {
            defaultPermission = AccessStatus.DENIED;
        }

        DynamicNamespacePrefixResolver nspr = new DynamicNamespacePrefixResolver();

        // Namespaces

        for (Iterator nsit = root.elementIterator(NAMESPACES); nsit.hasNext(); /**/)
        {
            Element namespacesElement = (Element) nsit.next();
            for (Iterator it = namespacesElement.elementIterator(NAMESPACE); it.hasNext(); /**/)
            {
                Element nameSpaceElement = (Element) it.next();
                nspr.registerNamespace(nameSpaceElement.attributeValue(NAMESPACE_PREFIX), nameSpaceElement
                        .attributeValue(NAMESPACE_URI));
            }
        }

        // Permission Sets

        for (Iterator psit = root.elementIterator(PERMISSION_SET); psit.hasNext(); /**/)
        {
            Element permissionSetElement = (Element) psit.next();
            PermissionSet permissionSet = new PermissionSet();
            permissionSet.initialise(permissionSetElement, nspr, this);

            permissionSets.put(permissionSet.getQName(), permissionSet);
        }

        buildUniquePermissionMap();

        // NodePermissions

        for (Iterator npit = root.elementIterator(GLOBAL_PERMISSION); npit.hasNext(); /**/)
        {
            Element globalPermissionElement = (Element) npit.next();
            GlobalPermissionEntry globalPermission = new GlobalPermissionEntry();
            globalPermission.initialise(globalPermissionElement, nspr, this);

            globalPermissions.add(globalPermission);
        }
    }

    /*
     * Create the XML document from the file location
     */
    private Document createDocument(String model)
    {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(model);
        if (is == null)
        {
            throw new PermissionModelException("File not found: " + model);
        }
        SAXReader reader = new SAXReader();
        try
        {
            Document document = reader.read(is);
            is.close();
            return document;
        }
        catch (DocumentException e)
        {
            throw new PermissionModelException("Failed to create permission model document ", e);
        }
        catch (IOException e)
        {
            throw new PermissionModelException("Failed to close permission model document ", e);
        }

    }

    public AccessStatus getDefaultPermission()
    {
        return defaultPermission;
    }

    public AccessStatus getDefaultPermission(PermissionReference pr)
    {
        Permission p = permissionMap.get(pr);
        if (p == null)
        {
            return defaultPermission;
        }
        else
        {
            return p.getDefaultPermission();
        }
    }

    public Set<? extends PermissionEntry> getGlobalPermissionEntries()
    {
        return Collections.unmodifiableSet(globalPermissions);
    }

    public Map<QName, PermissionSet> getPermissionSets()
    {
        return Collections.unmodifiableMap(permissionSets);
    }

    public Set<PermissionReference> getAllPermissions(QName type)
    {
        return getAllPermissionsImpl(type, false);
    }

    public Set<PermissionReference> getExposedPermissions(QName type)
    {
        return getAllPermissionsImpl(type, true);
    }

    @SuppressWarnings("unchecked")
    private Set<PermissionReference> getAllPermissionsImpl(QName type, boolean exposedOnly)
    {
        Map<QName, LinkedHashSet<PermissionReference>> cache;
        if (exposedOnly)
        {
            cache = this.cachedTypePermissionsExposed;
        }
        else
        {
            cache = this.cachedTypePermissionsUnexposed;
        }
        LinkedHashSet<PermissionReference> permissions = cache.get(type);
        if (permissions == null)
        {
            permissions = new LinkedHashSet<PermissionReference>(128, 1.0f);
            ClassDefinition cd = dictionaryService.getClass(type);
            if (cd != null)
            {
                if (cd.isAspect())
                {
                    addAspectPermissions(type, permissions, exposedOnly);
                }
                else
                {
                    mergeGeneralAspectPermissions(permissions, exposedOnly);
                    addTypePermissions(type, permissions, exposedOnly);
                }
            }
            cache.put(type, permissions);
        }
        return (Set<PermissionReference>) permissions.clone();
    }

    /**
     * Support to add permissions for types
     * 
     * @param type
     * @param permissions
     */
    private void addTypePermissions(QName type, Set<PermissionReference> permissions, boolean exposedOnly)
    {
        TypeDefinition typeDef = dictionaryService.getType(type);
        if (typeDef == null)
        {
            // the type definition is no longer in the dictionary - ignore
            return;
        }
        if (typeDef.getParentName() != null)
        {
            PermissionSet permissionSet = permissionSets.get(type);
            if (!exposedOnly || (permissionSet == null) || permissionSet.exposeAll())
            {
                addTypePermissions(typeDef.getParentName(), permissions, exposedOnly);
            }
        }
        for (AspectDefinition ad : typeDef.getDefaultAspects())
        {
            addAspectPermissions(ad.getName(), permissions, exposedOnly);
        }
        mergePermissions(permissions, type, exposedOnly, true);
    }

    /**
     * Support to add permissions for aspects.
     * 
     * @param type
     * @param permissions
     */
    private void addAspectPermissions(QName type, Set<PermissionReference> permissions, boolean exposedOnly)
    {
        AspectDefinition aspectDef = dictionaryService.getAspect(type);
        if (aspectDef == null)
        {
            // the aspect definition is no longer in the dictionary - ignore
            return;
        }
        if (aspectDef.getParentName() != null)
        {
            PermissionSet permissionSet = permissionSets.get(type);
            if (!exposedOnly || (permissionSet == null) || permissionSet.exposeAll())
            {
                addAspectPermissions(aspectDef.getParentName(), permissions, exposedOnly);
            }
        }
        mergePermissions(permissions, type, exposedOnly, true);
    }

    /**
     * Support to merge permissions together. Respects extended permissions.
     * 
     * @param target
     * @param type
     */
    private void mergePermissions(Set<PermissionReference> target, QName type, boolean exposedOnly, boolean typeRequired)
    {
        PermissionSet permissionSet = permissionSets.get(type);
        if (permissionSet != null)
        {
            for (PermissionGroup pg : permissionSet.getPermissionGroups())
            {
                if (!exposedOnly || permissionSet.exposeAll() || pg.isExposed())
                {
                    if (!pg.isExtends())
                    {
                        if (pg.isTypeRequired() == typeRequired)
                        {
                            target.add(pg);
                        }
                    }
                    else if (exposedOnly)
                    {
                        if (pg.isTypeRequired() == typeRequired)
                        {
                            target.add(getBasePermissionGroup(pg));
                        }
                    }
                }
            }
            for (Permission p : permissionSet.getPermissions())
            {
                if (!exposedOnly || permissionSet.exposeAll() || p.isExposed())
                {
                    if (p.isTypeRequired() == typeRequired)
                    {
                        target.add(p);
                    }
                }
            }
        }
    }

    private void mergeGeneralAspectPermissions(Set<PermissionReference> target, boolean exposedOnly)
    {
        for (QName aspect : dictionaryService.getAllAspects())
        {
            mergePermissions(target, aspect, exposedOnly, false);
        }
    }

    public Set<PermissionReference> getAllPermissions(NodeRef nodeRef)
    {
        return getExposedPermissionsImpl(nodeRef, false);
    }

    public Set<PermissionReference> getExposedPermissions(NodeRef nodeRef)
    {
        return getExposedPermissionsImpl(nodeRef, true);
    }

    public Set<PermissionReference> getExposedPermissionsImpl(NodeRef nodeRef, boolean exposedOnly)
    {
        //
        // TODO: cache permissions based on type and exposed flag
        // create JMeter test to see before/after effect!
        //
        QName typeName = nodeService.getType(nodeRef);

        Set<PermissionReference> permissions = getAllPermissionsImpl(typeName, exposedOnly);
        mergeGeneralAspectPermissions(permissions, exposedOnly);
        // Add non mandatory aspects...
        Set<QName> defaultAspects = new HashSet<QName>();
        for (AspectDefinition aspDef : dictionaryService.getType(typeName).getDefaultAspects())
        {
            defaultAspects.add(aspDef.getName());
        }
        for (QName aspect : nodeService.getAspects(nodeRef))
        {
            if (!defaultAspects.contains(aspect))
            {
                addAspectPermissions(aspect, permissions, exposedOnly);
            }
        }
        return permissions;
    }

    public synchronized Set<PermissionReference> getGrantingPermissions(PermissionReference permissionReference)
    {
        // Cache the results
        Set<PermissionReference> granters = grantingPermissions.get(permissionReference);
        if (granters == null)
        {
            granters = getGrantingPermissionsImpl(permissionReference);
            grantingPermissions.put(permissionReference, granters);
        }
        return granters;
    }

    private Set<PermissionReference> getGrantingPermissionsImpl(PermissionReference permissionReference)
    {
        // Query the model
        HashSet<PermissionReference> permissions = new HashSet<PermissionReference>(128, 1.0f);
        permissions.add(permissionReference);
        for (PermissionSet ps : permissionSets.values())
        {
            for (PermissionGroup pg : ps.getPermissionGroups())
            {
                if (grants(pg, permissionReference))
                {
                    permissions.add(getBasePermissionGroup(pg));
                }
                if (pg.isAllowFullControl())
                {
                    permissions.add(pg);
                }
            }
            for (Permission p : ps.getPermissions())
            {
                if (p.equals(permissionReference))
                {
                    for (PermissionReference pg : p.getGrantedToGroups())
                    {
                        permissions.add(getBasePermissionGroup(getPermissionGroup(pg)));
                    }
                }
                for (RequiredPermission rp : p.getRequiredPermissions())
                {
                    if (rp.equals(permissionReference) && rp.isImplies())
                    {
                        permissions.add(p);
                        break;
                    }
                }
            }
        }
        return permissions;
    }

    private boolean grants(PermissionGroup pg, PermissionReference permissionReference)
    {
        if (pg.getIncludedPermissionGroups().contains(permissionReference))
        {
            return true;
        }
        if (getGranteePermissions(pg).contains(permissionReference))
        {
            return true;
        }
        for (PermissionReference nested : pg.getIncludedPermissionGroups())
        {
            if (grants(getPermissionGroup(nested), permissionReference))
            {
                return true;
            }
        }
        return false;
    }

    public synchronized Set<PermissionReference> getGranteePermissions(PermissionReference permissionReference)
    {
        // Cache the results
        Set<PermissionReference> grantees = granteePermissions.get(permissionReference);
        if (grantees == null)
        {
            grantees = getGranteePermissionsImpl(permissionReference);
            granteePermissions.put(permissionReference, grantees);
        }
        return grantees;
    }

    private Set<PermissionReference> getGranteePermissionsImpl(PermissionReference permissionReference)
    {
        // Query the model
        HashSet<PermissionReference> permissions = new HashSet<PermissionReference>(128, 1.0f);
        permissions.add(permissionReference);
        for (PermissionSet ps : permissionSets.values())
        {
            for (PermissionGroup pg : ps.getPermissionGroups())
            {
                if (pg.equals(permissionReference))
                {
                    for (PermissionReference included : pg.getIncludedPermissionGroups())
                    {
                        permissions.addAll(getGranteePermissions(included));
                    }

                    if (pg.isExtends())
                    {
                        if (pg.getTypeQName() != null)
                        {
                            permissions.addAll(getGranteePermissions(new SimplePermissionReference(pg.getTypeQName(),
                                    pg.getName())));
                        }
                        else
                        {
                            ClassDefinition classDefinition = dictionaryService.getClass(pg.getQName());
                            QName parent = classDefinition.getParentName();
                            if (parent != null)
                            {
                                classDefinition = dictionaryService.getClass(parent);
                                PermissionGroup attempt = getPermissionGroupOrNull(new SimplePermissionReference(
                                        parent, pg.getName()));
                                if (attempt != null)
                                {
                                    permissions.addAll(getGranteePermissions(attempt));
                                }
                            }
                        }
                    }

                    if (pg.isAllowFullControl())
                    {
                        // add all available
                        permissions.addAll(getAllPermissions());
                    }
                }
            }
            PermissionGroup baseGroup = getBasePermissionGroupOrNull(getPermissionGroupOrNull(permissionReference));
            if (baseGroup != null)
            {
                for (Permission p : ps.getPermissions())
                {
                    for (PermissionReference grantedTo : p.getGrantedToGroups())
                    {
                        PermissionGroup base = getBasePermissionGroupOrNull(getPermissionGroupOrNull(grantedTo));
                        if (baseGroup.equals(base))
                        {
                            permissions.add(p);
                        }
                    }
                }
            }
        }
        return permissions;
    }

    private Set<PermissionReference> getAllPermissions()
    {
        HashSet<PermissionReference> permissions = new HashSet<PermissionReference>(128, 1.0f);
        for (PermissionSet ps : permissionSets.values())
        {
            for (PermissionGroup pg : ps.getPermissionGroups())
            {
                permissions.add(pg);
            }
            for (Permission p : ps.getPermissions())
            {
                permissions.add(p);
            }
        }
        return permissions;
    }

    /**
     * Support to find permission groups
     * 
     * @param target
     * @return
     */
    private PermissionGroup getPermissionGroupOrNull(PermissionReference target)
    {
        PermissionGroup pg = permissionGroupMap.get(target);
        return pg == null ? null : pg;
    }

    /**
     * Support to get a permission group
     * 
     * @param target
     * @return
     */
    private PermissionGroup getPermissionGroup(PermissionReference target)
    {
        PermissionGroup pg = getPermissionGroupOrNull(target);
        if (pg == null)
        {
            throw new PermissionModelException("There is no permission group :"
                    + target.getQName() + " " + target.getName());
        }
        return pg;
    }

    /**
     * Get the base permission group for a given permission group.
     * 
     * @param pg
     * @return
     */
    private synchronized PermissionGroup getBasePermissionGroupOrNull(PermissionGroup pg)
    {
        PermissionGroup permissionGroup = groupsToBaseGroup.get(pg);
        if (permissionGroup == null)
        {
            permissionGroup = getBasePermissionGroupOrNullImpl(pg);
            groupsToBaseGroup.put(pg, permissionGroup);
        }
        return permissionGroup;
    }

    /**
     * Query the model for a base permission group Uses the Data Dictionary to reolve inheritance
     * 
     * @param pg
     * @return
     */
    private PermissionGroup getBasePermissionGroupOrNullImpl(PermissionGroup pg)
    {
        if (pg == null)
        {
            return null;
        }
        if (pg.isExtends())
        {
            if (pg.getTypeQName() != null)
            {
                return getPermissionGroup(new SimplePermissionReference(pg.getTypeQName(), pg.getName()));
            }
            else
            {
                ClassDefinition classDefinition = dictionaryService.getClass(pg.getQName());
                QName parent;
                while ((parent = classDefinition.getParentName()) != null)
                {
                    classDefinition = dictionaryService.getClass(parent);
                    PermissionGroup attempt = getPermissionGroupOrNull(new SimplePermissionReference(parent, pg
                            .getName()));
                    if ((attempt != null) && (!attempt.isExtends()))
                    {
                        return attempt;
                    }
                }
                return null;
            }
        }
        else
        {
            return pg;
        }
    }

    private PermissionGroup getBasePermissionGroup(PermissionGroup target)
    {
        PermissionGroup pg = getBasePermissionGroupOrNull(target);
        if (pg == null)
        {
            throw new PermissionModelException("There is no parent for permission group :"
                    + target.getQName() + " " + target.getName());
        }
        return pg;
    }

    public Set<PermissionReference> getRequiredPermissions(PermissionReference required, QName qName,
            Set<QName> aspectQNames, RequiredPermission.On on)
    {
        PermissionGroup pg = getBasePermissionGroupOrNull(getPermissionGroupOrNull(required));
        if (pg == null)
        {
            return getRequirementsForPermission(required, on);
        }
        else
        {
            return getRequirementsForPermissionGroup(pg, on, qName, aspectQNames);
        }
    }

    /**
     * Get the requirements for a permission
     * 
     * @param required
     * @param on
     * @return
     */
    private Set<PermissionReference> getRequirementsForPermission(PermissionReference required, RequiredPermission.On on)
    {
        HashSet<PermissionReference> requiredPermissions = new HashSet<PermissionReference>();
        Permission p = getPermissionOrNull(required);
        if (p != null)
        {
            for (RequiredPermission rp : p.getRequiredPermissions())
            {
                if (!rp.isImplies() && rp.getOn().equals(on))
                {
                    requiredPermissions.add(rp);
                }
            }
        }
        return requiredPermissions;
    }

    /**
     * Get the requirements for a permission set
     * 
     * @param target
     * @param on
     * @param qName
     * @param aspectQNames
     * @return
     */
    private Set<PermissionReference> getRequirementsForPermissionGroup(PermissionGroup target,
            RequiredPermission.On on, QName qName, Set<QName> aspectQNames)
    {
        HashSet<PermissionReference> requiredPermissions = new HashSet<PermissionReference>(8, 1.0f);
        if (target == null)
        {
            return requiredPermissions;
        }
        for (PermissionSet ps : permissionSets.values())
        {
            for (PermissionGroup pg : ps.getPermissionGroups())
            {
                PermissionGroup base = getBasePermissionGroupOrNull(pg);
                if (target.equals(base)
                        && (!base.isTypeRequired() || isPartOfDynamicPermissionGroup(pg, qName, aspectQNames)))
                {
                    // Add includes
                    for (PermissionReference pr : pg.getIncludedPermissionGroups())
                    {
                        requiredPermissions.addAll(getRequirementsForPermissionGroup(
                                getBasePermissionGroupOrNull(getPermissionGroupOrNull(pr)), on, qName, aspectQNames));
                    }
                }
            }
            for (Permission p : ps.getPermissions())
            {
                for (PermissionReference grantedTo : p.getGrantedToGroups())
                {
                    PermissionGroup base = getBasePermissionGroupOrNull(getPermissionGroupOrNull(grantedTo));
                    if (target.equals(base)
                            && (!base.isTypeRequired() || isPartOfDynamicPermissionGroup(grantedTo, qName, aspectQNames)))
                    {
                        if (on == RequiredPermission.On.NODE)
                        {
                            requiredPermissions.add(p);
                        }
                    }
                }
            }
        }
        return requiredPermissions;
    }

    /**
     * Check type specifc extension of permission sets.
     * 
     * @param pr
     * @param typeQname
     * @param aspects
     * @return
     */
    private boolean isPartOfDynamicPermissionGroup(PermissionReference pr, QName typeQname, Set<QName> aspects)
    {
        if (dictionaryService.isSubClass(typeQname, pr.getQName()))
        {
            return true;
        }
        for (QName aspect : aspects)
        {
            if (dictionaryService.isSubClass(aspect, pr.getQName()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Utility method to find a permission
     * 
     * @param perm
     * @return
     */
    private Permission getPermissionOrNull(PermissionReference perm)
    {
        Permission p = permissionMap.get(perm);
        return p == null ? null : p;
    }

    public boolean checkPermission(PermissionReference required)
    {
        Permission permission = getPermissionOrNull(required);
        if (permission != null)
        {
            return true;
        }
        PermissionGroup pg = getPermissionGroupOrNull(required);
        if (pg != null)
        {
            if (pg.isExtends())
            {
                if (pg.getTypeQName() != null)
                {
                    return checkPermission(new SimplePermissionReference(pg.getTypeQName(), pg.getName()));
                }
                else
                {
                    ClassDefinition classDefinition = dictionaryService.getClass(pg.getQName());
                    QName parent;
                    while ((parent = classDefinition.getParentName()) != null)
                    {
                        classDefinition = dictionaryService.getClass(parent);
                        PermissionGroup attempt = getPermissionGroupOrNull(new SimplePermissionReference(parent, pg
                                .getName()));
                        if ((attempt != null) && attempt.isAllowFullControl())
                        {
                            return true;
                        }
                    }
                    return false;
                }
            }
            else
            {
                return pg.isAllowFullControl();
            }
        }
        else
        {
            return false;
        }

    }

    public PermissionReference getPermissionReference(QName qname, String permissionName)
    {
        if (permissionName == null)
        {
            return null;
        }
        PermissionReference pr = uniqueMap.get(permissionName);
        if (pr == null)
        {
            pr = permissionReferenceMap.get(permissionName);
            if (pr == null)
            {
                throw new UnsupportedOperationException("Can not find " + permissionName);
            }
        }
        return pr;

    }

    public boolean isUnique(PermissionReference permissionReference)
    {
        return uniqueMap.containsKey(permissionReference.getName());
    }

    private void buildUniquePermissionMap()
    {
        Set<String> excluded = new HashSet<String>(64, 1.0f);
        uniqueMap = new HashMap<String, PermissionReference>(128, 1.0f);
        permissionReferenceMap = new HashMap<String, PermissionReference>(128, 1.0f);
        permissionGroupMap = new HashMap<PermissionReference, PermissionGroup>(64, 1.0f);
        permissionMap = new HashMap<PermissionReference, Permission>(32, 1.0f);
        for (PermissionSet ps : permissionSets.values())
        {
            for (PermissionGroup pg : ps.getPermissionGroups())
            {
                permissionGroupMap.put(pg, pg);
                permissionReferenceMap.put(pg.toString(), pg);
            }
            for (Permission p : ps.getPermissions())
            {
                permissionReferenceMap.put(p.toString(), p);
                permissionMap.put(p, p);
            }
        }

        for (PermissionSet ps : permissionSets.values())
        {
            for (PermissionGroup pg : ps.getPermissionGroups())
            {
                if (uniqueMap.containsKey(pg.getName()) && !excluded.contains(pg.getName()))
                {
                    PermissionReference value = uniqueMap.get(pg.getName());
                    if (!value.equals(getBasePermissionGroup(pg)))
                    {
                        uniqueMap.remove(pg.getName());
                        excluded.add(pg.getName());
                    }
                }
                else
                {
                    uniqueMap.put(pg.getName(), getBasePermissionGroup(pg));
                }
            }
            for (Permission p : ps.getPermissions())
            {
                if (uniqueMap.containsKey(p.getName()) && !excluded.contains(p.getName()))
                {
                    PermissionReference value = uniqueMap.get(p.getName());
                    if (!value.equals(p))
                    {
                        uniqueMap.remove(p.getName());
                        excluded.add(p.getName());
                    }
                }
                else
                {
                    uniqueMap.put(p.getName(), p);
                }
            }
        }
        // Add all permissions to the unique list
        if (uniqueMap.containsKey(PermissionService.ALL_PERMISSIONS))
        {
            throw new IllegalStateException(
                    "There must not be a permission with the same name as the ALL_PERMISSION constant: "
                            + PermissionService.ALL_PERMISSIONS);
        }
        uniqueMap.put(PermissionService.ALL_PERMISSIONS, new SimplePermissionReference(QName.createQName(
                NamespaceService.SECURITY_MODEL_1_0_URI, PermissionService.ALL_PERMISSIONS),
                PermissionService.ALL_PERMISSIONS));

    }

}
