import type { ReactNode } from "react";
import { motion } from "framer-motion";
import styles from "../styles/AuthLayout.module.css";

interface AuthLayoutProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
}

export default function AuthLayout({ title, subtitle, children }: AuthLayoutProps) {
  return (
    <div className={styles.authContainer}>
      {/* Panel Izquierdo (Desktop) */}
      <div className={`${styles.leftPanel} flex flex-col justify-center items-center p-12 text-center`}>
        <div className="max-w-md flex flex-col items-center">
          
          <motion.img 
            src="/ditraflowicon.png" 
            alt="DitraFlow Logo" 
            className="w-32 h-auto mb-8 object-contain filter drop-shadow-[0_0_15px_rgba(139,92,246,0.3)]"
            initial={{ opacity: 0, scale: 0.8, y: -20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            transition={{ 
              duration: 0.6, 
              ease: "easeOut" 
            }}
          />

          <motion.h2 
            className={`${styles.leftTitle} mb-3`}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2, duration: 0.5 }}
          >
            Bienvenido a DitraFlow
          </motion.h2>

          <motion.p 
            className={styles.leftSubtitle}
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4, duration: 0.5 }}
          >
            Su aliado estratégico en procesos de digitalización.
          </motion.p>
          
        </div>
      </div>

      {/* Panel Derecho (Formularios) */}
      <div className={styles.authContent}>
        <h1 className={styles.title}>{title}</h1>
        {subtitle && <p className={styles.subtitle}>{subtitle}</p>}
        <div className={styles.childrenWrapper}>{children}</div>
      </div>
    </div>
  );
}