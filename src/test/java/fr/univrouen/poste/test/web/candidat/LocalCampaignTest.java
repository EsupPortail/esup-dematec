package fr.univrouen.poste.test.web.candidat;

import fr.univrouen.poste.config.AppContextTestConfig.TestClock;
import fr.univrouen.poste.domain.AppliConfig;
import fr.univrouen.poste.domain.AppliConfigFileType;
import fr.univrouen.poste.domain.PosteAPourvoir;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.domain.User;
import fr.univrouen.poste.services.AppliConfigService;
import fr.univrouen.poste.test.AbstractControllerTest;
import fr.univrouen.poste.test.TestUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Exécuté avant les imports de la suite. Les données de chaque scénario sont annulées.
@Rollback
public class LocalCampaignTest extends AbstractControllerTest {
    private static final LocalDateTime CLOSING_TIME = LocalDateTime.of(2038, 6, 15, 14, 30);
    private static final String EMAIL = "campagne-locale@example.org";
    private static final String PASSWORD = "Campagne-locale-2026!";
    private static final String APPLICATIONS = "/postecandidatures";
    private static final String SIGNUP_FOR_POSTS = "/addpostecandidatures";
    private static final String CONFIG = "/admin/appliconfig";

    private final LocalDateTime future = LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MINUTES);
    private final LocalDateTime past = future.minusDays(4);

    @Autowired
    private AppliConfigService appliConfigService;

    @Autowired
    private TestClock clock;

    @PersistenceContext
    private EntityManager entityManager;

    @After
    public void resetTestState() {
        clock.reset();
        appliConfigService.clearCache();
    }

    @Test
    public void candidatCreeSonCompteEtDeposeSurUnPosteLocalOuvert() throws Exception {
        configure(false, future);
        mockMvc.perform(registration()).andExpect(redirectedUrl("/"));
        assertTrue(users().isEmpty());
        configure(true, future);
        String open = createPost("LOCAL-OUVERT", future);
        String closed = createPost("LOCAL-CLOS", past);

        MockHttpSession session = registerAndLogin();
        String html = mockMvc.perform(get(SIGNUP_FOR_POSTS).session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("LOCAL-OUVERT"));
        assertFalse(html.contains("LOCAL-CLOS"));

        apply(session, open, closed);
        Map<String, PosteCandidature> applications = applications(null);
        assertEquals(Set.of("LOCAL-OUVERT"), applications.keySet());
        PosteCandidature application = applications.get("LOCAL-OUVERT");
        upload(session, application, "dossier.txt").andExpect(status().is3xxRedirection());
        assertEquals(1, readApplication(application).getCandidatureFiles().size());

        configure(true, past);
        mockMvc.perform(post("/signup").with(anonymous()).with(csrf())
                        .param("civilite", "Mme").param("firstName", "Autre").param("lastName", "Compte")
                        .param("emailAddress", "hors-delai@example.org"))
                .andExpect(redirectedUrl("/"));
        MockHttpSession reopenedSession = loginCandidate();
        String home = mockMvc.perform(get("/postecandidatures").session(reopenedSession))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(home.contains("Ajout de candidatures"));
        mockMvc.perform(get("/signup").with(anonymous())).andExpect(redirectedUrl("/"));
        configure(true, null);
        mockMvc.perform(post("/signup").with(anonymous()).with(csrf())
                        .param("civilite", "Mme").param("firstName", "Autre").param("lastName", "Compte")
                        .param("emailAddress", "sans-date@example.org"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void datesDAuditionParPosteEnModeLocal() throws Exception {
        checkAuditionDeadlines(true);
    }

    @Test
    public void datesDAuditionParPosteEnModeGalaxie() throws Exception {
        checkAuditionDeadlines(false);
    }

    @Test
    public void cloturesLocalesEtDAuditionALaMicrosecondePres() throws Exception {
        clock.fixAt(CLOSING_TIME.minus(1, ChronoUnit.MICROS));
        configure(true, CLOSING_TIME);
        String regular = createPost("LIMITE-INSCRIPTION", CLOSING_TIME);
        String audition = createPost("LIMITE-AUDITION", CLOSING_TIME);
        String unclaimed = createPost("LIMITE-SANS-DOSSIER", CLOSING_TIME);
        MockHttpSession session = registerAndLogin();
        apply(session, regular, audition);
        Map<String, PosteCandidature> dossiers = applications(null);
        PosteCandidature regularApplication = dossiers.get("LIMITE-INSCRIPTION");
        PosteCandidature auditionApplication = dossiers.get("LIMITE-AUDITION");
        markAuditionable(auditionApplication);
        updatePost(audition, CLOSING_TIME);

        assertEquals(dossiers.keySet(), applications(session).keySet());
        mockMvc.perform(get("/signup").with(anonymous())).andExpect(view().name("signup/index"));
        for (PosteCandidature dossier : dossiers.values()) {
            mockMvc.perform(get(APPLICATIONS + "/" + dossier.getId()).session(session)).andExpect(status().isOk());
            upload(session, dossier, "avant-cloture.txt").andExpect(status().is3xxRedirection());
        }

        for (LocalDateTime time : List.of(CLOSING_TIME, CLOSING_TIME.plus(1, ChronoUnit.MICROS))) {
            clock.fixAt(time);
            assertTrue(applications(session).isEmpty());
            checkAccessDenied(session, regularApplication, auditionApplication);
            apply(session, unclaimed);
            assertEquals(dossiers.keySet(), applications(null).keySet());
            mockMvc.perform(get("/signup").with(anonymous())).andExpect(redirectedUrl("/"));
            mockMvc.perform(registration("limite@example.org"))
                    .andExpect(redirectedUrl("/"));
            mockMvc.perform(login()).andExpect(redirectedUrl("/login?error"));
        }
    }

    @Test
    public void cloturesCandidatCandidatActifEtMembreIndependantes() throws Exception {
        String inactive = "candidat-sans-depot@example.org";
        String active = "candidat-avec-depot@example.org";
        String member = "membre-cloture@example.org";

        clock.fixAt(CLOSING_TIME.minusDays(1));
        configure(true, CLOSING_TIME.plusDays(5));
        createUser(inactive, true);
        createUser(active, true);
        String memberUrl = createUser(member, false);
        String postUrl = createPost("LIMITE-GLOBALE", CLOSING_TIME.plusDays(5));
        PosteAPourvoir poste = (PosteAPourvoir) model(mockMvc.perform(asAdmin(get(postUrl)))
                .andExpect(status().isOk()), "posteapourvoir");
        mockMvc.perform(asAdmin(put("/posteapourvoirs")
                        .param("id", poste.getId().toString()).param("version", poste.getVersion().toString())
                        .param("numEmploi", poste.getNumEmploi()).param("profil", poste.getProfil())
                        .param("localisation", poste.getLocalisation())
                        .param("dateEndSignupCandidat", poste.getDateEndSignupCandidat().toString())
                        .param("membres", lastPathSegment(memberUrl))))
                .andExpect(redirectedUrl(postUrl));
        // Les requêtes MockMvc du test partagent la transaction. Relire la relation inverse après la mise à jour.
        rereadDatabase();
        MockHttpSession memberSession = loginCandidate(member);
        mockMvc.perform(get(postUrl).session(memberSession)).andExpect(status().isOk());

        MockHttpSession inactiveSession = loginCandidate(inactive);
        MockHttpSession activeSession = loginCandidate(active);
        apply(inactiveSession, postUrl);
        apply(activeSession, postUrl);
        PosteCandidature inactiveApplication = applications(inactiveSession).get("LIMITE-GLOBALE");
        PosteCandidature activeApplication = applications(activeSession).get("LIMITE-GLOBALE");
        upload(activeSession, activeApplication, "depot-actif.txt").andExpect(status().is3xxRedirection());

        LocalDateTime activeDeadline = CLOSING_TIME.plusDays(1);
        LocalDateTime memberDeadline = CLOSING_TIME.plusDays(2);
        configure(false, CLOSING_TIME, activeDeadline, memberDeadline);
        checkGlobalDeadline(inactive, CLOSING_TIME, inactiveSession, inactiveApplication);
        mockMvc.perform(login(active)).andExpect(redirectedUrl("/"));
        mockMvc.perform(login(member)).andExpect(redirectedUrl("/"));
        checkGlobalDeadline(active, activeDeadline, activeSession, activeApplication);
        mockMvc.perform(login(member)).andExpect(redirectedUrl("/"));

        configure(false, memberDeadline.plusDays(1), activeDeadline, memberDeadline);
        mockMvc.perform(login(inactive)).andExpect(redirectedUrl("/"));
        mockMvc.perform(login(active)).andExpect(redirectedUrl("/login?error"));
        assertTrue(applications(inactiveSession).containsKey("LIMITE-GLOBALE"));
        checkGlobalDeadline(member, memberDeadline, null, null);
        mockMvc.perform(login(inactive)).andExpect(redirectedUrl("/"));
    }

    @Test
    public void candidatRemplaceUnePieceParUneNouvelleVersion() throws Exception {
        configure(true, future);
        String postUrl = createPost("REMPLACEMENT-PIECE", future);
        MockHttpSession session = registerAndLogin();
        apply(session, postUrl);
        PosteCandidature dossier = applications(session).get("REMPLACEMENT-PIECE");
        String dossierUrl = APPLICATIONS + "/" + dossier.getId();
        String filename = "dossier.txt";
        byte[] original = "Version initiale".getBytes(StandardCharsets.UTF_8);
        byte[] replacement = "Version corrigée".getBytes(StandardCharsets.UTF_8);

        upload(session, dossier, filename, original).andExpect(redirectedUrl(dossierUrl));
        var originalFiles = readApplication(dossier).getCandidatureFiles();
        assertEquals(1, originalFiles.size());
        Long originalId = originalFiles.iterator().next().getId();
        mockMvc.perform(get(dossierUrl + "/" + originalId).session(session))
                .andExpect(status().isOk()).andExpect(content().bytes(original));

        mockMvc.perform(post(dossierUrl + "/delFile/" + originalId).session(session).with(csrf()))
                .andExpect(redirectedUrl(dossierUrl));
        rereadDatabase();
        assertTrue(readApplication(dossier).getCandidatureFiles().isEmpty());
        mockMvc.perform(get(dossierUrl + "/" + originalId).session(session)).andExpect(status().isForbidden());

        upload(session, dossier, filename, replacement).andExpect(redirectedUrl(dossierUrl));
        rereadDatabase();
        var replacementFiles = readApplication(dossier).getCandidatureFiles();
        assertEquals(1, replacementFiles.size());
        var currentFile = replacementFiles.iterator().next();
        assertEquals(filename, currentFile.getFilename());
        assertNotEquals(originalId, currentFile.getId());
        mockMvc.perform(get(dossierUrl + "/" + currentFile.getId()).session(session))
                .andExpect(status().isOk()).andExpect(content().bytes(replacement));
    }

    @Test
    public void presidentAttribuePuisRetireUnRapporteurDeSaCommission() throws Exception {
        configure(true, future, future, null, true);
        String president = "president-rapporteurs@example.org";
        String member = "membre-rapporteur@example.org";
        String presidentId = lastPathSegment(createUser(president, false));
        String memberId = lastPathSegment(createUser(member, false));
        String outsiderId = lastPathSegment(createUser("hors-commission@example.org", false));
        String posteUrl = createPost("AFFECTATION-RAPPORTEUR", future);
        assignCommission(posteUrl, new String[]{presidentId}, presidentId, memberId);

        MockHttpSession candidateSession = registerAndLogin();
        apply(candidateSession, posteUrl);
        PosteCandidature dossier = applications(candidateSession).get("AFFECTATION-RAPPORTEUR");
        String dossierUrl = APPLICATIONS + "/" + dossier.getId();
        MockHttpSession presidentSession = loginCandidate(president);
        MockHttpSession memberSession = loginCandidate(member);
        String addReporter = dossierUrl + "/addReporter";
        String removeReporter = dossierUrl + "/delReporter";

        mockMvc.perform(get(dossierUrl).session(presidentSession)).andExpect(status().isOk())
                .andExpect(content().string(containsString(addReporter)));
        mockMvc.perform(get(dossierUrl).session(memberSession)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString(addReporter))));
        mockMvc.perform(post(addReporter).session(memberSession).with(csrf()).param("userId", memberId))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(addReporter).session(presidentSession).with(csrf()).param("userId", outsiderId))
                .andExpect(redirectedUrl(dossierUrl));
        rereadDatabase();
        assertTrue(readApplication(dossier).getReporters().isEmpty());

        mockMvc.perform(post(addReporter).session(presidentSession).with(csrf()).param("userId", memberId))
                .andExpect(redirectedUrl(dossierUrl));
        rereadDatabase();
        assertEquals(List.of(member), readApplication(dossier).getReporters().stream().map(User::getEmailAddress).toList());
        mockMvc.perform(get(dossierUrl).session(presidentSession)).andExpect(status().isOk())
                .andExpect(content().string(containsString(removeReporter)));
        mockMvc.perform(post(removeReporter).session(memberSession).with(csrf()).param("userId", memberId))
                .andExpect(status().isForbidden());
        assertEquals(1, readApplication(dossier).getReporters().size());

        mockMvc.perform(post(removeReporter).session(presidentSession).with(csrf()).param("userId", memberId))
                .andExpect(redirectedUrl(dossierUrl));
        rereadDatabase();
        assertTrue(readApplication(dossier).getReporters().isEmpty());
    }

    @Test
    public void auteurSupprimeSonRapportSiLOptionEstActive() throws Exception {
        configure(true, future);
        allowReportDeletion(true);
        String author = "auteur-rapport@example.org";
        String colleague = "autre-membre-rapport@example.org";
        String authorId = lastPathSegment(createUser(author, false));
        String colleagueId = lastPathSegment(createUser(colleague, false));
        String posteUrl = createPost("SUPPRESSION-RAPPORT", future);
        assignCommission(posteUrl, new String[0], authorId, colleagueId);

        MockHttpSession candidateSession = registerAndLogin();
        apply(candidateSession, posteUrl);
        PosteCandidature dossier = applications(candidateSession).get("SUPPRESSION-RAPPORT");
        String dossierUrl = APPLICATIONS + "/" + dossier.getId();
        MockHttpSession authorSession = loginCandidate(author);
        MockHttpSession colleagueSession = loginCandidate(colleague);
        MockMultipartFile pdf = TestUtils.getPdfFile();
        mockMvc.perform(multipart(dossierUrl + "/addMemberReviewFile")
                        .file(pdf).session(authorSession).with(csrf()))
                .andExpect(redirectedUrl(dossierUrl));
        rereadDatabase();

        var reports = readApplication(dossier).getMemberReviewFiles();
        assertEquals(1, reports.size());
        var report = reports.iterator().next();
        assertEquals(author, report.getMember().getEmailAddress());
        String deleteUrl = dossierUrl + "/delMemberReviewFile/" + report.getId();
        String downloadUrl = dossierUrl + "/reviewFile/" + report.getId();

        mockMvc.perform(get(dossierUrl).session(authorSession)).andExpect(status().isOk())
                .andExpect(content().string(containsString(deleteUrl)));
        mockMvc.perform(get(dossierUrl).session(colleagueSession)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString(deleteUrl))));
        mockMvc.perform(post(deleteUrl).session(colleagueSession).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(downloadUrl).session(colleagueSession)).andExpect(status().isOk())
                .andExpect(content().bytes(pdf.getBytes()));

        allowReportDeletion(false);
        mockMvc.perform(get(dossierUrl).session(authorSession)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString(deleteUrl))));
        mockMvc.perform(post(deleteUrl).session(authorSession).with(csrf()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(downloadUrl).session(authorSession)).andExpect(status().isOk())
                .andExpect(content().bytes(pdf.getBytes()));

        allowReportDeletion(true);
        mockMvc.perform(post(deleteUrl).session(authorSession).with(csrf()))
                .andExpect(redirectedUrl(dossierUrl));
        rereadDatabase();
        assertTrue(readApplication(dossier).getMemberReviewFiles().isEmpty());
        mockMvc.perform(get(downloadUrl).session(authorSession)).andExpect(status().isForbidden());
    }

    private void allowReportDeletion(boolean allowed) throws Exception {
        var params = campaignParameters();
        params.set("membreSupprReviewFile", Boolean.toString(allowed));
        mockMvc.perform(asAdmin(put(CONFIG).params(params))).andExpect(status().is3xxRedirection());
    }

    private void assignCommission(String posteUrl, String[] presidents, String... members) throws Exception {
        var poste = (PosteAPourvoir) model(mockMvc.perform(asAdmin(get(posteUrl)))
                .andExpect(status().isOk()), "posteapourvoir");
        var request = put("/posteapourvoirs")
                .param("id", poste.getId().toString()).param("version", poste.getVersion().toString())
                .param("numEmploi", poste.getNumEmploi()).param("profil", poste.getProfil())
                .param("localisation", poste.getLocalisation())
                .param("dateEndSignupCandidat", poste.getDateEndSignupCandidat().toString())
                .param("membres", members);
        if (presidents.length > 0) {
            request.param("presidents", presidents);
        }
        mockMvc.perform(asAdmin(request)).andExpect(redirectedUrl(posteUrl));
        rereadDatabase();
    }

    private void rereadDatabase() {
        entityManager.flush();
        entityManager.clear();
    }

    private String lastPathSegment(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private void checkGlobalDeadline(String email, LocalDateTime deadline, MockHttpSession session,
                                     PosteCandidature dossier) throws Exception {
        for (int micros : new int[]{-1, 0, 1}) {
            clock.fixAt(deadline.plus(micros, ChronoUnit.MICROS));
            boolean open = micros < 0;
            mockMvc.perform(login(email)).andExpect(redirectedUrl(open ? "/" : "/login?error"));
            if (session != null) {
                if (open) {
                    assertTrue(applications(session).containsKey(dossier.getPoste().getNumEmploi()));
                    mockMvc.perform(get(APPLICATIONS + "/" + dossier.getId()).session(session))
                            .andExpect(status().isOk());
                } else {
                    assertTrue(applications(session).isEmpty());
                    checkAccessDenied(session, dossier);
                }
            }
        }
    }

    private String createUser(String email, boolean candidate) throws Exception {
        var request = post("/admin/users").param("emailAddress", email).param("password", PASSWORD)
                .param("civilite", "Mme").param("prenom", "Camille").param("nom", "Clôture").param("enabled", "true");
        if (candidate) {
            request.param("numCandidat", email);
        }
        return mockMvc.perform(asAdmin(request)).andExpect(redirectedUrlPattern("/admin/users/*"))
                .andReturn().getResponse().getRedirectedUrl();
    }

    private void checkAuditionDeadlines(boolean localMode) throws Exception {
        configure(true, future);
        String firstPost = createPost("AUDITION-A", future);
        String secondPost = createPost("AUDITION-B", future);
        String undatedPost = createPost("AUDITION-SANS-DATE", future);
        MockHttpSession session = registerAndLogin();
        apply(session, firstPost, secondPost, undatedPost);
        Map<String, PosteCandidature> applications = applications(null);
        PosteCandidature first = applications.get("AUDITION-A");
        PosteCandidature second = applications.get("AUDITION-B");
        PosteCandidature undated = applications.get("AUDITION-SANS-DATE");
        upload(session, first, "avant-audition.txt").andExpect(status().is3xxRedirection());

        markAuditionable(first);
        markAuditionable(second);
        markAuditionable(undated);
        updatePost(firstPost, future);
        updatePost(secondPost, past);
        updatePost(undatedPost, null);
        configure(localMode, past);

        session = loginCandidate();
        assertEquals(Set.of("AUDITION-A"), applications(session).keySet());
        checkAccess(session, first, second, undated);
        var lockedFile = readApplication(first).getCandidatureFiles().iterator().next();
        assertFalse(lockedFile.getWriteable());
        mockMvc.perform(post(APPLICATIONS + "/" + first.getId() + "/delFile/" + lockedFile.getId())
                        .session(session).with(csrf()))
                .andExpect(status().isForbidden());
        upload(session, first, "pour-audition.txt").andExpect(status().is3xxRedirection());
        assertEquals(2, readApplication(first).getCandidatureFiles().size());

        updatePost(firstPost, past);
        updatePost(secondPost, future);
        assertEquals(Set.of("AUDITION-B"), applications(session).keySet());
        checkAccess(session, second, first, undated);
        upload(session, second, "pour-audition.txt").andExpect(status().is3xxRedirection());
        assertEquals(1, readApplication(second).getCandidatureFiles().size());

        clock.fixAt(future.minus(1, ChronoUnit.MICROS));
        assertEquals(Set.of("AUDITION-B"), applications(session).keySet());
        mockMvc.perform(login()).andExpect(redirectedUrl("/"));
        for (LocalDateTime time : List.of(future, future.plus(1, ChronoUnit.MICROS))) {
            clock.fixAt(time);
            assertTrue(applications(session).isEmpty());
            checkAccessDenied(session, second);
            mockMvc.perform(login()).andExpect(redirectedUrl("/login?error"));
        }
    }

    private void configure(boolean localMode, LocalDateTime date) throws Exception {
        configure(localMode, date, date, null);
    }

    private void configure(boolean localMode, LocalDateTime date, LocalDateTime activeDate,
                           LocalDateTime memberDate) throws Exception {
        configure(localMode, date, activeDate, memberDate, false);
    }

    private void configure(boolean localMode, LocalDateTime date, LocalDateTime activeDate,
                           LocalDateTime memberDate, boolean showReporters) throws Exception {
        var params = campaignParameters();
        params.set("candidatCanSignup", Boolean.toString(localMode));
        params.set("dateEndCandidat", Objects.toString(date, ""));
        params.set("dateEndCandidatActif", Objects.toString(activeDate, ""));
        params.set("dateEndMembre", Objects.toString(memberDate, ""));
        params.set("presidentReportersView", Boolean.toString(showReporters));
        mockMvc.perform(asAdmin(put(CONFIG).params(params))).andExpect(status().is3xxRedirection());
    }

    private MultiValueMap<String, String> campaignParameters() throws Exception {
        Page<?> configs = (Page<?>) model(mockMvc.perform(asAdmin(get(CONFIG)))
                .andExpect(status().isOk()), "appliconfigs");
        AppliConfig config = (AppliConfig) configs.getContent().get(0);
        return TestUtils.getParamsAsStringMap(config);
    }

    private String createPost(String code, LocalDateTime deadline) throws Exception {
        return mockMvc.perform(asAdmin(post("/posteapourvoirs").param("numEmploi", code)
                        .param("profil", code).param("localisation", "Rouen")
                        .param("dateEndSignupCandidat", deadline.toString())))
                .andExpect(redirectedUrlPattern("/posteapourvoirs/*")).andReturn().getResponse().getRedirectedUrl();
    }

    private void updatePost(String url, LocalDateTime auditionDeadline) throws Exception {
        PosteAPourvoir poste = (PosteAPourvoir) model(mockMvc.perform(asAdmin(get(url)))
                .andExpect(status().isOk()), "posteapourvoir");
        mockMvc.perform(asAdmin(put("/posteapourvoirs")
                        .param("id", poste.getId().toString())
                        .param("version", poste.getVersion().toString())
                        .param("numEmploi", poste.getNumEmploi()).param("profil", poste.getProfil())
                        .param("localisation", poste.getLocalisation())
                        .param("dateEndSignupCandidat", past.toString())
                        .param("dateEndCandidatAuditionnable", Objects.toString(auditionDeadline, ""))))
                .andExpect(redirectedUrl(url));
    }

    private MockHttpSession registerAndLogin() throws Exception {
        mockMvc.perform(registration()).andExpect(view().name("signup/thanks"));
        User user = (User) users().getContent().get(0);
        mockMvc.perform(get("/signup").with(anonymous()).param("activate", user.getActivationKey())
                        .param("emailAddress", EMAIL))
                .andExpect(view().name("signup/initpassword"));
        mockMvc.perform(post("/signup/initpassword").with(anonymous()).with(csrf()).param("emailAddress", EMAIL)
                        .param("activationKey", user.getActivationKey())
                        .param("password", PASSWORD).param("repeatPassword", PASSWORD))
                .andExpect(view().name("login"));
        return loginCandidate();
    }

    private Page<?> users() throws Exception {
        return (Page<?>) model(mockMvc.perform(asAdmin(get("/admin/users").param("nomOrPrenomOrEmailAddress", EMAIL)))
                .andExpect(status().isOk()), "users");
    }

    private MockHttpServletRequestBuilder registration() {
        return registration(EMAIL);
    }

    private MockHttpServletRequestBuilder registration(String email) {
        return post("/signup").with(anonymous()).with(csrf()).param("civilite", "Mme")
                .param("firstName", "Camille").param("lastName", "Locale").param("emailAddress", email);
    }

    private MockHttpSession loginCandidate() throws Exception {
        return loginCandidate(EMAIL);
    }

    private MockHttpSession loginCandidate(String email) throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(login(email)).andExpect(redirectedUrl("/"))
                .andReturn().getRequest().getSession(false);
        assertNotNull(session);
        return session;
    }

    private MockHttpServletRequestBuilder login() {
        return login(EMAIL);
    }

    private MockHttpServletRequestBuilder login(String email) {
        return post("/login").with(anonymous()).with(csrf()).param("username", email).param("password", PASSWORD);
    }

    private void apply(MockHttpSession session, String... urls) throws Exception {
        var request = post(SIGNUP_FOR_POSTS).session(session).with(csrf());
        for (String url : urls) {
            request.param("posteIds", lastPathSegment(url));
        }
        mockMvc.perform(request).andExpect(redirectedUrl(SIGNUP_FOR_POSTS));
    }

    private Map<String, PosteCandidature> applications(MockHttpSession session) throws Exception {
        var request = get(APPLICATIONS);
        if (session == null) {
            asAdmin(request);
        } else {
            request.session(session);
        }
        Page<?> page = (Page<?>) model(mockMvc.perform(request).andExpect(status().isOk()), "postecandidatures");
        return page.stream().map(PosteCandidature.class::cast)
                .collect(toMap(application -> application.getPoste().getNumEmploi(), application -> application));
    }

    private void markAuditionable(PosteCandidature application) throws Exception {
        String url = APPLICATIONS + "/" + application.getId();
        mockMvc.perform(asAdmin(post(url + "/auditionnable").param("auditionnable", "true")))
                .andExpect(redirectedUrl(url));
        assertTrue(readApplication(application).getAuditionnable());
    }

    private PosteCandidature readApplication(PosteCandidature application) throws Exception {
        return (PosteCandidature) model(mockMvc.perform(asAdmin(get(APPLICATIONS + "/" + application.getId())))
                .andExpect(status().isOk()), "postecandidature");
    }

    private void checkAccess(MockHttpSession session, PosteCandidature open, PosteCandidature... closed) throws Exception {
        mockMvc.perform(get(APPLICATIONS + "/" + open.getId()).session(session)).andExpect(status().isOk());
        checkAccessDenied(session, closed);
    }

    private void checkAccessDenied(MockHttpSession session, PosteCandidature... closed) throws Exception {
        for (PosteCandidature application : closed) {
            mockMvc.perform(get(APPLICATIONS + "/" + application.getId()).session(session)).andExpect(status().isForbidden());
            upload(session, application, "hors-delai.txt").andExpect(status().isForbidden());
        }
    }

    private ResultActions upload(MockHttpSession session, PosteCandidature application, String name) throws Exception {
        return upload(session, application, name, name.getBytes(StandardCharsets.UTF_8));
    }

    private ResultActions upload(MockHttpSession session, PosteCandidature application, String name, byte[] bytes)
            throws Exception {
        List<?> types = (List<?>) model(mockMvc.perform(asAdmin(get(APPLICATIONS + "/" + application.getId())))
                .andExpect(status().isOk()), "fileTypes");
        AppliConfigFileType type = (AppliConfigFileType) types.get(0);
        return mockMvc.perform(multipart(APPLICATIONS + "/" + application.getId() + "/addFile")
                .file(new MockMultipartFile("file", name, "text/plain", bytes))
                .param("fileType", type.getId().toString()).session(session).with(csrf()));
    }

    private MockHttpServletRequestBuilder asAdmin(MockHttpServletRequestBuilder request) {
        return request.with(user("admin").roles("ADMIN")).with(csrf());
    }

    private Object model(ResultActions result, String name) {
        return Objects.requireNonNull(result.andReturn().getModelAndView()).getModel().get(name);
    }
}
