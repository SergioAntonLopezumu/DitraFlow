# DitraFlow

Trabajo Fin de Grado — **Factores de éxito en un proceso de digitalización de PYMES**

- **Autor:** Sergio Antón López
- **Tutores:** María José Candel Romero y Francisco García Sánchez
- **Universidad:** Universidad de Murcia (UMU)
- **Grado:** Ingeniería Informática
- **Demo:** [ditraflow.shadowzones.org](https://ditraflow.shadowzones.org)

## Descripción

DitraFlow es una aplicación web responsiva que ayuda a las **pequeñas y medianas empresas (PYMES)** a afrontar su proceso de digitalización con menos incertidumbre. Para ello:

1. Evalúa su **nivel de madurez digital** mediante una encuesta personalizada según el sector económico (CNAE).
2. **Prioriza** matemáticamente (algoritmo TOPSIS) en qué áreas conviene invertir primero, según la brecha y el impacto potencial detectados en cada dimensión.
3. Genera una **hoja de ruta (roadmap)** personalizada y por fases.
4. Ofrece un **chatbot** con inteligencia artificial que acompaña a la empresa durante todo el proceso.

El proyecto nace de analizar las limitaciones de herramientas públicas existentes (Acelera pyme, HADA y DMAT), que carecen de cuestionarios adaptados al sector, navegación cómoda y soporte continuo durante el proceso de evaluación.

## Arquitectura

Aplicación **cliente-servidor en capas**, siguiendo el patrón **MVC**:

- **Capa de presentación (frontend):** interfaz de usuario.
- **Capa de control:** controladores REST que reciben las peticiones del frontend.
- **Capa de servicios:** lógica de negocio (autenticación, encuestas, análisis, roadmap, chatbot, IA).
- **Capa de datos:** persistencia mediante un modelo relacional.

### Backend

- **Spring Boot** (Java) como framework principal, con arquitectura **API REST**.
- **Spring Data JPA + Hibernate** para la persistencia y el mapeo objeto-relacional.
- **Spring Security + JWT + Bcrypt** para autenticación y seguridad.
- **Apache HttpClient** para la comunicación con las APIs de IA (OpenAI / Ollama), garantizando continuidad del servicio aunque falle una de ellas.
- Principales controladores: Autenticación, Encuestas, Respuestas de encuesta, Roadmaps y Chatbot.
- Entidades del dominio: `User`, `Survey`, `Question`, `SurveyResponse`, `Answer`, `Result`, `Roadmap`, `ChatMessage`.

### Frontend

- **React + TypeScript**, con arquitectura basada en **componentes** reutilizables.
- **Vite** como herramienta de build (sustituye a Create React App).
- **CSS Modules** para estilos encapsulados por componente.
- **Framer Motion** para animaciones.
- Estructurado en 4 páginas principales: `Home`, `Register`, `Login` y `Dashboard` (esta última con encuesta, resultados, roadmap, chatbot y ajustes).
- Diseño totalmente responsivo (escritorio, tablet y móvil).

### Base de datos

- **PostgreSQL**, base de datos relacional elegida por su cumplimiento estricto de ACID.

## Validación

El sistema se ha validado con una batería de **31 pruebas de integración** sobre los cinco casos de uso principales, con cobertura adicional de seguridad y aislamiento de datos.

## 📄 Memoria

La memoria completa del TFG, con el estado del arte, el diseño del sistema y la comparación con las herramientas existentes, está disponible en este repositorio.
