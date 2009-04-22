/** MODEL FACTORY **/
var modelFactory = function() { };
modelFactory.prototype = {
	getIteration: function(iterationId, callback) {
		if(!iterationId || iterationId < 1) {
			throw "Invalid iteration id";
		}
		if(typeof(callback) != "function") {
			throw "Invalid call back";
		}
		jQuery.ajax({
			async: true,
			error: function() {
				//throw "Data request failed!";
			},
			success: function(data,type) {
				var iteration = new iterationModel(data, iterationId);
				callback(iteration);
			},
			cache: false,
			dataType: "json",
			type: "POST",
			url: "iterationData.action",
			data: {iterationId: iterationId}
		});
	}
};

ModelFactory = new modelFactory();

/** ITERATION MODEL **/

iterationModel = function(iterationData, iterationId) {
	var goalPointer = [];
	this.iterationId = iterationId;
	var me = this;
	jQuery.each(iterationData.iterationGoals, function(index,iterationGoalData) { 
		goalPointer.push(new iterationGoalModel(iterationGoalData, me));
	});
	this.iterationGoals = goalPointer;
};
iterationModel.prototype = {
	getIterationGoals: function() {
		return this.iterationGoals;
	},
	reloadGoalData: function() {
	  var me = this;
	   jQuery.ajax({
	      async: false,
	      success: function(data,type) {
	        data = data.iterationGoals;
	        for(var i = 0 ; i < data.length; i++) {
	          for(var j = 0; j < me.iterationGoals.length; j++) {
	            if(data[i].id == me.iterationGoals[j].id) {
	              me.iterationGoals[i].setData(data[i]);
	            }
	          }
	        }
	      },
	      cache: false,
	      dataType: "json",
	      type: "POST",
	      url: "iterationData.action",
	      data: {iterationId: this.iterationId, excludeBacklogItems: true}
	    });
	},
	addGoal: function(goal) {
	  goal.iteration = this;
	  this.iterationGoals.push(goal);
	},
	removeGoal: function(goal) {
	  var goals = [];
	  for(var i = 0 ; i < this.iterationGoals.length; i++) {
	    if(this.iterationGoals[i] != goal) {
	      goals.push(this.iterationGoals[i]);
	    }
	  }
	  var me = this;
	  goal.remove(function() {
	    me.iterationGoals = goals;
	  });
	}
};

/** ITERATION GOAL MODEL **/

iterationGoalModel = function(iterationGoalData, parent) {
  this.metrics = {};
	this.setData(iterationGoalData, true);
	this.iteration = parent;
	this.editListeners = [];
	this.deleteListeners = [];
};
iterationGoalModel.prototype = {
	setData: function(data, includeMetrics) {
    this.persistedData = data;
    this.description = data.description;
    this.name = data.name;
    this.priority = data.priority;
    this.id = data.id;
    if(includeMetrics) {
      this.metrics = data.metrics;
    }
    if(data.backlogItems && data.backlogItems.length > 0) {
      this.setBacklogItems(data.backlogItems);
    }
    if(this.editListeners) {
      for(var i = 0; i < this.editListeners.length; i++) {
        this.editListeners[i]();
      }
    }
  },
  setBacklogItems: function(backlogItems) {
    if(!this.backlogItems || this.backlogItems.length == 0) {
      this.backlogItems = [];
      for(var i = 0 ; i < backlogItems.length ; i++) {
        this.backlogItems.push(new backlogItemModel(backlogItems[i], this));
      }
    }
  },
  copy: function() {
    var copy = new iterationGoalModel({}, this.iteration);
    copy.setData(this, true);
    if(!copy.metrics) copy.metrics = {};
    return copy;
  },
  getBacklogItems: function() {
    return this.backlogItems;
  },
  getId: function() {
		return this.id;
	},
	getName: function() {
		return this.name;
	},
	setName: function(name) {
		this.name = name;
		this.save();
	},
	getDescription: function() {
		return this.description;
	},
	setDescription: function(description) {
		this.description = description;
		this.save();
	},
	getPriority: function() {
		return this.priority;
	},
	setPriority: function(priority) {
		this.priority = priority;
		this.save();
	},
	getEffortLeft: function() {
		return this.metrics.effortLeft;
	},
	getEffortSpent: function() {
		return this.metrics.effortSpent;
	},
	getOriginalEstimate: function() {
		return this.metrics.originalEstimate;
	},
	getDoneTasks: function() {
		return this.metrics.doneTasks;
	},
	getTotalTasks: function() {
		return this.metrics.totalTasks;
	},
	beginTransaction: function() {
	  this.inTransaction = true;
	},
	commit: function() {
	  this.inTransaction = false;
	  this.save();
	},
	rollBack: function() {
	  this.setData(this.persistedData);
	  this.inTransaction = false;
	},
	addEditListener: function(listener) {
	  this.editListeners.push(listener);
	},
	addDeleteListener: function(listener) {
	  this.deleteListeners.push(listener);
	},
	remove: function(cb) {
	  var me = this;
	  jQuery.ajax({
      async: true,
      error: function() {
	      me.rollBack();
	      commonView.showError("An error occured while deleting an iteration goal.");
      },
      success: function(data,type) {
        cb();
        for(var i = 0 ; i < me.deleteListeners.length; i++) {
          me.deleteListeners[i]();
        }
        commonView.showOk("Iteration goal deleted.");
      },
      cache: false,
      type: "POST",
      url: "deleteIterationGoal.action",
      data: {iterationGoalId: this.id}
    });
	},
	save: function() {
	  if(this.inTransaction) {
	    return;
	  }
	  var me = this;
		var data  = {
				"iterationGoal.name": this.name,
				"iterationGoal.description": this.description,
				iterationId: this.iteration.iterationId
		};
		if(this.priority) data.priority = this.priority;
		if(this.id) data.iterationGoalId = this.id;
		if(this.name == undefined) data.name = "";
		if(this.description == undefined) data.description = "";
    jQuery.ajax({
      async: false,
      error: function() {
        commonView.showError("An error occured while saving an iteration goal.");
      },
      success: function(data,type) {
        me.setData(data,false);
        commonView.showOk("Iteration goal saved succesfully.");
      },
      cache: false,
      dataType: "json",
      type: "POST",
      url: "storeIterationGoal.action",
      data: data
    });
	}
};

var backlogItemModel = function(data, iterationGoal) {
  this.editListeners = [];
  this.deleteListener = [];
  this.setData(data);
};
backlogItemModel.prototype = {
  setData: function(data) {
    this.persistedData = data;
    this.name = data.name;
    this.description = data.description;
    this.created = data.created;
    this.priority = data.priority;
    this.state = data.state;
    
  },
  getName: function() {
    return this.name;
  },
  setName: function(name) {
    this.name = name;
    this.save();
  },
  getDescription: function() {
    return this.description;
  },
  setDescription: function(description) {
    this.description = description;
    this.save();
  },
  getCreated: function() {
    return this.created;
  },
  getPriority: function() {
    return this.priority;
  },
  setPriority: function(priority) {
    this.priority = priority;
    this.save();
  },
  getState: function() {
    return this.state;
  },
  setState: function(state) {
    this.state = state;
    this.save();
  },
  addEditListener: function(listener) {
    this.editListeners.push(listener);
  },
  addDeleteListener: function(listener) {
    this.deleteListeners.push(listener);
  },
  beginTransaction: function() {
    this.inTransaction = true;
  },
  commit: function() {
    this.inTransaction = false;
    this.save();
  },
  rollBack: function() {
    this.setData(this.persisted);
    this.inTransaction = false;
  },
  remove: function() {
    
  },
  save: function() {
    if(this.inTransaction) {
      return;
    }
  }
};