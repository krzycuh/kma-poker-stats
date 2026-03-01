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
        <Dialog.Panel className="flex w-full max-w-lg flex-col rounded-lg bg-white shadow-xl" style={{ maxHeight: 'calc(100vh - 2rem)' }}>
          <div className="flex-shrink-0 border-b px-4 py-3">
            <Dialog.Title className="text-base font-semibold text-gray-900">
              Link Player to User
            </Dialog.Title>
            <div className="mt-2">
              <input
                type="text"
                value={searchTerm}
                onChange={(event) => {
                  setSearchTerm(event.target.value)
                  setPage(0)
                }}
                placeholder="Search by name or email..."
                className="w-full rounded-md border border-gray-300 px-3 py-1.5 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="min-h-0 flex-1 overflow-y-auto">
            {isLoading && (
              <div className="py-10 text-center text-sm text-gray-500">Loading users...</div>
            )}

            {isError && (
              <div className="m-3 rounded-md bg-red-50 p-3 text-sm text-red-700">
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
              <div className="py-10 text-center text-sm text-gray-500">
                No unlinked users found.
              </div>
            )}

            {data && data.items.length > 0 && (
              <ul className="divide-y divide-gray-100">
                {data.items.map((user) => (
                  <li key={user.id} className="flex items-center gap-3 px-4 py-2.5 hover:bg-gray-50">
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium text-gray-900">{user.name}</p>
                      <p className="truncate text-xs text-gray-500">
                        {user.email}
                        <span className="text-gray-400"> · {new Date(user.createdAt).toLocaleDateString()}</span>
                      </p>
                    </div>
                    <button
                      onClick={() => handleSelect(user)}
                      className="flex-shrink-0 rounded-md bg-blue-600 px-3 py-1 text-xs font-medium text-white hover:bg-blue-700"
                    >
                      Link
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="flex flex-shrink-0 items-center justify-between border-t px-4 py-2.5">
            <button
              onClick={onClose}
              className="rounded-md border border-gray-300 px-3 py-1 text-xs text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <div className="flex items-center gap-1.5">
              <button
                onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                disabled={!hasPrev}
                className="rounded-md border border-gray-300 px-2 py-1 text-xs disabled:cursor-not-allowed disabled:opacity-50"
              >
                Prev
              </button>
              <span className="text-xs text-gray-500">
                {data ? data.page + 1 : 1}/{data?.totalPages ?? 1}
              </span>
              <button
                onClick={() => setPage((prev) => prev + 1)}
                disabled={!hasNext}
                className="rounded-md border border-gray-300 px-2 py-1 text-xs disabled:cursor-not-allowed disabled:opacity-50"
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
