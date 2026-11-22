/* ==========================================================================
   SkillConnect – Editable Mobile Home Screen Configuration & App Engine
   Every text, image path, icon, card field, and navigation destination is
   driven by DEFAULT_CONFIG / window.SKILLCONNECT_CONFIG.
   ========================================================================== */

const STORAGE_KEY = 'skillconnect_editable_config_v1';

const DEFAULT_CONFIG = {
  destinations: {
    brandHome: '#home',
    location: '#location-modal',
    gps: '#gps-request',
    notifications: '#notifications',
    search: '#search-filter',
    exploreNow: '#categories',
    viewAll: '#categories',
    categoryBase: '#category/',
    profileBase: '#professional/',
    callScheme: 'tel:',
    bookNow: '#booking-form',
    home: '#home',
    map: '#map',
    bookings: '#bookings',
    profile: '#profile'
  },
  header: {
    logoSrc: 'assets/icons/logo-skillconnect.svg',
    brandPrimary: 'Skill',
    brandAccent: 'Connect',
    subtitle: 'Find Skilled Professionals Near You',
    bellIconSrc: 'assets/icons/bell.svg',
    notificationCount: 3,
    locationPinSrc: 'assets/icons/pin-green.svg',
    locationText: 'Karachi, Sindh',
    locationOptions: [
      'Karachi, Sindh',
      'Gulshan-e-Iqbal, Karachi',
      'Clifton Block 5, Karachi',
      'DHA Phase 6, Karachi',
      'Lahore, Punjab',
      'Islamabad, ICT'
    ],
    gpsIconSrc: 'assets/icons/gps.svg',
    searchIconSrc: 'assets/icons/search.svg',
    searchPlaceholder: 'Search electrician, plumber, AC repair...'
  },
  banner: {
    activeSlide: 0,
    slides: [
      {
        id: 'slide-1',
        bgImageSrc: 'assets/images/banner-bg.svg',
        techImageSrc: 'assets/images/hero-technician.jpg',
        heading: 'Skilled Help.\nJust a Tap Away!',
        description: 'Find verified professionals\nnear your location',
        ctaLabel: 'Explore Now',
        ctaDestinationKey: 'exploreNow'
      },
      {
        id: 'slide-2',
        bgImageSrc: 'assets/images/banner-bg.svg',
        techImageSrc: 'assets/images/hero-technician.jpg',
        heading: 'Live GPS Tracking.\nZero Hidden Fees!',
        description: 'Connect directly with nearby\ntrusted local technicians',
        ctaLabel: 'Open Map',
        ctaDestinationKey: 'map'
      },
      {
        id: 'slide-3',
        bgImageSrc: 'assets/images/banner-bg.svg',
        techImageSrc: 'assets/images/hero-technician.jpg',
        heading: 'Book In Seconds.\nPay After Service!',
        description: 'Compare ratings, visit charges\n& call professionals directly',
        ctaLabel: 'View Bookings',
        ctaDestinationKey: 'bookings'
      }
    ]
  },
  categoriesSection: {
    heading: 'Popular Categories',
    viewAllLabel: 'View All',
    categories: [
      {
        id: 'electrician',
        label: 'Electrician',
        iconSrc: 'assets/icons/cat-electrician.svg',
        destination: '#category/electrician',
        isMore: false
      },
      {
        id: 'plumber',
        label: 'Plumber',
        iconSrc: 'assets/icons/cat-plumber.svg',
        destination: '#category/plumber',
        isMore: false
      },
      {
        id: 'ac-technician',
        label: 'AC Technician',
        iconSrc: 'assets/icons/cat-ac.svg',
        destination: '#category/ac-technician',
        isMore: false
      },
      {
        id: 'mechanic',
        label: 'Mechanic',
        iconSrc: 'assets/icons/cat-mechanic.svg',
        destination: '#category/mechanic',
        isMore: false
      },
      {
        id: 'carpenter',
        label: 'Carpenter',
        iconSrc: 'assets/icons/cat-carpenter.svg',
        destination: '#category/carpenter',
        isMore: false
      },
      {
        id: 'it-expert',
        label: 'IT Expert',
        iconSrc: 'assets/icons/cat-it.svg',
        destination: '#category/it-expert',
        isMore: false
      },
      {
        id: 'cctv-expert',
        label: 'CCTV Expert',
        iconSrc: 'assets/icons/cat-cctv.svg',
        destination: '#category/cctv-expert',
        isMore: false
      },
      {
        id: 'more',
        label: 'More',
        iconSrc: 'assets/icons/cat-more.svg',
        destination: '#categories',
        isMore: true
      }
    ],
    extraCategories: [
      { id: 'painter', label: 'Painter', iconSrc: 'assets/icons/cat-carpenter.svg' },
      { id: 'solar-tech', label: 'Solar Technician', iconSrc: 'assets/icons/cat-electrician.svg' },
      { id: 'appliance-repair', label: 'Appliance Repair', iconSrc: 'assets/icons/cat-ac.svg' },
      { id: 'welder', label: 'Welder', iconSrc: 'assets/icons/cat-mechanic.svg' },
      { id: 'generator-tech', label: 'Generator Tech', iconSrc: 'assets/icons/cat-electrician.svg' },
      { id: 'network-tech', label: 'Network Expert', iconSrc: 'assets/icons/cat-it.svg' }
    ]
  },
  nearbySection: {
    heading: 'Available Near You',
    pinIconSrc: 'assets/icons/pin-green.svg',
    verifiedIconSrc: 'assets/icons/verified.svg',
    starIconSrc: 'assets/icons/star.svg',
    phoneIconSrc: 'assets/icons/phone-green.svg',
    professionals: [
      {
        id: 'ali-raza',
        name: 'Ali Raza',
        verified: true,
        categoryId: 'electrician',
        categoryLabel: 'Electrician',
        experience: '5 Years Exp.',
        rating: '4.8',
        reviewsCount: 124,
        distance: '1.2 km',
        availableNow: true,
        availabilityLabel: 'Available Now',
        currencySymbol: '₹',
        chargeText: 'Rs. 500',
        chargeUnit: '(Visit)',
        phone: '+923001234567',
        photoSrc: 'assets/images/pro-ali-raza.jpg',
        callLabel: 'Call',
        bookLabel: 'Book Now',
        profileDestination: '#professional/ali-raza',
        bookDestination: '#booking-form',
        area: 'Gulshan-e-Iqbal, Karachi',
        bio: 'Certified residential & commercial electrician specializing in wiring, UPS/inverter setup, breaker panels, and emergency fault diagnostics.',
        mapX: 62,
        mapY: 36,
        showOnDefaultHome: true
      },
      {
        id: 'kamran-khan',
        name: 'Kamran Khan',
        verified: true,
        categoryId: 'plumber',
        categoryLabel: 'Plumber',
        experience: '7 Years Exp.',
        rating: '4.9',
        reviewsCount: 98,
        distance: '1.9 km',
        availableNow: true,
        availabilityLabel: 'Available Now',
        currencySymbol: '₹',
        chargeText: 'Rs. 600',
        chargeUnit: '(Visit)',
        phone: '+923012345678',
        photoSrc: 'assets/images/pro-ali-raza.jpg',
        callLabel: 'Call',
        bookLabel: 'Book Now',
        profileDestination: '#professional/kamran-khan',
        bookDestination: '#booking-form',
        area: 'PECHS Block 2, Karachi',
        bio: 'Leak detection, mixer installation, water motor & tank piping specialist.',
        mapX: 30,
        mapY: 64,
        showOnDefaultHome: false
      },
      {
        id: 'fahad-malik',
        name: 'Fahad Malik',
        verified: true,
        categoryId: 'ac-technician',
        categoryLabel: 'AC Technician',
        experience: '6 Years Exp.',
        rating: '4.7',
        reviewsCount: 86,
        distance: '2.4 km',
        availableNow: true,
        availabilityLabel: 'Available Now',
        currencySymbol: '₹',
        chargeText: 'Rs. 800',
        chargeUnit: '(Visit)',
        phone: '+923023456789',
        photoSrc: 'assets/images/pro-ali-raza.jpg',
        callLabel: 'Call',
        bookLabel: 'Book Now',
        profileDestination: '#professional/fahad-malik',
        bookDestination: '#booking-form',
        area: 'Clifton Block 5, Karachi',
        bio: 'Split & inverter AC master service, gas refilling, PCB diagnostics and installation.',
        mapX: 74,
        mapY: 72,
        showOnDefaultHome: false
      },
      {
        id: 'bilal-sheikh',
        name: 'Bilal Sheikh',
        verified: true,
        categoryId: 'mechanic',
        categoryLabel: 'Mechanic',
        experience: '8 Years Exp.',
        rating: '4.8',
        reviewsCount: 112,
        distance: '2.8 km',
        availableNow: true,
        availabilityLabel: 'Available Now',
        currencySymbol: '₹',
        chargeText: 'Rs. 700',
        chargeUnit: '(Visit)',
        phone: '+923034567890',
        photoSrc: 'assets/images/pro-ali-raza.jpg',
        callLabel: 'Call',
        bookLabel: 'Book Now',
        profileDestination: '#professional/bilal-sheikh',
        bookDestination: '#booking-form',
        area: 'Bahadurabad, Karachi',
        bio: 'Roadside diagnostics, battery jumpstart, hybrid scanning and brake repair.',
        mapX: 25,
        mapY: 31,
        showOnDefaultHome: false
      }
    ]
  },
  bottomNav: [
    {
      id: 'home',
      label: 'Home',
      iconSrc: 'assets/icons/nav-home.svg',
      destinationKey: 'home',
      destination: '#home'
    },
    {
      id: 'map',
      label: 'Map',
      iconSrc: 'assets/icons/nav-map.svg',
      destinationKey: 'map',
      destination: '#map'
    },
    {
      id: 'bookings',
      label: 'Bookings',
      iconSrc: 'assets/icons/nav-bookings.svg',
      destinationKey: 'bookings',
      destination: '#bookings'
    },
    {
      id: 'profile',
      label: 'Profile',
      iconSrc: 'assets/icons/nav-profile.svg',
      destinationKey: 'profile',
      destination: '#profile'
    }
  ],
  notifications: [
    {
      id: 'n1',
      title: 'Ali Raza is available nearby (1.2 km)',
      detail: 'Electrician • Instant dispatch open in Karachi, Sindh.',
      time: 'Just now',
      unread: true
    },
    {
      id: 'n2',
      title: 'Verified badge added to 14 local pros',
      detail: 'Updated background & skill verification completed.',
      time: '1h ago',
      unread: true
    },
    {
      id: 'n3',
      title: 'Free Direct Booking Active',
      detail: 'Zero platform commission — pay technicians directly after service.',
      time: 'Today',
      unread: true
    }
  ],
  bookings: [
    {
      id: 'bk-101',
      proName: 'Ali Raza',
      category: 'Electrician',
      area: 'Karachi, Sindh',
      dateTime: 'Today, 4:30 PM',
      charge: 'Rs. 500 (Visit)',
      status: 'Confirmed'
    }
  ]
};

function deepClone(obj) {
  return JSON.parse(JSON.stringify(obj));
}

function loadConfig() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const parsed = JSON.parse(saved);
      return {
        ...deepClone(DEFAULT_CONFIG),
        ...parsed,
        destinations: { ...DEFAULT_CONFIG.destinations, ...(parsed.destinations || {}) },
        header: { ...DEFAULT_CONFIG.header, ...(parsed.header || {}) },
        banner: { ...DEFAULT_CONFIG.banner, ...(parsed.banner || {}) },
        categoriesSection: { ...DEFAULT_CONFIG.categoriesSection, ...(parsed.categoriesSection || {}) },
        nearbySection: { ...DEFAULT_CONFIG.nearbySection, ...(parsed.nearbySection || {}) }
      };
    }
  } catch (err) {
    console.warn('Could not load saved config:', err);
  }
  return deepClone(DEFAULT_CONFIG);
}

window.SKILLCONNECT_CONFIG = loadConfig();

const appState = {
  currentView: 'home',
  searchQuery: '',
  selectedCategoryId: null,
  selectedMapProId: window.SKILLCONNECT_CONFIG.nearbySection.professionals[0]?.id || null,
  editMode: false,
  editorTab: 'header'
};

function saveConfig() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(window.SKILLCONNECT_CONFIG));
  } catch (err) {
    console.warn('Could not persist config to localStorage:', err);
  }
}

function showToast(message) {
  const toast = document.getElementById('scToast');
  if (!toast) return;
  toast.textContent = message;
  toast.classList.remove('hidden');
  clearTimeout(showToast._timer);
  showToast._timer = setTimeout(() => {
    toast.classList.add('hidden');
  }, 2600);
}

function escapeHtml(str) {
  return String(str ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function multilineHtml(str) {
  return escapeHtml(str).replace(/\n/g, '<br />');
}

/* ==================== ROUTER & CONFIGURABLE DESTINATIONS ==================== */
function navigateToDestination(dest, contextData = {}) {
  if (!dest) return;

  // Support external links if configured in SKILLCONNECT_CONFIG.destinations
  if (/^https?:\/\//i.test(dest)) {
    window.open(dest, '_blank', 'noopener');
    return;
  }

  if (dest.startsWith('tel:')) {
    window.location.href = dest;
    return;
  }

  if (dest === '#location-modal') {
    openLocationModal();
    return;
  }

  if (dest === '#gps-request') {
    requestBrowserGps();
    return;
  }

  if (dest === '#booking-form' && contextData.pro) {
    openBookingModal(contextData.pro);
    return;
  }

  if (dest.startsWith('#category/')) {
    const catId = dest.replace('#category/', '').trim();
    appState.selectedCategoryId = catId;
    switchView('categories');
    return;
  }

  if (dest.startsWith('#professional/')) {
    const proId = dest.replace('#professional/', '').trim();
    const pro = window.SKILLCONNECT_CONFIG.nearbySection.professionals.find((p) => p.id === proId);
    if (pro) openProfessionalModal(pro);
    return;
  }

  const clean = dest.replace(/^#/, '');
  if (['home', 'categories', 'map', 'bookings', 'profile', 'notifications'].includes(clean)) {
    if (clean === 'categories' && contextData.resetCategory) {
      appState.selectedCategoryId = null;
    }
    switchView(clean);
  }
}

function switchView(viewName) {
  appState.currentView = viewName;
  document.querySelectorAll('.view-screen').forEach((el) => el.classList.remove('view-active'));
  const target = document.getElementById(`view-${viewName}`);
  if (target) {
    target.classList.add('view-active');
  } else {
    document.getElementById('view-home').classList.add('view-active');
    appState.currentView = 'home';
  }

  if (viewName === 'categories') renderCategoriesPage();
  if (viewName === 'map') renderMapPage();
  if (viewName === 'bookings') renderBookingsPage();
  if (viewName === 'notifications') renderNotificationsPage();

  renderBottomNav();
  const screen = document.getElementById('mobileScreen');
  if (screen) screen.scrollTop = 0;
}

/* ==================== HOME SCREEN RENDERERS ==================== */
function renderHeader() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const h = cfg.header;

  document.getElementById('logoImg').src = h.logoSrc;
  document.getElementById('brandPrimary').textContent = h.brandPrimary;
  document.getElementById('brandAccent').textContent = h.brandAccent;
  document.getElementById('brandSubtitle').textContent = h.subtitle;
  document.getElementById('bellIconImg').src = h.bellIconSrc;

  const badge = document.getElementById('notificationBadge');
  badge.textContent = h.notificationCount;
  badge.classList.toggle('hidden', Number(h.notificationCount) <= 0);

  document.getElementById('locationPinImg').src = h.locationPinSrc;
  document.getElementById('locationText').textContent = h.locationText;
  document.getElementById('gpsIconImg').src = h.gpsIconSrc;
  document.getElementById('searchIconImg').src = h.searchIconSrc;
  document.getElementById('searchInput').placeholder = h.searchPlaceholder;
  document.getElementById('mapLocationSubtitle').textContent = `Centered around ${h.locationText}`;
}

function renderBanner() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const slides = cfg.banner.slides;
  const activeIdx = Math.max(0, Math.min(cfg.banner.activeSlide || 0, slides.length - 1));
  const slide = slides[activeIdx];

  document.getElementById('bannerBgImg').src = slide.bgImageSrc;
  document.getElementById('bannerTechImg').src = slide.techImageSrc;
  document.getElementById('bannerHeading').innerHTML = multilineHtml(slide.heading);
  document.getElementById('bannerDesc').innerHTML = multilineHtml(slide.description);
  document.getElementById('exploreBtnLabel').textContent = slide.ctaLabel;

  const dotsContainer = document.getElementById('bannerDots');
  dotsContainer.innerHTML = '';
  slides.forEach((s, idx) => {
    const dot = document.createElement('button');
    dot.type = 'button';
    dot.className = `sc-dot${idx === activeIdx ? ' active' : ''}`;
    dot.setAttribute('aria-label', `Banner slide ${idx + 1}`);
    dot.addEventListener('click', () => {
      cfg.banner.activeSlide = idx;
      renderBanner();
    });
    dotsContainer.appendChild(dot);
  });
}

function renderCategoriesGrid() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const catSec = cfg.categoriesSection;

  document.getElementById('popularCategoriesHeading').textContent = catSec.heading;
  document.getElementById('viewAllLabel').textContent = catSec.viewAllLabel;

  const grid = document.getElementById('categoriesGrid');
  grid.innerHTML = '';

  catSec.categories.forEach((cat) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `sc-cat-tile${appState.selectedCategoryId === cat.id ? ' active' : ''}`;
    btn.setAttribute('data-editable-id', `categories.${cat.id}`);
    btn.innerHTML = `
      <img src="${escapeHtml(cat.iconSrc)}" alt="${escapeHtml(cat.label)}" class="sc-cat-icon" />
      <span class="sc-cat-label">${escapeHtml(cat.label)}</span>
    `;
    btn.addEventListener('click', () => {
      if (appState.editMode) {
        openEditorToTab('categories');
        return;
      }
      if (cat.isMore) {
        navigateToDestination(cat.destination || cfg.destinations.viewAll, { resetCategory: true });
      } else {
        const dest = cat.destination || `${cfg.destinations.categoryBase}${cat.id}`;
        navigateToDestination(dest);
      }
    });
    grid.appendChild(btn);
  });
}

function buildProfessionalCardElement(pro) {
  const cfg = window.SKILLCONNECT_CONFIG;
  const n = cfg.nearbySection;

  const card = document.createElement('article');
  card.className = 'sc-pro-card';
  card.setAttribute('data-editable-id', `professional.${pro.id}`);
  card.setAttribute('tabindex', '0');

  card.innerHTML = `
    <div class="sc-pro-top">
      <img src="${escapeHtml(pro.photoSrc)}" alt="${escapeHtml(pro.name)}" class="sc-pro-avatar" />
      <div class="sc-pro-info">
        <div class="sc-pro-name-row">
          <h3 class="sc-pro-name">${escapeHtml(pro.name)}</h3>
          ${
            pro.verified
              ? `<img src="${escapeHtml(n.verifiedIconSrc)}" alt="Verified" class="sc-verified-icon" />`
              : ''
          }
        </div>
        <p class="sc-pro-meta">${escapeHtml(pro.categoryLabel)} &bull; ${escapeHtml(pro.experience)}</p>
        <div class="sc-pro-rating-row">
          <img src="${escapeHtml(n.starIconSrc)}" alt="Rating" class="sc-star-icon" />
          <span class="sc-rating-val">${escapeHtml(pro.rating)}</span>
          <span class="sc-rating-count">(${escapeHtml(pro.reviewsCount)} reviews)</span>
        </div>
      </div>
      <div class="sc-pro-status-col">
        <span class="sc-distance-pill">
          <img src="${escapeHtml(n.pinIconSrc)}" alt="" />
          <span>${escapeHtml(pro.distance)}</span>
        </span>
        <span class="sc-availability-badge">
          <span class="sc-status-dot${pro.availableNow ? '' : ' offline'}"></span>
          <span>${escapeHtml(pro.availabilityLabel)}</span>
        </span>
      </div>
    </div>
    <div class="sc-pro-bottom">
      <div class="sc-charge-text">
        <span class="sc-currency-symbol">${escapeHtml(pro.currencySymbol)}</span>
        <span class="sc-charge-amount">${escapeHtml(pro.chargeText)}</span>
        <span>${escapeHtml(pro.chargeUnit)}</span>
      </div>
      <div class="sc-card-actions">
        <a
          href="${escapeHtml(cfg.destinations.callScheme + pro.phone)}"
          class="sc-btn-call"
          aria-label="Call ${escapeHtml(pro.name)}"
        >
          <img src="${escapeHtml(n.phoneIconSrc)}" alt="" />
          <span>${escapeHtml(pro.callLabel || 'Call')}</span>
        </a>
        <button type="button" class="sc-btn-book">${escapeHtml(pro.bookLabel || 'Book Now')}</button>
      </div>
    </div>
  `;

  card.addEventListener('click', (e) => {
    if (appState.editMode) {
      e.preventDefault();
      openEditorToTab('professionals');
      return;
    }
    if (e.target.closest('.sc-btn-call') || e.target.closest('.sc-btn-book')) return;
    navigateToDestination(pro.profileDestination || `${cfg.destinations.profileBase}${pro.id}`);
  });

  const callBtn = card.querySelector('.sc-btn-call');
  callBtn.addEventListener('click', (e) => {
    if (appState.editMode) {
      e.preventDefault();
      openEditorToTab('professionals');
      return;
    }
    e.stopPropagation();
    showToast(`Calling ${pro.name} (${pro.phone})`);
  });

  const bookBtn = card.querySelector('.sc-btn-book');
  bookBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    if (appState.editMode) {
      openEditorToTab('professionals');
      return;
    }
    navigateToDestination(pro.bookDestination || cfg.destinations.bookNow, { pro });
  });

  return card;
}

function getFilteredProfessionals() {
  const pros = window.SKILLCONNECT_CONFIG.nearbySection.professionals;
  const q = appState.searchQuery.trim().toLowerCase();

  if (!q && !appState.selectedCategoryId) {
    const defaultVisible = pros.filter((p) => p.showOnDefaultHome !== false);
    return defaultVisible.length ? defaultVisible : pros.slice(0, 1);
  }

  return pros.filter((p) => {
    const matchesQuery =
      !q ||
      p.name.toLowerCase().includes(q) ||
      p.categoryLabel.toLowerCase().includes(q) ||
      p.area.toLowerCase().includes(q);
    const matchesCat = !appState.selectedCategoryId || p.categoryId === appState.selectedCategoryId;
    return matchesQuery && matchesCat;
  });
}

function renderProfessionalsHome() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const n = cfg.nearbySection;

  document.getElementById('nearbyPinImg').src = n.pinIconSrc;
  document.getElementById('availableNearYouHeading').textContent = n.heading;

  const filterChip = document.getElementById('resetFilterChip');
  const filterLabel = document.getElementById('activeFilterLabel');
  if (appState.searchQuery.trim()) {
    filterChip.classList.remove('hidden');
    filterLabel.textContent = `"${appState.searchQuery.trim()}"`;
  } else {
    filterChip.classList.add('hidden');
  }

  const list = document.getElementById('professionalsList');
  list.innerHTML = '';

  const filtered = getFilteredProfessionals();
  if (!filtered.length) {
    list.innerHTML = `
      <div class="info-card">
        <strong>No matching professionals found</strong>
        <span style="font-size:12.5px;color:#6b7280;">Try another search term or view all categories.</span>
      </div>
    `;
    return;
  }

  filtered.forEach((pro) => list.appendChild(buildProfessionalCardElement(pro)));
}

function renderBottomNav() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const nav = document.getElementById('bottomNav');
  nav.innerHTML = '';

  cfg.bottomNav.forEach((item) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    const isActive =
      appState.currentView === item.id ||
      (appState.currentView === 'categories' && item.id === 'home');
    btn.className = `sc-nav-item${isActive ? ' active' : ''}`;
    btn.setAttribute('data-editable-id', `bottomNav.${item.id}`);
    btn.innerHTML = `
      <img src="${escapeHtml(item.iconSrc)}" alt="" />
      <span>${escapeHtml(item.label)}</span>
    `;
    btn.addEventListener('click', () => {
      if (appState.editMode) {
        openEditorToTab('navigation');
        return;
      }
      const configuredDest =
        item.destination || cfg.destinations[item.destinationKey] || `#${item.id}`;
      navigateToDestination(configuredDest, { resetCategory: true });
    });
    nav.appendChild(btn);
  });
}

/* ==================== SUBPAGES ==================== */
function renderCategoriesPage() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const allCats = [
    ...cfg.categoriesSection.categories.filter((c) => !c.isMore),
    ...cfg.categoriesSection.extraCategories
  ];

  const activeCat = allCats.find((c) => c.id === appState.selectedCategoryId);
  document.getElementById('categoriesPageTitle').textContent = activeCat
    ? activeCat.label
    : 'All Service Categories';
  document.getElementById('categoriesPageSubtitle').textContent = activeCat
    ? `Verified ${activeCat.label} professionals near ${cfg.header.locationText}`
    : 'Choose any category to filter local skilled professionals';

  const container = document.getElementById('allCategoriesContainer');
  container.innerHTML = '';

  allCats.forEach((cat) => {
    const tile = document.createElement('button');
    tile.type = 'button';
    tile.className = `sc-cat-tile${appState.selectedCategoryId === cat.id ? ' active' : ''}`;
    tile.innerHTML = `
      <img src="${escapeHtml(cat.iconSrc)}" alt="${escapeHtml(cat.label)}" class="sc-cat-icon" />
      <span class="sc-cat-label">${escapeHtml(cat.label)}</span>
    `;
    tile.addEventListener('click', () => {
      appState.selectedCategoryId = appState.selectedCategoryId === cat.id ? null : cat.id;
      renderCategoriesPage();
    });
    container.appendChild(tile);
  });

  const prosList = document.getElementById('categoryProfessionalsList');
  prosList.innerHTML = '';
  const pros = cfg.nearbySection.professionals.filter(
    (p) => !appState.selectedCategoryId || p.categoryId === appState.selectedCategoryId
  );

  document.getElementById('categoryProsHeading').textContent = activeCat
    ? `${activeCat.label} Specialists (${pros.length})`
    : `All Nearby Professionals (${pros.length})`;

  if (!pros.length) {
    prosList.innerHTML = `
      <div class="info-card">
        <strong>No listings in this category yet</strong>
        <span style="font-size:12.5px;color:#6b7280;">You can add professionals in this category using the Live Editor.</span>
      </div>
    `;
    return;
  }

  pros.forEach((pro) => prosList.appendChild(buildProfessionalCardElement(pro)));
}

function renderMapPage() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const pros = cfg.nearbySection.professionals;
  const pinsContainer = document.getElementById('mapPinsContainer');
  pinsContainer.innerHTML = '';

  if (!appState.selectedMapProId && pros.length) {
    appState.selectedMapProId = pros[0].id;
  }

  pros.forEach((pro, idx) => {
    const pin = document.createElement('button');
    pin.type = 'button';
    pin.className = `map-pro-pin${appState.selectedMapProId === pro.id ? ' active' : ''}`;
    pin.style.left = `${pro.mapX || 25 + idx * 18}%`;
    pin.style.top = `${pro.mapY || 30 + (idx % 2) * 35}%`;
    pin.textContent = `${pro.name.split(' ')[0]} • ${pro.distance}`;
    pin.addEventListener('click', () => {
      appState.selectedMapProId = pro.id;
      renderMapPage();
    });
    pinsContainer.appendChild(pin);
  });

  const selectedWrap = document.getElementById('mapSelectedCardContainer');
  selectedWrap.innerHTML = '';
  const selectedPro = pros.find((p) => p.id === appState.selectedMapProId) || pros[0];
  if (selectedPro) {
    selectedWrap.appendChild(buildProfessionalCardElement(selectedPro));
  }
}

function renderBookingsPage() {
  const container = document.getElementById('bookingsHistoryContainer');
  const bookings = window.SKILLCONNECT_CONFIG.bookings;

  if (!bookings.length) {
    container.innerHTML = `<div class="info-card"><strong>No bookings yet</strong><span>Book any professional from Home to track visits here.</span></div>`;
    return;
  }

  container.innerHTML = bookings
    .map(
      (b) => `
      <div class="info-card">
        <div class="info-card-top">
          <strong>${escapeHtml(b.proName)} &bull; ${escapeHtml(b.category)}</strong>
          <span class="status-pill">${escapeHtml(b.status)}</span>
        </div>
        <div style="font-size:12.5px;color:#4b5563;">${escapeHtml(b.area)} &bull; ${escapeHtml(b.dateTime)}</div>
        <div style="font-size:12.5px;font-weight:600;color:#027245;">Charge: ${escapeHtml(b.charge)}</div>
      </div>
    `
    )
    .join('');
}

function renderNotificationsPage() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const container = document.getElementById('notificationsListContainer');
  container.innerHTML = cfg.notifications
    .map(
      (n) => `
      <div class="info-card" style="${n.unread ? 'border-color:#86efac;background:#f0fdf4;' : ''}">
        <div class="info-card-top">
          <strong style="font-size:13.5px;">${escapeHtml(n.title)}</strong>
          <span style="font-size:11px;color:#6b7280;">${escapeHtml(n.time)}</span>
        </div>
        <div style="font-size:12.5px;color:#374151;">${escapeHtml(n.detail)}</div>
      </div>
    `
    )
    .join('');
}

/* ==================== MODALS & GPS ==================== */
function closeModal() {
  document.getElementById('modalBackdrop').classList.add('hidden');
}

function openModal(html) {
  const backdrop = document.getElementById('modalBackdrop');
  const content = document.getElementById('modalContent');
  content.innerHTML = html;
  backdrop.classList.remove('hidden');
  content.querySelectorAll('[data-close-modal]').forEach((btn) => {
    btn.addEventListener('click', closeModal);
  });
}

function openLocationModal() {
  const h = window.SKILLCONNECT_CONFIG.header;
  openModal(`
    <div class="modal-header">
      <h3>Select Service Location</h3>
      <button type="button" class="modal-close-btn" data-close-modal aria-label="Close">&times;</button>
    </div>
    <div class="stack-form">
      ${h.locationOptions
        .map(
          (loc) => `
        <button type="button" class="location-option-btn${loc === h.locationText ? ' active' : ''}" data-pick-loc="${escapeHtml(loc)}">
          ${escapeHtml(loc)}
        </button>
      `
        )
        .join('')}
      <form id="customLocationForm" class="stack-form" style="margin-top:6px;">
        <label class="field-label">
          Or enter custom city / area
          <input type="text" id="customLocationInput" class="field-input" value="${escapeHtml(h.locationText)}" required />
        </label>
        <button type="submit" class="primary-full-btn">Update Location</button>
      </form>
    </div>
  `);

  document.querySelectorAll('[data-pick-loc]').forEach((btn) => {
    btn.addEventListener('click', () => {
      h.locationText = btn.getAttribute('data-pick-loc');
      saveConfig();
      renderHeader();
      closeModal();
      showToast(`Location set to ${h.locationText}`);
    });
  });

  document.getElementById('customLocationForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const val = document.getElementById('customLocationInput').value.trim();
    if (val) {
      h.locationText = val;
      saveConfig();
      renderHeader();
      closeModal();
      showToast(`Location updated to ${val}`);
    }
  });
}

function requestBrowserGps() {
  if (!('geolocation' in navigator)) {
    showToast('GPS geolocation is not supported in this browser.');
    return;
  }
  showToast('Requesting GPS location permission...');
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const lat = pos.coords.latitude.toFixed(3);
      const lng = pos.coords.longitude.toFixed(3);
      window.SKILLCONNECT_CONFIG.header.locationText = `GPS (${lat}, ${lng})`;
      saveConfig();
      renderHeader();
      showToast(`GPS synced (${lat}, ${lng})`);
    },
    () => {
      showToast('Location permission denied — using saved city.');
    },
    { timeout: 8000 }
  );
}

function openProfessionalModal(pro) {
  const n = window.SKILLCONNECT_CONFIG.nearbySection;
  openModal(`
    <div class="modal-header">
      <h3>Professional Profile</h3>
      <button type="button" class="modal-close-btn" data-close-modal aria-label="Close">&times;</button>
    </div>
    <div class="stack-form">
      <div class="sc-pro-top">
        <img src="${escapeHtml(pro.photoSrc)}" alt="${escapeHtml(pro.name)}" class="sc-pro-avatar" />
        <div class="sc-pro-info">
          <div class="sc-pro-name-row">
            <h3 class="sc-pro-name">${escapeHtml(pro.name)}</h3>
            ${pro.verified ? `<img src="${escapeHtml(n.verifiedIconSrc)}" alt="Verified" class="sc-verified-icon" />` : ''}
          </div>
          <p class="sc-pro-meta">${escapeHtml(pro.categoryLabel)} &bull; ${escapeHtml(pro.experience)}</p>
          <p class="sc-pro-meta">${escapeHtml(pro.area)} &bull; ${escapeHtml(pro.distance)}</p>
        </div>
      </div>
      <p style="margin:4px 0;font-size:13px;line-height:1.45;color:#374151;">${escapeHtml(pro.bio)}</p>
      <div class="info-card" style="background:#f8fafc;">
        <strong>Service Visit Charge: ${escapeHtml(pro.chargeText)} ${escapeHtml(pro.chargeUnit)}</strong>
        <span style="font-size:12px;color:#4b5563;">Direct payment after completion. No online payment gate.</span>
      </div>
      <div class="editor-row-2">
        <a href="${escapeHtml(window.SKILLCONNECT_CONFIG.destinations.callScheme + pro.phone)}" class="sc-btn-call" style="width:100%;height:42px;">
          <img src="${escapeHtml(n.phoneIconSrc)}" alt="" />
          <span>${escapeHtml(pro.callLabel || 'Call')}</span>
        </a>
        <button type="button" id="modalBookProBtn" class="sc-btn-book" style="width:100%;height:42px;">
          ${escapeHtml(pro.bookLabel || 'Book Now')}
        </button>
      </div>
    </div>
  `);

  document.getElementById('modalBookProBtn').addEventListener('click', () => {
    openBookingModal(pro);
  });
}

function openBookingModal(pro) {
  const h = window.SKILLCONNECT_CONFIG.header;
  openModal(`
    <div class="modal-header">
      <h3>Book ${escapeHtml(pro.name)}</h3>
      <button type="button" class="modal-close-btn" data-close-modal aria-label="Close">&times;</button>
    </div>
    <form id="bookingSubmitForm" class="stack-form">
      <div class="info-card" style="background:#f0fdf4;border-color:#bbf7d0;">
        <strong>${escapeHtml(pro.categoryLabel)} &bull; ${escapeHtml(pro.chargeText)} ${escapeHtml(pro.chargeUnit)}</strong>
        <span style="font-size:12px;color:#166534;">Free direct booking — pay ${escapeHtml(pro.name)} after job completion.</span>
      </div>
      <label class="field-label">
        Your Name
        <input type="text" id="bookingClientName" class="field-input" value="${escapeHtml(document.getElementById('userProfileNameInput').value)}" required />
      </label>
      <label class="field-label">
        Service Address
        <input type="text" id="bookingAddress" class="field-input" value="${escapeHtml(h.locationText)}" required />
      </label>
      <label class="field-label">
        Preferred Time
        <input type="text" id="bookingTime" class="field-input" value="Today, Within 1 Hour" required />
      </label>
      <label class="field-label">
        Work Description
        <textarea id="bookingNote" rows="2" class="field-input" placeholder="Describe the issue..."></textarea>
      </label>
      <button type="submit" class="primary-full-btn">Confirm Direct Booking</button>
    </form>
  `);

  document.getElementById('bookingSubmitForm').addEventListener('submit', (e) => {
    e.preventDefault();
    window.SKILLCONNECT_CONFIG.bookings.unshift({
      id: `bk-${Date.now()}`,
      proName: pro.name,
      category: pro.categoryLabel,
      area: document.getElementById('bookingAddress').value.trim(),
      dateTime: document.getElementById('bookingTime').value.trim(),
      charge: `${pro.chargeText} ${pro.chargeUnit}`,
      status: 'Requested'
    });
    saveConfig();
    closeModal();
    showToast(`Booking sent to ${pro.name}!`);
    switchView('bookings');
  });
}

/* ==================== LIVE CONTENT & ASSET EDITOR ==================== */
function openEditorToTab(tabName) {
  appState.editorTab = tabName || appState.editorTab;
  document.getElementById('editorPanel').classList.remove('hidden');
  document.querySelectorAll('#editorTabs .editor-tab').forEach((t) => {
    t.classList.toggle('active', t.getAttribute('data-tab') === appState.editorTab);
  });
  renderEditorBody();
}

function attachFileUploadHelper(inputEl, onDataUrl) {
  inputEl.addEventListener('change', () => {
    const file = inputEl.files && inputEl.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      onDataUrl(String(reader.result));
    };
    reader.readAsDataURL(file);
  });
}

function renderEditorBody() {
  const cfg = window.SKILLCONNECT_CONFIG;
  const body = document.getElementById('editorBody');
  body.innerHTML = '';

  if (appState.editorTab === 'header') {
    const slide = cfg.banner.slides[cfg.banner.activeSlide || 0];
    body.innerHTML = `
      <div class="editor-group">
        <h3 class="editor-group-title">Brand &amp; Header</h3>
        <div class="editor-row-2">
          <label class="field-label">Brand Primary <input id="edBrandPrimary" class="field-input" value="${escapeHtml(cfg.header.brandPrimary)}" /></label>
          <label class="field-label">Brand Accent <input id="edBrandAccent" class="field-input" value="${escapeHtml(cfg.header.brandAccent)}" /></label>
        </div>
        <label class="field-label">Tagline <input id="edSubtitle" class="field-input" value="${escapeHtml(cfg.header.subtitle)}" /></label>
        <label class="field-label">Logo Image Path
          <div class="editor-file-row">
            <input id="edLogoSrc" class="field-input" value="${escapeHtml(cfg.header.logoSrc)}" />
            <label class="editor-upload-btn">Upload<input type="file" id="edLogoUpload" accept="image/*" /></label>
          </div>
        </label>
        <div class="editor-row-2">
          <label class="field-label">Location Text <input id="edLocationText" class="field-input" value="${escapeHtml(cfg.header.locationText)}" /></label>
          <label class="field-label">Badge Count <input type="number" id="edNotifCount" class="field-input" value="${escapeHtml(cfg.header.notificationCount)}" /></label>
        </div>
        <label class="field-label">Search Placeholder <input id="edSearchPh" class="field-input" value="${escapeHtml(cfg.header.searchPlaceholder)}" /></label>
      </div>

      <div class="editor-group">
        <h3 class="editor-group-title">Promotional Banner &amp; Technician Photo</h3>
        <label class="field-label">Banner Background Image Path
          <div class="editor-file-row">
            <input id="edBannerBg" class="field-input" value="${escapeHtml(slide.bgImageSrc)}" />
            <label class="editor-upload-btn">Upload<input type="file" id="edBannerBgUpload" accept="image/*" /></label>
          </div>
        </label>
        <label class="field-label">Technician Photo Path
          <div class="editor-file-row">
            <input id="edBannerTech" class="field-input" value="${escapeHtml(slide.techImageSrc)}" />
            <label class="editor-upload-btn">Upload<input type="file" id="edBannerTechUpload" accept="image/*" /></label>
          </div>
        </label>
        <label class="field-label">Banner Heading <textarea id="edBannerHeading" rows="2" class="field-input">${escapeHtml(slide.heading)}</textarea></label>
        <label class="field-label">Banner Description <textarea id="edBannerDesc" rows="2" class="field-input">${escapeHtml(slide.description)}</textarea></label>
        <label class="field-label">CTA Button Text <input id="edBannerCta" class="field-input" value="${escapeHtml(slide.ctaLabel)}" /></label>
      </div>
    `;

    const bind = (id, fn) => {
      document.getElementById(id).addEventListener('input', (e) => {
        fn(e.target.value);
        saveConfig();
        renderAllHome();
      });
    };

    bind('edBrandPrimary', (v) => (cfg.header.brandPrimary = v));
    bind('edBrandAccent', (v) => (cfg.header.brandAccent = v));
    bind('edSubtitle', (v) => (cfg.header.subtitle = v));
    bind('edLogoSrc', (v) => (cfg.header.logoSrc = v));
    bind('edLocationText', (v) => (cfg.header.locationText = v));
    bind('edNotifCount', (v) => (cfg.header.notificationCount = Number(v) || 0));
    bind('edSearchPh', (v) => (cfg.header.searchPlaceholder = v));
    bind('edBannerBg', (v) => (slide.bgImageSrc = v));
    bind('edBannerTech', (v) => (slide.techImageSrc = v));
    bind('edBannerHeading', (v) => (slide.heading = v));
    bind('edBannerDesc', (v) => (slide.description = v));
    bind('edBannerCta', (v) => (slide.ctaLabel = v));

    attachFileUploadHelper(document.getElementById('edLogoUpload'), (url) => {
      cfg.header.logoSrc = url;
      document.getElementById('edLogoSrc').value = url;
      saveConfig();
      renderHeader();
    });
    attachFileUploadHelper(document.getElementById('edBannerBgUpload'), (url) => {
      slide.bgImageSrc = url;
      document.getElementById('edBannerBg').value = url;
      saveConfig();
      renderBanner();
    });
    attachFileUploadHelper(document.getElementById('edBannerTechUpload'), (url) => {
      slide.techImageSrc = url;
      document.getElementById('edBannerTech').value = url;
      saveConfig();
      renderBanner();
    });
  } else if (appState.editorTab === 'categories') {
    body.innerHTML = `
      <div class="editor-group">
        <h3 class="editor-group-title">Section Header</h3>
        <div class="editor-row-2">
          <label class="field-label">Heading <input id="edCatHeading" class="field-input" value="${escapeHtml(cfg.categoriesSection.heading)}" /></label>
          <label class="field-label">View All Label <input id="edViewAllLabel" class="field-input" value="${escapeHtml(cfg.categoriesSection.viewAllLabel)}" /></label>
        </div>
      </div>
    `;
    document.getElementById('edCatHeading').addEventListener('input', (e) => {
      cfg.categoriesSection.heading = e.target.value;
      saveConfig();
      renderCategoriesGrid();
    });
    document.getElementById('edViewAllLabel').addEventListener('input', (e) => {
      cfg.categoriesSection.viewAllLabel = e.target.value;
      saveConfig();
      renderCategoriesGrid();
    });

    cfg.categoriesSection.categories.forEach((cat, idx) => {
      const group = document.createElement('div');
      group.className = 'editor-group';
      group.innerHTML = `
        <h3 class="editor-group-title">Tile ${idx + 1}: ${escapeHtml(cat.label)}</h3>
        <div class="editor-row-2">
          <label class="field-label">Label <input class="field-input ed-cat-label" value="${escapeHtml(cat.label)}" /></label>
          <label class="field-label">Destination URL <input class="field-input ed-cat-dest" value="${escapeHtml(cat.destination || '')}" /></label>
        </div>
        <label class="field-label">Icon Image Path
          <div class="editor-file-row">
            <input class="field-input ed-cat-icon" value="${escapeHtml(cat.iconSrc)}" />
            <label class="editor-upload-btn">Upload<input type="file" class="ed-cat-upload" accept="image/*" /></label>
          </div>
        </label>
      `;
      group.querySelector('.ed-cat-label').addEventListener('input', (e) => {
        cat.label = e.target.value;
        saveConfig();
        renderCategoriesGrid();
      });
      group.querySelector('.ed-cat-dest').addEventListener('input', (e) => {
        cat.destination = e.target.value;
        saveConfig();
      });
      group.querySelector('.ed-cat-icon').addEventListener('input', (e) => {
        cat.iconSrc = e.target.value;
        saveConfig();
        renderCategoriesGrid();
      });
      attachFileUploadHelper(group.querySelector('.ed-cat-upload'), (url) => {
        cat.iconSrc = url;
        group.querySelector('.ed-cat-icon').value = url;
        saveConfig();
        renderCategoriesGrid();
      });
      body.appendChild(group);
    });
  } else if (appState.editorTab === 'professionals') {
    const pro = cfg.nearbySection.professionals[0];
    body.innerHTML = `
      <div class="editor-group">
        <h3 class="editor-group-title">Section Heading</h3>
        <label class="field-label">Title <input id="edNearbyHeading" class="field-input" value="${escapeHtml(cfg.nearbySection.heading)}" /></label>
      </div>
      <div class="editor-group">
        <h3 class="editor-group-title">Featured Professional (${escapeHtml(pro.name)})</h3>
        <label class="field-label">Profile Photo Path
          <div class="editor-file-row">
            <input id="edProPhoto" class="field-input" value="${escapeHtml(pro.photoSrc)}" />
            <label class="editor-upload-btn">Upload<input type="file" id="edProPhotoUpload" accept="image/*" /></label>
          </div>
        </label>
        <div class="editor-row-2">
          <label class="field-label">Full Name <input id="edProName" class="field-input" value="${escapeHtml(pro.name)}" /></label>
          <label class="field-label">Trade <input id="edProTrade" class="field-input" value="${escapeHtml(pro.categoryLabel)}" /></label>
        </div>
        <div class="editor-row-2">
          <label class="field-label">Experience <input id="edProExp" class="field-input" value="${escapeHtml(pro.experience)}" /></label>
          <label class="field-label">Distance <input id="edProDist" class="field-input" value="${escapeHtml(pro.distance)}" /></label>
        </div>
        <div class="editor-row-2">
          <label class="field-label">Rating <input id="edProRating" class="field-input" value="${escapeHtml(pro.rating)}" /></label>
          <label class="field-label">Reviews Count <input type="number" id="edProReviews" class="field-input" value="${escapeHtml(pro.reviewsCount)}" /></label>
        </div>
        <div class="editor-row-2">
          <label class="field-label">Charge Text <input id="edProCharge" class="field-input" value="${escapeHtml(pro.chargeText)}" /></label>
          <label class="field-label">Phone (tel:) <input id="edProPhone" class="field-input" value="${escapeHtml(pro.phone)}" /></label>
        </div>
        <div class="editor-row-2">
          <label class="field-label">Call Button Text <input id="edProCall" class="field-input" value="${escapeHtml(pro.callLabel)}" /></label>
          <label class="field-label">Book Button Text <input id="edProBook" class="field-input" value="${escapeHtml(pro.bookLabel)}" /></label>
        </div>
      </div>
    `;

    const bindPro = (id, fn) => {
      document.getElementById(id).addEventListener('input', (e) => {
        fn(e.target.value);
        saveConfig();
        renderProfessionalsHome();
      });
    };
    document.getElementById('edNearbyHeading').addEventListener('input', (e) => {
      cfg.nearbySection.heading = e.target.value;
      saveConfig();
      renderProfessionalsHome();
    });
    bindPro('edProPhoto', (v) => (pro.photoSrc = v));
    bindPro('edProName', (v) => (pro.name = v));
    bindPro('edProTrade', (v) => (pro.categoryLabel = v));
    bindPro('edProExp', (v) => (pro.experience = v));
    bindPro('edProDist', (v) => (pro.distance = v));
    bindPro('edProRating', (v) => (pro.rating = v));
    bindPro('edProReviews', (v) => (pro.reviewsCount = Number(v) || 0));
    bindPro('edProCharge', (v) => (pro.chargeText = v));
    bindPro('edProPhone', (v) => (pro.phone = v));
    bindPro('edProCall', (v) => (pro.callLabel = v));
    bindPro('edProBook', (v) => (pro.bookLabel = v));

    attachFileUploadHelper(document.getElementById('edProPhotoUpload'), (url) => {
      pro.photoSrc = url;
      document.getElementById('edProPhoto').value = url;
      saveConfig();
      renderProfessionalsHome();
    });
  } else if (appState.editorTab === 'navigation') {
    cfg.bottomNav.forEach((item) => {
      const group = document.createElement('div');
      group.className = 'editor-group';
      group.innerHTML = `
        <h3 class="editor-group-title">Bottom Tab: ${escapeHtml(item.label)}</h3>
        <div class="editor-row-2">
          <label class="field-label">Label <input class="field-input ed-nav-label" value="${escapeHtml(item.label)}" /></label>
          <label class="field-label">Destination URL <input class="field-input ed-nav-dest" value="${escapeHtml(item.destination)}" /></label>
        </div>
        <label class="field-label">Icon Image Path
          <div class="editor-file-row">
            <input class="field-input ed-nav-icon" value="${escapeHtml(item.iconSrc)}" />
            <label class="editor-upload-btn">Upload<input type="file" class="ed-nav-upload" accept="image/*" /></label>
          </div>
        </label>
      `;
      group.querySelector('.ed-nav-label').addEventListener('input', (e) => {
        item.label = e.target.value;
        saveConfig();
        renderBottomNav();
      });
      group.querySelector('.ed-nav-dest').addEventListener('input', (e) => {
        item.destination = e.target.value;
        saveConfig();
      });
      group.querySelector('.ed-nav-icon').addEventListener('input', (e) => {
        item.iconSrc = e.target.value;
        saveConfig();
        renderBottomNav();
      });
      attachFileUploadHelper(group.querySelector('.ed-nav-upload'), (url) => {
        item.iconSrc = url;
        group.querySelector('.ed-nav-icon').value = url;
        saveConfig();
        renderBottomNav();
      });
      body.appendChild(group);
    });

    const destGroup = document.createElement('div');
    destGroup.className = 'editor-group';
    destGroup.innerHTML = `<h3 class="editor-group-title">Configurable Button Destinations</h3>`;
    Object.keys(cfg.destinations).forEach((key) => {
      const lbl = document.createElement('label');
      lbl.className = 'field-label';
      lbl.textContent = key;
      const inp = document.createElement('input');
      inp.className = 'field-input';
      inp.value = cfg.destinations[key];
      inp.addEventListener('input', (e) => {
        cfg.destinations[key] = e.target.value;
        saveConfig();
      });
      lbl.appendChild(inp);
      destGroup.appendChild(lbl);
    });
    body.appendChild(destGroup);
  }
}

function renderAllHome() {
  renderHeader();
  renderBanner();
  renderCategoriesGrid();
  renderProfessionalsHome();
  renderBottomNav();
}

function exportConfigJson() {
  const blob = new Blob([JSON.stringify(window.SKILLCONNECT_CONFIG, null, 2)], {
    type: 'application/json'
  });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'skillconnect-config.json';
  a.click();
  URL.revokeObjectURL(url);
  showToast('Exported skillconnect-config.json');
}

function resetConfigToDefault() {
  localStorage.removeItem(STORAGE_KEY);
  window.SKILLCONNECT_CONFIG = deepClone(DEFAULT_CONFIG);
  appState.selectedCategoryId = null;
  appState.searchQuery = '';
  document.getElementById('searchInput').value = '';
  renderAllHome();
  if (!document.getElementById('editorPanel').classList.contains('hidden')) {
    renderEditorBody();
  }
  showToast('Restored default SkillConnect design & config');
}

/* ==================== EVENT WIRING ==================== */
function initApp() {
  renderAllHome();

  document.getElementById('brandLogoTrigger').addEventListener('click', (e) => {
    e.preventDefault();
    if (appState.editMode) return openEditorToTab('header');
    navigateToDestination(window.SKILLCONNECT_CONFIG.destinations.brandHome);
  });

  document.getElementById('notificationBtn').addEventListener('click', () => {
    if (appState.editMode) return openEditorToTab('header');
    navigateToDestination(window.SKILLCONNECT_CONFIG.destinations.notifications);
  });

  document.getElementById('locationBtn').addEventListener('click', () => {
    if (appState.editMode) return openEditorToTab('header');
    navigateToDestination(window.SKILLCONNECT_CONFIG.destinations.location);
  });

  document.getElementById('gpsBtn').addEventListener('click', () => {
    if (appState.editMode) return openEditorToTab('header');
    navigateToDestination(window.SKILLCONNECT_CONFIG.destinations.gps);
  });

  const searchInput = document.getElementById('searchInput');
  const clearSearchBtn = document.getElementById('clearSearchBtn');
  searchInput.addEventListener('input', (e) => {
    appState.searchQuery = e.target.value;
    clearSearchBtn.classList.toggle('hidden', !appState.searchQuery);
    renderProfessionalsHome();
  });

  document.getElementById('searchForm').addEventListener('submit', (e) => {
    e.preventDefault();
    renderProfessionalsHome();
    if (appState.searchQuery.trim()) {
      showToast(`Filtering professionals for "${appState.searchQuery.trim()}"`);
    }
  });

  clearSearchBtn.addEventListener('click', () => {
    appState.searchQuery = '';
    searchInput.value = '';
    clearSearchBtn.classList.add('hidden');
    renderProfessionalsHome();
  });

  document.getElementById('resetFilterChip').addEventListener('click', () => {
    appState.searchQuery = '';
    appState.selectedCategoryId = null;
    searchInput.value = '';
    clearSearchBtn.classList.add('hidden');
    renderAllHome();
  });

  document.getElementById('exploreNowBtn').addEventListener('click', () => {
    if (appState.editMode) return openEditorToTab('header');
    const cfg = window.SKILLCONNECT_CONFIG;
    const slide = cfg.banner.slides[cfg.banner.activeSlide || 0];
    const dest = cfg.destinations[slide.ctaDestinationKey] || cfg.destinations.exploreNow;
    navigateToDestination(dest, { resetCategory: true });
  });

  document.getElementById('viewAllBtn').addEventListener('click', () => {
    if (appState.editMode) return openEditorToTab('categories');
    navigateToDestination(window.SKILLCONNECT_CONFIG.destinations.viewAll, { resetCategory: true });
  });

  document.querySelectorAll('[data-nav]').forEach((btn) => {
    btn.addEventListener('click', () => switchView(btn.getAttribute('data-nav')));
  });

  document.getElementById('mapGpsSyncBtn').addEventListener('click', requestBrowserGps);

  document.getElementById('markAllReadBtn').addEventListener('click', () => {
    window.SKILLCONNECT_CONFIG.notifications.forEach((n) => (n.unread = false));
    window.SKILLCONNECT_CONFIG.header.notificationCount = 0;
    saveConfig();
    renderHeader();
    renderNotificationsPage();
    showToast('All notifications marked as read');
  });

  document.getElementById('userProfileForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const name = document.getElementById('userProfileNameInput').value.trim() || 'Usman Ahmed';
    const area = document.getElementById('userProfileAreaInput').value.trim() || 'Karachi, Sindh';
    document.getElementById('userProfileNameDisplay').textContent = name;
    document.getElementById('userProfileLocationDisplay').textContent = area;
    document.getElementById('userProfileInitials').textContent = name
      .split(' ')
      .map((s) => s[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
    window.SKILLCONNECT_CONFIG.header.locationText = area;
    saveConfig();
    renderHeader();
    showToast('Profile saved');
  });

  document.getElementById('modalBackdrop').addEventListener('click', (e) => {
    if (e.target.id === 'modalBackdrop') closeModal();
  });

  // Editor Controls
  const toggleBtn = document.getElementById('toggleEditModeBtn');
  const setEditMode = (active) => {
    appState.editMode = active;
    document.body.classList.toggle('edit-mode-active', active);
    toggleBtn.classList.toggle('active', active);
    if (active) {
      openEditorToTab(appState.editorTab);
    } else {
      document.getElementById('editorPanel').classList.add('hidden');
    }
  };

  toggleBtn.addEventListener('click', () => setEditMode(!appState.editMode));
  document.getElementById('openEditorFromProfileBtn').addEventListener('click', () => setEditMode(true));
  document.getElementById('closeEditorBtn').addEventListener('click', () => setEditMode(false));

  document.querySelectorAll('#editorTabs .editor-tab').forEach((tab) => {
    tab.addEventListener('click', () => {
      openEditorToTab(tab.getAttribute('data-tab'));
    });
  });

  document.getElementById('exportConfigBtn').addEventListener('click', exportConfigJson);
  document.getElementById('editorExportBtn').addEventListener('click', exportConfigJson);
  document.getElementById('resetConfigBtn').addEventListener('click', resetConfigToDefault);
  document.getElementById('editorResetBtn').addEventListener('click', resetConfigToDefault);
}

document.addEventListener('DOMContentLoaded', initApp);
