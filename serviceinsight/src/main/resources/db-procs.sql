drop procedure if exists asset_revenue;

delimiter //
create procedure asset_revenue
(in asset_id bigint, out monthly_cost Decimal (19,2))
begin
  select cost / life into monthly_cost from asset where id = asset_id;
end //

delimiter ;

-- test asset_revenue
call asset_revenue(1, @ar);
select @ar;

