# Placeholder Dockerfile for User Portal
# This will serve a simple placeholder page until the frontend is developed

FROM nginx:alpine

# Create a simple placeholder HTML page
RUN echo '<html><head><title>User Portal</title><style>body{font-family:Arial,sans-serif;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;background:linear-gradient(135deg,#f093fb 0%,#f5576c 100%);color:white;text-align:center;}h1{font-size:3em;margin-bottom:20px;}p{font-size:1.2em;}</style></head><body><div><h1>👥 User Portal</h1><p>Frontend application coming soon...</p><p>Port: 3001</p></div></body></html>' > /usr/share/nginx/html/index.html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]

# =============================================================================
# NOTE: Once you have your frontend application ready, replace this Dockerfile
# with the following multi-stage build configuration:
# =============================================================================
#
# # Multi-stage build for React/Vue/Angular application
# FROM node:20-alpine AS build
# WORKDIR /app
# COPY package*.json ./
# RUN npm ci
# COPY . .
# RUN npm run build
# 
# FROM nginx:alpine
# COPY --from=build /app/dist /usr/share/nginx/html
# # For React: use /app/build instead of /app/dist
# # For Angular: use /app/dist/user-portal
# COPY nginx.conf /etc/nginx/conf.d/default.conf
# EXPOSE 80
# CMD ["nginx", "-g", "daemon off;"]
