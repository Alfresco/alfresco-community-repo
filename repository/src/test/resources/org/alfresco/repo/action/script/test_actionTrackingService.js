var sleepActionType = "sleep-action";
var moveActionType = "move";

// Checks the details of one action
function testExecutionDetails()
{
	var definitions = actionTrackingService.getExecutingActions(sleepActionType);
	test.assertEquals(1, definitions.length);
	
	var definition = definitions[0];
	test.assertEquals(sleepActionType, definition.actionType);
	test.assertEquals(1, definition.executionInstance);
	test.assertEquals(NodeRef, definition.persistedActionRef.nodeRef.toString());
	test.assertEquals(false, definition.cancelRequested);
}

// Checks that we can list different actions
function testGetAllExecuting()
{
	var definitions;
	definitions = actionTrackingService.getAllExecutingActions();
	test.assertEquals(2, definitions.length);
	
	// Check for the two, but be aware that
	//  we don't know what order they'll be in
	var foundSleep = false;
	var foundMove = false;
	
	for(var i in definitions)
	{
		var definition = definitions[i];
		if(definition.actionType == sleepActionType)
		{
			foundSleep = true;
		}
		if(definition.actionType == moveActionType)
		{
			foundMove = true;
		}
	}
	
	test.assertEquals(true, foundSleep);
	test.assertEquals(true, foundMove);
}

// Test we can fetch by type
function testGetOfType()
{
	var definitions;
	
	// By name
	definitions = actionTrackingService.getExecutingActions(sleepActionType);
	test.assertEquals(1, definitions.length);
	test.assertEquals(sleepActionType, definitions[0].actionType);
	
	definitions = actionTrackingService.getExecutingActions(moveActionType);
	test.assertEquals(1, definitions.length);
	test.assertEquals(moveActionType, definitions[0].actionType);
	
	definitions = actionTrackingService.getExecutingActions("MADE UP");
	test.assertEquals(0, definitions.length);

	// By action
	definitions = actionTrackingService.getExecutingActions(SleepAction);
	test.assertEquals(1, definitions.length);
	test.assertEquals(sleepActionType, definitions[0].actionType);
}

// Test the we can request the cancellation
function testCancel()
{
	// Check
	var definitions = actionTrackingService.getExecutingActions(sleepActionType);
	test.assertEquals(1, definitions.length);
	
	var definition = definitions[0];
	test.assertEquals(false, definition.cancelRequested);
	
	// Cancel
	actionTrackingService.requestActionCancellation(definition);
	
	// Ensure it worked
	definitions = actionTrackingService.getExecutingActions(sleepActionType);
	test.assertEquals(1, definitions.length);
	
	definition = definitions[0];
	test.assertEquals(true, definition.cancelRequested);
}

// Execute Tests
testExecutionDetails();
testGetAllExecuting();
testGetOfType();
testCancel();