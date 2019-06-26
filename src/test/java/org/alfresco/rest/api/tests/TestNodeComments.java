/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.Comments;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Nodes;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.People;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Activity;
import org.alfresco.rest.api.tests.client.data.Comment;
import org.alfresco.rest.api.tests.client.data.SiteRole;
import org.alfresco.rest.api.tests.client.data.Tag;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * V1 REST API tests for Node Comments
 *
 */
public class TestNodeComments extends EnterpriseTestApi
{
	private TestNetwork network1;
	private TestNetwork network2;

	private List<TestPerson> people = new ArrayList<TestPerson>();
	private List<TestSite> sites = new ArrayList<TestSite>();

	private TestPerson person11;
	private TestPerson person12;
	private TestPerson person13;
	private TestPerson person14;

	private TestPerson person21;
	private TestPerson person22;
	
	private NodeRef nodeRef1;
	private NodeRef folderNodeRef1;
	private NodeRef nodeRef2;
	private NodeRef nodeRef3;
	private NodeRef nodeRef4;
	private NodeRef cmObjectNodeRef;
	private NodeRef customTypeObject;
	private NodeRef nodeRef5;
	
	@Override
	@Before
	public void setup() throws Exception
	{
		// init networks
		super.setup();
		
		Iterator<TestNetwork> accountsIt = getTestFixture().getNetworksIt();
		this.network1 = accountsIt.next();
		this.network2 = accountsIt.next();

		// Create users in different networks and a site
		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network1.createUser();
				people.add(person);
				person = network1.createUser();
				people.add(person);
				person = network1.createUser();
				people.add(person);
				person = network1.createUser();
				people.add(person);

				return null;
			}
		}, network1.getId());

		this.person11 = people.get(0);
		this.person12 = people.get(1);
		this.person13 = people.get(2);
		this.person14 = people.get(3);

		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network2.createUser();
				people.add(person);
				person = network2.createUser();
				people.add(person);

				return null;
			}
		}, network2.getId());

		this.person21 = people.get(4);
		this.person22 = people.get(5);

		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestSite site = network1.createSite(SiteVisibility.PRIVATE);
				sites.add(site);

				site.updateMember(person13.getId(), SiteRole.SiteCollaborator);
				site.updateMember(person14.getId(), SiteRole.SiteCollaborator);
				
				return null;
			}
		}, person11.getId(), network1.getId());

		final TestSite site1 = sites.get(0);
		
		final List<NodeRef> nodes = new ArrayList<NodeRef>();
		
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				NodeRef nodeRef = repoService.createDocument(site1.getContainerNodeRef("documentLibrary"), "Test Doc", "Test Content");
				nodes.add(nodeRef);
				NodeRef folderNodeRef = repoService.createFolder(site1.getContainerNodeRef("documentLibrary"), "Test Folder");
				nodeRef = folderNodeRef;
				nodes.add(nodeRef);
				nodeRef = repoService.createDocument(site1.getContainerNodeRef("documentLibrary"), "Test Doc 1", "Test Content 1");
				nodes.add(nodeRef);
				nodeRef = repoService.createDocument(site1.getContainerNodeRef("documentLibrary"), "Test Doc 2", "Test Content 2");
				nodes.add(nodeRef);
				nodeRef = repoService.createDocument(site1.getContainerNodeRef("documentLibrary"), "Test Doc 3", "Test Content 3");
				nodes.add(nodeRef);
				nodeRef = repoService.createCmObject(site1.getContainerNodeRef("documentLibrary"), "CM Object");
				nodes.add(nodeRef);
				nodeRef = repoService.createObjectOfCustomType(site1.getContainerNodeRef("documentLibrary"), "Custom type object", "{custom.model}sop");
				nodes.add(nodeRef);
				nodeRef = repoService.createDocument(folderNodeRef, "Test Doc 4", "Test Content 4 - in Test Folder");
				nodes.add(nodeRef);

				return null;
			}
		}, person11.getId(), network1.getId());
		
		this.nodeRef1 = nodes.get(0);
		this.folderNodeRef1 = nodes.get(1);
		this.nodeRef2 = nodes.get(2);
		this.nodeRef3 = nodes.get(3);
		this.nodeRef4 = nodes.get(4);
		this.cmObjectNodeRef = nodes.get(5);
		this.customTypeObject = nodes.get(6);
		this.nodeRef5 = nodes.get(7);
	}

	@Test
	// TODO test embedded entity createdBy full visibility e.g. view comment by another user who's full details the caller can't see
	// TODO test update comment and modifiedBy in result is a person object
	public void testNodeComments() throws Exception
	{
		Comments commentsProxy = publicApiClient.comments();
		Nodes nodesProxy = publicApiClient.nodes();
		People peopleProxy = publicApiClient.people();

		// Test Case cloud-1518
		// Create comments

		// invalid node id
		try
		{
			Comment comment = new Comment("Test Comment 4", "Test Comment 4");
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			commentsProxy.createNodeComment(GUID.generate(), comment);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// person from the same network - no permission
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
			Comment comment = new Comment("Test Comment 4", "Test Comment 4");
			commentsProxy.createNodeComment(nodeRef1.getId(), comment);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
		}
		
		// Test Case cloud-2196
		// multi-byte characters, create and update comments
		{
			Comment[] multiByteComments = new Comment[]
			{
					new Comment("ڠڡڢ", "ڠڡڢ"),
					new Comment("\u67e5\u770b\u5168\u90e8", "\u67e5\u770b\u5168\u90e8")
			};

			Map<String, Comment> createdComments = new HashMap<String, Comment>();
			for(Comment comment : multiByteComments)
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				Comment ret = commentsProxy.createNodeComment(nodeRef2.getId(), comment);
				createdComments.put(ret.getId(), ret);
			}
			
			// test that it is possible to add comment to custom type node
			commentsProxy.createNodeComment(customTypeObject.getId(), new Comment("Custom type node comment", "The Comment"));
			
			try
			{
				// test that it is not possible to add comment to cm:object node
				commentsProxy.createNodeComment(cmObjectNodeRef.getId(),  new Comment("CM Object node comment", "The Comment"));
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			// get comments of the non-folder/non-document nodeRef
			try
			{
				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems);
				commentsProxy.getNodeComments(cmObjectNodeRef.getId(), createParams(paging, null));
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
			}
			
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems);
			ListResponse<Comment> resp = commentsProxy.getNodeComments(nodeRef2.getId(), createParams(paging, null));
			List<Comment> retComments = resp.getList();
			assertEquals(2, retComments.size());
			for(Comment comment : retComments)
			{
				String commentId = comment.getId();
				Comment expectedComment = createdComments.get(commentId);
				expectedComment.expected(comment);
			}

			Comment[] multiByteCommentUpdates = new Comment[]
  			{
  					new Comment("ӉӋӐӞ", "ӉӋӐӞ"),
  					new Comment("\u4e00\u4e01\u4e02\u4e03", "\u4e00\u4e01\u4e02\u4e03")
  			};
			
			Map<String, Comment> updatedComments = new HashMap<String, Comment>();
			for(Comment comment : multiByteCommentUpdates)
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				Comment ret = commentsProxy.createNodeComment(nodeRef2.getId(), comment);
				updatedComments.put(ret.getId(), ret);
			}

			skipCount = 0;
			maxItems = 2;
			paging = getPaging(skipCount, maxItems);
			resp = commentsProxy.getNodeComments(nodeRef2.getId(), createParams(paging, null));
			retComments = resp.getList();
			assertEquals(2, retComments.size());
			for(Comment comment : retComments)
			{
				String commentId = comment.getId();
				Comment expectedComment = updatedComments.get(commentId);
				expectedComment.expected(comment);
			}
		}

		{
			// special characters
			Comment comment = new Comment("", "?*^&*(,");
			List<Comment> expectedComments = new ArrayList<Comment>(1);
			expectedComments.add(comment);

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			commentsProxy.createNodeComment(nodeRef3.getId(), comment);

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			ListResponse<Comment> resp = commentsProxy.getNodeComments(nodeRef3.getId(), createParams(paging, null));
			checkList(expectedComments.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		try
		{
			Comment comment = new Comment("", "");
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			commentsProxy.createNodeComment(nodeRef2.getId(), comment);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}

		DateFormat format = PublicApiDateFormat.getDateFormat();
		final List<Comment> expectedComments = new ArrayList<Comment>(10);
		final List<Comment> comments = new ArrayList<Comment>(10);
		comments.add(new Comment("Test Comment 4", "Test Comment 4"));
		comments.add(new Comment("Test Comment 1", "Test Comment 1"));
		comments.add(new Comment("Test Comment 3", "Test Comment 3"));
		comments.add(new Comment("Test Comment 2", "Test Comment 2"));

		{
			Date time = new Date();
			for(Comment comment : comments)
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
				Comment resp = commentsProxy.createNodeComment(nodeRef1.getId(), comment);
				// check response
				assertEquals(comment.getContent(), resp.getContent());
				assertFalse(StringUtils.isEmpty(resp.getCreatedBy().getDisplayName()));
				assertEquals(resp.getCreatedBy().getDisplayName(), person11.getDisplayName());
				assertTrue(format.parse(resp.getCreatedAt()).after(time));
				person11.expected(resp.getCreatedBy());
				assertNotNull(resp.getId());

				expectedComments.add(resp);
			}
			
			// check activities have been raised
			repoService.generateFeed();

			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

				Paging paging = getPaging(0, Integer.MAX_VALUE);
				ListResponse<Activity> activities = peopleProxy.getActivities(person11.getId(), createParams(paging, null));

				boolean found = false;
				for(Activity activity : activities.getList())
				{
					String activityType = activity.getActivityType();
					if(activityType.equals(ActivityType.COMMENT_CREATED))
					{
						Map<String, Object> summary = activity.getSummary();
						assertNotNull(summary);
						String objectId = (String)summary.get("objectId");
						assertNotNull(objectId);
						if(nodeRef1.getId().equals(objectId))
						{
							found = true;
							break;
						}
					}
				}

				assertTrue(found);
			}
		}

		// try to add a comment to a comment
		try
		{
			Comment comment = comments.get(0);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			Comment newComment = commentsProxy.createNodeComment(nodeRef1.getId(), comment);
			expectedComments.add(newComment);

			commentsProxy.createNodeComment(newComment.getId(), comment);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		// try to add a comment to a tag
		try
		{
			Comment comment = comments.get(0);
			Tag tag = new Tag("taggification");
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			Tag addedTag = nodesProxy.createNodeTag(nodeRef1.getId(), tag);
			commentsProxy.createNodeComment(addedTag.getId(), comment);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}
		
		// add a comment to a folder
		{
			Date time = new Date();

			Comment comment = comments.get(0);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			Comment resp = commentsProxy.createNodeComment(folderNodeRef1.getId(), comment);

			// check response
			assertEquals(comment.getContent(), resp.getContent());
			assertTrue(format.parse(resp.getCreatedAt()).after(time));
			person11.expected(resp.getCreatedBy());
			assertNotNull(resp.getId());
		}

		Collections.sort(expectedComments);

		// Test Case cloud-2205
		// Test Case cloud-2217
		// Test Case cloud-1517
		// pagination
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			ListResponse<Comment> resp = commentsProxy.getNodeComments(nodeRef1.getId(), createParams(paging, null));
			checkList(expectedComments.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}
		
		{
			int skipCount = 2;
			int maxItems = 10;
			Paging paging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			ListResponse<Comment> resp = commentsProxy.getNodeComments(nodeRef1.getId(), createParams(paging, null));
			checkList(expectedComments.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}
		
		// invalid node id - 404
		try
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging expectedPaging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			commentsProxy.getNodeComments("invalid", createParams(expectedPaging, null));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		try
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			commentsProxy.getNodeComments(nodeRef1.getId() + ";pwc", createParams(paging, null));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// suffix the node id with a version number
		{
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			ListResponse<Comment> resp = commentsProxy.getNodeComments(nodeRef1.getId() + ";3.0", createParams(paging, null));
			checkList(expectedComments.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		// view comments of a document created by another person in the same network, who is not a member of the site
		// in which the comment resides
		try
		{
			int skipCount = 0;
			int maxItems = 2;
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
			Paging expectedPaging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			commentsProxy.getNodeComments(nodeRef1.getId(), createParams(expectedPaging, null));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
		}

		// document owned by another person in another network, the user is not a member of that network
		try
		{
			int skipCount = 0;
			int maxItems = 2;
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person21.getId()));
			Paging expectedPaging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			commentsProxy.getNodeComments(nodeRef1.getId(), createParams(expectedPaging, null));
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}

		// Test Case cloud-1971
		// invalid methods
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			
			int skipCount = 0;
			int maxItems = 2;
			Paging expectedPaging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			ListResponse<Comment> resp = commentsProxy.getNodeComments(nodeRef1.getId(), createParams(expectedPaging, null));
			List<Comment> nodeComments = resp.getList();
			assertTrue(nodeComments.size() > 0);
			Comment comment = nodeComments.get(0);

			try
			{
				commentsProxy.create("nodes", nodeRef1.getId(), "comments", comment.getId(), null, "Unable to POST to a node comment");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode()); 
			}
	
			try
			{
				commentsProxy.update("nodes", nodeRef1.getId(), "comments", null, null, "Unable to PUT node comments");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
	
			try
			{
				commentsProxy.getSingle("nodes", nodeRef1.getId(), "comments", comment.getId(), "Unable to GET a node comment");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			try
			{
				commentsProxy.remove("nodes", nodeRef1.getId(), "comments", null, "Unable to DELETE node comments");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
		}

		// Test Case cloud-2184
		// update comments
		{
			Comment[] testComments = new Comment[]
			{
					new Comment("ӉӋӐӞ", "ӉӋӐӞ"),
					new Comment("\u4e00\u4e01\u4e02\u4e03", "\u4e00\u4e01\u4e02\u4e03")
			};
	
			List<Comment> mlComments = new ArrayList<Comment>();
			mlComments.add(new Comment("ӉӋӐӞ", "ӉӋӐӞ"));
			mlComments.add(new Comment("\u4e00\u4e01\u4e02\u4e03", "\u4e00\u4e01\u4e02\u4e03"));

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			// create some comments
			Map<String, Comment> createdComments = new HashMap<String, Comment>();
			for(Comment comment : testComments)
			{

				Comment ret = commentsProxy.createNodeComment(nodeRef4.getId(), comment);
				createdComments.put(ret.getId(), ret);
			}

			// update them with multi-byte content
			int i = 0;
			List<Comment> updatedComments = new ArrayList<Comment>();
			for(Comment comment : createdComments.values())
			{
				Comment updateComment = mlComments.get(i);
				Comment ret = commentsProxy.updateNodeComment(nodeRef4.getId(), comment.getId(), updateComment);
				updatedComments.add(ret);
				i++;
			}
			Collections.sort(updatedComments);

			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems, mlComments.size(), mlComments.size());
			ListResponse<Comment> resp = commentsProxy.getNodeComments(nodeRef4.getId(), createParams(paging, null));
			checkList(updatedComments, paging.getExpectedPaging(), resp);
		}
		                                    			
		// invalid node id
		try
		{
			Comment comment = expectedComments.get(1);
			Comment update = new Comment("Test Comment 4", "Test Comment 4");
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			commentsProxy.updateNodeComment(GUID.generate(), comment.getId(), update);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// invalid comment id
		try
		{
			Comment update = new Comment("Test Comment 4", "Test Comment 4");
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			commentsProxy.updateNodeComment(nodeRef1.getId(), GUID.generate(), update);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// person from the same network, not comment creator
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person12.getId()));
			Comment comment = expectedComments.get(1);
			Comment update = new Comment("Test Comment 4", "Test Comment 4");
			commentsProxy.updateNodeComment(nodeRef1.getId(), comment.getId(), update);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
		}

		// person from a different network
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person22.getId()));
			Comment comment = expectedComments.get(1);
			Comment update = new Comment("Test Comment 4", "Test Comment 4");
			commentsProxy.updateNodeComment(nodeRef1.getId(), comment.getId(), update);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
		}

		// successful update
		{
			Date time = new Date();

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			Comment comment = expectedComments.get(1);
			Comment update = new Comment("Updated comment", "Updated comment");
			Comment resp = commentsProxy.updateNodeComment(nodeRef1.getId(), comment.getId(), update);

			Thread.sleep(100); // simulate a user edit to a comment

			Comment expected = new Comment(comment);
			expected.setTitle("Updated comment");
			expected.setEdited(true);
			expected.setContent("Updated comment");
			expected.setModifiedBy(repoService.getPerson(person11.getId()));
			expected.setModifiedAt(PublicApiDateFormat.getDateFormat().format(time));
			expected.expected(resp);
		}

		// delete comments

		// invalid node ref
		try
		{
			Comment comment = expectedComments.get(1);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			commentsProxy.removeNodeComment(GUID.generate(), comment.getId());
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		
		// invalid comment id
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			commentsProxy.removeNodeComment(nodeRef1.getId(), GUID.generate());
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// successful delete
		{
			Comment toDelete = expectedComments.get(1);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			commentsProxy.removeNodeComment(nodeRef1.getId(), toDelete.getId());

			// check it's been removed
			int skipCount = 0;
			int maxItems = Integer.MAX_VALUE;
			Paging expectedPaging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));
			ListResponse<Comment> resp = commentsProxy.getNodeComments(nodeRef1.getId(), createParams(expectedPaging, null));
			List<Comment> actualComments = resp.getList();
			assertFalse(actualComments.contains(toDelete));
		}
		
		// PUT: test update with null/empty comment
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			Comment comment = new Comment();
			comment.setContent("my comment");
			Comment createdComment = commentsProxy.createNodeComment(nodeRef1.getId(), comment);

			Comment updatedComment = new Comment();
			updatedComment.setContent(null);
			commentsProxy.updateNodeComment(nodeRef1.getId(), createdComment.getId(), updatedComment);
			
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
		}
		
        // ACE-5463
        testSkipCountHighValue(expectedComments, commentsProxy);
	}
	
	@Test
	public void testNodeCommentsAndLocking() throws Exception
	{
		Comments commentsProxy = publicApiClient.comments();

		// locked node - cannot add/edit/delete comments (MNT-14945, MNT-16446)
		// only the lock owner can (ALF-21907)

		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    TestSite site = sites.get(0);
                    site.updateMember(person13.getId(), SiteRole.SiteManager);
                    site.updateMember(person14.getId(), SiteRole.SiteManager);

                    return null;
                }
            }, person11.getId(), network1.getId());

			Comment comment = new Comment();
			comment.setContent("my comment");
			Comment createdComment = commentsProxy.createNodeComment(nodeRef1.getId(), comment);

			TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					repoService.lockNode(nodeRef1);
					return null;
				}
			}, person13.getId(), network1.getId());

			// change to not lock owner and not node owner
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14.getId()));

			// test GET for a locked node
            int skipCount = 0;
            int maxItems = Integer.MAX_VALUE;
            Paging paging = getPaging(skipCount, maxItems);
			commentsProxy.getNodeComments(nodeRef1.getId(), createParams(paging, null));

			// test POST for a locked node
			try
			{
				comment = new Comment();
				comment.setContent("my other comment");
				createdComment = commentsProxy.createNodeComment(nodeRef1.getId(), comment);

				fail("");
			}
			catch (PublicApiException e)
			{
				assertEquals(HttpStatus.SC_CONFLICT, e.getHttpResponse().getStatusCode());
			}

			// test PUT for a locked node
			try
			{
				Comment updatedComment = new Comment();
				updatedComment.setContent("my comment");
				commentsProxy.updateNodeComment(nodeRef1.getId(), createdComment.getId(), updatedComment);

				fail("");
			}
			catch (PublicApiException e)
			{
				assertEquals(HttpStatus.SC_CONFLICT, e.getHttpResponse().getStatusCode());
			}

			// test DELETE for a locked node
			try
			{
				commentsProxy.removeNodeComment(nodeRef1.getId(), createdComment.getId());

				fail("");
			}
			catch (PublicApiException e)
			{
				assertEquals(HttpStatus.SC_CONFLICT, e.getHttpResponse().getStatusCode());
			}

            // change to node creator
            publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

            // test POST for a locked node
            try
            {
                comment = new Comment();
                comment.setContent("my other comment");
                createdComment = commentsProxy.createNodeComment(nodeRef1.getId(), comment);

                fail("");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_CONFLICT, e.getHttpResponse().getStatusCode());
            }

            // test PUT for a locked node
            try
            {
                Comment updatedComment = new Comment();
                updatedComment.setContent("my comment");
                commentsProxy.updateNodeComment(nodeRef1.getId(), createdComment.getId(), updatedComment);

                fail("");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_CONFLICT, e.getHttpResponse().getStatusCode());
            }

            // test DELETE for a locked node
            try
            {
                commentsProxy.removeNodeComment(nodeRef1.getId(), createdComment.getId());

                fail("");
            }
            catch (PublicApiException e)
            {
                assertEquals(HttpStatus.SC_CONFLICT, e.getHttpResponse().getStatusCode());
            }

			// change to lock owner
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person13.getId()));

			// test GET for a locked node
            commentsProxy.getNodeComments(nodeRef1.getId(), createParams(paging, null));

			// test POST for a locked node
			comment = new Comment();
			comment.setContent("my other comment");
			createdComment = commentsProxy.createNodeComment(nodeRef1.getId(), comment);

			// test PUT for a locked node
			Comment updatedComment = new Comment();
			updatedComment.setContent("my comment");
			commentsProxy.updateNodeComment(nodeRef1.getId(), createdComment.getId(), updatedComment);

			// test DELETE for a locked node
			commentsProxy.removeNodeComment(nodeRef1.getId(), createdComment.getId());
		}
		finally
		{
		    // undo the lock
			TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					repoService.unlockNode(nodeRef1);
					return null;
				}
			}, person13.getId(), network1.getId());

            // put the other members back to SiteCollaborator
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    TestSite site = sites.get(0);
                    site.updateMember(person13.getId(), SiteRole.SiteCollaborator);
                    site.updateMember(person14.getId(), SiteRole.SiteCollaborator);

                    return null;
                }
            }, person11.getId(), network1.getId());
		}
	}

	// lock recursively (MNT-14945, MNT-16446, REPO-1150)
	@Test
	public void testNodeCommentsAndLockingIncludingChildren() throws Exception
	{
		Comments commentsProxy = publicApiClient.comments();

		// TODO push-down to future CommentServiceImplTest (see ACE-5437) - since includeChildren is via LockService api only

		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person11.getId()));

			Comment comment = new Comment();
			comment.setContent("my comment");
			Comment createdComment = commentsProxy.createNodeComment(nodeRef5.getId(), comment);

			// recursive lock (folderRef1, nodeRef5)
			TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					repoService.lockNode(folderNodeRef1, LockType.WRITE_LOCK, 0, true);
					return null;
				}
			}, person11.getId(), network1.getId());

		}
		finally
		{
			TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					repoService.unlockNode(folderNodeRef1, true);
					return null;
				}
			}, network1.getId());
		}
	}

	@Test
	public void test_MNT_16446() throws Exception
	{
		Comments commentsProxy = publicApiClient.comments();

		// in a site

		publicApiClient.setRequestContext(new RequestContext(network1.getId(), person13.getId()));
		Comment comment = new Comment("Test Comment 1", "Test Comment 1");
		Comment resp = commentsProxy.createNodeComment(nodeRef1.getId(), comment);
		String commentId = resp.getId();

		// MNT-16446: another site collaborator should not be able to edit
		try
		{
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person14.getId()));
			Comment update = new Comment("Test Comment 4", "Test Comment 4");
			commentsProxy.updateNodeComment(nodeRef1.getId(), commentId, update);
			fail();
		}
		catch (PublicApiException e)
		{
			assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
		}

		publicApiClient.setRequestContext(new RequestContext(network1.getId(), person13.getId()));

		Comment update = new Comment("Updated comment", "Updated comment");
		commentsProxy.updateNodeComment(nodeRef1.getId(), commentId, update);

		commentsProxy.removeNodeComment(nodeRef1.getId(), commentId);
	}

    // test for retrieving the list of comments with high value of skipCount(e.g. 10)
    public void testSkipCountHighValue(List<Comment> expectedComments, Comments commentsProxy) throws PublicApiException
    {
        try
        {
            int skipCount = 10;
            int maxItems = 2;
            Paging paging = getPaging(skipCount, maxItems, expectedComments.size(), expectedComments.size());
            commentsProxy.getNodeComments(nodeRef1.getId(), createParams(paging, null));

        }
        catch (IllegalStateException e)
        {
            fail();
        }
    }
}
