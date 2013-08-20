package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Tree;

public class FolderNode extends CMISNode implements Serializable
{
	private static final long serialVersionUID = -7069586854942264572L;

	private Map<String, FolderNode> folderNodes = new HashMap<String, FolderNode>();
	private Map<String, CMISNode> documentNodes = new HashMap<String, CMISNode>();

	private Random random = new Random();

	public FolderNode(String nodeId, String guid, Map<String, Serializable> properties)
	{
		super(nodeId, guid, properties);
	}

	public boolean isFolder()
	{
		return true;
	}
	
	public void addFolder(FolderNode folder)
	{
		folderNodes.put(getBareObjectId(folder.getNodeId()), folder);
	}
	
	public void addNode(CMISNode node)
	{
		documentNodes.put(getBareObjectId(node.getNodeId()), node);
	}
	
	public Map<String, FolderNode> getFolderNodes()
	{
		return folderNodes;
	}

	public Map<String, CMISNode> getDocumentNodes()
	{
		return documentNodes;
	}

	private FolderNode getFolderNode(String objectId)
	{
		FolderNode n = folderNodes.get(getBareObjectId(objectId));
		return n;
	}

	private CMISNode getDocumentNode(String objectId)
	{
		CMISNode n = documentNodes.get(getBareObjectId(objectId));
		return n;
	}
	
	public CMISNode selectRandomFolderNode()
	{
		int idx = random.nextInt(folderNodes.size());
		return folderNodes.get(idx);
	}
	
	public CMISNode selectRandomDocumentNode()
	{
		int idx = random.nextInt(documentNodes.size());
		return documentNodes.get(idx);
	}

	@Override
	public String toString()
	{
		return "FolderNode [folderNodes=" + folderNodes + ", documentNodes="
				+ documentNodes + ", nodeId=" + nodeId + ", properties="
				+ properties + "]";
	}
	
	private String getBareObjectId(String objectId)
	{
		int idx = objectId.indexOf(";");
		String bareObjectId = null;
		if(idx == -1)
		{
			bareObjectId = objectId;
		}
		else
		{
			bareObjectId = objectId.substring(0, idx);
		}
		return bareObjectId;
	}

	private Tree<FileableCmisObject> findTree(String nodeId, List<Tree<FileableCmisObject>> nodes)
	{
		Tree<FileableCmisObject> ret = null;

		for(Tree<FileableCmisObject> tree : nodes)
		{
			FileableCmisObject item = tree.getItem();
			// compare only the "bare" object id i.e. without the version suffix
			if(getBareObjectId(nodeId).equals(getBareObjectId(item.getId())))
			{
				ret = tree;
				break;
			}
		}

		return ret;
	}

	private void checkChildrenImpl(Map<String, FolderNode> expectedFolderNodes, Map<String, CMISNode> expectedDocumentNodes)
	{
		for(FolderNode expectedFolderNode : expectedFolderNodes.values())
		{
			FolderNode actualFolderNode = getFolderNode(expectedFolderNode.getNodeId());
			assertNotNull("Unable to find node " + expectedFolderNode, actualFolderNode);

			// check a few basic properties
			Map<String, Serializable> expectedProperties = expectedFolderNode.getProperties();
			Map<String, Serializable> actualProperties = actualFolderNode.getProperties();
			AssertUtil.assertEquals("cmis:objectTypeId", expectedProperties.get("cmis:objectTypeId"), actualProperties.get("cmis:objectTypeId"));
			AssertUtil.assertEquals("cmis:path", expectedProperties.get("cmis:path"), actualProperties.get("cmis:path"));
			AssertUtil.assertEquals("cmis:name", expectedProperties.get("cmis:name"), actualProperties.get("cmis:name"));
		}

		for(CMISNode expectedDocumentNode : expectedDocumentNodes.values())
		{
			CMISNode actualDocumentNode = getDocumentNode(expectedDocumentNode.getNodeId());
			assertNotNull("Unable to find node " + expectedDocumentNode.getNodeId(), actualDocumentNode);

			// check a few basic properties
			Map<String, Serializable> expectedProperties = expectedDocumentNode.getProperties();
			Map<String, Serializable> actualProperties = actualDocumentNode.getProperties();
			AssertUtil.assertEquals("cmis:objectTypeId", expectedProperties.get("cmis:objectTypeId"), actualProperties.get("cmis:objectTypeId"));
			AssertUtil.assertEquals("cmis:path", expectedProperties.get("cmis:path"), actualProperties.get("cmis:path"));
			AssertUtil.assertEquals("cmis:name", expectedProperties.get("cmis:name"), actualProperties.get("cmis:name"));
		}
	}

	public void checkChildren(ItemIterable<CmisObject> expectedChildren)
	{
		Map<String, FolderNode> expectedFolderNodes = new HashMap<String, FolderNode>();
		Map<String, CMISNode> expectedDocumentNodes = new HashMap<String, CMISNode>();
		for(CmisObject child : expectedChildren)
		{
			CMISNode dn = CMISNode.createNode(child);
			if(dn instanceof FolderNode)
			{
				expectedFolderNodes.put(getBareObjectId(dn.getNodeId()), (FolderNode)dn);
			}
			else
			{
				expectedDocumentNodes.put(getBareObjectId(dn.getNodeId()), dn);
			}
		}
		checkChildrenImpl(expectedFolderNodes, expectedDocumentNodes);
	}
	
	public void checkChildren(List<String> expectedChildFolderIds, List<String> expectedChildDocumentIds)
	{
		Map<String, FolderNode> expectedFolderNodes = new HashMap<String, FolderNode>();
		for(String childObjectId : expectedChildFolderIds)
		{
			CMISNode n = CMISNode.createNode(getBareObjectId(childObjectId));
			if(n instanceof FolderNode)
			{
				FolderNode node = (FolderNode)n;
				expectedFolderNodes.put(getBareObjectId(node.getNodeId()), node);
			}
		}
		Map<String, CMISNode> expectedDocumentNodes = new HashMap<String, CMISNode>();
		for(String childObjectId : expectedChildDocumentIds)
		{
			CMISNode node = CMISNode.createNode(getBareObjectId(childObjectId));
			expectedDocumentNodes.put(getBareObjectId(node.getNodeId()), node);
		}

		checkChildrenImpl(expectedFolderNodes, expectedDocumentNodes);
	}

	public void checkDescendants(List<Tree<FileableCmisObject>> descendants)
	{
		for(FolderNode expectedFolderNode : folderNodes.values())
		{
			Tree<FileableCmisObject> item = findTree(expectedFolderNode.getNodeId(), descendants);
			assertNotNull(item);
			FolderNode actualFolderNode = (FolderNode)CMISNode.createNode(item.getItem());
			
			// check a few basic properties
			Map<String, Serializable> expectedProperties = expectedFolderNode.getProperties();
			Map<String, Serializable> actualProperties = actualFolderNode.getProperties();
			assertEquals(expectedProperties.get("cmis:objectTypeId"), actualProperties.get("cmis:objectTypeId"));
			assertEquals(expectedProperties.get("cmis:path"), actualProperties.get("cmis:path"));
			assertEquals(expectedProperties.get("cmis:name"), actualProperties.get("cmis:name"));

			expectedFolderNode.checkDescendants(item.getChildren());
		}

		for(CMISNode expectedDocumentNode : documentNodes.values())
		{
			Tree<FileableCmisObject> item = findTree(expectedDocumentNode.getNodeId(), descendants);
			assertNotNull(item);
			CMISNode actualNode = CMISNode.createNode(item.getItem());

			// check a few basic properties
			Map<String, Serializable> expectedProperties = expectedDocumentNode.getProperties();
			Map<String, Serializable> actualProperties = actualNode.getProperties();
			assertEquals(expectedProperties.get("cmis:objectTypeId"), actualProperties.get("cmis:objectTypeId"));
			assertEquals(expectedProperties.get("cmis:path"), actualProperties.get("cmis:path"));
			assertEquals(expectedProperties.get("cmis:name"), actualProperties.get("cmis:name"));
		}
	}
}
