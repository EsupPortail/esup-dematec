-- PostgreSQL / psql. Schéma public, mappings 2.0 antérieurs au commit 2b8fc67.
-- Prépare la base pour le code 2.0 réaligné sur le schéma 1.9.5 (pas le binaire 1.0).
-- Arrêter TOUTES les instances applicatives et tous les batchs ; sauvegarder la
-- base complète, Large Objects compris, et valider sur une restauration de recette.
-- Simulation par défaut : psql -X -d BASE -f realign-2.0-to-1.9.5.sql
-- Application explicite : psql -X -v apply=true -d BASE -f realign-2.0-to-1.9.5.sql
-- Aucun document supprimé. Toute divergence entre deux représentations provoque
-- une erreur et l'annulation de la transaction : ne pas contourner ce contrôle.
-- Ne pas redémarrer l'ancien WAR sur la base convertie.
\set ON_ERROR_STOP on
\if :{?apply}
\else
  \set apply false
\endif
BEGIN;
SET LOCAL search_path TO public, pg_temp;
SET LOCAL lock_timeout TO '5s';
SET LOCAL statement_timeout TO '10min';

DO $$
DECLARE t record;
BEGIN
  IF to_regclass('public.poste_candidature') IS NULL
     OR to_regclass('public.big_file') IS NULL
     OR to_regclass('public.c_user') IS NULL THEN
    RAISE EXCEPTION 'Base/schema inattendu : tables applicatives absentes de public';
  END IF;
  FOR t IN SELECT tablename FROM pg_tables WHERE schemaname='public'
           AND (tablename IN ('appli_config','appli_config_file_type','appli_version','big_file','c_user',
             'commission_entry','commission_excel','galaxie_entry','galaxie_excel','galaxie_mapping',
             'log_auth','log_file','log_import_commission','log_import_galaxie','log_mail','log_poste_file',
             'manager_review','member_review_file','poste_candidature','poste_candidature_file',
             'poste_candidature_tag','poste_candidature_tag_value','posteapourvoir','posteapourvoir_file','template_file')
             OR tablename LIKE 'poste_candidature_%' OR tablename LIKE 'postecandidature_%'
             OR tablename LIKE 'posteapourvoir_%') ORDER BY tablename
  LOOP
    EXECUTE format('LOCK TABLE public.%I IN ACCESS EXCLUSIVE MODE',t.tablename);
  END LOOP;
END $$;

CREATE OR REPLACE FUNCTION pg_temp.has_column(t text, c text) RETURNS boolean
LANGUAGE sql AS $$
  SELECT EXISTS(SELECT 1 FROM information_schema.columns
                WHERE table_schema='public' AND table_name=t AND column_name=c);
$$;

-- Renomme si la cible manque ; si les deux existent, la cible doit être
-- entièrement vide ou strictement identique à la source (NULL compris).
CREATE OR REPLACE FUNCTION pg_temp.realign_column(t text, src text, dst text) RETURNS void
LANGUAGE plpgsql AS $$
DECLARE populated boolean; different boolean; n bigint;
BEGIN
  IF NOT pg_temp.has_column(t,src) THEN
    IF NOT pg_temp.has_column(t,dst) THEN
      RAISE EXCEPTION 'Colonnes manquantes : %.% et %.%',t,src,t,dst;
    END IF;
    RETURN;
  END IF;
  IF NOT pg_temp.has_column(t,dst) THEN
    EXECUTE format('ALTER TABLE public.%I RENAME COLUMN %I TO %I',t,src,dst);
    RAISE NOTICE 'Renommage %.% -> %',t,src,dst;
    RETURN;
  END IF;
  EXECUTE format('SELECT EXISTS(SELECT 1 FROM public.%I WHERE %I IS NOT NULL)',t,dst) INTO populated;
  EXECUTE format('SELECT EXISTS(SELECT 1 FROM public.%I WHERE %I IS DISTINCT FROM %I)',t,dst,src) INTO different;
  IF populated AND different THEN
    RAISE EXCEPTION 'Conflit %.% / % : valeurs divergentes (NULL compris). Audit manuel requis.',t,src,dst;
  END IF;
  EXECUTE format('UPDATE public.%I SET %I=%I WHERE %I IS DISTINCT FROM %I',t,dst,src,dst,src);
  GET DIAGNOSTICS n=ROW_COUNT;
  RAISE NOTICE '%.% : % lignes reprises depuis %',t,dst,n,src;
END $$;

SELECT pg_temp.realign_column(t,'bigfile','big_file')
FROM unnest(ARRAY['commission_excel','galaxie_excel','member_review_file',
                  'posteapourvoir_file','poste_candidature_file','template_file']) AS t;
SELECT pg_temp.realign_column('poste_candidature_file','filetype','file_type');
SELECT pg_temp.realign_column('commission_entry','membre_id','membre');
SELECT pg_temp.realign_column('commission_entry','poste_id','poste');

-- Garantit les références des colonnes cibles préexistantes mais vides.
-- Un renommage conserve normalement déjà les contraintes de la colonne source.
DO $$
DECLARE r record; colnum smallint;
BEGIN
  FOR r IN SELECT * FROM (VALUES
    ('commission_excel','big_file','big_file'),('galaxie_excel','big_file','big_file'),
    ('member_review_file','big_file','big_file'),('posteapourvoir_file','big_file','big_file'),
    ('poste_candidature_file','big_file','big_file'),('template_file','big_file','big_file'),
    ('poste_candidature_file','file_type','appli_config_file_type'),
    ('commission_entry','membre','c_user'),('commission_entry','poste','posteapourvoir')
  ) AS v(tab,col,ref)
  LOOP
    SELECT attnum INTO colnum FROM pg_attribute
      WHERE attrelid=to_regclass(format('public.%I',r.tab)) AND attname=r.col AND NOT attisdropped;
    IF NOT EXISTS(SELECT 1 FROM pg_constraint
                   WHERE conrelid=to_regclass(format('public.%I',r.tab)) AND contype='f'
                     AND conkey=ARRAY[colnum] AND confrelid=to_regclass(format('public.%I',r.ref))) THEN
      EXECUTE format('ALTER TABLE public.%I ADD FOREIGN KEY (%I) REFERENCES public.%I(id)',r.tab,r.col,r.ref);
    END IF;
  END LOOP;
END $$;

CREATE OR REPLACE FUNCTION pg_temp.realign_links(dst text, definition text, cols text,
                                                source_query text, source_present boolean) RETURNS void
LANGUAGE plpgsql AS $$
DECLARE populated boolean; different boolean; n bigint;
BEGIN
  IF NOT source_present THEN
    IF to_regclass(format('public.%I',dst)) IS NULL THEN
      RAISE EXCEPTION 'Aucune représentation connue de la relation %',dst;
    END IF;
    RETURN;
  END IF;
  IF to_regclass(format('public.%I',dst)) IS NULL THEN
    EXECUTE format('CREATE TABLE public.%I (%s)',dst,definition);
  END IF;
  EXECUTE format('SELECT EXISTS(SELECT 1 FROM public.%I)',dst) INTO populated;
  IF populated THEN
    EXECUTE format('SELECT EXISTS((SELECT %s FROM public.%I EXCEPT %s) UNION ALL '
                   '(%s EXCEPT SELECT %s FROM public.%I))',cols,dst,source_query,source_query,cols,dst)
      INTO different;
    IF different THEN
      RAISE EXCEPTION 'Relation % : anciennes/nouvelles associations divergentes. Audit manuel requis.',dst;
    END IF;
    RAISE NOTICE '% : associations déjà identiques',dst;
    RETURN;
  END IF;
  EXECUTE format('INSERT INTO public.%I (%s) %s',dst,cols,source_query);
  GET DIAGNOSTICS n=ROW_COUNT;
  RAISE NOTICE '% : % associations reprises',dst,n;
END $$;

-- Les deux références de candidature ajoutées en 2.0 ne doivent pas se contredire.
DO $$ BEGIN
  IF pg_temp.has_column('poste_candidature_file','postecandidature_candidaturefiles')
     AND pg_temp.has_column('poste_candidature_file','poste_candidature') THEN
    IF EXISTS(SELECT 1 FROM poste_candidature_file
              WHERE poste_candidature IS NOT NULL
                AND poste_candidature IS DISTINCT FROM postecandidature_candidaturefiles) THEN
      RAISE EXCEPTION 'Deux références candidature divergentes dans poste_candidature_file';
    END IF;
  END IF;
END $$;

SELECT pg_temp.realign_links('poste_candidature_candidature_files',
  'poste_candidature bigint NOT NULL REFERENCES public.poste_candidature(id),
   candidature_files bigint NOT NULL UNIQUE REFERENCES public.poste_candidature_file(id),
   PRIMARY KEY(poste_candidature,candidature_files)',
  'poste_candidature,candidature_files',
  'SELECT postecandidature_candidaturefiles,id FROM public.poste_candidature_file WHERE postecandidature_candidaturefiles IS NOT NULL',
  pg_temp.has_column('poste_candidature_file','postecandidature_candidaturefiles'));
SELECT pg_temp.realign_links('poste_candidature_member_review_files',
  'poste_candidature bigint NOT NULL REFERENCES public.poste_candidature(id),
   member_review_files bigint NOT NULL UNIQUE REFERENCES public.member_review_file(id),
   PRIMARY KEY(poste_candidature,member_review_files)',
  'poste_candidature,member_review_files',
  'SELECT postecandidature_memberreviewfiles,id FROM public.member_review_file WHERE postecandidature_memberreviewfiles IS NOT NULL',
  pg_temp.has_column('member_review_file','postecandidature_memberreviewfiles'));
SELECT pg_temp.realign_links('posteapourvoir_poste_files',
  'posteapourvoir bigint NOT NULL REFERENCES public.posteapourvoir(id),
   poste_files bigint NOT NULL UNIQUE REFERENCES public.posteapourvoir_file(id),
   PRIMARY KEY(posteapourvoir,poste_files)',
  'posteapourvoir,poste_files',
  'SELECT posteapourvoir_postefiles,id FROM public.posteapourvoir_file WHERE posteapourvoir_postefiles IS NOT NULL',
  pg_temp.has_column('posteapourvoir_file','posteapourvoir_postefiles'));
SELECT pg_temp.realign_links('poste_candidature_tag_values',
  'poste_candidature_tag bigint NOT NULL REFERENCES public.poste_candidature_tag(id),
   "values" bigint NOT NULL UNIQUE REFERENCES public.poste_candidature_tag_value(id),
   PRIMARY KEY(poste_candidature_tag,"values")',
  'poste_candidature_tag,"values"',
  'SELECT postcandidaturetag_values,id FROM public.poste_candidature_tag_value WHERE postcandidaturetag_values IS NOT NULL',
  pg_temp.has_column('poste_candidature_tag_value','postcandidaturetag_values'));
SELECT pg_temp.realign_links('posteapourvoir_membres',
  'postes bigint NOT NULL REFERENCES public.posteapourvoir(id),
   membres bigint NOT NULL REFERENCES public.c_user(id), PRIMARY KEY(postes,membres)',
  'postes,membres','SELECT posteapourvoir,membres FROM public.posteapourvoir_user',
  to_regclass('public.posteapourvoir_user') IS NOT NULL);
SELECT pg_temp.realign_links('posteapourvoir_presidents',
  'posteapourvoir bigint NOT NULL REFERENCES public.posteapourvoir(id),
   presidents bigint NOT NULL REFERENCES public.c_user(id), PRIMARY KEY(posteapourvoir,presidents)',
  'posteapourvoir,presidents','SELECT posteapourvoir,presidents FROM public.posteapourvoir_user_1',
  to_regclass('public.posteapourvoir_user_1') IS NOT NULL);
SELECT pg_temp.realign_links('poste_candidature_reporters',
  'poste_candidature bigint NOT NULL REFERENCES public.poste_candidature(id),
   reporters bigint NOT NULL REFERENCES public.c_user(id), PRIMARY KEY(poste_candidature,reporters)',
  'poste_candidature,reporters','SELECT postecandidature,reporters FROM public.postecandidature_user',
  to_regclass('public.postecandidature_user') IS NOT NULL);
SELECT pg_temp.realign_links('poste_candidature_tags',
  'poste_candidature bigint NOT NULL REFERENCES public.poste_candidature(id),
   tags bigint NOT NULL REFERENCES public.poste_candidature_tag_value(id),
   tags_key bigint NOT NULL REFERENCES public.poste_candidature_tag(id),
   PRIMARY KEY(poste_candidature,tags_key)',
  'poste_candidature,tags,tags_key',
  'SELECT postecandidature,tags_id,tags_key FROM public.postecandidature_postcandidaturetagvalue',
  to_regclass('public.postecandidature_postcandidaturetagvalue') IS NOT NULL);

-- Archive les anciennes tables sans références contraignantes vers les données
-- actives. Ces instantanés ne suivent plus les écritures de la campagne.
CREATE SCHEMA IF NOT EXISTS dematec_before_realign;
DO $$
DECLARE t text; c record; obsolete record;
BEGIN
  FOREACH t IN ARRAY ARRAY['posteapourvoir_user','posteapourvoir_user_1',
                           'postecandidature_user','postecandidature_postcandidaturetagvalue']
  LOOP
    IF to_regclass(format('public.%I',t)) IS NOT NULL THEN
      IF to_regclass(format('dematec_before_realign.%I',t)) IS NOT NULL THEN
        RAISE EXCEPTION 'Archive déjà présente pour % : audit manuel requis',t;
      END IF;
      FOR c IN SELECT conname FROM pg_constraint
               WHERE conrelid=to_regclass(format('public.%I',t)) AND contype='f'
      LOOP
        EXECUTE format('ALTER TABLE public.%I DROP CONSTRAINT %I',t,c.conname);
      END LOOP;
      EXECUTE format('ALTER TABLE public.%I SET SCHEMA dematec_before_realign',t);
    END IF;
  END LOOP;
  -- Les colonnes inutilisées sont conservées, sans FK qui bloquerait les
  -- modifications/suppressions réalisées par les nouveaux mappings.
  FOR obsolete IN SELECT * FROM (VALUES
    ('commission_excel','bigfile'),('galaxie_excel','bigfile'),
    ('member_review_file','bigfile'),('posteapourvoir_file','bigfile'),
    ('poste_candidature_file','bigfile'),('template_file','bigfile'),
    ('poste_candidature_file','filetype'),('commission_entry','membre_id'),
    ('commission_entry','poste_id'),('poste_candidature_file','poste_candidature'),
    ('poste_candidature_file','postecandidature_candidaturefiles'),
    ('member_review_file','postecandidature_memberreviewfiles'),
    ('posteapourvoir_file','posteapourvoir_postefiles'),
    ('poste_candidature_tag_value','postcandidaturetag_values')
  ) AS v(tab,col)
  LOOP
    FOR c IN SELECT pc.conname FROM pg_constraint pc
             JOIN pg_attribute a ON a.attrelid=pc.conrelid AND a.attnum=ANY(pc.conkey)
             WHERE pc.conrelid=to_regclass(format('public.%I',obsolete.tab))
               AND pc.contype='f' AND a.attname=obsolete.col
    LOOP
      EXECUTE format('ALTER TABLE public.%I DROP CONSTRAINT %I',obsolete.tab,c.conname);
    END LOOP;
  END LOOP;
END $$;

-- Rétablit le verrouillage optimiste Roo sans écraser les compteurs existants.
DO $$
DECLARE t text;
BEGIN
  FOREACH t IN ARRAY ARRAY['appli_config','appli_config_file_type','appli_version','big_file','c_user',
    'commission_entry','commission_excel','galaxie_entry','galaxie_excel','galaxie_mapping',
    'log_auth','log_file','log_import_commission','log_import_galaxie','log_mail','log_poste_file',
    'manager_review','member_review_file','poste_candidature','poste_candidature_file',
    'poste_candidature_tag','poste_candidature_tag_value','posteapourvoir','posteapourvoir_file','template_file']
  LOOP
    EXECUTE format('ALTER TABLE public.%I ADD COLUMN IF NOT EXISTS version integer',t);
    EXECUTE format('UPDATE public.%I SET version=0 WHERE version IS NULL',t);
  END LOOP;
END $$;

-- Ne copie, ne supprime et ne réencode aucun Large Object.
DO $$ BEGIN
  IF EXISTS(SELECT 1 FROM public.big_file b LEFT JOIN pg_largeobject_metadata l ON l.oid=b.binary_file
            WHERE b.binary_file IS NOT NULL AND l.oid IS NULL) THEN
    RAISE EXCEPTION 'Large Objects manquants : restaurer/analyser la sauvegarde avant reprise';
  END IF;
END $$;
CREATE EXTENSION IF NOT EXISTS lo WITH SCHEMA public;
DO $$ BEGIN
  IF NOT EXISTS(SELECT 1 FROM pg_trigger WHERE tgrelid='public.big_file'::regclass
                AND NOT tgisinternal AND tgfoid='public.lo_manage()'::regprocedure) THEN
    CREATE TRIGGER t_big_file BEFORE UPDATE OR DELETE ON public.big_file
      FOR EACH ROW EXECUTE FUNCTION public.lo_manage(binary_file);
  END IF;
END $$;

-- RESTART est transactionnel, contrairement à setval : la simulation ou une
-- erreur ne modifie pas la séquence. Toutes les instances doivent être arrêtées.
CREATE SEQUENCE IF NOT EXISTS public.hibernate_sequence;
DO $$
DECLARE t text; n bigint; highest bigint:=0; previous bigint;
BEGIN
  FOREACH t IN ARRAY ARRAY['appli_config','appli_config_file_type','appli_version','big_file','c_user',
    'commission_entry','commission_excel','galaxie_entry','galaxie_excel','galaxie_mapping',
    'log_auth','log_file','log_import_commission','log_import_galaxie','log_mail','log_poste_file',
    'manager_review','member_review_file','poste_candidature','poste_candidature_file',
    'poste_candidature_tag','poste_candidature_tag_value','posteapourvoir','posteapourvoir_file','template_file']
  LOOP
    EXECUTE format('SELECT coalesce(max(id),0) FROM public.%I',t) INTO n;
    highest:=greatest(highest,n);
  END LOOP;
  SELECT last_value INTO previous FROM public.hibernate_sequence;
  highest:=greatest(highest,previous)+1;
  EXECUTE format('ALTER SEQUENCE public.hibernate_sequence INCREMENT BY 1 RESTART WITH %s',highest);
  RAISE NOTICE 'Prochain identifiant commun : %',highest;
END $$;

SELECT 'candidatures' AS controle,count(*) FROM public.poste_candidature
UNION ALL SELECT 'pièces candidature',count(*) FROM public.poste_candidature_candidature_files
UNION ALL SELECT 'rapports',count(*) FROM public.poste_candidature_member_review_files
UNION ALL SELECT 'fichiers poste',count(*) FROM public.posteapourvoir_poste_files
UNION ALL SELECT 'templates',count(*) FROM public.template_file
UNION ALL SELECT 'membres',count(*) FROM public.posteapourvoir_membres
UNION ALL SELECT 'présidents',count(*) FROM public.posteapourvoir_presidents
UNION ALL SELECT 'rapporteurs',count(*) FROM public.poste_candidature_reporters
UNION ALL SELECT 'valeurs de tags',count(*) FROM public.poste_candidature_tag_values
UNION ALL SELECT 'tags candidature',count(*) FROM public.poste_candidature_tags;

-- Réexécution possible avant reprise des écritures uniquement. Après la reprise,
-- les colonnes/instantanés obsolètes ne sont plus une source de conversion fiable.
-- Pour revenir en arrière, restaurer la sauvegarde avec son WAR/configuration.
\if :apply
COMMIT;
\echo 'Conversion appliquée. Démarrer uniquement le code aux mappings réalignés.'
\else
ROLLBACK;
\echo 'SIMULATION : aucune modification conservée. Pour appliquer : -v apply=true'
\endif
