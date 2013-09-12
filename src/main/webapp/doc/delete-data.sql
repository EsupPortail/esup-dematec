-- SET statement_timeout = 0;
-- SET client_encoding = 'UTF8';
-- SET standard_conforming_strings = off;
-- SET check_function_bodies = false;
-- SET client_min_messages = warning;
-- SET escape_string_warning = off;

-- SET search_path = public, pg_catalog;

delete from galaxie_entry;
delete from galaxie_excel;
delete from commission_entry;
delete from commission_excel;
delete from log_auth;
delete from log_file;
delete from log_import_commission;
delete from log_import_galaxie;
delete from log_mail;
delete from poste_candidature_candidature_files;
delete from poste_candidature_file;
delete from poste_candidature;
delete from posteapourvoir_membres;
delete from posteapourvoir;
delete from c_user where is_admin=false and is_manager=false and is_super_manager=false;

-- na pas faire de truncate sur big_file pour appel du trigger et suppression effective du blob
delete from big_file;

VACUUM FULL;


