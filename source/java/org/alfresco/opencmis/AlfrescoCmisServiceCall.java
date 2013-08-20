package org.alfresco.opencmis;

import org.apache.chemistry.opencmis.commons.server.CallContext;

public class AlfrescoCmisServiceCall
{
	private static ThreadLocal<CallContext> context = new ThreadLocal<CallContext>();
	
	public static void set(CallContext newContext)
	{
		context.set(newContext);
	}
	
	public static CallContext get()
	{
		return context.get();
	}
	
	public static void clear()
	{
		context.remove();
	}
}
