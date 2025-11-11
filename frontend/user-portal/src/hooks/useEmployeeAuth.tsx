import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export const useEmployeeAuth = () => {
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userEmail, setUserEmail] = useState<string | null>(null);

  useEffect(() => {
    const employeeUser = localStorage.getItem('employeeUser');
    const employeeEmail = localStorage.getItem('employeeEmail');
    
    if (employeeUser === 'true' && employeeEmail) {
      setIsAuthenticated(true);
      setUserEmail(employeeEmail);
    } else {
      navigate('/employee/login');
    }
  }, [navigate]);

  const logout = () => {
    localStorage.removeItem('employeeUser');
    localStorage.removeItem('employeeEmail');
    setIsAuthenticated(false);
    setUserEmail(null);
    navigate('/employee/login');
  };

  return { isAuthenticated, userEmail, logout };
};
