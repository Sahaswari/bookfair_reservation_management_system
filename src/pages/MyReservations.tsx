import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Download, MapPin, Calendar, AlertCircle } from "lucide-react";
import Header from "@/components/Header";

export default function MyReservations() {
  // Mock data - in real app this would come from backend
  const reservations = [];

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container py-8">
        <div className="max-w-4xl mx-auto space-y-6">
          <div>
            <h1 className="text-4xl font-bold mb-2">My Reservations</h1>
            <p className="text-muted-foreground">
              View and manage your stall reservations for CIBF 2025
            </p>
          </div>

          {reservations.length === 0 ? (
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
                {reservations.map((reservation: any) => (
                  <Card key={reservation.id}>
                    <CardHeader>
                      <div className="flex items-start justify-between">
                        <div>
                          <CardTitle>Reservation #{reservation.id}</CardTitle>
                          <CardDescription className="flex items-center gap-2 mt-1">
                            <Calendar className="h-4 w-4" />
                            Reserved on {reservation.date}
                          </CardDescription>
                        </div>
                        <Badge variant="secondary">Confirmed</Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="grid sm:grid-cols-2 gap-4">
                        <div>
                          <p className="text-sm font-medium text-muted-foreground mb-2">Stalls</p>
                          <div className="flex flex-wrap gap-2">
                            {reservation.stalls.map((stall: string) => (
                              <Badge key={stall} variant="outline">{stall}</Badge>
                            ))}
                          </div>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-muted-foreground mb-2">Total Amount</p>
                          <p className="text-2xl font-bold text-primary">
                            LKR {reservation.totalPrice.toLocaleString()}
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex gap-2 pt-2">
                        <Button variant="outline" className="flex-1">
                          <Download className="h-4 w-4 mr-2" />
                          Download QR Pass
                        </Button>
                        <Button variant="default" className="flex-1">
                          View Details
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>

              {/* Cancellation Policy */}
              <Card className="border-amber-200 dark:border-amber-900 bg-amber-50 dark:bg-amber-950/20">
                <CardContent className="pt-6">
                  <div className="flex items-start gap-3">
                    <AlertCircle className="h-5 w-5 text-amber-600 dark:text-amber-400 mt-0.5" />
                    <div>
                      <p className="font-semibold text-amber-900 dark:text-amber-100">Cancellation Policy</p>
                      <p className="text-sm text-amber-800 dark:text-amber-200 mt-1">
                        Reservations can be cancelled up to 30 days before the event start date (September 15, 2025). 
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
