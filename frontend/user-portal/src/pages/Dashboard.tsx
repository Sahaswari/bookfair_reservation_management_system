import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { BookOpen, CheckCircle2, MapPin, Tag } from "lucide-react";
import Header from "@/components/Header";
import { usePublicAuth } from "@/hooks/usePublicAuth";

export default function Dashboard() {
  const { isAuthenticated } = usePublicAuth();
  const reservationCount = 0;
  const maxReservations = 3;
  const progressPercent = (reservationCount / maxReservations) * 100;

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container py-8">
        <div className="max-w-4xl mx-auto space-y-8">
          {/* Welcome Card */}
          <Card className="gradient-warm text-primary-foreground">
            <CardContent className="pt-6">
              <div className="flex items-start justify-between">
                <div>
                  <h1 className="text-3xl font-bold mb-2">Welcome to CIBF 2025!</h1>
                  <p className="text-primary-foreground/90">
                    Manage your stall reservations and get ready for Sri Lanka's biggest book fair.
                  </p>
                </div>
                <BookOpen className="h-12 w-12 opacity-90" />
              </div>
            </CardContent>
          </Card>

          {/* Reservation Status */}
          <Card>
            <CardHeader>
              <CardTitle>Reservation Status</CardTitle>
              <CardDescription>
                You can reserve up to {maxReservations} stalls
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-2xl font-bold">{reservationCount} / {maxReservations}</span>
                <span className="text-sm text-muted-foreground">Stalls Reserved</span>
              </div>
              <Progress value={progressPercent} className="h-2" />
              {reservationCount === 0 && (
                <div className="bg-muted rounded-lg p-4">
                  <p className="text-sm text-muted-foreground">
                    You haven't reserved any stalls yet. Start by browsing available stalls on our interactive map.
                  </p>
                </div>
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
          {reservationCount === 0 && (
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
