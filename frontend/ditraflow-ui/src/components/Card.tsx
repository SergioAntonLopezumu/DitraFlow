import React from "react";

interface CardProps {
  children: React.ReactNode;
  className?: string; // <--- ESTO ES LO QUE EL COMPONENTE NECESITA
}

export default function Card({ children, className = "" }: CardProps) {
  return (
    <div className={`bg-white dark:bg-gray-800 p-6 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 ${className}`}>
      {children}
    </div>
  );
}