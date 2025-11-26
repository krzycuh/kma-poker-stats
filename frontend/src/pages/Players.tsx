import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { playerApi } from '../api/players'
import { adminUsersApi } from '../api/adminUsers'
import { useAuth } from '../hooks/useAuth'
import { UserRole } from '../types/auth'
import type { Player } from '../types/player'
import { PlayerFormModal } from '../components/PlayerFormModal'
import { LinkUserModal } from '../components/LinkUserModal'
import { ConfirmationModal } from '../components/ConfirmationModal'
import { useToast } from '../hooks/useToast'
import { PageHeader } from '../components/PageHeader'

export default function Players() {
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const { success, error: toastError } = useToast()
  const [searchTerm, setSearchTerm] = useState('')
  const [showInactive, setShowInactive] = useState(false)
  const [selectedPlayer, setSelectedPlayer] = useState<Player | null>(null)
  const [showModal, setShowModal] = useState(false)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)
  const [linkTarget, setLinkTarget] = useState<Player | null>(null)
  const [showLinkModal, setShowLinkModal] = useState(false)
  const [unlinkTarget, setUnlinkTarget] = useState<Player | null>(null)

  // Fetch players
  const {
    data: players,
    isLoading,
    error: playersError,
  } = useQuery({
    queryKey: ['players', searchTerm, showInactive],
    queryFn: () =>
      playerApi.list({
        searchTerm: searchTerm || undefined,
        includeInactive: showInactive,
      }),
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => playerApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['players'] })
      setShowDeleteConfirm(false)
      setSelectedPlayer(null)
    },
  })

  const linkMutation = useMutation({
    mutationFn: ({ playerId, userId }: { playerId: string; userId: string }) =>
      playerApi.linkUser(playerId, userId),
    onSuccess: () => {
      success('Player linked to user account')
      queryClient.invalidateQueries({ queryKey: ['players'] })
      queryClient.invalidateQueries({ queryKey: ['unlinked-users-count'] })
      queryClient.invalidateQueries({ queryKey: ['unlinked-users'] })
      setShowLinkModal(false)
      setLinkTarget(null)
    },
    onError: () => {
      toastError('Failed to link player. Please try again.')
    },
  })

  const unlinkMutation = useMutation({
    mutationFn: (playerId: string) => playerApi.unlinkUser(playerId),
    onSuccess: () => {
      success('Player unlinked from user account')
      queryClient.invalidateQueries({ queryKey: ['players'] })
      queryClient.invalidateQueries({ queryKey: ['unlinked-users-count'] })
      setUnlinkTarget(null)
    },
    onError: () => {
      toastError('Failed to unlink player. Please try again.')
    },
  })

  const { data: unlinkedCount } = useQuery({
    queryKey: ['unlinked-users-count'],
    queryFn: adminUsersApi.getUnlinkedCount,
    enabled: user?.role === UserRole.ADMIN,
    refetchInterval: 30000,
  })

  // Check if user is admin
  const isAdmin = user?.role === UserRole.ADMIN

  if (!isAdmin) {
    return (
      <div className="container mx-auto px-4 pt-4 pb-8">
        <div className="rounded-md bg-red-50 p-4 text-red-800">
          <p>You do not have permission to access this page.</p>
        </div>
      </div>
    )
  }

  const handleDelete = (player: Player) => {
    setSelectedPlayer(player)
    setShowDeleteConfirm(true)
  }

  const confirmDelete = () => {
    if (selectedPlayer) {
      deleteMutation.mutate(selectedPlayer.id)
    }
  }

  const handleAddNew = () => {
    setSelectedPlayer(null)
    setShowModal(true)
  }

  const handleEdit = (player: Player) => {
    setSelectedPlayer(player)
    setShowModal(true)
  }

  const handleCloseModal = () => {
    setShowModal(false)
    setSelectedPlayer(null)
  }

  const handleOpenLinkModal = (player: Player) => {
    setLinkTarget(player)
    setShowLinkModal(true)
  }

  const handleLinkSelect = (userSummary: { id: string }) => {
    if (!linkTarget) return
    linkMutation.mutate({
      playerId: linkTarget.id,
      userId: userSummary.id,
    })
  }

  const handleUnlink = (player: Player) => {
    setUnlinkTarget(player)
  }

  return (
    <div className="container mx-auto px-4 pt-4 pb-8">
      <PageHeader
        title="Players"
        description="Manage your club roster and account links"
        actions={
          <div className="flex flex-wrap items-center gap-3">
            {typeof unlinkedCount === 'number' && (
              <span className="rounded-full bg-yellow-100 px-3 py-1 text-sm font-medium text-yellow-800">
                Unlinked users: {unlinkedCount}
              </span>
            )}
            <button
              onClick={handleAddNew}
              className="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              Add Player
            </button>
          </div>
        }
      />

      {/* Search and filters */}
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center">
        <div className="flex-1">
          <input
            type="text"
            placeholder="Search players..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full rounded-md border border-gray-300 px-4 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          />
        </div>
        <label className="flex items-center">
          <input
            type="checkbox"
            checked={showInactive}
            onChange={(e) => setShowInactive(e.target.checked)}
            className="mr-2 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span className="text-sm text-gray-700">Show inactive</span>
        </label>
      </div>

      {/* Loading state */}
      {isLoading && (
        <div className="text-center text-gray-500">Loading players...</div>
      )}

      {/* Error state */}
      {playersError && (
        <div className="rounded-md bg-red-50 p-4 text-red-800">
          Error loading players. Please try again.
        </div>
      )}

      {/* Players grid */}
      {players && players.length === 0 && (
        <div className="rounded-md bg-gray-50 p-8 text-center text-gray-500">
          No players found. Add your first player to get started!
        </div>
      )}

      {players && players.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {players.map((player) => (
            <div
              key={player.id}
              className={`rounded-lg border p-4 shadow-sm transition-shadow hover:shadow-md ${
                !player.isActive ? 'bg-gray-50 opacity-75' : 'bg-white'
              }`}
            >
              <div className="flex items-start gap-3">
                {/* Avatar */}
                <div className="flex-shrink-0">
                  {player.avatarUrl ? (
                    <img
                      src={player.avatarUrl}
                      alt={player.name}
                      className="h-12 w-12 rounded-full object-cover"
                    />
                  ) : (
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-100 text-blue-600">
                      <span className="text-lg font-semibold">
                        {player.name.charAt(0).toUpperCase()}
                      </span>
                    </div>
                  )}
                </div>

                {/* Player info */}
                <div className="min-w-0 flex-1">
                  <h3 className="truncate text-lg font-semibold text-gray-900">
                    {player.name}
                  </h3>
                  {player.userId ? (
                    <p className="text-sm text-gray-500">Linked to account</p>
                  ) : (
                    <p className="text-sm text-orange-600">Not linked</p>
                  )}
                  {!player.isActive && (
                    <span className="inline-block rounded bg-gray-200 px-2 py-0.5 text-xs text-gray-600">
                      Inactive
                    </span>
                  )}
                </div>
              </div>

              {/* Actions */}
              <div className="mt-4 flex flex-wrap gap-2">
                <button
                  onClick={() => handleEdit(player)}
                  className="flex-1 rounded-md border border-gray-300 px-3 py-1.5 text-sm text-gray-700 hover:bg-gray-50"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(player)}
                  className="flex-1 rounded-md border border-red-300 px-3 py-1.5 text-sm text-red-600 hover:bg-red-50"
                >
                  Delete
                </button>
                {player.userId ? (
                  <button
                    onClick={() => handleUnlink(player)}
                    className="w-full rounded-md border border-yellow-300 px-3 py-1.5 text-sm text-yellow-700 hover:bg-yellow-50"
                  >
                    Unlink user
                  </button>
                ) : (
                  <button
                    onClick={() => handleOpenLinkModal(player)}
                    className="w-full rounded-md border border-blue-300 px-3 py-1.5 text-sm text-blue-700 hover:bg-blue-50"
                  >
                    Link user
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Delete confirmation modal */}
      {showDeleteConfirm && selectedPlayer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
          <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h3 className="mb-4 text-lg font-semibold text-gray-900">
              Confirm Delete
            </h3>
            <p className="mb-6 text-gray-600">
              Are you sure you want to delete{' '}
              <strong>{selectedPlayer.name}</strong>? This action will
              deactivate the player.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                disabled={deleteMutation.isPending}
                className="flex-1 rounded-md border border-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-50 disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                onClick={confirmDelete}
                disabled={deleteMutation.isPending}
                className="flex-1 rounded-md bg-red-600 px-4 py-2 text-white hover:bg-red-700 disabled:opacity-50"
              >
                {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}

      <PlayerFormModal isOpen={showModal} onClose={handleCloseModal} player={selectedPlayer} />
      <LinkUserModal
        isOpen={showLinkModal}
        onClose={() => {
          if (!linkMutation.isPending) {
            setShowLinkModal(false)
            setLinkTarget(null)
          }
        }}
        onSelect={handleLinkSelect}
      />
      <ConfirmationModal
        isOpen={!!unlinkTarget}
        onClose={() => {
          if (!unlinkMutation.isPending) {
            setUnlinkTarget(null)
          }
        }}
        onConfirm={() => {
          if (unlinkTarget) {
            unlinkMutation.mutate(unlinkTarget.id)
          }
        }}
        title="Unlink player?"
        description="This player will lose access to their personal dashboard until linked again."
        confirmText="Unlink"
        variant="warning"
        isLoading={unlinkMutation.isPending}
      />
    </div>
  )
}
