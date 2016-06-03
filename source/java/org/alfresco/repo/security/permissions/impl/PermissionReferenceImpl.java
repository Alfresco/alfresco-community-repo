package org.alfresco.repo.security.permissions.impl;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.service.namespace.QName;

/**
 * A simple permission reference (not persisted). A permission is identified by name for a given type, which is
 * identified by its qualified name.
 * 
 * @author andyh
 */
public class PermissionReferenceImpl extends AbstractPermissionReference
{
    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    private static HashMap<QName, HashMap<String, PermissionReferenceImpl>> instances = new HashMap<QName, HashMap<String, PermissionReferenceImpl>>();

    /**
     * 
     */
    private static final long serialVersionUID = -8639601925783501443L;

    private QName qName;

    private String name;

    /**
     * Factory method to create permission references
     * @param qName QName
     * @param name String
     * @return the permissions reference
     */
    public static PermissionReferenceImpl getPermissionReference(QName qName, String name)
    {
        lock.readLock().lock();
        try
        {
            HashMap<String, PermissionReferenceImpl> typed = instances.get(qName);
            if(typed != null)
            {
                PermissionReferenceImpl instance = typed.get(name);
                if(instance != null)
                {
                    return instance;
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
            HashMap<String, PermissionReferenceImpl> typed = instances.get(qName);
            if(typed == null)
            {
                typed = new HashMap<String, PermissionReferenceImpl>();
                instances.put(qName, typed);
            }
            PermissionReferenceImpl instance = typed.get(name);
            if(instance == null)
            {
                instance = new PermissionReferenceImpl(qName, name);
                typed.put(name, instance);
            }
            return instance;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    protected PermissionReferenceImpl(QName qName, String name)
    {
        this.qName = qName;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public QName getQName()
    {
        return qName;
    }
}
