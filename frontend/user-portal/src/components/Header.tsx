import { Link, useLocation, useNavigate } from "react-router-dom";
import { BookOpen, LogOut } from "lucide-react";
import { Button } from "./ui/button";
import { useAuth } from "@/hooks/useAuth";

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const isLoggedIn = !!user;

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/60">
      <div className="container flex h-16 items-center justify-between">
        <Link to="/" className="flex items-center gap-2">
          <BookOpen className="h-6 w-6 text-primary" />
          <div>
            <h1 className="text-lg font-bold">CIBF 2025</h1>
            <p className="text-xs text-muted-foreground">Book Fair</p>
          </div>
        </Link>

        <nav className="hidden md:flex items-center gap-6">
          {!isLoggedIn ? (
            <>
              <a href="/#about" className="text-sm font-medium hover:text-primary transition-colors">
                About
              </a>
              <a href="/#contact" className="text-sm font-medium hover:text-primary transition-colors">
                Contact
              </a>
            </>
          ) : (
            <>
              <Link to="/dashboard" className="text-sm font-medium hover:text-primary transition-colors">
                Dashboard
              </Link>
              <Link to="/reserve" className="text-sm font-medium hover:text-primary transition-colors">
                Reserve Stall
              </Link>
              <Link to="/my-reservations" className="text-sm font-medium hover:text-primary transition-colors">
                My Reservations
              </Link>
            </>
          )}
        </nav>

        <div className="flex items-center gap-2">
          {isLoggedIn ? (
            <Button variant="default" size="sm" onClick={handleLogout}>
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          ) : (
            <Button size="sm" asChild>
              <Link to="/login">Login</Link>
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
