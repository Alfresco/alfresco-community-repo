function applyTemplate(ctx) {
	return {
		name : 'template1_name',
		nodes : [ {
			name : 'My Documents'
		}, {
			name : 'Recent Documents'
		}, {
			name : 'Other Documents'
		} ]
	};
}

applyTemplate(context);