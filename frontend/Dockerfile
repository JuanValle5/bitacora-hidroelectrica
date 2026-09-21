# ============================================================
# Dockerfile para Frontend React / TypeScript / Vite
# Multi-stage build con Nginx para servir la SPA
# ============================================================

# Stage 1: Build SPA
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci || npm install
COPY . .
ARG VITE_API_URL=http://localhost:8081/api/v1
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# Stage 2: Runtime Nginx
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
