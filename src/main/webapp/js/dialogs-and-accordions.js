function fragmentTarget(selector) {
    return selector?.startsWith('#') ? document.getElementById(selector.slice(1)) : null;
}

const collapseTriggers = new Map();
const collapseAnimations = new WeakMap();
const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

function setCollapseState(panel, expanded) {
    panel.classList.toggle('in', expanded);
    panel.inert = !expanded;
    for (const trigger of collapseTriggers.get(panel)) {
        trigger.setAttribute('aria-expanded', String(expanded));
    }
}

function togglePanel(panel, expanded) {
    const height = panel.getBoundingClientRect().height;
    collapseAnimations.get(panel)?.cancel();
    const expandedHeight = panel.scrollHeight;
    setCollapseState(panel, expanded);

    const targetHeight = expanded ? expandedHeight : 0;
    if (height !== targetHeight && !reducedMotion.matches) {
        const animation = panel.animate(
            [{ height: height + 'px' }, { height: targetHeight + 'px' }],
            { duration: 350, easing: 'ease' }
        );
        collapseAnimations.set(panel, animation);
    }
}

for (const trigger of document.querySelectorAll('[data-demat-toggle="collapse"]')) {
    const panel = fragmentTarget(trigger.getAttribute('href'));
    if (!panel) {
        continue;
    }

    if (!collapseTriggers.has(panel)) {
        collapseTriggers.set(panel, []);
    }
    collapseTriggers.get(panel).push(trigger);
    trigger.setAttribute('aria-controls', panel.id);
    setCollapseState(panel, panel.classList.contains('in'));

    trigger.addEventListener('click', event => {
        event.preventDefault();
        const expanded = !panel.classList.contains('in');
        const group = fragmentTarget(trigger.dataset.parent);
        if (expanded && group) {
            for (const other of group.querySelectorAll(':scope > .accordion-group > .collapse.in')) {
                if (other !== panel && collapseTriggers.has(other)) {
                    togglePanel(other, false);
                }
            }
        }
        togglePanel(panel, expanded);
    });
}

function isBackdrop(event) {
    if (!event.target.matches('dialog.modal[open]')) {
        return false;
    }
    const bounds = event.target.getBoundingClientRect();
    return event.clientX < bounds.left || event.clientX >= bounds.right
        || event.clientY < bounds.top || event.clientY >= bounds.bottom;
}

let pressedBackdrop;
document.addEventListener('pointerdown', event => {
    pressedBackdrop = isBackdrop(event) ? event.target : null;
});

document.addEventListener('click', event => {
    const trigger = event.target.closest('[data-demat-toggle="modal"]');
    if (trigger) {
        const dialog = fragmentTarget(trigger.getAttribute('href'));
        if (dialog instanceof HTMLDialogElement) {
            event.preventDefault();
            dialog.showModal();
        }
        return;
    }

    const dismiss = event.target.closest('[data-demat-dismiss="modal"]');
    if (dismiss) {
        const dialog = dismiss.closest('dialog[open]');
        if (dialog) {
            event.preventDefault();
            dialog.close();
        }
        return;
    }

    if (pressedBackdrop === event.target && isBackdrop(event)) {
        event.target.close();
    }
});
