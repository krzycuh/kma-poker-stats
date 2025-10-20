import React from 'react';
import { Link } from 'react-router-dom';
import type { RecentSessionDto } from '../api/dashboard';
import { formatDateTime, formatGameType, formatProfitCents } from '../utils/format';

interface RecentSessionsListProps {
  sessions: RecentSessionDto[];
  showProfit?: boolean;
}

/**
 * Component to display list of recent sessions
 */
export const RecentSessionsList: React.FC<RecentSessionsListProps> = ({
  sessions,
  showProfit = false,
}) => {
  if (sessions.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">No sessions yet</p>
      </div>
    );
  }

  return (
    <div className="bg-white shadow overflow-hidden sm:rounded-md">
      <ul className="divide-y divide-gray-200">
        {sessions.map((session) => (
          <li key={session.sessionId}>
            <Link
              to={`/sessions/${session.sessionId}`}
              className="block hover:bg-gray-50 transition-colors"
            >
              <div className="px-4 py-4 sm:px-6">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-blue-600 truncate">
                      {formatGameType(session.gameType)} at {session.location}
                    </p>
                    <p className="text-sm text-gray-500">
                      {formatDateTime(session.startTime)} • {session.playerCount}{' '}
                      {session.playerCount === 1 ? 'player' : 'players'}
                    </p>
                  </div>
                  {showProfit && session.personalProfitCents !== null && (
                    <div className="ml-4 flex-shrink-0">
                      <span
                        className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold ${
                          session.isWinning
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {formatProfitCents(session.personalProfitCents)}
                      </span>
                    </div>
                  )}
                </div>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
};
