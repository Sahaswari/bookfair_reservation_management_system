import { apiFetch } from "./api";

export type ReservationStatus = "PENDING" | "CONFIRMED" | "CANCELLED";

export interface Reservation {
  id: string;
  userId: string;
  stallId: string;
  eventId: string;
  reservationDate: string;
  status: ReservationStatus;
  confirmationCode?: string;
  qrCodeUrl?: string;
  createdAt: string;
  updatedAt: string;
  userFirstName?: string;
  userLastName?: string;
  userEmail?: string;
  stallCode?: string;
  sizeCategory?: string;
  price?: number;
  locationX?: number;
  locationY?: number;
  stallReserved?: boolean;
  stallReservedBy?: string;
}

export interface CreateReservationPayload {
  userId: string;
  stallId: string;
  eventId: string;
}

export const reservationApi = {
  async createReservation(payload: CreateReservationPayload) {
    const res = await apiFetch<Reservation>("/api/reservations", {
      method: "POST",
      body: JSON.stringify(payload),
    });
    return res.data;
  },
};
