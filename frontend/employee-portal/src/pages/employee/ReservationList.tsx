import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Search, Eye, XCircle, ChevronLeft, ChevronRight, Loader2 } from "lucide-react";
import Header from "@/components/Header";
import { useEmployeeAuth } from "@/hooks/useEmployeeAuth";
import { stallApi } from "@/lib/stallApi";
import { useToast } from "@/components/ui/use-toast";

const ITEMS_PER_PAGE = 10;

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
  const [vendorFilter, setVendorFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(1);

  const reservationsQuery = useQuery({
    queryKey: ["reserved-stalls", vendorFilter],
    queryFn: () =>
      vendorFilter.trim() ? stallApi.listStallsByVendor(vendorFilter.trim()) : stallApi.listStalls(),
    select: (stalls) => stalls.filter((stall) => stall.isReserved),
  });

  const reservations = reservationsQuery.data ?? [];

  const filteredReservations = useMemo(() => {
    const normalized = searchQuery.trim().toLowerCase();
    if (!normalized) return reservations;

    return reservations.filter((reservation) => {
      const vendor = (reservation.reservedByName ?? reservation.reservedBy ?? "").toLowerCase();
      const code = reservation.stallCode.toLowerCase();
      return vendor.includes(normalized) || code.includes(normalized);
    });
  }, [reservations, searchQuery]);

  const totalPages = Math.ceil(filteredReservations.length / ITEMS_PER_PAGE) || 1;
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const paginatedReservations = filteredReservations.slice(startIndex, startIndex + ITEMS_PER_PAGE);

  const unreserveMutation = useMutation({
    mutationFn: (id: string) => stallApi.unreserveStall(id),
    onSuccess: () => {
      toast({ title: "Reservation released" });
      queryClient.invalidateQueries({ queryKey: ["reserved-stalls"] });
    },
    onError: () => toast({ title: "Failed to release reservation", variant: "destructive" }),
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
              <Input
                placeholder="Filter by vendor ID (calls /vendor endpoint)"
                value={vendorFilter}
                onChange={(e) => {
                  setVendorFilter(e.target.value);
                  setCurrentPage(1);
                }}
              />
            </div>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>All Reservations</CardTitle>
              <CardDescription>
                {filteredReservations.length} reserved stalls
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
                              {reservation.reservedByName || reservation.reservedBy || "Unknown"}
                            </span>
                            {reservation.reservedBy && (
                              <span className="text-xs text-muted-foreground">{reservation.reservedBy}</span>
                            )}
                          </div>
                        </TableCell>
                        <TableCell>{reservation.eventName ?? "-"}</TableCell>
                        <TableCell className="font-semibold">
                          LKR {reservation.price.toLocaleString()}
                        </TableCell>
                        <TableCell>{formatDate(reservation.updatedAt ?? reservation.createdAt)}</TableCell>
                        <TableCell>
                          <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">
                            Confirmed
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            <Button variant="ghost" size="icon" title="View Details" disabled>
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              variant="ghost"
                              size="icon"
                              title="Cancel Reservation"
                              onClick={() => unreserveMutation.mutate(reservation.id)}
                              disabled={unreserveMutation.isPending}
                            >
                              {unreserveMutation.isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                              ) : (
                                <XCircle className="h-4 w-4 text-destructive" />
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
