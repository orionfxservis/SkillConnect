/* ============================================================================
   SkillConnect — Laptop Web Admin Console & Live Current Mobile App Showcase
   Integrates with live Firebase Auth + Firestore via ./firebase-admin.js
   and preserves offline/preview functionality when signed out.
   ============================================================================ */

import {
  ADMIN_EMAIL,
  isAdminUser,
  subscribeAuthState,
  signInAdminWithGoogle,
  signOutAdmin,
  subscribePublicDirectory,
  subscribeAdminWorkspace,
  fetchVerificationDoc,
  reviewTechnicianAccount,
  updateUserAccount,
  savePromoBanner,
  deletePromoBanner,
  createOrUpdatePayment,
  approvePaymentAndRenewal,
  saveRenewalDecision,
  saveCategoryEntity,
  seedCategoriesList,
  updateBookingStatusAdmin,
  resolveComplaintAdmin
} from './firebase-admin.js';

const STORAGE_KEY = 'skillconnect_web_admin_v2';
const APK_RELEASE_URL = 'https://github.com/orionfxservis/SkillConnect/releases/tag/preview-apk';

/* ==================== 21 SERVICE CATEGORIES (MATCHING ANDROID Models.kt) ==================== */
const DEFAULT_CATEGORIES = [
  { id: 'electrician', name: 'Electrician', subtitle: 'Wiring, UPS, Solar & Breaker Repair', icon: '⚡', bg: '#fef3c7', sortOrder: 1, subcategories: ['Wiring', 'Breaker & DB', 'UPS & Inverter', 'Solar Setup', 'LED & Fan'] },
  { id: 'plumber', name: 'Plumber', subtitle: 'Leakage, Pipe Fitting, Motor & Tank', icon: '🔧', bg: '#dbeafe', sortOrder: 2, subcategories: ['Pipe Leakage', 'Water Motor', 'Bathroom Fitting', 'Geyser', 'Tank Cleaning'] },
  { id: 'ac_tech', name: 'AC & Fridge', subtitle: 'AC Service, Gas Refill & Cooling Repair', icon: '❄️', bg: '#e0f2fe', sortOrder: 3, subcategories: ['AC Service', 'Gas Refill', 'Inverter PCB', 'Fridge Repair', 'Installation'] },
  { id: 'carpenter', name: 'Carpenter', subtitle: 'Furniture, Doors, Locks & Cabinets', icon: '🪚', bg: '#ffedd5', sortOrder: 4, subcategories: ['Door Lock', 'Furniture Repair', 'Kitchen Cabinets', 'Polishing', 'Custom Wood'] },
  { id: 'mechanic', name: 'Auto Mechanic', subtitle: 'Car, Bike, Hybrid & Roadside Rescue', icon: '🚗', bg: '#fee2e2', sortOrder: 5, subcategories: ['Car Tuning', 'Bike Mechanic', 'AC & Battery', 'Roadside Rescue', 'Denting'] },
  { id: 'it_tech', name: 'IT & Computer', subtitle: 'Laptop, Wi-Fi, Printers & Networking', icon: '💻', bg: '#ede9fe', sortOrder: 6, subcategories: ['Laptop Repair', 'Windows & Software', 'Wi-Fi & Router', 'Printer Setup', 'Data Recovery'] },
  { id: 'cctv', name: 'CCTV & Security', subtitle: 'IP Cameras, DVR/NVR & Smart Locks', icon: '📹', bg: '#e2e8f0', sortOrder: 7, subcategories: ['CCTV Install', 'DVR/NVR Config', 'Biometric Lock', 'Intercom', 'Maintenance'] },
  { id: 'painter', name: 'Painter', subtitle: 'Interior, Exterior, Texture & Polish', icon: '🎨', bg: '#fce7f3', sortOrder: 8, subcategories: ['Interior Paint', 'Exterior Weather', 'Wall Texture', 'Wood Polish', 'Waterproofing'] },
  { id: 'mason', name: 'Mason & Civil', subtitle: 'Tiles, Marble, Plaster & Renovation', icon: '🧱', bg: '#fef2f2', sortOrder: 9, subcategories: ['Tile Fixing', 'Marble Polish', 'Plaster Work', 'Roof Seepage', 'Renovation'] },
  { id: 'cleaner', name: 'Home Cleaning', subtitle: 'Deep Clean, Sofa, Carpet & Tank Wash', icon: '🧹', bg: '#dcfce7', sortOrder: 10, subcategories: ['Deep Cleaning', 'Sofa & Carpet', 'Water Tank', 'Kitchen Degrease', 'Move-in Clean'] },
  { id: 'appliance', name: 'Appliance Repair', subtitle: 'Washing Machine, Microwave & Oven', icon: '🔌', bg: '#ecfeff', sortOrder: 11, subcategories: ['Washing Machine', 'Microwave', 'Dispenser', 'Kitchen Hood', 'LED TV'] },
  { id: 'welder', name: 'Welder & Steel', subtitle: 'Gates, Grills, Sheds & Iron Works', icon: '🔥', bg: '#ffedd5', sortOrder: 12, subcategories: ['Main Gate', 'Window Grills', 'Roof Shed', 'Stainless Steel', 'On-site Weld'] },
  { id: 'gardener', name: 'Gardener', subtitle: 'Lawn Care, Landscaping & Pruning', icon: '🌿', bg: '#dcfce7', sortOrder: 13, subcategories: ['Lawn Mowing', 'Landscaping', 'Tree Pruning', 'Pest Spray', 'Indoor Plants'] },
  { id: 'solar', name: 'Solar Energy', subtitle: 'Hybrid Inverters, Panels & Net Metering', icon: '☀️', bg: '#fef9c3', sortOrder: 14, subcategories: ['Hybrid System', 'Panel Cleaning', 'Net Metering', 'Battery Bank', 'Fault Check'] },
  { id: 'driver', name: 'Driver & Valet', subtitle: 'Family Driver, Pick & Drop, Outstation', icon: '🚘', bg: '#e0e7ff', sortOrder: 15, subcategories: ['Daily Driver', 'Outstation', 'Pick & Drop', 'Event Valet', 'Commercial'] },
  { id: 'tutor', name: 'Home Tutor', subtitle: 'Matric, O/A Levels, Science & Spoken', icon: '📚', bg: '#fae8ff', sortOrder: 16, subcategories: ['Math & Physics', 'O/A Levels', 'Primary Tuition', 'Quran & Arabic', 'Spoken English'] },
  { id: 'tailor', name: 'Tailor & Stitch', subtitle: 'Custom Suiting, Alterations & Ladies', icon: '🧵', bg: '#ffe4e6', sortOrder: 17, subcategories: ['Gents Suiting', 'Ladies Stitching', 'Alterations', 'Bridal Work', 'Curtains'] },
  { id: 'mover', name: 'Movers & Shifting', subtitle: 'Home Shifting, Packing & Loader Van', icon: '🚚', bg: '#fef3c7', sortOrder: 18, subcategories: ['House Shifting', 'Office Move', 'Loader Pickup', 'Packing Crew', 'Heavy Lift'] },
  { id: 'pest', name: 'Pest Control', subtitle: 'Termite, Bedbugs, Cockroach & Dengue', icon: '🛡️', bg: '#dcfce7', sortOrder: 19, subcategories: ['Termite Proof', 'Bedbugs', 'General Spray', 'Fumigation', 'Rodent Control'] },
  { id: 'beauty', name: 'Salon at Home', subtitle: 'Haircut, Grooming, Makeup & Spa', icon: '✂️', bg: '#fce7f3', sortOrder: 20, subcategories: ['Men Grooming', 'Bridal Makeup', 'Facial & Spa', 'Hair Styling', 'Mehndi'] },
  { id: 'photography', name: 'Photo & Events', subtitle: 'Events, Portraits, Video & Drone', icon: '📷', bg: '#e0f2fe', sortOrder: 21, subcategories: ['Wedding Shoot', 'Product Photo', 'Event Video', 'Drone Coverage', 'Editing'] }
];

const BANNER_PRESET_IMAGES = [
  { label: 'Hero Tech', path: 'assets/images/mobile/img_hero_technician.jpg' },
  { label: 'Dispatch', path: 'assets/images/mobile/img_hero_dispatch.jpg' },
  { label: 'Pro Expert', path: 'assets/images/mobile/img_pro_ali_raza.jpg' },
  { label: 'Slide 1', path: 'assets/images/mobile/img_cat_slide_1.jpg' },
  { label: 'Slide 2', path: 'assets/images/mobile/img_cat_slide_2.jpg' },
  { label: 'Slide 3', path: 'assets/images/mobile/img_cat_slide_3.jpg' },
  { label: 'Slide 4', path: 'assets/images/mobile/img_cat_slide_4.jpg' },
  { label: 'Slide 5', path: 'assets/images/mobile/img_cat_slide_5.jpg' }
];

const CITIES = [
  { name: 'Karachi, Sindh', lat: 24.8607, lng: 67.0011 },
  { name: 'Lahore, Punjab', lat: 31.5204, lng: 74.3587 },
  { name: 'Islamabad, ICT', lat: 33.6844, lng: 73.0479 },
  { name: 'Rawalpindi, Punjab', lat: 33.5651, lng: 73.0169 },
  { name: 'Multan, Punjab', lat: 30.1575, lng: 71.5249 }
];

const DAY_MS = 24 * 60 * 60 * 1000;

function makeInitialSeedData() {
  const now = Date.now();
  return {
    categories: DEFAULT_CATEGORIES.map((c) => ({ ...c, categoryId: c.id, active: true })),
    banners: [
      /* 3 Main Page (HOME) Banners */
      {
        bannerId: 'home_slide_1',
        placement: 'HOME',
        categoryId: 'all',
        eyebrow: 'Verified Local Pros',
        headline: 'Skilled Help. Just a Tap Away!',
        description: 'Find verified professionals near your location.',
        ctaLabel: 'Explore Now',
        ctaAction: 'explore_services',
        imageData: 'assets/images/mobile/img_hero_technician.jpg',
        active: true,
        sortOrder: 1
      },
      {
        bannerId: 'home_slide_2',
        placement: 'HOME',
        categoryId: 'all',
        eyebrow: 'Fast On-Time Dispatch',
        headline: 'Compare Rates & Live GPS',
        description: 'View transparent visit charges & customer ratings.',
        ctaLabel: 'Open Map',
        ctaAction: 'map',
        imageData: 'assets/images/mobile/img_hero_dispatch.jpg',
        active: true,
        sortOrder: 2
      },
      {
        bannerId: 'home_slide_3',
        placement: 'HOME',
        categoryId: 'all',
        eyebrow: 'SkillConnect Pro',
        headline: 'Unlimited Bookings & Leads',
        description: 'Grow your skilled services business across your city.',
        ctaLabel: 'Explore Pro Plan',
        ctaAction: 'subscription',
        imageData: 'assets/images/mobile/img_welcome_connect.jpg',
        active: true,
        sortOrder: 3
      },

      /* 5 Category Page Banners (exact 5 slides across every category page) */
      {
        bannerId: 'cat_slide_1',
        placement: 'CATEGORY',
        categoryId: 'all',
        eyebrow: 'Find Skilled Professionals',
        headline: 'Find the Right Professional!',
        description: 'Discover skilled and experienced professionals near your location.',
        ctaLabel: 'Explore Services',
        ctaAction: 'explore_services',
        imageData: 'assets/images/mobile/img_cat_slide_1.jpg',
        active: true,
        sortOrder: 1
      },
      {
        bannerId: 'cat_slide_2',
        placement: 'CATEGORY',
        categoryId: 'all',
        eyebrow: 'Need a Home Repair?',
        headline: 'Your Home, Our Professionals.',
        description: 'Find electricians, plumbers, carpenters and other home service experts.',
        ctaLabel: 'Find a Professional',
        ctaAction: 'map',
        imageData: 'assets/images/mobile/img_cat_slide_2.jpg',
        active: true,
        sortOrder: 2
      },
      {
        bannerId: 'cat_slide_3',
        placement: 'CATEGORY',
        categoryId: 'all',
        eyebrow: 'Grow Your Business',
        headline: 'Turn Your Skills Into Opportunities!',
        description: 'Register as a service provider and connect with customers in your area.',
        ctaLabel: 'Join SkillConnect',
        ctaAction: 'prohub',
        imageData: 'assets/images/mobile/img_cat_slide_3.jpg',
        active: true,
        sortOrder: 3
      },
      {
        bannerId: 'cat_slide_4',
        placement: 'CATEGORY',
        categoryId: 'all',
        eyebrow: 'Find Services Near You',
        headline: 'Local Services Made Easy.',
        description: 'Search nearby technicians, mechanics, teachers, gardeners and more.',
        ctaLabel: 'Find Nearby Services',
        ctaAction: 'map',
        imageData: 'assets/images/mobile/img_cat_slide_4.jpg',
        active: true,
        sortOrder: 4
      },
      {
        bannerId: 'cat_slide_5',
        placement: 'CATEGORY',
        categoryId: 'all',
        eyebrow: 'Unlimited Booking Opportunities',
        headline: 'Ready for More Bookings?',
        description: 'Explore SkillConnect Pro and enjoy unlimited service bookings.',
        ctaLabel: 'Explore Pro Plan',
        ctaAction: 'subscription',
        imageData: 'assets/images/mobile/img_cat_slide_5.jpg',
        active: true,
        sortOrder: 5
      }
    ],
    technicians: [
      {
        id: 'tech_ali_raza',
        userId: 'tech_ali_raza',
        fullName: 'Ali Raza',
        categoryId: 'electrician',
        subcategory: 'UPS & Inverter',
        expertise: 'UPS, Solar Inverter & 3-Phase Wiring Specialist',
        bio: '9 years residential & commercial electrical troubleshooting across Gulshan and Clifton.',
        yearsExperience: 9,
        contactPhone: '+92 300 4128901',
        phoneVerified: true,
        visitCharge: 'Rs. 500',
        availabilityHours: '8:00 AM - 10:00 PM',
        workingArea: 'Gulshan-e-Iqbal, Karachi',
        serviceRadiusKm: 18,
        availableNow: true,
        listed: true,
        verificationStatus: 'APPROVED',
        verifiedBadge: true,
        adminRemarks: 'CNIC & Electrical License verified',
        profilePhotoData: 'assets/images/mobile/img_pro_ali_raza.jpg',
        latitude: 24.918,
        longitude: 67.097,
        averageRating: 4.9,
        reviewCount: 64,
        completedJobs: 142
      },
      {
        id: 'tech_bilal_ahmed',
        userId: 'tech_bilal_ahmed',
        fullName: 'Bilal Ahmed',
        categoryId: 'plumber',
        subcategory: 'Pipe Leakage',
        expertise: 'PPRC Pipe Fitting, Water Motor & Bathroom Renovation',
        bio: 'Fast pressure testing and concealed leak detection without breaking tiles.',
        yearsExperience: 7,
        contactPhone: '+92 321 9845120',
        phoneVerified: true,
        visitCharge: 'Rs. 450',
        availabilityHours: '9:00 AM - 9:00 PM',
        workingArea: 'DHA Phase 6, Karachi',
        serviceRadiusKm: 15,
        availableNow: true,
        listed: true,
        verificationStatus: 'APPROVED',
        verifiedBadge: true,
        adminRemarks: 'Verified senior plumber',
        profilePhotoData: '',
        latitude: 24.806,
        longitude: 67.058,
        averageRating: 4.8,
        reviewCount: 41,
        completedJobs: 89
      },
      {
        id: 'tech_usman_ac',
        userId: 'tech_usman_ac',
        fullName: 'Usman Tariq',
        categoryId: 'ac_tech',
        subcategory: 'Inverter PCB',
        expertise: 'Split AC Master Service, Gas R410A & Inverter PCB',
        bio: 'Same-day cooling restore and coil jet wash with warranty.',
        yearsExperience: 6,
        contactPhone: '+92 333 7710924',
        phoneVerified: true,
        visitCharge: 'Rs. 700',
        availabilityHours: '9:00 AM - 11:00 PM',
        workingArea: 'PECHS Block 2, Karachi',
        serviceRadiusKm: 20,
        availableNow: true,
        listed: true,
        verificationStatus: 'APPROVED',
        verifiedBadge: true,
        adminRemarks: 'Verified HVAC technician',
        profilePhotoData: '',
        latitude: 24.872,
        longitude: 67.059,
        averageRating: 4.7,
        reviewCount: 29,
        completedJobs: 67
      },
      {
        id: 'tech_pending_kamran',
        userId: 'tech_pending_kamran',
        fullName: 'Kamran Siddiqui',
        categoryId: 'electrician',
        subcategory: 'Solar Setup',
        expertise: 'Domestic Wiring, Breaker Panel & Solar Hybrid Inverter',
        bio: 'Newly applied Skill Person awaiting laptop admin approval.',
        yearsExperience: 4,
        contactPhone: '+92 345 2201944',
        phoneVerified: true,
        visitCharge: 'Rs. 400',
        availabilityHours: '9:00 AM - 8:00 PM',
        workingArea: 'North Nazimabad, Karachi',
        serviceRadiusKm: 14,
        availableNow: true,
        listed: false,
        verificationStatus: 'PENDING',
        verifiedBadge: false,
        adminRemarks: '',
        cnicNumber: '42101-8834219-1',
        profilePhotoData: '',
        latitude: 24.94,
        longitude: 67.035,
        averageRating: 0,
        reviewCount: 0,
        completedJobs: 0
      },
      {
        id: 'tech_pending_farhan',
        userId: 'tech_pending_farhan',
        fullName: 'Farhan Mechanic',
        categoryId: 'mechanic',
        subcategory: 'Car Tuning',
        expertise: 'EFI Car Scanning, Hybrid Battery & Roadside Rescue',
        bio: 'Mobile car diagnostic specialist.',
        yearsExperience: 8,
        contactPhone: '+92 312 6540192',
        phoneVerified: true,
        visitCharge: 'Rs. 600',
        availabilityHours: '8:00 AM - 10:00 PM',
        workingArea: 'Johar Town, Lahore',
        serviceRadiusKm: 22,
        availableNow: true,
        listed: false,
        verificationStatus: 'PENDING',
        verifiedBadge: false,
        adminRemarks: '',
        cnicNumber: '35202-1940284-3',
        profilePhotoData: '',
        latitude: 31.469,
        longitude: 74.272,
        averageRating: 0,
        reviewCount: 0,
        completedJobs: 0
      }
    ],
    users: [
      { id: 'user_saad', userId: 'user_saad', displayName: 'Saad Mahmood', email: 'saad.m@example.com', phoneNumber: '+92 301 5550112', defaultArea: 'Gulshan, Karachi', role: 'CUSTOMER', enrolledTechnician: false, suspended: false },
      { id: 'tech_ali_raza', userId: 'tech_ali_raza', displayName: 'Ali Raza', email: 'ali.raza@example.com', phoneNumber: '+92 300 4128901', defaultArea: 'Gulshan, Karachi', role: 'PROVIDER', enrolledTechnician: true, suspended: false },
      { id: 'tech_bilal_ahmed', userId: 'tech_bilal_ahmed', displayName: 'Bilal Ahmed', email: 'bilal.p@example.com', phoneNumber: '+92 321 9845120', defaultArea: 'DHA, Karachi', role: 'PROVIDER', enrolledTechnician: true, suspended: false },
      { id: 'tech_pending_kamran', userId: 'tech_pending_kamran', displayName: 'Kamran Siddiqui', email: 'kamran.s@example.com', phoneNumber: '+92 345 2201944', defaultArea: 'North Nazimabad, Karachi', role: 'PROVIDER', enrolledTechnician: true, suspended: false }
    ],
    subscriptions: [
      {
        id: 'tech_ali_raza',
        userId: 'tech_ali_raza',
        providerName: 'Ali Raza',
        providerPhone: '+92 300 4128901',
        tier: 'PRO',
        renewalStatus: 'RENEWAL_PENDING',
        renewalExpiresAtMillis: now - 2 * DAY_MS,
        lastPaymentId: 'pay_ren_101',
        lastPaymentAmount: 499,
        approvedByAdmin: false,
        adminRemarks: 'Old Pro account submitted Rs. 499 JazzCash renewal'
      },
      {
        id: 'tech_bilal_ahmed',
        userId: 'tech_bilal_ahmed',
        providerName: 'Bilal Ahmed',
        providerPhone: '+92 321 9845120',
        tier: 'PRO',
        renewalStatus: 'EXPIRED',
        renewalExpiresAtMillis: now - 5 * DAY_MS,
        lastPaymentId: '',
        lastPaymentAmount: 499,
        approvedByAdmin: false,
        adminRemarks: 'Old account validity expired 5 days ago — needs renewal'
      },
      {
        id: 'tech_usman_ac',
        userId: 'tech_usman_ac',
        providerName: 'Usman Tariq',
        providerPhone: '+92 333 7710924',
        tier: 'PRO',
        renewalStatus: 'ACTIVE',
        renewalExpiresAtMillis: now + 24 * DAY_MS,
        lastPaymentId: 'pay_pro_100',
        lastPaymentAmount: 499,
        approvedByAdmin: true,
        adminRemarks: 'Renewed & approved for 1 month'
      }
    ],
    payments: [
      {
        id: 'pay_ren_101',
        paymentId: 'pay_ren_101',
        userId: 'tech_ali_raza',
        providerName: 'Ali Raza',
        providerPhone: '+92 300 4128901',
        categoryName: 'Electrician',
        planType: 'PRO_RENEWAL',
        amountPkr: 499,
        paymentMethod: 'JazzCash',
        transactionRef: 'JC-88921044',
        billingPeriodMonths: 1,
        receiptNote: 'Monthly Pro renewal sent via JazzCash mobile account',
        receiptImageData: '',
        status: 'PENDING',
        isRenewal: true,
        adminRemarks: '',
        createdAtMillis: now - 3 * 3600 * 1000
      },
      {
        id: 'pay_new_102',
        paymentId: 'pay_new_102',
        userId: 'tech_pending_kamran',
        providerName: 'Kamran Siddiqui',
        providerPhone: '+92 345 2201944',
        categoryName: 'Electrician',
        planType: 'PRO_NEW',
        amountPkr: 499,
        paymentMethod: 'EasyPaisa',
        transactionRef: 'EP-55201983',
        billingPeriodMonths: 1,
        receiptNote: 'New Skill Person Pro onboarding fee paid',
        receiptImageData: '',
        status: 'PENDING',
        isRenewal: false,
        adminRemarks: '',
        createdAtMillis: now - 7 * 3600 * 1000
      },
      {
        id: 'pay_pro_100',
        paymentId: 'pay_pro_100',
        userId: 'tech_usman_ac',
        providerName: 'Usman Tariq',
        providerPhone: '+92 333 7710924',
        categoryName: 'AC & Fridge',
        planType: 'PRO_RENEWAL',
        amountPkr: 499,
        paymentMethod: 'Bank Transfer',
        transactionRef: 'MEEZAN-40129',
        billingPeriodMonths: 1,
        receiptNote: 'Meezan IBFT confirmed',
        receiptImageData: '',
        status: 'APPROVED',
        isRenewal: true,
        adminRemarks: 'Verified in bank statement',
        createdAtMillis: now - 6 * DAY_MS
      }
    ],
    bookings: [
      {
        id: 'bk_901',
        clientName: 'Saad Mahmood',
        clientPhone: '+92 301 5550112',
        technicianId: 'tech_ali_raza',
        technicianName: 'Ali Raza',
        categoryId: 'electrician',
        serviceTitle: 'UPS Wiring & Breaker Check',
        serviceArea: 'Gulshan Block 13-D, Karachi',
        preferredTime: 'Today 4:00 PM',
        status: 'ACCEPTED'
      },
      {
        id: 'bk_902',
        clientName: 'Ayesha Khan',
        clientPhone: '+92 334 8801234',
        technicianId: 'tech_bilal_ahmed',
        technicianName: 'Bilal Ahmed',
        categoryId: 'plumber',
        serviceTitle: 'Kitchen Sink Pipe Leak',
        serviceArea: 'DHA Phase 6, Karachi',
        preferredTime: 'Tomorrow 11:00 AM',
        status: 'COMPLETED'
      }
    ],
    complaints: [
      {
        id: 'cmp_301',
        bookingId: 'bk_901',
        reporterName: 'Saad Mahmood',
        reportedTechId: 'tech_bilal_ahmed',
        reportedTechName: 'Bilal Ahmed',
        reason: 'Delayed Arrival',
        description: 'Arrived 40 minutes after scheduled slot without calling beforehand.',
        status: 'OPEN',
        adminResolution: ''
      }
    ],
    notifications: [
      { id: 'n1', title: 'New Account Pending', body: 'Kamran Siddiqui (Electrician) uploaded CNIC for approval.', time: 'Just now' },
      { id: 'n2', title: 'Old Account Renewal Submitted', body: 'Ali Raza paid Rs. 499 via JazzCash (JC-88921044) for renewal approval.', time: '3h ago' },
      { id: 'n3', title: 'Category Banners Synced', body: '5 promotional slides active at bottom of every category page.', time: 'Today' }
    ]
  };
}

function loadLocalData() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return makeInitialSeedData();
    const parsed = JSON.parse(raw);
    const fallback = makeInitialSeedData();
    return {
      categories: Array.isArray(parsed.categories) && parsed.categories.length ? parsed.categories : fallback.categories,
      banners: Array.isArray(parsed.banners) ? parsed.banners : fallback.banners,
      technicians: Array.isArray(parsed.technicians) ? parsed.technicians : fallback.technicians,
      users: Array.isArray(parsed.users) ? parsed.users : fallback.users,
      subscriptions: Array.isArray(parsed.subscriptions) ? parsed.subscriptions : fallback.subscriptions,
      payments: Array.isArray(parsed.payments) ? parsed.payments : fallback.payments,
      bookings: Array.isArray(parsed.bookings) ? parsed.bookings : fallback.bookings,
      complaints: Array.isArray(parsed.complaints) ? parsed.complaints : fallback.complaints,
      notifications: Array.isArray(parsed.notifications) ? parsed.notifications : fallback.notifications
    };
  } catch {
    return makeInitialSeedData();
  }
}

/* ==================== APPLICATION STATE ==================== */
const state = {
  authUser: null,
  isLiveAdmin: false,
  publicSynced: false,
  liveUnsubPublic: null,
  liveUnsubAdmin: null,

  adminSection: 'overview',
  accountFilter: 'PENDING', // PENDING | APPROVED | REJECTED | ALL_TECH | USERS | SUSPENDED
  accountSearch: '',
  renewalFilter: 'NEEDS_ACTION', // NEEDS_ACTION | RENEWAL_PENDING | EXPIRED | ACTIVE | ALL
  renewalSearch: '',
  bannerPlacementTab: 'ALL', // ALL | HOME | CATEGORY
  bannerCategoryFilter: 'all',
  paymentStatusFilter: 'ALL', // ALL | PENDING | APPROVED | REJECTED | REFUNDED
  paymentMethodFilter: 'ALL',
  paymentSearch: '',

  data: loadLocalData(),
  liveCollections: {
    categories: null,
    banners: null,
    technicians: null,
    users: null,
    subscriptions: null,
    payments: null,
    bookings: null,
    complaints: null
  },

  /* Current Mobile App Showcase State */
  mobileView: 'home', // home | categories | map | bookings | profile | subscription | prohub | notifications
  mobileLocation: CITIES[0],
  mobileSearch: '',
  selectedCategoryId: 'electrician',
  selectedSubcategory: 'All',
  categorySearch: '',
  selectedMapTechId: 'tech_ali_raza',
  homeBannerIndex: 0,
  categoryBannerIndex: 0,
  bannerAutoPausedUntil: 0,
  mobilePreviewPlan: 'FREE', // FREE | PRO
  mobileMonthlyBookingsUsed: 0
};

function persistLocal() {
  if (state.isLiveAdmin) return;
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state.data));
  } catch {
    // Ignore quota warnings for huge base64 previews
  }
}

/* Effective dataset: Live Firestore when available, otherwise clean local cache */
function getCategories() {
  const list = state.liveCollections.categories?.length
    ? state.liveCollections.categories
    : state.data.categories;
  return [...list].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
}

function getBanners() {
  const list = state.liveCollections.banners !== null
    ? state.liveCollections.banners
    : state.data.banners;
  return [...list].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
}

function getTechnicians() {
  return state.liveCollections.technicians !== null
    ? state.liveCollections.technicians
    : state.data.technicians;
}

function getUsers() {
  return state.liveCollections.users !== null
    ? state.liveCollections.users
    : state.data.users;
}

function getSubscriptions() {
  return state.liveCollections.subscriptions !== null
    ? state.liveCollections.subscriptions
    : state.data.subscriptions;
}

function getPayments() {
  const list = state.liveCollections.payments !== null
    ? state.liveCollections.payments
    : state.data.payments;
  return [...list].sort((a, b) => getMillis(b.createdAt || b.createdAtMillis) - getMillis(a.createdAt || a.createdAtMillis));
}

function getBookings() {
  return state.liveCollections.bookings !== null
    ? state.liveCollections.bookings
    : state.data.bookings;
}

function getComplaints() {
  return state.liveCollections.complaints !== null
    ? state.liveCollections.complaints
    : state.data.complaints;
}

/* ==================== UTILITIES ==================== */
function esc(val) {
  return String(val ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function getMillis(ts) {
  if (!ts) return 0;
  if (typeof ts === 'number') return ts;
  if (typeof ts.toMillis === 'function') return ts.toMillis();
  if (ts.seconds) return ts.seconds * 1000;
  return 0;
}

function fmtDate(ms) {
  const n = getMillis(ms);
  if (!n) return '—';
  return new Date(n).toLocaleDateString('en-PK', { day: '2-digit', month: 'short', year: 'numeric' });
}

function fmtRs(num) {
  return `Rs. ${Number(num || 0).toLocaleString('en-PK')}`;
}

function haversineKm(lat1, lng1, lat2, lng2) {
  const toRad = (deg) => (deg * Math.PI) / 180;
  const R = 6371;
  const dLat = toRad((lat2 || 24.86) - lat1);
  const dLng = toRad((lng2 || 67.0) - lng1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2 || 24.86)) * Math.sin(dLng / 2) ** 2;
  return (R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))).toFixed(1);
}

function statusBadgeClass(status) {
  const s = String(status || '').toUpperCase();
  if (['APPROVED', 'ACTIVE', 'COMPLETED', 'RESOLVED'].includes(s)) return 'badge-green';
  if (['PENDING', 'RENEWAL_PENDING', 'REQUESTED', 'INVESTIGATING', 'IN_PROGRESS', 'EN_ROUTE'].includes(s)) return 'badge-amber';
  if (['REJECTED', 'EXPIRED', 'SUSPENDED', 'CANCELLED', 'REFUNDED', 'OPEN'].includes(s)) return 'badge-red';
  return 'badge-gray';
}

function categoryMeta(categoryId) {
  return (
    getCategories().find((c) => c.id === categoryId || c.categoryId === categoryId) ||
    DEFAULT_CATEGORIES.find((c) => c.id === categoryId) || {
      id: categoryId,
      name: categoryId || 'Service',
      icon: '🔧',
      bg: '#dcfce7',
      subcategories: []
    }
  );
}

function showToast(message) {
  const el = document.getElementById('scToast');
  if (!el) return;
  el.textContent = message;
  el.classList.remove('hidden');
  clearTimeout(showToast._t);
  showToast._t = setTimeout(() => el.classList.add('hidden'), 2600);
}

function showNotice(htmlContent, isError = false) {
  const box = document.getElementById('portalNoticeBanner');
  if (!box) return;
  box.innerHTML = htmlContent;
  box.style.background = isError ? '#fef2f2' : '#fffbeb';
  box.style.borderColor = isError ? '#fecaca' : '#fde68a';
  box.style.color = isError ? '#991b1b' : '#78350f';
  box.classList.remove('hidden');
}

function hideNotice() {
  const box = document.getElementById('portalNoticeBanner');
  if (box) box.classList.add('hidden');
}

/* Compress uploaded banner / receipt photos via Canvas so Firestore <= 240,000 char rule always passes */
function compressImageFileToDataUrl(file, maxWidth = 880, maxChars = 155000) {
  return new Promise((resolve, reject) => {
    if (!file) return reject(new Error('No file selected'));
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('Failed to read image file'));
    reader.onload = () => {
      const img = new Image();
      img.onerror = () => reject(new Error('Invalid image file'));
      img.onload = () => {
        const scale = Math.min(1, maxWidth / (img.width || maxWidth));
        const w = Math.max(1, Math.round(img.width * scale));
        const h = Math.max(1, Math.round(img.height * scale));
        const canvas = document.createElement('canvas');
        canvas.width = w;
        canvas.height = h;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, w, h);

        let quality = 0.82;
        let out = canvas.toDataURL('image/jpeg', quality);
        while (out.length > maxChars && quality > 0.32) {
          quality -= 0.1;
          out = canvas.toDataURL('image/jpeg', quality);
        }
        if (out.length > 235000) {
          return reject(new Error('Image too large after compression. Please pick a smaller image.'));
        }
        resolve(out);
      };
      img.src = reader.result;
    };
    reader.readAsDataURL(file);
  });
}

/* ==================== FIREBASE AUTH & REAL-TIME SYNC ==================== */
function updateAuthUi() {
  const pillDot = document.getElementById('firebaseStatusDot');
  const pillText = document.getElementById('firebaseStatusText');
  const signInBtn = document.getElementById('adminGoogleSignInBtn');
  const signOutBtn = document.getElementById('adminSignOutBtn');
  const summaryEl = document.getElementById('adminAccountSummary');

  if (state.isLiveAdmin && state.authUser) {
    pillDot.classList.remove('offline');
    pillText.textContent = `Live Firestore Admin (${state.authUser.email})`;
    signInBtn.classList.add('hidden');
    signOutBtn.classList.remove('hidden');
    summaryEl.textContent = `${state.authUser.email} • Live`;
    hideNotice();
  } else if (state.authUser) {
    pillDot.classList.add('offline');
    pillText.textContent = `Signed in (${state.authUser.email}) — Read-only`;
    signInBtn.classList.add('hidden');
    signOutBtn.classList.remove('hidden');
    summaryEl.textContent = `Non-admin (${state.authUser.email})`;
  } else {
    pillDot.classList.add('offline');
    pillText.textContent = state.publicSynced
      ? 'Live Public Read + Local Preview Admin'
      : 'Preview Mode (Sign in to sync Firestore)';
    signInBtn.classList.remove('hidden');
    signOutBtn.classList.add('hidden');
    summaryEl.textContent = `${ADMIN_EMAIL} (Preview Mode)`;
  }
}

function stopLiveListeners() {
  if (state.liveUnsubPublic) {
    state.liveUnsubPublic();
    state.liveUnsubPublic = null;
  }
  if (state.liveUnsubAdmin) {
    state.liveUnsubAdmin();
    state.liveUnsubAdmin = null;
  }
}

function startPublicListeners() {
  stopLiveListeners();
  state.liveUnsubPublic = subscribePublicDirectory({
    onTechnicians: (docs) => {
      if (docs.length) state.liveCollections.technicians = docs;
      state.publicSynced = true;
      updateAuthUi();
      renderAll();
    },
    onCategories: (docs) => {
      if (docs.length) {
        state.liveCollections.categories = docs.map((d) => ({
          ...d,
          id: d.categoryId || d.id,
          icon: categoryMeta(d.categoryId || d.id).icon,
          bg: categoryMeta(d.categoryId || d.id).bg
        }));
      }
      renderAll();
    },
    onBanners: (docs) => {
      if (docs.length) state.liveCollections.banners = docs;
      renderAll();
    },
    onError: () => {
      // Keep graceful preview fallback if offline
    }
  });
}

function startAdminListeners() {
  stopLiveListeners();
  state.liveUnsubAdmin = subscribeAdminWorkspace({
    onUsers: (docs) => {
      state.liveCollections.users = docs;
      renderAll();
    },
    onTechnicians: (docs) => {
      state.liveCollections.technicians = docs;
      renderAll();
    },
    onCategories: (docs) => {
      if (docs.length) {
        state.liveCollections.categories = docs.map((d) => ({
          ...d,
          id: d.categoryId || d.id,
          icon: categoryMeta(d.categoryId || d.id).icon,
          bg: categoryMeta(d.categoryId || d.id).bg
        }));
      }
      renderAll();
    },
    onBookings: (docs) => {
      state.liveCollections.bookings = docs;
      renderAll();
    },
    onComplaints: (docs) => {
      state.liveCollections.complaints = docs;
      renderAll();
    },
    onBanners: (docs) => {
      state.liveCollections.banners = docs;
      renderAll();
    },
    onPayments: (docs) => {
      state.liveCollections.payments = docs;
      renderAll();
    },
    onSubscriptions: (docs) => {
      state.liveCollections.subscriptions = docs;
      renderAll();
    },
    onError: (col, err) => {
      showNotice(`Firestore sync warning on <strong>${esc(col)}</strong>: ${esc(err.message)}`, true);
    }
  });
}

function initFirebaseAuth() {
  subscribeAuthState((user) => {
    state.authUser = user || null;
    state.isLiveAdmin = isAdminUser(user);

    if (state.isLiveAdmin) {
      startAdminListeners();
      showToast('Connected as Live Firebase Admin');
    } else {
      state.liveCollections = {
        categories: null,
        banners: null,
        technicians: null,
        users: null,
        subscriptions: null,
        payments: null,
        bookings: null,
        complaints: null
      };
      startPublicListeners();
      if (user) {
        showNotice(
          `<span>Signed in as <strong>${esc(user.email)}</strong>. Admin write actions require verified Super Admin Google account <strong>${ADMIN_EMAIL}</strong>. Using local preview mode for admin edits.</span>`
        );
      } else {
        hideNotice();
      }
    }
    updateAuthUi();
    renderAll();
  });
}

async function handleGoogleSignIn() {
  try {
    await signInAdminWithGoogle();
  } catch (err) {
    const code = err?.code || '';
    if (code === 'auth/unauthorized-domain') {
      const host = window.location.hostname || 'orionfxservis.github.io';
      showNotice(
        `<div><strong>Google Sign-In domain authorization needed:</strong> Add <code>${esc(host)}</code> in <strong>Firebase Console &rarr; Authentication &rarr; Settings &rarr; Authorized domains</strong> to enable live Firestore Admin writes on this domain.</div>
         <button type="button" class="btn btn-outline btn-sm" id="copyAuthDomainBtn">Copy ${esc(host)}</button>`,
        false
      );
      document.getElementById('copyAuthDomainBtn')?.addEventListener('click', () => {
        navigator.clipboard?.writeText(host);
        showToast(`Copied ${host}`);
      });
    } else if (code !== 'auth/popup-closed-by-user') {
      showNotice(`Google Sign-In failed: ${esc(err.message || code)}`, true);
    }
  }
}

/* ==================== ADMIN MODALS ==================== */
function openAdminModal(html) {
  const backdrop = document.getElementById('adminModalBackdrop');
  const card = document.getElementById('adminModalCard');
  card.innerHTML = html;
  backdrop.classList.remove('hidden');
}

function closeAdminModal() {
  document.getElementById('adminModalBackdrop').classList.add('hidden');
}

/* ==================== ADMIN ACTIONS (LIVE FIRESTORE + LOCAL PREVIEW) ==================== */
async function performReviewAccount(tech, status, verifiedBadge, listed, remarks) {
  if (state.isLiveAdmin) {
    await reviewTechnicianAccount({
      techId: tech.userId || tech.id,
      verificationStatus: status,
      verifiedBadge,
      adminRemarks: remarks,
      listed
    });
  } else {
    const idx = state.data.technicians.findIndex((t) => (t.userId || t.id) === (tech.userId || tech.id));
    if (idx >= 0) {
      state.data.technicians[idx] = {
        ...state.data.technicians[idx],
        verificationStatus: status,
        verifiedBadge,
        listed,
        adminRemarks: remarks
      };
      persistLocal();
    }
  }
  showToast(`${tech.fullName}: ${status}`);
  renderAll();
}

async function performSaveBanner(bannerPayload) {
  if (state.isLiveAdmin) {
    await savePromoBanner(bannerPayload);
  } else {
    const id = bannerPayload.bannerId || `banner_${Date.now()}`;
    const idx = state.data.banners.findIndex((b) => b.bannerId === id);
    const record = { ...bannerPayload, bannerId: id };
    if (idx >= 0) state.data.banners[idx] = record;
    else state.data.banners.push(record);
    persistLocal();
  }
  showToast('Banner saved & synced to Mobile Preview');
  renderAll();
}

async function performDeleteBanner(bannerId) {
  if (state.isLiveAdmin) {
    await deletePromoBanner(bannerId);
  } else {
    state.data.banners = state.data.banners.filter((b) => b.bannerId !== bannerId);
    persistLocal();
  }
  showToast('Banner deleted');
  renderAll();
}

async function performApproveRenewal({
  userId,
  providerName,
  providerPhone,
  months = 1,
  amountPkr = 499,
  paymentMethod = 'JazzCash',
  transactionRef = '',
  adminRemarks = 'Old account renewal approved by Admin'
}) {
  const cleanId = userId;
  if (state.isLiveAdmin) {
    let payId = '';
    const linkedPending = getPayments().find(
      (p) => p.userId === cleanId && p.status === 'PENDING'
    );
    if (linkedPending) {
      await approvePaymentAndRenewal({
        payment: linkedPending,
        monthsToAdd: months,
        adminRemarks,
        alsoApproveTechnician: true
      });
      payId = linkedPending.paymentId || linkedPending.id;
    } else {
      await saveRenewalDecision({
        userId: cleanId,
        providerName,
        providerPhone,
        tier: 'PRO',
        renewalStatus: 'ACTIVE',
        monthsToAdd: months,
        lastPaymentId: payId,
        lastPaymentAmount: amountPkr,
        approvedByAdmin: true,
        adminRemarks,
        alsoReapproveTech: true
      });
    }
  } else {
    const now = Date.now();
    const subs = state.data.subscriptions;
    const idx = subs.findIndex((s) => s.userId === cleanId);
    const prevExpiry = idx >= 0 ? Number(subs[idx].renewalExpiresAtMillis || 0) : 0;
    const newExpiry = Math.max(now, prevExpiry) + Math.max(1, months) * 30 * DAY_MS;

    const linked = state.data.payments.find((p) => p.userId === cleanId && p.status === 'PENDING');
    if (linked) {
      linked.status = 'APPROVED';
      linked.adminRemarks = adminRemarks;
    }

    const updatedSub = {
      id: cleanId,
      userId: cleanId,
      providerName,
      providerPhone,
      tier: 'PRO',
      renewalStatus: 'ACTIVE',
      renewalExpiresAtMillis: newExpiry,
      lastPaymentId: linked?.paymentId || transactionRef || `ren_${Date.now()}`,
      lastPaymentAmount: Number(amountPkr) || 499,
      approvedByAdmin: true,
      adminRemarks
    };

    if (idx >= 0) subs[idx] = updatedSub;
    else subs.unshift(updatedSub);

    const techIdx = state.data.technicians.findIndex((t) => (t.userId || t.id) === cleanId);
    if (techIdx >= 0) {
      state.data.technicians[techIdx].verificationStatus = 'APPROVED';
      state.data.technicians[techIdx].verifiedBadge = true;
      state.data.technicians[techIdx].listed = true;
      state.data.technicians[techIdx].adminRemarks = adminRemarks;
    }
    persistLocal();
  }
  showToast(`Renewal approved for ${providerName} (+${months} mo)`);
  renderAll();
}

async function performRejectRenewal(sub, adminRemarks) {
  const cleanId = sub.userId || sub.id;
  if (state.isLiveAdmin) {
    await saveRenewalDecision({
      userId: cleanId,
      providerName: sub.providerName,
      providerPhone: sub.providerPhone,
      tier: sub.tier || 'FREE',
      renewalStatus: 'EXPIRED',
      monthsToAdd: 0,
      lastPaymentId: sub.lastPaymentId || '',
      lastPaymentAmount: sub.lastPaymentAmount || 0,
      approvedByAdmin: false,
      adminRemarks,
      alsoReapproveTech: false
    });
  } else {
    const idx = state.data.subscriptions.findIndex((s) => s.userId === cleanId);
    if (idx >= 0) {
      state.data.subscriptions[idx].renewalStatus = 'EXPIRED';
      state.data.subscriptions[idx].approvedByAdmin = false;
      state.data.subscriptions[idx].adminRemarks = adminRemarks;
      persistLocal();
    }
  }
  showToast(`Renewal marked expired for ${sub.providerName}`);
  renderAll();
}

async function performRecordPayment(payload, approveImmediately) {
  if (state.isLiveAdmin) {
    const paymentId = await createOrUpdatePayment({
      ...payload,
      status: approveImmediately ? 'APPROVED' : 'PENDING'
    });
    if (approveImmediately) {
      await approvePaymentAndRenewal({
        payment: { ...payload, paymentId, id: paymentId },
        monthsToAdd: Number(payload.billingPeriodMonths) || 1,
        adminRemarks: payload.adminRemarks || 'Payment verified & activated by Admin',
        alsoApproveTechnician: true
      });
    }
  } else {
    const paymentId = payload.paymentId || `pay_${Date.now()}`;
    const record = {
      ...payload,
      id: paymentId,
      paymentId,
      status: approveImmediately ? 'APPROVED' : 'PENDING',
      createdAtMillis: Date.now()
    };
    state.data.payments.unshift(record);
    if (approveImmediately) {
      await performApproveRenewal({
        userId: payload.userId,
        providerName: payload.providerName,
        providerPhone: payload.providerPhone,
        months: Number(payload.billingPeriodMonths) || 1,
        amountPkr: Number(payload.amountPkr) || 499,
        transactionRef: payload.transactionRef,
        adminRemarks: payload.adminRemarks || 'Payment verified & Pro activated'
      });
      return;
    }
    persistLocal();
  }
  showToast(approveImmediately ? 'Payment approved & account activated' : 'Payment logged as Pending');
  renderAll();
}

async function performPaymentDecision(payment, newStatus, remarks) {
  const months = Math.max(1, Number(payment.billingPeriodMonths) || 1);
  if (newStatus === 'APPROVED') {
    if (state.isLiveAdmin) {
      await approvePaymentAndRenewal({
        payment,
        monthsToAdd: months,
        adminRemarks: remarks || 'Payment verified & approved by Admin',
        alsoApproveTechnician: true
      });
    } else {
      const idx = state.data.payments.findIndex((p) => (p.paymentId || p.id) === (payment.paymentId || payment.id));
      if (idx >= 0) {
        state.data.payments[idx].status = 'APPROVED';
        state.data.payments[idx].adminRemarks = remarks;
      }
      await performApproveRenewal({
        userId: payment.userId,
        providerName: payment.providerName,
        providerPhone: payment.providerPhone,
        months,
        amountPkr: payment.amountPkr,
        transactionRef: payment.transactionRef,
        adminRemarks: remarks || 'Payment verified & renewal approved'
      });
      return;
    }
    showToast(`Approved Rs. ${payment.amountPkr} from ${payment.providerName}`);
  } else {
    if (state.isLiveAdmin) {
      await createOrUpdatePayment({
        ...payment,
        status: newStatus,
        adminRemarks: remarks
      });
    } else {
      const idx = state.data.payments.findIndex((p) => (p.paymentId || p.id) === (payment.paymentId || payment.id));
      if (idx >= 0) {
        state.data.payments[idx].status = newStatus;
        state.data.payments[idx].adminRemarks = remarks;
        persistLocal();
      }
    }
    showToast(`Payment marked ${newStatus}`);
  }
  renderAll();
}

/* ==================== ADMIN CONSOLE MODAL FORMS ==================== */
async function openInspectAccountModal(techId) {
  const tech = getTechnicians().find((t) => (t.userId || t.id) === techId);
  if (!tech) return;

  openAdminModal(`
    <div class="modal-header">
      <h3>Inspect Skill Person Account</h3>
      <button type="button" class="modal-close-btn" id="closeAdminModalBtn">&times;</button>
    </div>
    <p style="margin:0;color:#64748b;font-size:13px;">Loading verification records...</p>
  `);
  document.getElementById('closeAdminModalBtn')?.addEventListener('click', closeAdminModal);

  let verifDoc = null;
  if (state.isLiveAdmin) {
    try {
      verifDoc = await fetchVerificationDoc(tech.userId || tech.id);
    } catch {
      verifDoc = null;
    }
  }

  const cnic = verifDoc?.cnicNumber || tech.cnicNumber || 'Provided in app enrollment';
  const cat = categoryMeta(tech.categoryId);

  openAdminModal(`
    <div class="modal-header">
      <div>
        <h3 style="margin:0;">${esc(tech.fullName)}</h3>
        <span class="badge ${statusBadgeClass(tech.verificationStatus)}" style="margin-top:4px;">${esc(tech.verificationStatus)}</span>
      </div>
      <button type="button" class="modal-close-btn" id="closeAdminModalBtn">&times;</button>
    </div>

    <div class="form-grid-2" style="background:#f8fafc;padding:12px;border-radius:12px;border:1px solid #e2e8f0;font-size:12.5px;">
      <div><strong>Category:</strong> ${esc(cat.name)} (${esc(tech.subcategory || 'General')})</div>
      <div><strong>Phone:</strong> ${esc(tech.contactPhone)}</div>
      <div><strong>Working Area:</strong> ${esc(tech.workingArea)} (${esc(tech.serviceRadiusKm || 15)} km)</div>
      <div><strong>Visit Charge:</strong> ${esc(tech.visitCharge)}</div>
      <div><strong>CNIC Number:</strong> ${esc(cnic)}</div>
      <div><strong>Experience:</strong> ${esc(tech.yearsExperience || 0)} years</div>
    </div>

    <div style="font-size:12.5px;color:#334155;">
      <strong>Expertise &amp; Bio:</strong> ${esc(tech.expertise)} — ${esc(tech.bio || '')}
    </div>

    ${
      verifDoc?.cnicPhotoData || verifDoc?.cnicBackPhotoData || verifDoc?.certificatePhotoData
        ? `<div class="form-grid-2">
            ${verifDoc.cnicPhotoData ? `<div><div style="font-size:11px;font-weight:700;margin-bottom:4px;">CNIC Front</div><img src="${esc(verifDoc.cnicPhotoData)}" class="doc-preview-img" alt="CNIC Front"/></div>` : ''}
            ${verifDoc.cnicBackPhotoData ? `<div><div style="font-size:11px;font-weight:700;margin-bottom:4px;">CNIC Back</div><img src="${esc(verifDoc.cnicBackPhotoData)}" class="doc-preview-img" alt="CNIC Back"/></div>` : ''}
            ${verifDoc.certificatePhotoData ? `<div><div style="font-size:11px;font-weight:700;margin-bottom:4px;">Trade Certificate</div><img src="${esc(verifDoc.certificatePhotoData)}" class="doc-preview-img" alt="Certificate"/></div>` : ''}
          </div>`
        : `<div style="padding:10px;border-radius:10px;background:#f8fafc;border:1px dashed #cbd5e1;font-size:12px;color:#64748b;">
             ${state.isLiveAdmin ? 'No uploaded CNIC image attachment on this record.' : 'CNIC front/back & certificate images load directly from Firestore when signed in as Admin.'}
           </div>`
    }

    <label class="field-label">
      Admin Verification Remarks
      <input type="text" id="inspectRemarksInput" class="field-input" value="${esc(tech.adminRemarks || '')}" placeholder="e.g., CNIC & phone verified by Admin"/>
    </label>

    <div style="display:flex;gap:8px;flex-wrap:wrap;justify-content:flex-end;">
      <button type="button" class="btn btn-danger" id="modalRejectAccBtn">Reject Account</button>
      <button type="button" class="btn btn-outline" id="modalApproveBasicBtn">Approve (Listed)</button>
      <button type="button" class="btn btn-success" id="modalApproveVerifyBtn">Approve &amp; Verified Badge</button>
    </div>
  `);

  document.getElementById('closeAdminModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('modalApproveVerifyBtn')?.addEventListener('click', async () => {
    const remarks = document.getElementById('inspectRemarksInput').value.trim() || 'Verified & approved by Admin';
    await performReviewAccount(tech, 'APPROVED', true, true, remarks);
    closeAdminModal();
  });
  document.getElementById('modalApproveBasicBtn')?.addEventListener('click', async () => {
    const remarks = document.getElementById('inspectRemarksInput').value.trim() || 'Approved by Admin';
    await performReviewAccount(tech, 'APPROVED', false, true, remarks);
    closeAdminModal();
  });
  document.getElementById('modalRejectAccBtn')?.addEventListener('click', async () => {
    const remarks = document.getElementById('inspectRemarksInput').value.trim() || 'Incomplete CNIC or details';
    await performReviewAccount(tech, 'REJECTED', false, false, remarks);
    closeAdminModal();
  });
}

function openBannerEditorModal(existingBanner = null) {
  const b = existingBanner || {
    bannerId: '',
    placement: state.bannerPlacementTab === 'CATEGORY' ? 'CATEGORY' : 'HOME',
    categoryId: state.bannerPlacementTab === 'CATEGORY' ? state.bannerCategoryFilter : 'all',
    eyebrow: 'SkillConnect Special',
    headline: '',
    description: '',
    ctaLabel: 'Explore Now',
    ctaAction: 'explore_services',
    imageData: 'assets/images/mobile/img_hero_technician.jpg',
    active: true,
    sortOrder: getBanners().length + 1
  };

  let currentImg = b.imageData || 'assets/images/mobile/img_hero_technician.jpg';
  const catOptions = [
    '<option value="all">All Categories (Every Category Page)</option>',
    ...getCategories().map(
      (c) => `<option value="${esc(c.id)}" ${b.categoryId === c.id ? 'selected' : ''}>${esc(c.name)}</option>`
    )
  ].join('');

  openAdminModal(`
    <div class="modal-header">
      <h3>${existingBanner ? 'Edit Promotional Banner' : 'Add Banner (Main Page or Category Page)'}</h3>
      <button type="button" class="modal-close-btn" id="closeAdminModalBtn">&times;</button>
    </div>

    <div class="banner-mini-preview" id="modalBannerLivePreview">
      <img src="${esc(currentImg)}" alt="Banner Preview" class="banner-mini-img" id="modalBannerImgTag"/>
      <div class="banner-mini-text">
        <div class="banner-mini-eyebrow" id="modalPrevEyebrow">${esc(b.eyebrow)}</div>
        <div class="banner-mini-title" id="modalPrevHeadline">${esc(b.headline || 'Your Banner Headline')}</div>
        <div class="banner-mini-desc" id="modalPrevDesc">${esc(b.description || 'Short promotional description appears here.')}</div>
      </div>
      <span class="banner-mini-cta" id="modalPrevCta">${esc(b.ctaLabel || 'Explore Now')} &rarr;</span>
    </div>

    <form id="bannerEditorForm" style="display:flex;flex-direction:column;gap:10px;">
      <div class="form-grid-2">
        <label class="field-label">
          Placement
          <select id="bPlacement" class="field-input">
            <option value="HOME" ${b.placement === 'HOME' ? 'selected' : ''}>Main Page (Home Bottom Banner)</option>
            <option value="CATEGORY" ${b.placement === 'CATEGORY' ? 'selected' : ''}>Category Pages (Bottom Slider)</option>
          </select>
        </label>
        <label class="field-label" id="bCategoryWrap">
          Target Category
          <select id="bCategoryId" class="field-input">${catOptions}</select>
        </label>
      </div>

      <div class="form-grid-2">
        <label class="field-label">
          Eyebrow Label (Gold text)
          <input type="text" id="bEyebrow" class="field-input" value="${esc(b.eyebrow)}" maxlength="80" placeholder="e.g., Find Skilled Professionals" />
        </label>
        <label class="field-label">
          CTA Button Text
          <input type="text" id="bCtaLabel" class="field-input" value="${esc(b.ctaLabel)}" maxlength="40" required placeholder="e.g., Explore Services" />
        </label>
      </div>

      <label class="field-label">
        Headline *
        <input type="text" id="bHeadline" class="field-input" value="${esc(b.headline)}" maxlength="120" required placeholder="e.g., Need an Emergency Electrician?" />
      </label>

      <label class="field-label">
        Description
        <textarea id="bDescription" class="field-input" rows="2" maxlength="240" placeholder="Short description shown on banner...">${esc(b.description)}</textarea>
      </label>

      <div class="form-grid-2">
        <label class="field-label">
          CTA Navigation Target
          <select id="bCtaAction" class="field-input">
            <option value="explore_services" ${b.ctaAction === 'explore_services' ? 'selected' : ''}>Explore Services / Categories</option>
            <option value="map" ${b.ctaAction === 'map' ? 'selected' : ''}>Nearby Radar Map</option>
            <option value="subscription" ${b.ctaAction === 'subscription' ? 'selected' : ''}>My Subscription (Pro Rs. 499)</option>
            <option value="prohub" ${b.ctaAction === 'prohub' ? 'selected' : ''}>Pro Hub (Join SkillConnect)</option>
          </select>
        </label>
        <label class="field-label">
          Sort Order &amp; Visibility
          <div style="display:flex;gap:8px;align-items:center;">
            <input type="number" id="bSortOrder" class="field-input" min="1" max="999" value="${Number(b.sortOrder) || 1}" style="width:86px;" />
            <label style="display:flex;align-items:center;gap:6px;font-size:12.5px;">
              <input type="checkbox" id="bActive" ${b.active !== false ? 'checked' : ''} /> Active
            </label>
          </div>
        </label>
      </div>

      <div>
        <div style="font-size:12px;font-weight:600;color:#334155;margin-bottom:6px;">Choose Mobile Artwork Preset or Upload Custom Banner Photo</div>
        <div class="preset-thumb-row" id="bannerPresetsRow">
          ${BANNER_PRESET_IMAGES.map(
            (p) => `<button type="button" class="preset-thumb-btn ${currentImg === p.path ? 'active' : ''}" data-preset="${esc(p.path)}" title="${esc(p.label)}">
                      <img src="${esc(p.path)}" alt="${esc(p.label)}"/>
                    </button>`
          ).join('')}
        </div>
        <input type="file" id="bImageUpload" accept="image/*" style="margin-top:8px;font-size:12px;" />
      </div>

      <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:6px;">
        <button type="button" class="btn btn-outline" id="cancelBannerModalBtn">Cancel</button>
        <button type="submit" class="btn btn-primary">${existingBanner ? 'Save Banner Changes' : 'Publish Banner'}</button>
      </div>
    </form>
  `);

  const syncPreview = () => {
    const placement = document.getElementById('bPlacement').value;
    document.getElementById('bCategoryWrap').style.opacity = placement === 'HOME' ? '0.45' : '1';
    document.getElementById('bCategoryId').disabled = placement === 'HOME';
    document.getElementById('modalPrevEyebrow').textContent = document.getElementById('bEyebrow').value || '';
    document.getElementById('modalPrevHeadline').textContent = document.getElementById('bHeadline').value || 'Your Banner Headline';
    document.getElementById('modalPrevDesc').textContent = document.getElementById('bDescription').value || '';
    document.getElementById('modalPrevCta').textContent = `${document.getElementById('bCtaLabel').value || 'Explore'} →`;
    document.getElementById('modalBannerImgTag').src = currentImg;
  };

  syncPreview();
  ['bPlacement', 'bEyebrow', 'bHeadline', 'bDescription', 'bCtaLabel'].forEach((id) => {
    document.getElementById(id)?.addEventListener('input', syncPreview);
    document.getElementById(id)?.addEventListener('change', syncPreview);
  });

  document.querySelectorAll('#bannerPresetsRow .preset-thumb-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      currentImg = btn.dataset.preset;
      document.querySelectorAll('#bannerPresetsRow .preset-thumb-btn').forEach((x) => x.classList.remove('active'));
      btn.classList.add('active');
      syncPreview();
    });
  });

  document.getElementById('bImageUpload')?.addEventListener('change', async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      currentImg = await compressImageFileToDataUrl(file);
      document.querySelectorAll('#bannerPresetsRow .preset-thumb-btn').forEach((x) => x.classList.remove('active'));
      syncPreview();
      showToast('Custom image compressed & loaded');
    } catch (err) {
      showToast(err.message);
    }
  });

  document.getElementById('closeAdminModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('cancelBannerModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('bannerEditorForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const placement = document.getElementById('bPlacement').value;
    await performSaveBanner({
      bannerId: b.bannerId || `banner_${Date.now()}`,
      placement,
      categoryId: placement === 'HOME' ? 'all' : document.getElementById('bCategoryId').value,
      eyebrow: document.getElementById('bEyebrow').value.trim(),
      headline: document.getElementById('bHeadline').value.trim(),
      description: document.getElementById('bDescription').value.trim(),
      ctaLabel: document.getElementById('bCtaLabel').value.trim(),
      ctaAction: document.getElementById('bCtaAction').value,
      imageData: currentImg,
      active: document.getElementById('bActive').checked,
      sortOrder: Math.max(1, Number(document.getElementById('bSortOrder').value) || 1)
    });
    closeAdminModal();
  });
}

function openRecordPaymentModal(preselectTechId = '') {
  const techs = getTechnicians();
  const options = techs
    .map(
      (t) =>
        `<option value="${esc(t.userId || t.id)}" ${
          (t.userId || t.id) === preselectTechId ? 'selected' : ''
        }>${esc(t.fullName)} (${esc(categoryMeta(t.categoryId).name)})</option>`
    )
    .join('');

  let receiptImg = '';

  openAdminModal(`
    <div class="modal-header">
      <h3>Record Payment Received from Skill Person</h3>
      <button type="button" class="modal-close-btn" id="closeAdminModalBtn">&times;</button>
    </div>
    <form id="recordPaymentForm" style="display:flex;flex-direction:column;gap:10px;">
      <label class="field-label">
        Select Skill Person Account
        <select id="pTechSelect" class="field-input">
          ${options}
          <option value="__custom__">+ Enter Custom Skill Person</option>
        </select>
      </label>

      <div class="form-grid-2">
        <label class="field-label">
          Skill Person Name *
          <input type="text" id="pName" class="field-input" required />
        </label>
        <label class="field-label">
          Phone Number *
          <input type="text" id="pPhone" class="field-input" required />
        </label>
      </div>

      <div class="form-grid-2">
        <label class="field-label">
          Payment Purpose / Plan
          <select id="pPlanType" class="field-input">
            <option value="PRO_RENEWAL">Old Account Renewal (Pro)</option>
            <option value="PRO_NEW">New Pro Subscription</option>
            <option value="FEATURED">Featured Category Listing</option>
            <option value="OTHER">Other Service Fee</option>
          </select>
        </label>
        <label class="field-label">
          Payment Method
          <select id="pMethod" class="field-input">
            <option value="JazzCash">JazzCash</option>
            <option value="EasyPaisa">EasyPaisa</option>
            <option value="Bank Transfer">Bank Transfer</option>
            <option value="Cash">Cash</option>
            <option value="Google Play">Google Play</option>
          </select>
        </label>
      </div>

      <div class="form-grid-2">
        <label class="field-label">
          Amount Received (PKR Rs.) *
          <input type="number" id="pAmount" class="field-input" value="499" min="0" required />
        </label>
        <label class="field-label">
          Validity Extension (Months)
          <select id="pMonths" class="field-input">
            <option value="1" selected>1 Month</option>
            <option value="3">3 Months</option>
            <option value="6">6 Months</option>
            <option value="12">12 Months</option>
          </select>
        </label>
      </div>

      <div class="form-grid-2">
        <label class="field-label">
          Transaction / Receipt Reference *
          <input type="text" id="pTxRef" class="field-input" placeholder="e.g., JC-8829104" required />
        </label>
        <label class="field-label">
          Upload Receipt Screenshot (Optional)
          <input type="file" id="pReceiptUpload" accept="image/*" style="font-size:12px;" />
        </label>
      </div>

      <label class="field-label">
        Notes / Admin Remarks
        <input type="text" id="pNote" class="field-input" placeholder="Received via JazzCash / confirmed on call" />
      </label>

      <div style="display:flex;justify-content:flex-end;gap:8px;flex-wrap:wrap;margin-top:6px;">
        <button type="button" class="btn btn-outline" id="cancelPayModalBtn">Cancel</button>
        <button type="submit" data-mode="PENDING" class="btn btn-amber">Save as Pending</button>
        <button type="submit" data-mode="APPROVE" class="btn btn-success">Approve &amp; Activate / Renew Now</button>
      </div>
    </form>
  `);

  const fillFromTech = () => {
    const val = document.getElementById('pTechSelect').value;
    const t = techs.find((x) => (x.userId || x.id) === val);
    if (t) {
      document.getElementById('pName').value = t.fullName || '';
      document.getElementById('pPhone').value = t.contactPhone || '';
    }
  };
  fillFromTech();
  document.getElementById('pTechSelect')?.addEventListener('change', fillFromTech);

  document.getElementById('pReceiptUpload')?.addEventListener('change', async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      receiptImg = await compressImageFileToDataUrl(file, 720, 140000);
      showToast('Receipt image attached');
    } catch (err) {
      showToast(err.message);
    }
  });

  let submitMode = 'APPROVE';
  document.querySelectorAll('#recordPaymentForm button[type="submit"]').forEach((btn) => {
    btn.addEventListener('click', () => {
      submitMode = btn.dataset.mode || 'APPROVE';
    });
  });

  document.getElementById('closeAdminModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('cancelPayModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('recordPaymentForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const selVal = document.getElementById('pTechSelect').value;
    const chosenTech = techs.find((x) => (x.userId || x.id) === selVal);
    const planType = document.getElementById('pPlanType').value;

    await performRecordPayment(
      {
        userId: chosenTech ? chosenTech.userId || chosenTech.id : `manual_${Date.now()}`,
        providerName: document.getElementById('pName').value.trim(),
        providerPhone: document.getElementById('pPhone').value.trim(),
        categoryName: chosenTech ? categoryMeta(chosenTech.categoryId).name : 'General',
        planType,
        amountPkr: Number(document.getElementById('pAmount').value) || 499,
        paymentMethod: document.getElementById('pMethod').value,
        transactionRef: document.getElementById('pTxRef').value.trim(),
        billingPeriodMonths: Number(document.getElementById('pMonths').value) || 1,
        receiptNote: document.getElementById('pNote').value.trim(),
        receiptImageData: receiptImg,
        isRenewal: planType === 'PRO_RENEWAL',
        adminRemarks: document.getElementById('pNote').value.trim()
      },
      submitMode === 'APPROVE'
    );
    closeAdminModal();
  });
}

function openRenewalApprovalModal(sub) {
  const cleanId = sub?.userId || sub?.id || '';
  const techs = getTechnicians();
  const techOptions = techs
    .map(
      (t) =>
        `<option value="${esc(t.userId || t.id)}" ${
          (t.userId || t.id) === cleanId ? 'selected' : ''
        }>${esc(t.fullName)} — ${esc(t.contactPhone)}</option>`
    )
    .join('');

  openAdminModal(`
    <div class="modal-header">
      <h3>${sub ? `Approve Old Account Renewal — ${esc(sub.providerName)}` : 'Log & Approve Old Account Renewal'}</h3>
      <button type="button" class="modal-close-btn" id="closeAdminModalBtn">&times;</button>
    </div>
    <form id="renewalApproveForm" style="display:flex;flex-direction:column;gap:10px;">
      ${
        sub
          ? `<div style="background:#f8fafc;padding:10px 12px;border-radius:10px;border:1px solid #e2e8f0;font-size:12.5px;">
               <strong>${esc(sub.providerName)}</strong> (${esc(sub.providerPhone)})<br/>
               Current Status: <strong>${esc(sub.renewalStatus)}</strong> &bull; Expiry: <strong>${fmtDate(sub.renewalExpiresAtMillis)}</strong>
             </div>`
          : `<label class="field-label">
               Select Old Skill Person Account
               <select id="renTechSelect" class="field-input">${techOptions}</select>
             </label>`
      }

      <div class="form-grid-2">
        <label class="field-label">
          Extend Validity By
          <select id="renMonths" class="field-input">
            <option value="1" selected>1 Month (30 Days)</option>
            <option value="3">3 Months (90 Days)</option>
            <option value="6">6 Months (180 Days)</option>
            <option value="12">12 Months (1 Year)</option>
          </select>
        </label>
        <label class="field-label">
          Renewal Amount (Rs.)
          <input type="number" id="renAmount" class="field-input" value="${Number(sub?.lastPaymentAmount) || 499}" min="0" />
        </label>
      </div>

      <label class="field-label">
        Admin Approval Remarks
        <input type="text" id="renRemarks" class="field-input" value="Old account renewed & re-approved by Admin" />
      </label>

      <div style="display:flex;justify-content:flex-end;gap:8px;">
        <button type="button" class="btn btn-outline" id="cancelRenModalBtn">Cancel</button>
        <button type="submit" class="btn btn-success">Approve Renewal &amp; Keep Listed</button>
      </div>
    </form>
  `);

  document.getElementById('closeAdminModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('cancelRenModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('renewalApproveForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const targetId = sub ? cleanId : document.getElementById('renTechSelect').value;
    const tech = techs.find((t) => (t.userId || t.id) === targetId);
    await performApproveRenewal({
      userId: targetId,
      providerName: sub?.providerName || tech?.fullName || 'Skill Person',
      providerPhone: sub?.providerPhone || tech?.contactPhone || '',
      months: Number(document.getElementById('renMonths').value) || 1,
      amountPkr: Number(document.getElementById('renAmount').value) || 499,
      adminRemarks: document.getElementById('renRemarks').value.trim()
    });
    closeAdminModal();
  });
}

function openCategoryEditorModal(cat = null) {
  const c = cat || {
    id: '',
    categoryId: '',
    name: '',
    subtitle: '',
    iconKey: '',
    subcategories: [],
    active: true,
    sortOrder: getCategories().length + 1
  };
  openAdminModal(`
    <div class="modal-header">
      <h3>${cat ? `Edit Category: ${esc(c.name)}` : 'Add Service Category'}</h3>
      <button type="button" class="modal-close-btn" id="closeAdminModalBtn">&times;</button>
    </div>
    <form id="catForm" style="display:flex;flex-direction:column;gap:10px;">
      <div class="form-grid-2">
        <label class="field-label">
          Category ID (slug)
          <input type="text" id="catIdInput" class="field-input" value="${esc(c.categoryId || c.id)}" ${cat ? 'readonly' : 'required'} placeholder="e.g., home_security" />
        </label>
        <label class="field-label">
          Display Name *
          <input type="text" id="catNameInput" class="field-input" value="${esc(c.name)}" required />
        </label>
      </div>
      <label class="field-label">
        Subtitle
        <input type="text" id="catSubInput" class="field-input" value="${esc(c.subtitle || '')}" />
      </label>
      <label class="field-label">
        Subcategories (comma separated)
        <input type="text" id="catSubsInput" class="field-input" value="${esc((c.subcategories || []).join(', '))}" />
      </label>
      <div class="form-grid-2">
        <label class="field-label">
          Sort Order
          <input type="number" id="catSortInput" class="field-input" value="${Number(c.sortOrder) || 1}" min="1" />
        </label>
        <label class="field-label" style="justify-content:center;">
          <span>Visibility</span>
          <span style="display:flex;align-items:center;gap:6px;"><input type="checkbox" id="catActiveInput" ${c.active !== false ? 'checked' : ''} /> Active on Mobile App</span>
        </label>
      </div>
      <div style="display:flex;justify-content:flex-end;gap:8px;">
        <button type="button" class="btn btn-outline" id="cancelCatModalBtn">Cancel</button>
        <button type="submit" class="btn btn-primary">Save Category</button>
      </div>
    </form>
  `);
  document.getElementById('closeAdminModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('cancelCatModalBtn')?.addEventListener('click', closeAdminModal);
  document.getElementById('catForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('catIdInput').value.trim().toLowerCase().replace(/[^a-z0-9_-]/g, '_');
    const subs = document
      .getElementById('catSubsInput')
      .value.split(',')
      .map((s) => s.trim())
      .filter(Boolean);
    const payload = {
      id,
      categoryId: id,
      name: document.getElementById('catNameInput').value.trim(),
      subtitle: document.getElementById('catSubInput').value.trim(),
      iconKey: id,
      subcategories: subs,
      active: document.getElementById('catActiveInput').checked,
      sortOrder: Number(document.getElementById('catSortInput').value) || 1
    };
    if (state.isLiveAdmin) {
      await saveCategoryEntity(payload);
    } else {
      const idx = state.data.categories.findIndex((x) => x.id === id);
      const merged = { ...payload, icon: categoryMeta(id).icon, bg: categoryMeta(id).bg };
      if (idx >= 0) state.data.categories[idx] = merged;
      else state.data.categories.push(merged);
      persistLocal();
    }
    showToast(`Category ${payload.name} saved`);
    closeAdminModal();
    renderAll();
  });
}

/* ==================== RENDER LAPTOP ADMIN CONSOLE ==================== */
function renderSidebarBadges() {
  const pendingAcc = getTechnicians().filter((t) => t.verificationStatus === 'PENDING').length;
  const pendingRen = getSubscriptions().filter(
    (s) => s.renewalStatus === 'RENEWAL_PENDING' || s.renewalStatus === 'EXPIRED'
  ).length;
  const pendingPay = getPayments().filter((p) => p.status === 'PENDING').length;
  const openComp = getComplaints().filter((c) => c.status !== 'RESOLVED').length;

  const setBadge = (id, count) => {
    const el = document.getElementById(id);
    if (!el) return;
    el.textContent = String(count);
    el.classList.toggle('hidden', count === 0);
  };
  setBadge('badgePendingAccounts', pendingAcc);
  setBadge('badgePendingRenewals', pendingRen);
  setBadge('badgePendingPayments', pendingPay);
  setBadge('badgeOpenComplaints', openComp);

  document.querySelectorAll('.admin-nav-btn[data-admin-section]').forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.adminSection === state.adminSection);
  });
}

function renderAdminPane() {
  renderSidebarBadges();
  const root = document.getElementById('adminMainPane');
  if (!root) return;

  if (state.adminSection === 'overview') return renderAdminOverview(root);
  if (state.adminSection === 'accounts') return renderAdminAccounts(root);
  if (state.adminSection === 'renewals') return renderAdminRenewals(root);
  if (state.adminSection === 'banners') return renderAdminBanners(root);
  if (state.adminSection === 'payments') return renderAdminPayments(root);
  if (state.adminSection === 'categories') return renderAdminCategories(root);
  return renderAdminOperations(root);
}

function renderAdminOverview(root) {
  const techs = getTechnicians();
  const pendingAccounts = techs.filter((t) => t.verificationStatus === 'PENDING');
  const subs = getSubscriptions();
  const renewalQueue = subs.filter((s) => s.renewalStatus === 'RENEWAL_PENDING' || s.renewalStatus === 'EXPIRED');
  const payments = getPayments();
  const pendingPayments = payments.filter((p) => p.status === 'PENDING');
  const approvedRevenue = payments
    .filter((p) => p.status === 'APPROVED')
    .reduce((acc, p) => acc + (Number(p.amountPkr) || 0), 0);
  const banners = getBanners();
  const homeCount = banners.filter((b) => b.placement === 'HOME' && b.active).length;
  const catCount = banners.filter((b) => b.placement === 'CATEGORY' && b.active).length;

  root.innerHTML = `
    <div class="pane-header">
      <div>
        <h2>Admin Operations Dashboard</h2>
        <p>Approve new accounts, manage Main &amp; Category banners, approve old account renewals, and verify Skill Person payments</p>
      </div>
      <div style="display:flex;gap:8px;flex-wrap:wrap;">
        <button type="button" class="btn btn-primary" data-jump-section="banners" data-open-banner-add="1">+ Add Banner</button>
        <button type="button" class="btn btn-outline" data-jump-section="payments" data-open-pay-add="1">+ Record Payment</button>
      </div>
    </div>

    <div class="kpi-grid">
      <div class="kpi-card">
        <span class="kpi-label">New Accounts Pending</span>
        <span class="kpi-value">${pendingAccounts.length}</span>
        <span class="kpi-sub">${techs.filter((t) => t.verificationStatus === 'APPROVED').length} Approved Skill Persons</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">Old Renewals Due / Pending</span>
        <span class="kpi-value">${renewalQueue.length}</span>
        <span class="kpi-sub">${subs.filter((s) => s.renewalStatus === 'ACTIVE').length} Active Pro Renewals</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">Skill Person Payments</span>
        <span class="kpi-value">${fmtRs(approvedRevenue)}</span>
        <span class="kpi-sub">${pendingPayments.length} Pending Verification</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">Active App Banners</span>
        <span class="kpi-value">${homeCount + catCount}</span>
        <span class="kpi-sub">${homeCount} Home &bull; ${catCount} Category Slides</span>
      </div>
    </div>

    <div class="admin-card-grid">
      <div class="admin-card">
        <div class="info-card-top">
          <strong>Pending New Accounts (${pendingAccounts.length})</strong>
          <button type="button" class="btn btn-outline btn-sm" data-jump-section="accounts">Open All</button>
        </div>
        ${
          pendingAccounts.length
            ? pendingAccounts
                .slice(0, 4)
                .map(
                  (t) => `
                  <div style="display:flex;align-items:center;justify-content:space-between;gap:8px;padding:8px 0;border-bottom:1px solid #f1f5f9;">
                    <div>
                      <div style="font-weight:700;font-size:13px;">${esc(t.fullName)}</div>
                      <div style="font-size:11.5px;color:#64748b;">${esc(categoryMeta(t.categoryId).name)} &bull; ${esc(t.workingArea)}</div>
                    </div>
                    <div class="table-actions">
                      <button type="button" class="btn btn-outline btn-sm" data-inspect-tech="${esc(t.userId || t.id)}">Inspect</button>
                      <button type="button" class="btn btn-success btn-sm" data-quick-approve-tech="${esc(t.userId || t.id)}">Approve</button>
                    </div>
                  </div>`
                )
                .join('')
            : '<p style="font-size:12.5px;color:#64748b;margin:4px 0;">All new Skill Person applications are reviewed.</p>'
        }
      </div>

      <div class="admin-card">
        <div class="info-card-top">
          <strong>After Renew Old — Approve Queue (${renewalQueue.length})</strong>
          <button type="button" class="btn btn-outline btn-sm" data-jump-section="renewals">Manage Renewals</button>
        </div>
        ${
          renewalQueue.length
            ? renewalQueue
                .slice(0, 4)
                .map(
                  (s) => `
                  <div style="display:flex;align-items:center;justify-content:space-between;gap:8px;padding:8px 0;border-bottom:1px solid #f1f5f9;">
                    <div>
                      <div style="font-weight:700;font-size:13px;">${esc(s.providerName)}</div>
                      <div style="font-size:11.5px;color:#64748b;">
                        <span class="badge ${statusBadgeClass(s.renewalStatus)}">${esc(s.renewalStatus)}</span>
                        &bull; ${fmtRs(s.lastPaymentAmount || 499)}
                      </div>
                    </div>
                    <button type="button" class="btn btn-success btn-sm" data-quick-approve-renewal="${esc(s.userId || s.id)}">Approve Renewal</button>
                  </div>`
                )
                .join('')
            : '<p style="font-size:12.5px;color:#64748b;margin:4px 0;">No pending or expired renewals.</p>'
        }
      </div>

      <div class="admin-card">
        <div class="info-card-top">
          <strong>Pending Skill Person Payments (${pendingPayments.length})</strong>
          <button type="button" class="btn btn-outline btn-sm" data-jump-section="payments">All Payments</button>
        </div>
        ${
          pendingPayments.length
            ? pendingPayments
                .slice(0, 4)
                .map(
                  (p) => `
                  <div style="display:flex;align-items:center;justify-content:space-between;gap:8px;padding:8px 0;border-bottom:1px solid #f1f5f9;">
                    <div>
                      <div style="font-weight:700;font-size:13px;">${esc(p.providerName)} &bull; ${fmtRs(p.amountPkr)}</div>
                      <div style="font-size:11.5px;color:#64748b;">${esc(p.paymentMethod)} (${esc(p.transactionRef)})</div>
                    </div>
                    <button type="button" class="btn btn-success btn-sm" data-approve-payment="${esc(p.paymentId || p.id)}">Approve</button>
                  </div>`
                )
                .join('')
            : '<p style="font-size:12.5px;color:#64748b;margin:4px 0;">All payments verified.</p>'
        }
      </div>
    </div>
  `;

  bindAdminCommonActions(root);
}

function renderAdminAccounts(root) {
  const techs = getTechnicians();
  const users = getUsers();
  const q = state.accountSearch.trim().toLowerCase();

  const showingUsers = state.accountFilter === 'USERS' || state.accountFilter === 'SUSPENDED';

  const filteredTechs = techs.filter((t) => {
    if (['PENDING', 'APPROVED', 'REJECTED'].includes(state.accountFilter) && t.verificationStatus !== state.accountFilter) {
      return false;
    }
    if (!q) return true;
    return [t.fullName, t.categoryId, t.contactPhone, t.workingArea, t.cnicNumber]
      .join(' ')
      .toLowerCase()
      .includes(q);
  });

  const filteredUsers = users.filter((u) => {
    if (state.accountFilter === 'SUSPENDED' && !u.suspended) return false;
    if (!q) return true;
    return [u.displayName, u.email, u.phoneNumber, u.defaultArea, u.role]
      .join(' ')
      .toLowerCase()
      .includes(q);
  });

  const filterTabs = [
    ['PENDING', `Pending New (${techs.filter((t) => t.verificationStatus === 'PENDING').length})`],
    ['APPROVED', `Approved (${techs.filter((t) => t.verificationStatus === 'APPROVED').length})`],
    ['REJECTED', 'Rejected'],
    ['ALL_TECH', 'All Skill Persons'],
    ['USERS', 'App Users'],
    ['SUSPENDED', 'Suspended']
  ];

  root.innerHTML = `
    <div class="pane-header">
      <div>
        <h2>Approve New Accounts &amp; Directory</h2>
        <p>Inspect CNIC &amp; trade details for new Skill Persons, verify badges, or manage app user roles</p>
      </div>
      ${
        !state.isLiveAdmin
          ? `<button type="button" class="btn btn-outline" id="addDemoTechBtn">+ Add Test New Account</button>`
          : ''
      }
    </div>

    <div class="filter-toolbar">
      <div class="chip-tabs">
        ${filterTabs
          .map(
            ([k, label]) =>
              `<button type="button" class="chip-tab ${state.accountFilter === k ? 'active' : ''}" data-acc-filter="${k}">${esc(label)}</button>`
          )
          .join('')}
      </div>
      <input type="search" id="accSearchInput" class="search-box" value="${esc(state.accountSearch)}" placeholder="Search name, phone, area..." />
    </div>

    <div class="data-table-wrap">
      ${
        showingUsers
          ? `<table class="data-table">
               <thead>
                 <tr>
                   <th>User</th>
                   <th>Contact &amp; Area</th>
                   <th>Role</th>
                   <th>Status</th>
                   <th>Actions</th>
                 </tr>
               </thead>
               <tbody>
                 ${
                   filteredUsers.length
                     ? filteredUsers
                         .map(
                           (u) => `
                           <tr>
                             <td><strong>${esc(u.displayName)}</strong><br/><span style="font-size:11.5px;color:#64748b;">${esc(u.email)}</span></td>
                             <td>${esc(u.phoneNumber || '—')}<br/><span style="font-size:11.5px;color:#64748b;">${esc(u.defaultArea || '')}</span></td>
                             <td>
                               <select class="field-input" style="padding:5px 8px;font-size:12px;width:auto;" data-user-role="${esc(u.userId || u.id)}">
                                 <option value="CUSTOMER" ${u.role === 'CUSTOMER' ? 'selected' : ''}>CUSTOMER</option>
                                 <option value="PROVIDER" ${u.role === 'PROVIDER' ? 'selected' : ''}>PROVIDER</option>
                                 <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                               </select>
                             </td>
                             <td><span class="badge ${u.suspended ? 'badge-red' : 'badge-green'}">${u.suspended ? 'SUSPENDED' : 'ACTIVE'}</span></td>
                             <td>
                               <button type="button" class="btn btn-sm ${u.suspended ? 'btn-success' : 'btn-danger'}" data-toggle-suspend="${esc(u.userId || u.id)}">
                                 ${u.suspended ? 'Unsuspend' : 'Suspend'}
                               </button>
                             </td>
                           </tr>`
                         )
                         .join('')
                     : '<tr><td colspan="5" style="text-align:center;color:#64748b;">No matching users.</td></tr>'
                 }
               </tbody>
             </table>`
          : `<table class="data-table">
               <thead>
                 <tr>
                   <th>Skill Person</th>
                   <th>Category &amp; Area</th>
                   <th>Charge</th>
                   <th>Verification</th>
                   <th>Actions</th>
                 </tr>
               </thead>
               <tbody>
                 ${
                   filteredTechs.length
                     ? filteredTechs
                         .map((t) => {
                           const cat = categoryMeta(t.categoryId);
                           return `
                             <tr>
                               <td>
                                 <strong>${esc(t.fullName)}</strong> ${t.verifiedBadge ? '<span class="badge badge-green">Verified</span>' : ''}
                                 <div style="font-size:11.5px;color:#64748b;">${esc(t.contactPhone)}</div>
                               </td>
                               <td>
                                 <strong>${esc(cat.name)}</strong> &bull; ${esc(t.subcategory || 'General')}
                                 <div style="font-size:11.5px;color:#64748b;">${esc(t.workingArea)}</div>
                               </td>
                               <td>${esc(t.visitCharge)}</td>
                               <td>
                                 <span class="badge ${statusBadgeClass(t.verificationStatus)}">${esc(t.verificationStatus)}</span>
                                 <div style="font-size:11px;color:#64748b;margin-top:2px;">${t.listed ? 'Listed in App' : 'Hidden'}</div>
                               </td>
                               <td>
                                 <div class="table-actions">
                                   <button type="button" class="btn btn-outline btn-sm" data-inspect-tech="${esc(t.userId || t.id)}">Inspect</button>
                                   ${
                                     t.verificationStatus !== 'APPROVED'
                                       ? `<button type="button" class="btn btn-success btn-sm" data-quick-approve-tech="${esc(t.userId || t.id)}">Approve</button>`
                                       : ''
                                   }
                                   ${
                                     t.verificationStatus !== 'REJECTED'
                                       ? `<button type="button" class="btn btn-danger btn-sm" data-quick-reject-tech="${esc(t.userId || t.id)}">Reject</button>`
                                       : ''
                                   }
                                 </div>
                               </td>
                             </tr>`;
                         })
                         .join('')
                     : '<tr><td colspan="5" style="text-align:center;color:#64748b;">No Skill Person records in this filter.</td></tr>'
                 }
               </tbody>
             </table>`
      }
    </div>
  `;

  root.querySelectorAll('[data-acc-filter]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.accountFilter = btn.dataset.accFilter;
      renderAdminPane();
    })
  );
  document.getElementById('accSearchInput')?.addEventListener('input', (e) => {
    state.accountSearch = e.target.value;
    renderAdminPane();
  });
  document.getElementById('addDemoTechBtn')?.addEventListener('click', () => {
    const num = state.data.technicians.length + 1;
    const id = `tech_demo_${Date.now()}`;
    state.data.technicians.unshift({
      id,
      userId: id,
      fullName: `Nadeem Technician #${num}`,
      categoryId: 'plumber',
      subcategory: 'Water Motor',
      expertise: 'Water Motor Winding & Pipe Fitting',
      bio: 'New demo registration awaiting admin review.',
      yearsExperience: 5,
      contactPhone: '+92 302 9911022',
      phoneVerified: true,
      visitCharge: 'Rs. 450',
      availabilityHours: '9:00 AM - 9:00 PM',
      workingArea: 'Model Town, Lahore',
      serviceRadiusKm: 15,
      availableNow: true,
      listed: false,
      verificationStatus: 'PENDING',
      verifiedBadge: false,
      adminRemarks: '',
      cnicNumber: '35201-4401928-5',
      profilePhotoData: '',
      latitude: 31.483,
      longitude: 74.326,
      averageRating: 0,
      reviewCount: 0,
      completedJobs: 0
    });
    persistLocal();
    showToast('Added new pending demo account');
    renderAll();
  });

  root.querySelectorAll('[data-toggle-suspend]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const uid = btn.dataset.toggleSuspend;
      const u = users.find((x) => (x.userId || x.id) === uid);
      if (!u) return;
      if (state.isLiveAdmin) {
        await updateUserAccount({ userId: uid, suspended: !u.suspended, role: u.role || 'CUSTOMER' });
      } else {
        u.suspended = !u.suspended;
        persistLocal();
      }
      showToast(`${u.displayName}: ${u.suspended ? 'Active' : 'Suspended'}`);
      renderAll();
    })
  );

  root.querySelectorAll('[data-user-role]').forEach((sel) =>
    sel.addEventListener('change', async () => {
      const uid = sel.dataset.userRole;
      const u = users.find((x) => (x.userId || x.id) === uid);
      if (!u) return;
      if (state.isLiveAdmin) {
        await updateUserAccount({ userId: uid, suspended: Boolean(u.suspended), role: sel.value });
      } else {
        u.role = sel.value;
        persistLocal();
      }
      showToast(`Role updated to ${sel.value}`);
      renderAll();
    })
  );

  bindAdminCommonActions(root);
}

function renderAdminRenewals(root) {
  const subs = getSubscriptions();
  const q = state.renewalSearch.trim().toLowerCase();

  const filtered = subs.filter((s) => {
    if (state.renewalFilter === 'NEEDS_ACTION' && !['RENEWAL_PENDING', 'EXPIRED'].includes(s.renewalStatus)) {
      return false;
    }
    if (['RENEWAL_PENDING', 'EXPIRED', 'ACTIVE'].includes(state.renewalFilter) && s.renewalStatus !== state.renewalFilter) {
      return false;
    }
    if (!q) return true;
    return [s.providerName, s.providerPhone, s.tier, s.renewalStatus, s.lastPaymentId]
      .join(' ')
      .toLowerCase()
      .includes(q);
  });

  const tabs = [
    ['NEEDS_ACTION', `Needs Renewal Approval (${subs.filter((s) => ['RENEWAL_PENDING', 'EXPIRED'].includes(s.renewalStatus)).length})`],
    ['RENEWAL_PENDING', 'Renewal Pending'],
    ['EXPIRED', 'Expired Old Accounts'],
    ['ACTIVE', 'Active Renewed'],
    ['ALL', 'All Subscriptions']
  ];

  root.innerHTML = `
    <div class="pane-header">
      <div>
        <h2>Renew Old Accounts &amp; Approve</h2>
        <p>Approve renewal requests from existing Skill Persons, extend Pro validity (+1 to +12 months), and keep them active on the mobile app</p>
      </div>
      <button type="button" class="btn btn-primary" id="openNewRenewalBtn">+ Log Old Account Renewal</button>
    </div>

    <div class="filter-toolbar">
      <div class="chip-tabs">
        ${tabs
          .map(
            ([k, label]) =>
              `<button type="button" class="chip-tab ${state.renewalFilter === k ? 'active' : ''}" data-ren-filter="${k}">${esc(label)}</button>`
          )
          .join('')}
      </div>
      <input type="search" id="renSearchInput" class="search-box" value="${esc(state.renewalSearch)}" placeholder="Search provider, phone, ref..." />
    </div>

    <div class="data-table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>Old Skill Person</th>
            <th>Tier &amp; Status</th>
            <th>Valid Until</th>
            <th>Last Payment</th>
            <th>Admin Remarks</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${
            filtered.length
              ? filtered
                  .map(
                    (s) => `
                    <tr>
                      <td>
                        <strong>${esc(s.providerName)}</strong>
                        <div style="font-size:11.5px;color:#64748b;">${esc(s.providerPhone)}</div>
                      </td>
                      <td>
                        <span class="badge ${s.tier === 'PRO' ? 'badge-blue' : 'badge-gray'}">${esc(s.tier)}</span>
                        <span class="badge ${statusBadgeClass(s.renewalStatus)}">${esc(s.renewalStatus)}</span>
                      </td>
                      <td>${fmtDate(s.renewalExpiresAtMillis)}</td>
                      <td>
                        <strong>${fmtRs(s.lastPaymentAmount || 0)}</strong>
                        <div style="font-size:11px;color:#64748b;">${esc(s.lastPaymentId || 'No ref')}</div>
                      </td>
                      <td style="max-width:210px;font-size:12px;color:#475569;">${esc(s.adminRemarks || '—')}</td>
                      <td>
                        <div class="table-actions">
                          <button type="button" class="btn btn-success btn-sm" data-quick-approve-renewal="${esc(s.userId || s.id)}">Approve (+1 Mo)</button>
                          <button type="button" class="btn btn-outline btn-sm" data-custom-renewal="${esc(s.userId || s.id)}">Extend / Edit</button>
                          ${
                            s.renewalStatus !== 'EXPIRED'
                              ? `<button type="button" class="btn btn-danger btn-sm" data-expire-renewal="${esc(s.userId || s.id)}">Mark Expired</button>`
                              : ''
                          }
                        </div>
                      </td>
                    </tr>`
                  )
                  .join('')
              : '<tr><td colspan="6" style="text-align:center;color:#64748b;">No renewal accounts in this filter.</td></tr>'
          }
        </tbody>
      </table>
    </div>
  `;

  root.querySelectorAll('[data-ren-filter]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.renewalFilter = btn.dataset.renFilter;
      renderAdminPane();
    })
  );
  document.getElementById('renSearchInput')?.addEventListener('input', (e) => {
    state.renewalSearch = e.target.value;
    renderAdminPane();
  });
  document.getElementById('openNewRenewalBtn')?.addEventListener('click', () => openRenewalApprovalModal(null));
  root.querySelectorAll('[data-custom-renewal]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const s = subs.find((x) => (x.userId || x.id) === btn.dataset.customRenewal);
      if (s) openRenewalApprovalModal(s);
    })
  );
  root.querySelectorAll('[data-expire-renewal]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const s = subs.find((x) => (x.userId || x.id) === btn.dataset.expireRenewal);
      if (s) await performRejectRenewal(s, 'Marked expired awaiting renewal payment');
    })
  );

  bindAdminCommonActions(root);
}

function renderAdminBanners(root) {
  const banners = getBanners();
  const categories = getCategories();

  const filtered = banners.filter((b) => {
    if (state.bannerPlacementTab === 'HOME' && b.placement !== 'HOME') return false;
    if (state.bannerPlacementTab === 'CATEGORY') {
      if (b.placement !== 'CATEGORY') return false;
      if (state.bannerCategoryFilter !== 'all' && b.categoryId !== state.bannerCategoryFilter && b.categoryId !== 'all') {
        return false;
      }
    }
    return true;
  });

  root.innerHTML = `
    <div class="pane-header">
      <div>
        <h2>Manage Banners (Main Page &amp; Categories)</h2>
        <p>Add, edit, reorder, or toggle banners shown at the bottom of the Main Page and Category Pages in the Mobile App</p>
      </div>
      <button type="button" class="btn btn-primary" id="addBannerBtn">+ Add New Banner</button>
    </div>

    <div class="filter-toolbar">
      <div class="chip-tabs">
        <button type="button" class="chip-tab ${state.bannerPlacementTab === 'ALL' ? 'active' : ''}" data-ban-tab="ALL">All Banners (${banners.length})</button>
        <button type="button" class="chip-tab ${state.bannerPlacementTab === 'HOME' ? 'active' : ''}" data-ban-tab="HOME">Main Page / Home (${banners.filter((b) => b.placement === 'HOME').length})</button>
        <button type="button" class="chip-tab ${state.bannerPlacementTab === 'CATEGORY' ? 'active' : ''}" data-ban-tab="CATEGORY">Category Pages (${banners.filter((b) => b.placement === 'CATEGORY').length})</button>
      </div>
      ${
        state.bannerPlacementTab === 'CATEGORY'
          ? `<select id="banCatFilter" class="field-input" style="width:auto;min-width:190px;">
               <option value="all" ${state.bannerCategoryFilter === 'all' ? 'selected' : ''}>All Categories Filter</option>
               ${categories
                 .map(
                   (c) =>
                     `<option value="${esc(c.id)}" ${state.bannerCategoryFilter === c.id ? 'selected' : ''}>${esc(c.name)}</option>`
                 )
                 .join('')}
             </select>`
          : ''
      }
    </div>

    <div class="admin-card-grid">
      ${
        filtered.length
          ? filtered
              .map((b) => {
                const targetLabel =
                  b.placement === 'HOME'
                    ? 'Main Page (Home)'
                    : b.categoryId === 'all'
                    ? 'All Category Pages'
                    : `Category: ${categoryMeta(b.categoryId).name}`;
                return `
                  <div class="admin-card">
                    <div class="info-card-top">
                      <span class="badge ${b.placement === 'HOME' ? 'badge-blue' : 'badge-green'}">${esc(targetLabel)}</span>
                      <span class="badge ${b.active ? 'badge-green' : 'badge-gray'}">${b.active ? `Active #${b.sortOrder || 1}` : 'Hidden'}</span>
                    </div>

                    <div class="banner-mini-preview">
                      ${b.imageData ? `<img src="${esc(b.imageData)}" alt="${esc(b.headline)}" class="banner-mini-img" />` : ''}
                      <div class="banner-mini-text">
                        <div class="banner-mini-eyebrow">${esc(b.eyebrow)}</div>
                        <div class="banner-mini-title">${esc(b.headline)}</div>
                        <div class="banner-mini-desc">${esc(b.description)}</div>
                      </div>
                      <span class="banner-mini-cta">${esc(b.ctaLabel)} &rarr;</span>
                    </div>

                    <div style="display:flex;align-items:center;justify-content:space-between;gap:6px;flex-wrap:wrap;">
                      <div class="table-actions">
                        <button type="button" class="btn btn-outline btn-sm" data-edit-banner="${esc(b.bannerId || b.id)}">Edit</button>
                        <button type="button" class="btn btn-outline btn-sm" data-toggle-banner="${esc(b.bannerId || b.id)}">${b.active ? 'Disable' : 'Enable'}</button>
                        <button type="button" class="btn btn-outline btn-sm" data-preview-banner="${esc(b.bannerId || b.id)}">Preview in Phone</button>
                      </div>
                      <button type="button" class="btn btn-danger btn-sm" data-delete-banner="${esc(b.bannerId || b.id)}">Delete</button>
                    </div>
                  </div>`;
              })
              .join('')
          : '<div class="admin-card"><p style="margin:0;color:#64748b;">No banners found here. Click "+ Add New Banner" to publish one.</p></div>'
      }
    </div>
  `;

  document.getElementById('addBannerBtn')?.addEventListener('click', () => openBannerEditorModal(null));
  root.querySelectorAll('[data-ban-tab]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.bannerPlacementTab = btn.dataset.banTab;
      renderAdminPane();
    })
  );
  document.getElementById('banCatFilter')?.addEventListener('change', (e) => {
    state.bannerCategoryFilter = e.target.value;
    renderAdminPane();
  });

  root.querySelectorAll('[data-edit-banner]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const b = banners.find((x) => (x.bannerId || x.id) === btn.dataset.editBanner);
      if (b) openBannerEditorModal(b);
    })
  );

  root.querySelectorAll('[data-toggle-banner]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const b = banners.find((x) => (x.bannerId || x.id) === btn.dataset.toggleBanner);
      if (b) await performSaveBanner({ ...b, active: !b.active });
    })
  );

  root.querySelectorAll('[data-delete-banner]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      await performDeleteBanner(btn.dataset.deleteBanner);
    })
  );

  root.querySelectorAll('[data-preview-banner]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const b = banners.find((x) => (x.bannerId || x.id) === btn.dataset.previewBanner);
      if (!b) return;
      document.getElementById('portalWorkspace').dataset.activePane = 'mobile';
      if (b.placement === 'HOME') {
        const homeSlides = getActiveBannersForHome();
        const idx = Math.max(0, homeSlides.findIndex((x) => (x.bannerId || x.id) === (b.bannerId || b.id)));
        state.homeBannerIndex = idx;
        navigateMobile('home');
      } else {
        if (b.categoryId && b.categoryId !== 'all') state.selectedCategoryId = b.categoryId;
        const catSlides = getActiveBannersForCategory(state.selectedCategoryId);
        const idx = Math.max(0, catSlides.findIndex((x) => (x.bannerId || x.id) === (b.bannerId || b.id)));
        state.categoryBannerIndex = idx;
        navigateMobile('categories');
      }
      showToast('Focused banner in Current Mobile App');
    })
  );
}

function renderAdminPayments(root) {
  const payments = getPayments();
  const q = state.paymentSearch.trim().toLowerCase();

  const filtered = payments.filter((p) => {
    if (state.paymentStatusFilter !== 'ALL' && p.status !== state.paymentStatusFilter) return false;
    if (state.paymentMethodFilter !== 'ALL' && p.paymentMethod !== state.paymentMethodFilter) return false;
    if (!q) return true;
    return [p.providerName, p.providerPhone, p.transactionRef, p.categoryName, p.planType]
      .join(' ')
      .toLowerCase()
      .includes(q);
  });

  const approvedSum = payments
    .filter((p) => p.status === 'APPROVED')
    .reduce((acc, p) => acc + (Number(p.amountPkr) || 0), 0);
  const pendingSum = payments
    .filter((p) => p.status === 'PENDING')
    .reduce((acc, p) => acc + (Number(p.amountPkr) || 0), 0);

  root.innerHTML = `
    <div class="pane-header">
      <div>
        <h2>Payments Received from Skill Persons</h2>
        <p>Track JazzCash, EasyPaisa, Bank Transfer &amp; Cash fees; approve payments to activate new or renewed Pro accounts</p>
      </div>
      <div style="display:flex;gap:8px;flex-wrap:wrap;">
        <button type="button" class="btn btn-outline" id="exportPaymentsCsvBtn">Export CSV</button>
        <button type="button" class="btn btn-primary" id="recordPaymentBtn">+ Record Payment Received</button>
      </div>
    </div>

    <div class="kpi-grid">
      <div class="kpi-card">
        <span class="kpi-label">Total Verified Revenue</span>
        <span class="kpi-value">${fmtRs(approvedSum)}</span>
        <span class="kpi-sub">${payments.filter((p) => p.status === 'APPROVED').length} Approved Receipts</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">Pending Verification</span>
        <span class="kpi-value">${fmtRs(pendingSum)}</span>
        <span class="kpi-sub">${payments.filter((p) => p.status === 'PENDING').length} Awaiting Approval</span>
      </div>
      <div class="kpi-card">
        <span class="kpi-label">Renewal Payments</span>
        <span class="kpi-value">${payments.filter((p) => p.planType === 'PRO_RENEWAL' || p.isRenewal).length}</span>
        <span class="kpi-sub">Old Skill Persons Renewing</span>
      </div>
    </div>

    <div class="filter-toolbar">
      <div class="chip-tabs">
        ${['ALL', 'PENDING', 'APPROVED', 'REJECTED', 'REFUNDED']
          .map(
            (st) =>
              `<button type="button" class="chip-tab ${state.paymentStatusFilter === st ? 'active' : ''}" data-pay-status="${st}">${st}</button>`
          )
          .join('')}
      </div>
      <div style="display:flex;gap:8px;flex-wrap:wrap;">
        <select id="payMethodFilter" class="field-input" style="width:auto;">
          <option value="ALL" ${state.paymentMethodFilter === 'ALL' ? 'selected' : ''}>All Methods</option>
          <option value="JazzCash" ${state.paymentMethodFilter === 'JazzCash' ? 'selected' : ''}>JazzCash</option>
          <option value="EasyPaisa" ${state.paymentMethodFilter === 'EasyPaisa' ? 'selected' : ''}>EasyPaisa</option>
          <option value="Bank Transfer" ${state.paymentMethodFilter === 'Bank Transfer' ? 'selected' : ''}>Bank Transfer</option>
          <option value="Cash" ${state.paymentMethodFilter === 'Cash' ? 'selected' : ''}>Cash</option>
          <option value="Google Play" ${state.paymentMethodFilter === 'Google Play' ? 'selected' : ''}>Google Play</option>
        </select>
        <input type="search" id="paySearchInput" class="search-box" value="${esc(state.paymentSearch)}" placeholder="Search provider or Tx Ref..." />
      </div>
    </div>

    <div class="data-table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Skill Person</th>
            <th>Plan &amp; Period</th>
            <th>Amount</th>
            <th>Method &amp; Ref</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${
            filtered.length
              ? filtered
                  .map(
                    (p) => `
                    <tr>
                      <td>${fmtDate(p.createdAt || p.createdAtMillis)}</td>
                      <td>
                        <strong>${esc(p.providerName)}</strong>
                        <div style="font-size:11.5px;color:#64748b;">${esc(p.categoryName)} &bull; ${esc(p.providerPhone)}</div>
                      </td>
                      <td>
                        <strong>${esc(p.planType)}</strong>
                        <div style="font-size:11.5px;color:#64748b;">${Number(p.billingPeriodMonths) || 1} month(s)</div>
                      </td>
                      <td><strong>${fmtRs(p.amountPkr)}</strong></td>
                      <td>
                        <strong>${esc(p.paymentMethod)}</strong>
                        <div style="font-size:11.5px;color:#64748b;">${esc(p.transactionRef)}</div>
                      </td>
                      <td><span class="badge ${statusBadgeClass(p.status)}">${esc(p.status)}</span></td>
                      <td>
                        <div class="table-actions">
                          <button type="button" class="btn btn-outline btn-sm" data-view-receipt="${esc(p.paymentId || p.id)}">Receipt</button>
                          ${
                            p.status !== 'APPROVED'
                              ? `<button type="button" class="btn btn-success btn-sm" data-approve-payment="${esc(p.paymentId || p.id)}">Approve</button>`
                              : ''
                          }
                          ${
                            p.status === 'PENDING'
                              ? `<button type="button" class="btn btn-danger btn-sm" data-reject-payment="${esc(p.paymentId || p.id)}">Reject</button>`
                              : ''
                          }
                        </div>
                      </td>
                    </tr>`
                  )
                  .join('')
              : '<tr><td colspan="7" style="text-align:center;color:#64748b;">No payment records match the filter.</td></tr>'
          }
        </tbody>
      </table>
    </div>
  `;

  document.getElementById('recordPaymentBtn')?.addEventListener('click', () => openRecordPaymentModal());
  document.getElementById('exportPaymentsCsvBtn')?.addEventListener('click', () => {
    const header = ['Date', 'Provider', 'Phone', 'Category', 'Plan', 'Months', 'AmountPKR', 'Method', 'TxRef', 'Status'];
    const rows = filtered.map((p) => [
      fmtDate(p.createdAt || p.createdAtMillis),
      p.providerName,
      p.providerPhone,
      p.categoryName,
      p.planType,
      p.billingPeriodMonths || 1,
      p.amountPkr,
      p.paymentMethod,
      p.transactionRef,
      p.status
    ]);
    const csv = [header, ...rows]
      .map((r) => r.map((cell) => `"${String(cell ?? '').replace(/"/g, '""')}"`).join(','))
      .join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `skillconnect-payments-${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  });

  root.querySelectorAll('[data-pay-status]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.paymentStatusFilter = btn.dataset.payStatus;
      renderAdminPane();
    })
  );
  document.getElementById('payMethodFilter')?.addEventListener('change', (e) => {
    state.paymentMethodFilter = e.target.value;
    renderAdminPane();
  });
  document.getElementById('paySearchInput')?.addEventListener('input', (e) => {
    state.paymentSearch = e.target.value;
    renderAdminPane();
  });

  root.querySelectorAll('[data-view-receipt]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const p = payments.find((x) => (x.paymentId || x.id) === btn.dataset.viewReceipt);
      if (!p) return;
      openAdminModal(`
        <div class="modal-header">
          <h3>Payment Receipt — ${esc(p.providerName)}</h3>
          <button type="button" class="modal-close-btn" id="closeAdminModalBtn">&times;</button>
        </div>
        <div style="font-size:13px;line-height:1.5;">
          <div><strong>Amount:</strong> ${fmtRs(p.amountPkr)} (${Number(p.billingPeriodMonths) || 1} month)</div>
          <div><strong>Method:</strong> ${esc(p.paymentMethod)} &bull; <strong>Ref:</strong> ${esc(p.transactionRef)}</div>
          <div><strong>Plan:</strong> ${esc(p.planType)} &bull; <strong>Status:</strong> ${esc(p.status)}</div>
          <div><strong>Note:</strong> ${esc(p.receiptNote || '—')}</div>
        </div>
        ${
          p.receiptImageData
            ? `<img src="${esc(p.receiptImageData)}" alt="Payment Receipt" class="doc-preview-img" style="max-height:320px;" />`
            : '<div style="padding:12px;background:#f8fafc;border-radius:10px;font-size:12px;color:#64748b;">No receipt image uploaded for this entry.</div>'
        }
        <div style="display:flex;justify-content:flex-end;gap:8px;">
          ${p.status !== 'APPROVED' ? `<button type="button" class="btn btn-success" id="receiptApproveBtn">Approve &amp; Activate</button>` : ''}
          <button type="button" class="btn btn-outline" id="closeAdminModalBtn2">Close</button>
        </div>
      `);
      document.getElementById('closeAdminModalBtn')?.addEventListener('click', closeAdminModal);
      document.getElementById('closeAdminModalBtn2')?.addEventListener('click', closeAdminModal);
      document.getElementById('receiptApproveBtn')?.addEventListener('click', async () => {
        await performPaymentDecision(p, 'APPROVED', 'Receipt verified by Admin');
        closeAdminModal();
      });
    })
  );

  root.querySelectorAll('[data-reject-payment]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const p = payments.find((x) => (x.paymentId || x.id) === btn.dataset.rejectPayment);
      if (p) await performPaymentDecision(p, 'REJECTED', 'Transaction ref could not be verified');
    })
  );

  bindAdminCommonActions(root);
}

function renderAdminCategories(root) {
  const categories = getCategories();
  root.innerHTML = `
    <div class="pane-header">
      <div>
        <h2>Service Categories (${categories.length})</h2>
        <p>Manage categories shown on the Mobile Home carousel and targetable by Category Page banners</p>
      </div>
      <div style="display:flex;gap:8px;flex-wrap:wrap;">
        ${
          state.isLiveAdmin
            ? `<button type="button" class="btn btn-outline" id="seedCategoriesBtn">Sync 21 Default Categories</button>`
            : ''
        }
        <button type="button" class="btn btn-primary" id="addCategoryBtn">+ Add Category</button>
      </div>
    </div>

    <div class="admin-card-grid">
      ${categories
        .map(
          (c) => `
          <div class="admin-card">
            <div class="info-card-top">
              <div style="display:flex;align-items:center;gap:10px;">
                <div class="sc-cat-icon-circle" style="background:${esc(c.bg || '#dcfce7')};">${esc(c.icon || '🔧')}</div>
                <div>
                  <strong style="font-size:14px;">${esc(c.name)}</strong>
                  <div style="font-size:11.5px;color:#64748b;">#${c.sortOrder || 1} &bull; ${esc(c.id)}</div>
                </div>
              </div>
              <span class="badge ${c.active !== false ? 'badge-green' : 'badge-gray'}">${c.active !== false ? 'Active' : 'Hidden'}</span>
            </div>
            <div style="font-size:12px;color:#475569;">${esc(c.subtitle || '')}</div>
            <div style="font-size:11px;color:#64748b;">${(c.subcategories || []).map(esc).join(' &bull; ')}</div>
            <div style="display:flex;gap:8px;margin-top:auto;">
              <button type="button" class="btn btn-outline btn-sm" data-edit-cat="${esc(c.id)}">Edit</button>
              <button type="button" class="btn btn-outline btn-sm" data-open-cat-phone="${esc(c.id)}">View in Mobile App</button>
            </div>
          </div>`
        )
        .join('')}
    </div>
  `;

  document.getElementById('addCategoryBtn')?.addEventListener('click', () => openCategoryEditorModal(null));
  document.getElementById('seedCategoriesBtn')?.addEventListener('click', async () => {
    const added = await seedCategoriesList(DEFAULT_CATEGORIES);
    showToast(added ? `Synced ${added} missing categories` : 'All 21 categories already synced');
  });
  root.querySelectorAll('[data-edit-cat]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const cat = categories.find((x) => x.id === btn.dataset.editCat);
      if (cat) openCategoryEditorModal(cat);
    })
  );
  root.querySelectorAll('[data-open-cat-phone]').forEach((btn) =>
    btn.addEventListener('click', () => {
      document.getElementById('portalWorkspace').dataset.activePane = 'mobile';
      openCategoryInMobile(btn.dataset.openCatPhone);
    })
  );
}

function renderAdminOperations(root) {
  const bookings = getBookings();
  const complaints = getComplaints();

  root.innerHTML = `
    <div class="pane-header">
      <div>
        <h2>Bookings &amp; Reports Moderation</h2>
        <p>Monitor live customer bookings and resolve complaints reported against Skill Persons</p>
      </div>
    </div>

    <h3 style="margin:4px 0 0;font-size:15px;">Customer Bookings (${bookings.length})</h3>
    <div class="data-table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>Service</th>
            <th>Client</th>
            <th>Skill Person</th>
            <th>Area &amp; Slot</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          ${bookings
            .map(
              (b) => `
              <tr>
                <td><strong>${esc(b.serviceTitle)}</strong></td>
                <td>${esc(b.clientName)}<br/><span style="font-size:11px;color:#64748b;">${esc(b.clientPhone || '')}</span></td>
                <td>${esc(b.technicianName)}</td>
                <td>${esc(b.serviceArea)}<br/><span style="font-size:11px;color:#64748b;">${esc(b.preferredTime)}</span></td>
                <td>
                  <select class="field-input" style="padding:5px 8px;font-size:12px;width:auto;" data-booking-status="${esc(b.id)}">
                    ${['REQUESTED', 'ACCEPTED', 'EN_ROUTE', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']
                      .map((st) => `<option value="${st}" ${b.status === st ? 'selected' : ''}>${st}</option>`)
                      .join('')}
                  </select>
                </td>
              </tr>`
            )
            .join('')}
        </tbody>
      </table>
    </div>

    <h3 style="margin:6px 0 0;font-size:15px;">Complaints &amp; Safety Reports (${complaints.length})</h3>
    <div class="admin-card-grid">
      ${complaints
        .map(
          (c) => `
          <div class="admin-card">
            <div class="info-card-top">
              <strong>${esc(c.reason)}</strong>
              <span class="badge ${statusBadgeClass(c.status)}">${esc(c.status)}</span>
            </div>
            <div style="font-size:12.5px;color:#334155;">${esc(c.description)}</div>
            <div style="font-size:11.5px;color:#64748b;">Reporter: ${esc(c.reporterName)} &bull; Reported Pro: ${esc(c.reportedTechName)}</div>
            ${c.adminResolution ? `<div style="font-size:12px;color:#065f46;background:#ecfdf5;padding:7px 9px;border-radius:8px;">Resolution: ${esc(c.adminResolution)}</div>` : ''}
            ${
              c.status !== 'RESOLVED'
                ? `<div class="table-actions">
                     <button type="button" class="btn btn-success btn-sm" data-resolve-comp="${esc(c.id)}">Mark Resolved</button>
                   </div>`
                : ''
            }
          </div>`
        )
        .join('')}
    </div>
  `;

  root.querySelectorAll('[data-booking-status]').forEach((sel) =>
    sel.addEventListener('change', async () => {
      const id = sel.dataset.bookingStatus;
      if (state.isLiveAdmin) {
        await updateBookingStatusAdmin(id, sel.value);
      } else {
        const b = state.data.bookings.find((x) => x.id === id);
        if (b) {
          b.status = sel.value;
          persistLocal();
        }
      }
      showToast(`Booking updated to ${sel.value}`);
      renderAll();
    })
  );

  root.querySelectorAll('[data-resolve-comp]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const id = btn.dataset.resolveComp;
      const resolution = 'Reviewed by Admin and warning logged on provider profile.';
      if (state.isLiveAdmin) {
        await resolveComplaintAdmin(id, 'RESOLVED', resolution);
      } else {
        const c = state.data.complaints.find((x) => x.id === id);
        if (c) {
          c.status = 'RESOLVED';
          c.adminResolution = resolution;
          persistLocal();
        }
      }
      showToast('Complaint resolved');
      renderAll();
    })
  );
}

function bindAdminCommonActions(root) {
  root.querySelectorAll('[data-jump-section]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.adminSection = btn.dataset.jumpSection;
      renderAdminPane();
      if (btn.dataset.openBannerAdd) openBannerEditorModal(null);
      if (btn.dataset.openPayAdd) openRecordPaymentModal();
    })
  );
  root.querySelectorAll('[data-inspect-tech]').forEach((btn) =>
    btn.addEventListener('click', () => openInspectAccountModal(btn.dataset.inspectTech))
  );
  root.querySelectorAll('[data-quick-approve-tech]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const tech = getTechnicians().find((t) => (t.userId || t.id) === btn.dataset.quickApproveTech);
      if (tech) await performReviewAccount(tech, 'APPROVED', true, true, 'Verified & approved via Laptop Admin');
    })
  );
  root.querySelectorAll('[data-quick-reject-tech]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const tech = getTechnicians().find((t) => (t.userId || t.id) === btn.dataset.quickRejectTech);
      if (tech) await performReviewAccount(tech, 'REJECTED', false, false, 'Rejected via Laptop Admin');
    })
  );
  root.querySelectorAll('[data-quick-approve-renewal]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const s = getSubscriptions().find((x) => (x.userId || x.id) === btn.dataset.quickApproveRenewal);
      if (s) {
        await performApproveRenewal({
          userId: s.userId || s.id,
          providerName: s.providerName,
          providerPhone: s.providerPhone,
          months: 1,
          amountPkr: s.lastPaymentAmount || 499
        });
      }
    })
  );
  root.querySelectorAll('[data-approve-payment]').forEach((btn) =>
    btn.addEventListener('click', async () => {
      const p = getPayments().find((x) => (x.paymentId || x.id) === btn.dataset.approvePayment);
      if (p) await performPaymentDecision(p, 'APPROVED', 'Payment verified & approved by Admin');
    })
  );
}

/* ==================== CURRENT MOBILE APP LIVE SHOWCASE ==================== */
function getActiveBannersForHome() {
  const list = getBanners().filter((b) => b.active !== false && b.placement === 'HOME');
  return list.length ? list : makeInitialSeedData().banners.filter((b) => b.placement === 'HOME');
}

function getActiveBannersForCategory(categoryId) {
  const all = getBanners().filter((b) => b.active !== false && b.placement === 'CATEGORY');
  const targeted = all.filter((b) => b.categoryId === categoryId);
  const shared = all.filter((b) => b.categoryId === 'all');
  const merged = [...targeted, ...shared];
  return merged.length ? merged : makeInitialSeedData().banners.filter((b) => b.placement === 'CATEGORY');
}

function handleBannerCtaAction(action) {
  const a = String(action || 'explore_services');
  if (a === 'map') return navigateMobile('map');
  if (a === 'subscription') return navigateMobile('subscription');
  if (a === 'prohub') return navigateMobile('prohub');
  if (a.startsWith('cat:')) return openCategoryInMobile(a.slice(4));
  openCategoryInMobile(state.selectedCategoryId || 'electrician');
}

function renderBannerCarousel(cardEl, dotsEl, slides, indexKey) {
  if (!cardEl || !dotsEl || !slides.length) return;
  const idx = ((state[indexKey] % slides.length) + slides.length) % slides.length;
  state[indexKey] = idx;
  const slide = slides[idx];

  cardEl.innerHTML = `
    ${slide.imageData ? `<img src="${esc(slide.imageData)}" alt="${esc(slide.headline)}" class="sc-promo-img" />` : ''}
    <div class="sc-promo-text">
      <div class="sc-promo-eyebrow">${esc(slide.eyebrow || 'SkillConnect')}</div>
      <h3 class="sc-promo-headline">${esc(slide.headline)}</h3>
      <p class="sc-promo-desc">${esc(slide.description)}</p>
    </div>
    <button type="button" class="sc-promo-cta" data-banner-cta="${esc(slide.ctaAction || 'explore_services')}">
      ${esc(slide.ctaLabel || 'Explore Now')} &rarr;
    </button>
  `;

  cardEl.querySelector('[data-banner-cta]')?.addEventListener('click', (e) => {
    e.stopPropagation();
    state.bannerAutoPausedUntil = Date.now() + 8000;
    handleBannerCtaAction(e.currentTarget.dataset.bannerCta);
  });

  dotsEl.innerHTML = slides
    .map(
      (_, i) =>
        `<button type="button" class="sc-dot ${i === idx ? 'active' : ''}" data-dot-idx="${i}" aria-label="Slide ${i + 1}"></button>`
    )
    .join('');

  dotsEl.querySelectorAll('[data-dot-idx]').forEach((dot) =>
    dot.addEventListener('click', () => {
      state.bannerAutoPausedUntil = Date.now() + 8000;
      state[indexKey] = Number(dot.dataset.dotIdx) || 0;
      renderBannerCarousel(cardEl, dotsEl, slides, indexKey);
    })
  );
}

function bindSwipeForBanner(cardEl, getSlidesLen, indexKey, rerenderFn) {
  if (!cardEl || cardEl.dataset.swipeBound) return;
  cardEl.dataset.swipeBound = '1';
  let startX = 0;

  cardEl.addEventListener('pointerdown', (e) => {
    startX = e.clientX;
    state.bannerAutoPausedUntil = Date.now() + 8000;
  });
  cardEl.addEventListener('pointerup', (e) => {
    const dx = e.clientX - startX;
    const len = getSlidesLen();
    if (Math.abs(dx) < 28 || len <= 1) return;
    state[indexKey] = dx < 0 ? (state[indexKey] + 1) % len : (state[indexKey] - 1 + len) % len;
    rerenderFn();
  });
}

function navigateMobile(view) {
  state.mobileView = view;
  document.querySelectorAll('.mobile-screen .view-screen').forEach((sec) => {
    sec.classList.toggle('view-active', sec.id === `view-${view}`);
  });
  renderMobileBottomNav();
  renderMobileActiveView();
}

function openCategoryInMobile(categoryId) {
  state.selectedCategoryId = categoryId;
  state.selectedSubcategory = 'All';
  state.categorySearch = '';
  state.categoryBannerIndex = 0;
  navigateMobile('categories');
}

function renderMobileBottomNav() {
  const nav = document.getElementById('bottomNav');
  if (!nav) return;
  const items = [
    { id: 'home', label: 'Home', icon: 'assets/icons/nav-home.svg' },
    { id: 'map', label: 'Map', icon: 'assets/icons/nav-map.svg' },
    { id: 'bookings', label: 'Bookings', icon: 'assets/icons/nav-bookings.svg' },
    { id: 'profile', label: 'Profile', icon: 'assets/icons/nav-profile.svg' },
    { id: 'prohub', label: 'Pro Hub', icon: 'assets/icons/cat-electrician.svg' }
  ];
  const activeTab = state.mobileView === 'categories' ? 'home' : state.mobileView === 'subscription' ? 'profile' : state.mobileView;

  nav.innerHTML = items
    .map(
      (item) => `
      <button type="button" class="sc-nav-item ${activeTab === item.id ? 'active' : ''}" data-mobile-tab="${item.id}">
        <img src="${item.icon}" alt="${item.label}" />
        <span>${item.label}</span>
      </button>`
    )
    .join('');

  nav.querySelectorAll('[data-mobile-tab]').forEach((btn) =>
    btn.addEventListener('click', () => navigateMobile(btn.dataset.mobileTab))
  );
}

function buildMobileTechCardHtml(t) {
  const dist = haversineKm(state.mobileLocation.lat, state.mobileLocation.lng, t.latitude, t.longitude);
  const cat = categoryMeta(t.categoryId);
  const avatar = t.profilePhotoData || 'assets/images/mobile/img_pro_ali_raza.jpg';
  return `
    <article class="sc-pro-card" data-open-tech="${esc(t.userId || t.id)}">
      <div class="sc-pro-top">
        <img src="${esc(avatar)}" alt="${esc(t.fullName)}" class="sc-pro-avatar" />
        <div class="sc-pro-info">
          <div class="sc-pro-name-row">
            <h3 class="sc-pro-name">${esc(t.fullName)}</h3>
            ${t.verifiedBadge ? '<img src="assets/icons/verified.svg" alt="Verified" class="sc-verified-icon" />' : ''}
          </div>
          <p class="sc-pro-meta">${esc(cat.name)} &bull; ${esc(t.workingArea)}</p>
          <div class="sc-pro-rating-row">
            <img src="assets/icons/star.svg" alt="" class="sc-star-icon" />
            <strong>${Number(t.averageRating || 4.8).toFixed(1)}</strong>
            <span style="color:#64748b;">(${t.reviewCount || 12} reviews)</span>
          </div>
        </div>
        <div class="sc-pro-status-col">
          <span class="sc-distance-pill"><img src="assets/icons/pin-green.svg" alt="" />${dist} km</span>
          <span class="sc-availability-badge">${t.availableNow ? 'Available' : 'Busy'}</span>
        </div>
      </div>
      <div class="sc-pro-bottom">
        <div>
          <span class="sc-charge-amount">${esc(t.visitCharge)}</span>
          <span class="sc-charge-unit">/ visit</span>
        </div>
        <div class="sc-card-actions">
          <a href="tel:${esc(String(t.contactPhone || '').replace(/\s+/g, ''))}" class="sc-btn-call" data-stop-card="1">
            <img src="assets/icons/phone-green.svg" alt="" />Call
          </a>
          <button type="button" class="sc-btn-book" data-book-tech="${esc(t.userId || t.id)}" data-stop-card="1">Book Now</button>
        </div>
      </div>
    </article>
  `;
}

function bindMobileTechCardClicks(container) {
  container.querySelectorAll('[data-stop-card]').forEach((el) =>
    el.addEventListener('click', (e) => e.stopPropagation())
  );
  container.querySelectorAll('[data-book-tech]').forEach((btn) =>
    btn.addEventListener('click', () => openMobileBookingSheet(btn.dataset.bookTech))
  );
  container.querySelectorAll('[data-open-tech]').forEach((card) =>
    card.addEventListener('click', () => openMobileTechDetailSheet(card.dataset.openTech))
  );
}

function renderMobileHome() {
  document.getElementById('locationText').textContent = state.mobileLocation.name;
  document.getElementById('notificationBadge').textContent = String(state.data.notifications.length);

  const q = state.mobileSearch.trim().toLowerCase();
  const searchWrap = document.getElementById('homeSearchResultsWrap');
  const defaultBody = document.getElementById('homeDefaultBody');
  document.getElementById('clearSearchBtn').classList.toggle('hidden', !q);

  if (q) {
    searchWrap.classList.remove('hidden');
    defaultBody.classList.add('hidden');
    const matches = getTechnicians()
      .filter((t) => t.listed !== false && t.verificationStatus !== 'REJECTED')
      .filter((t) => [t.fullName, t.categoryId, t.subcategory, t.expertise, t.workingArea].join(' ').toLowerCase().includes(q));
    const listEl = document.getElementById('homeSearchResultsList');
    listEl.innerHTML = matches.length
      ? matches.map(buildMobileTechCardHtml).join('')
      : '<div class="info-card">No matching professionals found nearby.</div>';
    bindMobileTechCardClicks(listEl);
  } else {
    searchWrap.classList.add('hidden');
    defaultBody.classList.remove('hidden');
  }

  const carousel = document.getElementById('categoriesCarousel');
  const activeCats = getCategories().filter((c) => c.active !== false);
  carousel.innerHTML = activeCats
    .map(
      (c) => `
      <button type="button" class="sc-cat-tile" data-cat-id="${esc(c.id)}">
        <span class="sc-cat-icon-circle" style="background:${esc(c.bg || '#dcfce7')};">${esc(c.icon || '🔧')}</span>
        <span class="sc-cat-label">${esc(c.name)}</span>
      </button>`
    )
    .join('');

  carousel.querySelectorAll('[data-cat-id]').forEach((btn) =>
    btn.addEventListener('click', () => openCategoryInMobile(btn.dataset.catId))
  );

  const homeCard = document.getElementById('homeBannerCard');
  const homeDots = document.getElementById('homeBannerDots');
  renderBannerCarousel(homeCard, homeDots, getActiveBannersForHome(), 'homeBannerIndex');
  bindSwipeForBanner(homeCard, () => getActiveBannersForHome().length, 'homeBannerIndex', () =>
    renderBannerCarousel(homeCard, homeDots, getActiveBannersForHome(), 'homeBannerIndex')
  );
}

function renderMobileCategoryPage() {
  const cat = categoryMeta(state.selectedCategoryId);
  const subchips = ['All', ...(cat.subcategories || [])];

  document.getElementById('categoryPageIconBadge').textContent = cat.icon || '🔧';
  document.getElementById('categoriesPageTitle').textContent = cat.name;

  const q = state.categorySearch.trim().toLowerCase();
  const techs = getTechnicians()
    .filter((t) => t.listed !== false && t.verificationStatus !== 'REJECTED')
    .filter((t) => t.categoryId === cat.id)
    .filter((t) => state.selectedSubcategory === 'All' || t.subcategory === state.selectedSubcategory)
    .filter((t) => !q || [t.fullName, t.contactPhone, t.workingArea, t.expertise].join(' ').toLowerCase().includes(q))
    .sort(
      (a, b) =>
        Number(haversineKm(state.mobileLocation.lat, state.mobileLocation.lng, a.latitude, a.longitude)) -
        Number(haversineKm(state.mobileLocation.lat, state.mobileLocation.lng, b.latitude, b.longitude))
    );

  document.getElementById('categoriesPageSubtitle').textContent = `${techs.length} candidate(s) • Near to Far`;

  const subRow = document.getElementById('categorySubchipsRow');
  subRow.innerHTML = subchips
    .map(
      (s) =>
        `<button type="button" class="cat-subchip ${state.selectedSubcategory === s ? 'active' : ''}" data-subchip="${esc(s)}">${esc(s)}</button>`
    )
    .join('');
  subRow.querySelectorAll('[data-subchip]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.selectedSubcategory = btn.dataset.subchip;
      renderMobileCategoryPage();
    })
  );

  const listEl = document.getElementById('categoryProfessionalsList');
  listEl.innerHTML = techs.length
    ? techs.map(buildMobileTechCardHtml).join('')
    : `<div class="info-card"><strong>No listed candidates in ${esc(cat.name)} yet</strong><span style="font-size:12px;color:#64748b;">Approve a Skill Person in the Laptop Admin Console to list them here.</span></div>`;
  bindMobileTechCardClicks(listEl);

  /* 5-Slide Promotional Banner + 5 Dots at bottom (NO text line underneath) */
  const catCard = document.getElementById('categoryBannerCard');
  const catDots = document.getElementById('categoryBannerDots');
  const slides = getActiveBannersForCategory(cat.id);
  renderBannerCarousel(catCard, catDots, slides, 'categoryBannerIndex');
  bindSwipeForBanner(catCard, () => getActiveBannersForCategory(cat.id).length, 'categoryBannerIndex', () =>
    renderBannerCarousel(catCard, catDots, getActiveBannersForCategory(cat.id), 'categoryBannerIndex')
  );
}

function renderMobileMap() {
  document.getElementById('mapLocationSubtitle').textContent = `Centered around ${state.mobileLocation.name}`;
  const listed = getTechnicians().filter((t) => t.listed !== false && t.verificationStatus !== 'REJECTED');
  const pins = document.getElementById('mapPinsContainer');
  pins.innerHTML = listed
    .map((t, i) => {
      const left = 18 + ((i * 23) % 66);
      const top = 22 + ((i * 29) % 58);
      const isSelected = (t.userId || t.id) === state.selectedMapTechId;
      return `<button type="button" class="map-pro-pin ${isSelected ? 'active' : ''}" style="left:${left}%;top:${top}%;" data-map-tech="${esc(t.userId || t.id)}">${esc(t.fullName.split(' ')[0])} &bull; ${esc(t.visitCharge)}</button>`;
    })
    .join('');

  pins.querySelectorAll('[data-map-tech]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.selectedMapTechId = btn.dataset.mapTech;
      renderMobileMap();
    })
  );

  const selected = listed.find((t) => (t.userId || t.id) === state.selectedMapTechId) || listed[0];
  const cardBox = document.getElementById('mapSelectedCardContainer');
  cardBox.innerHTML = selected ? buildMobileTechCardHtml(selected) : '<div class="info-card">No active professionals on map.</div>';
  bindMobileTechCardClicks(cardBox);
}

function renderMobileBookings() {
  const bookings = getBookings();
  const container = document.getElementById('bookingsHistoryContainer');
  container.innerHTML = bookings.length
    ? bookings
        .map(
          (b) => `
          <div class="info-card">
            <div class="info-card-top">
              <strong>${esc(b.serviceTitle)}</strong>
              <span class="badge ${statusBadgeClass(b.status)}">${esc(b.status)}</span>
            </div>
            <div style="font-size:12px;color:#475569;">Skill Person: <strong>${esc(b.technicianName)}</strong></div>
            <div style="font-size:11.5px;color:#64748b;">${esc(b.serviceArea)} &bull; ${esc(b.preferredTime)}</div>
          </div>`
        )
        .join('')
    : '<div class="info-card">No bookings yet. Tap Book Now on any professional!</div>';
}

function renderMobileProfile() {
  const used = Math.min(5, state.mobileMonthlyBookingsUsed);
  const remaining = Math.max(0, 5 - used);
  const pct = Math.round((used / 5) * 100);
  const isPro = state.mobilePreviewPlan === 'PRO';

  const body = document.getElementById('mobileProfileBody');
  body.innerHTML = `
    <div class="profile-card">
      <div class="profile-avatar-circle">SC</div>
      <div class="profile-meta">
        <h3>Saad Mahmood</h3>
        <p>${esc(state.mobileLocation.name)} &bull; +92 301 5550112</p>
      </div>
    </div>

    ${
      isPro
        ? `<div class="promo-sub-banner pro-active">
             <div class="promo-sub-top">
               <strong style="font-size:13px;">SkillConnect Pro Member</strong>
               <span class="badge badge-amber">PRO PLAN</span>
             </div>
             <div style="font-size:13.5px;font-weight:700;">Unlimited Confirmed Service Bookings</div>
             <div style="font-size:11.5px;color:#475569;">Priority customer support &amp; verified Pro status active.</div>
             <button type="button" class="outline-full-btn" id="openMySubscriptionBtn">Manage My Subscription</button>
           </div>`
        : `<div class="promo-sub-banner">
             <div class="promo-sub-top">
               <span style="font-size:12px;font-weight:700;color:#065f46;">&#127873; Welcome to SkillConnect!</span>
               <span class="badge badge-green">FREE PLAN</span>
             </div>
             <strong style="font-size:15px;">Your Free Account is Active</strong>
             <p style="margin:0;font-size:11.5px;color:#334155;">
               ${
                 used >= 5
                   ? "You've reached your monthly Free Plan limit. Upgrade to Pro for unlimited bookings."
                   : 'Enjoy up to <strong>5 confirmed service bookings per month</strong> with your Free Plan.'
               }
             </p>
             <div class="promo-progress-track">
               <div class="promo-progress-fill" style="width:${pct}%;"></div>
             </div>
             <div style="display:flex;justify-content:space-between;font-size:11px;color:#475569;">
               <span>${used} of 5 bookings used.</span>
               <strong style="color:#065f46;">${remaining} bookings remaining.</strong>
             </div>
             <hr style="border:none;border-top:1px solid #e2e8f0;margin:2px 0;" />
             <strong style="font-size:12.5px;">&#128081; Want unlimited bookings?</strong>
             <p style="margin:0;font-size:11.5px;color:#334155;">Upgrade to SkillConnect Pro and receive unlimited service bookings.</p>
             <button type="button" class="promo-upgrade-btn" id="openMySubscriptionBtn">Upgrade to Pro – Rs. 499/month &rarr;</button>
           </div>`
    }

    <div class="info-card">
      <strong>Account Settings</strong>
      <div style="display:flex;justify-content:space-between;align-items:center;font-size:12px;padding:4px 0;">
        <span>Preview Monthly Free Bookings (${used}/5)</span>
        <button type="button" class="btn btn-outline btn-sm" id="simBookingIncBtn">+1 Booking</button>
      </div>
      <div style="display:flex;justify-content:space-between;align-items:center;font-size:12px;padding:4px 0;">
        <span>Current Plan Mode</span>
        <button type="button" class="btn btn-outline btn-sm" id="toggleFreeProPreviewBtn">Switch to ${isPro ? 'FREE' : 'PRO'}</button>
      </div>
    </div>
  `;

  document.getElementById('openMySubscriptionBtn')?.addEventListener('click', () => navigateMobile('subscription'));
  document.getElementById('simBookingIncBtn')?.addEventListener('click', () => {
    state.mobileMonthlyBookingsUsed = (state.mobileMonthlyBookingsUsed + 1) % 6;
    renderMobileProfile();
  });
  document.getElementById('toggleFreeProPreviewBtn')?.addEventListener('click', () => {
    state.mobilePreviewPlan = isPro ? 'FREE' : 'PRO';
    renderMobileProfile();
  });
}

function renderMobileSubscription() {
  const isPro = state.mobilePreviewPlan === 'PRO';
  const body = document.getElementById('mobileSubscriptionBody');
  body.innerHTML = `
    <div class="plan-compare-card">
      <div class="info-card-top">
        <strong>Free Plan</strong>
        ${!isPro ? '<span class="badge badge-green">Current Plan</span>' : ''}
      </div>
      <div style="font-size:20px;font-weight:800;">Rs. 0 <span style="font-size:12px;font-weight:500;color:#64748b;">/ month</span></div>
      <ul style="margin:0;padding-left:18px;font-size:12px;color:#334155;line-height:1.6;">
        <li>5 confirmed service bookings per month</li>
        <li>Browse all 21 SkillConnect categories</li>
        <li>Live GPS nearby search &amp; direct call</li>
      </ul>
    </div>

    <div class="plan-compare-card pro">
      <div class="info-card-top">
        <strong>SkillConnect Pro</strong>
        <span class="badge badge-amber">${isPro ? 'Active Pro' : 'Recommended'}</span>
      </div>
      <div style="font-size:20px;font-weight:800;color:#b45309;">Rs. 499 <span style="font-size:12px;font-weight:500;color:#64748b;">/ month</span></div>
      <ul style="margin:0;padding-left:18px;font-size:12px;color:#334155;line-height:1.6;">
        <li>Unlimited confirmed service bookings</li>
        <li>Priority Skill Person matching &amp; renewal support</li>
        <li>Zero monthly booking cap</li>
      </ul>
      <button type="button" class="promo-upgrade-btn" id="completeProUpgradeBtn">
        ${isPro ? 'Pro Plan Active' : 'Complete Upgrade – Rs. 499/month'}
      </button>
    </div>
  `;

  document.getElementById('completeProUpgradeBtn')?.addEventListener('click', () => {
    state.mobilePreviewPlan = 'PRO';
    showToast('Upgraded to SkillConnect Pro');
    navigateMobile('profile');
  });
}

function renderMobileProHub() {
  const subs = getSubscriptions();
  const body = document.getElementById('mobileProHubBody');
  body.innerHTML = `
    <div class="info-card" style="background:linear-gradient(135deg,#022c22,#027245);color:#ffffff;">
      <strong style="font-size:14.5px;">SkillConnect Provider &amp; Renewal Hub</strong>
      <p style="margin:0;font-size:11.5px;opacity:0.9;">
        New accounts and old Pro renewals submitted from the Android app appear instantly in the Laptop Admin Console for approval.
      </p>
      <a href="${APK_RELEASE_URL}" target="_blank" rel="noopener" class="btn btn-primary btn-sm" style="align-self:flex-start;margin-top:4px;">Download Android APK</a>
    </div>

    ${subs
      .map(
        (s) => `
        <div class="info-card">
          <div class="info-card-top">
            <strong>${esc(s.providerName)}</strong>
            <span class="badge ${statusBadgeClass(s.renewalStatus)}">${esc(s.renewalStatus)}</span>
          </div>
          <div style="font-size:11.5px;color:#475569;">Tier: ${esc(s.tier)} &bull; Valid until: ${fmtDate(s.renewalExpiresAtMillis)}</div>
          <div style="font-size:11px;color:#64748b;">${esc(s.adminRemarks || '')}</div>
        </div>`
      )
      .join('')}
  `;
}

function renderMobileNotifications() {
  const list = document.getElementById('notificationsListContainer');
  list.innerHTML = state.data.notifications
    .map(
      (n) => `
      <div class="info-card">
        <div class="info-card-top">
          <strong>${esc(n.title)}</strong>
          <span style="font-size:10.5px;color:#64748b;">${esc(n.time)}</span>
        </div>
        <div style="font-size:12px;color:#475569;">${esc(n.body)}</div>
      </div>`
    )
    .join('');
}

function renderMobileActiveView() {
  if (state.mobileView === 'home') return renderMobileHome();
  if (state.mobileView === 'categories') return renderMobileCategoryPage();
  if (state.mobileView === 'map') return renderMobileMap();
  if (state.mobileView === 'bookings') return renderMobileBookings();
  if (state.mobileView === 'profile') return renderMobileProfile();
  if (state.mobileView === 'subscription') return renderMobileSubscription();
  if (state.mobileView === 'prohub') return renderMobileProHub();
  return renderMobileNotifications();
}

/* ==================== MOBILE IN-FRAME SHEETS ==================== */
function openMobileSheet(html) {
  const backdrop = document.getElementById('modalBackdrop');
  const content = document.getElementById('modalContent');
  content.innerHTML = html;
  backdrop.classList.remove('hidden');
}

function closeMobileSheet() {
  document.getElementById('modalBackdrop').classList.add('hidden');
}

function openMobileTechDetailSheet(techId) {
  const t = getTechnicians().find((x) => (x.userId || x.id) === techId);
  if (!t) return;
  const cat = categoryMeta(t.categoryId);
  openMobileSheet(`
    <div class="modal-header">
      <h3>${esc(t.fullName)}</h3>
      <button type="button" class="modal-close-btn" id="closeMobileSheetBtn">&times;</button>
    </div>
    <p style="margin:0 0 6px;font-size:12px;color:#027245;font-weight:700;">${esc(cat.name)} &bull; ${esc(t.workingArea)}</p>
    <p style="margin:0 0 8px;font-size:12.5px;color:#334155;">${esc(t.expertise)}. ${esc(t.bio || '')}</p>
    <div style="display:flex;justify-content:space-between;font-size:12px;padding:8px 0;border-top:1px solid #f1f5f9;border-bottom:1px solid #f1f5f9;">
      <span>Visit Charge: <strong>${esc(t.visitCharge)}</strong></span>
      <span>Hours: <strong>${esc(t.availabilityHours)}</strong></span>
    </div>
    <div style="display:flex;gap:8px;margin-top:12px;">
      <a href="tel:${esc(String(t.contactPhone || '').replace(/\s+/g, ''))}" class="outline-full-btn" style="display:flex;align-items:center;justify-content:center;text-decoration:none;">Call</a>
      <button type="button" class="primary-full-btn" id="sheetBookNowBtn">Book Now</button>
    </div>
  `);
  document.getElementById('closeMobileSheetBtn')?.addEventListener('click', closeMobileSheet);
  document.getElementById('sheetBookNowBtn')?.addEventListener('click', () => openMobileBookingSheet(techId));
}

function openMobileBookingSheet(techId) {
  const t = getTechnicians().find((x) => (x.userId || x.id) === techId);
  if (!t) return;
  openMobileSheet(`
    <div class="modal-header">
      <h3>Book ${esc(t.fullName)}</h3>
      <button type="button" class="modal-close-btn" id="closeMobileSheetBtn">&times;</button>
    </div>
    <form id="mobileBookForm" style="display:flex;flex-direction:column;gap:8px;">
      <input type="text" id="mbTitle" class="field-input" placeholder="Service needed (e.g., Inverter setup)" required />
      <input type="text" id="mbArea" class="field-input" value="${esc(state.mobileLocation.name)}" required />
      <input type="text" id="mbTime" class="field-input" value="Today 5:00 PM" required />
      <button type="submit" class="primary-full-btn">Confirm Booking Request</button>
    </form>
  `);
  document.getElementById('closeMobileSheetBtn')?.addEventListener('click', closeMobileSheet);
  document.getElementById('mobileBookForm')?.addEventListener('submit', (e) => {
    e.preventDefault();
    state.data.bookings.unshift({
      id: `bk_${Date.now()}`,
      clientName: 'Saad Mahmood',
      clientPhone: '+92 301 5550112',
      technicianId: t.userId || t.id,
      technicianName: t.fullName,
      categoryId: t.categoryId,
      serviceTitle: document.getElementById('mbTitle').value.trim(),
      serviceArea: document.getElementById('mbArea').value.trim(),
      preferredTime: document.getElementById('mbTime').value.trim(),
      status: 'REQUESTED'
    });
    state.mobileMonthlyBookingsUsed = Math.min(5, state.mobileMonthlyBookingsUsed + 1);
    persistLocal();
    closeMobileSheet();
    showToast(`Booking sent to ${t.fullName}`);
    renderAll();
  });
}

function openMobileLocationSheet() {
  openMobileSheet(`
    <div class="modal-header">
      <h3>Select City / Area</h3>
      <button type="button" class="modal-close-btn" id="closeMobileSheetBtn">&times;</button>
    </div>
    <div style="display:flex;flex-direction:column;gap:8px;">
      ${CITIES.map(
        (c, i) => `<button type="button" class="outline-full-btn" data-pick-city="${i}">${esc(c.name)}</button>`
      ).join('')}
    </div>
  `);
  document.getElementById('closeMobileSheetBtn')?.addEventListener('click', closeMobileSheet);
  document.querySelectorAll('[data-pick-city]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.mobileLocation = CITIES[Number(btn.dataset.pickCity)] || CITIES[0];
      closeMobileSheet();
      renderMobileActiveView();
    })
  );
}

/* ==================== GLOBAL RENDER & BOOTSTRAP ==================== */
function renderAll() {
  renderAdminPane();
  renderMobileBottomNav();
  renderMobileActiveView();
}

function bindStaticListeners() {
  /* Auth & Topbar */
  document.getElementById('adminGoogleSignInBtn')?.addEventListener('click', handleGoogleSignIn);
  document.getElementById('adminSignOutBtn')?.addEventListener('click', () => signOutAdmin());
  document.getElementById('toggleMobileShowcaseBtn')?.addEventListener('click', () => {
    const pane = document.getElementById('mobileShowcasePane');
    const hidden = pane.classList.toggle('hidden');
    document.getElementById('toggleMobileShowcaseBtn').textContent = hidden ? 'Show Mobile App' : 'Hide Mobile App';
  });

  /* Viewport Switcher for narrow screens */
  document.querySelectorAll('.mode-switch-btn[data-portal-mode]').forEach((btn) =>
    btn.addEventListener('click', () => {
      const mode = btn.dataset.portalMode;
      document.getElementById('portalWorkspace').dataset.activePane = mode;
      document.querySelectorAll('.mode-switch-btn').forEach((x) => x.classList.toggle('active', x === btn));
    })
  );

  /* Admin Sidebar Navigation */
  document.querySelectorAll('.admin-nav-btn[data-admin-section]').forEach((btn) =>
    btn.addEventListener('click', () => {
      state.adminSection = btn.dataset.adminSection;
      renderAdminPane();
    })
  );

  document.getElementById('resetPreviewDataBtn')?.addEventListener('click', () => {
    localStorage.removeItem(STORAGE_KEY);
    state.data = makeInitialSeedData();
    persistLocal();
    showToast('Reset local demo cache');
    renderAll();
  });

  /* Modals Backdrop Close */
  document.getElementById('adminModalBackdrop')?.addEventListener('click', (e) => {
    if (e.target.id === 'adminModalBackdrop') closeAdminModal();
  });
  document.getElementById('modalBackdrop')?.addEventListener('click', (e) => {
    if (e.target.id === 'modalBackdrop') closeMobileSheet();
  });

  /* Mobile Preview Controls */
  document.getElementById('brandLogoTrigger')?.addEventListener('click', (e) => {
    e.preventDefault();
    navigateMobile('home');
  });
  document.getElementById('notificationBtn')?.addEventListener('click', () => navigateMobile('notifications'));
  document.getElementById('locationBtn')?.addEventListener('click', openMobileLocationSheet);
  document.getElementById('gpsBtn')?.addEventListener('click', () => {
    showToast(`GPS locked: ${state.mobileLocation.name}`);
  });
  document.getElementById('searchForm')?.addEventListener('submit', (e) => e.preventDefault());
  document.getElementById('searchInput')?.addEventListener('input', (e) => {
    state.mobileSearch = e.target.value;
    renderMobileHome();
  });
  document.getElementById('clearSearchBtn')?.addEventListener('click', () => {
    state.mobileSearch = '';
    document.getElementById('searchInput').value = '';
    renderMobileHome();
  });
  document.getElementById('resetSearchInlineBtn')?.addEventListener('click', () => {
    state.mobileSearch = '';
    document.getElementById('searchInput').value = '';
    renderMobileHome();
  });

  document.getElementById('catScrollLeftBtn')?.addEventListener('click', () => {
    document.getElementById('categoriesCarousel')?.scrollBy({ left: -170, behavior: 'smooth' });
  });
  document.getElementById('catScrollRightBtn')?.addEventListener('click', () => {
    document.getElementById('categoriesCarousel')?.scrollBy({ left: 170, behavior: 'smooth' });
  });

  document.getElementById('categoryBackBtn')?.addEventListener('click', () => navigateMobile('home'));
  document.getElementById('categoryGpsTopBtn')?.addEventListener('click', () => {
    showToast(`Sorted Near to Far (${state.mobileLocation.name})`);
  });
  document.getElementById('categorySearchInput')?.addEventListener('input', (e) => {
    state.categorySearch = e.target.value;
    renderMobileCategoryPage();
  });
  document.querySelectorAll('[data-mobile-nav]').forEach((btn) =>
    btn.addEventListener('click', () => navigateMobile(btn.dataset.mobileNav))
  );
  document.getElementById('mapGpsSyncBtn')?.addEventListener('click', () => {
    showToast('GPS pins refreshed');
    renderMobileMap();
  });

  /* Auto-slide Home & Category banners every 4 seconds (pauses on interaction) */
  setInterval(() => {
    if (Date.now() < state.bannerAutoPausedUntil) return;
    if (state.mobileView === 'home') {
      const slides = getActiveBannersForHome();
      if (slides.length > 1) {
        state.homeBannerIndex = (state.homeBannerIndex + 1) % slides.length;
        renderBannerCarousel(
          document.getElementById('homeBannerCard'),
          document.getElementById('homeBannerDots'),
          slides,
          'homeBannerIndex'
        );
      }
    } else if (state.mobileView === 'categories') {
      const slides = getActiveBannersForCategory(state.selectedCategoryId);
      if (slides.length > 1) {
        state.categoryBannerIndex = (state.categoryBannerIndex + 1) % slides.length;
        renderBannerCarousel(
          document.getElementById('categoryBannerCard'),
          document.getElementById('categoryBannerDots'),
          slides,
          'categoryBannerIndex'
        );
      }
    }
  }, 4000);
}

/* Boot */
bindStaticListeners();
renderAll();
initFirebaseAuth();
