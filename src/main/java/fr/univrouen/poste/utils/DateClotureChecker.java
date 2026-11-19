package fr.univrouen.poste.utils;

import fr.univrouen.poste.dao.AppliConfigDao;
import fr.univrouen.poste.dao.PosteAPourvoirDao;
import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.dao.UserDao;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class DateClotureChecker {

    @Resource
    Clock clock;

	@Resource
	AppliConfigDao appliConfigDao;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	UserDao userDao;

    @Resource
    PosteAPourvoirDao posteAPourvoirDao;

    public boolean isCurrentTimeOk4ThisCandidat(User targetUser) {
        if (!targetUser.getIsCandidat()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        AppliConfig config = appliConfigDao.getAppliConfig();
        Page<PosteCandidature> candidatures = posteCandidatureDao.findPosteCandidaturesByCandidat(targetUser);
        Set<Long> auditionPostIds = new HashSet<>();
        boolean hasRegularApplication = false;
        for (PosteCandidature candidature : candidatures) {
            if (candidature.getAuditionnable()) {
                auditionPostIds.add(candidature.getPoste().getId());
                if (isBeforeDeadline(now, candidature.getPoste().getDateEndCandidatAuditionnable())) {
                    return true;
                }
            } else {
                hasRegularApplication = true;
            }
        }

        boolean localMode = config != null && Boolean.TRUE.equals(config.getCandidatCanSignup());
        if (auditionPostIds.isEmpty() || !localMode && hasRegularApplication) {
            LocalDateTime deadline = getCandidateDeadline(targetUser, config);
            if (deadline == null || now.isBefore(deadline)) {
                return true;
            }
        }

        return localMode && posteAPourvoirDao.findPosteAPourvoirsByDateEndSignupCandidatGreaterThan(now)
                .getResultList().stream().anyMatch(poste -> !auditionPostIds.contains(poste.getId()));
    }

    public LocalDateTime getCandidateDeadline(User candidate, AppliConfig config) {
        if (config == null) {
            return null;
        }
        return userDao.isCandidatActif(candidate) ? config.getDateEndCandidatActif() : config.getDateEndCandidat();
    }

    public boolean isApplicationOpen(PosteCandidature application, boolean localMode,
                                     LocalDateTime candidateDeadline, LocalDateTime now) {
        if (application.getAuditionnable()) {
            return isBeforeDeadline(now, application.getPoste().getDateEndCandidatAuditionnable());
        }
        if (localMode) {
            return isBeforeDeadline(now, application.getPoste().getDateEndSignupCandidat());
        }
        return candidateDeadline == null || now.isBefore(candidateDeadline);
    }

    public boolean isCurrentTimeOk4ThisMembre(User targetUser) {
        AppliConfig config = appliConfigDao.getAppliConfig();
        LocalDateTime deadline = config == null ? null : config.getDateEndMembre();
        return targetUser.getIsMembre() && (deadline == null || LocalDateTime.now(clock).isBefore(deadline));
    }

    private boolean isBeforeDeadline(LocalDateTime now, LocalDateTime deadline) {
        return deadline != null && now.isBefore(deadline);
    }

}
