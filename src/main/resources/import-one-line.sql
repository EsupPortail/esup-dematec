--
-- Licensed to ESUP-Portail under one or more contributor license
-- agreements. See the NOTICE file distributed with this work for
-- additional information regarding copyright ownership.
--
-- ESUP-Portail licenses this file to you under the Apache License,
-- Version 2.0 (the "License"); you may not use this file except in
-- compliance with the License. You may obtain a copy of the License at:
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- SET statement_timeout = 0;
-- SET client_encoding = 'UTF8';
-- SET standard_conforming_strings = off;
-- SET check_function_bodies = false;
-- SET client_min_messages = warning;
-- SET escape_string_warning = off;
-- SET search_path = public, pg_catalog;
-- postgresql full text search
ALTER TABLE poste_candidature ADD COLUMN textsearchable_index_col tsvector;
UPDATE poste_candidature SET textsearchable_index_col = setweight(to_tsvector('simple', replace(coalesce(c_user.nom,''),'-',' ')), 'A') || setweight(to_tsvector('simple', replace(coalesce(c_user.prenom,''),'-',' ')), 'B') || setweight(to_tsvector('simple', coalesce(c_user.email_address,'')), 'B') || setweight(to_tsvector('simple', coalesce(c_user.num_candidat,'')), 'B') FROM c_user where poste_candidature.candidat=c_user.id; 
CREATE INDEX textsearch_idx ON poste_candidature USING gin(textsearchable_index_col); 
CREATE FUNCTION textsearchable_poste_candidature_trigger() RETURNS trigger AS $$ begin new.textsearchable_index_col := setweight(to_tsvector('simple', replace(coalesce(c_user.nom,''),'-',' ')), 'A') || setweight(to_tsvector('simple', replace(coalesce(c_user.prenom,''),'-',' ')), 'B') || setweight(to_tsvector('simple', coalesce(c_user.email_address,'')), 'B') || setweight(to_tsvector('simple', coalesce(c_user.num_candidat,'')), 'B') FROM c_user where new.candidat=c_user.id; return new; end $$ LANGUAGE plpgsql; 
CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE ON poste_candidature FOR EACH ROW EXECUTE PROCEDURE textsearchable_poste_candidature_trigger();
