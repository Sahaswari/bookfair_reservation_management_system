import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { clearSession, getEmployeeEmail, hasEmployeeSession } from "@/lib/api";

export const useEmployeeAuth = () => {
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userEmail, setUserEmail] = useState<string | null>(null);

  useEffect(() => {
    const hasSession = hasEmployeeSession();
    const email = getEmployeeEmail();

    if (hasSession && email) {
      setIsAuthenticated(true);
      setUserEmail(email);
    } else {
      navigate("/login");
    }
  }, [navigate]);

  const logout = () => {
    clearSession();
    setIsAuthenticated(false);
    setUserEmail(null);
    navigate("/login");
  };

  return { isAuthenticated, userEmail, logout };
};
