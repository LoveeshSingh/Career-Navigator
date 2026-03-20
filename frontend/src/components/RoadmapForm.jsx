import React, { useState, useEffect } from 'react';
import { Sparkles, ChevronRight, Loader2, Info, Building2, Briefcase } from 'lucide-react';
import { getRoles } from '../services/api';

const RoadmapForm = ({ formData, setFormData, handleSubmit, loading }) => {
  const [roles, setRoles] = useState([]);

  useEffect(() => {
    const fetchRoles = async () => {
      try {
        const data = await getRoles();
        setRoles(data);
      } catch (err) {
        console.error("Failed to load roles", err);
      }
    };
    fetchRoles();
  }, []);

  const handleRoleChange = (e) => {
    const roleId = e.target.value;
    const selectedRole = roles.find(r => r.id === roleId);
    setFormData({
      ...formData,
      roleId: roleId || null,
      role: selectedRole ? selectedRole.title : '',
      jdText: '' // Clear JD if role is selected
    });
  };

  const handleJdChange = (e) => {
    setFormData({
      ...formData,
      jdText: e.target.value,
      roleId: null, // Clear Role if JD is entered
      role: ''
    });
  };

  const isFormValid = formData.resumeText && (formData.jdText || formData.roleId);

  return (
    <div className="glass p-8 rounded-3xl border border-white/10 shadow-2xl space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Sparkles className="text-blue-400" size={20} />
          <h2 className="text-xl font-semibold">Generate Your Roadmap</h2>
        </div>
        <div className="px-3 py-1 bg-blue-500/10 border border-blue-500/20 rounded-full text-[10px] text-blue-400 uppercase tracking-tighter font-bold">
          Gemini 2.5 AI
        </div>
      </div>
      
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Step 1: Goal Definition */}
        <div className="space-y-4">
          <div className="flex items-center gap-2 text-sm font-medium text-slate-400 ml-1">
            <Building2 size={16} />
            <span>Target Goal (Choose One)</span>
          </div>

          <div className="space-y-4 p-5 bg-slate-900/40 rounded-2xl border border-slate-800 focus-within:border-blue-500/50 transition-all">
            <div className="space-y-2">
              <label className="text-xs text-slate-500 uppercase tracking-wider font-bold">Paste Job Description</label>
              <textarea 
                placeholder="Paste the target JD here to find exactly what they need..."
                className="w-full bg-slate-900/50 border border-slate-700/50 rounded-xl p-3 min-h-[100px] focus:ring-1 focus:ring-blue-500/30 outline-none transition-all resize-none text-sm"
                value={formData.jdText}
                onChange={handleJdChange}
              />
            </div>

            <div className="relative flex items-center justify-center py-2">
              <div className="absolute w-full h-[1px] bg-slate-800"></div>
              <span className="relative px-4 bg-slate-900 text-[10px] font-black text-slate-600 uppercase tracking-[0.2em]">OR</span>
            </div>

            <div className="space-y-2">
              <label className="text-xs text-slate-500 uppercase tracking-wider font-bold">Select Predefined Role</label>
              <div className="relative">
                <select 
                  className="w-full bg-slate-900/50 border border-slate-700/50 rounded-xl p-3 focus:ring-1 focus:ring-blue-500/30 outline-none transition-all appearance-none text-sm cursor-pointer"
                  value={formData.roleId || ''}
                  onChange={handleRoleChange}
                >
                  <option value="">Select a career path...</option>
                  {roles.map(role => (
                    <option key={role.id} value={role.id}>{role.title}</option>
                  ))}
                </select>
                <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">
                  <ChevronRight size={16} className="rotate-90" />
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Step 2: Current Context */}
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-sm font-medium text-slate-400 ml-1">
            <Briefcase size={16} />
            <span>Your Current Profile</span>
          </div>
          <textarea 
            required
            placeholder="Paste your resume or list your current skills..."
            className="w-full bg-slate-900/40 border border-slate-700/50 rounded-2xl p-4 min-h-[100px] focus:ring-2 focus:ring-blue-500/50 outline-none transition-all resize-none text-sm"
            value={formData.resumeText}
            onChange={(e) => setFormData({...formData, resumeText: e.target.value})}
          />
        </div>

        {/* Options Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest ml-1">Intensity Level</label>
            <select 
              className="w-full bg-slate-900/40 border border-slate-700/50 rounded-xl p-3 focus:ring-1 focus:ring-blue-500/30 outline-none transition-all text-sm"
              value={formData.level}
              onChange={(e) => setFormData({...formData, level: e.target.value})}
            >
              <option value="beginner">Beginner</option>
              <option value="intermediate">Intermediate</option>
              <option value="advanced">Advanced</option>
            </select>
          </div>
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-500 uppercase tracking-widest ml-1">Commitment (Hrs/Wk)</label>
            <input 
              type="number"
              min="1"
              max="168"
              className="w-full bg-slate-900/40 border border-slate-700/50 rounded-xl p-3 focus:ring-1 focus:ring-blue-500/30 outline-none transition-all text-sm"
              value={formData.hoursPerWeek}
              onChange={(e) => setFormData({...formData, hoursPerWeek: Math.max(1, parseInt(e.target.value) || 1)})}
            />
          </div>
        </div>

        {/* Slider */}
        <div className="space-y-3 p-5 bg-blue-500/5 rounded-2xl border border-blue-500/10">
          <div className="flex justify-between items-center text-sm">
            <label className="font-bold text-slate-400 text-xs uppercase tracking-widest">Skill Precision (Top K)</label>
            <span className="bg-blue-500/20 text-blue-400 px-3 py-0.5 rounded-full font-black text-sm">{formData.topK}</span>
          </div>
          <input 
            type="range"
            min="1"
            max="15"
            step="1"
            className="w-full h-1.5 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-blue-500"
            value={formData.topK}
            onChange={(e) => setFormData({...formData, topK: parseInt(e.target.value)})}
          />
          <div className="flex justify-between text-[10px] text-slate-500 font-bold uppercase tracking-[0.2em] px-1">
            <span>Essential</span>
            <span>Expertise</span>
          </div>
        </div>

        <button 
          type="submit"
          disabled={loading || !isFormValid}
          className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-black py-4 rounded-2xl shadow-2xl shadow-blue-900/40 flex items-center justify-center gap-3 transition-all active:scale-[0.98] disabled:opacity-30 disabled:grayscale disabled:cursor-not-allowed group"
        >
          {loading ? <Loader2 className="animate-spin" /> : <ChevronRight className="group-hover:translate-x-1 transition-transform" size={20} />}
          <span className="tracking-tight">{loading ? 'Synthesizing with Gemini...' : 'Forge Your Roadmap'}</span>
        </button>
      </form>
    </div>
  );
};

export default RoadmapForm;
