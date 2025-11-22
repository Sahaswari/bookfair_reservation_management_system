import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

export const usePublicAuth = () => {
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userEmail, setUserEmail] = useState<string | null>(null);

  useEffect(() => {
    const publicUser = localStorage.getItem('publicUser');
    const publicEmail = localStorage.getItem('publicEmail');
    
    if (publicUser === 'true' && publicEmail) {
      setIsAuthenticated(true);
      setUserEmail(publicEmail);
    } else {
      navigate('/login');
    }
  }, [navigate]);

  const logout = () => {
    localStorage.removeItem('publicUser');
    localStorage.removeItem('publicEmail');
    setIsAuthenticated(false);
    setUserEmail(null);
    navigate('/');
  };

  return { isAuthenticated, userEmail, logout };
};
