// Detectar si estamos en local o producción
const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
const BASE_URL = isLocal 
  ? "http://localhost:8080/api"
  : "https://api-ditraflow.shadowzones.org/api";

const getHeaders = () => ({
  "Authorization": `Bearer ${localStorage.getItem("token") || ""}`,
  "Content-Type": "application/json"
});

// Manejador centralizado para procesar respuestas y errores
const handleResponse = async (response: Response, isLogin: boolean = false) => {
  if (!isLogin && (response.status === 401 || response.status === 403)) {
    localStorage.removeItem("token");
    window.location.href = "/login";
    throw new Error("Sesión expirada");
  }

  const contentType = response.headers.get("content-type");
  const isJson = contentType && contentType.includes("application/json");

  if (!response.ok) {
    // Si la respuesta no es OK, leemos el contenido según su tipo real
    if (isJson) {
      const errorData = await response.json().catch(() => ({ message: null }));
      throw new Error(errorData.message || "Error en el servidor");
    } else {
      const errorText = await response.text().catch(() => "Error desconocido en el servidor");
      throw new Error(errorText);
    }
  }

  // Si la respuesta es OK, parseamos según corresponda
  return isJson ? await response.json() : await response.text();
};

export const apiService = {
  // Autenticación
  login: async (credentials: any) => {
    const response = await fetch(`${BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
    });
    return handleResponse(response, true);
  },

  register: async (userData: any) => {
    const response = await fetch(`${BASE_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });
    return handleResponse(response);
  },

  getSurveyQuestions: async () => {
    const response = await fetch(`${BASE_URL}/survey/questions`, {
      headers: getHeaders()
    });
    return handleResponse(response);
  },

  getSurveyQuestionsWithSector: async (sector: string) => {
    const response = await fetch(`${BASE_URL}/survey/questions?sector=${encodeURIComponent(sector)}`, {
      headers: getHeaders()
    });
    return handleResponse(response);
  },

  generateRoadmapForResult: async (resultId: number) => {
    const response = await fetch(`${BASE_URL}/roadmap/generate-for-result/${resultId}`, {
      method: "POST",
      headers: getHeaders()
    });
    return handleResponse(response);
  },

  checkSurveyStatus: async () => {    
    const response = await fetch(`${BASE_URL}/user/survey-status`, {
      headers: getHeaders()
    });
    return handleResponse(response);
  },

  submitSurvey: async (data: any) => {
    const response = await fetch(`${BASE_URL}/survey/submit`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(data)
    });
    return handleResponse(response);
  },

  updatePassword: async (password: string) => {
    const response = await fetch(`${BASE_URL}/user/password`, {
      method: "PUT",
      headers: getHeaders(),
      body: JSON.stringify({ password })
    });
    return handleResponse(response);
  },

  resetProgress: async () => {
    const response = await fetch(`${BASE_URL}/user/reset-progress`, {
      method: "DELETE",
      headers: getHeaders()
    });
    return handleResponse(response);
  },

  logout: () => {
    localStorage.removeItem("token");
    window.location.href = "/login";
  },

  deleteAccount: async () => {
    const response = await fetch(`${BASE_URL}/user/delete-account`, {
      method: "DELETE",
      headers: getHeaders()
    });
    
    if (response.ok) {
        localStorage.removeItem("token");
    }
    
    return handleResponse(response);
  },

  getUserResults: async () => {
    const response = await fetch(`${BASE_URL}/results`, {
      headers: getHeaders()
    });

    if (response.status === 401 || response.status === 403 || response.status === 404) {
      console.warn(`Aviso de control: Estado ${response.status} recibido al buscar resultados.`);
      return []; 
    }

    if (!response.ok) {
      throw new Error("Error al obtener resultados del servidor");
    }

    const contentType = response.headers.get("content-type");
    return contentType && contentType.includes("application/json") 
      ? await response.json() 
      : [];
  },

  getResultById: async (resultId: string | number) => {
    const response = await fetch(`${BASE_URL}/results/${resultId}`, {
      headers: getHeaders()
    });

    if (response.status === 404) {
      console.warn(`Resultado con ID ${resultId} no fue encontrado en el servidor.`);
      return null;
    }

    return handleResponse(response);
  },

  sendChatMessage: async (message: string, resultId: number | null) => {
    // El backend espera Long resultId que puede ser null
    const body = {
      message: message.trim(),
      resultId: typeof resultId === 'number' && resultId > 0 ? resultId : null
    };
    const response = await fetch(`${BASE_URL}/chatbot/send`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(body)
    });
    return handleResponse(response);
  },

  getChatHistory: async () => {
    const response = await fetch(`${BASE_URL}/chatbot/history`, {
      headers: getHeaders()
    });
    // Si falla o no hay historial, devolvemos un array vacío preventivo
    if (response.status === 401 || response.status === 403 || response.status === 404) {
      return [];
    }
    return handleResponse(response);
  },

  getUserProfile: async () => {
    const response = await fetch(`${BASE_URL}/user/profile`, { // O la ruta correcta en tu backend
      headers: getHeaders()
    });
    return handleResponse(response);
  },

  adminLogin: async (credentials: any) => {
    const response = await fetch(`${BASE_URL}/auth/admin-login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
    });
    return handleResponse(response, true);
  },

  admin: {
    getStats: async () => {
      const response = await fetch(`${BASE_URL}/admin/dashboard-data`, {
        headers: getHeaders()
      });
      return handleResponse(response);
    },
    
    getLogs: async () => {
      const response = await fetch(`${BASE_URL}/admin/users-logs`, {
        headers: getHeaders()
      });
      return handleResponse(response);
    },
    
    resetDatabase: async () => {
      const response = await fetch(`${BASE_URL}/admin/database/reset`, {
        method: "DELETE",
        headers: getHeaders()
      });
      return handleResponse(response);
    },

    createQuestion: async (questionData: { text: string; dimension: string; area: string }) => {
      const response = await fetch(`${BASE_URL}/survey/questions`, {
        method: "POST",
        headers: getHeaders(),
        body: JSON.stringify(questionData)
      });
      return handleResponse(response);
    },

    updateQuestion: async (id: number, questionData: { text: string; dimension: string; area: string }) => {
      const response = await fetch(`${BASE_URL}/admin/survey/questions/${id}`, {
        method: "PUT", // O PATCH, según use tu controlador de Spring Boot
        headers: getHeaders(),
        body: JSON.stringify(questionData)
      });
      return handleResponse(response);
    },

    updateSurveyName: async (newName: string) => {
      const response = await fetch(`${BASE_URL}/admin/survey/rename`, {
        method: "PUT",
        headers: getHeaders(),
        body: JSON.stringify({ name: newName })
      });
      return handleResponse(response);
    },

    deleteUser: async (userId: number) => {
      const response = await fetch(`${BASE_URL}/admin/user/${userId}`, {
        method: "DELETE",
        headers: getHeaders()
      });
      return handleResponse(response);
    },

    deleteUserData: async (userId: number) => {
      const response = await fetch(`${BASE_URL}/admin/user/${userId}/data`, {
        method: "DELETE",
        headers: getHeaders()
      });
      return handleResponse(response);
    },

    getUserDetails: async (userId: number) => {
      const response = await fetch(`${BASE_URL}/admin/user/${userId}/details`, {
        headers: getHeaders()
      });
      return handleResponse(response);
    },
  }
  
};