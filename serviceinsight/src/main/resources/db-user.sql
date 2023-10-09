CREATE USER 'siuser'@'localhost' IDENTIFIED BY 'L0gicalis';
GRANT ALL PRIVILEGES ON service_insight.* TO 'siuser'@'localhost' WITH GRANT OPTION;
CREATE USER 'siuser'@'%' IDENTIFIED BY 'L0gicalis';
GRANT ALL PRIVILEGES ON service_insight.* TO 'siuser'@'%' WITH GRANT OPTION;

