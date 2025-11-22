import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { authApi, clearTokens, UserProfile } from "@/lib/api";

type AuthContextType = {
  user: UserProfile | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (payload: {
    firstName: string;
    lastName: string;
    companyName: string;
    email: string;
    mobileNo: string;
    password: string;
  }) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const bootstrap = async () => {
      const hasToken =
        !!localStorage.getItem("auth.accessToken") ||
        !!localStorage.getItem("auth.refreshToken");
      if (!hasToken) {
        setLoading(false);
        return;
      }
      try {
        const profile = await authApi.currentUser();
        setUser(profile);
      } catch {
        clearTokens();
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    bootstrap();
  }, []);

  const login = async (email: string, password: string) => {
    const data = await authApi.login(email, password);
    setUser(data.user);
  };

  const register = async (payload: {
    firstName: string;
    lastName: string;
    companyName: string;
    email: string;
    mobileNo: string;
    password: string;
  }) => {
    const data = await authApi.register(payload);
    setUser(data.user);
  };

  const logout = async () => {
    await authApi.logout();
    setUser(null);
  };

  const value = useMemo(
    () => ({ user, loading, login, register, logout }),
    [loading, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
};
