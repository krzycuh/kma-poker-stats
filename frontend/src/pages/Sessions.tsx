import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { sessionApi } from '../api/sessions';
import { LoadingSkeleton } from '../components/LoadingSkeleton';
import { EmptyState } from '../components/EmptyState';
import {
  formatDateTime,
  formatGameType,
  formatCents,
} from '../utils/format';
import { PageHeader } from '../components/PageHeader';

/**
 * Sessions history page
 * Shows list of all game sessions with filtering and sorting
 */
export const Sessions: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');

  // Fetch all sessions
  const { data: sessions, isLoading, error } = useQuery({
    queryKey: ['sessions'],
    queryFn: sessionApi.getAll,
  });

  // Filter sessions based on search term
  const filteredSessions = sessions?.filter((session) =>
    session.location.toLowerCase().includes(searchTerm.toLowerCase()) ||
    formatGameType(session.gameType)
      .toLowerCase()
      .includes(searchTerm.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-gray-50 pt-4 pb-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <PageHeader
          title="Session History"
          description="Review and manage all logged sessions"
          actions={
            <div className="flex flex-wrap gap-3">
              <Link
                to="/log-session"
                className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
              >
                Log New Session
              </Link>
            </div>
          }
        />

        {/* Search */}
        <div className="mb-6">
          <label htmlFor="search" className="sr-only">
            Search sessions
          </label>
          <input
            type="text"
            id="search"
            placeholder="Search by location or game type..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="block w-full px-4 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        {/* Loading State */}
        {isLoading && <LoadingSkeleton variant="table" />}

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">
              Error loading sessions. Please try again.
            </p>
          </div>
        )}

        {/* Sessions List */}
        {filteredSessions && filteredSessions.length > 0 && (
          <div className="bg-white shadow overflow-hidden sm:rounded-md">
            <ul className="divide-y divide-gray-200">
              {filteredSessions.map((session) => (
                <li key={session.id}>
                  <Link
                    to={`/sessions/${session.id}`}
                    className="block hover:bg-gray-50 transition-colors"
                  >
                    <div className="px-4 py-4 sm:px-6">
                      <div className="flex items-center justify-between">
                        <div className="flex-1">
                          <div className="flex items-center">
                            <p className="text-lg font-medium text-blue-600 truncate">
                              {formatGameType(session.gameType)}
                            </p>
                            <span className="ml-3 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                              {session.location}
                            </span>
                          </div>
                          <div className="mt-2 flex items-center text-sm text-gray-500">
                            <span>{formatDateTime(session.startTime)}</span>
                            <span className="mx-2">•</span>
                            <span>Min Buy-in: {formatCents(session.minBuyInCents)}</span>
                          </div>
                        </div>
                        <div className="ml-4 flex-shrink-0">
                          <svg
                            className="h-5 w-5 text-gray-400"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M9 5l7 7-7 7"
                            />
                          </svg>
                        </div>
                      </div>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* Empty State */}
        {filteredSessions && filteredSessions.length === 0 && !searchTerm && (
          <EmptyState
            icon="🎲"
            title="No Sessions Yet"
            description="Start by logging your first poker session!"
            action={{
              label: 'Log Your First Session',
              onClick: () => window.location.href = '/log-session',
            }}
          />
        )}
        {filteredSessions && filteredSessions.length === 0 && searchTerm && (
          <EmptyState
            icon="🔍"
            title="No Matches Found"
            description={`No sessions found matching "${searchTerm}". Try a different search term.`}
          />
        )}
      </div>
    </div>
  );
};
