
dojo.provide("dojo.widget.TreeRpcControllerV3");



dojo.require("dojo.event.*");
dojo.require("dojo.json")
dojo.require("dojo.io.*");
dojo.require("dojo.widget.TreeLoadingControllerV3");

dojo.widget.tags.addParseTreeHandler("dojo:TreeRpcControllerV3");

dojo.widget.TreeRpcControllerV3 = function(){
	dojo.widget.TreeLoadingControllerV3.call(this);
}

dojo.inherits(dojo.widget.TreeRpcControllerV3, dojo.widget.TreeLoadingControllerV3);


// TODO: do something with addChild / setChild, so that RpcController become able
// to hook on this and report to server
dojo.lang.extend(dojo.widget.TreeRpcControllerV3, {
	widgetType: "TreeRpcControllerV3",

	extraRpcOnEdit: true,
				
	/**
	 * Make request to server about moving children.
	 *
	 * Request returns "true" if move succeeded,
	 * object with error field if failed
	 *
	 * I can't leave DragObject floating until async request returns, need to return false/true
	 * so making it sync way...
	 *
	 * Also, "loading" icon is not shown until function finishes execution, so no indication for remote request.
	*/
	doMove: function(child, newParent, index, sync){

		//if (newParent.isTreeNode) newParent.markLoading();

		
		var params = {
			// where from
			child: this.getInfo(child),
			childTree: this.getInfo(child.tree),
			// where to
			newParent: this.getInfo(newParent),
			newParentTree: this.getInfo(newParent.tree),
			newIndex: index
		};


		var deferred = this.runRpc({		
			url: this.getRpcUrl('move'),
			sync: sync,			
			params: params
		});

		var _this = this;
		var args = arguments;	
		
		//deferred.addCallback(function(res) { dojo.debug("doMove fired "+res); return res});
		
		deferred.addCallback(function() {			
			dojo.widget.TreeBasicControllerV3.prototype.doMove.apply(_this,args);
		});

		
		return deferred;
	}
});


// -------------- detach
dojo.lang.extend(dojo.widget.TreeRpcControllerV3, {
	
	prepareDetach: function(node, sync) {
		var deferred = this.startProcessing(node);		
		return deferred;
	},
	
	finalizeDetach: function(node) {
		this.finishProcessing(node);
	},
		

	doDetach: function(node, sync){

		
		var params = {
			node: this.getInfo(node),
			tree: this.getInfo(node.tree)
		}

		var deferred = this.runRpc({
			url: this.getRpcUrl('detach'),
			sync: sync,
			params: params			
		});
		
		
		var _this = this;
		var args = arguments;
		
		deferred.addCallback(function() {			
			dojo.widget.TreeBasicControllerV3.prototype.doDetach.apply(_this,args);
		});
		
						
		return deferred;

	}
});

	
// -------------------------- Inline edit node ---------------------	
dojo.lang.extend(dojo.widget.TreeRpcControllerV3, {
	/**
	 * send edit start request if needed
	 * useful for server-side locking 
	 */
	requestEditConfirmation: function(node, action, sync) {
		if (!this.extraRpcOnEdit) {			
			return dojo.Deferred.prototype.makeCalled();
		}
	
		//dojo.debug("requestEditConfirmation "+node+" "+action);
		
		var _this = this;
	
		var deferred = this.startProcessing(node);
			
		//dojo.debug("startProcessing "+node);
		
		var params = {
			node: this.getInfo(node),
			tree: this.getInfo(node.tree)
		}
		
		deferred.addCallback(function() {
			//dojo.debug("add action on requestEditConfirmation "+action);
			return _this.runRpc({
				url: _this.getRpcUrl(action),
				sync: sync,
				params: params			
			});
		});
		
		
		deferred.addBoth(function(r) {
			//dojo.debug("finish rpc with "+r);
			_this.finishProcessing(node);
			return r;
		});
	
		return deferred;
	},
	
	editLabelSave: function(node, newContent, sync) {
		var deferred = this.startProcessing(node);
						
		var _this = this;
		
		var params = {
			node: this.getInfo(node),
			tree: this.getInfo(node.tree),
			newContent: newContent
		}
		
	
		deferred.addCallback(function() {
			return _this.runRpc({
				url: _this.getRpcUrl('editLabelSave'),
				sync: sync,
				params: params			
			});
		});
		
		
		deferred.addBoth(function(r) {
			_this.finishProcessing(node);
			return r;
		});
	
		return deferred;
	},
	
	
	editLabelStart: function(node, sync) {		
		if (!this.canEditLabel(node)) {
			return false;
		}
		
		if (!this.editor.isClosed()) {
			//dojo.debug("editLabelStart editor open");
			var deferred = this.editLabelFinish(this.editor.saveOnBlur, sync);
			deferred.addCallback(function() {
				return _this.editLabelStart(node, sync);
			});
			return deferred;
		}
						
		var _this = this;
		//dojo.debug("editLabelStart closed, request");
		var deferred = this.requestEditConfirmation(node, 'editLabelStart', sync);
		
		deferred.addCallback(function() {
			//dojo.debug("start edit");
			_this.doEditLabelStart(node);
		});
	
		
		return deferred;
	
	},
	
	
	editLabelFinish: function(save, sync) {
		var _this = this;
		
		var node = this.editor.node;
		
		var deferred = dojo.Deferred.prototype.makeCalled();
		
		if (!save && !node.isPhantom) {
			deferred = this.requestEditConfirmation(this.editor.node,'editLabelFinishCancel', sync);
		}
		
		if (save) {					
			deferred = this.editLabelSave(this.editor.node, this.editor.getContents(), sync);
		}
		
		deferred.addCallback(function() {
			_this.doEditLabelFinish(save);
		});
		
		deferred.addErrback(function(r) {
			_this.doEditLabelFinish(false);
			return false;
		});
		
		return deferred;
	},
	
	createAndEdit: function(parent, index, sync) {
		var data = {title:parent.tree.defaultChildTitle};
		
		if (!this.canCreateChild(parent, index, data)) {
			return false;
		}
		
		/* close editor first */
		if (!this.editor.isClosed()) {
			//dojo.debug("editLabelStart editor open");
			var deferred = this.editLabelFinish(this.editor.saveOnBlur, sync);
			deferred.addCallback(function() {
				return _this.createAndEdit(parent, index, sync);
			});
			return deferred;
		}
			
		var _this = this;
		
		/* load parent and create child*/
		var deferred = this.prepareCreateChild(parent, index, data, sync);
		
		
		deferred.addCallback(function() {
			var child = dojo.widget.TreeBasicControllerV3.prototype.doCreateChild.call(_this,parent,index,data);			
			child.isPhantom = true;
			return child;
		});
		
		
		deferred.addBoth(function(r) {
			_this.finalizeCreateChild(parent, index, data, sync);
			return r;
		});
		
		/* expand parent */
		deferred.addCallback(function(child) {
			var d = _this.exposeCreateChild(parent, index, data, sync);
			d.addCallback(function() { return child });
			return d;
		});
		
		
		deferred.addCallback(function(child) {
			//dojo.debug("start edit");
			_this.doEditLabelStart(child);
			return child;
		});
		
		
		
		return deferred;
	
	}
	
	

});

dojo.lang.extend(dojo.widget.TreeRpcControllerV3, {

	prepareDestroy: function(node, sync) {
		//dojo.debug(node);
		var deferred = this.startProcessing(node);		
		return deferred;
	},
	
	finalizeDestroy: function(node) {
		this.finishProcessing(node);
	},
		

	doDestroy: function(node, sync){

		
		var params = {
			node: this.getInfo(node),
			tree: this.getInfo(node.tree)
		}

		var deferred = this.runRpc({
			url: this.getRpcUrl('destroy'),
			sync: sync,
			params: params			
		});
		
		
		var _this = this;
		var args = arguments;
		
		deferred.addCallback(function() {			
			dojo.widget.TreeBasicControllerV3.prototype.doDestroy.apply(_this,args);
		});
		
						
		return deferred;

	}
});
	

dojo.lang.extend(dojo.widget.TreeRpcControllerV3, {

	// -----------------------------------------------------------------------------
	//                             Create node stuff
	// -----------------------------------------------------------------------------


	doCreateChild: function(parent, index, data, sync){		
			
		var params = {
			tree: this.getInfo(parent.tree),
			parent: this.getInfo(parent),
			index: index,
			data: data
		}

		var deferred = this.runRpc({
			url: this.getRpcUrl('createChild'),
			sync: sync,
			params: params
		});
		
		var _this = this;
		var args = arguments;
		
		
		deferred.addCallback(function() {			
			return dojo.widget.TreeBasicControllerV3.prototype.doCreateChild.apply(_this,args);
		});
		
						
		return deferred;
	},
	
	
	doClone: function(child, newParent, index, deep, sync) {
		
		var params = {
			child: this.getInfo(child),
			newParent: this.getInfo(newParent),
			index: index,
			deep: deep ? true : false, // undefined -> false
			tree: this.getInfo(child.tree)
		}
		
		
		var deferred = this.runRpc({
			url: this.getRpcUrl('clone'),
			sync: sync,
			params: params
		});
		
		var _this = this;
		var args = arguments;
		
		deferred.addCallback(function() {			
			dojo.widget.TreeBasicControllerV3.prototype.doClone.apply(_this,args);
		});
		
						
		return deferred;	
	}
	
	
});
