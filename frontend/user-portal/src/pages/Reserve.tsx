import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { Info, X, RefreshCw, Loader2 } from "lucide-react";
import Header from "@/components/Header";
import { useAuth } from "@/hooks/useAuth";
import { stallApi, type Stall, type StallSizeCategory, type Event } from "@/lib/stallApi";
import { reservationApi, type Reservation } from "@/lib/reservationApi";
import { notificationApi } from "@/lib/notificationApi";
import { toast } from "sonner";
type UiStall = Stall & { status: "available" | "selected" | "reserved" };

const sizeLabels: Record<StallSizeCategory, string> = {
  SMALL: "Small",
  MEDIUM: "Medium",
  LARGE: "Large",
};

const sizeColors: Record<StallSizeCategory, string> = {
  SMALL: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300",
  MEDIUM: "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300",
  LARGE: "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300",
};

export default function Reserve() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const [selectedEventId, setSelectedEventId] = useState<string | null>(null);
  const [selectedStalls, setSelectedStalls] = useState<string[]>([]);
  const [filterSize, setFilterSize] = useState<StallSizeCategory | "ALL">("ALL");
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const reservationLimit: number = 3;

  const eventsQuery = useQuery({
    queryKey: ["events"],
    queryFn: stallApi.listEvents,
  });

  const events = eventsQuery.data || [];

  useEffect(() => {
    if (!selectedEventId && events.length > 0) {
      setSelectedEventId(events[0].id);
    }
  }, [events, selectedEventId]);

  const stallsQuery = useQuery({
    queryKey: ["available-stalls", selectedEventId],
    queryFn: () => stallApi.listAvailableStallsByEvent(selectedEventId!),
    enabled: !!selectedEventId,
  });

  const userReservationsQuery = useQuery({
    queryKey: ["reservations", user?.id],
    queryFn: () => reservationApi.listReservationsByUser(user!.id),
    enabled: !!user?.id,
  });

  useEffect(() => {
    if (userReservationsQuery.isError) {
      toast.error("Unable to load your existing reservations. Please refresh and try again.");
    }
  }, [userReservationsQuery.isError]);

  const activeReservationsForEvent = useMemo(() => {
    if (!selectedEventId) return 0;
    const reservations = userReservationsQuery.data || [];
    return reservations.filter(
      (reservation) => reservation.eventId === selectedEventId && reservation.status !== "CANCELLED",
    ).length;
  }, [userReservationsQuery.data, selectedEventId]);

  const availableSlotsForEvent = selectedEventId
    ? Math.max(reservationLimit - activeReservationsForEvent, 0)
    : reservationLimit;
  const hasReachedReservationLimit = selectedEventId ? availableSlotsForEvent === 0 : false;
  const selectionLimitForEvent = selectedEventId ? availableSlotsForEvent : reservationLimit;
  const remainingSelectableSlots = Math.max(selectionLimitForEvent - selectedStalls.length, 0);
  const isReservationLimitPending = userReservationsQuery.isPending && !userReservationsQuery.data;
  const isReservationLimitUnavailable = userReservationsQuery.isError;

  const uiStalls: UiStall[] = useMemo(() => {
    const available = stallsQuery.data || [];
    return available.map((stall) => ({
      ...stall,
      status: stall.isReserved
        ? "reserved"
        : selectedStalls.includes(stall.id)
          ? "selected"
          : "available",
    }));
  }, [stallsQuery.data, selectedStalls]);

  const filteredStalls = useMemo(() => {
    if (filterSize === "ALL") return uiStalls;
    return uiStalls.filter((s) => s.sizeCategory === filterSize);
  }, [uiStalls, filterSize]);

  const selectedStallsData = uiStalls.filter((s) => selectedStalls.includes(s.id));
  const totalPrice = selectedStallsData.reduce((sum, s) => sum + s.price, 0);

  const reserveMutation = useMutation<Reservation[]>({
    mutationFn: async () => {
      if (!user?.id) throw new Error("User not found");
      if (!selectedEventId) throw new Error("No event selected");
      if (isReservationLimitUnavailable) throw new Error("Unable to verify reservation availability");
      if (selectedStalls.length === 0) throw new Error("Please select at least one stall");
      if (selectedStalls.length > selectionLimitForEvent) {
        throw new Error("Selection exceeds remaining reservations for this event");
      }
      const reservations = await Promise.all(
        selectedStalls.map((stallId) =>
          reservationApi.createReservation({
            userId: user.id,
            stallId,
            eventId: selectedEventId,
          }),
        ),
      );
      return reservations;
    },
    onSuccess: (reservations) => {
      const stallLookup = new Map(selectedStallsData.map((stall) => [stall.id, stall]));

      reservations.forEach((reservation) => {
        const stallDetails = stallLookup.get(reservation.stallId);
        const eventDetails = events.find((event) => event.id === reservation.eventId);
        const eventName = eventDetails?.name ?? eventSummary?.name ?? "Book Fair";
        const stallCode = stallDetails?.stallCode ?? reservation.stallId;
        const salutation = user?.firstName ?? user?.email ?? "there";
        const formattedAmount =
          typeof stallDetails?.price === "number" ? `LKR ${stallDetails.price.toLocaleString()}` : null;
        const messageBase = `Hi ${salutation}, your reservation for ${stallCode} at ${eventName} is confirmed.`;
        const message = formattedAmount ? `${messageBase} Reservation amount: ${formattedAmount}.` : messageBase;

        void notificationApi
          .createNotification({
            userId: reservation.userId,
            reservationId: reservation.id,
            subject: `Your reservation for ${stallCode} is confirmed`,
            message,
            metadata: {
              eventId: reservation.eventId,
              eventName,
              stallId: reservation.stallId,
              stallCode,
              price: stallDetails?.price ?? null,
              source: "user-portal",
            },
          })
          .catch((error) => console.error("Failed to send reservation notification", error));
      });

      toast.success("Reservation successful");
      queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
      queryClient.invalidateQueries({ queryKey: ["reservations", user?.id] });
      navigate("/success", { state: { stalls: selectedStallsData, totalPrice } });
    },
    onError: (err) => {
      toast.error((err as Error).message || "Reservation failed");
    },
    onSettled: () => setShowConfirmModal(false),
  });

  const handleStallClick = (stall: UiStall) => {
    if (stall.isReserved) return;
    if (stall.status === "selected") {
      setSelectedStalls((prev) => prev.filter((id) => id !== stall.id));
      return;
    }
    if (isReservationLimitPending) {
      toast.info("Loading your reservation availability. Please try again in a moment.");
      return;
    }
    if (isReservationLimitUnavailable) {
      toast.error("Unable to verify your reservation quota right now. Please refresh the page.");
      return;
    }
    if (!selectedEventId) {
      toast.error("Please select an event before choosing stalls");
      return;
    }
    if (hasReachedReservationLimit) {
      toast.warning("You have already reserved the maximum stalls for this event");
      return;
    }
    if (remainingSelectableSlots <= 0) {
      toast.warning("You have already selected all remaining stalls for this event");
      return;
    }
    setSelectedStalls((prev) => [...prev, stall.id]);
  };

  const handleReserve = () => {
    if (!selectedEventId) {
      toast.error("Please choose an event before reserving");
      return;
    }
    if (isReservationLimitPending) {
      toast.info("Still verifying your reservation limit. Try again shortly.");
      return;
    }
    if (isReservationLimitUnavailable) {
      toast.error("Unable to verify your reservation quota. Please refresh and try again.");
      return;
    }
    if (hasReachedReservationLimit) {
      toast.error("You have already reserved the maximum stalls allowed for this event");
      return;
    }
    if (selectedStalls.length > selectionLimitForEvent) {
      toast.error("Your selection exceeds the remaining slots for this event");
      return;
    }
    if (selectedStalls.length === 0) {
      toast.error("Please select at least one stall");
      return;
    }
    setShowConfirmModal(true);
  };

  const resetSelection = () => setSelectedStalls([]);

  const eventSummary = events.find((e) => e.id === selectedEventId);

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container py-8">
        <div className="max-w-7xl mx-auto space-y-6">
          <div>
            <h1 className="text-4xl font-bold mb-2">Reserve Your Stall</h1>
            <p className="text-muted-foreground">
              Each event allows up to {reservationLimit} stall{reservationLimit === 1 ? "" : "s"} per exhibitor.
              {selectedEventId && (
                <>
                  {" "}
                  {isReservationLimitPending
                    ? "Checking your existing reservations..."
                    : isReservationLimitUnavailable
                      ? "Unable to retrieve your reservation count right now. Please refresh."
                      : `You currently have ${activeReservationsForEvent} active reservation${
                          activeReservationsForEvent === 1 ? "" : "s"
                        } for this event, so you can reserve ${availableSlotsForEvent} more.`}
                </>
              )}
            </p>
          </div>

          <Card>
            <CardContent className="pt-6 space-y-4">
              <div className="flex flex-wrap items-center gap-4 justify-between">
                <div className="space-y-1">
                  <p className="text-sm font-medium">Event</p>
                  <Select
                    value={selectedEventId ?? undefined}
                    onValueChange={(value) => {
                      setSelectedEventId(value);
                      resetSelection();
                    }}
                    disabled={eventsQuery.isLoading || events.length === 0}
                  >
                    <SelectTrigger className="w-[260px]">
                      <SelectValue placeholder={eventsQuery.isLoading ? "Loading events..." : "Choose event"} />
                    </SelectTrigger>
                    <SelectContent>
                      {events.map((event: Event) => (
                        <SelectItem key={event.id} value={event.id}>
                          {event.name} ({event.year})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {eventSummary && (
                    <p className="text-xs text-muted-foreground">
                      {eventSummary.location} • {eventSummary.startDate} ? {eventSummary.endDate}
                    </p>
                  )}
                  {selectedEventId && (
                    <p
                      className={`text-xs ${hasReachedReservationLimit ? "text-destructive" : "text-muted-foreground"}`}
                    >
                      {isReservationLimitPending
                        ? "Checking your reservations for this event..."
                        : isReservationLimitUnavailable
                          ? "Unable to determine remaining slots for this event"
                          : hasReachedReservationLimit
                            ? "You have already reached the maximum reservations for this event"
                            : `${availableSlotsForEvent} reservation slot${availableSlotsForEvent === 1 ? "" : "s"} remaining for this event`}
                    </p>
                  )}
                </div>

                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium">Filter by size:</span>
                  <Select value={filterSize} onValueChange={(val) => setFilterSize(val as StallSizeCategory | "ALL")}>
                    <SelectTrigger className="w-[160px]">
                      <SelectValue placeholder="Size" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="ALL">All</SelectItem>
                      <SelectItem value="SMALL">Small</SelectItem>
                      <SelectItem value="MEDIUM">Medium</SelectItem>
                      <SelectItem value="LARGE">Large</SelectItem>
                    </SelectContent>
                  </Select>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => {
                      resetSelection();
                      queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
                    }}
                  >
                    <RefreshCw className="h-4 w-4" />
                  </Button>
                </div>
              </div>

              <div className="flex items-center gap-4 text-sm">
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded bg-primary"></div>
                  <span>Selected</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded bg-card border-2"></div>
                  <span>Available</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 rounded bg-muted"></div>
                  <span>Reserved</span>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="grid lg:grid-cols-3 gap-6">
            {/* Stall Map */}
            <div className="lg:col-span-2">
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle>Exhibition Hall</CardTitle>
                    <Button variant="ghost" size="sm">
                      <Info className="h-4 w-4 mr-1" />
                      Guidelines
                    </Button>
                  </div>
                  <CardDescription>Click on available stalls to select them</CardDescription>
                </CardHeader>
                <CardContent>
                  {stallsQuery.isLoading && (
                    <div className="grid grid-cols-8 gap-2">
                      {Array.from({ length: 24 }).map((_, idx) => (
                        <Skeleton key={idx} className="aspect-square rounded-lg" />
                      ))}
                    </div>
                  )}

                  {!stallsQuery.isLoading && filteredStalls.length === 0 && (
                    <p className="text-sm text-muted-foreground">No available stalls for this event/size.</p>
                  )}

                  {!stallsQuery.isLoading && filteredStalls.length > 0 && (
                    <div className="grid grid-cols-8 gap-2">
                      {filteredStalls.map((stall) => (
                        <button
                          key={stall.id}
                          onClick={() => handleStallClick(stall)}
                          disabled={stall.isReserved}
                          className={`
                            aspect-square rounded-lg border-2 p-1 text-xs font-medium transition-all
                            ${stall.status === "selected" ? "bg-primary text-primary-foreground border-primary scale-105" : ""}
                            ${stall.status === "available" ? "bg-card hover:bg-accent hover:scale-105 cursor-pointer" : ""}
                            ${stall.isReserved ? "bg-muted text-muted-foreground cursor-not-allowed opacity-50" : ""}
                          `}
                          title={`${stall.stallCode} - ${sizeLabels[stall.sizeCategory]} - LKR ${stall.price.toLocaleString()}`}
                        >
                          <div className="flex flex-col items-center justify-center h-full">
                            <span className="font-bold">{stall.stallCode}</span>
                            <Badge variant="outline" className={`text-[8px] px-1 mt-1 ${sizeColors[stall.sizeCategory]}`}>
                              {stall.sizeCategory[0]}
                            </Badge>
                          </div>
                        </button>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>

            {/* Selection Summary */}
            <div className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>Your Selection</CardTitle>
                  <CardDescription>
                    {isReservationLimitPending
                      ? "Calculating remaining capacity..."
                      : isReservationLimitUnavailable
                        ? "Unable to determine remaining capacity"
                        : `${selectedStallsData.length} selected · ${remainingSelectableSlots} remaining`}
                  </CardDescription>
                  {hasReachedReservationLimit && !isReservationLimitPending && !isReservationLimitUnavailable && (
                    <p className="text-xs text-destructive mt-1">
                      You already hold the maximum number of reservations for this event.
                    </p>
                  )}
                </CardHeader>
                <CardContent className="space-y-4">
                  {selectedStallsData.length === 0 ? (
                    <p className="text-sm text-muted-foreground text-center py-4">
                      No stalls selected yet
                    </p>
                  ) : (
                    <>
                      <div className="space-y-2">
                        {selectedStallsData.map((stall) => (
                          <div key={stall.id} className="flex items-center justify-between p-2 bg-muted rounded-lg">
                            <div>
                              <p className="font-semibold">{stall.stallCode}</p>
                              <p className="text-xs text-muted-foreground capitalize">
                                {sizeLabels[stall.sizeCategory]}
                              </p>
                            </div>
                            <div className="flex items-center gap-2">
                              <span className="text-sm font-medium">
                                LKR {stall.price.toLocaleString()}
                              </span>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-6 w-6"
                                onClick={() => handleStallClick(stall)}
                              >
                                <X className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                        ))}
                      </div>

                      <div className="pt-4 border-t space-y-2">
                        <div className="flex justify-between text-lg font-bold">
                          <span>Total</span>
                          <span>LKR {totalPrice.toLocaleString()}</span>
                        </div>
                        <Button 
                          variant="hero" 
                          className="w-full" 
                          size="lg"
                          onClick={handleReserve}
                          disabled={
                            selectedStalls.length === 0 ||
                            reserveMutation.isPending ||
                            isReservationLimitPending ||
                            isReservationLimitUnavailable ||
                            hasReachedReservationLimit
                          }
                        >
                          Reserve Now
                        </Button>
                      </div>
                    </>
                  )}
                </CardContent>
              </Card>

              {/* Pricing Info */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Stall Pricing</CardTitle>
                </CardHeader>
                <CardContent className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span>Small (3m x 2m)</span>
                    <span className="font-semibold">LKR 15,000</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Medium (4m x 3m)</span>
                    <span className="font-semibold">LKR 25,000</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Large (6m x 4m)</span>
                    <span className="font-semibold">LKR 40,000</span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      {showConfirmModal && (
        <div className="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm flex items-center justify-center p-4">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Confirm Reservation</CardTitle>
              <CardDescription>Please review your selection</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                {selectedStallsData.map((stall) => (
                  <div key={stall.id} className="flex justify-between text-sm">
                    <span>
                      {stall.stallCode} ({sizeLabels[stall.sizeCategory]})
                    </span>
                    <span>LKR {stall.price.toLocaleString()}</span>
                  </div>
                ))}
              </div>
              <div className="pt-2 border-t">
                <div className="flex justify-between font-bold">
                  <span>Total</span>
                  <span>LKR {totalPrice.toLocaleString()}</span>
                </div>
              </div>
              
              <div className="flex items-start space-x-2 pt-2">
                <Checkbox id="confirm-terms" defaultChecked />
                <label htmlFor="confirm-terms" className="text-sm text-muted-foreground leading-tight">
                  I accept the reservation terms and understand that payment must be completed within 7 days
                </label>
              </div>

              <div className="flex gap-2 pt-2">
                <Button variant="outline" className="flex-1" onClick={() => setShowConfirmModal(false)}>
                  Cancel
                </Button>
                <Button
                  variant="hero"
                  className="flex-1"
                  onClick={() => reserveMutation.mutate()}
                  disabled={reserveMutation.isPending}
                >
                  {reserveMutation.isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                  Confirm Reservation
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}


