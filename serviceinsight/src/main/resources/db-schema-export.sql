-- This is an alternate/fake Chronos Export table to use instead of the real one from Dick
-- Create it in a MySQL database called Service_Insight_Export with the same creds as the real one
--
drop table if exists Export_Table;
create table Export_Table (
	UID int	not null auto_increment,
	Ticket varchar(20),
	Date timestamp,
	Customer_ID varchar(32),
	Customer varchar(255),
	CI_ID varchar(32),
	CI_Name varchar(255),
	Service_Name varchar(255),
	Task_Description varchar(255),
	Hours Decimal(19,4),
	Num_CIs int,
	Name varchar(255),
	Team varchar(255),
	Labor_Type varchar(20),
	Inserted timestamp,
	parent_id int,
	primary key (UID)
) ENGINE=InnoDB;

