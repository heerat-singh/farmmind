import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, ScrollView } from 'react-native';
import { NativeStackScreenProps } from '@react-navigation/native-stack';
import { RootStackParamList } from '../../navigation/RootNavigator';
import { useAuthStore } from '../../store/authStore';
import { api, ApiError } from '../../services/api';

type Props = NativeStackScreenProps<RootStackParamList, 'Register'>;

const LANGUAGES = [
  { code: 'en', label: 'English' },
  { code: 'pa', label: 'ਪੰਜਾਬੀ' },
  { code: 'hi', label: 'हिंदी' },
];

export default function RegisterScreen({ navigation }: Props) {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [language, setLanguage] = useState('en');
  const [loading, setLoading] = useState(false);
  const { setAuth } = useAuthStore();

  const isValid = fullName.trim() && email.trim() && password.length >= 8;

  const handleRegister = async () => {
    if (!isValid) return;
    setLoading(true);
    try {
      const res = await api.post<{ accessToken: string; user: any }>('/api/v1/auth/register', {
        fullName,
        email,
        password,
        language,
      });
      setAuth(res.user, res.accessToken);
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Registration failed. Please try again.';
      Alert.alert('Registration Failed', msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Create Account</Text>

      <TextInput
        style={styles.input}
        placeholder="Full Name"
        value={fullName}
        onChangeText={setFullName}
        autoCapitalize="words"
      />
      <TextInput
        style={styles.input}
        placeholder="Email"
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
        autoCapitalize="none"
        autoCorrect={false}
      />
      <TextInput
        style={styles.input}
        placeholder="Password (min 8 characters)"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
      />

      <Text style={styles.label}>Language</Text>
      <View style={styles.languageRow}>
        {LANGUAGES.map((lang) => (
          <TouchableOpacity
            key={lang.code}
            testID={`language-${lang.code}`}
            style={[styles.langBtn, language === lang.code && styles.langBtnActive]}
            onPress={() => setLanguage(lang.code)}
          >
            <Text style={[styles.langText, language === lang.code && styles.langTextActive]}>
              {lang.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      <TouchableOpacity
        style={[styles.button, (!isValid || loading) && styles.buttonDisabled]}
        onPress={handleRegister}
        disabled={!isValid || loading}
      >
        {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Create Account</Text>}
      </TouchableOpacity>

      <TouchableOpacity onPress={() => navigation.navigate('Login')}>
        <Text style={styles.link}>Already have an account? Log In</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flexGrow: 1, justifyContent: 'center', padding: 24, backgroundColor: '#fff' },
  title: { fontSize: 28, fontWeight: 'bold', color: '#2d6a4f', textAlign: 'center', marginBottom: 32 },
  input: { borderWidth: 1, borderColor: '#d1d5db', borderRadius: 8, padding: 14, marginBottom: 16, fontSize: 16 },
  label: { fontSize: 14, color: '#374151', marginBottom: 8, fontWeight: '500' },
  languageRow: { flexDirection: 'row', gap: 8, marginBottom: 24 },
  langBtn: { flex: 1, padding: 10, borderRadius: 8, borderWidth: 1, borderColor: '#d1d5db', alignItems: 'center' },
  langBtnActive: { borderColor: '#2d6a4f', backgroundColor: '#f0fdf4' },
  langText: { color: '#374151', fontSize: 14 },
  langTextActive: { color: '#2d6a4f', fontWeight: '600' },
  button: { backgroundColor: '#2d6a4f', padding: 16, borderRadius: 8, alignItems: 'center', marginBottom: 16 },
  buttonDisabled: { opacity: 0.6 },
  buttonText: { color: '#fff', fontSize: 16, fontWeight: '600' },
  link: { color: '#2d6a4f', textAlign: 'center', fontSize: 14 },
});
