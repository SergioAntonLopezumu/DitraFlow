// components/BottomNav.tsx
import styles from "../styles/BottomNav.module.css";

interface BottomNavProps {
  activeView: string;
  changeView: (view: string) => void;
}

export default function BottomNav({ activeView, changeView }: BottomNavProps) {
  const items = [
    { key: "dashboard", icon: "📊", label: "Dashboard" },
    { key: "survey", icon: "📝", label: "Encuesta" },
    { key: "settings", icon: "⚙️", label: "Ajustes" },
  ];

  return (
    <div className={styles.bottomNav}>
      {items.map((item) => (
        <div 
          key={item.key}
          className={`${styles.navItem} ${activeView === item.key ? styles.active : ""}`}
          onClick={() => changeView(item.key)}
        >
          <span className={styles.icon}>{item.icon}</span>
          <span>{item.label}</span>
        </div>
      ))}
    </div>
  );
}