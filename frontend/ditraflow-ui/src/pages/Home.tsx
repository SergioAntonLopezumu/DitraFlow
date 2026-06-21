import styles from "../styles/Home.module.css";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";

export default function Home() {
  const navigate = useNavigate();

  return (
    <div className={styles.container}>

      <motion.h1
        initial={{ opacity: 0, y: -100 }}
        animate={{ opacity: 1, y: 0 }}
        className={styles.title}
      >
        DitraFlow
      </motion.h1>

      <motion.p
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5 }}
        className={styles.subtitle}
      >
        Plataforma de análisis de madurez digital
      </motion.p>

      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.8 }}
        className={styles.info}
      >
        Universidad de Murcia · Facultad de Informática · 2026<br />
        Sergio Antón López
      </motion.div>

      <div className={styles.actions}>
        <button
          onClick={() => navigate("/login")}
          className={`${styles.button} ${styles.primary}`}
        >
          Iniciar sesión
        </button>

        <button
          onClick={() => navigate("/register")}
          className={`${styles.button} ${styles.secondary}`}
        >
          Registrarse
        </button>
      </div>
    </div>
  );
}