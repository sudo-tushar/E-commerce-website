import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../contexts/CartContext';
// import { useAuth } from '../contexts/AuthContext'; // Will be used when authentication is implemented
import api from '../services/api';
import { CreditCardIcon, TruckIcon } from '@heroicons/react/24/outline';

interface CheckoutForm {
  // Shipping Address
  shippingStreet: string;
  shippingCity: string;
  shippingState: string;
  shippingCountry: string;
  shippingPostalCode: string;
  
  // Billing Address
  billingStreet: string;
  billingCity: string;
  billingState: string;
  billingCountry: string;
  billingPostalCode: string;
  
  // Payment and Other
  paymentMethod: 'CREDIT_CARD' | 'DEBIT_CARD' | 'PAYPAL';
  sameAsShipping: boolean;
  notes: string;
}

const Checkout: React.FC = () => {
  const { cart, loading: cartLoading, refreshCart } = useCart();
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState<CheckoutForm>({
    shippingStreet: '',
    shippingCity: '',
    shippingState: '',
    shippingCountry: 'United States',
    shippingPostalCode: '',
    billingStreet: '',
    billingCity: '',
    billingState: '',
    billingCountry: 'United States',
    billingPostalCode: '',
    paymentMethod: 'CREDIT_CARD',
    sameAsShipping: true,
    notes: '',
  });
  
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState(1); // 1: Shipping, 2: Payment, 3: Review

  useEffect(() => {
    if (!cartLoading && (!cart || cart.items.length === 0)) {
      navigate('/cart');
    }
  }, [cart, cartLoading, navigate]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value, type } = e.target;
    
    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setFormData(prev => ({
        ...prev,
        [name]: checked,
        // If sameAsShipping is checked, copy shipping to billing
        ...(name === 'sameAsShipping' && checked && {
          billingStreet: prev.shippingStreet,
          billingCity: prev.shippingCity,
          billingState: prev.shippingState,
          billingCountry: prev.shippingCountry,
          billingPostalCode: prev.shippingPostalCode,
        })
      }));
    } else {
      setFormData(prev => {
        const newData = { ...prev, [name]: value };
        
        // If same as shipping is checked and we're updating shipping, update billing too
        if (prev.sameAsShipping && name.startsWith('shipping')) {
          const billingField = name.replace('shipping', 'billing') as keyof CheckoutForm;
          if (billingField in newData) {
            (newData as any)[billingField] = value;
          }
        }
        
        return newData;
      });
    }
  };

  const validateStep = (stepNumber: number) => {
    switch (stepNumber) {
      case 1:
        return formData.shippingStreet && formData.shippingCity && 
               formData.shippingState && formData.shippingPostalCode;
      case 2:
        return formData.paymentMethod && 
               (!formData.sameAsShipping ? 
                 (formData.billingStreet && formData.billingCity && 
                  formData.billingState && formData.billingPostalCode) : true);
      default:
        return true;
    }
  };

  const handleNext = () => {
    if (validateStep(step)) {
      setStep(prev => Math.min(prev + 1, 3));
    }
  };

  const handleBack = () => {
    setStep(prev => Math.max(prev - 1, 1));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const orderData = {
        paymentMethod: formData.paymentMethod,
        shippingStreet: formData.shippingStreet,
        shippingCity: formData.shippingCity,
        shippingState: formData.shippingState,
        shippingCountry: formData.shippingCountry,
        shippingPostalCode: formData.shippingPostalCode,
        billingStreet: formData.billingStreet,
        billingCity: formData.billingCity,
        billingState: formData.billingState,
        billingCountry: formData.billingCountry,
        billingPostalCode: formData.billingPostalCode,
        notes: formData.notes,
      };

      const response = await api.post('/orders', orderData);
      
      // Clear cart after successful order
      await refreshCart();
      
      // Navigate to order confirmation
      navigate(`/orders/${response.data.id}`, {
        state: { orderCreated: true }
      });
    } catch (error: any) {
      console.error('Order creation failed:', error);
      // Handle error (show toast, etc.)
    } finally {
      setLoading(false);
    }
  };

  if (cartLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return null; // Will redirect in useEffect
  }

  const subtotal = cart.totalAmount;
  const shipping = 10.00;
  const tax = subtotal * 0.08;
  const total = subtotal + shipping + tax;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Checkout</h1>
      
      {/* Progress Steps */}
      <div className="mb-8">
        <div className="flex items-center">
          {[1, 2, 3].map((stepNumber) => (
            <React.Fragment key={stepNumber}>
              <div className={`flex items-center justify-center w-10 h-10 rounded-full border-2 ${
                step >= stepNumber ? 'bg-primary-600 border-primary-600 text-white' : 'border-gray-300 text-gray-500'
              }`}>
                {stepNumber}
              </div>
              {stepNumber < 3 && (
                <div className={`flex-1 h-1 mx-4 ${
                  step > stepNumber ? 'bg-primary-600' : 'bg-gray-300'
                }`} />
              )}
            </React.Fragment>
          ))}
        </div>
        <div className="flex justify-between mt-2 text-sm text-gray-600">
          <span>Shipping</span>
          <span>Payment</span>
          <span>Review</span>
        </div>
      </div>

      <div className="lg:grid lg:grid-cols-12 lg:gap-x-12">
        <div className="lg:col-span-7">
          <form onSubmit={handleSubmit}>
            {/* Step 1: Shipping Information */}
            {step === 1 && (
              <div className="bg-white shadow rounded-lg p-6">
                <div className="flex items-center mb-6">
                  <TruckIcon className="h-6 w-6 text-primary-600 mr-3" />
                  <h2 className="text-lg font-medium text-gray-900">Shipping Information</h2>
                </div>
                
                <div className="grid grid-cols-1 gap-y-6 sm:grid-cols-2 sm:gap-x-4">
                  <div className="sm:col-span-2">
                    <label htmlFor="shippingStreet" className="block text-sm font-medium text-gray-700">
                      Street Address
                    </label>
                    <input
                      type="text"
                      name="shippingStreet"
                      id="shippingStreet"
                      value={formData.shippingStreet}
                      onChange={handleInputChange}
                      required
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                  </div>

                  <div>
                    <label htmlFor="shippingCity" className="block text-sm font-medium text-gray-700">
                      City
                    </label>
                    <input
                      type="text"
                      name="shippingCity"
                      id="shippingCity"
                      value={formData.shippingCity}
                      onChange={handleInputChange}
                      required
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                  </div>

                  <div>
                    <label htmlFor="shippingState" className="block text-sm font-medium text-gray-700">
                      State / Province
                    </label>
                    <input
                      type="text"
                      name="shippingState"
                      id="shippingState"
                      value={formData.shippingState}
                      onChange={handleInputChange}
                      required
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                  </div>

                  <div>
                    <label htmlFor="shippingPostalCode" className="block text-sm font-medium text-gray-700">
                      Postal Code
                    </label>
                    <input
                      type="text"
                      name="shippingPostalCode"
                      id="shippingPostalCode"
                      value={formData.shippingPostalCode}
                      onChange={handleInputChange}
                      required
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                  </div>

                  <div>
                    <label htmlFor="shippingCountry" className="block text-sm font-medium text-gray-700">
                      Country
                    </label>
                    <select
                      name="shippingCountry"
                      id="shippingCountry"
                      value={formData.shippingCountry}
                      onChange={handleInputChange}
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    >
                      <option value="United States">United States</option>
                      <option value="Canada">Canada</option>
                      <option value="United Kingdom">United Kingdom</option>
                    </select>
                  </div>
                </div>

                <div className="mt-6 flex justify-end">
                  <button
                    type="button"
                    onClick={handleNext}
                    disabled={!validateStep(1)}
                    className="bg-primary-600 border border-transparent rounded-md shadow-sm py-2 px-4 text-sm font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                  >
                    Continue to Payment
                  </button>
                </div>
              </div>
            )}

            {/* Step 2: Payment Information */}
            {step === 2 && (
              <div className="bg-white shadow rounded-lg p-6">
                <div className="flex items-center mb-6">
                  <CreditCardIcon className="h-6 w-6 text-primary-600 mr-3" />
                  <h2 className="text-lg font-medium text-gray-900">Payment Information</h2>
                </div>
                
                <div className="space-y-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700">Payment Method</label>
                    <div className="mt-2 space-y-2">
                      {(['CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL'] as const).map((method) => (
                        <div key={method} className="flex items-center">
                          <input
                            id={method}
                            name="paymentMethod"
                            type="radio"
                            value={method}
                            checked={formData.paymentMethod === method}
                            onChange={handleInputChange}
                            className="focus:ring-primary-500 h-4 w-4 text-primary-600 border-gray-300"
                          />
                          <label htmlFor={method} className="ml-3 block text-sm font-medium text-gray-700">
                            {method.replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase())}
                          </label>
                        </div>
                      ))}
                    </div>
                  </div>

                  <div className="flex items-center">
                    <input
                      id="sameAsShipping"
                      name="sameAsShipping"
                      type="checkbox"
                      checked={formData.sameAsShipping}
                      onChange={handleInputChange}
                      className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                    />
                    <label htmlFor="sameAsShipping" className="ml-2 block text-sm text-gray-900">
                      Billing address is same as shipping address
                    </label>
                  </div>

                  {!formData.sameAsShipping && (
                    <div className="grid grid-cols-1 gap-y-6 sm:grid-cols-2 sm:gap-x-4">
                      <div className="sm:col-span-2">
                        <label htmlFor="billingStreet" className="block text-sm font-medium text-gray-700">
                          Billing Street Address
                        </label>
                        <input
                          type="text"
                          name="billingStreet"
                          id="billingStreet"
                          value={formData.billingStreet}
                          onChange={handleInputChange}
                          required={!formData.sameAsShipping}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                        />
                      </div>

                      <div>
                        <label htmlFor="billingCity" className="block text-sm font-medium text-gray-700">
                          City
                        </label>
                        <input
                          type="text"
                          name="billingCity"
                          id="billingCity"
                          value={formData.billingCity}
                          onChange={handleInputChange}
                          required={!formData.sameAsShipping}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                        />
                      </div>

                      <div>
                        <label htmlFor="billingState" className="block text-sm font-medium text-gray-700">
                          State
                        </label>
                        <input
                          type="text"
                          name="billingState"
                          id="billingState"
                          value={formData.billingState}
                          onChange={handleInputChange}
                          required={!formData.sameAsShipping}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                        />
                      </div>

                      <div>
                        <label htmlFor="billingPostalCode" className="block text-sm font-medium text-gray-700">
                          Postal Code
                        </label>
                        <input
                          type="text"
                          name="billingPostalCode"
                          id="billingPostalCode"
                          value={formData.billingPostalCode}
                          onChange={handleInputChange}
                          required={!formData.sameAsShipping}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                        />
                      </div>

                      <div>
                        <label htmlFor="billingCountry" className="block text-sm font-medium text-gray-700">
                          Country
                        </label>
                        <select
                          name="billingCountry"
                          id="billingCountry"
                          value={formData.billingCountry}
                          onChange={handleInputChange}
                          className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                        >
                          <option value="United States">United States</option>
                          <option value="Canada">Canada</option>
                          <option value="United Kingdom">United Kingdom</option>
                        </select>
                      </div>
                    </div>
                  )}
                </div>

                <div className="mt-6 flex justify-between">
                  <button
                    type="button"
                    onClick={handleBack}
                    className="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                  >
                    Back
                  </button>
                  <button
                    type="button"
                    onClick={handleNext}
                    disabled={!validateStep(2)}
                    className="bg-primary-600 border border-transparent rounded-md shadow-sm py-2 px-4 text-sm font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                  >
                    Review Order
                  </button>
                </div>
              </div>
            )}

            {/* Step 3: Review Order */}
            {step === 3 && (
              <div className="bg-white shadow rounded-lg p-6">
                <h2 className="text-lg font-medium text-gray-900 mb-6">Review Your Order</h2>
                
                <div className="space-y-6">
                  <div>
                    <h3 className="text-sm font-medium text-gray-900">Order Items</h3>
                    <ul className="mt-3 divide-y divide-gray-200">
                      {cart.items.map((item) => (
                        <li key={item.id} className="py-3 flex justify-between">
                          <div className="flex">
                            <img
                              src={item.productImageUrl || 'https://via.placeholder.com/150x150?text=No+Image'}
                              alt={item.productName}
                              className="h-12 w-12 rounded-md object-cover"
                            />
                            <div className="ml-3">
                              <p className="text-sm font-medium text-gray-900">{item.productName}</p>
                              <p className="text-sm text-gray-500">Qty: {item.quantity}</p>
                            </div>
                          </div>
                          <p className="text-sm font-medium text-gray-900">${item.totalPrice.toFixed(2)}</p>
                        </li>
                      ))}
                    </ul>
                  </div>

                  <div>
                    <h3 className="text-sm font-medium text-gray-900">Shipping Address</h3>
                    <p className="mt-1 text-sm text-gray-600">
                      {formData.shippingStreet}<br />
                      {formData.shippingCity}, {formData.shippingState} {formData.shippingPostalCode}<br />
                      {formData.shippingCountry}
                    </p>
                  </div>

                  <div>
                    <h3 className="text-sm font-medium text-gray-900">Payment Method</h3>
                    <p className="mt-1 text-sm text-gray-600">
                      {formData.paymentMethod.replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase())}
                    </p>
                  </div>

                  <div>
                    <label htmlFor="notes" className="block text-sm font-medium text-gray-700">
                      Order Notes (Optional)
                    </label>
                    <textarea
                      name="notes"
                      id="notes"
                      rows={3}
                      value={formData.notes}
                      onChange={handleInputChange}
                      placeholder="Any special instructions for your order..."
                      className="mt-1 block w-full border-gray-300 rounded-md shadow-sm focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                    />
                  </div>
                </div>

                <div className="mt-6 flex justify-between">
                  <button
                    type="button"
                    onClick={handleBack}
                    className="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
                  >
                    Back
                  </button>
                  <button
                    type="submit"
                    disabled={loading}
                    className="bg-primary-600 border border-transparent rounded-md shadow-sm py-2 px-4 text-sm font-medium text-white hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50"
                  >
                    {loading ? 'Placing Order...' : 'Place Order'}
                  </button>
                </div>
              </div>
            )}
          </form>
        </div>

        {/* Order Summary Sidebar */}
        <div className="mt-10 lg:mt-0 lg:col-span-5">
          <div className="bg-gray-50 rounded-lg px-4 py-6 sm:p-6 lg:p-8 sticky top-8">
            <h2 className="text-lg font-medium text-gray-900">Order Summary</h2>

            <div className="mt-6 space-y-4">
              <div className="flex items-center justify-between">
                <dt className="text-sm text-gray-600">Subtotal</dt>
                <dd className="text-sm font-medium text-gray-900">${subtotal.toFixed(2)}</dd>
              </div>
              <div className="flex items-center justify-between">
                <dt className="text-sm text-gray-600">Shipping</dt>
                <dd className="text-sm font-medium text-gray-900">${shipping.toFixed(2)}</dd>
              </div>
              <div className="flex items-center justify-between">
                <dt className="text-sm text-gray-600">Tax</dt>
                <dd className="text-sm font-medium text-gray-900">${tax.toFixed(2)}</dd>
              </div>
              <div className="border-t border-gray-200 pt-4 flex items-center justify-between">
                <dt className="text-base font-medium text-gray-900">Order total</dt>
                <dd className="text-base font-medium text-gray-900">${total.toFixed(2)}</dd>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
