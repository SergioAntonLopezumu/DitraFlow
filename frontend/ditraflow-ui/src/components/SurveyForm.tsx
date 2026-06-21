import React, { useState, useEffect } from 'react';
import type { Question } from '../types/survey';
import styles from '../styles/SurveyForm.module.css';

interface SurveyFormProps {
  onSubmit: (answers: Answer[]) => void;
  loading?: boolean;
}

interface Answer {
  questionId: number;
  value: number;
  comment?: string;
}

interface GroupedQuestions {
  [key: string]: Question[];
}

const SurveyForm: React.FC<SurveyFormProps> = ({ onSubmit, loading = false }) => {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [answers, setAnswers] = useState<Map<number, Answer>>(new Map());
  const [currentDimension, setCurrentDimension] = useState<string>('STRATEGY');
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string>('');

  const dimensions = ['STRATEGY', 'PROCESSES', 'TECHNOLOGY', 'CULTURE', 'SKILLS'];
  const dimensionLabels: { [key: string]: string } = {
    STRATEGY: '📋 Estrategia Digital',
    PROCESSES: '⚙️ Procesos',
    TECHNOLOGY: '💻 Tecnología',
    CULTURE: '🎯 Cultura Organizacional',
    SKILLS: '👥 Habilidades y Talento'
  };

  useEffect(() => {
    fetchQuestions();
  }, []);

  const fetchQuestions = async () => {
    try {
      const response = await fetch('/api/survey/questions');
      if (!response.ok) throw new Error('Error al cargar preguntas');
      const data = await response.json();
      setQuestions(data);
      updateProgress();
    } catch (err) {
      setError('No se pudieron cargar las preguntas');
    }
  };

  const updateProgress = () => {
    const total = questions.length;
    const answered = answers.size;
    setProgress(total > 0 ? Math.round((answered / total) * 100) : 0);
  };

  const handleAnswerChange = (questionId: number, value: number) => {
    const newAnswers = new Map(answers);
    newAnswers.set(questionId, {
      questionId,
      value,
      comment: newAnswers.get(questionId)?.comment || ''
    });
    setAnswers(newAnswers);
    updateProgress();
  };

  const handleCommentChange = (questionId: number, comment: string) => {
    const newAnswers = new Map(answers);
    const existing = newAnswers.get(questionId) || { questionId, value: 0 };
    existing.comment = comment;
    newAnswers.set(questionId, existing);
    setAnswers(newAnswers);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (answers.size === 0) {
      setError('Por favor, responde al menos una pregunta');
      return;
    }

    const answersArray = Array.from(answers.values());
    onSubmit(answersArray);
  };

  const groupedQuestions = questions.reduce((acc: GroupedQuestions, q) => {
    if (!acc[q.dimension]) acc[q.dimension] = [];
    acc[q.dimension].push(q);
    return acc;
  }, {});

  const dimensionQuestions = groupedQuestions[currentDimension] || [];
  const scaleLabels = ['1 - Completamente en desacuerdo', '2 - En desacuerdo', '3 - Neutral', '4 - De acuerdo', '5 - Completamente de acuerdo'];

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>Encuesta de Madurez Digital Multidimensional</h1>
        <p>Evaluación integral del estado actual de transformación digital</p>
      </div>

      <div className={styles.progressBar}>
        <div className={styles.progress} style={{ width: `${progress}%` }}>
          {progress}%
        </div>
      </div>

      {error && <div className={styles.error}>{error}</div>}

      <form onSubmit={handleSubmit}>
        {/* Pestañas de dimensiones */}
        <div className={styles.dimensionTabs}>
          {dimensions.map((dim) => (
            <button
              key={dim}
              type="button"
              className={`${styles.tab} ${currentDimension === dim ? styles.active : ''}`}
              onClick={() => setCurrentDimension(dim)}
            >
              {dimensionLabels[dim]}
            </button>
          ))}
        </div>

        {/* Preguntas de la dimensión actual */}
        <div className={styles.questionsContainer}>
          <h2>{dimensionLabels[currentDimension]}</h2>
          <p className={styles.dimensionDesc}>
            {getDimensionDescription(currentDimension)}
          </p>

          {dimensionQuestions.map((question) => {
            const answer = answers.get(question.id);
            return (
              <div key={question.id} className={styles.questionCard}>
                <div className={styles.questionHeader}>
                  <label className={styles.questionText}>
                    {question.text}
                  </label>
                  {question.area && (
                    <span className={styles.area}>{question.area}</span>
                  )}
                </div>

                {/* Escala Likert */}
                <div className={styles.scale}>
                  {[1, 2, 3, 4, 5].map((value) => (
                    <label key={value} className={styles.scaleOption}>
                      <input
                        type="radio"
                        name={`question-${question.id}`}
                        value={value}
                        checked={answer?.value === value}
                        onChange={() => handleAnswerChange(question.id, value)}
                        disabled={loading}
                      />
                      <span className={styles.scaleLabel}>{value}</span>
                    </label>
                  ))}
                </div>

                {answer?.value && (
                  <p className={styles.scaleText}>{scaleLabels[answer.value - 1]}</p>
                )}

                {/* Campo comentario opcional */}
                <div className={styles.commentContainer}>
                  <textarea
                    placeholder="Comentario opcional..."
                    value={answer?.comment || ''}
                    onChange={(e) => handleCommentChange(question.id, e.target.value)}
                    disabled={loading}
                    className={styles.comment}
                  />
                </div>
              </div>
            );
          })}
        </div>

        {/* Botón enviar */}
        <div className={styles.buttonContainer}>
          <button
            type="submit"
            className={styles.submitBtn}
            disabled={loading || answers.size === 0}
          >
            {loading ? 'Procesando...' : 'Enviar Encuesta'}
          </button>
        </div>
      </form>
    </div>
  );
};

function getDimensionDescription(dimension: string): string {
  const descriptions: { [key: string]: string } = {
    STRATEGY: 'Evalúa la alineación estratégica y la visión digital de la empresa.',
    PROCESSES: 'Analiza la automatización y optimización de procesos de negocio.',
    TECHNOLOGY: 'Valora la modernidad de infraestructura y seguridad digital.',
    CULTURE: 'Mide la disposición organizacional al cambio y la innovación.',
    SKILLS: 'Evalúa las competencias digitales del equipo y capacidades.'
  };
  return descriptions[dimension] || '';
}

export default SurveyForm;
