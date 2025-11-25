import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { useToast } from "@/components/ui/use-toast";
import { Info, Loader2, Plus, RefreshCw, Search, Trash2 } from "lucide-react";
import Header from "@/components/Header";
import {
  type Event,
  type EventStatus,
  type Stall,
  type StallSizeCategory,
  type UpdateStallPayload,
  stallApi,
} from "@/lib/stallApi";
import { reservationApi, type Reservation } from "@/lib/reservationApi";
import { userApi, type VendorSummary } from "@/lib/userApi";
import { useEmployeeAuth } from "@/hooks/useEmployeeAuth";

const sizeColors: Record<StallSizeCategory, string> = {
  SMALL: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300",
  MEDIUM: "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300",
  LARGE: "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300",
};

const statusVariants: Record<EventStatus, "secondary" | "default" | "outline"> = {
  UPCOMING: "secondary",
  ONGOING: "default",
  ENDED: "outline",
};

const stallStatusVariant = (isReserved: boolean) => (isReserved ? "secondary" : "default");

const defaultEventForm = {
  name: "",
  year: new Date().getFullYear(),
  startDate: "",
  endDate: "",
  location: "",
  status: "UPCOMING" as EventStatus,
};

const defaultStallForm = {
  stallCode: "",
  sizeCategory: "MEDIUM" as StallSizeCategory,
  price: 0,
  locationX: "",
  locationY: "",
};

export default function StallManagement() {
  const { isAuthenticated } = useEmployeeAuth();
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const [selectedEventId, setSelectedEventId] = useState<string | null>(null);
  const [selectedStallId, setSelectedStallId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [vendorId, setVendorId] = useState("");
  const [eventForm, setEventForm] = useState(defaultEventForm);
  const [stallForm, setStallForm] = useState(defaultStallForm);
  const [eventFilters, setEventFilters] = useState<{ year?: number; status?: EventStatus | "ALL" }>({
    year: undefined,
    status: "ALL",
  });
  const [editPrice, setEditPrice] = useState<number>(0);

  const eventsQuery = useQuery({
    queryKey: ["events", eventFilters],
    queryFn: () => {
      if (eventFilters.year) {
        return stallApi.listEventsByYear(eventFilters.year);
      }
      if (eventFilters.status && eventFilters.status !== "ALL") {
        return stallApi.listEventsByStatus(eventFilters.status);
      }
      return stallApi.listEvents();
    },
  });

  const events = eventsQuery.data || [];

  useEffect(() => {
    if (!selectedEventId && events.length > 0) {
      setSelectedEventId(events[0].id);
    } else if (selectedEventId && !events.find((e) => e.id === selectedEventId)) {
      setSelectedEventId(events[0]?.id ?? null);
    }
  }, [events, selectedEventId]);

  const stallsQuery = useQuery({
    queryKey: ["stalls", selectedEventId],
    queryFn: () => stallApi.listStallsByEvent(selectedEventId!),
    enabled: !!selectedEventId,
  });

  const availableStallsQuery = useQuery({
    queryKey: ["available-stalls", selectedEventId],
    queryFn: () => stallApi.listAvailableStallsByEvent(selectedEventId!),
    enabled: !!selectedEventId,
  });

  const selectedStallDetailsQuery = useQuery({
    queryKey: ["stall", selectedStallId],
    queryFn: () => stallApi.getStall(selectedStallId!),
    enabled: !!selectedStallId,
  });

  const reservationsByEventQuery = useQuery({
    queryKey: ["reservations", selectedEventId],
    queryFn: () => reservationApi.getReservationsByEvent(selectedEventId!),
    enabled: !!selectedEventId,
  });

  const vendorsQuery = useQuery({
    queryKey: ["vendors"],
    queryFn: () => userApi.listVendors(),
  });

  const stalls = stallsQuery.data || [];
  const availableStalls = availableStallsQuery.data || [];
  const reservationsForEvent = reservationsByEventQuery.data || [];
  const vendors = vendorsQuery.data || [];

  const filteredStalls = useMemo(() => {
    const normalized = searchQuery.trim().toLowerCase();
    if (!normalized) return stalls;

    return stalls.filter((stall) => {
      const vendorMatch =
        (stall.reservedBy ?? "").toLowerCase().includes(normalized) ||
        (stall.reservedByName ?? "").toLowerCase().includes(normalized);
      const codeMatch = stall.stallCode.toLowerCase().includes(normalized);
      return vendorMatch || codeMatch;
    });
  }, [stalls, searchQuery]);

  useEffect(() => {
    if (filteredStalls.length > 0 && (!selectedStallId || !filteredStalls.find((s) => s.id === selectedStallId))) {
      setSelectedStallId(filteredStalls[0].id);
    }
  }, [filteredStalls, selectedStallId]);

  const selectedStall =
    selectedStallDetailsQuery.data ?? filteredStalls.find((stall) => stall.id === selectedStallId) ?? null;

  const activeReservation: Reservation | null = useMemo(() => {
    if (!selectedStall) return null;
    return (
      reservationsForEvent.find(
        (reservation) => reservation.stallId === selectedStall.id && reservation.status !== "CANCELLED",
      ) ?? null
    );
  }, [reservationsForEvent, selectedStall?.id]);

  const resolveVendorLabel = (vendor?: VendorSummary | null) => {
    if (!vendor) return "";
    if (vendor.companyName) return vendor.companyName;
    const name = `${vendor.firstName ?? ""} ${vendor.lastName ?? ""}`.trim();
    return name || vendor.email || "Vendor";
  };

  const selectedVendorLabel = resolveVendorLabel(vendors.find((v) => v.id === vendorId) ?? null);

  useEffect(() => {
    if (selectedStall) {
      setEditPrice(selectedStall.price);
    }
  }, [selectedStall?.id, selectedStall?.price]);

  const createEventMutation = useMutation({
    mutationFn: stallApi.createEvent,
    onSuccess: (created) => {
      toast({ title: "Event created", description: created.name });
      queryClient.invalidateQueries({ queryKey: ["events"] });
      setEventForm(defaultEventForm);
      setSelectedEventId(created.id);
    },
    onError: () => toast({ title: "Could not create event", variant: "destructive" }),
  });

  const updateEventStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: EventStatus }) => stallApi.updateEventStatus(id, status),
    onSuccess: () => {
      toast({ title: "Event status updated" });
      queryClient.invalidateQueries({ queryKey: ["events"] });
    },
    onError: () => toast({ title: "Failed to update status", variant: "destructive" }),
  });

  const deleteEventMutation = useMutation({
    mutationFn: (id: string) => stallApi.deleteEvent(id),
    onSuccess: () => {
      toast({ title: "Event deleted" });
      queryClient.invalidateQueries({ queryKey: ["events"] });
      setSelectedEventId(null);
    },
    onError: () => toast({ title: "Failed to delete event", variant: "destructive" }),
  });

  const createStallMutation = useMutation({
    mutationFn: stallApi.createStall,
    onSuccess: (stall) => {
      toast({ title: "Stall created", description: `Stall ${stall.stallCode}` });
      queryClient.invalidateQueries({ queryKey: ["stalls"] });
      queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
      setStallForm(defaultStallForm);
      setSelectedStallId(stall.id);
    },
    onError: () => toast({ title: "Could not create stall", variant: "destructive" }),
  });

  const updateStallMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateStallPayload }) =>
      stallApi.updateStall(id, payload),
    onSuccess: (_, { id }) => {
      toast({ title: "Stall updated" });
      queryClient.invalidateQueries({ queryKey: ["stalls"] });
      queryClient.invalidateQueries({ queryKey: ["stall", id] });
    },
    onError: () => toast({ title: "Failed to update stall", variant: "destructive" }),
  });

  const generateLayoutMutation = useMutation({
    mutationFn: (eventId: string) => stallApi.generateLayout(eventId),
    onSuccess: () => {
      toast({ title: "Bulk stalls created" });
      queryClient.invalidateQueries({ queryKey: ["stalls"] });
      queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
    },
    onError: (error: Error) =>
      toast({ title: error.message || "Failed to create bulk stalls", variant: "destructive" }),
  });

  const reserveMutation = useMutation({
    mutationFn: ({ stallId, vendorId, eventId }: { stallId: string; vendorId: string; eventId: string }) =>
      reservationApi.createReservation({
        userId: vendorId,
        stallId,
        eventId,
      }),
    onSuccess: () => {
      toast({ title: "Stall reserved" });
      queryClient.invalidateQueries({ queryKey: ["stalls"] });
      queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
      queryClient.invalidateQueries({ queryKey: ["stall"] });
      queryClient.invalidateQueries({ queryKey: ["reservations"] });
      if (selectedEventId) {
        queryClient.invalidateQueries({ queryKey: ["reservations", selectedEventId] });
      }
      setVendorId("");
    },
    onError: () => toast({ title: "Reservation failed", variant: "destructive" }),
  });

  const unreserveMutation = useMutation({
    mutationFn: (reservationId: string) => reservationApi.updateStatus(reservationId, "CANCELLED"),
    onSuccess: () => {
      toast({ title: "Stall released" });
      queryClient.invalidateQueries({ queryKey: ["stalls"] });
      queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
      queryClient.invalidateQueries({ queryKey: ["stall"] });
      queryClient.invalidateQueries({ queryKey: ["reservations"] });
      if (selectedEventId) {
        queryClient.invalidateQueries({ queryKey: ["reservations", selectedEventId] });
      }
    },
    onError: () => toast({ title: "Could not release stall", variant: "destructive" }),
  });

  const deleteStallMutation = useMutation({
    mutationFn: (id: string) => stallApi.deleteStall(id),
    onSuccess: () => {
      toast({ title: "Stall deleted" });
      queryClient.invalidateQueries({ queryKey: ["stalls"] });
      queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
      queryClient.invalidateQueries({ queryKey: ["stall"] });
      setSelectedStallId(null);
    },
    onError: () => toast({ title: "Failed to delete stall", variant: "destructive" }),
  });

  const totalStalls = stalls.length;
  const availableCount = availableStalls.length || stalls.filter((s) => !s.isReserved).length;
  const reservedCount = totalStalls - availableCount;

  const selectedEvent = events.find((e) => e.id === selectedEventId) ?? null;

  const handleCreateEvent = () => {
    if (!eventForm.name || !eventForm.startDate || !eventForm.endDate || !eventForm.location) {
      toast({ title: "Fill all event fields", variant: "destructive" });
      return;
    }
    createEventMutation.mutate({
      ...eventForm,
      year: Number(eventForm.year),
    });
  };

  const handleCreateStall = () => {
    if (!selectedEventId) {
      toast({ title: "Select an event first", variant: "destructive" });
      return;
    }
    if (!stallForm.stallCode || !stallForm.price) {
      toast({ title: "Stall code and price are required", variant: "destructive" });
      return;
    }

    createStallMutation.mutate({
      eventId: selectedEventId,
      stallCode: stallForm.stallCode.trim(),
      sizeCategory: stallForm.sizeCategory,
      price: Number(stallForm.price),
      locationX: stallForm.locationX ? Number(stallForm.locationX) : undefined,
      locationY: stallForm.locationY ? Number(stallForm.locationY) : undefined,
    });
  };

  const handleGenerateLayout = () => {
    if (!selectedEventId) {
      toast({ title: "Select an event first", variant: "destructive" });
      return;
    }
    if (stalls.length > 0) {
      toast({ title: "Event already has stalls", description: "Clear existing stalls before bulk generation", variant: "destructive" });
      return;
    }
    generateLayoutMutation.mutate(selectedEventId);
  };

  const handleReserve = () => {
    if (!selectedStall) return;
    if (!vendorId.trim()) {
      toast({ title: "Select a vendor to reserve", variant: "destructive" });
      return;
    }
    reserveMutation.mutate({ stallId: selectedStall.id, vendorId: vendorId.trim(), eventId: selectedStall.eventId });
  };

  const handleUpdateStall = () => {
    if (!selectedStall) return;
    const normalizedPrice = Number(editPrice);
    if (!Number.isFinite(normalizedPrice) || normalizedPrice <= 0) {
      toast({ title: "Enter a valid price", variant: "destructive" });
      return;
    }

    updateStallMutation.mutate({
      id: selectedStall.id,
      payload: {
        eventId: selectedStall.eventId,
        stallCode: selectedStall.stallCode,
        sizeCategory: selectedStall.sizeCategory,
        price: normalizedPrice,
        locationX: typeof selectedStall.locationX === "number" ? selectedStall.locationX : undefined,
        locationY: typeof selectedStall.locationY === "number" ? selectedStall.locationY : undefined,
      },
    });
  };

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <div className="container py-8 space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-4xl font-bold mb-1">Stall Management</h1>
            <p className="text-muted-foreground">Manage events, stalls, and live reservations</p>
          </div>
          <div className="flex gap-2">
            <Dialog>
              <DialogTrigger asChild>
                <Button variant="outline" size="sm">
                  <Plus className="h-4 w-4" /> New Event
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Create Event</DialogTitle>
                </DialogHeader>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 py-2">
                  <div>
                    <Label>Name</Label>
                    <Input
                      value={eventForm.name}
                      onChange={(e) => setEventForm({ ...eventForm, name: e.target.value })}
                      placeholder="Colombo International Book Fair"
                    />
                  </div>
                  <div>
                    <Label>Year</Label>
                    <Input
                      type="number"
                      value={eventForm.year}
                      onChange={(e) =>
                        setEventForm({ ...eventForm, year: Number(e.target.value) || new Date().getFullYear() })
                      }
                    />
                  </div>
                  <div>
                    <Label>Start Date</Label>
                    <Input
                      type="date"
                      value={eventForm.startDate}
                      onChange={(e) => setEventForm({ ...eventForm, startDate: e.target.value })}
                    />
                  </div>
                  <div>
                    <Label>End Date</Label>
                    <Input
                      type="date"
                      value={eventForm.endDate}
                      onChange={(e) => setEventForm({ ...eventForm, endDate: e.target.value })}
                    />
                  </div>
                  <div className="md:col-span-2">
                    <Label>Location</Label>
                    <Input
                      value={eventForm.location}
                      onChange={(e) => setEventForm({ ...eventForm, location: e.target.value })}
                      placeholder="BMICH, Colombo"
                    />
                  </div>
                  <div className="md:col-span-2">
                    <Label>Status</Label>
                    <Select
                      value={eventForm.status}
                      onValueChange={(value) => setEventForm({ ...eventForm, status: value as EventStatus })}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="UPCOMING">Upcoming</SelectItem>
                        <SelectItem value="ONGOING">Ongoing</SelectItem>
                        <SelectItem value="ENDED">Ended</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <DialogFooter>
                  <Button
                    onClick={handleCreateEvent}
                    disabled={createEventMutation.isPending}
                    className="w-full"
                    variant="employee"
                  >
                    {createEventMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                    Create Event
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>

            <Dialog>
              <DialogTrigger asChild>
                <Button variant="employee" size="sm" disabled={!selectedEventId}>
                  <Plus className="h-4 w-4" /> New Stall
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Create Stall</DialogTitle>
                  <CardDescription>Add a stall to the selected event</CardDescription>
                </DialogHeader>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 py-2">
                  <div>
                    <Label>Stall Code</Label>
                    <Input
                      value={stallForm.stallCode}
                      onChange={(e) => setStallForm({ ...stallForm, stallCode: e.target.value })}
                      placeholder="A001"
                    />
                  </div>
                  <div>
                    <Label>Size</Label>
                    <Select
                      value={stallForm.sizeCategory}
                      onValueChange={(value) => setStallForm({ ...stallForm, sizeCategory: value as StallSizeCategory })}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Size" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="SMALL">Small</SelectItem>
                        <SelectItem value="MEDIUM">Medium</SelectItem>
                        <SelectItem value="LARGE">Large</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label>Price (LKR)</Label>
                    <Input
                      type="number"
                      value={stallForm.price}
                      onChange={(e) => setStallForm({ ...stallForm, price: Number(e.target.value) })}
                    />
                  </div>
                  <div>
                    <Label>Location X</Label>
                    <Input
                      type="number"
                      value={stallForm.locationX}
                      onChange={(e) => setStallForm({ ...stallForm, locationX: e.target.value })}
                    />
                  </div>
                  <div>
                    <Label>Location Y</Label>
                    <Input
                      type="number"
                      value={stallForm.locationY}
                      onChange={(e) => setStallForm({ ...stallForm, locationY: e.target.value })}
                    />
                  </div>
                </div>
                <DialogFooter>
                  <Button
                    onClick={handleCreateStall}
                    disabled={createStallMutation.isPending}
                    variant="employee"
                    className="w-full"
                  >
                    {createStallMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                    Create Stall
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>

            <Button
              variant="secondary"
              size="sm"
              disabled={!selectedEventId || generateLayoutMutation.isPending}
              onClick={handleGenerateLayout}
            >
              {generateLayoutMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Bulk Create Stalls
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader className="grid gap-2 md:grid-cols-5 md:items-center">
            <div className="md:col-span-2">
              <CardTitle>Select Event</CardTitle>
              <CardDescription>Pick an event to view its stalls, availability, and reservations</CardDescription>
            </div>
            <div className="md:col-span-3 grid gap-3 md:grid-cols-3">
              <Select value={selectedEventId ?? undefined} onValueChange={(value) => setSelectedEventId(value)}>
                <SelectTrigger>
                  <SelectValue placeholder={eventsQuery.isLoading ? "Loading events..." : "Choose event"} />
                </SelectTrigger>
                <SelectContent>
                  {events.map((event) => (
                    <SelectItem key={event.id} value={event.id}>
                      {event.name} ({event.year})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Input
                type="number"
                placeholder="Filter by year"
                value={eventFilters.year ?? ""}
                onChange={(e) =>
                  setEventFilters({
                    ...eventFilters,
                    year: e.target.value ? Number(e.target.value) : undefined,
                  })
                }
              />
              <Select
                value={eventFilters.status ?? "ALL"}
                onValueChange={(value) => setEventFilters({ ...eventFilters, status: value as EventStatus | "ALL" })}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Status filter" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All statuses</SelectItem>
                  <SelectItem value="UPCOMING">Upcoming</SelectItem>
                  <SelectItem value="ONGOING">Ongoing</SelectItem>
                  <SelectItem value="ENDED">Ended</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            {selectedEvent ? (
              <>
                <div className="flex flex-wrap gap-3 items-center justify-between">
                  <div className="flex flex-wrap gap-3 items-center">
                    <Badge variant={statusVariants[selectedEvent.status]}>{selectedEvent.status}</Badge>
                    <span className="text-sm text-muted-foreground">
                      {selectedEvent.location} • {selectedEvent.startDate} to {selectedEvent.endDate}
                    </span>
                  </div>
                  <div className="flex gap-2">
                    <Select
                      value={selectedEvent.status}
                      onValueChange={(value) =>
                        updateEventStatusMutation.mutate({ id: selectedEvent.id, status: value as EventStatus })
                      }
                    >
                      <SelectTrigger className="w-[170px]">
                        <SelectValue placeholder="Update status" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="UPCOMING">Mark as Upcoming</SelectItem>
                        <SelectItem value="ONGOING">Mark as Ongoing</SelectItem>
                        <SelectItem value="ENDED">Mark as Ended</SelectItem>
                      </SelectContent>
                    </Select>
                    <Button
                      variant="destructive"
                      size="icon"
                      disabled={deleteEventMutation.isPending}
                      onClick={() => selectedEventId && deleteEventMutation.mutate(selectedEventId)}
                      title="Delete event"
                    >
                      {deleteEventMutation.isPending ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                      ) : (
                        <Trash2 className="h-4 w-4" />
                      )}
                    </Button>
                  </div>
                </div>
                <Separator />
                <div className="grid md:grid-cols-3 gap-4">
                  <Card>
                    <CardHeader className="pb-2">
                      <CardDescription>Total stalls</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="flex items-center justify-between">
                        <span className="text-3xl font-bold">{totalStalls}</span>
                        <RefreshCw
                          className="h-4 w-4 text-muted-foreground cursor-pointer"
                          onClick={() => {
                            queryClient.invalidateQueries({ queryKey: ["stalls"] });
                            queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
                          }}
                        />
                      </div>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardHeader className="pb-2">
                      <CardDescription>Available</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <span className="text-3xl font-bold text-primary">{availableCount}</span>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardHeader className="pb-2">
                      <CardDescription>Reserved</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <span className="text-3xl font-bold text-employee">{reservedCount}</span>
                    </CardContent>
                  </Card>
                </div>
              </>
            ) : (
              <p className="text-muted-foreground text-sm">Create or select an event to view stalls.</p>
            )}
          </CardContent>
        </Card>

        <div className="flex items-center justify-between gap-3">
          <div className="relative w-full max-w-md">
            <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search stalls or vendor ID"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9"
            />
          </div>
          <div className="text-sm text-muted-foreground">{filteredStalls.length} stalls in view</div>
        </div>

        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-3">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>Stalls</CardTitle>
                  {stallsQuery.isLoading && <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />}
                </div>
                <CardDescription>
                  Live stall availability per event
                </CardDescription>
              </CardHeader>
              <CardContent>
                {filteredStalls.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No stalls found for this event.</p>
                ) : (
                  <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-3">
                    {filteredStalls.map((stall) => (
                      <button
                        key={stall.id}
                        onClick={() => setSelectedStallId(stall.id)}
                        className={`rounded-lg border p-3 text-left transition-all ${
                          selectedStallId === stall.id
                            ? "border-employee shadow-md"
                            : "border-border hover:border-employee/60"
                        } ${stall.isReserved ? "bg-muted" : "bg-card"}`}
                      >
                        <div className="flex items-center justify-between">
                          <div className="font-semibold">{stall.stallCode}</div>
                          <Badge variant={stallStatusVariant(stall.isReserved)}>
                            {stall.isReserved ? "Reserved" : "Available"}
                          </Badge>
                        </div>
                        <div className="mt-2 flex items-center justify-between text-sm">
                          <Badge variant="outline" className={sizeColors[stall.sizeCategory]}>
                            {stall.sizeCategory}
                          </Badge>
                          <span className="font-semibold">LKR {stall.price.toLocaleString()}</span>
                        </div>
                        <div className="mt-1 text-xs text-muted-foreground">
                          {stall.eventName ?? "Event"} • {stall.locationX ?? "-"} / {stall.locationY ?? "-"}
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          <div>
            <Card className="sticky top-4">
              {selectedStall ? (
                <>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <CardTitle>Stall {selectedStall.stallCode}</CardTitle>
                      <Badge variant={stallStatusVariant(selectedStall.isReserved)}>
                        {selectedStall.isReserved ? "Reserved" : "Available"}
                      </Badge>
                    </div>
                    <CardDescription>{selectedStall.eventName ?? "Event"}</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="grid grid-cols-2 gap-3 text-sm">
                      <div>
                        <p className="text-muted-foreground">Size</p>
                        <p className="font-semibold">{selectedStall.sizeCategory}</p>
                      </div>
                      <div>
                        <p className="text-muted-foreground">Price</p>
                        <p className="font-semibold">LKR {selectedStall.price.toLocaleString()}</p>
                      </div>
                      <div>
                        <p className="text-muted-foreground">Coordinates</p>
                        <p className="font-semibold">
                          {selectedStall.locationX ?? "-"} / {selectedStall.locationY ?? "-"}
                        </p>
                      </div>
                      <div>
                        <p className="text-muted-foreground">Reserved By</p>
                        <p className="font-semibold">
                          {selectedStall.reservedByName || selectedStall.reservedBy || "—"}
                        </p>
                      </div>
                    </div>

                    <Separator />

                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <Label>Vendor</Label>
                        {selectedVendorLabel && (
                          <span className="text-xs text-muted-foreground">{selectedVendorLabel}</span>
                        )}
                      </div>
                      <Select
                        value={vendorId || undefined}
                        onValueChange={(value) => setVendorId(value)}
                        disabled={vendorsQuery.isLoading || vendors.length === 0}
                      >
                        <SelectTrigger>
                          <SelectValue
                            placeholder={
                              vendorsQuery.isLoading
                                ? "Loading vendors..."
                                : vendors.length === 0
                                  ? "No vendors available"
                                  : "Select vendor"
                            }
                          />
                        </SelectTrigger>
                        <SelectContent>
                          {vendors.map((vendor) => (
                            <SelectItem key={vendor.id} value={vendor.id}>
                              {resolveVendorLabel(vendor)}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="justify-start px-2"
                        onClick={() => setVendorId("")}
                        disabled={!vendorId}
                      >
                        Clear selection
                      </Button>
                      <div className="flex gap-2">
                        <Button
                          variant="employee"
                          className="flex-1"
                          onClick={handleReserve}
                          disabled={
                            reserveMutation.isPending || selectedStall.isReserved || !selectedStall || !vendorId
                          }
                        >
                          {reserveMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                          Reserve Stall
                        </Button>
                        <Button
                          variant="outline"
                          className="flex-1"
                          onClick={() => activeReservation && unreserveMutation.mutate(activeReservation.id)}
                          disabled={unreserveMutation.isPending || !activeReservation}
                        >
                          {unreserveMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                          Release
                        </Button>
                      </div>
                    </div>

                    <Separator />

                    <div className="space-y-2">
                      <Label>Adjust Price</Label>
                      <Input
                        type="number"
                        value={editPrice}
                        onChange={(e) => setEditPrice(Number(e.target.value))}
                      />
                      <Button
                        variant="outline"
                        onClick={handleUpdateStall}
                        disabled={updateStallMutation.isPending}
                      >
                        {updateStallMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                        Save Changes
                      </Button>
                    </div>

                    <Separator />

                    <Button
                      variant="destructive"
                      className="w-full"
                      onClick={() => selectedStall && deleteStallMutation.mutate(selectedStall.id)}
                      disabled={deleteStallMutation.isPending}
                    >
                      {deleteStallMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                      Delete Stall
                    </Button>
                  </CardContent>
                </>
              ) : (
                <CardContent className="py-16 text-center">
                  <Info className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">Select a stall from the list to view details</p>
                </CardContent>
              )}
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}

