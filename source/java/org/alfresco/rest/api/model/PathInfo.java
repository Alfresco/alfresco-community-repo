package org.alfresco.rest.api.model;

import java.util.List;

/**
 * Representation of a path info
 *
 * @author janv
 *
 */
public class PathInfo
{
	private String name;
	private Boolean isComplete;
	private List<ElementInfo> elements;

	public PathInfo()
	{
	}

	public PathInfo(String name, Boolean isComplete, List<ElementInfo> elements)
	{
		this.name = name;
		this.isComplete = isComplete;
		this.elements = elements;
	}

	public String getName() {
		return name;
	}

	public Boolean getIsComplete() {
		return isComplete;
	}

	public List<ElementInfo> getElements() {
		return elements;
	}

	public class ElementInfo {

		private String id;

		private String name;

		public ElementInfo()
		{
		}

		public ElementInfo(String id, String name)
		{
			this.id = id;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getId() {
			return id;
		}
	}
}
