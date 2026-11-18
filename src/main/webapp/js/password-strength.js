function passwordStrength(value) {
    if (!value) {
        return null;
    }

    const hasLowercase = /[a-z]/.test(value);
    const hasDigit = /[0-9]/.test(value);
    const hasSpecial = /\W/.test(value);

    if (value.length >= 7 && value.length <= 30 && !/\s/.test(value)
        && hasLowercase && hasDigit && hasSpecial) {
        return { name: 'strong', label: 'Fort' };
    }

    const hasUppercase = /[A-Z]/.test(value);
    const variety = [hasLowercase, hasUppercase, hasDigit, hasSpecial].filter(Boolean).length;
    if (variety >= 3) {
        return { name: 'medium', label: 'Moyen' };
    }

    return { name: 'weak', label: 'Faible' };
}

for (const container of document.querySelectorAll('.password-container')) {
    const password = container.querySelector('input.strong-password');
    const meter = container.querySelector('.strength-indicator .meter');
    if (!password || !meter) {
        continue;
    }

    function updateMeter() {
        const strength = passwordStrength(password.value);
        meter.classList.remove('weak', 'medium', 'strong');
        meter.textContent = strength?.label ?? '';
        if (strength) {
            meter.classList.add(strength.name);
        }
    }

    password.addEventListener('input', updateMeter);
    password.addEventListener('focus', updateMeter);
    password.addEventListener('blur', updateMeter);
    password.form?.addEventListener('reset', () => requestAnimationFrame(updateMeter));
    updateMeter();
}
