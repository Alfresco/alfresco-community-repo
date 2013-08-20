package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.client.PublicApiClient.Comments;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Nodes;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.PublicApiClient.Tags;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Comment;
import org.alfresco.rest.api.tests.client.data.Tag;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;

public class TestTags extends EnterpriseTestApi
{
	@Test
	public void testTags() throws Exception
	{
		Iterator<TestNetwork> networksIt = getTestFixture().getNetworksIt();
		assertTrue(networksIt.hasNext());
		final TestNetwork network1 = networksIt.next();
		assertTrue(networksIt.hasNext());
		final TestNetwork network2 = networksIt.next();

		final List<TestPerson> people = new ArrayList<TestPerson>(3);

		// create users and some preferences
		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network1.createUser();
				people.add(person);
				person = network1.createUser();
				people.add(person);
				return null;
			}
		}, network1.getId());
		
		TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestPerson person = network2.createUser();
				people.add(person);
				return null;
			}
		}, network2.getId());
		
		final TestPerson person1 = people.get(0);
		final TestPerson person2 = people.get(1);
		final TestPerson person3 = people.get(2);
		
		final List<NodeRef> nodes = new ArrayList<NodeRef>();
		final List<TestSite> sites = new ArrayList<TestSite>();

		// Create site
		TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
				TestSite site = network1.createSite(SiteVisibility.PRIVATE);
				sites.add(site);

				NodeRef nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc", "Test Content");
				nodes.add(nodeRef);
				
				nodeRef = repoService.createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc 1", "Test Content 1");
				nodes.add(nodeRef);

				return null;
			}
		}, person1.getId(), network1.getId());

		final NodeRef nodeRef1 = nodes.get(0);
		final NodeRef nodeRef2 = nodes.get(1);

		Nodes nodesProxy = publicApiClient.nodes();
		Comments commentsProxy = publicApiClient.comments();
		Tags tagsProxy = publicApiClient.tags();

		final List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("tag 1"));
		tags.add(new Tag("tag 9"));
		tags.add(new Tag("other tag 3"));
		tags.add(new Tag("my tag 1"));
		tags.add(new Tag("tag 5"));
		
		// try to add a tag to a comment
		try
		{
			Comment comment = new Comment("Test Comment", "Test Comment");
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			Comment newComment = commentsProxy.createNodeComment(nodeRef1.getId(), comment);
			Tag tag = new Tag("testTag");
			nodesProxy.createNodeTag(newComment.getId(), tag);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		// try to add a tag to a tag
		try
		{
			Tag tag = new Tag("testTag");
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			Tag newTag = nodesProxy.createNodeTag(nodeRef1.getId(), tag);
			nodesProxy.createNodeTag(newTag.getId(), tag);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
		}

		// Test Case cloud-2221
		// Test Case cloud-2222
		// multi-byte characters, special characters, create and update tags
		{
			Tag[] multiByteTags = new Tag[]
			{
					new Tag("\u67e5\u770b\u5168\u90e8"),
					new Tag("\u67e5\u770b\u5168\u91e8"),
					new Tag("%^&%&$^√Ç¬£@")
			};

			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));

			// first, create tags
			Map<String, Tag> createdTags = new HashMap<String, Tag>();
			for(Tag tag : multiByteTags)
			{
				Tag ret = nodesProxy.createNodeTag(nodeRef2.getId(), tag);
				createdTags.put(ret.getId(), ret);
			}

			int skipCount = 0;
			int maxItems = Integer.MAX_VALUE;
			Paging paging = getPaging(skipCount, maxItems);
			ListResponse<Tag> resp = nodesProxy.getNodeTags(nodeRef2.getId(), createParams(paging, null));
			List<Tag> retTags = resp.getList();
			assertEquals(createdTags.size(), retTags.size());
			for(Tag tag : retTags)
			{
				String tagId = tag.getId();
				Tag expectedTag = createdTags.get(tagId);
				expectedTag.expected(tag);
			}

			// special characters and update tags
//			{
//				Tag[] specialCharacterTags = new Tag[]
//				{
//						new Tag("\u67e5\u770b\u5168\u90e8"),
//						new Tag("\u67e5\u770b\u5168\u91e8")
//				};
//				
//				createdTags = new HashMap<String, Tag>();
//				for(Tag tag : specialCharacterTags)
//				{
//					Tag ret = nodesProxy.createNodeTag(nodeRef2.getId(), tag);
//					createdTags.put(ret.getId(), ret);
//				}
//				
//				
//				Tag tag = new Tag("%^&%&$^£@");
//				Tag ret = nodesProxy.createNodeTag(nodeRef2.getId(), tag);
//				createdTags.put(ret.getId(), ret);
//			}

			// update tags
			
			try
			{
				// update with an empty tag i.e. "" -> bad request
				Tag tag = new Tag("");
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.createNodeTag(nodeRef2.getId(), tag);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
			}
			
			List<Tag> tagUpdates = new ArrayList<Tag>(createdTags.values());
			tagUpdates.get(0).setTag("\u4e00\u4e01\u4e02\u4e10");
			tagUpdates.get(1).setTag("\u4e00\u4e01\u4e12\u4e11");
			tagUpdates.get(2).setTag("\u4e00\u4e01\u4e12\u4e12");
			Map<String, Tag> updatedTags = new HashMap<String, Tag>();
			for(Tag tag : tagUpdates)
			{
				Tag ret = tagsProxy.update(tag);
				assertNotNull(ret.getId());
				assertNotNull(ret.getTag());
//				tag.expected(ret); disabled because tag id changes
				updatedTags.put(ret.getId(), ret);
			}

			// get updated tags
			List<Tag> expectedNodeTags = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Tag>>()
			{
				@Override
				public List<Tag> doWork() throws Exception
				{
					List<Tag> tags = repoService.getTags(nodeRef2);
					return tags;
				}
			}, person1.getId(), network1.getId());
			
			skipCount = 0;
			maxItems = tagUpdates.size();
			paging = getPaging(skipCount, maxItems, tagUpdates.size(), tagUpdates.size());
			resp = nodesProxy.getNodeTags(nodeRef2.getId(), createParams(paging, null));
			checkList(expectedNodeTags.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
		}

		{
			List<Tag> createdTags = new ArrayList<Tag>();

			// Test Case cloud-1975
			for(Tag tag : tags)
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				Tag ret = nodesProxy.createNodeTag(nodeRef1.getId(), tag);
				assertEquals(tag.getTag(), ret.getTag());
				assertNotNull(ret.getId());
				createdTags.add(ret);
			}

			// update tag, empty string
			try
			{
				Tag tag = new Tag(createdTags.get(0).getId(), "");
				tagsProxy.update(tag);
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_BAD_REQUEST, e.getHttpResponse().getStatusCode());
			}
			
			// invalid node id
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.createNodeTag(GUID.generate(), tags.get(0));
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			// Test Case cloud-1973
			// Test Case cloud-2208
			// Test Case cloud-2219
			// check that the tags are there in the node tags, test paging
			List<Tag> expectedNodeTags = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Tag>>()
			{
				@Override
				public List<Tag> doWork() throws Exception
				{
					List<Tag> tags = repoService.getTags(nodeRef1);
					return tags;
				}
			}, person1.getId(), network1.getId());
			
			{
				int skipCount = 0;
				int maxItems = 2;
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				Paging paging = getPaging(skipCount, maxItems, expectedNodeTags.size(), expectedNodeTags.size());
				ListResponse<Tag> resp = nodesProxy.getNodeTags(nodeRef1.getId(), createParams(paging, null));
				checkList(expectedNodeTags.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}

			{
				int skipCount = 2;
				int maxItems = Integer.MAX_VALUE;
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				Paging paging = getPaging(skipCount, maxItems, expectedNodeTags.size(), expectedNodeTags.size());
				ListResponse<Tag> resp = nodesProxy.getNodeTags(nodeRef1.getId(), createParams(paging, null));
				checkList(expectedNodeTags.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
			}
			
			// invalid node
			try
			{
				int skipCount = 0;
				int maxItems = 2;
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				Paging paging = getPaging(skipCount, maxItems, expectedNodeTags.size(), expectedNodeTags.size());
				ListResponse<Tag> allTags = nodesProxy.getNodeTags("invalidNode", createParams(paging, null));
				checkList(expectedNodeTags.subList(skipCount, paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), allTags);
				fail("");
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
			}

			// user from another account - 403
			try
	        {
		        int skipCount = 0;
		        int maxItems = 2;
		        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person3.getId()));
		        Paging expectedPaging = getPaging(skipCount, maxItems, expectedNodeTags.size(), expectedNodeTags.size());
		        nodesProxy.getNodeTags(nodeRef1.getId(), createParams(expectedPaging, null));
		        fail("");
	        }
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_UNAUTHORIZED, e.getHttpResponse().getStatusCode());
			}

			// another user from the same account
			try
	        {
		        int skipCount = 0;
		        int maxItems = 2;
		        publicApiClient.setRequestContext(new RequestContext(network1.getId(), person2.getId()));
		        Paging paging = getPaging(skipCount, maxItems, expectedNodeTags.size(), expectedNodeTags.size());
		        ListResponse<Tag> resp = nodesProxy.getNodeTags(nodeRef1.getId(), createParams(paging, null));
		        checkList(expectedNodeTags.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), resp);
				fail();
	        }
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_FORBIDDEN, e.getHttpResponse().getStatusCode());
			}
			
			// Test Case cloud-1519
			// Test Case cloud-2206
			// Test Case cloud-2218
			// check that the tags are there in the network tags, test paging
			// TODO for user from another network who is invited to this network
			List<Tag> expectedNetworkTags = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Tag>>()
			{
				@Override
				public List<Tag> doWork() throws Exception
				{
					List<Tag> tags = repoService.getTags();
					return tags;
				}
			}, person1.getId(), network1.getId());

			{
				int skipCount = 0;
				int maxItems = 2;
				Paging paging = getPaging(skipCount, maxItems, expectedNetworkTags.size(), null);
				ListResponse<Tag> allTags = tagsProxy.getTags(createParams(paging, null));
				checkList(expectedNetworkTags.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), allTags);
			}

			{
				int skipCount = 2;
				int maxItems = Integer.MAX_VALUE;
				Paging paging = getPaging(skipCount, maxItems, expectedNetworkTags.size(), null);
				ListResponse<Tag> allTags = tagsProxy.getTags(createParams(paging, null));
				checkList(expectedNetworkTags.subList(skipCount, skipCount + paging.getExpectedPaging().getCount()), paging.getExpectedPaging(), allTags);
			}
		}

		{
			// Try a create with the same tag value
			Tag tag = tags.get(0);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			nodesProxy.createNodeTag(nodeRef1.getId(), tag);
		}

		try
		{
			// Invalid node id
			Tag tag = tags.get(0);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			nodesProxy.createNodeTag(GUID.generate(), tag);
			fail("");
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}

		// Test Case cloud-2183
		// update tags
		{
			// get a network tag
			int skipCount = 0;
			int maxItems = 2;
			Paging paging = getPaging(skipCount, maxItems);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			ListResponse<Tag> allTags = tagsProxy.getTags(createParams(paging, null));
			assertTrue(allTags.getList().size() > 0);

			// and update it
			Tag tag = allTags.getList().get(0);
			String newTagValue = GUID.generate();
			Tag newTag = new Tag(tag.getId(), newTagValue);
			publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
			Tag ret = tagsProxy.update(newTag);
			assertEquals(newTagValue, ret.getTag());
//			assertNotEquals(tag.getId, ret.getId()); // disabled due to CLOUD-628
		}
		
		// update invalid/unknown tag id
		try
		{
			Tag unknownTag = new Tag(GUID.generate(), GUID.generate());
			tagsProxy.update(unknownTag);
			fail();
		}
		catch(PublicApiException e)
		{
			assertEquals(HttpStatus.SC_NOT_FOUND, e.getHttpResponse().getStatusCode());
		}
		

		// Test Case cloud-1972
		// Test Case cloud-1974
		// not allowed methods
		{
			List<Tag> networkTags = TenantUtil.runAsUserTenant(new TenantRunAsWork<List<Tag>>()
			{
				@Override
				public List<Tag> doWork() throws Exception
				{
					List<Tag> tags = repoService.getTags();
					return tags;
				}
			}, person1.getId(), network1.getId());
			assertTrue(networkTags.size() > 0);

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.update("nodes", nodeRef1.getId(), "tags", null, null, "Unable to PUT node tags");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.remove("nodes", nodeRef1.getId(), "tags", null, "Unable to DELETE node tags");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				Tag tag = networkTags.get(0);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.update("nodes", nodeRef1.getId(), "tags", tag.getId(), null, "Unable to PUT node tag");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				Tag tag = networkTags.get(0);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.create("tags", null, null, null, tag.toJSON().toString(), "Unable to POST to tags");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			try
			{
				Tag tag = networkTags.get(0);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.update("tags", null, null, null, tag.toJSON().toString(), "Unable to PUT tags");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.remove("tags", null, null, null, "Unable to DELETE tags");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}

			try
			{
				Tag tag = networkTags.get(0);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.create("tags", tag.getId(), null, null, tag.toJSON().toString(), "Unable to POST to a tag");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			try
			{
				Tag tag = networkTags.get(0);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.remove("tags", tag.getId(), null, null, "Unable to DELETE a tag");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
			
			// delete node tag
			{
				Tag tag = networkTags.get(0);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.removeNodeTag(nodeRef1.getId(), tag.getId());

				// check that the tag is gone
				ListResponse<Tag> resp = nodesProxy.getNodeTags(nodeRef1.getId(), createParams(getPaging(0, Integer.MAX_VALUE), null));
				List<Tag> nodeTags = resp.getList();
				assertTrue(!nodeTags.contains(tag));
			}

			try
			{
				Tag tag = networkTags.get(0);

				publicApiClient.setRequestContext(new RequestContext(network1.getId(), person1.getId()));
				nodesProxy.getSingle("nodes", nodeRef1.getId(), "tags", tag.getId(), "Unable to GET node tag");
				fail();
			}
			catch(PublicApiException e)
			{
				assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, e.getHttpResponse().getStatusCode());
			}
		}
	}
}
