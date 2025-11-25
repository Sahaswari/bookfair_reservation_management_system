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

export const reservationApi = {
  getAllReservations: () => unwrap(apiFetch<Reservation[]>("/api/reservations")),
  getReservation: (id: string) => unwrap(apiFetch<Reservation>(`/api/reservations/${id}`)),
  updateStatus: (id: string, status: ReservationStatus) =>
    unwrap(
      apiFetch<Reservation>(`/api/reservations/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({ status }),
      }),
    ),
  getReservationsByStatus: (status: ReservationStatus) =>
    unwrap(apiFetch<Reservation[]>(`/api/reservations/status/${status}`)),
};
