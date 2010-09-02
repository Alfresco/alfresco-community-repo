// Test that we can work with the definition properly
function testReplicationDefinition()
{
	// Check the empty one
	test.assertEquals(EmptyName, Empty.replicationName);
	test.assertEquals("Empty", Empty.description);
	test.assertEquals(null, Empty.targetName);
	test.assertEquals(0, Empty.payload.length);
	
	// Check the persisted one
	test.assertEquals(PersistedName, Persisted.replicationName);
	test.assertEquals("Persisted", Persisted.description);
	test.assertEquals("TestTransferTarget", Persisted.targetName);
	test.assertEquals(2, Persisted.payload.length);
	
	test.assertEquals("workspace://SpacesStore/Testing", Persisted.payload[0].nodeRef.toString())
	test.assertEquals("workspace://SpacesStore/Testing2", Persisted.payload[1].nodeRef.toString())
}

// Test listing
function testListing()
{
	// TODO
}

// Test creating and saving
function testCreateSave()
{
	// Create
	var definition = replicationService.createReplicationDefinition("JS","From JS");
	test.assertEquals("JS", definition.replicationName);
	test.assertEquals("From JS", definition.description);
	test.assertEquals(null, definition.targetName);
	test.assertEquals(0, definition.payload.length);
	
	// Set some bits
	definition.targetName = "TargetTarget";
	nodes = [
        Persisted.payload[0], Persisted.payload[1] 
	]
	definition.payload = nodes
	
	// Save
	
	// Load and re-check
	definition = replicationService.loadReplicationDefinition("JS");
	test.assertEquals("JS", definition.replicationName);
	test.assertEquals("From JS", definition.description);
	test.assertEquals("TargetTarget", definition.targetName);
	test.assertEquals(2, definition.payload.length);
	
	test.assertEquals("workspace://SpacesStore/Testing", definition.payload[0].nodeRef.toString())
	test.assertEquals("workspace://SpacesStore/Testing2", definition.payload[1].nodeRef.toString())
}

// Tests running (without a full definition)
function testRunReplication()
{
	// TODO
}

// Execute Tests
testReplicationDefinition();
testListing();
testCreateSave();
testRunReplication();