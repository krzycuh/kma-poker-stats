import { Dialog } from '@headlessui/react'
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { playerApi } from '../api/players'
import type { Player, CreatePlayerRequest, UpdatePlayerRequest } from '../types/player'
import { useToast } from '../hooks/useToast'

type PlayerFormValues = {
  name: string
  avatarUrl: string
  userId: string
}

interface PlayerFormModalProps {
  isOpen: boolean
  onClose: () => void
  player?: Player | null
}

export function PlayerFormModal({ isOpen, onClose, player }: PlayerFormModalProps) {
  const { success, error } = useToast()
  const queryClient = useQueryClient()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<PlayerFormValues>({
    defaultValues: {
      name: player?.name ?? '',
      avatarUrl: player?.avatarUrl ?? '',
      userId: '',
    },
  })

  useEffect(() => {
    if (isOpen) {
      reset({
        name: player?.name ?? '',
        avatarUrl: player?.avatarUrl ?? '',
        userId: '',
      })
    }
  }, [isOpen, player, reset])

  const createMutation = useMutation({
    mutationFn: (payload: CreatePlayerRequest) => playerApi.create(payload),
    onSuccess: () => {
      success('Player created successfully')
      queryClient.invalidateQueries({ queryKey: ['players'] })
      onClose()
    },
    onError: () => {
      error('Failed to create player. Please try again.')
    },
  })

  const updateMutation = useMutation({
    mutationFn: (payload: { id: string; data: UpdatePlayerRequest }) =>
      playerApi.update(payload.id, payload.data),
    onSuccess: () => {
      success('Player updated successfully')
      queryClient.invalidateQueries({ queryKey: ['players'] })
      onClose()
    },
    onError: () => {
      error('Failed to update player. Please try again.')
    },
  })

  const onSubmit = (values: PlayerFormValues) => {
    const normalizedAvatar = values.avatarUrl?.trim() || null

    if (player) {
      updateMutation.mutate({
        id: player.id,
        data: {
          name: values.name.trim(),
          avatarUrl: normalizedAvatar,
        },
      })
    } else {
      createMutation.mutate({
        name: values.name.trim(),
        avatarUrl: normalizedAvatar,
        userId: values.userId.trim() || null,
      })
    }
  }

  const isSubmitting = createMutation.isPending || updateMutation.isPending

  if (!isOpen) {
    return null
  }

  return (
    <Dialog open={isOpen} onClose={onClose} className="relative z-50">
      <div className="fixed inset-0 bg-black/30" aria-hidden="true" />

      <div className="fixed inset-0 flex items-center justify-center p-4">
        <Dialog.Panel className="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl">
          <Dialog.Title className="mb-1 text-xl font-semibold text-gray-900">
            {player ? 'Edit Player' : 'Add Player'}
          </Dialog.Title>
          <Dialog.Description className="mb-6 text-sm text-gray-600">
            {player
              ? 'Update the player details below.'
              : 'Provide player details to add them to the roster.'}
          </Dialog.Description>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label htmlFor="name" className="mb-1 block text-sm font-medium text-gray-700">
                Name
              </label>
              <input
                id="name"
                type="text"
                {...register('name', {
                  required: 'Name is required',
                  minLength: { value: 2, message: 'Name must be at least 2 characters' },
                })}
                disabled={isSubmitting}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:cursor-not-allowed disabled:bg-gray-100"
              />
              {errors.name && <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>}
            </div>

            <div>
              <label htmlFor="avatarUrl" className="mb-1 block text-sm font-medium text-gray-700">
                Avatar URL
              </label>
              <input
                id="avatarUrl"
                type="url"
                placeholder="https://example.com/avatar.png"
                {...register('avatarUrl', {
                  validate: (value) => {
                    if (!value) return true
                    try {
                      new URL(value)
                      return true
                    } catch {
                      return 'Please enter a valid URL'
                    }
                  },
                })}
                disabled={isSubmitting}
                className="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:cursor-not-allowed disabled:bg-gray-100"
              />
              {errors.avatarUrl && (
                <p className="mt-1 text-sm text-red-600">{errors.avatarUrl.message}</p>
              )}
            </div>

            {!player && (
              <div>
                <label htmlFor="userId" className="mb-1 block text-sm font-medium text-gray-700">
                  Link to Existing User (optional)
                </label>
                <input
                  id="userId"
                  type="text"
                  placeholder="User ID (UUID)"
                  {...register('userId', {
                    validate: (value) =>
                      !value ||
                      /^[0-9a-fA-F-]{36}$/.test(value.trim()) ||
                      'Please enter a valid UUID',
                  })}
                  disabled={isSubmitting}
                  className="w-full rounded-md border border-gray-300 px-3 py-2 text-gray-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:cursor-not-allowed disabled:bg-gray-100"
                />
                <p className="mt-1 text-xs text-gray-500">
                  Provide this only if the player already has a user account.
                </p>
                {errors.userId && (
                  <p className="mt-1 text-sm text-red-600">{errors.userId.message}</p>
                )}
              </div>
            )}

            {player && player.userId && (
              <div className="rounded-md bg-gray-50 p-3 text-sm text-gray-600">
                Linked account: <span className="font-mono text-gray-800">{player.userId}</span>
              </div>
            )}

            <div className="mt-6 flex gap-3">
              <button
                type="button"
                onClick={onClose}
                disabled={isSubmitting}
                className="flex-1 rounded-md border border-gray-300 px-4 py-2 text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex-1 rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isSubmitting ? 'Saving...' : player ? 'Save Changes' : 'Create Player'}
              </button>
            </div>
          </form>
        </Dialog.Panel>
      </div>
    </Dialog>
  )
}
