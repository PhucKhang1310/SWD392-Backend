FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ENV NODE_ENV=production
ENV PORT=8080
EXPOSE 8080
CMD ["node", "./bin/www"]
