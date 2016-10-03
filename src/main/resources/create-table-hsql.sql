SET DATABASE SQL SYNTAX PGS true;
CREATE SCHEMA amor_test;


CREATE TABLE amor_test.meta_hydra (
	run_id bigint primary key,
	subject text not null,
	hydra text not null,
	added_meta_hydra timestamp default now()
);

CREATE TABLE amor_test.meta_table (
	run_id bigint primary key references amor_test.meta_hydra,
	logical_name text
);

CREATE TABLE amor_test.meta_state (
	run_id bigint primary key references amor_test.meta_hydra,
	state text
);

CREATE TABLE amor_test.amor_summaries (
	run_id bigint primary key references amor_test.meta_hydra,
	subject text not null,
	hydra text not null,
	summary_id text,
	title text,
	lang text,
	subject_title text,
	subject_lang text
);

	
CREATE TABLE amor_test.meta_ref_id_map (
	ref text primary key,
	map_id bigint not null
);

CREATE TABLE amor_test.x1000_fact (
	time_key bigint,
	area_key bigint,
	measure_key bigint,
	val text,
	constraint pk_fact primary key (time_key, area_key, measure_key) 
);

CREATE TABLE amor_test.x1000_tree (
	key bigint primary key,
	parent_key bigint references amor_test.x1000_tree,
	dim text not null,
	stage text not null,
	ref text not null
);

CREATE TABLE amor_test.x1000_meta (
	ref text,
	lang text,
	tag text,
	data text,
	constraint pk_meta primary key (ref, tag, lang)
);



INSERT INTO amor_test.meta_hydra(
	run_id,
	subject,
	hydra,
	added_meta_hydra
) values (
	1000,
	'x-subject',
	'test-hydra',
	to_date('2016-09-05','yyyy-mm-dd')
), (
	1001,
	'a-subject',
	'test-hydra',
	to_date('2016-09-03','yyyy-mm-dd')
), (
	1003,
	'a-subject',
	'b-hydra',
	to_date('2016-09-03','yyyy-mm-dd')
),
(
	1002,
	'third-subject',
	'test-hydra',
	to_date('2016-09-02','yyyy-mm-dd')
);

INSERT INTO amor_test.meta_state 
	(run_id, state) 
VALUES 
	(1000, 'test'),
	(1001, 'test'),
	(1003, 'test'),
	(1002, 'prod');
	
INSERT INTO amor_test.meta_table 
	(run_id, logical_name)
VALUES 
	(1000, 'fact'),
	(1001, 'fact'),
	(1003, 'fact'),
	(1002, 'fact');
	
	