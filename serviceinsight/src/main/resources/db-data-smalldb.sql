-- creates a minimal database
-- first run db-schema.sql on a new, blank db
-- run the db-schema-security.sql
-- run the commands in this script
-- you then have a minimal db with the login admin@logicalis.com / Spreadsh44t

--select * from labor_rate;
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('MST1', 'Tier 1', null, 34.96, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('MST2', 'Tier 2', null, 45.24, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('MST3', 'Tier 3', null, 100., 0.00, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('MST4', 'Tier 4', null, 100.80, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('Shift Leader', 'Shift Leader', null, 45.24, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('OMSSDM', 'SDM', null, 83.91, 1.02, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('OMSSDA', 'SDA', null, 83.91, 1.02, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('OSS-Tools', 'Development', null, 78.72, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('OMS400', 'Cloud Ops', null, 103.92, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('Admin', 'Admin', null, 0.00, 0.00, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('Managed Teams', 'Managed Teams', null, 0.00, 0.00, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('Managers', 'Managers', null, 0.00, 0.00, 1.00, 'system');

--select * from chronos_task_rule;
insert into chronos_task_rule (labor_type, task_description, rule) values ('Administrative', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('SetUp', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('Sales', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('Operational', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('Internal', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('Internal', 'Cloud Infrastructure', 'Cloud');

--select * from scheduled_task;
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('Chronos Labor Data Sync', 'chronos_sync', 'Task to Sync with Chronos Timekeeping system for labor data', '1', '2017-02-08 13:41:34.0', null, 'system', null);
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('Schedule for Indirect Labor Data Calculations', 'indirect_labor_unit_cost', 'Task to update indirect labor data', '1', '2017-02-08 16:41:34.0', null, 'system', null);
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('Labor Unit Cost', 'labor_unit_cost', 'Adds the labor component for the last month to Expense Category Unit Costs. Set to run on the first of the month... at 1:45am.', '1', '2017-11-16 13:17:40.0', null, 'system', null);
-- disabled...
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('Service Now Customer Sync', 'sn_customer_sync', 'Task to Sync the Service Now Customer Sys Id', '0', '2016-06-07 17:05:55.0', null, 'mfoster', null);
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('Service Now Contract Sync', 'sn_contract_sync', 'Task to Sync the Service Now Contract Sys Id', '0', '2016-06-07 17:05:55.0', null, 'mfoster', null);
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('Service Now Contract CI Sync', 'sn_contract_ci_sync', 'Task to Sync the Service Now Contract CIs', '0', '2016-06-07 17:05:56.0', null, 'mfoster', null);
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('One Customer Sync', 'one_customer_sync', 'Task to Sync with One Customer from Pricing', '0', '2016-10-06 17:27:16.0', null, 'mfoster', null);
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('OSP Service Sync', 'osp_service_sync', 'Task to Sync OSP Services with the Service Insight Services', '0', '2017-03-09 17:03:47.0', null, 'system', null);
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('Azure Rate Card Sync', 'azure_rate_card_sync', 'Task to Sync the Azure Rate Card. Set to run nightly.', '0', '2017-09-29 17:13:04.0', null, 'system', null);
insert into scheduled_task (name, code, description, enabled, created, updated, created_by, updated_by) values ('Azure Monthly Billing Sync', 'azure_monthly_billing_sync', 'Task to Sync the generate the contract services for Azure.', '0', '2017-09-29 17:13:06.0', null, 'system', null);

--select * from labor_import_log;
insert into labor_import_log (record_count, inserted_date) values (0, '2009-01-01 00:00:00');

--select * from customer where name like 'Logicalis%';
insert into customer (name, description, created, updated, created_by, updated_by, archived, sn_sys_id, alt_id, parent_id, phone, street_1, street_2, city, state, zip, country, si_enabled)
values ('Logicalis IT', null, '2015-04-08 08:29:34.0', '2017-11-16 03:00:11.0', 'data', 'onecustomer', '0', '2791d5830a0a3c1c00adea31fa20be61', 215, null, '(513) 707-7007', '', null, 'Bloomfield Hills', 'MI', '', 'USA', '1');

--select * from contract where customer_id in (select id from customer where name like 'Logicalis%' and archived is false)
insert into contract (alt_id, customer_id, name, sdm, sda, created, updated, signed_date, start_date, end_date, created_by, updated_by, emgr, job_number, archived, sn_sys_id, quote_id)
values ('LOGI4022813K', 1, 'DRaaS', null, 'Not Applicable', '2015-04-20 11:59:17.0', '2016-06-07 15:12:37.0', '2013-06-16 21:00:00.0', '2016-09-08 21:00:00.0', '2019-09-08 21:00:00.0', 'data', 'unknown', 'Dane Duncan', '0158745', '0', 'ce47812e0a0a3c540039daa926e65b1a', null);
insert into contract_invoice (contract_id, status, created, updated, created_by, updated_by, start_date, end_date) values (1, 'active', '2016-05-05 17:17:09.0', null, 'admin', null, '2017-11-01 00:00:00.0', '2017-11-30 00:00:00.0');

--select * from customer where name like 'IFS%';
insert into customer (name, description, created, updated, created_by, updated_by, archived, sn_sys_id, alt_id, parent_id, phone, street_1, street_2, city, state, zip, country, si_enabled)
values ('IFS', null, '2015-04-08 08:20:51.0', '2017-11-16 03:00:10.0', 'data', 'onecustomer', '0', 'a0a92ad5ed3bd08c1259b1b6a4af4bdc', 34, null, '(520) 396-2176', '300 Park Boulevard, Suite 555', null, 'Itasca', 'IL', '60143', 'USA', '1');

--select * from contract where customer_id in (select id from customer where name like 'IFS%' and archived is false)
insert into contract (alt_id, customer_id, name, sdm, sda, created, updated, signed_date, start_date, end_date, created_by, updated_by, emgr, job_number, archived, sn_sys_id, quote_id)
values ('IFSX7024660L', 2, 'IFS_Client - LEC and MS', null, 'Sandy Shute', '2015-04-17 14:17:40.0', '2017-07-12 10:30:05.0', '2012-01-12 21:00:00.0', '2012-02-12 21:00:00.0', '2021-11-29 21:00:00.0', 'data', 'dave.sumner@us.logicalis.com', 'Eric Rummel', '0136336', '0', '67d4f611edfbd08c1259b1b6a4af4b06', null);
insert into contract_invoice (contract_id, status, created, updated, created_by, updated_by, start_date, end_date) values (2, 'active', '2016-05-05 17:17:09.0', null, 'admin', null, '2017-11-01 00:00:00.0', '2017-11-30 00:00:00.0');

--select * from customer where name like 'GE Healthcare%';
insert into customer (name, description, created, updated, created_by, updated_by, archived, sn_sys_id, alt_id, parent_id, phone, street_1, street_2, city, state, zip, country, si_enabled)
values ('GE Healthcare', null, '2016-10-07 03:00:02.0', '2017-11-16 03:00:10.0', 'onecustomer', 'onecustomer', '0', 'f7574b3f371562c0cf13616043990e9b', 295, null, '', '9900 West Innovation Drive', null, 'Wauwatosa', 'Wisconsin', '53226', 'USA', '1');

--select * from contract where customer_id in (select id from customer where name like 'GE Healthcare%' and archived is false)
insert into contract (alt_id, customer_id, name, sdm, sda, created, updated, signed_date, start_date, end_date, created_by, updated_by, emgr, job_number, archived, sn_sys_id, quote_id)
values ('APIH7025566D', 3, 'GE Healthcare - Atlantic/Provider', null, 'Bill Bogard', '2015-04-09 13:27:54.0', '2017-08-14 22:42:31.0', '2011-07-07 21:00:00.0', '2011-07-14 21:00:00.0', '2019-12-30 21:00:00.0', 'data', 'unknown', 'Nicole Colpitts', '0129769', '0', 'bbc94260db8622c00e9950cbbf961968', null);
insert into contract_invoice (contract_id, status, created, updated, created_by, updated_by, start_date, end_date) values (3, 'active', '2016-05-05 17:17:09.0', null, 'admin', null, '2017-11-01 00:00:00.0', '2017-11-30 00:00:00.0');

--select * from service where name = 'Managed - Server - Linux' and active = true;
insert into service (code, osp_id, version, name, description, active, created, updated, created_by, updated_by, business_model)
values (null, 6, 2.5000, 'Managed - Server - MS Windows', 'description unavailable', '1', '2017-06-19 11:00:00.0', null, 'system', null, 'Managed');
insert into service (code, osp_id, version, name, description, active, created, updated, created_by, updated_by, business_model)
values (null, 42, 2.5000, 'Managed - Server - Linux', 'description unavailable', '1', '2017-06-19 11:00:00.0', null, 'system', null, 'Managed');

--select * from expense_category
--insert into expense_category (parent_id, name, description, target_utilization, units, created, updated, created_by, updated_by) values (null, 'CA', null, 0.00, null, '2017-03-10 08:56:30.0', null, 'mfoster', null);
--insert into expense_category (parent_id, name, description, target_utilization, units, created, updated, created_by, updated_by) values (1, 'CA Licenses', null, 0.00, 'Licenses', '2017-03-10 08:56:30.0', null, 'mfoster', null);
insert into expense_category (parent_id, name, description, target_utilization, units, created, updated, created_by, updated_by) values (null, 'Monitoring', null, 0.00, 'Units', '2017-03-10 08:56:30.0', null, 'mfoster', null);
insert into expense_category (parent_id, name, description, target_utilization, units, created, updated, created_by, updated_by) values (1, 'Nimsoft - Server Probes', null, 0.00, 'Probes', '2017-03-10 08:56:30.0', null, 'mfoster', null);
--insert into expense_category (parent_id, name, description, target_utilization, units, created, updated, created_by, updated_by) values (1, 'Nimsoft - Network Probes', null, 0.00, 'Probes', '2017-03-10 08:56:30.0', null, 'mfoster', null);

insert into device (part_number, description, created, updated, created_by, updated_by, alt_id, product_id, archived, osm_sync_enabled, default_osp_id, device_type)
values ('MS-SRV-LIX-MGD', 'Managed Linux Server', '2017-04-05 17:09:24.0', '2017-04-05 17:09:25.0', 'matt.foster@us.logicalis.com', 'matt.foster@us.logicalis.com', null, 129, 0, 0, 42, null);
insert into device (part_number, description, created, updated, created_by, updated_by, alt_id, product_id, archived, osm_sync_enabled, default_osp_id, device_type)
values ('MS-SRV-WIN-MGD', 'Managed Windows Server', '2015-09-23 14:48:30.0', '2017-04-05 17:09:25.0', 'Rose.Childress@us.logicalis.com', 'matt.foster@us.logicalis.com', null, null, 0, 1, 6, null);

insert into pricing_sheet (contract_id, osm_sync_enabled, created, updated, created_by, updated_by) values (1, 0, '2017-03-09 17:08:52.0', null, 'matt.foster@us.logicalis.com', null);
insert into pricing_sheet (contract_id, osm_sync_enabled, created, updated, created_by, updated_by) values (2, 0, '2017-03-09 17:08:52.0', null, 'matt.foster@us.logicalis.com', null);
insert into pricing_sheet (contract_id, osm_sync_enabled, created, updated, created_by, updated_by) values (3, 0, '2017-03-09 17:08:52.0', null, 'matt.foster@us.logicalis.com', null);

insert into users (username, password, enabled, token, token_expires) values ('admin@logicalis.com', '1ada3e1455fc5a7f8f35cf1b13a5c7b8', '1', '4d01eccd00ea443314f41e39b59831275d42a705b6f5f6a6c07278da02a55c2aa74d449db4ac0b31', '2015-07-23 15:49:25.0');

insert into location (parent_id, name, description, created, updated, created_by, updated_by) values (null, 'IO Dayton', 'Dayton Datacenter', '2017-03-10 12:08:18.0', null, 'mfoster', null);

--select * from authorities where username = 'Patrick.ONeil@us.logicalis.com';
insert into authorities (username, authority) values ('admin@logicalis.com', 'ROLE_ADMIN');

--select * from Export_Table where Customer like 'Logicalis IT%' and Date between '2017-10-01' and '2017-10-31' and task_description is not null;
insert into Export_Table (Ticket, Date, Customer_ID, Customer, CI_ID, CI_Name, Service_Name, Task_Description, Hours, Num_CIs, Name, Team, Labor_Type, Inserted)
values ('1001', '2017-10-04 16:00:00', '2791d5830a0a3c1c00adea31fa20be61', 'Logicalis IT', null, null, null, 'Nimsoft - Server Probes', 10., null, 'Joe Smith', 'Tier 3', 'Operational', '2017-10-09 03:00:18.63');
insert into Export_Table (Ticket, Date, Customer_ID, Customer, CI_ID, CI_Name, Service_Name, Task_Description, Hours, Num_CIs, Name, Team, Labor_Type, Inserted)
values ('2001', '2017-10-21 16:00:00', '2791d5830a0a3c1c00adea31fa20be61', 'Logicalis IT', null, null, null, 'Nimsoft - Server Probes', 10., null, 'Amy Sheever', 'Tier 3', 'Operational', '2017-10-09 03:00:18.63');

--select * from Export_Table where Customer like 'GE Healthcare%' and Date between '2017-10-01' and '2017-10-31' and task_description is not null;
insert into Export_Table (Ticket, Date, Customer_ID, Customer, CI_ID, CI_Name, Service_Name, Task_Description, Hours, Num_CIs, Name, Team, Labor_Type, Inserted)
values ('2002', '2017-10-22 16:00:00', 'f7574b3f371562c0cf13616043990e9b', 'GE Healthcare', null, null, null, 'Nimsoft - Server Probes', 10., null, 'Amy Sheever', 'Tier 3', 'Operational', '2017-10-09 03:00:18.63');
insert into Export_Table (Ticket, Date, Customer_ID, Customer, CI_ID, CI_Name, Service_Name, Task_Description, Hours, Num_CIs, Name, Team, Labor_Type, Inserted)
values ('1002', '2017-10-05 16:00:00', 'f7574b3f371562c0cf13616043990e9b', 'GE Healthcare', null, null, null, 'Nimsoft - Server Probes', 10., null, 'Joe Smith', 'Tier 3', 'Operational', '2017-10-09 03:00:18.63');

--select * from Export_Table where Customer like 'IFS%' and Date between '2017-10-01' and '2017-10-31' and task_description is not null;
insert into Export_Table (Ticket, Date, Customer_ID, Customer, CI_ID, CI_Name, Service_Name, Task_Description, Hours, Num_CIs, Name, Team, Labor_Type, Inserted)
values ('2003', '2017-10-23 16:00:00', 'a0a92ad5ed3bd08c1259b1b6a4af4bdc', 'IFS', null, null, null, 'Nimsoft - Server Probes', 10., null, 'Amy Sheever', 'Tier 3', 'Operational', '2017-10-09 03:00:18.63');
insert into Export_Table (Ticket, Date, Customer_ID, Customer, CI_ID, CI_Name, Service_Name, Task_Description, Hours, Num_CIs, Name, Team, Labor_Type, Inserted)
values ('1003', '2017-10-06 16:00:00', 'a0a92ad5ed3bd08c1259b1b6a4af4bdc', 'IFS', null, null, null, 'Nimsoft - Server Probes', 10., null, 'Joe Smith', 'Tier 3', 'Operational', '2017-10-09 03:00:18.63');

