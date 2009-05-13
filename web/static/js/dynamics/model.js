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
	this.itemsWithoutGoal = [];
	var me = this;
	jQuery.each(iterationData.iterationGoals, function(index,iterationGoalData) { 
		goalPointer.push(new iterationGoalModel(iterationGoalData, me));
	});
	if(iterationData.itemsWithoutGoal) {
		this.containerGoal = new iterationGoalModel({id: "", priority: 9999999}, this);
		this.containerGoal.save = function() {};
		this.containerGoal.remove = function() {};
		this.containerGoal.backlogItems = this.itemsWithoutGoal;
		this.containerGoal.metrics = {};
		this.containerGoal.reloadMetrics();
		jQuery.each(iterationData.itemsWithoutGoal, function(k,v) { 
			me.itemsWithoutGoal.push(new backlogItemModel(v,me,me.containerGoal));
		});
	}
	this.iterationGoals = goalPointer;
};
iterationModel.prototype = {
	getIterationGoals: function() {
		return this.iterationGoals;
	},
	getId: function() {
	  return this.iterationId;
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
	}, 
	getBacklogItems: function() { //blis without an iteration goal
	  return this.itemsWithoutGoal;
	},
	getPseudoGoal: function() {
		return this.containerGoal;
	}
};

/** ITERATION GOAL MODEL **/

iterationGoalModel = function(iterationGoalData, parent) {
  this.metrics = {};
  this.iteration = parent;
  this.backlogItems = [];
  this.setData(iterationGoalData, true);
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
        this.editListeners[i]({bubbleEvent: []});
      }
    }
  },
  setBacklogItems: function(backlogItems) {
    if(!this.backlogItems || this.backlogItems.length == 0) {
      this.backlogItems = [];
      for(var i = 0 ; i < backlogItems.length ; i++) {
        this.backlogItems.push(new backlogItemModel(backlogItems[i], this.iteration, this));
      }
    }
  },
  addBacklogItem: function(bli) {
	bli.backlog = this.iteration;
	bli.iterationGoal = this;
    this.backlogItems.push(bli);
    this.reloadMetrics();
  },
  removeBacklogItem: function(bli) {
    var tmp = this.backlogItems;
    this.backlogItems = [];
    for(var i = 0; i < tmp.length; i++) {
      if(tmp[i] != bli) {
    	  this.backlogItems.push(tmp[i]);
      }
    }
    this.reloadMetrics();
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
	reloadMetrics: function() {
		var me = this;
		jQuery.ajax({
			url: "calculateIterationGoalMetrics.action",
			data: {
				iterationGoalId: this.id,
				iterationId: this.iteration.iterationId
		    },
		    cache: false,
		    type: "POST",
		    dataType: "json",
		    success: function(data,type) {
		    	var nData = me.persistedData;
		    	nData.metrics = data;
		    	me.setData(nData,true);
		    }
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
		if(this.name == undefined) data["iterationGoal.name"] = "";
		if(this.description == undefined) data["iterationGoal.description"] = "";
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

var backlogItemModel = function(data, backlog, iterationGoal) {
  this.effortLeft = "";
  this.originalEstimate = "";
  this.editListeners = [];
  this.deleteListeners = [];
  this.backlog = backlog;
  this.iterationGoal = iterationGoal;
  if(data) this.setData(data);
};
backlogItemModel.prototype = {
  setData: function(data) {
    this.id = data.id;
    this.name = data.name;
    this.description = data.description;
    this.created = data.created;
    this.priority = data.priority;
    this.state = data.state;
    this.effortLeft = data.effortLeft;
    this.effortSpent = data.effortSpent;
    this.originalEstimate = data.originalEstimate;
    var bubbleEvents = [];
    if(this.persistedData) {
    	if(this.persistedData.effortLeft != this.effortLeft || this.persistedData.originalEstimate != this.originalEstimate || this.persistedData.state != data.state) {
    		bubbleEvents.push("metricsUpdated");
    	}
    } else if(!this.persistedData && data) {
    	bubbleEvents.push("metricsUpdated");
    }
    if(data.userData) {
    	this.users = data.userData;
    }
    if(data.businessThemes) {
    	this.themes = [];
    	for(var i = 0 ; i < data.businessThemes.length; i++) {
    		if(data.businessThemes[i] != null) {
    			this.themes.push(data.businessThemes[i]);
    		}
    	}
    }
    if(data.hourEntries) {
      this.hourEntries = [];
      for(var i = 0 ; i < data.hourEntries.length; i++) {
        if(data.hourEntries[i] != null) {
          this.hourEntries.push(new backlogItemHourEntryModel(this, data.hourEntries[i]));
        }
      }
    }
    if(data.tasks) {
      this.todos = [];
      for(var i = 0 ; i < data.tasks.length; i++) {
        if(data.tasks[i] != null) {
          this.todos.push(new todoModel(this,data.tasks[i]));
        }
      }
    }
    this.persistedData = data;
    for (var i = 0; i < this.editListeners.length; i++) {
      this.editListeners[i]({bubbleEvent: bubbleEvents});
    }
  },
  loadTodoAndSpentEffortData: function() {
    var me = this;
    $.ajax({
      url: "backlogItemJSON.action",
      data: {backlogItemId: this.id},
      async: false,
      cache: false,
      dataType: 'json',
      type: 'POST',
      success: function(data,type) {
        me.setData(data);
      }
    });
  },
  getHourEntries: function() {
    if(this.hourEntries == null) {
      this.loadTodoAndSpentEffortData();
    }
    return this.hourEntries;
  },
  reloadHourEntries: function() {
    this.loadTodoAndSpentEffortData();
  },
  reloadTodos: function() {
    this.loadTodoAndSpentEffortData();
  },
  getTodos: function() {
    if(this.todos == null) {
      this.loadTodoAndSpentEffortData();
    }
    return this.todos;
  },
  getThemes: function() {
	return this.themes;  
  },
  setThemes: function(themes) {
	this.themes = themes;  
  },
  getUsers: function() {
	  return this.users;
  },
  setUsers: function(users) {
	this.users = users;  
  },
  setUserIds: function(userIds) {
	this.userIds = userIds;
    this.save();
  },
  setThemeIds: function(themeIds) {
	this.themeIds = themeIds;
	this.save();
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
  getEffortLeft: function() {
    return this.effortLeft;
  },
  setEffortLeft: function(effortLeft) {
	var millis = agilefantUtils.aftimeToMillis(effortLeft);
	if(millis !== null) {
		this.effortLeft = millis;
    	this.save();
	}
  },
  getEffortSpent: function() {
    return this.effortSpent;
  },
  setEffortSpent: function(effortSpent) {
    this.effortSpent = effortSpent;
    this.save();
  },
  getOriginalEstimate: function() {
    return this.originalEstimate;
  },
  setOriginalEstimate: function(originalEstimate) {
	var millis = agilefantUtils.aftimeToMillis(originalEstimate);
	if(millis !== null) {  
      this.originalEstimate = millis;
      this.save();
	}
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
    this.setData(this.persistedData);
    this.userIds = null;
    this.themeIds = null;
    this.inTransaction = false;
  },
  remove: function() {
	  var me = this;
	  jQuery.ajax({
      async: true,
      error: function() {
	      me.rollBack();
	      commonView.showError("An error occured while deleting the backlog item.");
      },
      success: function(data,type) {
    	me.iterationGoal.removeBacklogItem(me);
        for(var i = 0 ; i < me.deleteListeners.length; i++) {
          me.deleteListeners[i]();
        }
        commonView.showOk("Backlog item deleted.");
      },
      cache: false,
      type: "POST",
      url: "ajaxDeleteBacklogItem.action",
      data: {backlogItemId: this.id}
    });
    
  },
  resetOriginalEstimate: function() {
	    if(this.inTransaction) {
	        return;
	      }
	      var me = this;
	      var data = {backlogItemId: this.id};
	      jQuery.ajax({
	        async: false,
	        error: function() {
	          commonView.showError("An error occured while saving the backlog item.");
	        },
	        success: function(data,type) {
	          me.setData(data,false);
	          commonView.showOk("Original estimate reseted succesfully.");
	        },
	        cache: false,
	        dataType: "json",
	        type: "POST",
	        url: "resetOriginalEstimate.action",
	        data: data
	      });
  },
  save: function() {
    if(this.inTransaction) {
      return;
    }
    var me = this;
    var data  = {
        "backlogItem.name": this.name,
        "backlogItem.state": this.state,
        "backlogItem.priority": this.priority,
        "backlogItem.description": this.description,
        "backlogItem.effortLeft": this.effortLeft,
        "backlogItem.originalEstimate": this.originalEstimate,
        "userIds": [],
        "themeIds": [],
        backlogId: this.backlog.getId(),
        backlogItemId: this.id
    };
    if(this.iterationGoal) {
    	data.iterationGoalId = this.iterationGoal.id;
    }
    if (this.userIds) {
      data["userIds"] = this.userIds;
      this.userIds = null;
    }
    else if (this.users) {
      data["userIds"] = [];
      for(var i = 0; i < this.users.length; i++) {
    	  data["userIds"].push(this.users[i].user.id);
      }
    }
    if(this.themeIds) {
      data["themeIds"] = this.themeIds;
      this.themeIds = null;
    } else if(this.themes) {
      data["themeIds"] = agilefantUtils.objectToIdArray(this.themes);
    }
    //conversions
    if(data["backlogItem.effortLeft"]) data["backlogItem.effortLeft"] /= 3600;
    if(data["backlogItem.effortLeft"] == null) data["backlogItem.effortLeft"] = "";
    if(data["backlogItem.originalEstimate"]) data["backlogItem.originalEstimate"] /= 3600;
    if(data["backlogItem.originalEstimate"] == null) data["backlogItem.originalEstimate"] = "";
    if(this.name == undefined) data.name = "";
    if(this.description == undefined) data.description = "";
    
    jQuery.ajax({
      async: false,
      error: function() {
        commonView.showError("An error occured while saving the backlog item.");
      },
      success: function(data,type) {
        me.setData(data);
        commonView.showOk("Backlog item saved succesfully.");
      },
      cache: false,
      dataType: "json",
      type: "POST",
      url: "ajaxStoreBacklogItem.action",
      data: data
    });
  }
};

/** BACKLOG ITEM HOUR ENTRY  **/

var backlogItemHourEntryModel = function(backlogItem, data) {
	
	this.editListeners = [];
	this.deleteListeners = [];
	this.backlogItem = backlogItem;
	this.setData(data);
};
backlogItemHourEntryModel.prototype = {
	setData: function(data) {
		var bubbleEvents = [];
		if(!this.persistedData || this.timeSpent != this.persistedData.timeSpent) {
			bubbleEvents.push("metricsUpdated");
		}
		
		this.user = data.user;
		this.timeSpent = data.timeSpent;
		this.comment = data.comment;
		this.date = data.date;
		
		for (var i = 0; i < this.editListeners.length; i++) {
			this.editListeners[i]({bubbleEvent: bubbleEvents});
		}
		this.persistedData = data;
	},
	setTimeSpent: function(timeSpent) {
		this.timeSpent = timeSpent;
	},
	getTimeSpent: function() {
		return this.timeSpent;
	},
	setUser: function(userId) {
		this.userId = userId;
	},
	getUser: function() {
		return this.user;
	},
	setComment: function(comment) {
	  this.comment = comment;
	},
	getComment: function() {
	  return this.comment;
	},
	setDate: function(date) {
	  this.date = date;
	},
	getDate: function() {
	  return this.date;
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
		this.setData(this.persistedData);
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

/** TODO MODEL **/

var todoModel = function(backlogItem, data) {
  this.editListeners = [];
  this.deleteListeners = [];
	this.backlogItem = backlogItem;
	this.setData(data);
};
todoModel.prototype = {
	setData: function(data) {
		var bubbleEvents = [];
		if(!this.persistedData || this.state != this.persistedData.state) {
			bubbleEvents.push("metricsUpdated");
		}
		this.state = data.state;
		this.name = data.name;
		
		for (var i = 0; i < this.editListeners.length; i++) {
			this.editListeners[i]({bubbleEvent: bubbleEvents});
		}
		this.persistedData = data;
	},
	setState: function(state) {
		this.state = state;
	},
	getState: function() {
		return this.state;
	},
	setName: function(name) {
	  this.name = name;
	},
	getName: function() {
	  return this.name;
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
		this.setData(this.persistedData);
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
