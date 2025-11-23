const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";
const ACCESS_TOKEN_KEY = "auth.accessToken";
const REFRESH_TOKEN_KEY = "auth.refreshToken";
const ACCESS_EXPIRES_AT_KEY = "auth.accessExpiresAt";

type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
};

type AuthTokens = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number; // milliseconds
};

export type UserProfile = {
  id: string;
  firstName: string;
  lastName: string;
  companyName: string;
  email: string;
  mobileNo: string;
  role: string;
  status: string;
  createdAt: string;
  updatedAt: string;
};

const getStoredTokens = () => {
  const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
  const accessExpiresAt = Number(localStorage.getItem(ACCESS_EXPIRES_AT_KEY));
  return { accessToken, refreshToken, accessExpiresAt };
};

const setTokens = (tokens: AuthTokens) => {
  const expiresAt = Date.now() + tokens.expiresIn - 5_000; // small buffer
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
  localStorage.setItem(ACCESS_EXPIRES_AT_KEY, expiresAt.toString());
};

export const clearTokens = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(ACCESS_EXPIRES_AT_KEY);
};

let refreshPromise: Promise<string | null> | null = null;

const refreshAccessToken = async (): Promise<string | null> => {
  const { refreshToken } = getStoredTokens();
  if (!refreshToken) return null;

  if (!refreshPromise) {
    refreshPromise = fetch(`${API_BASE_URL}/api/auth/refresh-token`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    })
      .then(async (res) => {
        if (!res.ok) throw new Error("Refresh failed");
        const json = (await res.json()) as ApiResponse<{ tokens: AuthTokens }>;
        setTokens(json.data.tokens);
        return json.data.tokens.accessToken;
      })
      .catch(() => {
        clearTokens();
        return null;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
};

const getValidAccessToken = async (): Promise<string | null> => {
  const { accessToken, accessExpiresAt } = getStoredTokens();
  if (accessToken && accessExpiresAt && Date.now() < accessExpiresAt) {
    return accessToken;
  }
  return refreshAccessToken();
};

type ApiFetchOptions = RequestInit & { skipAuth?: boolean; retry?: boolean };

export const apiFetch = async <T>(
  path: string,
  options: ApiFetchOptions = {},
): Promise<ApiResponse<T>> => {
  const { skipAuth, retry, headers, ...rest } = options;
  const mergedHeaders = new Headers(headers || {});

  if (!skipAuth) {
    const token = await getValidAccessToken();
    if (token) {
      mergedHeaders.set("Authorization", `Bearer ${token}`);
    }
  }

  if (!mergedHeaders.has("Content-Type") && rest.body) {
    mergedHeaders.set("Content-Type", "application/json");
  }
  mergedHeaders.set("Accept", "application/json");

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...rest,
    headers: mergedHeaders,
  });

  if (response.status === 401 && !retry && !skipAuth) {
    const newToken = await refreshAccessToken();
    if (newToken) {
      return apiFetch<T>(path, { ...options, retry: true });
    }
    throw new Error("Unauthorized");
  }

  const json = (await response.json()) as ApiResponse<T>;
  if (!response.ok || json.success === false) {
    throw new Error(json.message || "Request failed");
  }
  return json;
};

export const authApi = {
  async login(email: string, password: string) {
    const res = await apiFetch<{ user: UserProfile; tokens: AuthTokens }>(
      "/api/auth/login",
      {
        method: "POST",
        skipAuth: true,
        body: JSON.stringify({ email, password }),
      },
    );
    setTokens(res.data.tokens);
    return res.data;
  },

  async register(payload: {
    firstName: string;
    lastName: string;
    companyName: string;
    email: string;
    mobileNo: string;
    password: string;
    role?: string;
  }) {
    const res = await apiFetch<{ user: UserProfile; tokens: AuthTokens }>(
      "/api/auth/register",
      {
        method: "POST",
        skipAuth: true,
        body: JSON.stringify(payload),
      },
    );
    setTokens(res.data.tokens);
    return res.data;
  },

  async logout() {
    try {
      await apiFetch<null>("/api/auth/logout", { method: "POST" });
    } catch {
      // ignore logout errors to keep UX smooth
    } finally {
      clearTokens();
    }
  },

  async currentUser() {
    const res = await apiFetch<UserProfile>("/api/users/me", { method: "GET" });
    return res.data;
  },
};
