package org.alfresco.repo.solr;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * A method interceptor that intercepts method calls on the {@link SOLRTrackingComponent} 
 * in order to determine if a remote Solr instance is active. If so, an application event
 * is generated to indicate this.
 * 
 * This is used by the Solr JMX code to export Solr mbeans only if the remote Solr instance
 * is active.
 * 
 * since 4.0
 *
 */
public class SolrTrackingMethodInterceptor implements MethodInterceptor, ApplicationEventPublisherAware
{
    private final WriteLock writeLock;
	private boolean solrActive = false;
    private ApplicationEventPublisher applicationEventPublisher;

    public SolrTrackingMethodInterceptor()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        writeLock = lock.writeLock();
    }
    
    private void broadcastSolrActive()
    {
    	applicationEventPublisher.publishEvent(new SolrActiveEvent(this));
    	solrActive = true;
    }
    
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
	{
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        writeLock.lock();
        try
        {
        	if(!solrActive)
        	{
        		broadcastSolrActive();
        		solrActive = true;
        	}
        }
        finally
        {
            writeLock.unlock();
        }

        return mi.proceed();
    }

}
