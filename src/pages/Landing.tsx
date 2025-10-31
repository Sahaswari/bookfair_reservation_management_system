import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Checkbox } from "@/components/ui/checkbox";
import { BookOpen, MapPin, Calendar, Users } from "lucide-react";
import Header from "@/components/Header";
import heroImage from "@/assets/hero-bookfair.jpg";
import { toast } from "sonner";

export default function Landing() {
  const navigate = useNavigate();
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [regBusinessName, setRegBusinessName] = useState("");
  const [regContactPerson, setRegContactPerson] = useState("");
  const [regEmail, setRegEmail] = useState("");
  const [regPhone, setRegPhone] = useState("");
  const [regPassword, setRegPassword] = useState("");
  const [regConfirmPassword, setRegConfirmPassword] = useState("");
  const [acceptedTerms, setAcceptedTerms] = useState(false);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!loginEmail || !loginPassword) {
      toast.error("Please fill in all fields");
      return;
    }
    toast.success("Login successful!");
    navigate("/dashboard");
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!regBusinessName || !regContactPerson || !regEmail || !regPhone || !regPassword) {
      toast.error("Please fill in all required fields");
      return;
    }
    
    if (regPassword !== regConfirmPassword) {
      toast.error("Passwords do not match");
      return;
    }
    
    if (regPassword.length < 8) {
      toast.error("Password must be at least 8 characters");
      return;
    }
    
    if (!acceptedTerms) {
      toast.error("Please accept the terms and conditions");
      return;
    }
    
    toast.success("Registration successful! Welcome to CIBF!");
    navigate("/dashboard");
  };

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      {/* Hero Section */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 gradient-warm opacity-10"></div>
        <div className="container relative py-20 md:py-32">
          <div className="grid md:grid-cols-2 gap-12 items-center">
            <div className="space-y-6">
              <h1 className="text-5xl md:text-6xl font-bold leading-tight">
                Colombo International Book Fair <span className="text-primary">2025</span>
              </h1>
              <p className="text-xl text-muted-foreground">
                Sri Lanka's largest book exhibition at BMICH. Reserve your stall today and showcase your publications to thousands of book lovers.
              </p>
              <div className="flex flex-wrap gap-4">
                <Button variant="hero" size="lg" onClick={() => setShowAuthModal(true)}>
                  Reserve Your Stall
                </Button>
                <Button variant="outline" size="lg" asChild>
                  <a href="#about">Learn More</a>
                </Button>
              </div>
              <div className="flex flex-wrap gap-6 pt-4">
                <div className="flex items-center gap-2">
                  <Calendar className="h-5 w-5 text-primary" />
                  <span className="text-sm">Sept 15-25, 2025</span>
                </div>
                <div className="flex items-center gap-2">
                  <MapPin className="h-5 w-5 text-primary" />
                  <span className="text-sm">BMICH, Colombo</span>
                </div>
                <div className="flex items-center gap-2">
                  <Users className="h-5 w-5 text-primary" />
                  <span className="text-sm">100+ Publishers</span>
                </div>
              </div>
            </div>
            
            <div className="relative">
              <img 
                src={heroImage} 
                alt="Colombo International Book Fair" 
                className="rounded-2xl shadow-2xl w-full"
              />
            </div>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-20 bg-card">
        <div className="container">
          <div className="max-w-3xl mx-auto text-center space-y-6">
            <BookOpen className="h-12 w-12 text-primary mx-auto" />
            <h2 className="text-4xl font-bold">About CIBF</h2>
            <p className="text-lg text-muted-foreground">
              The Colombo International Book Fair, organized by the Sri Lanka Book Publishers' Association, 
              is the country's premier literary event. Held annually at the Bandaranaike Memorial International 
              Conference Hall (BMICH), it brings together publishers, authors, and book enthusiasts from across the globe.
            </p>
          </div>
          
          <div className="grid md:grid-cols-3 gap-8 mt-16">
            <Card>
              <CardHeader>
                <CardTitle>Easy Reservation</CardTitle>
                <CardDescription>Book your stall online in minutes</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Choose from small, medium, or large stalls with our interactive map. Reserve up to 3 stalls per vendor.
                </p>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader>
                <CardTitle>Prime Location</CardTitle>
                <CardDescription>BMICH exhibition hall</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Benefit from the prestigious venue with excellent foot traffic and visibility for your publications.
                </p>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader>
                <CardTitle>Digital Pass</CardTitle>
                <CardDescription>QR code confirmation</CardDescription>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground">
                  Receive instant confirmation with a QR code pass via email for seamless event entry.
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Contact Section */}
      <section id="contact" className="py-20">
        <div className="container max-w-2xl">
          <div className="text-center space-y-4 mb-12">
            <h2 className="text-4xl font-bold">Get in Touch</h2>
            <p className="text-muted-foreground">
              Have questions? Contact the Sri Lanka Book Publishers' Association
            </p>
          </div>
          
          <Card>
            <CardContent className="pt-6 space-y-4">
              <div>
                <p className="font-semibold">Email</p>
                <p className="text-muted-foreground">info@slbpa.lk</p>
              </div>
              <div>
                <p className="font-semibold">Phone</p>
                <p className="text-muted-foreground">+94 11 234 5678</p>
              </div>
              <div>
                <p className="font-semibold">Address</p>
                <p className="text-muted-foreground">
                  Sri Lanka Book Publishers' Association<br />
                  Colombo 07, Sri Lanka
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      {/* Auth Modal */}
      {showAuthModal && (
        <div className="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm flex items-center justify-center p-4">
          <Card className="w-full max-w-md max-h-[90vh] overflow-y-auto">
            <CardHeader>
              <CardTitle>Vendor Portal</CardTitle>
              <CardDescription>Login or register to reserve your stall</CardDescription>
            </CardHeader>
            <CardContent>
              <Tabs defaultValue="login">
                <TabsList className="grid w-full grid-cols-2">
                  <TabsTrigger value="login">Login</TabsTrigger>
                  <TabsTrigger value="register">Register</TabsTrigger>
                </TabsList>
                
                <TabsContent value="login">
                  <form onSubmit={handleLogin} className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="login-email">Email</Label>
                      <Input 
                        id="login-email" 
                        type="email" 
                        value={loginEmail}
                        onChange={(e) => setLoginEmail(e.target.value)}
                        required 
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="login-password">Password</Label>
                      <Input 
                        id="login-password" 
                        type="password" 
                        value={loginPassword}
                        onChange={(e) => setLoginPassword(e.target.value)}
                        required 
                      />
                    </div>
                    <div className="flex justify-between gap-2">
                      <Button type="button" variant="outline" onClick={() => setShowAuthModal(false)}>
                        Cancel
                      </Button>
                      <Button type="submit">Login</Button>
                    </div>
                  </form>
                </TabsContent>
                
                <TabsContent value="register">
                  <form onSubmit={handleRegister} className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="reg-business">Business Name *</Label>
                      <Input 
                        id="reg-business" 
                        value={regBusinessName}
                        onChange={(e) => setRegBusinessName(e.target.value)}
                        required 
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="reg-contact">Contact Person *</Label>
                      <Input 
                        id="reg-contact" 
                        value={regContactPerson}
                        onChange={(e) => setRegContactPerson(e.target.value)}
                        required 
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="reg-email">Email *</Label>
                      <Input 
                        id="reg-email" 
                        type="email" 
                        value={regEmail}
                        onChange={(e) => setRegEmail(e.target.value)}
                        required 
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="reg-phone">Phone *</Label>
                      <Input 
                        id="reg-phone" 
                        type="tel" 
                        value={regPhone}
                        onChange={(e) => setRegPhone(e.target.value)}
                        required 
                      />
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="reg-password">Password *</Label>
                      <Input 
                        id="reg-password" 
                        type="password" 
                        value={regPassword}
                        onChange={(e) => setRegPassword(e.target.value)}
                        required 
                      />
                      <p className="text-xs text-muted-foreground">Minimum 8 characters</p>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="reg-confirm">Confirm Password *</Label>
                      <Input 
                        id="reg-confirm" 
                        type="password" 
                        value={regConfirmPassword}
                        onChange={(e) => setRegConfirmPassword(e.target.value)}
                        required 
                      />
                    </div>
                    <div className="flex items-start space-x-2">
                      <Checkbox 
                        id="terms" 
                        checked={acceptedTerms}
                        onCheckedChange={(checked) => setAcceptedTerms(checked as boolean)}
                      />
                      <label htmlFor="terms" className="text-sm text-muted-foreground leading-tight">
                        I accept the terms and conditions and privacy policy
                      </label>
                    </div>
                    <div className="flex justify-between gap-2">
                      <Button type="button" variant="outline" onClick={() => setShowAuthModal(false)}>
                        Cancel
                      </Button>
                      <Button type="submit">Register</Button>
                    </div>
                  </form>
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
