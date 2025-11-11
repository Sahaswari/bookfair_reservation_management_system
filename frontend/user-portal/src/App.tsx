import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Reserve from "./pages/Reserve";
import Success from "./pages/Success";
import Genres from "./pages/Genres";
import MyReservations from "./pages/MyReservations";
import EmployeeLogin from "./pages/employee/EmployeeLogin";
import EmployeeDashboard from "./pages/employee/EmployeeDashboard";
import StallManagement from "./pages/employee/StallManagement";
import ReservationList from "./pages/employee/ReservationList";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/reserve" element={<Reserve />} />
          <Route path="/success" element={<Success />} />
          <Route path="/genres" element={<Genres />} />
          <Route path="/my-reservations" element={<MyReservations />} />
          <Route path="/employee/login" element={<EmployeeLogin />} />
          <Route path="/employee" element={<EmployeeLogin />} />
          <Route path="/employee/dashboard" element={<EmployeeDashboard />} />
          <Route path="/employee/stalls" element={<StallManagement />} />
          <Route path="/employee/reservations" element={<ReservationList />} />
          {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
