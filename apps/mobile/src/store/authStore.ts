import { create } from 'zustand';

interface User {
  id: string;
  email: string;
  fullName: string;
  language: string;
  subscriptionTier: 'free' | 'grower' | 'pro';
  questionsThisMonth: number;
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  isOnboarded: boolean;
  setAuth: (user: User, token: string) => void;
  setOnboarded: () => void;
  clearAuth: () => void;
  updateUser: (updates: Partial<User>) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  isOnboarded: false,

  setAuth: (user, accessToken) => set({ user, accessToken }),
  setOnboarded: () => set({ isOnboarded: true }),
  clearAuth: () => set({ user: null, accessToken: null, isOnboarded: false }),
  updateUser: (updates) =>
    set((state) => ({
      user: state.user ? { ...state.user, ...updates } : null,
    })),
}));

// Exported for use outside React components (e.g., api.ts)
export async function getAuthToken(): Promise<string | null> {
  return useAuthStore.getState().accessToken;
}
