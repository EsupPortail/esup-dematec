(() => {
    const sidebarToggle = document.getElementById('toggleSidebar');
    const sidebar = document.getElementById('demat-left');
    const content = document.getElementById('demat-right');
    if (sidebarToggle && sidebar && content) {
        const icon = sidebarToggle.querySelector('i');

        function setSidebarCollapsed(collapsed) {
            icon.classList.toggle('icon-chevron-right', collapsed);
            icon.classList.toggle('icon-chevron-left', !collapsed);
            content.classList.toggle('span12', collapsed);
            content.classList.toggle('span9', !collapsed);
            sidebar.hidden = collapsed;
            sidebarToggle.setAttribute('aria-expanded', String(!collapsed));
        }

        setSidebarCollapsed(sessionStorage.getItem('dematSidebar') === 'left');
        sidebarToggle.addEventListener('click', event => {
            event.preventDefault();
            const collapsed = !sidebar.hidden;
            setSidebarCollapsed(collapsed);
            sessionStorage.setItem('dematSidebar', collapsed ? 'left' : 'right');
        });
    }

    let submittedForms = new WeakSet();
    window.addEventListener('pageshow', event => {
        if (event.persisted) {
            submittedForms = new WeakSet();
        }
    });

    document.addEventListener('submit', event => {
        const form = event.target;
        if (event.defaultPrevented || form.method === 'dialog'
            || form.querySelector('[data-export-format]:checked') || form.elements.namedItem('templateFile')?.value) {
            return;
        }

        if (submittedForms.has(form)) {
            event.preventDefault();
            alert('Envoi en cours, merci de patienter ... ou de recharger complètement votre page [F5] pour annuler.');
            return;
        }
        submittedForms.add(form);
    });

    for (const button of document.querySelectorAll('.reset[type="reset"]')) {
        button.addEventListener('click', event => {
            event.preventDefault();
            const form = button.form;

            for (const field of form.querySelectorAll('input[type="text"], input[type="password"], input[type="file"], textarea')) {
                field.value = '';
                if (field.hasAttribute('data-filter-select')) {
                    field.dispatchEvent(new Event('input'));
                }
            }
            for (const select of form.querySelectorAll('select')) {
                select.value = '';
            }
            for (const checkbox of form.querySelectorAll('input[type="checkbox"], input[type="radio"]')) {
                checkbox.checked = false;
            }
        });
    }

    for (const option of document.querySelectorAll('[data-export-format]')) {
        option.addEventListener('change', () => {
            if (option.checked) {
                for (const other of option.form.querySelectorAll('[data-export-format]:checked')) {
                    if (other !== option) {
                        other.checked = false;
                    }
                }
            }
        });
    }

    for (const select of document.querySelectorAll('select[data-toggle="page-size"]')) {
        select.setAttribute('aria-label', 'Résultats par page');
        select.addEventListener('change', () => {
            const url = select.selectedOptions[0]?.dataset.pageSizeUrl;
            if (url) {
                window.location.assign(url);
            }
        });
    }

    const manageForm = document.getElementById('manageForm');
    const selectAll = manageForm?.querySelector('#selectAll');
    if (selectAll) {
        const checkboxes = Array.from(manageForm.querySelectorAll('.candidature-checkbox'));
        const actions = manageForm.querySelector('#demat-pc-multi-manage');

        function updateSelection() {
            const count = checkboxes.filter(checkbox => checkbox.checked).length;
            selectAll.checked = count > 0 && count === checkboxes.length;
            selectAll.indeterminate = count > 0 && count < checkboxes.length;
            if (actions) {
                actions.hidden = count === 0;
            }
        }

        selectAll.disabled = checkboxes.length === 0;
        selectAll.addEventListener('change', () => {
            for (const checkbox of checkboxes) {
                checkbox.checked = selectAll.checked;
            }
            updateSelection();
        });
        for (const checkbox of checkboxes) {
            checkbox.addEventListener('change', updateSelection);
        }
        updateSelection();
    }

    for (const message of document.querySelectorAll('[data-alert-message]')) {
        alert(message.dataset.alertMessage);
    }
})();
