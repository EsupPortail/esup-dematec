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
            search.fill("");
            search.pressSequentially("admin");
            assertThat(users).hasValue("admin");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Trouver")).click();
            assertThat(users).hasValue("admin");
            assertThat(page.locator("tbody").getByText("admin", new Locator.GetByTextOptions().setExact(true)).first())
                    .isVisible();
        });
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
