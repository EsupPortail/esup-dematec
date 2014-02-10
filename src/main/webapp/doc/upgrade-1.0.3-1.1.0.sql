-- SET statement_timeout = 0;
-- SET client_encoding = 'UTF8';
-- SET standard_conforming_strings = off;
-- SET check_function_bodies = false;
-- SET client_min_messages = warning;
-- SET escape_string_warning = off;

-- SET search_path = public, pg_catalog;

alter table poste_candidature add column manager_review bigint;
WITH manager_review_id AS (
   UPDATE poste_candidature SET manager_review=nextval('hibernate_sequence') RETURNING manager_review
  )
INSERT INTO manager_review (id, review_status) SELECT manager_review,'Non_vue' FROM manager_review_id;


