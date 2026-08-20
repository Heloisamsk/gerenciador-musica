FROM node:24-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci --ignore-scripts

COPY . .

ARG BUILD_CONFIGURATION=production

RUN npm run build -- --configuration="${BUILD_CONFIGURATION}"


FROM nginxinc/nginx-unprivileged:alpine

COPY --chown=101:101 nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build --chown=101:101 \
    /app/dist/gerenciador_musica_frontend/browser/ \
    /usr/share/nginx/html/

USER 101:101

EXPOSE 8080
