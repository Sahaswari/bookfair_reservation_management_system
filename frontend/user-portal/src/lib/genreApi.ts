import { apiFetch, unwrapResponse } from "./api";

export type Genre = {
  id: string;
  code: string;
  name: string;
  description?: string;
  displayOrder?: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
};

type CreateGenrePayload = {
  name: string;
  description?: string;
  userId: string;
  displayOrder?: number;
};

const buildRequest = (payload: CreateGenrePayload) => {
  const sanitizedName = payload.name.trim();
  return {
    code: sanitizedName
      .toUpperCase()
      .replace(/[^A-Z0-9]+/g, "_")
      .replace(/^_+|_+$/g, "")
      .slice(0, 50),
    name: sanitizedName,
    description: payload.description?.trim() || undefined,
    displayOrder: payload.displayOrder ?? 0,
    isActive: true,
    userId: payload.userId,
  };
};

export const genreApi = {
  listAll: async (): Promise<Genre[]> => {
    const res = await apiFetch<Genre[]>("/api/genres", { method: "GET", rawResponse: true });
    return unwrapResponse(res);
  },
  listByUser: async (userId: string): Promise<Genre[]> => {
    const res = await apiFetch<Genre[]>(`/api/genres/user/${userId}`, {
      method: "GET",
      rawResponse: true,
    });
    return unwrapResponse(res);
  },
  create: async (payload: CreateGenrePayload): Promise<Genre> => {
    const res = await apiFetch<Genre>("/api/genres", {
      method: "POST",
      rawResponse: true,
      body: JSON.stringify(buildRequest(payload)),
    });
    return unwrapResponse(res);
  },
  remove: async (id: string): Promise<void> => {
    await apiFetch<void>(`/api/genres/${id}`, { method: "DELETE", rawResponse: true });
  },
};
