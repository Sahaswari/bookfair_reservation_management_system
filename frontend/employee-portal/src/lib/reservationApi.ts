import { apiFetch, type ApiResponse } from "./api";

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
  createdAt?: string;
  updatedAt?: string;
  userFirstName?: string;
  userLastName?: string;
  userEmail?: string;
  stallCode?: string;
  sizeCategory?: string;
  price?: number;
  locationX?: number;
  locationY?: number;
}

const unwrap = async <T>(promise: Promise<ApiResponse<T>>) => {
  const res = await promise;
  return res.data;
};

export interface CreateReservationPayload {
  userId: string;
  stallId: string;
  eventId: string;
}

export const reservationApi = {
  getAllReservations: () => unwrap(apiFetch<Reservation[]>("/api/reservations")),
  getReservation: (id: string) => unwrap(apiFetch<Reservation>(`/api/reservations/${id}`)),
  getReservationsByStatus: (status: ReservationStatus) =>
    unwrap(apiFetch<Reservation[]>(`/api/reservations/status/${status}`)),
  getReservationsByEvent: (eventId: string) =>
    unwrap(apiFetch<Reservation[]>(`/api/reservations/event/${eventId}`)),
  getReservationsByUser: (userId: string) =>
    unwrap(apiFetch<Reservation[]>(`/api/reservations/user/${userId}`)),
  createReservation: (payload: CreateReservationPayload) =>
    unwrap(
      apiFetch<Reservation>("/api/reservations", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
    ),
  updateStatus: (id: string, status: ReservationStatus) =>
    unwrap(
      apiFetch<Reservation>(`/api/reservations/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({ status }),
      }),
    ),
  deleteReservation: (id: string) =>
    unwrap(apiFetch<string>(`/api/reservations/${id}`, { method: "DELETE" })),
};
