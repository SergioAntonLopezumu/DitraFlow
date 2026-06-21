FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
# Para producción es mejor usar Nginx, pero para empezar:
CMD ["npm", "run", "dev", "--", "--host"]
