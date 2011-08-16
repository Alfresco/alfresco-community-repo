/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.RequiredPermission;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.security.permissions.impl.RequiredPermission.On;
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
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultDocumentType;
import org.springframework.util.FileCopyUtils;

/**
 * The implementation of the model DAO Reads and stores the top level model information Encapsulates access to this
 * information
 * 
 * @author andyh
 */
public class PermissionModel implements ModelDAO
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
    private String dtdSchema;
    private boolean validate = true;

    /*
     * (non-Javadoc)
     * @seeorg.alfresco.repo.security.permissions.impl.ModelDAO#hasFull(org.alfresco.repo.security.permissions.
     * PermissionReference)
     */
    private static PermissionReference ALL = SimplePermissionReference.getPermissionReference(QName.createQName(NamespaceService.SECURITY_MODEL_1_0_URI,
            PermissionService.ALL_PERMISSIONS), PermissionService.ALL_PERMISSIONS);

    private static class MutableState    
    {
        private final DictionaryService dictionaryService;

        // Aprrox 6 - default size OK
        private Map<QName, PermissionSet> permissionSets = new HashMap<QName, PermissionSet>(128);
    
        // Global permissions - default size OK
        private Set<GlobalPermissionEntry> globalPermissions = new HashSet<GlobalPermissionEntry>();
    
        private AccessStatus defaultPermission;

        private Collection<QName> allAspects;

        private Map<String, PermissionReference> uniqueMap;

        private Map<PermissionReference, Permission> permissionMap;

        private Map<PermissionReference, PermissionGroup> permissionGroupMap;

        private Map<String, PermissionReference> permissionReferenceMap;

        private Map<PermissionReference, Set<PermissionReference>> grantingPermissions = new HashMap<PermissionReference, Set<PermissionReference>>(256);

        // Cache grantees
        private Map<PermissionReference, Set<PermissionReference>> granteePermissions = new HashMap<PermissionReference, Set<PermissionReference>>(256);

        // Cache the mapping of extended groups to the base
        private Map<PermissionGroup, PermissionGroup> groupsToBaseGroup = new HashMap<PermissionGroup, PermissionGroup>(256);    
        
        private Map<RequiredKey, Set<PermissionReference>> requiredPermissionsCache = new HashMap<RequiredKey, Set<PermissionReference>>(1024);
        
        private Map<Pair<PermissionReference, RequiredPermission.On>, Set<PermissionReference>> unconditionalRequiredPermissionsCache = new HashMap<Pair<PermissionReference, RequiredPermission.On>, Set<PermissionReference>>(1024);

        private Map<QName, Set<PermissionReference>> cachedTypePermissionsExposed = new HashMap<QName, Set<PermissionReference>>(256);

        private Map<QName, Set<PermissionReference>> cachedTypePermissionsUnexposed = new HashMap<QName, Set<PermissionReference>>(256);
                
        private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        
        public MutableState(DictionaryService dictionaryService)
        {
            this.dictionaryService = dictionaryService;
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
                        return checkPermission(SimplePermissionReference.getPermissionReference(pg.getTypeQName(), pg.getName()));
                    }
                    else
                    {
                        ClassDefinition classDefinition = dictionaryService.getClass(pg.getQName());
                        QName parent;
                        while ((parent = classDefinition.getParentName()) != null)
                        {
                            classDefinition = dictionaryService.getClass(parent);

                            PermissionGroup attempt = getPermissionGroupOrNull(SimplePermissionReference.getPermissionReference(parent, pg.getName()));
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

        private Set<PermissionReference> getAllPermissionsImpl(QName type, boolean exposedOnly)
        {
            Map<QName, Set<PermissionReference>> cache;
            if (exposedOnly)
            {
                cache = cachedTypePermissionsExposed;
            }
            else
            {
                cache = cachedTypePermissionsUnexposed;
            }
            Set<PermissionReference> permissions = cache.get(type);
            if (permissions == null)
            {
                boolean hadWriteLock = lock.isWriteLockedByCurrentThread();
                if (!hadWriteLock)
                {                
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                }
                try
                {
                    permissions = cache.get(type);
                    if (permissions == null)
                    {                
                        permissions = new LinkedHashSet<PermissionReference>(256, 1.0f);
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
                        permissions = Collections.unmodifiableSet(permissions);
                        cache.put(type, permissions);
                    }
                }
                finally
                {
                    if (!hadWriteLock)
                    {                
                        lock.readLock().lock();
                        lock.writeLock().unlock();
                    }
                }
            }
            return permissions;
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
                                target.add(SimplePermissionReference.getPermissionReference(pg.getQName(), pg.getName()));
                            }
                        }
                        else if (exposedOnly)
                        {
                            if (pg.isTypeRequired() == typeRequired)
                            {
                                PermissionReference base = getBasePermissionGroup(pg);
                                target.add(SimplePermissionReference.getPermissionReference(base.getQName(), base.getName()));
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
                            target.add(SimplePermissionReference.getPermissionReference(p.getQName(), p.getName()));
                        }
                    }
                }
            }
        }

        private void mergeGeneralAspectPermissions(Set<PermissionReference> target, boolean exposedOnly)
        {
            for (QName aspect : allAspects)
            {
                mergePermissions(target, aspect, exposedOnly, false);
            }
        }
        
        /**
         * Support to find permission groups
         * 
         * @param target
         * @return the permission group
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
         * @return the permission group
         */
        private PermissionGroup getPermissionGroup(PermissionReference target)
        {
            PermissionGroup pg = getPermissionGroupOrNull(target);
            if (pg == null)
            {
                throw new PermissionModelException("There is no permission group :" + target.getQName() + " " + target.getName());
            }
            return pg;
        }

        /**
         * Get the base permission group for a given permission group.
         * 
         * @param pg
         * @return the permission group
         */
        private PermissionGroup getBasePermissionGroupOrNull(PermissionGroup pg)
        {
            if (pg == null)
            {
                return null;
            }
            PermissionGroup permissionGroup = groupsToBaseGroup.get(pg);
            if (permissionGroup == null)
            {
                boolean hadWriteLock = lock.isWriteLockedByCurrentThread();
                if (!hadWriteLock)
                {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                }
                permissionGroup = groupsToBaseGroup.get(pg);
                if (permissionGroup == null)
                {                
                    permissionGroup = getBasePermissionGroupOrNullImpl(pg);
                    groupsToBaseGroup.put(pg, permissionGroup);
                }
                if (!hadWriteLock)
                {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
            return permissionGroup;
        }

        /**
         * Query the model for a base permission group Uses the Data Dictionary to reolve inheritance
         * 
         * @param pg
         * @return the permission group
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
                    return getPermissionGroup(SimplePermissionReference.getPermissionReference(pg.getTypeQName(), pg.getName()));
                }
                else
                {
                    ClassDefinition classDefinition = dictionaryService.getClass(pg.getQName());
                    QName parent;
                    while ((parent = classDefinition.getParentName()) != null)
                    {
                        classDefinition = dictionaryService.getClass(parent);
                        PermissionGroup attempt = getPermissionGroupOrNull(SimplePermissionReference.getPermissionReference(parent, pg.getName()));
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
                throw new PermissionModelException("There is no parent for permission group :" + target.getQName() + " " + target.getName());
            }
            return pg;
        }
        
        private Set<PermissionReference> getGrantingPermissionsImpl(PermissionReference permissionReference)
        {
            // Query the model
            HashSet<PermissionReference> permissions = new HashSet<PermissionReference>(256, 1.0f);
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

        private Set<PermissionReference> getGranteePermissionsImpl(PermissionReference permissionReference)
        {
            // Query the model (we have the write lock)
            HashSet<PermissionReference> permissions = new HashSet<PermissionReference>(256, 1.0f);
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
                                permissions.addAll(getGranteePermissions(SimplePermissionReference.getPermissionReference(pg.getTypeQName(), pg.getName())));
                            }
                            else
                            {
                                ClassDefinition classDefinition = dictionaryService.getClass(pg.getQName());
                                QName parent = classDefinition.getParentName();
                                if (parent != null)
                                {
                                    classDefinition = dictionaryService.getClass(parent);
                                    PermissionGroup attempt = getPermissionGroupOrNull(SimplePermissionReference.getPermissionReference(parent, pg.getName()));
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
        
        private Set<PermissionReference> getImmediateGranteePermissionsImpl(PermissionReference permissionReference)
        {
            // Query the model
            HashSet<PermissionReference> permissions = new HashSet<PermissionReference>(256);
            for (PermissionSet ps : permissionSets.values())
            {
                for (PermissionGroup pg : ps.getPermissionGroups())
                {
                    if (pg.equals(permissionReference))
                    {
                        for (PermissionReference included : pg.getIncludedPermissionGroups())
                        {
                            permissions.add(included);
                        }
    
                        if (pg.isExtends())
                        {
                            if (pg.getTypeQName() != null)
                            {
                                permissions.addAll(getImmediateGranteePermissions(SimplePermissionReference.getPermissionReference(pg.getTypeQName(), pg.getName())));
                            }
                            else
                            {
                                ClassDefinition classDefinition = dictionaryService.getClass(pg.getQName());
                                QName parent = classDefinition.getParentName();
                                if (parent != null)
                                {
                                    classDefinition = dictionaryService.getClass(parent);
                                    PermissionGroup attempt = getPermissionGroupOrNull(SimplePermissionReference.getPermissionReference(parent, pg.getName()));
                                    if (attempt != null)
                                    {
                                        permissions.addAll(getImmediateGranteePermissions(attempt));
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
            HashSet<PermissionReference> permissions = new HashSet<PermissionReference>(256, 1.0f);
            for (PermissionSet ps : permissionSets.values())
            {
                for (PermissionGroup pg : ps.getPermissionGroups())
                {
                    permissions.add(SimplePermissionReference.getPermissionReference(pg.getQName(), pg.getName()));
                }
                for (Permission p : ps.getPermissions())
                {
                    permissions.add(SimplePermissionReference.getPermissionReference(p.getQName(), p.getName()));
                }
            }
            return permissions;
        }
        
        private Set<PermissionReference> getGranteePermissions(PermissionReference permissionReference)
        {
            if(permissionReference == null)
            {
                return Collections.<PermissionReference>emptySet();
            }
            
            // Cache the results
            Set<PermissionReference> grantees = granteePermissions.get(permissionReference);
            if (grantees == null)
            {
                boolean hadWriteLock = lock.isWriteLockedByCurrentThread();
                if (!hadWriteLock)
                {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                }
                try
                {
                    grantees = granteePermissions.get(permissionReference);
                    if (grantees == null)
                    {
                        Set<PermissionReference> internal = getGranteePermissionsImpl(permissionReference);
                        grantees = new HashSet<PermissionReference>();
                        for (PermissionReference grantee : internal)
                        {
                            grantees.add(SimplePermissionReference.getPermissionReference(grantee.getQName(), grantee.getName()));
                        }
                        grantees = Collections.unmodifiableSet(grantees);
                        granteePermissions.put(permissionReference, grantees);
                    }
                }
                finally
                {
                    if (!hadWriteLock)
                    {
                        lock.readLock().lock();
                        lock.writeLock().unlock();
                    }
                }
            }
            return grantees;
        }

        private Set<PermissionReference> getImmediateGranteePermissions(PermissionReference permissionReference)
        {
            // Cache the results

            Set<PermissionReference> internal = getImmediateGranteePermissionsImpl(permissionReference);
            Set<PermissionReference> grantees = new HashSet<PermissionReference>();
            for (PermissionReference grantee : internal)
            {
                grantees.add(SimplePermissionReference.getPermissionReference(grantee.getQName(), grantee.getName()));
            }
            grantees = Collections.unmodifiableSet(grantees);

            return grantees;
        }
        
        private Set<PermissionReference> getRequiredPermissions(PermissionReference required, QName qName, Set<QName> aspectQNames, RequiredPermission.On on)
        {
            // Cache lookup as this is static
            if((required == null) || (qName == null))
            {
                return Collections.<PermissionReference>emptySet();
            }
            
            RequiredKey key = generateKey(required, qName, aspectQNames, on);

            Set<PermissionReference> answer = requiredPermissionsCache.get(key);
            if (answer == null)
            {
                boolean hadWriteLock = lock.isWriteLockedByCurrentThread();
                if (!hadWriteLock)
                {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                }
                try
                {
                    answer = requiredPermissionsCache.get(key);
                    if (answer == null)
                    {
                        PermissionGroup pg = getBasePermissionGroupOrNull(getPermissionGroupOrNull(required));
                        if (pg == null)
                        {
                            answer = getRequirementsForPermission(required, on);
                        }
                        else
                        {
                            answer = getRequirementsForPermissionGroup(pg, on, qName, aspectQNames);
                        }
                        answer = Collections.unmodifiableSet(answer);
                        requiredPermissionsCache.put(key, answer);
                    }
                }
                finally
                {
                    if (!hadWriteLock)
                    {
                        lock.readLock().lock();
                        lock.writeLock().unlock();
                    }
                }
            }
            return answer;
        }
        
        private Set<PermissionReference> getUnconditionalRequiredPermissions(PermissionReference required, RequiredPermission.On on)
        {
            // Cache lookup as this is static
            if(required == null)
            {
                return Collections.<PermissionReference>emptySet();
            }
            Pair<PermissionReference, RequiredPermission.On> key = new Pair<PermissionReference, RequiredPermission.On>(required, on);

            Set<PermissionReference> answer = unconditionalRequiredPermissionsCache.get(key);
            if (answer == null)
            {
                boolean hadWriteLock = lock.isWriteLockedByCurrentThread();
                if (!hadWriteLock)
                {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                }
                try
                {
                    answer = unconditionalRequiredPermissionsCache.get(key);
                    if (answer == null)
                    {
                        PermissionGroup pg = getBasePermissionGroupOrNull(getPermissionGroupOrNull(required));
                        if (pg == null)
                        {
                            answer = getRequirementsForPermission(required, on);
                        }
                        else
                        {
                            answer = getUnconditionalRequirementsForPermissionGroup(pg, on);
                        }
                        answer = Collections.unmodifiableSet(answer);
                        unconditionalRequiredPermissionsCache.put(key, answer);
                    }
                }
                finally
                {
                    if (!hadWriteLock)
                    {
                        lock.readLock().lock();
                        lock.writeLock().unlock();
                    }
                }
            }
            return answer;
        }

        /**
         * Get the requirements for a permission
         * 
         * @param required
         * @param on
         * @return the set of permission references
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
         * @return the set of permission references
         */
        private Set<PermissionReference> getRequirementsForPermissionGroup(PermissionGroup target, RequiredPermission.On on, QName qName, Set<QName> aspectQNames)
        {
            HashSet<PermissionReference> requiredPermissions = new HashSet<PermissionReference>(16, 1.0f);
            if (target == null)
            {
                return requiredPermissions;
            }
            for (PermissionSet ps : permissionSets.values())
            {
                for (PermissionGroup pg : ps.getPermissionGroups())
                {
                    PermissionGroup base = getBasePermissionGroupOrNull(pg);
                    if ((target.equals(base) || target.isAllowFullControl()) && (!base.isTypeRequired() || isPartOfDynamicPermissionGroup(pg, qName, aspectQNames)))
                    {
                        // Add includes
                        for (PermissionReference pr : pg.getIncludedPermissionGroups())
                        {
                            requiredPermissions.addAll(getRequirementsForPermissionGroup(getBasePermissionGroupOrNull(getPermissionGroupOrNull(pr)), on, qName, aspectQNames));
                        }
                    }
                }
                for (Permission p : ps.getPermissions())
                {
                    for (PermissionReference grantedTo : p.getGrantedToGroups())
                    {
                        PermissionGroup base = getBasePermissionGroupOrNull(getPermissionGroupOrNull(grantedTo));
                        if ((target.equals(base) || target.isAllowFullControl()) && (!base.isTypeRequired() || isPartOfDynamicPermissionGroup(grantedTo, qName, aspectQNames)))
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
        
        private Set<PermissionReference> getUnconditionalRequirementsForPermissionGroup(PermissionGroup target, RequiredPermission.On on)
        {
            HashSet<PermissionReference> requiredPermissions = new HashSet<PermissionReference>(16, 1.0f);
            if (target == null)
            {
                return requiredPermissions;
            }
            for (PermissionSet ps : permissionSets.values())
            {
                for (PermissionGroup pg : ps.getPermissionGroups())
                {
                    PermissionGroup base = getBasePermissionGroupOrNull(pg);
                    if ((target.equals(base) || target.isAllowFullControl()) && (!base.isTypeRequired()))
                    {
                        // Add includes
                        for (PermissionReference pr : pg.getIncludedPermissionGroups())
                        {
                            requiredPermissions.addAll(getUnconditionalRequirementsForPermissionGroup(getBasePermissionGroupOrNull(getPermissionGroupOrNull(pr)), on));
                        }
                    }
                }
                for (Permission p : ps.getPermissions())
                {
                    for (PermissionReference grantedTo : p.getGrantedToGroups())
                    {
                        PermissionGroup base = getBasePermissionGroupOrNull(getPermissionGroupOrNull(grantedTo));
                        if ((target.equals(base) || target.isAllowFullControl()) && (!base.isTypeRequired()))
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
         * Utility method to find a permission
         * 
         * @param perm
         * @return the permission
         */
        private Permission getPermissionOrNull(PermissionReference perm)
        {
            Permission p = permissionMap.get(perm);
            return p == null ? null : p;
        }
        
        /**
         * Check type specifc extension of permission sets.
         * 
         * @param pr
         * @param typeQname
         * @param aspects
         * @return true if dynamic
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
            Set<String> excluded = new HashSet<String>(128, 1.0f);
            uniqueMap = new HashMap<String, PermissionReference>(256);
            permissionReferenceMap = new HashMap<String, PermissionReference>(256);
            permissionGroupMap = new HashMap<PermissionReference, PermissionGroup>(128);
            permissionMap = new HashMap<PermissionReference, Permission>(64);
            for (PermissionSet ps : permissionSets.values())
            {
                for (PermissionGroup pg : ps.getPermissionGroups())
                {
                    permissionGroupMap.put(SimplePermissionReference.getPermissionReference(pg.getQName(), pg.getName()), pg);
                    permissionReferenceMap.put(pg.toString(), SimplePermissionReference.getPermissionReference(pg.getQName(), pg.getName()));
                }
                for (Permission p : ps.getPermissions())
                {
                    permissionReferenceMap.put(p.toString(), SimplePermissionReference.getPermissionReference(p.getQName(), p.getName()));
                    permissionMap.put(SimplePermissionReference.getPermissionReference(p.getQName(), p.getName()), p);
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
                        PermissionReference base = getBasePermissionGroup(pg);
                        uniqueMap.put(pg.getName(), SimplePermissionReference.getPermissionReference(base.getQName(), base.getName()));
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
                        uniqueMap.put(p.getName(), SimplePermissionReference.getPermissionReference(p.getQName(), p.getName()));
                    }
                }
            }
            // Add all permissions to the unique list
            if (uniqueMap.containsKey(PermissionService.ALL_PERMISSIONS))
            {
                throw new IllegalStateException("There must not be a permission with the same name as the ALL_PERMISSION constant: " + PermissionService.ALL_PERMISSIONS);
            }
            uniqueMap.put(PermissionService.ALL_PERMISSIONS, SimplePermissionReference.getPermissionReference(QName.createQName(NamespaceService.SECURITY_MODEL_1_0_URI,
                    PermissionService.ALL_PERMISSIONS), PermissionService.ALL_PERMISSIONS));

        }
        
        private boolean hasFull(PermissionReference permissionReference)
        {
            if (permissionReference == null)
            {
                return false;
            }
            if(permissionReference.equals(ALL))
            {
                return true;
            }
            PermissionGroup group = getPermissionGroupOrNull(permissionReference);
            if (group == null)
            {
                return false;
            }
            else
            {
                if (group.isAllowFullControl())
                {
                    return true;
                }
                else
                {
                    if(group.isExtends())
                    {
                        if (group.getTypeQName() != null)
                        {
                            return hasFull(SimplePermissionReference.getPermissionReference(group.getTypeQName(), group.getName()));
                        }
                        else
                        {
                            ClassDefinition classDefinition = dictionaryService.getClass(group.getQName());
                            QName parent;
                            while ((parent = classDefinition.getParentName()) != null)
                            {
                                classDefinition = dictionaryService.getClass(parent);
                                PermissionGroup attempt = getPermissionGroupOrNull(SimplePermissionReference.getPermissionReference(parent, group.getName()));
                                if ((attempt != null) && (attempt.isAllowFullControl()))
                                {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        }            
    }

    private MutableState mutableState;

    /**
     * Default constructor
     */
    public PermissionModel()
    {
        super();
    }

    // IOC

    /**
     * Set the model
     * 
     * @param model
     */
    public void setModel(String model)
    {
        this.model = model;
    }

    /**
     * Set the dtd schema that is used to validate permission model
     * 
     * @param dtdSchema
     */
    public void setDtdSchema(String dtdSchema)
    {
        this.dtdSchema = dtdSchema;
    }
    
    /**
     * Indicates whether model should be validated on initialization against specified dtd
     * 
     * @param validate
     */
    public void setValidate(boolean validate)
    {
        this.validate = validate;
    }

    /**
     * Set the dictionary service
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the node service
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Adds the {@link #setModel(String) model}.
     */
    public void init()
    {
        mutableState = new MutableState(dictionaryService);
        addPermissionModel(this.model);
    }

    /**
     * Adds a permission model
     * 
     * @param model
     *            path to the permission model to add
     */
    public void addPermissionModel(String model)
    {        
        Document document = createDocument(model);
        Element root = document.getRootElement();

        mutableState.lock.writeLock().lock();
        
        try
        {
            Attribute defaultPermissionAttribute = root.attribute(DEFAULT_PERMISSION);
            if (defaultPermissionAttribute != null)
            {
                if (defaultPermissionAttribute.getStringValue().equalsIgnoreCase(ALLOW))
                {
                    mutableState.defaultPermission = AccessStatus.ALLOWED;
                }
                else if (defaultPermissionAttribute.getStringValue().equalsIgnoreCase(DENY))
                {
                    mutableState.defaultPermission = AccessStatus.DENIED;
                }
                else
                {
                    throw new PermissionModelException("The default permission must be deny or allow");
                }
            }
            else
            {
                mutableState.defaultPermission = AccessStatus.DENIED;
            }
    
            DynamicNamespacePrefixResolver nspr = new DynamicNamespacePrefixResolver();
    
            // Namespaces
    
            for (Iterator<Element> nsit = root.elementIterator(NAMESPACES); nsit.hasNext(); /**/)
            {
                Element namespacesElement = (Element) nsit.next();
                for (Iterator<Element> it = namespacesElement.elementIterator(NAMESPACE); it.hasNext(); /**/)
                {
                    Element nameSpaceElement = (Element) it.next();
                    nspr.registerNamespace(nameSpaceElement.attributeValue(NAMESPACE_PREFIX), nameSpaceElement.attributeValue(NAMESPACE_URI));
                }
            }
    
            // Permission Sets
    
            for (Iterator<Element> psit = root.elementIterator(PERMISSION_SET); psit.hasNext(); /**/)
            {
                Element permissionSetElement = (Element) psit.next();
                PermissionSet permissionSet = new PermissionSet();
                permissionSet.initialise(permissionSetElement, nspr, this);
    
                mutableState.permissionSets.put(permissionSet.getQName(), permissionSet);
            }
    
            mutableState.buildUniquePermissionMap();
    
            // NodePermissions
    
            for (Iterator<Element> npit = root.elementIterator(GLOBAL_PERMISSION); npit.hasNext(); /**/)
            {
                Element globalPermissionElement = (Element) npit.next();
                GlobalPermissionEntry globalPermission = new GlobalPermissionEntry();
                globalPermission.initialise(globalPermissionElement, nspr, this);
    
                mutableState.globalPermissions.add(globalPermission);
            }
    
            // Cache all aspect list
    
            mutableState.allAspects = dictionaryService.getAllAspects();
        }
        finally
        {        
            mutableState.lock.writeLock().unlock();
        }
    }

    /*
     * Create the XML document from the file location
     */
    private Document createDocument(String model)
    {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(model);
        URL dtdSchemaUrl = this.getClass().getClassLoader().getResource(dtdSchema); 
        if (is == null)
        {
            throw new PermissionModelException("File not found: " + model);
        }
        SAXReader reader = new SAXReader();
        try
        {
            if (validate)
            {
                if (dtdSchemaUrl != null)
                {
                    is = processModelDocType(is, dtdSchemaUrl.toString());
                    reader.setValidation(true);
                }
                else
                {
                    throw new PermissionModelException("Couldn't obtain DTD schema to validate permission model.");
                }
            }
            
            Document document = reader.read(is);
            is.close();
            return document;
        }
        catch (DocumentException e)
        {
            throw new PermissionModelException("Failed to create permission model document: " + model, e);
        }
        catch (IOException e)
        {
            throw new PermissionModelException("Failed to close permission model document: " + model, e);
        }

    }

    /*
     * Replace or add correct DOCTYPE to the xml to allow validation against dtd
     */
    private InputStream processModelDocType(InputStream is, String dtdSchemaUrl) throws DocumentException, IOException
    {
        SAXReader reader = new SAXReader();
        // read document without validation
        Document doc = reader.read(is);
        DocumentType docType = doc.getDocType();
        if (docType != null)
        {
            // replace DOCTYPE setting the full path to the xsd
            docType.setSystemID(dtdSchemaUrl);
        }
        else
        {
            // add the DOCTYPE
            docType = new DefaultDocumentType(doc.getRootElement().getName(), dtdSchemaUrl);
            doc.setDocType(docType);
        }

        File tempFile = TempFileProvider.createTempFile("permissionModel-", ".tmp");

        // copy the modified permission model to the temp file
        FileCopyUtils.copy(doc.asXML().getBytes(), tempFile);

        return new FileInputStream(tempFile);
    }

    /**
     * Set the default access status
     * 
     * @return the default access status
     */
    public AccessStatus getDefaultPermission()
    {
        AccessStatus defaultPermission;
        mutableState.lock.readLock().lock();
        defaultPermission = mutableState.defaultPermission;
        mutableState.lock.readLock().unlock();
        return defaultPermission;
    }

    /**
     * Get the default acces status for the givne permission
     * 
     * @param pr
     * @return the access status
     */
    public AccessStatus getDefaultPermission(PermissionReference pr)
    {
        mutableState.lock.readLock().lock();
        try
        {
            Permission p = mutableState.permissionMap.get(pr);
            if (p == null)
            {
                return mutableState.defaultPermission;
            }
            else
            {
                return p.getDefaultPermission();
            }
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }

    public Set<? extends PermissionEntry> getGlobalPermissionEntries()
    {
        mutableState.lock.readLock().lock();
        try
        {
            return Collections.unmodifiableSet(new HashSet<GlobalPermissionEntry>(mutableState.globalPermissions));
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }

    /**
     * Get the permission sets by type
     * 
     * @return the permission sets by type
     */
    public Map<QName, PermissionSet> getPermissionSets()
    {
        mutableState.lock.readLock().lock();
        try
        {
            return Collections.unmodifiableMap(new HashMap<QName, PermissionSet>(mutableState.permissionSets));
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }

    public Set<PermissionReference> getAllPermissions(QName type)
    {
        return getAllPermissionsImpl(type, null, false);
    }

    public Set<PermissionReference> getExposedPermissions(QName type)
    {
        return getAllPermissionsImpl(type, null, true);
    }

    public Set<PermissionReference> getAllPermissions(NodeRef nodeRef)
    {
        return getAllPermissionsImpl(nodeService.getType(nodeRef), nodeService.getAspects(nodeRef), false);
    }

    public Set<PermissionReference> getExposedPermissions(NodeRef nodeRef)
    {
        return getAllPermissionsImpl(nodeService.getType(nodeRef), nodeService.getAspects(nodeRef), true);
    }

    public Set<PermissionReference> getAllPermissions(QName typeName, Set<QName> aspects)
    {
        return getAllPermissionsImpl(typeName, aspects, false);
    }

    private Set<PermissionReference> getAllPermissionsImpl(QName typeName, Set<QName> aspects, boolean exposedOnly)
    {
        Set<PermissionReference> permissions = new LinkedHashSet<PermissionReference>(128, 1.0f);

        ClassDefinition cd = dictionaryService.getClass(typeName);
        mutableState.lock.readLock().lock();
        try
        {
            permissions.addAll(mutableState.getAllPermissionsImpl(typeName, exposedOnly));
    
            if (cd != null && aspects != null)
            {
                Set<QName> defaultAspects = cd.getDefaultAspectNames();
                for (QName aspect : aspects)
                {
                    if (!defaultAspects.contains(aspect))
                    {
                        mutableState.addAspectPermissions(aspect, permissions, exposedOnly);
                    }
                }
            }
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
        return permissions;
    }

    public Set<PermissionReference> getGrantingPermissions(PermissionReference permissionReference)
    {
        if(permissionReference == null)
        {
            return Collections.<PermissionReference>emptySet();
        }
        
        mutableState.lock.readLock().lock();
        // Cache the results
        Set<PermissionReference> granters = mutableState.grantingPermissions.get(permissionReference);
        if (granters == null)
        {
            mutableState.lock.readLock().unlock();
            mutableState.lock.writeLock().lock();
            
            try
            {
                granters = mutableState.grantingPermissions.get(permissionReference);
                if (granters == null)
                {            
                    Set<PermissionReference> internal = mutableState.getGrantingPermissionsImpl(permissionReference);
                    granters = new HashSet<PermissionReference>();
                    for (PermissionReference grantee : internal)
                    {
                        granters.add(SimplePermissionReference.getPermissionReference(grantee.getQName(), grantee.getName()));
                    }
                    granters = Collections.unmodifiableSet(granters);
                    mutableState.grantingPermissions.put(permissionReference, granters);
                }
            }
            finally
            {
                mutableState.lock.writeLock().unlock();
            }
        }
        else
        {
            mutableState.lock.readLock().unlock();
        }
        return granters;
    }

    static RequiredKey generateKey(PermissionReference required, QName qName, Set<QName> aspectQNames, RequiredPermission.On on)
    {
        return RequiredKey.getRequiredKey(required, qName, aspectQNames, on);
    }

    /**
     * Cache key
     * 
     * @author andyh
     */
    public static class RequiredKey
    {
        PermissionReference required;

        QName qName;

        Set<QName> aspectQNames;

        RequiredPermission.On on;

        int hashCode = 0;

        private static ReadWriteLock lock = new ReentrantReadWriteLock();

        private static Map<PermissionReference, Map<QName, Map<Set<QName>, EnumMap<RequiredPermission.On, RequiredKey>>>> instances = new HashMap<PermissionReference, Map<QName, Map<Set<QName>, EnumMap<RequiredPermission.On, RequiredKey>>>>();

        /**
         * factory for the key
         * 
         * @param required
         * @param qName
         * @param aspectQNames
         * @param on
         * @return the key
         */
        public static RequiredKey getRequiredKey(PermissionReference required, QName qName, Set<QName> aspectQNames, RequiredPermission.On on)
        {
            lock.readLock().lock();
            try
            {
                Map<QName, Map<Set<QName>, EnumMap<RequiredPermission.On, RequiredKey>>> byPermRef = instances.get(required);
                if (byPermRef != null)
                {
                    Map<Set<QName>, EnumMap<RequiredPermission.On, RequiredKey>> byType = byPermRef.get(qName);
                    if (byType != null)
                    {
                        EnumMap<RequiredPermission.On, RequiredKey> byAspects = byType.get(aspectQNames);
                        if (byAspects != null)
                        {
                            RequiredKey instance = byAspects.get(on);
                            if (instance != null)
                            {
                                return instance;
                            }
                        }
                    }
                }
            }
            finally
            {
                lock.readLock().unlock();
            }

            lock.writeLock().lock();
            try
            {
                Map<QName, Map<Set<QName>, EnumMap<RequiredPermission.On, RequiredKey>>> byPermRef = instances.get(required);
                if (byPermRef == null)
                {
                    byPermRef = new HashMap<QName, Map<Set<QName>, EnumMap<RequiredPermission.On, RequiredKey>>>();
                    instances.put(required, byPermRef);
                }

                Map<Set<QName>, EnumMap<RequiredPermission.On, RequiredKey>> byType = byPermRef.get(qName);
                if (byType == null)
                {
                    byType = new HashMap<Set<QName>, EnumMap<RequiredPermission.On, RequiredKey>>();
                    byPermRef.put(qName, byType);
                }
                EnumMap<RequiredPermission.On, RequiredKey> byAspects = byType.get(aspectQNames);
                if (byAspects == null)
                {
                    byAspects = new EnumMap<RequiredPermission.On, RequiredKey>(RequiredPermission.On.class);
                    byType.put(aspectQNames, byAspects);
                }
                RequiredKey instance = byAspects.get(on);
                if (instance == null)
                {
                    instance = new RequiredKey(required, qName, aspectQNames, on);
                    byAspects.put(on, instance);
                }
                return instance;

            }
            finally
            {
                lock.writeLock().unlock();
            }
        }

        RequiredKey(PermissionReference required, QName qName, Set<QName> aspectQNames, RequiredPermission.On on)
        {
            this.required = required;
            this.qName = qName;
            this.aspectQNames = aspectQNames;
            this.on = on;
        }

        @Override
        public int hashCode()
        {
            if (hashCode == 0)
            {
                final int PRIME = 1000003;
                int result = 1;
                result = PRIME * result + ((aspectQNames == null) ? 0 : aspectQNames.hashCode());
                result = PRIME * result + ((on == null) ? 0 : on.ordinal());
                result = PRIME * result + ((qName == null) ? 0 : qName.hashCode());
                result = PRIME * result + ((required == null) ? 0 : required.hashCode());
                hashCode = result;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final RequiredKey other = (RequiredKey) obj;

            if (required == null)
            {
                if (other.required != null)
                    return false;
            }
            else if (!required.equals(other.required))
                return false;

            if (qName == null)
            {
                if (other.qName != null)
                    return false;
            }
            else if (!qName.equals(other.qName))
                return false;

            if (on == null)
            {
                if (other.on != null)
                    return false;
            }
            else if (!on.equals(other.on))
                return false;

            if (aspectQNames == null)
            {
                if (other.aspectQNames != null)
                    return false;
            }
            else if (!aspectQNames.equals(other.aspectQNames))
                return false;

            return true;
        }

    }

    public boolean checkPermission(PermissionReference required)
    {
        mutableState.lock.readLock().lock();
        try
        {
            return mutableState.checkPermission(required);
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.impl.ModelDAO#getGranteePermissions(org.alfresco.repo.security.permissions.PermissionReference)
     */
    public Set<PermissionReference> getGranteePermissions(PermissionReference permissionReference)
    {
        mutableState.lock.readLock().lock();
        try
        {
           return mutableState.getGranteePermissions(permissionReference); 
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.impl.ModelDAO#getImmediateGranteePermissions(org.alfresco.repo.security.permissions.PermissionReference)
     */
    public Set<PermissionReference> getImmediateGranteePermissions(PermissionReference permissionReference)
    {
        mutableState.lock.readLock().lock();
        try
        {
           return mutableState.getImmediateGranteePermissions(permissionReference); 
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.impl.ModelDAO#getPermissionReference(org.alfresco.service.namespace.QName, java.lang.String)
     */
    public PermissionReference getPermissionReference(QName qname, String permissionName)
    {
        mutableState.lock.readLock().lock();
        try
        {
           return mutableState.getPermissionReference(qname, permissionName); 
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.impl.ModelDAO#getRequiredPermissions(org.alfresco.repo.security.permissions.PermissionReference, org.alfresco.service.namespace.QName, java.util.Set, org.alfresco.repo.security.permissions.impl.RequiredPermission.On)
     */
    public Set<PermissionReference> getRequiredPermissions(PermissionReference required, QName qName,
            Set<QName> aspectQNames, On on)
    {
        mutableState.lock.readLock().lock();
        try
        {
           return mutableState.getRequiredPermissions(required, qName, aspectQNames, on); 
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }
    
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.impl.ModelDAO#getUnconditionalRequiredPermissions(org.alfresco.repo.security.permissions.PermissionReference, org.alfresco.repo.security.permissions.impl.RequiredPermission.On)
     */
    @Override
    public Set<PermissionReference> getUnconditionalRequiredPermissions(PermissionReference required, On on)
    {
        mutableState.lock.readLock().lock();
        try
        {
           return mutableState.getUnconditionalRequiredPermissions(required, on); 
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.impl.ModelDAO#isUnique(org.alfresco.repo.security.permissions.PermissionReference)
     */
    public boolean isUnique(PermissionReference permissionReference)
    {
        mutableState.lock.readLock().lock();
        try
        {
           return mutableState.isUnique(permissionReference); 
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.impl.ModelDAO#getAllExposedPermissions()
     */
    public Set<PermissionReference> getAllExposedPermissions()
    {        
        Set<PermissionReference> permissions = new HashSet<PermissionReference>(256);
        mutableState.lock.readLock().lock();
        try
        {
            for (PermissionSet ps : mutableState.permissionSets.values())
            {
                for (PermissionGroup pg : ps.getPermissionGroups())
                {
                    if (pg.isExposed())
                    {
                        permissions.add(SimplePermissionReference.getPermissionReference(pg.getQName(), pg.getName()));
                    }
                }
                for (Permission p : ps.getPermissions())
                {
                    if (p.isExposed())
                    {
                        permissions.add(SimplePermissionReference.getPermissionReference(p.getQName(), p.getName()));
                    }
                }
            }
            return permissions;
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }        
    }
    
    public boolean hasFull(PermissionReference permissionReference)
    {
        mutableState.lock.readLock().lock();
        try
        {
           return mutableState.hasFull(permissionReference); 
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.impl.ModelDAO#getAllPermissions()
     */
    @Override
    public Set<PermissionReference> getAllPermissions()
    {
        mutableState.lock.readLock().lock();
        try
        {
           return mutableState.getAllPermissions(); 
        }
        finally
        {
            mutableState.lock.readLock().unlock();
        }
    }

}
