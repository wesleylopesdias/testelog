insert into customer (name) values ('API Foo');
insert into customer (name) values ('INT Foo');

insert into service (code, osp_id, version, name, description, active, created) values ('01-001', 53, 1.11, 'Enterprise Cloud - Virtualization & Cloud - X86', 1, 'Base Virtual DataCenter/Infrastructure', '2014-01-01 00:00:00');
insert into service (code, osp_id, version, name, description, active, created) values ('01-002', 253, 1.21, 'Enterprise Cloud - Server - Linux', 'Management of Linux in the Cloud...', 1, '2014-01-01 00:00:00');
insert into service (code, osp_id, version, name, description, active, created) values ('01-003', 243, 2.30, 'Enterprise Cloud - Server - MS Windows', 'Management of Windows in the Cloud...', 1, '2014-01-01 00:00:00');
insert into service_related (service_id, related_id) values (1, 2);
insert into service_related (service_id, related_id) values (1, 3);

insert into lineitem (name, units, mode) values ('Linux Server', 'Servers', 'months');
insert into lineitem (name, units, mode) values ('Windows Server', 'Servers', 'months');
insert into lineitem (name, units, mode) values ('RAM', 'GB', 'months');
insert into lineitem (name, units, mode) values ('CPU', 'Cores', 'days');
insert into lineitem (name, units, mode) values ('Disk', 'GB', 'months');
insert into lineitem (name, units, mode) values ('Encrypted Disk', 'GB', 'months');
insert into lineitem (name, units, mode) values ('Windows License', 'Licenses', 'months');

insert into contract (alt_id, name, customer_id, sdm, sda, start_date, end_date, created) values ('CON-124', 'API', 1, 'Jim', 'Sally', '2015-01-01 00:00:00', '2016-01-31 23:59:59', '2015-01-01 16:13:45');
-- contract 2:
insert into contract (alt_id, name, customer_id, sdm, sda, start_date, end_date, created) values ('CON-123', 'Test Contract', 1, 'Jim', 'Sally', '2015-01-01 00:00:00', '2015-06-30 23:59:59', '2015-01-01 16:13:45');

insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 1, 2000., 10000., '2015-01-01 00:00:00', '2016-01-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_service (contract_id, service_id, start_date, end_date, created) values (1, 2, '2015-01-01 00:00:00', '2016-01-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_service (contract_id, service_id, start_date, end_date, created) values (1, 3, '2015-01-01 00:00:00', '2016-01-31 23:59:59', '2015-01-01 16:13:45');
-- contract 2:
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (2, 1, 2000., 10000., '2015-01-01 00:00:00', '2015-06-30 23:59:59', '2015-01-01 16:13:45');
insert into contract_service (contract_id, service_id, start_date, end_date, created) values (2, 2, '2015-01-01 00:00:00', '2015-06-30 23:59:59', '2015-01-01 16:13:45');

-- server instances
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 2, 1, 20, 350.00, '2015-01-01 00:00:00', '2016-01-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 3, 2, 10, 300.00, '2015-01-01 00:00:00', '2015-05-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 3, 2, 11, 300.00, '2015-06-01 00:00:00', '2015-07-31 23:59:59', '2015-06-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 3, 2, 12, 300.00, '2015-08-01 00:00:00', '2015-08-01 16:13:45');
-- RAM
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 2, 3, 160, 40.00, '2015-01-01 00:00:00', '2016-01-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 3, 3, 80, 40.00, '2015-01-01 00:00:00', '2015-05-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 3, 3, 88, 40.00, '2015-06-01 00:00:00', '2015-07-31 23:59:59', '2015-06-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 3, 3, 96, 40.00, '2015-08-01 00:00:00', '2015-08-01 16:13:45');
-- CPU
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 2, 4, 20, 25.00, '2015-01-01 00:00:00', '2016-01-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 3, 4, 10, 25.00, '2015-01-01 00:00:00', '2015-05-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 3, 4, 11, 25.00, '2015-06-01 00:00:00', '2015-07-31 23:59:59', '2015-06-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 3, 4, 12, 25.00, '2015-08-01 00:00:00', '2015-08-01 16:13:45');
-- Win License
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 3, 7, 10, 24.00, '2015-01-01 00:00:00', '2015-05-31 23:59:59', '2015-01-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, end_date, created) values (1, 3, 7, 11, 24.00, '2015-06-01 00:00:00', '2015-07-31 23:59:59', '2015-06-01 16:13:45');
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 3, 7, 12, 24.00, '2015-08-01 00:00:00', '2015-08-01 16:13:45');

-- contract 2: 20 mngd linux servers
-- server instances
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (2, 2, 1, 10, 350.00, '2015-01-01 00:00:00', '2015-01-01 16:13:45');
-- RAM
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (2, 2, 3, 80, 40.00, '2015-01-01 00:00:00', '2015-01-01 16:13:45');
-- CPU
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (2, 2, 4, 10, 25.00, '2015-01-01 00:00:00', '2015-01-01 16:13:45');

--insert into labor_data (contract_id, labor_input_id, sn_hours, payroll_hours, created) values (1, 1, 813.29, 5628.42, '2015-02-01 00:00:00');
--insert into labor_data (contract_id, labor_input_id, sn_hours, payroll_hours, created) values (1, 2, 871.7, 6066.2, '2015-02-01 00:00:00');
