/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.lock.mem;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.lock.LockUtils;
import org.alfresco.repo.lock.traitextender.LockableAspectInterceptorExtension;
import org.alfresco.repo.lock.traitextender.LockableAspectInterceptorTrait;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.AJExtender;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;

/**
 * NodeService interceptor to spoof the cm:lockable aspect when reading a node that has an ephemeral lock on it. LockService policies such as beforeDeleteNode that protect locked nodes, would not fire for nodes with ephemeral locks on them unless they are reported to have the cm:lockable aspect on them. As ephemeral locks are only held in memory the nodes have not been marked with this aspect, so the aspect must be spoofed.
 * <p>
 * This interceptor checks for EPHEMERAL lock directly - MNT-10477 fix.
 * 
 * @author Matt Ward
 */
public class LockableAspectInterceptor implements MethodInterceptor, Extensible
{
    private LockStore lockStore;
    private AuthenticationService authenticationService;
    private NodeService nodeService;
    private LockService lockService;

    private Set<String> methodsToCheck = new HashSet<String>();

    private final ThreadLocal<Boolean> threadEnabled;

    private final ExtendedTrait<LockableAspectInterceptorTrait> lockableAspectInterceptorTrait;

    /**
     * Default constructor.
     */
    public LockableAspectInterceptor()
    {
        threadEnabled = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue()
            {
                // The interceptor is enabled for all threads by default.
                return Boolean.TRUE;
            }
        };
        lockableAspectInterceptorTrait = new ExtendedTrait<LockableAspectInterceptorTrait>(createTrait());
    }

    public void init()
    {
        /* check for lock before following methods proceed - MNT-10477 */
        methodsToCheck.add("addAspect");
        methodsToCheck.add("addProperties");
        methodsToCheck.add("removeAspect");
        methodsToCheck.add("removeProperty");
        methodsToCheck.add("setProperties");
        methodsToCheck.add("setProperty");
        methodsToCheck.add("setType");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        if (!threadEnabled.get())
        {
            // Interceptor is not enabled for this thread.
            return invocation.proceed();
        }

        final String methodName = invocation.getMethod().getName();
        final Object[] args = invocation.getArguments();

        if (methodName.equals("hasAspect"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            QName aspectTypeQName = (QName) args[1];

            LockState lockState = getLockState(nodeRef);
            if (ContentModel.ASPECT_LOCKABLE.equals(aspectTypeQName) && isEphemeralLock(lockState))
            {
                return true;
            }
            return invocation.proceed();
        }
        else if (methodName.equals("getAspects"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            Set<QName> aspects = (Set<QName>) invocation.proceed();
            LockState lockState = getLockState(nodeRef);
            if (isEphemeralLock(lockState) && !aspects.contains(ContentModel.ASPECT_LOCKABLE))
            {
                aspects.add(ContentModel.ASPECT_LOCKABLE);
            }
            return aspects;
        }
        else if (methodName.equals("getProperties"))
        {
            NodeRef nodeRef = (NodeRef) args[0];

            Map<QName, Serializable> properties = (Map<QName, Serializable>) invocation.proceed();
            LockState lockState = getLockState(nodeRef);
            if (isEphemeralLock(lockState))
            {
                String userName = lockState.getOwner();
                properties.put(ContentModel.PROP_LOCK_OWNER, userName);
                properties.put(ContentModel.PROP_LOCK_TYPE, lockState.getLockType().toString());
                properties.put(ContentModel.PROP_EXPIRY_DATE, lockState.getExpires());
                properties.put(ContentModel.PROP_LOCK_LIFETIME, Lifetime.EPHEMERAL.toString());
            }
            else if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
            {
                // Persistent lock, ensure lifetime property is present.
                if (!properties.containsKey(ContentModel.PROP_LOCK_LIFETIME))
                {
                    properties.put(ContentModel.PROP_LOCK_LIFETIME, Lifetime.PERSISTENT.toString());
                }
            }
            return properties;
        }
        else if (methodName.equals("getProperty"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            QName propQName = (QName) args[1];

            // Avoid locking unless it is an interesting property.
            if (isLockProperty(propQName))
            {
                LockState lockState = getLockState(nodeRef);
                if (isEphemeralLock(lockState))
                {
                    if (ContentModel.PROP_LOCK_OWNER.equals(propQName))
                    {
                        return lockState.getOwner();
                    }
                    else if (ContentModel.PROP_LOCK_TYPE.equals(propQName))
                    {
                        return lockState.getLockType().toString();
                    }
                    else if (ContentModel.PROP_EXPIRY_DATE.equals(propQName))
                    {
                        return lockState.getExpires();
                    }
                    else if (ContentModel.PROP_LOCK_LIFETIME.equals(propQName))
                    {
                        return lockState.getLifetime().toString();
                    }
                }
                else if (ContentModel.PROP_LOCK_LIFETIME.equals(propQName))
                {
                    // Is there a persistent lock?
                    if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
                    {
                        return Lifetime.PERSISTENT.toString();
                    }
                }
            }
            return invocation.proceed();
        }
        else if (methodName.equals("setProperties"))
        {
            // Ephemeral locks must not be persisted to the database.
            // TODO: This is potentially creating an ephemeral lock here, put it in the lockstore?
            NodeRef nodeRef = (NodeRef) args[0];
            Map<QName, Serializable> newProperties = (Map<QName, Serializable>) args[1];
            /* MNT-10477 fix */
            checkForLockIfEphemeral(nodeRef);

            if (Lifetime.EPHEMERAL.toString().equals(newProperties.get(ContentModel.PROP_LOCK_LIFETIME)))
            {
                Map<QName, Serializable> convertedProperties = filterLockProperties(newProperties);
                // Now complete the call by passing the converted properties
                nodeService.setProperties(nodeRef, convertedProperties);
                return null;
            }
            else if (newProperties.containsKey(ContentModel.PROP_LOCK_LIFETIME))
            {
                // Always remove this property, even for persistent locks.
                newProperties.remove(ContentModel.PROP_LOCK_LIFETIME);
                nodeService.setProperties(nodeRef, newProperties);
                return null;
            }
            else
            {
                return invocation.proceed();
            }
        }
        else if (methodsToCheck.contains(methodName))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            /* MNT-10477 fix */
            checkForLockIfEphemeral(nodeRef);

            return invocation.proceed();
        }
        else
        {
            // If not a special case, invoke the original method.
            return invocation.proceed();
        }
    }

    /**
     * Enables the interceptor for the current thread. This would normally be used in a finally block to re-enable the interceptor for a previously {@link #disableForThread() disabled} thread.
     */
    public void enableForThread()
    {
        threadEnabled.set(Boolean.TRUE);
    }

    /**
     * Disables the interceptor for the current thread. Follow with a try/finally block as described for {@link #enableForThread()}.
     */
    public void disableForThread()
    {
        threadEnabled.set(Boolean.FALSE);
    }

    /**
     * Given a set of properties, removes those where <code>isLockProperty(propQName) == true</code>.
     * 
     * @param properties
     *            Map<QName, Serializable>
     * @return filtered properties.
     */
    private Map<QName, Serializable> filterLockProperties(Map<QName, Serializable> properties)
    {
        Map<QName, Serializable> filteredProps = new HashMap<QName, Serializable>(properties.size() * 2);

        // Only add non-lock properties
        for (QName propQName : properties.keySet())
        {
            if (!isLockProperty(propQName))
            {
                filteredProps.put(propQName, properties.get(propQName));
            }
        }
        return filteredProps;
    }

    /**
     * Return true if the specified property QName is for a lock-related property.
     */
    private boolean isLockProperty(QName propQName)
    {
        boolean isLockProp = propQName.equals(ContentModel.PROP_LOCK_OWNER) ||
                propQName.equals(ContentModel.PROP_LOCK_TYPE) ||
                propQName.equals(ContentModel.PROP_LOCK_LIFETIME) ||
                propQName.equals(ContentModel.PROP_EXPIRY_DATE);
        return isLockProp;
    }

    private boolean isEphemeralLock(LockState lockState)
    {
        boolean ephemeral = lockState != null &&
                lockState.isLockInfo() &&
                lockState.getLifetime() == Lifetime.EPHEMERAL;
        return ephemeral;
    }

    private void checkForLockIfEphemeral(NodeRef nodeRef)
    {
        LockState lockState = getLockState(nodeRef);
        if (isEphemeralLock(lockState))
        {
            lockService.checkForLock(nodeRef);
        }
    }

    public void setLockStore(LockStore lockStore)
    {
        this.lockStore = lockStore;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }

    @Extend(traitAPI = LockableAspectInterceptorTrait.class, extensionAPI = LockableAspectInterceptorExtension.class)
    private LockState getLockState(NodeRef nodeRef)
    {
        LockState lockState = lockStore.get(nodeRef);
        // Disregard in-memory lock if expired
        if (lockState != null)
        {
            String lockOwner = lockState.getOwner();
            Date expiryDate = lockState.getExpires();
            LockStatus status = LockUtils.lockStatus(lockOwner, lockOwner, expiryDate);
            if (status.equals(LockStatus.LOCK_EXPIRED))
            {
                lockState = null;
            }
        }
        return lockState;
    }

    private LockableAspectInterceptorTrait createTrait()
    {
        return new LockableAspectInterceptorTrait() {

            @Override
            public LockState traitImplOf_getLockState(final NodeRef nodeRef)
            {
                return AJExtender.run(new AJExtender.ExtensionBypass<LockState>() {
                    @Override
                    public LockState run()
                    {
                        return getLockState(nodeRef);
                    };
                });
            }
        };
    }

    @Override
    public <T extends Trait> ExtendedTrait<T> getTrait(Class<? extends T> traitAPI)
    {
        return (ExtendedTrait<T>) lockableAspectInterceptorTrait;
    }
}
