import { Dialog } from '@headlessui/react'
import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { adminUsersApi, type UserSummary } from '../api/adminUsers'

interface LinkUserModalProps {
  isOpen: boolean
  onClose: () => void
  onSelect: (user: UserSummary) => void
}

export function LinkUserModal({ isOpen, onClose, onSelect }: LinkUserModalProps) {
  const [searchTerm, setSearchTerm] = useState('')
  const [page, setPage] = useState(0)

  useEffect(() => {
    if (isOpen) {
      setSearchTerm('')
      setPage(0)
    }
  }, [isOpen])

  const trimmedSearch = searchTerm.trim()

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['unlinked-users', searchTerm, page, isOpen],
    queryFn: () =>
      adminUsersApi.listUnlinked({
        searchTerm: trimmedSearch || undefined,
        page,
        pageSize: 10,
      }),
    enabled: isOpen,
  })

  const handleSelect = (user: UserSummary) => {
    onSelect(user)
  }

  const hasPrev = (data?.page ?? 0) > 0
  const hasNext = data ? data.page < data.totalPages - 1 : false

  if (!isOpen) {
    return null
  }

  return (
    <Dialog open={isOpen} onClose={onClose} className="relative z-50">
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />

      <div className="fixed inset-0 flex items-center justify-center p-4">
        <Dialog.Panel className="w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl">
          <Dialog.Title className="text-xl font-semibold text-gray-900">
            Link Player to User
          </Dialog.Title>
          <Dialog.Description className="mt-1 text-sm text-gray-600">
            Select a registered user account to link with this player.
          </Dialog.Description>

          <div className="mt-6">
            <input
              type="text"
              value={searchTerm}
              onChange={(event) => {
                setSearchTerm(event.target.value)
                setPage(0)
              }}
              placeholder="Search by name or email..."
              className="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
          </div>

          <div className="mt-4 min-h-[280px]">
            {isLoading && (
              <div className="py-16 text-center text-gray-500">Loading users...</div>
            )}

            {isError && (
              <div className="rounded-md bg-red-50 p-4 text-red-700">
                Failed to load users.{' '}
                <button
                  onClick={() => refetch()}
                  className="font-semibold underline underline-offset-2"
                >
                  Retry
                </button>
              </div>
            )}

            {!isLoading && data && data.items.length === 0 && (
              <div className="rounded-md bg-gray-50 p-8 text-center text-gray-500">
                No unlinked users found.
              </div>
            )}

            {data && data.items.length > 0 && (
              <ul className="divide-y divide-gray-200 rounded-md border border-gray-200">
                {data.items.map((user) => (
                  <li key={user.id} className="flex items-center justify-between p-4">
                    <div>
                      <p className="font-medium text-gray-900">{user.name}</p>
                      <p className="text-sm text-gray-600">{user.email}</p>
                      <p className="text-xs text-gray-500">
                        Registered on {new Date(user.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <button
                      onClick={() => handleSelect(user)}
                      className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                    >
                      Link
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="mt-4 flex items-center justify-between">
            <button
              onClick={onClose}
              className="rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <div className="flex items-center gap-2">
              <button
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={!hasPrev}
                className="rounded-md border border-gray-300 px-3 py-1 text-sm disabled:cursor-not-allowed disabled:opacity-50"
              >
                Previous
              </button>
              <span className="text-sm text-gray-600">
                Page {data ? data.page + 1 : 1} of {data?.totalPages ?? 1}
              </span>
              <button
                onClick={() => setPage((prev) => prev + 1)}
                disabled={!hasNext}
                className="rounded-md border border-gray-300 px-3 py-1 text-sm disabled:cursor-not-allowed disabled:opacity-50"
              >
                Next
              </button>
            </div>
          </div>
        </Dialog.Panel>
      </div>
    </Dialog>
  )
}
