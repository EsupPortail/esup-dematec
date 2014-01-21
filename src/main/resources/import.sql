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


-- POSTGRESQL LO TRIGGER
-- Only super-user can create extension lo
-- CREATE EXTENSION lo;
-- CREATE TRIGGER t_big_file BEFORE UPDATE OR DELETE ON big_file  FOR EACH ROW EXECUTE PROCEDURE lo_manage(binary_file);

-- ADMIN USER
insert into c_user (id, activation_date, activation_key, email_address, enabled, is_admin, is_manager, is_super_manager, password, version, login_failed_nb, login_failed_time) 
values (1, NULL, NULL, 'admin', TRUE, TRUE, FALSE, FALSE, '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 1, 0, 0);


--  appli_config
INSERT INTO appli_config (
id, 
texte_candidat_aide_candidature_depot, 
texte_candidat_aide_candidatures, 
mail_from, 
mail_subject, 
texte_mail_activation, 
texte_mail_password_oublie, 
texte_membre_aide_candidatures, 
texte_premiere_page_anonyme, 
texte_premiere_page_candidat, 
texte_premiere_page_membre, 
titre, 
version, 
pied_page, 
image_url,
mail_subject_membre, 
texte_mail_activation_membre,
mail_return_receipt_mode_type,
texte_mail_candidat_return_receipt,
texte_entete_mail_candidat_auditionnable text,
texte_piedpage_mail_candidat_auditionnable text) 

VALUES (

2, 

'<div class="well help"><p>Merci de sélectionner un fichier puis de valider votre sélection pour l''envoi effectif.</p></div>', 

'<div class="well help">  <p>Voici ci-dessous la liste de vos différentes candidatures.</p>  
<p>Merci de cliquer sur les boutons "voir" des différentes candidatures afin de les compléter (dépôt de fichiers).</p></div>', 

'EsupDematEC@univ-maville.fr',

'Université ESUP - Recrutement EC 2013',

E'Bonjour.\n
Nous venons de vous créer un compte sur l''application de l''Université ESUP.\n
Merci d''activer celui-ci via ce lien :\n
http://localhost:8080/EsupDematEC/signup/activate/@@mailAddress@@/@@activationKey@@\n
--\nUniversité ESUP - Direction des Ressources Humaines',  

E'Bonjour,\n\nVous avez demandé à récupérer un nouveau password. \n\nVotre nouveau password est:\n\n @@newPassword@@ \n
http://localhost:8080/PosteCandidature/changepassword/index \n
--\nUniversité ESUP - Direction des Ressources Humaines',

'<div class="well help">  <p>En tant que membre (d''une commission) sur un ou plusieurs postes donnés, vous pouvez consulter ici les différentes candidatures liées à ce ou    ces poste(s).</p>
<p>Pour cela, cliquez sur le bouton "voir" du poste souhaité.</p></div>', 

'<div>  
<p>    <span class="badge badge-success">1</span>    Enregistrement des candidatures sur Galaxie ( <a href="https://www.galaxie.enseignementsup-recherche.gouv.fr/ensup/candidats.html">https://www.galaxie.enseignementsup-recherche.gouv.fr/ensup/candidats.html</a>    ).  </p>
<p class="important">    <span class="badge badge-important">2</span>    Aucun dossier ou dépôt de pièce complémentaire ne sera accepté après le ****.  </p>
<p>    <span class="badge badge-success">3</span>    Le fichier numérique devra être <b>exclusivement au format PDF</b> et contenir toutes les pièces exigées par la réglementation en vigueur.  </p>
<div class="encart">    <span class="badge badge-warning">4</span>    Si vous venez de vous inscrire sur le portail Galaxie, vos identifiant et mot de passe vont être automatiquement générés et envoyés <b>au      plus tard 48h</b> après votre inscription, à l''adresse e-mail indiquée sur Galaxie.  </div></div>', 

'<div class="hero-unit">
<h3>Bienvenue</h3>
<p>Cette application permet de gérer le dépôt des dossiers de candidatures dans le cadre du recrutement des enseignants-chercheurs et ATER.</p>
</div>', 

'<div class="hero-unit"><h3>Bienvenue</h3><p>Cette application permet de gérer le dépôt des dossiers de candidatures dans le cadre du recrutement des enseignants-chercheurs et ATER.</p></div>', 

'Recrutement des enseignants-chercheurs', 

1, 

'<footer  id="footer" class="footer" >    
<span><a href="http://www.esup-portail.org/">Université ESUP</a></span> - 
<span>Direction des Ressources Humaines</span></footer><small id="mention-legale">   
<p>Les informations recueillies font l’objet d’un traitement informatique destiné à la gestion dématérialisée des campagnes de recrutement      d''Enseignants-Chercheurs &amp; d''Attachés Temporaires d''Enseignement et de Recherche (ATER).         </p>        
<p>     Nous vous informons que les données vous concernant sont        informatisées. Elles ne sont pas transmises à des tiers et seront traitées de façon confidentielle. Ces données sont conservées pour la durée de la     campagne de recrutement.        </p>        
<p>Conformément à la loi « informatique et libertés » du 6 janvier 1978 modifiée en 2004, vous bénéficiez d’un droit d’accès        et de rectification aux informations qui vous concernent, que vous pouvez exercer en vous adressant à :     </p>        
<p>*** Adresse de l''université *** </p></small>', 

'/resources/images/logo-esup-dematec.png',

'Université ESUP - Recrutement EC 2013',

E'Bonjour.\n\nEn votre qualité de membre du comité de sélection, nous venons de vous créer un compte sur l''application de l''Université ESUP afin de vous permettre d''avoir accès aux dossiers des candidats.\n
Merci d''activer ce lien en le copiant dans la barre d''adresse de votre navigateur Internet :\n\
http://localhost:8080/EsupDematEC/signup/activate/@@mailAddress@@/@@activationKey@@\n
Il vous sera demandé de saisir le mot de passe de votre choix.\n
Cordialement.\n\n--\nUniversité ESUP\nDirection des Ressources Humaines\nBureau des Personnels Enseignants\n',

'NEVER',

E'Bonjour.\n\nVotre fichier @@fileName@@ a bien été chargé dans l''application de l''Université ESUP pour le poste @@numEmploi@@.\n\n--\nUniversité ESUP - Direction des Ressources Humaines\n',

E'Bonjour.\n\nVous avez été retenu pour audition sur le poste @@numEmploi@@.',

E'Merci de compléter le dossier de cette candidature via l''application de l''Université ESUP :\nhttp://localhost:8080/PosteCandidature/\n\n--\nUniversité ESUP - Direction des Ressources Humaines\n'

);


