package fr.univrouen.poste.test.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.SelectOption;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
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
    public void recevabiliteGroupeeDesCandidaturesSelectionnees() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            assertEquals(200, page.navigate(baseUrl + "/postecandidatures").status());

            List<Locator> selected = List.of(ligneAvecCellule(page, "candidat2@example.org"),
                    ligneAvecCellule(page, "candidat4@example.org"));
            Locator unselected = ligneAvecCellule(page, "candidat@example.org");
            for (Locator row : selected) {
                assertThat(row.getByAltText("Recevable", new Locator.GetByAltTextOptions().setExact(true))).isVisible();
                row.locator(".candidature-checkbox").check();
            }
            assertThat(unselected.locator(".candidature-checkbox")).not().isChecked();

            page.onceDialog(dialog -> dialog.accept());
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Non Recevables").setExact(true)).click();
            assertEquals(200, page.reload().status());
            assertThat(unselected.getByAltText("Recevable", new Locator.GetByAltTextOptions().setExact(true))).isVisible();
            for (Locator row : selected) {
                assertThat(row.getByAltText("Non recevable", new Locator.GetByAltTextOptions().setExact(true))).isVisible();
                row.locator(".candidature-checkbox").check();
            }

            page.onceDialog(dialog -> dialog.accept());
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Recevables").setExact(true)).click();
            assertEquals(200, page.reload().status());
            for (Locator row : selected) {
                assertThat(row.getByAltText("Recevable", new Locator.GetByAltTextOptions().setExact(true))).isVisible();
            }
        });
    }

    @Test
    public void exportZipDesFichiersDuPosteSelectionne() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            Path pdf = Path.of("src/test/resources/doc-dummy.pdf");
            for (String candidate : List.of("candidat2@example.org", "candidat@example.org")) {
                ouvrirCandidature(page, candidate);
                Locator upload = page.locator("form[action$='/addFile']").first();
                upload.locator("input[type=file]").setInputFiles(pdf);
                upload.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Valider")).click();
                assertThat(page.getByRole(AriaRole.LINK,
                        new Page.GetByRoleOptions().setName(pdf.getFileName().toString()))).isVisible();
            }

            Locator filter = filtrerParPoste(page, "4087");
            filter.getByLabel("Zip").check();
            Download download = telechargerExport(page, filter, "demat.zip");
            try (ZipFile archive = new ZipFile(download.path().toFile())) {
                assertEquals(1, archive.size());
                var entry = archive.entries().nextElement();
                assertTrue(entry.getName(), entry.getName().matches("4087/ALIX-MICHELE-305/\\d+-doc-dummy\\.pdf"));
                try (var data = archive.getInputStream(entry)) {
                    assertArrayEquals(Files.readAllBytes(pdf), data.readAllBytes());
                }
            }
        });
    }

    @Test
    public void exportDesCourrielsDuPosteSelectionne() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);

            Locator filter = filtrerParPoste(page, "4087");
            filter.getByLabel("Liste mails").check();
            Download download = telechargerExport(page, filter, "emails.txt");
            assertEquals(List.of("candidat2@example.org", "candidat4@example.org"), Files.readAllLines(download.path()));
        });
    }

    @Test
    public void exportWordDesCandidaturesDuPosteSelectionne() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);

            Path template = Path.of("src/main/webapp/doc/template-multi-candidatures.docx");
            String filename = template.getFileName().toString();
            page.navigate(baseUrl + "/admin/templatefiles?form");
            page.getByLabel("Fichier", new Page.GetByLabelOptions().setExact(true)).setInputFiles(template);
            page.getByLabel("Type de fichier template").selectOption("MULTI_CANDIDATURES");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
            assertThat(page).hasURL(baseUrl + "/admin/templatefiles");

            ouvrirFormulaireDeModification(page, ligneAvecCellule(page, filename));
            Locator type = page.getByLabel("Type de fichier template");
            assertThat(type).hasValue("MULTI_CANDIDATURES");
            String templateId = page.locator("input[name=id]").inputValue();
            String replacementFilename = "modele-candidatures-modifie.docx";
            String replacementText = "Modèle Word remplacé par Playwright";
            try (var input = Files.newInputStream(template);
                 var document = new XWPFDocument(input);
                 var output = new ByteArrayOutputStream()) {
                document.createParagraph().createRun().setText(replacementText);
                document.write(output);
                page.getByLabel("Fichier", new Page.GetByLabelOptions().setExact(true)).setInputFiles(
                        new FilePayload(replacementFilename,
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                output.toByteArray()));
            }
            String templatesUrl = baseUrl + "/admin/templatefiles";
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
            assertThat(page).hasURL(templatesUrl);
            assertEquals(200, page.reload().status());
            assertThat(ligneAvecCellule(page, filename)).hasCount(0);
            ouvrirFormulaireDeModification(page, ligneAvecCellule(page, replacementFilename));
            assertThat(page.locator("input[name=id]")).hasValue(templateId);
            assertThat(type).hasValue("MULTI_CANDIDATURES");

            Locator filter = filtrerParPoste(page, "4087");
            filter.getByLabel("Export Word via un modèle").selectOption(new SelectOption().setLabel(replacementFilename));
            Download download = telechargerExport(page, filter, replacementFilename);
            try (var input = Files.newInputStream(download.path()); var document = new XWPFDocument(input)) {
                assertTrue(document.getParagraphs().stream().anyMatch(paragraph -> paragraph.getText().contains("4087")));
                assertTrue(document.getParagraphs().stream().anyMatch(paragraph -> paragraph.getText().equals(replacementText)));
                var rows = document.getTables().get(0).getRows();
                assertEquals(3, rows.size());
                List<String> names = rows.subList(1, rows.size()).stream()
                        .map(row -> row.getCell(1).getText().trim() + " " + row.getCell(2).getText().trim())
                        .sorted()
                        .toList();
                assertEquals(List.of("ALIX MICHELE", "MERLIN TITI"), names);
            }
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
    public void importEtFiltresDesCandidaturesPourUnMembreDeCommission() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            ouvrirCandidature(page, "candidat@example.org");
            confirmerDecision(page, "auditionnableDialog", "auditionnable");

            String member = "commission-playwright@example.org";
            String password = "Playwright123!";
            importerCommission(page, member, "4086", "4087");
            activerCompte(page, member, password);
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
    public void depotTelechargementEtSuppressionDUnRapportDeCommission() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            String author = "rapport-auteur-navigateur@example.org";
            String colleague = "rapport-collegue-navigateur@example.org";
            String password = "Commission-rapport-2026!";
            for (String member : List.of(author, colleague)) {
                importerCommission(page, member, "4087");
                activerCompte(page, member, password);
                login(page);
            }

            assertEquals(200, page.navigate(baseUrl + "/admin/appliconfig").status());
            ouvrirFormulaireDeModification(page, page.locator("body"));
            String configUrl = page.url();
            Locator allowDeletion = page.locator("input[name=membreSupprReviewFile]");
            boolean originalSetting = allowDeletion.isChecked();
            String candidate = "candidat2@example.org";
            String filename = "rapport-commission.pdf";
            byte[] content = Files.readAllBytes(Path.of("src/test/resources/doc-dummy.pdf"));
            Locator reportRow = ligneAvecCellule(page, filename);
            Locator remove = reportRow.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Supprimer"));
            try {
                allowDeletion.uncheck();
                saveCampaignSettings(page);
                page.context().clearCookies();
                renseignerConnexion(page, author, password);
                ouvrirCandidature(page, candidate);
                Locator upload = page.locator("form[action$='/addMemberReviewFile']");
                Locator input = upload.locator("input[type=file]");
                Locator send = upload.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Valider"));
                FilePayload report = new FilePayload(filename, "application/pdf", content);
                input.setInputFiles(report);
                send.click();
                assertEquals(200, page.reload().status());
                assertThat(reportRow.getByRole(AriaRole.CELL,
                        new Locator.GetByRoleOptions().setName(author).setExact(true))).isVisible();
                assertThat(remove).hasCount(0);

                List<String> alerts = new ArrayList<>();
                page.onceDialog(dialog -> {
                    alerts.add(dialog.message());
                    dialog.accept();
                });
                input.setInputFiles(report);
                send.click();
                page.waitForLoadState();
                assertEquals(List.of("Un fichier de même nom existe déjà !"), alerts);
                assertThat(reportRow).hasCount(1);

                page.context().clearCookies();
                login(page);
                assertEquals(200, page.navigate(configUrl).status());
                allowDeletion.check();
                saveCampaignSettings(page);
                page.context().clearCookies();
                renseignerConnexion(page, colleague, password);
                ouvrirCandidature(page, candidate);
                Locator downloadLink = reportRow.getByRole(AriaRole.LINK,
                        new Locator.GetByRoleOptions().setName(filename).setExact(true));
                Download download = page.waitForDownload(downloadLink::click);
                assertEquals(filename, download.suggestedFilename());
                assertArrayEquals(content, Files.readAllBytes(download.path()));
                assertThat(remove).hasCount(0);

                page.context().clearCookies();
                renseignerConnexion(page, author, password);
                ouvrirCandidature(page, candidate);
                page.onceDialog(dialog -> dialog.accept());
                remove.click();
                assertEquals(200, page.reload().status());
                assertThat(reportRow).hasCount(0);
            } finally {
                page.context().clearCookies();
                login(page);
                assertEquals(200, page.navigate(configUrl).status());
                allowDeletion.setChecked(originalSetting);
                saveCampaignSettings(page);
            }
        });
    }

    @Test
    public void switchUserVersUnMembrePuisRetourAdministrateur() {
        runInBrowser(page -> {
            String member = "commission-su-navigateur@example.org";
            login(page);
            importerCandidatures(page);
            importerCommission(page, member, "4086");
            activerCompte(page, member, "Commission-su-2026!");
            login(page);

            page.locator("#su a").click();
            Locator switchForm = page.locator("form[action$='/login/impersonate']");
            switchForm.locator("input[name=username]").fill(member);
            switchForm.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("SU").setExact(true)).click();

            Locator identity = page.locator("#logout input[type=submit]");
            assertThat(identity).hasValue("Déconnexion [" + member + "]");
            page.navigate(baseUrl + "/postecandidatures");
            assertThat(ligneAvecCellule(page, "4086").first()).isVisible();
            assertThat(ligneAvecCellule(page, "4087")).hasCount(0);
            assertThat(page.locator("#selectAll")).hasCount(0);

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("SORTIR DU SU !")).click();
            assertThat(identity).hasValue("Déconnexion [admin]");
            assertThat(page.locator("#su a")).isVisible();
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
    public void candidatureLocaleSansImportGalaxie() {
        runInBrowser(page -> {
            String email = "campagne-locale-navigateur@example.org";
            String password = "Campagne-locale-2026!";
            String post = "LOCAL-NAVIGATEUR";
            login(page);
            page.navigate(baseUrl + "/admin/appliconfig");
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Modifier").setExact(true)).click();
            String configUrl = page.url();
            Locator signupEnabled = page.locator("input[name=candidatCanSignup]");
            Locator registrationDeadline = page.locator("input[name=dateEndCandidat]");
            boolean originalSignup = signupEnabled.isChecked();
            String originalDeadline = registrationDeadline.inputValue();

            signupEnabled.check();
            registrationDeadline.fill("2035-11-25T14:30");
            saveCampaignSettings(page);
            String applicationUrl = null;
            try {
                page.navigate(baseUrl + "/posteapourvoirs?form");
                page.getByLabel("Num Emploi").fill(post);
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Modifier Poste")).click();
                page.getByLabel("Date End Signup Candidat").fill("2035-11-26T15:00");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Modifier").setExact(true)).click();

                page.context().clearCookies();
                page.navigate(baseUrl + "/login");
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Créer un compte")).click();
                page.locator("#_civilite_id").selectOption("Mme");
                page.locator("#_firstName_id").fill("Camille");
                page.locator("#_lastName_id").fill("Locale");
                page.locator("#_emailAddress_id").fill(email);
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer mon compte")).click();
                assertThat(page.getByText("Création de compte réussie.")).isVisible();

                login(page);
                activerCompte(page, email, password);
                renseignerConnexion(page, email, password);
                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Ajout de candidatures")).click();
                assertThat(page).hasURL(baseUrl + "/addpostecandidatures");
                Locator available = page.locator("li").filter(new Locator.FilterOptions().setHasText(post));
                available.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Candidater")).click();
                assertThat(available.getByText("Candidature enregistrée")).isVisible();
                page.navigate(baseUrl + "/postecandidatures");
                ligneAvecCellule(page, post).getByRole(AriaRole.LINK,
                        new Locator.GetByRoleOptions().setName("Voir").setExact(true)).click();
                applicationUrl = page.url();
                Locator upload = page.locator("form[action$='/addFile']");
                Path pdf = Path.of("src/test/resources/doc-dummy.pdf");
                upload.locator("input[type=file]").setInputFiles(pdf);
                upload.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Valider")).click();
                Locator file = page.getByRole(AriaRole.LINK,
                        new Page.GetByRoleOptions().setName(pdf.getFileName().toString()).setExact(true));
                Download download = page.waitForDownload(file::click);
                assertEquals(pdf.getFileName().toString(), download.suggestedFilename());
                assertArrayEquals(Files.readAllBytes(pdf), Files.readAllBytes(download.path()));

                page.onceDialog(dialog -> dialog.accept());
                ligneAvecCellule(page, pdf.getFileName().toString())
                        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Supprimer")).click();
                assertEquals(200, page.reload().status());
                assertThat(file).hasCount(0);

                String newPassword = "Campagne-locale-2804!";
                assertEquals(200, page.navigate(baseUrl + "/changepassword/index").status());
                Locator changePassword = page.locator("form[action$='/changepassword/update']");
                changePassword.locator("input[name=oldPassword]").fill(password);
                changePassword.locator("input[name=newPassword]").fill(newPassword);
                changePassword.locator("input[name=newPasswordAgain]").fill(newPassword);
                changePassword.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Enregistrer")).click();
                assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                        .setName("Votre mot de passe a été modifié avec succès."))).isVisible();
                page.context().clearCookies();
                renseignerConnexion(page, email, newPassword);
                assertThat(page.locator("#logout input[type=submit]")).hasValue("Déconnexion [" + email + "]");
            } finally {
                page.context().clearCookies();
                login(page);
                page.navigate(configUrl);
                if (applicationUrl != null) {
                    String csrfToken = page.locator("form[action$='/admin/appliconfig'] input[name=_csrf]").inputValue();
                    assertEquals(302, page.request().delete(applicationUrl, RequestOptions.create()
                            .setHeader("X-CSRF-TOKEN", csrfToken).setMaxRedirects(0)).status());
                }
                signupEnabled.setChecked(originalSignup);
                registrationDeadline.fill(originalDeadline);
                saveCampaignSettings(page);
            }
        });
    }

    private void saveCampaignSettings(Page page) {
        Response save = page.waitForResponse(response -> response.url().equals(baseUrl + "/admin/appliconfig")
                && response.request().method().equals("POST"),
                () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour")).click());
        assertEquals(302, save.status());
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

            assertEquals(200, page.navigate(baseUrl + "/posteapourvoirs?size=100").status());
            ouvrirFormulaireDeModification(page, ligneAvecCellule(page, "999999"));
            assertThat(date).hasValue("2035-11-26T15:00");
        });
    }

    @Test
    public void piecesJointesInternesDUnPoste() {
        runInBrowser(page -> {
            login(page);
            String postesUrl = baseUrl + "/posteapourvoirs";
            String numero = "999998";
            assertEquals(200, page.navigate(postesUrl + "?form").status());
            page.getByLabel("Num Emploi").fill(numero);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
            assertThat(page).hasURL(Pattern.compile("/posteapourvoirs/\\d+$"));
            String posteUrl = page.url();

            Path pdf = Path.of("src/test/resources/doc-dummy.pdf");
            List<Path> documents = List.of(pdf, Path.of("src/test/resources/doc-dummy.odt"));
            Locator upload = page.locator("form[action$='/addFile']");
            Locator fileInput = upload.locator("input[type=file]");
            Locator send = upload.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Envoyer"));
            for (Path document : documents) {
                fileInput.setInputFiles(document);
                send.click();
                assertThat(ligneAvecCellule(page, document.getFileName().toString())).isVisible();
            }

            String filename = pdf.getFileName().toString();
            List<String> alerts = new ArrayList<>();
            page.onceDialog(dialog -> {
                alerts.add(dialog.message());
                dialog.accept();
            });
            fileInput.setInputFiles(pdf);
            send.click();
            page.waitForLoadState();
            assertEquals(List.of("Un fichier de même nom existe déjà !"), alerts);
            assertThat(ligneAvecCellule(page, filename)).hasCount(1);

            String member = "pieces-poste-navigateur@example.org";
            String password = "Pieces-poste-2026!";
            importerCommission(page, member, numero);
            activerCompte(page, member, password);
            renseignerConnexion(page, member, password);
            assertEquals(200, page.navigate(postesUrl).status());
            ligneAvecCellule(page, numero).getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Voir").setExact(true)).click();
            assertThat(page).hasURL(posteUrl);
            assertThat(upload).hasCount(0);
            assertThat(page.locator("form[action*='/delFile/']")).hasCount(0);

            Download download = page.waitForDownload(() -> page.getByRole(AriaRole.LINK,
                    new Page.GetByRoleOptions().setName(filename).setExact(true)).click());
            assertEquals(filename, download.suggestedFilename());
            assertArrayEquals(Files.readAllBytes(pdf), Files.readAllBytes(download.path()));

            Locator export = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Export zip"));
            Download zip = page.waitForDownload(export::click);
            assertTrue(zip.suggestedFilename(), zip.suggestedFilename().matches(numero + "-poste-[0-9-]+\\.zip"));
            try (ZipFile archive = new ZipFile(zip.path().toFile())) {
                assertEquals(documents.size(), archive.size());
                for (Path document : documents) {
                    var entry = archive.stream()
                            .filter(file -> file.getName().matches("\\d+-" + Pattern.quote(document.getFileName().toString())))
                            .findFirst().orElseThrow();
                    try (var data = archive.getInputStream(entry)) {
                        assertArrayEquals(Files.readAllBytes(document), data.readAllBytes());
                    }
                }
            }

            page.context().clearCookies();
            login(page);
            assertEquals(200, page.navigate(posteUrl).status());
            for (Path document : documents) {
                Locator row = ligneAvecCellule(page, document.getFileName().toString());
                page.onceDialog(dialog -> dialog.accept());
                row.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Supprimer")).click();
                assertEquals(200, page.reload().status());
                assertThat(row).hasCount(0);
            }
            assertThat(export).hasCount(0);
        });
    }

    @Test
    public void modificationDUneEntreeGalaxieImportee() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            String email = "candidat2@example.org";
            String entriesUrl = baseUrl + "/admin/galaxieentrys";
            assertEquals(200, page.navigate(entriesUrl + "?search=" + email).status());
            ligneAvecCellule(page, email).first().getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Voir").setExact(true)).click();
            String detailUrl = page.url();
            Locator edit = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Modifier").setExact(true));
            Response form = page.waitForResponse(response -> response.url().startsWith(detailUrl + "?form"),
                    edit::click);
            assertEquals("Ouverture du formulaire de modification Galaxie", 200, form.status());
            assertThat(page.getByLabel("Email", new Page.GetByLabelOptions().setExact(true))).hasValue(email);
            Locator candidat = page.getByLabel("Candidat", new Page.GetByLabelOptions().setExact(true));
            Locator poste = page.getByLabel("Poste", new Page.GetByLabelOptions().setExact(true));
            Locator candidature = page.getByLabel("Candidature", new Page.GetByLabelOptions().setExact(true));
            String candidatId = candidat.inputValue();
            String posteId = poste.inputValue();
            String candidatureId = candidature.inputValue();
            assertTrue("Les associations de l'entrée importée sont présélectionnées",
                    !candidatId.isEmpty() && !posteId.isEmpty() && !candidatureId.isEmpty());

            String localisation = "Localisation modifiée avec Playwright";
            page.getByLabel("Localisation").fill(localisation);
            Response save = page.waitForResponse(response -> response.url().equals(entriesUrl)
                            && response.request().method().equals("POST"),
                    () -> page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour")).click());
            assertEquals("Enregistrement de l'entrée Galaxie", 302, save.status());
            assertThat(page).hasURL(detailUrl);
            assertEquals(200, page.reload().status());
            assertThat(page.getByText(localisation, new Page.GetByTextOptions().setExact(true))).isVisible();
            edit.click();
            assertThat(candidat).hasValue(candidatId);
            assertThat(poste).hasValue(posteId);
            assertThat(candidature).hasValue(candidatureId);
        });
    }

    @Test
    public void modificationDUnUtilisateurDepuisLaListeEtLaFiche() {
        runInBrowser(page -> {
            login(page);
            String usersUrl = baseUrl + "/admin/users";
            String email = "edition-utilisateur@example.org";
            page.navigate(usersUrl + "?form");
            page.locator("input[name=emailAddress]").fill(email);
            page.locator("input[name=password]").fill("Edition-navigateur-2026!");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
            assertThat(page).hasURL(Pattern.compile("/admin/users/\\d+$"));
            ouvrirFormulaireDeModification(page, page.locator("body"));
            assertThat(page.locator("input[name=emailAddress]")).hasValue(email);

            assertEquals(200, page.navigate(usersUrl + "?nomOrPrenomOrEmailAddress=" + email).status());
            ouvrirFormulaireDeModification(page, ligneAvecCellule(page, email));
            page.getByLabel("Prénom").fill("Prénom modifié");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour")).click();
            assertThat(page).hasURL(Pattern.compile("/admin/users/\\d+$"));
            ouvrirFormulaireDeModification(page, page.locator("body"));
            assertThat(page.getByLabel("Prénom")).hasValue("Prénom modifié");
        });
    }

    @Test
    public void consultationEtTelechargementDuFichierGalaxieImporte() {
        runInBrowser(page -> {
            login(page);
            importerCandidatures(page);
            Path workbook = Path.of("src/main/webapp/doc/EXTETBF.xls");
            String filename = workbook.getFileName().toString();
            assertEquals(200, page.navigate(baseUrl + "/admin/galaxieexcels").status());
            ligneAvecCellule(page, filename).getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName("Voir").setExact(true)).click();
            assertThat(ligneAvecCellule(page, "candidat2@example.org")).isVisible();
            Download download = page.waitForDownload(() ->
                    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Fichier original")).click());
            assertEquals(filename, download.suggestedFilename());
            assertArrayEquals(Files.readAllBytes(workbook), Files.readAllBytes(download.path()));
        });
    }

    @Test
    public void validationDuMappingGalaxieAvecUnFichierExcel() {
        runInBrowser(page -> {
            login(page);
            String mappingUrl = baseUrl + "/admin/galaxiemapping";
            assertEquals(200, page.navigate(mappingUrl).status());
            Locator file = page.locator("form[action$='/testFile'] input[type=file]");
            Locator test = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Tester le mapping"));
            List<String> messages = new ArrayList<>();
            page.onDialog(dialog -> {
                messages.add(dialog.message());
                dialog.accept();
            });

            file.setInputFiles(Path.of("src/main/webapp/doc/EXTETBF.xls"));
            test.click();
            assertThat(page).hasURL(mappingUrl);
            assertEquals(List.of("Test OK"), messages);

            try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
                var headers = workbook.createSheet().createRow(0);
                headers.createCell(0).setCellValue("N° emploi");
                headers.createCell(1).setCellValue("N° candidat");
                workbook.write(output);
                file.setInputFiles(new FilePayload("sans-email.xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray()));
            }
            test.click();
            assertThat(page).hasURL(mappingUrl);
            assertEquals(2, messages.size());
            assertTrue(messages.get(1), messages.get(1).contains("colonne(s) Email est (sont) manquante(s)"));
        });
    }

    @Test
    public void enregistrementEtRelectureDuMappingGalaxie() {
        runInBrowser(page -> {
            login(page);
            String listUrl = baseUrl + "/admin/galaxiemapping";
            assertEquals(200, page.navigate(listUrl).status());
            Locator row = page.locator("tbody tr");
            ouvrirFormulaireDeModification(page, row);
            verifierEnregistrementEtRestaurer(page, listUrl, row, page.getByLabel("ID email"), "Adresse électronique");
        });
    }

    @Test
    public void enregistrementEtRelectureDUnTypeDeFichier() {
        runInBrowser(page -> {
            login(page);
            String listUrl = baseUrl + "/admin/appliconfigfiletype";
            assertEquals(200, page.navigate(listUrl).status());
            Locator row = ligneAvecCellule(page, "Fichier de candidature");
            ouvrirFormulaireDeModification(page, row);
            verifierEnregistrementEtRestaurer(page, listUrl, row,
                    page.locator("input[name=candidatureNbFileMax]"), "4");
        });
    }

    private void verifierEnregistrementEtRestaurer(Page page, String listUrl, Locator row, Locator field, String newValue) {
        String editUrl = page.url();
        String originalValue = field.inputValue();
        Locator save = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour"));
        try {
            field.fill(newValue);
            save.click();
            assertThat(page).hasURL(listUrl);
            ouvrirFormulaireDeModification(page, row);
            assertThat(field).hasValue(newValue);
        } finally {
            assertEquals(200, page.navigate(editUrl).status());
            field.fill(originalValue);
            save.click();
            assertThat(page).hasURL(listUrl);
        }
    }

    @Test
    public void modificationDUnTagEtDeSaValeur() {
        runInBrowser(page -> {
            login(page);
            String tagsUrl = baseUrl + "/admin/candidaturetags";
            String valuesUrl = baseUrl + "/admin/candidaturevaluetags";
            String tag = "Évaluation navigateur";
            String value = "À relire";
            page.navigate(tagsUrl + "?form");
            page.locator("input[name=name]").fill(tag);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Créer").setExact(true)).click();
            assertThat(page).hasURL(Pattern.compile("/admin/candidaturetags/\\d+\\?form$"));

            assertEquals(200, page.navigate(tagsUrl).status());
            ouvrirFormulaireDeModification(page, ligneAvecCellule(page, tag));
            Locator tagName = page.locator("input[name=name]");
            assertThat(tagName).hasValue(tag);
            String updatedTag = "Évaluation modifiée";
            tagName.fill(updatedTag);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour")).click();
            assertThat(tagName).hasValue(updatedTag);
            page.getByPlaceholder("Nouvelle valeur").fill(value);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ajouter Valeur")).click();
            assertThat(page.getByText(value, new Page.GetByTextOptions().setExact(true))).isVisible();

            assertEquals(200, page.navigate(tagsUrl).status());
            assertThat(ligneAvecCellule(page, updatedTag)).isVisible();
            Response values = page.waitForResponse(response -> response.url().equals(valuesUrl),
                    () -> page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Valeurs des tags")).click());
            assertEquals("Liste des valeurs de tags", 200, values.status());
            ouvrirFormulaireDeModification(page, ligneAvecCellule(page, value));
            Locator valueInput = page.locator("input[name=value]");
            assertThat(valueInput).hasValue(value);
            String updatedValue = "Relu par Playwright";
            valueInput.fill(updatedValue);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Mettre à jour")).click();
            assertEquals(200, page.reload().status());
            assertThat(ligneAvecCellule(page, updatedValue)
                    .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(updatedTag).setExact(true))).isVisible();
        });
    }

    private void ouvrirFormulaireDeModification(Page page, Locator container) {
        Locator link = container.getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Modifier").setExact(true));
        String editUrl = (String) link.evaluate("link => link.href");
        Response response = page.waitForResponse(result -> result.url().equals(editUrl), link::click);
        assertEquals("Ouverture du formulaire " + editUrl, 200, response.status());
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
            saveCampaignSettings(page);
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

    private void importerCommission(Page page, String email, String... postes) throws IOException {
        byte[] content;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Commission");
            var headers = sheet.createRow(0);
            headers.createCell(0).setCellValue("Poste");
            headers.createCell(1).setCellValue("Email");
            headers.createCell(2).setCellValue("Nom");
            headers.createCell(3).setCellValue("Prénom");
            for (int i = 0; i < postes.length; i++) {
                var row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(postes[i]);
                row.createCell(1).setCellValue(email);
                row.createCell(2).setCellValue("Commission");
                row.createCell(3).setCellValue("Playwright");
            }
            workbook.write(output);
            content = output.toByteArray();
        }

        page.navigate(baseUrl + "/admin/commissionexcels?form");
        page.locator("input[type=file]").setInputFiles(new FilePayload("commission.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content));
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Importer").setExact(true)).click();
        assertThat(page).hasURL(baseUrl + "/admin/commissionexcels");

        page.navigate(baseUrl + "/admin/commissionentrys");
        page.locator("form[action$='/generatecommissions']").getByRole(AriaRole.BUTTON).click();
        assertThat(page).hasURL(baseUrl + "/admin/logimportcommissions");
    }

    private void activerCompte(Page page, String email, String password) {
        // Le SMTP est désactivé pour cette suite. La fiche admin fournit la clé.
        page.navigate(baseUrl + "/admin/users?nomOrPrenomOrEmailAddress=" + email);
        ligneAvecCellule(page, email).getByRole(AriaRole.LINK,
                new Locator.GetByRoleOptions().setName("Voir").setExact(true)).click();
        Locator activationKey = page.locator(".well").filter(new Locator.FilterOptions()
                .setHas(page.getByText("Clé d'activation", new Page.GetByTextOptions().setExact(true))))
                .locator("span");
        assertThat(activationKey).hasText(Pattern.compile("[A-Za-z0-9_-]{43}"));
        String key = activationKey.innerText();

        page.context().clearCookies();
        page.navigate(baseUrl + "/signup/activate/" + email + "/" + key);
        page.locator("#_password_id").fill(password);
        page.locator("#_repeatPassword_id").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Confirmer")).click();
    }

    private void importerCandidatures(Page page) {
        page.navigate(baseUrl + "/postecandidatures");
        if (ligneAvecCellule(page, "candidat2@example.org").count() > 0) {
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

    private Locator filtrerParPoste(Page page, String poste) {
        page.navigate(baseUrl + "/postecandidatures");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Filtrer").setExact(true)).click();
        Locator filter = page.locator("#filterForm");
        filter.getByLabel("Postes", new Locator.GetByLabelOptions().setExact(true)).selectOption(poste);
        return filter;
    }

    private Download telechargerExport(Page page, Locator filter, String filename) {
        Locator submit = filter.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Trouver"));
        Download download = page.waitForDownload(submit::click);
        assertEquals(filename, download.suggestedFilename());
        return download;
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

    @FunctionalInterface
    private interface BrowserScenario {
        void run(Page page) throws IOException;
    }

    private void runInBrowser(BrowserScenario scenario) {
        try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"))) {
            context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
            Page page = context.newPage();
            List<String> pageErrors = new ArrayList<>();
            page.onPageError(pageErrors::add);
            boolean passed = false;
            try {
                scenario.run(page);
                assertTrue("Erreurs JavaScript : " + pageErrors, pageErrors.isEmpty());
                passed = true;
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
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
