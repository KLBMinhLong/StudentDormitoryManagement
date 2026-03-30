(function () {
    const STORAGE_KEY = 'admin_sidebar_collapsed';

    const MENU = {
        dashboard: {
            href: '/admin',
            label: 'Bảng điều khiển',
            icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M3 3h8v8H3zM13 3h8v5h-8zM13 10h8v11h-8zM3 13h8v8H3z"/></svg>'
        },
        buildings: {
            href: '/admin/buildings',
            label: 'Quản lý tòa nhà',
            icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M4 21V5a2 2 0 0 1 2-2h8v18M14 7h6v14M8 7h2M8 11h2M8 15h2"/></svg>'
        },
        rooms: {
            href: '/admin/rooms',
            label: 'Quản lý phòng',
            icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M3 20h18M5 20V7a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v13M9 10h6M9 14h6"/></svg>'
        },
        beds: {
            href: '/admin/beds',
            label: 'Quản lý giường',
            icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M3 11h18v6H3zM6 11V7a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v4M3 17v2M21 17v2"/></svg>'
        },
        students: {
            href: '/admin/student_management.html',
            label: 'Quản lý sinh viên',
            icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>'
        },
        contracts: {
            href: '/admin/contracts-management.html',
            label: 'Quản lý hợp đồng',
            icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>'
        },
        contractRegistration: {
            href: '/admin/contract_registration.html',
            label: 'Lập hợp đồng mới',
            icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>'
        },
        userHome: {
            href: '/home',
            label: 'Trang người dùng',
            icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M3 11.5 12 4l9 7.5M5 10v10h14V10"/></svg>'
        }
    };

    function renderMenuItem(key, activeKey) {
        const item = MENU[key];
        if (!item) return '';

        const activeClass = key === activeKey
            ? 'bg-primary text-white border-primary'
            : 'border-border text-text-main hover:bg-slate-50';

        return '<a href="' + item.href + '" class="sidebar-item rounded-base border px-3 py-2 text-sm font-medium ' + activeClass + '">' +
            '<span class="sidebar-item-icon">' + item.icon + '</span>' +
            '<span class="sidebar-item-label">' + item.label + '</span>' +
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
        const mountId = opts.mountId || 'adminSidebarMount';
        const active = opts.active || 'dashboard';
        const caption = opts.caption || 'Tác vụ quản trị';

        const mountPoint = document.getElementById(mountId);
        const layout = document.getElementById('adminLayout');
        if (!mountPoint || !layout) return;

        mountPoint.innerHTML = '' +
            '<aside class="admin-sidebar-shell rounded-base bg-bg-card border border-border shadow-soft p-4 md:p-5 md:sticky md:top-6" aria-label="Thanh tác vụ quản trị">' +
            '  <div class="sidebar-top flex items-center justify-between gap-2 pb-4 border-b border-border">' +
            '    <div class="min-w-0">' +
            '      <p class="sidebar-brand text-xs text-text-muted">Ký túc xá sinh viên</p>' +
            '      <h1 class="sidebar-app-title font-heading text-lg mt-1">' + caption + '</h1>' +
            '    </div>' +
            '    <button id="adminSidebarToggle" type="button" class="rounded-base border border-border px-2.5 py-2 text-text-main hover:bg-slate-50" aria-label="Thu gọn sidebar" title="Thu gọn / Mở rộng">' +
            '      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg>' +
            '    </button>' +
            '  </div>' +
            '  <nav class="mt-3 space-y-2 flex-1">' +
            renderMenuItem('dashboard', active) +
            renderMenuItem('buildings', active) +
            renderMenuItem('rooms', active) +
            renderMenuItem('beds', active) +
            renderMenuItem('students', active) +
            renderMenuItem('contracts', active) +
            renderMenuItem('contractRegistration', active) +
            renderMenuItem('userHome', active) +
            '  </nav>' +
            '  <button id="adminSidebarLogout" type="button" class="sidebar-logout mt-4 w-full rounded-base bg-primary hover:bg-primary-hover text-white px-3 py-2 text-sm font-medium inline-flex items-center justify-center gap-2">' +
            '    <span class="sidebar-item-icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/></svg></span>' +
            '    <span class="sidebar-logout-label">Đăng xuất</span>' +
            '  </button>' +
            '</aside>';

        const toggleBtn = document.getElementById('adminSidebarToggle');
        const logoutBtn = document.getElementById('adminSidebarLogout');

        const savedCollapsed = getSavedCollapsedState();
        applyCollapsedState(layout, savedCollapsed);

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

    window.AdminSidebar = { mount: mount };
})();