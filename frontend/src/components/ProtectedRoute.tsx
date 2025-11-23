import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { UserRole } from '../types/auth';

/**
 * Protected route wrapper component
 * Redirects to login if user is not authenticated
 * Optionally checks for required role
 */

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireRole?: UserRole;
  requireLinkedPlayer?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireRole,
  requireLinkedPlayer = false,
}) => {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requireRole && user?.role !== requireRole) {
    return <Navigate to="/" replace />;
  }

  if (requireLinkedPlayer && !user?.linkedPlayerId) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};
