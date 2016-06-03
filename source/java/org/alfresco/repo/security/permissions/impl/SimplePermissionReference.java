package org.alfresco.repo.security.permissions.impl;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.service.namespace.QName;

/**
 * A simple permission reference.
 * 
 * @author andyh
 */
public final class SimplePermissionReference extends AbstractPermissionReference
{   
    private static final long serialVersionUID = 637302438293417818L;

    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    private static HashMap<QName, HashMap<String, SimplePermissionReference>> instances = new HashMap<QName, HashMap<String, SimplePermissionReference>>();

    /**
     * Factory method to create simple permission refrences
     * 
     * @return a simple permission reference
     */
    public static SimplePermissionReference getPermissionReference(QName qName, String name)
    {
        lock.readLock().lock();
        try
        {
            HashMap<String, SimplePermissionReference> typed = instances.get(qName);
            if(typed != null)
            {
                SimplePermissionReference instance = typed.get(name);
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
            HashMap<String, SimplePermissionReference> typed = instances.get(qName);
            if(typed == null)
            {
                typed = new HashMap<String, SimplePermissionReference>();
                instances.put(qName, typed);
            }
            SimplePermissionReference instance = typed.get(name);
            if(instance == null)
            {
                instance = new SimplePermissionReference(qName, name);
                typed.put(name, instance);
            }
            return instance;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    
    /*
     * The type
     */
    private QName qName;
    
    /*
     * The name of the permission
     */
    private String name;
    
    
    protected SimplePermissionReference(QName qName, String name)
    {
        super();
        this.qName = qName;
        this.name = name;
    }

    public QName getQName()
    {
        return qName;
    }

    public String getName()
    {
        return name;
    }

}
