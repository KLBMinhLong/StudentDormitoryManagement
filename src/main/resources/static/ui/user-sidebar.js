(function () {
    const STORAGE_KEY = 'user_sidebar_collapsed';

    const MENU = {
        home: {
            href: '/home.html',
            label: 'Trang chủ',
            icon: 'fa-solid fa-house'
        },
        login: {
            href: '/login.html',
            label: 'Đăng nhập',
            icon: 'fa-solid fa-right-to-bracket'
        },
        profile: {
            href: '/user/student-profile.html',
            label: 'Hồ sơ của tôi',
            icon: 'fa-solid fa-id-card'
        },
        contracts: {
            href: '/user/contracts.html',
            label: 'Hợp đồng của tôi',
            icon: 'fa-solid fa-file-contract'
        },
        utilities: {
            href: '/user/utility-dashboard.html',
            label: 'Biểu đồ điện nước',
            icon: 'fa-solid fa-chart-column'
        },
        invoices: {
            href: '/user/my-invoices.html',
            label: 'Hóa đơn của tôi',
            icon: 'fa-solid fa-receipt'
        },
        issues: {
            href: '/user/report-issue.html',
            label: 'Báo hỏng',
            icon: 'fa-solid fa-wrench'
        }
    };

    function renderMenuItem(key, activeKey) {
        const item = MENU[key];
        if (!item) return '';

        const activeClass = key === activeKey
            ? 'bg-primary text-white border-primary shadow-soft'
            : 'border-border text-text-main hover:bg-slate-50';

        return '<a href="' + item.href + '" class="user-sidebar-item rounded-base border px-3 py-2 text-sm font-medium ' + activeClass + '">' +
            '<span class="user-sidebar-item-icon"><i class="' + item.icon + '" aria-hidden="true"></i></span>' +
            '<span class="user-sidebar-label">' + item.label + '</span>' +
            '</a>';
    }

    function applyCollapsedState(layout, collapsed) {
        if (!layout) return;

        if (collapsed && window.innerWidth >= 768) {
            layout.classList.add('sidebar-icons-only');
        } else {
            layout.classList.remove('sidebar-icons-only');
        }
    }

    function getSavedCollapsedState() {
        return localStorage.getItem(STORAGE_KEY) === '1';
    }

    function saveCollapsedState(collapsed) {
        localStorage.setItem(STORAGE_KEY, collapsed ? '1' : '0');
    }

    function mount(options) {
        const opts = options || {};
        const mountId = opts.mountId || 'userSidebarMount';
        const active = opts.active || 'home';
        const caption = opts.caption || 'Khu vực sinh viên';
        const authenticated = Boolean(opts.authenticated);

        const mountPoint = document.getElementById(mountId);
        const layout = document.getElementById('userLayout');
        if (!mountPoint || !layout) return;

        mountPoint.innerHTML = '' +
            '<aside class="user-sidebar-shell rounded-base bg-bg-card border border-border shadow-soft p-4 md:p-5 md:sticky md:top-6" aria-label="Thanh tác vụ người dùng">' +
            '  <div class="user-sidebar-top flex items-center justify-between gap-2 pb-4 border-b border-border">' +
            '    <div class="min-w-0">' +
            '      <p class="user-sidebar-brand text-xs text-text-muted">Ký túc xá sinh viên</p>' +
            '      <h1 class="user-sidebar-title font-heading text-lg mt-1">' + caption + '</h1>' +
            '    </div>' +
            '    <button id="userSidebarToggle" type="button" class="user-sidebar-toggle rounded-base border border-border px-2.5 py-2 text-text-main hover:bg-slate-50" aria-label="Thu gọn thanh tác vụ" title="Thu gọn / Mở rộng">' +
            '      <i class="fa-solid fa-angles-left" aria-hidden="true"></i>' +
            '    </button>' +
            '  </div>' +
            '  <nav class="mt-3 space-y-2 flex-1">' +
            renderMenuItem('home', active) +
                        (authenticated
                                ? renderMenuItem('profile', active) +
                                    renderMenuItem('contracts', active) +
                                    renderMenuItem('utilities', active) +
                                    renderMenuItem('invoices', active) +
                                    renderMenuItem('issues', active) +
                                    '</nav>' +
                                    '<button id="userSidebarLogout" type="button" class="user-sidebar-logout mt-4 w-full rounded-base bg-primary hover:bg-primary-hover text-white px-3 py-2 text-sm font-medium inline-flex items-center justify-center gap-2">' +
                                    '    <span class="user-sidebar-item-icon"><i class="fa-solid fa-right-from-bracket" aria-hidden="true"></i></span>' +
                                    '    <span class="user-sidebar-label">Đăng xuất</span>' +
                                    '</button>'
                                : renderMenuItem('login', active) +
                                    '</nav>' +
                                    '<div class="mt-4 rounded-base border border-dashed border-border bg-slate-50 px-3 py-3 text-xs text-text-muted">Đăng nhập để xem hồ sơ, hợp đồng, hóa đơn và biểu đồ điện nước.</div>') +
            '</aside>';

        const toggleBtn = document.getElementById('userSidebarToggle');
        const logoutBtn = document.getElementById('userSidebarLogout');

        applyCollapsedState(layout, getSavedCollapsedState());

        if (toggleBtn) {
            toggleBtn.addEventListener('click', function () {
                const willCollapse = !layout.classList.contains('sidebar-icons-only');
                applyCollapsedState(layout, willCollapse);
                saveCollapsedState(willCollapse);
            });
        }

        if (logoutBtn) {
            logoutBtn.addEventListener('click', function () {
                localStorage.removeItem('dormitory_access_token');
                window.location.href = '/login.html';
            });
        }

        window.addEventListener('resize', function () {
            applyCollapsedState(layout, getSavedCollapsedState());
        });
    }

    window.UserSidebar = { mount: mount };
})();