import React from 'react';
import { Cpu } from 'lucide-react';

const Navbar = () => {
  return (
    <header className="fixed top-0 w-full z-50 glass border-b border-slate-700/50">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-blue-600 rounded-lg shadow-lg shadow-blue-500/20">
            <Cpu className="text-white" size={24} />
          </div>
          <h1 className="text-xl font-outfit font-bold bg-gradient-to-r from-white to-slate-400 bg-clip-text text-transparent">
            Career Navigator <span className="text-blue-500 text-sm font-medium">AI</span>
          </h1>
        </div>
        <div className="text-sm text-slate-400 hidden sm:block">
          Elite Roadmap Engine
        </div>
      </div>
    </header>
  );
};

export default Navbar;
