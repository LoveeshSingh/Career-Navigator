import React, { useState } from 'react';
import { generateRoadmap } from './services/api';
import Navbar from './components/Navbar';
import RoadmapForm from './components/RoadmapForm';
import RoadmapResult from './components/RoadmapResult';
import FallbackResult from './components/FallbackResult';
import {
  Search,
  Loader2,
  Sparkles
} from 'lucide-react';

function App() {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    jdText: '',
    resumeText: '',
    roleId: null, // UUID from database
    role: '',     // Display name/role title
    level: 'intermediate',
    hoursPerWeek: 15,
    topK: 5
  });
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const data = await generateRoadmap(formData);
      setResult(data);
    } catch (err) {
      setError('Failed to generate roadmap. Please check your backend connection and API configuration.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen app-container text-slate-100 font-inter">
      {/* Background Decorative Elements */}
      <div className="blob blob-1"></div>
      <div className="blob blob-2"></div>

      <Navbar />

      <main className="max-w-7xl mx-auto px-6 pt-32 pb-20 relative z-10 flex flex-col items-center">
        <div className={`w-full grid ${result ? 'lg:grid-cols-12' : 'max-w-3xl'} gap-12 transition-all duration-700`}>

          <section className={`${result ? 'lg:col-span-5' : 'w-full'} space-y-6 transition-all`}>
            <RoadmapForm
              formData={formData}
              setFormData={setFormData}
              handleSubmit={handleSubmit}
              loading={loading}
            />
            {error && <div className="mt-4 p-4 bg-red-500/20 border border-red-500/50 text-red-200 rounded-xl text-center text-sm">{error}</div>}
          </section>

        {/* Output Display Section */}
        {result || loading ? (
          <section className="lg:col-span-7 min-w-0 space-y-6 animate-in">
            {loading ? (
              <div className="h-full min-h-[500px] flex flex-col items-center justify-center space-y-6 glass rounded-3xl">
                <div className="relative">
                  <div className="w-16 h-16 border-4 border-blue-500/20 border-t-blue-500 rounded-full animate-spin"></div>
                  <Sparkles className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 text-blue-400 animate-pulse" size={24} />
                </div>
                <div className="text-center">
                  <p className="text-xl font-medium">Crafting Your Roadmap</p>
                  <div className="flex gap-1 justify-center mt-2">
                    <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                    <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                    <span className="w-2 h-2 bg-blue-500 rounded-full animate-bounce"></span>
                  </div>
                </div>
              </div>
            ) : (
              result.mode === 'fallback' ? (
                <FallbackResult data={result.data} />
              ) : (
                <RoadmapResult result={result} />
              )
            )}
          </section>
        ) : null}
        </div>
      </main>

      <footer className="max-w-7xl mx-auto px-6 py-10 text-center text-slate-500 text-sm border-t border-slate-800/50 mt-20">
        Powered by Career Navigator Elite Engine
      </footer>
    </div>
  );
}

export default App;
