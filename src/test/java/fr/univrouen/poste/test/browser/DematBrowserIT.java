package fr.univrouen.poste.test.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
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
            page.navigate(baseUrl + "/login");
            assertEquals("undefined", page.evaluate("typeof window.jQuery"));
            login(page);
            assertEquals("undefined", page.evaluate("typeof window.jQuery"));
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

            Locator pageSize = page.getByLabel("Résultats par page");
            pageSize.selectOption("50");
            assertThat(page).hasURL(Pattern.compile("[?&]size=50(?:&|$)"));
            assertThat(pageSize).hasValue("50");
            assertThat(users).hasValue("admin");

            search.fill("introuvable");
            assertThat(users.locator("option")).hasCount(0);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Effacer")).click();
            assertThat(search).hasValue("");
            assertThat(users.locator("option[value=admin]")).hasCount(1);
            assertThat(users).hasValue("");
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
            assertThat(page.locator("#selectAll")).hasJSProperty("indeterminate", true);
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
            Locator finder = page.locator("#filterForm");
            Locator zip = finder.getByLabel("Zip");
            Locator csv = finder.getByLabel("csv");
            zip.check();
            csv.check();
            assertThat(zip).not().isChecked();
            Locator submitFilter = finder.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Trouver"));
            assertEquals("candidatures.csv", page.waitForDownload(submitFilter::click).suggestedFilename());
            assertEquals("candidatures.csv", page.waitForDownload(submitFilter::click).suggestedFilename());
            csv.uncheck();

            Locator search = page.getByPlaceholder("Filtrer candidats");
            Locator options = page.locator("#candidatSelect option");
            Locator firstRow = candidates.first().locator("xpath=ancestor::tr");
            assertThat(firstRow.getByAltText("Recevable")).isVisible();
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

            Locator upload = page.locator("form[action$='/addFile']").first();
            Path pdf = Path.of("src/test/resources/doc-dummy.pdf");
            upload.locator("input[type=file]").setInputFiles(pdf);
            upload.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Valider")).click();
            assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("doc-dummy.pdf"))).isVisible();

            List<String> alerts = new ArrayList<>();
            page.onceDialog(alert -> {
                alerts.add(alert.message());
                alert.accept();
            });
            upload.locator("input[type=file]").setInputFiles(pdf);
            upload.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Valider")).click();
            page.waitForLoadState();
            assertEquals(List.of("Un fichier de même nom existe déjà !"), alerts);

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
    public void confirmationEtRelectureDUneAudition() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            ouvrirCandidature(page, "candidat2@example.org");
            String candidatureUrl = page.url();
            confirmerDecision(page, "auditionnableDialog", "auditionnable");

            page.reload();
            assertThat(page).hasURL(candidatureUrl);
            assertThat(page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Non retenu pour audition").setExact(true))).isVisible();
            assertThat(page.locator("#auditionnableDialog")).hasCount(0);
        });
    }

    @Test
    public void filtresDesCandidaturesPourUnMembreDeCommission() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            ouvrirCandidature(page, "candidat@example.org");
            confirmerDecision(page, "auditionnableDialog", "auditionnable");

            String member = "commission-playwright@example.org";
            String password = "Playwright123!";
            page.navigate(baseUrl + "/admin/users?form");
            page.locator("#emailAddress").fill(member);
            page.locator("input[name=password]").fill(password);
            page.locator("#civilite").fill("Mme");
            page.locator("#nom").fill("Commission");
            page.locator("#prenom").fill("Playwright");
            page.getByLabel("Compte activé").check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
            assertThat(page).hasURL(Pattern.compile("/admin/users/\\d+$"));

            for (String post : List.of("4086", "4087")) {
                page.navigate(baseUrl + "/posteapourvoirs");
                ligneAvecCellule(page, post).getByRole(AriaRole.LINK,
                        new Locator.GetByRoleOptions().setName("Modifier").setExact(true)).click();
                page.getByLabel("Membres").selectOption(new SelectOption().setLabel(member));
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Modifier").setExact(true)).click();
                assertThat(page).hasURL(Pattern.compile("/posteapourvoirs/\\d+$"));
            }

            page.locator("#logout input[type=submit]").click();
            renseignerConnexion(page, member, password);
            page.navigate(baseUrl + "/postecandidatures");
            Locator filter = page.locator("a[href='#collapseFilterMembre']");
            assertThat(filter).hasAttribute("aria-expanded", "false");
            assertThat(ligneAvecCellule(page, "4086").first()).isVisible();
            assertThat(ligneAvecCellule(page, "4087").first()).isVisible();
            assertThat(page.locator("#selectAll")).hasCount(0);

            filter.click();
            assertThat(filter).hasAttribute("aria-expanded", "true");
            Locator panel = page.locator("#collapseFilterMembre");
            panel.getByLabel("Postes").selectOption("4086");
            Locator audition = panel.getByLabel("Auditionnable");
            Locator submit = panel.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Trouver"));
            audition.selectOption("true");
            submit.click();
            assertThat(audition).hasValue("true");
            assertThat(page.locator("#manageForm tbody tr")).hasCount(1);
            assertThat(ligneAvecCellule(page, "candidat@example.org")).isVisible();

            audition.selectOption("false");
            submit.click();
            assertThat(audition).hasValue("false");
            assertThat(page.locator("#manageForm tbody tr")).hasCount(1);
            assertThat(ligneAvecCellule(page, "candidat3@example.org")).isVisible();
        });
    }

    @Test
    public void consultationDesTagsApresRechargement() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);

            String tag = "Priorité";
            String value = "À examiner";
            String candidate = "candidat2@example.org";
            page.navigate(baseUrl + "/admin/candidaturetags?form");
            page.locator("input[name=name]").fill(tag);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
            assertThat(page).hasURL(Pattern.compile("/admin/candidaturetags/\\d+\\?form$"));
            page.getByPlaceholder("Nouvelle valeur").fill(value);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ajouter Valeur")).click();
            assertThat(page.getByText(value, new Page.GetByTextOptions().setExact(true))).isVisible();

            ouvrirCandidature(page, candidate);
            Locator tags = page.locator("form[action$='/updateTags']");
            tags.locator("select").selectOption(new SelectOption().setLabel(value));
            tags.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Mettre à jour / Sauver les tags")).click();

            page.navigate(baseUrl + "/postecandidatures");
            page.reload();
            Locator trigger = ligneAvecCellule(page, candidate).getByTitle("Voir les tags");
            trigger.click();
            Locator dialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Tags").setExact(true));
            assertThat(dialog.getByText(tag + " :", new Locator.GetByTextOptions().setExact(true))).isVisible();
            assertThat(dialog.locator(".modal-body")).containsText(value);
            dialog.locator(".modal-footer")
                    .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Fermer")).click();
            assertThat(dialog).isHidden();
            assertThat(trigger).isFocused();
        });
    }

    @Test
    public void confirmationEtRelectureDUneCandidatureLaureate() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);

            page.navigate(baseUrl + "/admin/appliconfig");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Modifier").setExact(true)).click();
            page.locator("input[name=laureatEnable]").check();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour")).click();

            String candidate = "candidat4@example.org";
            ouvrirCandidature(page, candidate);
            String candidatureUrl = page.url();
            confirmerDecision(page, "laureatDialog", "laureat");
            page.reload();
            assertThat(page).hasURL(candidatureUrl);
            assertThat(page.getByText("Cette candidature est lauréate.", new Page.GetByTextOptions().setExact(true)))
                    .isVisible();
            assertThat(page.locator("#laureatDialog")).hasCount(0);

            page.navigate(baseUrl + "/postecandidatures");
            assertThat(ligneAvecCellule(page, candidate)
                    .getByAltText("Lauréat", new Locator.GetByAltTextOptions().setExact(true))).isVisible();
        });
    }

    @Test
    public void protectionContreLesDoublesSoumissions() {
        runInBrowser(page -> {
            login(page);
            page.navigate(baseUrl + "/changepassword/index");
            List<String> alerts = new ArrayList<>();
            page.onceDialog(alert -> {
                alerts.add(alert.message());
                alert.accept();
            });

            Locator form = page.locator("form[action$='/changepassword/update']");
            Object submissions = form.evaluate("""
                    form => {
                        const submit = () => form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
                        form.addEventListener('submit', event => event.preventDefault(), { once: true });
                        return [submit(), submit(), submit()];
                    }
                    """);
            assertEquals(List.of(false, true, false), submissions);
            assertEquals(1, alerts.size());
            assertTrue(alerts.get(0).startsWith("Envoi en cours"));
        });
    }

    @Test
    public void affichageDesGraphiques() {
        runInBrowser(page -> {
            login(page);
            assertEquals(200, page.navigate(baseUrl + "/admin/chart").status());
            assertEquals(List.of("uploadChart", "authChart", "sizeChart", "sizeMemberChart", "sizePosteChart"),
                    page.evaluate("""
                    () => [window.chartUpload, window.chartAuth, window.chartSize,
                           window.chartSizeMember, window.chartSizePoste].map(chart => chart?.chart?.canvas.id)
                    """));
            assertEquals("2.0 kB", page.evaluate("humanFileSize(2048)"));
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
            assertThat(date).hasAttribute("type", "datetime-local");
            date.fill("2035-11-26T15:00");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Modifier").setExact(true)).click();
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Modifier Poste")).click();
            assertThat(page).hasURL(editUrl);
            assertThat(date).hasValue("2035-11-26T15:00");
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
            assertThat(date).hasAttribute("type", "datetime-local");
            date.fill("2035-11-25T14:30");
            Locator optionalDate = page.locator("input[name=dateEndCandidatActif]");
            optionalDate.fill("2035-11-26T11:15");
            optionalDate.clear();

            Locator color = page.locator("input[name=colorCandidatureVue]");
            Locator picker = color.locator("xpath=preceding-sibling::input[@type='color'][1]");
            color.fill("#abc");
            assertThat(picker).hasValue("#aabbcc");
            picker.fill("#126a9c");
            assertThat(color).hasValue("#126a9c");

            Locator optionalColor = page.locator("input[name=colorReporterTag]");
            optionalColor.clear();
            assertThat(optionalColor.locator("xpath=preceding-sibling::input[@type='color'][1]")).hasValue("#000000");

            Locator richText = page.locator("textarea[name=textePremierePageAnonyme]");
            Locator toolbar = richText.locator("xpath=preceding-sibling::div[@role='group'][1]");
            Locator toggleSource = toolbar.getByRole(AriaRole.BUTTON,
                    new Locator.GetByRoleOptions().setName("Source HTML"));
            Locator editor = richText.locator("xpath=following-sibling::iframe[1]").contentFrame().locator("body");
            assertThat(richText).isHidden();
            toggleSource.click();
            richText.fill("<p>Lien Playwright</p>");
            toggleSource.click();
            editor.press("ControlOrMeta+a");
            toolbar.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Gras")).click();
            assertThat(editor.locator("b, strong")).hasText("Lien Playwright");
            toolbar.getByLabel("Couleur du texte").selectOption("red");
            assertThat(editor.locator(".wysiwyg-color-red")).hasCSS("color", "rgb(255, 0, 0)");
            toolbar.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Insérer un lien")).click();
            Locator linkDialog = page.getByRole(AriaRole.DIALOG, new Page.GetByRoleOptions().setName("Insérer un lien"));
            Locator url = linkDialog.getByRole(AriaRole.TEXTBOX);
            assertThat(url).isFocused();
            url.fill("https://example.org/");
            url.press("Enter");
            assertThat(linkDialog).isHidden();
            assertThat(editor.getByRole(AriaRole.LINK)).hasAttribute("href", "https://example.org/");

            Locator footer = page.locator("textarea[name=piedPage]");
            Locator footerSource = footer.locator("xpath=preceding-sibling::div[@role='group'][1]")
                    .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Source HTML"));
            footerSource.click();
            String existingFooter = footer.inputValue();
            assertTrue(existingFooter.contains("id="));
            footerSource.click();

            Locator candidateHelp = page.locator("textarea[name=texteCandidatAideCandidatures]");
            Locator helpSource = candidateHelp.locator("xpath=preceding-sibling::div[@role='group'][1]")
                    .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Source HTML"));
            String customHtml = "<SECTION class='well' data-note='A&amp;B'><p><strong>Campagne Playwright</strong></p></SECTION>";
            helpSource.click();
            candidateHelp.fill(customHtml);
            helpSource.click();
            Locator helpPreview = candidateHelp.locator("xpath=following-sibling::iframe[1]").contentFrame()
                    .locator("section.well[data-note='A&B'] strong");
            assertThat(helpPreview).hasText("Campagne Playwright");
            helpSource.click();
            assertThat(candidateHelp).hasValue(customHtml);
            helpSource.click();

            String editUrl = page.url();
            Response save = page.waitForResponse(response -> response.url().equals(baseUrl + "/admin/appliconfig")
                    && response.request().method().equals("POST"),
                    () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour")).click());
            assertEquals(302, save.status());
            page.navigate(editUrl);
            assertThat(date).hasValue("2035-11-25T14:30");
            assertThat(optionalDate).hasValue("");
            assertThat(color).hasValue("#126a9c");
            assertThat(picker).hasValue("#126a9c");
            assertThat(optionalColor).hasValue("");
            assertThat(editor.getByRole(AriaRole.LINK)).hasAttribute("href", "https://example.org/");
            assertThat(editor.locator("b, strong")).hasText("Lien Playwright");
            assertThat(editor.locator(".wysiwyg-color-red")).hasCSS("color", "rgb(255, 0, 0)");
            toggleSource.click();
            assertThat(richText).hasValue(Pattern.compile("https://example.org/"));
            footerSource.click();
            assertThat(footer).hasValue(existingFooter);
            helpSource.click();
            assertThat(candidateHelp).hasValue(customHtml);
        });
    }

    private void importerCandidatures(Page page) {
        page.navigate(baseUrl + "/postecandidatures");
        if (page.locator("#manageForm .candidature-checkbox").count() > 0) {
            return;
        }

        page.navigate(baseUrl + "/admin/galaxieexcels?form");
        page.getByLabel("Sélectionner fichier Excel").setInputFiles(Path.of("src/main/webapp/doc/EXTETBF.xls"));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Importer").setExact(true)).click();
        assertThat(page).hasURL(baseUrl + "/admin/galaxieexcels");

        page.navigate(baseUrl + "/admin/galaxieentrys");
        page.locator("form[action$='/generatecandidatspostes']").getByRole(AriaRole.BUTTON).click();
        assertThat(page).hasURL(baseUrl + "/admin/logimportgalaxies");
    }

    private Locator ligneAvecCellule(Page page, String texte) {
        return page.locator("tbody tr").filter(new Locator.FilterOptions().setHas(
                page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(texte).setExact(true))));
    }

    private void ouvrirCandidature(Page page, String email) {
        page.navigate(baseUrl + "/postecandidatures");
        ligneAvecCellule(page, email).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Voir").setExact(true)).click();
    }

    private void confirmerDecision(Page page, String modalId, String action) {
        page.locator("a[href='#" + modalId + "']").click();
        Locator dialog = page.locator("#" + modalId);
        List<String> confirmations = new ArrayList<>();
        page.onceDialog(confirmation -> {
            confirmations.add(confirmation.message());
            confirmation.accept();
        });
        Response response = page.waitForResponse(r -> r.url().endsWith("/" + action)
                        && r.request().method().equals("POST"),
                () -> dialog.getByRole(AriaRole.BUTTON,
                        new Locator.GetByRoleOptions().setName("Confirmer").setExact(true)).click());
        assertEquals(List.of("Êtes-vous sûr ?"), confirmations);
        assertEquals(302, response.status());
    }

    private void login(Page page) {
        renseignerConnexion(page, "admin", "admin");
        assertThat(page.locator("#toggleSidebar")).isVisible();
    }

    private void renseignerConnexion(Page page, String username, String password) {
        page.navigate(baseUrl + "/login");
        page.locator("#j_username").fill(username);
        page.locator("#j_password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Valider")).click();
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
