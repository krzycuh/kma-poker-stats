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
  const [showUnlinkedOnly, setShowUnlinkedOnly] = useState(false)
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

  const displayedPlayers = showUnlinkedOnly
    ? players?.filter((p) => !p.userEmail)
    : players

  return (
    <div className="container mx-auto px-3 sm:px-4 pt-3 sm:pt-4 pb-4 sm:pb-8">
      <PageHeader
        title="Players"
        description="Manage your club roster and account links"
        actions={
          <div className="flex flex-wrap items-center gap-2 sm:gap-3">
            {typeof unlinkedCount === 'number' && unlinkedCount > 0 && (
              <button
                onClick={() => setShowUnlinkedOnly(!showUnlinkedOnly)}
                className={`rounded-full px-2.5 sm:px-3 py-1 text-xs sm:text-sm font-medium transition ${
                  showUnlinkedOnly
                    ? 'bg-yellow-500 text-white'
                    : 'bg-yellow-100 text-yellow-800 hover:bg-yellow-200'
                }`}
              >
                {showUnlinkedOnly ? 'Show all' : `Unlinked: ${unlinkedCount}`}
              </button>
            )}
            <button
              onClick={handleAddNew}
              className="rounded-md bg-blue-600 px-3 sm:px-4 py-1.5 sm:py-2 text-xs sm:text-sm text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              Add Player
            </button>
          </div>
        }
      />

      {/* Search and filters */}
      <div className="mb-3 sm:mb-6 flex flex-col gap-2 sm:gap-4 sm:flex-row sm:items-center">
        <div className="flex-1">
          <input
            type="text"
            placeholder="Search players..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full rounded-md border border-gray-300 px-3 sm:px-4 py-1.5 sm:py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
          />
        </div>
        <label className="flex items-center">
          <input
            type="checkbox"
            checked={showInactive}
            onChange={(e) => setShowInactive(e.target.checked)}
            className="mr-2 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
          />
          <span className="text-xs sm:text-sm text-gray-700">Show inactive</span>
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
      {displayedPlayers && displayedPlayers.length === 0 && (
        <div className="rounded-md bg-gray-50 p-4 sm:p-8 text-center text-sm text-gray-500">
          {showUnlinkedOnly ? 'All players are linked!' : 'No players found. Add your first player to get started!'}
        </div>
      )}

      {displayedPlayers && displayedPlayers.length > 0 && (
        <>
          {/* Mobile: compact single-column list */}
          <div className="flex flex-col gap-1.5 md:hidden">
            {displayedPlayers.map((player) => (
              <div
                key={player.id}
                className={`flex items-center gap-2 rounded-lg border px-2.5 py-2 transition-shadow hover:shadow-sm ${
                  !player.isActive ? 'bg-gray-50 opacity-75' : 'bg-white'
                }`}
              >
                {player.avatarUrl ? (
                  <img
                    src={player.avatarUrl}
                    alt={player.name}
                    className="h-8 w-8 flex-shrink-0 rounded-full object-cover"
                  />
                ) : (
                  <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-blue-600">
                    <span className="text-sm font-semibold">
                      {player.name.charAt(0).toUpperCase()}
                    </span>
                  </div>
                )}
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="truncate text-sm font-semibold text-gray-900">
                      {player.name}
                    </span>
                    {!player.isActive && (
                      <span className="flex-shrink-0 rounded bg-gray-200 px-1.5 py-0.5 text-[10px] text-gray-600">
                        Inactive
                      </span>
                    )}
                  </div>
                  {player.userEmail ? (
                    <p className="truncate text-xs text-gray-500">{player.userEmail}</p>
                  ) : (
                    <p className="text-xs text-orange-500">Not linked</p>
                  )}
                </div>
                <div className="flex flex-shrink-0 items-center gap-1">
                  <button
                    onClick={() => handleEdit(player)}
                    className="rounded-md p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                    title="Edit"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="h-4 w-4">
                      <path d="M2.695 14.763l-1.262 3.154a.5.5 0 00.65.65l3.155-1.262a4 4 0 001.343-.885L17.5 5.5a2.121 2.121 0 00-3-3L3.58 13.42a4 4 0 00-.885 1.343z" />
                    </svg>
                  </button>
                  <button
                    onClick={() => handleDelete(player)}
                    className="rounded-md p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500"
                    title="Delete"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="h-4 w-4">
                      <path fillRule="evenodd" d="M8.75 1A2.75 2.75 0 006 3.75v.443c-.795.077-1.584.176-2.365.298a.75.75 0 10.23 1.482l.149-.022.841 10.518A2.75 2.75 0 007.596 19h4.807a2.75 2.75 0 002.742-2.53l.841-10.52.149.023a.75.75 0 00.23-1.482A41.03 41.03 0 0014 4.193V3.75A2.75 2.75 0 0011.25 1h-2.5zM10 4c.84 0 1.673.025 2.5.075V3.75c0-.69-.56-1.25-1.25-1.25h-2.5c-.69 0-1.25.56-1.25 1.25v.325C8.327 4.025 9.16 4 10 4zM8.58 7.72a.75.75 0 00-1.5.06l.3 7.5a.75.75 0 101.5-.06l-.3-7.5zm4.34.06a.75.75 0 10-1.5-.06l-.3 7.5a.75.75 0 101.5.06l.3-7.5z" clipRule="evenodd" />
                    </svg>
                  </button>
                  {player.userEmail ? (
                    <button
                      onClick={() => handleUnlink(player)}
                      className="rounded-md p-1.5 text-yellow-500 hover:bg-yellow-50 hover:text-yellow-600"
                      title="Unlink user"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="h-4 w-4">
                        <path d="M12.232 4.232a2.5 2.5 0 013.536 3.536l-1.225 1.224a.75.75 0 001.061 1.06l1.224-1.224a4 4 0 00-5.656-5.656l-3 3a4 4 0 00.225 5.865.75.75 0 00.977-1.138 2.5 2.5 0 01-.142-3.667l3-3z" />
                        <path d="M7.768 15.768a2.5 2.5 0 01-3.536-3.536l1.225-1.224a.75.75 0 00-1.061-1.06l-1.224 1.224a4 4 0 005.656 5.656l3-3a4 4 0 00-.225-5.865.75.75 0 00-.977 1.138 2.5 2.5 0 01.142 3.667l-3 3z" />
                        <path d="M3.28 2.22a.75.75 0 00-1.06 1.06l14.5 14.5a.75.75 0 101.06-1.06L3.28 2.22z" />
                      </svg>
                    </button>
                  ) : (
                    <button
                      onClick={() => handleOpenLinkModal(player)}
                      className="rounded-md p-1.5 text-blue-400 hover:bg-blue-50 hover:text-blue-600"
                      title="Link user"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="h-4 w-4">
                        <path d="M12.232 4.232a2.5 2.5 0 013.536 3.536l-1.225 1.224a.75.75 0 001.061 1.06l1.224-1.224a4 4 0 00-5.656-5.656l-3 3a4 4 0 00.225 5.865.75.75 0 00.977-1.138 2.5 2.5 0 01-.142-3.667l3-3z" />
                        <path d="M7.768 15.768a2.5 2.5 0 01-3.536-3.536l1.225-1.224a.75.75 0 00-1.061-1.06l-1.224 1.224a4 4 0 005.656 5.656l3-3a4 4 0 00-.225-5.865.75.75 0 00-.977 1.138 2.5 2.5 0 01.142 3.667l-3 3z" />
                      </svg>
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Desktop: spacious grid cards */}
          <div className="hidden gap-4 md:grid md:grid-cols-2 lg:grid-cols-3">
            {displayedPlayers.map((player) => (
              <div
                key={player.id}
                className={`rounded-lg border p-4 shadow-sm transition-shadow hover:shadow-md ${
                  !player.isActive ? 'bg-gray-50 opacity-75' : 'bg-white'
                }`}
              >
                <div className="flex items-start gap-3">
                  {player.avatarUrl ? (
                    <img
                      src={player.avatarUrl}
                      alt={player.name}
                      className="h-12 w-12 flex-shrink-0 rounded-full object-cover"
                    />
                  ) : (
                    <div className="flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-blue-100 text-blue-600">
                      <span className="text-lg font-semibold">
                        {player.name.charAt(0).toUpperCase()}
                      </span>
                    </div>
                  )}
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <h3 className="truncate text-lg font-semibold text-gray-900">
                        {player.name}
                      </h3>
                      {!player.isActive && (
                        <span className="flex-shrink-0 rounded bg-gray-200 px-1.5 py-0.5 text-xs text-gray-600">
                          Inactive
                        </span>
                      )}
                    </div>
                    {player.userEmail ? (
                      <p className="truncate text-sm text-gray-500">{player.userEmail}</p>
                    ) : (
                      <p className="text-sm text-orange-500">Not linked</p>
                    )}
                  </div>
                </div>
                <div className="mt-3 flex items-center gap-1.5">
                  <button
                    onClick={() => handleEdit(player)}
                    className="rounded-md p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                    title="Edit"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="h-4.5 w-4.5 h-[18px] w-[18px]">
                      <path d="M2.695 14.763l-1.262 3.154a.5.5 0 00.65.65l3.155-1.262a4 4 0 001.343-.885L17.5 5.5a2.121 2.121 0 00-3-3L3.58 13.42a4 4 0 00-.885 1.343z" />
                    </svg>
                  </button>
                  <button
                    onClick={() => handleDelete(player)}
                    className="rounded-md p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500"
                    title="Delete"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="h-[18px] w-[18px]">
                      <path fillRule="evenodd" d="M8.75 1A2.75 2.75 0 006 3.75v.443c-.795.077-1.584.176-2.365.298a.75.75 0 10.23 1.482l.149-.022.841 10.518A2.75 2.75 0 007.596 19h4.807a2.75 2.75 0 002.742-2.53l.841-10.52.149.023a.75.75 0 00.23-1.482A41.03 41.03 0 0014 4.193V3.75A2.75 2.75 0 0011.25 1h-2.5zM10 4c.84 0 1.673.025 2.5.075V3.75c0-.69-.56-1.25-1.25-1.25h-2.5c-.69 0-1.25.56-1.25 1.25v.325C8.327 4.025 9.16 4 10 4zM8.58 7.72a.75.75 0 00-1.5.06l.3 7.5a.75.75 0 101.5-.06l-.3-7.5zm4.34.06a.75.75 0 10-1.5-.06l-.3 7.5a.75.75 0 101.5.06l.3-7.5z" clipRule="evenodd" />
                    </svg>
                  </button>
                  <div className="flex-1" />
                  {player.userEmail ? (
                    <button
                      onClick={() => handleUnlink(player)}
                      className="rounded-md border border-yellow-300 px-3 py-1 text-xs text-yellow-700 hover:bg-yellow-50"
                    >
                      Unlink
                    </button>
                  ) : (
                    <button
                      onClick={() => handleOpenLinkModal(player)}
                      className="rounded-md border border-blue-300 px-3 py-1 text-xs text-blue-700 hover:bg-blue-50"
                    >
                      Link user
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </>
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
