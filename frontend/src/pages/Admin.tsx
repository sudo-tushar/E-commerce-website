import React from 'react';
import { Routes, Route, Link, useLocation } from 'react-router-dom';
import {
  HomeIcon,
  ShoppingBagIcon,
  CubeIcon,
  UsersIcon,
  ChartBarIcon,
  Cog6ToothIcon,
} from '@heroicons/react/24/outline';

const Admin: React.FC = () => {
  const location = useLocation();

  const navigation = [
    { name: 'Dashboard', href: '/admin', icon: HomeIcon },
    { name: 'Products', href: '/admin/products', icon: CubeIcon },
    { name: 'Orders', href: '/admin/orders', icon: ShoppingBagIcon },
    { name: 'Users', href: '/admin/users', icon: UsersIcon },
    { name: 'Analytics', href: '/admin/analytics', icon: ChartBarIcon },
    { name: 'Settings', href: '/admin/settings', icon: Cog6ToothIcon },
  ];

  const isActive = (href: string) => {
    if (href === '/admin') {
      return location.pathname === '/admin';
    }
    return location.pathname.startsWith(href);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <div className="w-64 bg-white shadow-sm">
        <div className="flex items-center justify-center h-16 px-4 border-b border-gray-200">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-lg">E</span>
            </div>
            <span className="text-xl font-bold text-gray-900">Admin Panel</span>
          </div>
        </div>
        
        <nav className="mt-5 px-2">
          <div className="space-y-1">
            {navigation.map((item) => {
              const Icon = item.icon;
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={`group flex items-center px-2 py-2 text-base font-medium rounded-md ${
                    isActive(item.href)
                      ? 'bg-primary-100 text-primary-900'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  }`}
                >
                  <Icon
                    className={`mr-4 flex-shrink-0 h-6 w-6 ${
                      isActive(item.href) ? 'text-primary-500' : 'text-gray-400 group-hover:text-gray-500'
                    }`}
                    aria-hidden="true"
                  />
                  {item.name}
                </Link>
              );
            })}
          </div>
        </nav>
      </div>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="bg-white shadow-sm border-b border-gray-200">
          <div className="px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16">
              <div className="flex items-center">
                <h1 className="text-2xl font-semibold text-gray-900">
                  {navigation.find(nav => isActive(nav.href))?.name || 'Admin'}
                </h1>
              </div>
              <div className="flex items-center">
                <Link
                  to="/"
                  className="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium"
                >
                  Back to Store
                </Link>
              </div>
            </div>
          </div>
        </header>

        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50">
          <div className="container mx-auto px-6 py-8">
            <Routes>
              <Route path="/" element={<AdminDashboard />} />
              <Route path="/products" element={<AdminProducts />} />
              <Route path="/orders" element={<AdminOrders />} />
              <Route path="/users" element={<AdminUsers />} />
              <Route path="/analytics" element={<AdminAnalytics />} />
              <Route path="/settings" element={<AdminSettings />} />
            </Routes>
          </div>
        </main>
      </div>
    </div>
  );
};

// Admin Dashboard Component
const AdminDashboard: React.FC = () => {
  const stats = [
    { name: 'Total Orders', value: '12,345', change: '+12%', color: 'text-green-600' },
    { name: 'Revenue', value: '$54,321', change: '+8%', color: 'text-green-600' },
    { name: 'Customers', value: '8,765', change: '+15%', color: 'text-green-600' },
    { name: 'Products', value: '432', change: '+3%', color: 'text-green-600' },
  ];

  return (
    <div>
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <div key={stat.name} className="bg-white overflow-hidden shadow rounded-lg">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 bg-primary-500 rounded-md flex items-center justify-center">
                    <ChartBarIcon className="w-5 h-5 text-white" />
                  </div>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">{stat.name}</dt>
                    <dd className="flex items-baseline">
                      <div className="text-2xl font-semibold text-gray-900">{stat.value}</div>
                      <div className={`ml-2 flex items-baseline text-sm font-semibold ${stat.color}`}>
                        {stat.change}
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-8">
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg leading-6 font-medium text-gray-900">Quick Actions</h3>
            <div className="mt-5 grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
              <Link
                to="/admin/products"
                className="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-primary-500 rounded-lg border border-gray-300 hover:border-gray-400"
              >
                <div>
                  <span className="rounded-lg inline-flex p-3 bg-primary-50 text-primary-700 ring-4 ring-white">
                    <CubeIcon className="h-6 w-6" aria-hidden="true" />
                  </span>
                </div>
                <div className="mt-4">
                  <h3 className="text-lg font-medium">
                    <span className="absolute inset-0" aria-hidden="true" />
                    Manage Products
                  </h3>
                  <p className="mt-2 text-sm text-gray-500">
                    Add, edit, or remove products from your catalog.
                  </p>
                </div>
              </Link>

              <Link
                to="/admin/orders"
                className="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-primary-500 rounded-lg border border-gray-300 hover:border-gray-400"
              >
                <div>
                  <span className="rounded-lg inline-flex p-3 bg-primary-50 text-primary-700 ring-4 ring-white">
                    <ShoppingBagIcon className="h-6 w-6" aria-hidden="true" />
                  </span>
                </div>
                <div className="mt-4">
                  <h3 className="text-lg font-medium">
                    <span className="absolute inset-0" aria-hidden="true" />
                    View Orders
                  </h3>
                  <p className="mt-2 text-sm text-gray-500">
                    Process and track customer orders.
                  </p>
                </div>
              </Link>

              <Link
                to="/admin/users"
                className="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-primary-500 rounded-lg border border-gray-300 hover:border-gray-400"
              >
                <div>
                  <span className="rounded-lg inline-flex p-3 bg-primary-50 text-primary-700 ring-4 ring-white">
                    <UsersIcon className="h-6 w-6" aria-hidden="true" />
                  </span>
                </div>
                <div className="mt-4">
                  <h3 className="text-lg font-medium">
                    <span className="absolute inset-0" aria-hidden="true" />
                    Manage Users
                  </h3>
                  <p className="mt-2 text-sm text-gray-500">
                    View and manage customer accounts.
                  </p>
                </div>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// Placeholder components for other admin sections
const AdminProducts: React.FC = () => (
  <div className="bg-white shadow rounded-lg p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-4">Products Management</h2>
    <p className="text-gray-600">Product management interface coming soon...</p>
  </div>
);

const AdminOrders: React.FC = () => (
  <div className="bg-white shadow rounded-lg p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-4">Orders Management</h2>
    <p className="text-gray-600">Order management interface coming soon...</p>
  </div>
);

const AdminUsers: React.FC = () => (
  <div className="bg-white shadow rounded-lg p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-4">Users Management</h2>
    <p className="text-gray-600">User management interface coming soon...</p>
  </div>
);

const AdminAnalytics: React.FC = () => (
  <div className="bg-white shadow rounded-lg p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-4">Analytics Dashboard</h2>
    <p className="text-gray-600">Analytics dashboard coming soon...</p>
  </div>
);

const AdminSettings: React.FC = () => (
  <div className="bg-white shadow rounded-lg p-6">
    <h2 className="text-2xl font-bold text-gray-900 mb-4">Settings</h2>
    <p className="text-gray-600">Settings interface coming soon...</p>
  </div>
);

export default Admin;
