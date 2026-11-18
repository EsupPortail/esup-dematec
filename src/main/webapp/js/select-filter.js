function optionTextMatcher(query) {
    try {
        const pattern = new RegExp(query, 'i');
        return text => pattern.test(text);
    } catch (error) {
        if (!(error instanceof SyntaxError)) {
            throw error;
        }

        // Une expression en cours de saisie peut être incomplète.
        const literal = query.toLowerCase();
        return text => text.toLowerCase().includes(literal);
    }
}

for (const input of document.querySelectorAll('input[data-filter-select]')) {
    const select = document.getElementById(input.dataset.filterSelect);
    if (!(select instanceof HTMLSelectElement)) {
        continue;
    }

    const options = Array.from(select.options, option => ({
        text: option.textContent,
        value: option.value
    }));

    input.addEventListener('input', () => {
        const matches = optionTextMatcher(input.value.trim());
        const filteredOptions = document.createDocumentFragment();

        for (const option of options) {
            if (matches(option.text)) {
                filteredOptions.append(new Option(option.text, option.value));
            }
        }

        select.replaceChildren(filteredOptions);
        select.scrollTop = 0;
        if (select.options.length === 1) {
            select.selectedIndex = 0;
        }
    });
}
