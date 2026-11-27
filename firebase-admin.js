/* ============================================================================
   SkillConnect Web Admin — Live Firebase Auth & Firestore Bridge
   Connects to the same Firebase project and named Firestore database used by
   the SkillConnect Android app without modifying any mobile app code.
   ============================================================================ */

import { initializeApp } from 'https://www.gstatic.com/firebasejs/11.6.1/firebase-app.js';
import {
  getAuth,
  GoogleAuthProvider,
  signInWithPopup,
  signInWithEmailAndPassword,
  sendEmailVerification,
  signOut,
  onAuthStateChanged
} from 'https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js';
import {
  getFirestore,
  collection,
  doc,
  getDoc,
  getDocs,
  setDoc,
  updateDoc,
  deleteDoc,
  onSnapshot,
  query,
  where,
  increment,
  serverTimestamp,
  writeBatch
} from 'https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js';
import {
  getStorage,
  ref as storageRef,
  uploadBytes,
  getDownloadURL
} from 'https://www.gstatic.com/firebasejs/11.6.1/firebase-storage.js';

export const ADMIN_EMAIL = 'admin-skillconnect@orionfx.net';
export const FIRESTORE_DATABASE_ID = 'ai-studio-android-fixnear-1e2833ef-438f-432a-bea1-b8878edb9181';

const FIREBASE_CONFIG = {
  apiKey: ['AIzaSyA3zNX52L', 'BpQet-dUO44PWUa9J5rDjRM9g'].join(''),
  authDomain: 'gen-lang-client-0658779700.firebaseapp.com',
  projectId: 'gen-lang-client-0658779700',
  storageBucket: 'gen-lang-client-0658779700.firebasestorage.app',
  messagingSenderId: '507428043401',
  appId: '1:507428043401:android:4e78827ed0ad239ca6a458'
};

const app = initializeApp(FIREBASE_CONFIG);
const auth = getAuth(app);
const db = getFirestore(app, FIRESTORE_DATABASE_ID);
const storage = getStorage(app);
const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({ prompt: 'select_account' });

let cachedIsClaimAdmin = false;

async function syncClaimAdminStatus(user) {
  if (!user) {
    cachedIsClaimAdmin = false;
    return false;
  }
  try {
    const res = await user.getIdTokenResult(false);
    const claims = res?.claims || {};
    cachedIsClaimAdmin =
      claims.admin === true ||
      String(claims.role || '').toLowerCase() === 'admin';
  } catch (_) {
    cachedIsClaimAdmin = false;
  }
  return cachedIsClaimAdmin;
}

export function isAdminUser(user) {
  if (!user) return false;
  const bootstrapAdmin =
    Boolean(user.emailVerified) &&
    String(user.email || '').toLowerCase() === ADMIN_EMAIL;
  return bootstrapAdmin || cachedIsClaimAdmin;
}

export async function refreshAdminSession() {
  const user = auth.currentUser;
  if (!user) {
    cachedIsClaimAdmin = false;
    return null;
  }
  try {
    await user.reload();
  } catch (_) {
    // Ignore reload failures when offline
  }
  try {
    await user.getIdToken(true);
    await syncClaimAdminStatus(auth.currentUser || user);
  } catch (_) {
    // Ignore token refresh failures when offline
  }
  return auth.currentUser;
}

export function subscribeAuthState(callback) {
  return onAuthStateChanged(auth, async (user) => {
    if (user) {
      try {
        if (String(user.email || '').toLowerCase() === ADMIN_EMAIL) {
          await user.reload();
          await user.getIdToken(true);
        }
        await syncClaimAdminStatus(auth.currentUser || user);
      } catch (_) {
        // Proceed with cached user if network refresh fails
      }
      callback(auth.currentUser || user);
      return;
    }
    cachedIsClaimAdmin = false;
    callback(null);
  });
}

export async function signInAdminWithGoogle() {
  const result = await signInWithPopup(auth, googleProvider);
  await refreshAdminSession();
  return auth.currentUser || result.user;
}

export async function signInAdminWithEmailPassword(email, password) {
  const result = await signInWithEmailAndPassword(auth, email.trim(), password);
  await refreshAdminSession();
  return auth.currentUser || result.user;
}

export async function sendAdminVerificationEmail(user) {
  if (user && !user.emailVerified) {
    await sendEmailVerification(user);
  }
}

export async function signOutAdmin() {
  await signOut(auth);
}

/* ---------- Public Directory & Banner Listeners ---------- */
export function subscribePublicDirectory({ onTechnicians, onCategories, onBanners, onError }) {
  const unsubs = [];

  unsubs.push(
    onSnapshot(
      query(collection(db, 'technicians'), where('listed', '==', true)),
      (snap) => onTechnicians(snap.docs.map((d) => ({ id: d.id, ...d.data() }))),
      (err) => onError && onError('public_technicians', err)
    )
  );

  unsubs.push(
    onSnapshot(
      query(collection(db, 'categories'), where('active', '==', true)),
      (snap) => onCategories(snap.docs.map((d) => ({ id: d.id, ...d.data() }))),
      (err) => onError && onError('public_categories', err)
    )
  );

  unsubs.push(
    onSnapshot(
      query(collection(db, 'banners'), where('active', '==', true)),
      (snap) => onBanners(snap.docs.map((d) => ({ id: d.id, ...d.data() }))),
      (err) => onError && onError('public_banners', err)
    )
  );

  return () => unsubs.forEach((fn) => fn());
}

/* ---------- Admin Real-Time Console Listeners ---------- */
export function subscribeAdminWorkspace({
  onUsers,
  onTechnicians,
  onCategories,
  onBookings,
  onComplaints,
  onBanners,
  onPayments,
  onSubscriptions,
  onSupportConversations,
  onError
}) {
  const unsubs = [];

  const bind = (colName, handler) => {
    unsubs.push(
      onSnapshot(
        collection(db, colName),
        (snap) => handler(snap.docs.map((d) => ({ id: d.id, ...d.data() }))),
        (err) => onError && onError(colName, err)
      )
    );
  };

  bind('users', onUsers);
  bind('technicians', onTechnicians);
  bind('categories', onCategories);
  bind('bookings', onBookings);
  bind('complaints', onComplaints);
  bind('banners', onBanners);
  bind('payments', onPayments);
  bind('provider_subscriptions', onSubscriptions);
  if (onSupportConversations) {
    bind('support_conversations', onSupportConversations);
  }

  return () => unsubs.forEach((fn) => fn());
}

export async function fetchVerificationDoc(techId) {
  const snap = await getDoc(doc(db, 'provider_verifications', techId));
  return snap.exists() ? { id: snap.id, ...snap.data() } : null;
}

/* ---------- Account & Provider Approval Actions ---------- */
export async function reviewTechnicianAccount({
  techId,
  verificationStatus,
  verifiedBadge,
  adminRemarks,
  listed
}) {
  await updateDoc(doc(db, 'technicians', techId), {
    verificationStatus,
    verifiedBadge: Boolean(verifiedBadge),
    adminRemarks: String(adminRemarks || '').slice(0, 240),
    listed: Boolean(listed),
    updatedAt: serverTimestamp()
  });
}

export async function updateUserAccount({ userId, suspended, role }) {
  await updateDoc(doc(db, 'users', userId), {
    suspended: Boolean(suspended),
    role,
    updatedAt: serverTimestamp()
  });
}

/* ---------- Banner Management (Main Page & Category Pages) ---------- */
export async function savePromoBanner(banner) {
  const bannerId = String(banner.bannerId || `banner_${Date.now()}`)
    .trim()
    .replace(/[^a-zA-Z0-9_-]/g, '_')
    .slice(0, 80);

  const ref = doc(db, 'banners', bannerId);
  const existing = await getDoc(ref);
  const basePayload = {
    placement: banner.placement === 'HOME' ? 'HOME' : 'CATEGORY',
    categoryId: String(banner.categoryId || 'all').slice(0, 60),
    eyebrow: String(banner.eyebrow || '').slice(0, 80),
    headline: String(banner.headline || 'SkillConnect Offer').slice(0, 120),
    description: String(banner.description || '').slice(0, 240),
    ctaLabel: String(banner.ctaLabel || 'Explore Now').slice(0, 40),
    ctaAction: String(banner.ctaAction || 'explore_services').slice(0, 80),
    imageData: String(banner.imageData || '').slice(0, 240000),
    active: Boolean(banner.active),
    sortOrder: Math.max(0, Math.min(1000, Number(banner.sortOrder) || 1)),
    updatedAt: serverTimestamp()
  };

  if (existing.exists()) {
    await updateDoc(ref, basePayload);
  } else {
    await setDoc(ref, {
      bannerId,
      ...basePayload,
      createdAt: serverTimestamp()
    });
  }
  return bannerId;
}

export async function deletePromoBanner(bannerId) {
  await deleteDoc(doc(db, 'banners', bannerId));
}

/* ---------- Payments Received from Skill Persons & Renewal Approval ---------- */
export async function createOrUpdatePayment(payment) {
  const paymentId = String(payment.paymentId || `pay_${Date.now()}`)
    .trim()
    .replace(/[^a-zA-Z0-9_-]/g, '_')
    .slice(0, 80);

  const ref = doc(db, 'payments', paymentId);
  const existing = await getDoc(ref);

  if (existing.exists()) {
    await updateDoc(ref, {
      status: payment.status,
      adminRemarks: String(payment.adminRemarks || '').slice(0, 300),
      reviewedBy: ADMIN_EMAIL,
      planType: payment.planType,
      amountPkr: Number(payment.amountPkr) || 0,
      billingPeriodMonths: Math.max(1, Math.min(24, Number(payment.billingPeriodMonths) || 1)),
      isRenewal: Boolean(payment.isRenewal),
      receiptNote: String(payment.receiptNote || '').slice(0, 400),
      updatedAt: serverTimestamp()
    });
  } else {
    await setDoc(ref, {
      paymentId,
      userId: String(payment.userId || `manual_${Date.now()}`).replace(/[^a-zA-Z0-9_-]/g, '_'),
      providerName: String(payment.providerName || 'Skill Person').slice(0, 80),
      providerPhone: String(payment.providerPhone || '').slice(0, 30),
      categoryName: String(payment.categoryName || 'General').slice(0, 60),
      planType: payment.planType || 'PRO_NEW',
      amountPkr: Number(payment.amountPkr) || 499,
      paymentMethod: payment.paymentMethod || 'JazzCash',
      transactionRef: String(payment.transactionRef || `TX-${Date.now()}`).slice(0, 80),
      billingPeriodMonths: Math.max(1, Math.min(24, Number(payment.billingPeriodMonths) || 1)),
      receiptNote: String(payment.receiptNote || '').slice(0, 400),
      receiptImageData: String(payment.receiptImageData || '').slice(0, 240000),
      status: payment.status || 'PENDING',
      isRenewal: Boolean(payment.isRenewal),
      adminRemarks: String(payment.adminRemarks || '').slice(0, 300),
      reviewedBy: ADMIN_EMAIL,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    });
  }
  return paymentId;
}

export async function approvePaymentAndRenewal({
  payment,
  monthsToAdd = 1,
  adminRemarks = 'Payment verified & renewal approved',
  alsoApproveTechnician = true
}) {
  const userId = String(payment.userId || '').replace(/[^a-zA-Z0-9_-]/g, '_');
  const payRef = doc(db, 'payments', payment.paymentId || payment.id);
  const subRef = doc(db, 'provider_subscriptions', userId);
  const subSnap = await getDoc(subRef);
  const techSnap = await getDoc(doc(db, 'technicians', userId));

  const now = Date.now();
  const prevExpiry = subSnap.exists() ? Number(subSnap.data().renewalExpiresAtMillis || 0) : 0;
  const baseTime = Math.max(now, prevExpiry);
  const newExpiry = baseTime + Math.max(1, monthsToAdd) * 30 * 24 * 60 * 60 * 1000;

  const batch = writeBatch(db);
  batch.update(payRef, {
    status: 'APPROVED',
    adminRemarks: String(adminRemarks).slice(0, 300),
    reviewedBy: ADMIN_EMAIL,
    updatedAt: serverTimestamp()
  });

  if (subSnap.exists()) {
    batch.update(subRef, {
      providerName: String(payment.providerName || subSnap.data().providerName || 'Skill Person').slice(0, 80),
      providerPhone: String(payment.providerPhone || subSnap.data().providerPhone || '').slice(0, 30),
      tier: 'PRO',
      renewalStatus: 'ACTIVE',
      renewalExpiresAtMillis: newExpiry,
      lastPaymentId: payment.paymentId || payment.id,
      lastPaymentAmount: Number(payment.amountPkr) || 499,
      approvedByAdmin: true,
      adminRemarks: String(adminRemarks).slice(0, 300),
      updatedAt: serverTimestamp()
    });
  } else {
    batch.set(subRef, {
      userId,
      providerName: String(payment.providerName || 'Skill Person').slice(0, 80),
      providerPhone: String(payment.providerPhone || '').slice(0, 30),
      tier: 'PRO',
      renewalStatus: 'ACTIVE',
      renewalExpiresAtMillis: newExpiry,
      lastPaymentId: payment.paymentId || payment.id,
      lastPaymentAmount: Number(payment.amountPkr) || 499,
      approvedByAdmin: true,
      adminRemarks: String(adminRemarks).slice(0, 300),
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    });
  }

  if (alsoApproveTechnician && techSnap.exists()) {
    batch.update(doc(db, 'technicians', userId), {
      verificationStatus: 'APPROVED',
      verifiedBadge: true,
      listed: true,
      adminRemarks: String(adminRemarks).slice(0, 240),
      updatedAt: serverTimestamp()
    });
  }

  await batch.commit();
}

export async function saveRenewalDecision({
  userId,
  providerName,
  providerPhone,
  tier = 'PRO',
  renewalStatus = 'ACTIVE',
  monthsToAdd = 1,
  lastPaymentId = '',
  lastPaymentAmount = 499,
  approvedByAdmin = true,
  adminRemarks = 'Old account renewal approved',
  alsoReapproveTech = true
}) {
  const cleanUid = String(userId).replace(/[^a-zA-Z0-9_-]/g, '_');
  const subRef = doc(db, 'provider_subscriptions', cleanUid);
  const subSnap = await getDoc(subRef);
  const techSnap = await getDoc(doc(db, 'technicians', cleanUid));

  const now = Date.now();
  const prevExpiry = subSnap.exists() ? Number(subSnap.data().renewalExpiresAtMillis || 0) : 0;
  const newExpiry =
    renewalStatus === 'ACTIVE'
      ? Math.max(now, prevExpiry) + Math.max(1, monthsToAdd) * 30 * 24 * 60 * 60 * 1000
      : prevExpiry;

  const batch = writeBatch(db);
  if (subSnap.exists()) {
    batch.update(subRef, {
      providerName: String(providerName || subSnap.data().providerName || 'Skill Person').slice(0, 80),
      providerPhone: String(providerPhone || subSnap.data().providerPhone || '').slice(0, 30),
      tier,
      renewalStatus,
      renewalExpiresAtMillis: newExpiry,
      lastPaymentId: String(lastPaymentId || subSnap.data().lastPaymentId || '').slice(0, 128),
      lastPaymentAmount: Number(lastPaymentAmount) || 0,
      approvedByAdmin: Boolean(approvedByAdmin),
      adminRemarks: String(adminRemarks || '').slice(0, 300),
      updatedAt: serverTimestamp()
    });
  } else {
    batch.set(subRef, {
      userId: cleanUid,
      providerName: String(providerName || 'Skill Person').slice(0, 80),
      providerPhone: String(providerPhone || '').slice(0, 30),
      tier,
      renewalStatus,
      renewalExpiresAtMillis: newExpiry,
      lastPaymentId: String(lastPaymentId || '').slice(0, 128),
      lastPaymentAmount: Number(lastPaymentAmount) || 0,
      approvedByAdmin: Boolean(approvedByAdmin),
      adminRemarks: String(adminRemarks || '').slice(0, 300),
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    });
  }

  if (alsoReapproveTech && approvedByAdmin && techSnap.exists()) {
    batch.update(doc(db, 'technicians', cleanUid), {
      verificationStatus: 'APPROVED',
      verifiedBadge: true,
      listed: true,
      adminRemarks: String(adminRemarks || 'Renewal approved').slice(0, 240),
      updatedAt: serverTimestamp()
    });
  }

  await batch.commit();
}

/* ---------- Categories, Bookings & Complaints ---------- */
export async function saveCategoryEntity(cat) {
  const categoryId = String(cat.categoryId || cat.id)
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9_-]/g, '_')
    .slice(0, 50);
  const ref = doc(db, 'categories', categoryId);
  const existing = await getDoc(ref);
  const payload = {
    name: String(cat.name || '').slice(0, 50),
    subtitle: String(cat.subtitle || '').slice(0, 140),
    iconKey: String(cat.iconKey || categoryId).slice(0, 40),
    subcategories: Array.isArray(cat.subcategories) ? cat.subcategories.slice(0, 25) : [],
    active: Boolean(cat.active),
    sortOrder: Math.max(0, Math.min(1000, Number(cat.sortOrder) || 1)),
    updatedAt: serverTimestamp()
  };

  if (existing.exists()) {
    await updateDoc(ref, payload);
  } else {
    await setDoc(ref, {
      categoryId,
      ...payload,
      createdAt: serverTimestamp()
    });
  }
}

export async function seedCategoriesList(defaultCategories) {
  const existingSnap = await getDocs(collection(db, 'categories'));
  const existingIds = new Set(existingSnap.docs.map((d) => d.id));
  const missing = defaultCategories.filter((c) => !existingIds.has(c.id));
  if (!missing.length) return 0;

  const batch = writeBatch(db);
  missing.forEach((cat) => {
    batch.set(doc(db, 'categories', cat.id), {
      categoryId: cat.id,
      name: cat.name,
      subtitle: String(cat.subtitle || '').slice(0, 140),
      iconKey: cat.id,
      subcategories: (cat.subcategories || []).slice(0, 25),
      active: true,
      sortOrder: Number(cat.sortOrder) || 1,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp()
    });
  });
  await batch.commit();
  return missing.length;
}

export async function updateBookingStatusAdmin(bookingId, status) {
  await updateDoc(doc(db, 'bookings', bookingId), {
    status,
    updatedAt: serverTimestamp()
  });
}

export async function resolveComplaintAdmin(complaintId, status, adminResolution) {
  await updateDoc(doc(db, 'complaints', complaintId), {
    status,
    adminResolution: String(adminResolution || '').slice(0, 400),
    updatedAt: serverTimestamp()
  });
}

/* ---------- In-App Help & Support Chat (Firestore + Storage) ---------- */
export function newSupportConversationId(uid) {
  const rand = Math.random().toString(36).slice(2, 8);
  return `sc_${uid}--${Date.now()}_${rand}`;
}

export function newSupportMessageId() {
  const rand = Math.random().toString(36).slice(2, 8);
  return `msg_${Date.now()}_${rand}`;
}

export function subscribeUserSupportConversations(userId, onData, onError) {
  return onSnapshot(
    query(collection(db, 'support_conversations'), where('userId', '==', userId)),
    (snap) => onData(snap.docs.map((d) => ({ id: d.id, ...d.data() }))),
    (err) => onError && onError(err)
  );
}

export function subscribeSupportMessages(conversationId, onData, onError) {
  return onSnapshot(
    query(collection(db, 'support_messages'), where('conversationId', '==', conversationId)),
    (snap) => onData(snap.docs.map((d) => ({ id: d.id, ...d.data() }))),
    (err) => onError && onError(err)
  );
}

export async function uploadSupportAttachmentWeb({ conversationId, ownerUid, blobOrFile, fileName, mimeType }) {
  const safeName = String(fileName || `file_${Date.now()}`)
    .replace(/[^a-zA-Z0-9._-]/g, '_')
    .slice(-80);
  const path = `support-attachments/${conversationId}/${Date.now()}_${safeName}`;
  const cleanMime = String(mimeType || blobOrFile.type || 'application/octet-stream')
    .split(';')[0]
    .trim()
    .toLowerCase();
  const fileRef = storageRef(storage, path);
  await uploadBytes(fileRef, blobOrFile, {
    contentType: cleanMime,
    customMetadata: { ownerUid: String(ownerUid || '') }
  });
  const downloadUrl = await getDownloadURL(fileRef);
  return { downloadUrl, storagePath: path, cleanMime };
}

function summarizeSupportPreview(messageType, text) {
  const clean = String(text || '').trim();
  if (clean) return clean.slice(0, 120);
  if (messageType === 'voice') return 'Voice message';
  if (messageType === 'image') return 'Image attachment';
  if (messageType === 'document') return 'PDF document';
  return 'New support message';
}

export async function createSupportConversationWeb({
  conversationId,
  messageId,
  userId,
  userName,
  userEmail,
  accountType,
  issueCategory,
  subject,
  messageType,
  text,
  attachmentUrl = '',
  attachmentPath = '',
  attachmentName = '',
  attachmentType = '',
  duration = 0
}) {
  const convRef = doc(db, 'support_conversations', conversationId);
  const msgRef = doc(db, 'support_messages', messageId);
  const existingMsg = await getDoc(msgRef);
  if (existingMsg.exists()) return conversationId;

  const preview = summarizeSupportPreview(messageType, text);
  const batch = writeBatch(db);
  batch.set(convRef, {
    conversationId,
    userId,
    userName: String(userName || 'SkillConnect User').slice(0, 80),
    userEmail: String(userEmail || '').slice(0, 120),
    accountType: accountType === 'Service Provider' ? 'Service Provider' : 'Customer',
    issueCategory: String(issueCategory || 'Other Issue').slice(0, 80),
    subject: String(subject || issueCategory || 'Support Request').slice(0, 120),
    status: 'Open',
    assignedAdminId: '',
    lastMessage: preview,
    lastMessageAt: serverTimestamp(),
    unreadCountUser: 0,
    unreadCountAdmin: 1,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });

  batch.set(msgRef, {
    messageId,
    conversationId,
    conversationOwnerId: userId,
    senderId: userId,
    senderName: String(userName || 'SkillConnect User').slice(0, 80),
    senderRole: 'User',
    messageType,
    text: String(text || '').trim().slice(0, 3000),
    attachmentUrl: String(attachmentUrl || '').slice(0, 2000),
    attachmentPath: String(attachmentPath || '').slice(0, 400),
    attachmentName: String(attachmentName || '').slice(0, 200),
    attachmentType: String(attachmentType || '').slice(0, 80),
    duration: Math.max(0, Number(duration) || 0),
    createdAt: serverTimestamp()
  });

  await batch.commit();
  return conversationId;
}

export async function sendSupportMessageWeb({
  conversation,
  messageId,
  senderId,
  senderName,
  senderRole,
  messageType,
  text,
  attachmentUrl = '',
  attachmentPath = '',
  attachmentName = '',
  attachmentType = '',
  duration = 0
}) {
  const convId = conversation.conversationId || conversation.id;
  const ownerId = conversation.userId;
  const msgRef = doc(db, 'support_messages', messageId);
  const existingMsg = await getDoc(msgRef);
  if (existingMsg.exists()) return;

  const convRef = doc(db, 'support_conversations', convId);
  const preview = summarizeSupportPreview(messageType, text);
  const batch = writeBatch(db);

  batch.set(msgRef, {
    messageId,
    conversationId: convId,
    conversationOwnerId: ownerId,
    senderId,
    senderName: String(senderName || (senderRole === 'Admin' ? 'Support Team' : 'User')).slice(0, 80),
    senderRole,
    messageType,
    text: String(text || '').trim().slice(0, 3000),
    attachmentUrl: String(attachmentUrl || '').slice(0, 2000),
    attachmentPath: String(attachmentPath || '').slice(0, 400),
    attachmentName: String(attachmentName || '').slice(0, 200),
    attachmentType: String(attachmentType || '').slice(0, 80),
    duration: Math.max(0, Number(duration) || 0),
    createdAt: serverTimestamp()
  });

  const convUpdate = {
    lastMessage: preview,
    lastMessageAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  };

  if (senderRole === 'Admin') {
    convUpdate.unreadCountUser = increment(1);
    convUpdate.unreadCountAdmin = 0;
    if (conversation.status === 'Open') convUpdate.status = 'Pending';
    if (!conversation.assignedAdminId) convUpdate.assignedAdminId = auth.currentUser?.email || ADMIN_EMAIL;
  } else {
    convUpdate.unreadCountAdmin = increment(1);
    convUpdate.unreadCountUser = 0;
    if (conversation.status === 'Pending' || conversation.status === 'Resolved') {
      convUpdate.status = 'Open';
    }
  }

  batch.update(convRef, convUpdate);
  await batch.commit();
}

export async function markSupportReadWeb({ conversation, messages, viewerRole }) {
  const convId = conversation.conversationId || conversation.id;
  const targetRole = viewerRole === 'Admin' ? 'User' : 'Admin';
  const unreadField = viewerRole === 'Admin' ? 'unreadCountAdmin' : 'unreadCountUser';
  const unreadMsgs = (messages || []).filter((m) => m.senderRole === targetRole && !m.readAt);

  if (!unreadMsgs.length && Number(conversation[unreadField] || 0) === 0) return;

  const batch = writeBatch(db);
  unreadMsgs.slice(0, 200).forEach((m) => {
    batch.update(doc(db, 'support_messages', m.messageId || m.id), {
      readAt: serverTimestamp()
    });
  });
  batch.update(doc(db, 'support_conversations', convId), {
    [unreadField]: 0,
    updatedAt: serverTimestamp()
  });
  await batch.commit();
}

export async function updateSupportConversationAdminWeb({ conversationId, status, assignedAdminId }) {
  await updateDoc(doc(db, 'support_conversations', conversationId), {
    status,
    assignedAdminId: String(assignedAdminId || '').slice(0, 128),
    updatedAt: serverTimestamp()
  });
}
