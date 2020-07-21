/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forum.CommentService;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.model.Comment;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TypeConstraint;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CommentsImpl} class.
 *
 * @author Chris Shields
 */
public class CommentsImplUnitTest
{
    private CommentsImpl commentsImpl;
    private Nodes nodes;
    private TypeConstraint typeConstraint;
    private CommentService commentService;
    private NodeService nodeService;
    private ContentService contentService;
    private People people;
    
    @Before
    public void setUp(){
        commentsImpl = new CommentsImpl();
        nodes = mock(Nodes.class);
        typeConstraint = mock(TypeConstraint.class);
        commentService = mock(CommentService.class);
        nodeService = mock(NodeService.class);
        contentService = mock(ContentService.class);
        people = mock(People.class);

        commentsImpl.setNodes(nodes);
        commentsImpl.setTypeConstraint(typeConstraint);
        commentsImpl.setCommentService(commentService);
        commentsImpl.setNodeService(nodeService);
        commentsImpl.setContentService(contentService);
        commentsImpl.setPeople(people);
    }

    @Test
    public void createComment()
    {
        String nodeId = "node-id";
        Comment comment = new Comment();
        NodeRef nodeRef = new NodeRef("protocol", "identifier", "id");
        NodeRef commentNode = new NodeRef("protocol", "identifier", "comment-id");
        Map<String, Boolean> map = new HashMap<>();
        map.put(CommentService.CAN_EDIT, true);
        map.put(CommentService.CAN_DELETE, true);

        Map<QName, Serializable> nodeProps = new HashMap<>();
        nodeProps.put(ContentModel.PROP_CREATOR, "user1");
        nodeProps.put(ContentModel.PROP_MODIFIER, "user2");
        
        Person person1 = new Person();
        person1.setUserName("user1");
        person1.setEmail("user1@alfresco.com");
        Person person2 = new Person();
        person2.setUserName("user2");
        person2.setEmail("user2@alfresco.com");

        when(nodes.validateNode(nodeId)).thenReturn(nodeRef);
        when(typeConstraint.matches(nodeRef)).thenReturn(true);
        when(commentService.createComment(nodeRef, comment.getTitle(), comment.getContent(), false)).thenReturn(commentNode);
        when(nodeService.getProperties(commentNode)).thenReturn(nodeProps);
        when(commentService.getCommentPermissions(any(NodeRef.class), any(NodeRef.class))).thenReturn(map);
        when(people.getPerson(eq("user1"), any(List.class))).thenReturn(person1);
        when(people.getPerson(eq("user2"), any(List.class))).thenReturn(person2);

        Comment resultComment = commentsImpl.createComment(nodeId, comment);

        assertNotNull(resultComment);
        assertNotNull(resultComment.getCreatedBy());
        assertEquals("user1", resultComment.getCreatedBy().getUserName());
        assertEquals("user1@alfresco.com", resultComment.getCreatedBy().getEmail());
        assertNotNull(resultComment.getModifiedBy());
        assertEquals("user2", resultComment.getModifiedBy().getUserName());
        assertEquals("user2@alfresco.com", resultComment.getModifiedBy().getEmail());
    }

    @Test
    public void testCreateCommentForDeletedUser(){
        
        String nodeId = "node-id";
        Comment comment = new Comment();
        NodeRef nodeRef = new NodeRef("protocol", "identifier", "id");
        NodeRef commentNode = new NodeRef("protocol", "identifier", "comment-id");
        Map<String, Boolean> map = new HashMap<>();
        map.put(CommentService.CAN_EDIT, true);
        map.put(CommentService.CAN_DELETE, true);
        
        Map<QName, Serializable> nodeProps = new HashMap<>();
        nodeProps.put(ContentModel.PROP_CREATOR, "user1");
        nodeProps.put(ContentModel.PROP_MODIFIER, "user2");

        when(nodes.validateNode(nodeId)).thenReturn(nodeRef);
        when(typeConstraint.matches(nodeRef)).thenReturn(true);
        when(commentService.createComment(nodeRef, comment.getTitle(), comment.getContent(), false)).thenReturn(commentNode);
        when(nodeService.getProperties(commentNode)).thenReturn(nodeProps);
        when(commentService.getCommentPermissions(any(NodeRef.class), any(NodeRef.class))).thenReturn(map);
        when(people.getPerson(eq("user1"), any(List.class))).thenThrow(EntityNotFoundException.class);
        when(people.getPerson(eq("user2"), any(List.class))).thenThrow(EntityNotFoundException.class);

        Comment resultComment = commentsImpl.createComment(nodeId, comment);
        
        assertNotNull(resultComment);
        assertNotNull(resultComment.getCreatedBy());
        assertEquals("user1", resultComment.getCreatedBy().getUserName());
        assertNull(resultComment.getCreatedBy().getEmail());
        assertNotNull(resultComment.getModifiedBy());
        assertEquals("user2", resultComment.getModifiedBy().getUserName());
        assertNull(resultComment.getCreatedBy().getEmail());
    }
}
