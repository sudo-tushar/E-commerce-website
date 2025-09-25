import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from './AuthContext';
import { toast } from 'react-hot-toast';

interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productSlug: string;
  productImageUrl?: string;
  unitPrice: number;
  quantity: number;
  totalPrice: number;
  isAvailable: boolean;
}

interface Cart {
  id: number;
  items: CartItem[];
  totalAmount: number;
  totalItems: number;
}

interface CartContextType {
  cart: Cart | null;
  loading: boolean;
  addToCart: (productId: number, quantity: number) => Promise<void>;
  updateCartItem: (cartItemId: number, quantity: number) => Promise<void>;
  removeFromCart: (cartItemId: number) => Promise<void>;
  clearCart: () => Promise<void>;
  refreshCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const useCart = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const { currentUser } = useAuth();

  const refreshCart = async () => {
    if (!currentUser) {
      setCart(null);
      setLoading(false);
      return;
    }

    try {
      const response = await api.get('/cart');
      setCart(response.data);
    } catch (error: any) {
      console.error('Error fetching cart:', error);
      if (error.response?.status !== 404) {
        // Create empty cart if it doesn't exist
        setCart({
          id: 0,
          items: [],
          totalAmount: 0,
          totalItems: 0,
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (productId: number, quantity: number) => {
    try {
      const response = await api.post('/cart/add', {
        productId,
        quantity,
      });
      setCart(response.data);
      toast.success('Added to cart!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to add to cart');
      throw error;
    }
  };

  const updateCartItem = async (cartItemId: number, quantity: number) => {
    try {
      const response = await api.put(`/cart/items/${cartItemId}?quantity=${quantity}`);
      setCart(response.data);
      toast.success('Cart updated!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to update cart');
      throw error;
    }
  };

  const removeFromCart = async (cartItemId: number) => {
    try {
      const response = await api.delete(`/cart/items/${cartItemId}`);
      setCart(response.data);
      toast.success('Item removed from cart!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to remove item');
      throw error;
    }
  };

  const clearCart = async () => {
    try {
      await api.delete('/cart');
      setCart({
        id: 0,
        items: [],
        totalAmount: 0,
        totalItems: 0,
      });
      toast.success('Cart cleared!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to clear cart');
      throw error;
    }
  };

  useEffect(() => {
    refreshCart();
  }, [currentUser]); // eslint-disable-line react-hooks/exhaustive-deps

  const value: CartContextType = {
    cart,
    loading,
    addToCart,
    updateCartItem,
    removeFromCart,
    clearCart,
    refreshCart,
  };

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};
