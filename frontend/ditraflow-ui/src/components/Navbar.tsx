export default function Navbar() {
  return (
    <nav className="bg-slate-800 p-4 shadow-lg border-b border-slate-700">
      <div className="container mx-auto flex justify-between items-center">
        <h1 className="text-xl font-bold text-blue-400">DitraFlow</h1>
        <button 
          onClick={() => { localStorage.clear(); window.location.href = "/login"; }}
          className="text-sm bg-red-900 px-3 py-1 rounded hover:bg-red-800"
        >
          Cerrar sesión
        </button>
      </div>
    </nav>
  );
}