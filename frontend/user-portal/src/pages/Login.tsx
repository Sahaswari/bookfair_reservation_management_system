import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { BookOpen } from "lucide-react";
import { Link } from "react-router-dom";
import { toast } from "sonner";
import loginImage from "@/assets/login.jpg";

export default function Login() {
  const navigate = useNavigate();
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [regEmail, setRegEmail] = useState("");
  const [regPassword, setRegPassword] = useState("");
  const [regConfirmPassword, setRegConfirmPassword] = useState("");
  const [businessName, setBusinessName] = useState("");
  const [contactPerson, setContactPerson] = useState("");
  const [phone, setPhone] = useState("");
  const [acceptTerms, setAcceptTerms] = useState(false);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!loginEmail || !loginPassword) {
      toast.error("Please fill in all fields");
      return;
    }
    
    // Store public user session
    localStorage.setItem('publicUser', 'true');
    localStorage.setItem('publicEmail', loginEmail);
    
    toast.success("Login successful!");
    navigate("/dashboard");
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    if (!regEmail || !regPassword || !regConfirmPassword || !businessName || !contactPerson || !phone) {
      toast.error("Please fill in all fields");
      return;
    }
    if (regPassword !== regConfirmPassword) {
      toast.error("Passwords do not match");
      return;
    }
    if (!acceptTerms) {
      toast.error("Please accept the terms and conditions");
      return;
    }
    
    // Store public user session
    localStorage.setItem('publicUser', 'true');
    localStorage.setItem('publicEmail', regEmail);
    
    toast.success("Registration successful!");
    navigate("/dashboard");
  };

  return (
  <div className="min-h-screen bg-background">
    {/* Header (unchanged) */}
    <header className="sticky top-0 z-50 w-full border-b bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/60">
      <div className="container flex h-16 items-center justify-between">
        <Link to="/" className="flex items-center gap-2">
          <BookOpen className="h-6 w-6 text-primary" />
          <div>
            <h1 className="text-lg font-bold">CIBF 2025</h1>
            <p className="text-xs text-muted-foreground">Book Fair</p>
          </div>
        </Link>
        <Link to="/">
          <Button variant="ghost" size="sm">Back to Home</Button>
        </Link>
      </div>
    </header>

    {/* ======== New Layout (Two Panel) ======== */}
    <div className="w-full flex items-center justify-center py-10">
      <div className="w-full max-w-5xl grid grid-cols-1 md:grid-cols-2 rounded-3xl overflow-hidden shadow-xl bg-card">

        {/* ========= LEFT: Login Card ========= */}
        <div className="p-10 bg-gradient-to-b from-card to-background">
          <Card className="border-none shadow-none bg-transparent">
            <CardHeader className="text-left px-0">
              <CardTitle className="text-2xl">Vendor Portal</CardTitle>
              <CardDescription>Login or register to reserve your stall</CardDescription>
            </CardHeader>

            <CardContent className="px-0">
              <Tabs defaultValue="login" className="w-full">
                <TabsList className="grid w-full grid-cols-2">
                  <TabsTrigger value="login">Login</TabsTrigger>
                  <TabsTrigger value="register">Register</TabsTrigger>
                </TabsList>

                {/* Login */}
                <TabsContent value="login">
                  <form onSubmit={handleLogin} className="space-y-4 mt-4">
                    <div className="space-y-2">
                      <Label>Email</Label>
                      <Input
                        type="email"
                        placeholder="vendor@example.com"
                        value={loginEmail}
                        onChange={(e) => setLoginEmail(e.target.value)}
                      />
                    </div>

                    <div className="space-y-2">
                      <Label>Password</Label>
                      <Input
                        type="password"
                        value={loginPassword}
                        onChange={(e) => setLoginPassword(e.target.value)}
                      />
                    </div>

                    <Button type="submit" className="w-full">Login</Button>
                  </form>
                </TabsContent>

                {/* Register */}
                <TabsContent value="register">
                  <form onSubmit={handleRegister} className="space-y-4 mt-4">
                    <div className="space-y-2">
                      <Label>Business Name</Label>
                      <Input
                        placeholder="Your Publishing House"
                        value={businessName}
                        onChange={(e) => setBusinessName(e.target.value)}
                      />
                    </div>

                    <div className="space-y-2">
                      <Label>Contact Person</Label>
                      <Input
                        placeholder="John Doe"
                        value={contactPerson}
                        onChange={(e) => setContactPerson(e.target.value)}
                      />
                    </div>

                    <div className="space-y-2">
                      <Label>Phone</Label>
                      <Input
                        type="tel"
                        placeholder="+94 XX XXX XXXX"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                      />
                    </div>

                    <div className="space-y-2">
                      <Label>Email</Label>
                      <Input
                        type="email"
                        placeholder="vendor@example.com"
                        value={regEmail}
                        onChange={(e) => setRegEmail(e.target.value)}
                      />
                    </div>

                    <div className="space-y-2">
                      <Label>Password</Label>
                      <Input
                        type="password"
                        value={regPassword}
                        onChange={(e) => setRegPassword(e.target.value)}
                      />
                    </div>

                    <div className="space-y-2">
                      <Label>Confirm Password</Label>
                      <Input
                        type="password"
                        value={regConfirmPassword}
                        onChange={(e) => setRegConfirmPassword(e.target.value)}
                      />
                    </div>

                    <div className="flex items-center space-x-2">
                      <Checkbox
                        checked={acceptTerms}
                        onCheckedChange={(c) => setAcceptTerms(c as boolean)}
                      />
                      <Label className="text-sm">I accept the terms and conditions</Label>
                    </div>

                    <Button type="submit" className="w-full">Register</Button>
                  </form>
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>
        </div>

        {/* ========= RIGHT: Image Panel ========= */}
        <div className="hidden md:block relative">
          <img
            src={loginImage}
            alt="Login visual"
            className="w-full h-full object-cover"
          />
        </div>

      </div>
    </div>
  </div>
);

}
