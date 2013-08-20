package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.rest.api.tests.PublicApiDateFormat;
import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Comment implements Serializable, ExpectedComparison, Comparable<Comment>
{
	private static final long serialVersionUID = -1804591941972808543L;

	private String nodeId;
	private String id;
	private String title;
	private String content;
	private Boolean edited;
	private Person createdBy;
	private String createdAt;
	private String modifiedAt;
	private Person modifiedBy;
	private Boolean updated;
//	private Boolean canEdit;
//	private Boolean canDelete;

	public Comment()
	{
	}
	
	public Comment(Comment c)
	{
		this(c.getNodeId(), c.getId(), c.getTitle(), c.getContent(), c.getCreatedBy(), c.getCreatedAt(), c.getModifiedBy(), c.getModifiedAt(), c.getUpdated(), c.getEdited());
	}

	public Comment(String title, String content, Boolean edited, Boolean updated, Person createdBy, Person modifiedBy)
	{
		this.title = title;
		this.content = content;
		this.edited = edited;
		this.updated = updated;
		this.createdBy = createdBy;
		this.modifiedBy = modifiedBy;
		
		DateFormat format = PublicApiDateFormat.getDateFormat();
		this.createdAt = format.format(new Date());
		this.modifiedAt = format.format(new Date());
		
	}

	public Comment(String title, String content)
	{
		this.title = title;
		this.content = content;
	}

	public Comment(String nodeId, String id, String title, String content, Person createdBy, String createdAt, Person modifiedBy, String modifiedAt, Boolean updated, Boolean edited)
	{
		this.nodeId = nodeId;
		this.id = id;
		this.title = title;
		this.content = content;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.modifiedBy = modifiedBy;
		this.modifiedAt = modifiedAt;
		this.updated = updated;
		this.edited = edited;
	}

	public Boolean getEdited()
	{
		return edited;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public String getId()
	{
		return id;
	}

	public String getTitle()
	{
		return title;
	}

	public String getContent()
	{
		return content;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public Person getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Person createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public Person getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Person modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Boolean getUpdated() {
		return updated;
	}

	public void setUpdated(Boolean updated) {
		this.updated = updated;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	public Boolean isEdited() {
		return edited;
	}

	public void setEdited(Boolean edited) {
		this.edited = edited;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON(boolean createdByVisibility)
	{
		JSONObject commentJson = new JSONObject();
//		commentJson.put("id", getId());
		commentJson.put("title", getTitle());
		commentJson.put("content", getContent());
//		if(createdBy != null)
//		{
//			commentJson.put("createdBy", createdBy.toJSON(createdByVisibility));
//		}
//		commentJson.put("createdAt", getCreatedAt());
//		if(modifiedBy != null)
//		{
//			commentJson.put("modifiedBy", modifiedBy.toJSON(createdByVisibility));
//		}
//		commentJson.put("modifiedAt", getModifiedAt());
//		commentJson.put("edited", isEdited());
		return commentJson;
	}
	
	@Override
	public String toString()
	{
		return "Comment [nodeId=" + nodeId + ", id=" + id + ", title=" + title
				+ ", content=" + content + ", createdBy=" + createdBy
				+ ", createdAt=" + createdAt + ", modifiedAt=" + modifiedAt
				+ ", modifiedBy=" + modifiedBy + ", updated=" + updated + "]";
	}

	public static Comment parseComment(String nodeId, JSONObject jsonObject)
	{
		String id = (String)jsonObject.get("id");
		String title = (String)jsonObject.get("title");
		String content = (String)jsonObject.get("content");
		JSONObject createdByJson = (JSONObject)jsonObject.get("createdBy");
		Person createdBy = null;
		if(createdByJson != null)
		{
			createdBy = Person.parsePerson(createdByJson);
		}
		String createdAt = (String)jsonObject.get("createdAt");
		JSONObject modifiedByJson = (JSONObject)jsonObject.get("modifiedBy");
		Person modifiedBy = null;
		if(modifiedByJson != null)
		{
			modifiedBy = Person.parsePerson(modifiedByJson);
		}
		String modifiedAt = (String)jsonObject.get("modifiedAt");
		Boolean edited = (Boolean)jsonObject.get("edited");
		Boolean updated = (Boolean)jsonObject.get("updated");
		Comment comment = new Comment(nodeId, id, title, content, createdBy, createdAt, modifiedBy, modifiedAt, updated, edited);
		return comment;
	}

	public static ListResponse<Comment> parseComments(String nodeId, JSONObject jsonObject)
	{
		List<Comment> comments = new ArrayList<Comment>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			comments.add(Comment.parseComment(nodeId, entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
		return new ListResponse<Comment>(paging, comments);
	}
	
	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof Comment);
		
		Comment other = (Comment)o;
		
		AssertUtil.assertEquals("content", content, other.getContent());
		AssertUtil.assertEquals("id", id, other.getId());
		AssertUtil.assertEquals("title", title, other.getTitle());
		AssertUtil.assertEquals("edited", edited, other.isEdited());
		if(createdBy != null)
		{
			createdBy.expected(other.getCreatedBy());
		}

		String modifiedAtStr = getModifiedAt();
		if(modifiedAtStr != null)
		{
			try
			{
				Date modifiedAt = PublicApiDateFormat.getDateFormat().parse(modifiedAtStr);
				Date otherModifiedAt = PublicApiDateFormat.getDateFormat().parse(other.getModifiedAt());
				assertTrue(otherModifiedAt.after(modifiedAt) || otherModifiedAt.equals(modifiedAt));
			}
			catch(ParseException e)
			{
				throw new RuntimeException(e);
			}
		}

		String createdAtStr = getCreatedAt();
		if(createdAtStr != null)
		{
			try
			{
				Date createdAt = PublicApiDateFormat.getDateFormat().parse(createdAtStr);
				Date otherCreatedAt = PublicApiDateFormat.getDateFormat().parse(other.getCreatedAt());
				assertTrue(otherCreatedAt.after(createdAt) || otherCreatedAt.equals(createdAt));
			}
			catch(ParseException e)
			{
				throw new RuntimeException(e);
			}
		}

		if(modifiedBy != null)
		{
			modifiedBy.expected(other.getModifiedBy());
		}
		AssertUtil.assertEquals("updated", updated, other.getUpdated());
	}

	@Override
	public int compareTo(Comment o)
	{
		// reverse chronological order
		return o.getCreatedAt().compareTo(createdAt);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Comment other = (Comment) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
