import React, { useState, useRef } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, FlatList,
  StyleSheet, KeyboardAvoidingView, Platform, ActivityIndicator,
} from 'react-native';
import { useAuthStore } from '../../store/authStore';

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  createdAt: Date;
}

const FREE_TIER_LIMIT = 10;

export default function ChatScreen() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const flatListRef = useRef<FlatList>(null);
  const { user } = useAuthStore();

  const questionsRemaining = user
    ? Math.max(0, FREE_TIER_LIMIT - (user.questionsThisMonth ?? 0))
    : FREE_TIER_LIMIT;

  const showPaywall = user?.subscriptionTier === 'free' && questionsRemaining === 0;

  const sendMessage = async () => {
    if (!input.trim() || isLoading) return;
    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: input.trim(),
      createdAt: new Date(),
    };
    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setIsLoading(true);

    try {
      // TODO: wire to api-gateway via SSE
      // Placeholder response for skeleton
      await new Promise((r) => setTimeout(r, 1000));
      const aiMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: 'AI response coming soon — backend integration in progress.',
        createdAt: new Date(),
      };
      setMessages((prev) => [...prev, aiMsg]);
    } finally {
      setIsLoading(false);
    }
  };

  const renderMessage = ({ item }: { item: ChatMessage }) => (
    <View
      testID={item.role === 'assistant' ? `ai-message-${messages.filter(m => m.role === 'assistant').indexOf(item)}` : undefined}
      style={[styles.bubble, item.role === 'user' ? styles.userBubble : styles.aiBubble]}
    >
      <Text style={[styles.bubbleText, item.role === 'user' && styles.userBubbleText]}>
        {item.content}
      </Text>
    </View>
  );

  return (
    <KeyboardAvoidingView
      testID="chat-screen"
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
    >
      {user?.subscriptionTier === 'free' && (
        <View style={styles.usageBanner}>
          <Text style={styles.usageText}>{questionsRemaining} questions left this month</Text>
        </View>
      )}

      <FlatList
        ref={flatListRef}
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={renderMessage}
        contentContainerStyle={styles.messageList}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd()}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <Text style={styles.emptyTitle}>Ask FarmMind anything</Text>
            <Text style={styles.emptySubtitle}>
              "Why are my tomato leaves curling?"{'\n'}
              "When should I plant corn in Ontario?"{'\n'}
              "What's causing spots on my plants?"
            </Text>
          </View>
        }
      />

      {isLoading && (
        <View style={styles.thinkingRow}>
          <ActivityIndicator size="small" color="#2d6a4f" />
          <Text style={styles.thinkingText}>FarmMind is thinking...</Text>
        </View>
      )}

      {showPaywall ? (
        <View testID="paywall-modal" style={styles.paywall}>
          <Text style={styles.paywallText}>Monthly limit reached</Text>
          <TouchableOpacity style={styles.upgradeButton}>
            <Text style={styles.upgradeButtonText}>Upgrade for $19/month</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <View style={styles.inputRow}>
          <TextInput
            testID="message-input"
            style={styles.input}
            placeholder="Ask a farming question..."
            value={input}
            onChangeText={setInput}
            multiline
            maxLength={1000}
          />
          <TouchableOpacity
            testID="send-button"
            style={[styles.sendButton, (!input.trim() || isLoading) && styles.sendButtonDisabled]}
            onPress={sendMessage}
            disabled={!input.trim() || isLoading}
          >
            <Text style={styles.sendButtonText}>→</Text>
          </TouchableOpacity>
        </View>
      )}
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f9fafb' },
  usageBanner: { backgroundColor: '#fef3c7', padding: 8, alignItems: 'center' },
  usageText: { color: '#92400e', fontSize: 12 },
  messageList: { padding: 16, flexGrow: 1 },
  bubble: { maxWidth: '80%', padding: 12, borderRadius: 16, marginBottom: 12 },
  userBubble: { alignSelf: 'flex-end', backgroundColor: '#2d6a4f', borderBottomRightRadius: 4 },
  aiBubble: { alignSelf: 'flex-start', backgroundColor: '#fff', borderBottomLeftRadius: 4, shadowColor: '#000', shadowOpacity: 0.05, shadowRadius: 4, elevation: 2 },
  bubbleText: { fontSize: 15, color: '#111827', lineHeight: 22 },
  userBubbleText: { color: '#fff' },
  emptyState: { flex: 1, justifyContent: 'center', alignItems: 'center', paddingTop: 80 },
  emptyTitle: { fontSize: 20, fontWeight: '600', color: '#2d6a4f', marginBottom: 16 },
  emptySubtitle: { fontSize: 14, color: '#6b7280', textAlign: 'center', lineHeight: 24 },
  thinkingRow: { flexDirection: 'row', alignItems: 'center', gap: 8, padding: 12, paddingHorizontal: 16 },
  thinkingText: { color: '#6b7280', fontSize: 13 },
  inputRow: { flexDirection: 'row', padding: 12, gap: 8, backgroundColor: '#fff', borderTopWidth: 1, borderTopColor: '#e5e7eb' },
  input: { flex: 1, borderWidth: 1, borderColor: '#d1d5db', borderRadius: 20, paddingHorizontal: 16, paddingVertical: 10, fontSize: 15, maxHeight: 100 },
  sendButton: { width: 44, height: 44, borderRadius: 22, backgroundColor: '#2d6a4f', justifyContent: 'center', alignItems: 'center', alignSelf: 'flex-end' },
  sendButtonDisabled: { opacity: 0.5 },
  sendButtonText: { color: '#fff', fontSize: 20 },
  paywall: { padding: 20, backgroundColor: '#fff', borderTopWidth: 1, borderTopColor: '#e5e7eb', alignItems: 'center' },
  paywallText: { fontSize: 16, fontWeight: '600', color: '#111827', marginBottom: 12 },
  upgradeButton: { backgroundColor: '#2d6a4f', paddingHorizontal: 24, paddingVertical: 12, borderRadius: 8 },
  upgradeButtonText: { color: '#fff', fontWeight: '600' },
});
