package org.alfresco.opencmis;

import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * A registry of OpenCMIS bindings to dispatchers.
 * 
 * @author steveglover
 *
 */
public interface CMISDispatcherRegistry
{
	/*
	 * Supported CMIS bindings
	 */
	public static enum Binding
	{
		atom, browser;
		
		public BindingType getOpenCmisBinding()
		{
			BindingType bindingType = null;

			if(this == atom)
			{
				bindingType = BindingType.ATOMPUB;
			}
			else if(this == browser)
			{
				bindingType = BindingType.BROWSER;
			}

			return bindingType;
		}
	};
	
	public static class Endpoint
	{
		private Binding binding;
		private String version;

		public Endpoint(Binding binding, String version)
		{
			super();
			this.binding = binding;
			this.version = version;
		}
		
		public Binding getBinding()
		{
			return binding;
		}
		
		public String getVersion()
		{
			return version;
		}
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((binding == null) ? 0 : binding.hashCode());
			result = prime * result
					+ ((version == null) ? 0 : version.hashCode());
			return result;
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
			Endpoint other = (Endpoint) obj;
			if (binding != other.binding)
				return false;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (!version.equals(other.version))
				return false;
			return true;
		}
	}
	
	public void registerDispatcher(Endpoint endpoint, CMISDispatcher dispatcher);
	public CMISDispatcher getDispatcher(WebScriptRequest req);
}
