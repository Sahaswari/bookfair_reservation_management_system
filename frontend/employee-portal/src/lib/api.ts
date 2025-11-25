const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const ACCESS_TOKEN_KEY = "employee.accessToken";
const REFRESH_TOKEN_KEY = "employee.refreshToken";
const ACCESS_EXPIRES_AT_KEY = "employee.accessExpiresAt";
const EMPLOYEE_EMAIL_KEY = "employeeEmail";
const EMPLOYEE_FLAG_KEY = "employeeUser";

export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
};

export type AuthTokens = {
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

const extractFirstErrorMessage = (data: unknown): string | null => {
  if (!data) return null;
  if (typeof data === "string") return data;
  if (Array.isArray(data)) {
    for (const value of data) {
      const nested = extractFirstErrorMessage(value);
      if (nested) return nested;
    }
  }
  if (typeof data === "object") {
    for (const value of Object.values(data as Record<string, unknown>)) {
      const nested = extractFirstErrorMessage(value);
      if (nested) return nested;
    }
  }
  return null;
};

const setTokens = (tokens: AuthTokens) => {
  const expiresAt = Date.now() + tokens.expiresIn - 5_000; // small buffer before expiry
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
  localStorage.setItem(ACCESS_EXPIRES_AT_KEY, expiresAt.toString());
};

export const clearSession = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(ACCESS_EXPIRES_AT_KEY);
  localStorage.removeItem(EMPLOYEE_EMAIL_KEY);
  localStorage.removeItem(EMPLOYEE_FLAG_KEY);
};

let refreshPromise: Promise<string | null> | null = null;

const refreshAccessToken = async (): Promise<string | null> => {
  const { refreshToken } = getStoredTokens();
  if (!refreshToken) return null;

  if (!refreshPromise) {
    refreshPromise = fetch(`${API_BASE_URL}/api/auth/refresh-token`, {
      method: "POST",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify({ refreshToken }),
    })
      .then(async (res) => {
        if (!res.ok) throw new Error("Refresh failed");
        const json = (await res.json()) as ApiResponse<{ tokens: AuthTokens }>;
        setTokens(json.data.tokens);
        return json.data.tokens.accessToken;
      })
      .catch(() => {
        clearSession();
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

  const rawBody = await response.text();
  let parsedBody: ApiResponse<T> | null = null;
  if (rawBody) {
    try {
      parsedBody = JSON.parse(rawBody) as ApiResponse<T>;
    } catch (err) {
      // Non-JSON body; leave parsedBody as null so we surface raw text below.
    }
  }

  if (!response.ok || parsedBody?.success === false) {
    const detailedMessage =
      extractFirstErrorMessage(parsedBody?.data) ||
      parsedBody?.message ||
      rawBody ||
      response.statusText ||
      "Request failed";
    throw new Error(detailedMessage);
  }

  if (!parsedBody) {
    throw new Error("Unexpected empty server response");
  }

  return parsedBody;
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
    localStorage.setItem(EMPLOYEE_EMAIL_KEY, res.data.user.email);
    localStorage.setItem(EMPLOYEE_FLAG_KEY, "true");
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
    localStorage.setItem(EMPLOYEE_EMAIL_KEY, res.data.user.email);
    localStorage.setItem(EMPLOYEE_FLAG_KEY, "true");
    return res.data;
  },

  async logout() {
    try {
      await apiFetch<null>("/api/auth/logout", { method: "POST" });
    } catch {
      // ignore logout errors
    } finally {
      clearSession();
    }
  },
};

export const getEmployeeEmail = () => localStorage.getItem(EMPLOYEE_EMAIL_KEY);
export const hasEmployeeSession = () => !!localStorage.getItem(ACCESS_TOKEN_KEY);
