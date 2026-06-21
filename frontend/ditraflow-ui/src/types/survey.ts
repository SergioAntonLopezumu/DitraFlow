export interface Question {
  id: number;
  text: string;
  dimension: 'STRATEGY' | 'PROCESSES' | 'TECHNOLOGY' | 'CULTURE' | 'SKILLS';
  area: string;
  weight: number;
  impactScore: number;
  questionOrder: number;
}

export interface Answer {
  questionId: number;
  value: number;
  comment?: string;
}

export interface SurveyResponse {
  surveyId: number;
  answers: Answer[];
}

export interface Result {
  id: number;
  score: number;
  strategyScore: number;
  processesScore: number;
  technologyScore: number;
  cultureScore: number;
  skillsScore: number;
  digitalMaturityLevel: string;
  diagnosticAnalysis: string;
  prioritizedGaps: string;
}

export interface Roadmap {
  id: number;
  resultId: number;
  stepsDescription: string;
  estimatedDurationMonths: number;
  prioritizedAreas: string;
  recommendations: string;
}

export interface ChatMessage {
  id: number;
  userMessage: string;
  botResponse: string;
  timestamp: string;
}
