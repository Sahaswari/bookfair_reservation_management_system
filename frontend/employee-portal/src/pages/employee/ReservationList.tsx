import { useState } from "react";
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
import { Search, Eye, XCircle, ChevronLeft, ChevronRight } from "lucide-react";
import Header from "@/components/Header";

export default function ReservationList() {
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  // Mock data - would come from backend in production
  const mockReservations = Array.from({ length: 25 }, (_, i) => ({
    id: `RES${String(i + 1).padStart(4, '0')}`,
    vendor: `Publisher ${i + 1}`,
    stalls: [`A${String(i + 1).padStart(2, '0')}`, `B${String(i + 1).padStart(2, '0')}`],
    status: i % 3 === 0 ? 'Pending Payment' : 'Confirmed',
    date: new Date(2025, 0, 15 + i).toLocaleDateString(),
    totalPrice: 40000 + (i * 1000),
  }));

  const filteredReservations = mockReservations.filter(
    r => r.vendor.toLowerCase().includes(searchQuery.toLowerCase()) ||
         r.id.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const totalPages = Math.ceil(filteredReservations.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedReservations = filteredReservations.slice(startIndex, startIndex + itemsPerPage);

  return (
    <div className="min-h-screen bg-background">
      <Header isEmployee />
      
      <div className="container py-8">
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-bold mb-2">Reservation List</h1>
              <p className="text-muted-foreground">
                Manage all vendor reservations
              </p>
            </div>
            <div className="relative w-64">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search reservations..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>All Reservations</CardTitle>
              <CardDescription>
                {filteredReservations.length} total reservations
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>ID</TableHead>
                      <TableHead>Vendor</TableHead>
                      <TableHead>Stalls</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Date</TableHead>
                      <TableHead>Amount</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {paginatedReservations.map((reservation) => (
                      <TableRow key={reservation.id}>
                        <TableCell className="font-medium">{reservation.id}</TableCell>
                        <TableCell>{reservation.vendor}</TableCell>
                        <TableCell>
                          <div className="flex gap-1">
                            {reservation.stalls.map((stall) => (
                              <Badge key={stall} variant="outline" className="text-xs">
                                {stall}
                              </Badge>
                            ))}
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant={reservation.status === 'Confirmed' ? 'default' : 'secondary'}>
                            {reservation.status}
                          </Badge>
                        </TableCell>
                        <TableCell>{reservation.date}</TableCell>
                        <TableCell className="font-semibold">
                          LKR {reservation.totalPrice.toLocaleString()}
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            <Button variant="ghost" size="icon" title="View Details">
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button variant="ghost" size="icon" title="Cancel Reservation">
                              <XCircle className="h-4 w-4 text-destructive" />
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
                  Showing {startIndex + 1} to {Math.min(startIndex + itemsPerPage, filteredReservations.length)} of {filteredReservations.length} reservations
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
