import { Link, useLocation, useNavigate } from "react-router-dom";
import { BookOpen, LogOut } from "lucide-react";
import { Button } from "./ui/button";

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const isLoggedIn = localStorage.getItem("employeeUser") === "true";
  const isAuthScreen = location.pathname === "/" || location.pathname === "/login";

  const handleLogout = () => {
    localStorage.removeItem("employeeUser");
    localStorage.removeItem("employeeEmail");
    navigate("/login");
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/60">
      <div className="container flex h-16 items-center justify-between">
        <Link to={isLoggedIn ? "/dashboard" : "/login"} className="flex items-center gap-2">
          <BookOpen className="h-6 w-6 text-employee" />
          <div>
            <h1 className="text-lg font-bold">CIBF 2025</h1>
            <p className="text-xs text-muted-foreground">Organiser Portal</p>
          </div>
        </Link>

        {isLoggedIn && !isAuthScreen && (
          <nav className="hidden md:flex items-center gap-6">
            <Link to="/dashboard" className="text-sm font-medium hover:text-employee transition-colors">
              Dashboard
            </Link>
            <Link to="/stalls" className="text-sm font-medium hover:text-employee transition-colors">
              Stall Management
            </Link>
            <Link to="/reservations" className="text-sm font-medium hover:text-employee transition-colors">
              Reservations
            </Link>
          </nav>
        )}

        <div className="flex items-center gap-2">
          {isLoggedIn ? (
            <Button variant="employee" size="sm" onClick={handleLogout}>
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          ) : (
            <Button variant="employee" size="sm" asChild>
              <Link to="/login">Login</Link>
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
