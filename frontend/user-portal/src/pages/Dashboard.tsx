import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { BookOpen, CheckCircle2, Loader2, MapPin, Tag } from "lucide-react";
import Header from "@/components/Header";
import { useAuth } from "@/hooks/useAuth";
import { reservationApi, type Reservation, type ReservationStatus } from "@/lib/reservationApi";
import { stallApi, type Event } from "@/lib/stallApi";

const statusStyles: Record<ReservationStatus, { label: string; className: string }> = {
  PENDING: {
    label: "Pending",
    className: "bg-amber-100 text-amber-800 border-amber-200 dark:bg-amber-900/30 dark:text-amber-200",
  },
  CONFIRMED: {
    label: "Confirmed",
    className: "bg-emerald-100 text-emerald-800 border-emerald-200 dark:bg-emerald-900/30 dark:text-emerald-200",
  },
  CANCELLED: {
    label: "Cancelled",
    className: "bg-rose-100 text-rose-800 border-rose-200 dark:bg-rose-900/30 dark:text-rose-200",
  },
};

export default function Dashboard() {
  const { user } = useAuth();
  const maxReservations = 3;
  const [selectedEventId, setSelectedEventId] = useState<string | null>(null);

  const eventsQuery = useQuery({
    queryKey: ["events"],
    queryFn: stallApi.listEvents,
  });

  const events = (eventsQuery.data || []) as Event[];

  useEffect(() => {
    if (events.length === 0) return;
    if (!selectedEventId || !events.some((event) => event.id === selectedEventId)) {
      setSelectedEventId(events[0].id);
    }
  }, [events, selectedEventId]);

  const reservationsQuery = useQuery({
    queryKey: ["reservations", user?.id],
    queryFn: () => reservationApi.listReservationsByUser(user!.id),
    enabled: !!user?.id,
  });

  const reservations = (reservationsQuery.data || []) as Reservation[];
  const activeReservations = reservations.filter((reservation) => reservation.status !== "CANCELLED");
  const selectedEvent = useMemo(
    () => events.find((event) => event.id === selectedEventId) ?? null,
    [events, selectedEventId],
  );
  const isEventsLoading = eventsQuery.isLoading;
  const hasEvents = events.length > 0;
  const eventSelectionDisabled = isEventsLoading || !hasEvents;

  const reservationsForSelectedEvent = useMemo(() => {
    if (!selectedEventId) return [];
    return activeReservations.filter((reservation) => reservation.eventId === selectedEventId);
  }, [activeReservations, selectedEventId]);

  const reservationCount = reservationsForSelectedEvent.length;
  const progressPercent = Math.min(100, (reservationCount / maxReservations) * 100);
  const recentReservations = [...reservationsForSelectedEvent]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 3);

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container py-8">
        <div className="max-w-4xl mx-auto space-y-8">
          {/* Welcome Card */}
          <Card className="gradient-warm text-primary-foreground">
            <CardContent className="pt-6 space-y-6">
              <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div>
                  <h1 className="text-3xl font-bold mb-2">
                    {selectedEvent ? `Welcome to ${selectedEvent.name}!` : "Welcome to the Book Fair!"}
                  </h1>
                  <p className="text-primary-foreground/90">
                    {selectedEvent
                      ? `Manage your stall reservations and updates for ${selectedEvent.name}.`
                      : "Select an event to manage your stall reservations and stay updated."}
                  </p>
                  {selectedEvent && (
                    <p className="text-sm text-primary-foreground/80 mt-2">
                      {selectedEvent.location} | {selectedEvent.startDate} - {selectedEvent.endDate}
                    </p>
                  )}
                  {!hasEvents && !isEventsLoading && (
                    <p className="text-sm text-primary-foreground/80 mt-2">
                      No events are available yet. Please check back soon.
                    </p>
                  )}
                </div>
                <div className="w-full md:w-64 space-y-2">
                  <div className="flex items-center gap-2 text-xs uppercase tracking-wide text-primary-foreground/80">
                    <BookOpen className="h-4 w-4" />
                    <span>Select event</span>
                  </div>
                  <Select
                    value={selectedEventId ?? undefined}
                    onValueChange={(value) => setSelectedEventId(value)}
                    disabled={eventSelectionDisabled}
                  >
                    <SelectTrigger className="bg-white/10 border-white/30 text-primary-foreground">
                      <SelectValue placeholder={isEventsLoading ? "Loading events..." : "Choose event"} />
                    </SelectTrigger>
                    <SelectContent>
                      {events.map((event) => (
                        <SelectItem key={event.id} value={event.id}>
                          {event.name} ({event.year})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {eventsQuery.isError && (
                    <p className="text-xs text-red-100">Unable to load events. Please refresh.</p>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Reservation Status */}
          <Card>
            <CardHeader>
              <CardTitle>Reservation Status</CardTitle>
              <CardDescription>
                {isEventsLoading
                  ? "Loading events..."
                  : selectedEvent
                    ? `You can reserve up to ${maxReservations} stalls for ${selectedEvent.name}.`
                    : "Select an event to view reservation limits."}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {selectedEvent ? (
                <>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="text-2xl font-bold">{reservationCount} / {maxReservations}</span>
                      {reservationsQuery.isFetching && (
                        <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
                      )}
                    </div>
                    <span className="text-sm text-muted-foreground">
                      {selectedEvent.name} | Stalls Reserved
                    </span>
                  </div>
                  <Progress value={progressPercent} className="h-2" />

                  {reservationsQuery.isLoading && (
                    <p className="text-sm text-muted-foreground">Loading your reservations...</p>
                  )}

                  {!reservationsQuery.isLoading && reservationCount === 0 && (
                    <div className="bg-muted rounded-lg p-4">
                      <p className="text-sm text-muted-foreground">
                        You haven't reserved any stalls for {selectedEvent.name} yet. Start by browsing the map to secure
                        your preferred spots.
                      </p>
                    </div>
                  )}

                  {!reservationsQuery.isLoading && reservationCount > 0 && (
                    <div className="space-y-3">
                      {recentReservations.map((reservation) => {
                        const status = statusStyles[reservation.status] ?? statusStyles.PENDING;
                        const priceValue = Number(reservation.price ?? 0).toLocaleString();

                        return (
                          <div
                            key={reservation.id}
                            className="flex items-center justify-between rounded-lg border p-3 bg-card"
                          >
                            <div>
                              <p className="font-semibold">Stall {reservation.stallCode ?? "N/A"}</p>
                              <p className="text-xs text-muted-foreground">
                                {selectedEvent.name} | LKR {priceValue}
                              </p>
                            </div>
                            <Badge variant="outline" className={status.className}>
                              {status.label}
                            </Badge>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </>
              ) : (
                <p className="text-sm text-muted-foreground">
                  {isEventsLoading
                    ? "Loading events..."
                    : hasEvents
                      ? "Select an event from the dropdown above to view reservation stats."
                      : "No events available to display yet."}
                </p>
              )}
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <div className="grid md:grid-cols-3 gap-4">
            <Link to="/reserve">
              <Card className="hover:shadow-lg transition-shadow cursor-pointer">
                <CardHeader>
                  <MapPin className="h-8 w-8 text-primary mb-2" />
                  <CardTitle>Reserve Stall</CardTitle>
                  <CardDescription>
                    Browse and select from available stalls
                  </CardDescription>
                </CardHeader>
              </Card>
            </Link>

            <Link to="/my-reservations">
              <Card className="hover:shadow-lg transition-shadow cursor-pointer">
                <CardHeader>
                  <CheckCircle2 className="h-8 w-8 text-primary mb-2" />
                  <CardTitle>My Reservations</CardTitle>
                  <CardDescription>
                    View and manage your reservations
                  </CardDescription>
                </CardHeader>
              </Card>
            </Link>

            <Link to="/genres">
              <Card className="hover:shadow-lg transition-shadow cursor-pointer">
                <CardHeader>
                  <Tag className="h-8 w-8 text-primary mb-2" />
                  <CardTitle>Update Genres</CardTitle>
                  <CardDescription>
                    Add literary genres you publish
                  </CardDescription>
                </CardHeader>
              </Card>
            </Link>
          </div>

          {/* Information Box */}
          <Card>
            <CardHeader>
              <CardTitle>Important Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <div className="flex items-start gap-2">
                <CheckCircle2 className="h-5 w-5 text-primary mt-0.5" />
                <p className="text-sm">Stall setup begins September 13, 2025 (2 days before the fair)</p>
              </div>
              <div className="flex items-start gap-2">
                <CheckCircle2 className="h-5 w-5 text-primary mt-0.5" />
                <p className="text-sm">All payments must be completed within 7 days of reservation</p>
              </div>
              <div className="flex items-start gap-2">
                <CheckCircle2 className="h-5 w-5 text-primary mt-0.5" />
                <p className="text-sm">Cancellations are allowed up to 30 days before the event</p>
              </div>
              <div className="flex items-start gap-2">
                <CheckCircle2 className="h-5 w-5 text-primary mt-0.5" />
                <p className="text-sm">Your QR pass will be emailed within 24 hours of payment confirmation</p>
              </div>
            </CardContent>
          </Card>

          {/* CTA */}
          {!reservationsQuery.isLoading && reservationCount === 0 && (
            <div className="text-center py-8">
              <Button variant="hero" size="lg" asChild>
                <Link to="/reserve">Start Reserving Stalls</Link>
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
