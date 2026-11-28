const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const CLIENT_UID = "client_alice_123";
const TECH_UID = "tech_bob_456";
const OTHER_UID = "stranger_eve_789";
const ADMIN_UID = "admin_super_001";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

function makeUserProfile(uid, name = "Alice", role = "CUSTOMER") {
  const ts = pastDate(10000);
  return {
    userId: uid,
    displayName: name,
    email: `${name.toLowerCase()}@example.com`,
    phoneNumber: "+1 555-0101",
    defaultArea: "Midtown West",
    latitude: 37.7749,
    longitude: -122.4194,
    role,
    enrolledTechnician: false,
    suspended: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

function makeTechnicianProfile(uid, name = "Bob Electrical") {
  const ts = pastDate(10000);
  return {
    userId: uid,
    fullName: name,
    email: "bob@example.com",
    gender: "Male",
    categoryId: "electrician",
    additionalCategoryIds: ["solar_technician"],
    subcategory: "Breaker Panels & Rewiring",
    professionalTitle: "Master Electrician & Solar Specialist",
    expertise: "Panel Upgrades, Wiring, Smart Switches",
    bio: "Licensed master electrician.",
    qualification: "DAE Diploma",
    yearsExperience: 8,
    contactPhone: "+1 555-0199",
    phoneVerified: false,
    visitCharge: "Rs. 500",
    hourlyCharge: "Rs. 800/hr",
    dailyCharge: "Rs. 3500/day",
    serviceChargeNote: "Quote confirmed after on-site check",
    workingDays: ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
    availabilityHours: "08:00 AM - 08:00 PM",
    emergencyAvailable: true,
    city: "Karachi",
    workingArea: "Gulshan-e-Iqbal",
    preferredAreas: ["Gulshan-e-Iqbal", "PECHS"],
    travelToCustomer: true,
    serviceRadiusKm: 15,
    availableNow: true,
    listed: true,
    termsAccepted: true,
    verificationConsent: true,
    verificationStatus: "PENDING",
    verifiedBadge: false,
    adminRemarks: "",
    profilePhotoData: "",
    portfolioImages: [],
    latitude: 37.7812,
    longitude: -122.4110,
    ratingSum: 0,
    reviewCount: 0,
    averageRating: 0.0,
    completedJobs: 0,
    locationUpdatedAt: ts,
    createdAt: ts,
    updatedAt: ts,
  };
}

function makeBooking(clientId, techId) {
  const ts = pastDate(5000);
  return {
    clientId,
    clientName: "Alice",
    clientPhone: "+1 555-0101",
    technicianId: techId,
    technicianName: "Bob Electrical",
    technicianPhone: "+1 555-0199",
    categoryId: "electrician",
    serviceTitle: "Breaker keeps tripping",
    issueDescription: "Kitchen circuit breaker trips whenever toaster and microwave run.",
    serviceArea: "420 Market St, Midtown",
    preferredTime: "Available Now (Urgent)",
    clientLat: 37.7749,
    clientLng: -122.4194,
    status: "REQUESTED",
    reviewed: false,
    participantIds: [clientId, techId],
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users can browse active categories, listed technicians, and approved reviews, but cannot read private data or write anything", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = pastDate(5000);

  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("technicians").doc(TECH_UID).set(makeTechnicianProfile(TECH_UID));
    await adminDb.collection("technicians").doc("unlisted_tech").set({
      ...makeTechnicianProfile("unlisted_tech", "Hidden Pro"),
      listed: false,
    });
    await adminDb.collection("categories").doc("electrician").set({
      categoryId: "electrician",
      name: "Electrician",
      subtitle: "Wiring & breakers",
      iconKey: "electrician",
      subcategories: ["Wiring"],
      active: true,
      sortOrder: 1,
      createdAt: ts,
      updatedAt: ts,
    });
    await adminDb.collection("reviews").doc("booking_pub").set({
      bookingId: "booking_pub",
      technicianId: TECH_UID,
      clientId: CLIENT_UID,
      clientName: "Alice",
      rating: 5,
      comment: "Prompt service.",
      highlights: ["On Time"],
      approved: true,
      createdAt: ts,
    });
    await adminDb.collection("provider_verifications").doc(TECH_UID).set({
      userId: TECH_UID,
      cnicNumber: "35202-1234567-1",
      cnicPhotoData: "",
      cnicBackPhotoData: "",
      certificatePhotoData: "",
      createdAt: ts,
      updatedAt: ts,
    });
  });

  // Public directory reads succeed
  await assertSucceeds(unauthDb.collection("technicians").where("listed", "==", true).get());
  await assertSucceeds(unauthDb.collection("technicians").doc(TECH_UID).get());
  await assertSucceeds(unauthDb.collection("categories").where("active", "==", true).get());
  await assertSucceeds(
    unauthDb.collection("reviews").where("technicianId", "==", TECH_UID).where("approved", "==", true).get()
  );

  // Private collections, unlisted pros, and ALL writes remain blocked
  await assertFails(unauthDb.collection("technicians").doc("unlisted_tech").get());
  await assertFails(unauthDb.collection("users").doc(CLIENT_UID).get());
  await assertFails(unauthDb.collection("provider_verifications").doc(TECH_UID).get());
  await assertFails(unauthDb.collection("bookings").where("participantIds", "array-contains", CLIENT_UID).get());
  await assertFails(unauthDb.collection("complaints").where("reporterId", "==", CLIENT_UID).get());
  await assertFails(unauthDb.collection("technicians").doc("rogue_tech").set(makeTechnicianProfile("rogue_tech")));
  await assertFails(unauthDb.collection("bookings").doc("rogue_booking").set(makeBooking("rogue", TECH_UID)));
});

test("Users cannot self-assign ADMIN role, can save favorite technicians, and email spoofing without email_verified is rejected", async () => {
  const clientDb = testEnv.authenticatedContext(CLIENT_UID).firestore();
  await assertFails(clientDb.collection("users").doc(CLIENT_UID).set(makeUserProfile(CLIENT_UID, "Alice", "ADMIN")));
  await assertSucceeds(clientDb.collection("users").doc(CLIENT_UID).set(makeUserProfile(CLIENT_UID, "Alice", "CUSTOMER")));
  await assertSucceeds(
    clientDb.collection("users").doc(CLIENT_UID).update({
      savedTechnicianIds: [TECH_UID],
      updatedAt: pastDate(1000),
    })
  );

  // Spoofed admin email with email_verified = false must fail admin-only category write
  const spoofAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: "admin-skillconnect@orionfx.net",
    email_verified: false,
  }).firestore();
  const ts = pastDate(2000);
  await assertFails(
    spoofAdminDb.collection("categories").doc("drone_repair").set({
      categoryId: "drone_repair",
      name: "Drone Repair",
      subtitle: "Gimbal and motor fixes",
      iconKey: "build",
      subcategories: ["Gimbal"],
      active: true,
      sortOrder: 25,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Separate technician test account (orionfxservis@gmail.com) must NOT have admin privileges
  const techTesterDb = testEnv.authenticatedContext("tech_tester_uid", {
    email: "orionfxservis@gmail.com",
    email_verified: true,
  }).firestore();
  await assertFails(
    techTesterDb.collection("categories").doc("drone_repair").set({
      categoryId: "drone_repair",
      name: "Drone Repair",
      subtitle: "Gimbal and motor fixes",
      iconKey: "build",
      subcategories: ["Gimbal"],
      active: true,
      sortOrder: 25,
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Provider enrollment blocks self-approval, isolates CNIC & certificate PII, and allows Super Admin approval & category management", async () => {
  const techDb = testEnv.authenticatedContext(TECH_UID).firestore();
  const clientDb = testEnv.authenticatedContext(CLIENT_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "admin-skillconnect@orionfx.net",
    email_verified: true,
  }).firestore();

  // Provider cannot self-set APPROVED or verifiedBadge=true
  const spoofedTech = makeTechnicianProfile(TECH_UID);
  spoofedTech.verificationStatus = "APPROVED";
  spoofedTech.verifiedBadge = true;
  await assertFails(techDb.collection("technicians").doc(TECH_UID).set(spoofedTech));

  // Valid PENDING enrollment succeeds
  await assertSucceeds(techDb.collection("technicians").doc(TECH_UID).set(makeTechnicianProfile(TECH_UID)));

  // CNIC & certificate verification doc is isolated from other customers, visible to owner & Super Admin
  const ts = pastDate(2000);
  await assertSucceeds(
    techDb.collection("provider_verifications").doc(TECH_UID).set({
      userId: TECH_UID,
      cnicNumber: "35202-1234567-1",
      cnicPhotoData: "data:image/jpeg;base64,front",
      cnicBackPhotoData: "data:image/jpeg;base64,back",
      certificatePhotoData: "data:image/jpeg;base64,cert",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(clientDb.collection("provider_verifications").doc(TECH_UID).get());
  await assertSucceeds(adminDb.collection("provider_verifications").doc(TECH_UID).get());

  // Provider cannot self-approve; Super Admin can approve & toggle verifiedBadge
  await assertFails(
    techDb.collection("technicians").doc(TECH_UID).update({
      verificationStatus: "APPROVED",
      verifiedBadge: true,
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    adminDb.collection("technicians").doc(TECH_UID).update({
      verificationStatus: "APPROVED",
      verifiedBadge: true,
      adminRemarks: "CNIC verified",
      updatedAt: pastDate(1000),
    })
  );

  // Super Admin manages categories & subcategories
  await assertSucceeds(
    adminDb.collection("categories").doc("solar_technician").set({
      categoryId: "solar_technician",
      name: "Solar Technician",
      subtitle: "Panels, MPPT inverters & batteries",
      iconKey: "solar",
      subcategories: ["Inverter Repair", "Battery Bank"],
      active: true,
      sortOrder: 7,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(clientDb.collection("categories").where("active", "==", true).get());
});

test("Booking, private chat, atomic review, and Super Admin complaint moderation work end-to-end", async () => {
  const clientDb = testEnv.authenticatedContext(CLIENT_UID).firestore();
  const techDb = testEnv.authenticatedContext(TECH_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "admin-skillconnect@orionfx.net",
    email_verified: true,
  }).firestore();

  await assertSucceeds(techDb.collection("technicians").doc(TECH_UID).set(makeTechnicianProfile(TECH_UID)));

  const bookingId = "booking_001";
  await assertSucceeds(clientDb.collection("bookings").doc(bookingId).set(makeBooking(CLIENT_UID, TECH_UID)));

  // Client files complaint; other user cannot read; Super Admin resolves
  const ts = pastDate(2000);
  await assertSucceeds(
    clientDb.collection("complaints").doc("comp_1").set({
      bookingId,
      reporterId: CLIENT_UID,
      reporterName: "Alice",
      reportedTechId: TECH_UID,
      reportedTechName: "Bob Electrical",
      reason: "Late Arrival",
      description: "Technician arrived 45 minutes past agreed window.",
      status: "OPEN",
      adminResolution: "",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(otherDb.collection("complaints").doc("comp_1").get());
  await assertSucceeds(
    adminDb.collection("complaints").doc("comp_1").update({
      status: "RESOLVED",
      adminResolution: "Issued warning to provider.",
      updatedAt: pastDate(1000),
    })
  );

  // Progress booking & submit atomic review
  await assertSucceeds(techDb.collection("bookings").doc(bookingId).update({ status: "ACCEPTED", updatedAt: pastDate(1500) }));
  await assertSucceeds(techDb.collection("bookings").doc(bookingId).update({ status: "IN_PROGRESS", updatedAt: pastDate(1300) }));

  const completeBatch = techDb.batch();
  completeBatch.update(techDb.collection("bookings").doc(bookingId), { status: "COMPLETED", updatedAt: pastDate(1200) });
  completeBatch.update(techDb.collection("technicians").doc(TECH_UID), {
    completedJobs: 1,
    lastCompletedBookingId: bookingId,
    updatedAt: pastDate(1200),
  });
  await assertSucceeds(completeBatch.commit());

  const reviewBatch = clientDb.batch();
  reviewBatch.set(clientDb.collection("reviews").doc(bookingId), {
    bookingId,
    technicianId: TECH_UID,
    clientId: CLIENT_UID,
    clientName: "Alice",
    rating: 4,
    comment: "Good technical fix, slightly late.",
    highlights: ["Clean Work"],
    approved: true,
    createdAt: pastDate(1000),
  });
  reviewBatch.update(clientDb.collection("bookings").doc(bookingId), { reviewed: true, updatedAt: pastDate(1000) });
  reviewBatch.update(clientDb.collection("technicians").doc(TECH_UID), {
    ratingSum: 4,
    reviewCount: 1,
    averageRating: 4.0,
    lastReviewBookingId: bookingId,
    updatedAt: pastDate(1000),
  });
  await assertSucceeds(reviewBatch.commit());
});

test("Admin manages Home/Category banners, verifies Skill Person payments, and approves old-account renewals while non-admins are blocked from self-approving", async () => {
  const techDb = testEnv.authenticatedContext(TECH_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "admin-skillconnect@orionfx.net",
    email_verified: true,
  }).firestore();
  const ts = pastDate(2000);

  // Non-admin cannot create banner; Admin can create HOME & CATEGORY banners; public can read active ones
  await assertFails(
    techDb.collection("banners").doc("b_home_1").set({
      bannerId: "b_home_1",
      placement: "HOME",
      categoryId: "all",
      eyebrow: "Promo",
      headline: "Rogue Banner",
      description: "Unauthorized",
      ctaLabel: "Go",
      ctaAction: "explore",
      imageData: "assets/images/mobile/img_hero_technician.jpg",
      active: true,
      sortOrder: 1,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    adminDb.collection("banners").doc("b_home_1").set({
      bannerId: "b_home_1",
      placement: "HOME",
      categoryId: "all",
      eyebrow: "Featured",
      headline: "Skilled Help. Just a Tap Away!",
      description: "Find verified professionals near your location",
      ctaLabel: "Explore Now",
      ctaAction: "explore",
      imageData: "assets/images/mobile/img_hero_technician.jpg",
      active: true,
      sortOrder: 1,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    adminDb.collection("banners").doc("b_cat_1").set({
      bannerId: "b_cat_1",
      placement: "CATEGORY",
      categoryId: "electrician",
      eyebrow: "Need a Home Repair?",
      headline: "Your Home, Our Professionals.",
      description: "Certified electricians ready near you.",
      ctaLabel: "Find a Professional",
      ctaAction: "find_pro",
      imageData: "assets/images/mobile/img_cat_slide_2.jpg",
      active: true,
      sortOrder: 1,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(otherDb.collection("banners").where("active", "==", true).get());

  // Skill Person submits PENDING renewal payment, cannot self-approve; other user cannot read; Admin approves payment + renewal
  await assertFails(
    techDb.collection("payments").doc("pay_self_approve").set({
      paymentId: "pay_self_approve",
      userId: TECH_UID,
      providerName: "Bob Electrical",
      providerPhone: "+923001234567",
      categoryName: "Electrician",
      planType: "PRO_RENEWAL",
      amountPkr: 499,
      paymentMethod: "JazzCash",
      transactionRef: "JC-998877",
      billingPeriodMonths: 1,
      receiptNote: "Renewal payment",
      receiptImageData: "",
      status: "APPROVED",
      isRenewal: true,
      adminRemarks: "",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    techDb.collection("payments").doc("pay_001").set({
      paymentId: "pay_001",
      userId: TECH_UID,
      providerName: "Bob Electrical",
      providerPhone: "+923001234567",
      categoryName: "Electrician",
      planType: "PRO_RENEWAL",
      amountPkr: 499,
      paymentMethod: "JazzCash",
      transactionRef: "JC-998877",
      billingPeriodMonths: 1,
      receiptNote: "Paid via JazzCash for monthly renewal",
      receiptImageData: "",
      status: "PENDING",
      isRenewal: true,
      adminRemarks: "",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(otherDb.collection("payments").doc("pay_001").get());

  await assertSucceeds(
    adminDb.collection("payments").doc("pay_001").update({
      status: "APPROVED",
      adminRemarks: "Verified JazzCash receipt",
      reviewedBy: "admin-skillconnect@orionfx.net",
      updatedAt: pastDate(1000),
    })
  );

  // Admin approves old-account renewal subscription; non-admin cannot self-approve PRO
  await assertFails(
    techDb.collection("provider_subscriptions").doc(TECH_UID).set({
      userId: TECH_UID,
      providerName: "Bob Electrical",
      providerPhone: "+923001234567",
      tier: "PRO",
      renewalStatus: "ACTIVE",
      renewalExpiresAtMillis: Date.now() + 86400000 * 30,
      lastPaymentId: "pay_001",
      lastPaymentAmount: 499,
      approvedByAdmin: true,
      adminRemarks: "",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    adminDb.collection("provider_subscriptions").doc(TECH_UID).set({
      userId: TECH_UID,
      providerName: "Bob Electrical",
      providerPhone: "+923001234567",
      tier: "PRO",
      renewalStatus: "ACTIVE",
      renewalExpiresAtMillis: Date.now() + 86400000 * 30,
      lastPaymentId: "pay_001",
      lastPaymentAmount: 499,
      approvedByAdmin: true,
      adminRemarks: "Old account renewal approved",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Verified admin-skillconnect@orionfx.net can list all 8 Admin Console collections while non-admin cannot list users", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "admin-skillconnect@orionfx.net",
    email_verified: true,
  }).firestore();
  const nonAdminDb = testEnv.authenticatedContext(CLIENT_UID, {
    email: "orionfxservis@gmail.com",
    email_verified: true,
  }).firestore();

  await assertFails(nonAdminDb.collection("users").get());
  await assertSucceeds(adminDb.collection("users").get());
  await assertSucceeds(adminDb.collection("technicians").get());
  await assertSucceeds(adminDb.collection("categories").get());
  await assertSucceeds(adminDb.collection("bookings").get());
  await assertSucceeds(adminDb.collection("complaints").get());
  await assertSucceeds(adminDb.collection("banners").get());
  await assertSucceeds(adminDb.collection("payments").get());
  await assertSucceeds(adminDb.collection("provider_subscriptions").get());
  await assertSucceeds(adminDb.collection("support_conversations").get());
  await assertSucceeds(adminDb.collection("support_messages").get());
});

test("In-App Help & Support isolates user tickets, blocks senderRole/assignedAdminId tampering, allows admin claim replies & status workflow, and validates readAt", async () => {
  const clientDb = testEnv.authenticatedContext(CLIENT_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const customClaimAdminDb = testEnv.authenticatedContext("custom_claim_admin", {
    admin: true,
    email_verified: true,
  }).firestore();
  const ts = pastDate(2000);
  const convId = `sc_${CLIENT_UID}--1700000001_ab12`;

  const validConv = {
    conversationId: convId,
    userId: CLIENT_UID,
    userName: "Alice",
    userEmail: "alice@example.com",
    accountType: "Customer",
    issueCategory: "Booking Problem",
    subject: "Electrician did not confirm time",
    status: "Open",
    assignedAdminId: "",
    lastMessage: "Please help with my booking.",
    lastMessageAt: ts,
    unreadCountUser: 0,
    unreadCountAdmin: 1,
    createdAt: ts,
    updatedAt: ts,
  };

  // User cannot spoof conversationId owned by someone else or pre-assign an admin
  await assertFails(
    clientDb.collection("support_conversations").doc(`sc_${OTHER_UID}--123`).set({
      ...validConv,
      conversationId: `sc_${OTHER_UID}--123`,
    })
  );
  await assertFails(
    clientDb.collection("support_conversations").doc(convId).set({
      ...validConv,
      assignedAdminId: "custom_claim_admin",
    })
  );

  // Valid atomic batch: create conversation + initial user text message
  const msg1Id = "msg_1700000001_u1";
  const createBatch = clientDb.batch();
  createBatch.set(clientDb.collection("support_conversations").doc(convId), validConv);
  createBatch.set(clientDb.collection("support_messages").doc(msg1Id), {
    messageId: msg1Id,
    conversationId: convId,
    conversationOwnerId: CLIENT_UID,
    senderId: CLIENT_UID,
    senderName: "Alice",
    senderRole: "User",
    messageType: "text",
    text: "Please help with my booking.",
    attachmentUrl: "",
    attachmentPath: "",
    attachmentName: "",
    attachmentType: "",
    duration: 0,
    createdAt: ts,
  });
  await assertSucceeds(createBatch.commit());

  // Other user cannot get or list Alice's conversation or messages
  await assertFails(otherDb.collection("support_conversations").doc(convId).get());
  await assertFails(otherDb.collection("support_messages").doc(msg1Id).get());
  await assertFails(
    otherDb.collection("support_messages").where("conversationId", "==", convId).where("conversationOwnerId", "==", CLIENT_UID).get()
  );

  // Alice can read & list her own conversation & messages
  await assertSucceeds(clientDb.collection("support_conversations").doc(convId).get());
  await assertSucceeds(
    clientDb.collection("support_conversations").where("userId", "==", CLIENT_UID).get()
  );
  await assertSucceeds(
    clientDb.collection("support_messages").where("conversationId", "==", convId).where("conversationOwnerId", "==", CLIENT_UID).get()
  );

  // Alice cannot impersonate Admin or assign admin or resolve her own ticket
  await assertFails(
    clientDb.collection("support_messages").doc("msg_spoof").set({
      messageId: "msg_spoof",
      conversationId: convId,
      conversationOwnerId: CLIENT_UID,
      senderId: CLIENT_UID,
      senderName: "Support",
      senderRole: "Admin",
      messageType: "text",
      text: "Fake admin reply",
      attachmentUrl: "",
      attachmentPath: "",
      attachmentName: "",
      attachmentType: "",
      duration: 0,
      createdAt: pastDate(1500),
    })
  );
  await assertFails(
    clientDb.collection("support_conversations").doc(convId).update({
      assignedAdminId: CLIENT_UID,
      updatedAt: pastDate(1500),
    })
  );
  await assertFails(
    clientDb.collection("support_conversations").doc(convId).update({
      status: "Resolved",
      updatedAt: pastDate(1500),
    })
  );

  // Alice can attach an image with valid storage path inside her conversation
  const msgImgId = "msg_1700000002_img";
  await assertSucceeds(
    clientDb.collection("support_messages").doc(msgImgId).set({
      messageId: msgImgId,
      conversationId: convId,
      conversationOwnerId: CLIENT_UID,
      senderId: CLIENT_UID,
      senderName: "Alice",
      senderRole: "User",
      messageType: "image",
      text: "Screenshot attached",
      attachmentUrl: "https://firebasestorage.googleapis.com/v0/b/demo/o/screenshot.jpg",
      attachmentPath: `support-attachments/${convId}/screenshot_1.jpg`,
      attachmentName: "screenshot_1.jpg",
      attachmentType: "image/jpeg",
      duration: 0,
      createdAt: pastDate(1400),
    })
  );

  // Admin with custom claim can read, reply as Admin, increment unreadCountUser, and set Pending/Resolved
  const adminReplyId = "msg_1700000003_adm";
  const adminBatch = customClaimAdminDb.batch();
  adminBatch.set(customClaimAdminDb.collection("support_messages").doc(adminReplyId), {
    messageId: adminReplyId,
    conversationId: convId,
    conversationOwnerId: CLIENT_UID,
    senderId: "custom_claim_admin",
    senderName: "Support Team",
    senderRole: "Admin",
    messageType: "text",
    text: "We checked with your technician and updated your slot.",
    attachmentUrl: "",
    attachmentPath: "",
    attachmentName: "",
    attachmentType: "",
    duration: 0,
    createdAt: pastDate(1000),
  });
  adminBatch.update(customClaimAdminDb.collection("support_conversations").doc(convId), {
    assignedAdminId: "custom_claim_admin",
    status: "Pending",
    lastMessage: "We checked with your technician and updated your slot.",
    lastMessageAt: pastDate(1000),
    unreadCountUser: 1,
    unreadCountAdmin: 0,
    updatedAt: pastDate(1000),
  });
  await assertSucceeds(adminBatch.commit());

  // Alice marks Admin reply as read and clears her unreadCountUser
  const readBatch = clientDb.batch();
  readBatch.update(clientDb.collection("support_messages").doc(adminReplyId), {
    readAt: pastDate(500),
  });
  readBatch.update(clientDb.collection("support_conversations").doc(convId), {
    unreadCountUser: 0,
    updatedAt: pastDate(500),
  });
  await assertSucceeds(readBatch.commit());

  // Non-existent message/conversation get check does not crash rules; existing doc still blocked for other users
  await assertSucceeds(clientDb.collection("support_messages").doc("msg_non_existent_retry_check").get());
  await assertFails(otherDb.collection("support_messages").doc(adminReplyId).get());

  // Alice sends a 2nd follow-up text message while ticket status is Pending (both keeping Pending and reopening Open must succeed)
  const msg2Id = "msg_1700000004_u2";
  const followUpBatch = clientDb.batch();
  followUpBatch.set(clientDb.collection("support_messages").doc(msg2Id), {
    messageId: msg2Id,
    conversationId: convId,
    conversationOwnerId: CLIENT_UID,
    senderId: CLIENT_UID,
    senderName: "Alice",
    senderRole: "User",
    messageType: "text",
    text: "Maybe it is happening on update?",
    attachmentUrl: "",
    attachmentPath: "",
    attachmentName: "",
    attachmentType: "",
    duration: 0,
    createdAt: pastDate(300),
  });
  followUpBatch.update(clientDb.collection("support_conversations").doc(convId), {
    status: "Pending",
    lastMessage: "Maybe it is happening on update?",
    lastMessageAt: pastDate(300),
    unreadCountAdmin: 1,
    unreadCountUser: 0,
    updatedAt: pastDate(300),
  });
  await assertSucceeds(followUpBatch.commit());

  // Alice sends a 3rd message as a Voice note in the same Pending conversation
  const msgVoiceId = "msg_1700000005_voice";
  const voiceBatch = clientDb.batch();
  voiceBatch.set(clientDb.collection("support_messages").doc(msgVoiceId), {
    messageId: msgVoiceId,
    conversationId: convId,
    conversationOwnerId: CLIENT_UID,
    senderId: CLIENT_UID,
    senderName: "Alice",
    senderRole: "User",
    messageType: "voice",
    text: "",
    attachmentUrl: "https://firebasestorage.googleapis.com/v0/b/demo/o/voice_1.m4a",
    attachmentPath: `support-attachments/${convId}/1700000005_voice.m4a`,
    attachmentName: "voice_1.m4a",
    attachmentType: "audio/mp4",
    duration: 6,
    createdAt: pastDate(200),
  });
  voiceBatch.update(clientDb.collection("support_conversations").doc(convId), {
    status: "Pending",
    lastMessage: "Voice message (6s)",
    lastMessageAt: pastDate(200),
    unreadCountAdmin: 2,
    unreadCountUser: 0,
    updatedAt: pastDate(200),
  });
  await assertSucceeds(voiceBatch.commit());

  // Nobody can delete support messages or conversations
  await assertFails(clientDb.collection("support_messages").doc(msg1Id).delete());
  await assertFails(customClaimAdminDb.collection("support_conversations").doc(convId).delete());
});

test("Admin manages Accounts expense heads and monthly expenses while non-admins are blocked", async () => {
  const clientDb = testEnv.authenticatedContext(CLIENT_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "admin-skillconnect@orionfx.net",
    email_verified: true,
  }).firestore();
  const ts = pastDate(2000);

  await assertFails(
    clientDb.collection("expense_heads").doc("head_office").set({
      headId: "head_office",
      name: "Office Rent",
      description: "Monthly co-working desk",
      active: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    adminDb.collection("expense_heads").doc("head_office").set({
      headId: "head_office",
      name: "Office Rent",
      description: "Monthly co-working desk",
      active: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertFails(clientDb.collection("expense_heads").get());
  await assertSucceeds(adminDb.collection("expense_heads").get());

  await assertFails(
    clientDb.collection("expenses").doc("exp_1").set({
      expenseId: "exp_1",
      headId: "head_office",
      headName: "Office Rent",
      title: "November Desk Rent",
      notes: "Paid via Bank Transfer",
      amountPkr: 15000,
      monthKey: "2026-11",
      expenseDate: "2026-11-05",
      paymentMethod: "Bank Transfer",
      createdBy: "admin-skillconnect@orionfx.net",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    adminDb.collection("expenses").doc("exp_1").set({
      expenseId: "exp_1",
      headId: "head_office",
      headName: "Office Rent",
      title: "November Desk Rent",
      notes: "Paid via Bank Transfer",
      amountPkr: 15000,
      monthKey: "2026-11",
      expenseDate: "2026-11-05",
      paymentMethod: "Bank Transfer",
      createdBy: "admin-skillconnect@orionfx.net",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(adminDb.collection("expenses").get());
  await assertFails(clientDb.collection("expenses").get());
  await assertSucceeds(adminDb.collection("expenses").doc("exp_1").delete());
});



