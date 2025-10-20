import React from 'react';
import { useAuth } from '../hooks/useAuth';
import { Link } from 'react-router-dom';
import { UserRole } from '../types/auth';

/**
 * Dashboard page component
 * Main landing page after login
 */

export const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();

  if (!user) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-bold text-gray-900">Poker Stats</h1>
            </div>
            <div className="flex items-center space-x-4">
              <Link to="/profile" className="text-gray-700 hover:text-gray-900">
                Profile
              </Link>
              {user.role === UserRole.ADMIN && (
                <Link to="/players" className="text-gray-700 hover:text-gray-900">
                  Players
                </Link>
              )}
              <button
                onClick={logout}
                className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
        <div className="bg-white overflow-hidden shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              Welcome, {user.name}!
            </h2>
            <div className="space-y-4">
              <div>
                <p className="text-gray-600">Email: {user.email}</p>
                <p className="text-gray-600">
                  Role: {user.role === 'ADMIN' ? 'Administrator' : 'Casual Player'}
                </p>
              </div>
              
              <div className="border-t border-gray-200 pt-4">
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  Phase 1 Authentication Complete! 🎉
                </h3>
                <p className="text-gray-600">
                  The authentication system is now fully functional. You can:
                </p>
                <ul className="mt-2 list-disc list-inside text-gray-600 space-y-1">
                  <li>Register new accounts</li>
                  <li>Login with JWT authentication</li>
                  <li>Update your profile</li>
                  <li>Change your password</li>
                  <li>Automatic token refresh</li>
                </ul>
                
                <div className="mt-4 p-4 bg-blue-50 rounded-md">
                  <p className="text-sm text-blue-800">
                    <strong>Next steps:</strong> Phase 2 will implement Player Management and Core Data Models.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
