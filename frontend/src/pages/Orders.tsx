import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { EyeIcon, TruckIcon } from '@heroicons/react/24/outline';

interface Order {
  id: number;
  orderNumber: string;
  status: string;
  paymentStatus: string;
  totalAmount: number;
  items: any[];
  trackingNumber?: string;
  createdAt: string;
  shippedAt?: string;
  deliveredAt?: string;
}

const Orders: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async (pageNum = 0) => {
    try {
      const response = await api.get(`/orders/user?page=${pageNum}&size=10`);
      if (pageNum === 0) {
        setOrders(response.data.content);
      } else {
        setOrders(prev => [...prev, ...response.data.content]);
      }
      setHasMore(!response.data.last);
      setPage(pageNum);
    } catch (error) {
      console.error('Error fetching orders:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMore = () => {
    fetchOrders(page + 1);
  };

  const getStatusColor = (status: string) => {
    const colors = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      CONFIRMED: 'bg-blue-100 text-blue-800',
      PROCESSING: 'bg-indigo-100 text-indigo-800',
      SHIPPED: 'bg-purple-100 text-purple-800',
      DELIVERED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-red-100 text-red-800',
    };
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800';
  };

  const getPaymentStatusColor = (status: string) => {
    const colors = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      PAID: 'bg-green-100 text-green-800',
      FAILED: 'bg-red-100 text-red-800',
      REFUNDED: 'bg-gray-100 text-gray-800',
      PARTIALLY_REFUNDED: 'bg-orange-100 text-orange-800',
    };
    return colors[status as keyof typeof colors] || 'bg-gray-100 text-gray-800';
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const cancelOrder = async (orderId: number) => {
    if (window.confirm('Are you sure you want to cancel this order?')) {
      try {
        await api.post(`/orders/${orderId}/cancel`);
        fetchOrders(0); // Refresh orders
      } catch (error) {
        console.error('Error cancelling order:', error);
      }
    }
  };

  if (loading && orders.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Order History</h1>
      
      {orders.length === 0 ? (
        <div className="text-center py-16">
          <div className="mx-auto h-24 w-24 text-gray-400 mb-4">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l-1 4H6l-1-4z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">No orders yet</h2>
          <p className="text-gray-600 mb-8">When you place your first order, it will appear here.</p>
          <Link
            to="/products"
            className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 transition-colors"
          >
            Start Shopping
          </Link>
        </div>
      ) : (
        <div className="space-y-6">
          {orders.map((order) => (
            <div key={order.id} className="bg-white shadow rounded-lg overflow-hidden">
              <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg leading-6 font-medium text-gray-900">
                      Order #{order.orderNumber}
                    </h3>
                    <p className="mt-1 max-w-2xl text-sm text-gray-500">
                      Placed on {formatDate(order.createdAt)}
                    </p>
                  </div>
                  <div className="flex items-center space-x-4">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(order.status)}`}>
                      {order.status.replace('_', ' ')}
                    </span>
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getPaymentStatusColor(order.paymentStatus)}`}>
                      {order.paymentStatus.replace('_', ' ')}
                    </span>
                  </div>
                </div>
              </div>

              <div className="px-4 py-5 sm:p-6">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  {/* Order Items */}
                  <div className="md:col-span-2">
                    <h4 className="text-sm font-medium text-gray-900 mb-3">Items</h4>
                    <div className="space-y-3">
                      {order.items.map((item, index) => (
                        <div key={index} className="flex items-center">
                          <div className="flex-shrink-0 w-12 h-12 bg-gray-200 rounded-md overflow-hidden">
                            <img
                              src={item.productImageUrl || 'https://via.placeholder.com/150x150?text=No+Image'}
                              alt={item.productName}
                              className="w-full h-full object-cover"
                            />
                          </div>
                          <div className="ml-3 flex-1">
                            <p className="text-sm font-medium text-gray-900">
                              {item.productName}
                            </p>
                            <p className="text-sm text-gray-500">
                              Qty: {item.quantity} × ${item.unitPrice.toFixed(2)}
                            </p>
                          </div>
                          <div className="text-sm font-medium text-gray-900">
                            ${item.totalPrice.toFixed(2)}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Order Summary */}
                  <div>
                    <h4 className="text-sm font-medium text-gray-900 mb-3">Order Summary</h4>
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-gray-600">Total</span>
                        <span className="font-medium text-gray-900">
                          ${order.totalAmount.toFixed(2)}
                        </span>
                      </div>
                      {order.trackingNumber && (
                        <div className="flex items-center text-primary-600">
                          <TruckIcon className="h-4 w-4 mr-1" />
                          <span className="text-xs">
                            Tracking: {order.trackingNumber}
                          </span>
                        </div>
                      )}
                      {order.shippedAt && (
                        <p className="text-xs text-gray-500">
                          Shipped: {formatDate(order.shippedAt)}
                        </p>
                      )}
                      {order.deliveredAt && (
                        <p className="text-xs text-green-600">
                          Delivered: {formatDate(order.deliveredAt)}
                        </p>
                      )}
                    </div>
                  </div>
                </div>

                <div className="mt-6 flex justify-between items-center">
                  <div className="flex space-x-3">
                    <Link
                      to={`/orders/${order.id}`}
                      className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
                    >
                      <EyeIcon className="h-4 w-4 mr-1" />
                      View Details
                    </Link>
                    {(order.status === 'PENDING' || order.status === 'CONFIRMED') && (
                      <button
                        onClick={() => cancelOrder(order.id)}
                        className="inline-flex items-center px-3 py-2 border border-red-300 shadow-sm text-sm leading-4 font-medium rounded-md text-red-700 bg-white hover:bg-red-50"
                      >
                        Cancel Order
                      </button>
                    )}
                  </div>
                  
                  {order.status === 'DELIVERED' && (
                    <button className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
                      Reorder
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
          
          {hasMore && (
            <div className="text-center">
              <button
                onClick={loadMore}
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-primary-700 bg-primary-100 hover:bg-primary-200"
              >
                Load More Orders
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Orders;
