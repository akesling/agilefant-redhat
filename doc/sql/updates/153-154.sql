alter table backlogitem add column parentBli_id integer;
alter table backlogitem add index FK655CD5909392F6DD (parentBli_id), add constraint FK655CD5909392F6DD foreign key (parentBli_id) references backlogitem (id);
