/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
