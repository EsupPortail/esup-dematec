/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.batch;

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.EmailService;
import fr.univrouen.poste.services.PasswordService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service batch permettant de renvoyer un mail d'activation à tous les candidats
 * n'ayant pas encore activé leur compte (activationDate == null).
 */
@Service
public class ResendActivationMailService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    UserDao userDao;

    @Resource
    AppliConfigDao appliConfigDao;

    @Resource
    EmailService emailService;

    @Resource
    PasswordService passwordService;

    /**
     * Envoie un mail d'activation à chaque candidat dont le compte n'est pas encore activé.
     * Une nouvelle clé d'activation est générée et stockée pour chaque candidat concerné.
     */
    @Transactional
    public void resendActivationMails() {
        List<User> allUsers = userDao.findAllUsers();

        List<User> candidatsNonActives = allUsers.stream()
                .filter(u -> u.getNumCandidat() != null && !u.getNumCandidat().isEmpty())
                .filter(u -> u.getActivationDate() == null)
                .toList();

        logger.info("Nombre de candidats sans activation : {}", candidatsNonActives.size());

        String mailFrom    = appliConfigDao.getAppliConfig().getMailFrom();
        String mailSubject = appliConfigDao.getAppliConfig().getMailSubject();
        String mailTemplate = appliConfigDao.getAppliConfig().getTexteMailActivation();

        int sent = 0;
        int failed = 0;

        for (User user : candidatsNonActives) {
            // Génère une nouvelle clé d'activation
            String activationKey = passwordService.generateActivationKey();
            user.setActivationKey(activationKey);
            userDao.saveUser(user);

            String mailTo = user.getEmailAddress();
            String mailMessage = mailTemplate
                    .replaceAll("@@mailAddress@@", mailTo)
                    .replaceAll("@@activationKey@@", activationKey)
                    .replaceAll("@@nom@@",    WordUtils.capitalizeFully(user.getNom()))
                    .replaceAll("@@prenom@@", WordUtils.capitalizeFully(user.getPrenom()));

            boolean success = emailService.sendMessage(mailFrom, mailTo, mailSubject, mailMessage);
            if (success) {
                logger.info("Mail d'activation renvoyé à : {}", mailTo);
                sent++;
            } else {
                logger.warn("Échec d'envoi du mail d'activation à : {}", mailTo);
                failed++;
            }
        }

        logger.info("Renvoi des mails d'activation terminé. Envoyés : {}, Échecs : {}", sent, failed);
    }
}

