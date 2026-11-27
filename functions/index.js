const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { initializeApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { getMessaging } = require('firebase-admin/messaging');

initializeApp();

const DATABASE_ID = 'ai-studio-android-fixnear-1e2833ef-438f-432a-bea1-b8878edb9181';
const db = getFirestore(DATABASE_ID);

function buildMessagePreview(msg) {
  if (!msg) return 'New message from SkillConnect Support';
  if (msg.messageType === 'voice') return 'Voice message from SkillConnect Support';
  if (msg.messageType === 'image') return msg.text ? `Image: ${msg.text}` : 'Sent an image';
  if (msg.messageType === 'document') return msg.attachmentName ? `PDF: ${msg.attachmentName}` : 'Sent a PDF document';
  return String(msg.text || 'New message').slice(0, 140);
}

/**
 * Sends a Firebase Cloud Messaging push notification to the conversation owner
 * whenever an authorized Support Admin posts a new message in support_messages.
 */
exports.notifyUserOnSupportAdminReply = onDocumentCreated(
  {
    document: 'support_messages/{messageId}',
    database: DATABASE_ID,
    region: 'us-central1',
  },
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const msg = snapshot.data();
    if (!msg || msg.senderRole !== 'Admin') return;

    const ownerUid = msg.conversationOwnerId;
    const conversationId = msg.conversationId;
    if (!ownerUid || !conversationId) return;

    const tokensSnap = await db
      .collection('users')
      .doc(ownerUid)
      .collection('fcm_tokens')
      .get();

    if (tokensSnap.empty) return;

    const tokens = tokensSnap.docs
      .map((doc) => doc.data()?.token)
      .filter((t) => typeof t === 'string' && t.length > 10);

    if (!tokens.length) return;

    const preview = buildMessagePreview(msg);
    const response = await getMessaging().sendEachForMulticast({
      tokens,
      notification: {
        title: 'SkillConnect Support Team',
        body: preview,
      },
      data: {
        type: 'support_reply',
        conversationId: String(conversationId),
        messageId: String(msg.messageId || event.params.messageId),
        senderName: String(msg.senderName || 'Support Team'),
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'skillconnect_support_replies',
          clickAction: 'FLUTTER_NOTIFICATION_CLICK',
        },
      },
    });

    // Clean up invalid / expired device tokens
    const deletePromises = [];
    response.responses.forEach((res, idx) => {
      if (!res.success) {
        const code = res.error?.code || '';
        if (
          code === 'messaging/registration-token-not-registered' ||
          code === 'messaging/invalid-registration-token'
        ) {
          const invalidToken = tokens[idx];
          const matchingDoc = tokensSnap.docs.find((d) => d.data()?.token === invalidToken);
          if (matchingDoc) {
            deletePromises.push(matchingDoc.ref.delete());
          }
        }
      }
    });

    if (deletePromises.length) {
      await Promise.allSettled(deletePromises);
    }
  }
);
