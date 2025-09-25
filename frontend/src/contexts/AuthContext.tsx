import React, { createContext, useContext, useEffect, useState } from 'react';
import {
  User,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
  GoogleAuthProvider,
  signInWithPopup,
  sendPasswordResetEmail,
  updateProfile,
} from 'firebase/auth';
import { auth } from '../config/firebase';
import api from '../services/api';
import { toast } from 'react-hot-toast';

interface UserData {
  id?: number;
  firebaseUid: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  role: 'CUSTOMER' | 'ADMIN';
  isActive: boolean;
}

interface AuthContextType {
  currentUser: User | null;
  userData: UserData | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, firstName: string, lastName: string, phone?: string) => Promise<void>;
  logout: () => Promise<void>;
  loginWithGoogle: () => Promise<void>;
  resetPassword: (email: string) => Promise<void>;
  updateUserProfile: (firstName: string, lastName: string, phone?: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [userData, setUserData] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchUserData = async (user: User) => {
    try {
      const response = await api.get('/users/profile');
      setUserData(response.data);
    } catch (error) {
      console.error('Error fetching user data:', error);
    }
  };

  const registerUserInBackend = async (user: User, firstName: string, lastName: string, phone?: string) => {
    try {
      const response = await api.post('/users/register', {
        firebaseUid: user.uid,
        firstName,
        lastName,
        email: user.email,
        phone,
      });
      setUserData(response.data);
    } catch (error) {
      console.error('Error registering user in backend:', error);
      throw error;
    }
  };

  const login = async (email: string, password: string) => {
    try {
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      await fetchUserData(userCredential.user);
      toast.success('Successfully logged in!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to log in');
      throw error;
    }
  };

  const signup = async (email: string, password: string, firstName: string, lastName: string, phone?: string) => {
    try {
      const userCredential = await createUserWithEmailAndPassword(auth, email, password);
      
      // Update Firebase profile
      await updateProfile(userCredential.user, {
        displayName: `${firstName} ${lastName}`,
      });

      // Register in backend
      await registerUserInBackend(userCredential.user, firstName, lastName, phone);
      
      toast.success('Account created successfully!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to create account');
      throw error;
    }
  };

  const loginWithGoogle = async () => {
    try {
      const provider = new GoogleAuthProvider();
      const userCredential = await signInWithPopup(auth, provider);
      const user = userCredential.user;
      
      const displayName = user.displayName || '';
      const [firstName = '', lastName = ''] = displayName.split(' ');
      
      // Try to fetch existing user data or register new user
      try {
        await fetchUserData(user);
      } catch (error) {
        // If user doesn't exist, register them
        await registerUserInBackend(user, firstName, lastName);
      }
      
      toast.success('Successfully logged in with Google!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to log in with Google');
      throw error;
    }
  };

  const logout = async () => {
    try {
      await signOut(auth);
      setUserData(null);
      toast.success('Successfully logged out!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to log out');
      throw error;
    }
  };

  const resetPassword = async (email: string) => {
    try {
      await sendPasswordResetEmail(auth, email);
      toast.success('Password reset email sent!');
    } catch (error: any) {
      toast.error(error.message || 'Failed to send password reset email');
      throw error;
    }
  };

  const updateUserProfile = async (firstName: string, lastName: string, phone?: string) => {
    try {
      if (currentUser) {
        await updateProfile(currentUser, {
          displayName: `${firstName} ${lastName}`,
        });
        
        const response = await api.put('/users/profile', {
          firstName,
          lastName,
          phone,
          email: currentUser.email,
          firebaseUid: currentUser.uid,
        });
        
        setUserData(response.data);
        toast.success('Profile updated successfully!');
      }
    } catch (error: any) {
      toast.error(error.message || 'Failed to update profile');
      throw error;
    }
  };

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      setCurrentUser(user);
      if (user) {
        await fetchUserData(user);
      } else {
        setUserData(null);
      }
      setLoading(false);
    });

    return unsubscribe;
  }, []);

  const value: AuthContextType = {
    currentUser,
    userData,
    loading,
    login,
    signup,
    logout,
    loginWithGoogle,
    resetPassword,
    updateUserProfile,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
