# 🚀 Deployment guide — Finanz API

This guide allows you to deploy the API in the cloud so that recruiters and interviewers can test it without installing anything.

---

## ⚡ Cardless alternative: GitHub Codespaces

If you don't want to add card in Render/Railway/Fly.io, use **GitHub Codespaces**:

1. The "Open in GitHub Codespaces" button is already in the README
2. Anyone with a GitHub account can open the repo and the API runs automatically
3. In the PORTS tab, port 8080 gives a public URL to test the endpoints
4. **No card required** — GitHub gives 120 hours/month free to personal accounts

Required files: `.devcontainer/devcontainer.json` (already included).

---

---

## Prerequisites

- **GitHub account** (code must be in a repository)
- **Account** on the platform of your choice (Render, Railway or Fly.io)

---

## Option 1: Render (recommended — easier)

1. **Upload the project to GitHub** (if you haven't already):
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/TU_USUARIO/finanz-api.git
   git push -u origin main
   ```

2. **Create an account on [Render](https://render.com)** (free with GitHub).

3. **New Web Service**:
- Dashboard → **New** → **Web Service**
- Connect your GitHub repository
- Render will automatically detect the `render.yaml`

4. **Settings** (if you don't use Blueprint):
- **Build Command**: *(empty — uses Dockerfile)*
- **Start Command**: *(empty — uses Dockerfile)*
- **Environment**: `SPRING_PROFILES_ACTIVE` = `cloud`

5. **Deploy**: Render will build the Docker image and deploy. You will get a URL like:
   ```
   https://finanz-api.onrender.com
   ```

6. **Test the API**:
   ```bash
   curl https://TU-URL.onrender.com/api/saldo
   curl -X POST https://TU-URL.onrender.com/api/transacciones/simple \
     -H "Content-Type: application/json" \
     -d '{"descripcion": "Nómina", "monto": 1800.0, "tipo": "INGRESO"}'
   ```

⚠️ **Note**: On the free plan, Render "sleeps" the service after ~15 minutes of inactivity. The first request may take 30–60 s to respond.

---

## Option 2: Railway

1. **Upload the project to GitHub**.

2. **Create an account on [Railway](https://railway.app)** (free with GitHub).

3. **New project**:
- **New Project** → **Deploy from GitHub repo**
- Select your repository
- Railway will detect the `Dockerfile` automatically

4. **Environment variables** (Settings → Variables):
- `SPRING_PROFILES_ACTIVE` = `cloud`

5. **Generate Domain**: Settings → Networking → **Generate Domain**

6. **Test the API** with the generated URL.

---

## Option 3: Fly.io

1. **Install Fly CLI**:
   ```bash
   curl -L https://fly.io/install.sh | sh
   ```

2. **Login**:
   ```bash
   fly auth login
   ```

3. **Deploy** (from the project root):
   ```bash
   cd finanz-api # carpeta del módulo Finanz API
   fly launch
   ```
- Answer the questions (app name, region, etc.)
- Don't add PostgreSQL database if it asks you

4. **Deployment**:
   ```bash
   fly deploy
   ```

5. **URL**: `https://finanz-api.fly.dev` (or whatever Fly assigns you)

---

## Add URL to README

Once deployed, update your `README_ES.md` or `OVERVIEW_ES.md` with something like:

```markdown
## 🌐 API en vivo

**URL**: https://tu-app.onrender.com (o la que tengas)

### Probar la API

```bash
# Current balance
curl https://tu-app.onrender.com/api/saldo

# Register an entry
curl -X POST https://tu-app.onrender.com/api/transacciones/simple \
-H "Content-Type: application/json" \
-d '{"description": "Payroll", "amount": 1800.0, "type": "INCOME"}'

# Summary of the month
curl https://tu-app.onrender.com/api/resumen/mes-actual
```
```

---

## Deployment files included

|Archive|Use|
|---------|-----|
|`Dockerfile`|Docker image (Java 21, multi-stage)|
|`.dockerignore`|Exclude unnecessary files from the build|
|`render.yaml`|Blueprint to Render|
|`railway.json`|Configuration for Railway|
|`fly.toml`|Settings for Fly.io|
|`application-cloud.properties`|Spring profile for cloud (H2 in memory)|

---

## Important notes

- **Ephemeral data**: In the cloud we use H2 in memory. Data is lost when restarting the service (normal in demos).
- **H2 Console disabled**: For security, the H2 console is disabled in the `cloud` profile.
- **Java 21 in Docker**: The `Dockerfile` uses Java 21 for maximum compatibility with cloud platforms.
