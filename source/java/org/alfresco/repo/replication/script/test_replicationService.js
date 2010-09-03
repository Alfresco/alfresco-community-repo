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
	test.assertEquals(PersistedTarget, Persisted.targetName);
	test.assertEquals(2, Persisted.payload.length);
	
	test.assertEquals("workspace://SpacesStore/Testing", Persisted.payload[0].nodeRef.toString())
	test.assertEquals("workspace://SpacesStore/Testing2", Persisted.payload[1].nodeRef.toString())
}

// Test listing
function testListing()
{
	// All
	var definitions = replicationService.loadReplicationDefinitions();
	test.assertEquals(2, definitions.length);
	
	var foundP1 = false;
	var foundP2 = false;
	for(var i in definitions)
	{
		var definition = definitions[i];
		if(definition.replicationName == PersistedName)
		{
			foundP1 = true;
			test.assertEquals(PersistedName, definition.replicationName);
			test.assertEquals("Persisted", definition.description);
			test.assertEquals(PersistedTarget, definition.targetName);
			test.assertEquals(2, definition.payload.length);
			
			test.assertEquals("workspace://SpacesStore/Testing", definition.payload[0].nodeRef.toString())
			test.assertEquals("workspace://SpacesStore/Testing2", definition.payload[1].nodeRef.toString())
		}
		if(definition.replicationName == Persisted2Name)
		{
			foundP2 = true;
			test.assertEquals(Persisted2Name, definition.replicationName);
			test.assertEquals("Persisted2", definition.description);
			test.assertEquals(Persisted2Target, definition.targetName);
			test.assertEquals(0, definition.payload.length);
		}
	}
	
	// By target - for Persisted
	definitions = replicationService.loadReplicationDefinitions(PersistedTarget);
	test.assertEquals(1, definitions.length);
	test.assertEquals(PersistedName, definitions[0].replicationName);
	
	// By target - for Persisted2
	definitions = replicationService.loadReplicationDefinitions(Persisted2Target);
	test.assertEquals(1, definitions.length);
	test.assertEquals(Persisted2Name, definitions[0].replicationName);
	
	// By target - invalid target
	definitions = replicationService.loadReplicationDefinitions("MadeUpDoesntExit");
	test.assertEquals(0, definitions.length);
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
	
	// Won't be there if loaded
	test.assertEquals(null, replicationService.loadReplicationDefinition("JS"));
	
	// Save it
	replicationService.saveReplicationDefinition(definition);
	
	// Load and re-check
	definition = replicationService.loadReplicationDefinition("JS");
	test.assertNotNull(definition);
	test.assertEquals("JS", definition.replicationName);
	test.assertEquals("From JS", definition.description);
	test.assertEquals("TargetTarget", definition.targetName);
	test.assertEquals(2, definition.payload.length);
	
	test.assertEquals("workspace://SpacesStore/Testing", definition.payload[0].nodeRef.toString())
	test.assertEquals("workspace://SpacesStore/Testing2", definition.payload[1].nodeRef.toString())
}

// Tests running (without a full definition, so should quickly fail)
function testRunReplication()
{
	var definition = replicationService.loadReplicationDefinition(Persisted2Name);
	test.assertNotNull(definition);
		
	// Should give an error about no payload
	try {
		replicationService.replicate(definition);
		test.fail("Shouldn't be able to run a definition lacking a payload");
	} catch(err) {
		var msg = err.message;
		test.assertTrue(msg.indexOf("payload") > -1, "Payload error not found in " + msg);
	}
}

// Execute Tests
testReplicationDefinition();
testListing();
testCreateSave();
testRunReplication();