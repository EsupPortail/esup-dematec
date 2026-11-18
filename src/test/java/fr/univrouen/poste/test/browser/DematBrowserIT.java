package fr.univrouen.poste.test.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DematBrowserIT {
    private static String baseUrl;
    private static Playwright playwright;
    private static Browser browser;

    @Rule
    public final TestName testName = new TestName();

    @BeforeClass
    public static void launchBrowser() {
        baseUrl = System.getProperty("playwright.baseUrl");
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("La propriété playwright.baseUrl doit désigner l'application de test");
        }
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterClass
    public static void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    public void connexionPubliqueEtMenuLateral() {
        runInBrowser(page -> {
            login(page);
            Locator sidebar = page.locator("#demat-left");
            Locator toggle = page.locator("#toggleSidebar");

            assertThat(sidebar).isVisible();
            toggle.click();
            assertThat(sidebar).isHidden();
            page.reload();
            assertThat(sidebar).isHidden();
            toggle.click();
            assertThat(sidebar).isVisible();
        });
    }

    @Test
    public void filtreDuJournalDesAuthentifications() {
        runInBrowser(page -> {
            login(page);
            page.navigate(baseUrl + "/admin/logauths");
            Locator filter = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Filtrer").setExact(true));
            Locator search = page.locator("#textbox");
            Locator users = page.locator("#userId");

            Locator panel = page.locator("#collapseFilter");
            assertThat(panel).hasCSS("height", "0px");
            filter.click();
            assertThat(panel).containsClass("in");
            assertThat(users.locator("option[value=admin]")).hasCount(1);

            search.pressSequentially("introuvable");
            assertThat(users.locator("option")).hasCount(0);
            search.fill("[");
            assertThat(users.locator("option")).hasCount(0);
            search.fill("  ^AD.*N$  ");
            assertThat(users).hasValue("admin");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Trouver")).click();
            assertThat(users).hasValue("admin");
            assertThat(page.locator("tbody").getByText("admin", new Locator.GetByTextOptions().setExact(true)).first())
                    .isVisible();
        });
    }

    @Test
    public void selectionEtAuditionDesCandidatures() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            page.navigate(baseUrl + "/postecandidatures");

            Locator candidates = page.locator("#manageForm .candidature-checkbox");
            Locator actions = page.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Gestion des candidatures").setExact(true));
            assertThat(candidates.nth(1)).isVisible();
            assertThat(actions).isHidden();
            candidates.first().check();
            assertThat(actions).isVisible();
            candidates.first().uncheck();
            assertThat(actions).isHidden();

            Locator selectAll = page.locator("#selectAll");
            selectAll.check();
            assertThat(page.locator("#manageForm .candidature-checkbox:checked")).hasCount(candidates.count());
            assertThat(actions).isVisible();
            Locator openAudition = page.locator("a[href='#auditionnableDialog']");
            openAudition.click();
            Locator dialog = page.locator("#auditionnableDialog");
            assertThat(dialog).isVisible();
            page.keyboard().press("Escape");
            assertThat(dialog).isHidden();
            assertThat(openAudition).isFocused();
            openAudition.click();
            dialog.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Annuler")).click();
            assertThat(dialog).isHidden();
            assertThat(selectAll).isChecked();
            selectAll.uncheck();
            assertThat(page.locator("#manageForm .candidature-checkbox:checked")).hasCount(0);
            assertThat(actions).isHidden();

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Filtrer").setExact(true)).click();
            Locator search = page.getByPlaceholder("Filtrer candidats");
            Locator options = page.locator("#candidatSelect option");
            Locator firstRow = candidates.first().locator("xpath=ancestor::tr");
            String email = firstRow.getByRole(AriaRole.CELL,
                    new Locator.GetByRoleOptions().setName(Pattern.compile("@"))).innerText();
            search.pressSequentially("introuvable");
            assertThat(options).hasCount(0);
            search.clear();
            search.pressSequentially(email);
            assertThat(options).hasCount(1);
            assertThat(options.first()).hasText(email);
            assertThat(options.first()).hasJSProperty("selected", true);

            firstRow.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Voir").setExact(true)).click();
            page.locator("a[href='#auditionnableDialog']").click();
            assertThat(dialog).isVisible();
            dialog.getByText("Annuler", new Locator.GetByTextOptions().setExact(true)).click();
            assertThat(dialog).isHidden();

            Locator comment = page.locator("form[action$='/updateManagerComment']");
            comment.locator("textarea").fill("Commentaire de commission Playwright");
            comment.getByRole(AriaRole.BUTTON).click();
            assertThat(comment.locator("textarea")).hasValue("Commentaire de commission Playwright");

            page.navigate(baseUrl + "/postecandidatures");
            page.getByTitle("Voir le commentaire").click();
            Locator commentDialog = page.locator("[id^=comment-modal-]");
            assertThat(commentDialog.getByText("Commentaire de commission Playwright")).isVisible();
            page.mouse().click(5, 5);
            assertThat(commentDialog).isHidden();
            page.getByTitle("Voir le commentaire").click();
            commentDialog.locator(".modal-footer")
                    .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Fermer")).click();
            assertThat(commentDialog).isHidden();
        });
    }

    @Test
    public void dateDAuditionDUnPoste() {
        runInBrowser(page -> {
            login(page);
            page.navigate(baseUrl + "/posteapourvoirs?form");
            page.getByLabel("Num Emploi").fill("999999");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Modifier Poste")).click();
            String editUrl = page.url();

            Locator date = page.getByLabel("Date End Candidat Auditionnable");
            date.click();
            assertThat(page.locator(".xdsoft_datetimepicker:visible")).hasCount(1);
            date.fill("26/11/2035 15:00");
            date.press("Tab");
            assertThat(date).hasValue("26/11/2035 15:00");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Modifier").setExact(true)).click();
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Modifier Poste")).click();
            assertThat(page).hasURL(editUrl);
            assertThat(date).hasValue("26/11/2035 15:00");
        });
    }

    @Test
    public void filtresRepliablesDeLAdministration() {
        runInBrowser(page -> {
            login(page);
            for (String screen : List.of("users", "commissionentrys", "galaxieentrys", "logimportcommissions",
                    "logimportgalaxies", "logpostefiles", "logfiles", "logmails")) {
                Response response = page.navigate(baseUrl + "/admin/" + screen);
                assertEquals(screen, 200, response.status());
                Locator filter = page.getByRole(AriaRole.LINK,
                        new Page.GetByRoleOptions().setName("Filtrer").setExact(true));
                Locator panel = page.locator("#collapseFilter");
                assertThat(panel).hasCSS("height", "0px");
                assertThat(filter).hasAttribute("aria-expanded", "false");
                filter.click();
                assertThat(filter).hasAttribute("aria-expanded", "true");
                assertThat(panel).not().hasCSS("height", "0px");
                filter.click();
                assertThat(filter).hasAttribute("aria-expanded", "false");
                assertThat(panel).hasCSS("height", "0px");
            }
        });
    }

    @Test
    public void filtresTexteDesJournauxFichiersEtCourriels() {
        runInBrowser(page -> {
            login(page);
            Locator filter = page.getByRole(AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Filtrer").setExact(true));
            page.navigate(baseUrl + "/admin/logfiles");
            filter.click();
            verifierFiltrageDesOptions(page.locator("#textboxMail"));
            verifierFiltrageDesOptions(page.locator("#textboxNom"));
            verifierFiltrageDesOptions(page.locator("#textbox"));

            page.navigate(baseUrl + "/admin/logmails");
            filter.click();
            verifierFiltrageDesOptions(page.locator("#textbox"));
        });
    }

    private void verifierFiltrageDesOptions(Locator search) {
        Locator options = search.locator("xpath=preceding-sibling::select[1]").locator("option");
        int initialCount = options.count();
        assertTrue("Le filtre doit démarrer avec des options", initialCount > 0);

        search.pressSequentially("introuvable");
        assertThat(options).hasCount(0);
        search.clear();
        assertThat(options).hasCount(initialCount);
    }

    @Test
    public void indicateurDeRobustesseDuMotDePasse() {
        runInBrowser(page -> {
            login(page);
            page.navigate(baseUrl + "/changepassword/index");
            Locator password = page.locator("input[name=newPassword]");
            Locator meter = page.locator(".strength-indicator .meter");

            assertThat(meter).hasText("");
            password.fill("Abc1");
            assertThat(meter).hasText("Moyen");
            password.fill("abcDEF123!abc");
            assertThat(meter).hasText("Fort");
            assertThat(meter).containsClass("strong");
            password.fill("abcdefghijkl");
            assertThat(meter).hasText("Faible");
            assertThat(meter).containsClass("weak");
            assertThat(meter).not().containsClass("strong");
            password.clear();
            assertThat(meter).hasText("");
            assertThat(meter).not().containsClass("weak");
        });
    }

    @Test
    public void enregistrementDesParametresDeCampagne() {
        runInBrowser(page -> {
            login(page);
            page.navigate(baseUrl + "/admin/appliconfig");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Modifier").setExact(true)).click();
            Locator date = page.locator("input[name=dateEndCandidat]");
            date.click();
            assertThat(page.locator(".xdsoft_datetimepicker:visible")).hasCount(1);
            date.fill("25/11/2035 14:30");
            date.press("Tab");
            assertThat(date).hasValue("25/11/2035 14:30");

            Locator color = page.locator("input[name=colorCandidatureVue]");
            color.click();
            assertThat(page.locator(".minicolors-panel:visible")).hasCount(1);
            color.fill("#126a9c");
            color.press("Tab");
            assertThat(color).hasValue("#126a9c");

            Locator richText = page.locator("textarea[name=textePremierePageAnonyme]");
            Locator toolbar = richText.locator("xpath=preceding-sibling::ul[contains(@class,'wysihtml5-toolbar')][1]");
            Locator toggleSource = toolbar.locator("[data-wysihtml5-action=change_view]");
            assertThat(richText).isHidden();
            Locator editor = richText.locator("xpath=following-sibling::iframe[1]").contentFrame().locator("body");
            editor.fill("Lien Playwright");
            editor.press("ControlOrMeta+a");
            toolbar.locator("[data-wysihtml5-command=createLink]").click();
            Locator linkDialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Insérer un lien"));
            Locator url = linkDialog.getByRole(AriaRole.TEXTBOX);
            assertThat(url).isFocused();
            url.fill("https://example.org/");
            url.press("Enter");
            assertThat(linkDialog).isHidden();
            assertThat(editor.getByRole(AriaRole.LINK)).hasAttribute("href", "https://example.org/");
            toggleSource.click();
            richText.fill("<p><strong>Campagne Playwright</strong></p>");
            toggleSource.click();
            assertThat(richText).isHidden();
            toggleSource.click();
            assertThat(richText).hasValue("<p><strong>Campagne Playwright</strong></p>");

            String editUrl = page.url();
            Response save = page.waitForResponse(response -> response.url().equals(baseUrl + "/admin/appliconfig")
                    && response.request().method().equals("POST"),
                    () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour")).click());
            assertEquals(302, save.status());
            page.navigate(editUrl);
            assertThat(date).hasValue("25/11/2035 14:30");
            assertThat(color).hasValue("#126a9c");
            toggleSource.click();
            assertThat(richText).hasValue("<p><strong>Campagne Playwright</strong></p>");
        });
    }

    private void importerCandidatures(Page page) {
        page.navigate(baseUrl + "/admin/galaxieexcels?form");
        page.getByLabel("Sélectionner fichier Excel").setInputFiles(Path.of("src/main/webapp/doc/EXTETBF.xls"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Importer").setExact(true)).click();
        assertThat(page).hasURL(baseUrl + "/admin/galaxieexcels");

        page.navigate(baseUrl + "/admin/galaxieentrys");
        page.locator("form[action$='/generatecandidatspostes']").getByRole(AriaRole.BUTTON).click();
        assertThat(page).hasURL(baseUrl + "/admin/logimportgalaxies");
    }

    private void login(Page page) {
        page.navigate(baseUrl + "/login");
        page.locator("#j_username").fill("admin");
        page.locator("#j_password").fill("admin");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Valider")).click();
        assertThat(page.locator("#toggleSidebar")).isVisible();
    }

    private void runInBrowser(Consumer<Page> scenario) {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"))) {
            context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
            Page page = context.newPage();
            List<String> pageErrors = new ArrayList<>();
            page.onPageError(pageErrors::add);
            boolean passed = false;
            try {
                scenario.accept(page);
                assertTrue("Erreurs JavaScript : " + pageErrors, pageErrors.isEmpty());
                passed = true;
            } finally {
                if (passed) {
                    context.tracing().stop();
                } else {
                    Path trace = Path.of("target", "playwright", testName.getMethodName() + ".zip");
                    context.tracing().stop(new Tracing.StopOptions().setPath(trace));
                }
            }
        }
    }
}
