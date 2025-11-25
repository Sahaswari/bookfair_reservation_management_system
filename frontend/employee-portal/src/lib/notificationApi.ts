import { apiFetch } from "./api";

export type NotificationChannel = "EMAIL" | "SMS" | "IN_APP";

export interface CreateNotificationPayload {
  userId: string;
  reservationId?: string;
  channel?: NotificationChannel;
  subject?: string;
  message: string;
  templateCode?: string;
  metadata?: Record<string, unknown>;
}

export const notificationApi = {
  async createNotification<T = unknown>(payload: CreateNotificationPayload) {
    const response = await apiFetch<T>("/api/notifications", {
      method: "POST",
      body: JSON.stringify({
        channel: payload.channel ?? "EMAIL",
        ...payload,
      }),
    });

    return response.data;
  },
};
