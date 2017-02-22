CREATE SCHEMA amor_prod;
SET SCHEMA amor_prod;

CREATE TABLE meta_hydra
(
  run_id bigint NOT NULL,
  subject character varying(50) NOT NULL,
  hydra character varying(50) NOT NULL,
  added_meta_hydra timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT meta_hydra_pkey PRIMARY KEY (run_id)
);

CREATE TABLE meta_state
(
  run_id bigint NOT NULL,
  state character varying(4) NOT NULL,
  added_meta_state timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT meta_state_pkey PRIMARY KEY (run_id, state),
  CONSTRAINT meta_state_run_id_fkey FOREIGN KEY (run_id)
      REFERENCES meta_hydra (run_id) 
);

CREATE TABLE meta_table
(
  run_id bigint,
  logical_name character varying(100) NOT NULL,
  physical_name character varying(100) NOT NULL,
  added_meta_table timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT meta_table_run_id_fkey FOREIGN KEY (run_id)
      REFERENCES meta_hydra (run_id)
);

CREATE TABLE amor_prod.meta_ref_id_map
(
  map_id SERIAL NOT NULL,
  ref text,
  CONSTRAINT meta_ref_id_map_pkey PRIMARY KEY (map_id)
);

CREATE TABLE amor_prod.meta_map_in_use
(
  map_id integer NOT NULL,
  run_id bigint NOT NULL,
  CONSTRAINT meta_map_in_use_pkey PRIMARY KEY (map_id, run_id)
)

CREATE TABLE user_log
(
  log_id text NOT NULL,
  subject text,
  hydra text,
  run_id text,
  fact text,
  host text,
  ip_addr text,
  session_id text,
  view text,
  filter_zero character(1),
  filter_empty character(1),
  show_codes character(1),
  ts timestamp without time zone DEFAULT now(),
  CONSTRAINT user_log_pkey PRIMARY KEY (log_id)
);

CREATE TABLE user_log_selection
(
  log_id text,
  dimension text,
  node text,
  usage character(1),
  CONSTRAINT user_log_selection_log_id_fkey FOREIGN KEY (log_id)
      REFERENCES amor_prod.user_log (log_id) 
);


CREATE OR REPLACE VIEW meta_state_current AS 
 SELECT meta_hydra.subject, meta_hydra.hydra, meta_state.state, max(meta_hydra.run_id) AS run_id
   FROM meta_hydra
NATURAL JOIN meta_state
  GROUP BY meta_hydra.subject, meta_hydra.hydra, meta_state.state;

CREATE OR REPLACE VIEW meta_table_current AS 
 SELECT meta_state_current.subject, meta_state_current.hydra, meta_state_current.state, meta_state_current.run_id, meta_table.logical_name, meta_table.physical_name
   FROM amor_prod.meta_state_current
NATURAL JOIN meta_table;


CREATE TABLE public.template_summary
(
  run_id bigint,
  summary_id text,
  summary_xml text
);

CREATE OR REPLACE FUNCTION public.amor_summaries(p_schemaname text)
  RETURNS SETOF public.template_summary AS
$BODY$
  DECLARE
    hydra record;
    summary template_summary%rowtype;
  BEGIN
        FOR hydra IN 
		select schemaname::text, tablename::text, substring(tablename from 2 for (strpos(tablename, '_')  - 2))::bigint as run_id
		from pg_tables 
		where tablename like '%amor_summary' 
		and schemaname = p_schemaname
		LOOP

		FOR summary IN
			EXECUTE 
				'SELECT '|| hydra.run_id ||'::bigint, s.*
				 FROM
				 '|| hydra.schemaname || '.' || hydra.tablename || ' as s' LOOP
			RETURN NEXT summary;
		END LOOP;
		
	END LOOP;
  END;
$BODY$
LANGUAGE plpgsql;

create or replace function amor_prod.f_cube_property(
	p_subject text,
	p_hydra text,
	p_state text,
	p_cube text,
	p_predicate text,
	p_lang text
) returns text
as $$
declare 
	v_runid text;
	v_value text;
begin
	select run_id into v_runid
	from amor_prod.meta_state_current 
	where subject = p_subject
	and hydra = p_hydra
	and state = p_state;

	raise notice 'run_id: %', v_runid; 
	execute 'select data
		from amor_prod.x'|| v_runid ||'_meta 
		where ref in (
			select ref from amor_prod.x'|| v_runid ||'_meta 
			where data = ''' || p_cube || '''
		)
		and tag = $1
		and lang = $2' into v_value using p_predicate, p_lang ;

	return v_value;
end
$$ language plpgsql;


grant execute on function amor_prod.f_cube_property(text,text,text,text,text,text) to amor_user;


create or replace function amor_prod.f_summary_name(
	p_subject text,
	p_hydra text,
	p_state text,
	p_summary text,
	p_lang text
) returns text
as $$
declare 
	v_runid text;
	v_value text;
begin
	execute
	'with x as 
		(select 
		  summary_xml as col,
		  row_number() over (partition by subject, hydra, summary_id order by run_id desc) rn
		from amor_prod.amor_summaries
		join amor_prod.meta_hydra using (run_id)
		join amor_prod.meta_state_current using (subject, hydra, run_id)
		join amor_prod.meta_table using (run_id)
			where state = $1
			and subject = $2
			and summary_id  = $3
			and hydra = $4) 
	select title::text from (
		select 
			unnest(xpath(''/summary/title/text()'', col::xml)) title,
			unnest(xpath(''/summary/title/@lang'', col::xml)) lang
		from x
		where rn = 1
	) d where lang::text = $5
	'
	into v_value 
	using p_state, p_subject, p_summary, p_hydra, p_lang;
	
	return v_value;
exception 
	when invalid_xml_content 
	then return 'n/a';
	
end
$$ language plpgsql;

grant execute on function amor_prod.f_summary_name(text,text,text,text,text) to amor_user;



