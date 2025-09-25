import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../contexts/CartContext';
import { TrashIcon, PlusIcon, MinusIcon } from '@heroicons/react/24/outline';

const Cart: React.FC = () => {
  const { cart, loading, updateCartItem, removeFromCart, clearCart } = useCart();
  const navigate = useNavigate();

  const handleQuantityChange = async (cartItemId: number, newQuantity: number) => {
    if (newQuantity < 1) {
      await removeFromCart(cartItemId);
    } else {
      await updateCartItem(cartItemId, newQuantity);
    }
  };

  const handleProceedToCheckout = () => {
    navigate('/checkout');
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center">
          <div className="mx-auto h-24 w-24 text-gray-400 mb-4">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l-1 4H6l-1-4z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Your cart is empty</h2>
          <p className="text-gray-600 mb-8">Looks like you haven't added anything to your cart yet.</p>
          <Link
            to="/products"
            className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 transition-colors"
          >
            Continue Shopping
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Shopping Cart</h1>
      
      <div className="lg:grid lg:grid-cols-12 lg:gap-x-12 lg:items-start">
        {/* Cart Items */}
        <div className="lg:col-span-7">
          <div className="flow-root">
            <ul className="-my-6 divide-y divide-gray-200">
              {cart.items.map((item) => (
                <li key={item.id} className="flex py-6">
                  <div className="flex-shrink-0 w-24 h-24 border border-gray-200 rounded-md overflow-hidden">
                    <img
                      src={item.productImageUrl || 'https://via.placeholder.com/150x150?text=No+Image'}
                      alt={item.productName}
                      className="w-full h-full object-center object-cover"
                    />
                  </div>

                  <div className="ml-4 flex-1 flex flex-col">
                    <div>
                      <div className="flex justify-between text-base font-medium text-gray-900">
                        <h3>
                          <Link to={`/products/${item.productSlug}`} className="hover:text-primary-600">
                            {item.productName}
                          </Link>
                        </h3>
                        <p className="ml-4">${item.totalPrice.toFixed(2)}</p>
                      </div>
                      <p className="mt-1 text-sm text-gray-500">
                        ${item.unitPrice.toFixed(2)} each
                      </p>
                      {!item.isAvailable && (
                        <p className="mt-1 text-sm text-red-600">
                          Out of stock
                        </p>
                      )}
                    </div>
                    
                    <div className="flex-1 flex items-end justify-between text-sm">
                      <div className="flex items-center">
                        <button
                          onClick={() => handleQuantityChange(item.id, item.quantity - 1)}
                          className="p-1 text-gray-400 hover:text-gray-500"
                          disabled={!item.isAvailable}
                        >
                          <MinusIcon className="h-4 w-4" />
                        </button>
                        <span className="mx-2 text-gray-700 font-medium">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                          className="p-1 text-gray-400 hover:text-gray-500"
                          disabled={!item.isAvailable}
                        >
                          <PlusIcon className="h-4 w-4" />
                        </button>
                      </div>

                      <div className="flex">
                        <button
                          type="button"
                          onClick={() => removeFromCart(item.id)}
                          className="font-medium text-red-600 hover:text-red-500 flex items-center"
                        >
                          <TrashIcon className="h-4 w-4 mr-1" />
                          Remove
                        </button>
                      </div>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </div>
          
          <div className="mt-6 flex justify-between">
            <button
              onClick={clearCart}
              className="text-sm font-medium text-gray-600 hover:text-gray-500"
            >
              Clear Cart
            </button>
            <Link
              to="/products"
              className="text-sm font-medium text-primary-600 hover:text-primary-500"
            >
              Continue Shopping
            </Link>
          </div>
        </div>

        {/* Order Summary */}
        <div className="mt-16 rounded-lg bg-gray-50 px-4 py-6 sm:p-6 lg:col-span-5 lg:mt-0 lg:p-8">
          <h2 className="text-lg font-medium text-gray-900">Order Summary</h2>

          <div className="mt-6 space-y-4">
            <div className="flex items-center justify-between">
              <dt className="text-sm text-gray-600">Subtotal</dt>
              <dd className="text-sm font-medium text-gray-900">
                ${cart.totalAmount.toFixed(2)}
              </dd>
            </div>
            
            <div className="flex items-center justify-between">
              <dt className="text-sm text-gray-600">Shipping estimate</dt>
              <dd className="text-sm font-medium text-gray-900">$10.00</dd>
            </div>
            
            <div className="flex items-center justify-between">
              <dt className="text-sm text-gray-600">Tax estimate</dt>
              <dd className="text-sm font-medium text-gray-900">
                ${(cart.totalAmount * 0.08).toFixed(2)}
              </dd>
            </div>
            
            <div className="border-t border-gray-200 pt-4 flex items-center justify-between">
              <dt className="text-base font-medium text-gray-900">Order total</dt>
              <dd className="text-base font-medium text-gray-900">
                ${(cart.totalAmount + 10 + (cart.totalAmount * 0.08)).toFixed(2)}
              </dd>
            </div>
          </div>

          <div className="mt-6">
            <button
              onClick={handleProceedToCheckout}
              disabled={cart.items.some(item => !item.isAvailable)}
              className="w-full bg-primary-600 border border-transparent rounded-md shadow-sm py-3 px-4 text-base font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-50 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {cart.items.some(item => !item.isAvailable) 
                ? 'Some items unavailable'
                : 'Proceed to Checkout'
              }
            </button>
          </div>

          <div className="mt-6 flex justify-center text-center text-sm text-gray-500">
            <p>
              or{' '}
              <Link
                to="/products"
                className="font-medium text-primary-600 hover:text-primary-500"
              >
                Continue Shopping
                <span aria-hidden="true"> &rarr;</span>
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;
