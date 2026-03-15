# 🚀 Guía de despliegue — BudgetApp

Esta guía te permite desplegar la API en la nube para que recruiters y entrevistadores puedan probarla sin instalar nada.

---

## Requisitos previos

- **Cuenta en GitHub** (el código debe estar en un repositorio)
- **Cuenta** en la plataforma que elijas (Render, Railway o Fly.io)

---

## Opción 1: Render (recomendado — más sencillo)

1. **Sube el proyecto a GitHub** (si aún no lo has hecho):
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/TU_USUARIO/budgetapp.git
   git push -u origin main
   ```

2. **Crea cuenta en [Render](https://render.com)** (gratis con GitHub).

3. **Nuevo Web Service**:
   - Dashboard → **New** → **Web Service**
   - Conecta tu repositorio de GitHub
   - Render detectará automáticamente el `render.yaml`

4. **Configuración** (si no usas Blueprint):
   - **Build Command**: *(vacío — usa Dockerfile)*
   - **Start Command**: *(vacío — usa Dockerfile)*
   - **Environment**: `SPRING_PROFILES_ACTIVE` = `cloud`

5. **Deploy**: Render construirá la imagen Docker y desplegará. Obtendrás una URL como:
   ```
   https://budgetapp-api.onrender.com
   ```

6. **Prueba la API**:
   ```bash
   curl https://TU-URL.onrender.com/api/saldo
   curl -X POST https://TU-URL.onrender.com/api/transacciones/simple \
     -H "Content-Type: application/json" \
     -d '{"descripcion": "Nómina", "monto": 1800.0, "tipo": "INGRESO"}'
   ```

⚠️ **Nota**: En el plan gratuito, Render "duerme" el servicio tras ~15 min de inactividad. La primera petición puede tardar 30–60 s en responder.

---

## Opción 2: Railway

1. **Sube el proyecto a GitHub**.

2. **Crea cuenta en [Railway](https://railway.app)** (gratis con GitHub).

3. **Nuevo proyecto**:
   - **New Project** → **Deploy from GitHub repo**
   - Selecciona tu repositorio
   - Railway detectará el `Dockerfile` automáticamente

4. **Variables de entorno** (Settings → Variables):
   - `SPRING_PROFILES_ACTIVE` = `cloud`

5. **Genera dominio**: Settings → Networking → **Generate Domain**

6. **Prueba la API** con la URL generada.

---

## Opción 3: Fly.io

1. **Instala Fly CLI**:
   ```bash
   curl -L https://fly.io/install.sh | sh
   ```

2. **Inicia sesión**:
   ```bash
   fly auth login
   ```

3. **Despliega** (desde la raíz del proyecto):
   ```bash
   cd budgetapp
   fly launch
   ```
   - Responde las preguntas (nombre de app, región, etc.)
   - No añadas base de datos PostgreSQL si te lo pregunta

4. **Despliegue**:
   ```bash
   fly deploy
   ```

5. **URL**: `https://budgetapp-api.fly.dev` (o la que te asigne Fly)

---

## Añadir la URL al README

Una vez desplegado, actualiza tu `README.md` o `README_PUBLIC.md` con algo como:

```markdown
## 🌐 API en vivo

**URL**: https://tu-app.onrender.com (o la que tengas)

### Probar la API

```bash
# Saldo actual
curl https://tu-app.onrender.com/api/saldo

# Registrar un ingreso
curl -X POST https://tu-app.onrender.com/api/transacciones/simple \
  -H "Content-Type: application/json" \
  -d '{"descripcion": "Nómina", "monto": 1800.0, "tipo": "INGRESO"}'

# Resumen del mes
curl https://tu-app.onrender.com/api/resumen/mes-actual
```
```

---

## Archivos de despliegue incluidos

| Archivo | Uso |
|---------|-----|
| `Dockerfile` | Imagen Docker (Java 21, multi-stage) |
| `.dockerignore` | Excluye archivos innecesarios del build |
| `render.yaml` | Blueprint para Render |
| `railway.json` | Configuración para Railway |
| `fly.toml` | Configuración para Fly.io |
| `application-cloud.properties` | Perfil Spring para cloud (H2 en memoria) |

---

## Notas importantes

- **Datos efímeros**: En cloud usamos H2 en memoria. Los datos se pierden al reiniciar el servicio (normal en demos).
- **H2 Console deshabilitada**: Por seguridad, la consola H2 está desactivada en el perfil `cloud`.
- **Java 21 en Docker**: El `Dockerfile` usa Java 21 para máxima compatibilidad con las plataformas cloud.
