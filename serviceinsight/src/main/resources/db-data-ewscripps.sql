insert into customer (name, sn_sys_id) values ('EW Scripps', '39b6f4a50a0a3c1c01fc6a171fff3433');
insert into contract (alt_id, job_number, name, emgr, customer_id, sdm, sda, signed_date, start_date, end_date, created) values ('EWS-C124', '0159076 - 3', 'EW Scripps Contract', 'Jen', 3, 'Taylor', 'Johnson', '2014-07-01 00:00:00', '2014-08-01 00:00:00', '2017-07-31 23:59:59', '2014-06-11 16:13:45');

insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (4, 2, 100.00, 186.00, '2015-06-15 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (4, 2, 100.00, 186.00, '2015-06-15 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (4, 2, 100.00, 186.00, '2015-06-15 00:00:00', '2017-07-31 00:00:00', '2014-06-11 16:13:45');
insert into contract_service_device (contract_service_id, device_id, name) values (14, 38, 'c1pwwtap21');
insert into contract_service_device (contract_service_id, device_id, name) values (15, 133, 'c1dwssrs01');
insert into contract_service_device (contract_service_id, device_id, name) values (16, 133, 'c1dxbiap01');
