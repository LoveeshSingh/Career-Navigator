import React from 'react';
import { BookOpen, ExternalLink } from 'lucide-react';

const FallbackResult = ({ data }) => {
  return (
    <div className="glass p-8 rounded-3xl border border-white/10 shadow-2xl animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="flex items-center gap-2 mb-6 border-b border-slate-700 pb-4">
        <BookOpen className="text-blue-400" size={20} />
        <h3 className="text-xl font-semibold">Your Study Plan</h3>
      </div>
      <div className="space-y-6">
        <div className="p-4 bg-orange-500/10 border border-orange-500/30 text-orange-200 rounded-xl text-sm mb-4">
          <strong>Note:</strong> Gemini was unavailable. I've generated a high-quality fallback roadmap for you.
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {data && data.length > 0 ? (
            data.map((item, idx) => (
              <div key={idx} className="p-4 bg-slate-900/50 border border-slate-700 rounded-2xl flex flex-col justify-between">
                <span className="font-bold text-lg mb-2 text-white">{item.skill}</span>
                <a 
                  href={item.video} 
                  target="_blank" 
                  rel="noreferrer"
                  className="inline-flex items-center gap-2 text-sm text-blue-400 hover:text-blue-300 transition-colors"
                >
                  <ExternalLink size={14} /> Watch Expert Tutorial
                </a>
              </div>
            ))
          ) : (
            <p className="text-center text-slate-400">No fallback skills identified.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default FallbackResult;
