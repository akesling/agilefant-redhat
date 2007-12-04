create table ActivityType (id integer not null auto_increment, targetSpendingPercentage integer not null check (targetSpendingPercentage>=0 and targetSpendingPercentage<=100), name varchar(255) not null unique, description text, primary key (id)) ENGINE=InnoDB;
create table Backlog (backlogtype varchar(31) not null, id integer not null auto_increment, name varchar(255), description text, startDate datetime, endDate datetime, rank integer not null default 0 not null, product_id integer, project_id integer, activityType_id integer, assignee_id integer, history_fk integer, owner_id integer, primary key (id)) ENGINE=InnoDB;
create table BacklogItem (id integer not null auto_increment, effortLeft integer, originalEstimate integer, name varchar(255), priority integer, description text, state integer, assignee_id integer, iterationGoal_id integer, backlog_id integer not null, primary key (id)) ENGINE=InnoDB;
create table History (DTYPE integer not null, id integer not null auto_increment, primary key (id)) ENGINE=InnoDB;
create table HistoryEntry (id integer not null auto_increment, effortLeft integer, originalEstimate integer, date date, history_id integer, primary key (id)) ENGINE=InnoDB;
create table IterationGoal (id integer not null auto_increment, name varchar(255), priority integer, description text, iteration_id integer not null, primary key (id)) ENGINE=InnoDB;
create table Task (id integer not null auto_increment, created datetime, name varchar(255), priority integer, description text, state integer, backlogItem_id integer not null, creator_id integer, primary key (id)) ENGINE=InnoDB;
create table User (id integer not null auto_increment, loginName varchar(255) unique, email varchar(255), fullName varchar(255), password varchar(255), primary key (id)) ENGINE=InnoDB;
create table WorkType (id integer not null auto_increment, name varchar(255) not null, description text, activityType_id integer, primary key (id)) ENGINE=InnoDB;
alter table Backlog add index FK4E86B8DDCA187B22 (project_id), add constraint FK4E86B8DDCA187B22 foreign key (project_id) references Backlog (id);
alter table Backlog add index FK4E86B8DDC91A641F (history_fk), add constraint FK4E86B8DDC91A641F foreign key (history_fk) references History (id);
alter table Backlog add index FK4E86B8DD31FA7A4E (assignee_id), add constraint FK4E86B8DD31FA7A4E foreign key (assignee_id) references User (id);
alter table Backlog add index FK4E86B8DDCC65BE32 (activityType_id), add constraint FK4E86B8DDCC65BE32 foreign key (activityType_id) references ActivityType (id);
alter table Backlog add index FK4E86B8DDA7FE2362 (product_id), add constraint FK4E86B8DDA7FE2362 foreign key (product_id) references Backlog (id);
alter table Backlog add index FK4E86B8DD2D47BAEA (owner_id), add constraint FK4E86B8DD2D47BAEA foreign key (owner_id) references User (id);
alter table BacklogItem add index FKC8B7F1907A2D5E2 (iterationGoal_id), add constraint FKC8B7F1907A2D5E2 foreign key (iterationGoal_id) references IterationGoal (id);
alter table BacklogItem add index FKC8B7F19031FA7A4E (assignee_id), add constraint FKC8B7F19031FA7A4E foreign key (assignee_id) references User (id);
alter table BacklogItem add index FKC8B7F190F63400A2 (backlog_id), add constraint FKC8B7F190F63400A2 foreign key (backlog_id) references Backlog (id);
alter table HistoryEntry add index FK9367445EC91A6475 (history_id), add constraint FK9367445EC91A6475 foreign key (history_id) references History (id);
alter table HistoryEntry add index FK9367445EFD7DC542 (history_id), add constraint FK9367445EFD7DC542 foreign key (history_id) references History (id);
alter table IterationGoal add index FKBCC95B704157D2A2 (iteration_id), add constraint FKBCC95B704157D2A2 foreign key (iteration_id) references Backlog (id);
alter table Task add index FK27A9A5E94683E2 (backlogItem_id), add constraint FK27A9A5E94683E2 foreign key (backlogItem_id) references BacklogItem (id);
alter table Task add index FK27A9A51C5D0ED1 (creator_id), add constraint FK27A9A51C5D0ED1 foreign key (creator_id) references User (id);
alter table WorkType add index FK5EE3E0BCC65BE32 (activityType_id), add constraint FK5EE3E0BCC65BE32 foreign key (activityType_id) references ActivityType (id);