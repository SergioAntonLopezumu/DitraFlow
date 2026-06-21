import React from "react";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  hasError?: boolean; // Añadimos la propiedad para controlar el error
}

export default function Input({ hasError, className = "", ...props }: InputProps) {
  // Combinamos tus estilos por defecto con un borde rojo si hay un error
  const errorStyles = hasError 
    ? { border: "2px solid #ef4444", backgroundColor: "#fef2f2" } 
    : {};

  return (
    <input
      {...props}
      style={{
        width: "100%",
        padding: "0.75rem",
        borderRadius: "0.375rem",
        border: "1px solid #d1d5db",
        outline: "none",
        fontSize: "0.95rem",
        transition: "all 0.2s ease-in-out",
        ...errorStyles, // Aplica el rojo si toca
        ...props.style
      }}
      className={className}
    />
  );
}