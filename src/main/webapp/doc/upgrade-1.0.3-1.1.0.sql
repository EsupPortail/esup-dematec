/*
Commandes à exécuter pour passage de la version 1.0.3 à 1.1.0, 
à faire après avoir redémarré EsupDematEC avec hibernate.hbm2ddl.auto (persitence.xml) positionné à update 

Il faut également renseigner / compléter la configuration (éléments nouveaux depuis la 1.0.3, à vide après l'update) via l'IHM directement.

*/
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


