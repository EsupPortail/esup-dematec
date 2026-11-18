for (const field of document.querySelectorAll('input[data-native-color]')) {
    const picker = document.createElement('input');
    picker.type = 'color';
    picker.className = 'input-mini';
    picker.setAttribute('aria-label', 'Choisir la couleur — ' + field.labels[0].textContent.trim());

    function updatePicker() {
        const color = field.value.trim();
        if (/^#[0-9a-f]{6}$/i.test(color)) {
            picker.value = color;
        } else if (/^#[0-9a-f]{3}$/i.test(color)) {
            picker.value = color.replace(/[0-9a-f]/gi, digit => digit + digit);
        } else {
            picker.value = '#000000';
        }
    }

    field.addEventListener('input', updatePicker);
    picker.addEventListener('input', () => {
        field.value = picker.value;
    });
    updatePicker();
    field.before(picker);
}
