import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { BookOpen, Shield } from "lucide-react";
import { toast } from "sonner";
import loginImage from "@/assets/employee-login.jpg";

export default function EmployeeLogin() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email || !password) {
      toast.error("Please fill in all fields");
      return;
    }
    
    // Store employee session (separate from public users)
    localStorage.setItem("employeeUser", "true");
    localStorage.setItem("employeeEmail", email);
    
    toast.success("Login successful!");
    navigate("/dashboard");
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4">
      <div className="w-full max-w-5xl grid grid-cols-1 md:grid-cols-2 rounded-3xl overflow-hidden shadow-xl bg-card">

        {/* LEFT PANEL */}
        <div className="p-10 space-y-8 flex flex-col justify-center">

          {/* ----- HEADING ----- */}
          <div className="text-center space-y-2">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-employee/10 mb-4">
              <BookOpen className="h-8 w-8 text-employee" />
            </div>
            <h1 className="text-3xl font-bold">CIBF Organiser Portal</h1>
            <p className="text-muted-foreground">Staff and administrator access only</p>
          </div>

          {/* ----- LOGIN CARD ----- */}
          <Card className="rounded-2xl shadow-lg border bg-card">
            <CardHeader>
              <div className="flex items-center gap-2">
                <Shield className="h-5 w-5 text-employee" />
                <CardTitle>Employee Login</CardTitle>
              </div>
              <CardDescription>
                Enter your credentials to access the admin dashboard
              </CardDescription>
            </CardHeader>

            <CardContent>
              <form onSubmit={handleLogin} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="staff@slbpa.lk"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="password">Password</Label>
                    <a href="#" className="text-sm text-employee hover:underline">
                      Forgot password?
                    </a>
                  </div>
                  <Input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>

                <Button type="submit" variant="employee" className="w-full" size="lg">
                  Sign In
                </Button>
              </form>
            </CardContent>
          </Card>

          <p className="text-center text-sm text-muted-foreground">
            Need vendor access? Contact the SLBPA digital team.
          </p>
        </div>

        {/* RIGHT IMAGE */}
        <div className="hidden md:block relative">
          <img
            src={loginImage}
            alt="Employee Login Visual"
            className="w-full h-full object-cover"
          />
        </div>
      </div>
    </div>
  );
}
