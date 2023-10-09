create view view_asset_detail_cost as
select assetc.name category, asset.name asset, assetd.name detail, assetd.id detail_id, assetd.capacity, asset.cost/asset.life cost_per_month, assetd.cost_fraction*(asset.cost/asset.life) cost_contribution
from asset_detail assetd
left join asset asset on assetd.asset_id = asset.id
left join asset_category assetc on asset.asset_category_id = assetc.id
where asset.disposal is null or asset.disposal > now()
order by assetc.name, asset.name, assetd.name;
