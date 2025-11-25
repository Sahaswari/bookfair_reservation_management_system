import { useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Download, MapPin, Calendar, AlertCircle, Loader2 } from "lucide-react";
import Header from "@/components/Header";
import { useAuth } from "@/hooks/useAuth";
import { stallApi, type Event } from "@/lib/stallApi";
import { reservationApi, type Reservation, type ReservationStatus } from "@/lib/reservationApi";
import { toast } from "sonner";

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

export default function MyReservations() {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const reservationsQuery = useQuery({
    queryKey: ["reservations", user?.id],
    queryFn: () => reservationApi.listReservationsByUser(user!.id),
    enabled: !!user?.id,
  });

  const eventsQuery = useQuery({
    queryKey: ["events"],
    queryFn: stallApi.listEvents,
  });

  const eventsById = useMemo(() => {
    const map = new Map<string, Event>();
    (eventsQuery.data || []).forEach((event) => map.set(event.id, event));
    return map;
  }, [eventsQuery.data]);

  const cancelReservationMutation = useMutation({
    mutationFn: (reservationId: string) => reservationApi.deleteReservation(reservationId),
    onSuccess: () => {
      toast.success("Reservation cancelled");
      queryClient.invalidateQueries({ queryKey: ["reservations", user?.id] });
      queryClient.invalidateQueries({ queryKey: ["available-stalls"] });
    },
    onError: (error: unknown) => {
      toast.error((error as Error)?.message || "Unable to cancel reservation");
    },
  });

  const reservations = reservationsQuery.data || [];

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container py-8">
        <div className="max-w-4xl mx-auto space-y-6">
          <div>
            <h1 className="text-4xl font-bold mb-2">My Reservations</h1>
            <p className="text-muted-foreground">
              View and manage your stall reservations
            </p>
          </div>

          {reservationsQuery.isLoading && (
            <p className="text-sm text-muted-foreground">Loading your reservations...</p>
          )}

          {!reservationsQuery.isLoading && reservations.length === 0 ? (
            <Card>
              <CardContent className="py-16 text-center space-y-4">
                <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-muted">
                  <MapPin className="h-8 w-8 text-muted-foreground" />
                </div>
                <div>
                  <h3 className="text-xl font-semibold mb-2">No Reservations Yet</h3>
                  <p className="text-muted-foreground mb-6">
                    You haven't reserved any stalls. Start by browsing our interactive map.
                  </p>
                  <Button variant="hero" size="lg" asChild>
                    <a href="/reserve">Reserve Stalls</a>
                  </Button>
                </div>
              </CardContent>
            </Card>
          ) : (
            <>
              {/* Reservation Cards */}
              <div className="space-y-4">
                {reservations.map((reservation: Reservation) => {
                  const status = statusStyles[reservation.status] ?? statusStyles.PENDING;
                  const eventInfo = eventsById.get(reservation.eventId);
                  const isConfirmed = reservation.status === "CONFIRMED";
                  const canDownloadPass = Boolean(isConfirmed && reservation.qrCodeUrl);
                  const priceValue = Number(reservation.price ?? 0);
                  const priceDisplay = priceValue.toLocaleString();

                  return (
                    <Card key={reservation.id}>
                      <CardHeader>
                        <div className="flex items-start justify-between">
                          <div>
                            <CardTitle>Stall {reservation.stallCode ?? "N/A"}</CardTitle>
                            <CardDescription className="flex items-center gap-2 mt-1">
                              <Calendar className="h-4 w-4" />
                              Event: {eventInfo?.name ?? reservation.eventId}
                            </CardDescription>
                          </div>
                          <Badge variant="outline" className={status.className}>
                            {status.label}
                          </Badge>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-4">
                        <div className="grid sm:grid-cols-2 gap-4">
                          <div>
                            <p className="text-sm font-medium text-muted-foreground mb-2">Size</p>
                            <Badge variant="outline">{reservation.sizeCategory ?? "--"}</Badge>
                          </div>
                          <div>
                            <p className="text-sm font-medium text-muted-foreground mb-2">Amount</p>
                            <p className="text-2xl font-bold text-primary">LKR {priceDisplay}</p>
                          </div>
                        </div>
                        <div className="flex gap-2 pt-2">
                          <Button
                            variant="outline"
                            className="flex-1"
                            disabled={!canDownloadPass}
                            asChild={canDownloadPass}
                          >
                            {canDownloadPass ? (
                              <a href={reservation.qrCodeUrl!} target="_blank" rel="noreferrer">
                                <Download className="h-4 w-4 mr-2" />
                                Download QR Pass
                              </a>
                            ) : (
                              <span className="flex items-center justify-center">
                                <Download className="h-4 w-4 mr-2" />
                                Download QR Pass
                              </span>
                            )}
                          </Button>
                          <Button
                            variant="default"
                            className="flex-1"
                            onClick={() => cancelReservationMutation.mutate(reservation.id)}
                            disabled={cancelReservationMutation.isPending || reservation.status === "CANCELLED"}
                          >
                            {cancelReservationMutation.isPending ? (
                              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                            ) : null}
                            {reservation.status === "CANCELLED" ? "Cancelled" : "Cancel Reservation"}
                          </Button>
                        </div>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>

              {/* Cancellation Policy */}
              <Card className="border-amber-200 dark:border-amber-900 bg-amber-50 dark:bg-amber-950/20">
                <CardContent className="pt-6">
                  <div className="flex items-start gap-3">
                    <AlertCircle className="h-5 w-5 text-amber-600 dark:text-amber-400 mt-0.5" />
                    <div>
                      <p className="font-semibold text-amber-900 dark:text-amber-100">Cancellation Policy</p>
                      <p className="text-sm text-amber-800 dark:text-amber-200 mt-1">
                        Reservations can be cancelled up to 30 days before the event start date. 
                        Cancellations made within 30 days are subject to a 50% cancellation fee.
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
