(function () {
    function parseFilename(contentDisposition, defaultFilename) {
        if (!contentDisposition) {
            return defaultFilename;
        }

        const utfMatch = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
        if (utfMatch && utfMatch[1]) {
            try {
                return decodeURIComponent(utfMatch[1]);
            } catch (error) {
                return utfMatch[1];
            }
        }

        const match = contentDisposition.match(/filename="?([^";]+)"?/i);
        return match && match[1] ? match[1] : defaultFilename;
    }

    async function extractErrorMessage(response, fallbackMessage) {
        const text = await response.text();
        if (!text) {
            return fallbackMessage;
        }

        try {
            const payload = JSON.parse(text);
            return payload.message || fallbackMessage;
        } catch (error) {
            return text;
        }
    }

    function setLoadingState(button, loadingText) {
        const original = {
            html: button.innerHTML,
            disabled: button.disabled
        };

        button.disabled = true;
        button.classList.add('opacity-70', 'cursor-not-allowed');
        button.innerHTML = '<svg class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity="0.25" stroke-width="2"/><path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg><span>' + loadingText + '</span>';

        if (!button.classList.contains('flex')) {
            button.classList.add('inline-flex', 'items-center', 'gap-2');
        }

        return original;
    }

    function restoreState(button, original) {
        button.innerHTML = original.html;
        button.disabled = original.disabled;
        button.classList.remove('opacity-70', 'cursor-not-allowed');
    }

    async function downloadExcel(options) {
        const {
            url,
            token,
            button,
            defaultFilename = 'report.xlsx',
            loadingText = 'Đang xuất...',
            fallbackError = 'Xuất Excel thất bại.',
            onSuccess,
            onError
        } = options;

        const original = setLoadingState(button, loadingText);

        try {
            const headers = token ? { Authorization: 'Bearer ' + token } : {};
            const response = await fetch(url, { headers });
            if (!response.ok) {
                const message = await extractErrorMessage(response, fallbackError);
                throw new Error(message);
            }

            const blob = await response.blob();
            const filename = parseFilename(response.headers.get('content-disposition') || '', defaultFilename);

            const objectUrl = URL.createObjectURL(blob);
            const anchor = document.createElement('a');
            anchor.href = objectUrl;
            anchor.download = filename;
            document.body.appendChild(anchor);
            anchor.click();
            anchor.remove();
            URL.revokeObjectURL(objectUrl);

            if (typeof onSuccess === 'function') {
                onSuccess();
            }
        } catch (error) {
            if (typeof onError === 'function') {
                onError(error);
            } else {
                throw error;
            }
        } finally {
            restoreState(button, original);
        }
    }

    window.AdminExport = {
        downloadExcel: downloadExcel
    };
})();
