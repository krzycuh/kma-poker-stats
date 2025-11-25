import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { sessionApi } from '../api/sessions';
import { useAuth } from '../hooks/useAuth';
import { UserRole } from '../types/auth';
import { ConfirmationModal } from '../components/ConfirmationModal';
import { useToast } from '../hooks/useToast';
import {
  formatDateTime,
  formatGameType,
  formatCents,
  formatProfitCents,
} from '../utils/format';
import type { SessionResult } from '../types/gameSession';

/**
 * Session detail page
 * Shows detailed information about a specific game session
 */
export const SessionDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const toast = useToast();
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const isAdmin = user?.role === UserRole.ADMIN;

  // Fetch session details
  const { data: session, isLoading, error } = useQuery({
    queryKey: ['session', id],
    queryFn: () => sessionApi.getById(id!),
    enabled: !!id,
  });

  const renderPlayerInfo = (result: SessionResult) => {
    const fallback = `Player ${result.playerId.substring(0, 6)}`;
    const label = result.playerName || fallback;
    const initials = result.playerName
      ? result.playerName
          .split(' ')
          .filter(Boolean)
          .map((part) => part[0])
          .join('')
          .slice(0, 2)
          .toUpperCase()
      : fallback
          .replace('Player ', '')
          .slice(0, 2)
          .toUpperCase();

    return (
      <div className="flex items-center gap-3">
        {result.playerAvatarUrl ? (
          <img
            src={result.playerAvatarUrl}
            alt={`${label} avatar`}
            className="h-10 w-10 rounded-full object-cover"
          />
        ) : (
          <div className="h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center text-sm font-semibold text-gray-600">
            {initials || 'PL'}
          </div>
        )}
        <div className="min-w-0">
          <p className="font-medium text-gray-900 truncate">{label}</p>
          <p className="text-xs text-gray-500 truncate">
            {result.linkedUserId ? 'Linked user' : 'Unlinked player'}
          </p>
        </div>
      </div>
    );
  };

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (sessionId: string) => sessionApi.delete(sessionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sessions'] });
      toast.success('Session deleted successfully');
      navigate('/sessions');
    },
    onError: () => {
      toast.error('Failed to delete session. Please try again.');
    },
  });

  const handleDelete = () => {
    deleteMutation.mutate(id!);
    setShowDeleteModal(false);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading session details...</p>
        </div>
      </div>
    );
  }

  if (error || !session) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">Session not found or error loading.</p>
            <Link
              to="/sessions"
              className="mt-4 inline-block text-blue-600 hover:text-blue-800"
            >
              ← Back to Sessions
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // Calculate totals
  const totalBuyIn = session.results.reduce(
    (sum, r) => sum + r.buyInCents,
    0
  );
  const totalCashOut = session.results.reduce(
    (sum, r) => sum + r.cashOutCents,
    0
  );
  const isBalanced = totalBuyIn === totalCashOut;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="md:flex md:items-center md:justify-between">
            <div className="flex-1 min-w-0">
              <Link
                to="/sessions"
                className="text-sm text-gray-500 hover:text-gray-700 mb-2 inline-block"
              >
                ← Back to Sessions
              </Link>
              <h1 className="text-2xl font-bold text-gray-900">
                {formatGameType(session.session.gameType)} Session
              </h1>
              <p className="mt-1 text-sm text-gray-500">
                {session.session.location} •{' '}
                {formatDateTime(session.session.startTime)}
              </p>
            </div>
            {isAdmin && (
              <div className="mt-4 flex space-x-3 md:mt-0 md:ml-4">
                <Link
                  to={`/sessions/${id}/edit`}
                  className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                >
                  Edit Session
                </Link>
                <button
                  onClick={() => setShowDeleteModal(true)}
                  className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-red-600 hover:bg-red-700 transition-colors focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
                  aria-label="Delete session"
                >
                  Delete
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="space-y-6">
          {/* Session Details Card */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">
              Session Details
            </h2>
            <dl className="grid grid-cols-1 gap-x-4 gap-y-4 sm:grid-cols-2">
              <div>
                <dt className="text-sm font-medium text-gray-500">Game Type</dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {formatGameType(session.session.gameType)}
                </dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Location</dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {session.session.location}
                </dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">
                  Session Date
                </dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {formatDateTime(session.session.startTime)}
                </dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">
                  Min Buy-in
                </dt>
                <dd className="mt-1 text-sm text-gray-900">
                  {formatCents(session.session.minBuyInCents)}
                </dd>
              </div>
              {session.session.notes && (
                <div className="sm:col-span-2">
                  <dt className="text-sm font-medium text-gray-500">Notes</dt>
                  <dd className="mt-1 text-sm text-gray-900">
                    {session.session.notes}
                  </dd>
                </div>
              )}
            </dl>
          </div>

          {/* Player Results */}
          <div className="bg-white shadow rounded-lg overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-medium text-gray-900">
                Player Results
              </h2>
            </div>

            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Player
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Buy-in
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Cash-out
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Profit/Loss
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {session.results
                    .sort((a, b) => b.profitCents - a.profitCents)
                    .map((result) => (
                      <tr key={result.id}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {renderPlayerInfo(result)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatCents(result.buyInCents)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatCents(result.cashOutCents)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm">
                          <span
                            className={`font-semibold ${
                              result.profitCents >= 0
                                ? 'text-green-600'
                                : 'text-red-600'
                            }`}
                          >
                            {formatProfitCents(result.profitCents)}
                          </span>
                        </td>
                      </tr>
                    ))}
                </tbody>
                <tfoot className="bg-gray-50">
                  <tr>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      Total
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900">
                      {formatCents(totalBuyIn)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900">
                      {formatCents(totalCashOut)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {!isBalanced && (
                        <span className="text-red-600 text-xs">
                          ⚠️ Not balanced
                        </span>
                      )}
                      {isBalanced && (
                        <span className="text-green-600 text-xs">
                          ✓ Balanced
                        </span>
                      )}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      <ConfirmationModal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        onConfirm={handleDelete}
        title="Delete Session"
        description="Are you sure you want to delete this session? This action cannot be undone and all associated data will be permanently removed."
        confirmText="Delete"
        cancelText="Cancel"
        variant="danger"
        isLoading={deleteMutation.isPending}
      />
    </div>
  );
};
