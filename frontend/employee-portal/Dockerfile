# Placeholder Dockerfile for the Employee Portal
# Replace with a multi-stage build (documented below) once the organiser UI is production ready.

FROM nginx:alpine

# Temporary splash page so the container serves something useful
RUN cat <<'HTML' > /usr/share/nginx/html/index.html
<!doctype html>
<html>
  <head>
    <title>Employee Portal</title>
    <style>
      body {
        font-family: Arial, sans-serif;
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0;
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: white;
        text-align: center;
      }
      h1 { font-size: 3rem; margin-bottom: 1rem; }
      p { font-size: 1.1rem; }
    </style>
  </head>
  <body>
    <div>
      <h1>Employee Portal</h1>
      <p>Organiser frontend build coming soon...</p>
      <p>Port: 3002</p>
    </div>
  </body>
</html>
HTML

COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

# =============================================================================
# Multi-stage build template
# =============================================================================
# FROM node:20-alpine AS build
# WORKDIR /app
# COPY package*.json ./
# RUN npm ci
# COPY . .
# RUN npm run build
#
# FROM nginx:alpine
# COPY --from=build /app/dist /usr/share/nginx/html
# COPY nginx.conf /etc/nginx/conf.d/default.conf
# EXPOSE 80
# CMD ["nginx", "-g", "daemon off;"]
