import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Search, XCircle, ChevronLeft, ChevronRight, Loader2, CheckCircle } from "lucide-react";
import Header from "@/components/Header";
import { useEmployeeAuth } from "@/hooks/useEmployeeAuth";
import { reservationApi } from "@/lib/reservationApi";
import { stallApi, type StallSizeCategory } from "@/lib/stallApi";
import { userApi } from "@/lib/userApi";
import { useToast } from "@/components/ui/use-toast";

const ITEMS_PER_PAGE = 10;
const SIZE_OPTIONS: StallSizeCategory[] = ["SMALL", "MEDIUM", "LARGE"];
const STATUS_OPTIONS = ["PENDING", "CONFIRMED", "CANCELLED"] as const;

const formatDate = (value?: string) => {
  if (!value) return "-";
  const d = new Date(value);
  return Number.isNaN(d.getTime()) ? "-" : d.toLocaleDateString();
};

export default function ReservationList() {
  const { isAuthenticated } = useEmployeeAuth();
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState("");
  const [vendorFilter, setVendorFilter] = useState<"ALL" | string>("ALL");
  const [sizeFilter, setSizeFilter] = useState<"ALL" | StallSizeCategory>("ALL");
  const [eventFilter, setEventFilter] = useState<string>("ALL");
  const [statusFilter, setStatusFilter] = useState<"ALL" | (typeof STATUS_OPTIONS)[number]>("ALL");
  const [currentPage, setCurrentPage] = useState(1);

  const reservationsQuery = useQuery({
    queryKey: ["reservations"],
    queryFn: () => reservationApi.getAllReservations(),
  });

  const eventsQuery = useQuery({
    queryKey: ["events", "all"],
    queryFn: () => stallApi.listEvents(),
  });

  const vendorsQuery = useQuery({
    queryKey: ["vendors", "all"],
    queryFn: () => userApi.listVendors(),
  });

  const reservations = reservationsQuery.data ?? [];
  const eventNameById = useMemo(() => {
    const lookup: Record<string, string> = {};
    (eventsQuery.data ?? []).forEach((event) => {
      lookup[event.id] = event.name;
    });
    return lookup;
  }, [eventsQuery.data]);

  const filteredReservations = useMemo(() => {
    const normalized = searchQuery.trim().toLowerCase();
    let result = reservations;

    if (vendorFilter !== "ALL") {
      result = result.filter((r) => r.userId === vendorFilter);
    }

    if (sizeFilter !== "ALL") {
      result = result.filter(
        (r) => (r.sizeCategory ?? "").toUpperCase() === sizeFilter,
      );
    }

    if (eventFilter !== "ALL") {
      result = result.filter((r) => r.eventId === eventFilter);
    }

    if (statusFilter !== "ALL") {
      result = result.filter((r) => r.status === statusFilter);
    }

    if (!normalized) return result;

    return result.filter((reservation) => {
      const vendor = `${reservation.userFirstName || ""} ${reservation.userLastName || ""}`.trim().toLowerCase();
      const code = (reservation.stallCode || "").toLowerCase();
      return vendor.includes(normalized) || code.includes(normalized);
    });
  }, [reservations, searchQuery, vendorFilter, sizeFilter, eventFilter, statusFilter]);

  const totalPages = Math.ceil(filteredReservations.length / ITEMS_PER_PAGE) || 1;
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const paginatedReservations = filteredReservations.slice(startIndex, startIndex + ITEMS_PER_PAGE);

  const confirmMutation = useMutation({
    mutationFn: (id: string) => reservationApi.updateStatus(id, "CONFIRMED"),
    onSuccess: () => {
      toast({ title: "Reservation confirmed" });
      queryClient.invalidateQueries({ queryKey: ["reservations"] });
    },
    onError: () => toast({ title: "Failed to confirm reservation", variant: "destructive" }),
  });

  const cancelReservationMutation = useMutation({
    mutationFn: (id: string) => reservationApi.updateStatus(id, "CANCELLED"),
    onSuccess: () => {
      toast({ title: "Reservation cancelled" });
      queryClient.invalidateQueries({ queryKey: ["reservations"] });
    },
    onError: () => toast({ title: "Failed to cancel reservation", variant: "destructive" }),
  });

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background">
      <Header isEmployee />
      
      <div className="container py-8">
        <div className="space-y-6">
          <div className="flex items-center justify-between gap-4 flex-wrap">
            <div>
              <h1 className="text-4xl font-bold mb-2">Reservation List</h1>
              <p className="text-muted-foreground">Live reserved stalls from stall-service</p>
            </div>
            <div className="flex flex-wrap gap-3">
              <div className="relative w-64">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search stall or vendor..."
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="pl-9"
                />
              </div>
              <Select
                value={vendorFilter}
                onValueChange={(value) => {
                  setVendorFilter(value);
                  setCurrentPage(1);
                }}
              >
                <SelectTrigger className="w-64">
                  <SelectValue placeholder="Vendor" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All vendors</SelectItem>
                  {(vendorsQuery.data ?? []).map((vendor) => (
                    <SelectItem key={vendor.id} value={vendor.id}>
                      {vendor.companyName || `${vendor.firstName ?? ""} ${vendor.lastName ?? ""}`.trim() || vendor.email || vendor.id}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select
                value={eventFilter}
                onValueChange={(value) => {
                  setEventFilter(value);
                  setCurrentPage(1);
                }}
              >
                <SelectTrigger className="w-52">
                  <SelectValue placeholder="Event" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All events</SelectItem>
                  {(eventsQuery.data ?? []).map((event) => (
                    <SelectItem key={event.id} value={event.id}>
                      {event.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select
                value={sizeFilter}
                onValueChange={(value) => {
                  setSizeFilter(value as "ALL" | StallSizeCategory);
                  setCurrentPage(1);
                }}
              >
                <SelectTrigger className="w-48">
                  <SelectValue placeholder="Stall size" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All sizes</SelectItem>
                  {SIZE_OPTIONS.map((size) => (
                    <SelectItem key={size} value={size}>
                      {size.charAt(0) + size.slice(1).toLowerCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Select
                value={statusFilter}
                onValueChange={(value) => {
                  setStatusFilter(value as "ALL" | (typeof STATUS_OPTIONS)[number]);
                  setCurrentPage(1);
                }}
              >
                <SelectTrigger className="w-48">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All statuses</SelectItem>
                  {STATUS_OPTIONS.map((status) => (
                    <SelectItem key={status} value={status}>
                      {status.charAt(0) + status.slice(1).toLowerCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>All Reservations</CardTitle>
              <CardDescription>
                {filteredReservations.length} reservations tracked via reservation service
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Stall</TableHead>
                      <TableHead>Vendor</TableHead>
                      <TableHead>Event</TableHead>
                      <TableHead>Price</TableHead>
                      <TableHead>Updated</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {reservationsQuery.isLoading && (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center text-muted-foreground">
                          <div className="flex items-center justify-center gap-2">
                            <Loader2 className="h-4 w-4 animate-spin" /> Loading reservations...
                          </div>
                        </TableCell>
                      </TableRow>
                    )}
                    {!reservationsQuery.isLoading && paginatedReservations.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center text-muted-foreground">
                          No reservations found.
                        </TableCell>
                      </TableRow>
                    )}
                    {paginatedReservations.map((reservation) => (
                      <TableRow key={reservation.id}>
                        <TableCell className="font-medium">{reservation.stallCode}</TableCell>
                        <TableCell>
                          <div className="flex flex-col">
                            <span className="font-semibold">
                              {reservation.userFirstName} {reservation.userLastName}
                            </span>
                            <span className="text-xs text-muted-foreground">{reservation.userEmail}</span>
                          </div>
                        </TableCell>
                        <TableCell>
                          {eventNameById[reservation.eventId ?? ""] ?? reservation.eventId ?? "-"}
                        </TableCell>
                        <TableCell className="font-semibold">
                          LKR {reservation.price?.toLocaleString() ?? "-"}
                        </TableCell>
                        <TableCell>{formatDate(reservation.updatedAt ?? reservation.createdAt)}</TableCell>
                        <TableCell>
                          <Badge variant={reservation.status === "CONFIRMED" ? "default" : "secondary"}>
                            {reservation.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            <Button
                              variant="ghost"
                              size="icon"
                              title="Approve Reservation"
                              onClick={() => confirmMutation.mutate(reservation.id)}
                              disabled={
                                confirmMutation.isPending ||
                                !["PENDING", "CANCELLED"].includes(reservation.status)
                              }
                            >
                              {confirmMutation.isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                              ) : (
                                <CheckCircle
                                  className={`h-4 w-4 ${
                                    ["PENDING", "CANCELLED"].includes(reservation.status)
                                      ? "text-green-600"
                                      : "text-muted-foreground"
                                  }`}
                                />
                              )}
                            </Button>
                            <Button
                              variant="ghost"
                              size="icon"
                              title="Reject Reservation"
                              onClick={() => cancelReservationMutation.mutate(reservation.id)}
                              disabled={
                                cancelReservationMutation.isPending ||
                                !["PENDING", "CONFIRMED"].includes(reservation.status)
                              }
                            >
                              {cancelReservationMutation.isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                              ) : (
                                <XCircle
                                  className={`h-4 w-4 ${
                                    ["PENDING", "CONFIRMED"].includes(reservation.status)
                                      ? "text-destructive"
                                      : "text-muted-foreground"
                                  }`}
                                />
                              )}
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              {/* Pagination */}
              <div className="flex items-center justify-between mt-4">
                <p className="text-sm text-muted-foreground">
                  Showing {startIndex + 1} to {Math.min(startIndex + ITEMS_PER_PAGE, filteredReservations.length)} of {filteredReservations.length} reservations
                </p>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                    disabled={currentPage === 1}
                  >
                    <ChevronLeft className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
                    disabled={currentPage === totalPages}
                  >
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
