# ========================================================================
# Create Database Schema to capture the OAI Data Records 
# ========================================================================

# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# ListRecord Return tables (including multiple metadata items and multiple 
# set references 
# ISSUE: Technically metadata doesnt have to be dublin core. Need to check for this
# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

CREATE TABLE oai_identify (
    a_library_num int,  # Number allocated for library ident when scanning OAI List    
    a_responseDate datetime,
    repositoryName text,
    baseURL text,
    protocolVersion text,
    earliestDatestamp datetime,
    deletedRecord text,
    granularity text
) ; 

CREATE TABLE oaiI_adminEmail (
    a_library_num int,  # Number allocated for library ident when scanning OAI List    
    adminEmail text
) ;

CREATE TABLE oaiI_compression (
    a_library_num int,  # Number allocated for library ident when scanning OAI List    
    compression text
) ;

CREATE TABLE oaiI_description_1 (
    a_library_num        int,  # Number allocated for library ident when scanning OAI List
    oai_identifier       text,
    scheme               text, 
    repositoryIdentifier text,
    oaidelimiter         text, # delimiter in xml is a reserved word in SQL 
    sampleIdentifier     text

) ; 

CREATE TABLE oaiI_f_description_2 (
    a_library_num          int,  # Number allocated for library ident when scanning OAI List
    oai_identifier       text,
    scheme               text, 
    repositoryIdentifier text,
    oaidelimiter         text, 
    sampleIdentifier     text,
    title                text,
    content              text,
    metadataPolicy       text,
    dataPolicy           text
) ; 

CREATE TABLE repository_author (
    a_library_num          int,  # Number allocated for library ident when scanning OAI List
    title text,
    name  text,
    email text,
    version text
) ;

# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# ListRecord Return tables (including multiple metadata items and multiple 
# set references 
# ISSUE: Technically metadata doesnt have to be dublin core. Need to check for this
# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

CREATE TABLE oai_ListRecord ( # a number of foreign keys exist too
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  library_URL text, # URL of the library used to scan
  header_identifier varchar (256),
  header_datestamp datetime
) ;

CREATE TABLE oaiLR_dc_title (
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  lang text, 
  title text
) ;

CREATE TABLE oaiLR_dc_creator (
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  metadata_dc_creator text 
) ;

CREATE TABLE oaiLR_dc_description ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  lang text, 
  description text
) ;

CREATE TABLE oaiLR_dc_subject ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  subject text
) ;


CREATE TABLE oaiLR_dc_publisher ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  publisher text
) ;

CREATE TABLE oaiLR_dc_contributor ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  contributor text
) ;

CREATE TABLE oaiLR_dc_date ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  date text
) ;
 
CREATE TABLE oaiLR_dc_type ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  type text
) ;

CREATE TABLE oaiLR_dc_format ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  format text
) ;

CREATE TABLE oaiLR_dc_identifier ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  identifier text
) ;

CREATE TABLE oaiLR_dc_source ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  source text
) ;

CREATE TABLE oaiLR_dc_language ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  language text
) ;

CREATE TABLE oaiLR_dc_relation ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  relation text
) ;

CREATE TABLE oaiLR_dc_coverage ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  coverage text
) ;

CREATE TABLE oaiLR_dc_rights ( 
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  rights text
) ;

CREATE TABLE oai_record_f_setspec (
  a_library_num int,  # Number allocated for library ident when scanning OAI List
  header_identifier varchar (255),
  setSpec varchar (255)
) ;

# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# ListSet Return tables 
# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

CREATE TABLE oai_ListSet ( # a number of foreign keys exist too
  a_library_num int,  # Number allocated for library ident when scanning OAI List
    setSpec varchar (255),
    setName varchar (255),
	setDescription varchar (255)
) ;

# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# Country Information Table
# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

CREATE TABLE oai_supplemental (
    a_library_num int,  # Number allocated for library ident when scanning OAI List
    country_code text,
    country      text,
    region       text,
    source       text,
    MainURL      text,
    area         text,
    comment      text
) ;

# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# Gartner Information Table
# xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

CREATE TABLE gartner_topic_and_date (
  gyear     int    ,
  seq       int    ,
  topic     varchar (255) ,
  maturity  double ,
  gphase    int
) ;

CREATE TABLE gartner_topic (
  gyear        int    ,
  seq          int    ,
  topic        varchar (255) ,
  maturity     double ,
  gphase       int
) ;

CREATE TABLE gartner_topic_words (
  topic        varchar (255) ,
  topic_word   varchar (255) 
) ;
