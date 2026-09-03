# --- Etap 1: budowanie ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# --- Etap 2: finalny obraz (nginx) ---
FROM nginx:alpine
ARG NGINX_CONF=nginx.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY ${NGINX_CONF} /etc/nginx/conf.d/default.conf
EXPOSE 80