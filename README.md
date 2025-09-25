# E-Commerce Platform

A complete, production-ready e-commerce platform built with React.js frontend and Java Spring Boot backend.

## 🚀 Features

### Customer Features
- **User Authentication**: Firebase-powered authentication with email/password and Google sign-in
- **Product Catalog**: Browse products by categories, search, filter by price and brand
- **Shopping Cart**: Add/remove items, update quantities, real-time cart updates
- **Checkout Process**: Multi-step checkout with shipping and billing addresses
- **Order Management**: View order history, track orders, order status updates
- **User Profile**: Manage personal information and addresses

### Admin Features
- **Admin Dashboard**: Overview of orders, revenue, customers, and products
- **Product Management**: Add, edit, delete products and categories
- **Order Management**: Process orders, update order status, manage shipments
- **User Management**: View and manage customer accounts
- **Analytics Dashboard**: Sales reports and business insights

### Technical Features
- **Responsive Design**: Mobile-first design with Tailwind CSS
- **Real-time Updates**: Live cart updates and order status changes
- **Secure Payments**: Stripe integration for payment processing
- **Role-based Access**: Customer and admin role management
- **API Documentation**: RESTful API with comprehensive endpoints
- **Database Management**: MySQL with JPA/Hibernate ORM

## 🛠️ Tech Stack

### Frontend
- **React.js 18** with TypeScript
- **Tailwind CSS** for styling
- **React Router** for navigation
- **Context API** for state management
- **Axios** for API calls
- **Firebase** for authentication
- **React Hot Toast** for notifications
- **Headless UI** for accessible components

### Backend
- **Java 17** with Spring Boot 3.2
- **Spring Security** for authentication
- **Spring Data JPA** with Hibernate
- **MySQL** database
- **Firebase Admin SDK** for token verification
- **Stripe API** for payment processing
- **Maven** for dependency management
- **Lombok** for reducing boilerplate code

## 📋 Prerequisites

- **Node.js** 18+ and npm
- **Java** 17+
- **Maven** 3.6+
- **MySQL** 8.0+
- **Firebase** project (for authentication)
- **Stripe** account (for payments, optional for demo)

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd WebSite
```

### 2. Backend Setup

#### Configure Database
```sql
CREATE DATABASE ecommerce_db;
CREATE USER 'ecommerce_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'ecommerce_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Set Environment Variables
```bash
# Database
export DB_URL=jdbc:mysql://localhost:3306/ecommerce_db
export DB_USERNAME=ecommerce_user
export DB_PASSWORD=your_password

# Firebase (get from Firebase Console)
export FIREBASE_SERVICE_ACCOUNT_KEY='{"type": "service_account", ...}'

# Stripe (get from Stripe Dashboard)
export STRIPE_SECRET_KEY=sk_test_...
export STRIPE_PUBLISHABLE_KEY=pk_test_...

# JWT
export JWT_SECRET=your-secret-key
export JWT_EXPIRATION=86400000
```

#### Run Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 3. Frontend Setup

#### Set Environment Variables
Create `frontend/.env.local`:
```env
# Firebase Configuration (get from Firebase Console)
REACT_APP_FIREBASE_API_KEY=your_api_key
REACT_APP_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
REACT_APP_FIREBASE_PROJECT_ID=your_project_id
REACT_APP_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
REACT_APP_FIREBASE_MESSAGING_SENDER_ID=123456789
REACT_APP_FIREBASE_APP_ID=1:123456789:web:abcdef

# Backend API URL
REACT_APP_API_BASE_URL=http://localhost:8080/api

# Stripe
REACT_APP_STRIPE_PUBLISHABLE_KEY=pk_test_...
```

#### Install and Run
```bash
cd frontend
npm install
npm start
```

The frontend will start on `http://localhost:3000`

## 🔧 Configuration

### Firebase Setup
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Enable Authentication with Email/Password and Google providers
3. Get your configuration keys and service account key
4. Add authorized domains for your application

### Stripe Setup
1. Create a Stripe account at [Stripe Dashboard](https://dashboard.stripe.com)
2. Get your publishable and secret keys
3. Configure webhooks for payment confirmations (production)

### Database Migration
The application uses JPA with `ddl-auto=update` for development. For production:
1. Set `ddl-auto=validate`
2. Use Flyway or Liquibase for database migrations
3. Create proper database indexes for performance

## 📁 Project Structure

```
WebSite/
├── backend/                 # Spring Boot API
│   ├── src/main/java/
│   │   └── com/ecommerce/
│   │       ├── config/      # Configuration classes
│   │       ├── controller/  # REST controllers
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── entity/      # JPA entities
│   │       ├── repository/  # Data repositories
│   │       ├── service/     # Business logic
│   │       └── security/    # Security configuration
│   └── src/main/resources/
│       └── application.properties
├── frontend/                # React application
│   ├── src/
│   │   ├── components/      # Reusable components
│   │   ├── contexts/        # React contexts
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   └── config/          # Configuration files
│   └── public/
└── README.md
```

## 🔐 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Firebase Integration**: Reliable authentication provider
- **CORS Configuration**: Properly configured cross-origin requests
- **Input Validation**: Server-side validation for all inputs
- **SQL Injection Prevention**: Parameterized queries with JPA
- **Role-based Access**: Different access levels for customers and admins

## 🚀 Deployment

### Backend Deployment
1. Build the JAR file: `mvn clean package`
2. Set production environment variables
3. Deploy to your cloud provider (AWS, Google Cloud, Heroku)
4. Configure production database and connection pooling

### Frontend Deployment
1. Build for production: `npm run build`
2. Deploy static files to CDN or hosting service
3. Configure environment variables for production
4. Set up proper HTTPS and domain configuration

## 📊 Sample Data

The application includes a data initialization service that populates the database with:
- Sample categories (Electronics, Clothing, Home & Garden, etc.)
- Sample products with images and descriptions
- Admin user account (for testing)

## 🔍 API Documentation

### Authentication Endpoints
- `POST /api/users/register` - User registration
- `GET /api/users/profile` - Get user profile

### Product Endpoints
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/slug/{slug}` - Get product by slug
- `GET /api/products/search` - Search products
- `GET /api/products/featured` - Get featured products

### Cart Endpoints
- `GET /api/cart` - Get user's cart
- `POST /api/cart/add` - Add item to cart
- `PUT /api/cart/items/{id}` - Update cart item quantity
- `DELETE /api/cart/items/{id}` - Remove item from cart

### Order Endpoints
- `POST /api/orders` - Create new order
- `GET /api/orders/user` - Get user's orders
- `GET /api/orders/{id}` - Get order details
- `POST /api/orders/{id}/cancel` - Cancel order

## 🧪 Testing

### Backend Testing
```bash
cd backend
mvn test
```

### Frontend Testing
```bash
cd frontend
npm test
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new features
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the documentation in the code comments
- Review the API endpoints and their usage

## 🔮 Future Enhancements

- **Product Reviews**: Customer reviews and ratings
- **Wishlist**: Save products for later
- **Inventory Management**: Advanced inventory tracking
- **Email Notifications**: Order confirmations and updates
- **Multi-language Support**: Internationalization
- **Advanced Analytics**: Detailed business intelligence
- **Mobile App**: React Native mobile application
