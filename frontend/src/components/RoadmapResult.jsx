import React from 'react';
import ReactMarkdown from 'react-markdown';
import { BookOpen, Sparkles, Award, CheckCircle2, AlertCircle, Percent } from 'lucide-react';

const RoadmapResult = ({ result }) => {
  if (!result) return null;

  const { matchPercentage, missingSkills, presentSkills } = result;

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-700">
      {/* Skill Profile Match Metric */}
      {(matchPercentage !== undefined) && (
        <div className="glass p-8 rounded-3xl border border-white/10 shadow-2xl bg-gradient-to-br from-blue-500/5 to-transparent">
          <div className="flex flex-col md:flex-row items-center gap-8">
            {/* Percentage Circle/Indicator */}
            <div className="relative flex-shrink-0">
              <svg className="w-24 h-24 transform -rotate-90">
                <circle
                  cx="48"
                  cy="48"
                  r="40"
                  stroke="currentColor"
                  strokeWidth="8"
                  fill="transparent"
                  className="text-slate-800"
                />
                <circle
                  cx="48"
                  cy="48"
                  r="40"
                  stroke="currentColor"
                  strokeWidth="8"
                  fill="transparent"
                  strokeDasharray={2 * Math.PI * 40}
                  strokeDashoffset={2 * Math.PI * 40 * (1 - (matchPercentage / 100))}
                  className="text-blue-500 transition-all duration-1000 ease-out"
                />
              </svg>
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-xl font-black text-white">{Math.round(matchPercentage)}%</span>
                <span className="text-[8px] font-bold text-slate-500 uppercase tracking-widest">Match</span>
              </div>
            </div>

            <div className="flex-grow space-y-4 text-center md:text-left">
              <div>
                <h3 className="text-lg font-bold text-white flex items-center justify-center md:justify-start gap-2">
                  <Percent size={18} className="text-blue-400" />
                  Skill Profile Analysis
                </h3>
                <p className="text-sm text-slate-400 mt-1">
                  We've compared your resume against the target requirements.
                </p>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {presentSkills && presentSkills.length > 0 && (
                  <div className="p-3 bg-green-500/5 border border-green-500/10 rounded-xl">
                    <div className="flex items-center gap-2 mb-2">
                      <CheckCircle2 size={12} className="text-green-400" />
                      <span className="text-[10px] font-black uppercase tracking-widest text-green-400/80">Strengths ({presentSkills.length})</span>
                    </div>
                    <div className="flex flex-wrap mt-2">
                      {presentSkills.map((s, i) => (
                        <span 
                          key={i} 
                          style={{ marginRight: '12px', marginBottom: '8px' }}
                          className="inline-flex items-center px-3 py-1.5 bg-green-500/10 text-green-300 text-[10px] font-medium rounded-lg border border-green-500/20 whitespace-nowrap"
                        >
                          {s}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
                
                {missingSkills && missingSkills.length > 0 && (
                  <div className="p-3 bg-blue-500/5 border border-blue-500/10 rounded-xl">
                    <div className="flex items-center gap-2 mb-2">
                      <AlertCircle size={12} className="text-blue-400" />
                      <span className="text-[10px] font-black uppercase tracking-widest text-blue-400/80">Missing Skills ({missingSkills.length})</span>
                    </div>
                    <div className="flex flex-wrap mt-2">
                      {missingSkills.map((s, i) => (
                        <span 
                          key={i} 
                          style={{ marginRight: '12px', marginBottom: '8px' }}
                          className="inline-flex items-center px-3 py-1.5 bg-blue-500/10 text-blue-300 text-[10px] font-medium rounded-lg border border-blue-500/20 whitespace-nowrap"
                        >
                          {s}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Roadmap Content */}
      <div className="glass p-8 rounded-3xl border border-white/10 shadow-2xl overflow-hidden">
        <div className="flex items-center gap-2 mb-6 border-b border-slate-700 pb-4">
          <BookOpen className="text-blue-400" size={20} />
          <h3 className="text-xl font-semibold">Your Personalized Study Plan</h3>
        </div>
        <div className="prose prose-invert max-w-none roadmap-markdown overflow-x-auto break-words">
          {result?.data?.roadmap_details ? (
            <ReactMarkdown>{result.data.roadmap_details}</ReactMarkdown>
          ) : (
            <div className="text-center py-8">
              <Sparkles className="mx-auto mb-4 text-blue-400" size={32} />
              <p className="text-lg font-medium text-white">
                {result?.message || "Great news! Your profile is already aligned with the required skills."}
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Certifications & Badges */}
      {result.mode !== 'fallback' && result.data && result.data.suggested_certifications && (
        <div className="glass p-8 rounded-3xl border border-white/10 shadow-2xl">
          <div className="flex items-center gap-2 mb-6 border-b border-slate-700 pb-4">
            <Award className="text-indigo-400" size={20} />
            <h3 className="text-xl font-semibold">Expert Certifications</h3>
          </div>
          <ul className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {result.data.suggested_certifications.map((cert, idx) => (
              <li key={idx} className="flex items-start gap-3 p-4 bg-slate-900/50 rounded-2xl border border-slate-700/50 group hover:border-indigo-500/50 transition-all">
                <Award size={18} className="text-indigo-500 mt-1 flex-shrink-0 group-hover:scale-110 transition-transform" />
                <span className="text-sm text-slate-300 group-hover:text-white transition-colors">{cert}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};

export default RoadmapResult;
