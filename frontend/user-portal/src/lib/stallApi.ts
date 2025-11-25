import { apiFetch, type ApiResponse } from "./api";

export type EventStatus = "UPCOMING" | "ONGOING" | "ENDED";

export interface Event {
  id: string;
  year: number;
  name: string;
  startDate: string;
  endDate: string;
  location: string;
  status: EventStatus;
}

export type StallSizeCategory = "SMALL" | "MEDIUM" | "LARGE";

export interface Stall {
  id: string;
  eventId: string;
  eventName?: string;
  stallCode: string;
  sizeCategory: StallSizeCategory;
  price: number;
  locationX?: number;
  locationY?: number;
  isReserved: boolean;
  reservedBy?: string;
  reservedByName?: string;
  createdAt?: string;
  updatedAt?: string;
}

const unwrap = async <T>(promise: Promise<ApiResponse<T>>) => {
  const res = await promise;
  return res.data;
};

export const stallApi = {
  listEvents: () => unwrap(apiFetch<Event[]>("/api/events")),
  listEventsByStatus: (status: EventStatus) =>
    unwrap(apiFetch<Event[]>(`/api/events/status/${status}`)),
  listEventsByYear: (year: number) => unwrap(apiFetch<Event[]>(`/api/events/year/${year}`)),

  listAvailableStallsByEvent: (eventId: string) =>
    unwrap(apiFetch<Stall[]>(`/api/stalls/event/${eventId}/available`)),
  listAvailableStallsBySize: (eventId: string, size: StallSizeCategory) =>
    unwrap(apiFetch<Stall[]>(`/api/stalls/event/${eventId}/available/size/${size}`)),
  listStallsByVendor: (vendorId: string) =>
    unwrap(apiFetch<Stall[]>(`/api/stalls/vendor/${vendorId}`)),
  reserveStall: (stallId: string, vendorId: string) =>
    unwrap(apiFetch<Stall>(`/api/stalls/${stallId}/reserve?vendorId=${encodeURIComponent(vendorId)}`, { method: "POST" })),
  unreserveStall: (stallId: string) =>
    unwrap(apiFetch<Stall>(`/api/stalls/${stallId}/unreserve`, { method: "POST" })),
};
