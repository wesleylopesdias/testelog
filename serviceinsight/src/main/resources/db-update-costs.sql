-- drop old cost tables...
drop view if exists view_asset_detail_cost;
drop table if exists standard_cost;
drop table if exists labor_data;
drop table if exists labor_input;
drop table if exists cost_general;
drop table if exists cost_spla;
drop table if exists asset_detail;
drop table if exists asset;
drop table if exists asset_category;

-- TODO from db-schema.sql, views or other scripts:
-- run all the creates for the new expense/asset/cost structures, including any
-- views or other sql

-- other updates to existing tables that are a part of the costs deployment below

-- need to make some on delete fixes
alter table service_related drop foreign key service_related_ibfk_1;
alter table service_related add foreign key (service_id) references service(id) on delete cascade;
alter table service_related drop foreign key service_related_ibfk_2;
alter table service_related add foreign key (related_id) references service(id) on delete cascade;

alter table contract_lineitem drop foreign key contract_lineitem_ibfk_3;
alter table contract_lineitem add foreign key (service_id) references service(id) on delete cascade;
alter table contract_lineitem drop foreign key contract_lineitem_ibfk_4;
alter table contract_lineitem add foreign key (lineitem_id) references lineitem(id) on delete cascade;
alter table contract_lineitem drop foreign key contract_lineitem_ibfk_5;
alter table contract_lineitem add foreign key (contract_update_id) references contract_update(id) on delete cascade;
