select business_model from service where active = true and osp_id = 17

select count(id), sum(hours * tier_rate), chronos_task, labor_type from labor_data group by chronos_task, labor_type order by 4, 3;
select count(id), sum(hours * tier_rate), chronos_task, labor_type from labor_data where service_id is null group by chronos_task, labor_type order by 4, 3;
select count(id), sum(hours * tier_rate), chronos_task, labor_type from labor_data where service_id is null and customer_sysid is null group by chronos_task, labor_type order by 4, 3;

select count(id), sum(hours * tier_rate), chronos_task, labor_type from labor_data where service_name is not null group by chronos_task, labor_type order by 4, 3;
select count(id), sum(hours * tier_rate), chronos_task, labor_type from labor_data where ci_sysid is not null and service_name is not null group by chronos_task, labor_type order by 4, 3;

select distinct chronos_task from labor_data;

drop table if exists chronos_task_rule;
create table chronos_task_rule (
    id int not null auto_increment,
    labor_type varchar(20) not null,
    chronos_task varchar(255),
    unique key uk_laborTypeChronosTask (labor_type, chronos_task),
    rule varchar(50) not null,
    primary key (id)
) ENGINE=InnoDB;

insert into chronos_task_rule (labor_type, chronos_task, rule) values ('Administrative', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, chronos_task, rule) values ('Setup', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, chronos_task, rule) values ('Sales', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, chronos_task, rule) values ('Operational', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, chronos_task, rule) values ('Internal', null, 'SPREAD_ALL');
insert into chronos_task_rule (labor_type, chronos_task, rule) values ('Internal', 'Cloud Infrastructure', 'Cloud');

select distinct business_model from service;

update chronos_task_rule set chronos_task = 'Internal Systems' where labor_type = 'Internal' and chronos_task is null;

select distinct labor_type, chronos_task from labor_data where customer_name is null and service_name is null order by 1, 2
select count(*) from labor_data where service_name is not null and customer_name is null;

select ld.hours * ld.tier_rate as cost, ld.customer_name, ld.service_name, cr.labor_type, cr.chronos_task from labor_data ld
left join chronos_task_rule cr on cr.labor_type = ld.labor_type and cr.chronos_task = ld.chronos_task
where cr.rule = 'SPREAD_CLOUD' and ld.work_date between '2016-10-01' and '2016-11-30'

select round(sum(ld.hours * ld.tier_rate), 2) as cost from labor_data ld
left join chronos_task_rule cr on cr.labor_type = ld.labor_type and cr.chronos_task = ld.chronos_task
where cr.rule = 'SPREAD_ALL' and ld.work_date between '2016-10-01' and '2016-11-30'

select sum(ld.hours * ld.tier_rate), rl.labor_type, rl.chronos_task, ld.labor_type, ld.chronos_task, rl.rule
from labor_data ld left join chronos_task_rule rl on rl.labor_type = ld.labor_type and (rl.chronos_task is null or rl.chronos_task = ld.chronos_task)
where ld.customer_name is null and ld.service_name is null and ld.work_date between '2016-01-01' and '2016-11-30' and ld.tier_rate is not null and rl.rule = 'SPREAD_ALL'
group by rl.labor_type, rl.chronos_task, ld.labor_type, ld.chronos_task, rl.rule
-- 618361.54	Internal	(null)
-- 207976.46	Internal	Internal Systems

update chronos_task_rule set chronos_task = null where labor_type = 'Internal' and chronos_task = 'Internal Systems'

insert into chronos_task_rule (chronos_task, rule) values ();

select customer_name, hours * tier_rate, labor_type, chronos_task from labor_data where ci_sysid is not null and service_name is null;

-- 80270
select count(*) from labor_data where chronos_task is null and service_name is null

-- 11741 records... 13,281 hours
select count(*) from labor_data where tier_rate is null

select sum(hours) from labor_data where tier_rate is null

-- $9,553,580
select count(*) as records, sum(hours * tier_rate) as cost, labor_type, chronos_task, customer_name from labor_data where service_name is null group by labor_type, chronos_task, customer_name order by 2 desc, 3, 4, 5
select sum(hours * tier_rate) as cost from labor_data where service_name is null

-- $12,030,442
select sum(hours * tier_rate) from labor_data

select * from labor_data where customer_name = 'Logicalis US' and work_date between '2016-01-01' and '2016-11-30'

select * from customer where name like 'Logicalis%'

-- 2 and 435
select * from contract where customer_id = 2;
update contract set customer_id = 435 where id = 346;
select * from contract_service where contract_id = 346;

select labor_type, chronos_task, round(sum(hours * tier_rate), 2) from labor_data where customer_name is null group by labor_type, chronos_task

select labor_type, chronos_task, count(*), round(sum(hours * tier_rate), 2) from labor_data where customer_name is null and service_name is null group by labor_type, chronos_task

select labor_type, chronos_task, count(*), round(sum(hours * tier_rate), 2) from labor_data where customer_name is not null and service_name is null group by labor_type, chronos_task

select distinct labor_type, chronos_task, count(*) from labor_data group by labor_type, chronos_task

select round(hours * tier_rate, 2), customer_name, service_name, ci_sysid, labor_type, chronos_task from labor_data group by customer_name, service_name, ci_sysid, labor_type, chronos_task order by 1 desc

select customer_name, service_name, ci_sysid, labor_type, chronos_task from labor_data group by customer_name, service_name, ci_sysid, labor_type, chronos_task

select count(*), round(sum(hours * tier_rate), 2), customer_name, service_name, ci_sysid from labor_data where labor_type = 'Administrative' and chronos_task = 'Administrative' group by customer_name, service_name, ci_sysid

select count(*), round(sum(hours * tier_rate), 2), customer_name, service_name, ci_sysid from labor_data where labor_type = 'Operational' group by customer_name, service_name, ci_sysid

select count(*), round(sum(hours * tier_rate), 2), service_name, ci_sysid from labor_data where labor_type = 'Operational' group by service_name, ci_sysid

select count(*), round(sum(hours * tier_rate), 2), ci_sysid from labor_data where labor_type = 'Operational' group by ci_sysid

select count(*), round(sum(hours * tier_rate), 2) from labor_data where customer_name is not null or service_name is not null

select count(*), round(sum(hours * tier_rate), 2) from labor_data where customer_name is not null and service_name is null

select count(*), round(sum(hours * tier_rate / 1000000), 2) from labor_data where customer_name is null and service_name is null and chronos_task is not null

select count(*), round(sum(hours * tier_rate / 12000000 * 100), 2), labor_type, chronos_task from labor_data
where customer_name is null and service_name is null and chronos_task is not null group by labor_type, chronos_task order by 2 desc, 1 desc

select count(*), round(sum(hours * tier_rate) / 120300, 3), labor_type, chronos_task from labor_data
where customer_name is null and service_name is null and chronos_task is null group by labor_type, chronos_task order by 2 desc, 1 desc

-- a customer and service name exist for just about every record where a ci is also specififed
select count(*), round(sum(hours * tier_rate) / 120300, 3), labor_type, chronos_task from labor_data
where customer_name is null and service_name is null and ci_sysid is not null group by labor_type, chronos_task order by 2 desc, 1 desc

-- tiny amount of data...
select count(*), round(sum(hours * tier_rate) / 120300, 3), labor_type, chronos_task from labor_data
where service_name is null and ci_sysid is not null group by labor_type, chronos_task order by 2 desc, 1 desc

-- targeting specifc labor types
select count(*), round(sum(hours * tier_rate) / 120300, 3), labor_type, chronos_task from labor_data
where customer_name is null and service_name is null and labor_type = 'Internal' group by labor_type, chronos_task order by 2 desc, 1 desc

select count(*), round(sum(hours * tier_rate) / 120300, 3), labor_type, chronos_task from labor_data
where customer_name is not null and service_name is null and labor_type = 'Operational' group by labor_type, chronos_task order by 2 desc, 1 desc

select sum(csvc.quantity) quantity, sum(csvc.quantity * csvc.onetime_revenue) onetime, sum(csvc.quantity * csvc.recurring_revenue) revenue, svc.name, csvc.start_date, csvc.end_date
from contract_service csvc left join service svc on svc.id = csvc.service_id left join contract ctr on ctr.id = csvc.contract_id left join customer cst on cst.id = ctr.customer_id
where (csvc.start_date <= '2016-01-01' or csvc.start_date between '2016-01-01' and '2016-01-31') and (csvc.end_date is null or csvc.end_date between '2016-01-01'
and '2016-01-31' or csvc.end_date >= '2016-01-31') and csvc.status = 'active'
--and svc.business_model = 'Managed'
--and svc.osp_id = 17
--and cst.id = 2
group by svc.business_model, svc.osp_id, svc.code, csvc.start_date, csvc.end_date
order by svc.business_model, svc.name, svc.code, csvc.start_date, csvc.end_date

-- device count: 12715, 9711
select sum(csvc.quantity) quantity from contract_service csvc left join service svc on svc.id = csvc.service_id left join contract ctr on ctr.id = csvc.contract_id left join customer cst on cst.id = ctr.customer_id where (csvc.start_date <= '2016-11-01' or csvc.start_date between '2016-11-01' and '2016-11-30') and (csvc.end_date is null or csvc.end_date between '2016-11-01' and '2016-11-30' or csvc.end_date >= '2016-11-30') and csvc.status = 'active'
--and svc.business_model = 'Managed'
--and svc.osp_id = 17
--and cst.id = 2
group by svc.business_model, svc.osp_id, svc.code, csvc.start_date, csvc.end_date
order by svc.business_model, svc.name, svc.code, csvc.start_date, csvc.end_date

-- 232680 + 6766 = 239446
select sum(ld.hours * ld.tier_rate) cost, ld.labor_type, rl.chronos_task from labor_data ld left join chronos_task_rule rl on rl.labor_type = ld.labor_type and (rl.chronos_task is null or rl.chronos_task = ld.chronos_task)
where ld.customer_name is null and ld.service_name is null and ld.work_date between '2016-01-01' and '2016-01-31' and ld.tier_rate is not null and rl.rule = 'SPREAD_ALL' group by ld.labor_type, rl.chronos_task

select sum(ld.hours * ld.tier_rate) cost from labor_data ld left join chronos_task_rule rl on rl.labor_type = ld.labor_type and (rl.chronos_task is null or rl.chronos_task = ld.chronos_task)
where ld.customer_name is null and ld.service_name is null and ld.work_date between '2016-01-01' and '2016-01-31' and ld.tier_rate is not null and rl.rule = 'SPREAD_ALL'

select sum(ld.hours * ld.tier_rate) from labor_data ld where ld.labor_type = 'Setup' and ld.customer_name is null and ld.service_name is null and ld.work_date between '2016-01-01' and '2016-01-31'

select * from labor_data where chronos_task is null and service_name is null

select round(sum(hours * tier_rate) / 120300, 3) from labor_data where chronos_task is null and service_name is null

select round(sum(ld.hours * ld.tier_rate), 2) perc_of_total, ld.labor_type, ld.chronos_task
from labor_data ld
left join chronos_task_rule rl on rl.labor_type = ld.labor_type and rl.chronos_task = ld.chronos_task
where ld.customer_name is null and ld.service_name is null and rl.rule = 'SPREAD_CLOUD'
group by ld.labor_type, ld.chronos_task order by 1 desc

select round(sum(ld.hours * ld.tier_rate), 2) perc_of_total
from labor_data ld
left join chronos_task_rule rl on rl.labor_type = ld.labor_type and rl.chronos_task = ld.chronos_task
where ld.customer_name is null and ld.service_name is null and rl.rule = 'SPREAD_ALL'
group by ld.labor_type, ld.chronos_task, rl.labor_type, rl.chronos_task order by 1 desc

select sum(ld.hours * ld.tier_rate) perc_of_total from labor_data ld left join chronos_task_rule rl on rl.labor_type = ld.labor_type and rl.chronos_task = ld.chronos_task where ld.customer_name is null and ld.service_name is null and rl.rule = 'SPREAD_ALL'

select count(*) from service where active = true

select round(sum(hours * tier_rate) / 1000000, 2) from labor_data

select count(*), business_model from service where active = true group by business_model
select business_model from service where osp_id = 17 and active = 'true'
-- adjustment rollup without relation to a service on a contract
select adj.contract_id, sum(adj.adjustment) adjustment, adj.adjustment_type, adj.start_date, adj.end_date
from contract_adjustment adj
left join contract c on adj.contract_id = c.id
where c.id = 1
--and (adj.start_date <= '2015-05-01 00:00:00' or adj.start_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00')
--and (adj.end_date is null or adj.end_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00' or adj.end_date >= '2015-05-31 00:00:00')
group by adj.contract_id, adj.adjustment_type, adj.start_date, adj.end_date
order by adj.start_date desc

select adj.id, adj.contract_id, adj.contract_service_id, csvc.service_id, adj.contract_update_id, adj.adjustment, adj.adjustment_type, adj.note, adj.start_date, adj.end_date,
adj.created, adj.created_by, adj.updated, adj.updated_by from contract_adjustment adj left join contract_service csvc on adj.contract_service_id = csvc.id
where adj.contract_service_id = 2 order by adj.created desc

-- rollup queries?
select adj.contract_id, csvc.service_id, sum(adj.adjustment), adj.adjustment_type, adj.start_date, adj.end_date, count(adj.id)
from contract_adjustment adj
left join contract c on adj.contract_id = c.id
left outer join contract_service csvc on adj.contract_service_id = csvc.id
left outer join service svc on csvc.service_id = svc.id
where c.id = 1
and csvc.id is null or (
(csvc.start_date <= '2015-05-01 00:00:00' or csvc.start_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00')
and (csvc.end_date is null or csvc.end_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00' or csvc.end_date >= '2015-05-31 00:00:00'))
group by adj.contract_id, csvc.service_id, adj.adjustment_type, adj.start_date, adj.end_date
order by svc.name

select adj.contract_id, csvc.service_id, sum(adj.adjustment), adj.adjustment_type, adj.start_date, adj.end_date, count(adj.id)
from contract_adjustment adj
left join contract_service csvc on adj.contract_service_id = csvc.id
left join service svc on csvc.service_id = svc.id
where csvc.contract_id = 1
and (csvc.start_date <= '2015-05-01 00:00:00' or csvc.start_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00')
and (csvc.end_date is null or csvc.end_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00' or csvc.end_date >= '2015-05-31 00:00:00')
group by adj.contract_id, csvc.service_id, adj.adjustment_type, adj.start_date, adj.end_date
order by svc.name

-- select contract updates in relation to a service on a contract
select adj.id, adj.contract_id, adj.contract_service_id, adj.contract_update_id, adj.adjustment, adj.adjustment_type, adj.note, adj.start_date, adj.end_date,
adj.created, adj.created_by, adj.updated, adj.updated_by
from contract_adjustment adj
left join contract_service csvc on  adj.contract_service_id = csvc.id
left outer join service svc on csvc.service_id = svc.id
where adj.contract_service_id = 2
and (csvc.start_date <= '2015-05-01 00:00:00' or csvc.start_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00')
and (csvc.end_date is null or csvc.end_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00' or csvc.end_date >= '2015-05-31 00:00:00')

select adj.id, adj.contract_id, adj.contract_service_id, adj.contract_update_id, adj.adjustment, adj.adjustment_type, adj.note, adj.start_date, adj.end_date, adj.created, adj.created_by, adj.updated, adj.updated_by from contract_adjustment adj left join contract_service csvc on  adj.contract_service_id = csvc.id left outer join service svc on csvc.service_id = svc.id where adj.contract_service_id = 2 and (csvc.start_date <= '2015-05-01 00:00:00' or csvc.start_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00') and (csvc.end_date is null or csvc.end_date between '2015-05-01 00:00:00' and '2015-05-31 00:00:00' or csvc.end_date >= '2015-05-31 00:00:00')

-- finding contract adjustments where only contractId might be available
select adj.id, adj.contract_id, adj.contract_service_id, svc.id, adj.contract_update_id, adj.adjustment, adj.adjustment_type, adj.note, adj.start_date, adj.end_date
from contract_adjustment adj
left join contract c on adj.contract_id = c.id
left outer join contract_service csvc on  adj.contract_service_id = csvc.id
left outer join service svc on csvc.service_id = svc.id
where c.id = 1
and csvc.id is null or (
(csvc.start_date <= '2017-06-01 00:00:00' or csvc.start_date between '2017-06-01 00:00:00' and '2017-06-30 00:00:00')
and (csvc.end_date is null or csvc.end_date between '2017-06-01 00:00:00' and '2017-06-30 00:00:00' or csvc.end_date >= '2017-06-30 00:00:00'))
order by svc.name

select adj.id, adj.contract_id, adj.contract_service_id, svc.id, adj.contract_update_id, adj.adjustment, adj.adjustment_type, adj.note, adj.start_date, adj.end_date from contract_adjustment adj left join contract c on adj.contract_id = c.id left outer join contract_service csvc on  adj.contract_service_id = csvc.id left outer join service svc on csvc.service_id = svc.id where c.id = 1 and csvc.id is null or ( (csvc.start_date <= '2017-06-01 00:00:00' or csvc.start_date between '2017-06-01 00:00:00' and '2017-06-30 00:00:00') and (csvc.end_date is null or csvc.end_date between '2017-06-01 00:00:00' and '2017-06-30 00:00:00' or csvc.end_date >= '2017-06-30 00:00:00')) order by svc.name

select * from service where active = true and name like 'Monitoring%' order by name

select (osp_id + 10000) osp_id, version, replace(name, 'Managed', 'Monitoring') name, description, active from service where name like 'Managed%' order by name



select clit.id

select csvc.id, csvc.service_id, csvc.contract_id, csvc.contract_update_id, service.code, service.osp_id, service.name, service.version, csvc.onetime_revenue, csvc.recurring_revenue, csvc.quantity,
count(clit.lineitem_id) lineitems, csvc.start_date, csvc.end_date
from contract_service csvc left join service service on service.id = csvc.service_id left outer join contract_lineitem clit on clit.contract_service_id = csvc.id
group by csvc.id, csvc.service_id, csvc.contract_id, csvc.contract_update_id, service.code, service.osp_id, service.name, service.version, csvc.onetime_revenue, csvc.recurring_revenue,
csvc.quantity, csvc.start_date, csvc.end_date

select * from service where active = true

-- service rollup query between '2014-01-01 00:00:00' and '2017-12-31 23:59:59'
select csvc.service_id, csvc.contract_id, csvc.contract_update_id, svc.code, svc.osp_id, svc.name, svc.version, sum(csvc.quantity) quantity, csvc.start_date, csvc.end_date,
sum(csvc.quantity * csvc.onetime_revenue) onetime, sum(csvc.quantity * csvc.recurring_revenue) revenue
from service svc left join contract_service csvc on csvc.service_id = svc.id
where csvc.contract_id = 1
and (csvc.start_date <= '2014-01-01 00:00:00' or csvc.start_date between '2014-01-01 00:00:00' and '2017-12-31 23:59:59')
and (csvc.end_date is null or csvc.end_date between '2014-01-01 00:00:00' and '2017-12-31 23:59:59' or csvc.end_date >= '2017-12-31 23:59:59')
group by csvc.service_id, csvc.contract_id, csvc.start_date, csvc.end_date
order by svc.name, csvc.start_date

select csvc.id, csvc.service_id, csvc.contract_id, csvc.contract_update_id, service.code, service.osp_id, service.name, service.version, csvc.onetime_revenue, csvc.recurring_revenue, csvc.quantity,
count(clit.lineitem_id) lineitems, csvc.start_date, csvc.end_date
from contract_service csvc
left join service service on service.id = csvc.service_id
left outer join contract_lineitem clit on clit.contract_service_id = csvc.id
group by csvc.id, csvc.service_id, csvc.contract_id, csvc.contract_update_id, service.code, service.osp_id,
service.name, service.version, csvc.onetime_revenue, csvc.recurring_revenue, csvc.quantity, csvc.start_date, csvc.end_date


select csvc.service_id, csvc.contract_id, svc.name, sum(csvc.quantity) quantity, csvc.start_date, csvc.end_date, sum(csvc.quantity * csvc.onetime_revenue) onetime, sum(csvc.quantity * csvc.recurring_revenue) revenue from service svc left join contract_service csvc on csvc.service_id = svc.id where csvc.contract_id = 1 and (csvc.start_date <= '2015-01-01 00:00:00' or csvc.start_date between '2015-01-01 00:00:00' and '2017-12-31 23:59:59') and (csvc.end_date is null or csvc.end_date between '2015-01-01 00:00:00' and '2017-12-31 23:59:59' or csvc.end_date >= '2017-12-31 23:59:59') group by csvc.service_id, csvc.contract_id, csvc.start_date, csvc.end_date

insert into service (code, osp_id, version, name, description, created) values ('05-001-01', 253, 1.21, 'Enterprise Cloud - Server - Linux', 'Management of Linux in the Cloud...', '2014-01-01 00:00:00');

insert into contract_service (contract_id, service_id, onetime_revenue, recurring_revenue, start_date, end_date, created) values (1, 2, 150.00, 186.00, '2015-07-01 00:00:00', '2017-07-31 23:59:59', '2014-06-11 16:13:45');

select ad.id ad_id, ad.name detail, a.name asset, ac.name category
from asset_detail ad
left join asset a on a.id = ad.asset_id
left join asset_category ac on ac.id = a.asset_category_id
order by 4, 3, 2

select contract.id, contract.alt_id, contract.name, count(csvc.service_id) services
from contract contract left join contract_service csvc on csvc.contract_id = contract.id
where contract.id = 1
and (contract.end_date is null or contract.end_date >= '2015-03-02 00:00:00')
and (csvc.end_date is null or csvc.end_date >= '2015-03-02 00:00:00')
group by contract.id, contract.alt_id, contract.name

select customer.id, customer.name, count(contract.id) contracts
from customer customer
left join contract contract on contract.customer_id = customer.id
where customer.name like '%API%' and (contract.end_date is null or contract.end_date >= '2015-03-02 00:00:00')
group by customer.id, customer.name

select assetc.name category, asset.name asset, assetd.name detail, assetd.id detail_id, assetd.capacity, asset.cost/asset.life cost_per_month, assetd.cost_fraction*(asset.cost/asset.life) cost_contribution
from asset_detail assetd
left join asset asset on assetd.asset_id = asset.id
left join asset_category assetc on asset.asset_category_id = assetc.id
where asset.disposal is null or asset.disposal > now()
order by assetc.name, asset.name, assetd.name;

-- summing service revenue across customers
select c_svc.contract_id, c_svc.service_id, svc.name service, svc.version, c_svc.onetime_revenue onetime, c_svc.recurring_revenue recurring, c_svc.start_date, c_svc.end_date
from contract_service c_svc
left join service svc on svc.id = c_svc.service_id
where svc.id = 1
and (c_svc.start_date <= '2015-02-01 00:00:00' or c_svc.start_date between '2015-02-01 00:00:00' and '2015-02-28 23:59:59')
and (c_svc.end_date is null or c_svc.end_date between '2015-02-01 00:00:00' and '2015-02-28 23:59:59' or c_svc.end_date >= '2015-02-28 23:59:59')

-- summing across customers
select svc.id, svc.name service, svc.version, c_svc.onetime_revenue onetime, c_svc.recurring_revenue recurring, c_svc.start_date, c_svc.end_date
from contract_service c_svc
left join service svc on svc.id = c_svc.service_id
where svc.id = 1
and (c_svc.start_date <= '2015-02-01 00:00:00' or c_svc.start_date between '2015-02-01 00:00:00' and '2015-02-28 23:59:59')
and (c_svc.end_date is null or c_svc.end_date between '2015-02-01 00:00:00' and '2015-02-28 23:59:59' or c_svc.end_date >= '2015-02-28 23:59:59')

select c_lir.id, c_lir.contract_id, c_lir.service_id, c_lir.lineitem_id, lir.name lineitem, c_lir.onetime_revenue onetime, c_lir.count count, c_lir.recurring_revenue recurring, c_lir.start_date, c_lir.end_date
from contract_lineitem c_lir
left join lineitem lir on lir.id = c_lir.lineitem_id
where c_lir.contract_id = 1 and c_lir.service_id = 3
and (c_lir.start_date <= '2015-05-01 00:00:00' or c_lir.start_date between '2015-05-01 00:00:00' and '2015-05-31 23:59:59')
and (c_lir.end_date is null or c_lir.end_date between '2015-05-01 00:00:00' and '2015-05-31 23:59:59' or c_lir.end_date >= '2015-05-31 23:59:59')

select svc.name service, svc.version, c_svc.onetime_revenue onetime, c_svc.recurring_revenue recurring, c_svc.start_date, c_svc.end_date
from contract_service c_svc
left join service svc on svc.id = c_svc.service_id
where c_svc.contract_id = 1
and (c_svc.start_date <= '2015-02-01 00:00:00' or c_svc.start_date between '2015-02-01 00:00:00' and '2015-02-28 23:59:59')
and (c_svc.end_date is null or c_svc.end_date between '2015-02-01 00:00:00' and '2015-02-28 23:59:59' or c_svc.end_date >= '2015-02-28 23:59:59')

update asset set asset_category_id = 1 where id = 6;

select category, detail, sum(cost_contribution) monthly_cost, sum(capacity) units, sum(cost_contribution)/sum(capacity) base_standard
from view_asset_detail_cost group by category, detail order by 1, 2

select category, sum(cost_contribution) category_monthly_cost from view_asset_detail_cost where category != 'Equipment' group by category

select
        (select name from customers cust where cust.id = c.customer_id)  as customer,
        sbi.name,
        su.start_date ,
        su.units,
        cp.pricing as rate,
        (su.units*cp.pricing) as price,
        (su.units*cp.pricing)*(timestamp(last_day(su.start_date),'23:59:59')-su.start_date)/(timestamp(last_day(su.start_date),'23:59:59')-timestamp(DATE_FORMAT('2014-02-18 16:13:45'  ,'%Y-%m-01'),'00:00:00')) as billable
from
        service_usage su,
        service_billable_items sbi,
        contracts c,
        contract_pricing cp
where
        su.stop_date is null and
        sbi.id = su.service_billable_item_id and
        c.id = su.contract_id and
        cp.service_billable_item_id = sbi.id;

select last_day(su.start_date) from service_usage su;
select timestamp(last_day(su.start_date),'23:59:59') from service_usage su;
select timestamp(DATE_FORMAT('2014-06-18 16:13:45'  ,'%Y-%m-01'),'00:00:00') as ts from service_usage su;
select DATE_FORMAT('2014-06-18 16:13:45'  ,'%Y-%m-01') as ts from service_usage su;
select timestamp(last_day(su.start_date),'23:59:59')-timestamp(DATE_FORMAT('2014-06-18 16:13:45'  ,'%Y-%m-01'),'00:00:00') as foo from service_usage su;

select (su.units*cp.pricing)*(timestamp(last_day(su.start_date),'23:59:59')-su.start_date)/
(timestamp(last_day(su.start_date),'23:59:59')-timestamp(DATE_FORMAT('2014-06-18 16:13:45'  ,'%Y-%m-01'),'00:00:00')) as ts
from service_usage su, contract_pricing cp;

