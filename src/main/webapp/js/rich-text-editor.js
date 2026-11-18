(() => {
    const { bootstrap, docs, demat } = document.currentScript.dataset;
    const template = document.getElementById('rich-text-toolbar');
    const linkDialog = document.getElementById('rich-text-link');
    const linkForm = linkDialog.querySelector('form');
    const linkUrl = linkForm.querySelector('input');
    let linkSelection;

    linkForm.addEventListener('submit', event => {
        event.preventDefault();
        const href = linkUrl.value.trim();
        if (!href || !linkSelection) {
            return;
        }

        const { editor, bookmark } = linkSelection;
        linkDialog.close();
        editor.focus();
        editor.composer.selection.setBookmark(bookmark);
        editor.composer.commands.exec('createLink', { href, target: '_blank', rel: 'nofollow' });
    });

    linkDialog.addEventListener('close', () => {
        linkSelection?.editor.focus();
        linkSelection = null;
    });

    for (const source of document.querySelectorAll('textarea.wysiwyg')) {
        const editor = new wysihtml5.Editor(source, {
            // Les fragments existants peuvent contenir du HTML personnalisé.
            parser: html => html,
            stylesheets: [bootstrap, docs, demat],
            sync: false
        });

        if (!editor.isCompatible()) {
            continue;
        }

        editor.on('load', () => {
            const toolbar = template.content.firstElementChild.cloneNode(true);
            const toggleSource = toolbar.querySelector('[data-editor-source]');
            const linkButton = toolbar.querySelector('[data-editor-link]');
            const commands = toolbar.querySelectorAll('[data-editor-command]');
            const composer = editor.composer;
            let visualBaseline = composer.getValue();

            const fieldLabel = source.labels[0].textContent.trim();
            toolbar.setAttribute('aria-label', 'Mise en forme — ' + fieldLabel);
            composer.iframe.title = fieldLabel;
            source.before(toolbar);

            function syncSource() {
                if (editor.currentView !== composer) {
                    return;
                }
                const html = composer.getValue();
                // Une prévisualisation seule ne doit pas réécrire le HTML enregistré.
                if (html !== visualBaseline) {
                    source.value = html;
                    visualBaseline = html;
                }
            }

            toggleSource.addEventListener('click', () => {
                const showSource = editor.currentView === composer;
                if (showSource) {
                    syncSource();
                } else {
                    composer.setValue(source.value);
                    visualBaseline = composer.getValue();
                }

                editor.fire('change_view', showSource ? 'textarea' : 'composer');
                toggleSource.setAttribute('aria-pressed', String(showSource));
                for (const control of commands) {
                    control.disabled = showSource;
                }
                linkButton.disabled = showSource;
            });

            function updateCommandStates() {
                for (const button of toolbar.querySelectorAll('button[data-editor-command][aria-pressed]')) {
                    button.setAttribute('aria-pressed', String(Boolean(composer.commands.state(button.dataset.editorCommand))));
                }
            }

            for (const control of commands) {
                if (control instanceof HTMLSelectElement) {
                    control.addEventListener('change', () => {
                        if (control.value) {
                            editor.focus();
                            composer.commands.exec(control.dataset.editorCommand, control.value);
                            control.value = '';
                        }
                    });
                } else {
                    control.addEventListener('mousedown', event => event.preventDefault());
                    control.addEventListener('click', () => {
                        editor.focus();
                        composer.commands.exec(control.dataset.editorCommand);
                        updateCommandStates();
                    });
                }
            }

            linkButton.addEventListener('mousedown', event => event.preventDefault());
            linkButton.addEventListener('click', () => {
                editor.focus();
                if (composer.commands.state('createLink')) {
                    composer.commands.exec('createLink');
                    return;
                }

                linkSelection = { editor, bookmark: composer.selection.getBookmark() };
                linkUrl.value = 'https://';
                linkDialog.showModal();
                linkUrl.select();
            });

            composer.doc.addEventListener('selectionchange', updateCommandStates);
            source.form.addEventListener('submit', syncSource);
        });
    }
})();
