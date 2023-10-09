insert into location (name) values ('Phoenix / Southwest');
insert into location (name) values ('West Chester Operations Center');
-- sample of a child location
insert into location (name, parent_id) values ('4th Floor', 2);

insert into customer (name, sn_sys_id) values ('Arcadia Resources, Inc.', '9200443834ccf8c81259236c43b96824');

insert into service (business_model, code, osp_id, version, name, description, created) values ('Cloud', '05-001-00', 351, 0.02, 'Enterprise Cloud - Virtualization & Cloud - X86', 'Base Virtual DataCenter/Infrastructure', '2014-01-01 00:00:00');
insert into service (business_model, code, osp_id, version, name, description, created) values ('Cloud', '04-001-01', 53, 1.13, 'Enterprise Cloud - Server - Linux', 'Management of Linux in the Cloud...', '2014-01-01 00:00:00');
insert into service (code, osp_id, version, name, description, created) values (null, 90002, 1.0, 'Non-Portfolio Products', 'Non-Portfolio Products', '2016-07-21 00:00:00');
insert into service (business_model, code, osp_id, version, name, description, created) values ('Cloud', '05-001-02', 52, 1.13, 'Enterprise Cloud - Server - MS Windows', 'Management of Windows in the Cloud...', '2014-01-01 00:00:00');
insert into service (business_model, code, osp_id, version, name, description, created) values ('Managed', null, 1, 1.12, 'Managed - Network - Cisco', 'description unavailable', '2014-01-01 00:00:00');

-- device examples
insert into device (alt_id, part_number, description) values ('10011', 'MS-NET-CVPN-P', 'Firewall / VPN Appliance - Cisco');
insert into device (alt_id, part_number, description) values ('10066', 'MS-NET-CRS-P', 'Router, Small - Cisco and HP Devices');
insert into device (alt_id, part_number, description) values ('10077', 'MS-NET-CSL-P', 'Switch, Large - Cisco Devices');
insert into device (alt_id, part_number, description) values ('10088', 'MS-NET-CSS-P', 'Switch, Small - Cisco Devices');

-- examples of "real" items that incur COST
insert into lineitem (name, units, mode) values ('RAM', 'GB', 'months');
insert into lineitem (name, units, mode) values ('CPU', 'Cores', 'days');
insert into lineitem (name, units, mode) values ('Disk', 'GB', 'months');
insert into lineitem (name, units, mode) values ('BW', 'MBps', 'months');

-- examples of REVENUE lineitems
insert into lineitem (name, units, mode) values ('1VCPU', 'Cores', 'days');
insert into lineitem (name, units, mode) values ('2VCPU', 'Cores', 'days');
insert into lineitem (name, units, mode) values ('Windows Server', 'Server', 'days');
insert into lineitem (name, units, mode) values ('Linux Server', 'Server', 'days');
insert into lineitem (name, units, mode) values ('Virtual Backup', 'GB', 'months');

insert into contract (alt_id, job_number, name, emgr, customer_id, sdm, sda, signed_date, start_date, end_date, created) values ('ARC-C124', '0169431', 'Arcadia x86 Container', 'Jen', 1, 'Taylor', 'Johnson', '2014-07-01 00:00:00', '2014-08-01 00:00:00', '2017-07-31 23:59:59', '2014-06-11 16:13:45');
insert into contract (alt_id, job_number, name, emgr, customer_id, sdm, sda, signed_date, start_date, end_date, created) values ('ARC-C000', '0123456', 'Arcadia Internal', 'Jen', 1, 'Taylor', 'Johnson', '2015-01-01 09:00:00', '2015-02-01 00:00:00', '2017-12-31 23:59:59', '2015-01-01 09:00:00');
insert into contract_update (alt_id, job_number, note, contract_id, signed_date) values ('ABCDE', '01234567', 'a PCR for X', 1, '2015-05-01 12:00:00');


insert into contract_group (contract_id, name) values (2, 'Arcadia Dept 1');
insert into contract_group (contract_id, name) values (2, 'Arcadia Dept 2');

-- x86 container
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 1, 0., 2916.71, '2014-08-01 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 2, 100.00, 186.00, '2015-05-01 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 2, 100.00, 186.00, '2015-05-01 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 2, 100.00, 186.00, '2015-06-15 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 2, 100.00, 186.00, '2015-06-15 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 2, 50.00, 100.00, '2015-10-01 00:00:00', '2015-11-15 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 2, 50.00, 100.00, '2015-10-01 00:00:00', '2015-11-15 00:00:00', '2014-06-11 16:13:45');

-- insert 2 servers in 2 different "groups" into contract 2
insert into contract_service (contract_id, contract_group_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (2, 1, 2, 100.00, 186.00, '2015-02-01 00:00:00', '2017-12-31 23:59:59', '2015-01-01 09:00:00');
insert into contract_service (contract_id, contract_group_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (2, 2, 2, 100.00, 186.00, '2015-02-01 00:00:00', '2017-12-31 23:59:59', '2015-01-01 09:00:00');

insert into contract_update_contract_service (contract_update_id, contract_service_id, note) values (1, 4, 'added server at customer request');
insert into contract_update_contract_service (contract_update_id, contract_service_id, note) values (1, 5, 'added another server at customer request');

insert into contract_service_device (contract_service_id, device_id, name) values (1, 38, 'arc-ent-cntr01');
insert into contract_service_device (contract_service_id, device_id, name) values (2, 133, 'arc-ent-srvr01');
insert into contract_service_device (contract_service_id, device_id, name) values (3, 133, 'arc-ent-srvr02');
insert into contract_service_device (contract_service_id, device_id, name) values (4, 133, 'arc-ent-srvr03');
insert into contract_service_device (contract_service_id, device_id, name) values (5, 133, 'arc-ent-srvr04');
insert into contract_service_device (contract_service_id, device_id, name) values (6, 133, 'arc-ent-srvr05');
insert into contract_service_device (contract_service_id, device_id, name) values (7, 133, 'arc-ent-srvr06');

insert into contract_adjustment (contract_id, adjustment, adjustment_type, note, start_date, end_date) values (1, 101., 'onetime', 'a onetime credit at contract level', '2015-05-01 00:00:00', '2015-05-31 00:00:00');
insert into contract_adjustment (contract_id, contract_update_id, adjustment, adjustment_type, note, start_date, end_date) values (2, 1, 500., 'recurring', 'a recurring, 1yr credit at contract level, on a pcr', '2015-07-01 00:00:00', '2015-09-30 00:00:00');
insert into contract_adjustment (contract_id, contract_update_id, adjustment, adjustment_type, note, start_date, end_date) values (1, 1, -200., 'onetime', 'a onetime charge/penalty at contract level, on a pcr', '2015-06-01 00:00:00', '2015-06-30 00:00:00');

-- contract invoices
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-01-01 00:00:00', '2016-01-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-02-01 00:00:00', '2016-02-29 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-03-01 00:00:00', '2016-03-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-04-01 00:00:00', '2016-04-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-05-01 00:00:00', '2016-05-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-06-01 00:00:00', '2016-06-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-07-01 00:00:00', '2016-07-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-08-01 00:00:00', '2016-08-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-09-01 00:00:00', '2016-09-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-10-01 00:00:00', '2016-10-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-11-01 00:00:00', '2016-11-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2016-12-01 00:00:00', '2016-12-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2017-01-01 00:00:00', '2017-01-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2017-02-01 00:00:00', '2017-02-28 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2017-03-01 00:00:00', '2017-03-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2017-04-01 00:00:00', '2017-04-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2017-05-01 00:00:00', '2017-05-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2017-06-01 00:00:00', '2017-06-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (1, '2017-07-01 00:00:00', '2017-07-31 00:00:00', 'active');

insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-01-01 00:00:00', '2016-01-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-02-01 00:00:00', '2016-02-29 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-03-01 00:00:00', '2016-03-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-04-01 00:00:00', '2016-04-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-05-01 00:00:00', '2016-05-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-06-01 00:00:00', '2016-06-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-07-01 00:00:00', '2016-07-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-08-01 00:00:00', '2016-08-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-09-01 00:00:00', '2016-09-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-10-01 00:00:00', '2016-10-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-11-01 00:00:00', '2016-11-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2016-12-01 00:00:00', '2016-12-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-01-01 00:00:00', '2017-01-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-02-01 00:00:00', '2017-02-28 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-03-01 00:00:00', '2017-03-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-04-01 00:00:00', '2017-04-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-05-01 00:00:00', '2017-05-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-06-01 00:00:00', '2017-06-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-07-01 00:00:00', '2017-07-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-08-01 00:00:00', '2017-08-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-09-01 00:00:00', '2017-09-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-10-01 00:00:00', '2017-10-31 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-11-01 00:00:00', '2017-11-30 00:00:00', 'active');
insert into contract_invoice (contract_id, start_date, end_date, status) values (2, '2017-12-01 00:00:00', '2017-12-31 00:00:00', 'active');

-- device association

-- RAM
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 1, 1, 36, 35., '2014-08-01 00:00:00', '2014-06-11 16:13:45');
-- CPU
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 2, 2, 14, 9., '2014-08-01 00:00:00', '2014-06-11 16:13:45');
-- Storage
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 1, 3, 5, 5., '2014-08-01 00:00:00', '2014-06-11 16:13:45');
-- BW
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 1, 4, 5, 3.6, '2014-08-01 00:00:00', '2014-06-11 16:13:45');
-- 1VCPU
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 1, 5, 1, 59., '2014-08-01 00:00:00', '2014-06-11 16:13:45');
-- 2VCPU
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 1, 6, 1, 120., '2014-08-01 00:00:00', '2014-06-11 16:13:45');
-- Windows Server
insert into contract_lineitem (contract_id, service_id, lineitem_id, quantity, recurring_revenue, start_date, created) values (1, 1, 7, 1, 465., '2014-08-01 00:00:00', '2014-06-11 16:13:45');

