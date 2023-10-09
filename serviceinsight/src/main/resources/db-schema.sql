-- revenue
drop table if exists azure_uplift;
drop table if exists azure_offer_term;
drop table if exists azure_meter_rate;
drop table if exists azure_meter;
drop table if exists pricing_sheet;
drop table if exists pricing_sheet_product;
drop table if exists contract_lineitem;
drop table if exists contract_service_device;
drop table if exists contract_update_contract_service;
drop table if exists service_now_ci;
drop table if exists contract_adjustment;
drop table if exists contract_service;
drop table if exists contract_service_azure;
drop table if exists contract_update;
drop table if exists contract_invoice;
drop table if exists device_merge_log;
drop table if exists device_property;
drop table if exists device_relationship;
drop table if exists device_unit_count;
drop table if exists device;
drop table if exists contract_group;
drop table if exists lineitem;
drop table if exists service_related;
-- costs
drop table if exists unit_cost;
drop table if exists chronos_task_mapping;
drop table if exists service_expense_category;
drop table if exists cost_allocation_lineitem;
drop table if exists cost_allocation;
drop table if exists unallocated_expense;
drop table if exists expense;
drop table if exists asset_item_fraction;
drop table if exists asset_item;
drop table if exists asset_type;
drop table if exists asset;
drop table if exists asset_category;
drop table if exists chronos_task_rule;
drop table if exists indirect_labor_unit_cost;
drop table if exists derived_labor_data;
drop table if exists grouped_labor_data;
drop table if exists raw_labor_data;
drop table if exists labor_rate;
drop table if exists labor_import_log;
drop table if exists cost_item_fraction;
drop table if exists cost_item; 
drop table if exists cost_category; 
drop table if exists expense_category;
drop table if exists expense_type;
drop table if exists expense_type_ref;
drop table if exists service;
-- common
drop table if exists contract;
drop table if exists business_model;
drop table if exists business_unit;
drop table if exists customer;
drop table if exists location;
drop table if exists scheduled_task;

-- Similar to OSP Service Category business models - used to drive splitting costs between business model units, like Managed Services and Cloud

-- represents a physical location, like a datacenter, building, etc...
create table location (
	id int not null auto_increment,
	parent_id int,
	foreign key (parent_id) references location (id),
	name varchar(255) not null,
	description varchar(255),
	alt_name varchar(255),
	is_displayed_revenue tinyint not null default 0,
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

create table customer (
	id bigint not null auto_increment,
	parent_id bigint,
        foreign key (parent_id) references customer(id),
	alt_id bigint,
	name varchar(255) not null,
	description varchar(255),
	phone varchar(100),
	street_1 varchar(100),
	street_2 varchar(100),
	city varchar(100),
	state varchar(100),
	zip varchar(20),
	country varchar(100),
	archived tinyint(1) not null default 0,
	si_enabled tinyint(1) not null default 1,
	sn_sys_id varchar(100),
	alt_name varchar(255)
	azure_customer_id varchar(255),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

create table service (
	id bigint not null auto_increment,
	code varchar(10),
	osp_id bigint,
	version Decimal (19,4),
	name varchar(255) not null,
	business_model varchar(255),
	description varchar(1000),
	active tinyint(1) default 1,
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	unique key uk_service_idv (osp_id, version),
	primary key (id)
) ENGINE=InnoDB;

create table service_related (
	service_id bigint not null,
	foreign key (service_id) references service(id) on delete cascade,
	related_id bigint not null,
	foreign key (related_id) references service(id) on delete cascade,
	primary key (service_id, related_id)
) ENGINE=InnoDB;

create table lineitem (
	id bigint not null auto_increment,
	name varchar(255) not null,
	description varchar(1000),
	units varchar(50),
	mode varchar(50),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

create table device (
	id bigint not null auto_increment,
	alt_id varchar(20),
	part_number varchar(255),
	description varchar(255),
	device_type varchar(100),
	units varchar(20),
	product_id bigint,
	archived tinyint not null default 0,
	osm_sync_enabled tinyint not null default 0,
	default_osp_id bigint,
	foreign key (default_osp_id) references service(osp_id),
	activate_add_business_service tinyint(1) not null default 0,
	activate_sync_enabled tinyint(1) not null default 0,
	is_ci tinyint(1) not null default 0,
	require_unit_count tinyint(1) not null default 0,
	cost_allocation_option tinyint(1) not null default 0,
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	unique key uk_pno_desc (part_number, description),
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
	primary key(device_id, related_device_id, relationship)
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

create table contract (
	id bigint not null auto_increment,
	alt_id varchar(20),
	job_number varchar(20),
	customer_id bigint not null, 
	foreign key(customer_id) references customer(id) on delete cascade,
	name varchar(255) not null,
	emgr varchar(255),
	sdm varchar(255),
	sda varchar(255),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	signed_date timestamp null,
	service_start_date timestamp null,
	start_date timestamp not null,
	end_date timestamp null,
	archived tinyint(1) not null default 0,
	sn_sys_id varchar(100),
	quote_id bigint,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	unique key uk_altid_custid (alt_id, customer_id),
	unique key uk_jobno_custid (job_number, customer_id),
	primary key (id)
) ENGINE=InnoDB;

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
    
-- might be interesting to update this table each time a contract is changed...
create table contract_update (
	id bigint not null auto_increment,
	alt_id varchar(20),
	job_number varchar(20),
	ticket_number varchar(20) null,
	note varchar(500) not null,
	contract_id bigint not null,
	foreign key (contract_id) references contract(id) on delete cascade,
	signed_date timestamp null,
	effective_date timestamp null,
	onetime_price Decimal (19,2) default 0.,
	recurring_price Decimal (19,2) default 0.,
	updated timestamp not null default current_timestamp,
	updated_by varchar(50) not null default 'system',
	primary key (id)
) ENGINE=InnoDB;

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

--this table is used to save the azure details, so the system can auto-generate the bill every month
create table contract_service_subscription (
    id bigint not null auto_increment,
    parent_id bigint,
    foreign key (parent_id) references contract_service(id),
    contract_id bigint not null, 
    foreign key(contract_id) references contract(id),
    device_id bigint not null, 
    foreign key(device_id) references device(id),
    service_id bigint not null, 
    foreign key(service_id) references service(id),
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

-- services on a contract might change over time. when a contract is updated, services that 
create table contract_service (
	id bigint not null auto_increment,
	parent_id bigint,
	foreign key (parent_id) references contract_service(id) on delete cascade,
	contract_id bigint not null,
	foreign key (contract_id) references contract(id) on delete cascade,
	service_id bigint not null,
	foreign key (service_id) references service(id) on delete cascade,
	contract_group_id bigint,
	foreign key (contract_group_id) references contract_group(id) on delete cascade,
	contract_service_azure_id bigint,
	foreign key (contract_service_azure_id) references contract_service_azure(id) on delete cascade,
	quantity int not null default 1,
	onetime_revenue Decimal (19,2) default 0.,
	recurring_revenue Decimal (19,2) default 0.,
	note varchar(500),
	status varchar(20) not null default 'active',
	hidden tinyint(1) not null default 0,
	reason varchar(50),
	quote_line_item_id bigint,
	created timestamp not null default current_timestamp,
	updated timestamp null,
	start_date timestamp not null,
	end_date timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

-- a contract might require a financial adjustment, possibly related to a contract update / PCR
create table contract_adjustment (
	id bigint not null auto_increment,
	contract_id bigint not null,
	foreign key (contract_id) references contract(id) on delete cascade,
	contract_update_id bigint,
	foreign key (contract_update_id) references contract_update(id) on delete cascade,
	contract_group_id bigint,
	foreign key (contract_group_id) references contract_group(id) on delete cascade,
	adjustment Decimal (19,2) default 0.,
	adjustment_type varchar(50) not null,
	status varchar(50) not null default 'active',
	note varchar(500),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	start_date timestamp not null,
	end_date timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

-- a contract update (PCR) obviously is related to many contract services, but contract service changes need to be able to be associated with many contract updates
-- example: on a PCR, a contract service is added. later, on a different PCR, it is end dated.
create table contract_update_contract_service (
	contract_update_id bigint not null,
	foreign key (contract_update_id) references contract_update(id) on delete cascade,
	contract_service_id bigint not null,
	foreign key (contract_service_id) references contract_service(id) on delete cascade,
	primary key (contract_update_id, contract_service_id),
	note varchar(500),
	operation varchar(50),
	created timestamp not null default current_timestamp,
	created_by varchar(50) not null default 'system'
) ENGINE=InnoDB;

create table contract_service_device (
	contract_service_id bigint not null,
	foreign key (contract_service_id) references contract_service(id) on delete cascade,
	device_id bigint not null,
	foreign key (device_id) references device(id) on delete cascade,
	location_id int,
    foreign key (location_id) references location(id),
	name varchar(255),
	note varchar(500),
	unit_count int,
	created timestamp not null default current_timestamp,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (contract_service_id)
) ENGINE=InnoDB;

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
    removal_revenue Decimal (19,2) default 0.,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    unique key uk_pricing_sheet_product (pricing_sheet_id, device_id),
    primary key (id)
) ENGINE=InnoDB;  

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

-- breakpoint: this is not required, but allows for a price to go up or down above a number of units
create table contract_lineitem (
	id bigint not null auto_increment,
	contract_id bigint not null,
	foreign key (contract_id) references contract(id) on delete cascade,
	contract_service_id bigint,
	foreign key (contract_service_id) references contract_service(id) on delete cascade,
	service_id bigint,
	foreign key (service_id) references service(id) on delete cascade,
	lineitem_id bigint not null,
	foreign key (lineitem_id) references lineitem(id) on delete cascade,
	contract_update_id bigint,
	foreign key (contract_update_id) references contract_update(id) on delete cascade,
	quantity int not null default 1,
	onetime_revenue Decimal (19,2) default 0.,
	recurring_revenue Decimal (19,2) default 0.,
	breakpoint Decimal (19,2),
	created timestamp not null default current_timestamp,
	updated timestamp null,
	start_date timestamp not null,
	end_date timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

-- A utility bill expense, or purchase, such as "RAM"
create table expense (
	id bigint not null auto_increment,
	alt_id varchar(20),
	expense_type varchar(50) not null,
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

-- typing of costs, such as "rent", "utilities", "licenses", "SPLA"
create table expense_category (
    id int not null auto_increment,
    parent_id int,
    foreign key (parent_id) references expense_category (id),
    name varchar(100) not null,
    description varchar(255),
    target_utilization Decimal (19,2) default 0.,
    units varchar(20),
    labor_split Decimal (10,3) default 0.,
    created timestamp not null default current_timestamp,
    updated timestamp null,
    created_by varchar(50) not null default 'system',
    updated_by varchar(50),
    primary key (id)
) ENGINE=InnoDB;

-- discreet assets contributing to costs, including fields for amortizing, "life", etc...
create table asset_item (
	id bigint not null auto_increment,
	expense_id_ref bigint,
	unique key uk_expense_id (expense_id_ref),
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

-- join table for assigning cost fractions of an asset item to an asset (ex. server item has 50% of cost assigned to X86 RAM
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

-- discreet cost items with fields used to determine dates of use in cost calculations
create table cost_item (
	id bigint not null auto_increment,
	expense_id_ref bigint,
	unique key uk_expense_id (expense_id_ref),
	cost_allocation_lineitem_id_ref bigint,
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
	azure_customer_name varchar(255),
	azure_invoice_id varchar(50),
	azure_subscription_id varchar(50),
	aws_subscription_id varchar(50),
	device_id int,
	foreign key (device_id) references device (id) on delete cascade,
	spla_cost_catalog_id int,
	foreign key (spla_cost_catalog_id) references spla_cost_catalog (id) on delete cascade,
	cost_type varchar(50) not null,
	cost_subtype varchar(50),
	created timestamp not null default current_timestamp,
	applied timestamp not null,
	updated timestamp null,
	created_by varchar(50) not null default 'system',
	updated_by varchar(50),
	primary key (id)
) ENGINE=InnoDB;

-- join table for assigning cost fractions of a cost item to an category
create table cost_item_fraction (
	expense_category_id int not null,
	foreign key (expense_category_id) references expense_category (id),
	cost_item_id bigint not null,
	foreign key (cost_item_id) references cost_item (id) on delete cascade,
	cost_fraction Decimal (19,2) default 0.,
	primary key (expense_category_id, cost_item_id)
) ENGINE=InnoDB;

create table unit_cost (
    id bigint not null auto_increment,
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    total_cost Decimal (19,2) default 0.,
    total_labor Decimal (19,2) default 0.,
    total_units int default 0,
    service_total_units int default 0,
    device_total_units int default 0,
    applied_date date not null,
    unique key uk_expid_applied (expense_category_id, applied_date),
    primary key (id)
) ENGINE=InnoDB;

create table service_expense_category (
    osp_id bigint not null,
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    quantity int not null,
    primary key (osp_id, expense_category_id)
) ENGINE=InnoDB;

create table device_expense_category (
    device_id bigint not null,
    foreign key (device_id) references device (id),
    expense_category_id int not null,
    foreign key (expense_category_id) references expense_category (id),
    quantity int not null,
    primary key (device_id, expense_category_id)
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
	specific_total Decimal (19,2) default 0.,
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
    name varchar(100) not null,
    description varchar(255),
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

create table labor_import_log (
    record_count int not null default 0,
    inserted_date timestamp not null
) ENGINE=InnoDB;

create table labor_rate (
    id int not null auto_increment,
    code varchar(20) not null,
    unique key uk_laborRateCode (code),
    name varchar(50) not null,
    description varchar(255),
    unique key uk_laborRateName (name),
    rate Decimal(10,2) not null default 0.,
    addl_rate Decimal(10,2) not null default 0.,
    rate_factor Decimal(10,2) not null default 1.,
    created timestamp not null default current_timestamp,
    created_by varchar(50) not null default 'system',
    primary key (id)
) ENGINE=InnoDB;

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
    subtask_description  varchar(255),
    labor_type varchar(20),
    expense_category_id int,
    foreign key (expense_category_id) references expense_category(id),
    rule varchar(50),
    worker varchar(255),
    tier_name varchar(255),
    tier_code varchar(255),
    tier_rate Decimal(10,2),
    addl_tier_rate Decimal(10,2),
    chronos_inserted_date timestamp,
    record_type varchar(30),
    primary key (id)
) ENGINE=InnoDB;

create index idx_labor_type on raw_labor_data (labor_type);
create index idx_chronos_task on raw_labor_data (task_description);
create index idx_task_chronos_subtask on raw_labor_data (task_description, subtask_description);
create index idx_labor_type_chronos_task on raw_labor_data (labor_type, task_description);
create index idx_work_date on raw_labor_data (work_date);
create index idx_worker on raw_labor_data (worker);
create index idx_tier_name on raw_labor_data (tier_name);
create index idx_tier_code on raw_labor_data (tier_code);
create index idx_business_model on raw_labor_data (business_model);
create index idx_osp_id on raw_labor_data (osp_id);
create index idx_record_type on raw_labor_data (record_type);

create table grouped_labor_data (
    work_date date,
    labor_total Decimal(10,2) not null default 0.,
    addl_labor_total Decimal(10,2) not null default 0.,
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
    subtask_description  varchar(255),
    expense_category_id int,
    foreign key (expense_category_id) references expense_category(id),
    rule varchar(50),
    record_type varchar(30),
    onboarding tinyint(1) not null default 0
) ENGINE=InnoDB;

create index idx_gr_labor_type on grouped_labor_data (labor_type);
create index idx_gr_chronos_task on grouped_labor_data (task_description);
create index idx_gr_labor_type_chronos_task on grouped_labor_data (labor_type, task_description);
create index idx_gr_task_chronos_subtask on grouped_labor_data (task_description, subtask_description);
create index idx_gr_work_date on grouped_labor_data (work_date);
create index idx_gr_business_model on grouped_labor_data (business_model);
create index idx_gr_osp_id on grouped_labor_data (osp_id);
create index idx_gr_record_type on grouped_labor_data (record_type);

create table derived_labor_data (
    work_date date,
    labor_total Decimal(10,2) not null default 0.,
    addl_labor_total Decimal(10,2) not null default 0.,
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
    task_description varchar(255),
    subtask_description  varchar(255),
    onboarding tinyint(1) not null default 0
) ENGINE=InnoDB;

create index idx_d_labor_type on derived_labor_data (labor_type);
create index idx_d_chronos_task on derived_labor_data (task_description);
create index idx_d_labor_type_chronos_task on derived_labor_data (labor_type, task_description);
create index idx_d_chronos_subtask on derived_labor_data (task_description, subtask_description);
create index idx_d_work_date on derived_labor_data (work_date);
create index idx_d_business_model on derived_labor_data (business_model);
create index idx_d_osp_id on derived_labor_data (osp_id);

create table indirect_labor_unit_cost (
    id bigint not null auto_increment,
    business_model varchar(255) not null default 'blended',
    unit_cost Decimal (19,2) default 0.,
    addl_unit_cost Decimal (19,2) default 0.,
    applied_date date not null,
    primary key (id)
) ENGINE=InnoDB;

create table chronos_task_rule (
    id int not null auto_increment,
    labor_type varchar(20) not null,
    task_description varchar(255),
    unique key uk_laborTypeChronosTask (labor_type, task_description),
    rule varchar(50) not null,
    primary key (id)
) ENGINE=InnoDB;

create table chronos_task_mapping (
  task_description varchar(255),
  subtask_description varchar(255),
  expense_category_id int,
  unique key uk_expCatId (expense_category_id),
  foreign key (expense_category_id) references expense_category(id) on delete cascade,
  primary key (task_description, subtask_description)
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
    unique key uk_scheduled_task_code (code),
    primary key (id)
) ENGINE=InnoDB;
