insert into customer (name, sn_sys_id) values ('Anixter Global', 'c5b6977c0a0a3c1b0111ba7adf5c43d0');

insert into contract (alt_id, job_number, name, emgr, customer_id, sdm, sda, signed_date, start_date, end_date, created) values ('ANIX-C124', '0169431', 'Anixter x86 Container', 'John', 2, 'Taylor', 'Johnson', '2014-07-01 00:00:00', '2014-08-01 00:00:00', '2017-07-31 23:59:59', '2014-06-11 16:13:45');

-- x86 container
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (2, 1, 0., 2916.71, '2014-08-01 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (2, 2, 100.00, 186.00, '2015-05-01 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (2, 2, 100.00, 186.00, '2015-05-01 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (2, 2, 100.00, 186.00, '2015-06-15 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');

insert into contract_service_device (contract_service_id, device_id, name) values (10, 38, 'arc-ent-cntr01');
insert into contract_service_device (contract_service_id, device_id, name) values (11, 133, 'arc-ent-srvr01');
insert into contract_service_device (contract_service_id, device_id, name) values (12, 133, 'arc-ent-srvr02');
insert into contract_service_device (contract_service_id, device_id, name) values (13, 133, 'arc-ent-srvr03');

