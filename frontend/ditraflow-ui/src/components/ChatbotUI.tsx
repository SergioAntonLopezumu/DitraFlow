import React, { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import styles from '../styles/ChatBot.module.css';
import { apiService } from '../services/api'; // Importamos tu servicio centralizado

interface ChatMessage {
  id?: number;
  userMessage: string;
  botResponse: string;
  timestamp?: string;
}

interface ChatbotUIProps {
  resultId?: number | null;
}

const ChatbotUI: React.FC<ChatbotUIProps> = ({ resultId }) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Cargar historial de mensajes al montar el componente
  useEffect(() => {
    const loadHistory = async () => {
      try {
        setLoading(true);
        const history = await apiService.getChatHistory();
        if (Array.isArray(history) && history.length > 0) {
          // El backend suele devolver del más reciente al más antiguo, invertimos el orden para la UI
          setMessages(history.reverse());
        }
      } catch (err) {
        console.error("Error al recuperar el historial del chat:", err);
        setMessages([]); // Fallback a lista vacía
      } finally {
        setLoading(false);
      }
    };
    loadHistory();
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputValue.trim()) return;

    const currentInput = inputValue;
    setInputValue(''); 
    setLoading(true);
    setError('');

    // Añadimos el mensaje de forma optimista
    const optimisticUserMessage: ChatMessage = {
      id: -1, // ID temporal hasta que el servidor responda
      userMessage: currentInput,
      botResponse: '', // Placeholder mientras espera respuesta
      timestamp: new Date().toISOString()
    };
    setMessages(prev => [...prev, optimisticUserMessage]);

    try {
      const data = await apiService.sendChatMessage(currentInput, resultId || null);
      
      if (!data || typeof data !== 'object') {
        throw new Error('Respuesta inválida del servidor');
      }

      // Actualizamos el último mensaje con el éxito real
      setMessages(prev => {
        const updated = [...prev];
        const lastIdx = updated.length - 1;
        if (lastIdx >= 0) {
          updated[lastIdx].botResponse = data.botResponse || 'No se pudo procesar la respuesta';
          updated[lastIdx].id = data.id;
          updated[lastIdx].timestamp = data.timestamp;
        }
        return updated;
      });

    } catch (err: any) {
      console.error("Error enviando mensaje:", err);
      
      // Extraemos el mensaje real enviado por Spring Boot
      const errorMessage = err.message || 'No se pudo conectar con el asistente. Por favor intenta de nuevo.';
      setError(errorMessage);

      // En lugar de borrar todo el bloque, mutamos el mensaje optimista para mostrar el error
      setMessages(prev => {
        const updated = [...prev];
        const lastIdx = updated.length - 1;
        if (lastIdx >= 0) {
          updated[lastIdx].botResponse = `⚠️ ${errorMessage}`;
        }
        return updated;
      });
      
      setInputValue(currentInput); // Devolvemos el texto al input por si quiere reintentar
    } finally {
      setLoading(false);
    }
  };

  const suggestedQuestions = [
    '¿Cuál es mi nivel de madurez digital actual?',
    '¿Qué recomendaciones me hace para mejorar?',
    '¿Cómo puedo automatizar mis procesos?',
    '¿Qué habilidades necesita mi equipo?',
    '¿Cuál es la inversión estimada para la transformación?'
  ];

  return (
    <div className={styles.chatbotContainer}>
      <div className={styles.header}>
        <div className={styles.headerContent}>
          <div className={styles.icon}>💬</div>
          <div>
            <h2>Asistente de Soporte</h2>
            <p>Preguntas sobre transformación digital</p>
          </div>
        </div>
      </div>

      <div className={styles.messagesContainer}>
        {messages.length === 0 && !loading ? (
          <div className={styles.emptyState}>
            <div className={styles.emptyIcon}>?</div>
            <h3>Bienvenido al Asistente</h3>
            <p>Puedo ayudarte con preguntas sobre tu diagnóstico y plan de transformación digital.</p>
            
            <div className={styles.suggestedQuestions}>
              <p className={styles.suggestLabel}>Preguntas sugeridas:</p>
              {suggestedQuestions.map((question, idx) => (
                <button
                  key={idx}
                  type="button"
                  className={styles.suggestBtn}
                  onClick={() => setInputValue(question)}
                >
                  {question}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className={styles.messages}>
            {messages.map((msg, idx) => (
              <div key={idx} className={styles.messageGroup}>
                <div className={styles.userMessage}>
                  <div className={styles.messageBubble}>
                    {msg.userMessage}
                  </div>
                </div>

                {msg.botResponse && (
                  <div className={styles.botMessage}>
                    <div className={styles.botIcon}>🤖</div>
                    {/* Añadimos una clase o estilos directos para que las listas y párrafos de Markdown se adecúen al diseño */}
                    <div className={`${styles.messageBubble} ${styles.markdownContent}`} style={{ lineHeight: '1.5' }}>
                      <ReactMarkdown>{msg.botResponse}</ReactMarkdown>
                    </div>
                  </div>
                )}
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>
        )}

        {loading && (
          <div className={styles.loadingMessage}>
            <div className={styles.botIcon}>🤖</div>
            <div className={styles.typingIndicator}>
              <span>.</span>
              <span>.</span>
              <span>.</span>
            </div>
          </div>
        )}

        {error && <div className={styles.errorMessage}>Error: {error}</div>}
      </div>

      <form onSubmit={handleSendMessage} className={styles.inputContainer}>
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Escribe tu pregunta aquí..."
          disabled={loading}
          className={styles.input}
        />
        <button
          type="submit"
          disabled={loading || !inputValue.trim()}
          className={styles.sendBtn}
        >
          →
        </button>
      </form>
    </div>
  );
};

export default ChatbotUI;