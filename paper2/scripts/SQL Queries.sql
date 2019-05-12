----------------------------------
-- Get back metadata and title text
-----------------------------------

select 	a.a_library_num,
		a.header_identifier, 
		a.header_datestamp,
		b.title
from 
		oai_ListRecord a, 
		oaiLR_dc_title b 
where 
		b.lang="en-US" and 
		a.header_identifier = b.header_identifier;
	 
-----------------------------------
-- Title and Description
-----------------------------------
	 
select 	a.a_library_num,
		a.header_identifier, 
		a.header_datestamp,
		b.title,
		c.description
from 
		oai_ListRecord a, 
		oaiLR_dc_title b, 
		oaiLR_dc_description c 
where 
		b.lang="en-US" and 
		c.lang="en-US" and 
		a.header_identifier = b.header_identifier and 
		a.header_identifier = c.header_identifier;
		
		
-----------------------------------
-- Calculate some statistics
-----------------------------------
		
select distinct (a_library_num) from oai_record;  

select count(*) from oai_ListRecord;
select count(*) from oaiLR_dc_title;
select count(*) from oaiLR_dc_descr;
select count(*) from oaiLR_dc_creator;

-----------------------------------
-- Analysis queries
-----------------------------------

select * from oailr_dc_description where lang="en-US" and description like "%lithium%";

select a.description, b.header_datestamp from oailr_dc_description a , oai_ListRecord b where a.lang="en-US" and a.description like "%lithium%" and a.a_library_num = b.a_library_num and a.header_identifier = b.header_identifier order by b.header_datestamp;