import { useLocation, Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { CheckCircle, Mail, QrCode } from "lucide-react";
import Header from "@/components/Header";

export default function Success() {
  const location = useLocation();
  const { stalls = [], totalPrice = 0 } = location.state || {};

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container py-16">
        <div className="max-w-2xl mx-auto space-y-8">
          <div className="text-center space-y-4">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary/10">
              <CheckCircle className="h-8 w-8 text-primary" />
            </div>
            <h1 className="text-4xl font-bold">Reservation Successful!</h1>
            <p className="text-lg text-muted-foreground">
              Your stalls have been successfully reserved for CIBF 2025
            </p>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Reservation Details</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <p className="text-sm font-medium text-muted-foreground">Reserved Stalls</p>
                <div className="grid grid-cols-2 gap-2">
                  {stalls.map((stall: any) => (
                    <div key={stall.id} className="bg-muted rounded-lg p-3">
                      <p className="font-semibold">{stall.label}</p>
                      <p className="text-sm text-muted-foreground capitalize">{stall.size} stall</p>
                      <p className="text-sm font-medium mt-1">LKR {stall.price.toLocaleString()}</p>
                    </div>
                  ))}
                </div>
              </div>

              <div className="pt-4 border-t">
                <div className="flex justify-between items-center">
                  <span className="text-lg font-semibold">Total Amount</span>
                  <span className="text-2xl font-bold text-primary">LKR {totalPrice.toLocaleString()}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-primary/20 bg-primary/5">
            <CardContent className="pt-6 space-y-4">
              <div className="flex items-start gap-3">
                <Mail className="h-5 w-5 text-primary mt-0.5" />
                <div>
                  <p className="font-semibold">Confirmation Email</p>
                  <p className="text-sm text-muted-foreground">
                    A confirmation email with payment instructions and your QR pass will be sent to your registered email within 24 hours.
                  </p>
                </div>
              </div>
              
              <div className="flex items-start gap-3">
                <QrCode className="h-5 w-5 text-primary mt-0.5" />
                <div>
                  <p className="font-semibold">QR Code Pass</p>
                  <p className="text-sm text-muted-foreground">
                    Your unique QR code will grant you entry to the event and access to your reserved stalls. Please save it on your mobile device.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Next Steps</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-start gap-2">
                <div className="flex-shrink-0 w-6 h-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-sm font-semibold">
                  1
                </div>
                <p className="text-sm">Complete payment within 7 days via bank transfer (details in email)</p>
              </div>
              <div className="flex items-start gap-2">
                <div className="flex-shrink-0 w-6 h-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-sm font-semibold">
                  2
                </div>
                <p className="text-sm">Add your literary genres to help visitors find your stall</p>
              </div>
              <div className="flex items-start gap-2">
                <div className="flex-shrink-0 w-6 h-6 rounded-full bg-primary text-primary-foreground flex items-center justify-center text-sm font-semibold">
                  3
                </div>
                <p className="text-sm">Set up your stall from September 13, 2025</p>
              </div>
            </CardContent>
          </Card>

          <div className="flex flex-col sm:flex-row gap-4">
            <Button variant="default" size="lg" className="flex-1" asChild>
              <Link to="/dashboard">Go to Dashboard</Link>
            </Button>
            <Button variant="hero" size="lg" className="flex-1" asChild>
              <Link to="/genres">Add Genres Now</Link>
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
