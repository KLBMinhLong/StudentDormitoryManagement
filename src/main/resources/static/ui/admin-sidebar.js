(function () {
    const STORAGE_KEY = 'admin_sidebar_collapsed';

    function ensureFontAwesome() {
        if (document.getElementById('fontAwesomeCdn')) return;
        const link = document.createElement('link');
        link.id = 'fontAwesomeCdn';
        link.rel = 'stylesheet';
        link.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css';
        document.head.appendChild(link);
    }

    const MENU = {
        dashboard: {
            href: '/admin',
            label: 'Bảng điều khiển',
            icon: 'fa-solid fa-gauge-high'
        },
        buildings: {
            href: '/admin/buildings',
            label: 'Quản lý tòa nhà',
            icon: 'fa-solid fa-building'
        },
        rooms: {
            href: '/admin/rooms',
            label: 'Quản lý phòng',
            icon: 'fa-solid fa-door-open'
        },
        beds: {
            href: '/admin/beds',
            label: 'Quản lý giường',
            icon: 'fa-solid fa-bed'
        },
        students: {
            href: '/admin/student_management.html',
            label: 'Quản lý sinh viên',
            icon: 'fa-solid fa-user-graduate'
        },
        contracts: {
            href: '/admin/contracts-management.html',
            label: 'Quản lý hợp đồng',
            icon: 'fa-solid fa-file-contract'
        },
        contractRegistration: {
            href: '/admin/contract_registration.html',
            label: 'Lập hợp đồng mới',
            icon: 'fa-solid fa-pen-to-square'
        },
        utilities: {
            href: '/admin/utility-records.html',
            label: 'Quản lý điện nước',
            icon: 'fa-solid fa-bolt'
        },
        invoices: {
            href: '/admin/invoices.html',
            label: 'Quản lý hóa đơn',
            icon: 'fa-solid fa-receipt'
        },
        maintenance: {
            href: '/admin/maintenance-kanban.html',
            label: 'Sửa chữa vật dụng',
            icon: 'fa-solid fa-screwdriver-wrench'
        },
        userHome: {
            href: '/home.html',
            label: 'Trang người dùng',
            icon: 'fa-solid fa-house'
        }
    };

    function renderMenuItem(key, activeKey) {
        const item = MENU[key];
        if (!item) return '';

        const activeClass = key === activeKey
            ? 'bg-primary text-white border-primary'
            : 'border-border text-text-main hover:bg-slate-50';

        return '<a href="' + item.href + '" class="sidebar-item rounded-base border px-3 py-2 text-sm font-medium ' + activeClass + '">' +
            '<span class="sidebar-item-icon"><i class="' + item.icon + '" aria-hidden="true"></i></span>' +
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

        ensureFontAwesome();

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
            '      <i class="fa-solid fa-angles-left" aria-hidden="true"></i>' +
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
            renderMenuItem('utilities', active) +
            renderMenuItem('invoices', active) +
            renderMenuItem('maintenance', active) +
            renderMenuItem('userHome', active) +
            '  </nav>' +
            '  <button id="adminSidebarLogout" type="button" class="sidebar-logout mt-4 w-full rounded-base bg-primary hover:bg-primary-hover text-white px-3 py-2 text-sm font-medium inline-flex items-center justify-center gap-2">' +
            '    <span class="sidebar-item-icon"><i class="fa-solid fa-right-from-bracket" aria-hidden="true"></i></span>' +
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