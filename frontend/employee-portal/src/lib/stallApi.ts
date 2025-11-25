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
  totalStalls?: number;
  availableStalls?: number;
  createdAt?: string;
  updatedAt?: string;
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

export interface CreateEventPayload {
  year: number;
  name: string;
  startDate: string;
  endDate: string;
  location: string;
  status?: EventStatus;
}

export interface UpdateEventPayload extends Partial<CreateEventPayload> {}

export interface CreateStallPayload {
  eventId: string;
  stallCode: string;
  sizeCategory: StallSizeCategory;
  price: number;
  locationX?: number;
  locationY?: number;
}

export interface UpdateStallPayload {
  eventId: string;
  stallCode: string;
  sizeCategory: StallSizeCategory;
  price: number;
  locationX?: number;
  locationY?: number;
}

const unwrap = async <T>(promise: Promise<ApiResponse<T>>) => {
  const res = await promise;
  return res.data;
};

export const stallApi = {
  listEvents: () => unwrap(apiFetch<Event[]>("/api/events")),
  getEvent: (id: string) => unwrap(apiFetch<Event>(`/api/events/${id}`)),
  listEventsByYear: (year: number) => unwrap(apiFetch<Event[]>(`/api/events/year/${year}`)),
  listEventsByStatus: (status: EventStatus) =>
    unwrap(apiFetch<Event[]>(`/api/events/status/${status}`)),
  createEvent: (payload: CreateEventPayload) =>
    unwrap(apiFetch<Event>("/api/events", { method: "POST", body: JSON.stringify(payload) })),
  updateEvent: (id: string, payload: UpdateEventPayload) =>
    unwrap(apiFetch<Event>(`/api/events/${id}`, { method: "PUT", body: JSON.stringify(payload) })),
  updateEventStatus: (id: string, status: EventStatus) =>
    unwrap(
      apiFetch<Event>(`/api/events/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
      }),
    ),
  deleteEvent: (id: string) => unwrap(apiFetch<void>(`/api/events/${id}`, { method: "DELETE" })),

  listStalls: () => unwrap(apiFetch<Stall[]>("/api/stalls")),
  getStall: (id: string) => unwrap(apiFetch<Stall>(`/api/stalls/${id}`)),
  listStallsByEvent: (eventId: string) =>
    unwrap(apiFetch<Stall[]>(`/api/stalls/event/${eventId}`)),
  listAvailableStallsByEvent: (eventId: string) =>
    unwrap(apiFetch<Stall[]>(`/api/stalls/event/${eventId}/available`)),
  listAvailableStallsBySize: (eventId: string, size: StallSizeCategory) =>
    unwrap(apiFetch<Stall[]>(`/api/stalls/event/${eventId}/available/size/${size}`)),
  listStallsByVendor: (vendorId: string) =>
    unwrap(apiFetch<Stall[]>(`/api/stalls/vendor/${vendorId}`)),
  createStall: (payload: CreateStallPayload) =>
    unwrap(apiFetch<Stall>("/api/stalls", { method: "POST", body: JSON.stringify(payload) })),
  generateLayout: (eventId: string) =>
    unwrap(
      apiFetch<Stall[]>("/api/stalls/layout", {
        method: "POST",
        body: JSON.stringify({ eventId }),
      }),
    ),
  updateStall: (id: string, payload: UpdateStallPayload) =>
    unwrap(apiFetch<Stall>(`/api/stalls/${id}`, { method: "PUT", body: JSON.stringify(payload) })),
  reserveStall: (id: string, vendorId: string) =>
    unwrap(apiFetch<Stall>(`/api/stalls/${id}/reserve?vendorId=${encodeURIComponent(vendorId)}`, { method: "POST" })),
  unreserveStall: (id: string) =>
    unwrap(apiFetch<Stall>(`/api/stalls/${id}/unreserve`, { method: "POST" })),
  deleteStall: (id: string) => unwrap(apiFetch<void>(`/api/stalls/${id}`, { method: "DELETE" })),
};
