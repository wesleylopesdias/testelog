insert into expense_type_ref (name, display_name, description) values ('asset', 'Asset', 'An expense that is determined to be an asset, such as a server, or other hardware, even a building or equipment mover');
insert into expense_type_ref (name, display_name, description) values ('cost', 'Expense' , 'An expense that is determined to be non concrete asset oriented, like a utility bill, or rent or some other charge, professional maintainance or repairs');

insert into expense (alt_id, expense_type_ref_id, name, description, amount, quantity) values ('AJ-123', 1, 'Cloud RAM Purchase', 'Online purchase from Wholesale RAM', 1200.00, 48);
insert into expense (alt_id, expense_type_ref_id, name, description, amount) values ('7AB-001', 1, 'Cloud Blade Server', 'Trial purchase of blade from BladeSales', 757.89);
insert into expense (alt_id, expense_type_ref_id, name, description, amount) values ('11100021', 2, 'Rent - WC DC', 'Monthly rent for West Chester building', 988.0);

insert into expense_type (expense_type_ref_id, name, description) values (1, 'Memory', 'General type for computing memory');
insert into expense_type (expense_type_ref_id, name, description) values (1, 'CPU', 'General type for computing power');
insert into expense_type (expense_type_ref_id, name, description) values (1, 'Storage', 'General type for storage');
insert into expense_type (expense_type_ref_id, name, description) values (1, 'Backup storage', 'General type for things backup storage');
insert into expense_type (expense_type_ref_id, name, description) values (1, 'X86 Server', 'Lorem Ipsum');
insert into expense_type (expense_type_ref_id, name, description) values (1, 'Cisco network switch', 'Ipsum de lorean farrat');
insert into expense_type (expense_type_ref_id, name, description) values (2, 'Cloud', 'Any all cloud related business');
insert into expense_type (expense_type_ref_id, name, description) values (2, 'Managed Services', 'Managed Services related business');

insert into expense_category (name, description) values ('X86', 'Any/All X86 assets');
insert into expense_category (name, description) values ('AIX', 'Any/All AIX assets');
insert into expense_category (name, description) values ('IBM', 'Any/All IBM assets');
insert into expense_category (name, description) values ('Storage', '');
insert into expense_category (name, description) values ('Equipment', '');
insert into expense_category (name, description) values ('General Equipment', '');
insert into expense_category (name, description) values ('Leased Equipment', '');
insert into expense_category (name, description) values ('Cloud Equipment', '');

/*
insert into asset (asset_category_id, name, target_utilization, units) values (1, 'GHz CPU', .80, 'GHz');
insert into asset (asset_category_id, name, target_utilization, units) values (1, 'GB RAM', .80, 'GB');
insert into asset (asset_category_id, name, target_utilization, units) values (2, 'rPerf Units', .80, 'GHz');
insert into asset (asset_category_id, name, target_utilization, units) values (2, 'GB RAM', .80, 'GB');
insert into asset (asset_category_id, name, target_utilization, units) values (3, 'CPW', .80, 'GHz');
insert into asset (asset_category_id, name, target_utilization, units) values (3, 'GB RAM', .80, 'GB');
insert into asset (asset_category_id, name, target_utilization, units) values (4, 'T1/T2/T3', .80, 'GB');
insert into asset (asset_category_id, name, target_utilization, units) values (4, 'SSD', .80, 'GB');
insert into asset (asset_category_id, name, target_utilization, units) values (4, 'Encrypted', .80, 'GB');
insert into asset (asset_category_id, name, target_utilization) values (5, 'Equipment', .80);
insert into asset (asset_category_id, name, target_utilization, units) values (6, 'GHz CPU', .80, 'GHz');
insert into asset (asset_category_id, name, target_utilization, units) values (6, 'GB RAM', .80, 'GB');
insert into asset (asset_category_id, name, target_utilization) values (7, 'Equipment', .80);
*/

insert into asset_item (expense_type_id, expense_id_ref, name, description, amount, quantity, life, acquired) values (1, 1, 'Cloud RAM Purchase', 'Online purchase from Wholesale RAM-n-CPU', 1200.00, 48, 36, '2014-01-01');
insert into asset_item (expense_type_id, name, description, amount, quantity, life, acquired) values (2, 'Cloud CPU Purchase', 'Online purchase from Wholesale RAM-n-CPU', 555.50, 100, 36, '2014-01-01');
insert into asset_item (expense_type_id, expense_id_ref, name, description, amount, quantity, life, acquired) values (5, 2, 'Cloud Blade Server', 'Trial purchase of blade from BladeSales', 757.89, 1, 36, '2014-01-01');

insert into asset_item_fraction (expense_category_id, asset_item_id, cost_fraction, target_utilization) values (2, 1, 1., .80);
insert into asset_item_fraction (expense_category_id, asset_item_id, cost_fraction, target_utilization) values (1, 2, 1., .80);
insert into asset_item_fraction (expense_category_id, asset_item_id, cost_fraction, target_utilization) values (2, 3, .60, .80);
insert into asset_item_fraction (expense_category_id, asset_item_id, cost_fraction, target_utilization) values (1, 3, .40, .80);

/*
insert into business_unit (name, description) values ('Cloud', 'Any all cloud related business');
insert into business_unit (name, description) values ('Managed Services', 'Managed Services related business');
*/

insert into expense_category (name, description) values ('Rent', 'building type rent');
insert into expense_category (name) values ('Licenses');
insert into expense_category (name, description) values ('SPLA', 'Service Provider License Agreement');
insert into expense_category (name, description) values ('Telecom', 'Telecom related bill');
insert into expense_category (name, description) values ('Equipment Leases', 'trucks, equip. etc.');
insert into expense_category (name, description) values ('Utilities', 'lighting or electric bill...');
insert into expense_category (name, parent_id, description) values ('Datacenter', 1, 'DC rent');

insert into cost_item (expense_type_id, expense_id_ref, name, description, amount, applied, part_number, sku) values (1, 3, 'West Chester power building', null, 988.00, '2014-06-14 00:00:00', null, null);
insert into cost_item (expense_type_id, name, description, amount, applied, part_number, sku) values (2, 'Windows SQL Server 2015', 'Ipsum lorem', 1077.00, '2014-06-01 00:00:00', 'A01-888', '123456');
insert into cost_item (expense_type_id, name, description, amount, applied, part_number, sku) values (4, 'West Chester power bill Oct ''15', null, 2023.00, '2014-10-31 00:00:00', null, null);

insert into cost_item_fraction (expense_category_id, cost_item_id, cost_fraction) values (1, 1, .5);
insert into cost_item_fraction (expense_category_id, cost_item_id, cost_fraction) values (2, 1, .5);
insert into cost_item_fraction (expense_category_id, cost_item_id, cost_fraction) values (1, 2, 1.);
insert into cost_item_fraction (expense_category_id, cost_item_id, cost_fraction) values (1, 3, .5);
insert into cost_item_fraction (expense_category_id, cost_item_id, cost_fraction) values (2, 3, .5);

insert into labor_import_log (record_count, inserted_date) values (0, '2016-08-31 03:00:00');

insert into labor_rate(code, name, rate) values ('MST1', 'Tier 1', 46.04);
insert into labor_rate(code, name, rate) values ('MST2', 'Tier 2', 61.34);
insert into labor_rate(code, name, rate) values ('MST3', 'Tier 3', 85.24);
insert into labor_rate(code, name, rate) values ('MST4', 'Tier 4', 127.40);
insert into labor_rate(code, name, rate) values ('SDM', 'SDM', 104.38);
insert into labor_rate(code, name, rate) values ('Development', 'Development', 101.76);

/*
insert into cost_spla (license, part_no, sku, cost) values ('ExchgEntSAL ALNG LicSAPk MVL', '9MC-00001', 'Q76764', 4.29);
insert into cost_spla (license, part_no, sku, cost) values ('ExchgStdPlusSAL ALNG LicSAPk MVL', 'F09-00018', 'Q76770', 3.00);
insert into cost_spla (license, part_no, sku, cost) values ('SQLSvrEntCore ALNG LicSAPk MVL 2Lic CoreLic', '7JQ-00341', 'NY9533', 460.15);
insert into cost_spla (license, part_no, sku, cost) values ('SQLSvrStdCore ALNG LicSAPk MVL 2Lic CoreLic', '7NQ-00302', 'NY9535', 119.99);
insert into cost_spla (license, part_no, sku, cost) values ('WinRmtDsktpSrvcsSAL ALNG LicSAPk MVL', '6WC-00002', 'BU2506', 4.19);
insert into cost_spla (license, part_no, sku, cost) values ('WinSvrDataCtr ALNG LicSAPk MVL 1Proc', 'P71-01031', 'Q76723', 90);
insert into cost_spla (license, part_no, sku, cost) values ('WinSvrStd ALNG LicSAPk MVL 1Proc', 'P73-04837', 'V94166', 12.26);

insert into cost_general (name, allocation, six_month_avg, notes) values ('Rent', 'SP', 185274.33, 'Reasonable Split Needed, Assumed 50/50 for demonstration purposes.');
insert into cost_general (name, allocation, six_month_avg, notes) values ('Licenses', 'SP', 100041.33, 'CA/Nimsoft & Service Now – Scrub costs to confirm our use versus sale to customer as ITSM, Reasonable Split Needed, Assumed 50/50 for  demonstration purposes.');
insert into cost_general (name, allocation, six_month_avg, notes) values ('Telecom', 'SP', 43290.92, 'Reasonable Split Needed, Assumed 50/50 for this demonstration purposes.');
insert into cost_general (name, allocation, six_month_avg, notes) values ('Business Service', 'MS', 81980.32, 'Requires Data Scrub of each individual item. Review SOW’s and work with team. Phase 2, Backup 25k, Security 21K, Hosted Voice, 10K, Hosted Exchange 9K');
insert into cost_general (name, allocation, six_month_avg, notes) values ('SPLA', 'BL', 79807.92, 'Standard Cost per License');
insert into cost_general (name, allocation, six_month_avg, notes) values ('Equipment Lease', 'Equip', 19940.07, 'Handle within/same method as Equipment Tables');
insert into cost_general (name, allocation, six_month_avg, notes) values ('Services', 'SP', 53666.67, 'Ideally Standard Labor Rates - Addressed with Labor');
insert into cost_general (name, allocation, six_month_avg, notes) values ('Cloud Maintenance', 'CL', 64935.27, 'May need to split some of these costs between LEC/MS - Assumed 100% LEC for demonstration purposes.');
insert into cost_general (name, allocation, six_month_avg, notes) values ('MS Maintenance', 'MS', 2046.40, 'May need to split some of these costs between LEC/MS - Assumed 100% MS for demonstration purposes.');
insert into cost_general (name, allocation, six_month_avg, notes) values ('Cloud Variable', 'CL', 2046.40, 'Misc. Expenses');
insert into cost_general (name, allocation, six_month_avg, notes) values ('MS Variable', 'MS', 6187.79, 'Misc. Expenses');

insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MS', .144, 34.73);
insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MSLEC', .144, 48.28);
insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MSMGR', .004, 55.5);
insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MSOH', .008, 38.03);
insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MSSDM', .025, 42.18);
insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MST1', .424, 16.30);
insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MST2', .312, 23.99);
insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MST3', .164, 41.72);
insert into labor_input (dt_code, utilization, avg_unb_rate) values ('MST4', .254, 50.94);
*/
