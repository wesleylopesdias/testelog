drop table if exists service_usage;
drop table if exists contract_pricing;
drop table if exists contract_service;
drop table if exists contract;
drop table if exists service_product;
drop table if exists product;
drop table if exists service;
drop table if exists customer;

create table customer (
	id bigint not null auto_increment,
	primary key (id),
	name varchar(255) not null
);

create table service (
	id bigint not null auto_increment,
	primary key (id),
	name varchar(255) not null,
	description varchar(2000)
);

create table product (
	id bigint not null auto_increment,
	primary key (id),
	name varchar(255),
	description varchar(2000),
	units varchar(50),
	mode varchar(50)
);

create table service_product (
	service_id bigint not null,
	foreign key (service_id) references service(id) on delete no action on update no action,
	product_id bigint not null,
	foreign key (product_id) references product(id) on delete no action on update no action,
	primary key (service_id, product_id)
);

-- term: months (-1 is never ending)
create table contract (
	id bigint not null auto_increment,
	primary key (id),
	name varchar(255) not null,
	customer_id bigint not null, 
	foreign key(customer_id) references customer(id) on delete no action on update no action,
	start_date timestamp not null,
	term bigint not null
);

create table contract_service (
	contract_id bigint not null,
	foreign key (contract_id) references contract(id) on delete no action on update no action,
	service_id bigint not null,
	foreign key (service_id) references service(id) on delete no action on update no action,
	primary key (contract_id, service_id)
);

-- breakpoint: this is not required, but allows for a price to go up or down above a number of units
create table contract_pricing (
	contract_id bigint not null, 
	foreign key (contract_id) references contract(id) on delete no action on update no action,
	service_id bigint not null,
	foreign key (service_id) references service(id) on delete no action on update no action,
	product_id bigint not null,
	foreign key (product_id) references product(id) on delete no action on update no action,
	primary key (contract_id, service_id, product_id),
	pricing Decimal (19,4) not null,
	breakpoint Decimal (19,4) null
);

create table service_usage (
	id bigint not null auto_increment,
	primary key (id),
	contract_id bigint not null,
	foreign key (contract_id) references contract(id) on delete no action on update no action,
	service_id bigint not null,
	foreign key (service_id) references service(id) on delete no action on update no action,
	product_id bigint not null,
	foreign key (product_id) references product(id) on delete no action on update no action,
	item_identifier varchar(255),
	units Decimal (19,4) not null,
	start_date timestamp not null,
	stop_date timestamp null
);
