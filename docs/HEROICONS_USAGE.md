# Heroicons Usage Guidelines

## Giới thiệu

Heroicons là thư viện icon chính thức của Tailwind Labs, cung cấp hơn 450 icon đẹp, consistent và scalable. Dự án sử dụng Heroicons thay thế cho các SVG custom xấu để cải thiện hiệu năng và tính chuyên nghiệp.

## Cách sử dụng Heroicons

### 1. CDN Setup

Thêm CDN link vào `<head>` của HTML file:

```html
<!-- Heroicons CDN -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/heroicons@2.0.18/outline/index.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/heroicons@2.0.18/solid/index.min.css">
```

### 2. Cơ bản - Sử dụng Icon

#### Outline Icons (Mặc định - 24x24px)
```html
<svg class="w-6 h-6 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
  <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 1114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
</svg>
```

#### Solid Icons
```html
<svg class="w-6 h-6 text-primary" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
</svg>
```

### 3. Tailwind Size Classes

| Class | Size | Use Case |
|-------|------|----------|
| `w-4 h-4` | 16x16px | Nhỏ, inline, badges |
| `w-5 h-5` | 20x20px | Form fields, buttons |
| `w-6 h-6` | 24x24px | **Mặc định**, buttons, headers |
| `w-8 h-8` | 32x32px | Large buttons, hero sections |
| `w-10 h-10` | 40x40px | Navigation, avatars |
| `w-12 h-12` | 48x48px | Hero sections, large UI |

### 4. Tailwind Color Classes

```html
<!-- Sử dụng currentColor kết hợp với text color classes -->
<svg class="w-6 h-6 text-primary" ...>
<svg class="w-6 h-6 text-secondary" ...>
<svg class="w-6 h-6 text-success" ...>
<svg class="w-6 h-6 text-danger" ...>
<svg class="w-6 h-6 text-text-muted" ...>
```

### 5. Icon Naming Convention

**Outline Icons** (mặc định cho hầu hết trường hợp):
- User, UserGroup, Cog, Settings, Home, Building, Calendar, Clock, Lock, Eye, EyeSlash, Mail, Phone, MapPin, Badge, CheckCircle, ExclamationCircle, X, ChevronRight, etc.

**Solid Icons** (khi cần emphasis):
- Dùng cho các action important, status indicators
- Ví dụ: checkmark, error, warning states

### 6. Quy tắc sử dụng

#### Form Fields
```html
<!-- Icon bên phải input -->
<div class="relative">
  <input type="password" ... class="pr-10" />
  <button class="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-main">
    <svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
      <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
  </button>
</div>
```

#### Buttons with Icons
```html
<!-- Submit button với spinner -->
<button class="flex items-center justify-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover rounded-base text-white">
  <span>Đăng nhập</span>
  <svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12a7.5 7.5 0 1115 0m-15 0a7.5 7.5 0 1015 0m-15 0H3m15 0h3" />
  </svg>
</button>
```

#### Table & List Icons
```html
<!-- Action icons trong table row -->
<div class="flex gap-3">
  <button class="text-blue-500 hover:text-blue-700" title="Chi tiết">
    <svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
      <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
    </svg>
  </button>
  <button class="text-amber-500 hover:text-amber-700" title="Chỉnh sửa">
    <svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
    </svg>
  </button>
  <button class="text-red-500 hover:text-red-700" title="Xóa">
    <svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 2.991a48.814 48.814 0 00-7.48-.982c-2.318.0-4.465.698-6.143 1.712m15.848 1.495a48.44 48.44 0 00-3.494-.541c-2.257 0-4.368.75-6.148 2.016m7.48 0a48.998 48.998 0 00-7.48-.982m0 0c-2.318 0-4.465.698-6.143 1.712m0 0A17.933 17.933 0 012.031 12c0 5.591 3.824 10.29 9 11.622m0-20.667v.006.009v-.01m0 0c2.318 0 4.465.698 6.143 1.712" />
    </svg>
  </button>
</div>
```

#### Navigation Icons
```html
<!-- Navbar/sidebar navigation -->
<a href="/dashboard" class="flex items-center gap-3 p-3 rounded-base hover:bg-gray-100">
  <svg class="w-6 h-6 text-primary" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25" />
  </svg>
  <span>Trang chủ</span>
</a>
```

#### Status Indicators
```html
<!-- Success/Error/Warning states -->
<!-- Success -->
<div class="flex items-center gap-2 p-3 bg-green-50 border border-green-200 rounded-base">
  <svg class="w-5 h-5 text-success" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
  </svg>
  <span>Thành công!</span>
</div>

<!-- Error -->
<div class="flex items-center gap-2 p-3 bg-red-50 border border-red-200 rounded-base">
  <svg class="w-5 h-5 text-danger" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c.866 1.5 2.54 2.373 4.303 2.373.88 0 1.7-.23 2.413-.633M9 21H5.25A2.25 2.25 0 013 18.75V4.5A2.25 2.25 0 015.25 2.25h13.5A2.25 2.25 0 0121 4.5v14.25A2.25 2.25 0 0118.75 21M9 21h6m0 0h3.75A2.25 2.25 0 0021 18.75V4.5A2.25 2.25 0 0018.75 2.25H9M9 3h6m0 18v-3.75a2.25 2.25 0 00-2.25-2.25H9a2.25 2.25 0 00-2.25 2.25V21" />
  </svg>
  <span>Lỗi! Vui lòng thử lại</span>
</div>
```

### 7. Common Heroicons Used in This Project

| Icon Name | Path | Use Case |
|-----------|------|----------|
| User | `M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 1114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z` | User profiles, avatars |
| Eye / EyeSlash | Password visibility toggle |
| Check Circle | Status success |
| X Circle | Status error |
| ExclamationCircle | Alerts, warnings |
| ChevronRight | Navigation, breadcrumbs |
| Plus | Add actions |
| Trash | Delete actions |
| PencilSquare | Edit actions |
| Cog | Settings |
| Menu | Navigation toggle |
| Home | Dashboard |
| Building2 | Buildings |
| Door | Rooms |
| DocumentText | Contracts |

### 8. Performance Tips

1. **Sử dụng `stroke-width="1.5"` cho outline icons** - consistent sizing
2. **Dùng `currentColor`** - để inherit từ class color
3. **Cache CDN** - Browser sẽ cache Heroicons CDN
4. **Lazy load khi cần** - động tạo icon nếu cần

### 9. Accessibility

```html
<!-- Icon cần có aria-label hoặc title -->
<button title="Xóa" aria-label="Xóa item này">
  <svg class="w-5 h-5 text-danger" ...>...</svg>
</button>

<!-- Hoặc combine với text -->
<button>
  <svg class="w-5 h-5" ...></svg>
  <span>Xóa</span>
</button>
```

---

**Mục tiêu:** Sử dụng Heroicons một cách consistent, đẹp, và chuyên nghiệp trong toàn bộ ứng dụng.
