import { Outlet } from 'react-router-dom';
import { Navigation } from './Navigation';

/**
 * Shared layout for authenticated pages.
 * Renders the persistent navigation and reserves space for page content.
 */
export function AppLayout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      <main className="px-4 py-6 sm:px-6 lg:px-8">
        <Outlet />
      </main>
    </div>
  );
}
