-- SI_SP49
insert into scheduled_task (name, code, description, enabled) values ('M365 New Commerce Device Sync', 'pricing_m365nc_sync', 'Task to Sync M365 NC devices to SI', 1);
alter table device add column term_duration varchar(20);
alter table device add column billing_plan varchar(50);
alter table device add column segment varchar(50);

create table microsoft_price_list_nc_product (
    id bigint not null auto_increment,
    microsoft_price_list_id bigint not null,
    foreign key (microsoft_price_list_id) references microsoft_price_list(id) on delete cascade,
    product_title varchar(1000) not null,
    product_id varchar(100) not null,
    sku_title varchar(1000) not null,
    sku_description varchar(5000) not null,
    publisher varchar(500) not null,
    term_duration varchar(50) not null,
    billing_plan varchar(50) not null,
    unit_price Decimal (19,2) not null default 0.,
    erp_price Decimal (19,2) not null default 0.,
    tags varchar(500),
    segment varchar(50) not null,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    effective_start_date timestamp,
    effective_end_date timestamp,
    primary key (id)
) ENGINE=InnoDB;

-- SI_SP48
alter table contract add column file_path varchar(500);
alter table contract_update add column file_path varchar(500);

alter table contract add column renewal_status varchar(50);
alter table contract add column renewal_change Decimal (19,2) not null default 0.;
alter table contract add column renewal_notes varchar(500) after renewal_change;

create table contract_personnel (
    id bigint not null auto_increment,
    contract_id bigint not null,
    foreign key (contract_id) references contract(id),
    user_id bigint not null,
    foreign key (user_id) references users(id),
    type varchar(50) not null,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table customer_personnel (
    id bigint not null auto_increment,
    customer_id bigint not null,
    foreign key (customer_id) references customer(id),
    user_id bigint not null,
    foreign key (user_id) references users(id),
    type varchar(50) not null,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;


-- SI_SP47
alter table device modify column alt_id varchar(100);
alter table device modify column part_number varchar(255);

insert into scheduled_task (name, code, description, enabled) values ('M365 Device Sync', 'pricing_m365_sync', 'Task to Sync M365 devices to SI', 1);
insert into scheduled_task (name, code, description, enabled) values ('O365 Device Sync', 'pricing_o365_sync', 'Task to Sync O365 devices to SI', 1);
insert into scheduled_task (name, code, description, enabled) values ('Azure M365 Invoice Sync', 'azure_m365_sync', 'Task to Sync M365 invoices to bill for them in SI', 1);

create table microsoft_price_list (
    id bigint not null auto_increment,
    type varchar(50) not null,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    month date not null,
    primary key (id)
) ENGINE=InnoDB;

create table microsoft_price_list_product (
    id bigint not null auto_increment,
    microsoft_price_list_id bigint not null,
    foreign key (microsoft_price_list_id) references microsoft_price_list(id) on delete cascade,
    offer_name varchar(1000) not null,
    offer_id varchar(100) not null,
    license_agreement_type varchar(50) not null,
    purchase varchar(50) not null,
    secondary_license_type varchar(50) not null,
    end_customer_type varchar(50) not null,
    list_price Decimal (19,2) not null default 0.,
    erp_price Decimal (19,2) not null default 0.,
    material varchar(20),
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;


alter table pricing_sheet_product add column discount Decimal (19,2) after removal_revenue;
alter table pricing_sheet_product add column erp_price Decimal (19,2) after removal_revenue;
alter table pricing_sheet_product add column subscription_start_date date after discount;
alter table pricing_sheet_product add column subscription_end_date date after subscription_start_date;
alter table pricing_sheet_product add column unit_count int not null default 0 after subscription_end_date;
alter table pricing_sheet_product add column previous_unit_count int not null default 0 after unit_count;

create table microsoft_365_subscription_config (
    id bigint not null auto_increment,
    contract_id bigint not null,
    foreign key (contract_id) references contract(id),
    device_id bigint not null,
    foreign key (device_id) references device(id),
    service_id bigint not null,
    foreign key (service_id) references service(id),
    tenant_id varchar(255) not null,
    type varchar(50) not null default 'M365',
    support_type varchar(50) not null,
    support_flat_fee Decimal (19,2) not null default 0.,
    support_percent Decimal (19,2) not null default 0.,
    active tinyint(1) not null default 1,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

alter table contract_service add column microsoft_365_subscription_config_id bigint after contract_service_subscription_id;
alter table contract_service add foreign key (microsoft_365_subscription_config_id) references microsoft_365_subscription_config(id);

alter table microsoft_365_subscription_config add column device_id bigint not null after contract_id;
alter table microsoft_365_subscription_config add foreign key (device_id) references device(id);
alter table microsoft_365_subscription_config add column service_id bigint not null after device_id;
alter table microsoft_365_subscription_config add foreign key (service_id) references service(id);


-- Hotfix (changing length of note column)
ALTER TABLE contract_update MODIFY COLUMN note VARCHAR(2000);

-- Hotfix (changing length of note column)
ALTER TABLE contract_service MODIFY COLUMN note VARCHAR(2000);

-- SI_SP46.5
alter table customer add column azure_customer_id varchar(255);

-- SI_SP46.4
alter table contract_update add column onetime_price Decimal (19,2) default 0.;
alter table contract_update add column recurring_price Decimal (19,2) default 0.;

-- SI_SP46.1

-- SI_SP46
-- updates, fixes to contract_service_subscription table

-- turn on the monthly job
update scheduled_task set enabled = true where code = 'azure_monthly_billing_sync';
-- A JBT customer Id was set to a subscription Id
update contract_service_subscription set customer_id = '91c22079-02ce-47e8-bb5b-c93a3d5f1a78' where customer_id = '06626DDA-F4F0-4BE1-996A-5E0B330182CD';
-- DuPage: customerId is subtly wrong in the contract_service_subscription table
update contract_service_subscription set customer_id = 'd3678ef5-c51b-42ec-aa28-cb7fc6afd263' where customer_id = 'd3678ef5-c516-42ec-aa28-cb7fc6afd263';
-- delete Beeline subscriptions in CSS table that are not in Azure
-- or should they just be end dated??
update contract_service_subscription set end_date = '2020-03-31' where name = 'ger-gtwy' and subscription_id = 'a44f2148-3f81-4dbf-8321-28d341e6a1cd';
update contract_service_subscription set end_date = '2020-03-31' where name = 'ger-npd' and subscription_id = '3d16b685-1fb7-4920-a7b9-691971552c9f';
update contract_service_subscription set end_date = '2020-03-31' where name = 'ger-prd' and subscription_id = '77c8a247-4e99-4c18-b445-37626ae14bee';
update contract_service_subscription set end_date = '2020-03-31' where name = 'glb-gtwy' and subscription_id = '8714c94f-9c37-401b-86a6-b762bd844fc1';
update contract_service_subscription set end_date = '2020-03-31' where name = 'glb-npd' and subscription_id = '37948b82-8beb-404b-b64f-06ad14a265a0';
update contract_service_subscription set end_date = '2020-03-31' where name = 'glb-prd' and subscription_id = '5e383420-1915-4c61-98bb-e8d891c2bf26';
delete from contract_service_subscription where subscription_id = '1d3c824962a3';
update contract_service_subscription set end_date = '2020-03-31' where name = 'root-secops' and subscription_id = 'c40fc505-7ef8-48d0-beb1-1ad31231db6a';
-- add missing Beeline subscriptions (exist in Azure but not in CSS table)
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (762, 1811, 1379, '3289e021-493f-463c-bfc1-feb3ba494685', 'Beeline Visual Studio Pro (Azure)','14143fe4-bc04-4454-abc5-6dc2a12b2f1c', '2020-03-01', '2025-02-28', 'mavenj', 'cspazure');
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (762, 1811, 1379, '3289e021-493f-463c-bfc1-feb3ba494685', 'de-prod-classic','776e5390-f090-4cdd-8b5a-2c289698204c', '2020-03-01', '2025-02-28', 'mavenj', 'cspazure');
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (762, 1811, 1379, '3289e021-493f-463c-bfc1-feb3ba494685', 'de-prod-digitalexperience','374e688d-35b8-4153-9c11-f02f7c5ba9fa', '2020-03-01', '2025-02-28', 'mavenj', 'cspazure');
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (762, 1811, 1379, '3289e021-493f-463c-bfc1-feb3ba494685', 'de-prod-esignature','d262742e-981e-47e9-863a-b1e06ba6fb39', '2020-03-01', '2025-02-28', 'mavenj', 'cspazure');
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (762, 1811, 1379, '3289e021-493f-463c-bfc1-feb3ba494685', 'us-prod-classic','4df7c9d7-6f24-4dc1-8160-b5722c8b6c56', '2020-03-01', '2025-02-28', 'mavenj', 'cspazure');
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (762, 1811, 1379, '3289e021-493f-463c-bfc1-feb3ba494685', 'us-prod-digitalexperience','d572bae6-fa12-4ce1-a147-c6a2fb0c177c', '2020-03-01', '2025-02-28', 'mavenj', 'cspazure');
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (762, 1811, 1379, '3289e021-493f-463c-bfc1-feb3ba494685', 'us-prod-esignature','70cb5c5f-fbf5-4df4-aeef-21935d1a7921', '2020-03-01', '2025-02-28', 'mavenj', 'cspazure');
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (762, 1811, 1379, '3289e021-493f-463c-bfc1-feb3ba494685', 'us-productdesign','c40fc505-7ef8-48d0-beb1-1ad31231db6a', '2020-03-01', '2025-02-28', 'mavenj', 'cspazure');

-- Clayton customer active but end_date in CSS is in the past? switching it to a different contract
-- also, the Clayton susbcription id has a trailing Space that needs removing
update contract_service_subscription set contract_id = 763, end_date = '2025-02-28', subscription_id = '8837930A-D92C-44DB-9E34-E3CA97B36954' where customer_id = '3a9fa694-d5f2-4738-bf31-8388e32fdfe4' and subscription_id = '8837930A-D92C-44DB-9E34-E3CA97B36954 ';

-- add G&W Electric
insert into contract_service_subscription (contract_id, device_id, service_id, customer_id, name, subscription_id, start_date, end_date, created_by, subscription_type) values (821, 1811, 1379, '3f944c04-0f59-41d8-a423-716a63f07edd', 'G&W Azure Primary','29729aa5-e1fc-4189-b2f1-4aa52e9b9a1d', '2020-09-01', '2025-02-28', 'mavenj', 'cspazure');

-- Micro Focus BS
-- end date subscriptions not found in Azure
update contract_service_subscription set end_date = '2020-03-31' where customer_id = '67cc942d-10f0-4325-9254-6f64b4e9271c';
-- seems like this subscription has an incorrect customer_id associated with it... all the others are the "67cc942d-" id
update contract_service_subscription set customer_id = '67cc942d-10f0-4325-9254-6f64b4e9271c', end_date = '2020-03-31' where customer_id = 'b5deacb3-04a3-40e0-ba72-3d2e1bcdbf74';

-- SI_SP45 changes to pull in Azure Plan Recurring Costs
insert into device (part_number, description, created_by, product_id, default_osp_id, device_type) values ('MSFT-CSP-AZRPLN-IAAS', 'Microsoft Azure Plan IaaS', 'system', 498, 90004, 'cspazureplan');


-- adding new non-portfolio services for dedicated gear and DCaaS
insert into service (osp_id, version, name, description, business_model, active, created_by) values (90009, 1.0, 'Non-Portfolio Products (Reserved Gear)', 'Custom Service for Non-Portfolio Products that are Reserved Gear', 'Cloud', true, 'mfoster');
insert into service (osp_id, version, name, description, business_model, active, created_by) values (90010, 1.0, 'Non-Portfolio Products (DCaaS)', 'Custom Service for Non-Portfolio Products that are DCaaS', 'Cloud', true, 'mfoster');


-- adding this just as a way to keep track of description changes in the DB. it's not connected to the app in any other way right now. we may display it in the future.
create table device_change_log (
    id bigint not null auto_increment,
    device_id bigint not null,
    foreign key (device_id) references device (id) on delete cascade,
    old_description varchar(255),
    new_description varchar(255) not null,
    updated timestamp not null default current_timestamp,
    updated_by varchar(50) not null default 'system',
    primary key (id)
) ENGINE=InnoDB;

-- making unit_cost table "customer aware" finally and getting rid of service_count, since we've switched to devices
drop table unit_cost;
create table unit_cost (
    id bigint not null auto_increment,
    customer_id bigint,
    foreign key (customer_id) references customer (id),
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    total_cost Decimal (19,2) default 0.,
    total_labor Decimal (19,2) default 0.,
    device_total_units int default 0,
    applied_date date not null,
    unique key uk_custid_expid_applied (customer_id, expense_category_id, applied_date),
    primary key (id)
) ENGINE=InnoDB;

alter table device_expense_category add column allocation_category tinyint(1) not null default 0;

-- JIRA task SI-521
insert into scheduled_task (name, code, description, enabled) values ('Data Warehouse CI Unit Cost Sync', 'data_warehouse_ci_cost_sync', 'Task to UPDATE CI Cost column for a specified month in the Data Warehouse db.', 1);

-- JIRA task SI-503
insert into scheduled_task (name, code, description, enabled) values ('Unit Cost Count Update', 'update_unit_cost_totals', 'Updates all of the Unit Cost table service and device counts', '1');

-- cloud_costs
alter table device add column pricing_sync_enabled tinyint(1) not null default 0;
alter table device add column catalog_recurring_cost Decimal (19,2) default 0.;
alter table device add column catalog_recurring_price Decimal (19,2) default 0.;
insert into scheduled_task (name, code, description, enabled) values ('Pricing Tool Device Sync', 'pricing_device_sync', 'Matches Devices from Pricing Tool and Updates Standard Cost/Price', '1');

alter table cost_item add column cost_allocation_lineitem_id_ref bigint;
alter table cost_item add column cost_subtype varchar(50) after cost_type;
alter table expense modify column name varchar(500);
alter table cost_item modify column name varchar(500);
alter table cost_item modify column description varchar(750);

insert into scheduled_task (name, code, description, enabled) values ('Device Unit Count Update', 'update_device_unit_count', 'Updates all of the device unit count for each device in device_unit_count', '1');

alter table device add column cost_allocation_option tinyint(1) not null default 0;

alter table cost_allocation_lineitem drop column specific_total;
alter table cost_allocation_lineitem add column specific_amount Decimal (19,2) default 0. after specific_allocation;
alter table cost_allocation_lineitem add column infrastructure_note varchar(255) after specific_amount;

--only need these 3 lines if you have already created unallocated_expense
--alter table unallocated_expense drop foreign key unallocated_expense_ibfk_2;
--alter table unallocated_expense drop column cost_allocation_id;
--alter table unallocated_expense add column cost_allocation_ref_id bigint after po_number;

create table device_unit_count (
	id bigint not null auto_increment,
	device_id bigint not null,
    foreign key (device_id) references device (id),
	unit_count int not null default 0,
	created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    month date not null,
	primary key (id)
) ENGINE=InnoDB;

create table cost_allocation (
	id bigint not null auto_increment,
	multi_tenant_total Decimal (19,2) default 0.,
	rent_total Decimal (19,2) default 0.,
	dedicated_total Decimal (19,2) default 0.,
	specific_total Decimal (19,2) default 0.,
	status varchar(50),
	created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    month date not null,
	primary key (id)
) ENGINE=InnoDB;

create table cost_allocation_lineitem (
	id bigint not null auto_increment,
	cost_allocation_id bigint not null,
    foreign key (cost_allocation_id) references cost_allocation (id),
    osp_id bigint not null,
    foreign key (osp_id) references service (osp_id),
    device_id bigint not null,
    foreign key (device_id) references device (id),
    multi_tenant_allocation Decimal (19,2) default 0.,
	multi_tenant_amount Decimal (19,2) default 0.,
	rent_allocation Decimal (19,2) default 0.,
	rent_amount Decimal (19,2) default 0.,
	specific_allocation Decimal (19,2) default 0.,
	specific_amount Decimal (19,2) default 0.,
	infrastructure_note varchar(255),
	units int not null default 0,
	cost_model_price Decimal (19,2) default 0.,
	created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

create table unallocated_expense (
    id bigint not null auto_increment,
    alt_id varchar(20),
    name varchar(500) not null,
    description varchar(750),
    amount Decimal(19,2) default 0.,
    quantity int not null default 1,
    osp_id bigint not null,
    foreign key (osp_id) references service (osp_id),
    vendor varchar(255),
    po_number varchar(255),
    cost_allocation_ref_id bigint,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    month date not null,
    primary key (id)
) ENGINE=InnoDB;

alter table contract_service add column hidden tinyint(1) not null default 0 after status;
alter table contract_service add column reason varchar(50) after hidden;

alter table device_relationship drop primary key, add primary key(device_id, related_device_id, relationship);

create table device_expense_category (
    device_id bigint not null,
    foreign key (device_id) references device (id),
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    quantity int not null,
    allocation_category tinyint(1) not null default 0,
    primary key (device_id, expense_category_id)
) ENGINE=InnoDB;

create table device_property (
	id bigint not null auto_increment,
	device_id bigint not null,
	foreign key (device_id) references device (id) on delete cascade,
	type varchar(50) not null,
	unit_count int,
	str_value varchar(255),
	unit_type varchar(10),
	created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

create table device_relationship (
	device_id bigint not null,
	foreign key (device_id) references device (id) on delete cascade,
	related_device_id bigint not null,
	foreign key (related_device_id) references device (id) on delete cascade,
	relationship varchar(50) not null default 'optional',
	spec_units int not null default 0,
	sorder int not null default 0,
	primary key (device_id, related_device_id)
) ENGINE=InnoDB;

alter table contract_service add column parent_id bigint after id;
alter table contract_service add foreign key (parent_id) references contract_service(id) on delete cascade;
alter table device add column units varchar(20) after device_type;
alter table unit_cost add column device_total_units int default 0 after service_total_units;

-- SI_SP43.6
insert into scheduled_task (name, code, description, enabled) values ('Data Warehouse Cost Sync', 'data_warehouse_cost_sync', 'Task to push Costs for the the month to the Data Warehouse db.', 1);

insert into scheduled_task (name, code, description, enabled) values ('Data Warehouse Contract Sync', 'data_warehouse_contract_sync', 'Task to push SOWs for the the month to the Data Warehouse db.', 1);
insert into scheduled_task (name, code, description, enabled) values ('Data Warehouse PCR Sync', 'data_warehouse_pcr_sync', 'Task to push PCRs for the the month to the Data Warehouse db.', 1);


-- SI_SP43.1
alter table users add column name varchar(255) not null after password;
alter table users add column title varchar(255) after name;

alter table device add column require_unit_count tinyint(1) not null default 0;


-- add the disabled field to control Service visibility in lists
alter table service add column disabled tinyint(1) default 0 after active;
-- Set all the current Professional Services to disabled
update service set disabled = true, business_model = 'Other' where name like 'Architecture & Design%';
update service set disabled = true, business_model = 'Other' where name like 'Assessment%';
update service set disabled = true, business_model = 'Other' where name like 'Implementation%';
update service set disabled = true, business_model = 'Other' where name like 'Upgrades%';
update service set disabled = true, business_model = 'Other' where name like 'Professional Support%';
update service set disabled = true, business_model = 'Other' where name like 'Migrations%';
update service set disabled = true, business_model = 'Other' where name like 'Health Check%';
update service set disabled = true, business_model = 'Other' where name like 'Training%';
update service set disabled = true, business_model = 'Other' where name like 'Usage & Adoption%';

-- check carefully that all other Services have the correct business_model
select id, osp_id, name, business_model, active, disabled from service order by business_model, name, active, disabled
-- some Managed Services were set to Other...
update service set business_model = 'Managed' where name like 'Assisted Support%';
update service set business_model = 'Managed' where name like 'Operate%';
update service set business_model = 'Managed' where name like 'Response%';
update service set business_model = 'Managed' where name like 'Service Desk%';
-- these are Lifecycle and should already be Other...
update service set business_model = 'Other' where name like 'Maintenance Assist%';
update service set business_model = 'Other' where name like 'Support & Maintenance%';
update service set business_model = 'Other' where name like 'Vendor Direct Maintenance%';

-- SI_SP43
alter table cost_item add column device_id bigint after aws_subscription_id;
alter table cost_item add column spla_cost_catalog_id bigint after device_id;
insert into scheduled_task (name, code, description, enabled) values ('Data Warehouse CI Sync', 'data_warehouse_ci_sync', 'Task to push CIs for the the month to the Data Warehouse db.', 1);

create table service_align_log (
    id bigint not null auto_increment,
    device_id bigint,
    foreign key (device_id) references device(id),
    new_service_id bigint not null,
    foreign key (new_service_id) references service(id),
    change_details text,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

-- SI_SP42

-- double check the id of the "Reserved" part-number record
update device set device_type = 'cspreserved' where id = 2126;

-- SI_SP41
alter table cost_item add column cost_type varchar(50) not null default 'general' after aws_subscription_id;

create table spla_cost_catalog (
    id bigint not null auto_increment,
    name varchar(500),
    alt_id varchar(100),
    cost Decimal (19,2) default 0.,
    vendor varchar(50) not null default 'other',
    type varchar(50) not null default 'single',
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category(id),
    active tinyint(1) not null default 1,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

alter table device add column spla_cost_catalog_id bigint;
alter table device add foreign key (spla_cost_catalog_id) references spla_cost_catalog(id);
alter table device drop foreign key device_ibfk_2;
alter table device drop column spla_cost_catalog_id;

create table device_spla_cost_catalog (
  device_id bigint not null,
  foreign key (device_id) references device(id),
  spla_cost_catalog_id bigint not null,
  foreign key (spla_cost_catalog_id) references spla_cost_catalog(id),
  primary key (device_id, spla_cost_catalog_id)
) ENGINE=InnoDB;

insert into scheduled_task (name, code, description, enabled) values ('Generate SPLA Costs', 'spla_monthly_cost_generation', 'Task to Generate SPLA Costs for Active Contracts.', '0');

-- SI_SP40
alter table cost_item add column aws_subscription_id varchar(50) after azure_subscription_id;

alter table customer drop column osm_pcr_request_enabled;
alter table contract_service_device drop column osm_removal_enabled;
alter table device add column pricing_sheet_enabled tinyint(1) not null default 0;
alter table device drop column osm_sync_enabled;

alter table pricing_sheet drop column osm_sync_enabled;
alter table pricing_sheet add column active tinyint(1) not null default 1;

alter table pricing_sheet_product add column status varchar(20) not null default 'active';
alter table pricing_sheet_product add column status_message text;
alter table pricing_sheet_product add column manual_override tinyint(1) not null default 0;
alter table pricing_sheet_product add column notes varchar(5000);

insert into scheduled_task (name, code, description, enabled) values ('Generate Pricing Sheets', 'generate_pricing_sheets_sync', 'Task to Generate Pricing Sheets for Active Contracts.', '1');

-- will want to run this to enable the first batch of devices for pricing sheets
update device set pricing_sheet_enabled = 1 where activate_sync_enabled = 1;

-- SI_SP39
alter table cost_item add column azure_customer_name varchar(255) after location_id;
alter table cost_item add column azure_invoice_id varchar(50) after azure_customer_name;
alter table cost_item add column azure_subscription_id varchar(50) after azure_invoice_id;
insert into scheduled_task (name, code, description, enabled) values ('Azure Monthly Customer Cost Import', 'azure_monthly_cost_import', 'Imports invoiced costs from Azure into Service Insight', '1');

alter table customer add column alt_name varchar(255);

insert into service (osp_id, version, name, description, business_model, active, created_by) values (90006, 1.0, 'Non-Portfolio Products (Managed Services)', 'Custom Service for Non-Portfolio Products that are Managed', 'Managed', true, 'mfoster');
insert into service (osp_id, version, name, description, business_model, active, created_by) values (90007, 1.0, 'Non-Portfolio Products (Cloud)', 'Custom Service for Non-Portfolio Products that are Cloud', 'Cloud', true, 'mfoster');
insert into service (osp_id, version, name, description, business_model, active, created_by) values (90008, 1.0, 'Non-Portfolio Products (Customer Specific)', 'Custom Service for Non-Portfolio Products that are Customer Specific', 'Other', true, 'mfoster');


-- SI_SP38
-- these are to remove the "on delete cascade" from these foreign keys. I noticed them on there randomly and don't think they should be there.
SELECT * FROM information_schema.REFERENTIAL_CONSTRAINTS; --find the constraints on contract_service_subscription to modify;
alter table contract_service_subscription drop foreign key contract_service_subscription_ibfk_1;
alter table contract_service_subscription drop foreign key contract_service_subscription_ibfk_2;
alter table contract_service_subscription drop foreign key contract_service_subscription_ibfk_3;
alter table contract_service_subscription add foreign key(contract_id) references contract(id);
alter table contract_service_subscription add foreign key(device_id) references device(id);
alter table contract_service_subscription add foreign key(service_id) references service(id);



-- SI_SP37
alter table device add column activate_add_business_service tinyint(1) not null default 0;
alter table device add column activate_sync_enabled tinyint(1) not null default 0;
alter table device add column is_ci tinyint(1) not null default 0;

-- create new pseudo services
insert into service (osp_id, version, name, description, business_model, active, created_by) values (90003, 1.0, 'CSP - O365', 'Custom Service for CSP - Office 365', 'CSP', true, 'poneil');
insert into service (osp_id, version, name, description, business_model, active, created_by) values (90004, 1.0, 'CSP - Azure', 'Custom Service for CSP - Azure', 'CSP', true, 'poneil');
insert into service (osp_id, version, name, description, business_model, active, created_by) values (90005, 1.0, 'CSP - AWS', 'Custom Service for CSP - AWS', 'CSP', true, 'poneil');
update service set business_model = 'Other' where business_model is null;

-- update labor records for 'Other' business model
update raw_labor_data set business_model = 'Other' where service_name is not null and business_model is null and osp_id is not null;
update grouped_labor_data set business_model = 'Other' where service_name is not null and business_model is null and osp_id is not null;
update derived_labor_data set business_model = 'Other' where service_name is not null and business_model is null and osp_id is not null;

-- CHECK SERVICE IDs...
select * from service where active = true and osp_id in (90001, 90002, 90003, 90004, 90005);

-- relate device to new Services by device_type
update device set default_osp_id = 90003 where device_type = 'cspO365';
update device set default_osp_id = 90004 where device_type = 'cspazure';
update device set default_osp_id = 90005 where device_type = 'aws';

-- relate contract services and subscriptions that match these devices but referenced less refined pseudo service
-- updates CS for o365
update contract_service cs
inner join contract_service_device csd on cs.id = csd.contract_service_id
inner join device d on csd.device_id = d.id
set cs.service_id = 1368
where cs.service_id in (575, 683)
and d.device_type = 'cspo365';
update contract_service_subscription set service_id = 1368 where service_id in (575, 683) and device_id in (select id from device where device_type = 'cspO365');

-- updates CS for azure
update contract_service cs
inner join contract_service_device csd on cs.id = csd.contract_service_id
inner join device d on csd.device_id = d.id
set cs.service_id = 1369
where cs.service_id in (575, 683)
and d.device_type = 'cspazure';
update contract_service_subscription set service_id = 1369 where service_id in (575, 683) and device_id in (select id from device where device_type = 'cspazure');

-- updates CS for AWS
update contract_service cs
inner join contract_service_device csd on cs.id = csd.contract_service_id
inner join device d on csd.device_id = d.id
set cs.service_id = 1370
where cs.service_id in (575, 683)
and d.device_type = 'aws';
update contract_service_subscription set service_id = 1370 where service_id in (575, 683) and device_id in (select id from device where device_type = 'aws');

-- chronos_payrollcode
alter table raw_labor_data add column tier_code varchar(255) after tier_name;
create index idx_tier_code on raw_labor_data (tier_code);

update raw_labor_data rd
inner join labor_rate lr on rd.tier_name = lr.name
set rd.tier_code = lr.code where rd.tier_code is null;

-- SI_SP36
alter table contract_service drop foreign key contract_service_ibfk_3;
alter table contract_service add foreign key (contract_group_id) references contract_group(id);

alter table contract_adjustment drop foreign key contract_adjustment_ibfk_3;
alter table contract_adjustment add foreign key (contract_group_id) references contract_group(id);


--remove the foreign key on authorities
alter table authorities drop foreign key fk_authorities_users;

--remove the primary from users and add it back as in id
ALTER TABLE users DROP PRIMARY KEY;
ALTER TABLE users ADD id bigint PRIMARY KEY AUTO_INCREMENT first;

--add the primary to authorities and the user_id reference
alter table authorities add column id bigint PRIMARY KEY AUTO_INCREMENT first;
alter table authorities add column user_id bigint after id;

--set all the foreign keys in the authorities table
update authorities auth join users u on auth.username = u.username set auth.user_id = u.id;

--set the foreign key and ensure it's not null
alter table authorities add constraint fk_user_authorities foreign key (user_id) references users(id);
alter table authorities modify column user_id bigint not null;

--drop the username field from authorities
alter table authorities drop index ix_auth_username;
alter table authorities drop column username;
--add new unique index for user_id / authority
ALTER TABLE authorities ADD UNIQUE ix_auth_user_id(user_id, authority);


-- SI_SP35
alter table contract_service drop foreign key contract_service_ibfk_4;
alter table contract_service change column contract_service_azure_id contract_service_subscription_id bigint;
rename table contract_service_azure to contract_service_subscription;
alter table contract_service add foreign key (contract_service_subscription_id) references contract_service_subscription(id);
alter table contract_service_subscription add column subscription_type varchar(20) not null default 'cspazure';

insert into location (name, description, created, created_by, alt_name) values ('AWS', 'Amazon Web Service Datacenters', now(), 'mfoster', 'AWS');
insert into device (part_number, description, created, created_by, product_id, default_osp_id, device_type) values ('AMZN-AWS-IAAS', 'Amazon Web Services IaaS', now(), 'mfoster', 400, 90002, 'aws');

alter table contract_service_subscription add column customer_type varchar(20);
insert into scheduled_task (name, code, description, enabled) values ('AWS Monthly Billing Sync', 'aws_monthly_billing_sync', 'Task to Sync the generate the contract services for AWS.', '1');

alter table azure_uplift modify column code varchar(50) not null;
insert into azure_uplift (code, description, uplift, uplift_type, active, created, created_by, start_date) values ('aws_new_cust_prdct_uplft', 'A margin added to the cost of the product for Net-New AWS Customers.', 0.08, 'percentage', 1, now(), 'mfoster', '2018-04-01 00:00:00');
insert into azure_uplift (code, description, uplift, uplift_type, active, created, created_by, start_date) values ('aws_exstng_cust_prdct_uplft', 'A margin added to the cost of the product for exising customers brought into our account.', 0.03, 'percentage', 1, now(), 'mfoster', '2018-04-01 00:00:00');
rename table azure_uplift to subscription_uplift;

-- SI_SP34
alter table contract add column service_start_date timestamp null;
alter table customer add column osm_pcr_request_enabled tinyint(1) not null default 0;
alter table contract_service_device add column osm_removal_enabled tinyint(1) not null default 0;

alter table raw_labor_data add column subtask_description varchar(255) after task_description;
alter table grouped_labor_data add column subtask_description varchar(255) after task_description;
alter table derived_labor_data add column subtask_description varchar(255) after task_description;

create index idx_task_chronos_subtask on raw_labor_data (task_description, subtask_description);
create index idx_gr_task_chronos_subtask on grouped_labor_data (task_description, subtask_description);
create index idx_d_chronos_subtask on derived_labor_data (task_description, subtask_description);

create table chronos_task_mapping (
  task_description varchar(255),
  subtask_description varchar(255),
  expense_category_id int,
  unique key uk_expCatId (expense_category_id),
  foreign key (expense_category_id) references expense_category(id) on delete cascade,
  primary key (task_description, subtask_description)
) ENGINE=InnoDB;

insert into chronos_task_mapping values ('Cloud Infrastructure', 'Backup', 44);
insert into chronos_task_mapping values ('Cloud Infrastructure', 'Facilities', 51);
insert into chronos_task_mapping values ('Cloud Infrastructure', 'Network', 28);
insert into chronos_task_mapping values ('Cloud Infrastructure', 'AIX', 25);
insert into chronos_task_mapping values ('Cloud Infrastructure', 'IBM i', 22);
insert into chronos_task_mapping values ('Cloud Infrastructure', 'Storage', 41);
insert into chronos_task_mapping values ('Cloud Infrastructure', 'Software', 57);
insert into chronos_task_mapping values ('Cloud Infrastructure', 'X86', 17);
insert into chronos_task_mapping values ('Managed Services Infrastructure', 'Monitoring', 1);
insert into chronos_task_mapping values ('Managed Services Infrastructure', 'Patching', 6);
insert into chronos_task_mapping values ('Managed Services Infrastructure', 'Tools', 11);

-- SI_SP33
alter table expense_category add column labor_split Decimal (10,3) not null default 0. after units;
alter table grouped_labor_data add column onboarding tinyint(1) not null default 0;
alter table derived_labor_data add column onboarding tinyint(1) not null default 0;
alter table contract_adjustment add column status varchar(50) not null default 'active';
insert into scheduled_task (name, code, description, enabled) values ('UMP CI Sync', 'ump_ci_sync', 'Task to Sync additional CI data from the UMP database. Set to run daily.', '1');

create table contract_service_detail (
    contract_service_id bigint not null,
    foreign key (contract_service_id) references contract_service(id) on delete cascade,
    location varchar(255),
    operating_system varchar(100),
    cpu_count int,
    memory_gb Decimal (19,2),
    storage_gb Decimal (19,2),
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (contract_service_id)
) ENGINE=InnoDB;

alter table contract_service_device add column location_id int;
alter table contract_service_device add foreign key (location_id) references location(id);

alter table location add column alt_name varchar(255);
update location set alt_name = 'Logicalis - IO-Ohio' where id = 2;
update location set alt_name = 'Logicalis - LEC West' where id = 3;
insert into location (name, description, created, created_by, alt_name) values ('Azure', 'Microsoft Azure Datacenters', now(), 'mfoster', 'Azure');

alter table location add column is_displayed_revenue tinyint not null default 0;

-- SI_SP31
insert into scheduled_task (name, code, description, enabled) values ('Labor Unit Cost', 'labor_unit_cost', 'Adds the labor component for the last month to Expense Category Unit Costs. Set to run on the first of the month... at 1:45am.', '1');
drop table if exists unit_cost;
create table unit_cost (
    id bigint not null auto_increment,
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    total_cost Decimal (19,2) default 0.,
    total_labor Decimal (19,2) default 0.,
    total_units int default 0,
    service_total_units int default 0,
    applied_date date not null,
    unique key uk_expid_applied (expense_category_id, applied_date),
    primary key (id)
) ENGINE=InnoDB;
-- SI_SP30
create table device_merge_log (
    id bigint not null auto_increment,
    old_id bigint not null,
    old_part_number varchar(255),
    old_description varchar(255),
    new_id bigint not null,
    new_part_number varchar(255) not null,
    new_description varchar(255) not null,
    change_details varchar(20000),
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

-- EITHER re-create (if re-importing) or alter the tables
alter table raw_labor_data add column worker varchar(255);
create index idx_worker on raw_labor_data (worker);

-- SI_SP29

-- seed the import log table...
insert into labor_import_log (record_count, inserted_date) values (0, '2009-01-01 03:00:00');

-- EITHER re-create (if re-importing) or alter the tables
alter table labor_rate add column addl_rate Decimal(10,2) not null default 0.;
alter table raw_labor_data add column addl_tier_rate Decimal(10,2);
alter table grouped_labor_data add column addl_labor_total Decimal(10,2) not null default 0.;
alter table derived_labor_data add column addl_labor_total Decimal(10,2) not null default 0.;
alter table indirect_labor_unit_cost add column addl_unit_cost Decimal (19,2) default 0.;

insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('MST1', 'Tier 1', null, 34.96, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('MST2', 'Tier 2', null, 45.24, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('MST3', 'Tier 3', null, 68.54, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('MST4', 'Tier 4', null, 100.80, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('Shift Leader', 'Shift Leader', null, 45.24, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('OMSSDM', 'SDM', null, 83.91, 1.02, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('OMSSDA', 'SDA', null, 83.91, 1.02, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('OSS-Tools', 'Development', null, 78.72, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('OMS400', 'Cloud Ops', null, 103.92, 1.40, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('Admin', 'Admin', null, 0.00, 0.00, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('Managed Teams', 'Managed Teams', null, 0.00, 0.00, 1.00, 'system');
insert into labor_rate (code, name, description, rate, addl_rate, rate_factor, created_by) values ('Managers', 'Managers', null, 0.00, 0.00, 1.00, 'system');

create table azure_meter (
    id varchar(255) not null,
    name varchar(255),
    category varchar(255),
    subcategory varchar(255),
    region varchar(50),
    unit varchar(50),
    included_quantity Decimal (19,6) default 0.,
    currency varchar(3),
    tax_included tinyint(1) not null default 0,
    locale varchar(10),
    effective_date timestamp null,
    primary key (id)
) ENGINE=InnoDB;

create table azure_meter_rate (
    azure_meter_id varchar(255) not null,
    foreign key(azure_meter_id) references azure_meter(id) on delete cascade,
    rate_key varchar(50) not null,
    value Decimal (19,6) default 0.,
    unique key uk_azuremeter_idkey (azure_meter_id, rate_key)
) ENGINE=InnoDB;

create table azure_offer_term (
    name varchar(255),
    discount Decimal (19,6) default 0.,
    azure_meter_id varchar(255),
    foreign key(azure_meter_id) references azure_meter(id) on delete cascade,
    currency varchar(3),
    locale varchar(10),
    effective_date timestamp null
) ENGINE=InnoDB;

create table azure_uplift (
    id bigint not null auto_increment,
    code varchar(20),
    description varchar(255),
    uplift Decimal (19,4) default 0.,
    uplift_type varchar(100) not null,
    active tinyint not null default 0,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    start_date timestamp not null,
    end_date timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    note varchar(500),
    primary key (id)
) ENGINE=InnoDB;

create table contract_service_azure (
    id bigint not null auto_increment,
    contract_id bigint not null, 
    foreign key(contract_id) references contract(id) on delete cascade,
    device_id bigint not null, 
    foreign key(device_id) references device(id) on delete cascade,
    service_id bigint not null, 
    foreign key(service_id) references service(id) on delete cascade,
    customer_id varchar(255),
    subscription_id varchar(255),
    name varchar(255),
    created timestamp not null default current_timestamp,
    updated timestamp null,
    start_date timestamp not null,
    end_date timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

alter table contract_service add column contract_service_azure_id bigint;
alter table contract_service add foreign key (contract_service_azure_id) references contract_service_azure(id) on delete cascade;

alter table device add column device_type varchar(100);
--TODO -- Update product_id
insert into device (part_number, description, created, created_by, product_id, default_osp_id, device_type) values ('MSFT-CSP-AZR-IAAS', 'Microsoft Azure IaaS', now(), 'mfoster', 400, 90002, 'cspazure');

insert into azure_uplift (code, description, uplift, uplift_type, active, created, created_by, start_date) values ('prdct_uplft', 'A product margin added to the cost of the product.', 0.10, 'percentage', 1, now(), 'mfoster', '2017-07-01 00:00:00');
insert into azure_uplift (code, description, uplift, uplift_type, active, created, created_by, start_date) values ('spprt_uplft', 'A support margin added to the cost of the product.', 0.08, 'percentage', 1, now(), 'mfoster', '2017-07-01 00:00:00');

insert into scheduled_task (name, code, description, enabled) values ('Azure Rate Card Sync', 'azure_rate_card_sync', 'Task to Sync the Azure Rate Card. Set to run nightly.', '1');
insert into scheduled_task (name, code, description, enabled) values ('Azure Monthly Billing Sync', 'azure_monthly_billing_sync', 'Task to Sync the generate the contract services for Azure.', '1');

-- SI_SP27
alter table pricing_sheet_product add column removal_revenue Decimal (19,2) default 0. after recurring_revenue;

-- SI_SP26
--SHOW CREATE TABLE cost_item; --to get foreign keys to drop
alter table cost_item drop foreign key cost_item_ibfk_1;
alter table cost_item drop column expense_type_id;
alter table asset_item drop foreign key asset_item_ibfk_1;
alter table asset_item drop column expense_type_id;
alter table expense drop foreign key expense_ibfk_1;
alter table expense drop column expense_type_ref_id;
alter table expense add column expense_type varchar(50) not null;
drop table expense_type;
drop table expense_type_ref;
--drop table asset_category; --drop this??

-- SI_SP25
insert into scheduled_task (name, code, description, enabled) values ('OSP Service Sync', 'osp_service_sync', 'Task to Sync OSP Services with the Service Insight Services', '1');

alter table device add column archived tinyint not null default 0;
alter table device add column osm_sync_enabled tinyint not null default 0;
alter table device add column default_osp_id bigint;
alter table device add foreign key (default_osp_id) references service(osp_id);

create table pricing_sheet (
    id bigint not null auto_increment,
    contract_id bigint not null, 
    foreign key(contract_id) references contract(id) on delete cascade,
    osm_sync_enabled tinyint not null default 0,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table pricing_sheet_product (
    id bigint not null auto_increment,
    pricing_sheet_id bigint not null, 
    foreign key(pricing_sheet_id) references pricing_sheet(id) on delete cascade,
    device_id bigint not null, 
    foreign key(device_id) references device(id) on delete cascade,
    service_id bigint not null, 
    foreign key(service_id) references service(id) on delete cascade,
    onetime_revenue Decimal (19,2) default 0.,
    recurring_revenue Decimal (19,2) default 0.,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    unique key uk_pricing_sheet_product (pricing_sheet_id, device_id),
    primary key (id)
) ENGINE=InnoDB;  

-- labor cost update
-- we have a new labor_data table
drop table if exists labor_data;
drop table if exists derived_labor_data;

-- obsolete these tables...
drop table if exists labor_input;
-- new labor rate table...
drop table if exists labor_rate;
-- asset??
drop table if exists asset;
-- asset_category??
drop table if exists asset_category;

drop table if exists service_expense_category;
create table service_expense_category (
    osp_id bigint not null,
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    quantity int not null,
    primary key (osp_id, expense_category_id)
) ENGINE=InnoDB;

drop table if exists unit_cost;
create table unit_cost (
    id bigint not null auto_increment,
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    total_cost Decimal (19,2) default 0.,
    total_units int not null,
    applied_date date not null,
    unique key uk_expid_applied (expense_category_id, applied_date),
    primary key (id)
) ENGINE=InnoDB;

-- chronos task rules
drop table if exists chronos_task_rule;
create table chronos_task_rule (
    id int not null auto_increment,
    labor_type varchar(20) not null,
    task_description varchar(255),
    unique key uk_laborTypeChronosTask (labor_type, task_description),
    rule varchar(50) not null,
    primary key (id)
) ENGINE=InnoDB;

insert into chronos_task_rule (labor_type, task_description, rule) values ('Administrative', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('SetUp', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('Sales', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('Operational', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('Internal', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, task_description, rule) values ('Internal', 'Cloud Infrastructure', 'Cloud');

drop table if exists labor_rate;
create table labor_rate (
    id int not null auto_increment,
    code varchar(20) not null,
    unique key uk_laborRateCode (code),
    name varchar(50) not null,
    description varchar(255),
    unique key uk_laborRateName (name),
    rate Decimal(10,2) not null default 0.,
    rate_factor Decimal(10,2) not null default 1.,
    created timestamp not null default current_timestamp,
    created_by varchar(50) not null default 'system',
    primary key (id)
) ENGINE=InnoDB;

-- ARE THESE RIGHT? COMPLETE?
insert into labor_rate (code, name, description, rate) values ('MST1', 'Tier 1', null, 36.19);
insert into labor_rate (code, name, description, rate) values ('MST2', 'Tier 2', null, 49.35);
insert into labor_rate (code, name, description, rate) values ('MST3', 'Tier 3', null, 73.67);
insert into labor_rate (code, name, description, rate) values ('MST4', 'Tier 4', null, 102.11);
insert into labor_rate (code, name, description, rate) values ('Shift Leader', 'Shift Leader', null, 49.35);
insert into labor_rate (code, name, description, rate) values ('OMSSDM', 'SDM', null, 99.45);
insert into labor_rate (code, name, description, rate) values ('OMSSDA', 'SDA', null, 99.45);
insert into labor_rate (code, name, description, rate) values ('OSS-Tools', 'Development', null, 69.45);
insert into labor_rate (code, name, description, rate) values ('OMS400', 'Cloud Ops', null, 0.00);
insert into labor_rate (code, name, description, rate) values ('Admin', 'Admin', null, 0.00);
insert into labor_rate (code, name, description, rate) values ('Managed Teams', 'Managed Teams', null, 0.00);
insert into labor_rate (code, name, description, rate) values ('Managers', 'Managers', null, 0.00);

drop table if exists labor_import_log;
create table labor_import_log (
    record_count int not null default 0,
    inserted_date timestamp not null
) ENGINE=InnoDB;

drop table if exists labor_data;
drop table if exists raw_labor_data;
create table raw_labor_data (
    id bigint not null auto_increment,
    chronos_id int,
    ticket varchar(20),
    work_date date,
    hours Decimal(10,3),
    customer_name varchar(255),
    customer_sysid varchar(32),
    customer_id bigint,
    foreign key (customer_id) references customer(id),
    ci_name varchar(255),
    ci_sysid varchar(32),
    num_cis int,
    service_name varchar(255),
    service_id bigint,
    osp_id bigint,
    foreign key (service_id) references service(id),
    business_model varchar(255),
    task_description varchar(255),
    labor_type varchar(20),
    expense_category_id int,
    foreign key (expense_category_id) references expense_category(id),
    rule varchar(50),
    tier_name varchar(255),
    tier_rate Decimal(10,2),
    chronos_inserted_date timestamp,
    record_type varchar(30),
    primary key (id)
) ENGINE=InnoDB;

create index idx_labor_type on raw_labor_data (labor_type);
create index idx_chronos_task on raw_labor_data (task_description);
create index idx_labor_type_chronos_task on raw_labor_data (labor_type, task_description);
create index idx_work_date on raw_labor_data (work_date);
create index idx_tier_name on raw_labor_data (tier_name);
create index idx_business_model on raw_labor_data (business_model);
create index idx_osp_id on raw_labor_data (osp_id);
create index idx_record_type on raw_labor_data (record_type);

drop table if exists grouped_labor_data;
create table grouped_labor_data (
    work_date date,
    labor_total Decimal(10,2) not null default 0.,
    customer_name varchar(255),
    customer_sysid varchar(32),
    customer_id bigint,
    foreign key (customer_id) references customer(id),
    service_name varchar(255),
    service_id bigint,
    osp_id bigint,
    foreign key (service_id) references service(id),
    business_model varchar(255),
    labor_type varchar(20),
    task_description varchar(255),
    expense_category_id int,
    foreign key (expense_category_id) references expense_category(id),
    rule varchar(50),
    record_type varchar(30)
) ENGINE=InnoDB;

create index idx_gr_labor_type on grouped_labor_data (labor_type);
create index idx_gr_chronos_task on grouped_labor_data (task_description);
create index idx_gr_labor_type_chronos_task on grouped_labor_data (labor_type, task_description);
create index idx_gr_work_date on grouped_labor_data (work_date);
create index idx_gr_business_model on grouped_labor_data (business_model);
create index idx_gr_osp_id on grouped_labor_data (osp_id);
create index idx_gr_record_type on grouped_labor_data (record_type);

drop table if exists derived_labor_data;
create table derived_labor_data (
    work_date date,
    labor_total Decimal(10,2) not null default 0.,
    customer_name varchar(255) not null,
    customer_sysid varchar(32) not null,
    customer_id bigint not null,
    foreign key (customer_id) references customer(id),
    service_name varchar(255) not null,
    service_id bigint not null,
    osp_id bigint not null,
    foreign key (service_id) references service(id),
    business_model varchar(255),
    labor_type varchar(20),
    task_description varchar(255)
) ENGINE=InnoDB;

create index idx_d_labor_type on derived_labor_data (labor_type);
create index idx_d_chronos_task on derived_labor_data (task_description);
create index idx_d_labor_type_chronos_task on derived_labor_data (labor_type, task_description);
create index idx_d_work_date on derived_labor_data (work_date);
create index idx_d_business_model on derived_labor_data (business_model);
create index idx_d_osp_id on derived_labor_data (osp_id);

drop table if exists indirect_labor_unit_cost;
create table indirect_labor_unit_cost (
    id bigint not null auto_increment,
    business_model varchar(255) not null default 'blended',
    unit_cost Decimal (19,2) default 0.,
    applied_date date not null,
    primary key (id)
) ENGINE=InnoDB;

-- seed the import log table...
insert into labor_import_log (record_count, inserted_date) values (0, '2009-01-01 03:00:00');

insert into scheduled_task (name, code, description, enabled) values ('Chronos Labor Data Sync', 'chronos_sync', 'Task to Sync with Chronos Timekeeping system for labor data', '0');
insert into scheduled_task (name, code, description, enabled) values ('Schedule for Indirect Labor Data Calculations', 'indirect_labor_unit_cost', 'Task to update indirect labor data', '0');

-- when Dick "makes" data for our devlab, it all had an Inserted date of 10-27, which made it
-- hard to control limiting data being read for a local database, so I update the records to look like
-- they were inserted at reasonable dates compared to the work date itself...
-- note: each month is about 34k records

-- MAYBE THIS ISN'T NECESSARY with current data?
update Export_Table set Inserted = '2016-02-01 03:00:00' where Date between '2016-01-01 00:00:00' and '2016-01-31 23:59:59';
update Export_Table set Inserted = '2016-03-01 03:00:00' where Date between '2016-02-01 00:00:00' and '2016-02-29 23:59:59';
update Export_Table set Inserted = '2016-04-01 03:00:00' where Date between '2016-03-01 00:00:00' and '2016-03-31 23:59:59';
update Export_Table set Inserted = '2016-05-01 03:00:00' where Date between '2016-04-01 00:00:00' and '2016-04-30 23:59:59';
update Export_Table set Inserted = '2016-06-01 03:00:00' where Date between '2016-05-01 00:00:00' and '2016-05-31 23:59:59';
update Export_Table set Inserted = '2016-07-01 03:00:00' where Date between '2016-06-01 00:00:00' and '2016-06-30 23:59:59';
update Export_Table set Inserted = '2016-08-01 03:00:00' where Date between '2016-07-01 00:00:00' and '2016-07-31 23:59:59';
update Export_Table set Inserted = '2016-09-01 03:00:00' where Date between '2016-08-01 00:00:00' and '2016-08-31 23:59:59';
update Export_Table set Inserted = '2016-10-01 03:00:00' where Date between '2016-09-01 00:00:00' and '2016-09-30 23:59:59';
update Export_Table set Inserted = '2016-11-01 03:00:00' where Date between '2016-10-01 00:00:00' and '2016-10-31 23:59:59';

-- update expense_category table to remove unique index
alter table expense_category drop index uk_cost_name;

-- END labor costs release

-- branch onecustomer
alter table customer drop column alt_id;
alter table customer add column alt_id bigint;
alter table customer drop index uk_name;
alter table customer add column parent_id bigint;
alter table customer add foreign key (parent_id) references customer(id);
alter table customer add column phone varchar(100);
alter table customer add column street_1 varchar(100);
alter table customer add column street_2 varchar(100);
alter table customer add column city varchar(100);
alter table customer add column state varchar(100);
alter table customer add column zip varchar(20);
alter table customer add column country varchar(100);
alter table customer add column si_enabled tinyint(1) not null default 1;

insert into scheduled_task (code, name, description, enabled, created, created_by) values ('one_customer_sync', 'One Customer Sync', 'Task to Sync with One Customer from Pricing', 1, now(), 'mfoster');


-- branch SI_SP20
alter table contract add column quote_id bigint;
alter table device add column product_id bigint;
alter table contract_service add column quote_line_item_id bigint;

-- branch SI_SP17
alter table customer add column sn_sys_id varchar(100);
alter table contract add column sn_sys_id varchar(100);

create table service_now_ci (
    id bigint not null auto_increment,
    contract_id bigint not null, 
    foreign key(contract_id) references contract(id) on delete cascade,
    contract_service_id bigint, 
    foreign key(contract_service_id) references contract_service(id) on delete cascade,
    contract_sn_sys_id varchar(100) not null,
    sn_sys_id varchar(100) not null,
    name varchar(255),
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table scheduled_task (
    id bigint not null auto_increment,
    name varchar(100) not null,
    code varchar(100) not null,
    description varchar(255),
    enabled tinyint(1) not null default 0,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

insert into scheduled_task (code, name, description, enabled, created, created_by) values ('sn_customer_sync', 'Service Now Customer Sync', 'Task to Sync the Service Now Customer Sys Id', 1, now(), 'mfoster');
insert into scheduled_task (code, name, description, enabled, created, created_by) values ('sn_contract_sync', 'Service Now Contract Sync', 'Task to Sync the Service Now Contract Sys Id', 1, now(), 'mfoster');
insert into scheduled_task (code, name, description, enabled, created, created_by) values ('sn_contract_ci_sync', 'Service Now Contract CI Sync', 'Task to Sync the Service Now Contract CIs', 1, now(), 'mfoster');

-- branch SI_SP16
create table contract_invoice (
    id bigint not null auto_increment,
    contract_id bigint not null, 
    foreign key(contract_id) references contract(id) on delete cascade,
    status varchar(50) not null default 'active',
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    start_date timestamp not null,
    end_date timestamp not null,
    primary key (id)
) ENGINE=InnoDB;

alter table customer add column archived tinyint(1) not null default 0;
alter table contract add column archived tinyint(1) not null default 0;


-- branch SI_SP14
drop table if exists standard_cost;
drop table if exists cost_general;
drop table if exists cost_spla;
drop table if exists asset_detail;
drop table if exists cost;

create table location (
    id int not null auto_increment,
    parent_id int,
    foreign key (parent_id) references location (id),
    name varchar(255) not null,
    description varchar(255),
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table expense_type_ref (
    id int not null auto_increment,
    name varchar(100) not null,
    display_name varchar(100) not null,
    unique key uk_expense_type_ref_name (name),
    description varchar(255),
    primary key (id)
) ENGINE=InnoDB;

create table expense_type (
    id int not null auto_increment,
    name varchar(255) not null,
    expense_type_ref_id int not null,
    foreign key (expense_type_ref_id) references expense_type_ref(id) on delete cascade,
    description varchar(255),
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table expense (
    id bigint not null auto_increment,
    alt_id varchar(20),
    expense_type_ref_id int not null,
    foreign key (expense_type_ref_id) references expense_type_ref(id) on delete cascade,
    name varchar(100) not null,
    description varchar(255),
    amount Decimal(19,2) default 0.,
    quantity int not null default 1,
    customer_id bigint,
    foreign key (customer_id) references customer (id) on delete cascade,
    contract_id bigint,
    foreign key (contract_id) references contract (id) on delete cascade,
    location_id int,
    foreign key (location_id) references location (id) on delete cascade,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table expense_category (
    id int not null auto_increment,
    parent_id int,
    foreign key (parent_id) references expense_category (id),
    name varchar(100) not null,
    description varchar(255),
    target_utilization Decimal (19,2) default 0.,
    units varchar(20),
    unique key uk_cost_name (name),
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table asset_item (
    id bigint not null auto_increment,
    expense_id_ref bigint,
    unique key uk_expense_id (expense_id_ref),
    expense_type_id int not null,
    foreign key (expense_type_id) references expense_type (id),
    name varchar(255) not null,
    description varchar(255),
    amount Decimal (19,2) default 0.,
    quantity int,
    part_number varchar(50),
    sku varchar(50),
    life int,
    acquired date not null,
    disposal date,
    customer_id bigint,
    foreign key (customer_id) references customer (id) on delete cascade,
    contract_id bigint,
    foreign key (contract_id) references contract (id) on delete cascade,
    location_id int,
    foreign key (location_id) references location (id) on delete cascade,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table asset_item_fraction (
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id) on delete cascade,
    asset_item_id bigint not null,
    foreign key (asset_item_id) references asset_item (id) on delete cascade,
    cost_fraction Decimal (19,2) default 0.,
    target_utilization Decimal (19,2) default 0.,
    quantity int not null default 1,
    primary key (expense_category_id, asset_item_id)
) ENGINE=InnoDB;

create table cost_item (
    id bigint not null auto_increment,
    expense_id_ref bigint,
    unique key uk_expense_id (expense_id_ref),
    expense_type_id int not null,
    foreign key (expense_type_id) references expense_type(id) on delete cascade,
    name varchar(255) not null,
    description varchar(255),
    amount Decimal (19,2) default 0.,
    quantity int,
    part_number varchar(50),
    sku varchar(50),
    customer_id bigint,
    foreign key (customer_id) references customer (id) on delete cascade,
    contract_id bigint,
    foreign key (contract_id) references contract (id) on delete cascade,
    location_id int,
    foreign key (location_id) references location (id) on delete cascade,
    created timestamp not null default current_timestamp,
    applied timestamp not null,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

create table cost_item_fraction (
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    cost_item_id bigint not null,
    foreign key (cost_item_id) references cost_item (id) on delete cascade,
    cost_fraction Decimal (19,2) default 0.,
    primary key (expense_category_id, cost_item_id)
) ENGINE=InnoDB;



-- branch SI_SP13

alter table service add column business_model varchar(255);

update service set business_model = 'Managed' where substring_index(name, ' -', 1) = 'Managed' or substring_index(name, ' -', 1) = 'Monitoring';
update service set business_model = 'Cloud' where substring_index(name, ' -', 1) = 'Provisioned' or substring_index(name, ' -', 1) = 'Dedicated Cloud' or substring_index(name, ' -', 1) = 'Enterprise Cloud' or substring_index(name, ' -', 1) = 'On-Demand Cloud';


alter table contract_service add column status varchar(20) not null default 'active';

-- adding contract group relationship to a contract adjustment
alter table contract_adjustment add column contract_group_id bigint;
alter table contract_adjustment add foreign key (contract_group_id) references contract_group(id) on delete cascade;

-- making a change to the contract_service_device so that the PK is really the contract service id which enforces that there is really only ONE device associated with a particular contract service entry
alter table contract_service_device add unit_count int null;
alter table contract_service_device drop primary key, add primary key (contract_service_id);

alter table contract_update add ticket_number varchar(20) after job_number;
alter table contract_update add effective_date timestamp after signed_date;

-- setting user token_expires date explicitly because adding it to the test table didn't allow it to default causing the Cannot convert value '0000-00-00 00:00:00' from column 4 to TIMESTAMP exception
update users set token_expires = '2015-07-22 00:00:00' where token_expires = '0000-00-00 00:00:00';

-- contract group change
-- a group or "sub-level" of a contract, for relating contract services to, for example, a customer's own customers
create table contract_group (
	id bigint not null auto_increment,
	contract_id bigint not null,
	name varchar(255) not null,
	description varchar(255),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

alter table contract_service add column contract_group_id bigint;
alter table contract_service add foreign key (contract_group_id) references contract_group(id) on delete cascade;

-- change password stuff
alter table users add column token varchar(100);
alter table users add column token_expires timestamp;

-- security fixes
update authorities set authority = 'ROLE_USER' where authority = 'USER';
update authorities set authority = 'ROLE_ADMIN' where authority = 'ADMIN';

-- 5/21 and on changes
drop table if exists contract_adjustment;
create table contract_adjustment (
	id bigint not null auto_increment,
	contract_id bigint not null,
	foreign key (contract_id) references contract(id) on delete cascade,
	contract_update_id bigint,
	foreign key (contract_update_id) references contract_update(id) on delete cascade,
	adjustment Decimal (19,2) default 0.,
	adjustment_type varchar(50) not null,
	note varchar(500),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	start_date timestamp not null,
	end_date timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

-- had to update this table field to match prod to schema
alter table contract_service modify column note varchar(500);

-- 5/13 and on changes
alter table customer add unique key uk_name (name);
alter table contract add unique key uk_altid_custid (alt_id, customer_id);
alter table contract add unique key uk_jobno_custid (job_number, customer_id);
alter table device modify column part_number varchar(50);
alter table device modify column description varchar(255);
alter table device add unique key uk_pno_desc (part_number, description);

-- cascade delete on contract_service service fk and add created info to contract_update_contract_service
alter table contract_service drop foreign key contract_service_ibfk_2;
alter table contract_service add foreign key (service_id) references service(id) on delete cascade;
alter table contract_update_contract_service add column created timestamp not null default current_timestamp;
alter table contract_update_contract_service add column created_by varchar(50) not null default 'system';

-- fix PCR foreign key constraint error
alter table contract_update_contract_service drop foreign key contract_update_contract_service_ibfk_1;
alter table contract_update_contract_service add foreign key (contract_update_id) references contract_update(id) on delete cascade;
alter table contract_update_contract_service drop foreign key contract_update_contract_service_ibfk_2;
alter table contract_update_contract_service add foreign key (contract_service_id) references contract_service(id) on delete cascade;

-- add contract and PCR job # and contract level engagement manager
alter table contract add column emgr varchar(255);
alter table contract add column job_number varchar(20);
alter table contract_update add column job_number varchar(20);

-- bug fix
alter table contract_update add column signed_date timestamp null;

-- table updates for TEST environment for deploying 'device' structure and other tweaks
alter table service modify column description varchar(1000);
alter table lineitem modify column name varchar(255) not null;
alter table lineitem modify column description varchar(1000);
create table device (
	id bigint not null auto_increment,
	part_number varchar(255),
	description varchar(1000),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;
alter table device add column alt_id varchar(20);
create table contract_service_device (
	contract_service_id bigint not null,
	foreign key (contract_service_id) references contract_service(id) on delete cascade,
	device_id bigint not null,
	foreign key (device_id) references device(id) on delete cascade,
	name varchar(255),
	note varchar(500),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (contract_service_id, device_id)
) ENGINE=InnoDB;

-- table updates for TEST environment after deploying Contract Update commit
alter table contract_update modify column note varchar(500) not null;
alter table contract_update add column alt_id varchar(20);

alter table contract_service modify column note varchar(500) not null;
alter table contract_service drop column contract_update_id;

create table contract_update_contract_service (
       contract_update_id bigint not null,
       foreign key (contract_update_id) references contract_update(id),
       contract_service_id bigint not null,
       foreign key (contract_service_id) references contract_service(id),
       primary key (contract_update_id, contract_service_id),
       note varchar(500),
       operation varchar(50)
) ENGINE=InnoDB;

