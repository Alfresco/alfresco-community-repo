
dojo.provide("dojo.widget.TreeTimeoutIterator");


dojo.require("dojo.event.*");
dojo.require("dojo.json")
dojo.require("dojo.io.*");
dojo.require("dojo.widget.TreeCommon");


/**
 * Iterates the tree processNext
 * filterFunc/filterObj called to determine if I need to pass the node
 * 
 * callFunc/callObj called to process the node
 *    callObj.callFunc(elem, iterator) should call iterator.forward() to go on
 *    callFunc may change elem to another object (e.g create widget from it),
 *       keeping its parent and parent position are untouched *
 *
 * finishFunc/finishObj called at the end
 *
 * TODO: it should work only sync-way to solve CPU-hungry tasks 
 */
dojo.widget.TreeTimeoutIterator = function(elem, callFunc, callObj) {
	var _this = this;
	
	this.currentParent = elem;
	
	this.callFunc = callFunc;
	this.callObj = callObj ? callObj: this;
	this.stack = [];
		
}


dojo.lang.extend(dojo.widget.TreeTimeoutIterator, {

	// public
	maxStackDepth: Number.POSITIVE_INFINITY,
	
	stack: null,
	currentParent: null,
		
	currentIndex: 0,
	
	filterFunc: function() { return true },
	
	finishFunc: function() { return true },
	
	
	setFilter: function(func, obj) {
		this.filterFunc = func;
		this.filterObj = obj;
	},
	
	
	setMaxLevel: function(level) {
		if (level<1) {
			dojo.raise("setMaxLevel: level="+level+" should be >=1");
		}
		
		this.maxStackDepth = level-2;
	},
	
	forward: function(timeout) {
		var _this = this;
		
		if (this.timeout) {
			// tid will be assigned at the end of outer func execution
			var tid = setTimeout(function() {_this.processNext(); clearTimeout(tid); }, _this.timeout);
		} else {
			return this.processNext();
		}
	},
	
	start: function(processFirst) {
		if (processFirst) {
			return this.callFunc.call(this.callObj, this.currentParent, this);			
		}
		
		return this.processNext();
	},
	
	/**
	 * @private
	 * find next node, move current parent to it if possible & process
	 */
	processNext: function() {
				
		//dojo.debug("processNext with currentParent "+this.currentParent+" index "+this.currentIndex);
		var handler;
		
		var _this = this;
		
		var found;
		
		var next;
			
		
		while (true) {
			var children = this.currentParent.children;
		
			if (children && children.length) {
		
				// look for a node that can be the next target
				do {					
					next = children[this.currentIndex];
					//dojo.debug("check "+next);
				} while (this.currentIndex++ < children.length && !(found = this.filterFunc.call(this.filterObj,next)));
			
			
				if (found) {
					//dojo.debug("found "+next);
					// move to next node as new parent if depth is fine
					// I can't check current children to decide whether to move it or not,
					// because expand may populate children					
					if (next.isFolder && this.stack.length <= this.maxStackDepth) {
						this.moveParent(next,0);
					}
					//dojo.debug("Run callFunc on "+next);
					return this.callFunc.call(this.callObj, next, this);					
				}
			}
				
			if (this.stack.length) {
				this.popParent();
				continue;
			}
			
			break;
		}

		/**
		 * couldn't find next node to process, finish here
		 */
		return this.finishFunc.call(this.finishObj);

	},
	
	setFinish: function(func, obj) {
		this.finishFunc = func;
		this.finishObj = obj;
	},
		
	popParent: function() {
		var p = this.stack.pop();
		//dojo.debug("Pop "+p[0]+":"+p[1]);		
		this.currentParent = p[0];
		this.currentIndex = p[1];
	},
	
	moveParent: function(nextParent, nextIndex) {
		//dojo.debug("Move from "+this.currentParent+":"+this.currentIndex+" to "+nextParent+":"+nextIndex);
		this.stack.push([this.currentParent, this.currentIndex]);
		this.currentParent = nextParent;
		this.currentIndex = nextIndex;
	}
		


});