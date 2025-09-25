#!/bin/bash

# E-Commerce Platform Deployment Script
# This script builds and deploys both frontend and backend

set -e  # Exit on any error

echo "🚀 Starting E-Commerce Platform Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    command -v java >/dev/null 2>&1 || { print_error "Java is required but not installed. Please install Java 17+"; exit 1; }
    command -v mvn >/dev/null 2>&1 || { print_error "Maven is required but not installed. Please install Maven 3.6+"; exit 1; }
    command -v node >/dev/null 2>&1 || { print_error "Node.js is required but not installed. Please install Node.js 18+"; exit 1; }
    command -v npm >/dev/null 2>&1 || { print_error "npm is required but not installed. Please install npm"; exit 1; }
    
    print_success "All prerequisites are installed"
}

# Build backend
build_backend() {
    print_status "Building backend..."
    cd backend
    
    # Clean and compile
    mvn clean compile
    
    # Run tests
    print_status "Running backend tests..."
    mvn test
    
    # Package application
    print_status "Packaging backend application..."
    mvn package -DskipTests
    
    cd ..
    print_success "Backend build completed"
}

# Build frontend
build_frontend() {
    print_status "Building frontend..."
    cd frontend
    
    # Install dependencies
    print_status "Installing frontend dependencies..."
    npm install
    
    # Run tests
    print_status "Running frontend tests..."
    npm test -- --coverage --silent --watchAll=false
    
    # Build for production
    print_status "Building frontend for production..."
    npm run build
    
    cd ..
    print_success "Frontend build completed"
}

# Setup database
setup_database() {
    print_status "Database setup..."
    print_warning "Please ensure MySQL is running and database is created"
    print_warning "Run these SQL commands manually:"
    echo "CREATE DATABASE IF NOT EXISTS ecommerce_db;"
    echo "CREATE USER IF NOT EXISTS 'ecommerce_user'@'localhost' IDENTIFIED BY 'your_password';"
    echo "GRANT ALL PRIVILEGES ON ecommerce_db.* TO 'ecommerce_user'@'localhost';"
    echo "FLUSH PRIVILEGES;"
    echo ""
    read -p "Press Enter to continue once database is set up..."
}

# Start backend
start_backend() {
    print_status "Starting backend server..."
    cd backend
    
    # Check if environment variables are set
    if [[ -z "$DB_URL" || -z "$DB_USERNAME" || -z "$DB_PASSWORD" ]]; then
        print_warning "Database environment variables not set. Using defaults."
        print_warning "Set DB_URL, DB_USERNAME, and DB_PASSWORD for production"
    fi
    
    # Start Spring Boot application
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=prod > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > ../backend.pid
    
    cd ..
    print_success "Backend started with PID: $BACKEND_PID"
}

# Start frontend (for development)
start_frontend_dev() {
    print_status "Starting frontend development server..."
    cd frontend
    
    # Start React development server
    nohup npm start > ../logs/frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > ../frontend.pid
    
    cd ..
    print_success "Frontend development server started with PID: $FRONTEND_PID"
}

# Deploy frontend to production (static files)
deploy_frontend_prod() {
    print_status "Deploying frontend for production..."
    
    if [ ! -d "frontend/build" ]; then
        print_error "Frontend build directory not found. Run build first."
        exit 1
    fi
    
    # Create deployment directory
    mkdir -p deploy/frontend
    
    # Copy build files
    cp -r frontend/build/* deploy/frontend/
    
    print_success "Frontend deployed to deploy/frontend/"
    print_status "Serve these files with nginx or your preferred web server"
}

# Stop services
stop_services() {
    print_status "Stopping services..."
    
    if [ -f "backend.pid" ]; then
        BACKEND_PID=$(cat backend.pid)
        kill $BACKEND_PID 2>/dev/null || true
        rm backend.pid
        print_success "Backend stopped"
    fi
    
    if [ -f "frontend.pid" ]; then
        FRONTEND_PID=$(cat frontend.pid)
        kill $FRONTEND_PID 2>/dev/null || true
        rm frontend.pid
        print_success "Frontend stopped"
    fi
}

# Create logs directory
mkdir -p logs

# Parse command line arguments
case "${1:-help}" in
    "build")
        check_prerequisites
        build_backend
        build_frontend
        print_success "Build completed successfully!"
        ;;
    "deploy-dev")
        check_prerequisites
        setup_database
        build_backend
        build_frontend
        start_backend
        sleep 10  # Wait for backend to start
        start_frontend_dev
        print_success "Development deployment completed!"
        print_status "Backend: http://localhost:8080/api"
        print_status "Frontend: http://localhost:3000"
        print_status "Use './deploy.sh stop' to stop services"
        ;;
    "deploy-prod")
        check_prerequisites
        setup_database
        build_backend
        build_frontend
        deploy_frontend_prod
        start_backend
        print_success "Production deployment completed!"
        print_status "Backend: http://localhost:8080/api"
        print_status "Frontend files: ./deploy/frontend/"
        print_warning "Configure your web server to serve frontend files"
        ;;
    "stop")
        stop_services
        ;;
    "clean")
        print_status "Cleaning build artifacts..."
        rm -rf backend/target
        rm -rf frontend/build
        rm -rf frontend/node_modules
        rm -rf deploy
        rm -rf logs
        print_success "Clean completed"
        ;;
    "help"|*)
        echo "E-Commerce Platform Deployment Script"
        echo ""
        echo "Usage: $0 [COMMAND]"
        echo ""
        echo "Commands:"
        echo "  build        Build both frontend and backend"
        echo "  deploy-dev   Deploy for development (with hot reload)"
        echo "  deploy-prod  Deploy for production"
        echo "  stop         Stop running services"
        echo "  clean        Clean build artifacts"
        echo "  help         Show this help message"
        echo ""
        echo "Environment Variables:"
        echo "  DB_URL                Database connection URL"
        echo "  DB_USERNAME           Database username"
        echo "  DB_PASSWORD           Database password"
        echo "  FIREBASE_SERVICE_ACCOUNT_KEY  Firebase service account JSON"
        echo "  STRIPE_SECRET_KEY     Stripe secret key"
        echo "  STRIPE_PUBLISHABLE_KEY  Stripe publishable key"
        echo "  JWT_SECRET            JWT signing secret"
        echo ""
        echo "Examples:"
        echo "  $0 build              # Build the application"
        echo "  $0 deploy-dev         # Start development environment"
        echo "  $0 deploy-prod        # Deploy for production"
        echo "  $0 stop               # Stop all services"
        ;;
esac
