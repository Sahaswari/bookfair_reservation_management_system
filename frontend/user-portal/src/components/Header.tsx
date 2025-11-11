import { Link, useLocation, useNavigate } from "react-router-dom";
import { BookOpen, LogOut } from "lucide-react";
import { Button } from "./ui/button";

interface HeaderProps {
  isEmployee?: boolean;
}

export default function Header({ isEmployee = false }: HeaderProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const isLoggedIn = location.pathname.includes("/dashboard") || location.pathname.includes("/employee");

  const handleLogout = () => {
    if (isEmployee) {
      localStorage.removeItem('employeeUser');
      localStorage.removeItem('employeeEmail');
      navigate('/employee/login');
    } else {
      localStorage.removeItem('publicUser');
      localStorage.removeItem('publicEmail');
      navigate('/');
    }
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/60">
      <div className="container flex h-16 items-center justify-between">
        <Link to={isEmployee ? "/employee" : "/"} className="flex items-center gap-2">
          <BookOpen className={isEmployee ? "h-6 w-6 text-employee" : "h-6 w-6 text-primary"} />
          <div>
            <h1 className="text-lg font-bold">CIBF 2025</h1>
            <p className="text-xs text-muted-foreground">{isEmployee ? "Organiser Portal" : "Book Fair"}</p>
          </div>
        </Link>

        <nav className="hidden md:flex items-center gap-6">
          {!isEmployee && !isLoggedIn && (
            <>
              <Link to="/#about" className="text-sm font-medium hover:text-primary transition-colors">
                About
              </Link>
              <Link to="/#contact" className="text-sm font-medium hover:text-primary transition-colors">
                Contact
              </Link>
            </>
          )}
          
          {isLoggedIn && !isEmployee && (
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
          
          {isLoggedIn && isEmployee && (
            <>
              <Link to="/employee/dashboard" className="text-sm font-medium hover:text-employee transition-colors">
                Dashboard
              </Link>
              <Link to="/employee/stalls" className="text-sm font-medium hover:text-employee transition-colors">
                Stall Management
              </Link>
              <Link to="/employee/reservations" className="text-sm font-medium hover:text-employee transition-colors">
                Reservations
              </Link>
            </>
          )}
        </nav>

        <div className="flex items-center gap-2">
          {isLoggedIn ? (
            <Button variant={isEmployee ? "employee" : "default"} size="sm" onClick={handleLogout}>
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          ) : (
            <Button variant={isEmployee ? "employee" : "default"} size="sm" asChild>
              <Link to={isEmployee ? "/employee/login" : "/login"}>Login</Link>
            </Button>
          )}
        </div>
      </div>
    </header>
  );
}
